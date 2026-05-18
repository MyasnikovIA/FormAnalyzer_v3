// utils/FormPathUtils.java
package ru.tmis.analyzer.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FormPathUtils {

    /**
     * Нормализация пути формы для отображения в отчете
     * Поддерживает:
     * - /Forms/Path/To/Form.frm
     * - UserFormsXXX/Path/To/Form.frm
     * - UserFormsXXX/Path/To/Form.dfrm
     * - UserFormsXXX/Path/To/Form.d/something.dfrm
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

        // Проверяем, является ли это UserForms
        boolean isUserForm = normalized.matches("^UserForms[A-Za-z0-9_]*/.*");

        if (!isUserForm) {
            // Обычная форма - добавляем префикс Forms/ если нужно
            if (!normalized.startsWith("Forms/")) {
                normalized = "Forms/" + normalized;
            }
            // Добавляем .frm если нет расширения
            if (!normalized.endsWith(".frm") && !normalized.endsWith(".dfrm")) {
                normalized = normalized + ".frm";
            }
        } else {
            // UserForms - просто убеждаемся, что есть расширение
            if (!normalized.endsWith(".frm") && !normalized.endsWith(".dfrm") && !normalized.contains(".d/")) {
                normalized = normalized + ".frm";
            }
        }

        return normalized;
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

        // Проверяем, является ли это UserForms
        boolean isUserForm = normalized.matches("^UserForms[A-Za-z0-9_]*/.*");

        if (!isUserForm) {
            // Обычная форма
            if (!normalized.startsWith("Forms/")) {
                normalized = "Forms/" + normalized;
            }
            if (!normalized.endsWith(".frm") && !normalized.endsWith(".dfrm")) {
                normalized = normalized + ".frm";
            }
        } else {
            // UserForms - не добавляем префикс Forms
            if (!normalized.endsWith(".frm") && !normalized.endsWith(".dfrm") && !normalized.contains(".d/")) {
                normalized = normalized + ".frm";
            }
        }

        return normalized.replace("/", File.separator);
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