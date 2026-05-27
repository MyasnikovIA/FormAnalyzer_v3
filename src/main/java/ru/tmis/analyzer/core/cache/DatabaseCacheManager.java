// core/cache/DatabaseCacheManager.java
package ru.tmis.analyzer.core.cache;

import ru.tmis.analyzer.core.db.DatabaseObjectChecker;
import ru.tmis.analyzer.core.model.DbReportInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;
import ru.tmis.analyzer.utils.NetworkUtils;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Централизованное управление кэшами для БД объектов
 */
public class DatabaseCacheManager {
    static {
        // Статическая инициализация драйверов
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Oracle JDBC driver not found in static init: " + e.getMessage());
        }
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] PostgreSQL JDBC driver not found in static init: " + e.getMessage());
        }
    }

    private static DiskCacheManager diskCacheManager = new DiskCacheManager();

    // ==================== ОСНОВНЫЕ КЭШИ ====================
    private static final Map<String, String> oracleViewDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> postgresViewDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> oracleTableDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> postgresTableDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> oracleFunctionBodyCache = new ConcurrentHashMap<>();
    private static final Map<String, String> postgresFunctionBodyCache = new ConcurrentHashMap<>();
    private static final Map<String, String> oraclePackageSpecCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> oracleCountCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> postgresCountCache = new ConcurrentHashMap<>();
    private static final Map<String, String> brokerExecProcCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> oracleReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> postgresReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> viewDependenciesCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> pkCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> notNullCache = new ConcurrentHashMap<>();
    private static final Map<String, Object> postgresFunctionCheckCache = new ConcurrentHashMap<>();
    private static final Map<String, Integer> postgresViewOidCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DbReportInfo>> oracleCompositeReportsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<DbReportInfo>> postgresCompositeReportsCache = new ConcurrentHashMap<>();

    // ==================== ДОПОЛНИТЕЛЬНЫЕ КЭШИ ====================
    private static final Map<String, String> constantsCache = new ConcurrentHashMap<>();
    private static final Map<String, String> systemOptionsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<ColumnInfo>> tableColumnsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<ForeignKeyInfo>> foreignKeysCache = new ConcurrentHashMap<>();
    private static final Map<String, List<IndexInfo>> indexesCache = new ConcurrentHashMap<>();
    private static final Map<String, SequenceInfo> sequencesCache = new ConcurrentHashMap<>();
    private static final Map<String, String> synonymsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<TriggerInfo>> triggersCache = new ConcurrentHashMap<>();
    private static final Map<String, PartitionInfo> partitionsCache = new ConcurrentHashMap<>();
    private static final Map<String, TableStatistics> tableStatisticsCache = new ConcurrentHashMap<>();
    private static final Map<String, String> userTypesCache = new ConcurrentHashMap<>();
    private static final Map<String, String> materializedViewsCache = new ConcurrentHashMap<>();
    private static final Map<String, String> standaloneProceduresCache = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> privilegesCache = new ConcurrentHashMap<>();

    // ==================== КЭШ ОТСУТСТВУЮЩИХ ОБЪЕКТОВ ====================
    private static final Map<String, Boolean> missingObjectsCache = new ConcurrentHashMap<>();

    // ==================== СТАТУСЫ ПОДКЛЮЧЕНИЯ ====================
    private static volatile boolean oracleAvailable = true;
    private static volatile boolean postgresAvailable = true;
    private static volatile boolean oracleChecked = false;
    private static volatile boolean postgresChecked = false;

    // ==================== КОНФИГУРАЦИЯ БД ====================
    private static volatile String cachedOracleUrl;
    private static volatile String cachedOracleUser;
    private static volatile String cachedOraclePassword;
    private static volatile String cachedPostgresUrl;
    private static volatile String cachedPostgresUser;
    private static volatile String cachedPostgresPassword;

    // ==================== АВТОСОХРАНЕНИЕ ====================
    private static ScheduledExecutorService scheduler;
    private static final AtomicBoolean saveScheduled = new AtomicBoolean(false);
    private static final AtomicInteger pendingChanges = new AtomicInteger(0);
    private static volatile long lastSaveTime = System.currentTimeMillis();
    private static final int SAVE_DELAY_SECONDS = 5;
    private static final int MAX_PENDING_CHANGES = 50;
    private static final int AUTO_SAVE_INTERVAL_MINUTES = 5;
    private static volatile boolean isSchedulerShutdown = false;

    // ==================== ИНИЦИАЛИЗАЦИЯ ====================

    public static void initDbConfig(String oracleUrl, String oracleUser, String oraclePassword,
                                    String postgresUrl, String postgresUser, String postgresPassword,
                                    String postgresMisUser) {
        // Загружаем драйверы ПЕРЕД использованием
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            System.out.println("[DB] Oracle JDBC driver loaded");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Oracle JDBC driver not found: " + e.getMessage());
        }

        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("[DB] PostgreSQL JDBC driver loaded");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] PostgreSQL JDBC driver not found: " + e.getMessage());
        }

        cachedOracleUrl = oracleUrl;
        cachedOracleUser = oracleUser;
        cachedOraclePassword = oraclePassword;
        cachedPostgresUrl = postgresUrl;
        cachedPostgresUser = postgresUser;
        cachedPostgresPassword = postgresPassword;

        oracleChecked = false;
        postgresChecked = false;
        oracleAvailable = true;
        postgresAvailable = true;

        checkConnections();
    }

    public static void checkConnections() {
        checkOracleConnection();
        checkPostgresConnection();

        System.out.println("[DB] Статус подключений:");
        System.out.println("  Oracle: " + (oracleAvailable ? "ДОСТУПНА" : "НЕДОСТУПНА"));
        System.out.println("  PostgreSQL: " + (postgresAvailable ? "ДОСТУПНА" : "НЕДОСТУПНА"));
    }

    public static void resetConnectionStatus() {
        oracleChecked = false;
        postgresChecked = false;
        oracleAvailable = true;
        postgresAvailable = true;
    }

    public static boolean isOracleAvailable() { return oracleAvailable; }
    public static boolean isPostgresAvailable() { return postgresAvailable; }

    // ==================== ПРОВЕРКА ПОДКЛЮЧЕНИЙ ====================

    private static void checkOracleConnection() {
        if (oracleChecked) return;
        if (cachedOracleUrl == null || cachedOracleUrl.isEmpty()) {
            oracleAvailable = false;
            oracleChecked = true;
            System.err.println("[DB] Oracle URL не настроен");
            return;
        }
        if (!isOracleServerAvailable()) {
            oracleAvailable = false;
            oracleChecked = true;
            System.err.println("[DB] Oracle сервер недоступен");
            return;
        }
        try (Connection conn = DriverManager.getConnection(cachedOracleUrl, cachedOracleUser, cachedOraclePassword)) {
            oracleAvailable = conn.isValid(5);
        } catch (SQLException e) {
            oracleAvailable = false;
            System.err.println("[DB] Oracle недоступен: " + e.getMessage());
        }
        oracleChecked = true;
    }

    private static void checkPostgresConnection() {
        if (postgresChecked) return;
        if (cachedPostgresUrl == null || cachedPostgresUrl.isEmpty()) {
            postgresAvailable = false;
            postgresChecked = true;
            System.err.println("[DB] PostgreSQL URL не настроен");
            return;
        }
        if (!isPostgresServerAvailable()) {
            postgresAvailable = false;
            postgresChecked = true;
            System.err.println("[DB] PostgreSQL сервер недоступен");
            return;
        }
        try (Connection conn = DriverManager.getConnection(cachedPostgresUrl, cachedPostgresUser, cachedPostgresPassword)) {
            postgresAvailable = conn.isValid(5);
        } catch (SQLException e) {
            postgresAvailable = false;
            System.err.println("[DB] PostgreSQL недоступен: " + e.getMessage());
        }
        postgresChecked = true;
    }

    public static boolean isOracleServerAvailable() {
        if (cachedOracleUrl == null || cachedOracleUrl.isEmpty()) return false;
        return NetworkUtils.isDatabaseServerAvailableWithCache(cachedOracleUrl);
    }

    public static boolean isPostgresServerAvailable() {
        if (cachedPostgresUrl == null || cachedPostgresUrl.isEmpty()) return false;
        return NetworkUtils.isDatabaseServerAvailableWithCache(cachedPostgresUrl);
    }

    // ==================== АВТОСОХРАНЕНИЕ ====================

    public static void initAutoSave() {
        if (scheduler == null || scheduler.isShutdown()) {
            isSchedulerShutdown = false;
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "CacheAutoSave");
                t.setDaemon(true);
                return t;
            });

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    if (!isSchedulerShutdown && System.currentTimeMillis() - lastSaveTime > AUTO_SAVE_INTERVAL_MINUTES * 60 * 1000) {
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

    public static void shutdownAutoSave() {
        isSchedulerShutdown = true;
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

    private static void markChanged() {
        if (isSchedulerShutdown || scheduler == null || scheduler.isShutdown()) {
            return;
        }
        int changes = pendingChanges.incrementAndGet();
        lastSaveTime = System.currentTimeMillis();

        if (changes >= MAX_PENDING_CHANGES) {
            if (saveScheduled.compareAndSet(false, true)) {
                try {
                    scheduler.execute(() -> {
                        try {
                            forceSaveToDisk();
                        } finally {
                            saveScheduled.set(false);
                        }
                    });
                } catch (RejectedExecutionException e) {
                    System.err.println("[Cache] Задача отклонена (scheduler остановлен): " + e.getMessage());
                    saveScheduled.set(false);
                }
            }
        } else if (!saveScheduled.getAndSet(true)) {
            try {
                scheduler.schedule(() -> {
                    try {
                        forceSaveToDisk();
                    } finally {
                        saveScheduled.set(false);
                    }
                }, SAVE_DELAY_SECONDS, TimeUnit.SECONDS);
            } catch (RejectedExecutionException e) {
                System.err.println("[Cache] Задача отклонена (scheduler остановлен): " + e.getMessage());
                saveScheduled.set(false);
            }
        }
    }

    public static void forceSaveToDisk() {
        if (isSchedulerShutdown) {
            try {
                saveToDisk();
                System.out.println("[Cache] Кэш сохранён на диск (синхронно)");
            } catch (Exception e) {
                System.err.println("[Cache] Ошибка синхронного сохранения: " + e.getMessage());
            }
            return;
        }
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

    public static void saveToDisk() {
        try {
            CacheSnapshot snapshot = new CacheSnapshot();
            snapshot.setOracleViewDDL(new ConcurrentHashMap<>(oracleViewDDLCache));
            snapshot.setPostgresViewDDL(new ConcurrentHashMap<>(postgresViewDDLCache));
            snapshot.setOracleTableDDL(new ConcurrentHashMap<>(oracleTableDDLCache));
            snapshot.setPostgresTableDDL(new ConcurrentHashMap<>(postgresTableDDLCache));
            snapshot.setOracleFunctionBody(new ConcurrentHashMap<>(oracleFunctionBodyCache));
            snapshot.setPostgresFunctionBody(new ConcurrentHashMap<>(postgresFunctionBodyCache));
            snapshot.setOraclePackageSpec(new ConcurrentHashMap<>(oraclePackageSpecCache));
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
            snapshot.setConstantsCache(new ConcurrentHashMap<>(constantsCache));
            snapshot.setSystemOptionsCache(new ConcurrentHashMap<>(systemOptionsCache));

            diskCacheManager.saveAllCaches(snapshot);
        } catch (Exception e) {
            System.err.println("[DiskCache] Ошибка сохранения кэшей: " + e.getMessage());
        }
    }

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
            if (snapshot.getOraclePackageSpec() != null) oraclePackageSpecCache.putAll(snapshot.getOraclePackageSpec());
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
            if (snapshot.getConstantsCache() != null) constantsCache.putAll(snapshot.getConstantsCache());
            if (snapshot.getSystemOptionsCache() != null) systemOptionsCache.putAll(snapshot.getSystemOptionsCache());

            System.out.println("[DiskCache] Кэши загружены с диска");
            printStats();
        } catch (Exception e) {
            System.err.println("[DiskCache] Ошибка загрузки кэшей: " + e.getMessage());
        }
    }

    public static void clearAll() {
        oracleViewDDLCache.clear();
        postgresViewDDLCache.clear();
        oracleTableDDLCache.clear();
        postgresTableDDLCache.clear();
        oracleFunctionBodyCache.clear();
        postgresFunctionBodyCache.clear();
        oraclePackageSpecCache.clear();
        viewDependenciesCache.clear();
        oracleCountCache.clear();
        postgresCountCache.clear();
        brokerExecProcCache.clear();
        oracleReportsCache.clear();
        postgresReportsCache.clear();
        oracleCompositeReportsCache.clear();
        postgresCompositeReportsCache.clear();
        postgresViewOidCache.clear();
        pkCache.clear();
        notNullCache.clear();
        postgresFunctionCheckCache.clear();
        constantsCache.clear();
        systemOptionsCache.clear();
        tableColumnsCache.clear();
        foreignKeysCache.clear();
        indexesCache.clear();
        sequencesCache.clear();
        synonymsCache.clear();
        triggersCache.clear();
        missingObjectsCache.clear();

        System.out.println("[КЭШ] Все кэши очищены");
    }

    public static void setCacheOutputDir(String outputDir) {
        diskCacheManager.setOutputDir(outputDir);
        System.out.println("[DatabaseCacheManager] Установлена директория кэша: " + outputDir + "/DatabaseCache");
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
        System.out.println("Oracle Package Spec: " + oraclePackageSpecCache.size());
        System.out.println("View Dependencies: " + viewDependenciesCache.size());
        System.out.println("Oracle Count: " + oracleCountCache.size());
        System.out.println("PostgreSQL Count: " + postgresCountCache.size());
        System.out.println("Broker ExecProc: " + brokerExecProcCache.size());
        System.out.println("Oracle Reports: " + oracleReportsCache.size());
        System.out.println("PostgreSQL Reports: " + postgresReportsCache.size());
        System.out.println("Primary Key: " + pkCache.size());
        System.out.println("NOT NULL: " + notNullCache.size());
        System.out.println("Constants: " + constantsCache.size());
        System.out.println("System Options: " + systemOptionsCache.size());
        System.out.println("PostgreSQL View OID: " + postgresViewOidCache.size());
        System.out.println("Статус Oracle: " + (oracleAvailable ? "ДОСТУПНА" : "НЕДОСТУПНА"));
        System.out.println("Статус PostgreSQL: " + (postgresAvailable ? "ДОСТУПНА" : "НЕДОСТУПНА"));
    }

    // ==================== ГЕТТЕРЫ РАЗМЕРОВ КЭШЕЙ ====================

    public static int getOracleViewDDLCacheSize() { return oracleViewDDLCache.size(); }
    public static int getPostgresViewDDLCacheSize() { return postgresViewDDLCache.size(); }
    public static int getOracleTableDDLCacheSize() { return oracleTableDDLCache.size(); }
    public static int getPostgresTableDDLCacheSize() { return postgresTableDDLCache.size(); }
    public static int getOracleFunctionBodyCacheSize() { return oracleFunctionBodyCache.size(); }
    public static int getPostgresFunctionBodyCacheSize() { return postgresFunctionBodyCache.size(); }
    public static int getBrokerExecProcCacheSize() { return brokerExecProcCache.size(); }
    public static int getOracleReportsCacheSize() { return oracleReportsCache.size(); }
    public static int getPostgresReportsCacheSize() { return postgresReportsCache.size(); }
    public static int getConstantsCacheSize() { return constantsCache.size(); }
    public static int getSystemOptionsCacheSize() { return systemOptionsCache.size(); }

    // ==================== МЕТОДЫ ПРЯМОГО СОХРАНЕНИЯ (ДЛЯ ОПТИМИЗИРОВАННОЙ ЗАГРУЗКИ) ====================

    public static void putConstant(String constCode, String constValue) {
        String key = constCode.toUpperCase();
        constantsCache.put(key, constValue);
        markChanged();
    }

    public static void putSystemOption(String optionCode, String optionValue) {
        String key = optionCode.toUpperCase();
        systemOptionsCache.put(key, optionValue);
        markChanged();
    }

    public static void putBrokerExecProc(String unit, String action, String execProc) {
        String key = (unit + "_" + action).toUpperCase();
        brokerExecProcCache.put(key, execProc);
        markChanged();
    }

    public static void putOraclePackageSpec(String packageName, String spec) {
        String key = packageName.toUpperCase();
        if (spec != null) {
            oraclePackageSpecCache.put(key, spec);
            markChanged();
        }
    }

    public static void putOracleReport(String unitCode, List<DbReportInfo> reports) {
        String key = unitCode.toUpperCase();
        if (reports != null) {
            oracleReportsCache.put(key, reports);
            markChanged();
        }
    }

    public static void putPostgresReport(String unitCode, List<DbReportInfo> reports) {
        String key = unitCode.toUpperCase();
        if (reports != null) {
            postgresReportsCache.put(key, reports);
            markChanged();
        }
    }

    // ==================== МЕТОДЫ ПОЛУЧЕНИЯ С ЛЕНИВОЙ ЗАГРУЗКОЙ ====================

    public static String getOracleViewDDL(String viewName, Supplier<String> loader) {
        String key = viewName.toUpperCase();
        if (missingObjectsCache.containsKey(key)) return null;

        String cached = oracleViewDDLCache.get(key);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null) {
            oracleViewDDLCache.put(key, value);
            markChanged();
        } else {
            missingObjectsCache.put(key, true);
        }
        return value;
    }

    public static String getPostgresViewDDL(String viewName, Supplier<String> loader) {
        if (!isPostgresAvailable()) return null;
        String key = viewName.toLowerCase();

        String cached = postgresViewDDLCache.get(key);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null) {
            postgresViewDDLCache.put(key, value);
            markChanged();
        }
        return value;
    }

    // DatabaseCacheManager.java

    /**
     * Получение DDL таблицы с принудительной загрузкой при отсутствии
     */
    public static String getOracleTableDDL(String tableName, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = tableName.toUpperCase();

        // Проверяем кэш
        String cached = oracleTableDDLCache.get(key);
        if (cached != null) {
            return cached;
        }

        // Если нет в кэше - загружаем из БД
        System.out.println("[КЭШ] Таблица " + tableName + " не найдена в кэше, загружаем из БД...");
        String value = loader.get();

        if (value != null && !value.isEmpty()) {
            oracleTableDDLCache.put(key, value);
            markChanged();
            // Принудительно сохраняем после добавления важных данных
            forceSaveToDisk();
            System.out.println("[КЭШ] Таблица " + tableName + " загружена и сохранена");
        } else {
            System.out.println("[КЭШ] Таблица " + tableName + " не найдена в БД");
            // Запоминаем, что таблицы нет, чтобы не запрашивать снова
            missingObjectsCache.put(key, true);
        }
        return value;
    }

    /**
     * Получение зависимостей вьюхи с принудительной загрузкой
     */
    @SuppressWarnings("unchecked")
    public static ViewTableDependencies getViewDependencies(String viewName, Supplier<ViewTableDependencies> loader) {
        if (!isOracleAvailable()) return null;
        String key = viewName.toUpperCase();

        // Проверяем кэш
        Object cached = viewDependenciesCache.get(key);
        if (cached != null) {
            if (cached instanceof ViewTableDependencies) {
                return (ViewTableDependencies) cached;
            } else {
                // Неверный тип в кэше - удаляем
                viewDependenciesCache.remove(key);
            }
        }

        // Если нет в кэше - загружаем из БД
        System.out.println("[КЭШ] Вьюха " + viewName + " не найдена в кэше, загружаем из БД...");
        ViewTableDependencies value = loader.get();

        if (value != null) {
            viewDependenciesCache.put(key, value);
            markChanged();
            // Принудительно сохраняем после добавления важных данных
            forceSaveToDisk();
            System.out.println("[КЭШ] Вьюха " + viewName + " загружена и сохранена. Таблиц: " +
                    (value.getOracleTables() != null ? value.getOracleTables().size() : 0));
        }
        return value;
    }

    public static String getPostgresTableDDL(String tableName, Supplier<String> loader) {
        if (!isPostgresAvailable()) return null;
        String key = tableName.toLowerCase();

        String cached = postgresTableDDLCache.get(key);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null) {
            postgresTableDDLCache.put(key, value);
            markChanged();
        }
        return value;
    }

    public static String getOracleFunctionBody(String functionKey, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = functionKey.toUpperCase();

        String cached = oracleFunctionBodyCache.get(key);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null) {
            oracleFunctionBodyCache.put(key, value);
            markChanged();
        }
        return value;
    }

    public static String getPostgresFunctionBody(String functionKey, Supplier<String> loader) {
        if (!isPostgresAvailable()) return null;
        String key = functionKey.toLowerCase();

        String cached = postgresFunctionBodyCache.get(key);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null) {
            postgresFunctionBodyCache.put(key, value);
            markChanged();
        }
        return value;
    }

    public static Long getOracleCount(String objectName, Supplier<Long> loader) {
        if (!isOracleAvailable()) return -1L;
        String key = objectName.toUpperCase();

        Long cached = oracleCountCache.get(key);
        if (cached != null) return cached;

        Long value = loader.get();
        if (value != null && value >= 0) {
            oracleCountCache.put(key, value);
            markChanged();
        }
        return value != null ? value : -1L;
    }

    public static Long getPostgresCount(String objectName, Supplier<Long> loader) {
        if (!isPostgresAvailable()) return -1L;
        String key = objectName.toLowerCase();

        Long cached = postgresCountCache.get(key);
        if (cached != null) return cached;

        Long value = loader.get();
        if (value != null && value >= 0) {
            postgresCountCache.put(key, value);
            markChanged();
        }
        return value != null ? value : -1L;
    }

    public static String getBrokerExecProc(String unit, String action, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = (unit + "_" + action).toUpperCase();

        String cached = brokerExecProcCache.get(key);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null) {
            brokerExecProcCache.put(key, value);
            markChanged();
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static List<DbReportInfo> getOracleReports(String unitCode, Supplier<List<DbReportInfo>> loader) {
        if (!isOracleAvailable()) return Collections.emptyList();
        String key = unitCode.toUpperCase();

        Object cached = oracleReportsCache.get(key);
        if (cached != null) return (List<DbReportInfo>) cached;

        List<DbReportInfo> value = loader.get();
        if (value != null && !value.isEmpty()) {
            oracleReportsCache.put(key, value);
            markChanged();
        }
        return value != null ? value : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public static List<DbReportInfo> getPostgresReports(String unitCode, Supplier<List<DbReportInfo>> loader) {
        if (!isPostgresAvailable()) return Collections.emptyList();
        String key = unitCode.toUpperCase();

        Object cached = postgresReportsCache.get(key);
        if (cached != null) return (List<DbReportInfo>) cached;

        List<DbReportInfo> value = loader.get();
        if (value != null && !value.isEmpty()) {
            postgresReportsCache.put(key, value);
            markChanged();
        }
        return value != null ? value : Collections.emptyList();
    }



    @SuppressWarnings("unchecked")
    public static DatabaseObjectChecker.PrimaryKeyInfo getPrimaryKeyInfo(String tableName, Supplier<DatabaseObjectChecker.PrimaryKeyInfo> loader) {
        if (!isOracleAvailable() || !isPostgresAvailable()) return null;
        String key = tableName.toUpperCase();

        Object cached = pkCache.get(key);
        if (cached != null) return (DatabaseObjectChecker.PrimaryKeyInfo) cached;

        DatabaseObjectChecker.PrimaryKeyInfo value = loader.get();
        if (value != null) {
            pkCache.put(key, value);
            markChanged();
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static List<DatabaseObjectChecker.NotNullConstraintInfo> getNotNullConstraints(String tableName, Supplier<List<DatabaseObjectChecker.NotNullConstraintInfo>> loader) {
        if (!isOracleAvailable() || !isPostgresAvailable()) return Collections.emptyList();
        String key = tableName.toUpperCase();

        Object cached = notNullCache.get(key);
        if (cached != null) return (List<DatabaseObjectChecker.NotNullConstraintInfo>) cached;

        List<DatabaseObjectChecker.NotNullConstraintInfo> value = loader.get();
        if (value != null && !value.isEmpty()) {
            notNullCache.put(key, value);
            markChanged();
        }
        return value != null ? value : Collections.emptyList();
    }

    public static String getConstant(String constCode, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = constCode.toUpperCase();

        String cached = constantsCache.get(key);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null) {
            constantsCache.put(key, value);
            markChanged();
        }
        return value;
    }

    public static String getSystemOption(String optionCode, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = optionCode.toUpperCase();

        String cached = systemOptionsCache.get(key);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null) {
            systemOptionsCache.put(key, value);
            markChanged();
        }
        return value;
    }

    public static List<ColumnInfo> getTableColumns(String tableName, Supplier<List<ColumnInfo>> loader) {
        if (!isOracleAvailable()) return Collections.emptyList();
        String key = tableName.toUpperCase();

        @SuppressWarnings("unchecked")
        List<ColumnInfo> cached = (List<ColumnInfo>) tableColumnsCache.get(key);
        if (cached != null) return cached;

        List<ColumnInfo> value = loader.get();
        if (value != null && !value.isEmpty()) {
            tableColumnsCache.put(key, value);
            markChanged();
        }
        return value != null ? value : Collections.emptyList();
    }

    public static List<ForeignKeyInfo> getForeignKeys(String tableName, Supplier<List<ForeignKeyInfo>> loader) {
        if (!isOracleAvailable()) return Collections.emptyList();
        String key = tableName.toUpperCase();

        @SuppressWarnings("unchecked")
        List<ForeignKeyInfo> cached = (List<ForeignKeyInfo>) foreignKeysCache.get(key);
        if (cached != null) return cached;

        List<ForeignKeyInfo> value = loader.get();
        if (value != null && !value.isEmpty()) {
            foreignKeysCache.put(key, value);
            markChanged();
        }
        return value != null ? value : Collections.emptyList();
    }

    public static List<IndexInfo> getIndexes(String tableName, Supplier<List<IndexInfo>> loader) {
        if (!isOracleAvailable()) return Collections.emptyList();
        String key = tableName.toUpperCase();

        @SuppressWarnings("unchecked")
        List<IndexInfo> cached = (List<IndexInfo>) indexesCache.get(key);
        if (cached != null) return cached;

        List<IndexInfo> value = loader.get();
        if (value != null && !value.isEmpty()) {
            indexesCache.put(key, value);
            markChanged();
        }
        return value != null ? value : Collections.emptyList();
    }

    public static String getSynonymTarget(String synonymName, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = synonymName.toUpperCase();

        String cached = synonymsCache.get(key);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null) {
            synonymsCache.put(key, value);
            markChanged();
        }
        return value;
    }

    public static List<DbReportInfo> getOracleCompositeReports(int parentId, Supplier<List<DbReportInfo>> loader) {
        if (!isOracleAvailable()) return Collections.emptyList();
        String key = "COMPOSITE_ORACLE_" + parentId;
        return oracleCompositeReportsCache.computeIfAbsent(key, k -> loader.get());
    }

    public static List<DbReportInfo> getPostgresCompositeReports(int parentId, Supplier<List<DbReportInfo>> loader) {
        if (!isPostgresAvailable()) return Collections.emptyList();
        String key = "COMPOSITE_POSTGRES_" + parentId;
        return postgresCompositeReportsCache.computeIfAbsent(key, k -> loader.get());
    }

    public static int getPostgresViewOid(String viewName, Supplier<Integer> loader) {
        if (!isPostgresAvailable()) return -1;
        String key = viewName.toLowerCase();

        Integer cached = postgresViewOidCache.get(key);
        if (cached != null) return cached;

        Integer value = loader.get();
        if (value != null && value > 0) {
            postgresViewOidCache.put(key, value);
            markChanged();
        }
        return value != null ? value : -1;
    }

    public static boolean isOracleViewDDLCached(String viewName) {
        return oracleViewDDLCache.containsKey(viewName.toUpperCase());
    }

    public static boolean isOracleTableDDLCached(String tableName) {
        return oracleTableDDLCache.containsKey(tableName.toUpperCase());
    }

    public static boolean isOracleFunctionBodyCached(String functionKey) {
        return oracleFunctionBodyCache.containsKey(functionKey.toUpperCase());
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЙ КЛАСС ДЛЯ СНАПШОТА ====================

    public static class CacheSnapshot {
        private Map<String, String> oracleViewDDL = new ConcurrentHashMap<>();
        private Map<String, String> postgresViewDDL = new ConcurrentHashMap<>();
        private Map<String, String> oracleTableDDL = new ConcurrentHashMap<>();
        private Map<String, String> postgresTableDDL = new ConcurrentHashMap<>();
        private Map<String, String> oracleFunctionBody = new ConcurrentHashMap<>();
        private Map<String, String> postgresFunctionBody = new ConcurrentHashMap<>();
        private Map<String, String> oraclePackageSpec = new ConcurrentHashMap<>();
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
        private Map<String, String> constantsCache = new ConcurrentHashMap<>();
        private Map<String, String> systemOptionsCache = new ConcurrentHashMap<>();

        // Геттеры
        public Map<String, String> getOracleViewDDL() { return oracleViewDDL; }
        public Map<String, String> getPostgresViewDDL() { return postgresViewDDL; }
        public Map<String, String> getOracleTableDDL() { return oracleTableDDL; }
        public Map<String, String> getPostgresTableDDL() { return postgresTableDDL; }
        public Map<String, String> getOracleFunctionBody() { return oracleFunctionBody; }
        public Map<String, String> getPostgresFunctionBody() { return postgresFunctionBody; }
        public Map<String, String> getOraclePackageSpec() { return oraclePackageSpec; }
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
        public Map<String, String> getConstantsCache() { return constantsCache; }
        public Map<String, String> getSystemOptionsCache() { return systemOptionsCache; }

        // Сеттеры
        public void setOracleViewDDL(Map<String, String> map) { if (map != null) this.oracleViewDDL = new ConcurrentHashMap<>(map); }
        public void setPostgresViewDDL(Map<String, String> map) { if (map != null) this.postgresViewDDL = new ConcurrentHashMap<>(map); }
        public void setOracleTableDDL(Map<String, String> map) { if (map != null) this.oracleTableDDL = new ConcurrentHashMap<>(map); }
        public void setPostgresTableDDL(Map<String, String> map) { if (map != null) this.postgresTableDDL = new ConcurrentHashMap<>(map); }
        public void setOracleFunctionBody(Map<String, String> map) { if (map != null) this.oracleFunctionBody = new ConcurrentHashMap<>(map); }
        public void setPostgresFunctionBody(Map<String, String> map) { if (map != null) this.postgresFunctionBody = new ConcurrentHashMap<>(map); }
        public void setOraclePackageSpec(Map<String, String> map) { if (map != null) this.oraclePackageSpec = new ConcurrentHashMap<>(map); }
        public void setViewDependencies(Map<String, Object> map) { if (map != null) this.viewDependencies = new ConcurrentHashMap<>(map); }
        public void setOracleCount(Map<String, Long> map) { if (map != null) this.oracleCount = new ConcurrentHashMap<>(map); }
        public void setPostgresCount(Map<String, Long> map) { if (map != null) this.postgresCount = new ConcurrentHashMap<>(map); }
        public void setBrokerExecProc(Map<String, String> map) { if (map != null) this.brokerExecProc = new ConcurrentHashMap<>(map); }
        public void setOracleReports(Map<String, Object> map) { if (map != null) this.oracleReports = new ConcurrentHashMap<>(map); }
        public void setPostgresReports(Map<String, Object> map) { if (map != null) this.postgresReports = new ConcurrentHashMap<>(map); }
        public void setPrimaryKeyCache(Map<String, Object> map) { if (map != null) this.primaryKeyCache = new ConcurrentHashMap<>(map); }
        public void setNotNullCache(Map<String, Object> map) { if (map != null) this.notNullCache = new ConcurrentHashMap<>(map); }
        public void setPostgresViewOid(Map<String, Integer> map) { if (map != null) this.postgresViewOid = new ConcurrentHashMap<>(map); }
        public void setPostgresFunctionCheck(Map<String, Object> map) { if (map != null) this.postgresFunctionCheck = new ConcurrentHashMap<>(map); }
        public void setConstantsCache(Map<String, String> map) { if (map != null) this.constantsCache = new ConcurrentHashMap<>(map); }
        public void setSystemOptionsCache(Map<String, String> map) { if (map != null) this.systemOptionsCache = new ConcurrentHashMap<>(map); }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ ДЛЯ ХРАНЕНИЯ МЕТАДАННЫХ ====================

    public static class ColumnInfo implements Serializable {
        private final String columnName;
        private final String dataType;
        private final int dataLength;
        private final Integer precision;
        private final Integer scale;
        private final boolean nullable;
        private final String defaultValue;
        private final String comment;

        public ColumnInfo(String columnName, String dataType, int dataLength,
                          Integer precision, Integer scale, boolean nullable,
                          String defaultValue, String comment) {
            this.columnName = columnName;
            this.dataType = dataType;
            this.dataLength = dataLength;
            this.precision = precision;
            this.scale = scale;
            this.nullable = nullable;
            this.defaultValue = defaultValue;
            this.comment = comment;
        }

        public String getColumnName() { return columnName; }
        public String getDataType() { return dataType; }
        public int getDataLength() { return dataLength; }
        public Integer getPrecision() { return precision; }
        public Integer getScale() { return scale; }
        public boolean isNullable() { return nullable; }
        public String getDefaultValue() { return defaultValue; }
        public String getComment() { return comment; }

        @Override
        public String toString() {
            return String.format("%s %s%s%s", columnName, dataType,
                    dataLength > 0 ? "(" + dataLength + ")" : "",
                    !nullable ? " NOT NULL" : "");
        }
    }

    public static class ForeignKeyInfo implements Serializable {
        private final String constraintName;
        private final String foreignTable;
        private final String foreignColumn;
        private final String referencedTable;
        private final String referencedColumn;
        private final String deleteRule;

        public ForeignKeyInfo(String constraintName, String foreignTable, String foreignColumn,
                              String referencedTable, String referencedColumn, String deleteRule) {
            this.constraintName = constraintName;
            this.foreignTable = foreignTable;
            this.foreignColumn = foreignColumn;
            this.referencedTable = referencedTable;
            this.referencedColumn = referencedColumn;
            this.deleteRule = deleteRule;
        }

        public String getConstraintName() { return constraintName; }
        public String getForeignTable() { return foreignTable; }
        public String getForeignColumn() { return foreignColumn; }
        public String getReferencedTable() { return referencedTable; }
        public String getReferencedColumn() { return referencedColumn; }
        public String getDeleteRule() { return deleteRule; }
    }

    public static class IndexInfo implements Serializable {
        private final String indexName;
        private final String columnName;
        private final boolean unique;
        private final String indexType;
        private final int position;

        public IndexInfo(String indexName, String columnName, boolean unique,
                         String indexType, int position) {
            this.indexName = indexName;
            this.columnName = columnName;
            this.unique = unique;
            this.indexType = indexType;
            this.position = position;
        }

        public String getIndexName() { return indexName; }
        public String getColumnName() { return columnName; }
        public boolean isUnique() { return unique; }
        public String getIndexType() { return indexType; }
        public int getPosition() { return position; }
    }

    public static class SequenceInfo implements Serializable {
        private final String sequenceName;
        private final long minValue;
        private final long maxValue;
        private final long incrementBy;
        private final long lastNumber;

        public SequenceInfo(String sequenceName, long minValue, long maxValue,
                            long incrementBy, long lastNumber) {
            this.sequenceName = sequenceName;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.incrementBy = incrementBy;
            this.lastNumber = lastNumber;
        }

        public String getSequenceName() { return sequenceName; }
        public long getMinValue() { return minValue; }
        public long getMaxValue() { return maxValue; }
        public long getIncrementBy() { return incrementBy; }
        public long getLastNumber() { return lastNumber; }
    }

    public static class TriggerInfo implements Serializable {
        private final String triggerName;
        private final String tableName;
        private final String triggeringEvent;
        private final String timing;
        private final String status;
        private final String description;

        public TriggerInfo(String triggerName, String tableName, String triggeringEvent,
                           String timing, String status, String description) {
            this.triggerName = triggerName;
            this.tableName = tableName;
            this.triggeringEvent = triggeringEvent;
            this.timing = timing;
            this.status = status;
            this.description = description;
        }

        public String getTriggerName() { return triggerName; }
        public String getTableName() { return tableName; }
        public String getTriggeringEvent() { return triggeringEvent; }
        public String getTiming() { return timing; }
        public String getStatus() { return status; }
        public String getDescription() { return description; }
    }

    public static class PartitionInfo implements Serializable {
        private final String partitionName;
        private final String partitionPosition;
        private final String highValue;
        private final String tablespaceName;

        public PartitionInfo(String partitionName, String partitionPosition,
                             String highValue, String tablespaceName) {
            this.partitionName = partitionName;
            this.partitionPosition = partitionPosition;
            this.highValue = highValue;
            this.tablespaceName = tablespaceName;
        }

        public String getPartitionName() { return partitionName; }
        public String getPartitionPosition() { return partitionPosition; }
        public String getHighValue() { return highValue; }
        public String getTablespaceName() { return tablespaceName; }
    }

    public static class TableStatistics implements Serializable {
        private final long numRows;
        private final long blocks;
        private final long emptyBlocks;
        private final long avgSpace;
        private final long chainCnt;
        private final long avgRowLen;
        private final Date lastAnalyzed;

        public TableStatistics(long numRows, long blocks, long emptyBlocks,
                               long avgSpace, long chainCnt, long avgRowLen, Date lastAnalyzed) {
            this.numRows = numRows;
            this.blocks = blocks;
            this.emptyBlocks = emptyBlocks;
            this.avgSpace = avgSpace;
            this.chainCnt = chainCnt;
            this.avgRowLen = avgRowLen;
            this.lastAnalyzed = lastAnalyzed;
        }

        public long getNumRows() { return numRows; }
        public long getBlocks() { return blocks; }
        public long getEmptyBlocks() { return emptyBlocks; }
        public long getAvgSpace() { return avgSpace; }
        public long getChainCnt() { return chainCnt; }
        public long getAvgRowLen() { return avgRowLen; }
        public Date getLastAnalyzed() { return lastAnalyzed; }
    }
    // DatabaseCacheManager.java - добавить в раздел методов получения

    @SuppressWarnings("unchecked")
    public static <T> T getPostgresFunctionCheck(String functionName, Supplier<T> loader) {
        if (!isPostgresAvailable()) {
            return null;
        }
        String key = functionName.toLowerCase();

        Object cached = postgresFunctionCheckCache.get(key);
        if (cached != null) {
            return (T) cached;
        }

        T value = loader.get();
        if (value != null) {
            postgresFunctionCheckCache.put(key, value);
            markChanged();
        }
        return value;
    }
    // DatabaseCacheManager.java - добавить в раздел методов записи

    public static void putPostgresFunctionCheck(String functionName, Object value) {
        String key = functionName.toLowerCase();
        if (value != null) {
            postgresFunctionCheckCache.put(key, value);
            markChanged();
        }
    }
    public static int getPostgresFunctionCheckCacheSize() {
        return postgresFunctionCheckCache.size();
    }
    // DatabaseCacheManager.java - добавить в раздел методов получения (после getPostgresFunctionBody)

    public static String getOraclePackageSpec(String packageName, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = packageName.toUpperCase();

        String cached = oraclePackageSpecCache.get(key);
        if (cached != null) {
            return cached;
        }

        String value = loader.get();
        if (value != null) {
            oraclePackageSpecCache.put(key, value);
            markChanged();
        }
        return value;
    }

    public static void putTableColumns(String tableName, List<ColumnInfo> columns) {
        String key = tableName.toUpperCase();
        tableColumnsCache.put(key, columns);
        markChanged();
    }

    public static void putIndexes(String tableName, List<IndexInfo> indexes) {
        String key = tableName.toUpperCase();
        indexesCache.put(key, indexes);
        markChanged();
    }

    public static void putForeignKeys(String tableName, List<ForeignKeyInfo> fks) {
        String key = tableName.toUpperCase();
        foreignKeysCache.put(key, fks);
        markChanged();
    }

    public static void putSequence(String sequenceName, SequenceInfo sequence) {
        String key = sequenceName.toUpperCase();
        sequencesCache.put(key, sequence);
        markChanged();
    }

    public static void putTrigger(String triggerName, List<TriggerInfo> triggers) {
        String key = triggerName.toUpperCase();
        triggersCache.put(key, triggers);
        markChanged();
    }

    public static void putSynonym(String synonymName, String targetTable) {
        String key = synonymName.toUpperCase();
        synonymsCache.put(key, targetTable);
        markChanged();
    }
    public static int getOraclePackageSpecCacheSize() {
        return oraclePackageSpecCache.size();
    }
    /**
     * Очищает кэш зависимостей для указанной вьюхи
     */
    public static void clearViewDependency(String viewName) {
        String key = viewName.toUpperCase();
        viewDependenciesCache.remove(key);
        System.out.println("[КЭШ] Очищена зависимость для вьюхи: " + viewName);
    }

    /**
     * Очищает весь кэш зависимостей вьюх
     */
    public static void clearAllViewDependencies() {
        viewDependenciesCache.clear();
        System.out.println("[КЭШ] Очищены все зависимости вьюх");
    }


    /**
     * Прямое получение DDL вьюхи из кэша (без ленивой загрузки)
     * @param viewName имя вьюхи
     * @return DDL или null если нет в кэше
     */
    public static String getOracleViewDDL(String viewName) {
        return oracleViewDDLCache.get(viewName.toUpperCase());
    }

    /**
     * Прямое получение DDL вьюхи PostgreSQL из кэша
     */
    public static String getPostgresViewDDL(String viewName) {
        return postgresViewDDLCache.get(viewName.toLowerCase());
    }

    /**
     * Прямое получение DDL таблицы Oracle из кэша
     */
    public static String getOracleTableDDL(String tableName) {
        return oracleTableDDLCache.get(tableName.toUpperCase());
    }

    /**
     * Прямое получение DDL таблицы PostgreSQL из кэша
     */
    public static String getPostgresTableDDL(String tableName) {
        return postgresTableDDLCache.get(tableName.toLowerCase());
    }


// core/cache/DatabaseCacheManager.java - добавить все эти методы

// ==================== LAZY МЕТОДЫ ДЛЯ ВСЕХ ТИПОВ ДАННЫХ ====================
    /**
     * Получить DDL Oracle вьюхи с автоматической загрузкой при отсутствии в кэше
     */
    public static String getOracleViewDDLLazy(String viewName, Supplier<String> loader) {
        String key = viewName.toUpperCase();
        String cached = getOracleViewDDL(viewName);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null && !value.isEmpty()) {
            oracleViewDDLCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] Oracle вьюха " + viewName + " загружена и сохранена");
        }
        return value;
    }
    /**
     * Получить DDL PostgreSQL вьюхи с автоматической загрузкой при отсутствии в кэше
     */
    public static String getPostgresViewDDLLazy(String viewName, Supplier<String> loader) {
        if (!isPostgresAvailable()) return null;
        String key = viewName.toLowerCase();
        String cached = getPostgresViewDDL(viewName);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null && !value.isEmpty()) {
            postgresViewDDLCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] PostgreSQL вьюха " + viewName + " загружена и сохранена");
        }
        return value;
    }


    /**
     * Получить DDL Oracle таблицы с автоматической загрузкой при отсутствии в кэше
     */
    public static String getOracleTableDDLLazy(String tableName, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = tableName.toUpperCase();
        String cached = getOracleTableDDL(tableName);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null && !value.isEmpty()) {
            oracleTableDDLCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] Oracle таблица " + tableName + " загружена и сохранена");
        } else {
            missingObjectsCache.put(key, true);
        }
        return value;
    }


    /**
     * Получить DDL PostgreSQL таблицы с автоматической загрузкой при отсутствии в кэше
     */
    public static String getPostgresTableDDLLazy(String tableName, Supplier<String> loader) {
        if (!isPostgresAvailable()) return null;
        String key = tableName.toLowerCase();
        String cached = getPostgresTableDDL(tableName);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null && !value.isEmpty()) {
            postgresTableDDLCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] PostgreSQL таблица " + tableName + " загружена и сохранена");
        }
        return value;
    }

    /**
     * Получить тело Oracle функции с автоматической загрузкой при отсутствии в кэше
     */
    public static String getOracleFunctionBodyLazy(String functionKey, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = functionKey.toUpperCase();
        String cached = getOracleFunctionBody(key, () -> null); // используем существующий метод
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null && !value.isEmpty()) {
            oracleFunctionBodyCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] Oracle функция " + functionKey + " загружена и сохранена");
        }
        return value;
    }

    /**
     * Получить тело PostgreSQL функции с автоматической загрузкой при отсутствии в кэше
     */
    public static String getPostgresFunctionBodyLazy(String functionKey, Supplier<String> loader) {
        if (!isPostgresAvailable()) return null;
        String key = functionKey.toLowerCase();
        String cached = getPostgresFunctionBody(key, () -> null);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null && !value.isEmpty()) {
            postgresFunctionBodyCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] PostgreSQL функция " + functionKey + " загружена и сохранена");
        }
        return value;
    }

    /**
     * Получить execProc для брокера с автоматической загрузкой при отсутствии в кэше
     */
    public static String getBrokerExecProcLazy(String unit, String action, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = (unit + "_" + action).toUpperCase();
        String cached = brokerExecProcCache.get(key);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null && !value.isEmpty()) {
            brokerExecProcCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] Брокер unit=" + unit + ", action=" + action + " -> " + value);
        }
        return value;
    }

    /**
     * Получить константу с автоматической загрузкой при отсутствии в кэше
     */
    public static String getConstantLazy(String constCode, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = constCode.toUpperCase();
        String cached = constantsCache.get(key);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null && !value.isEmpty()) {
            constantsCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] Константа " + constCode + " = " + value);
        }
        return value;
    }

    /**
     * Получить системную опцию с автоматической загрузкой при отсутствии в кэше
     */
    public static String getSystemOptionLazy(String optionCode, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = optionCode.toUpperCase();
        String cached = systemOptionsCache.get(key);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null && !value.isEmpty()) {
            systemOptionsCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] Системная опция " + optionCode + " = " + value);
        }
        return value;
    }

    /**
     * Получить список отчётов Oracle с автоматической загрузкой при отсутствии в кэше
     */
    @SuppressWarnings("unchecked")
    public static List<DbReportInfo> getOracleReportsLazy(String unitCode, Supplier<List<DbReportInfo>> loader) {
        if (!isOracleAvailable()) return Collections.emptyList();
        String key = unitCode.toUpperCase();

        Object cached = oracleReportsCache.get(key);
        if (cached != null) {
            return (List<DbReportInfo>) cached;
        }

        List<DbReportInfo> value = loader.get();
        if (value != null && !value.isEmpty()) {
            oracleReportsCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] Отчёты Oracle для unit=" + unitCode + ": " + value.size() + " шт.");
        }
        return value != null ? value : Collections.emptyList();
    }

    /**
     * Получить список отчётов PostgreSQL с автоматической загрузкой при отсутствии в кэше
     */
    @SuppressWarnings("unchecked")
    public static List<DbReportInfo> getPostgresReportsLazy(String unitCode, Supplier<List<DbReportInfo>> loader) {
        if (!isPostgresAvailable()) return Collections.emptyList();
        String key = unitCode.toUpperCase();

        Object cached = postgresReportsCache.get(key);
        if (cached != null) {
            return (List<DbReportInfo>) cached;
        }

        List<DbReportInfo> value = loader.get();
        if (value != null && !value.isEmpty()) {
            postgresReportsCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] Отчёты PostgreSQL для unit=" + unitCode + ": " + value.size() + " шт.");
        }
        return value != null ? value : Collections.emptyList();
    }

    /**
     * Получить количество записей в Oracle с автоматической загрузкой при отсутствии в кэше
     */
    public static Long getOracleCountLazy(String objectName, Supplier<Long> loader) {
        if (!isOracleAvailable()) return -1L;
        String key = objectName.toUpperCase();

        Long cached = oracleCountCache.get(key);
        if (cached != null) return cached;

        Long value = loader.get();
        if (value != null && value >= 0) {
            oracleCountCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] Oracle COUNT(" + objectName + ") = " + value);
        }
        return value != null ? value : -1L;
    }

    /**
     * Получить количество записей в PostgreSQL с автоматической загрузкой при отсутствии в кэше
     */
    public static Long getPostgresCountLazy(String objectName, Supplier<Long> loader) {
        if (!isPostgresAvailable()) return -1L;
        String key = objectName.toLowerCase();

        Long cached = postgresCountCache.get(key);
        if (cached != null) return cached;

        Long value = loader.get();
        if (value != null && value >= 0) {
            postgresCountCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] PostgreSQL COUNT(" + objectName + ") = " + value);
        }
        return value != null ? value : -1L;
    }

    /**
     * Получить OID вьюхи PostgreSQL с автоматической загрузкой при отсутствии в кэше
     */
    public static Integer getPostgresViewOidLazy(String viewName, Supplier<Integer> loader) {
        if (!isPostgresAvailable()) return -1;
        String key = viewName.toLowerCase();

        Integer cached = postgresViewOidCache.get(key);
        if (cached != null) return cached;

        Integer value = loader.get();
        if (value != null && value > 0) {
            postgresViewOidCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] PostgreSQL OID для вьюхи " + viewName + " = " + value);
        }
        return value != null ? value : -1;
    }

    /**
     * Получить спецификацию Oracle пакета с автоматической загрузкой при отсутствии в кэше
     */
    public static String getOraclePackageSpecLazy(String packageName, Supplier<String> loader) {
        if (!isOracleAvailable()) return null;
        String key = packageName.toUpperCase();

        String cached = oraclePackageSpecCache.get(key);
        if (cached != null) return cached;

        String value = loader.get();
        if (value != null && !value.isEmpty()) {
            oraclePackageSpecCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] Oracle пакет " + packageName + " загружен и сохранён");
        }
        return value;
    }

    /**
     * Получить информацию о первичном ключе с автоматической загрузкой
     */
    @SuppressWarnings("unchecked")
    public static DatabaseObjectChecker.PrimaryKeyInfo getPrimaryKeyInfoLazy(String tableName, Supplier<DatabaseObjectChecker.PrimaryKeyInfo> loader) {
        if (!isOracleAvailable() || !isPostgresAvailable()) return null;
        String key = tableName.toUpperCase();

        Object cached = pkCache.get(key);
        if (cached != null) {
            return (DatabaseObjectChecker.PrimaryKeyInfo) cached;
        }

        DatabaseObjectChecker.PrimaryKeyInfo value = loader.get();
        if (value != null) {
            pkCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] PK информация для таблицы " + tableName + " загружена");
        }
        return value;
    }

    /**
     * Получить NOT NULL constraints с автоматической загрузкой
     */
    @SuppressWarnings("unchecked")
    public static List<DatabaseObjectChecker.NotNullConstraintInfo> getNotNullConstraintsLazy(String tableName, Supplier<List<DatabaseObjectChecker.NotNullConstraintInfo>> loader) {
        if (!isOracleAvailable() || !isPostgresAvailable()) return Collections.emptyList();
        String key = tableName.toUpperCase();

        Object cached = notNullCache.get(key);
        if (cached != null) {
            return (List<DatabaseObjectChecker.NotNullConstraintInfo>) cached;
        }

        List<DatabaseObjectChecker.NotNullConstraintInfo> value = loader.get();
        if (value != null && !value.isEmpty()) {
            notNullCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] NOT NULL constraints для таблицы " + tableName + " загружены");
        }
        return value != null ? value : Collections.emptyList();
    }

    /**
     * Получить результат проверки PostgreSQL функции с автоматической загрузкой
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPostgresFunctionCheckLazy(String functionName, Supplier<T> loader) {
        if (!isPostgresAvailable()) return null;
        String key = functionName.toLowerCase();

        Object cached = postgresFunctionCheckCache.get(key);
        if (cached != null) {
            return (T) cached;
        }

        T value = loader.get();
        if (value != null) {
            postgresFunctionCheckCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] Результат проверки функции PostgreSQL " + functionName + " сохранён");
        }
        return value;
    }

    public static void putOracleViewDDL(String viewName, String ddl) {
        String key = viewName.toUpperCase();
        if (ddl != null && !ddl.isEmpty()) {
            oracleViewDDLCache.put(key, ddl);
            markChanged();
        }
    }

    public static void putPostgresViewDDL(String viewName, String ddl) {
        String key = viewName.toLowerCase();
        if (ddl != null && !ddl.isEmpty()) {
            postgresViewDDLCache.put(key, ddl);
            markChanged();
        }
    }

    public static void putOracleTableDDL(String tableName, String ddl) {
        String key = tableName.toUpperCase();
        if (ddl != null && !ddl.isEmpty()) {
            oracleTableDDLCache.put(key, ddl);
            markChanged();
        }
    }

    public static void putPostgresTableDDL(String tableName, String ddl) {
        String key = tableName.toLowerCase();
        if (ddl != null && !ddl.isEmpty()) {
            postgresTableDDLCache.put(key, ddl);
            markChanged();
        }
    }

    public static void putOracleFunctionBody(String functionKey, String body) {
        String key = functionKey.toUpperCase();
        if (body != null && !body.isEmpty()) {
            oracleFunctionBodyCache.put(key, body);
            markChanged();
        }
    }

    public static void putPostgresFunctionBody(String functionKey, String body) {
        String key = functionKey.toLowerCase();
        if (body != null && !body.isEmpty()) {
            postgresFunctionBodyCache.put(key, body);
            markChanged();
        }
    }

    public static void putViewDependencies(String viewName, ViewTableDependencies deps) {
        String key = viewName.toUpperCase();
        if (deps != null) {
            viewDependenciesCache.put(key, deps);
            markChanged();
        }
    }

    public static ViewTableDependencies getViewDependenciesLazy(String viewName, Supplier<ViewTableDependencies> loader) {
        String key = viewName.toUpperCase();

        // Проверяем кэш через прямой метод
        ViewTableDependencies cached = getViewDependencies(viewName);
        if (cached != null) {
            return cached;
        }

        // Загружаем через ленивый загрузчик
        System.out.println("[КЭШ] Загрузка зависимостей вьюхи " + viewName + " из БД...");
        ViewTableDependencies value = loader.get();
        if (value != null) {
            viewDependenciesCache.put(key, value);
            markChanged();
            System.out.println("[КЭШ] Зависимости вьюхи " + viewName + " сохранены в кэш");
        }
        return value;
    }

    /**
     * Прямое получение зависимостей вьюхи из кэша
     */
    public static ViewTableDependencies getViewDependencies(String viewName) {
        Object cached = viewDependenciesCache.get(viewName.toUpperCase());
        if (cached instanceof ViewTableDependencies) {
            return (ViewTableDependencies) cached;
        }
        return null;
    }
}