// core/extractor/processors/PopupMenuProcessor.java

package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.db.ReportsFromDbService;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.DbReportInfo;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.PopupMenuInfo;
import ru.tmis.analyzer.config.SettingsModel;

import java.util.*;
import java.util.regex.Pattern;

public class PopupMenuProcessor implements IXmlProcessor {

    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("^[-]+$");

    private final SettingsModel settings;
    private final ReportsFromDbService reportsService;
    private final Map<String, PopupMenuInfo> menuMap = new LinkedHashMap<>();
    private final List<AutoPopupInfo> autoPopups = new ArrayList<>();

    // Множество для сбора форм отчётов из AutoPopupMenu (для добавления в reports)
    private final Set<String> reportFormsFromAutoPopup = new LinkedHashSet<>();
    // Множество для сбора путей форм для добавления в jsForms
    private final Set<String> jsFormsFromAutoPopup = new LinkedHashSet<>();

    public PopupMenuProcessor(SettingsModel settings) {
        this.settings = settings;
        this.reportsService = new ReportsFromDbService(settings);
    }

    @Override
    public String getName() {
        return "PopupMenuProcessor";
    }

    @Override
    public int getPriority() {
        return 85;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        menuMap.clear();
        autoPopups.clear();
        reportFormsFromAutoPopup.clear();
        jsFormsFromAutoPopup.clear();

        // 1. Поиск D3 PopupMenu (cmpPopupMenu)
        Elements d3Popups = doc.select("cmpPopupMenu");
        for (Element popup : d3Popups) {
            String name = popup.attr("name");
            if (name == null || name.isEmpty()) continue;
            PopupMenuInfo menu = new PopupMenuInfo(name);
            parseMenuItems(popup, menu.getRootItems());
            menuMap.put(name, menu);
        }

        // 1.1. Поиск D3 PopupMenu (cmpPopup)
        Elements d3CmpPopups = doc.select("cmpPopup");
        for (Element popup : d3CmpPopups) {
            String name = popup.attr("name");
            if (name == null || name.isEmpty()) continue;
            PopupMenuInfo menu = new PopupMenuInfo(name);
            parseMenuItems(popup, menu.getRootItems());
            menuMap.put(name, menu);
        }

        // 2. Поиск M2 PopupMenu
        Elements m2Popups = doc.select("component[cmptype=Popup]");
        for (Element popup : m2Popups) {
            String name = popup.attr("name");
            if (name == null || name.isEmpty()) continue;
            PopupMenuInfo menu = new PopupMenuInfo(name);
            parseMenuItems(popup, menu.getRootItems());
            menuMap.put(name, menu);
        }

        // 3. Поиск D3 AutoPopupMenu
        Elements d3AutoPopups = doc.select("cmpAutoPopupMenu");
        for (Element autoPopup : d3AutoPopups) {
            String joinMenu = autoPopup.attr("join_menu");
            String name = autoPopup.attr("name");
            String unit = autoPopup.attr("unit");
            if (joinMenu == null || joinMenu.isEmpty()) continue;

            AutoPopupInfo info = new AutoPopupInfo();
            info.targetMenuName = joinMenu;
            info.autoPopupName = (name != null && !name.isEmpty()) ? name :
                    (unit != null && !unit.isEmpty()) ? unit : "";
            info.unit = unit;
            parseMenuItems(autoPopup, info.items);
            autoPopups.add(info);
        }

        // 4. Поиск M2 AutoPopupMenu
        Elements m2AutoPopups = doc.select("component[cmptype=AutoPopupMenu]");
        for (Element autoPopup : m2AutoPopups) {
            String joinMenu = autoPopup.attr("join_menu");
            String name = autoPopup.attr("name");
            String unit = autoPopup.attr("unit");
            if (joinMenu == null || joinMenu.isEmpty()) continue;

            AutoPopupInfo info = new AutoPopupInfo();
            info.targetMenuName = joinMenu;
            info.autoPopupName = (name != null && !name.isEmpty()) ? name :
                    (unit != null && !unit.isEmpty()) ? unit : "";
            info.unit = unit;
            parseMenuItems(autoPopup, info.items);
            autoPopups.add(info);
        }

        // 5. Объединяем AutoPopupMenu с целевыми PopupMenu
        for (AutoPopupInfo autoPopup : autoPopups) {
            PopupMenuInfo targetMenu = menuMap.get(autoPopup.targetMenuName);
            if (targetMenu != null) {
                // Добавляем пункты из XML
                for (PopupMenuInfo.MenuItem item : autoPopup.items) {
                    item.setFromAutoPopup(true);
                    item.setAutoPopupName(autoPopup.autoPopupName);
                    targetMenu.addItem(item);
                }

                // ========== ДОБАВЛЯЕМ ОТЧЁТЫ ИЗ ORACLE ==========
                if (autoPopup.unit != null && !autoPopup.unit.isEmpty()) {
                    System.out.println("[PopupMenuProcessor] Обработка AutoPopup для unit=" + autoPopup.unit);
                    List<DbReportInfo> dbReports = reportsService.getReportsByUnit(autoPopup.unit);
                    System.out.println("[PopupMenuProcessor] Найдено отчётов в БД Oracle: " + dbReports.size());

                    if (!dbReports.isEmpty()) {
                        // Извлекаем формы отчётов для добавления в reports и jsForms
                        extractReportFormsFromDbReports(dbReports);

                        // Форматируем отчёты в виде дерева
                        List<String> formattedReports = ReportsFromDbService.formatReportsForDisplay(
                                dbReports, autoPopup.autoPopupName, "", true);

                        for (String formattedReport : formattedReports) {
                            PopupMenuInfo.MenuItem dbItem = new PopupMenuInfo.MenuItem();
                            dbItem.setCaption(formattedReport);
                            dbItem.setDbReport(true);
                            targetMenu.addItem(dbItem);
                        }
                    }
                }
            }
        }

        // 6. Сохраняем в FormInfo
        List<PopupMenuInfo> result = new ArrayList<>();
        for (PopupMenuInfo menu : menuMap.values()) {
            if (!menu.getRootItems().isEmpty()) {
                result.add(menu);
            }
        }

        formInfo.setPopupMenus(result);

        // 7. Добавляем собранные формы отчётов в reports (блок "Отчеты вызываемые на форме")
        for (String reportForm : reportFormsFromAutoPopup) {
            formInfo.addReport(reportForm);
            System.out.println("  [PopupMenuProcessor] Добавлен отчёт в блок 'Отчеты вызываемые на форме': " + reportForm);
        }

        // 8. Добавляем собранные пути форм в jsForms (блок "Список вызываемых форм в JS:")
        for (String jsFormPath : jsFormsFromAutoPopup) {
            String normalizedPath = normalizeJsFormPath(jsFormPath);
            if (normalizedPath != null && !normalizedPath.isEmpty()) {
                formInfo.addJsForm(normalizedPath);
                System.out.println("  [PopupMenuProcessor] Добавлена форма в блок 'Список вызываемых форм в JS': " + normalizedPath);
            }
        }
    }

