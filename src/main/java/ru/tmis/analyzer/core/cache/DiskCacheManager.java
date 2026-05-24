// core/cache/DiskCacheManager.java
package ru.tmis.analyzer.core.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Менеджер для сохранения и загрузки кэша на диск
 * Кэш сохраняется в подкаталог DatabaseCache внутри каталога отчётов
 */
public class DiskCacheManager {

    private static final String CACHE_SUBDIR = "DatabaseCache";

    private static final String VIEW_DDL_ORACLE_FILE = "view_ddl_oracle.json";
    private static final String VIEW_DDL_POSTGRES_FILE = "view_ddl_postgres.json";
    private static final String TABLE_DDL_ORACLE_FILE = "table_ddl_oracle.json";
    private static final String TABLE_DDL_POSTGRES_FILE = "table_ddl_postgres.json";
    private static final String FUNCTION_BODY_ORACLE_FILE = "function_body_oracle.json";
    private static final String FUNCTION_BODY_POSTGRES_FILE = "function_body_postgres.json";
    private static final String VIEW_DEPENDENCIES_FILE = "view_dependencies.json";
    private static final String COUNT_ORACLE_FILE = "count_oracle.json";
    private static final String COUNT_POSTGRES_FILE = "count_postgres.json";
    private static final String BROKER_EXECPROC_FILE = "broker_execproc.json";
    private static final String REPORTS_ORACLE_FILE = "reports_oracle.json";
    private static final String REPORTS_POSTGRES_FILE = "reports_postgres.json";
    private static final String PK_CACHE_FILE = "pk_cache.json";
    private static final String NOT_NULL_CACHE_FILE = "not_null_cache.json";
    private static final String POSTGRES_VIEW_OID_FILE = "postgres_view_oid.json";
    private static final String POSTGRES_FUNCTION_CHECK_FILE = "postgres_function_check.json";

    private Path cachePath;
    private final Gson gson;

    public DiskCacheManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Установить директорию для кэша (внутри каталога отчётов)
     * @param outputDir каталог отчётов
     */
    public void setOutputDir(String outputDir) {
        if (outputDir == null || outputDir.isEmpty()) {
            this.cachePath = Paths.get(CACHE_SUBDIR);
        } else {
            this.cachePath = Paths.get(outputDir, CACHE_SUBDIR);
        }
        System.out.println("[DiskCache] Директория кэша: " + cachePath.toAbsolutePath());
    }

    /**
     * Инициализация директории кэша
     */
    public void init() throws IOException {
        if (cachePath == null) {
            setOutputDir(null);
        }
        if (!Files.exists(cachePath)) {
            Files.createDirectories(cachePath);
            System.out.println("[DiskCache] Создана директория кэша: " + cachePath.toAbsolutePath());
        }
    }

    // ==================== СОХРАНЕНИЕ ====================

    public void saveViewDDLOracle(Map<String, String> cache) {
        saveToFile(VIEW_DDL_ORACLE_FILE, cache);
    }

    public void saveViewDDLPostgres(Map<String, String> cache) {
        saveToFile(VIEW_DDL_POSTGRES_FILE, cache);
    }

    public void saveTableDDLOracle(Map<String, String> cache) {
        saveToFile(TABLE_DDL_ORACLE_FILE, cache);
    }

    public void saveTableDDLPostgres(Map<String, String> cache) {
        saveToFile(TABLE_DDL_POSTGRES_FILE, cache);
    }

    public void saveFunctionBodyOracle(Map<String, String> cache) {
        saveToFile(FUNCTION_BODY_ORACLE_FILE, cache);
    }

    public void saveFunctionBodyPostgres(Map<String, String> cache) {
        saveToFile(FUNCTION_BODY_POSTGRES_FILE, cache);
    }

    public void saveViewDependencies(Map<String, Object> cache) {
        saveToFile(VIEW_DEPENDENCIES_FILE, cache);
    }

    public void saveCountOracle(Map<String, Long> cache) {
        saveToFile(COUNT_ORACLE_FILE, cache);
    }

