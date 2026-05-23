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
    private static final Map<String, String> formContentCache = new ConcurrentHashMap<>(25000);
    private static final Map<String, Boolean> formExistsCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> formLastModifiedCache = new ConcurrentHashMap<>();

    private static volatile boolean enabled = true;

    /**
     * Включить/выключить кэширование форм
     */
    public static void setEnabled(boolean enabled) {
        FormCache.enabled = enabled;
        if (!enabled) {
            clear();
            System.out.println("[FormCache] Кэширование форм ВЫКЛЮЧЕНО, кэш очищен");
        } else {
            System.out.println("[FormCache] Кэширование форм ВКЛЮЧЕНО");
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Получить содержимое формы - ТОЛЬКО ИЗ ПАМЯТИ!
     */
    public static String getFormContent(String formPath) {
        if (!enabled) {
            return null;
        }
        return formContentCache.get(formPath);
    }

    /**
     * Загрузить форму в память (вызывается только при предзагрузке)
     */
    public static void putFormContent(String formPath, String content) {
        if (enabled && content != null) {
            formContentCache.put(formPath, content);
            formExistsCache.put(formPath, true);
        }
    }

    /**
     * Получить содержимое формы с поддержкой физического пути (для обратной совместимости)
     * @deprecated Используйте getFormContent(String formPath)
     */
    @Deprecated
    public static String getFormContent(Path filePath, String formPath) {
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

        // Сначала проверяем память
        String cached = formContentCache.get(formPath);
        if (cached != null) {
            return cached;
        }

        // Если нет в памяти, читаем с диска
        try {
            if (Files.exists(filePath)) {
                String content = Files.readString(filePath);
                formContentCache.put(formPath, content);
                formExistsCache.put(formPath, true);
                return content;
            }
            formExistsCache.put(formPath, false);
        } catch (Exception e) {
            System.err.println("Ошибка чтения формы " + formPath + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Проверить существование формы в кэше
     */
    public static boolean formExists(String formPath) {
        if (!enabled) return false;
        return formExistsCache.getOrDefault(formPath, false);
    }

    /**
     * Проверить, есть ли форма в кэше
     */
    public static boolean contains(String formPath) {
        return formContentCache.containsKey(formPath);
    }

    /**
     * Получить количество форм в кэше
     */
    public static int size() {
        if (!enabled) return 0;
        return formContentCache.size();
    }

    /**
     * Получить количество форм в кэше (алиас для size)
     */
    public static int getCachedFormsCount() {
        return size();
    }

    /**
     * Получить использование памяти кэшем в байтах
     */
    public static long getMemoryUsageBytes() {
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
        return getMemoryUsageBytes() / (1024 * 1024);
    }

    /**
     * Очистить кэш
     */
    public static void clear() {
        formContentCache.clear();
        formExistsCache.clear();
        formLastModifiedCache.clear();
    }
}