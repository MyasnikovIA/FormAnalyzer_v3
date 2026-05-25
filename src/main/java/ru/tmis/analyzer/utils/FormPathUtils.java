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

        // Заменяем обратные слеши на прямые для единообразия (внутреннее представление)
        normalized = normalized.replace("\\", "/");

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
     * Получение физического пути к файлу формы (с учётом OS)
     */
    public static Path getPhysicalPath(String projectPath, String formPath) {
        String normalized = normalizeFormPathForFs(formPath);
        return Paths.get(projectPath, normalized);
    }

    /**
     * Нормализация пути для файловой системы (с учётом OS)
     */
    public static String normalizeFormPathForFs(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }

        String normalized = path.trim();

        // Заменяем обратные слеши на прямые для единообразия
        normalized = normalized.replace("\\", "/");

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

        // Используем File.separator для разделителя в зависимости от ОС
        return normalized.replace("/", File.separator);
    }

    /**
     * Получение относительного пути от корня проекта
     */
    public static String getRelativePath(String projectRoot, String absolutePath) {
        Path root = Paths.get(projectRoot);
        Path abs = Paths.get(absolutePath);

        try {
            return root.relativize(abs).toString().replace("\\", "/");
        } catch (IllegalArgumentException e) {
            return absolutePath;
        }
    }

    /**
     * Конвертирует путь из формата приложения в системный формат
     * @param appPath путь в формате приложения (с /)
     * @return путь в формате текущей ОС
     */
    public static String toSystemPath(String appPath) {
        if (appPath == null) return null;
        return appPath.replace("/", File.separator);
    }

    /**
     * Конвертирует путь из системного формата в формат приложения (с /)
     * @param systemPath путь в формате текущей ОС
     * @return путь в формате приложения
     */
    public static String toAppPath(String systemPath) {
        if (systemPath == null) return null;
        return systemPath.replace("\\", "/");
    }
}