    public void saveCountPostgres(Map<String, Long> cache) {
        saveToFile(COUNT_POSTGRES_FILE, cache);
    }

    public void saveBrokerExecProc(Map<String, String> cache) {
        saveToFile(BROKER_EXECPROC_FILE, cache);
    }

    public void saveReportsOracle(Map<String, Object> cache) {
        saveToFile(REPORTS_ORACLE_FILE, cache);
    }

    public void saveReportsPostgres(Map<String, Object> cache) {
        saveToFile(REPORTS_POSTGRES_FILE, cache);
    }

    public void savePrimaryKeyCache(Map<String, Object> cache) {
        saveToFile(PK_CACHE_FILE, cache);
    }

    public void saveNotNullCache(Map<String, Object> cache) {
        saveToFile(NOT_NULL_CACHE_FILE, cache);
    }

    public void savePostgresViewOid(Map<String, Integer> cache) {
        saveToFile(POSTGRES_VIEW_OID_FILE, cache);
    }

    public void savePostgresFunctionCheck(Map<String, Object> cache) {
        saveToFile(POSTGRES_FUNCTION_CHECK_FILE, cache);
    }

    /**
     * Сохранить все кэши
     */
    public void saveAllCaches(DatabaseCacheManager.CacheSnapshot snapshot) {
        try {
            init();
            saveViewDDLOracle(snapshot.getOracleViewDDL());
            saveViewDDLPostgres(snapshot.getPostgresViewDDL());
            saveTableDDLOracle(snapshot.getOracleTableDDL());
            saveTableDDLPostgres(snapshot.getPostgresTableDDL());
            saveFunctionBodyOracle(snapshot.getOracleFunctionBody());
            saveFunctionBodyPostgres(snapshot.getPostgresFunctionBody());
            saveViewDependencies(snapshot.getViewDependencies());
            saveCountOracle(snapshot.getOracleCount());
            saveCountPostgres(snapshot.getPostgresCount());
            saveBrokerExecProc(snapshot.getBrokerExecProc());
            saveReportsOracle(snapshot.getOracleReports());
            saveReportsPostgres(snapshot.getPostgresReports());
            savePrimaryKeyCache(snapshot.getPrimaryKeyCache());
            saveNotNullCache(snapshot.getNotNullCache());
            savePostgresViewOid(snapshot.getPostgresViewOid());
            savePostgresFunctionCheck(snapshot.getPostgresFunctionCheck());
            System.out.println("[DiskCache] Все кэши сохранены на диск");
        } catch (Exception e) {
            System.err.println("[DiskCache] Ошибка сохранения кэшей: " + e.getMessage());
        }
    }

    // ==================== ЗАГРУЗКА ====================