    /**
     * Извлекает формы отчётов из списка DbReportInfo для добавления в reports и jsForms
     */
    private void extractReportFormsFromDbReports(List<DbReportInfo> reports) {
        if (reports == null || reports.isEmpty()) return;

        for (DbReportInfo report : reports) {
            // Если это WEB-форма (REP_TYPE = 1) и есть REP_FILENAME
            if (report.getRepType() == 1 && report.getRepFilename() != null && !report.getRepFilename().isEmpty()) {
                String formPath = report.getRepFilename();
                if (!formPath.endsWith(".frm")) {
                    formPath = formPath + ".frm";
                }
                if (!formPath.startsWith("Reports/")) {
                    formPath = "Reports/" + formPath;
                }

                // Форматируем строку отчёта для блока "Отчеты вызываемые на форме"
                String formattedReport = String.format("%s (WEB-форма) %s",
                        report.getRepCode() != null ? report.getRepCode() : "?",
                        formPath);
                reportFormsFromAutoPopup.add(formattedReport);

                // Добавляем путь формы в jsForms
                jsFormsFromAutoPopup.add(formPath);

                System.out.println("  [PopupMenuProcessor] Найдена форма отчёта в AutoPopup: " + formattedReport);
            }

            // Рекурсивно обрабатываем дочерние отчёты (для составных отчётов)
            if (report.hasChildren()) {
                extractReportFormsFromDbReports(report.getChildren());
            }
        }
    }

    /**
     * Нормализация пути формы для jsForms
     */
    private String normalizeJsFormPath(String formPath) {
        if (formPath == null || formPath.trim().isEmpty()) return null;

        String normalized = formPath.trim();

        // Убираем ведущий слеш
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        // Добавляем .frm если нет расширения
        if (!normalized.endsWith(".frm") && !normalized.endsWith(".dfrm")) {
            normalized = normalized + ".frm";
        }

        return normalized;
    }

    private void parseMenuItems(Element parent, List<PopupMenuInfo.MenuItem> items) {
        Elements children = parent.children();

        for (Element child : children) {
            String tagName = child.tagName().toLowerCase();
            boolean isPopupItem = false;

            if (tagName.equalsIgnoreCase("cmpPopupItem") ||
                    (tagName.equals("component") && "PopupItem".equalsIgnoreCase(child.attr("cmptype")))) {
                isPopupItem = true;
            }

            if (isPopupItem) {
                String caption = child.attr("caption");
                String name = child.attr("name");

                if (caption != null && SEPARATOR_PATTERN.matcher(caption).matches()) {
                    continue;
                }

                PopupMenuInfo.MenuItem item = new PopupMenuInfo.MenuItem();
                if (caption != null && !caption.isEmpty()) {
                    item.setCaption(caption);
                }
                if (name != null && !name.isEmpty()) {
                    item.setName(name);
                }

                parseMenuItems(child, item.getChildren());
                items.add(item);
            } else if (tagName.equals("cmppopupmenu") ||
                    (tagName.equals("component") && "Popup".equalsIgnoreCase(child.attr("cmptype"))) ||
                    tagName.equals("cmppopup")) {
                parseMenuItems(child, items);
            } else {
                parseMenuItems(child, items);
            }
        }
    }

    private static class AutoPopupInfo {
        String targetMenuName;
        String autoPopupName;
        String unit;
        List<PopupMenuInfo.MenuItem> items = new ArrayList<>();
    }
}