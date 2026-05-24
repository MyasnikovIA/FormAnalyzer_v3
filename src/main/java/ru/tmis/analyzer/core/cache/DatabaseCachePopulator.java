// core/cache/DatabaseCachePopulator.java
package ru.tmis.analyzer.core.cache;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.db.*;
import ru.tmis.analyzer.core.log.ILogger;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DatabaseCachePopulator {

    private final SettingsModel settings;
    private final OracleService oracleService;
    private final PostgresService postgresService;
    private final ReportsFromDbService reportsService;
    private ILogger logger;
    private AtomicBoolean stopRequested = new AtomicBoolean(false);
    private Consumer<String> progressCallback;

    private int viewsLoaded = 0;
    private int tablesLoaded = 0;
    private int functionsLoaded = 0;
    private int reportsLoaded = 0;
    private int brokersLoaded = 0;

    public DatabaseCachePopulator(SettingsModel settings) {
        this.settings = settings;
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
        this.reportsService = new ReportsFromDbService(settings);
    }

    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }

    public void setStopRequested(AtomicBoolean stopRequested) {
        this.stopRequested = stopRequested;
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

    private void updateProgress(String status) {
        if (progressCallback != null) {
            progressCallback.accept(status);
        }
    }

    public PopulateResult populateAll() {
        log("=== НАЧАЛО ЗАПОЛНЕНИЯ КЭША ДАННЫМИ ИЗ БД ===");
        long startTime = System.currentTimeMillis();

        PopulateResult result = new PopulateResult();

        if (!stopRequested.get()) {
            loadAllOracleViews();
            result.oracleViewsCount = viewsLoaded;
        }

        if (!stopRequested.get()) {
            loadAllPostgresViews();
            result.postgresViewsCount = viewsLoaded - result.oracleViewsCount;
        }

        if (!stopRequested.get()) {
            loadAllTablesFromViews();
            result.tablesCount = tablesLoaded;
        }

        if (!stopRequested.get()) {
            loadAllOraclePackageFunctions();
            result.oracleFunctionsCount = functionsLoaded;
        }

        if (!stopRequested.get()) {
            loadAllPostgresFunctions();
            result.postgresFunctionsCount = functionsLoaded - result.oracleFunctionsCount;
        }

        if (!stopRequested.get()) {
            loadAllBrokers();
            result.brokersCount = brokersLoaded;
        }

        if (!stopRequested.get()) {
            loadAllOracleReports();
            result.oracleReportsCount = reportsLoaded;
        }

        if (!stopRequested.get()) {
            loadAllPostgresReports();
            result.postgresReportsCount = reportsLoaded - result.oracleReportsCount;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        result.elapsedMs = elapsed;

        log("=== ЗАПОЛНЕНИЕ КЭША ЗАВЕРШЕНО ===");
        log("Время выполнения: " + (elapsed / 1000) + " сек");
        log(result.toString());

        DatabaseCacheManager.forceSaveToDisk();

        return result;
    }

    private void loadAllOracleViews() {
        log("[Oracle] Загрузка всех вьюх...");
        updateProgress("Загрузка вьюх из Oracle...");

        String sql = "SELECT VIEW_NAME FROM ALL_VIEWS WHERE OWNER = ? AND VIEW_NAME LIKE 'D\\_%' ESCAPE '\\'";

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            pstmt.setQueryTimeout(60);
            ResultSet rs = pstmt.executeQuery();

            List<String> viewNames = new ArrayList<>();
            while (rs.next()) {
                viewNames.add(rs.getString("VIEW_NAME"));
            }

            log("[Oracle] Найдено вьюх: " + viewNames.size());
            updateProgress("Загрузка DDL вьюх Oracle (" + viewNames.size() + " шт.)...");

            int total = viewNames.size();
            for (int i = 0; i < total; i++) {
                if (stopRequested.get()) return;
                String viewName = viewNames.get(i);
                final int currentIndex = i + 1;
                final int currentTotal = total;

                if (currentIndex % 50 == 0) {
                    updateProgress("Oracle вьюхи: " + currentIndex + "/" + currentTotal);
                }

                DatabaseCacheManager.getOracleViewDDL(viewName, () -> {
                    log("  [" + currentIndex + "/" + currentTotal + "] Загрузка вьюхи: " + viewName);
                    return oracleService.getViewDDL(viewName);
                });
                viewsLoaded++;
            }

        } catch (SQLException e) {
            error("[Oracle] Ошибка: " + e.getMessage());
        }
    }

    private void loadAllPostgresViews() {
        log("[PostgreSQL] Загрузка всех вьюх...");
        updateProgress("Загрузка вьюх из PostgreSQL...");

        String sql = "SELECT schemaname, viewname FROM pg_views WHERE schemaname = 'public' AND viewname LIKE 'D\\_%'";

        try (Connection conn = getPostgresConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(60);
            ResultSet rs = stmt.executeQuery(sql);

            List<String> viewNames = new ArrayList<>();
            while (rs.next()) {
                viewNames.add(rs.getString("viewname"));
            }

            log("[PostgreSQL] Найдено вьюх: " + viewNames.size());
            updateProgress("Загрузка DDL вьюх PostgreSQL (" + viewNames.size() + " шт.)...");

            int total = viewNames.size();
            for (int i = 0; i < total; i++) {
                if (stopRequested.get()) return;
                String viewName = viewNames.get(i);
                final int currentIndex = i + 1;
                final int currentTotal = total;

                if (currentIndex % 50 == 0) {
                    updateProgress("PostgreSQL вьюхи: " + currentIndex + "/" + currentTotal);
                }

                DatabaseCacheManager.getPostgresViewDDL(viewName, () -> {
                    log("  [" + currentIndex + "/" + currentTotal + "] Загрузка вьюхи: " + viewName);
                    return postgresService.getViewDDL(viewName);
                });
                viewsLoaded++;
            }

        } catch (SQLException e) {
            error("[PostgreSQL] Ошибка: " + e.getMessage());
        }
    }

    private void loadAllTablesFromViews() {
        log("[DB] Загрузка DDL таблиц...");
        updateProgress("Загрузка DDL таблиц...");

        Set<String> allTables = new LinkedHashSet<>();

        String oracleSql = "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME LIKE 'D\\_%' ESCAPE '\\'";

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(oracleSql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                allTables.add(rs.getString("TABLE_NAME"));
            }

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки списка таблиц: " + e.getMessage());
        }

        log("[DB] Найдено таблиц: " + allTables.size());
        updateProgress("Загрузка DDL таблиц (" + allTables.size() + " шт.)...");

        List<String> tableList = new ArrayList<>(allTables);
        int total = tableList.size();

        for (int i = 0; i < total; i++) {
            if (stopRequested.get()) return;
            String tableName = tableList.get(i);
            final int currentIndex = i + 1;
            final int currentTotal = total;

            if (currentIndex % 50 == 0) {
                updateProgress("Таблицы: " + currentIndex + "/" + currentTotal);
            }

            DatabaseCacheManager.getOracleTableDDL(tableName, () -> {
                log("  [" + currentIndex + "/" + currentTotal + "] Oracle таблица: " + tableName);
                return oracleService.getTableDDL(tableName);
            });

            DatabaseCacheManager.getPostgresTableDDL(tableName, () -> {
                return postgresService.getTableDDL(tableName);
            });

            tablesLoaded++;
        }
    }

    private void loadAllOraclePackageFunctions() {
        log("[Oracle] Загрузка пакетных функций...");
        updateProgress("Загрузка функций Oracle...");

        String sql = "SELECT DISTINCT NAME FROM ALL_SOURCE " +
                "WHERE OWNER = ? AND TYPE = 'PACKAGE' AND NAME LIKE 'D\\_PKG\\_%' ESCAPE '\\'";

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            pstmt.setQueryTimeout(60);
            ResultSet rs = pstmt.executeQuery();

            List<String> packageNames = new ArrayList<>();
            while (rs.next()) {
                packageNames.add(rs.getString("NAME"));
            }

            log("[Oracle] Найдено пакетов: " + packageNames.size());

            int pkgCount = 0;
            int funcCount = 0;
            int totalPackages = packageNames.size();

            for (int i = 0; i < totalPackages; i++) {
                if (stopRequested.get()) break;
                String packageName = packageNames.get(i);
                pkgCount++;
                final int currentPkgIndex = i + 1;

                updateProgress("Oracle функции: пакет " + currentPkgIndex + "/" + totalPackages);

                String funcSql = "SELECT DISTINCT NAME FROM ALL_SOURCE " +
                        "WHERE OWNER = ? AND TYPE = 'PACKAGE' AND NAME = ? AND TEXT LIKE '%FUNCTION%'";

                try (PreparedStatement pstmt2 = conn.prepareStatement(funcSql)) {
                    pstmt2.setString(1, settings.getOracleUser().toUpperCase());
                    pstmt2.setString(2, packageName);
                    ResultSet rs2 = pstmt2.executeQuery();

                    List<String> funcNames = new ArrayList<>();
                    while (rs2.next()) {
                        funcNames.add(rs2.getString("NAME"));
                    }

                    for (String funcName : funcNames) {
                        final String finalPackageName = packageName;
                        final String finalFuncName = funcName;
                        String fullName = packageName + "." + funcName;

                        DatabaseCacheManager.getOracleFunctionBody(fullName, () -> {
                            log("  Загрузка функции: " + finalPackageName + "." + finalFuncName);
                            return oracleService.getFunctionBody(finalPackageName, finalFuncName);
                        });
                        functionsLoaded++;
                        funcCount++;
                    }
                }
            }

            log("[Oracle] Загружено функций: " + funcCount);

        } catch (SQLException e) {
            error("[Oracle] Ошибка: " + e.getMessage());
        }
    }

    private void loadAllPostgresFunctions() {
        log("[PostgreSQL] Загрузка всех функций...");
        updateProgress("Загрузка функций PostgreSQL...");

        String sql = "SELECT proname FROM pg_proc p " +
                "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                "WHERE n.nspname = 'public' AND proname LIKE 'd\\_%'";

        try (Connection conn = getPostgresConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(60);
            ResultSet rs = stmt.executeQuery(sql);

            List<String> functionNames = new ArrayList<>();
            while (rs.next()) {
                functionNames.add(rs.getString("proname"));
            }

            log("[PostgreSQL] Найдено функций: " + functionNames.size());
            updateProgress("Загрузка DDL функций PostgreSQL (" + functionNames.size() + " шт.)...");

            int total = functionNames.size();
            for (int i = 0; i < total; i++) {
                if (stopRequested.get()) break;
                String funcName = functionNames.get(i);
                final int currentIndex = i + 1;
                final int currentTotal = total;

                if (currentIndex % 50 == 0) {
                    updateProgress("PostgreSQL функции: " + currentIndex + "/" + currentTotal);
                }

                DatabaseCacheManager.getPostgresFunctionBody(funcName, () -> {
                    log("  [" + currentIndex + "/" + currentTotal + "] Функция: " + funcName);
                    return postgresService.getFunctionBody(funcName);
                });
                functionsLoaded++;
            }

        } catch (SQLException e) {
            error("[PostgreSQL] Ошибка: " + e.getMessage());
        }
    }

    private void loadAllBrokers() {
        log("[Oracle] Загрузка всех брокеров...");
        updateProgress("Загрузка брокеров...");

        String sql = "SELECT unitbpcode, standard_action, execproc FROM D_UNITBPS";

        try (Connection conn = getOracleConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(60);
            ResultSet rs = stmt.executeQuery(sql);

            int count = 0;
            while (rs.next()) {
                if (stopRequested.get()) break;
                count++;
                final int currentCount = count;

                String unit = rs.getString("unitbpcode");
                String action = rs.getString("standard_action");
                String execProc = rs.getString("execproc");

                if (unit != null && action != null && execProc != null) {
                    final String finalUnit = unit;
                    final String finalAction = action;
                    final String finalExecProc = execProc;

                    DatabaseCacheManager.getBrokerExecProc(unit, action, () -> {
                        log("  [" + currentCount + "] Брокер: " + finalUnit + "_" + finalAction + " -> " + finalExecProc);
                        return finalExecProc;
                    });
                    brokersLoaded++;
                }
            }

            log("[Oracle] Загружено брокеров: " + brokersLoaded);

        } catch (SQLException e) {
            error("[Oracle] Ошибка: " + e.getMessage());
        }
    }

    private void loadAllOracleReports() {
        log("[Oracle] Загрузка всех отчётов...");
        updateProgress("Загрузка отчётов Oracle...");

        String sql = "SELECT DISTINCT UNITCODE FROM D_REPORTS_LINKS";

        try (Connection conn = getOracleConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(60);
            ResultSet rs = stmt.executeQuery(sql);

            List<String> unitCodes = new ArrayList<>();
            while (rs.next()) {
                String unitCode = rs.getString("UNITCODE");
                if (unitCode != null && !unitCode.isEmpty()) {
                    unitCodes.add(unitCode);
                }
            }

            log("[Oracle] Найдено UNITCODE: " + unitCodes.size());
            updateProgress("Загрузка отчётов Oracle (" + unitCodes.size() + " шт.)...");

            int total = unitCodes.size();
            for (int i = 0; i < total; i++) {
                if (stopRequested.get()) break;
                String unitCode = unitCodes.get(i);
                final int currentIndex = i + 1;
                final int currentTotal = total;
                final String finalUnitCode = unitCode;

                if (currentIndex % 50 == 0) {
                    updateProgress("Oracle отчёты: " + currentIndex + "/" + currentTotal);
                }

                DatabaseCacheManager.getOracleReports(unitCode, () -> {
                    log("  [" + currentIndex + "/" + currentTotal + "] Загрузка отчётов для unit=" + finalUnitCode);
                    return reportsService.getReportsByUnit(finalUnitCode);
                });
                reportsLoaded++;
            }

            log("[Oracle] Загружено отчётов для " + reportsLoaded + " unit'ов");

        } catch (SQLException e) {
            error("[Oracle] Ошибка: " + e.getMessage());
        }
    }

    private void loadAllPostgresReports() {
        log("[PostgreSQL] Загрузка всех отчётов...");
        updateProgress("Загрузка отчётов PostgreSQL...");

        String sql = "SELECT DISTINCT unitcode FROM d_reports_links";

        try (Connection conn = getPostgresConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(60);
            ResultSet rs = stmt.executeQuery(sql);

            List<String> unitCodes = new ArrayList<>();
            while (rs.next()) {
                String unitCode = rs.getString("unitcode");
                if (unitCode != null && !unitCode.isEmpty()) {
                    unitCodes.add(unitCode);
                }
            }

            log("[PostgreSQL] Найдено UNITCODE: " + unitCodes.size());
            updateProgress("Загрузка отчётов PostgreSQL (" + unitCodes.size() + " шт.)...");

            PostgresReportsService pgReportsService = new PostgresReportsService(settings);
            int total = unitCodes.size();

            for (int i = 0; i < total; i++) {
                if (stopRequested.get()) break;
                String unitCode = unitCodes.get(i);
                final int currentIndex = i + 1;
                final int currentTotal = total;
                final String finalUnitCode = unitCode;

                if (currentIndex % 50 == 0) {
                    updateProgress("PostgreSQL отчёты: " + currentIndex + "/" + currentTotal);
                }

                DatabaseCacheManager.getPostgresReports(unitCode, () -> {
                    log("  [" + currentIndex + "/" + currentTotal + "] Загрузка отчётов PostgreSQL для unit=" + finalUnitCode);
                    return pgReportsService.getReportsByUnit(finalUnitCode);
                });
                reportsLoaded++;
            }

            log("[PostgreSQL] Загружено отчётов для " + reportsLoaded + " unit'ов");

        } catch (SQLException e) {
            error("[PostgreSQL] Ошибка: " + e.getMessage());
        }
    }

    private Connection getOracleConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", settings.getOracleUser());
        props.setProperty("password", settings.getOraclePassword());
        props.setProperty("oracle.net.CONNECT_TIMEOUT", "30000");
        props.setProperty("oracle.jdbc.ReadTimeout", "60000");
        return DriverManager.getConnection(settings.getOracleUrl(), props);
    }

    private Connection getPostgresConnection() throws SQLException {
        DriverManager.setLoginTimeout(30);
        Connection conn = DriverManager.getConnection(
                settings.getPostgresUrl(),
                settings.getPostgresUser(),
                settings.getPostgresPassword()
        );
        conn.setAutoCommit(true);
        return conn;
    }

    public static class PopulateResult {
        public int oracleViewsCount = 0;
        public int postgresViewsCount = 0;
        public int tablesCount = 0;
        public int oracleFunctionsCount = 0;
        public int postgresFunctionsCount = 0;
        public int brokersCount = 0;
        public int oracleReportsCount = 0;
        public int postgresReportsCount = 0;
        public long elapsedMs = 0;
        public int constantsCount = 0;
        public int systemOptionsCount = 0;
        public int tablesMetadataCount = 0;
        public int synonymsCount = 0;
        public int sequencesCount = 0;
        public int triggersCount = 0;

        @Override
        public String toString() {
            return String.format(
                    "Результат:\n" +
                            "  Oracle вьюхи: %d\n" +
                            "  PostgreSQL вьюхи: %d\n" +
                            "  Таблицы: %d\n" +
                            "  Oracle функции: %d\n" +
                            "  PostgreSQL функции: %d\n" +
                            "  Брокеры: %d\n" +
                            "  Oracle отчёты: %d\n" +
                            "  PostgreSQL отчёты: %d\n" +
                            "  Константы: %d\n" +
                            "  Системные опции: %d\n" +
                            "  Метаданные таблиц: %d\n" +
                            "  Синонимы: %d\n" +
                            "  Всего объектов: %d\n" +
                            "  Время: %.1f сек",
                    oracleViewsCount, postgresViewsCount, tablesCount,
                    oracleFunctionsCount, postgresFunctionsCount,
                    brokersCount, oracleReportsCount, postgresReportsCount,
                    constantsCount, systemOptionsCount, tablesMetadataCount, synonymsCount,
                    oracleViewsCount + postgresViewsCount + tablesCount +
                            oracleFunctionsCount + postgresFunctionsCount +
                            brokersCount + oracleReportsCount + postgresReportsCount +
                            constantsCount + systemOptionsCount + tablesMetadataCount + synonymsCount,
                    elapsedMs / 1000.0
            );
        }
    }
    /**
     * Загрузка всех констант из D_PKG_CONSTANTS
     */
    private void loadAllConstants() {
        log("[Oracle] Загрузка всех констант...");
        updateProgress("Загрузка констант...");

        String sql = "SELECT CONST_CODE, CONST_VALUE FROM D_PKG_CONSTANTS";

        try (Connection conn = getOracleConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(60);
            ResultSet rs = stmt.executeQuery(sql);

            int count = 0;
            while (rs.next()) {
                if (stopRequested.get()) break;
                String constCode = rs.getString("CONST_CODE");
                String constValue = rs.getString("CONST_VALUE");

                DatabaseCacheManager.getConstant(constCode, () -> constValue);
                count++;

                if (count % 100 == 0) {
                    updateProgress("Константы: " + count);
                }
            }

            log("[Oracle] Загружено констант: " + count);

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки констант: " + e.getMessage());
        }
    }

    /**
     * Загрузка всех системных опций
     */
    private void loadAllSystemOptions() {
        log("[Oracle] Загрузка системных опций...");
        updateProgress("Загрузка системных опций...");

        String sql = "SELECT OPTION_CODE, OPTION_VALUE FROM D_SYSTEM_OPTIONS";

        try (Connection conn = getOracleConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(60);
            ResultSet rs = stmt.executeQuery(sql);

            int count = 0;
            while (rs.next()) {
                if (stopRequested.get()) break;
                String optionCode = rs.getString("OPTION_CODE");
                String optionValue = rs.getString("OPTION_VALUE");

                DatabaseCacheManager.getSystemOption(optionCode, () -> optionValue);
                count++;

                if (count % 100 == 0) {
                    updateProgress("Системные опции: " + count);
                }
            }

            log("[Oracle] Загружено системных опций: " + count);

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки опций: " + e.getMessage());
        }
    }

    /**
     * Загрузка метаданных всех таблиц
     */
    private void loadAllTableMetadata() {
        log("[Oracle] Загрузка метаданных таблиц...");
        updateProgress("Загрузка метаданных таблиц...");

        DatabaseMetadataService metadataService = new DatabaseMetadataService(settings);

        // Получаем список всех таблиц D_*
        String sql = "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME LIKE 'D\\_%' ESCAPE '\\'";

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            ResultSet rs = pstmt.executeQuery();

            List<String> tableNames = new ArrayList<>();
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }

            log("[Oracle] Найдено таблиц для анализа метаданных: " + tableNames.size());
            updateProgress("Загрузка метаданных таблиц (" + tableNames.size() + " шт.)...");

            int total = tableNames.size();
            for (int i = 0; i < total; i++) {
                if (stopRequested.get()) break;
                String tableName = tableNames.get(i);
                final int currentIndex = i + 1;
                final int currentTotal = total;

                if (currentIndex % 50 == 0) {
                    updateProgress("Метаданные: " + currentIndex + "/" + currentTotal);
                }

                // Загружаем колонки
                metadataService.getTableColumns(tableName);
                // Загружаем индексы
                metadataService.getIndexes(tableName);
                // Загружаем внешние ключи
                metadataService.getForeignKeys(tableName);

                if (currentIndex % 10 == 0) {
                    log("  [" + currentIndex + "/" + currentTotal + "] Загружены метаданные для: " + tableName);
                }
            }

            log("[Oracle] Загружены метаданные для " + tableNames.size() + " таблиц");

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки метаданных: " + e.getMessage());
        }
    }

    /**
     * Загрузка синонимов (ускоряет поиск объектов)
     */
    private void loadAllSynonyms() {
        log("[Oracle] Загрузка синонимов...");
        updateProgress("Загрузка синонимов...");

        String sql = "SELECT SYNONYM_NAME, TABLE_OWNER, TABLE_NAME FROM ALL_SYNONYMS " +
                "WHERE OWNER = 'PUBLIC' AND TABLE_NAME LIKE 'D\\_%' ESCAPE '\\'";

        try (Connection conn = getOracleConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(60);
            ResultSet rs = stmt.executeQuery(sql);

            int count = 0;
            while (rs.next()) {
                if (stopRequested.get()) break;
                String synonymName = rs.getString("SYNONYM_NAME");
                String tableName = rs.getString("TABLE_NAME");

                // Кэшируем синоним -> таблица
                DatabaseCacheManager.getSynonymTarget(synonymName, () -> tableName);
                count++;
            }

            log("[Oracle] Загружено синонимов: " + count);

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки синонимов: " + e.getMessage());
        }
    }
}