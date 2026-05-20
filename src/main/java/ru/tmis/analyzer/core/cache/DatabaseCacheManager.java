// core/cache/DatabaseCacheManager.java
package ru.tmis.analyzer.core.cache;

import ru.tmis.analyzer.core.db.DatabaseObjectChecker;
import ru.tmis.analyzer.core.model.DbReportInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;


public class DatabaseCacheManager {
    // ==================== СУЩЕСТВУЮЩИЕ КЭШИ ====================
    private static final Map<String, String> oracleViewDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> postgresViewDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> oracleTableDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> postgresTableDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> oracleFunctionBodyCache = new ConcurrentHashMap<>();
    private static final Map<String, String> postgresFunctionBodyCache = new ConcurrentHashMap<>();
    private static final Map<String, ViewTableDependencies> viewDependenciesCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> oracleCountCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> postgresCountCache = new ConcurrentHashMap<>();

    // ==================== НОВЫЕ КЭШИ ====================
    private static final Map<String, String> brokerExecProcCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DbReportInfo>> oracleReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DbReportInfo>> oracleCompositeReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DbReportInfo>> postgresReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DbReportInfo>> postgresCompositeReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, Integer> postgresViewOidCache = new ConcurrentHashMap<>();
    private static final Map<String, DatabaseObjectChecker.PrimaryKeyInfo> pkCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DatabaseObjectChecker.NotNullConstraintInfo>> notNullCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> postgresFunctionCheckCache = new ConcurrentHashMap<>();

    // ==================== СТАТУСЫ ПОДКЛЮЧЕНИЯ К БД ====================
    private static final AtomicBoolean oracleAvailable = new AtomicBoolean(true);
    private static final AtomicBoolean postgresAvailable = new AtomicBoolean(true);

    private static long lastOracleCheck = 0;
    private static long lastPostgresCheck = 0;
    private static final long CHECK_INTERVAL = 60000; // 60 секунд

    private static String cachedOracleUrl;
    private static String cachedOracleUser;
    private static String cachedOraclePassword;
    private static String cachedPostgresUrl;
    private static String cachedPostgresUser;
    private static String cachedPostgresPassword;
    private static String cachedPostgresMisUser;

    /**
     * Инициализация конфигурации БД (вызывать при старте и при изменении настроек)
     */
    public static void initDbConfig(String oracleUrl, String oracleUser, String oraclePassword,
                                    String postgresUrl, String postgresUser, String postgresPassword,
                                    String postgresMisUser) {
        cachedOracleUrl = oracleUrl;
        cachedOracleUser = oracleUser;
        cachedOraclePassword = oraclePassword;
        cachedPostgresUrl = postgresUrl;
        cachedPostgresUser = postgresUser;
        cachedPostgresPassword = postgresPassword;
        cachedPostgresMisUser = postgresMisUser;

        oracleAvailable.set(true);
        postgresAvailable.set(true);
        lastOracleCheck = 0;
        lastPostgresCheck = 0;
    }

    /**
     * Проверка доступности Oracle
     */
    public static boolean isOracleAvailable() {
        long now = System.currentTimeMillis();
        if (now - lastOracleCheck > CHECK_INTERVAL && !oracleAvailable.get()) {
            checkOracleConnection();
        }
        return oracleAvailable.get();
    }

    /**
     * Проверка доступности PostgreSQL
     */
    public static boolean isPostgresAvailable() {
        long now = System.currentTimeMillis();
        if (now - lastPostgresCheck > CHECK_INTERVAL && !postgresAvailable.get()) {
            checkPostgresConnection();
        }
        return postgresAvailable.get();
    }
    private static void checkOracleConnection() {
        if (cachedOracleUrl == null || cachedOracleUrl.isEmpty()) {
            oracleAvailable.set(false);
            return;
        }

        try (Connection conn = DriverManager.getConnection(
                cachedOracleUrl, cachedOracleUser, cachedOraclePassword)) {
            oracleAvailable.set(conn.isValid(5));
            if (!oracleAvailable.get()) {
                System.err.println("[DB] Oracle недоступен");
            }
        } catch (SQLException e) {
            oracleAvailable.set(false);
            System.err.println("[DB] Oracle недоступен: " + e.getMessage());
        }
        lastOracleCheck = System.currentTimeMillis();
    }

