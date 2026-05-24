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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Централизованное управление кэшами для БД объектов
 */
public class DatabaseCacheManager {

    private static DiskCacheManager diskCacheManager = new DiskCacheManager();
    // Изменяем типы кэшей на Object для универсальности
    private static final Map<String, Object> viewDependenciesCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> oracleReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> postgresReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> pkCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> notNullCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> postgresFunctionCheckCache = new ConcurrentHashMap<>();

    private static ScheduledExecutorService scheduler;
    private static final AtomicBoolean saveScheduled = new AtomicBoolean(false);
    private static final AtomicInteger pendingChanges = new AtomicInteger(0);
    private static volatile long lastSaveTime = System.currentTimeMillis();
    private static final int SAVE_DELAY_SECONDS = 5;      // Задержка перед сохранением (сек)
    private static final int MAX_PENDING_CHANGES = 50;     // Максимум изменений до принудительного сохранения
    private static final int AUTO_SAVE_INTERVAL_MINUTES = 5;

    // ==================== СУЩЕСТВУЮЩИЕ КЭШИ ====================
    private static final Map<String, String> oracleViewDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> postgresViewDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> oracleTableDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> postgresTableDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> oracleFunctionBodyCache = new ConcurrentHashMap<>();
    private static final Map<String, String> postgresFunctionBodyCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> oracleCountCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> postgresCountCache = new ConcurrentHashMap<>();

    // ==================== НОВЫЕ КЭШИ ====================
    private static final Map<String, String> brokerExecProcCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DbReportInfo>> oracleCompositeReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DbReportInfo>> postgresCompositeReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, Integer> postgresViewOidCache = new ConcurrentHashMap<>();

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

    // ==================== ИНИЦИАЛИЗАЦИЯ И ПРОВЕРКИ ====================

    /**
     * Инициализация конфигурации БД (вызывается при старте и при сохранении настроек)
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

        // Сбрасываем статусы при новой конфигурации
        oracleChecked = false;
        postgresChecked = false;
        oracleAvailable = true;
        postgresAvailable = true;

        // Выполняем проверку
        checkConnections();
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
            return; // Уже проверяли в этой сессии
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
            return; // Уже проверяли в этой сессии
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

    public static List<DbReportInfo> getOracleCompositeReports(int parentId, Supplier<List<DbReportInfo>> loader) {
        if (!isOracleAvailable()) {
            return Collections.emptyList();
        }
        String key = "COMPOSITE_ORACLE_" + parentId;
        return oracleCompositeReportsCache.computeIfAbsent(key, k -> loader.get());
    }
    public static List<DbReportInfo> getPostgresCompositeReports(int parentId, Supplier<List<DbReportInfo>> loader) {
        if (!isPostgresAvailable()) {
            return Collections.emptyList();
        }
        String key = "COMPOSITE_POSTGRES_" + parentId;
        return postgresCompositeReportsCache.computeIfAbsent(key, k -> loader.get());
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
        System.out.println("Статус Oracle: " + (oracleAvailable ? "ДОСТУПНА" : "НЕДОСТУПНА"));
        System.out.println("Статус PostgreSQL: " + (postgresAvailable ? "ДОСТУПНА" : "НЕДОСТУПНА"));
    }

    /**
     * Снимок всех кэшей для сохранения на диск
     */
    public static class CacheSnapshot {
        private Map<String, String> oracleViewDDL = new ConcurrentHashMap<>();
        private Map<String, String> postgresViewDDL = new ConcurrentHashMap<>();
        private Map<String, String> oracleTableDDL = new ConcurrentHashMap<>();
        private Map<String, String> postgresTableDDL = new ConcurrentHashMap<>();
        private Map<String, String> oracleFunctionBody = new ConcurrentHashMap<>();
        private Map<String, String> postgresFunctionBody = new ConcurrentHashMap<>();
        private Map<String, Object> viewDependencies = new ConcurrentHashMap<>();
        private Map<String, Long> oracleCount = new ConcurrentHashMap<>();
        private Map<String, Long> postgresCount = new ConcurrentHashMap<>();
        private Map<String, String> brokerExecProc = new ConcurrentHashMap<>();
        private Map<String, Object> oracleReports = new ConcurrentHashMap<>();
        private Map<String, Object> postgresReports = new ConcurrentHashMap<>();
        private Map<String, Object> primaryKeyCache = new ConcurrentHashMap<>();
        private Map<String, Object> notNullCache = new ConcurrentHashMap<>();
        private Map<String, Integer> postgresViewOid = new ConcurrentHashMap<>();
        private Map<String, Object> postgresFunctionCheck = new ConcurrentHashMap<>();

