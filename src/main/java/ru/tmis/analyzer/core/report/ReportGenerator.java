// core/report/ReportGenerator.java
package ru.tmis.analyzer.core.report;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.core.db.*;
import ru.tmis.analyzer.core.llm.LLMPromptGenerator;
import ru.tmis.analyzer.core.model.*;

import ru.tmis.analyzer.core.db.PostgresPackageChecker;



import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReportGenerator {

    private final String outputDir;
    private final AppConfig config;
    private List<FormInfo> forms;
    private final SettingsModel settings;

    private final Map<String, Long> oracleCountCache = new ConcurrentHashMap<>();
    private final Map<String, Long> postgresCountCache = new ConcurrentHashMap<>();

    private OracleService oracleService;
    private PostgresService postgresService;
    private transient ReportsFromDbService reportsService;

    private boolean csvFileCreated = false;
    private Path csvPath;
    private final AtomicBoolean isAnalysisStopped = new AtomicBoolean(false);

    public ReportGenerator(String outputDir, AppConfig config) {
        this.outputDir = outputDir;
        this.config = config;
        this.forms = new ArrayList<>();
        this.settings = SettingsModel.getInstance();
        this.oracleService = new OracleService(settings.getOracleUrl(), settings.getOracleUser(), settings.getOraclePassword());
        this.postgresService = new PostgresService(settings.getPostgresUrl(), settings.getPostgresUser(), settings.getPostgresPassword(), settings.getMisUser());
        this.reportsService = new ReportsFromDbService(settings);
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


    /**
     * Формирует безопасное имя файла из пути формы
     * Заменяет все разделители пути на '#'
     * Пример: Forms/ArmPatientsInDep/SubForms/hh_mp_prescribes.frm -> Forms#ArmPatientsInDep#SubForms#hh_mp_prescribes.frm.txt
     */
    private String getSafeFileName(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        // Убираем маркер SubForm если есть
        if (normalized.startsWith("(sub)_")) {
            normalized = normalized.substring(6);
        }
        String safeName = normalized.replace("/", "#").replace("\\", "#");
        return safeName + ".txt";
    }
    /**
     * Получает полный путь к файлу отчёта с учётом подкаталога Forms
     * @param formPath путь к форме
     * @return полный путь к файлу отчёта
     */
    private Path getFormReportPath(String formPath) throws IOException {
        Path outputPath = Paths.get(outputDir);

        // Создаём подкаталог Forms внутри outputDir
        Path formsSubDir = outputPath.resolve("Forms");
        if (!Files.exists(formsSubDir)) {
            Files.createDirectories(formsSubDir);
        }

        String fileName = getSafeFileName(formPath);
        return formsSubDir.resolve(fileName);
    }
    /**
     * Сохраняет отчет для отдельной формы в отдельный файл
     */
    public void appendFormToMainReport(FormInfo formInfo) throws IOException {
        // 1. Сохраняем отдельный файл для формы (TXT)
        saveFormReportToFile(formInfo);

        // 2. Добавляем в общий отчет (только в forms_report.txt, без создания дубликата)
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        Path reportPath = outputPath.resolve("forms_report.txt");

        if (!Files.exists(reportPath)) {
            createMainReportHeader();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(reportPath.toFile(), true))) {
            writeFormReport(writer, formInfo);
        }

        // 3. Генерация CSV отчета (ОДИН РАЗ)
        if (config != null && config.isEnableCSVExport()) {
            appendToCSVReport(formInfo);

            // 4. Генерация отдельного CSV файла для формы
            SingleFormCSVReportGenerator singleCsvGen = new SingleFormCSVReportGenerator(outputDir);
            Path singleCsvPath = singleCsvGen.saveFormCSVReport(formInfo);
            System.out.println("  Отдельный CSV отчет сохранен: " + singleCsvPath);
        }

        // 5. Генерация JSON отчета
        if (config != null && config.isEnableJSONExport()) {
            appendToJSONReport(formInfo);
        }

        // 6. Генерация MD промпта
        if (config != null && config.isEnableLLMExport()) {
            generateLLMPromptForForm(formInfo);
        }
    }


    private void writeFormReport(PrintWriter writer, FormInfo form) {
        writer.println("-".repeat(100));
        writer.println("ФОРМА: " + form.getFormPath());
        writer.println("-".repeat(100));

        // Исправлено: относительный путь для базовой формы
        writer.println("Базовая форма: " + getRelativePath(form.getBaseFormPath()));
        if (form.isFullyReplaced()) {
            writer.println("СТАТУС: ПОЛНОСТЬЮ ЗАМЕНЕНА");
            // Исправлено: относительный путь для файла замены
            writer.println("Файл замены: " + getRelativePath(form.getReplacementPath()));
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

        // Отчеты вызываемые на форме
        if (!form.getReports().isEmpty()) {
            writer.println("Отчеты вызываемые на форме (коды/формы отчета):");
            for (String report : form.getReports()) {
                // Форматируем отчёт с информацией из БД (если нужно)
                String formattedReport = formatReportWithDbInfo(report);
                writer.println("        " + formattedReport + ";");
            }
            writer.println();
        }

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
            writer.println("ИСПОЛЬЗУЕМЫЕ ВЬЮХИ:");
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

        // unitCompositions
        if (!form.getUnitCompositions().isEmpty()) {
            writer.println("ВСЕ КОМПОЗИЦИИ UnitEdit на форме (JS+тэги):");
            for (String comp : form.getUnitCompositions()) {
                writer.println("    " + comp);
            }
            writer.println();
        }

        // блок для jsUnitCompositions
        if (!form.getJsUnitCompositions().isEmpty()) {
            writer.println("JS Unit Compositions (только из вызовов openWindow/openD3Form):");
            for (String comp : form.getJsUnitCompositions()) {
                writer.println("    " + comp);
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
        // ========== СТАТИСТИКА КОНВЕРТАЦИИ ==========
        ConversionStatistics stats = form.getConversionStatistics();
        if (stats != null && stats.getTotalQueries() > 0) {
            writer.println("СТАТИСТИКА КОНВЕРТАЦИИ SQL ЗАПРОСОВ:");
            writer.println("    Всего SQL запросов: " + stats.getTotalQueries());
            writer.println("    Конвертировано (с Router): " + stats.getConvertedQueries());
            writer.println("    Процент конвертации: " + String.format("%.1f%%", stats.getConversionPercent()));

            if (stats.isFullyConverted()) {
                writer.println("    СТАТУС: ✓ ФОРМА ПОЛНОСТЬЮ КОНВЕРТИРОВАНА ДЛЯ POSTGRESQL");
            } else if (stats.isNotConverted()) {
                writer.println("    СТАТУС: ✗ ФОРМА НЕ КОНВЕРТИРОВАНА");
            } else {
                writer.println("    СТАТУС: ⚠ ФОРМА КОНВЕРТИРОВАНА ЧАСТИЧНО");
            }
            writer.println();

            // Детали по каждому запросу
            writer.println("ДЕТАЛИ КОНВЕРТАЦИИ ЗАПРОСОВ:");
            for (Map.Entry<String, ConversionStatistics.QueryConversionInfo> entry :
                    stats.getQueryDetails().entrySet()) {
                ConversionStatistics.QueryConversionInfo info = entry.getValue();
                writer.println("    " + info.getComponentType() + ": " + info.getComponentName());
                writer.println("        Статус: " + info.getStatus());
                if (info.hasRouter()) {
                    writer.println("        Oracle SQL: " + (info.hasOracleSql() ? "✓ есть" : "✗ отсутствует"));
                    writer.println("        PostgreSQL SQL: " + (info.hasPostgresSql() ? "✓ есть" : "✗ отсутствует"));
                }
            }
            writer.println();
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
        formInfo.setTablesFromViews(allTables);
        System.out.println("[DEBUG] Сохранено таблиц в FormInfo: " + allTables.size());
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
                String relativePath = getRelativePath(override.getOverridePath());
                String fileName = relativePath.substring(relativePath.lastIndexOf("/") + 1);

                switch (override.getType()) {
                    case FULL_OVERRIDE:
                        fullReplacements.add(relativePath);
                        break;
                    case PARTIAL_OVERRIDE:
                        partialDfrm.add(relativePath);
                        break;
                    case DOT_D_OVERRIDE:
                        if (relativePath.contains(".d/")) {
                            String catalogPath = relativePath.substring(0, relativePath.indexOf(".d/") + 2);
                            dotDCatalogs.computeIfAbsent(catalogPath, k -> new LinkedHashSet<>()).add(fileName);
                        } else {
                            partialDfrm.add(relativePath);
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

    /**
     * Добавляет данные формы в JSON отчет
     */
    private void appendToJSONReport(FormInfo formInfo) throws IOException {
        JSONReportGenerator jsonGen = new JSONReportGenerator(outputDir, config);
        jsonGen.appendFormToJSON(formInfo);
        System.out.println("  JSON отчет обновлен");
    }


    private void appendToCSVReport(FormInfo formInfo) throws IOException {
        if (!config.isEnableCSVExport()) return;

        // Если анализ остановлен - не добавляем новые записи
        if (isAnalysisStopped.get()) {
            System.out.println("[CSV] Анализ остановлен, запись в CSV прекращена");
            return;
        }

        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        csvPath = outputPath.resolve("forms_export.csv");

        // Проверяем, есть ли уже эта форма в CSV
        if (Files.exists(csvPath) && isFormAlreadyInCsv(csvPath, formInfo.getFormPath())) {
            System.out.println("[CSV] Форма уже есть в общем CSV, пропускаем: " + formInfo.getFormPath());
            return;
        }

        boolean fileExists = Files.exists(csvPath);

        // Если файл не существует - создаём его (пустой с заголовком)
        if (!fileExists) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(csvPath.toFile()))) {
                writer.println("ФОРМА;БЛОК;ЗНАЧЕНИЕ");
                System.out.println("[CSV] Создан новый CSV файл: " + csvPath);
            }
            csvFileCreated = true;
        }

        // Добавляем данные формы
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvPath.toFile(), true))) {
            String formName = formInfo.getFormPath();

            // ... остальной код записи блоков ...
            writeCSVBlock(writer, formName, "Юзерформы", formInfo.getOverrides(),
                    formInfo.isFullyReplaced(), formInfo.getReplacementPath());
            writeCSVBlock(writer, formName, "subForm", formInfo.getSubForms());
            writeCSVBlock(writer, formName, "формы JS", formInfo.getJsForms());
            writeCSVBlock(writer, formName, "Отчеты, вызываемые на форме", formInfo.getReports());

            // Вьюхи (D_V_*)
            Set<String> views = new LinkedHashSet<>();
            for (String tv : formInfo.getTablesViews()) {
                if (tv.startsWith("D_V_")) {
                    views.add(tv);
                }
            }
            writeCSVBlock(writer, formName, "Вьюхи", views);

            // Таблицы
            Set<String> tables = formInfo.getTablesFromViews();
            if (tables == null || tables.isEmpty()) {
                tables = new LinkedHashSet<>();
            }
            for (String tv : formInfo.getTablesViews()) {
                if (tv.startsWith("D_") && !tv.startsWith("D_V_")) {
                    tables.add(tv);
                }
            }
            writeCSVBlock(writer, formName, "Таблицы", tables);

            // Пакеты и функции
            writeCSVBlock(writer, formName, "Пакеты и функции", formInfo.getPackagesFunctions());

            // СО
            writeCSVBlock(writer, formName, "СО", formInfo.getSystemOptions());

            // Универсальные композиции
            Set<String> allCompositions = new LinkedHashSet<>();
            if (formInfo.getUnitCompositions() != null) {
                allCompositions.addAll(formInfo.getUnitCompositions());
            }
            if (formInfo.getJsUnitCompositions() != null) {
                allCompositions.addAll(formInfo.getJsUnitCompositions());
            }
            writeCSVBlock(writer, formName, "Универсальные композиции", allCompositions);

            // Неопределенные
            writeCSVBlock(writer, formName, "Неопределенные", formInfo.getUnknownObjects());

            System.out.println("[CSV] Данные формы добавлены в CSV: " + formName);
        }
    }

    /**
     * Записывает блок в CSV (для простых коллекций)
     */
    private void writeCSVBlock(PrintWriter writer, String formName, String blockName, Set<String> values) {
        System.out.println("[CSV] writeCSVBlock: block=" + blockName + ", values.size=" + (values == null ? "null" : values.size()));

        if (values == null || values.isEmpty()) {
            return;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty() && !value.equals("(не найдено)")) {
                writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV(value));
                System.out.println("[CSV] Записано: " + formName + ";" + blockName + ";" + value);
            }
        }
    }

    /**
     * Записывает блок UserForms в CSV
     */
    private void writeCSVBlock(PrintWriter writer, String formName, String blockName,
                               List<FormInfo.OverrideInfo> overrides, boolean fullyReplaced, String replacementPath) {

        if (fullyReplaced && replacementPath != null) {
            String relativePath = getRelativePath(replacementPath);
            writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV(relativePath));
        }

        if (overrides == null || overrides.isEmpty()) {
            return;
        }

        Map<String, List<FormInfo.OverrideInfo>> overridesByRegion = new LinkedHashMap<>();
        for (FormInfo.OverrideInfo override : overrides) {
            overridesByRegion.computeIfAbsent(override.getRegionName(), k -> new ArrayList<>()).add(override);
        }

        for (Map.Entry<String, List<FormInfo.OverrideInfo>> entry : overridesByRegion.entrySet()) {
            String region = entry.getKey();
            for (FormInfo.OverrideInfo override : entry.getValue()) {
                String relativePath = getRelativePath(override.getOverridePath());
                String value =  getRelativePathWithinRegion(override.getOverridePath(), region);
                writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV(value));
            }
        }
    }

    /**
     * Получить относительный путь от корня проекта
     */
    private String getRelativePath(String absolutePath) {
        if (absolutePath == null) return "";

        String projectPath = settings.getProjectPath();
        if (projectPath == null || projectPath.isEmpty()) {
            return absolutePath;
        }

        String normalizedProject = projectPath.replace("\\", "/");
        String normalizedPath = absolutePath.replace("\\", "/");

        if (normalizedPath.startsWith(normalizedProject)) {
            String relative = normalizedPath.substring(normalizedProject.length());
            if (relative.startsWith("/")) {
                relative = relative.substring(1);
            }
            return relative;
        }

        return absolutePath;
    }

    /**
     * Получить путь относительно региона UserForms
     */
    private String getRelativePathWithinRegion(String absolutePath, String region) {
        if (absolutePath == null || region == null) return "";

        String normalizedPath = absolutePath.replace("\\", "/");
        String normalizedRegion = region.replace("\\", "/");

        int regionIndex = normalizedPath.indexOf(normalizedRegion);
        if (regionIndex >= 0) {
            String relative = normalizedPath.substring(regionIndex);
            return relative;
        }

        // Если не нашли регион, пробуем извлечь имя файла
        int lastSlash = normalizedPath.lastIndexOf("/");
        if (lastSlash >= 0) {
            return normalizedPath.substring(lastSlash + 1);
        }

        return absolutePath;
    }


    /**
     * Получает все таблицы из вьюх
     */
    private Set<String> getViewTables(FormInfo formInfo) {
        Set<String> viewTables = new LinkedHashSet<>();

        // Важно: проверяем через formInfo.getViewDependencies()
        Map<String, ViewTableDependencies> deps = formInfo.getViewDependencies();
        if (deps != null && !deps.isEmpty()) {
            for (Map.Entry<String, ViewTableDependencies> entry : deps.entrySet()) {
                ViewTableDependencies dep = entry.getValue();
                if (dep != null && dep.getOracleTables() != null) {
                    viewTables.addAll(dep.getOracleTables());
                }
            }
        }

        return viewTables;
    }

    /**
     * Генерирует MD промпт для формы
     */
    private void generateLLMPromptForForm(FormInfo formInfo) {
        try {
            LLMPromptGenerator llmGen = new LLMPromptGenerator(config);
            String mdFilePath = llmGen.generateForSingleForm(formInfo, outputDir);
            System.out.println("  LLM промпт сохранен: " + mdFilePath);
        } catch (Exception e) {
            System.err.println("  Ошибка сохранения LLM промпта: " + e.getMessage());
        }
    }

    /**
     * Экранирование CSV
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        String escaped = value.replace(";", ",");
        if (escaped.contains("\"")) {
            escaped = escaped.replace("\"", "\"\"");
        }
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
            escaped = "\"" + escaped + "\"";
        }
        return escaped;
    }
    /**
     * Получает таблицы из вьюх, читая из уже сохранённого TXT отчёта
     * Это гарантирует соответствие данных между TXT и CSV
     */
    private Set<String> getTablesFromTxtReport(FormInfo formInfo) {
        Set<String> tables = new LinkedHashSet<>();

        // Формируем путь к TXT отчёту
        String fileName = getSafeFileName(formInfo.getFormPath());
        Path reportPath = Paths.get(outputDir, fileName);

        if (!Files.exists(reportPath)) {
            System.out.println("[CSV] TXT отчёт не найден: " + reportPath);
            return tables;
        }

        try {
            String content = new String(Files.readAllBytes(reportPath),
                    java.nio.charset.StandardCharsets.UTF_8);

            // Ищем блок "ТАБЛИЦЫ, ИСПОЛЬЗУЕМЫЕ ЧЕРЕЗ ВЬЮХИ (уникальные для этой формы):"
            String marker = "ТАБЛИЦЫ, ИСПОЛЬЗУЕМЫЕ ЧЕРЕЗ ВЬЮХИ (уникальные для этой формы):";
            int startIndex = content.indexOf(marker);

            if (startIndex != -1) {
                // Находим конец блока (следующий заголовок или конец файла)
                int endIndex = content.indexOf("\n\n", startIndex + marker.length());
                if (endIndex == -1) {
                    endIndex = content.length();
                }

                String section = content.substring(startIndex + marker.length(), endIndex);

                // Извлекаем все D_* таблицы
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\s+(D_[A-Z0-9_]+)");
                java.util.regex.Matcher matcher = pattern.matcher(section);

                while (matcher.find()) {
                    String table = matcher.group(1);
                    if (!table.isEmpty() && !table.startsWith("D_V_")) {
                        tables.add(table);
                    }
                }

                System.out.println("[CSV] Из TXT отчёта извлечено таблиц: " + tables.size());
            } else {
                System.out.println("[CSV] Блок таблиц не найден в TXT отчёте");
            }

        } catch (IOException e) {
            System.err.println("[CSV] Ошибка чтения TXT отчёта: " + e.getMessage());
        }

        return tables;
    }




    /**
     * Универсальный метод для извлечения данных из TXT отчёта по заголовку блока
     * @param formInfo информация о форме
     * @param blockTitle заголовок блока (например, "ТАБЛИЦЫ, ИСПОЛЬЗУЕМЫЕ ЧЕРЕЗ ВЬЮХИ")
     * @param pattern регулярное выражение для извлечения значений
     * @return множество извлечённых значений
     */
    private Set<String> extractFromTxtReport(FormInfo formInfo, String blockTitle, String pattern) {
        Set<String> result = new LinkedHashSet<>();

        String fileName = getSafeFileName(formInfo.getFormPath());
        Path reportPath = Paths.get(outputDir, fileName);

        if (!Files.exists(reportPath)) {
            return result;
        }

        try {
            String content = new String(Files.readAllBytes(reportPath),
                    java.nio.charset.StandardCharsets.UTF_8);

            int startIndex = content.indexOf(blockTitle);
            if (startIndex != -1) {
                int endIndex = content.indexOf("\n\n", startIndex + blockTitle.length());
                if (endIndex == -1) {
                    endIndex = content.length();
                }

                String section = content.substring(startIndex + blockTitle.length(), endIndex);

                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(section);

                while (m.find()) {
                    String value = m.group(1);
                    if (value != null && !value.isEmpty()) {
                        result.add(value);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения TXT отчёта: " + e.getMessage());
        }

        return result;
    }
    /**
     * Извлекает ТОЛЬКО таблицы из блока "ТАБЛИЦЫ, ИСПОЛЬЗУЕМЫЕ ЧЕРЕЗ ВЬЮХИ"
     * Исключает пакеты D_PKG_*
     */
    private Set<String> extractTablesFromTxtReport(FormInfo formInfo) {
        Set<String> tables = new LinkedHashSet<>();

        String fileName = getSafeFileName(formInfo.getFormPath());
        Path reportPath = Paths.get(outputDir, fileName);

        if (!Files.exists(reportPath)) {
            return tables;
        }

        try {
            String content = new String(Files.readAllBytes(reportPath),
                    java.nio.charset.StandardCharsets.UTF_8);

            String blockTitle = "ТАБЛИЦЫ, ИСПОЛЬЗУЕМЫЕ ЧЕРЕЗ ВЬЮХИ (уникальные для этой формы):";
            int startIndex = content.indexOf(blockTitle);

            if (startIndex != -1) {
                int endIndex = content.indexOf("\n\n", startIndex + blockTitle.length());
                if (endIndex == -1) {
                    endIndex = content.length();
                }

                String section = content.substring(startIndex + blockTitle.length(), endIndex);

                // Разбиваем по строкам
                for (String line : section.split("\\r?\\n")) {
                    String trimmed = line.trim();
                    // Только D_* но не D_PKG_* и не пустые
                    if (trimmed.startsWith("D_") && !trimmed.startsWith("D_PKG_") && !trimmed.isEmpty()) {
                        tables.add(trimmed);
                        System.out.println("[CSV] Таблица: " + trimmed);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения TXT отчёта: " + e.getMessage());
        }

        System.out.println("[CSV] Итого таблиц: " + tables.size());
        return tables;
    }
    /**
     * Форматирует отчёт с информацией из БД, если это код
     */
    private String formatReportWithDbInfo(String report) {
        // Если это путь к файлу (содержит / или .frm) - возвращаем как есть
        if (report.contains("/") || report.endsWith(".frm")) {
            return report;
        }

        // Проверяем кэш в ReportsFromDbService
        DbReportInfo dbReport = reportsService.getReportByCode(report);
        if (dbReport != null) {
            String typeName = dbReport.getRepTypeName();
            StringBuilder sb = new StringBuilder();
            sb.append(report).append(" (").append(typeName).append(")");

            // Если REP_TYPE = 1 (WEB-форма) и есть REP_FILENAME
            if (dbReport.getRepType() == 1 && dbReport.getRepFilename() != null && !dbReport.getRepFilename().isEmpty()) {
                String formPath = dbReport.getRepFilename();
                if (!formPath.endsWith(".frm")) {
                    formPath = formPath + ".frm";
                }
                if (!formPath.startsWith("Reports/")) {
                    formPath = "Reports/" + formPath;
                }
                sb.append(" ").append(formPath);
            }
            return sb.toString();
        }

        return report;
    }

    /**
     * Выводит общую статистику конвертации по всем формам
     */
    private void writeConversionSummary(PrintWriter writer) {
        if (forms.isEmpty()) return;

        // Собираем статистику из всех форм
        int totalForms = 0;
        int convertedForms = 0;
        int notConvertedForms = 0;
        int totalQueries = 0;
        int convertedQueries = 0;

        for (FormInfo form : forms) {
            ConversionStatistics stats = form.getConversionStatistics();
            if (stats != null && stats.getTotalQueries() > 0) {
                totalForms++;
                totalQueries += stats.getTotalQueries();
                convertedQueries += stats.getConvertedQueries();

                if (stats.isFullyConverted()) {
                    convertedForms++;
                } else if (stats.isNotConverted()) {
                    notConvertedForms++;
                }
            }
        }

        if (totalForms == 0) return;

        writer.println();
        writer.println("=".repeat(100));
        writer.println("=== ОБЩАЯ СТАТИСТИКА КОНВЕРТАЦИИ ===");
        writer.println("=".repeat(100));
        writer.println();
        writer.println("Формы с SQL запросами: " + totalForms);
        writer.println("  Полностью конвертировано: " + convertedForms +
                " (" + String.format("%.1f", (convertedForms * 100.0) / totalForms) + "%)");
        writer.println("  Частично конвертировано: " + (totalForms - convertedForms - notConvertedForms));
        writer.println("  Не конвертировано: " + notConvertedForms +
                " (" + String.format("%.1f", (notConvertedForms * 100.0) / totalForms) + "%)");
        writer.println();
        writer.println("SQL запросы:");
        writer.println("  Всего: " + totalQueries);
        writer.println("  Конвертировано: " + convertedQueries +
                " (" + String.format("%.1f", (convertedQueries * 100.0) / totalQueries) + "%)");
        writer.println();
    }

    /**
     * Сохраняет отчет для отдельной формы в отдельный файл в подкаталоге Forms
     */
    public void saveFormReportToFile(FormInfo formInfo) throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        // Создаём подкаталог Forms внутри outputDir
        Path formsSubDir = outputPath.resolve("Forms");
        if (!Files.exists(formsSubDir)) {
            Files.createDirectories(formsSubDir);
        }

        String fileName = getSafeFileName(formInfo.getFormPath());
        Path formReportPath = formsSubDir.resolve(fileName);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(formReportPath))) {
            writeFormReport(writer, formInfo);
        }

        System.out.println("Отчет для формы сохранен: " + formReportPath);
    }
    /**
     * Проверяет, есть ли уже форма в общем CSV отчете
     */
    private boolean isFormAlreadyInCsv(Path csvPath, String formName) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                // Извлекаем имя формы из первой колонки
                String[] parts = line.split(";", 3);
                if (parts.length >= 1) {
                    String existingForm = parts[0];
                    if (existingForm.startsWith("\"") && existingForm.endsWith("\"")) {
                        existingForm = existingForm.substring(1, existingForm.length() - 1);
                    }
                    if (existingForm.equals(formName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    // Добавить метод для отметки остановки
    public void markStopped() {
        isAnalysisStopped.set(true);
    }
    public void cleanupCsvOnStop() {
        if (csvFileCreated && csvPath != null && Files.exists(csvPath)) {
            try {
                Files.delete(csvPath);
                System.out.println("[CSV] Удалён неполный CSV файл: " + csvPath);
            } catch (IOException e) {
                System.err.println("[CSV] Ошибка удаления CSV файла: " + e.getMessage());
            }
        }
    }

}