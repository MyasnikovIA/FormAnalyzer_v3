// core/cache/DatabaseCachePopulator.java
package ru.tmis.analyzer.core.cache;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.db.*;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.DbReportInfo;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private int postgresPackagesLoaded = 0; // <-- ДОБАВИТЬ

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

        // 6. Загрузка всех функций из PostgreSQL (ОПТИМИЗИРОВАННО)
        if (!stopRequested.get()) {
            functionsLoaded = 0;
            result.postgresFunctionsCount = loadAllPostgresFunctionsOptimized();
        }

        // 7. Загрузка всех брокеров (ОПТИМИЗИРОВАННО)
        if (!stopRequested.get()) {
            brokersLoaded = 0;
            result.brokersCount = loadAllBrokersOptimized();
        }

        // 8. Загрузка всех отчётов из Oracle (ОПТИМИЗИРОВАННО)
        if (!stopRequested.get()) {
            result.oracleReportsCount = loadAllOracleReportsOptimized();
        }

        // 9. Загрузка всех отчётов из PostgreSQL (ОПТИМИЗИРОВАННО)
        if (!stopRequested.get()) {
            result.postgresReportsCount = loadAllPostgresReportsOptimized();
        }

        // 10. Загрузка всех констант (ОПТИМИЗИРОВАННО)
        if (!stopRequested.get()) {
            result.constantsCount = loadAllConstantsOptimized();
        }

        // 11. Загрузка всех системных опций (ОПТИМИЗИРОВАННО)
        if (!stopRequested.get()) {
            result.systemOptionsCount = loadAllSystemOptionsOptimized();
        }

        // 12. Загрузка метаданных таблиц (ОПТИМИЗИРОВАННО)
        if (!stopRequested.get()) {
            result.metadataCount = loadAllTableMetadataOptimized();
        }

        // 13. Загрузка всех синонимов (ОПТИМИЗИРОВАННО)
        if (!stopRequested.get()) {
            result.synonymsCount = loadAllSynonymsOptimized();
        }

        // 14. Загрузка последовательностей (ОПТИМИЗИРОВАННО)
        if (!stopRequested.get()) {
            result.sequencesCount = loadAllSequencesOptimized();
        }

        // 15. Загрузка пакетов Oracle (ОПТИМИЗИРОВАННО)
        if (!stopRequested.get()) {
            result.packagesCount = loadAllOraclePackagesOptimized();
        }

        // 16. Загрузка пакетов PostgreSQL (ОПТИМИЗИРОВАННО)
        if (!stopRequested.get()) {
            result.postgresPackagesCount = loadAllPostgresPackagesOptimized();
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
        log("=== ЗАГРУЗКА ТОЛЬКО БРОКЕРОВ (ОПТИМИЗИРОВАННО) ===");
        PopulateResult result = new PopulateResult();
        result.brokersCount = loadAllBrokersOptimized();
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
        log("=== ЗАГРУЗКА ТОЛЬКО КОНСТАНТ (ОПТИМИЗИРОВАННО) ===");
        PopulateResult result = new PopulateResult();
        result.constantsCount = loadAllConstantsOptimized();
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }


    public PopulateResult loadOptionsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО СИСТЕМНЫХ ОПЦИЙ (ОПТИМИЗИРОВАННО) ===");
        PopulateResult result = new PopulateResult();
        result.systemOptionsCount = loadAllSystemOptionsOptimized();
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
        log("=== ЗАГРУЗКА ТОЛЬКО МЕТАДАННЫХ (ОПТИМИЗИРОВАННО) ===");
        PopulateResult result = new PopulateResult();
        result.metadataCount = loadAllTableMetadataOptimized();
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadSequencesOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ПОСЛЕДОВАТЕЛЬНОСТЕЙ (ОПТИМИЗИРОВАННО) ===");
        PopulateResult result = new PopulateResult();
        result.sequencesCount = loadAllSequencesOptimized();
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
        log("=== ЗАГРУЗКА ТОЛЬКО СИНОНИМОВ (ОПТИМИЗИРОВАННО) ===");
        PopulateResult result = new PopulateResult();
        result.synonymsCount = loadAllSynonymsOptimized();
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
        public int postgresPackagesCount = 0;  // <-- ДОБАВИТЬ ЭТУ СТРОКУ
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
                    packagesCount + postgresPackagesCount + sequencesCount + triggersCount;

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
                            "  📦 PostgreSQL пакеты:     %6d\n" +
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
                    packagesCount, postgresPackagesCount,
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
        log("=== ЗАГРУЗКА ТОЛЬКО ORACLE ВЬЮХ (ОПТИМИЗИРОВАННО) ===");
        PopulateResult result = new PopulateResult();
        result.oracleViewsCount = loadAllOracleViewsOptimized();  // ← оптимизированный
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadPostgresViewsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО POSTGRESQL ВЬЮХ (ОПТИМИЗИРОВАННО) ===");
        PopulateResult result = new PopulateResult();
        result.postgresViewsCount = loadAllPostgresViewsOptimized();  // ← оптимизированный
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
        log("=== ЗАГРУЗКА ТОЛЬКО POSTGRESQL ФУНКЦИЙ (ОПТИМИЗИРОВАННО) ===");
        PopulateResult result = new PopulateResult();
        result.postgresFunctionsCount = loadAllPostgresFunctionsOptimized();
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
        log("=== ЗАГРУЗКА ТОЛЬКО POSTGRESQL ПАКЕТОВ (ОПТИМИЗИРОВАННО) ===");
        PopulateResult result = new PopulateResult();
        result.postgresPackagesCount = loadAllPostgresPackagesOptimized();
        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }

    public PopulateResult loadOracleReportsOnly() {
        log("=== ЗАГРУЗКА ТОЛЬКО ORACLE ОТЧЁТОВ (ОПТИМИЗИРОВАННО) ===");
        PopulateResult result = new PopulateResult();
        result.oracleReportsCount = loadAllOracleReportsOptimized();
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

// DatabaseCachePopulator.java - добавить метод

    /**
     * Оптимизированная загрузка всех отчётов из Oracle одним запросом
     */
    private int loadAllOracleReportsOptimized() {
        log("[Oracle] Оптимизированная загрузка всех отчётов (один запрос)...");
        updateProgress("Загрузка отчётов Oracle...");

        // Один запрос получает ВСЕ отчёты со всеми полями
        String sql =
                "SELECT drl.UNITCODE, rep.ID, drl.PRIV_NAME, rep.REP_TYPE, " +
                        //"rep.REP_DATA," +
                        " rep.REP_FILENAME, rep.REP_NAME, rep.REP_CODE, rep.LPU " +
                        "FROM D_REPORTS_LINKS drl " +
                        "JOIN D_REPORTS rep ON drl.PID = rep.ID " +
                        "ORDER BY drl.UNITCODE";

        // Карта для группировки отчётов по UNITCODE
        Map<String, List<DbReportInfo>> reportsByUnit = new LinkedHashMap<>();
        int totalReports = 0;

        try (Connection conn = getOracleConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(120);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                if (stopRequested.get()) break;

                String unitCode = rs.getString("UNITCODE");
                if (unitCode == null || unitCode.trim().isEmpty()) {
                    continue;
                }

                DbReportInfo report = new DbReportInfo();
                report.setRepID(rs.getInt("ID"));
                report.setPrivName(rs.getString("PRIV_NAME"));
                report.setRepType(rs.getInt("REP_TYPE"));
                report.setRepData(rs.getBytes("REP_DATA"));
                report.setRepFilename(rs.getString("REP_FILENAME"));
                report.setRepName(rs.getString("REP_NAME"));
                report.setRepCode(rs.getString("REP_CODE"));
                report.setUnitCode(rs.getString("LPU"));

                // Группируем по UNITCODE
                reportsByUnit.computeIfAbsent(unitCode, k -> new ArrayList<>()).add(report);
                totalReports++;
            }

            log("[Oracle] Получено отчётов: " + totalReports + ", UNITCODE: " + reportsByUnit.size());

            // Сохраняем в кэш
            int savedUnits = 0;
            for (Map.Entry<String, List<DbReportInfo>> entry : reportsByUnit.entrySet()) {
                if (stopRequested.get()) break;

                String unitCode = entry.getKey();
                List<DbReportInfo> reports = entry.getValue();

                // Загружаем составные отчёты (рекурсивно)
                for (DbReportInfo report : reports) {
                    if (report.isComposite()) {
                        List<DbReportInfo> children = loadCompositeReportsOptimized(report.getRepID());
                        for (DbReportInfo child : children) {
                            report.addChild(child);
                        }
                    }
                }

                // Прямое сохранение в кэш
                DatabaseCacheManager.putOracleReport(unitCode, reports);
                savedUnits++;

                if (savedUnits % 10 == 0) {
                    updateProgress("Oracle отчёты: обработано " + savedUnits + " unit'ов");
                    log("  Обработано " + savedUnits + " unit'ов, сохранено отчётов: " + totalReports);
                }
            }

            log("[Oracle] Загружено отчётов для " + savedUnits + " unit'ов, всего отчётов: " + totalReports);
            return savedUnits;

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки отчётов: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Оптимизированная загрузка составных отчётов
     */
    private List<DbReportInfo> loadCompositeReportsOptimized(int parentReportId) {
        List<DbReportInfo> result = new ArrayList<>();

        String sql =
                "SELECT rep.ID, rep.REP_CODE, rep.REP_NAME, rep.REP_TYPE, " +
                        "rep.REP_FILENAME, rep.LPU, drl.PRIV_NAME " +
                        "FROM D_REPORTS_STRUCTURE t " +
                        "JOIN D_REPORTS rep ON rep.ID = t.SUBREPORT " +
                        "LEFT JOIN D_REPORTS_LINKS drl ON drl.PID = rep.ID " +
                        "WHERE t.PID = ? ORDER BY t.SORT";

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, parentReportId);
            pstmt.setQueryTimeout(30);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DbReportInfo report = new DbReportInfo();
                report.setRepID(rs.getInt("ID"));
                report.setRepCode(rs.getString("REP_CODE"));
                report.setRepName(rs.getString("REP_NAME"));
                report.setRepType(rs.getInt("REP_TYPE"));
                report.setRepFilename(rs.getString("REP_FILENAME"));
                report.setPrivName(rs.getString("PRIV_NAME"));
                report.setUnitCode(rs.getString("LPU"));

                if (report.isComposite()) {
                    List<DbReportInfo> children = loadCompositeReportsOptimized(report.getRepID());
                    for (DbReportInfo child : children) {
                        report.addChild(child);
                    }
                }
                result.add(report);
            }

        } catch (SQLException e) {
            error("Ошибка загрузки составных отчётов для ID=" + parentReportId + ": " + e.getMessage());
        }

        return result;
    }
    /**
     * Оптимизированная загрузка всех отчётов из PostgreSQL одним запросом
     */
    private int loadAllPostgresReportsOptimized() {
        log("[PostgreSQL] Оптимизированная загрузка всех отчётов (один запрос)...");
        updateProgress("Загрузка отчётов PostgreSQL...");

        String sql =
                "SELECT unitcode, id, priv_name, rep_type, rep_data, " +
                        "rep_filename, rep_name, rep_code " +
                        "FROM d_reports_links drl " +
                        "JOIN d_reports rep ON drl.pid = rep.id " +
                        "ORDER BY unitcode";

        Map<String, List<DbReportInfo>> reportsByUnit = new LinkedHashMap<>();
        int totalReports = 0;

        try (Connection conn = getPostgresConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(120);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                if (stopRequested.get()) break;

                String unitCode = rs.getString("unitcode");
                if (unitCode == null || unitCode.trim().isEmpty()) {
                    continue;
                }

                DbReportInfo report = new DbReportInfo();
                report.setRepID(rs.getInt("id"));
                report.setPrivName(rs.getString("priv_name"));
                report.setRepType(rs.getInt("rep_type"));
                report.setRepData(rs.getBytes("rep_data"));
                report.setRepFilename(rs.getString("rep_filename"));
                report.setRepName(rs.getString("rep_name"));
                report.setRepCode(rs.getString("rep_code"));
                report.setUnitCode(unitCode);

                reportsByUnit.computeIfAbsent(unitCode, k -> new ArrayList<>()).add(report);
                totalReports++;
            }

            log("[PostgreSQL] Получено отчётов: " + totalReports + ", UNITCODE: " + reportsByUnit.size());

            int savedUnits = 0;
            for (Map.Entry<String, List<DbReportInfo>> entry : reportsByUnit.entrySet()) {
                if (stopRequested.get()) break;

                String unitCode = entry.getKey();
                List<DbReportInfo> reports = entry.getValue();

                // Загружаем составные отчёты
                for (DbReportInfo report : reports) {
                    if (report.isComposite()) {
                        List<DbReportInfo> children = loadPostgresCompositeReportsOptimized(report.getRepID());
                        for (DbReportInfo child : children) {
                            report.addChild(child);
                        }
                    }
                }

                DatabaseCacheManager.putPostgresReport(unitCode, reports);
                savedUnits++;
            }

            log("[PostgreSQL] Загружено отчётов для " + savedUnits + " unit'ов, всего отчётов: " + totalReports);
            return savedUnits;

        } catch (SQLException e) {
            error("[PostgreSQL] Ошибка загрузки отчётов: " + e.getMessage());
        }
        return 0;
    }

    private List<DbReportInfo> loadPostgresCompositeReportsOptimized(int parentReportId) {
        List<DbReportInfo> result = new ArrayList<>();

        String sql =
                "SELECT rep.id, rep.rep_code, rep.rep_name, rep.rep_type, " +
                        "rep.rep_filename, drl.priv_name " +
                        "FROM d_reports_structure t " +
                        "JOIN d_reports rep ON rep.id = t.subreport " +
                        "LEFT JOIN d_reports_links drl ON drl.pid = rep.id " +
                        "WHERE t.pid = ? ORDER BY t.sort";

        try (Connection conn = getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, parentReportId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DbReportInfo report = new DbReportInfo();
                report.setRepID(rs.getInt("id"));
                report.setRepCode(rs.getString("rep_code"));
                report.setRepName(rs.getString("rep_name"));
                report.setRepType(rs.getInt("rep_type"));
                report.setRepFilename(rs.getString("rep_filename"));
                report.setPrivName(rs.getString("priv_name"));

                if (report.isComposite()) {
                    List<DbReportInfo> children = loadPostgresCompositeReportsOptimized(report.getRepID());
                    for (DbReportInfo child : children) {
                        report.addChild(child);
                    }
                }
                result.add(report);
            }

        } catch (SQLException e) {
            error("Ошибка загрузки составных отчётов PostgreSQL для ID=" + parentReportId + ": " + e.getMessage());
        }

        return result;
    }

    /**
     * Оптимизированная загрузка всех функций PostgreSQL одним запросом
     */
    private int loadAllPostgresFunctionsOptimized() {
        log("[PostgreSQL] Оптимизированная загрузка всех функций (один запрос)...");
        updateProgress("Загрузка функций PostgreSQL...");

        // Один запрос получает ВСЕ функции с их телами
        String sql =
                "SELECT p.proname, " +
                        "       pg_get_functiondef(p.oid) as funcdef, " +
                        "       pg_get_function_arguments(p.oid) as func_args " +
                        "FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = 'public' " +
                        "  AND (proname LIKE 'd\\_%' OR proname LIKE 'f\\_%') " +
                        "ORDER BY proname";

        int count = 0;

        try (Connection conn = getPostgresConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(120);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                if (stopRequested.get()) break;

                String funcName = rs.getString("proname");
                String funcDef = rs.getString("funcdef");
                String funcArgs = rs.getString("func_args");

                if (funcDef != null && !funcDef.isEmpty()) {
                    // Прямое сохранение в кэш
                    DatabaseCacheManager.putPostgresFunctionBody(funcName, funcDef);
                    count++;

                    if (count % 50 == 0) {
                        updateProgress("PostgreSQL функции: " + count);
                        log("  Загружено функций: " + count);
                    }
                }
            }

            log("[PostgreSQL] Загружено функций: " + count);
            return count;

        } catch (SQLException e) {
            error("[PostgreSQL] Ошибка загрузки функций: " + e.getMessage());
        }
        return 0;
    }
    /**
     * Оптимизированная загрузка всех пакетов/функций PostgreSQL одним запросом
     */
    private int loadAllPostgresPackagesOptimized() {
        log("[PostgreSQL] Оптимизированная загрузка всех пакетов (один запрос)...");
        updateProgress("Загрузка пакетов PostgreSQL...");
        int count = 0;
        // Загружаем все функции, включая пакетные
        String sql =
                "SELECT p.proname, " +
                        "       pg_get_functiondef(p.oid) as funcdef, " +
                        "       pg_get_function_arguments(p.oid) as func_args, " +
                        "       pg_get_function_result(p.oid) as func_result " +
                        "FROM pg_proc p " +
                        "JOIN pg_namespace n ON p.pronamespace = n.oid " +
                        "WHERE n.nspname = 'public' " +
                        "  AND (proname LIKE 'd\\_pkg\\_%' OR proname LIKE 'd\\_%' OR proname LIKE 'f\\_%') " +
                        "ORDER BY proname";


        try (Connection conn = getPostgresConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(120);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                if (stopRequested.get()) break;

                String funcName = rs.getString("proname");
                String funcDef = rs.getString("funcdef");

                // Пропускаем системные функции
                if (funcName.startsWith("d_pkg_constants") ||
                        funcName.startsWith("d_pkg_options") ||
                        funcName.startsWith("d_pkg_option_specs")) {
                    continue;
                }

                if (funcDef != null && !funcDef.isEmpty()) {
                    DatabaseCacheManager.putPostgresFunctionBody(funcName, funcDef);
                    count++;

                    if (count % 50 == 0) {
                        updateProgress("PostgreSQL пакеты: " + count);
                        log("  Загружено пакетов/функций: " + count);
                    }
                }
            }

            log("[PostgreSQL] Загружено пакетов/функций: " + count);
            packagesLoaded = count;
            postgresPackagesLoaded = count;
            return count;

        } catch (SQLException e) {
            error("[PostgreSQL] Ошибка загрузки пакетов: " + e.getMessage());
        }
        return 0;
    }
    /**
     * Оптимизированная загрузка всех функций Oracle одним запросом
     */
    private int loadAllOracleFunctionsOptimized() {
        log("[Oracle] Оптимизированная загрузка всех функций (один запрос)...");
        updateProgress("Загрузка функций Oracle...");

        String sql =
                "SELECT OWNER, NAME, TYPE, LINE, TEXT " +
                        "FROM ALL_SOURCE " +
                        "WHERE OWNER = ? " +
                        "  AND TYPE = 'PACKAGE BODY' " +
                        "  AND NAME LIKE 'D\\_PKG\\_%' ESCAPE '\\' " +
                        "ORDER BY OWNER, NAME, LINE";

        Map<String, StringBuilder> functionsMap = new LinkedHashMap<>();
        int count = 0;

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            pstmt.setQueryTimeout(120);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (stopRequested.get()) break;

                String owner = rs.getString("OWNER");
                String name = rs.getString("NAME");
                String text = rs.getString("TEXT");

                String key = (owner + "." + name).toUpperCase();
                functionsMap.computeIfAbsent(key, k -> new StringBuilder()).append(text);
            }

            log("[Oracle] Получено пакетов: " + functionsMap.size());

            for (Map.Entry<String, StringBuilder> entry : functionsMap.entrySet()) {
                if (stopRequested.get()) break;

                String packageName = entry.getKey();
                String packageBody = entry.getValue().toString();

                // Извлекаем отдельные функции из тела пакета
                extractAndSaveFunctions(packageName, packageBody);
                count++;

                if (count % 10 == 0) {
                    updateProgress("Oracle функции: обработано " + count + " пакетов");
                }
            }

            log("[Oracle] Загружено функций: " + functionsLoaded);
            return functionsLoaded;

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки функций: " + e.getMessage());
        }
        return 0;
    }

    private void extractAndSaveFunctions(String packageName, String packageBody) {
        // Регулярное выражение для поиска функций в теле пакета
        Pattern funcPattern = Pattern.compile(
                "(?i)(FUNCTION\\s+(\\w+)\\s*\\([^)]*\\)\\s+RETURN\\s+\\w+\\s+(IS|AS)\\s+.*?END\\s+\\2\\s*;)",
                Pattern.DOTALL);

        Matcher matcher = funcPattern.matcher(packageBody);
        while (matcher.find()) {
            String funcName = matcher.group(2);
            String funcBody = matcher.group(1);
            String fullName = packageName + "." + funcName;

            DatabaseCacheManager.putOracleFunctionBody(fullName, funcBody);
            functionsLoaded++;
        }
    }
    /**
     * Оптимизированная загрузка всех метаданных таблиц одним запросом
     */
    private int loadAllTableMetadataOptimized() {
        log("[Oracle] Оптимизированная загрузка всех метаданных таблиц (один запрос)...");
        updateProgress("Загрузка метаданных таблиц...");

        // ОДИН запрос получает ВСЕ метаданные: колонки, комментарии, индексы, FK
        String sql =
                "SELECT " +
                        "  tc.TABLE_NAME, " +
                        "  tc.COLUMN_NAME, " +
                        "  tc.DATA_TYPE, " +
                        "  tc.DATA_LENGTH, " +
                        "  tc.DATA_PRECISION, " +
                        "  tc.DATA_SCALE, " +
                        "  tc.NULLABLE, " +
                        "  tc.DATA_DEFAULT, " +
                        "  cc.COMMENTS AS COLUMN_COMMENT, " +
                        "  (SELECT LISTAGG(INDEX_NAME, ',') WITHIN GROUP (ORDER BY INDEX_NAME) " +
                        "   FROM ALL_INDEXES i WHERE i.TABLE_NAME = tc.TABLE_NAME AND i.OWNER = tc.OWNER) AS INDEXES, " +
                        "  (SELECT LISTAGG(CONSTRAINT_NAME, ',') WITHIN GROUP (ORDER BY CONSTRAINT_NAME) " +
                        "   FROM ALL_CONSTRAINTS c WHERE c.TABLE_NAME = tc.TABLE_NAME AND c.OWNER = tc.OWNER AND c.CONSTRAINT_TYPE = 'R') AS FK_CONSTRAINTS " +
                        "FROM ALL_TAB_COLUMNS tc " +
                        "LEFT JOIN ALL_COL_COMMENTS cc ON cc.OWNER = tc.OWNER AND cc.TABLE_NAME = tc.TABLE_NAME AND cc.COLUMN_NAME = tc.COLUMN_NAME " +
                        "WHERE tc.OWNER = ? " +
                        "  AND tc.TABLE_NAME LIKE 'D\\_%' ESCAPE '\\' " +
                        "ORDER BY tc.TABLE_NAME, tc.COLUMN_ID";

        Map<String, List<DatabaseCacheManager.ColumnInfo>> columnsByTable = new LinkedHashMap<>();
        Map<String, String> indexesByTable = new LinkedHashMap<>();
        Map<String, String> fkByTable = new LinkedHashMap<>();
        int totalColumns = 0;

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            pstmt.setQueryTimeout(180);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (stopRequested.get()) break;

                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("DATA_TYPE");
                int dataLength = rs.getInt("DATA_LENGTH");
                int precision = rs.getInt("DATA_PRECISION");
                int scale = rs.getInt("DATA_SCALE");
                boolean nullable = "Y".equals(rs.getString("NULLABLE"));
                String defaultValue = rs.getString("DATA_DEFAULT");
                String columnComment = rs.getString("COLUMN_COMMENT");
                String indexes = rs.getString("INDEXES");
                String fkConstraints = rs.getString("FK_CONSTRAINTS");

                // Сохраняем колонки
                DatabaseCacheManager.ColumnInfo colInfo = new DatabaseCacheManager.ColumnInfo(
                        columnName, dataType, dataLength,
                        precision > 0 ? precision : null,
                        scale > 0 ? scale : null,
                        nullable, defaultValue, columnComment
                );
                columnsByTable.computeIfAbsent(tableName, k -> new ArrayList<>()).add(colInfo);
                totalColumns++;

                // Сохраняем индексы
                if (indexes != null && !indexes.isEmpty()) {
                    indexesByTable.put(tableName, indexes);
                }

                // Сохраняем FK
                if (fkConstraints != null && !fkConstraints.isEmpty()) {
                    fkByTable.put(tableName, fkConstraints);
                }
            }

            log("[Oracle] Получено колонок: " + totalColumns + ", таблиц: " + columnsByTable.size());

            // Сохраняем в кэш
            int savedTables = 0;
            for (Map.Entry<String, List<DatabaseCacheManager.ColumnInfo>> entry : columnsByTable.entrySet()) {
                if (stopRequested.get()) break;

                String tableName = entry.getKey();
                List<DatabaseCacheManager.ColumnInfo> columns = entry.getValue();

                // Сохраняем колонки
                DatabaseCacheManager.putTableColumns(tableName, columns);

                // Сохраняем индексы
                String indexesStr = indexesByTable.get(tableName);
                if (indexesStr != null) {
                    List<DatabaseCacheManager.IndexInfo> indexes = parseIndexes(indexesStr, tableName);
                    DatabaseCacheManager.putIndexes(tableName, indexes);
                }

                // Сохраняем FK
                String fkStr = fkByTable.get(tableName);
                if (fkStr != null) {
                    List<DatabaseCacheManager.ForeignKeyInfo> fks = parseForeignKeys(fkStr, tableName);
                    DatabaseCacheManager.putForeignKeys(tableName, fks);
                }

                savedTables++;
                if (savedTables % 50 == 0) {
                    updateProgress("Метаданные: обработано " + savedTables + " таблиц");
                }
            }

            log("[Oracle] Загружены метаданные для " + savedTables + " таблиц");
            return savedTables;

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки метаданных: " + e.getMessage());
        }
        return 0;
    }

    private List<DatabaseCacheManager.IndexInfo> parseIndexes(String indexesStr, String tableName) {
        List<DatabaseCacheManager.IndexInfo> result = new ArrayList<>();
        String[] indexNames = indexesStr.split(",");
        for (String indexName : indexNames) {
            result.add(new DatabaseCacheManager.IndexInfo(indexName.trim(), tableName, false, "NORMAL", 1));
        }
        return result;
    }

    private List<DatabaseCacheManager.ForeignKeyInfo> parseForeignKeys(String fkStr, String tableName) {
        List<DatabaseCacheManager.ForeignKeyInfo> result = new ArrayList<>();
        String[] fkNames = fkStr.split(",");
        for (String fkName : fkNames) {
            result.add(new DatabaseCacheManager.ForeignKeyInfo(fkName.trim(), tableName, "", "", "", "NO ACTION"));
        }
        return result;
    }
    /**
     * Супер-оптимизация: загрузка ВСЕХ данных одним запросом (замена 10+ отдельных запросов)
     */
    private void loadEverythingOptimized() {
        log("[Oracle] СУПЕР-ОПТИМИЗАЦИЯ: загрузка всех данных одним запросом...");

        String sql =
                "SELECT 'TABLE' as OBJECT_TYPE, TABLE_NAME as OBJECT_NAME, 'DDL' as PROPERTY, " +
                        "       DBMS_METADATA.GET_DDL('TABLE', TABLE_NAME, OWNER) as VALUE " +
                        "FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME LIKE 'D\\_%' ESCAPE '\\' " +
                        "UNION ALL " +
                        "SELECT 'VIEW', VIEW_NAME, 'DDL', TEXT FROM ALL_VIEWS WHERE OWNER = ? AND VIEW_NAME LIKE 'D\\_%' ESCAPE '\\' " +
                        "UNION ALL " +
                        "SELECT 'CONSTANT', CONST_CODE, 'VALUE', CONST_VALUE FROM D_PKG_CONSTANTS " +
                        "UNION ALL " +
                        "SELECT 'OPTION', OPTION_CODE, 'VALUE', OPTION_VALUE FROM D_SYSTEM_OPTIONS " +
                        "UNION ALL " +
                        "SELECT 'BROKER', unitbpcode || '_' || standard_action, 'EXECPROC', execproc FROM D_UNITBPS";

        int count = 0;

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            pstmt.setString(2, settings.getOracleUser().toUpperCase());
            pstmt.setQueryTimeout(300);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (stopRequested.get()) break;

                String objectType = rs.getString("OBJECT_TYPE");
                String objectName = rs.getString("OBJECT_NAME");
                String property = rs.getString("PROPERTY");
                String value = rs.getString("VALUE");

                switch (objectType) {
                    case "TABLE":
                        if (value != null) {
                            DatabaseCacheManager.putOracleTableDDL(objectName, value);
                        }
                        break;
                    case "VIEW":
                        if (value != null) {
                            DatabaseCacheManager.putOracleViewDDL(objectName, value);
                        }
                        break;
                    case "CONSTANT":
                        DatabaseCacheManager.putConstant(objectName, value);
                        break;
                    case "OPTION":
                        DatabaseCacheManager.putSystemOption(objectName, value);
                        break;
                    case "BROKER":
                        DatabaseCacheManager.putBrokerExecProc(objectName, property, value);
                        break;
                }
                count++;
            }

            log("[Oracle] Супер-оптимизация завершена. Обработано объектов: " + count);

        } catch (SQLException e) {
            error("[Oracle] Ошибка супер-оптимизации: " + e.getMessage());
        }
    }
    /**
     * Оптимизированная загрузка всех синонимов одним запросом
     */
    private int loadAllSynonymsOptimized() {
        log("[Oracle] Оптимизированная загрузка всех синонимов (один запрос)...");
        updateProgress("Загрузка синонимов...");

        String sql =
                "SELECT SYNONYM_NAME, TABLE_OWNER, TABLE_NAME " +
                        "FROM ALL_SYNONYMS " +
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

                DatabaseCacheManager.putSynonym(synonymName, tableName);
                count++;

                if (count % 500 == 0) {
                    updateProgress("Синонимы: " + count);
                }
            }

            log("[Oracle] Загружено синонимов: " + count);
            return count;

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки синонимов: " + e.getMessage());
        }
        return 0;
    }/**
     * Оптимизированная загрузка всех последовательностей одним запросом
     */
    private int loadAllSequencesOptimized() {
        log("[Oracle] Оптимизированная загрузка всех последовательностей (один запрос)...");
        updateProgress("Загрузка последовательностей...");

        String sql =
                "SELECT SEQUENCE_NAME, MIN_VALUE, MAX_VALUE, INCREMENT_BY, LAST_NUMBER " +
                        "FROM ALL_SEQUENCES " +
                        "WHERE SEQUENCE_OWNER = ? AND SEQUENCE_NAME LIKE 'SEQ\\_%' ESCAPE '\\'";

        int count = 0;

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            pstmt.setQueryTimeout(60);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (stopRequested.get()) break;

                String seqName = rs.getString("SEQUENCE_NAME");
                DatabaseCacheManager.SequenceInfo seqInfo = new DatabaseCacheManager.SequenceInfo(
                        seqName,
                        rs.getLong("MIN_VALUE"),
                        rs.getLong("MAX_VALUE"),
                        rs.getLong("INCREMENT_BY"),
                        rs.getLong("LAST_NUMBER")
                );

                DatabaseCacheManager.putSequence(seqName, seqInfo);
                count++;

                if (count % 500 == 0) {
                    updateProgress("Последовательности: " + count);
                }
            }

            log("[Oracle] Загружено последовательностей: " + count);
            return count;

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки последовательностей: " + e.getMessage());
        }
        return 0;
    }
    public PopulateResult loadEverythingOptimizedOnly() {
        log("=== СУПЕР-ОПТИМИЗАЦИЯ: ЗАГРУЗКА ВСЕХ ДАННЫХ ОДНИМ ЗАПРОСОМ ===");
        PopulateResult result = new PopulateResult();

        loadEverythingOptimized();

        // Заполняем результат статистикой из кэша
        result.oracleTablesCount = DatabaseCacheManager.getOracleTableDDLCacheSize();
        result.oracleViewsCount = DatabaseCacheManager.getOracleViewDDLCacheSize();
        result.constantsCount = DatabaseCacheManager.getConstantsCacheSize();
        result.systemOptionsCount = DatabaseCacheManager.getSystemOptionsCacheSize();
        result.brokersCount = DatabaseCacheManager.getBrokerExecProcCacheSize();

        DatabaseCacheManager.forceSaveToDisk();
        return result;
    }
    // DatabaseCachePopulator.java

    /**
     * Оптимизированная загрузка всех брокеров одним запросом
     */
    private int loadAllBrokersOptimized() {
        log("[Oracle] Оптимизированная загрузка всех брокеров (один запрос)...");
        updateProgress("Загрузка брокеров...");

        // Один запрос получает ВСЕ записи из D_UNITBPS
        String sql = "SELECT unitbpcode, standard_action, execproc FROM D_UNITBPS";

        int count = 0;

        try (Connection conn = getOracleConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(120);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                if (stopRequested.get()) break;

                String unit = rs.getString("unitbpcode");
                String action = rs.getString("standard_action");
                String execProc = rs.getString("execproc");

                if (unit != null && action != null && execProc != null && !execProc.isEmpty()) {
                    // Прямое сохранение в кэш
                    DatabaseCacheManager.putBrokerExecProc(unit, action, execProc);
                    count++;

                    if (count % 1000 == 0) {
                        updateProgress("Брокеры: " + count);
                        log("  Загружено брокеров: " + count);
                    }
                }
            }

            log("[Oracle] Загружено брокеров: " + count);
            brokersLoaded = count;
            return count;

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки брокеров: " + e.getMessage());
        }
        return 0;
    }// DatabaseCachePopulator.java

    /**
     * Оптимизированная загрузка всех системных опций одним запросом
     */
    private int loadAllSystemOptionsOptimized() {
        log("[Oracle] Оптимизированная загрузка системных опций (один запрос)...");
        updateProgress("Загрузка системных опций...");

        // Один запрос получает ВСЕ системные опции
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

                if (optionCode != null && !optionCode.isEmpty()) {
                    // Прямое сохранение в кэш
                    DatabaseCacheManager.putSystemOption(optionCode, optionValue);
                    count++;

                    if (count % 500 == 0) {
                        updateProgress("Системные опции: " + count);
                    }
                }
            }

            log("[Oracle] Загружено системных опций: " + count);
            int systemOptionsLoaded = count;
            return count;

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки системных опций: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Оптимизированная загрузка всех констант одним запросом
     */
    private int loadAllConstantsOptimized() {
        log("[Oracle] Оптимизированная загрузка констант (один запрос)...");
        updateProgress("Загрузка констант...");

        // Один запрос получает ВСЕ константы
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

                if (constCode != null && !constCode.isEmpty()) {
                    // Прямое сохранение в кэш
                    DatabaseCacheManager.putConstant(constCode, constValue);
                    count++;

                    if (count % 500 == 0) {
                        updateProgress("Константы: " + count);
                    }
                }
            }

            log("[Oracle] Загружено констант: " + count);
            return count;

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки констант: " + e.getMessage());
        }
        return 0;
    }

    // DatabaseCachePopulator.java

    /**
     * Оптимизированная загрузка всех пакетов Oracle одним запросом
     */
    private int loadAllOraclePackagesOptimized() {
        log("[Oracle] Оптимизированная загрузка всех пакетов (один запрос)...");
        updateProgress("Загрузка пакетов Oracle...");

        // Один запрос получает все пакеты из Oracle
        String sql =
                "SELECT DISTINCT NAME, OWNER, TYPE " +
                        "FROM ALL_SOURCE " +
                        "WHERE OWNER = ? " +
                        "  AND TYPE = 'PACKAGE' " +
                        "  AND NAME LIKE 'D\\_PKG\\_%' ESCAPE '\\' " +
                        "ORDER BY NAME";

        int count = 0;

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            pstmt.setQueryTimeout(60);
            ResultSet rs = pstmt.executeQuery();

            List<String> packageNames = new ArrayList<>();
            while (rs.next()) {
                if (stopRequested.get()) break;
                String packageName = rs.getString("NAME");
                packageNames.add(packageName);
            }

            log("[Oracle] Найдено пакетов: " + packageNames.size());
            updateProgress("Загрузка пакетов Oracle (" + packageNames.size() + " шт.)...");

            // Второй запрос - получаем текст всех пакетов
            String sourceSql =
                    "SELECT NAME, TEXT, LINE " +
                            "FROM ALL_SOURCE " +
                            "WHERE OWNER = ? " +
                            "  AND TYPE = 'PACKAGE' " +
                            "  AND NAME LIKE 'D\\_PKG\\_%' ESCAPE '\\' " +
                            "ORDER BY NAME, LINE";

            try (PreparedStatement pstmt2 = conn.prepareStatement(sourceSql)) {
                pstmt2.setString(1, settings.getOracleUser().toUpperCase());
                pstmt2.setQueryTimeout(120);
                ResultSet rs2 = pstmt2.executeQuery();

                // Собираем текст каждого пакета
                Map<String, StringBuilder> packageSpecs = new LinkedHashMap<>();
                while (rs2.next()) {
                    if (stopRequested.get()) break;
                    String packageName = rs2.getString("NAME");
                    String text = rs2.getString("TEXT");

                    packageSpecs.computeIfAbsent(packageName, k -> new StringBuilder()).append(text);
                }

                // Сохраняем в кэш
                for (Map.Entry<String, StringBuilder> entry : packageSpecs.entrySet()) {
                    if (stopRequested.get()) break;

                    String packageName = entry.getKey();
                    String specText = entry.getValue().toString();

                    if (specText != null && !specText.isEmpty()) {
                        String formattedSpec = "-- Oracle Package: " + packageName + "\n" + specText;
                        DatabaseCacheManager.putOraclePackageSpec(packageName, formattedSpec);
                        count++;

                        if (count % 50 == 0) {
                            updateProgress("Oracle пакеты: " + count + "/" + packageSpecs.size());
                            log("  Загружено пакетов: " + count);
                        }
                    }
                }
            }

            log("[Oracle] Загружено пакетов: " + count);
            packagesLoaded = count;
            return count;

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки пакетов: " + e.getMessage());
        }
        return 0;
    }
    /**
     * Оптимизированная загрузка всех вьюх Oracle одним запросом
     */
    private int loadAllOracleViewsOptimized() {
        log("[Oracle] Оптимизированная загрузка всех вьюх (один запрос)...");
        updateProgress("Загрузка вьюх из Oracle...");

        // ОДИН запрос получает DDL всех вьюх
        String sql =
                "SELECT VIEW_NAME, TEXT " +
                        "FROM ALL_VIEWS " +
                        "WHERE OWNER = ? AND VIEW_NAME LIKE 'D\\_%' ESCAPE '\\'";

        int count = 0;
        int total = 0;

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            pstmt.setQueryTimeout(120);
            ResultSet rs = pstmt.executeQuery();

            // Подсчитываем общее количество
            List<String> viewNames = new ArrayList<>();
            Map<String, String> viewDDLMap = new LinkedHashMap<>();

            while (rs.next()) {
                if (stopRequested.get()) break;
                String viewName = rs.getString("VIEW_NAME");
                String text = rs.getString("TEXT");

                viewNames.add(viewName);
                if (text != null) {
                    viewDDLMap.put(viewName, "-- Oracle View: " + viewName + "\n" + text);
                }
                total++;
            }

            log("[Oracle] Найдено вьюх: " + total);
            updateProgress("Сохранение вьюх Oracle (" + total + " шт.)...");

            // Сохраняем в кэш (без отдельных запросов)
            for (Map.Entry<String, String> entry : viewDDLMap.entrySet()) {
                if (stopRequested.get()) break;

                String viewName = entry.getKey();
                String ddl = entry.getValue();

                DatabaseCacheManager.putOracleViewDDL(viewName, ddl);
                count++;

                if (count % 500 == 0) {
                    updateProgress("Oracle вьюхи: " + count + "/" + total);
                    log("  Сохранено вьюх: " + count);
                }
            }

            log("[Oracle] Загружено вьюх: " + count);
            viewsLoaded = count;
            return count;

        } catch (SQLException e) {
            error("[Oracle] Ошибка загрузки вьюх: " + e.getMessage());
        }
        return 0;
    }
    /**
     * Оптимизированная загрузка всех вьюх PostgreSQL одним запросом
     */
    private int loadAllPostgresViewsOptimized() {
        log("[PostgreSQL] Оптимизированная загрузка всех вьюх (один запрос)...");
        updateProgress("Загрузка вьюх из PostgreSQL...");

        // Один запрос получает DDL всех вьюх
        String sql =
                "SELECT schemaname, viewname, pg_get_viewdef(viewname, true) as viewdef " +
                        "FROM pg_views " +
                        "WHERE schemaname = 'public' AND viewname LIKE 'D\\_%'";

        int count = 0;

        try (Connection conn = getPostgresConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(120);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                if (stopRequested.get()) break;

                String viewName = rs.getString("viewname");
                String viewDef = rs.getString("viewdef");

                if (viewDef != null && !viewDef.isEmpty()) {
                    String ddl = "-- PostgreSQL View: " + viewName + "\n" + viewDef;
                    DatabaseCacheManager.putPostgresViewDDL(viewName, ddl);
                    count++;

                    if (count % 500 == 0) {
                        updateProgress("PostgreSQL вьюхи: " + count);
                        log("  Загружено вьюх: " + count);
                    }
                }
            }

            log("[PostgreSQL] Загружено вьюх: " + count);
            viewsLoaded = count;
            return count;

        } catch (SQLException e) {
            error("[PostgreSQL] Ошибка загрузки вьюх: " + e.getMessage());
        }
        return 0;
    }
}