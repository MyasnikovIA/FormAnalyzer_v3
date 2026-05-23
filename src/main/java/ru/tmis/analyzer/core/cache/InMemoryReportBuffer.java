// core/cache/InMemoryReportBuffer.java
package ru.tmis.analyzer.core.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryReportBuffer {

    // Хранилища в памяти
    private static final Map<String, String> txtBuffer = new ConcurrentHashMap<>();
    private static final List<String> csvLines = Collections.synchronizedList(new ArrayList<>());
    private static final List<JsonObject> jsonObjects = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, String> mdBuffer = new ConcurrentHashMap<>();

    private static final AtomicInteger totalForms = new AtomicInteger(0);

    // ========== НАСТРОЙКИ ПЕРИОДИЧЕСКОЙ ЗАПИСИ ==========
    private static final int FLUSH_INTERVAL_SECONDS = 10;      // Сбрасывать каждые 10 секунд
    private static final int FLUSH_BATCH_SIZE = 500;           // Сбрасывать при накоплении 500 отчётов
    private static final int CSV_BATCH_SIZE = 1000;            // Сбрасывать CSV при 1000 строк

    private static ScheduledExecutorService scheduler;
    private static volatile boolean running = true;
    private static String outputDir;

    // Статистика
    private static final AtomicInteger txtFlushCount = new AtomicInteger(0);
    private static final AtomicInteger csvFlushCount = new AtomicInteger(0);
    private static final AtomicInteger jsonFlushCount = new AtomicInteger(0);

    /**
     * Инициализация буфера с фоновой периодической записью
     */
    public static void init(String outputDirectory) {
        outputDir = outputDirectory;
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                if (running) {
                    flushAllIfNeeded();
                }
            }, FLUSH_INTERVAL_SECONDS, FLUSH_INTERVAL_SECONDS, TimeUnit.SECONDS);
            System.out.println("[Buffer] Фоновая запись запущена (интервал=" + FLUSH_INTERVAL_SECONDS + "с, " +
                    "порог TXT=" + FLUSH_BATCH_SIZE + ", порог CSV=" + CSV_BATCH_SIZE + ")");
        }
    }

    /**
     * Проверка и периодический сброс
     */
    private static void flushAllIfNeeded() {
        boolean needFlush = false;

        if (txtBuffer.size() >= FLUSH_BATCH_SIZE) {
            System.out.println("[Buffer] Достигнут порог TXT буфера (" + txtBuffer.size() + "), сброс...");
            flushTxt();
            needFlush = true;
        }

        if (csvLines.size() >= CSV_BATCH_SIZE) {
            System.out.println("[Buffer] Достигнут порог CSV буфера (" + csvLines.size() + "), сброс...");
            flushCsv();
            needFlush = true;
        }

        if (jsonObjects.size() >= FLUSH_BATCH_SIZE) {
            System.out.println("[Buffer] Достигнут порог JSON буфера (" + jsonObjects.size() + "), сброс...");
            flushJson();
            needFlush = true;
        }

        if (!needFlush && (txtBuffer.size() > 0 || csvLines.size() > 0 || jsonObjects.size() > 0)) {
            // Если есть данные, но порог не достигнут, всё равно сбрасываем раз в минуту
            System.out.println("[Buffer] Периодический сброс (TXT=" + txtBuffer.size() +
                    ", CSV=" + csvLines.size() + ", JSON=" + jsonObjects.size() + ")");
            flushTxt();
            flushCsv();
            flushJson();
        }
    }

    /**
     * Добавление TXT отчёта
     */
    public static void addTxtReport(String formPath, String content) {
        txtBuffer.put(formPath, content);
        totalForms.incrementAndGet();
    }

    /**
     * Добавление CSV строки
     */
    public static void addCsvLine(String line) {
        csvLines.add(line);
    }

    /**
     * Добавление JSON объекта
     */
    public static void addJsonObject(JsonObject obj) {
        jsonObjects.add(obj);
    }

    /**
     * Добавление MD промпта
     */
    public static void addMdPrompt(String formPath, String content) {
        mdBuffer.put(formPath, content);
    }

    /**
     * Сброс TXT буфера на диск
     */
    public static void flushTxt() {
        if (txtBuffer.isEmpty()) return;

        try {
            Path formsDir = Paths.get(outputDir, "Forms");
            if (!Files.exists(formsDir)) {
                Files.createDirectories(formsDir);
            }

            int count = 0;
            for (Map.Entry<String, String> entry : txtBuffer.entrySet()) {
                String fileName = getSafeFileName(entry.getKey());
                Path filePath = formsDir.resolve(fileName);
                Files.writeString(filePath, entry.getValue(), StandardCharsets.UTF_8);
                count++;
            }

            txtFlushCount.incrementAndGet();
            System.out.println("[Buffer] TXT сброшен: " + count + " файлов (всего сбросов: " + txtFlushCount.get() + ")");
            txtBuffer.clear();
        } catch (IOException e) {
            System.err.println("[Buffer] Ошибка сброса TXT: " + e.getMessage());
        }
    }

    /**
     * Сброс CSV буфера на диск
     */
    public static void flushCsv() {
        if (csvLines.isEmpty()) return;

        try {
            Path outputPath = Paths.get(outputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }

            Path csvPath = outputPath.resolve("forms_export.csv");
            boolean exists = Files.exists(csvPath);

            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(csvPath.toFile(), true)))) {
                if (!exists) {
                    writer.println("ФОРМА;БЛОК;ЗНАЧЕНИЕ");
                }
                for (String line : csvLines) {
                    writer.println(line);
                }
                writer.flush();
            }

            csvFlushCount.incrementAndGet();
            System.out.println("[Buffer] CSV сброшен: " + csvLines.size() + " строк (всего сбросов: " + csvFlushCount.get() + ")");
            csvLines.clear();
        } catch (IOException e) {
            System.err.println("[Buffer] Ошибка сброса CSV: " + e.getMessage());
        }
    }

    /**
     * Сброс JSON буфера на диск
     */
    public static void flushJson() {
        if (jsonObjects.isEmpty()) return;

        try {
            Path outputPath = Paths.get(outputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }

            Path jsonPath = outputPath.resolve("forms_export.json");
            JsonArray existingForms = new JsonArray();

            // Загружаем существующие
            if (Files.exists(jsonPath)) {
                String content = Files.readString(jsonPath);
                if (!content.isEmpty()) {
                    try {
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        JsonObject root = gson.fromJson(content, JsonObject.class);
                        if (root.has("forms")) {
                            existingForms = root.getAsJsonArray("forms");
                        }
                    } catch (Exception e) {
                        // Игнорируем
                    }
                }
            }

            // Добавляем новые
            for (JsonObject obj : jsonObjects) {
                existingForms.add(obj);
            }

            // Сохраняем
            JsonObject root = new JsonObject();
            root.add("forms", existingForms);
            root.addProperty("totalForms", existingForms.size());
            root.addProperty("exportDate", new Date().toString());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(jsonPath, gson.toJson(root), StandardCharsets.UTF_8);

            jsonFlushCount.incrementAndGet();
            System.out.println("[Buffer] JSON сброшен: " + jsonObjects.size() + " объектов (всего сбросов: " + jsonFlushCount.get() + ")");
            jsonObjects.clear();
        } catch (IOException e) {
            System.err.println("[Buffer] Ошибка сброса JSON: " + e.getMessage());
        }
    }

    /**
     * Сброс MD буфера на диск
     */
    public static void flushMd() {
        if (mdBuffer.isEmpty()) return;

        try {
            Path formsDir = Paths.get(outputDir, "Forms");
            if (!Files.exists(formsDir)) {
                Files.createDirectories(formsDir);
            }

            int count = 0;
            for (Map.Entry<String, String> entry : mdBuffer.entrySet()) {
                String fileName = getSafeFileNameForMd(entry.getKey());
                Path filePath = formsDir.resolve(fileName);
                Files.writeString(filePath, entry.getValue(), StandardCharsets.UTF_8);
                count++;
            }

            System.out.println("[Buffer] MD сброшен: " + count + " файлов");
            mdBuffer.clear();
        } catch (IOException e) {
            System.err.println("[Buffer] Ошибка сброса MD: " + e.getMessage());
        }
    }

    /**
     * Полный сброс всех буферов (вызывается при завершении)
     */
    public static void flushToDisk(String outputDirParam) {
        if (outputDir == null) {
            outputDir = outputDirParam;
        }

        System.out.println("[Buffer] Финальный сброс всех буферов...");
        running = false;

        if (scheduler != null) {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }

        flushTxt();
        flushCsv();
        flushJson();
        flushMd();

        System.out.println("[Buffer] Финальный сброс завершён. Всего форм: " + totalForms.get());
        System.out.println("[Buffer] Статистика: TXT сбросов=" + txtFlushCount.get() +
                ", CSV сбросов=" + csvFlushCount.get() +
                ", JSON сбросов=" + jsonFlushCount.get());
    }

    /**
     * Очистка буферов
     */
    public static void clear() {
        txtBuffer.clear();
        csvLines.clear();
        jsonObjects.clear();
        mdBuffer.clear();
        totalForms.set(0);
    }

    public static int getTotalForms() {
        return totalForms.get();
    }

    public static int getTxtBufferSize() {
        return txtBuffer.size();
    }

    public static int getCsvBufferSize() {
        return csvLines.size();
    }

    public static int getJsonBufferSize() {
        return jsonObjects.size();
    }

    private static String getSafeFileName(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) normalized = normalized.substring(1);
        if (normalized.startsWith("(sub)_")) normalized = normalized.substring(6);
        if (normalized.startsWith("Forms/")) normalized = normalized.substring(6);
        if (normalized.endsWith(".frm")) normalized = normalized.substring(0, normalized.length() - 4);
        if (normalized.endsWith(".dfrm")) normalized = normalized.substring(0, normalized.length() - 5);
        return normalized.replace("/", "#").replace("\\", "#") + ".txt";
    }

    private static String getSafeFileNameForMd(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) normalized = normalized.substring(1);
        if (normalized.startsWith("(sub)_")) normalized = normalized.substring(6);
        if (normalized.startsWith("Forms/")) normalized = normalized.substring(6);
        if (normalized.endsWith(".frm")) normalized = normalized.substring(0, normalized.length() - 4);
        if (normalized.endsWith(".dfrm")) normalized = normalized.substring(0, normalized.length() - 5);
        return normalized.replace("/", "#").replace("\\", "#") + ".md";
    }
}