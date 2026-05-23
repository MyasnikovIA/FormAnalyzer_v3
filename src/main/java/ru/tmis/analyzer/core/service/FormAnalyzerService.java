// core/service/FormAnalyzerService.java (исправленный)
package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.FormCache;
import ru.tmis.analyzer.core.extractor.ExtractorManager;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;
import ru.tmis.analyzer.core.report.ReportGenerator;
import ru.tmis.analyzer.utils.CommentRemover;
import ru.tmis.analyzer.utils.FormPathUtils;
import ru.tmis.analyzer.core.llm.LLMPromptGenerator;

import java.io.IOException;
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
import java.util.concurrent.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


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
    private final Object reportLock = new Object(); // Для синхронизации записи отчётов

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

        // ========== ВАЖНО: устанавливаем режим кэширования форм ==========
        FormCache.setEnabled(useMemoryCache);
        // =================================================================

        // Запускаем фоновый поток для записи отчётов (только если не используем кэш)
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

    // Быстрая проверка существования отчёта (замена Files.exists)
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

    // Конструктор с одним параметром (загружает config)
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

    // core/service/FormAnalyzerService.java
    public List<FormInfo> analyzeAllForms(int parallelThreads) throws Exception {
        Set<String> formsToAnalyzeList = getFormsToAnalyze();
        if (formsToAnalyzeList == null || formsToAnalyzeList.isEmpty()) {
            return new ArrayList<>();
        }

        // Используем переданное количество потоков
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

        // Собираем результаты
        for (Future<FormInfo> future : futures) {
            try {
                FormInfo form = future.get(60, TimeUnit.MINUTES);
                // результат уже добавлен
            } catch (TimeoutException e) {
                System.err.println("Таймаут при анализе формы");
                future.cancel(true);
            } catch (Exception e) {
                System.err.println("Ошибка при получении результата: " + e.getMessage());
            }
        }

        return results;
    }

    // Перегруженный метод для обратной совместимости
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
     * Анализ одной формы
     * @param formPath путь к форме
     * @param skipCacheCheck пропустить проверку существования отчёта (для рекурсивного анализа)
     * @return информация о форме или null, если форма пропущена
     */
    // core/service/FormAnalyzerService.java
    /**
     * Анализ одной формы
     * @param formPath путь к форме
     * @param skipCacheCheck пропустить проверку существования отчёта (для рекурсивного анализа)
     * @return информация о форме или null, если форма пропущена
     */
    public FormInfo analyzeForm(String formPath, boolean skipCacheCheck) {
        String normalizedPath = FormPathUtils.normalizeFormPath(formPath);

        System.out.println("  Проверка формы: " + normalizedPath);

        if (!scannerService.baseFormExists(normalizedPath)) {
            System.err.println("Базовая форма не найдена: " + normalizedPath);
            return null;
        }

        // ========== ПРОВЕРКА: ЕСЛИ ОТЧЁТ УЖЕ СУЩЕСТВУЕТ ==========
        if (!skipCacheCheck) {
            Path reportPath = getReportPath(normalizedPath);
            if (Files.exists(reportPath)) {
                log("Отчет уже существует: " + reportPath.toString());
                log("  Форма пропущена (отчет построен ранее)");
                return null;
            }
        }
        // =======================================================

        FormInfo formInfo = userFormsResolver.resolveOverrides(normalizedPath);

        Path baseFormPathObj = scannerService.getBaseFormPath(normalizedPath);
        formInfo.setBaseFormPath(baseFormPathObj.toString());

        // ========== ПОЛУЧАЕМ СОДЕРЖИМОЕ ФОРМЫ ==========
        String baseContent;

        if (useMemoryCache) {
            // Режим оперативной памяти: берём из кэша (который загружен заранее)
            baseContent = FormCache.getFormContent(normalizedPath);
            if (baseContent == null) {
                System.err.println("Форма не найдена в кэше: " + normalizedPath);
                System.err.println("Возможно, не была выполнена предзагрузка форм.");
                return null;
            }
        } else {
            // Режим диска: читаем напрямую с диска
            baseContent = scannerService.readFileContent(baseFormPathObj);
            if (baseContent == null) {
                System.err.println("Не удалось прочитать содержимое формы: " + baseFormPathObj);
                return null;
            }
        }
        // ================================================================

        // Удаляем комментарии
        String contentWithoutComments = CommentRemover.removeAllComments(baseContent);

        // Передаём в процессор очищенное содержимое
        extractorManager.process(contentWithoutComments, formInfo);

        // Извлекаем AutoPopupMenu
        extractAutoPopupMenus(contentWithoutComments, formInfo);

        // Собираем все вьюхи, используемые в этой форме
        Set<String> viewNames = new LinkedHashSet<>();
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_V_")) {
                viewNames.add(tv);
            }
        }

        // Загружаем зависимости вьюх
        if (!viewNames.isEmpty()) {
            Map<String, ViewTableDependencies> viewDeps = loadViewDependencies(viewNames);
            formInfo.setViewDependencies(viewDeps);
            log("  Сохранено зависимостей вьюх: " + viewDeps.size() + " шт.");

            for (Map.Entry<String, ViewTableDependencies> entry : viewDeps.entrySet()) {
                log("    Вьюха " + entry.getKey() + " содержит " + entry.getValue().getOracleTables().size() + " таблиц");
            }
        }

        // Генерация LLM промпта для формы (если включено в настройках)
        if (formInfo != null && config != null && config.isEnableLLMExport()) {
            try {
                LLMPromptGenerator llmGen = new LLMPromptGenerator(config);
                String mdFilePath = llmGen.generateForSingleForm(formInfo, settings.getOutputDir());
                log("  LLM промпт сохранен: " + mdFilePath);
            } catch (Exception e) {
                error("  Ошибка сохранения LLM промпта: " + e.getMessage());
            }
        }

        // ========== СОХРАНЕНИЕ ОТЧЁТА В ЗАВИСИМОСТИ ОТ РЕЖИМА ==========
        if (useMemoryCache) {
            // Режим оперативной памяти: добавляем в буфер
            queueFormForReport(formInfo);
        } else {
            // Режим диска: сразу пишем на диск
            try {
                ReportGenerator reportGen = new ReportGenerator(settings.getOutputDir(), config);
                reportGen.createMainReportHeader();
                reportGen.appendFormToMainReport(formInfo);
                log("  Отчёт сохранён на диск: " + formInfo.getFormPath());
            } catch (IOException e) {
                error("  Ошибка сохранения отчёта для " + formInfo.getFormPath() + ": " + e.getMessage());
            }
        }
        // =================================================================

        return formInfo;
    }

    /**
     * Анализ одной формы (с проверкой существования отчёта)
     * @param formPath путь к форме
     * @return информация о форме или null, если форма пропущена
     */
    public FormInfo analyzeForm(String formPath) {
        return analyzeForm(formPath, false);
    }

    /**
     * Получает полный путь к файлу отчёта с учётом подкаталога Forms
     */
    private Path getReportPath(String formPath) {
        String outputDir = settings.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }
        String safeFileName = getSafeFileName(formPath);
        return Paths.get(outputDir, "Forms", safeFileName);
    }

    /**
     * Формирует безопасное имя файла отчёта
     */
    private String getSafeFileName(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized.replace("/", "#").replace("\\", "#") + ".txt";
    }


    // Добавить метод для асинхронного сохранения:
    public void queueFormForReport(FormInfo formInfo) {
        if (formInfo != null) {
            reportQueue.offer(formInfo);
        }
    }

    /**
     * Возвращает список форм для анализа
     * ИСПОЛЬЗУЕТ ТОЛЬКО findAllForms() - единый метод сканирования
     */
    private Set<String> getFormsToAnalyze() throws IOException {
        // Если установлен прямой список (например, из дерева форм), используем его
        if (formsToAnalyze != null && !formsToAnalyze.isEmpty()) {
            System.out.println("Используем прямой список форм (" + formsToAnalyze.size() + " шт.)");
            return formsToAnalyze;
        }

        // ========== ЕДИНЫЙ МЕТОД СКАНИРОВАНИЯ ==========
        FileScannerService scanner = new FileScannerService(settings.getProjectPath());
        Set<String> allForms = scanner.findAllForms();

        System.out.println("Найдено форм при сканировании: " + allForms.size());
        return allForms;
    }

    /**
     * @deprecated Используйте FileScannerService.findAllForms() вместо этого метода
     */
    @Deprecated
    private Set<String> scanAllForms() throws IOException {
        return new FileScannerService(settings.getProjectPath()).findAllForms();
    }

    /**
     * Загрузка зависимостей вьюх (какие таблицы используются внутри каждой вьюхи)
     */

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

            // Проверяем глобальный кэш
            if (ViewDependencyAnalyzer.isInCache(viewName)) {
                System.out.println("    [" + count + "/" + viewNames.size() + "] " + viewName + " (из кэша)");
                ViewTableDependencies deps = analyzer.analyzeViewPublic(viewName);
                result.put(viewName, deps);
                continue;
            }

            // Проверяем локальный кэш формы
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


    //Вынести в отдельный класс  AutoPopupMenuExtractorService
    /**
     * Извлечь unit из AutoPopupMenu компонентов
     * Поддерживает:
     * - D3: <cmpAutoPopupMenu unit="..."/>
     * - M2: <component cmptype="AutoPopupMenu" unit="..."/>
     * - D3 с пробелами: <cmpAutoPopupMenu ... unit="..." .../>
     * - M2 с другими атрибутами: <component cmptype="AutoPopupMenu" name="..." unit="..." .../>
     */
    private void extractAutoPopupMenus(String content, FormInfo formInfo) {
        if (content == null || content.isEmpty()) return;

        Set<String> foundUnits = new LinkedHashSet<>();

        // Паттерн 1: D3 синтаксис - cmpAutoPopupMenu с unit
        // Пример: <cmpAutoPopupMenu unit="DIRECTION_SERVICES"/>
        //         <cmpAutoPopupMenu name="someName" unit="DIRECTION_SERVICES"/>
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

        // Паттерн 2: M2 синтаксис - component с cmptype="AutoPopupMenu" и unit
        // Пример: <component cmptype="AutoPopupMenu" unit="DIRECTION_SERVICES"/>
        //         <component name="pm" cmptype="AutoPopupMenu" unit="DIRECTION_SERVICES"/>
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

        // Паттерн 3: Обратный порядок (unit может быть до cmptype)
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

        // Паттерн 4: Сокращенная форма D3 (без пробелов между атрибутами)
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

        // Добавляем все найденные unit в FormInfo
        for (String unit : foundUnits) {
            formInfo.addAutoPopupMenu(unit);
        }

        System.out.println("  [AutoPopupMenu] Всего найдено unit: " + foundUnits.size());
    }
    /**
     * Устанавливает список форм для анализа напрямую (без чтения из файла)
     */
    public void setFormsToAnalyze(Set<String> forms) {
        this.formsToAnalyze = forms;
    }

    /**
     * Очищает прямой список форм
     */
    public void clearFormsToAnalyze() {
        this.formsToAnalyze = null;
    }

    /**
     * Сканирует весь проект и анализирует все найденные формы
     */
    /**
     * Сканирует весь проект и анализирует только новые (необработанные) формы
     */
    public List<FormInfo> scanAllFormsAndAnalyze() throws Exception {
        Set<String> allForms = scanAllForms();
        Set<String> formsToProcess = new LinkedHashSet<>();
        String outputDir = settings.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }

        // Фильтруем формы - оставляем только те, у которых нет отчёта
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


    // Добавить метод для установки количества потоков:
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