// core/cache/DatabaseCacheManager.java

package ru.tmis.analyzer.core.cache;

import ru.tmis.analyzer.core.db.*;
import ru.tmis.analyzer.core.model.DbReportInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final Map<String, String> brokerExecProcCache = new ConcurrentHashMap<>();


    // ==================== НОВЫЕ КЭШИ ====================
    private static final Map<String, List<DbReportInfo>> oracleReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DbReportInfo>> oracleCompositeReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DbReportInfo>> postgresReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DbReportInfo>> postgresCompositeReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, Integer> postgresViewOidCache = new ConcurrentHashMap<>();
    private static final Map<String, DatabaseObjectChecker.PrimaryKeyInfo> pkCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DatabaseObjectChecker.NotNullConstraintInfo>> notNullCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> postgresFunctionCheckCache = new ConcurrentHashMap<>();

    // ==================== СТАТУСЫ ПОДКЛЮЧЕНИЯ ====================
    private static volatile boolean oracleAvailable = true;
    private static volatile boolean postgresAvailable = true;
    private static volatile boolean oracleChecked = false;
    private static volatile boolean postgresChecked = false;

    // Конфигурация БД (кэшируем для проверки)
    private static volatile String cachedOracleUrl;
    private static volatile String cachedOracleUser;
    private static volatile String cachedOraclePassword;
    private static volatile String cachedPostgresUrl;
    private static volatile String cachedPostgresUser;
    private static volatile String cachedPostgresPassword;


    // Время жизни кэша (миллисекунды)
    private static final long CACHE_TTL = 3600000; // 1 час
    private static final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();


    // ==================== ИНИЦИАЛИЗАЦИЯ И ПРОВЕРКИ ====================

    /**
     * Инициализация конфигурации БД (вызывается при старте и при сохранении настроек)
     */
    // core/cache/DatabaseCacheManager.java - метод initDbConfig
    public static void initDbConfig(String oracleUrl, String oracleUser, String oraclePassword,
                                    String postgresUrl, String postgresUser, String postgresPassword,
                                    String postgresMisUser) {
        cachedOracleUrl = oracleUrl;
        cachedOracleUser = oracleUser;
        cachedOraclePassword = oraclePassword;
        cachedPostgresUrl = postgresUrl;
        cachedPostgresUser = postgresUser;
        cachedPostgresPassword = postgresPassword;

        // Сбрасываем статусы при новой конфигурации
        oracleChecked = false;
        postgresChecked = false;
        oracleAvailable = true;
        postgresAvailable = true;

        // Выполняем проверку
        checkConnections();

        // Инициализируем пул соединений через DatabaseConnectionManager (ТОЛЬКО ПОСЛЕ ПРОВЕРКИ ДОСТУПНОСТИ)
        DatabaseConnectionManager.init(oracleUrl, oracleUser, oraclePassword,
                postgresUrl, postgresUser, postgresPassword,
                postgresMisUser);

        // Обновляем статусы на основе проверки сети
        oracleAvailable = DatabaseConnectionManager.isOracleNetworkAvailable();
        postgresAvailable = DatabaseConnectionManager.isPostgresNetworkAvailable();
    }

    /**
     * Проверить доступность БД и сохранить статус
     */
    public static void checkConnections() {
        checkOracleConnection();
        checkPostgresConnection();

        System.out.println("[DB] Статус подключений:");
        System.out.println("  Oracle: " + (oracleAvailable ? "ДОСТУПНА" : "НЕДОСТУПНА"));
        System.out.println("  PostgreSQL: " + (postgresAvailable ? "ДОСТУПНА" : "НЕДОСТУПНА"));
    }

    private static void checkOracleConnection() {
        if (oracleChecked) {
            return;
        }

        if (cachedOracleUrl == null || cachedOracleUrl.isEmpty()) {
            oracleAvailable = false;
            oracleChecked = true;
            System.err.println("[DB] Oracle URL не настроен");
            return;
        }

        try (Connection conn = DriverManager.getConnection(
                cachedOracleUrl, cachedOracleUser, cachedOraclePassword)) {
            oracleAvailable = conn.isValid(5);
        } catch (SQLException e) {
            oracleAvailable = false;
            System.err.println("[DB] Oracle недоступен: " + e.getMessage());
        }
        oracleChecked = true;
    }

    private static void checkPostgresConnection() {
        if (postgresChecked) {
            return;
        }

        if (cachedPostgresUrl == null || cachedPostgresUrl.isEmpty()) {
            postgresAvailable = false;
            postgresChecked = true;
            System.err.println("[DB] PostgreSQL URL не настроен");
            return;
        }

        try (Connection conn = DriverManager.getConnection(
                cachedPostgresUrl, cachedPostgresUser, cachedPostgresPassword)) {
            postgresAvailable = conn.isValid(5);
        } catch (SQLException e) {
            postgresAvailable = false;
            System.err.println("[DB] PostgreSQL недоступен: " + e.getMessage());
        }
        postgresChecked = true;
    }

    /**
     * Принудительно сбросить статус проверки (при изменении настроек)
     */
    public static void resetConnectionStatus() {
        oracleChecked = false;
        postgresChecked = false;
        oracleAvailable = true;
        postgresAvailable = true;
    }

    public static boolean isOracleAvailable() {
        return oracleAvailable;
    }

    public static boolean isPostgresAvailable() {
        return postgresAvailable;
    }

    // ==================== ОБЩИЕ МЕТОДЫ ДЛЯ ORACLE ====================


    public static ViewTableDependencies getViewDependencies(String viewName, Supplier<ViewTableDependencies> loader) {
        if (!isOracleAvailable()) {
            return null;
        }
        String key = viewName.toUpperCase();
        return viewDependenciesCache.computeIfAbsent(key, k -> loader.get());
    }


    public static List<DbReportInfo> getOracleReports(String unitCode, Supplier<List<DbReportInfo>> loader) {
        if (!isOracleAvailable()) {
            return Collections.emptyList();
        }
        String key = unitCode.toUpperCase();
        return oracleReportsCache.computeIfAbsent(key, k -> loader.get());
    }

    public static List<DbReportInfo> getOracleCompositeReports(int parentId, Supplier<List<DbReportInfo>> loader) {
        if (!isOracleAvailable()) {
            return Collections.emptyList();
        }
        String key = "COMPOSITE_ORACLE_" + parentId;
        return oracleCompositeReportsCache.computeIfAbsent(key, k -> loader.get());
    }


    public static List<DbReportInfo> getPostgresReports(String unitCode, Supplier<List<DbReportInfo>> loader) {
        if (!isPostgresAvailable()) {
            return Collections.emptyList();
        }
        String key = unitCode.toUpperCase();
        return postgresReportsCache.computeIfAbsent(key, k -> loader.get());
    }

    public static List<DbReportInfo> getPostgresCompositeReports(int parentId, Supplier<List<DbReportInfo>> loader) {
        if (!isPostgresAvailable()) {
            return Collections.emptyList();
        }
        String key = "COMPOSITE_POSTGRES_" + parentId;
        return postgresCompositeReportsCache.computeIfAbsent(key, k -> loader.get());
    }

    public static int getPostgresViewOid(String viewName, Supplier<Integer> loader) {
        if (!isPostgresAvailable()) {
            return -1;
        }
        String key = viewName.toLowerCase();
        return postgresViewOidCache.computeIfAbsent(key, k -> loader.get());
    }

    public static DatabaseObjectChecker.PrimaryKeyInfo getPrimaryKeyInfo(String tableName, Supplier<DatabaseObjectChecker.PrimaryKeyInfo> loader) {
        if (!isOracleAvailable() || !isPostgresAvailable()) {
            return null;
        }
        String key = tableName.toUpperCase();
        return pkCache.computeIfAbsent(key, k -> loader.get());
    }

    public static List<DatabaseObjectChecker.NotNullConstraintInfo> getNotNullConstraints(String tableName, Supplier<List<DatabaseObjectChecker.NotNullConstraintInfo>> loader) {
        if (!isOracleAvailable() || !isPostgresAvailable()) {
            return Collections.emptyList();
        }
        String key = tableName.toUpperCase();
        return notNullCache.computeIfAbsent(key, k -> loader.get());
    }

    @SuppressWarnings("unchecked")
    public static <T> T getPostgresFunctionCheck(String functionName, Supplier<T> loader) {
        if (!isPostgresAvailable()) {
            return null;
        }
        String key = functionName.toLowerCase();
        return (T) postgresFunctionCheckCache.computeIfAbsent(key, k -> loader.get());
    }

    // ==================== СТАТИСТИКА ====================

    /**
     * Получить из кэша с проверкой TTL
     */
    private static <T> T getFromCache(Map<String, T> cache, String key) {
        T value = cache.get(key);
        if (value != null) {
            Long timestamp = cacheTimestamps.get(key);
            if (timestamp != null && (System.currentTimeMillis() - timestamp) < CACHE_TTL) {
                return value;
            } else {
                // Просрочено - удаляем
                cache.remove(key);
                cacheTimestamps.remove(key);
                return null;
            }
        }
        return null;
    }
    /**
     * Сохранить в кэш
     */
    private static <T> void putToCache(Map<String, T> cache, String key, T value) {
        cache.put(key, value);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }
    public static String getOracleViewDDL(String viewName, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = viewName.toUpperCase();

        String cached = getFromCache(oracleViewDDLCache, key);
        if (cached != null) return cached;

        String ddl = loader.get();
        if (ddl != null) {
            putToCache(oracleViewDDLCache, key, ddl);
        }
        return ddl;
    }

    public static String getOracleTableDDL(String tableName, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = tableName.toUpperCase();

        String cached = getFromCache(oracleTableDDLCache, key);
        if (cached != null) return cached;

        String ddl = loader.get();
        if (ddl != null) {
            putToCache(oracleTableDDLCache, key, ddl);
        }
        return ddl;
    }

    public static String getOracleFunctionBody(String functionKey, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = functionKey.toUpperCase();

        String cached = getFromCache(oracleFunctionBodyCache, key);
        if (cached != null) return cached;

        String body = loader.get();
        if (body != null) {
            putToCache(oracleFunctionBodyCache, key, body);
        }
        return body;
    }

    public static Long getOracleCount(String objectName, Supplier<Long> loader) {
        if (!isOracleAvailable()) return -1L;
        String key = objectName.toUpperCase();

        Long cached = getFromCache(oracleCountCache, key);
        if (cached != null) return cached;

        Long count = loader.get();
        if (count != null && count >= 0) {
            putToCache(oracleCountCache, key, count);
        }
        return count;
    }

    public static String getBrokerExecProc(String unit, String action, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = (unit + "_" + action).toUpperCase();

        String cached = getFromCache(brokerExecProcCache, key);
        if (cached != null) return cached;

        String execProc = loader.get();
        if (execProc != null) {
            putToCache(brokerExecProcCache, key, execProc);
        }
        return execProc;
    }

    // ========== МЕТОДЫ ДЛЯ POSTGRESQL ==========

    public static String getPostgresViewDDL(String viewName, Supplier<String> loader) {
        if (!isPostgresAvailable()) return null;
        String key = viewName.toLowerCase();

        String cached = getFromCache(postgresViewDDLCache, key);
        if (cached != null) return cached;

        String ddl = loader.get();
        if (ddl != null) {
            putToCache(postgresViewDDLCache, key, ddl);
        }
        return ddl;
    }

    public static String getPostgresTableDDL(String tableName, Supplier<String> loader) {
        if (!isPostgresAvailable()) return null;
        String key = tableName.toLowerCase();

        String cached = getFromCache(postgresTableDDLCache, key);
        if (cached != null) return cached;

        String ddl = loader.get();
        if (ddl != null) {
            putToCache(postgresTableDDLCache, key, ddl);
        }
        return ddl;
    }

    public static String getPostgresFunctionBody(String functionKey, Supplier<String> loader) {
        if (!isPostgresAvailable()) return null;
        String key = functionKey.toLowerCase();

        String cached = getFromCache(postgresFunctionBodyCache, key);
        if (cached != null) return cached;

        String body = loader.get();
        if (body != null) {
            putToCache(postgresFunctionBodyCache, key, body);
        }
        return body;
    }

    public static Long getPostgresCount(String objectName, Supplier<Long> loader) {
        if (!isPostgresAvailable()) return -1L;
        String key = objectName.toLowerCase();

        Long cached = getFromCache(postgresCountCache, key);
        if (cached != null) return cached;

        Long count = loader.get();
        if (count != null && count >= 0) {
            putToCache(postgresCountCache, key, count);
        }
        return count;
    }

    // ========== СТАТИСТИКА ==========

    public static void printStats() {
        System.out.println("=== СТАТИСТИКА ГЛОБАЛЬНОГО КЭША БД ===");
        System.out.println("Oracle View DDL: " + oracleViewDDLCache.size());
        System.out.println("PostgreSQL View DDL: " + postgresViewDDLCache.size());
        System.out.println("Oracle Table DDL: " + oracleTableDDLCache.size());
        System.out.println("PostgreSQL Table DDL: " + postgresTableDDLCache.size());
        System.out.println("Oracle Function Body: " + oracleFunctionBodyCache.size());
        System.out.println("PostgreSQL Function Body: " + postgresFunctionBodyCache.size());
        System.out.println("Oracle Count: " + oracleCountCache.size());
        System.out.println("PostgreSQL Count: " + postgresCountCache.size());
        System.out.println("Broker ExecProc: " + brokerExecProcCache.size());
    }

    public static void clearAll() {
        oracleViewDDLCache.clear();
        postgresViewDDLCache.clear();
        oracleTableDDLCache.clear();
        postgresTableDDLCache.clear();
        oracleFunctionBodyCache.clear();
        postgresFunctionBodyCache.clear();
        oracleCountCache.clear();
        postgresCountCache.clear();
        brokerExecProcCache.clear();
        cacheTimestamps.clear();
        System.out.println("[КЭШ] Все кэши БД очищены");
    }

}