package ru.tmis.analyzer.core.report;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.db.ReportsFromDbService;
import ru.tmis.analyzer.core.model.DbReportInfo;
import ru.tmis.analyzer.core.model.FormInfo;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CSVReportGenerator {

    private final String outputDir;
    private final SettingsModel settings;  // Добавить
    private transient ReportsFromDbService reportsService;

    private final List<String> csvBuffer = Collections.synchronizedList(new ArrayList<>());
    private static final int CSV_BATCH_SIZE = 500;


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
                Set<String> allBrokers = new LinkedHashSet<>();
                if (form.getBrokers() != null) {
                    for (String comp : form.getBrokers()) {
                        String normalized = comp.replaceAll(";","").replaceAll(",","").replaceAll("\"","");
                        allBrokers.add(normalized);
                    }
                }
                writeBlock(writer, formName, "Брокеры", allBrokers);

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
    /**
     * Добавляет данные формы в CSV буфер (пакетная запись)
     */
    public void appendFormToCSVBatch(FormInfo formInfo) {
        String formName = formInfo.getFormPath();

        // SubForm
        for (String subForm : formInfo.getSubForms()) {
            csvBuffer.add(escapeCSV(formName) + ";subForm;" + escapeCSV(subForm));
        }

        // формы JS
        for (String jsForm : formInfo.getJsForms()) {
            csvBuffer.add(escapeCSV(formName) + ";формы JS;" + escapeCSV(jsForm));
        }

        // Вьюхи
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_V_")) {
                csvBuffer.add(escapeCSV(formName) + ";Вьюхи;" + escapeCSV(tv));
            }
        }

        // Таблицы
        Set<String> tables = formInfo.getTablesFromViews();
        if (tables != null) {
            for (String table : tables) {
                csvBuffer.add(escapeCSV(formName) + ";Таблицы;" + escapeCSV(table));
            }
        }

        // Пакеты и функции
        for (String pf : formInfo.getPackagesFunctions()) {
            csvBuffer.add(escapeCSV(formName) + ";Пакеты и функции;" + escapeCSV(pf));
        }

        // Системные опции
        for (String opt : formInfo.getSystemOptions()) {
            csvBuffer.add(escapeCSV(formName) + ";СО;" + escapeCSV(opt));
        }

        // Брокеры
        for (String broker : formInfo.getBrokers()) {
            String normalized = broker.replaceAll(";", "").replaceAll(",", "").replaceAll("\"", "");
            csvBuffer.add(escapeCSV(formName) + ";Брокеры;" + escapeCSV(normalized));
        }

        // Периодически сбрасываем буфер на диск
        if (csvBuffer.size() >= CSV_BATCH_SIZE) {
            try {
                flushCSV();
            } catch (IOException e) {
                System.err.println("Ошибка сброса CSV буфера: " + e.getMessage());
            }
        }
    }

    /**
     * Сбрасывает накопленные CSV строки на диск
     */
    public void flushCSV() throws IOException {
        if (csvBuffer.isEmpty()) return;

        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        Path csvPath = outputPath.resolve("forms_export.csv");
        boolean exists = Files.exists(csvPath);

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(csvPath.toFile(), true)))) {
            if (!exists) {
                writer.println("ФОРМА;БЛОК;ЗНАЧЕНИЕ");
            }
            for (String line : csvBuffer) {
                writer.println(line);
            }
            writer.flush();
        }
        csvBuffer.clear();
    }

    /**
     * Генерирует все CSV строки для формы (без записи в файл)
     * @return список строк CSV для этой формы (без заголовка)
     */
    public List<String> generateFormCsvLines(FormInfo formInfo) {
        List<String> lines = new ArrayList<>();

        if (formInfo == null) return lines;

        String formName = formInfo.getFormPath();

        // 1. SubForm
        for (String subForm : formInfo.getSubForms()) {
            lines.add(escapeCSV(formName) + ";subForm;" + escapeCSV(subForm));
        }

        // 2. формы JS
        for (String jsForm : formInfo.getJsForms()) {
            lines.add(escapeCSV(formName) + ";формы JS;" + escapeCSV(jsForm));
        }

        // 3. Отчеты, вызываемые на форме
        for (String report : formInfo.getReports()) {
            lines.add(escapeCSV(formName) + ";Отчеты, вызываемые на форме;" + escapeCSV(report));
        }

        // 4. Вьюхи (D_V_*)
        Set<String> views = new LinkedHashSet<>();
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_V_")) {
                views.add(tv);
            }
        }
        for (String view : views) {
            lines.add(escapeCSV(formName) + ";Вьюхи;" + escapeCSV(view));
        }

        // 5. Таблицы из вьюх
        Set<String> tablesFromViews = formInfo.getTablesFromViews();
        if (tablesFromViews != null) {
            for (String table : tablesFromViews) {
                lines.add(escapeCSV(formName) + ";Таблицы;" + escapeCSV(table));
            }
        }

        // 6. Прямые таблицы (D_* не начинающиеся с D_V_)
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_") && !tv.startsWith("D_V_")) {
                lines.add(escapeCSV(formName) + ";Таблицы;" + escapeCSV(tv));
            }
        }

        // 7. Пакеты и функции
        for (String pf : formInfo.getPackagesFunctions()) {
            lines.add(escapeCSV(formName) + ";Пакеты и функции;" + escapeCSV(pf));
        }

        // 8. СО (системные опции)
        for (String opt : formInfo.getSystemOptions()) {
            lines.add(escapeCSV(formName) + ";СО;" + escapeCSV(opt));
        }

        // 9. Универсальные композиции
        Set<String> allCompositions = new LinkedHashSet<>();
        if (formInfo.getUnitCompositions() != null) {
            for (String comp : formInfo.getUnitCompositions()) {
                String normalized = comp.replace("=\"", ":").replace("\"", "").replace(" composition:", " composition:").replace(",", "");
                normalized = normalized.replaceAll(",", "");
                normalized = normalized.replaceAll("\"", "");
                allCompositions.add(normalized);
            }
        }
        if (formInfo.getJsUnitCompositions() != null) {
            for (String comp : formInfo.getJsUnitCompositions()) {
                String normalized = comp.replace("=\"", ":").replace("\"", "").replace(" composition:", " composition:").replace(",", "");
                normalized = normalized.replaceAll(",", "");
                normalized = normalized.replaceAll(";", "");
                normalized = normalized.replaceAll("\"", "");
                allCompositions.add(normalized);
            }
        }
        for (String comp : allCompositions) {
            lines.add(escapeCSV(formName) + ";Универсальные композиции;" + escapeCSV(comp));
        }

        // 10. Пользовательские процедуры
        for (String proc : formInfo.getUserProcedures()) {
            lines.add(escapeCSV(formName) + ";Пользовательские процедуры;" + escapeCSV(proc));
        }

        // 11. Константы
        for (String constant : formInfo.getConstants()) {
            lines.add(escapeCSV(formName) + ";Константы;" + escapeCSV(constant));
        }

        // 12. Брокеры
        Set<String> allBrokers = new LinkedHashSet<>();
        if (formInfo.getBrokers() != null) {
            for (String comp : formInfo.getBrokers()) {
                String normalized = comp.replaceAll(";", "").replaceAll(",", "").replaceAll("\"", "");
                allBrokers.add(normalized);
            }
        }
        for (String broker : allBrokers) {
            lines.add(escapeCSV(formName) + ";Брокеры;" + escapeCSV(broker));
        }

        // 13. Неопределенные
        for (String unknown : formInfo.getUnknownObjects()) {
            lines.add(escapeCSV(formName) + ";Неопределенные;" + escapeCSV(unknown));
        }

        // 14. Юзерформы (UserForms)
        if (formInfo.isFullyReplaced() && formInfo.getReplacementPath() != null) {
            String relativePath = getRelativePath(formInfo.getReplacementPath());
            lines.add(escapeCSV(formName) + ";Юзерформы;" + escapeCSV(relativePath));
        }

        if (formInfo.getOverrides() != null && !formInfo.getOverrides().isEmpty()) {
            Map<String, List<FormInfo.OverrideInfo>> overridesByRegion = new LinkedHashMap<>();
            for (FormInfo.OverrideInfo override : formInfo.getOverrides()) {
                overridesByRegion.computeIfAbsent(override.getRegionName(), k -> new ArrayList<>()).add(override);
            }
            for (Map.Entry<String, List<FormInfo.OverrideInfo>> entry : overridesByRegion.entrySet()) {
                String region = entry.getKey();
                for (FormInfo.OverrideInfo override : entry.getValue()) {
                    String relativePath = getRelativePath(override.getOverridePath());
                    String value = getRelativePathWithinRegion(relativePath, region);
                    lines.add(escapeCSV(formName) + ";Юзерформы;" + escapeCSV(value));
                }
            }
        }

        return lines;
    }

    /**
     * Получить относительный путь от корня проекта
     */
    private String getRelativePath(String absolutePath) {
        if (absolutePath == null) return "";

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
            return normalizedPath.substring(regionIndex);
        }

        return relativePath;
    }

    /**
     * Создать полный CSV файл для отдельной формы (с заголовком)
     */
    /**
     * Создать полный CSV файл для отдельной формы (с заголовком)
     * Включает ВСЕ блоки, даже если данных нет - пишет "(не найдено)"
     */
    public String generateFormCsvContent(FormInfo formInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("ФОРМА;БЛОК;ЗНАЧЕНИЕ\n");

        if (formInfo == null) {
            return sb.toString();
        }

        String formName = formInfo.getFormPath();

        // 1. Юзерформы
        writeCsvBlock(sb, formName, "Юзерформы", formInfo.getOverrides(),
                formInfo.isFullyReplaced(), formInfo.getReplacementPath());

        // 2. subForm
        writeCsvBlock(sb, formName, "subForm", formInfo.getSubForms());

        // 3. формы JS
        writeCsvBlock(sb, formName, "формы JS", formInfo.getJsForms());

        // 4. Отчеты, вызываемые на форме
        writeCsvBlock(sb, formName, "Отчеты, вызываемые на форме", formInfo.getReports());

        // 5. Вьюхи (D_V_*)
        Set<String> views = new LinkedHashSet<>();
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_V_")) {
                views.add(tv);
            }
        }
        writeCsvBlock(sb, formName, "Вьюхи", views);

        // 6. Таблицы (D_* не начинающиеся с D_V_)
        Set<String> tables = new LinkedHashSet<>();
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_") && !tv.startsWith("D_V_")) {
                tables.add(tv);
            }
        }
        // Добавляем таблицы из вьюх
        Set<String> tablesFromViews = formInfo.getTablesFromViews();
        if (tablesFromViews != null) {
            tables.addAll(tablesFromViews);
        }
        writeCsvBlock(sb, formName, "Таблицы", tables);

        // 7. Пакеты и функции
        writeCsvBlock(sb, formName, "Пакеты и функции", formInfo.getPackagesFunctions());

        // 8. СО (системные опции)
        writeCsvBlock(sb, formName, "СО", formInfo.getSystemOptions());

        // 9. Универсальные композиции (объединяем оба блока)
        Set<String> allCompositions = new LinkedHashSet<>();
        if (formInfo.getUnitCompositions() != null) {
            for (String comp : formInfo.getUnitCompositions()) {
                String normalized = comp.replace("=\"", ":").replace("\"", "").replace(" composition:", " composition:").replace(",", "");
                normalized = normalized.replaceAll(",", "");
                normalized = normalized.replaceAll("\"", "");
                allCompositions.add(normalized);
            }
        }
        if (formInfo.getJsUnitCompositions() != null) {
            for (String comp : formInfo.getJsUnitCompositions()) {
                String normalized = comp.replace("=\"", ":").replace("\"", "").replace(" composition:", " composition:").replace(",", "");
                normalized = normalized.replaceAll(",", "");
                normalized = normalized.replaceAll(";", "");
                normalized = normalized.replaceAll("\"", "");
                allCompositions.add(normalized);
            }
        }
        writeCsvBlock(sb, formName, "Универсальные композиции", allCompositions);

        // 10. Пользовательские процедуры
        writeCsvBlock(sb, formName, "Пользовательские процедуры", formInfo.getUserProcedures());

        // 11. Константы
        writeCsvBlock(sb, formName, "Константы", formInfo.getConstants());

        // 12. Брокеры
        Set<String> allBrokers = new LinkedHashSet<>();
        if (formInfo.getBrokers() != null) {
            for (String comp : formInfo.getBrokers()) {
                String normalized = comp.replaceAll(";", "").replaceAll(",", "").replaceAll("\"", "");
                allBrokers.add(normalized);
            }
        }
        writeCsvBlock(sb, formName, "Брокеры", allBrokers);

        // 13. Неопределенные (РАЗОБРАТЬ АНАЛИТИКОМ)
        writeCsvBlock(sb, formName, "Неопределенные", formInfo.getUnknownObjects());

        return sb.toString();
    }

    /**
     * Записывает блок в CSV строку (для Set коллекций)
     * Если коллекция пустая, пишет "(не найдено)"
     */
    private void writeCsvBlock(StringBuilder sb, String formName, String blockName, Set<String> values) {
        if (values == null || values.isEmpty()) {
            sb.append(escapeCSV(formName)).append(";")
                    .append(escapeCSV(blockName)).append(";")
                    .append("(не найдено)\n");
        } else {
            for (String value : values) {
                if (value != null && !value.trim().isEmpty()) {
                    sb.append(escapeCSV(formName)).append(";")
                            .append(escapeCSV(blockName)).append(";")
                            .append(escapeCSV(value)).append("\n");
                }
            }
        }
    }

    /**
     * Записывает блок UserForms в CSV строку
     */
    private void writeCsvBlock(StringBuilder sb, String formName, String blockName,
                               List<FormInfo.OverrideInfo> overrides, boolean fullyReplaced, String replacementPath) {

        boolean hasData = false;

        if (fullyReplaced && replacementPath != null) {
            String relativePath = getRelativePath(replacementPath);
            sb.append(escapeCSV(formName)).append(";")
                    .append(escapeCSV(blockName)).append(";")
                    .append(escapeCSV(relativePath)).append("\n");
            hasData = true;
        }

        if (overrides != null && !overrides.isEmpty()) {
            Map<String, List<FormInfo.OverrideInfo>> overridesByRegion = new LinkedHashMap<>();
            for (FormInfo.OverrideInfo override : overrides) {
                overridesByRegion.computeIfAbsent(override.getRegionName(), k -> new ArrayList<>()).add(override);
            }

            for (Map.Entry<String, List<FormInfo.OverrideInfo>> entry : overridesByRegion.entrySet()) {
                String region = entry.getKey();
                for (FormInfo.OverrideInfo override : entry.getValue()) {
                    String relativePath = getRelativePath(override.getOverridePath());
                    String value = getRelativePathWithinRegion(relativePath, region);
                    sb.append(escapeCSV(formName)).append(";")
                            .append(escapeCSV(blockName)).append(";")
                            .append(escapeCSV(value)).append("\n");
                    hasData = true;
                }
            }
        }

        // Если нет данных - пишем "(не найдено)"
        if (!hasData) {
            sb.append(escapeCSV(formName)).append(";")
                    .append(escapeCSV(blockName)).append(";")
                    .append("(не найдено)\n");
        }
    }
    }