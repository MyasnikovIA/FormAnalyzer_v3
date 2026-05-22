package ru.tmis.analyzer.core.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReportBuffer {

    // Хранилище TXT отчётов в памяти
    private static final Map<String, String> txtBuffer = new ConcurrentHashMap<>();

    // Хранилище CSV строк
    private static final List<String> csvLines = Collections.synchronizedList(new ArrayList<>());

    // Хранилище JSON объектов
    private static final List<JsonObject> jsonObjects = Collections.synchronizedList(new ArrayList<>());

    // Хранилище MD файлов
    private static final Map<String, String> mdBuffer = new ConcurrentHashMap<>();

    private static int totalForms = 0;

    public static void addTxtReport(String formPath, String content) {
        txtBuffer.put(formPath, content);
        totalForms++;
    }

    public static void addCsvLine(String line) {
        csvLines.add(line);
    }

    public static void addJsonObject(JsonObject obj) {
        jsonObjects.add(obj);
    }

    public static void addMdPrompt(String formPath, String content) {
        mdBuffer.put(formPath, content);
    }

    public static int getTotalForms() {
        return totalForms;
    }

    public static void clear() {
        txtBuffer.clear();
        csvLines.clear();
        jsonObjects.clear();
        mdBuffer.clear();
        totalForms = 0;
    }

    public static void flushToDisk(String outputDir) throws IOException {
        System.out.println("[Buffer] Выгрузка на диск...");
        long startTime = System.currentTimeMillis();

        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        // 1. Сохраняем TXT отчёты
        Path formsDir = outputPath.resolve("Forms");
        if (!Files.exists(formsDir)) {
            Files.createDirectories(formsDir);
        }

        int txtCount = 0;
        for (Map.Entry<String, String> entry : txtBuffer.entrySet()) {
            String fileName = getSafeFileName(entry.getKey());
            Path filePath = formsDir.resolve(fileName);
            Files.writeString(filePath, entry.getValue(), StandardCharsets.UTF_8);
            txtCount++;
            if (txtCount % 100 == 0) {
                System.out.println("  Сохранено TXT: " + txtCount);
            }
        }
        System.out.println("  Сохранено TXT: " + txtCount);

        // 2. Сохраняем CSV
        if (!csvLines.isEmpty()) {
            Path csvPath = outputPath.resolve("forms_export.csv");
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(csvPath))) {
                writer.println("ФОРМА;БЛОК;ЗНАЧЕНИЕ");
                for (String line : csvLines) {
                    writer.println(line);
                }
            }
            System.out.println("  Сохранён CSV: " + csvLines.size() + " строк");
        }

        // 3. Сохраняем JSON
        if (!jsonObjects.isEmpty()) {
            Path jsonPath = outputPath.resolve("forms_export.json");
            JsonArray formsArray = new JsonArray();
            for (JsonObject obj : jsonObjects) {
                formsArray.add(obj);
            }
            JsonObject root = new JsonObject();
            root.add("forms", formsArray);
            root.addProperty("totalForms", formsArray.size());
            root.addProperty("exportDate", new Date().toString());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(jsonPath, gson.toJson(root), StandardCharsets.UTF_8);
            System.out.println("  Сохранён JSON: " + jsonObjects.size() + " форм");
        }

        // 4. Сохраняем MD файлы
        if (!mdBuffer.isEmpty()) {
            int mdCount = 0;
            for (Map.Entry<String, String> entry : mdBuffer.entrySet()) {
                String fileName = getSafeFileNameForMd(entry.getKey());
                Path filePath = formsDir.resolve(fileName);
                Files.writeString(filePath, entry.getValue(), StandardCharsets.UTF_8);
                mdCount++;
            }
            System.out.println("  Сохранено MD: " + mdCount);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("[Buffer] Выгрузка завершена за " + elapsed + " мс");
        System.out.println("  Всего форм: " + totalForms);
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