package ru.tmis.analyzer.core.report;

import ru.tmis.analyzer.core.model.FormInfo;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CSVReportGenerator {

    private final String outputDir;

    public CSVReportGenerator(String outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Генерирует CSV отчет по всем формам
     * @param forms список всех форм
     * @return путь к созданному файлу
     */
    public Path generateCSVReport(List<FormInfo> forms) throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        Path csvPath = outputPath.resolve("forms_export.csv");

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(csvPath))) {
            // Заголовок CSV
            writer.println("ФОРМА;БЛОК;ЗНАЧЕНИЕ");

            for (FormInfo form : forms) {
                String formName = form.getFormPath();

                // 1. Юзерформы
                writeBlock(writer, formName, "Юзерформы", form.getOverrides(), form.isFullyReplaced(), form.getReplacementPath());

                // 2. SubForm
                writeBlock(writer, formName, "subForm", form.getSubForms());

                // 3. формы JS
                writeBlock(writer, formName, "формы JS", form.getJsForms());

                // 4. Отчеты, вызываемые на форме
                writeBlock(writer, formName, "Отчеты, вызываемые на форме", form.getReports());

                // 5. Вьюхи (D_V_*)
                Set<String> views = new LinkedHashSet<>();
                for (String tv : form.getTablesViews()) {
                    if (tv.startsWith("D_V_")) {
                        views.add(tv);
                    }
                }
                writeBlock(writer, formName, "Вьюхи", views);

                // 6. Таблицы (D_* не начинающиеся с D_V_)
                Set<String> tables = new LinkedHashSet<>();
                for (String tv : form.getTablesViews()) {
                    if (tv.startsWith("D_") && !tv.startsWith("D_V_")) {
                        tables.add(tv);
                    }
                }
                writeBlock(writer, formName, "Таблицы", tables);

                // 7. Пакеты и функции
                writeBlock(writer, formName, "Пакеты и функции", form.getPackagesFunctions());

                // 8. СО (системные опции)
                writeBlock(writer, formName, "СО", form.getSystemOptions());

                // 9. Универсальные композиции
                writeBlock(writer, formName, "Универсальные композиции", form.getJsUnitCompositions());

                // 10. Пользовательские процедуры
                writeBlock(writer, formName, "Пользовательские процедуры", form.getUserProcedures());

                // 11. Константы
                writeBlock(writer, formName, "Константы", form.getConstants());

                // 12. Брокеры
                writeBlock(writer, formName, "Брокеры", form.getBrokers());

                // 13. Неопределенные (РАЗОБРАТЬ АНАЛИТИКОМ)
                writeBlock(writer, formName, "Неопределенные", form.getUnknownObjects());
            }
        }

        System.out.println("CSV отчет сохранен: " + csvPath);
        return csvPath;
    }

    /**
     * Записывает блок в CSV
     */
    private void writeBlock(PrintWriter writer, String formName, String blockName, Set<String> values) {
        if (values == null || values.isEmpty()) {
            writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";(не найдено)");
        } else {
            for (String value : values) {
                writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV(value));
            }
        }
    }


    /**
     * Записывает блок UserForms (особый случай, так как там сложная структура)
     */
    private void writeBlock(PrintWriter writer, String formName, String blockName,
                            List<FormInfo.OverrideInfo> overrides, boolean fullyReplaced, String replacementPath) {

        if (fullyReplaced && replacementPath != null) {
            // writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV("ПОЛНАЯ ЗАМЕНА: " + replacementPath));
            writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV( replacementPath));
        }

        if (overrides == null || overrides.isEmpty()) {
            writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";(не найдено)");
        } else {
            Map<String, List<FormInfo.OverrideInfo>> overridesByRegion = new LinkedHashMap<>();
            for (FormInfo.OverrideInfo override : overrides) {
                overridesByRegion.computeIfAbsent(override.getRegionName(), k -> new ArrayList<>()).add(override);
            }

            for (Map.Entry<String, List<FormInfo.OverrideInfo>> entry : overridesByRegion.entrySet()) {
                String region = entry.getKey();
                for (FormInfo.OverrideInfo override : entry.getValue()) {
                    // String value = region + ": " + override.getType().getDescription() + " - " + override.getOverridePath();
                    String value = override.getType().getDescription() + " - " + override.getOverridePath();
                    writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV(value));
                }
            }
        }
    }
    /**
     * Экранирование CSV (замена кавычек и точек с запятой)
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        // Заменяем точку с запятой на запятую для корректного CSV
        String escaped = value.replace(";", ",");
        // Если есть кавычки, экранируем
        if (escaped.contains("\"")) {
            escaped = escaped.replace("\"", "\"\"");
        }
        // Если есть специальные символы, оборачиваем в кавычки
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
            escaped = "\"" + escaped + "\"";
        }
        return escaped;
    }
}