    private static void checkPostgresConnection() {
        if (cachedPostgresUrl == null || cachedPostgresUrl.isEmpty()) {
            postgresAvailable.set(false);
            return;
        }

        try (Connection conn = DriverManager.getConnection(
                cachedPostgresUrl, cachedPostgresUser, cachedPostgresPassword)) {
            postgresAvailable.set(conn.isValid(5));
            if (!postgresAvailable.get()) {
                System.err.println("[DB] PostgreSQL недоступен");
            }
        } catch (SQLException e) {
            postgresAvailable.set(false);
            System.err.println("[DB] PostgreSQL недоступен: " + e.getMessage());
        }
        lastPostgresCheck = System.currentTimeMillis();
    }

    public static void checkConnections() {
        checkOracleConnection();
        checkPostgresConnection();
    }

    // ==================== СУЩЕСТВУЮЩИЕ МЕТОДЫ С ПРОВЕРКОЙ ====================

    public static String getOracleViewDDL(String viewName, Supplier<String> loader) {
        if (!isOracleAvailable()) {
            System.err.println("[КЭШ] Oracle недоступен, пропускаем запрос DDL вьюхи: " + viewName);
            return null;
        }
        String key = viewName.toUpperCase();
        return oracleViewDDLCache.computeIfAbsent(key, k -> loader.get());
    }

    public static String getPostgresViewDDL(String viewName, Supplier<String> loader) {
        if (!isPostgresAvailable()) {
            System.err.println("[КЭШ] PostgreSQL недоступен, пропускаем запрос DDL вьюхи: " + viewName);
            return null;
        }
        String key = viewName.toLowerCase();
        return postgresViewDDLCache.computeIfAbsent(key, k -> loader.get());
    }

    public static String getOracleTableDDL(String tableName, Supplier<String> loader) {
        if (!isOracleAvailable()) {
            System.err.println("[КЭШ] Oracle недоступен, пропускаем запрос DDL таблицы: " + tableName);
            return null;
        }
        String key = tableName.toUpperCase();
        return oracleTableDDLCache.computeIfAbsent(key, k -> loader.get());
    }

    public static String getPostgresTableDDL(String tableName, Supplier<String> loader) {
        if (!isPostgresAvailable()) {
            System.err.println("[КЭШ] PostgreSQL недоступен, пропускаем запрос DDL таблицы: " + tableName);
            return null;
        }
        String key = tableName.toLowerCase();
        return postgresTableDDLCache.computeIfAbsent(key, k -> loader.get());
    }

    public static String getOracleFunctionBody(String functionKey, Supplier<String> loader) {
        if (!isOracleAvailable()) {
            System.err.println("[КЭШ] Oracle недоступен, пропускаем запрос тела функции: " + functionKey);
            return null;
        }
        String key = functionKey.toUpperCase();
        return oracleFunctionBodyCache.computeIfAbsent(key, k -> loader.get());
    }

    public static String getPostgresFunctionBody(String functionKey, Supplier<String> loader) {
        if (!isPostgresAvailable()) {
            System.err.println("[КЭШ] PostgreSQL недоступен, пропускаем запрос тела функции: " + functionKey);
            return null;
        }
        String key = functionKey.toLowerCase();
        return postgresFunctionBodyCache.computeIfAbsent(key, k -> loader.get());
    }

