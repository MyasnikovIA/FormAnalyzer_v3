// core/cache/PostgresDataCache.java
package ru.tmis.analyzer.core.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import ru.tmis.analyzer.core.db.DatabaseConnectionManager;
import ru.tmis.analyzer.core.model.DbReportInfo;
import ru.tmis.analyzer.core.db.PostgresPackageChecker;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Кэш для данных из PostgreSQL БД (отчёты, функции, вьюхи)
 * Данные сохраняются на диск и загружаются при старте
 */
public class PostgresDataCache {

    private static final String CACHE_SUBDIR = "DataCache";
    private static final String REPORTS_CACHE_FILE = "postgres_reports_cache.json";
    private static final String COMPOSITE_REPORTS_CACHE_FILE = "postgres_composite_reports_cache.json";
    private static final String FUNCTIONS_CACHE_FILE = "postgres_functions_cache.json";
    private static final String VIEW_OID_CACHE_FILE = "postgres_view_oid_cache.json";

    private static volatile PostgresDataCache instance;
    private String cacheDirPath;

    // Кэши в памяти
    private final Map<String, List<DbReportInfo>> reportsByUnitCache = new ConcurrentHashMap<>();
    private final Map<Integer, List<DbReportInfo>> compositeReportsCache = new ConcurrentHashMap<>();
    private final Map<String, PostgresPackageChecker.FunctionInfo> functionsCache = new ConcurrentHashMap<>();
    private final Map<String, Integer> viewOidCache = new ConcurrentHashMap<>();

    // Блокировки для потокобезопасности
    private final ReentrantReadWriteLock reportsLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock functionsLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock viewOidLock = new ReentrantReadWriteLock();

    // Флаг инициализации
    private volatile boolean initialized = false;
    private volatile boolean postgresAvailable = true;

    private PostgresDataCache() {}

    public static PostgresDataCache getInstance() {
        if (instance == null) {
            synchronized (PostgresDataCache.class) {
                if (instance == null) {
                    instance = new PostgresDataCache();
                }
            }
        }
        return instance;
    }

    /**
     * Инициализация кэша: загрузка с диска
     * @param outputDir директория отчётов (туда же будет сохранён кэш)
     */
    public void init(String outputDir) {
        if (initialized) return;

        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }
        this.cacheDirPath = Paths.get(outputDir, CACHE_SUBDIR).toString();

        System.out.println("[PostgresDataCache] Инициализация кэша, директория: " + cacheDirPath);

        // Загружаем кэш с диска
        loadReportsCacheFromDisk();
        loadFunctionsCacheFromDisk();
        loadViewOidCacheFromDisk();

