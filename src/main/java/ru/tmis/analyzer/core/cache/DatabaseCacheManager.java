package ru.tmis.analyzer.core.cache;

import ru.tmis.analyzer.core.model.ViewTableDependencies;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Централизованное управление кэшами для БД объектов
 */
public class DatabaseCacheManager {

    // Кэш DDL вьюх
    private static final Map<String, String> oracleViewDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> postgresViewDDLCache = new ConcurrentHashMap<>();

    // Кэш DDL таблиц
    private static final Map<String, String> oracleTableDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> postgresTableDDLCache = new ConcurrentHashMap<>();

    // Кэш тел функций
    private static final Map<String, String> oracleFunctionBodyCache = new ConcurrentHashMap<>();
    private static final Map<String, String> postgresFunctionBodyCache = new ConcurrentHashMap<>();

    // Кэш зависимостей вьюх (таблицы внутри вьюхи)
    private static final Map<String, ViewTableDependencies> viewDependenciesCache = new ConcurrentHashMap<>();

    // Кэш количества записей
    private static final Map<String, Long> oracleCountCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> postgresCountCache = new ConcurrentHashMap<>();

    // Статистика кэша
    private static int oracleViewHits = 0;
    private static int oracleViewMisses = 0;
    private static int postgresViewHits = 0;
    private static int postgresViewMisses = 0;
    private static int viewDepsHits = 0;
    private static int viewDepsMisses = 0;

    // Геттеры с проверкой кэша
    public static String getOracleViewDDL(String viewName, java.util.function.Supplier<String> loader) {
        String key = viewName.toUpperCase();
        if (oracleViewDDLCache.containsKey(key)) {
            oracleViewHits++;
            return oracleViewDDLCache.get(key);
        }
        oracleViewMisses++;
        String ddl = loader.get();
        if (ddl != null) {
            oracleViewDDLCache.put(key, ddl);
        }
        return ddl;
    }

    public static String getPostgresViewDDL(String viewName, java.util.function.Supplier<String> loader) {
        String key = viewName.toLowerCase();
        if (postgresViewDDLCache.containsKey(key)) {
            postgresViewHits++;
            return postgresViewDDLCache.get(key);
        }
        postgresViewMisses++;
        String ddl = loader.get();
        if (ddl != null) {
            postgresViewDDLCache.put(key, ddl);
        }
        return ddl;
    }

    public static String getOracleTableDDL(String tableName, java.util.function.Supplier<String> loader) {
        String key = tableName.toUpperCase();
        if (oracleTableDDLCache.containsKey(key)) {
            return oracleTableDDLCache.get(key);
        }
        String ddl = loader.get();
        if (ddl != null) {
            oracleTableDDLCache.put(key, ddl);
        }
        return ddl;
    }

    public static String getPostgresTableDDL(String tableName, java.util.function.Supplier<String> loader) {
        String key = tableName.toLowerCase();
        if (postgresTableDDLCache.containsKey(key)) {
            return postgresTableDDLCache.get(key);
        }
        String ddl = loader.get();
        if (ddl != null) {
            postgresTableDDLCache.put(key, ddl);
        }
        return ddl;
    }

    public static String getOracleFunctionBody(String functionKey, java.util.function.Supplier<String> loader) {
        String key = functionKey.toUpperCase();
        if (oracleFunctionBodyCache.containsKey(key)) {
            return oracleFunctionBodyCache.get(key);
        }
        String body = loader.get();
        if (body != null) {
            oracleFunctionBodyCache.put(key, body);
        }
        return body;
    }

    public static String getPostgresFunctionBody(String functionKey, java.util.function.Supplier<String> loader) {
        String key = functionKey.toLowerCase();
        if (postgresFunctionBodyCache.containsKey(key)) {
            return postgresFunctionBodyCache.get(key);
        }
        String body = loader.get();
        if (body != null) {
            postgresFunctionBodyCache.put(key, body);
        }
        return body;
    }

    public static ViewTableDependencies getViewDependencies(String viewName, java.util.function.Supplier<ViewTableDependencies> loader) {
        String key = viewName.toUpperCase();
        if (viewDependenciesCache.containsKey(key)) {
            viewDepsHits++;
            return viewDependenciesCache.get(key);
        }
        viewDepsMisses++;
        ViewTableDependencies deps = loader.get();
        if (deps != null) {
            viewDependenciesCache.put(key, deps);
        }
        return deps;
    }

    public static Long getOracleCount(String objectName, java.util.function.Supplier<Long> loader) {
        String key = objectName.toUpperCase();
        return oracleCountCache.computeIfAbsent(key, k -> loader.get());
    }

    public static Long getPostgresCount(String objectName, java.util.function.Supplier<Long> loader) {
        String key = objectName.toLowerCase();
        return postgresCountCache.computeIfAbsent(key, k -> loader.get());
    }

    // Статистика
    public static void printStats() {
        System.out.println("=== СТАТИСТИКА КЭША ===");
        System.out.println("Oracle View DDL: hits=" + oracleViewHits + ", misses=" + oracleViewMisses +
                ", size=" + oracleViewDDLCache.size());
        System.out.println("PostgreSQL View DDL: hits=" + postgresViewHits + ", misses=" + postgresViewMisses +
                ", size=" + postgresViewDDLCache.size());
        System.out.println("View Dependencies: hits=" + viewDepsHits + ", misses=" + viewDepsMisses +
                ", size=" + viewDependenciesCache.size());
        System.out.println("Oracle Table DDL: size=" + oracleTableDDLCache.size());
        System.out.println("PostgreSQL Table DDL: size=" + postgresTableDDLCache.size());
        System.out.println("Oracle Function Body: size=" + oracleFunctionBodyCache.size());
        System.out.println("PostgreSQL Function Body: size=" + postgresFunctionBodyCache.size());
        System.out.println("Oracle Count: size=" + oracleCountCache.size());
        System.out.println("PostgreSQL Count: size=" + postgresCountCache.size());
    }

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
        oracleViewHits = 0;
        oracleViewMisses = 0;
        postgresViewHits = 0;
        postgresViewMisses = 0;
        viewDepsHits = 0;
        viewDepsMisses = 0;
        System.out.println("[КЭШ] Все кэши очищены");
    }
}