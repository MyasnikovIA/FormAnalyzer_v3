// core/service/FormAnalyzerService.java
package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.core.db.OracleService;
import ru.tmis.analyzer.core.db.PostgresService;
import ru.tmis.analyzer.core.extractor.ExtractorManager;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.RouterInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;
import ru.tmis.analyzer.core.report.CSVMergeService;
import ru.tmis.analyzer.utils.CommentRemover;
import ru.tmis.analyzer.utils.FormPathUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FormAnalyzerService {

    private final SettingsModel settings;
    private final AppConfig config;
    private final FileScannerService scannerService;
    private final UserFormsResolver userFormsResolver;
    private ExtractorManager extractorManager;
    private Set<String> formsToAnalyze;
    private boolean csvFileCreatedDuringSession = false;
    private RouterGeneratorService routerGeneratorService;

    private final OracleService oracleService;
    private final PostgresService postgresService;

    private BooleanSupplier stopCondition = () -> false;
    private ILogger logger;
    private AtomicBoolean stopFlag = null;

    public interface FormAnalyzedCallback {
        void onFormAnalyzed(FormInfo formInfo);
    }

    private FormAnalyzedCallback formAnalyzedCallback;
    private final Map<String, ViewTableDependencies> viewDependenciesCache = new ConcurrentHashMap<>();
    private ProgressCallback progressCallback;

    public interface ProgressCallback {
        void onProgress(int processed, int total, String currentForm);
    }

    public FormAnalyzerService(SettingsModel settings, AppConfig config) {
        this.settings = settings;
        this.config = config;
        this.scannerService = new FileScannerService(settings.getProjectPath());
        this.userFormsResolver = new UserFormsResolver(scannerService);
        this.extractorManager = new ExtractorManager(settings, config);
        this.routerGeneratorService = new RouterGeneratorService(settings);
        this.oracleService = new OracleService(
                settings.getOracleUrl(),
                settings.getOracleUser(),
                settings.getOraclePassword()
        );
        this.postgresService = new PostgresService(
                settings.getPostgresUrl(),
                settings.getPostgresUser(),
                settings.getPostgresPassword(),
                settings.getMisUser()
        );
    }

    public FormAnalyzerService(SettingsModel settings) {
        this(settings, AppConfig.load());
    }

    /**
     * Установка логгера
     */
    public void setLogger(ILogger logger) {
        this.logger = logger;

        // Передаём логгер в extractorManager
        if (this.extractorManager != null) {
            this.extractorManager.setLogger(logger);
        }
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

        // Если condition является AtomicBoolean, передаём его в extractorManager
        if (condition instanceof AtomicBoolean) {
            this.stopFlag = (AtomicBoolean) condition;
            this.extractorManager.setStopRequested(this.stopFlag);

            // Также передаём логгер
            if (this.logger != null) {
                this.extractorManager.setLogger(this.logger);
            }
        }
    }

    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }

    public void setFormAnalyzedCallback(FormAnalyzedCallback callback) {
        this.formAnalyzedCallback = callback;
    }

    public void setFormsToAnalyze(Set<String> forms) {
        this.formsToAnalyze = forms;
    }

    public void clearFormsToAnalyze() {
        this.formsToAnalyze = null;
    }

    public boolean isCsvFileCreatedDuringSession() {
        return csvFileCreatedDuringSession;
    }

    /**
     * Проверяет, нужно ли склеивать CSV отчеты перед анализом
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
        return hasSingleCsv && !hasCommonCsv;
    }

    /**
     * Выполняет склеивание всех существующих CSV отчетов
     */
    public int mergeExistingCsvReports() throws IOException {
        CSVMergeService mergeService = new CSVMergeService(settings.getOutputDir());
        return mergeService.mergeAllCsvReports();
    }

    /**
     * Анализ всех форм из списка
     */
    public List<FormInfo> analyzeAllForms() throws IOException {
        Set<String> formsToAnalyzeList = getFormsToAnalyze();

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

    /**
     * Сканирует весь проект и анализирует только новые формы (без отчётов)
     */
    public List<FormInfo> scanAllFormsAndAnalyze() throws IOException {
        csvFileCreatedDuringSession = false;
        Set<String> allForms = scanAllForms();
        Set<String> formsToProcess = new LinkedHashSet<>();
        String outputDir = settings.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }

        for (String formPath : allForms) {
            String normalized = FormPathUtils.normalizeFormPath(formPath);
            Path reportPath = getReportPath(normalized);
            if (!Files.exists(reportPath)) {
                formsToProcess.add(formPath);
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

    public FormInfo analyzeForm(String formPath) {
        // Проверка остановки в начале
        if (stopCondition.getAsBoolean()) {
            log("Анализ остановлен пользователем перед обработкой формы: " + formPath);
            return null;
        }


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

        // Проверка остановки перед разрешением переопределений
        if (stopCondition.getAsBoolean()) {
            log("Анализ остановлен пользователем перед разрешением переопределений: " + formPath);
            return null;
        }

        FormInfo formInfo = userFormsResolver.resolveOverrides(normalizedPath);

        Path baseFormPathObj = scannerService.getBaseFormPath(normalizedPath);
        formInfo.setBaseFormPath(baseFormPathObj.toString());

        // Проверка остановки перед чтением файла
        if (stopCondition.getAsBoolean()) {
            log("Анализ остановлен пользователем перед чтением файла: " + formPath);
            return null;
        }

        String baseContent = scannerService.readFileContent(baseFormPathObj);
        if (baseContent == null) {
            System.err.println("Не удалось прочитать содержимое формы: " + baseFormPathObj);
            return null;
        }
        // Проверка остановки перед удалением комментариев
        if (stopCondition.getAsBoolean()) {
            log("Анализ остановлен пользователем перед удалением комментариев: " + formPath);
            return null;
        }

        // ========== ОПРЕДЕЛЯЕМ СТИЛЬ ФОРМЫ ==========
        FormInfo.FormStyle formStyle = detectFormStyle(baseContent);
        formInfo.setFormStyle(formStyle);
        log("  Стиль формы: " + formStyle.getName());

        // Удаляем комментарии
        String contentWithoutComments = CommentRemover.removeAllComments(baseContent);

        // Проверка остановки перед обработкой экстракторами
        if (stopCondition.getAsBoolean()) {
            log("Анализ остановлен пользователем перед извлечением данных: " + formPath);
            return null;
        }

        // Передаём в процессор очищенное содержимое
        extractorManager.process(contentWithoutComments, formInfo);
        if (stopCondition.getAsBoolean()) {
            log("Анализ остановлен пользователем после извлечения данных: " + formPath);
            return null;
        }

        // Генерируем Router из sqlQueries (которые не имеют Router)
        List<RouterInfo> generatedFromSql = routerGeneratorService.generateRoutersFromSqlQueries(formInfo);
        for (RouterInfo router : generatedFromSql) {
            // Добавляем в соответствующий список
            if (router.getParentType() == RouterInfo.ParentType.ACTION ||
                    router.getParentType() == RouterInfo.ParentType.BEFORE_ACTION) {
                formInfo.addActionRouter(router);
            } else {
                formInfo.addDataSetRouter(router);
            }
            log("  Сгенерирован Router (converted=false) для SQL: " + router.getName());
        }
        // Генерируем Router из брокеров
        List<RouterInfo> generatedFromBrokers = routerGeneratorService.generateRoutersFromBrokers(formInfo);
        for (RouterInfo router : generatedFromBrokers) {
            formInfo.addActionRouter(router);
            log("  Сгенерирован Router (converted=false) для брокера: " + router.getName());
        }

        // Проверка остановки после extractorManager.process()
        if (stopCondition.getAsBoolean()) {
            log("Анализ остановлен пользователем после извлечения данных: " + formPath);
            return null;
        }

        // Извлекаем AutoPopupMenu
        extractAutoPopupMenus(contentWithoutComments, formInfo);

        // Проверка остановки перед загрузкой вьюх
        if (stopCondition.getAsBoolean()) {
            log("Анализ остановлен пользователем перед загрузкой вьюх: " + formPath);
            return null;
        }

        // ========== СОБИРАЕМ ВСЕ ВЬЮХИ ==========
        Set<String> viewNames = new LinkedHashSet<>();
        System.out.println("formInfo.getTablesViews() "+formInfo.getTablesViews());
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_V_")) {
                viewNames.add(tv);
                log("  Найдена вьюха: " + tv);
            }
        }

        // Также добавляем вьюхи из UnknownObjects (на всякий случай)
        for (String obj : formInfo.getUnknownObjects()) {
            if (obj.startsWith("D_V_")) {
                viewNames.add(obj);
                log("  Найдена вьюха в UnknownObjects: " + obj);
            }
        }

        // ========== ЗАГРУЖАЕМ ЗАВИСИМОСТИ ВЬЮХ ==========
        if (!viewNames.isEmpty()) {
            // Проверка остановки перед загрузкой зависимостей
            if (stopCondition.getAsBoolean()) {
                log("Анализ остановлен пользователем перед загрузкой зависимостей вьюх: " + formPath);
                return formInfo;
            }

            Map<String, ViewTableDependencies> viewDeps = loadViewDependencies(viewNames);
            formInfo.setViewDependencies(viewDeps);
            log("  Сохранено зависимостей вьюх: " + viewDeps.size() + " шт.");

            for (Map.Entry<String, ViewTableDependencies> entry : viewDeps.entrySet()) {
                log("    Вьюха " + entry.getKey() + " содержит " + entry.getValue().getOracleTables().size() + " таблиц");
                for (String table : entry.getValue().getOracleTables()) {
                    log("      - " + table);
                    formInfo.addTableFromView(table);
                }
            }

            // ========== НОВЫЙ КОД: Загрузка DDL для LLM ПОСЛЕ загрузки зависимостей ==========
            if (config != null && config.isEnableLLMExport()) {
                log("  Загрузка DDL данных для LLM отчёта...");
                LLMDataLoader llmDataLoader = new LLMDataLoader(settings, config);
                llmDataLoader.loadLLMData(formInfo);
                log("  Загружено Oracle вьюх: " + formInfo.getOracleViewDDLs().size());
                log("  Загружено PostgreSQL вьюх: " + formInfo.getPostgresViewDDLs().size());
                log("  Загружено таблиц: " + formInfo.getOracleTableDDLs().size() +
                        " (Oracle), " + formInfo.getPostgresTableDDLs().size() + " (PostgreSQL)");
            }
            // ===================================================
        }
        return formInfo;
    }

    /**
     * Загрузка зависимостей вьюх (с использованием кэша)
     */
    private Map<String, ViewTableDependencies> loadViewDependencies(Set<String> viewNames) {
        if (viewNames.isEmpty()) {
            return Collections.emptyMap();
        }

        // Проверяем доступность Oracle (нужна для получения DDL вьюх)
        if (!DatabaseCacheManager.isOracleServerAvailable()) {
            log("  Oracle сервер недоступен, пропускаем загрузку зависимостей вьюх");
            return Collections.emptyMap();
        }

        Map<String, ViewTableDependencies> result = new LinkedHashMap<>();
        ViewDependencyAnalyzer analyzer = new ViewDependencyAnalyzer(settings);

        log("  Загрузка зависимостей для " + viewNames.size() + " вьюх...");

        int count = 0;
        for (String viewName : viewNames) {
            if (stopCondition.getAsBoolean()) {
                break;
            }
            count++;

            log("    [" + count + "/" + viewNames.size() + "] Анализ вьюхи: " + viewName);

            try {
                ViewTableDependencies deps = analyzer.analyzeView(viewName);
                if (deps != null) {
                    result.put(viewName, deps);
                    log("      OK, таблиц: " + deps.getOracleTables().size());
                    for (String table : deps.getOracleTables()) {
                        log("        - " + table);
                    }
                } else {
                    log("      ОШИБКА: deps is null");
                }
            } catch (Exception e) {
                log("      ОШИБКА: " + e.getMessage());
                ViewTableDependencies errorDeps = new ViewTableDependencies(viewName);
                errorDeps.setExistsInOracle(false);
                errorDeps.setOracleError(e.getMessage());
                result.put(viewName, errorDeps);
            }
        }

        log("  Загружено зависимостей: " + result.size());
        return result;
    }

    /**
     * Принудительная загрузка зависимостей вьюх (игнорируя кэш)
     */
    private Map<String, ViewTableDependencies> loadViewDependenciesForce(Set<String> viewNames) {
        if (viewNames.isEmpty()) {
            return Collections.emptyMap();
        }

        if (!DatabaseCacheManager.isOracleAvailable()) {
            log("  Oracle сервер недоступен, пропускаем загрузку зависимостей вьюх");
            return Collections.emptyMap();
        }

        Map<String, ViewTableDependencies> result = new LinkedHashMap<>();

        // Очищаем кэш для этих вьюх
        for (String viewName : viewNames) {
            DatabaseCacheManager.clearViewDependency(viewName);
            viewDependenciesCache.remove(viewName);
        }

        ViewDependencyAnalyzer analyzer = new ViewDependencyAnalyzer(settings);
        System.out.println("  Принудительная загрузка зависимостей для " + viewNames.size() + " вьюх...");

        int count = 0;
        for (String viewName : viewNames) {
            if (stopCondition.getAsBoolean()) {
                break;
            }
            count++;

            System.out.print("    Анализ вьюхи [" + count + "/" + viewNames.size() + "]: " + viewName + " ... ");

            try {
                ViewTableDependencies deps = analyzer.analyzeView(viewName);
                result.put(viewName, deps);
                viewDependenciesCache.put(viewName, deps);

                if (deps.isExistsInOracle()) {
                    System.out.println("OK (таблиц: " + deps.getOracleTables().size() + ")");
                } else {
                    System.out.println("НЕ НАЙДЕНА В ORACLE - " + deps.getOracleError());
                }
            } catch (Exception e) {
                System.err.println("ОШИБКА: " + e.getMessage());
                ViewTableDependencies errorDeps = new ViewTableDependencies(viewName);
                errorDeps.setExistsInOracle(false);
                errorDeps.setOracleError(e.getMessage());
                result.put(viewName, errorDeps);
                viewDependenciesCache.put(viewName, errorDeps);
            }
        }

        int foundCount = (int) result.values().stream().filter(ViewTableDependencies::isExistsInOracle).count();
        int totalTables = result.values().stream()
                .filter(ViewTableDependencies::isExistsInOracle)
                .mapToInt(v -> v.getOracleTables().size())
                .sum();
        System.out.println("  Итого: найдено вьюх " + foundCount + " из " + viewNames.size() +
                ", таблиц: " + totalTables);

        return result;
    }

    /**
     * Получить список форм для анализа
     */
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

        System.out.println("Список форм пуст");
        return null;
    }

    /**
     * Сканирование всех форм в проекте
     */
    private Set<String> scanAllForms() throws IOException {
        Set<String> allForms = new LinkedHashSet<>();
        Path rootPath = Paths.get(settings.getProjectPath());

        Path formsPath = rootPath.resolve("Forms");
        if (Files.exists(formsPath)) {
            try (Stream<Path> walk = Files.walk(formsPath)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".frm"))
                        .forEach(p -> {
                            String relativePath = formsPath.relativize(p).toString().replace("\\", "/");
                            allForms.add("Forms/" + relativePath);
                        });
            }
        }

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
                                    });
                        } catch (IOException e) {
                            System.err.println("Ошибка сканирования " + dirName + ": " + e.getMessage());
                        }
                    });
        }

        return allForms;
    }

    /**
     * Получить путь к файлу отчёта
     */
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

    /**
     * Извлечение AutoPopupMenu из формы
     */
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
            }
        }

        for (String unit : foundUnits) {
            formInfo.addAutoPopupMenu(unit);
        }
    }

    /**
     * Принудительная загрузка таблиц из вьюх (игнорируя кэш)
     */
    private Set<String> loadTablesFromViewsForce(Set<String> viewNames) {
        Set<String> allTables = new LinkedHashSet<>();

        for (String viewName : viewNames) {
            // Очищаем кэш для этой вьюхи
            DatabaseCacheManager.clearViewDependency(viewName);

            // Загружаем заново
            ViewDependencyAnalyzer analyzer = new ViewDependencyAnalyzer(settings);
            ViewTableDependencies deps = analyzer.analyzeView(viewName);

            if (deps != null && deps.isExistsInOracle()) {
                allTables.addAll(deps.getOracleTables());
                System.out.println("  Вьюха " + viewName + " содержит таблицы: " + deps.getOracleTables());
            }
        }

        return allTables;
    }

    // Создание ExtractorManager с передачей флага остановки
    private void initExtractorManager() {
        this.extractorManager = new ExtractorManager(settings, config);
        if (stopCondition instanceof AtomicBoolean) {
            this.extractorManager.setStopRequested((AtomicBoolean) stopCondition);
        }
        this.extractorManager.setLogger(logger != null ? logger : new ILogger() {
            @Override
            public void log(String message) {
                System.out.println(message);
            }

            @Override
            public void error(String message) {
                System.err.println(message);
            }

            @Override
            public void debug(String message) {
                System.out.println("[DEBUG] " + message);
            }
        });
    }

    /**
     * Установка флага остановки (прямая передача)
     */
    public void setStopFlag(AtomicBoolean stopFlag) {
        this.stopFlag = stopFlag;
        this.stopCondition = stopFlag::get;

        // Передаём в extractorManager
        if (this.extractorManager != null) {
            this.extractorManager.setStopRequested(stopFlag);
        }
    }

    /**
     * Определяет стиль формы по содержимому XML
     *
     * @param xmlContent содержимое формы
     * @return стиль формы (M2, D3 или UNKNOWN)
     */
    private FormInfo.FormStyle detectFormStyle(String xmlContent) {
        if (xmlContent == null || xmlContent.isEmpty()) {
            return FormInfo.FormStyle.UNKNOWN;
        }

        // Ищем первый div с cmptype="Form"
        // Паттерн для D3: class="d3form" или class содержит d3form
        Pattern d3Pattern = Pattern.compile(
                "<div\\s+[^>]*?cmptype\\s*=\\s*[\"']Form[\"'][^>]*?class\\s*=\\s*[\"'][^\"']*\\bd3form\\b[^\"']*[\"']",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );

        // Паттерн для M2: div с cmptype="Form" без d3form в class
        Pattern m2Pattern = Pattern.compile(
                "<div\\s+[^>]*?cmptype\\s*=\\s*[\"']Form[\"'][^>]*?(?:class\\s*=\\s*[\"'][^\"']*[\"'])?",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );

        Matcher d3Matcher = d3Pattern.matcher(xmlContent);
        if (d3Matcher.find()) {
            System.out.println("[FormStyle] Определён стиль D3 (найден class=\"d3form\")");
            return FormInfo.FormStyle.D3;
        }

        Matcher m2Matcher = m2Pattern.matcher(xmlContent);
        if (m2Matcher.find()) {
            System.out.println("[FormStyle] Определён стиль M2 (форма без d3form)");
            return FormInfo.FormStyle.M2;
        }

        return FormInfo.FormStyle.UNKNOWN;
    }
    private ExtractorManager createExtractorManager() {
        ExtractorManager manager = new ExtractorManager(settings, config);
        if (stopFlag != null) {
            manager.setStopRequested(stopFlag);
        }
        if (logger != null) {
            manager.setLogger(logger);
        }
        return manager;
    }
}