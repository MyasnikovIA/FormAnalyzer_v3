// core/cache/DatabaseCacheManager.java

package ru.tmis.analyzer.core.cache;

import ru.tmis.analyzer.core.db.DatabaseObjectChecker;
import ru.tmis.analyzer.core.model.DbReportInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Централизованное управление кэшами для БД объектов
 */
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

    // Брокеры (Oracle)
    private static final Map<String, String> brokerExecProcCache = new ConcurrentHashMap<>();

    // Отчёты (Oracle)
    private static final Map<String, List<DbReportInfo>> oracleReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DbReportInfo>> oracleCompositeReportsCache = new ConcurrentHashMap<>();

    // Отчёты (PostgreSQL)
    private static final Map<String, List<DbReportInfo>> postgresReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DbReportInfo>> postgresCompositeReportsCache = new ConcurrentHashMap<>();

    // OID вьюх (PostgreSQL)
    private static final Map<String, Integer> postgresViewOidCache = new ConcurrentHashMap<>();

    // Проверка первичных ключей
    private static final Map<String, DatabaseObjectChecker.PrimaryKeyInfo> pkCache = new ConcurrentHashMap<>();

    // Проверка NOT NULL constraints
    private static final Map<String, List<DatabaseObjectChecker.NotNullConstraintInfo>> notNullCache = new ConcurrentHashMap<>();

    // Проверка функций PostgreSQL (результаты plpgsql_check)
    private static final Map<String, Object> postgresFunctionCheckCache = new ConcurrentHashMap<>();

    // ==================== ГЕТТЕРЫ С КЭШИРОВАНИЕМ ====================

    // Существующие методы (оставляем без изменений)
    public static String getOracleViewDDL(String viewName, Supplier<String> loader) {
        String key = viewName.toUpperCase();
        return oracleViewDDLCache.computeIfAbsent(key, k -> loader.get());
    }

    public static String getPostgresViewDDL(String viewName, Supplier<String> loader) {
        String key = viewName.toLowerCase();
        return postgresViewDDLCache.computeIfAbsent(key, k -> loader.get());
    }

    public static String getOracleTableDDL(String tableName, Supplier<String> loader) {
        String key = tableName.toUpperCase();
        return oracleTableDDLCache.computeIfAbsent(key, k -> loader.get());
    }

    public static String getPostgresTableDDL(String tableName, Supplier<String> loader) {
        String key = tableName.toLowerCase();
        return postgresTableDDLCache.computeIfAbsent(key, k -> loader.get());
    }

    public static String getOracleFunctionBody(String functionKey, Supplier<String> loader) {
        String key = functionKey.toUpperCase();
        return oracleFunctionBodyCache.computeIfAbsent(key, k -> loader.get());
    }

    public static String getPostgresFunctionBody(String functionKey, Supplier<String> loader) {
        String key = functionKey.toLowerCase();
        return postgresFunctionBodyCache.computeIfAbsent(key, k -> loader.get());
    }

    public static ViewTableDependencies getViewDependencies(String viewName, Supplier<ViewTableDependencies> loader) {
        String key = viewName.toUpperCase();
        return viewDependenciesCache.computeIfAbsent(key, k -> loader.get());
    }

    public static Long getOracleCount(String objectName, Supplier<Long> loader) {
        String key = objectName.toUpperCase();
        return oracleCountCache.computeIfAbsent(key, k -> loader.get());
    }

    public static Long getPostgresCount(String objectName, Supplier<Long> loader) {
        String key = objectName.toLowerCase();
        return postgresCountCache.computeIfAbsent(key, k -> loader.get());
    }

    // ==================== НОВЫЕ МЕТОДЫ ====================

    /**
     * 1. Кэш для брокеров (поиск execProc в Oracle)
     */
    public static String getBrokerExecProc(String unit, String action, Supplier<String> loader) {
        String key = (unit + "_" + action).toUpperCase();
        return brokerExecProcCache.computeIfAbsent(key, k -> loader.get());
    }

    /**
     * 2. Кэш для отчётов Oracle по unit
     */
    public static List<DbReportInfo> getOracleReports(String unitCode, Supplier<List<DbReportInfo>> loader) {
        String key = unitCode.toUpperCase();
        return oracleReportsCache.computeIfAbsent(key, k -> loader.get());
    }

    /**
     * 3. Кэш для составных отчётов Oracle
     */
    public static List<DbReportInfo> getOracleCompositeReports(int parentId, Supplier<List<DbReportInfo>> loader) {
        String key = "COMPOSITE_ORACLE_" + parentId;
        return oracleCompositeReportsCache.computeIfAbsent(key, k -> loader.get());
    }

    /**
     * 4. Кэш для отчётов PostgreSQL по unit
     */
    public static List<DbReportInfo> getPostgresReports(String unitCode, Supplier<List<DbReportInfo>> loader) {
        String key = unitCode.toUpperCase();
        return postgresReportsCache.computeIfAbsent(key, k -> loader.get());
    }

    /**
     * 5. Кэш для составных отчётов PostgreSQL
     */
    public static List<DbReportInfo> getPostgresCompositeReports(int parentId, Supplier<List<DbReportInfo>> loader) {
        String key = "COMPOSITE_POSTGRES_" + parentId;
        return postgresCompositeReportsCache.computeIfAbsent(key, k -> loader.get());
    }

    /**
     * 6. Кэш для OID вьюх PostgreSQL
     */
    public static int getPostgresViewOid(String viewName, Supplier<Integer> loader) {
        String key = viewName.toLowerCase();
        return postgresViewOidCache.computeIfAbsent(key, k -> loader.get());
    }

    /**
     * 7. Кэш для проверки первичных ключей
     */
    public static DatabaseObjectChecker.PrimaryKeyInfo getPrimaryKeyInfo(String tableName, Supplier<DatabaseObjectChecker.PrimaryKeyInfo> loader) {
        String key = tableName.toUpperCase();
        return pkCache.computeIfAbsent(key, k -> loader.get());
    }

    /**
     * 8. Кэш для проверки NOT NULL constraints
     */
    public static List<DatabaseObjectChecker.NotNullConstraintInfo> getNotNullConstraints(String tableName, Supplier<List<DatabaseObjectChecker.NotNullConstraintInfo>> loader) {
        String key = tableName.toUpperCase();
        return notNullCache.computeIfAbsent(key, k -> loader.get());
    }

    /**
     * 9. Кэш для проверки функций PostgreSQL
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPostgresFunctionCheck(String functionName, Supplier<T> loader) {
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
    }

    // ==================== ОЧИСТКА ====================

    public static void clearAll() {
        // Существующие
        oracleViewDDLCache.clear();
        postgresViewDDLCache.clear();
        oracleTableDDLCache.clear();
        postgresTableDDLCache.clear();
        oracleFunctionBodyCache.clear();
        postgresFunctionBodyCache.clear();
        viewDependenciesCache.clear();
        oracleCountCache.clear();
        postgresCountCache.clear();

        // Новые
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