    @SuppressWarnings("unchecked")
    public Map<String, String> loadViewDDLOracle() {
        return loadFromFile(VIEW_DDL_ORACLE_FILE, new TypeToken<ConcurrentHashMap<String, String>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> loadViewDDLPostgres() {
        return loadFromFile(VIEW_DDL_POSTGRES_FILE, new TypeToken<ConcurrentHashMap<String, String>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> loadTableDDLOracle() {
        return loadFromFile(TABLE_DDL_ORACLE_FILE, new TypeToken<ConcurrentHashMap<String, String>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> loadTableDDLPostgres() {
        return loadFromFile(TABLE_DDL_POSTGRES_FILE, new TypeToken<ConcurrentHashMap<String, String>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> loadFunctionBodyOracle() {
        return loadFromFile(FUNCTION_BODY_ORACLE_FILE, new TypeToken<ConcurrentHashMap<String, String>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> loadFunctionBodyPostgres() {
        return loadFromFile(FUNCTION_BODY_POSTGRES_FILE, new TypeToken<ConcurrentHashMap<String, String>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> loadViewDependencies() {
        return loadFromFile(VIEW_DEPENDENCIES_FILE, new TypeToken<ConcurrentHashMap<String, Object>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Long> loadCountOracle() {
        return loadFromFile(COUNT_ORACLE_FILE, new TypeToken<ConcurrentHashMap<String, Long>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Long> loadCountPostgres() {
        return loadFromFile(COUNT_POSTGRES_FILE, new TypeToken<ConcurrentHashMap<String, Long>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> loadBrokerExecProc() {
        return loadFromFile(BROKER_EXECPROC_FILE, new TypeToken<ConcurrentHashMap<String, String>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> loadReportsOracle() {
        return loadFromFile(REPORTS_ORACLE_FILE, new TypeToken<ConcurrentHashMap<String, Object>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> loadReportsPostgres() {
        return loadFromFile(REPORTS_POSTGRES_FILE, new TypeToken<ConcurrentHashMap<String, Object>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> loadPrimaryKeyCache() {
        return loadFromFile(PK_CACHE_FILE, new TypeToken<ConcurrentHashMap<String, Object>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> loadNotNullCache() {
        return loadFromFile(NOT_NULL_CACHE_FILE, new TypeToken<ConcurrentHashMap<String, Object>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Integer> loadPostgresViewOid() {
        return loadFromFile(POSTGRES_VIEW_OID_FILE, new TypeToken<ConcurrentHashMap<String, Integer>>(){}.getType());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> loadPostgresFunctionCheck() {
        return loadFromFile(POSTGRES_FUNCTION_CHECK_FILE, new TypeToken<ConcurrentHashMap<String, Object>>(){}.getType());
    }

    /**
     * Загрузить все кэши
     */
    public DatabaseCacheManager.CacheSnapshot loadAllCaches() {
        DatabaseCacheManager.CacheSnapshot snapshot = new DatabaseCacheManager.CacheSnapshot();

        snapshot.setOracleViewDDL(loadViewDDLOracle());
        snapshot.setPostgresViewDDL(loadViewDDLPostgres());
        snapshot.setOracleTableDDL(loadTableDDLOracle());
        snapshot.setPostgresTableDDL(loadTableDDLPostgres());
        snapshot.setOracleFunctionBody(loadFunctionBodyOracle());
        snapshot.setPostgresFunctionBody(loadFunctionBodyPostgres());
        snapshot.setViewDependencies(loadViewDependencies());
        snapshot.setOracleCount(loadCountOracle());
        snapshot.setPostgresCount(loadCountPostgres());
        snapshot.setBrokerExecProc(loadBrokerExecProc());
        snapshot.setOracleReports(loadReportsOracle());
        snapshot.setPostgresReports(loadReportsPostgres());
        snapshot.setPrimaryKeyCache(loadPrimaryKeyCache());
        snapshot.setNotNullCache(loadNotNullCache());
        snapshot.setPostgresViewOid(loadPostgresViewOid());
        snapshot.setPostgresFunctionCheck(loadPostgresFunctionCheck());

        return snapshot;
    }

    /**
     * Проверяет, есть ли сохранённый кэш
     */
    public boolean hasCache() {
        if (cachePath == null) return false;
        return Files.exists(cachePath.resolve(VIEW_DDL_ORACLE_FILE));
    }

    // ==================== ПРИВАТНЫЕ МЕТОДЫ ====================

    private void saveToFile(String fileName, Object data) {
        if (cachePath == null) return;
        try {
            Path filePath = cachePath.resolve(fileName);
            String json = gson.toJson(data);
            Files.writeString(filePath, json);
        } catch (IOException e) {
            System.err.println("[DiskCache] Ошибка сохранения " + fileName + ": " + e.getMessage());
        }
    }

    private <T> T loadFromFile(String fileName, java.lang.reflect.Type type) {
        if (cachePath == null) return null;
        try {
            Path filePath = cachePath.resolve(fileName);
            if (Files.exists(filePath)) {
                String json = Files.readString(filePath);
                return gson.fromJson(json, type);
            }
        } catch (IOException e) {
            System.err.println("[DiskCache] Ошибка загрузки " + fileName + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Очистить директорию кэша
     */
    public void clearCache() throws IOException {
        if (cachePath != null && Files.exists(cachePath)) {
            try (Stream<Path> walk = Files.walk(cachePath)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
            System.out.println("[DiskCache] Директория кэша очищена: " + cachePath);
        }
    }
}