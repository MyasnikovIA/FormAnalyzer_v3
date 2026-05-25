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

        // 1. Загрузка всех вьюх из Oracle
        if (!stopRequested.get()) {
            viewsLoaded = 0;
            loadAllOracleViews();
            result.oracleViewsCount = viewsLoaded;
        }

        // 2. Загрузка всех вьюх из PostgreSQL
        if (!stopRequested.get()) {
            viewsLoaded = 0;
            loadAllPostgresViews();
            result.postgresViewsCount = viewsLoaded;
        }

        // 3. Прямая загрузка DDL всех таблиц из Oracle
        if (!stopRequested.get()) {
            tablesLoaded = 0;
            loadAllOracleTablesDirectly();
            result.oracleTablesCount = tablesLoaded;
        }

        // 4. Прямая загрузка DDL всех таблиц из PostgreSQL
        if (!stopRequested.get()) {
            tablesLoaded = 0;
            loadAllPostgresTablesDirectly();
            result.postgresTablesCount = tablesLoaded;
        }

        // 5. Загрузка всех пакетных функций из Oracle
        if (!stopRequested.get()) {
            functionsLoaded = 0;
            loadAllOraclePackageFunctions();
            result.oracleFunctionsCount = functionsLoaded;
        }

        // 6. Загрузка всех функций из PostgreSQL
        if (!stopRequested.get()) {
            functionsLoaded = 0;
            loadAllPostgresFunctions();
            result.postgresFunctionsCount = functionsLoaded;
        }

        // 7. Загрузка всех брокеров
        if (!stopRequested.get()) {
            brokersLoaded = 0;
            loadAllBrokers();
            result.brokersCount = brokersLoaded;
        }

        // 8. Загрузка всех отчётов из Oracle
        if (!stopRequested.get()) {
            reportsLoaded = 0;
            loadAllOracleReports();
            result.oracleReportsCount = reportsLoaded;
        }

        // 9. Загрузка всех отчётов из PostgreSQL
        if (!stopRequested.get()) {
            reportsLoaded = 0;
            loadAllPostgresReports();
            result.postgresReportsCount = reportsLoaded;
        }

        // ========== ДОБАВИТЬ ВЫЗОВЫ НОВЫХ МЕТОДОВ ==========

        // 10. Загрузка всех констант
        if (!stopRequested.get()) {
            int  constantsLoaded = loadAllConstants();
            result.constantsCount = constantsLoaded;
        }

        // 11. Загрузка всех системных опций
        if (!stopRequested.get()) {
            int systemOptionsLoaded = loadAllSystemOptions();
            result.systemOptionsCount = systemOptionsLoaded;
        }

        // 12. Загрузка метаданных таблиц
        if (!stopRequested.get()) {
            int metadataLoaded = loadAllTableMetadata();
            result.metadataCount = metadataLoaded;
        }

        // 13. Загрузка всех синонимов
        if (!stopRequested.get()) {
            int synonymsLoaded = loadAllSynonyms();
            result.synonymsCount = synonymsLoaded;
        }

        // ================================================

        long elapsed = System.currentTimeMillis() - startTime;
        result.elapsedMs = elapsed;

        log("=== ЗАПОЛНЕНИЕ КЭША ЗАВЕРШЕНО ===");
        log("Время выполнения: " + (elapsed / 1000) + " сек");
        log(result.toString());

        // Принудительно сохраняем кэш на диск
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

            // Подсчитываем, сколько уже есть в кэше
            int alreadyCached = 0;
            int toLoad = 0;

            for (String viewName : viewNames) {
                String key = viewName.toUpperCase();
                if (DatabaseCacheManager.isOracleViewDDLCached(key)) {
                    alreadyCached++;
                } else {
                    toLoad++;
                }
            }

            log("[Oracle] Уже в кэше: " + alreadyCached + ", требуется загрузить: " + toLoad);
            updateProgress("Загрузка вьюх Oracle (новых: " + toLoad + ")...");

            int loaded = 0;
            for (String viewName : viewNames) {
                if (stopRequested.get()) return;

                String key = viewName.toUpperCase();

                // ПРОВЕРКА: если уже есть в кэше - пропускаем
                if (DatabaseCacheManager.isOracleViewDDLCached(key)) {
                    if (loaded % 500 == 0) {
                        log("  Пропущено (уже в кэше): " + viewName);
                    }
                    continue;  // ← ПЕРЕХОДИМ К СЛЕДУЮЩЕМУ ОБЪЕКТУ
                }

                loaded++;
                final int currentLoaded = loaded;

                int finalToLoad = toLoad;
                DatabaseCacheManager.getOracleViewDDL(viewName, () -> {
                    log("  [" + currentLoaded + "/" + finalToLoad + "] Загрузка вьюхи: " + viewName);
                    return oracleService.getViewDDL(viewName);
                });
                viewsLoaded++;
            }

            log("[Oracle] Загружено новых вьюх: " + loaded);

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

            int totalFunctions = 0;
            int loaded = 0;
            int skipped = 0;

            for (String packageName : packageNames) {
                if (stopRequested.get()) break;

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
                        totalFunctions++;
                        String fullName = packageName + "." + funcName;

                        // ПРОВЕРКА: если уже есть в кэше - пропускаем
                        if (DatabaseCacheManager.isOracleFunctionBodyCached(fullName)) {
                            skipped++;
                            continue;
                        }

                        loaded++;
                        final int currentLoaded = loaded;
                        final String finalPackageName = packageName;
                        final String finalFuncName = funcName;

                        DatabaseCacheManager.getOracleFunctionBody(fullName, () -> {
                            log("  [" + currentLoaded + "] Загрузка функции: " + finalPackageName + "." + finalFuncName);
                            return oracleService.getFunctionBody(finalPackageName, finalFuncName);
                        });
                        functionsLoaded++;
                    }
                }
            }

            log("[Oracle] Функций всего: " + totalFunctions +
                    ", уже в кэше: " + skipped +
                    ", загружено: " + loaded);

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
            int postgresReportsCount = 0;
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
                postgresReportsCount++;
            }
            reportsLoaded += postgresReportsCount;
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

        // Устанавливаем контекст МИС
        String misUser = settings.getMisUser();
        if (misUser != null && !misUser.trim().isEmpty()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT set_config('mis.user', '" + misUser + "', false)");
            } catch (SQLException e) {
                System.err.println("  [PostgreSQL] Ошибка установки контекста: " + e.getMessage());
            }
        }

        return conn;
    }

    public static class PopulateResult {
        public int oracleViewsCount = 0;
        public int postgresViewsCount = 0;
        public int oracleTablesCount = 0;
        public int postgresTablesCount = 0;
        public int oracleFunctionsCount = 0;
        public int postgresFunctionsCount = 0;
        public int brokersCount = 0;
        public int oracleReportsCount = 0;
        public int postgresReportsCount = 0;
        public int constantsCount = 0;        // Добавить
        public int systemOptionsCount = 0;    // Добавить
        public int metadataCount = 0;         // Добавить
        public int synonymsCount = 0;         // Добавить
        public long elapsedMs = 0;

        @Override
        public String toString() {
            int totalObjects = oracleViewsCount + postgresViewsCount +
                    oracleTablesCount + postgresTablesCount +
                    oracleFunctionsCount + postgresFunctionsCount +
                    brokersCount + oracleReportsCount + postgresReportsCount +
                    constantsCount + systemOptionsCount + metadataCount + synonymsCount;

            return String.format(
                    "Результат загрузки кэша:\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "  📊 Oracle вьюхи:          %6d\n" +
                            "  📊 PostgreSQL вьюхи:      %6d\n" +
                            "  📋 Oracle таблицы:        %6d\n" +
                            "  📋 PostgreSQL таблицы:    %6d\n" +
                            "  🔧 Oracle функции:        %6d\n" +
                            "  🔧 PostgreSQL функции:    %6d\n" +
                            "  🔗 Брокеры:               %6d\n" +
                            "  📄 Oracle отчёты:         %6d\n" +
                            "  📄 PostgreSQL отчёты:     %6d\n" +
                            "  📌 Константы:             %6d\n" +
                            "  ⚙️ Системные опции:       %6d\n" +
                            "  🏷️ Метаданные таблиц:     %6d\n" +
                            "  🔍 Синонимы:              %6d\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "  ✅ ВСЕГО ОБЪЕКТОВ:        %6d\n" +
                            "  ⏱️ Время выполнения:      %.1f сек",
                    oracleViewsCount, postgresViewsCount,
                    oracleTablesCount, postgresTablesCount,
                    oracleFunctionsCount, postgresFunctionsCount,
                    brokersCount, oracleReportsCount, postgresReportsCount,
                    constantsCount, systemOptionsCount, metadataCount, synonymsCount,
                    totalObjects,
                    elapsedMs / 1000.0
            );
        }
    }
    /**
     * Загрузка всех констант из D_PKG_CONSTANTS
     */
    private int loadAllConstants() {
        log("[Oracle] Загрузка всех констант...");
        updateProgress("Загрузка констант...");

        String sql = "SELECT CONST_CODE, CONST_VALUE FROM D_PKG_CONSTANTS";
        int count = 0;
        try (Connection conn = getOracleConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(60);
            ResultSet rs = stmt.executeQuery(sql);

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
        return count;
    }

    /**
     * Загрузка всех системных опций
     */
    private int loadAllSystemOptions() {
        log("[Oracle] Загрузка системных опций...");
        updateProgress("Загрузка системных опций...");

        String sql = "SELECT OPTION_CODE, OPTION_VALUE FROM D_SYSTEM_OPTIONS";
        int count = 0;

        try (Connection conn = getOracleConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(60);
            ResultSet rs = stmt.executeQuery(sql);

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
        return count;
    }

    /**
     * Загрузка метаданных всех таблиц
     */
    private int loadAllTableMetadata() {
        log("[Oracle] Загрузка метаданных таблиц...");
        updateProgress("Загрузка метаданных таблиц...");

        DatabaseMetadataService metadataService = new DatabaseMetadataService(settings);

        // Получаем список всех таблиц D_*
        String sql = "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME LIKE 'D\\_%' ESCAPE '\\'";
        int total =0;
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

            total = tableNames.size();
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
        return total;
    }

    /**
     * Загрузка синонимов (ускоряет поиск объектов)
     */
    private int loadAllSynonyms() {
        log("[Oracle] Загрузка синонимов...");
        updateProgress("Загрузка синонимов...");

        String sql = "SELECT SYNONYM_NAME, TABLE_OWNER, TABLE_NAME FROM ALL_SYNONYMS " +
                "WHERE OWNER = 'PUBLIC' AND TABLE_NAME LIKE 'D\\_%' ESCAPE '\\'";

        int count = 0;
        try (Connection conn = getOracleConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(60);
            ResultSet rs = stmt.executeQuery(sql);

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
        return count;
    }
    /**
     * Загрузка DDL всех таблиц D_* из Oracle
     */
    private void loadAllOracleTablesDirectly() {
        log("[Oracle] Прямая загрузка DDL всех таблиц...");
        updateProgress("Загрузка DDL всех таблиц Oracle...");

        String sql = "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME LIKE 'D\\_%' ESCAPE '\\'";

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            pstmt.setQueryTimeout(120);
            ResultSet rs = pstmt.executeQuery();

            List<String> tableNames = new ArrayList<>();
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (!tableName.startsWith("D_TEMP") && !tableName.startsWith("D_BIN")) {
                    tableNames.add(tableName);
                }
            }

            // Подсчитываем, сколько уже в кэше
            int alreadyCached = 0;
            int toLoad = 0;
            for (String tableName : tableNames) {
                if (DatabaseCacheManager.isOracleTableDDLCached(tableName)) {
                    alreadyCached++;
                } else {
                    toLoad++;
                }
            }

            log("[Oracle] Таблиц всего: " + tableNames.size() +
                    ", уже в кэше: " + alreadyCached +
                    ", требуется загрузить: " + toLoad);
            updateProgress("Загрузка DDL таблиц Oracle (новых: " + toLoad + ")...");

            int loaded = 0;
            for (String tableName : tableNames) {
                if (stopRequested.get()) return;

                // ПРОВЕРКА: если уже есть в кэше - пропускаем
                if (DatabaseCacheManager.isOracleTableDDLCached(tableName)) {
                    continue;
                }

                loaded++;
                final int currentLoaded = loaded;

                if (currentLoaded % 100 == 0 || currentLoaded == toLoad) {
                    updateProgress("Oracle таблицы: " + currentLoaded + "/" + toLoad);
                    log("  [" + currentLoaded + "/" + toLoad + "] Загрузка таблицы: " + tableName);
                }

                int finalToLoad = toLoad;
                DatabaseCacheManager.getOracleTableDDL(tableName, () -> {
                    String ddl = oracleService.getTableDDL(tableName);
                    if (ddl == null) {
                        log("  [" + currentLoaded + "/" + finalToLoad + "] НЕ ЗАГРУЖЕНА: " + tableName + " (нет в БД)");
                    } else {
                        log("  [" + currentLoaded + "/" + finalToLoad + "] Загрузка таблицы: " + tableName);
                    }
                    return ddl;
                });
                tablesLoaded++;
            }

            log("[Oracle] Загружено новых таблиц: " + loaded);

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки списка таблиц: " + e.getMessage());
        }
    }

    /**
     * Загрузка DDL всех таблиц из PostgreSQL
     */
    private void loadAllPostgresTablesDirectly() {
        log("[PostgreSQL] Прямая загрузка DDL всех таблиц...");
        updateProgress("Загрузка DDL всех таблиц PostgreSQL...");

        String sql = "SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename LIKE 'd\\_%'";

        try (Connection conn = getPostgresConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(120);
            ResultSet rs = stmt.executeQuery(sql);

            List<String> tableNames = new ArrayList<>();
            while (rs.next()) {
                String tableName = rs.getString("tablename");
                tableNames.add(tableName);
            }

            log("[PostgreSQL] Найдено таблиц для загрузки: " + tableNames.size());
            updateProgress("Загрузка DDL таблиц PostgreSQL (" + tableNames.size() + " шт.)...");

            int total = tableNames.size();
            for (int i = 0; i < total; i++) {
                if (stopRequested.get()) {
                    log("Загрузка таблиц PostgreSQL прервана");
                    return;
                }

                String tableName = tableNames.get(i);
                final int currentIndex = i + 1;

                if (currentIndex % 100 == 0 || currentIndex == total) {
                    updateProgress("PostgreSQL таблицы: " + currentIndex + "/" + total);
                    log("  [" + currentIndex + "/" + total + "] Загрузка таблицы: " + tableName);
                }

                DatabaseCacheManager.getPostgresTableDDL(tableName, () -> {
                    return postgresService.getTableDDL(tableName);
                });
            }

        } catch (SQLException e) {
            error("[PostgreSQL] Ошибка загрузки списка таблиц: " + e.getMessage());
        }
    }
}