        initialized = true;
        System.out.println("[PostgresDataCache] Инициализация завершена");
    }

    /**
     * Установить статус доступности PostgreSQL
     */
    public void setPostgresAvailable(boolean available) {
        this.postgresAvailable = available;
    }

    public boolean isPostgresAvailable() {
        return postgresAvailable;
    }

    // ==================== МЕТОДЫ ДЛЯ ОТЧЁТОВ ====================

    /**
     * Получить отчёты по unit (сначала из памяти, потом из диска, потом из БД)
     */
    public List<DbReportInfo> getReportsByUnit(String unitCode, java.util.function.Supplier<List<DbReportInfo>> dbLoader) {
        if (!postgresAvailable) {
            return Collections.emptyList();
        }

        if (unitCode == null || unitCode.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String key = unitCode.toUpperCase();

        // 1. Проверяем память
        reportsLock.readLock().lock();
        try {
            if (reportsByUnitCache.containsKey(key)) {
                System.out.println("[PostgresDataCache] Отчёты для unit=" + unitCode + " получены из памяти");
                return reportsByUnitCache.get(key);
            }
        } finally {
            reportsLock.readLock().unlock();
        }

        // 2. Загружаем из БД (и сохраняем в кэш)
        reportsLock.writeLock().lock();
        try {
            // Double-check после получения блокировки
            if (reportsByUnitCache.containsKey(key)) {
                return reportsByUnitCache.get(key);
            }

            System.out.println("[PostgresDataCache] Загрузка отчётов для unit=" + unitCode + " из БД...");
            List<DbReportInfo> reports = dbLoader.get();
            if (reports != null && !reports.isEmpty()) {
                reportsByUnitCache.put(key, reports);
                // Сохраняем на диск асинхронно
                saveReportsCacheToDiskAsync();
            }
            return reports != null ? reports : Collections.emptyList();
        } finally {
            reportsLock.writeLock().unlock();
        }
    }

    /**
     * Получить составные части отчёта
     */
    public List<DbReportInfo> getCompositeReports(int parentId, java.util.function.Supplier<List<DbReportInfo>> dbLoader) {
        if (!postgresAvailable) {
            return Collections.emptyList();
        }

        // 1. Проверяем память
        reportsLock.readLock().lock();
        try {
            if (compositeReportsCache.containsKey(parentId)) {
                System.out.println("[PostgresDataCache] Составные отчёты для ID=" + parentId + " получены из памяти");
                return compositeReportsCache.get(parentId);
            }
        } finally {
            reportsLock.readLock().unlock();
        }

        // 2. Загружаем из БД
        reportsLock.writeLock().lock();
        try {
            if (compositeReportsCache.containsKey(parentId)) {
                return compositeReportsCache.get(parentId);
            }

            System.out.println("[PostgresDataCache] Загрузка составных отчётов для ID=" + parentId + " из БД...");
            List<DbReportInfo> reports = dbLoader.get();
            if (reports != null && !reports.isEmpty()) {
                compositeReportsCache.put(parentId, reports);
                saveReportsCacheToDiskAsync();
            }
            return reports != null ? reports : Collections.emptyList();
        } finally {
            reportsLock.writeLock().unlock();
        }
    }

    // ==================== МЕТОДЫ ДЛЯ ФУНКЦИЙ ====================

    /**
     * Получить информацию о функции (с кэшированием)
     */
    public PostgresPackageChecker.FunctionInfo getFunctionInfo(String functionName,
                                                               java.util.function.Supplier<PostgresPackageChecker.FunctionInfo> dbLoader) {
        if (!postgresAvailable) {
            return null;
        }

        if (functionName == null || functionName.trim().isEmpty()) {
            return null;
        }

        String key = functionName.toLowerCase();

        functionsLock.readLock().lock();
        try {
            if (functionsCache.containsKey(key)) {
                System.out.println("[PostgresDataCache] Функция " + functionName + " получена из памяти");
                return functionsCache.get(key);
            }
        } finally {
            functionsLock.readLock().unlock();
        }

        functionsLock.writeLock().lock();
        try {
            if (functionsCache.containsKey(key)) {
                return functionsCache.get(key);
            }

            System.out.println("[PostgresDataCache] Загрузка функции " + functionName + " из БД...");
            PostgresPackageChecker.FunctionInfo info = dbLoader.get();
            if (info != null) {
                functionsCache.put(key, info);
                saveFunctionsCacheToDiskAsync();
            }
            return info;
        } finally {
            functionsLock.writeLock().unlock();
        }
    }

    /**
     * Получить информацию о нескольких функциях (пакетная загрузка с кэшированием)
     */
    public Map<String, PostgresPackageChecker.FunctionInfo> getFunctionsInfo(Set<String> functionNames,
                                                                             java.util.function.Function<Set<String>, Map<String, PostgresPackageChecker.FunctionInfo>> dbLoader) {
        if (!postgresAvailable || functionNames == null || functionNames.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, PostgresPackageChecker.FunctionInfo> result = new LinkedHashMap<>();
        Set<String> missingFunctions = new LinkedHashSet<>();

        // 1. Сначала проверяем кэш
        functionsLock.readLock().lock();
        try {
            for (String name : functionNames) {
                String key = name.toLowerCase();
                if (functionsCache.containsKey(key)) {
                    result.put(name, functionsCache.get(key));
                } else {
                    missingFunctions.add(name);
                }
            }
        } finally {
            functionsLock.readLock().unlock();
        }

        // 2. Если есть отсутствующие, загружаем из БД
        if (!missingFunctions.isEmpty()) {
            System.out.println("[PostgresDataCache] Загрузка " + missingFunctions.size() + " функций из БД...");
            Map<String, PostgresPackageChecker.FunctionInfo> loaded = dbLoader.apply(missingFunctions);

            functionsLock.writeLock().lock();
            try {
                for (Map.Entry<String, PostgresPackageChecker.FunctionInfo> entry : loaded.entrySet()) {
                    String key = entry.getKey().toLowerCase();
                    functionsCache.put(key, entry.getValue());
                    result.put(entry.getKey(), entry.getValue());
                }
                if (!loaded.isEmpty()) {
                    saveFunctionsCacheToDiskAsync();
                }
            } finally {
                functionsLock.writeLock().unlock();
            }
        }

        return result;
    }

    // ==================== МЕТОДЫ ДЛЯ OID ВЬЮХ ====================

    /**
     * Получить OID вьюхи по имени
     */
    public int getViewOid(String viewName, java.util.function.Supplier<Integer> dbLoader) {
        if (!postgresAvailable) {
            return -1;
        }

        if (viewName == null || viewName.trim().isEmpty()) {
            return -1;
        }

        String key = viewName.toLowerCase();

        viewOidLock.readLock().lock();
        try {
            if (viewOidCache.containsKey(key)) {
                return viewOidCache.get(key);
            }
        } finally {
            viewOidLock.readLock().unlock();
        }

        viewOidLock.writeLock().lock();
        try {
            if (viewOidCache.containsKey(key)) {
                return viewOidCache.get(key);
            }

            System.out.println("[PostgresDataCache] Загрузка OID для вьюхи " + viewName + " из БД...");
            int oid = dbLoader.get();
            if (oid > 0) {
                viewOidCache.put(key, oid);
                saveViewOidCacheToDiskAsync();
            }
            return oid;
        } finally {
            viewOidLock.writeLock().unlock();
        }
    }

    // ==================== СОХРАНЕНИЕ НА ДИСК ====================

    /**
     * Сохранить кэш отчётов на диск
     */
    private void saveReportsCacheToDisk() {
        if (cacheDirPath == null) return;

        try {
            Path cacheDir = Paths.get(cacheDirPath);
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Сохраняем reportsByUnit
            Map<String, List<DbReportInfo>> reportsSnapshot;
            reportsLock.readLock().lock();
            try {
                reportsSnapshot = new LinkedHashMap<>(reportsByUnitCache);
            } finally {
                reportsLock.readLock().unlock();
            }

            if (!reportsSnapshot.isEmpty()) {
                String reportsJson = gson.toJson(reportsSnapshot);
                Path reportsPath = cacheDir.resolve(REPORTS_CACHE_FILE);
                Files.writeString(reportsPath, reportsJson, StandardCharsets.UTF_8);
                System.out.println("[PostgresDataCache] Сохранён кэш отчётов (" + reportsSnapshot.size() + " записей)");
            }

            // Сохраняем compositeReports
            Map<Integer, List<DbReportInfo>> compositeSnapshot;
            reportsLock.readLock().lock();
            try {
                compositeSnapshot = new LinkedHashMap<>(compositeReportsCache);
            } finally {
                reportsLock.readLock().unlock();
            }

            if (!compositeSnapshot.isEmpty()) {
                String compositeJson = gson.toJson(compositeSnapshot);
                Path compositePath = cacheDir.resolve(COMPOSITE_REPORTS_CACHE_FILE);
                Files.writeString(compositePath, compositeJson, StandardCharsets.UTF_8);
                System.out.println("[PostgresDataCache] Сохранён кэш составных отчётов (" + compositeSnapshot.size() + " записей)");
            }

        } catch (IOException e) {
            System.err.println("[PostgresDataCache] Ошибка сохранения кэша отчётов: " + e.getMessage());
        }
    }

    private void saveReportsCacheToDiskAsync() {
        Thread.startVirtualThread(() -> saveReportsCacheToDisk());
    }

    /**
     * Сохранить кэш функций на диск
     */
    private void saveFunctionsCacheToDisk() {
        if (cacheDirPath == null) return;

        try {
            Path cacheDir = Paths.get(cacheDirPath);
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            Map<String, PostgresPackageChecker.FunctionInfo> functionsSnapshot;
            functionsLock.readLock().lock();
            try {
                functionsSnapshot = new LinkedHashMap<>(functionsCache);
            } finally {
                functionsLock.readLock().unlock();
            }

            if (!functionsSnapshot.isEmpty()) {
                String functionsJson = gson.toJson(functionsSnapshot);
                Path functionsPath = cacheDir.resolve(FUNCTIONS_CACHE_FILE);
                Files.writeString(functionsPath, functionsJson, StandardCharsets.UTF_8);
                System.out.println("[PostgresDataCache] Сохранён кэш функций (" + functionsSnapshot.size() + " записей)");
            }

        } catch (IOException e) {
            System.err.println("[PostgresDataCache] Ошибка сохранения кэша функций: " + e.getMessage());
        }
    }

    private void saveFunctionsCacheToDiskAsync() {
        Thread.startVirtualThread(() -> saveFunctionsCacheToDisk());
    }

    /**
     * Сохранить кэш OID вьюх на диск
     */
    private void saveViewOidCacheToDisk() {
        if (cacheDirPath == null) return;

        try {
            Path cacheDir = Paths.get(cacheDirPath);
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            Map<String, Integer> oidSnapshot;
            viewOidLock.readLock().lock();
            try {
                oidSnapshot = new LinkedHashMap<>(viewOidCache);
            } finally {
                viewOidLock.readLock().unlock();
            }

            if (!oidSnapshot.isEmpty()) {
                String oidJson = gson.toJson(oidSnapshot);
                Path oidPath = cacheDir.resolve(VIEW_OID_CACHE_FILE);
                Files.writeString(oidPath, oidJson, StandardCharsets.UTF_8);
                System.out.println("[PostgresDataCache] Сохранён кэш OID вьюх (" + oidSnapshot.size() + " записей)");
            }

        } catch (IOException e) {
            System.err.println("[PostgresDataCache] Ошибка сохранения кэша OID: " + e.getMessage());
        }
    }

    private void saveViewOidCacheToDiskAsync() {
        Thread.startVirtualThread(() -> saveViewOidCacheToDisk());
    }

    // ==================== ЗАГРУЗКА С ДИСКА ====================

    /**
     * Загрузить кэш отчётов с диска
     */
    @SuppressWarnings("unchecked")
    private void loadReportsCacheFromDisk() {
        if (cacheDirPath == null) return;

        Path cacheDir = Paths.get(cacheDirPath);
        if (!Files.exists(cacheDir)) {
            System.out.println("[PostgresDataCache] Директория кэша не существует, пропускаем загрузку");
            return;
        }

        Gson gson = new GsonBuilder().create();
        Type reportsMapType = new TypeToken<Map<String, List<DbReportInfo>>>(){}.getType();
        Type compositeMapType = new TypeToken<Map<Integer, List<DbReportInfo>>>(){}.getType();

        // Загружаем reportsByUnit
        Path reportsPath = cacheDir.resolve(REPORTS_CACHE_FILE);
        if (Files.exists(reportsPath)) {
            try {
                String json = Files.readString(reportsPath, StandardCharsets.UTF_8);
                if (json != null && !json.trim().isEmpty() && isValidJson(json)) {
                    Map<String, List<DbReportInfo>> loaded = gson.fromJson(json, reportsMapType);
                    if (loaded != null && !loaded.isEmpty()) {
                        reportsLock.writeLock().lock();
                        try {
                            reportsByUnitCache.putAll(loaded);
                        } finally {
                            reportsLock.writeLock().unlock();
                        }
                        System.out.println("[PostgresDataCache] Загружен кэш отчётов с диска (" + loaded.size() + " записей)");
                    } else {
                        Files.delete(reportsPath);
                    }
                } else {
                    System.out.println("[PostgresDataCache] Файл кэша отчётов повреждён, удаляем");
                    Files.delete(reportsPath);
                }
            } catch (IOException | JsonSyntaxException e) {
                System.err.println("[PostgresDataCache] Ошибка загрузки кэша отчётов: " + e.getMessage());
                try {
                    Files.delete(reportsPath);
                } catch (IOException ex) {
                    System.err.println("[PostgresDataCache] Не удалось удалить повреждённый файл: " + ex.getMessage());
                }
            }
        }

        // Загружаем compositeReports
        Path compositePath = cacheDir.resolve(COMPOSITE_REPORTS_CACHE_FILE);
        if (Files.exists(compositePath)) {
            try {
                String json = Files.readString(compositePath, StandardCharsets.UTF_8);
                if (json != null && !json.trim().isEmpty() && isValidJson(json)) {
                    Map<Integer, List<DbReportInfo>> loaded = gson.fromJson(json, compositeMapType);
                    if (loaded != null && !loaded.isEmpty()) {
                        reportsLock.writeLock().lock();
                        try {
                            compositeReportsCache.putAll(loaded);
                        } finally {
                            reportsLock.writeLock().unlock();
                        }
                        System.out.println("[PostgresDataCache] Загружен кэш составных отчётов с диска (" + loaded.size() + " записей)");
                    } else {
                        Files.delete(compositePath);
                    }
                } else {
                    System.out.println("[PostgresDataCache] Файл кэша составных отчётов повреждён, удаляем");
                    Files.delete(compositePath);
                }
            } catch (IOException | JsonSyntaxException e) {
                System.err.println("[PostgresDataCache] Ошибка загрузки кэша составных отчётов: " + e.getMessage());
                try {
                    Files.delete(compositePath);
                } catch (IOException ex) {
                    System.err.println("[PostgresDataCache] Не удалось удалить повреждённый файл: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Загрузить кэш функций с диска
     */
    private void loadFunctionsCacheFromDisk() {
        if (cacheDirPath == null) return;

        Path cacheDir = Paths.get(cacheDirPath);
        if (!Files.exists(cacheDir)) {
            return;
        }

        Path functionsPath = cacheDir.resolve(FUNCTIONS_CACHE_FILE);
        if (Files.exists(functionsPath)) {
            try {
                Gson gson = new GsonBuilder().create();
                Type functionsMapType = new TypeToken<Map<String, PostgresPackageChecker.FunctionInfo>>(){}.getType();
                String json = Files.readString(functionsPath, StandardCharsets.UTF_8);
                if (json != null && !json.trim().isEmpty() && isValidJson(json)) {
                    Map<String, PostgresPackageChecker.FunctionInfo> loaded = gson.fromJson(json, functionsMapType);
                    if (loaded != null && !loaded.isEmpty()) {
                        functionsLock.writeLock().lock();
                        try {
                            functionsCache.putAll(loaded);
                        } finally {
                            functionsLock.writeLock().unlock();
                        }
                        System.out.println("[PostgresDataCache] Загружен кэш функций с диска (" + loaded.size() + " записей)");
                    } else {
                        Files.delete(functionsPath);
                    }
                } else {
                    System.out.println("[PostgresDataCache] Файл кэша функций повреждён, удаляем");
                    Files.delete(functionsPath);
                }
            } catch (IOException | JsonSyntaxException e) {
                System.err.println("[PostgresDataCache] Ошибка загрузки кэша функций: " + e.getMessage());
                try {
                    Files.delete(functionsPath);
                } catch (IOException ex) {
                    System.err.println("[PostgresDataCache] Не удалось удалить повреждённый файл: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Загрузить кэш OID вьюх с диска
     */
    private void loadViewOidCacheFromDisk() {
        if (cacheDirPath == null) return;

        Path cacheDir = Paths.get(cacheDirPath);
        if (!Files.exists(cacheDir)) {
            return;
        }

        Path oidPath = cacheDir.resolve(VIEW_OID_CACHE_FILE);
        if (Files.exists(oidPath)) {
            try {
                Gson gson = new GsonBuilder().create();
                Type oidMapType = new TypeToken<Map<String, Integer>>(){}.getType();
                String json = Files.readString(oidPath, StandardCharsets.UTF_8);
                if (json != null && !json.trim().isEmpty() && isValidJson(json)) {
                    Map<String, Integer> loaded = gson.fromJson(json, oidMapType);
                    if (loaded != null && !loaded.isEmpty()) {
                        viewOidLock.writeLock().lock();
                        try {
                            viewOidCache.putAll(loaded);
                        } finally {
                            viewOidLock.writeLock().unlock();
                        }
                        System.out.println("[PostgresDataCache] Загружен кэш OID вьюх с диска (" + loaded.size() + " записей)");
                    } else {
                        Files.delete(oidPath);
                    }
                } else {
                    System.out.println("[PostgresDataCache] Файл кэша OID вьюх повреждён, удаляем");
                    Files.delete(oidPath);
                }
            } catch (IOException | JsonSyntaxException e) {
                System.err.println("[PostgresDataCache] Ошибка загрузки кэша OID: " + e.getMessage());
                try {
                    Files.delete(oidPath);
                } catch (IOException ex) {
                    System.err.println("[PostgresDataCache] Не удалось удалить повреждённый файл: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Проверка валидности JSON строки
     */
    private boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) return false;
        String trimmed = json.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    // ==================== ОЧИСТКА И СТАТИСТИКА ====================

    /**
     * Очистить все кэши (память и диск)
     */
    public void clearAll() {
        reportsLock.writeLock().lock();
        try {
            reportsByUnitCache.clear();
            compositeReportsCache.clear();
        } finally {
            reportsLock.writeLock().unlock();
        }

        functionsLock.writeLock().lock();
        try {
            functionsCache.clear();
        } finally {
            functionsLock.writeLock().unlock();
        }

        viewOidLock.writeLock().lock();
        try {
            viewOidCache.clear();
        } finally {
            viewOidLock.writeLock().unlock();
        }

        // Удаляем файлы кэша
        if (cacheDirPath != null) {
            try {
                Path cacheDir = Paths.get(cacheDirPath);
                if (Files.exists(cacheDir)) {
                    Files.walk(cacheDir)
                            .filter(Files::isRegularFile)
                            .forEach(file -> {
                                String fileName = file.getFileName().toString();
                                if (fileName.startsWith("postgres_")) {
                                    try {
                                        Files.delete(file);
                                    } catch (IOException e) {
                                        System.err.println("[PostgresDataCache] Ошибка удаления файла: " + file);
                                    }
                                }
                            });
                    System.out.println("[PostgresDataCache] Кэш PostgreSQL полностью очищен");
                }
            } catch (IOException e) {
                System.err.println("[PostgresDataCache] Ошибка очистки кэша: " + e.getMessage());
            }
        }
    }

    /**
     * Вывести статистику кэша
     */
    public void printStats() {
        System.out.println("=== СТАТИСТИКА КЭША POSTGRESQL DATA ===");
        System.out.println("Отчёты по unit: " + reportsByUnitCache.size());
        System.out.println("Составные отчёты: " + compositeReportsCache.size());
        System.out.println("Функции: " + functionsCache.size());
        System.out.println("OID вьюх: " + viewOidCache.size());
        System.out.println("Доступность PostgreSQL: " + (postgresAvailable ? "ДА" : "НЕТ"));
        System.out.println("========================================");
    }
    /**
     * Принудительное синхронное сохранение кэша на диск
     */
    public void saveReportsCacheToDiskSync() {
        saveReportsCacheToDisk();
        saveFunctionsCacheToDisk();
        saveViewOidCacheToDisk();
    }
}