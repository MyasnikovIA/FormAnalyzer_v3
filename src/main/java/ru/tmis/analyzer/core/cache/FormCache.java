package ru.tmis.analyzer.core.cache;

import ru.tmis.analyzer.config.AppConfig;

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

    private static volatile boolean enabled = true;  // По умолчанию включён

    /**
     * Включить/выключить кэширование форм
     */
    public static void setEnabled(boolean enabled) {
        FormCache.enabled = enabled;
        if (!enabled) {
            clear();  // При выключении очищаем кэш
            System.out.println("[FormCache] Кэширование форм ВЫКЛЮЧЕНО, кэш очищен");
        } else {
            System.out.println("[FormCache] Кэширование форм ВКЛЮЧЕНО");
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Получить содержимое формы из кэша или загрузить с диска
     * @param filePath путь к файлу
     * @param formPath путь к форме (ключ для кэша)
     * @return содержимое формы
     */
    public static String getFormContent(Path filePath, String formPath) {
        // Если кэширование выключено - всегда читаем с диска
        if (!enabled) {
            try {
                if (Files.exists(filePath)) {
                    return Files.readString(filePath);
                }
                return null;
            } catch (Exception e) {
                System.err.println("Ошибка чтения формы " + formPath + ": " + e.getMessage());
                return null;
            }
        }

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
        if (!enabled) return false;
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
        if (!enabled) return 0;
        return formContentCache.size();
    }

    /**
     * Получить использование памяти кэшем в байтах
     */
    public static long getCacheMemoryUsage() {
        if (!enabled) return 0;
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