        // Геттеры
        public Map<String, String> getOracleViewDDL() { return oracleViewDDL; }
        public Map<String, String> getPostgresViewDDL() { return postgresViewDDL; }
        public Map<String, String> getOracleTableDDL() { return oracleTableDDL; }
        public Map<String, String> getPostgresTableDDL() { return postgresTableDDL; }
        public Map<String, String> getOracleFunctionBody() { return oracleFunctionBody; }
        public Map<String, String> getPostgresFunctionBody() { return postgresFunctionBody; }
        public Map<String, Object> getViewDependencies() { return viewDependencies; }
        public Map<String, Long> getOracleCount() { return oracleCount; }
        public Map<String, Long> getPostgresCount() { return postgresCount; }
        public Map<String, String> getBrokerExecProc() { return brokerExecProc; }
        public Map<String, Object> getOracleReports() { return oracleReports; }
        public Map<String, Object> getPostgresReports() { return postgresReports; }
        public Map<String, Object> getPrimaryKeyCache() { return primaryKeyCache; }
        public Map<String, Object> getNotNullCache() { return notNullCache; }
        public Map<String, Integer> getPostgresViewOid() { return postgresViewOid; }
        public Map<String, Object> getPostgresFunctionCheck() { return postgresFunctionCheck; }

        // Сеттеры
        public void setOracleViewDDL(Map<String, String> map) {
            if (map != null) this.oracleViewDDL = new ConcurrentHashMap<>(map);
        }
        public void setPostgresViewDDL(Map<String, String> map) {
            if (map != null) this.postgresViewDDL = new ConcurrentHashMap<>(map);
        }
        public void setOracleTableDDL(Map<String, String> map) {
            if (map != null) this.oracleTableDDL = new ConcurrentHashMap<>(map);
        }
        public void setPostgresTableDDL(Map<String, String> map) {
            if (map != null) this.postgresTableDDL = new ConcurrentHashMap<>(map);
        }
        public void setOracleFunctionBody(Map<String, String> map) {
            if (map != null) this.oracleFunctionBody = new ConcurrentHashMap<>(map);
        }
        public void setPostgresFunctionBody(Map<String, String> map) {
            if (map != null) this.postgresFunctionBody = new ConcurrentHashMap<>(map);
        }
        public void setViewDependencies(Map<String, Object> map) {
            if (map != null) this.viewDependencies = new ConcurrentHashMap<>(map);
        }
        public void setOracleCount(Map<String, Long> map) {
            if (map != null) this.oracleCount = new ConcurrentHashMap<>(map);
        }
        public void setPostgresCount(Map<String, Long> map) {
            if (map != null) this.postgresCount = new ConcurrentHashMap<>(map);
        }
        public void setBrokerExecProc(Map<String, String> map) {
            if (map != null) this.brokerExecProc = new ConcurrentHashMap<>(map);
        }
        public void setOracleReports(Map<String, Object> map) {
            if (map != null) this.oracleReports = new ConcurrentHashMap<>(map);
        }
        public void setPostgresReports(Map<String, Object> map) {
            if (map != null) this.postgresReports = new ConcurrentHashMap<>(map);
        }
        public void setPrimaryKeyCache(Map<String, Object> map) {
            if (map != null) this.primaryKeyCache = new ConcurrentHashMap<>(map);
        }
        public void setNotNullCache(Map<String, Object> map) {
            if (map != null) this.notNullCache = new ConcurrentHashMap<>(map);
        }
        public void setPostgresViewOid(Map<String, Integer> map) {
            if (map != null) this.postgresViewOid = new ConcurrentHashMap<>(map);
        }
        public void setPostgresFunctionCheck(Map<String, Object> map) {
            if (map != null) this.postgresFunctionCheck = new ConcurrentHashMap<>(map);
        }
    }



