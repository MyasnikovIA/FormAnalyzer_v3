// core/report/CSVReportGenerator.java
package ru.tmis.analyzer.core.report;

import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.PopupMenuInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;

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

                // 1. ЮЗЕРФОРМЫ
                writeBlock(writer, formName, "ЮЗЕРФОРМЫ", form.getOverrides(), form.isFullyReplaced(), form.getReplacementPath());

                // 2. SubForm
                writeBlock(writer, formName, "SubForm", form.getSubForms());

                // 3. Список вызываемых форм в JS
                writeBlock(writer, formName, "Список вызываемых форм в JS", form.getJsForms());

                // 4. ИСПОЛЬЗУЕМЫЕ ТАБЛИЦЫ И ВЬЮХИ
                writeBlock(writer, formName, "ИСПОЛЬЗУЕМЫЕ ТАБЛИЦЫ И ВЬЮХИ", form.getTablesViews());

                // 5. ТАБЛИЦЫ, ИСПОЛЬЗУЕМЫЕ ЧЕРЕЗ ВЬЮХИ (уникальные для этой формы)
                Set<String> viewTables = getViewTables(form);
                writeBlock(writer, formName, "ТАБЛИЦЫ, ИСПОЛЬЗУЕМЫЕ ЧЕРЕЗ ВЬЮХИ (уникальные для этой формы)", viewTables);

                // 6. ИСПОЛЬЗУЕМЫЕ ПАКЕТЫ И ФУНКЦИИ
                writeBlock(writer, formName, "ИСПОЛЬЗУЕМЫЕ ПАКЕТЫ И ФУНКЦИИ", form.getPackagesFunctions());

                // 7. СИСТЕМНЫЕ ОПЦИИ
                writeBlock(writer, formName, "СИСТЕМНЫЕ ОПЦИИ", form.getSystemOptions());

                // 8. КОНСТАНТЫ
                writeBlock(writer, formName, "КОНСТАНТЫ", form.getConstants());

                // 9. ПОЛЬЗОВАТЕЛЬСКИЕ ПРОЦЕДУРЫ
                writeBlock(writer, formName, "ПОЛЬЗОВАТЕЛЬСКИЕ ПРОЦЕДУРЫ", form.getUserProcedures());

                // 10. КОДЫ ПОДКЛЮЧАЕМОГО AUTOPOPUP МЕНЮ
                writeBlock(writer, formName, "Коды подключаемого AutoPopUp меню на форме", form.getAutoPopupMenus());

                // 11. БРОКЕРЫ
                writeBlock(writer, formName, "БРОКЕРЫ", form.getBrokers());

                // 12. КОМПОЗИЦИИ
                writeBlock(writer, formName, "КОМПОЗИЦИИ UnitEdit на форме", form.getUnitCompositions());

                // 13. JS UNIT COMPOSITIONS
                writeBlock(writer, formName, "JS Unit Compositions", form.getJsUnitCompositions());

                // 14. РАЗОБРАТЬ АНАЛИТИКОМ
                writeBlock(writer, formName, "РАЗОБРАТЬ АНАЛИТИКОМ", form.getUnknownObjects());

                // 15. ОТЧЕТЫ
                writeBlock(writer, formName, "ОТЧЕТЫ", form.getReports());
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
            writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV("ПОЛНАЯ ЗАМЕНА: " + replacementPath));
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
                    String value = region + ": " + override.getType().getDescription() + " - " + override.getOverridePath();
                    writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV(value));
                }
            }
        }
    }
    /**
     * Получает все таблицы из вьюх
     */
    private Set<String> getViewTables(FormInfo formInfo) {
        Set<String> viewTables = new LinkedHashSet<>();
        if (formInfo.getViewDependencies() != null) {
            for (Map.Entry<String, ViewTableDependencies> entry : formInfo.getViewDependencies().entrySet()) {
                viewTables.addAll(entry.getValue().getOracleTables());
            }
        }
        return viewTables;
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