    public static ViewTableDependencies getViewDependencies(String viewName, Supplier<ViewTableDependencies> loader) {
        if (!isOracleAvailable()) {
            System.err.println("[КЭШ] Oracle недоступен, пропускаем запрос зависимостей вьюхи: " + viewName);
            return null;
        }
        String key = viewName.toUpperCase();
        return viewDependenciesCache.computeIfAbsent(key, k -> loader.get());
    }

    public static Long getOracleCount(String objectName, Supplier<Long> loader) {
        if (!isOracleAvailable()) {
            System.err.println("[КЭШ] Oracle недоступен, пропускаем подсчёт записей: " + objectName);
            return -1L;
        }
        String key = objectName.toUpperCase();
        return oracleCountCache.computeIfAbsent(key, k -> loader.get());
    }

    public static Long getPostgresCount(String objectName, Supplier<Long> loader) {
        if (!isPostgresAvailable()) {
            System.err.println("[КЭШ] PostgreSQL недоступен, пропускаем подсчёт записей: " + objectName);
            return -1L;
        }
        String key = objectName.toLowerCase();
        return postgresCountCache.computeIfAbsent(key, k -> loader.get());
    }

    // ==================== НОВЫЕ МЕТОДЫ С ПРОВЕРКОЙ ====================

    public static String getBrokerExecProc(String unit, String action, Supplier<String> loader) {
        if (!isOracleAvailable()) {
            System.err.println("[КЭШ] Oracle недоступен, пропускаем поиск execProc для unit=" + unit + ", action=" + action);
            return null;
        }
        String key = (unit + "_" + action).toUpperCase();
        return brokerExecProcCache.computeIfAbsent(key, k -> loader.get());
    }

    public static List<DbReportInfo> getOracleReports(String unitCode, Supplier<List<DbReportInfo>> loader) {
        if (!isOracleAvailable()) {
            System.err.println("[КЭШ] Oracle недоступен, пропускаем запрос отчётов для unit=" + unitCode);
            return Collections.emptyList();
        }
        String key = unitCode.toUpperCase();
        return oracleReportsCache.computeIfAbsent(key, k -> loader.get());
    }

    public static List<DbReportInfo> getOracleCompositeReports(int parentId, Supplier<List<DbReportInfo>> loader) {
        if (!isOracleAvailable()) {
            System.err.println("[КЭШ] Oracle недоступен, пропускаем запрос составных отчётов для ID=" + parentId);
            return Collections.emptyList();
        }
        String key = "COMPOSITE_ORACLE_" + parentId;
        return oracleCompositeReportsCache.computeIfAbsent(key, k -> loader.get());
    }

    public static List<DbReportInfo> getPostgresReports(String unitCode, Supplier<List<DbReportInfo>> loader) {
        if (!isPostgresAvailable()) {
            System.err.println("[КЭШ] PostgreSQL недоступен, пропускаем запрос отчётов для unit=" + unitCode);
            return Collections.emptyList();
        }
        String key = unitCode.toUpperCase();
        return postgresReportsCache.computeIfAbsent(key, k -> loader.get());
    }

    public static List<DbReportInfo> getPostgresCompositeReports(int parentId, Supplier<List<DbReportInfo>> loader) {
        if (!isPostgresAvailable()) {
            System.err.println("[КЭШ] PostgreSQL недоступен, пропускаем запрос составных отчётов для ID=" + parentId);
            return Collections.emptyList();
        }
        String key = "COMPOSITE_POSTGRES_" + parentId;
        return postgresCompositeReportsCache.computeIfAbsent(key, k -> loader.get());
    }

    public static int getPostgresViewOid(String viewName, Supplier<Integer> loader) {
        if (!isPostgresAvailable()) {
            System.err.println("[КЭШ] PostgreSQL недоступен, пропускаем запрос OID вьюхи: " + viewName);
            return -1;
        }
        String key = viewName.toLowerCase();
        return postgresViewOidCache.computeIfAbsent(key, k -> loader.get());
    }

