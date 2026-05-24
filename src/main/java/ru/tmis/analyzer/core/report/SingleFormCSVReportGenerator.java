// core/report/SingleFormCSVReportGenerator.java
package ru.tmis.analyzer.core.report;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.db.ReportsFromDbService;
import ru.tmis.analyzer.core.model.DbReportInfo;
import ru.tmis.analyzer.core.model.FormInfo;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SingleFormCSVReportGenerator {

    private final String outputDir;
    private final SettingsModel settings;
    private transient ReportsFromDbService reportsService;
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);


    public SingleFormCSVReportGenerator(String outputDir) {
        this.outputDir = outputDir;
        this.settings = SettingsModel.getInstance();
        this.reportsService = new ReportsFromDbService(settings);
    }

    /**
     * Сохраняет CSV отчет для одной формы в подкаталоге CSV_reports
     * @param formInfo информация о форме
     * @return путь к созданному файлу
     */
    public Path saveFormCSVReport(FormInfo formInfo) throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        // Создаём подкаталог CSV_reports
        Path csvSubDir = outputPath.resolve("CSV_reports");
        if (!Files.exists(csvSubDir)) {
            Files.createDirectories(csvSubDir);
        }

        String fileName = getSafeFileName(formInfo.getFormPath());
        Path csvPath = csvSubDir.resolve(fileName);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(csvPath))) {
            // Заголовок CSV (как в общем отчете)
            writer.println("ФОРМА;БЛОК;ЗНАЧЕНИЕ");

            String formName = formInfo.getFormPath();

            // 1. Юзерформы
            writeBlock(writer, formName, "Юзерформы", formInfo.getOverrides(),
                    formInfo.isFullyReplaced(), formInfo.getReplacementPath());

            // 2. subForm
            writeBlock(writer, formName, "subForm", formInfo.getSubForms());

            // 3. формы JS
            writeBlock(writer, formName, "формы JS", formInfo.getJsForms());

            // 4. Отчеты, вызываемые на форме
            writeBlock(writer, formName, "Отчеты, вызываемые на форме", formInfo.getReports());

            // 5. Вьюхи (D_V_*)
            Set<String> views = new LinkedHashSet<>();
            for (String tv : formInfo.getTablesViews()) {
                if (tv.startsWith("D_V_")) {
                    views.add(tv);
                }
            }
            writeBlock(writer, formName, "Вьюхи", views);

            // 6. Таблицы
            Set<String> tables = new LinkedHashSet<>(formInfo.getTablesFromViews());
            for (String tv : formInfo.getTablesViews()) {
                if (tv.startsWith("D_") && !tv.startsWith("D_V_")) {
                    tables.add(tv);
                }
            }
            writeBlock(writer, formName, "Таблицы", tables);

            // 7. Пакеты и функции
            writeBlock(writer, formName, "Пакеты и функции", formInfo.getPackagesFunctions());

            // 8. СО (системные опции)
            writeBlock(writer, formName, "СО", formInfo.getSystemOptions());

            // 9. Универсальные композиции
            Set<String> allCompositions = new LinkedHashSet<>();
            if (formInfo.getUnitCompositions() != null) {
                for (String comp : formInfo.getUnitCompositions()) {
                    String normalized = normalizeComposition(comp);
                    allCompositions.add(normalized);
                }
            }
            if (formInfo.getJsUnitCompositions() != null) {
                for (String comp : formInfo.getJsUnitCompositions()) {
                    String normalized = normalizeComposition(comp);
                    allCompositions.add(normalized);
                }
            }
            writeBlock(writer, formName, "Универсальные композиции", allCompositions);

            // 10. Пользовательские процедуры
            writeBlock(writer, formName, "Пользовательские процедуры", formInfo.getUserProcedures());

            // 11. Константы
            writeBlock(writer, formName, "Константы", formInfo.getConstants());

            // 12. Брокеры
            Set<String> allBrokers = new LinkedHashSet<>();
            if (formInfo.getBrokers() != null) {
                for (String comp : formInfo.getBrokers()) {
                    String normalized = comp.replaceAll("[;,]", "").replaceAll("\"", "");
                    allBrokers.add(normalized);
                }
            }
            writeBlock(writer, formName, "Брокеры", allBrokers);

            // 13. Неопределенные
            writeBlock(writer, formName, "Неопределенные", formInfo.getUnknownObjects());
        }

        return csvPath;
    }

    private String normalizeComposition(String comp) {
        if (comp == null) return "";
        return comp.replace("=\"", ":")
                .replace("\"", "")
                .replace(" composition:", " composition:")
                .replace(",", "")
                .replace(";", "");
    }

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
                            List<FormInfo.OverrideInfo> overrides, boolean fullyReplaced, String replacementPath) {

        if (fullyReplaced && replacementPath != null) {
            String relativePath = getRelativePath(replacementPath);
            writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV(relativePath));
        }

        if (overrides == null || overrides.isEmpty()) {
            return;
        }

        Map<String, List<FormInfo.OverrideInfo>> overridesByRegion = new LinkedHashMap<>();
        for (FormInfo.OverrideInfo override : overrides) {
            overridesByRegion.computeIfAbsent(override.getRegionName(), k -> new ArrayList<>()).add(override);
        }

        for (Map.Entry<String, List<FormInfo.OverrideInfo>> entry : overridesByRegion.entrySet()) {
            String region = entry.getKey();
            for (FormInfo.OverrideInfo override : entry.getValue()) {
                String relativePath = getRelativePath(override.getOverridePath());
                String value = getRelativePathWithinRegion(relativePath, region);
                writer.println(escapeCSV(formName) + ";" + escapeCSV(blockName) + ";" + escapeCSV(value));
            }
        }
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

    private String getSafeFileName(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("(sub)_")) {
            normalized = normalized.substring(6);
        }
        String safeName = normalized.replace("/", "#").replace("\\", "#");
        return safeName + ".csv";
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        String escaped = value.replace(";", ",");
        if (escaped.contains("\"")) {
            escaped = escaped.replace("\"", "\"\"");
        }
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
            escaped = "\"" + escaped + "\"";
        }
        return escaped;
    }

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

    private String getRelativePathWithinRegion(String relativePath, String region) {
        if (relativePath == null || region == null) return "";

        String normalizedPath = relativePath.replace("\\", "/");
        String normalizedRegion = region.replace("\\", "/");

        int regionIndex = normalizedPath.indexOf(normalizedRegion);
        if (regionIndex >= 0) {
            return normalizedPath.substring(regionIndex);
        }

        return relativePath;
    }

    public void setStopRequested(boolean stop) {
        this.stopRequested.set(stop);
    }

    public boolean isStopRequested() {
        return stopRequested.get();
    }

    private String formatReportWithDbInfo(String report) {
        // Проверка на остановку
        if (stopRequested.get()) {
            return report;
        }

        if (report.contains("/") || report.endsWith(".frm")) {
            return report;
        }

        try {
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
        } catch (Exception e) {
            // При остановке не выводим ошибку
            if (!stopRequested.get()) {
                System.err.println("[SingleFormCSV] Ошибка получения информации об отчёте " + report + ": " + e.getMessage());
            }
        }

        return report;
    }
}