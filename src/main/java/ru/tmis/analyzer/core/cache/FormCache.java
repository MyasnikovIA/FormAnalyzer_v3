package ru.tmis.analyzer.core.cache;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Кэш для хранения содержимого форм в оперативной памяти
 */
public class FormCache {
    private static final Map<String, String> formContentCache = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> formExistsCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> formLastModifiedCache = new ConcurrentHashMap<>();

    /**
     * Получить содержимое формы из кэша или загрузить с диска
     * @param filePath путь к файлу
     * @param formPath путь к форме (ключ для кэша)
     * @return содержимое формы
     */
    public static String getFormContent(Path filePath, String formPath) {
        String key = formPath;

        // Проверяем кэш
        if (formContentCache.containsKey(key)) {
            // Проверяем, не изменился ли файл на диске
            try {
                long lastModified = Files.getLastModifiedTime(filePath).toMillis();
                Long cachedModified = formLastModifiedCache.get(key);
                if (cachedModified != null && cachedModified == lastModified) {
                    return formContentCache.get(key); // Кэш актуален
                }
            } catch (Exception e) {
                // Игнорируем ошибки
            }
        }

        // Загружаем с диска
        try {
            if (Files.exists(filePath)) {
                String content = Files.readString(filePath);
                formContentCache.put(key, content);
                formExistsCache.put(key, true);
                formLastModifiedCache.put(key, Files.getLastModifiedTime(filePath).toMillis());
                return content;
            }
            formExistsCache.put(key, false);
        } catch (Exception e) {
            System.err.println("Ошибка чтения формы " + formPath + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Проверить существование формы
     */
    public static boolean formExists(String formPath) {
        return formExistsCache.getOrDefault(formPath, false);
    }

    /**
     * Очистить кэш
     */
    public static void clear() {
        formContentCache.clear();
        formExistsCache.clear();
        formLastModifiedCache.clear();
    }

    /**
     * Получить количество форм в кэше
     */
    public static int getCachedFormsCount() {
        return formContentCache.size();
    }

    /**
     * Получить использование памяти кэшем в байтах
     */
    public static long getCacheMemoryUsage() {
        long total = 0;
        for (String content : formContentCache.values()) {
            if (content != null) {
                total += content.getBytes().length;
            }
        }
        return total;
    }

    /**
     * Получить использование памяти в МБ
     */
    public static long getCacheMemoryUsageMB() {
        return getCacheMemoryUsage() / (1024 * 1024);
    }
}