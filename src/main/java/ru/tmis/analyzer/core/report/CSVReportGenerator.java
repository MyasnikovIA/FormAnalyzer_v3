package ru.tmis.analyzer.core.report;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.db.ReportsFromDbService;
import ru.tmis.analyzer.core.model.BrokerInfo;
import ru.tmis.analyzer.core.model.DbReportInfo;
import ru.tmis.analyzer.core.model.FormInfo;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CSVReportGenerator {

    private final String outputDir;
    private final SettingsModel settings;  // Добавить
    private transient ReportsFromDbService reportsService;

    public CSVReportGenerator(String outputDir) {
        this.outputDir = outputDir;
        this.settings = SettingsModel.getInstance();
        this.reportsService = new ReportsFromDbService(settings);
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
                Set<String> tables = form.getTablesFromViews();
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

                // 9. Универсальные композиции (объединяем оба блока)
                Set<String> allCompositions = new LinkedHashSet<>();
                if (form.getUnitCompositions() != null) {
                    for (String comp : form.getUnitCompositions()) {
                        // Преобразуем формат unit="XXX" composition="YYY" в unit:XXX composition:YYY;
                        String normalized = comp.replace("=\"", ":").replace("\"", "").replace(" composition:", " composition:").replace(",", "");;
                        normalized = normalized.replaceAll(",","");
                        normalized = normalized.replaceAll("\"","");
                        allCompositions.add(normalized);
                    }
                }
                if (form.getJsUnitCompositions() != null) {
                    for (String comp : form.getJsUnitCompositions()) {
                        // Преобразуем формат unit="XXX" composition="YYY" в unit:XXX composition:YYY;
                        String normalized = comp.replace("=\"", ":").replace("\"", "").replace(" composition:", " composition:").replace(",", "");
                        normalized = normalized.replaceAll(",","");
                        normalized = normalized.replaceAll(";","");
                        normalized = normalized.replaceAll("\"","");
                        allCompositions.add(normalized);
                    }
                }
                writeBlock(writer, formName, "Универсальные композиции", allCompositions);

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
                String formattedValue = formatReportWithDbInfo(value);
                writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV(formattedValue));
            }
        }
    }

    private void writeBlock(PrintWriter writer, String formName, String blockName,
                            List<BrokerInfo> brokers) {
        if (brokers == null || brokers.isEmpty()) {
            writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";(не найдено)");
        } else {
            for (BrokerInfo broker : brokers) {
                String value = broker.getDisplayString();
                writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV(value));
            }
        }
    }

    /**
     * Записывает блок UserForms в CSV
     */
    private void writeBlock(PrintWriter writer, String formName, String blockName,
                            List<FormInfo.OverrideInfo> overrides, boolean fullyReplaced, String replacementPath) {

        if (fullyReplaced && replacementPath != null) {
            String relativePath = getRelativePath(replacementPath);
            writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV(relativePath));
        }

        if (overrides == null || overrides.isEmpty()) {
            // Не пишем "(не найдено)" - пропускаем
            return;
        } else {
            Map<String, List<FormInfo.OverrideInfo>> overridesByRegion = new LinkedHashMap<>();
            for (FormInfo.OverrideInfo override : overrides) {
                overridesByRegion.computeIfAbsent(override.getRegionName(), k -> new ArrayList<>()).add(override);
            }

            for (Map.Entry<String, List<FormInfo.OverrideInfo>> entry : overridesByRegion.entrySet()) {
                String region = entry.getKey();
                for (FormInfo.OverrideInfo override : entry.getValue()) {
                    String relativePath = getRelativePath(override.getOverridePath());
                    // Формат: UserFormsRegion\путь\к\файлу.dfrm
                    String value = getRelativePathWithinRegion(relativePath, region);
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
    /**
     * Получить относительный путь от корня проекта
     */
    private String getRelativePath(String absolutePath) {
        if (absolutePath == null || absolutePath.isEmpty()) return "";

        String projectPath = settings.getProjectPath();
        if (projectPath == null || projectPath.isEmpty()) {
            return absolutePath;
        }

        String normalizedProject = projectPath.replace("\\", "/");
        String normalizedPath = absolutePath.replace("\\", "/");

        if (normalizedPath.startsWith(normalizedProject)) {
            String relative = normalizedPath.substring(normalizedProject.length());
            if (relative.startsWith("/")) {
                relative = relative.substring(1);
            }
            return relative;
        }

        return absolutePath;
    }
    /**
     * Получить путь относительно региона UserForms
     */
    private String getRelativePathWithinRegion(String relativePath, String region) {
        if (relativePath == null || region == null) return "";

        String normalizedPath = relativePath.replace("\\", "/");
        String normalizedRegion = region.replace("\\", "/");

        int regionIndex = normalizedPath.indexOf(normalizedRegion);
        if (regionIndex >= 0) {
            String result = normalizedPath.substring(regionIndex);
            return result;
        }

        return relativePath;
    }

    /**
     * Форматирует отчёт с информацией из БД
     */
    private String formatReportWithDbInfo(String report) {
        if (report.contains("/") || report.endsWith(".frm")) {
            return report;
        }

        DbReportInfo dbReport = reportsService.getReportByCode(report);
        if (dbReport != null) {
            String typeName = getRepTypeName(dbReport.getRepType());
            StringBuilder sb = new StringBuilder();
            sb.append(report).append(" (").append(typeName).append(")");

            if (dbReport.getRepType() == 1 && dbReport.getRepFilename() != null && !dbReport.getRepFilename().isEmpty()) {
                String formPath = dbReport.getRepFilename();
                if (!formPath.endsWith(".frm")) formPath = formPath + ".frm";
                if (!formPath.startsWith("Reports/")) formPath = "Reports/" + formPath;
                sb.append(" ").append(formPath);
            }
            return sb.toString();
        }

        return report;
    }

    private String getRepTypeName(int repType) {
        switch (repType) {
            case 0: return "Crystal Reports";
            case 1: return "WEB-форма";
            case 2: return "Crystal Reports(PDF)";
            case 3: return "Бланк";
            case 5: return "WEB-конструктор";
            case 6: return "Составной";
            default: return "Неизвестный тип (" + repType + ")";
        }
    }
}