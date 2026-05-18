package ru.tmis.analyzer.core.report;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.core.db.*;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.PopupMenuInfo;
import ru.tmis.analyzer.core.model.SqlInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;

import ru.tmis.analyzer.core.db.PostgresPackageChecker;



import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReportGenerator {

    private final String outputDir;
    private final AppConfig config;
    private List<FormInfo> forms;
    private final SettingsModel settings;

    private final Map<String, Long> oracleCountCache = new ConcurrentHashMap<>();
    private final Map<String, Long> postgresCountCache = new ConcurrentHashMap<>();

    private OracleService oracleService;
    private PostgresService postgresService;

    // В конструкторе инициализируем сервисы (если ещё нет)

    public ReportGenerator(String outputDir, AppConfig config) {
        this.outputDir = outputDir;
        this.config = config;
        this.forms = new ArrayList<>();
        this.settings = SettingsModel.getInstance();
        this.oracleService = new OracleService(settings.getOracleUrl(), settings.getOracleUser(), settings.getOraclePassword());
        this.postgresService = new PostgresService(settings.getPostgresUrl(), settings.getPostgresUser(), settings.getPostgresPassword(), settings.getMisUser());
    }

    // Метод для получения количества записей в Oracle (с кэшированием)
    private long getOracleCount(String objectName) {
        return DatabaseCacheManager.getOracleCount(objectName, () ->
                oracleService.getTableCount(objectName));
    }

    // Метод для получения количества записей в PostgreSQL (с кэшированием)
    private long getPostgresCount(String objectName) {
        return DatabaseCacheManager.getPostgresCount(objectName, () ->
                postgresService.getTableCount(objectName));
    }
    // Форматирование числа (тысячи, миллионы)
    private String formatCount(long count) {
        if (count < 0) return "ошибка";
        if (count >= 1_000_000) return String.format("%.2f млн", count / 1_000_000.0);
        if (count >= 1_000) return String.format("%.2f тыс", count / 1_000.0);
        return String.valueOf(count);
    }

    public void addForm(FormInfo form) {
        forms.add(form);
    }

    public void addAllForms(List<FormInfo> forms) {
        this.forms.addAll(forms);
    }

    public void generateMainReport() throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        Path reportPath = outputPath.resolve("forms_report.txt");

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(reportPath))) {
            writeHeader(writer);

            for (FormInfo form : forms) {
                writeFormReport(writer, form);
            }

            writeFooter(writer);
        }

        System.out.println("Отчет сохранен: " + reportPath);
    }

    private void writeHeader(PrintWriter writer) {
        writer.println("=".repeat(100));
        writer.println("=== ОТЧЕТ ПО ФОРМАМ T-MIS ===");
        writer.println("Дата создания: " + new Date());
        writer.println("Всего форм: " + forms.size());
        writer.println("=".repeat(100));
        writer.println();
    }

    private void writeFormReport(PrintWriter writer, FormInfo form) {
        writer.println("-".repeat(100));
        writer.println("ФОРМА: " + form.getFormPath());
        writer.println("-".repeat(100));
        writer.println("Базовая форма: " + form.getBaseFormPath());

        if (form.isFullyReplaced()) {
            writer.println("СТАТУС: ПОЛНОСТЬЮ ЗАМЕНЕНА");
            writer.println("Файл замены: " + form.getReplacementPath());
        } else if (!form.getOverrides().isEmpty()) {
            writer.println("СТАТУС: ЧАСТИЧНО ПЕРЕОПРЕДЕЛЕНА");
        } else {
            writer.println("СТАТУС: БАЗОВАЯ ФОРМА");
        }
        writer.println();

        writeUserFormsSection(writer, form);

        // SubForm
        writer.println("SubForm:");
        if (form.getSubForms().isEmpty()) {
            writer.println("     (не найдено)");
        } else {
            for (String subForm : form.getSubForms()) {
                writer.println("     " + subForm);
            }
        }
        writer.println();

        // Список вызываемых форм в JS
        writer.println("Список вызываемых форм в JS:");
        if (form.getJsForms().isEmpty()) {
            writer.println("     (не найдено)");
        } else {
            for (String jsForm : form.getJsForms()) {
                writer.println("     " + jsForm);
            }
        }
        writer.println();

        if (!form.getAutoPopupMenus().isEmpty()) {
            writer.println("Коды подключаемого AutoPopUp меню на форме:");
            for (String unit : form.getAutoPopupMenus()) {
                writer.println("        " + unit + ";");
            }
            writer.println();
        }

        if (config.isIncludePopupMenus() && form.getPopupMenus() != null && !form.getPopupMenus().isEmpty()) {
            writePopupMenusBlock(writer, form.getPopupMenus(), "OracleSQL");
        }

       // Контекстное меню (ПКМ) - PostgreSQL
        if (config.isIncludePostgresPopupMenus() && form.getPopupMenusPg() != null && !form.getPopupMenusPg().isEmpty()) {
            writePopupMenusBlock(writer, form.getPopupMenusPg(), "PostgreSQL");
        }

        // SQL запросы
        if (config.isIncludeSqlContent()) {
            writeSqlQueries(writer, form);
        } else {
            writer.println("SQL ЗАПРОСЫ (" + form.getSqlQueries().size() + "):");
            writer.println("     (содержимое скрыто)");
            writer.println();
        }

        // Используемые таблицы и вьюхи
        if (config.isIncludeTablesViews() && !form.getTablesViews().isEmpty()) {
            writer.println("ИСПОЛЬЗУЕМЫЕ ТАБЛИЦЫ И ВЬЮХИ:");
            for (String tv : form.getTablesViews()) {
                writer.println("    " + tv);
            }
            writer.println();
        }

        // Таблицы, используемые через вьюхи
        System.out.println("[DEBUG] Checking viewDependencies: " + (form.getViewDependencies() != null ? form.getViewDependencies().size() : "null"));
        if (form.getViewDependencies() != null && !form.getViewDependencies().isEmpty()) {
            System.out.println("[DEBUG] Calling writeViewTablesBlock");
            writeViewTablesBlock(writer, form, form.getViewDependencies());
        } else {
            System.out.println("[DEBUG] viewDependencies is null or empty, skipping");
        }

        // Детальное содержимое вьюх
        if (config.isIncludeViewDetails() && form.getViewDependencies() != null && !form.getViewDependencies().isEmpty()) {
            writeViewDetailsSection(writer, form, form.getViewDependencies());
        }

        // Используемые пакеты и функции
        if (!form.getPackagesFunctions().isEmpty()) {
            writer.println("ИСПОЛЬЗУЕМЫЕ ПАКЕТЫ И ФУНКЦИИ:");
            for (String pf : form.getPackagesFunctions()) {
                writer.println("    " + pf);
            }
            writer.println();
        }

        // Константы
        if (!form.getConstants().isEmpty()) {
            writer.println("КОНСТАНТЫ:");
            for (String constant : form.getConstants()) {
                writer.println("    " + constant);
            }
            writer.println();
        }

        // Системные опции
        if (!form.getSystemOptions().isEmpty()) {
            writer.println("СИСТЕМНЫЕ ОПЦИИ:");
            for (String opt : form.getSystemOptions()) {
                writer.println("    " + opt);
            }
            writer.println();
        }

        // ========== ПОЛЬЗОВАТЕЛЬСКИЕ ПРОЦЕДУРЫ ==========
        if (form.getUserProcedures() != null && !form.getUserProcedures().isEmpty()) {
            writer.println("ПОЛЬЗОВАТЕЛЬСКИЕ ПРОЦЕДУРЫ:");
            for (String proc : form.getUserProcedures()) {
                writer.println("    " + proc);
            }
            writer.println();
        }

        // Композиции UnitEdit
        if (!form.getUnitCompositions().isEmpty()) {
            writer.println("КОМПОЗИЦИИ UnitEdit:");
            for (String comp : form.getUnitCompositions()) {
                writer.println("    " + comp);
            }
            writer.println();
        }

        // Отчеты вызываемые на форме
        if (!form.getReports().isEmpty()) {
            writer.println("Отчеты вызываемые на форме (коды/формы отчета):");
            for (String report : form.getReports()) {
                writer.println("        " + report + ";");
            }
            writer.println();
        }

        // Брокеры
        if (!form.getBrokers().isEmpty()) {
            writer.println("БРОКЕРЫ:");
            for (String broker : form.getBrokers()) {
                writer.println("    " + broker);
            }
            writer.println();
        }

        // Разобрать аналитиком
        if (!form.getUnknownObjects().isEmpty()) {
            writer.println("РАЗОБРАТЬ АНАЛИТИКОМ:");
            for (String obj : form.getUnknownObjects()) {
                writer.println("    " + obj);
            }
            writer.println();
        }

        // Проверка пакетов/функций в PostgreSQL
        if (config.isCheckPostgresPackages() && !form.getPackagesFunctions().isEmpty()) {
            writer.println("ПРОВЕРКА ПАКЕТОВ/ФУНКЦИЙ В PostgreSQL:");
            writer.println();

            PostgresPackageChecker checker = new PostgresPackageChecker(
                    settings.getPostgresUrl(),
                    settings.getPostgresUser(),
                    settings.getPostgresPassword()
            );
            // Можно установить логгер
            // checker.setLogCallback(...);

            Map<String, PostgresPackageChecker.FunctionInfo> results = checker.checkFunctions(form.getPackagesFunctions());

            for (Map.Entry<String, PostgresPackageChecker.FunctionInfo> entry : results.entrySet()) {
                String funcName = entry.getKey();
                PostgresPackageChecker.FunctionInfo info = entry.getValue();

                writer.println("  " + funcName + ":");
                writer.println("    Статус: " + info.getStatus());
                if (info.getSignature() != null && !info.getSignature().isEmpty()) {
                    writer.println("    Сигнатура: " + info.getSignature());
                }
                if (info.hasErrors()) {
                    writer.println("    ОШИБКИ:");
                    for (String err : info.getErrors()) writer.println("      " + err);
                }
                if (info.hasWarnings()) {
                    writer.println("    ПРЕДУПРЕЖДЕНИЯ:");
                    for (String warn : info.getWarnings()) writer.println("      " + warn);
                }
                writer.println();
            }
        }
// Проверка первичных ключей
        if (config.isCheckPostgresPK()) {
            Set<String> allTables = getAllTablesForForm(form);
            if (!allTables.isEmpty()) {
                writer.println("ПРОВЕРКА ПЕРВИЧНЫХ КЛЮЧЕЙ (Oracle vs PostgreSQL):");
                writer.println();
                DatabaseObjectChecker checker = new DatabaseObjectChecker(SettingsModel.getInstance());
                int pkCount = 0;
                for (String tableName : allTables) {
                    pkCount++;
                    DatabaseObjectChecker.PrimaryKeyInfo pkInfo = checker.checkPrimaryKey(tableName);
                    writer.println("  " + tableName + ":");
                    writer.println("    Статус: " + pkInfo.getStatus());
                    System.out.println("    [" + pkCount + "/" + allTables.size() + "] Проверка PK: " + tableName);
                    if (pkInfo.hasPKInOracle()) {
                        writer.println("    Oracle PK поля: " + String.join(", ", pkInfo.getOracleColumns()));
                    }
                    if (pkInfo.hasPKInPostgres()) {
                        writer.println("    PostgreSQL PK поля: " + String.join(", ", pkInfo.getPostgresColumns()));
                    }
                    writer.println();
                }
            } else {
                writer.println("ПРОВЕРКА ПЕРВИЧНЫХ КЛЮЧЕЙ (Oracle vs PostgreSQL):");
                writer.println("     (нет таблиц для проверки)");
                writer.println();
            }
        }

// Проверка NOT NULL constraints
        if (config.isCheckNotNullConstraints()) {
            Set<String> allTables = getAllTablesForForm(form);
            if (!allTables.isEmpty()) {
                writer.println("ПРОВЕРКА NOT NULL CONSTRAINT (Oracle vs PostgreSQL):");
                writer.println();
                DatabaseObjectChecker checker = new DatabaseObjectChecker(SettingsModel.getInstance());
                int nnCount = 0;
                for (String tableName : allTables) {
                    nnCount++;
                    System.out.println("    [" + nnCount + "/" + allTables.size() + "] Проверка NOT NULL: " + tableName);
                    List<DatabaseObjectChecker.NotNullConstraintInfo> constraints = checker.checkNotNullConstraints(tableName);
                    boolean hasIssues = false;
                    for (DatabaseObjectChecker.NotNullConstraintInfo info : constraints) {
                        if (!info.isMatch()) {
                            if (!hasIssues) {
                                writer.println("  " + tableName + ":");
                                hasIssues = true;
                            }
                            writer.println("    Колонка: " + info.getColumnName());
                            writer.println("      Oracle: " + (info.isNotNullInOracle() ? "NOT NULL" : "NULL разрешен"));
                            writer.println("      PostgreSQL: " + (info.isNotNullInPostgres() ? "NOT NULL" : "NULL разрешен"));
                            writer.println("      " + info.getStatus());
                            String recommendation = info.getRecommendation();
                            if (recommendation != null) {
                                writer.println("      Рекомендация: " + recommendation);
                            }
                            writer.println();
                        }
                    }
                    if (!hasIssues) {
                        writer.println("  " + tableName + ": OK (все NULL constraints совпадают)");
                        writer.println();
                    }
                }
            } else {
                writer.println("ПРОВЕРКА NOT NULL CONSTRAINT (Oracle vs PostgreSQL):");
                writer.println("     (нет таблиц для проверки)");
                writer.println();
            }
        }
    }

    private void writeSqlQueries(PrintWriter writer, FormInfo form) {
        writer.println("SQL ЗАПРОСЫ (" + form.getSqlQueries().size() + "):");
        writer.println();

        int num = 1;
        for (SqlInfo sql : form.getSqlQueries()) {
            writer.println("  [" + num + "] " + sql.getSourceType() + ": " + sql.getComponentName());
            writer.println("      Источник: " + sql.getSourcePath());
            writer.println("      SQL:");

            String content = sql.getSqlContent();
            if (content != null && !content.isEmpty()) {
                for (String line : content.split("\\r?\\n")) {
                    writer.println("      " + line);
                }
            }
            writer.println();
            num++;
        }
    }

    private void writeFooter(PrintWriter writer) {
        writer.println();
        writer.println("=".repeat(100));
        writer.println("=== КОНЕЦ ОТЧЕТА ===");
        writer.println("=".repeat(100));
    }

    public void generateSummaryReport() throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        Path summaryPath = outputPath.resolve("summary_report.txt");

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(summaryPath))) {
            writer.println("=".repeat(80));
            writer.println("=== ОБЩАЯ СТАТИСТИКА ===");
            writer.println("Дата: " + new Date());
            writer.println("=".repeat(80));
            writer.println();

            writer.println("Всего форм: " + forms.size());

            int totalSql = forms.stream().mapToInt(FormInfo::getTotalSqlQueries).sum();
            writer.println("Всего SQL запросов: " + totalSql);

            Set<String> allTables = new LinkedHashSet<>();
            Set<String> allViews = new LinkedHashSet<>();
            Set<String> allPackages = new LinkedHashSet<>();
            Set<String> allConstants = new LinkedHashSet<>();

            for (FormInfo form : forms) {
                for (String tv : form.getTablesViews()) {
                    if (tv.startsWith("D_V_")) {
                        allViews.add(tv);
                    } else {
                        allTables.add(tv);
                    }
                }
                allPackages.addAll(form.getPackagesFunctions());
                allConstants.addAll(form.getConstants());
            }

            writer.println("Уникальных таблиц: " + allTables.size());
            writer.println("Уникальных вьюх: " + allViews.size());
            writer.println("Уникальных пакетов/функций: " + allPackages.size());
            writer.println("Уникальных констант: " + allConstants.size());

            writer.println();
            writer.println("=".repeat(80));
        }

        System.out.println("Сводный отчет сохранен: " + summaryPath);
    }


    /**
     * Вывод таблиц, используемых через вьюхи
     * @param writer PrintWriter для вывода
     * @param formInfo информация о форме
     * @param viewDependencies карта зависимостей вьюх
     */
    private void writeViewTablesBlock(PrintWriter writer, FormInfo formInfo,
                                      Map<String, ViewTableDependencies> viewDependencies) {
        if (viewDependencies == null || viewDependencies.isEmpty()) {
            System.out.println("[DEBUG] writeViewTablesBlock: viewDependencies is null or empty");
            return;
        }

        // Собираем все вьюхи, используемые в этой форме
        Set<String> viewsUsed = new LinkedHashSet<>();
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_V_")) {
                viewsUsed.add(tv);
            }
        }

        if (viewsUsed.isEmpty()) {
            System.out.println("[DEBUG] writeViewTablesBlock: no views found in form");
            return;
        }

        // Собираем уникальные таблицы из всех вьюх
        Set<String> allTables = new LinkedHashSet<>();
        for (String viewName : viewsUsed) {
            ViewTableDependencies deps = viewDependencies.get(viewName);
            if (deps != null && deps.isExistsInOracle()) {
                allTables.addAll(deps.getOracleTables());
            }
        }

        if (allTables.isEmpty()) {
            System.out.println("[DEBUG] writeViewTablesBlock: no tables extracted from views");
            return;
        }

        writer.println("ТАБЛИЦЫ, ИСПОЛЬЗУЕМЫЕ ЧЕРЕЗ ВЬЮХИ (уникальные для этой формы):");
        for (String table : allTables) {
            writer.println("    " + table);
        }
        writer.println();

        System.out.println("[DEBUG] writeViewTablesBlock: wrote " + allTables.size() + " tables");
    }

    /**
     * Вывод контекстного меню (PopupMenu) в виде дерева
     */
    private void writePopupMenusBlock(PrintWriter writer, List<PopupMenuInfo> menus, String source) {
        if (menus == null || menus.isEmpty()) {
            return;
        }
        writer.println();
        writer.println("Контекстное меню используемое на форме (ПКМ) – данные из " + source + ":");
        writer.println();
        for (int i = 0; i < menus.size(); i++) {
            PopupMenuInfo menu = menus.get(i);
            boolean isLast = (i == menus.size() - 1);

            // Вывод корневого элемента меню
            String prefix = isLast ? "└── " : "├── ";
            writer.println(prefix + "name=\"" + menu.getName() + "\"");

            // Вывод пунктов меню
            writeMenuTree(writer, menu.getRootItems(), isLast ? "    " : "│   ");

            if (!isLast) {
                writer.println("│   ");
            }
        }
        writer.println();
    }

    /**
     * Вывод информации о переопределениях из UserForms
     */
    private void writeUserFormsSection(PrintWriter writer, FormInfo formInfo) {
        writer.println("ЮЗЕРФОРМЫ:");

        if (formInfo.getOverrides().isEmpty() && !formInfo.isFullyReplaced()) {
            writer.println("     (не найдено)");
            writer.println();
            return;
        }

        Map<String, List<FormInfo.OverrideInfo>> overridesByRegion = new LinkedHashMap<>();
        for (FormInfo.OverrideInfo override : formInfo.getOverrides()) {
            overridesByRegion.computeIfAbsent(override.getRegionName(), k -> new ArrayList<>()).add(override);
        }

        for (Map.Entry<String, List<FormInfo.OverrideInfo>> entry : overridesByRegion.entrySet()) {
            String region = entry.getKey();
            List<FormInfo.OverrideInfo> overrides = entry.getValue();

            writer.println("     ===== " + region + " =====");

            Set<String> fullReplacements = new LinkedHashSet<>();
            Set<String> partialDfrm = new LinkedHashSet<>();
            Map<String, Set<String>> dotDCatalogs = new LinkedHashMap<>();

            for (FormInfo.OverrideInfo override : overrides) {
                String path = override.getOverridePath();
                String fileName = path.substring(path.lastIndexOf("/") + 1);

                switch (override.getType()) {
                    case FULL_OVERRIDE:
                        fullReplacements.add(path);
                        break;
                    case PARTIAL_OVERRIDE:
                        partialDfrm.add(path);
                        break;
                    case DOT_D_OVERRIDE:
                        if (path.contains(".d/")) {
                            String catalogPath = path.substring(0, path.indexOf(".d/") + 2);
                            dotDCatalogs.computeIfAbsent(catalogPath, k -> new LinkedHashSet<>()).add(fileName);
                        } else {
                            partialDfrm.add(path);
                        }
                        break;
                }
            }

            for (String path : fullReplacements) {
                writer.println("        ПОЛНАЯ ЗАМЕНА: " + path);
            }

            for (Map.Entry<String, Set<String>> catalogEntry : dotDCatalogs.entrySet()) {
                String catalogPath = catalogEntry.getKey();
                writer.println("        КАТАЛОГ: " + catalogPath);
                for (String fileName : catalogEntry.getValue()) {
                    writer.println("            └── " + fileName);
                }
            }

            for (String path : partialDfrm) {
                writer.println("        ЧАСТИЧНОЕ ПЕРЕОПРЕДЕЛЕНИЕ: " + path);
            }

            writer.println();
        }

        writer.println();
    }

    /**
     * Рекурсивный вывод дерева пунктов меню
     */
    private void writeMenuTree(PrintWriter writer, List<PopupMenuInfo.MenuItem> items, String indent) {
        for (int i = 0; i < items.size(); i++) {
            PopupMenuInfo.MenuItem item = items.get(i);
            boolean isLast = (i == items.size() - 1);

            String branch = isLast ? "└── " : "├── ";
            String childIndent = indent + (isLast ? "    " : "│   "); // Oracle

            if (item.isDbReport()) {
                writer.println(item.getCaption());
            } else {
                String displayText = item.getPrefix() + item.getDisplayCaption();
                writer.println(indent + branch + displayText);
            }

            if (item.hasChildren()) {
                writeMenuTree(writer, item.getChildren(), childIndent);
            }
        }
    }


    /**
     * Вывод подробной информации о вьюхах (какие таблицы используются)
     */
    private void writeViewDetailsSection(PrintWriter writer, FormInfo formInfo,
                                         Map<String, ViewTableDependencies> viewDependencies) {
        if (viewDependencies == null || viewDependencies.isEmpty()) return;

        // Собираем вьюхи, используемые в форме
        Set<String> viewsUsed = new LinkedHashSet<>();
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_V_")) viewsUsed.add(tv);
        }
        if (viewsUsed.isEmpty()) {
            writer.println("  (нет вьюх для детального анализа)");
            writer.println();
            return;
        }

        writer.println("ДЕТАЛЬНОЕ СОДЕРЖИМОЕ ВЬЮХ (таблицы):");
        writer.println();

        for (String viewName : viewsUsed) {
            ViewTableDependencies deps = viewDependencies.get(viewName);
            if (deps == null || !deps.isExistsInOracle()) {
                if (deps != null && deps.getOracleError() != null) {
                    writer.println("  " + viewName + ":");
                    writer.println("      Ошибка: " + deps.getOracleError());
                    writer.println();
                }
                continue;
            }

            // Количество записей во вьюхе
            long viewOracleCount = getOracleCount(viewName);
            long viewPostgresCount = getPostgresCount(viewName);
            String viewCountStr = String.format(" (Oracle: %s) (PostgreSQL: %s)",
                    formatCount(viewOracleCount), formatCount(viewPostgresCount));

            writer.print("  " + viewName + viewCountStr + ":");
            writer.println();

            Set<String> tables = deps.getOracleTables();
            if (tables.isEmpty()) {
                writer.println("      (таблицы не найдены)");
            } else {
                for (String table : tables) {
                    if ("D_V_URPRIVS".equals(table)) continue;
                    long oracleCount = getOracleCount(table);
                    long postgresCount = getPostgresCount(table);
                    String tableCountStr = String.format(" (Oracle: %s) (PostgreSQL: %s)",
                            formatCount(oracleCount), formatCount(postgresCount));
                    writer.println("          " + table + tableCountStr);
                }
            }
            writer.println();
        }
    }


    private Set<String> getAllTablesForForm(FormInfo formInfo) {
        Set<String> allTables = new LinkedHashSet<>();
        // Прямые таблицы (без префикса D_V_)
        for (String tv : formInfo.getTablesViews()) {
            if (!tv.startsWith("D_V_")) {
                allTables.add(tv);
            }
        }
        // Таблицы из вьюх
        if (formInfo.getViewDependencies() != null) {
            for (Map.Entry<String, ViewTableDependencies> entry : formInfo.getViewDependencies().entrySet()) {
                allTables.addAll(entry.getValue().getOracleTables());
            }
        }
        // Исключаем служебные
        allTables.remove("D_V_URPRIVS");
        return allTables;
    }

    public void createMainReportHeader() throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        Path reportPath = outputPath.resolve("forms_report.txt");

        // Если файл уже существует, не перезаписываем заголовок
        if (Files.exists(reportPath)) {
            return;
        }

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(reportPath))) {
            writer.println("=".repeat(100));
            writer.println("=== ОТЧЕТ ПО ФОРМАМ T-MIS ===");
            writer.println("Дата создания: " + new Date());
            writer.println("=".repeat(100));
            writer.println();
        }
    }
    public void appendFormToMainReport(FormInfo formInfo) throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        Path reportPath = outputPath.resolve("forms_report.txt");

        // Если файл не существует, создаём с заголовком
        if (!Files.exists(reportPath)) {
            createMainReportHeader();
        }

        // Дописываем форму в конец файла
        try (PrintWriter writer = new PrintWriter(new FileWriter(reportPath.toFile(), true))) {
            writeFormReport(writer, formInfo);
        }
    }

    /**
     * Вывод списка вызываемых форм в JS
     */
    private void writeJsFormsBlock(PrintWriter writer, FormInfo form) {
        if (!form.getJsForms().isEmpty()) {
            writer.println("Список вызываемых форм в JS:");
            for (String jsForm : form.getJsForms()) {
                writer.println("     " + jsForm);
            }
            writer.println();
        }
    }
    /**
     * Вывод отчетов, вызываемых на форме
     */
    private void writeReportsBlock(PrintWriter writer, FormInfo form) {
        if (!form.getReports().isEmpty()) {
            writer.println("Отчеты вызываемые на форме (коды/формы отчета):");
            for (String report : form.getReports()) {
                writer.println("        " + report + ";");
            }
            writer.println();
        }
    }

    private void writeConstantsBlock(PrintWriter writer, FormInfo form) {
        if (!form.getConstants().isEmpty()) {
            writer.println("КОНСТАНТЫ:");
            for (String constant : form.getConstants()) {
                writer.println("    " + constant);
            }
            writer.println();
        }
    }

}