// core/cache/OracleDataCache.java
package ru.tmis.analyzer.core.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import ru.tmis.analyzer.core.db.DatabaseConnectionManager;
import ru.tmis.analyzer.core.model.DbReportInfo;
import ru.tmis.analyzer.core.model.BrokerInfo;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Кэш для данных из Oracle БД (отчёты и брокеры)
 * Данные сохраняются на диск и загружаются при старте
 */
public class OracleDataCache {

    private static final String CACHE_SUBDIR = "DataCache";
    private static final String REPORTS_CACHE_FILE = "oracle_reports_cache.json";
    private static final String BROKERS_CACHE_FILE = "oracle_brokers_cache.json";
    private static final String COMPOSITE_REPORTS_CACHE_FILE = "oracle_composite_reports_cache.json";

    private static volatile OracleDataCache instance;
    private String cacheDirPath;

    // Кэши в памяти
    private final Map<String, List<DbReportInfo>> reportsByUnitCache = new ConcurrentHashMap<>();
    private final Map<Integer, List<DbReportInfo>> compositeReportsCache = new ConcurrentHashMap<>();
    private final Map<String, DbReportInfo> reportByCodeCache = new ConcurrentHashMap<>();
    private final Map<String, String> brokerExecProcCache = new ConcurrentHashMap<>();

    // Блокировки для потокобезопасности
    private final ReentrantReadWriteLock reportsLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock brokersLock = new ReentrantReadWriteLock();

    // Флаг инициализации
    private volatile boolean initialized = false;

    private OracleDataCache() {}

