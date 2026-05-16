// utils/FormPathUtils.java
package ru.tmis.analyzer.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FormPathUtils {

    /**
     * Нормализация пути формы для отображения в отчете
     */
    public static String normalizeFormPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }

        String normalized = path.trim();

        // Убираем ведущий слеш
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        // Убираем префикс Forms/ если есть
        if (normalized.startsWith("Forms/")) {
            normalized = normalized.substring(6);
        }
        // Добавляем .frm если нет расширения
        if (!normalized.endsWith(".frm") && !normalized.endsWith(".dfrm")) {
            normalized = normalized + ".frm";
        }

        return "/Forms/" + normalized;
    }

    /**
     * Получение физического пути к файлу формы
     */
    public static Path getPhysicalPath(String projectPath, String formPath) {
        String normalized = normalizeFormPathForFs(formPath);
        return Paths.get(projectPath, normalized);
    }

    /**
     * Нормализация пути для файловой системы
     */
    public static String normalizeFormPathForFs(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }

        String normalized = path.trim();

        // Убираем ведущий слеш
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        // Убираем префикс Forms/ если есть
        if (normalized.startsWith("Forms/")) {
            normalized = normalized.substring(6);
        } else if (normalized.startsWith("Forms")) {
            normalized = normalized.substring(5);
        }
        // Добавляем .frm если нет расширения
        if (!normalized.endsWith(".frm") && !normalized.endsWith(".dfrm")) {
            normalized = normalized + ".frm";
        }

        return "Forms" + java.io.File.separator + normalized;
    }

    public static String getRelativePath(String projectRoot, String absolutePath) {
        Path root = Paths.get(projectRoot);
        Path abs = Paths.get(absolutePath);

        try {
            return root.relativize(abs).toString().replace("\\", "/");
        } catch (IllegalArgumentException e) {
            return absolutePath;
        }
    }
}