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
    private int packagesLoaded = 0;
    private int sequencesLoaded = 0;
    private int triggersLoaded = 0;
    private int synonymsLoaded = 0;

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

    // ==================== ОСНОВНОЙ МЕТОД ====================

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

        // 10. Загрузка всех констант
        if (!stopRequested.get()) {
            result.constantsCount = loadAllConstants();
        }

        // 11. Загрузка всех системных опций
        if (!stopRequested.get()) {
            result.systemOptionsCount = loadAllSystemOptions();
        }

        // 12. Загрузка метаданных таблиц
        if (!stopRequested.get()) {
            result.metadataCount = loadAllTableMetadata();
        }

        // 13. Загрузка всех синонимов
        if (!stopRequested.get()) {
            result.synonymsCount = loadAllSynonyms();
        }

        // 14. Загрузка пакетов Oracle
        if (!stopRequested.get()) {
            result.packagesCount = loadAllOraclePackages();
        }

        // 15. Загрузка последовательностей
        if (!stopRequested.get()) {
            result.sequencesCount = loadAllSequences();
        }

        // 16. Загрузка триггеров
        if (!stopRequested.get()) {
            result.triggersCount = loadAllTriggers();
        }

        long elapsed = System.currentTimeMillis() - startTime;
        result.elapsedMs = elapsed;

        log("=== ЗАПОЛНЕНИЕ КЭША ЗАВЕРШЕНО ===");
        log("Время выполнения: " + (elapsed / 1000) + " сек");
        log(result.toString());

        DatabaseCacheManager.forceSaveToDisk();

        return result;
    }

    // ==================== ЗАГРУЗКА ВЬЮХ ====================

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

            int alreadyCached = 0;
            int toLoad = 0;

            for (String viewName : viewNames) {
                if (DatabaseCacheManager.isOracleViewDDLCached(viewName)) {
                    alreadyCached++;
                } else {
                    toLoad++;
                }
            }

            log("[Oracle] Уже в кэше: " + alreadyCached + ", требуется загрузить: " + toLoad);
            updateProgress("Загрузка вьюх Oracle (новых: " + toLoad + ")...");

            int loaded = 0;
            int total = viewNames.size();
            for (int i = 0; i < total; i++) {
                if (stopRequested.get()) return;
                String viewName = viewNames.get(i);

                if (DatabaseCacheManager.isOracleViewDDLCached(viewName)) {
                    continue;
                }

                loaded++;
                final int currentLoaded = loaded;
                final int finalToLoad = toLoad;

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

    // ==================== ЗАГРУЗКА ТАБЛИЦ ====================

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

                if (DatabaseCacheManager.isOracleTableDDLCached(tableName)) {
                    continue;
                }

                loaded++;
                final int currentLoaded = loaded;
                final int finalToLoad = toLoad;

                if (currentLoaded % 100 == 0 || currentLoaded == finalToLoad) {
                    updateProgress("Oracle таблицы: " + currentLoaded + "/" + finalToLoad);
                }

                DatabaseCacheManager.getOracleTableDDL(tableName, () -> {
                    if (currentLoaded % 100 == 0 || currentLoaded == finalToLoad) {
                        log("  [" + currentLoaded + "/" + finalToLoad + "] Загрузка таблицы: " + tableName);
                    }
                    return oracleService.getTableDDL(tableName);
                });
                tablesLoaded++;
            }

            log("[Oracle] Загружено новых таблиц: " + loaded);

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки списка таблиц: " + e.getMessage());
        }
    }

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
                tableNames.add(rs.getString("tablename"));
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

    // ==================== ЗАГРУЗКА ФУНКЦИЙ ====================

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

    // ==================== ЗАГРУЗКА БРОКЕРОВ ====================

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

                String unit = rs.getString("unitbpcode");
                String action = rs.getString("standard_action");
                String execProc = rs.getString("execproc");

                if (unit != null && action != null && execProc != null) {
                    final int currentCount = count;
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

    // ==================== ЗАГРУЗКА ОТЧЁТОВ ====================

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
            log("[PostgreSQL] Загружено отчётов для " + postgresReportsCount + " unit'ов");

        } catch (SQLException e) {
            error("[PostgreSQL] Ошибка: " + e.getMessage());
        }
    }

    // ==================== ЗАГРУЗКА КОНСТАНТ ====================

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

    // ==================== ЗАГРУЗКА СИСТЕМНЫХ ОПЦИЙ ====================

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

    // ==================== ЗАГРУЗКА МЕТАДАННЫХ ====================

    private int loadAllTableMetadata() {
        log("[Oracle] Загрузка метаданных таблиц...");
        updateProgress("Загрузка метаданных таблиц...");

        DatabaseMetadataService metadataService = new DatabaseMetadataService(settings);
        String sql = "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME LIKE 'D\\_%' ESCAPE '\\'";
        int total = 0;

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

                metadataService.getTableColumns(tableName);
                metadataService.getIndexes(tableName);
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

    // ==================== ЗАГРУЗКА СИНОНИМОВ ====================

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

                DatabaseCacheManager.getSynonymTarget(synonymName, () -> tableName);
                count++;

                if (count % 500 == 0) {
                    updateProgress("Синонимы: " + count);
                }
            }

            log("[Oracle] Загружено синонимов: " + count);
            synonymsLoaded = count;

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки синонимов: " + e.getMessage());
        }
        return count;
    }

    // ==================== ЗАГРУЗКА ПАКЕТОВ ====================

    private int loadAllOraclePackages() {
        log("[Oracle] Загрузка всех пакетов...");
        updateProgress("Загрузка пакетов Oracle...");

        String sql = "SELECT DISTINCT NAME FROM ALL_SOURCE " +
                "WHERE OWNER = ? AND TYPE = 'PACKAGE' AND NAME LIKE 'D\\_PKG\\_%' ESCAPE '\\'";

        int loaded = 0;

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
            updateProgress("Загрузка пакетов Oracle (" + packageNames.size() + " шт.)...");

            for (int i = 0; i < packageNames.size(); i++) {
                if (stopRequested.get()) break;
                String packageName = packageNames.get(i);
                loaded++;

                if (loaded % 50 == 0) {
                    updateProgress("Пакеты Oracle: " + loaded + "/" + packageNames.size());
                }

                DatabaseCacheManager.getOraclePackageSpec(packageName, () -> {
                    return fetchPackageSpec(packageName);
                });
            }

            log("[Oracle] Загружено пакетов: " + loaded);
            packagesLoaded = loaded;

        } catch (SQLException e) {
            error("[Oracle] Ошибка: " + e.getMessage());
        }
        return loaded;
    }

    private String fetchPackageSpec(String packageName) {
        String sql = "SELECT TEXT FROM ALL_SOURCE " +
                "WHERE OWNER = ? AND TYPE = 'PACKAGE' AND NAME = ? ORDER BY LINE";

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            pstmt.setString(2, packageName);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder spec = new StringBuilder();
            while (rs.next()) {
                spec.append(rs.getString("TEXT"));
            }

            if (spec.length() > 0) {
                return "-- Oracle Package: " + packageName + "\n" + spec.toString();
            }

        } catch (SQLException e) {
            error("Ошибка получения спецификации пакета " + packageName + ": " + e.getMessage());
        }
        return null;
    }

    // ==================== ЗАГРУЗКА ПОСЛЕДОВАТЕЛЬНОСТЕЙ ====================

    private int loadAllSequences() {
        log("[Oracle] Загрузка всех последовательностей...");
        updateProgress("Загрузка последовательностей...");

        String sql = "SELECT SEQUENCE_NAME FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = ? AND SEQUENCE_NAME LIKE 'SEQ\\_%' ESCAPE '\\'";
        int count = 0;

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (stopRequested.get()) break;
                String seqName = rs.getString("SEQUENCE_NAME");
                count++;

                if (count % 500 == 0) {
                    updateProgress("Последовательности: " + count);
                }
            }

            log("[Oracle] Загружено последовательностей: " + count);
            sequencesLoaded = count;

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки последовательностей: " + e.getMessage());
        }
        return count;
    }

    // ==================== ЗАГРУЗКА ТРИГГЕРОВ ====================

    private int loadAllTriggers() {
        log("[Oracle] Загрузка всех триггеров...");
        updateProgress("Загрузка триггеров...");

        String sql = "SELECT TRIGGER_NAME, TABLE_NAME FROM ALL_TRIGGERS WHERE OWNER = ? AND TRIGGER_NAME LIKE 'TRG\\_%' ESCAPE '\\'";
        int count = 0;

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (stopRequested.get()) break;
                String triggerName = rs.getString("TRIGGER_NAME");
                count++;

                if (count % 500 == 0) {
                    updateProgress("Триггеры: " + count);
                }
            }

            log("[Oracle] Загружено триггеров: " + count);
            triggersLoaded = count;

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки триггеров: " + e.getMessage());
        }
        return count;
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ ЧАСТИЧНОЙ ЗАГРУЗКИ ====================

    public PopulateResult loadViewsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ВЬЮХ ===");
        PopulateResult result = new PopulateResult();

        viewsLoaded = 0;
        loadAllOracleViews();
        result.oracleViewsCount = viewsLoaded;

        viewsLoaded = 0;
        loadAllPostgresViews();
        result.postgresViewsCount = viewsLoaded;

        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadTablesOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ТАБЛИЦ ===");
        PopulateResult result = new PopulateResult();

        tablesLoaded = 0;
        loadAllOracleTablesDirectly();
        result.oracleTablesCount = tablesLoaded;

        tablesLoaded = 0;
        loadAllPostgresTablesDirectly();
        result.postgresTablesCount = tablesLoaded;

        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadFunctionsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ФУНКЦИЙ ===");
        PopulateResult result = new PopulateResult();

        functionsLoaded = 0;
        loadAllOraclePackageFunctions();
        result.oracleFunctionsCount = functionsLoaded;

        functionsLoaded = 0;
        loadAllPostgresFunctions();
        result.postgresFunctionsCount = functionsLoaded;

        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadBrokersOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО БРОКЕРОВ ===");
        PopulateResult result = new PopulateResult();

        brokersLoaded = 0;
        loadAllBrokers();
        result.brokersCount = brokersLoaded;

        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadReportsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ОТЧЁТОВ ===");
        PopulateResult result = new PopulateResult();

        reportsLoaded = 0;
        loadAllOracleReports();
        result.oracleReportsCount = reportsLoaded;

        reportsLoaded = 0;
        loadAllPostgresReports();
        result.postgresReportsCount = reportsLoaded;

        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadConstantsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО КОНСТАНТ ===");
        PopulateResult result = new PopulateResult();

        result.constantsCount = loadAllConstants();

        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadOptionsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО СИСТЕМНЫХ ОПЦИЙ ===");
        PopulateResult result = new PopulateResult();

        result.systemOptionsCount = loadAllSystemOptions();

        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadPackagesOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ПАКЕТОВ ===");
        PopulateResult result = new PopulateResult();

        result.packagesCount = loadAllOraclePackages();

        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadMetadataOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО МЕТАДАННЫХ ===");
        PopulateResult result = new PopulateResult();

        result.metadataCount = loadAllTableMetadata();

        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadSequencesOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ПОСЛЕДОВАТЕЛЬНОСТЕЙ ===");
        PopulateResult result = new PopulateResult();

        result.sequencesCount = loadAllSequences();

        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadTriggersOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ТРИГГЕРОВ ===");
        PopulateResult result = new PopulateResult();

        result.triggersCount = loadAllTriggers();

        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadSynonymsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО СИНОНИМОВ ===");
        PopulateResult result = new PopulateResult();

        result.synonymsCount = loadAllSynonyms();

        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    // ==================== СОЕДИНЕНИЯ С БД ====================

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

    // ==================== РЕЗУЛЬТАТ ====================

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
        public int constantsCount = 0;
        public int systemOptionsCount = 0;
        public int metadataCount = 0;
        public int synonymsCount = 0;
        public int packagesCount = 0;
        public int sequencesCount = 0;
        public int triggersCount = 0;
        public long elapsedMs = 0;

        @Override
        public String toString() {
            int totalObjects = oracleViewsCount + postgresViewsCount +
                    oracleTablesCount + postgresTablesCount +
                    oracleFunctionsCount + postgresFunctionsCount +
                    brokersCount + oracleReportsCount + postgresReportsCount +
                    constantsCount + systemOptionsCount + metadataCount + synonymsCount +
                    packagesCount + sequencesCount + triggersCount;

            return String.format(
                    "Результат загрузки кэша:\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "  📊 Oracle вьюхи:          %6d\n" +
                            "  📊 PostgreSQL вьюхи:      %6d\n" +
                            "  📋 Oracle таблицы:        %6d\n" +
                            "  📋 PostgreSQL таблицы:    %6d\n" +
                            "  🔧 Oracle функции:        %6d\n" +
                            "  🔧 PostgreSQL функции:    %6d\n" +
                            "  📦 Oracle пакеты:         %6d\n" +
                            "  🔗 Брокеры:               %6d\n" +
                            "  📄 Oracle отчёты:         %6d\n" +
                            "  📄 PostgreSQL отчёты:     %6d\n" +
                            "  📌 Константы:             %6d\n" +
                            "  ⚙️ Системные опции:       %6d\n" +
                            "  🏷️ Метаданные таблиц:     %6d\n" +
                            "  🔢 Последовательности:    %6d\n" +
                            "  ⚡ Триггеры:              %6d\n" +
                            "  🔍 Синонимы:              %6d\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "  ✅ ВСЕГО ОБЪЕКТОВ:        %6d\n" +
                            "  ⏱️ Время выполнения:      %.1f сек",
                    oracleViewsCount, postgresViewsCount,
                    oracleTablesCount, postgresTablesCount,
                    oracleFunctionsCount, postgresFunctionsCount,
                    packagesCount,
                    brokersCount, oracleReportsCount, postgresReportsCount,
                    constantsCount, systemOptionsCount, metadataCount,
                    sequencesCount, triggersCount, synonymsCount,
                    totalObjects,
                    elapsedMs / 1000.0
            );
        }
    }
    // Добавить в DatabaseCachePopulator.java

    public PopulateResult loadOracleViewsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ORACLE ВЬЮХ ===");
        PopulateResult result = new PopulateResult();
        viewsLoaded = 0;
        loadAllOracleViews();
        result.oracleViewsCount = viewsLoaded;
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadPostgresViewsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО POSTGRESQL ВЬЮХ ===");
        PopulateResult result = new PopulateResult();
        viewsLoaded = 0;
        loadAllPostgresViews();
        result.postgresViewsCount = viewsLoaded;
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadOracleTablesOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ORACLE ТАБЛИЦ ===");
        PopulateResult result = new PopulateResult();
        tablesLoaded = 0;
        loadAllOracleTablesDirectly();
        result.oracleTablesCount = tablesLoaded;
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadPostgresTablesOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО POSTGRESQL ТАБЛИЦ ===");
        PopulateResult result = new PopulateResult();
        tablesLoaded = 0;
        loadAllPostgresTablesDirectly();
        result.postgresTablesCount = tablesLoaded;
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadOracleFunctionsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ORACLE ФУНКЦИЙ ===");
        PopulateResult result = new PopulateResult();
        functionsLoaded = 0;
        loadAllOraclePackageFunctions();
        result.oracleFunctionsCount = functionsLoaded;
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadPostgresFunctionsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО POSTGRESQL ФУНКЦИЙ ===");
        PopulateResult result = new PopulateResult();
        functionsLoaded = 0;
        loadAllPostgresFunctions();
        result.postgresFunctionsCount = functionsLoaded;
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadOraclePackagesOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ORACLE ПАКЕТОВ ===");
        PopulateResult result = new PopulateResult();
        packagesLoaded = 0;
        loadAllOraclePackages();
        result.packagesCount = packagesLoaded;
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadPostgresPackagesOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО POSTGRESQL ПАКЕТОВ ===");
        PopulateResult result = new PopulateResult();
        loadAllPostgresPackages();
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadOracleReportsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ORACLE ОТЧЁТОВ ===");
        PopulateResult result = new PopulateResult();
        reportsLoaded = 0;
        loadAllOracleReports();
        result.oracleReportsCount = reportsLoaded;
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadPostgresReportsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО POSTGRESQL ОТЧЁТОВ ===");
        PopulateResult result = new PopulateResult();
        reportsLoaded = 0;
        loadAllPostgresReports();
        result.postgresReportsCount = reportsLoaded;
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }
// Добавить в DatabaseCachePopulator.java

    /**
     * Загрузка всех пакетов/функций из PostgreSQL
     */
    private int loadAllPostgresPackages() {
        log("[PostgreSQL] Загрузка всех пакетов/функций...");
        updateProgress("Загрузка пакетов PostgreSQL...");

        String sql = "SELECT proname, prosrc FROM pg_proc p " +
                "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                "WHERE n.nspname = 'public' AND (proname LIKE 'd\\_pkg\\_%' OR proname LIKE 'd\\_%')";

        int loaded = 0;

        try (Connection conn = getPostgresConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(60);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                if (stopRequested.get()) break;
                String funcName = rs.getString("proname");

                // Пропускаем системные функции
                if (funcName.startsWith("d_pkg_constants") ||
                        funcName.startsWith("d_pkg_options") ||
                        funcName.startsWith("d_pkg_option_specs")) {
                    continue;
                }

                DatabaseCacheManager.getPostgresFunctionBody(funcName, () -> {
                    log("  Загрузка пакета/функции PostgreSQL: " + funcName);
                    return postgresService.getFunctionBody(funcName);
                });
                loaded++;

                if (loaded % 50 == 0) {
                    updateProgress("PostgreSQL пакеты: " + loaded);
                }
            }

            log("[PostgreSQL] Загружено пакетов/функций: " + loaded);
            packagesLoaded = loaded;

        } catch (SQLException e) {
            error("[PostgreSQL] Ошибка загрузки пакетов: " + e.getMessage());
        }
        return loaded;
    }


}