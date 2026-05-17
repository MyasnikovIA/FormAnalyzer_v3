// core/report/ReportGenerator.java
package ru.tmis.analyzer.core.report;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.PopupMenuInfo;
import ru.tmis.analyzer.core.model.SqlInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ReportGenerator {

    private final String outputDir;
    private final AppConfig config;
    private List<FormInfo> forms;

    public ReportGenerator(String outputDir, AppConfig config) {
        this.outputDir = outputDir;
        this.config = config;
        this.forms = new ArrayList<>();
    }

    public void addForm(FormInfo form) {
        forms.add(form);
    }

    public void addAllForms(List<FormInfo> forms) {
        this.forms.addAll(forms);
    }

    public void generateMainReport() throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        Path reportPath = outputPath.resolve("forms_report.txt");

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(reportPath))) {
            writeHeader(writer);

            for (FormInfo form : forms) {
                writeFormReport(writer, form);
            }

            writeFooter(writer);
        }

        System.out.println("Отчет сохранен: " + reportPath);
    }

    private void writeHeader(PrintWriter writer) {
        writer.println("=".repeat(100));
        writer.println("=== ОТЧЕТ ПО ФОРМАМ T-MIS ===");
        writer.println("Дата создания: " + new Date());
        writer.println("Всего форм: " + forms.size());
        writer.println("=".repeat(100));
        writer.println();
    }

    private void writeFormReport(PrintWriter writer, FormInfo form) {
        writer.println("-".repeat(100));
        writer.println("ФОРМА: " + form.getFormPath());
        writer.println("-".repeat(100));
        writer.println("Базовая форма: " + form.getBaseFormPath());

        if (form.isFullyReplaced()) {
            writer.println("СТАТУС: ПОЛНОСТЬЮ ЗАМЕНЕНА");
            writer.println("Файл замены: " + form.getReplacementPath());
        } else if (!form.getOverrides().isEmpty()) {
            writer.println("СТАТУС: ЧАСТИЧНО ПЕРЕОПРЕДЕЛЕНА");
        } else {
            writer.println("СТАТУС: БАЗОВАЯ ФОРМА");
        }
        writer.println();

        // SubForm
        writer.println("SubForm:");
        if (form.getSubForms().isEmpty()) {
            writer.println("     (не найдено)");
        } else {
            for (String subForm : form.getSubForms()) {
                writer.println("     " + subForm);
            }
        }
        writer.println();

        // Список вызываемых форм в JS
        writer.println("Список вызываемых форм в JS:");
        if (form.getJsForms().isEmpty()) {
            writer.println("     (не найдено)");
        } else {
            for (String jsForm : form.getJsForms()) {
                writer.println("     " + jsForm);
            }
        }
        writer.println();

        // Контекстное меню (ПКМ)
        writePopupMenusBlock(writer, form);

        // SQL запросы
        if (config.isIncludeSqlContent()) {
            writeSqlQueries(writer, form);
        } else {
            writer.println("SQL ЗАПРОСЫ (" + form.getSqlQueries().size() + "):");
            writer.println("     (содержимое скрыто)");
            writer.println();
        }

        // Используемые таблицы и вьюхи
        if (config.isIncludeTablesViews() && !form.getTablesViews().isEmpty()) {
            writer.println("ИСПОЛЬЗУЕМЫЕ ТАБЛИЦЫ И ВЬЮХИ:");
            for (String tv : form.getTablesViews()) {
                writer.println("    " + tv);
            }
            writer.println();
        }
        // Таблицы, используемые через вьюхи
        System.out.println("[DEBUG] Checking viewDependencies: " + (form.getViewDependencies() != null ? form.getViewDependencies().size() : "null"));
        if (form.getViewDependencies() != null && !form.getViewDependencies().isEmpty()) {
            System.out.println("[DEBUG] Calling writeViewTablesBlock");
            writeViewTablesBlock(writer, form, form.getViewDependencies());
        } else {
            System.out.println("[DEBUG] viewDependencies is null or empty, skipping");
        }

        // Используемые пакеты и функции
        if (!form.getPackagesFunctions().isEmpty()) {
            writer.println("ИСПОЛЬЗУЕМЫЕ ПАКЕТЫ И ФУНКЦИИ:");
            for (String pf : form.getPackagesFunctions()) {
                writer.println("    " + pf);
            }
            writer.println();
        }

        // Константы
        if (!form.getConstants().isEmpty()) {
            writer.println("КОНСТАНТЫ:");
            for (String constant : form.getConstants()) {
                writer.println("    " + constant);
            }
            writer.println();
        }

        // Системные опции
        if (!form.getSystemOptions().isEmpty()) {
            writer.println("СИСТЕМНЫЕ ОПЦИИ:");
            for (String opt : form.getSystemOptions()) {
                writer.println("    " + opt);
            }
            writer.println();
        }

        // Пользовательские процедуры
        if (!form.getUserProcedures().isEmpty()) {
            writer.println("ПОЛЬЗОВАТЕЛЬСКИЕ ПРОЦЕДУРЫ:");
            for (String proc : form.getUserProcedures()) {
                writer.println("    " + proc);
            }
            writer.println();
        }

        // Композиции UnitEdit
        if (!form.getUnitCompositions().isEmpty()) {
            writer.println("КОМПОЗИЦИИ UnitEdit:");
            for (String comp : form.getUnitCompositions()) {
                writer.println("    " + comp);
            }
            writer.println();
        }

        // Отчеты вызываемые на форме
        if (!form.getReports().isEmpty()) {
            writer.println("Отчеты вызываемые на форме (коды/формы отчета):");
            for (String report : form.getReports()) {
                writer.println("        " + report + ";");
            }
            writer.println();
        }

        // Брокеры
        if (!form.getBrokers().isEmpty()) {
            writer.println("БРОКЕРЫ:");
            for (String broker : form.getBrokers()) {
                writer.println("    " + broker);
            }
            writer.println();
        }

        // Разобрать аналитиком
        if (!form.getUnknownObjects().isEmpty()) {
            writer.println("РАЗОБРАТЬ АНАЛИТИКОМ:");
            for (String obj : form.getUnknownObjects()) {
                writer.println("    " + obj);
            }
            writer.println();
        }
    }

    private void writeSqlQueries(PrintWriter writer, FormInfo form) {
        writer.println("SQL ЗАПРОСЫ (" + form.getSqlQueries().size() + "):");
        writer.println();

        int num = 1;
        for (SqlInfo sql : form.getSqlQueries()) {
            writer.println("  [" + num + "] " + sql.getSourceType() + ": " + sql.getComponentName());
            writer.println("      Источник: " + sql.getSourcePath());
            writer.println("      SQL:");

            String content = sql.getSqlContent();
            if (content != null && !content.isEmpty()) {
                for (String line : content.split("\\r?\\n")) {
                    writer.println("      " + line);
                }
            }
            writer.println();
            num++;
        }
    }

    private void writeFooter(PrintWriter writer) {
        writer.println();
        writer.println("=".repeat(100));
        writer.println("=== КОНЕЦ ОТЧЕТА ===");
        writer.println("=".repeat(100));
    }

    public void generateSummaryReport() throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        Path summaryPath = outputPath.resolve("summary_report.txt");

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(summaryPath))) {
            writer.println("=".repeat(80));
            writer.println("=== ОБЩАЯ СТАТИСТИКА ===");
            writer.println("Дата: " + new Date());
            writer.println("=".repeat(80));
            writer.println();

            writer.println("Всего форм: " + forms.size());

            int totalSql = forms.stream().mapToInt(FormInfo::getTotalSqlQueries).sum();
            writer.println("Всего SQL запросов: " + totalSql);

            Set<String> allTables = new LinkedHashSet<>();
            Set<String> allViews = new LinkedHashSet<>();
            Set<String> allPackages = new LinkedHashSet<>();
            Set<String> allConstants = new LinkedHashSet<>();

            for (FormInfo form : forms) {
                for (String tv : form.getTablesViews()) {
                    if (tv.startsWith("D_V_")) {
                        allViews.add(tv);
                    } else {
                        allTables.add(tv);
                    }
                }
                allPackages.addAll(form.getPackagesFunctions());
                allConstants.addAll(form.getConstants());
            }

            writer.println("Уникальных таблиц: " + allTables.size());
            writer.println("Уникальных вьюх: " + allViews.size());
            writer.println("Уникальных пакетов/функций: " + allPackages.size());
            writer.println("Уникальных констант: " + allConstants.size());

            writer.println();
            writer.println("=".repeat(80));
        }

        System.out.println("Сводный отчет сохранен: " + summaryPath);
    }


    /**
     * Вывод таблиц, используемых через вьюхи
     * @param writer PrintWriter для вывода
     * @param formInfo информация о форме
     * @param viewDependencies карта зависимостей вьюх
     */
    // core/report/ReportGenerator.java

    private void writeViewTablesBlock(PrintWriter writer, FormInfo formInfo,
                                      Map<String, ViewTableDependencies> viewDependencies) {
        if (viewDependencies == null || viewDependencies.isEmpty()) {
            System.out.println("[DEBUG] writeViewTablesBlock: viewDependencies is null or empty");
            return;
        }

        // Собираем все вьюхи, используемые в этой форме
        Set<String> viewsUsed = new LinkedHashSet<>();
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_V_")) {
                viewsUsed.add(tv);
            }
        }

        if (viewsUsed.isEmpty()) {
            System.out.println("[DEBUG] writeViewTablesBlock: no views found in form");
            return;
        }

        // Собираем уникальные таблицы из всех вьюх
        Set<String> allTables = new LinkedHashSet<>();
        for (String viewName : viewsUsed) {
            ViewTableDependencies deps = viewDependencies.get(viewName);
            if (deps != null && deps.isExistsInOracle()) {
                allTables.addAll(deps.getOracleTables());
            }
        }

        if (allTables.isEmpty()) {
            System.out.println("[DEBUG] writeViewTablesBlock: no tables extracted from views");
            return;
        }

        writer.println("ТАБЛИЦЫ, ИСПОЛЬЗУЕМЫЕ ЧЕРЕЗ ВЬЮХИ (уникальные для этой формы):");
        for (String table : allTables) {
            writer.println("    " + table);
        }
        writer.println();

        System.out.println("[DEBUG] writeViewTablesBlock: wrote " + allTables.size() + " tables");
    }


    // core/report/ReportGenerator.java

    /**
     * Вывод контекстного меню (PopupMenu) в виде дерева
     */
    private void writePopupMenusBlock(PrintWriter writer, FormInfo form) {
        List<PopupMenuInfo> menus = form.getPopupMenus();
        if (menus == null || menus.isEmpty()) {
            return;
        }

        writer.println();
        writer.println("Контекстное меню используемое на форме (ПКМ):");
        writer.println();

        for (int i = 0; i < menus.size(); i++) {
            PopupMenuInfo menu = menus.get(i);
            boolean isLast = (i == menus.size() - 1);

            // Вывод корневого элемента меню
            String prefix = isLast ? "└── " : "├── ";
            writer.println(prefix + "name=\"" + menu.getName() + "\"");

            // Вывод пунктов меню
            writeMenuTree(writer, menu.getRootItems(), isLast ? "    " : "│   ");

            if (!isLast) {
                writer.println();
            }
        }
        writer.println();
    }

    /**
     * Рекурсивный вывод дерева пунктов меню
     */
    private void writeMenuTree(PrintWriter writer, List<PopupMenuInfo.MenuItem> items, String indent) {
        for (int i = 0; i < items.size(); i++) {
            PopupMenuInfo.MenuItem item = items.get(i);
            boolean isLast = (i == items.size() - 1);

            String branch = isLast ? "└── " : "├── ";
            String childIndent = indent + (isLast ? "    " : "│   ");

            if (item.isDbReport()) {
                // caption уже содержит полный отступ и символы дерева
                writer.println(item.getCaption());
            } else {
                String displayText = item.getPrefix() + item.getDisplayCaption();
                writer.println(indent + branch + displayText);
            }

            if (item.hasChildren()) {
                writeMenuTree(writer, item.getChildren(), childIndent);
            }
        }
    }



    /**
     * Рекурсивный вывод пунктов меню
     * @param writer PrintWriter
     * @param items список пунктов меню
     * @param level уровень вложенности (количество отступов)
     */
    private void writeMenuItems(PrintWriter writer, List<PopupMenuInfo.MenuItem> items, int level) {
        String indent = "    ".repeat(level + 1); // +1 для учета корневого отступа

        for (PopupMenuInfo.MenuItem item : items) {
            writer.println(indent + item.getDisplayCaption());

            if (item.hasChildren()) {
                writeMenuItems(writer, item.getChildren(), level + 1);
            }
        }
    }
    /**
     * Вывод списка вызываемых форм в JS
     */
    private void writeJsFormsBlock(PrintWriter writer, FormInfo form) {
        if (!form.getJsForms().isEmpty()) {
            writer.println("Список вызываемых форм в JS:");
            for (String jsForm : form.getJsForms()) {
                writer.println("     " + jsForm);
            }
            writer.println();
        }
    }
    /**
     * Вывод отчетов, вызываемых на форме
     */
    private void writeReportsBlock(PrintWriter writer, FormInfo form) {
        if (!form.getReports().isEmpty()) {
            writer.println("Отчеты вызываемые на форме (коды/формы отчета):");
            for (String report : form.getReports()) {
                writer.println("        " + report + ";");
            }
            writer.println();
        }
    }

    private void writeConstantsBlock(PrintWriter writer, FormInfo form) {
        if (!form.getConstants().isEmpty()) {
            writer.println("КОНСТАНТЫ:");
            for (String constant : form.getConstants()) {
                writer.println("    " + constant);
            }
            writer.println();
        }
    }
}