    /**
     * Сохранить кэши на диск
     */
    public static void saveToDisk() {
        try {
            CacheSnapshot snapshot = new CacheSnapshot();
            snapshot.setOracleViewDDL(new ConcurrentHashMap<>(oracleViewDDLCache));
            snapshot.setPostgresViewDDL(new ConcurrentHashMap<>(postgresViewDDLCache));
            snapshot.setOracleTableDDL(new ConcurrentHashMap<>(oracleTableDDLCache));
            snapshot.setPostgresTableDDL(new ConcurrentHashMap<>(postgresTableDDLCache));
            snapshot.setOracleFunctionBody(new ConcurrentHashMap<>(oracleFunctionBodyCache));
            snapshot.setPostgresFunctionBody(new ConcurrentHashMap<>(postgresFunctionBodyCache));
            snapshot.setViewDependencies(new ConcurrentHashMap<>(viewDependenciesCache));
            snapshot.setOracleCount(new ConcurrentHashMap<>(oracleCountCache));
            snapshot.setPostgresCount(new ConcurrentHashMap<>(postgresCountCache));
            snapshot.setBrokerExecProc(new ConcurrentHashMap<>(brokerExecProcCache));
            snapshot.setOracleReports(new ConcurrentHashMap<>(oracleReportsCache));
            snapshot.setPostgresReports(new ConcurrentHashMap<>(postgresReportsCache));
            snapshot.setPrimaryKeyCache(new ConcurrentHashMap<>(pkCache));
            snapshot.setNotNullCache(new ConcurrentHashMap<>(notNullCache));
            snapshot.setPostgresViewOid(new ConcurrentHashMap<>(postgresViewOidCache));
            snapshot.setPostgresFunctionCheck(new ConcurrentHashMap<>(postgresFunctionCheckCache));

            diskCacheManager.saveAllCaches(snapshot);
        } catch (Exception e) {
            System.err.println("[DiskCache] Ошибка сохранения кэшей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Очистка кэшей
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
    /**
     * Установить директорию для кэша (вызывается при инициализации)
     * @param outputDir каталог отчётов
     */
    public static void setCacheOutputDir(String outputDir) {
        diskCacheManager.setOutputDir(outputDir);
        System.out.println("[DatabaseCacheManager] Установлена директория кэша: " + outputDir + "/DatabaseCache");
    }

    /**
     * Загрузить кэши с диска
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void loadFromDisk() {
        try {
            diskCacheManager.init();
            if (!diskCacheManager.hasCache()) {
                System.out.println("[DiskCache] Сохранённый кэш не найден, будет выполнен загрузка из БД");
                return;
            }

            CacheSnapshot snapshot = diskCacheManager.loadAllCaches();

            if (snapshot.getOracleViewDDL() != null) oracleViewDDLCache.putAll(snapshot.getOracleViewDDL());
            if (snapshot.getPostgresViewDDL() != null) postgresViewDDLCache.putAll(snapshot.getPostgresViewDDL());
            if (snapshot.getOracleTableDDL() != null) oracleTableDDLCache.putAll(snapshot.getOracleTableDDL());
            if (snapshot.getPostgresTableDDL() != null) postgresTableDDLCache.putAll(snapshot.getPostgresTableDDL());
            if (snapshot.getOracleFunctionBody() != null) oracleFunctionBodyCache.putAll(snapshot.getOracleFunctionBody());
            if (snapshot.getPostgresFunctionBody() != null) postgresFunctionBodyCache.putAll(snapshot.getPostgresFunctionBody());
            if (snapshot.getViewDependencies() != null) viewDependenciesCache.putAll(snapshot.getViewDependencies());
            if (snapshot.getOracleCount() != null) oracleCountCache.putAll(snapshot.getOracleCount());
            if (snapshot.getPostgresCount() != null) postgresCountCache.putAll(snapshot.getPostgresCount());
            if (snapshot.getBrokerExecProc() != null) brokerExecProcCache.putAll(snapshot.getBrokerExecProc());
            if (snapshot.getOracleReports() != null) oracleReportsCache.putAll(snapshot.getOracleReports());
            if (snapshot.getPostgresReports() != null) postgresReportsCache.putAll(snapshot.getPostgresReports());
            if (snapshot.getPrimaryKeyCache() != null) pkCache.putAll(snapshot.getPrimaryKeyCache());
            if (snapshot.getNotNullCache() != null) notNullCache.putAll(snapshot.getNotNullCache());
            if (snapshot.getPostgresViewOid() != null) postgresViewOidCache.putAll(snapshot.getPostgresViewOid());
            if (snapshot.getPostgresFunctionCheck() != null) postgresFunctionCheckCache.putAll(snapshot.getPostgresFunctionCheck());

            System.out.println("[DiskCache] Кэши загружены с диска");
            printStats();
        } catch (Exception e) {
            System.err.println("[DiskCache] Ошибка загрузки кэшей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Инициализация автоматического сохранения кэша
     */
    public static void initAutoSave() {
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "CacheAutoSave");
                t.setDaemon(true);
                return t;
            });

            // Периодическое автосохранение
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    if (System.currentTimeMillis() - lastSaveTime > AUTO_SAVE_INTERVAL_MINUTES * 60 * 1000) {
                        System.out.println("[Cache] Выполнение периодического автосохранения...");
                        forceSaveToDisk();
                    }
                } catch (Exception e) {
                    System.err.println("[Cache] Ошибка периодического сохранения: " + e.getMessage());
                }
            }, AUTO_SAVE_INTERVAL_MINUTES, AUTO_SAVE_INTERVAL_MINUTES, TimeUnit.MINUTES);

            System.out.println("[Cache] Автоматическое сохранение кэша запущено");
        }
    }
    /**
     * Остановка автоматического сохранения (при завершении программы)
     */
    public static void shutdownAutoSave() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("[Cache] Автоматическое сохранение кэша остановлено");
        }
    }

    /**
     * Отметить изменение в кэше и запланировать сохранение
     */
    private static void markChanged() {
        int changes = pendingChanges.incrementAndGet();
        lastSaveTime = System.currentTimeMillis();

        // Принудительное сохранение при большом количестве изменений
        if (changes >= MAX_PENDING_CHANGES) {
            if (saveScheduled.compareAndSet(false, true)) {
                scheduler.execute(() -> {
                    try {
                        forceSaveToDisk();
                    } finally {
                        saveScheduled.set(false);
                    }
                });
            }
        } else if (!saveScheduled.getAndSet(true)) {
            // Отложенное сохранение
            scheduler.schedule(() -> {
                try {
                    forceSaveToDisk();
                } finally {
                    saveScheduled.set(false);
                }
            }, SAVE_DELAY_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * Принудительное сохранение кэша на диск
     */
    public static void forceSaveToDisk() {
        try {
            int changes = pendingChanges.getAndSet(0);
            if (changes > 0 || System.currentTimeMillis() - lastSaveTime > AUTO_SAVE_INTERVAL_MINUTES * 60 * 1000) {
                saveToDisk();
                System.out.println("[Cache] Кэш сохранён на диск (изменений: " + changes + ")");
                lastSaveTime = System.currentTimeMillis();
            }
        } catch (Exception e) {
            System.err.println("[Cache] Ошибка принудительного сохранения: " + e.getMessage());
            pendingChanges.set(0);
        }
    }

    // ==================== МОДИФИЦИРОВАННЫЕ МЕТОДЫ ЗАПИСИ ====================

    // Вместо прямого put, используем обёртки с пометкой изменений

    public static void putOracleViewDDL(String key, String value) {
        oracleViewDDLCache.put(key, value);
        markChanged();
    }

    public static void putPostgresViewDDL(String key, String value) {
        postgresViewDDLCache.put(key, value);
        markChanged();
    }

    public static void putOracleTableDDL(String key, String value) {
        oracleTableDDLCache.put(key, value);
        markChanged();
    }

    public static void putPostgresTableDDL(String key, String value) {
        postgresTableDDLCache.put(key, value);
        markChanged();
    }

    public static void putOracleFunctionBody(String key, String value) {
        oracleFunctionBodyCache.put(key, value);
        markChanged();
    }

    public static void putPostgresFunctionBody(String key, String value) {
        postgresFunctionBodyCache.put(key, value);
        markChanged();
    }

    public static void putViewDependency(String key, Object value) {
        viewDependenciesCache.put(key, value);
        markChanged();
    }

    public static void putOracleCount(String key, Long value) {
        oracleCountCache.put(key, value);
        markChanged();
    }

    public static void putPostgresCount(String key, Long value) {
        postgresCountCache.put(key, value);
        markChanged();
    }

    public static void putBrokerExecProc(String key, String value) {
        brokerExecProcCache.put(key, value);
        markChanged();
    }

    public static void putOracleReport(String key, Object value) {
        oracleReportsCache.put(key, value);
        markChanged();
    }

    public static void putPostgresReport(String key, Object value) {
        postgresReportsCache.put(key, value);
        markChanged();
    }

    public static void putPrimaryKey(String key, Object value) {
        pkCache.put(key, value);
        markChanged();
    }

    public static void putNotNullConstraint(String key, Object value) {
        notNullCache.put(key, value);
        markChanged();
    }

    public static void putPostgresViewOid(String key, Integer value) {
        postgresViewOidCache.put(key, value);
        markChanged();
    }

    public static void putPostgresFunctionCheck(String key, Object value) {
        postgresFunctionCheckCache.put(key, value);
        markChanged();
    }

    // ==================== МОДИФИЦИРОВАННЫЕ МЕТОДЫ ПОЛУЧЕНИЯ С ЗАГРУЗКОЙ ====================

    @SuppressWarnings("unchecked")
    public static ViewTableDependencies getViewDependencies(String viewName, Supplier<ViewTableDependencies> loader) {
        if (!isOracleAvailable()) {
            return null;
        }
        String key = viewName.toUpperCase();

        // Проверяем наличие в кэше
        if (viewDependenciesCache.containsKey(key)) {
            System.out.println("[КЭШ] Вьюха " + viewName + " взята из локального кэша");
            return (ViewTableDependencies) viewDependenciesCache.get(key);
        }

        System.out.println("[КЭШ] Вьюха " + viewName + " НЕ найдена в кэше, загружаем из БД");
        ViewTableDependencies value = loader.get();
        if (value != null) {
            viewDependenciesCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] Вьюха " + viewName + " сохранена в локальный кэш");
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static List<DbReportInfo> getOracleReports(String unitCode, Supplier<List<DbReportInfo>> loader) {
        if (!isOracleAvailable()) {
            return Collections.emptyList();
        }
        String key = unitCode.toUpperCase();
        return (List<DbReportInfo>) oracleReportsCache.computeIfAbsent(key, k -> {
            List<DbReportInfo> value = loader.get();
            markChanged();
            return value;
        });
    }

    @SuppressWarnings("unchecked")
    public static List<DbReportInfo> getPostgresReports(String unitCode, Supplier<List<DbReportInfo>> loader) {
        if (!isPostgresAvailable()) {
            return Collections.emptyList();
        }
        String key = unitCode.toUpperCase();
        return (List<DbReportInfo>) postgresReportsCache.computeIfAbsent(key, k -> {
            List<DbReportInfo> value = loader.get();
            markChanged();
            return value;
        });
    }

    @SuppressWarnings("unchecked")
    public static DatabaseObjectChecker.PrimaryKeyInfo getPrimaryKeyInfo(String tableName, Supplier<DatabaseObjectChecker.PrimaryKeyInfo> loader) {
        if (!isOracleAvailable() || !isPostgresAvailable()) {
            return null;
        }
        String key = tableName.toUpperCase();
        return (DatabaseObjectChecker.PrimaryKeyInfo) pkCache.computeIfAbsent(key, k -> {
            DatabaseObjectChecker.PrimaryKeyInfo value = loader.get();
            markChanged();
            return value;
        });
    }

    @SuppressWarnings("unchecked")
    public static List<DatabaseObjectChecker.NotNullConstraintInfo> getNotNullConstraints(String tableName, Supplier<List<DatabaseObjectChecker.NotNullConstraintInfo>> loader) {
        if (!isOracleAvailable() || !isPostgresAvailable()) {
            return Collections.emptyList();
        }
        String key = tableName.toUpperCase();
        return (List<DatabaseObjectChecker.NotNullConstraintInfo>) notNullCache.computeIfAbsent(key, k -> {
            List<DatabaseObjectChecker.NotNullConstraintInfo> value = loader.get();
            markChanged();
            return value;
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> T getPostgresFunctionCheck(String functionName, Supplier<T> loader) {
        if (!isPostgresAvailable()) {
            return null;
        }
        String key = functionName.toLowerCase();
        return (T) postgresFunctionCheckCache.computeIfAbsent(key, k -> {
            T value = loader.get();
            markChanged();
            return value;
        });
    }

    // Для простых кэшей (String -> String, Long, Integer)
    public static String getOracleViewDDL(String viewName, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = viewName.toUpperCase();
        return oracleViewDDLCache.computeIfAbsent(key, k -> {
            String value = loader.get();
            markChanged();
            return value;
        });
    }

    public static String getPostgresViewDDL(String viewName, Supplier<String> loader) {
        if (!isPostgresAvailable()) return null;
        String key = viewName.toLowerCase();
        return postgresViewDDLCache.computeIfAbsent(key, k -> {
            String value = loader.get();
            markChanged();
            return value;
        });
    }

    public static String getOracleTableDDL(String tableName, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = tableName.toUpperCase();
        return oracleTableDDLCache.computeIfAbsent(key, k -> {
            String value = loader.get();
            markChanged();
            return value;
        });
    }

    public static String getPostgresTableDDL(String tableName, Supplier<String> loader) {
        if (!isPostgresAvailable()) return null;
        String key = tableName.toLowerCase();
        return postgresTableDDLCache.computeIfAbsent(key, k -> {
            String value = loader.get();
            markChanged();
            return value;
        });
    }

    public static String getOracleFunctionBody(String functionKey, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = functionKey.toUpperCase();
        return oracleFunctionBodyCache.computeIfAbsent(key, k -> {
            String value = loader.get();
            markChanged();
            return value;
        });
    }

    public static String getPostgresFunctionBody(String functionKey, Supplier<String> loader) {
        if (!isPostgresAvailable()) return null;
        String key = functionKey.toLowerCase();
        return postgresFunctionBodyCache.computeIfAbsent(key, k -> {
            String value = loader.get();
            markChanged();
            return value;
        });
    }

    public static Long getOracleCount(String objectName, Supplier<Long> loader) {
        if (!isOracleAvailable()) return -1L;
        String key = objectName.toUpperCase();
        return oracleCountCache.computeIfAbsent(key, k -> {
            Long value = loader.get();
            markChanged();
            return value;
        });
    }

    public static Long getPostgresCount(String objectName, Supplier<Long> loader) {
        if (!isPostgresAvailable()) return -1L;
        String key = objectName.toLowerCase();
        return postgresCountCache.computeIfAbsent(key, k -> {
            Long value = loader.get();
            markChanged();
            return value;
        });
    }

    public static String getBrokerExecProc(String unit, String action, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = (unit + "_" + action).toUpperCase();
        return brokerExecProcCache.computeIfAbsent(key, k -> {
            String value = loader.get();
            markChanged();
            return value;
        });
    }

    public static int getPostgresViewOid(String viewName, Supplier<Integer> loader) {
        if (!isPostgresAvailable()) return -1;
        String key = viewName.toLowerCase();
        return postgresViewOidCache.computeIfAbsent(key, k -> {
            Integer value = loader.get();
            markChanged();
            return value;
        });
    }
}