    public static OracleDataCache getInstance() {
        if (instance == null) {
            synchronized (OracleDataCache.class) {
                if (instance == null) {
                    instance = new OracleDataCache();
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

        System.out.println("[OracleDataCache] Инициализация кэша, директория: " + cacheDirPath);

        // Загружаем кэш с диска
        loadReportsCacheFromDisk();
        loadBrokersCacheFromDisk();

        initialized = true;
        System.out.println("[OracleDataCache] Инициализация завершена");
    }

    /**
     * Получить отчёты по unit (сначала из памяти, потом из диска, потом из БД)
     */
    public List<DbReportInfo> getReportsByUnit(String unitCode, java.util.function.Supplier<List<DbReportInfo>> dbLoader) {
        if (unitCode == null || unitCode.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String key = unitCode.toUpperCase();

        // 1. Проверяем память
        reportsLock.readLock().lock();
        try {
            if (reportsByUnitCache.containsKey(key)) {
                System.out.println("[OracleDataCache] Отчёты для unit=" + unitCode + " получены из памяти");
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

            System.out.println("[OracleDataCache] Загрузка отчётов для unit=" + unitCode + " из БД...");
            List<DbReportInfo> reports = dbLoader.get();
            if (reports != null) {
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
        // 1. Проверяем память
        reportsLock.readLock().lock();
        try {
            if (compositeReportsCache.containsKey(parentId)) {
                System.out.println("[OracleDataCache] Составные отчёты для ID=" + parentId + " получены из памяти");
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

            System.out.println("[OracleDataCache] Загрузка составных отчётов для ID=" + parentId + " из БД...");
            List<DbReportInfo> reports = dbLoader.get();
            if (reports != null) {
                compositeReportsCache.put(parentId, reports);
                saveReportsCacheToDiskAsync();
            }
            return reports != null ? reports : Collections.emptyList();
        } finally {
            reportsLock.writeLock().unlock();
        }
    }

    /**
     * Получить отчёт по коду
     */
    public DbReportInfo getReportByCode(String repCode, java.util.function.Supplier<DbReportInfo> dbLoader) {
        if (repCode == null || repCode.trim().isEmpty()) {
            return null;
        }

        String key = repCode.toUpperCase();

        reportsLock.readLock().lock();
        try {
            if (reportByCodeCache.containsKey(key)) {
                System.out.println("[OracleDataCache] Отчёт по коду " + repCode + " получен из памяти");
                return reportByCodeCache.get(key);
            }
        } finally {
            reportsLock.readLock().unlock();
        }

        reportsLock.writeLock().lock();
        try {
            if (reportByCodeCache.containsKey(key)) {
                return reportByCodeCache.get(key);
            }

            System.out.println("[OracleDataCache] Загрузка отчёта по коду " + repCode + " из БД...");
            DbReportInfo report = dbLoader.get();
            if (report != null) {
                reportByCodeCache.put(key, report);
                saveReportsCacheToDiskAsync();
            }
            return report;
        } finally {
            reportsLock.writeLock().unlock();
        }
    }

    /**
     * Получить execProc для брокера
     */
    public String getBrokerExecProc(String unit, String action, java.util.function.Supplier<String> dbLoader) {
        if (unit == null || action == null) return null;

        String key = (unit + "_" + action).toUpperCase();

        brokersLock.readLock().lock();
        try {
            if (brokerExecProcCache.containsKey(key)) {
                System.out.println("[OracleDataCache] Брокер unit=" + unit + ", action=" + action + " получен из памяти");
                return brokerExecProcCache.get(key);
            }
        } finally {
            brokersLock.readLock().unlock();
        }

        brokersLock.writeLock().lock();
        try {
            if (brokerExecProcCache.containsKey(key)) {
                return brokerExecProcCache.get(key);
            }

            System.out.println("[OracleDataCache] Загрузка брокера unit=" + unit + ", action=" + action + " из БД...");
            String execProc = dbLoader.get();
            if (execProc != null) {
                brokerExecProcCache.put(key, execProc);
                saveBrokersCacheToDiskAsync();
            }
            return execProc;
        } finally {
            brokersLock.writeLock().unlock();
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
                System.out.println("[OracleDataCache] Сохранён кэш отчётов (" + reportsSnapshot.size() + " записей)");
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
                System.out.println("[OracleDataCache] Сохранён кэш составных отчётов (" + compositeSnapshot.size() + " записей)");
            }

            // Сохраняем reportByCode
            Map<String, DbReportInfo> byCodeSnapshot;
            reportsLock.readLock().lock();
            try {
                byCodeSnapshot = new LinkedHashMap<>(reportByCodeCache);
            } finally {
                reportsLock.readLock().unlock();
            }

            // Можно сохранить в тот же файл или отдельный
            // Для простоты сохраняем вместе с reportsByUnit

        } catch (IOException e) {
            System.err.println("[OracleDataCache] Ошибка сохранения кэша отчётов: " + e.getMessage());
        }
    }

    /**
     * Асинхронное сохранение кэша отчётов
     */
    private void saveReportsCacheToDiskAsync() {
        Thread.startVirtualThread(() -> saveReportsCacheToDisk());
    }

    /**
     * Сохранить кэш брокеров на диск
     */
    private void saveBrokersCacheToDisk() {
        if (cacheDirPath == null) return;

        try {
            Path cacheDir = Paths.get(cacheDirPath);
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            Map<String, String> brokersSnapshot;
            brokersLock.readLock().lock();
            try {
                brokersSnapshot = new LinkedHashMap<>(brokerExecProcCache);
            } finally {
                brokersLock.readLock().unlock();
            }

            if (!brokersSnapshot.isEmpty()) {
                String brokersJson = gson.toJson(brokersSnapshot);
                Path brokersPath = cacheDir.resolve(BROKERS_CACHE_FILE);
                Files.writeString(brokersPath, brokersJson, StandardCharsets.UTF_8);
                System.out.println("[OracleDataCache] Сохранён кэш брокеров (" + brokersSnapshot.size() + " записей)");
            }
        } catch (IOException e) {
            System.err.println("[OracleDataCache] Ошибка сохранения кэша брокеров: " + e.getMessage());
        }
    }

    /**
     * Асинхронное сохранение кэша брокеров
     */
    private void saveBrokersCacheToDiskAsync() {
        Thread.startVirtualThread(() -> saveBrokersCacheToDisk());
    }

    // ==================== ЗАГРУЗКА С ДИСКА ====================

    /**
     * Загрузить кэш отчётов с диска
     */
    /**
     * Загрузить кэш отчётов с диска
     */
    @SuppressWarnings("unchecked")
    private void loadReportsCacheFromDisk() {
        if (cacheDirPath == null) return;

        Path cacheDir = Paths.get(cacheDirPath);
        if (!Files.exists(cacheDir)) {
            System.out.println("[OracleDataCache] Директория кэша не существует, пропускаем загрузку");
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
                // Проверяем, что JSON не пустой и валидный
                if (json != null && !json.trim().isEmpty() && isValidJson(json)) {
                    Map<String, List<DbReportInfo>> loaded = gson.fromJson(json, reportsMapType);
                    if (loaded != null && !loaded.isEmpty()) {
                        reportsLock.writeLock().lock();
                        try {
                            reportsByUnitCache.putAll(loaded);
                        } finally {
                            reportsLock.writeLock().unlock();
                        }
                        System.out.println("[OracleDataCache] Загружен кэш отчётов с диска (" + loaded.size() + " записей)");
                    } else {
                        System.out.println("[OracleDataCache] Файл кэша отчётов пуст, удаляем");
                        Files.delete(reportsPath);
                    }
                } else {
                    System.out.println("[OracleDataCache] Файл кэша отчётов повреждён, удаляем: " + reportsPath);
                    Files.delete(reportsPath);
                }
            } catch (IOException e) {
                System.err.println("[OracleDataCache] Ошибка загрузки кэша отчётов: " + e.getMessage());
                // Удаляем повреждённый файл
                try {
                    Files.delete(reportsPath);
                    System.out.println("[OracleDataCache] Повреждённый файл удалён: " + reportsPath);
                } catch (IOException ex) {
                    System.err.println("[OracleDataCache] Не удалось удалить повреждённый файл: " + ex.getMessage());
                }
            } catch (JsonSyntaxException e) {
                System.err.println("[OracleDataCache] JSON синтаксическая ошибка в файле кэша: " + e.getMessage());
                // Удаляем повреждённый файл
                try {
                    Files.delete(reportsPath);
                    System.out.println("[OracleDataCache] Повреждённый JSON файл удалён: " + reportsPath);
                } catch (IOException ex) {
                    System.err.println("[OracleDataCache] Не удалось удалить повреждённый файл: " + ex.getMessage());
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
                        System.out.println("[OracleDataCache] Загружен кэш составных отчётов с диска (" + loaded.size() + " записей)");
                    } else {
                        Files.delete(compositePath);
                    }
                } else {
                    System.out.println("[OracleDataCache] Файл кэша составных отчётов повреждён, удаляем");
                    Files.delete(compositePath);
                }
            } catch (IOException | JsonSyntaxException e) {
                System.err.println("[OracleDataCache] Ошибка загрузки кэша составных отчётов: " + e.getMessage());
                try {
                    Files.delete(compositePath);
                } catch (IOException ex) {
                    System.err.println("[OracleDataCache] Не удалось удалить повреждённый файл: " + ex.getMessage());
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
        // Простая проверка: должен начинаться с { или [ и заканчиваться } или ]
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
    /**
     * Загрузить кэш брокеров с диска
     */
    private void loadBrokersCacheFromDisk() {
        if (cacheDirPath == null) return;

        Path cacheDir = Paths.get(cacheDirPath);
        if (!Files.exists(cacheDir)) {
            return;
        }

        Path brokersPath = cacheDir.resolve(BROKERS_CACHE_FILE);
        if (Files.exists(brokersPath)) {
            try {
                Gson gson = new GsonBuilder().create();
                Type brokersMapType = new TypeToken<Map<String, String>>(){}.getType();
                String json = Files.readString(brokersPath, StandardCharsets.UTF_8);
                if (json != null && !json.trim().isEmpty() && isValidJson(json)) {
                    Map<String, String> loaded = gson.fromJson(json, brokersMapType);
                    if (loaded != null && !loaded.isEmpty()) {
                        brokersLock.writeLock().lock();
                        try {
                            brokerExecProcCache.putAll(loaded);
                        } finally {
                            brokersLock.writeLock().unlock();
                        }
                        System.out.println("[OracleDataCache] Загружен кэш брокеров с диска (" + loaded.size() + " записей)");
                    } else {
                        Files.delete(brokersPath);
                    }
                } else {
                    System.out.println("[OracleDataCache] Файл кэша брокеров повреждён, удаляем");
                    Files.delete(brokersPath);
                }
            } catch (IOException | JsonSyntaxException e) {
                System.err.println("[OracleDataCache] Ошибка загрузки кэша брокеров: " + e.getMessage());
                try {
                    Files.delete(brokersPath);
                } catch (IOException ex) {
                    System.err.println("[OracleDataCache] Не удалось удалить повреждённый файл: " + ex.getMessage());
                }
            }
        }
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
            reportByCodeCache.clear();
        } finally {
            reportsLock.writeLock().unlock();
        }

        brokersLock.writeLock().lock();
        try {
            brokerExecProcCache.clear();
        } finally {
            brokersLock.writeLock().unlock();
        }

        // Удаляем файлы кэша
        if (cacheDirPath != null) {
            try {
                Path cacheDir = Paths.get(cacheDirPath);
                if (Files.exists(cacheDir)) {
                    Files.walk(cacheDir)
                            .filter(Files::isRegularFile)
                            .forEach(file -> {
                                try {
                                    Files.delete(file);
                                } catch (IOException e) {
                                    System.err.println("[OracleDataCache] Ошибка удаления файла: " + file);
                                }
                            });
                    System.out.println("[OracleDataCache] Кэш полностью очищен");
                }
            } catch (IOException e) {
                System.err.println("[OracleDataCache] Ошибка очистки кэша: " + e.getMessage());
            }
        }
    }

    /**
     * Вывести статистику кэша
     */
    public void printStats() {
        System.out.println("=== СТАТИСТИКА КЭША ORACLE DATA ===");
        System.out.println("Отчёты по unit: " + reportsByUnitCache.size());
        System.out.println("Составные отчёты: " + compositeReportsCache.size());
        System.out.println("Отчёты по коду: " + reportByCodeCache.size());
        System.out.println("Брокеры (unit+action): " + brokerExecProcCache.size());
        System.out.println("===================================");
    }
    /**
     * Принудительное синхронное сохранение кэша на диск
     */
    public void saveReportsCacheToDiskSync() {
        saveReportsCacheToDisk();
        saveBrokersCacheToDisk();
    }
}