    public static DatabaseObjectChecker.PrimaryKeyInfo getPrimaryKeyInfo(String tableName, Supplier<DatabaseObjectChecker.PrimaryKeyInfo> loader) {
        if (!isOracleAvailable() || !isPostgresAvailable()) {
            System.err.println("[КЭШ] Одна из БД недоступна, пропускаем проверку PK для таблицы: " + tableName);
            return null;
        }
        String key = tableName.toUpperCase();
        return pkCache.computeIfAbsent(key, k -> loader.get());
    }

    public static List<DatabaseObjectChecker.NotNullConstraintInfo> getNotNullConstraints(String tableName, Supplier<List<DatabaseObjectChecker.NotNullConstraintInfo>> loader) {
        if (!isOracleAvailable() || !isPostgresAvailable()) {
            System.err.println("[КЭШ] Одна из БД недоступна, пропускаем проверку NOT NULL для таблицы: " + tableName);
            return Collections.emptyList();
        }
        String key = tableName.toUpperCase();
        return notNullCache.computeIfAbsent(key, k -> loader.get());
    }

    @SuppressWarnings("unchecked")
    public static <T> T getPostgresFunctionCheck(String functionName, Supplier<T> loader) {
        if (!isPostgresAvailable()) {
            System.err.println("[КЭШ] PostgreSQL недоступен, пропускаем проверку функции: " + functionName);
            return null;
        }
        String key = functionName.toLowerCase();
        return (T) postgresFunctionCheckCache.computeIfAbsent(key, k -> loader.get());
    }

    // ==================== СТАТИСТИКА ====================

    public static void printStats() {
        System.out.println("=== СТАТИСТИКА КЭША ===");
        System.out.println("Oracle View DDL: " + oracleViewDDLCache.size());
        System.out.println("PostgreSQL View DDL: " + postgresViewDDLCache.size());
        System.out.println("Oracle Table DDL: " + oracleTableDDLCache.size());
        System.out.println("PostgreSQL Table DDL: " + postgresTableDDLCache.size());
        System.out.println("Oracle Function Body: " + oracleFunctionBodyCache.size());
        System.out.println("PostgreSQL Function Body: " + postgresFunctionBodyCache.size());
        System.out.println("View Dependencies: " + viewDependenciesCache.size());
        System.out.println("Oracle Count: " + oracleCountCache.size());
        System.out.println("PostgreSQL Count: " + postgresCountCache.size());
        System.out.println("Broker ExecProc: " + brokerExecProcCache.size());
        System.out.println("Oracle Reports: " + oracleReportsCache.size());
        System.out.println("PostgreSQL Reports: " + postgresReportsCache.size());
        System.out.println("Primary Key: " + pkCache.size());
        System.out.println("NOT NULL: " + notNullCache.size());
        System.out.println("PostgreSQL View OID: " + postgresViewOidCache.size());
        System.out.println("PostgreSQL Function Check: " + postgresFunctionCheckCache.size());
        System.out.println("Oracle available: " + oracleAvailable.get());
        System.out.println("PostgreSQL available: " + postgresAvailable.get());
    }

    // ==================== ОЧИСТКА ====================

    public static void clearAll() {
        oracleViewDDLCache.clear();
        postgresViewDDLCache.clear();
        oracleTableDDLCache.clear();
        postgresTableDDLCache.clear();
        oracleFunctionBodyCache.clear();
        postgresFunctionBodyCache.clear();
        viewDependenciesCache.clear();
        oracleCountCache.clear();
        postgresCountCache.clear();

        brokerExecProcCache.clear();
        oracleReportsCache.clear();
        oracleCompositeReportsCache.clear();
        postgresReportsCache.clear();
        postgresCompositeReportsCache.clear();
        postgresViewOidCache.clear();
        pkCache.clear();
        notNullCache.clear();
        postgresFunctionCheckCache.clear();

        System.out.println("[КЭШ] Все кэши очищены");
    }
}