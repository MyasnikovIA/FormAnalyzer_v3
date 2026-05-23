// core/service/FormAnalyzerService.java
package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.FormCache;
import ru.tmis.analyzer.core.cache.InMemoryReportBuffer;
import ru.tmis.analyzer.core.extractor.ExtractorManager;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;
import ru.tmis.analyzer.core.report.CSVReportGenerator;
import ru.tmis.analyzer.core.report.JSONReportGenerator;
import ru.tmis.analyzer.core.report.ReportGenerator;
import ru.tmis.analyzer.utils.CommentRemover;
import ru.tmis.analyzer.utils.FormPathUtils;
import ru.tmis.analyzer.core.llm.LLMPromptGenerator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FormAnalyzerService {

    private final SettingsModel settings;
    private final AppConfig config;
    private final FileScannerService scannerService;
    private final UserFormsResolver userFormsResolver;
    private final ExtractorManager extractorManager;
    private Set<String> formsToAnalyze;
    private final boolean useMemoryCache;

    private final Set<String> existingReportsCache = ConcurrentHashMap.newKeySet();
    private final BlockingQueue<FormInfo> reportQueue = new LinkedBlockingQueue<>();
    private ExecutorService reportWriterExecutor;
    private volatile boolean writerRunning = true;

    private ExecutorService executor;
    private final Object reportLock = new Object();

    // Генераторы для CSV и JSON
    private final CSVReportGenerator csvGenerator;
    private final JSONReportGenerator jsonGenerator;

    public interface FormAnalyzedCallback {
        void onFormAnalyzed(FormInfo formInfo);
    }
    private FormAnalyzedCallback formAnalyzedCallback;
    private final Map<String, ViewTableDependencies> viewDependenciesCache = new ConcurrentHashMap<>();

    private BooleanSupplier stopCondition = () -> false;
    private ProgressCallback progressCallback;
    private ILogger logger;

    private BooleanSupplier pausedCondition = () -> false;
    private final Object pauseLock = new Object();

    public interface ProgressCallback {
        void onProgress(int processed, int total, String currentForm);
    }

    public FormAnalyzerService(SettingsModel settings, AppConfig config) {
        this.settings = settings;
        this.config = config;
        this.scannerService = new FileScannerService(settings.getProjectPath());
        this.userFormsResolver = new UserFormsResolver(scannerService);
        this.extractorManager = new ExtractorManager(settings);
        this.useMemoryCache = config != null && config.isUseMemoryCache();

        // Инициализация генераторов
        this.csvGenerator = new CSVReportGenerator(settings.getOutputDir());
        this.jsonGenerator = new JSONReportGenerator(settings.getOutputDir(), config);

        FormCache.setEnabled(useMemoryCache);

        // Запускаем фоновый поток для записи отчётов (только для режима диска)
        if (!useMemoryCache) {
            startReportWriter();
        }
    }

    private void startReportWriter() {
        reportWriterExecutor = Executors.newSingleThreadExecutor();
        reportWriterExecutor.submit(() -> {
            while (writerRunning) {
                try {
                    FormInfo formInfo = reportQueue.poll(500, TimeUnit.MILLISECONDS);
                    if (formInfo != null) {
                        ReportGenerator reportGen = new ReportGenerator(settings.getOutputDir(), config);
                        reportGen.createMainReportHeader();
                        reportGen.appendFormToMainReport(formInfo);

                        // CSV и JSON также записываем
                        csvGenerator.appendFormToCSVBatch(formInfo);
                        if (config != null && config.isEnableJSONExport()) {
                            jsonGenerator.appendFormToJSON(formInfo);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Ошибка записи отчёта: " + e.getMessage());
                }
            }
        });
    }

    private boolean hasReportFast(String formPath) {
        String safeName = getSafeFileName(formPath);
        if (existingReportsCache.contains(safeName)) {
            return true;
        }

        String outputDir = settings.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }
        Path reportPath = Paths.get(outputDir, "Forms", safeName);

        if (Files.exists(reportPath)) {
            existingReportsCache.add(safeName);
            return true;
        }
        return false;
    }

    public FormAnalyzerService(SettingsModel settings) {
        this(settings, AppConfig.load());
    }

    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

    private void log(String message) {
        if (logger != null) {
            logger.log(message);
        }
        System.out.println(message);
    }

    private void error(String message) {
        if (logger != null) {
            logger.error(message);
        }
        System.err.println(message);
    }

    public void setStopCondition(BooleanSupplier condition) {
        this.stopCondition = condition;
    }

    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }

    public List<FormInfo> analyzeAllForms(int parallelThreads) throws Exception {
        Set<String> formsToAnalyzeList = getFormsToAnalyze();
        if (formsToAnalyzeList == null || formsToAnalyzeList.isEmpty()) {
            return new ArrayList<>();
        }

        if (executor == null || executor.isShutdown()) {
            int threads = parallelThreads > 0 ? parallelThreads : Runtime.getRuntime().availableProcessors();
            executor = Executors.newFixedThreadPool(threads);
            System.out.println("Параллельный анализ запущен с " + threads + " потоками");
        }

        List<FormInfo> results = Collections.synchronizedList(new ArrayList<>());
        List<Future<FormInfo>> futures = new ArrayList<>();
        AtomicInteger processed = new AtomicInteger(0);
        int total = formsToAnalyzeList.size();

        for (String formPath : formsToAnalyzeList) {
            checkPause();
            if (stopCondition.getAsBoolean()) break;

            futures.add(executor.submit(() -> {
                if (stopCondition.getAsBoolean()) return null;

                int current = processed.incrementAndGet();
                if (progressCallback != null) {
                    progressCallback.onProgress(current, total, formPath);
                }

                System.out.print("Анализ [" + current + "/" + total + "]: " + formPath + " ... ");

                try {
                    FormInfo formInfo = analyzeForm(formPath);
                    if (formInfo != null) {
                        results.add(formInfo);
                        if (formAnalyzedCallback != null) {
                            synchronized (reportLock) {
                                formAnalyzedCallback.onFormAnalyzed(formInfo);
                            }
                        }
                        System.out.println("OK (SQL: " + formInfo.getSqlQueries().size() + ")");
                        return formInfo;
                    } else {
                        System.out.println("ПРОПУЩЕН");
                        return null;
                    }
                } catch (Exception e) {
                    System.err.println("ОШИБКА: " + e.getMessage());
                    return null;
                }
            }));
        }

        for (Future<FormInfo> future : futures) {
            try {
                future.get(60, TimeUnit.MINUTES);
            } catch (TimeoutException e) {
                System.err.println("Таймаут при анализе формы");
                future.cancel(true);
            } catch (Exception e) {
                System.err.println("Ошибка при получении результата: " + e.getMessage());
            }
        }

        return results;
    }

    public List<FormInfo> analyzeAllForms() throws Exception {
        return analyzeAllForms(Runtime.getRuntime().availableProcessors());
    }

    public void reset() {
        if (formsToAnalyze != null) {
            formsToAnalyze.clear();
            formsToAnalyze = null;
        }
        viewDependenciesCache.clear();
    }

    /**
     * Сохранение отчёта с учётом режима работы (RAM или DISK)
     */
    private void saveReport(FormInfo formInfo) {
        if (useMemoryCache) {
            // ========== РЕЖИМ RAM: всё в буфер ==========
            String txtContent = generateTxtContent(formInfo);
            InMemoryReportBuffer.addTxtReport(formInfo.getFormPath(), txtContent);

            // CSV в буфер
            csvGenerator.appendFormToCSVBatch(formInfo);

            // JSON в буфер
            if (config != null && config.isEnableJSONExport()) {
                try {
                    jsonGenerator.appendFormToJSON(formInfo);
                } catch (IOException e) {
                    error("Ошибка JSON: " + e.getMessage());
                }
            }

            // MD в буфер
            if (config != null && config.isEnableLLMExport()) {
                String mdContent = generateMdContent(formInfo);
                InMemoryReportBuffer.addMdPrompt(formInfo.getFormPath(), mdContent);
            }
        } else {
            // ========== РЕЖИМ DISK: асинхронная запись ==========
            queueFormForReport(formInfo);
            csvGenerator.appendFormToCSVBatch(formInfo);
            if (config != null && config.isEnableJSONExport()) {
                try {
                    jsonGenerator.appendFormToJSON(formInfo);
                } catch (IOException e) {
                    error("Ошибка JSON: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Генерация TXT содержимого для буфера (режим RAM)
     */
    private String generateTxtContent(FormInfo formInfo) {
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);

        writer.println("-".repeat(100));
        writer.println("ФОРМА: " + formInfo.getFormPath());
        writer.println("-".repeat(100));
        writer.println("Базовая форма: " + formInfo.getBaseFormPath());
        if (formInfo.isFullyReplaced()) {
            writer.println("СТАТУС: ПОЛНОСТЬЮ ЗАМЕНЕНА");
            writer.println("Файл замены: " + formInfo.getReplacementPath());
        } else if (!formInfo.getOverrides().isEmpty()) {
            writer.println("СТАТУС: ЧАСТИЧНО ПЕРЕОПРЕДЕЛЕНА");
        } else {
            writer.println("СТАТУС: БАЗОВАЯ ФОРМА");
        }
        writer.println();

        writer.flush();
        return sw.toString();
    }

    /**
     * Генерация MD содержимого для буфера (режим RAM)
     */
    private String generateMdContent(FormInfo formInfo) {
        try {
            LLMPromptGenerator llmGen = new LLMPromptGenerator(config);
            return llmGen.generateForSingleForm(formInfo, settings.getOutputDir());
        } catch (Exception e) {
            error("Ошибка генерации MD: " + e.getMessage());
            return "";
        }
    }

    /**
     * Анализ одной формы
     */
    public FormInfo analyzeForm(String formPath, boolean skipCacheCheck) {
        String normalizedPath = FormPathUtils.normalizeFormPath(formPath);

        System.out.println("  Проверка формы: " + normalizedPath);

        if (!scannerService.baseFormExists(normalizedPath)) {
            System.err.println("Базовая форма не найдена: " + normalizedPath);
            return null;
        }

        if (!skipCacheCheck) {
            Path reportPath = getReportPath(normalizedPath);
            if (Files.exists(reportPath)) {
                log("Отчет уже существует: " + reportPath.toString());
                log("  Форма пропущена (отчет построен ранее)");
                return null;
            }
        }

        FormInfo formInfo = userFormsResolver.resolveOverrides(normalizedPath);

        Path baseFormPathObj = scannerService.getBaseFormPath(normalizedPath);
        formInfo.setBaseFormPath(baseFormPathObj.toString());

        String baseContent;
        if (useMemoryCache) {
            baseContent = FormCache.getFormContent(normalizedPath);
            if (baseContent == null) {
                System.err.println("Форма не найдена в кэше: " + normalizedPath);
                System.err.println("Возможно, не была выполнена предзагрузка форм.");
                return null;
            }
        } else {
            baseContent = scannerService.readFileContent(baseFormPathObj);
            if (baseContent == null) {
                System.err.println("Не удалось прочитать содержимое формы: " + baseFormPathObj);
                return null;
            }
        }

        String contentWithoutComments = CommentRemover.removeAllComments(baseContent);
        extractorManager.process(contentWithoutComments, formInfo);
        extractAutoPopupMenus(contentWithoutComments, formInfo);

        Set<String> viewNames = new LinkedHashSet<>();
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_V_")) {
                viewNames.add(tv);
            }
        }

        if (!viewNames.isEmpty()) {
            Map<String, ViewTableDependencies> viewDeps = loadViewDependencies(viewNames);
            formInfo.setViewDependencies(viewDeps);
            log("  Сохранено зависимостей вьюх: " + viewDeps.size() + " шт.");

            for (Map.Entry<String, ViewTableDependencies> entry : viewDeps.entrySet()) {
                log("    Вьюха " + entry.getKey() + " содержит " + entry.getValue().getOracleTables().size() + " таблиц");
            }
        }

        // ========== СОХРАНЯЕМ ОТЧЁТ ==========
        saveReport(formInfo);
        // ====================================

        return formInfo;
    }

    public FormInfo analyzeForm(String formPath) {
        return analyzeForm(formPath, false);
    }

    private Path getReportPath(String formPath) {
        String outputDir = settings.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }
        String safeFileName = getSafeFileName(formPath);
        return Paths.get(outputDir, "Forms", safeFileName);
    }

    private String getSafeFileName(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized.replace("/", "#").replace("\\", "#") + ".txt";
    }

    public void queueFormForReport(FormInfo formInfo) {
        if (formInfo != null) {
            reportQueue.offer(formInfo);
        }
    }

    private Set<String> getFormsToAnalyze() throws IOException {
        if (formsToAnalyze != null && !formsToAnalyze.isEmpty()) {
            System.out.println("Используем прямой список форм (" + formsToAnalyze.size() + " шт.)");
            return formsToAnalyze;
        }

        Set<String> forms = new LinkedHashSet<>();
        Path listFile = Paths.get("forms_list.txt");

        if (Files.exists(listFile)) {
            List<String> lines = Files.readAllLines(listFile);
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    String normalized = FormPathUtils.normalizeFormPath(trimmed);
                    forms.add(normalized);
                }
            }
            if (!forms.isEmpty()) {
                return forms;
            }
        }

        FileScannerService scanner = new FileScannerService(settings.getProjectPath());
        Set<String> allForms = scanner.findAllForms();
        System.out.println("Найдено форм при сканировании: " + allForms.size());
        return allForms;
    }

    private Set<String> scanAllForms() throws IOException {
        return new FileScannerService(settings.getProjectPath()).findAllForms();
    }

    private Map<String, ViewTableDependencies> loadViewDependencies(Set<String> viewNames) {
        if (viewNames.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, ViewTableDependencies> result = new LinkedHashMap<>();
        ViewDependencyAnalyzer analyzer = new ViewDependencyAnalyzer(settings);

        System.out.println("  Загрузка зависимостей для " + viewNames.size() + " вьюх...");

        int count = 0;
        for (String viewName : viewNames) {
            if (stopCondition.getAsBoolean()) {
                break;
            }
            count++;

            if (ViewDependencyAnalyzer.isInCache(viewName)) {
                System.out.println("    [" + count + "/" + viewNames.size() + "] " + viewName + " (из кэша)");
                ViewTableDependencies deps = analyzer.analyzeViewPublic(viewName);
                result.put(viewName, deps);
                continue;
            }

            if (viewDependenciesCache.containsKey(viewName)) {
                System.out.println("    [" + count + "/" + viewNames.size() + "] " + viewName + " (из локального кэша)");
                result.put(viewName, viewDependenciesCache.get(viewName));
                continue;
            }

            System.out.print("    Анализ вьюхи [" + count + "/" + viewNames.size() + "]: " + viewName + " ... ");

            try {
                ViewTableDependencies deps = analyzer.analyzeView(viewName);
                result.put(viewName, deps);
                viewDependenciesCache.put(viewName, deps);
                System.out.println("OK (таблиц: " + deps.getOracleTables().size() + ")");
            } catch (Exception e) {
                System.err.println("ОШИБКА: " + e.getMessage());
                ViewTableDependencies errorDeps = new ViewTableDependencies(viewName);
                errorDeps.setExistsInOracle(false);
                errorDeps.setOracleError(e.getMessage());
                result.put(viewName, errorDeps);
                viewDependenciesCache.put(viewName, errorDeps);
            }
        }

        return result;
    }

    public void setFormAnalyzedCallback(FormAnalyzedCallback callback) {
        this.formAnalyzedCallback = callback;
    }

    private void extractAutoPopupMenus(String content, FormInfo formInfo) {
        if (content == null || content.isEmpty()) return;

        Set<String> foundUnits = new LinkedHashSet<>();

        Pattern d3Pattern = Pattern.compile(
                "<cmpAutoPopupMenu\\s+[^>]*?\\bunit\\s*=\\s*['\"]([^'\"]+)['\"][^>]*/?>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher d3Matcher = d3Pattern.matcher(content);
        while (d3Matcher.find()) {
            String unit = d3Matcher.group(1);
            if (unit != null && !unit.isEmpty()) {
                foundUnits.add(unit);
                System.out.println("  [AutoPopupMenu] D3: unit=" + unit);
            }
        }

        Pattern m2Pattern = Pattern.compile(
                "<component\\s+[^>]*?cmptype\\s*=\\s*['\"]AutoPopupMenu['\"][^>]*?\\bunit\\s*=\\s*['\"]([^'\"]+)['\"][^>]*/?>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher m2Matcher = m2Pattern.matcher(content);
        while (m2Matcher.find()) {
            String unit = m2Matcher.group(1);
            if (unit != null && !unit.isEmpty()) {
                foundUnits.add(unit);
                System.out.println("  [AutoPopupMenu] M2: unit=" + unit);
            }
        }

        Pattern m2PatternReverse = Pattern.compile(
                "<component\\s+[^>]*?\\bunit\\s*=\\s*['\"]([^'\"]+)['\"][^>]*?cmptype\\s*=\\s*['\"]AutoPopupMenu['\"][^>]*/?>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher m2ReverseMatcher = m2PatternReverse.matcher(content);
        while (m2ReverseMatcher.find()) {
            String unit = m2ReverseMatcher.group(1);
            if (unit != null && !unit.isEmpty()) {
                foundUnits.add(unit);
                System.out.println("  [AutoPopupMenu] M2 (reverse): unit=" + unit);
            }
        }

        Pattern d3CompactPattern = Pattern.compile(
                "<cmpAutoPopupMenu\\s+unit\\s*=\\s*['\"]([^'\"]+)['\"]\\s*/?>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher d3CompactMatcher = d3CompactPattern.matcher(content);
        while (d3CompactMatcher.find()) {
            String unit = d3CompactMatcher.group(1);
            if (unit != null && !unit.isEmpty()) {
                foundUnits.add(unit);
                System.out.println("  [AutoPopupMenu] D3 compact: unit=" + unit);
            }
        }

        for (String unit : foundUnits) {
            formInfo.addAutoPopupMenu(unit);
        }

        System.out.println("  [AutoPopupMenu] Всего найдено unit: " + foundUnits.size());
    }

    public void setFormsToAnalyze(Set<String> forms) {
        this.formsToAnalyze = forms;
    }

    public void clearFormsToAnalyze() {
        this.formsToAnalyze = null;
    }

    public List<FormInfo> scanAllFormsAndAnalyze() throws Exception {
        Set<String> allForms = scanAllForms();
        Set<String> formsToProcess = new LinkedHashSet<>();
        String outputDir = settings.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }

        for (String formPath : allForms) {
            String normalized = FormPathUtils.normalizeFormPath(formPath);
            String safeFileName = getSafeFileName(normalized);
            Path reportPath = Paths.get(outputDir, safeFileName);

            if (!Files.exists(reportPath)) {
                formsToProcess.add(formPath);
            } else {
                log("  Отчет уже существует: " + formPath);
            }
        }

        log("Всего найдено форм: " + allForms.size());
        log("Новых форм для анализа: " + formsToProcess.size());

        if (formsToProcess.isEmpty()) {
            log("Нет новых форм для анализа");
            return new ArrayList<>();
        }

        setFormsToAnalyze(formsToProcess);
        return analyzeAllForms();
    }

    public void setParallelThreads(int threads) {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public void shutdown() {
        writerRunning = false;
        if (reportWriterExecutor != null) {
            reportWriterExecutor.shutdown();
            try {
                reportWriterExecutor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                reportWriterExecutor.shutdownNow();
            }
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                executor.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }

        // Сбрасываем буфер CSV
        try {
            csvGenerator.flushCSV();
        } catch (IOException e) {
            error("Ошибка сброса CSV: " + e.getMessage());
        }
    }

    public void setPaused(BooleanSupplier condition) {
        this.pausedCondition = condition;
    }

    private void checkPause() {
        if (pausedCondition != null && pausedCondition.getAsBoolean()) {
            synchronized (pauseLock) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}