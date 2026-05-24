// core/service/FormAnalyzerService.java (исправленный)
package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.extractor.ExtractorManager;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;
import ru.tmis.analyzer.core.report.CSVMergeService;
import ru.tmis.analyzer.core.report.SingleFormCSVReportGenerator;
import ru.tmis.analyzer.utils.CommentRemover;
import ru.tmis.analyzer.utils.FormPathUtils;
import ru.tmis.analyzer.core.llm.LLMPromptGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FormAnalyzerService {

    private boolean csvFileCreatedDuringSession = false;

    private final SettingsModel settings;
    private final AppConfig config;
    private final FileScannerService scannerService;
    private final UserFormsResolver userFormsResolver;
    private final ExtractorManager extractorManager;
    private Set<String> formsToAnalyze;

    public interface FormAnalyzedCallback {
        void onFormAnalyzed(FormInfo formInfo);
    }
    private FormAnalyzedCallback formAnalyzedCallback;
    private final Map<String, ViewTableDependencies> viewDependenciesCache = new ConcurrentHashMap<>();

    private BooleanSupplier stopCondition = () -> false;
    private ProgressCallback progressCallback;
    private ILogger logger;  // <-- Добавить логгер

    public interface ProgressCallback {
        void onProgress(int processed, int total, String currentForm);
    }

    public FormAnalyzerService(SettingsModel settings, AppConfig config) {
        this.settings = settings;
        this.config = config;
        this.scannerService = new FileScannerService(settings.getProjectPath());
        this.userFormsResolver = new UserFormsResolver(scannerService);
        this.extractorManager = new ExtractorManager(settings);
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

    public List<FormInfo> analyzeAllForms() throws IOException {
        Set<String> formsToAnalyzeList = getFormsToAnalyze();

        // Если список пуст (null) - возвращаем null, чтобы вызвать предупреждение
        if (formsToAnalyzeList == null) {
            System.out.println("Список форм пуст, требуется подтверждение пользователя");
            return null;
        }

        List<FormInfo> results = new ArrayList<>();

        System.out.println("Найдено форм для анализа: " + formsToAnalyzeList.size());

        int processed = 0;
        int total = formsToAnalyzeList.size();

        for (String formPath : formsToAnalyzeList) {
            if (stopCondition.getAsBoolean()) {
                System.out.println("Анализ остановлен пользователем");
                break;
            }

            processed++;
            if (progressCallback != null) {
                progressCallback.onProgress(processed, total, formPath);
            }

            System.out.print("Анализ [" + processed + "/" + total + "]: " + formPath + " ... ");

            try {
                FormInfo formInfo = analyzeForm(formPath);
                if (formInfo != null) {
                    results.add(formInfo);
                    if (formAnalyzedCallback != null) {
                        formAnalyzedCallback.onFormAnalyzed(formInfo);
                    }
                    if (formInfo.getSqlQueries() != null && !formInfo.getSqlQueries().isEmpty()) {
                        System.out.println("OK (SQL: " + formInfo.getSqlQueries().size() + ")");
                    } else {
                        System.out.println("OK (SQL: 0)");
                    }
                } else {
                    System.out.println("ПРОПУЩЕН");
                }
            } catch (Exception e) {
                System.err.println("ОШИБКА: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return results;
    }

    // core/service/FormAnalyzerService.java

    // В методе analyzeForm изменить проверку отчёта
    public FormInfo analyzeForm(String formPath) {
        String normalizedPath = FormPathUtils.normalizeFormPath(formPath);

        System.out.println("  Проверка формы: " + normalizedPath);

        if (!scannerService.baseFormExists(normalizedPath)) {
            System.err.println("Базовая форма не найдена: " + normalizedPath);
            return null;
        }

        // ========== ПРОВЕРКА: ЕСЛИ ОТЧЁТ УЖЕ СУЩЕСТВУЕТ ==========
        Path reportPath = getReportPath(normalizedPath);

        if (Files.exists(reportPath)) {
            log("Отчет уже существует: " + reportPath.toString());
            log("  Форма пропущена (отчет построен ранее)");
            return null;
        }
        // =======================================================

        FormInfo formInfo = userFormsResolver.resolveOverrides(normalizedPath);

        Path baseFormPathObj = scannerService.getBaseFormPath(normalizedPath);
        formInfo.setBaseFormPath(baseFormPathObj.toString());

        String baseContent = scannerService.readFileContent(baseFormPathObj);
        if (baseContent == null) {
            System.err.println("Не удалось прочитать содержимое формы: " + baseFormPathObj);
            return null;
        }

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
        return formInfo;
    }

    private Set<String> getFormsToAnalyze() throws IOException {
        // Если установлен прямой список, используем его
        if (formsToAnalyze != null && !formsToAnalyze.isEmpty()) {
            System.out.println("Используем прямой список форм (" + formsToAnalyze.size() + " шт.)");
            return formsToAnalyze;
        }

        // Иначе читаем из файла как обычно
        Set<String> forms = new LinkedHashSet<>();
        Path listFile = Paths.get("forms_list.txt");

        if (Files.exists(listFile)) {
            List<String> lines = Files.readAllLines(listFile);
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    String normalized = FormPathUtils.normalizeFormPath(trimmed);
                    forms.add(normalized);
                    System.out.println("  Добавлена форма из списка: " + normalized);
                }
            }
            if (!forms.isEmpty()) {
                return forms;
            }
        }

        // Если список пуст - возвращаем null, чтобы вызвать предупреждение
        System.out.println("Список форм пуст");
        return null;
    }

    private Set<String> scanAllForms() throws IOException {
        Set<String> allForms = new LinkedHashSet<>();
        Path rootPath = Paths.get(settings.getProjectPath());

        // Сканируем Forms
        Path formsPath = rootPath.resolve("Forms");
        if (Files.exists(formsPath)) {
            try (Stream<Path> walk = Files.walk(formsPath)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".frm"))
                        .forEach(p -> {
                            String relativePath = formsPath.relativize(p).toString().replace("\\", "/");
                            allForms.add("Forms/" + relativePath);
                            System.out.println("  Найдена форма: Forms/" + relativePath);
                        });
            }
        }

        // Сканируем UserForms
        try (Stream<Path> list = Files.list(rootPath)) {
            list.filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("UserForms"))
                    .forEach(userFormsDir -> {
                        String dirName = userFormsDir.getFileName().toString();
                        try (Stream<Path> walk = Files.walk(userFormsDir)) {
                            walk.filter(Files::isRegularFile)
                                    .filter(p -> p.toString().endsWith(".frm") || p.toString().endsWith(".dfrm"))
                                    .forEach(p -> {
                                        String relativePath = userFormsDir.relativize(p).toString().replace("\\", "/");
                                        allForms.add(dirName + "/" + relativePath);
                                        System.out.println("  Найдена форма: " + dirName + "/" + relativePath);
                                    });
                        } catch (IOException e) {
                            System.err.println("Ошибка сканирования " + dirName + ": " + e.getMessage());
                        }
                    });
        }

        System.out.println("Всего найдено форм: " + allForms.size());
        return allForms;
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
    public List<FormInfo> scanAllFormsAndAnalyze() throws IOException {
        csvFileCreatedDuringSession = false; // Сброс флага при новом сканировании

        Set<String> allForms = scanAllForms();
        Set<String> formsToProcess = new LinkedHashSet<>();
        String outputDir = settings.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }

        // Фильтруем формы - оставляем только те, у которых нет отчёта
        for (String formPath : allForms) {
            String normalized = FormPathUtils.normalizeFormPath(formPath);
            Path reportPath = getReportPath(normalized);

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
     * Выполняет склеивание всех существующих CSV отчетов перед началом анализа новых форм
     * @return количество склеенных файлов
     */
    public int mergeExistingCsvReports() throws IOException {
        CSVMergeService mergeService = new CSVMergeService(settings.getOutputDir());
        return mergeService.mergeAllCsvReports();
    }

    /**
     * Проверяет, нужно ли склеивать CSV отчеты перед анализом
     * @return true если есть отдельные CSV файлы и нет общего отчета
     */
    public boolean shouldMergeCsvReports() {
        CSVMergeService mergeService = new CSVMergeService(settings.getOutputDir());

        boolean hasSingleCsv = false;
        Path csvReportsDir = Paths.get(settings.getOutputDir(), "CSV_reports");
        if (Files.exists(csvReportsDir)) {
            try (Stream<Path> walk = Files.walk(csvReportsDir)) {
                hasSingleCsv = walk.filter(Files::isRegularFile)
                        .anyMatch(p -> p.toString().endsWith(".csv"));
            } catch (IOException e) {
                // ignore
            }
        }

        boolean hasCommonCsv = mergeService.commonCsvExists();

        // Нужно склеить, если есть отдельные CSV файлы, но нет общего отчета
        return hasSingleCsv && !hasCommonCsv;
    }
    public boolean isCsvFileCreatedDuringSession() {
        return csvFileCreatedDuringSession;
    }

}