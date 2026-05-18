// core/extractor/processors/PopupMenuProcessorPg.java

package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.db.PostgresReportsService;
import ru.tmis.analyzer.core.db.ReportsFromDbService;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.DbReportInfo;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.PopupMenuInfo;
import ru.tmis.analyzer.config.SettingsModel;

import java.util.*;
import java.util.regex.Pattern;

public class PopupMenuProcessorPg implements IXmlProcessor {

    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("^[-]+$");

    private final SettingsModel settings;
    private final PostgresReportsService postgresReportsService;
    private final Map<String, PopupMenuInfo> menuMap = new LinkedHashMap<>();
    private final List<AutoPopupInfo> autoPopups = new ArrayList<>();

    public PopupMenuProcessorPg(SettingsModel settings) {
        this.settings = settings;
        this.postgresReportsService = new PostgresReportsService(settings);
    }

    @Override
    public String getName() {
        return "PopupMenuProcessorPg";
    }

    @Override
    public int getPriority() {
        return 86;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        menuMap.clear();
        autoPopups.clear();

        // 1. Поиск D3 PopupMenu (cmpPopupMenu)
        Elements d3Popups = doc.select("cmpPopupMenu");
        for (Element popup : d3Popups) {
            String name = popup.attr("name");
            if (name == null || name.isEmpty()) continue;
            PopupMenuInfo menu = new PopupMenuInfo(name);
            parseMenuItems(popup, menu.getRootItems());
            menuMap.put(name, menu);
        }

        // 1.1. Поиск D3 PopupMenu (cmpPopup) - ДОБАВЛЕНО
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
            // Исправлено: fallback на unit, если name отсутствует
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
            // Исправлено: fallback на unit, если name отсутствует
            info.autoPopupName = (name != null && !name.isEmpty()) ? name :
                    (unit != null && !unit.isEmpty()) ? unit : "";
            info.unit = unit;
            parseMenuItems(autoPopup, info.items);
            autoPopups.add(info);
        }

        // 5. Объединяем AutoPopupMenu с целевыми PopupMenu (используем PostgreSQL)
        for (AutoPopupInfo autoPopup : autoPopups) {
            PopupMenuInfo targetMenu = menuMap.get(autoPopup.targetMenuName);
            if (targetMenu != null) {
                // Добавляем пункты из XML
                for (PopupMenuInfo.MenuItem item : autoPopup.items) {
                    item.setFromAutoPopup(true);
                    item.setAutoPopupName(autoPopup.autoPopupName);
                    targetMenu.addItem(item);
                }

                // Если есть unit, добавляем отчеты из PostgreSQL БД
                if (autoPopup.unit != null && !autoPopup.unit.isEmpty()) {
                    List<DbReportInfo> dbReports = postgresReportsService.getReportsByUnit(autoPopup.unit);

                    if (!dbReports.isEmpty()) {
                        // Используем форматирование с правильной иерархией
                        List<String> formattedReports = PostgresReportsService.formatReportsForDisplay(
                                dbReports, autoPopup.autoPopupName, "", true);

                        for (String formattedReport : formattedReports) {
                            PopupMenuInfo.MenuItem dbItem = new PopupMenuInfo.MenuItem();
                            // Добавляем пометку PostgreSQL, но сохраняем полную строку с символами дерева
                            dbItem.setCaption(formattedReport + " (PostgreSQL)");
                            dbItem.setDbReport(true);
                            targetMenu.addItem(dbItem);
                        }
                    }
                }
            }
        }

        // 6. Сохраняем в FormInfo в отдельное поле popupMenusPg
        List<PopupMenuInfo> result = new ArrayList<>();
        for (PopupMenuInfo menu : menuMap.values()) {
            if (!menu.getRootItems().isEmpty()) {
                result.add(menu);
            }
        }

        formInfo.setPopupMenusPg(result);
    }

    /**
     * Рекурсивный парсинг пунктов меню
     */
    private void parseMenuItems(Element parent, List<PopupMenuInfo.MenuItem> items) {
        Elements children = parent.children();

        for (Element child : children) {
            String tagName = child.tagName().toLowerCase();
            boolean isPopupItem = false;

            // ИСПРАВЛЕНО: добавлено cmpPopupItem с игнорированием регистра
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
                    tagName.equals("cmppopup")) {  // ДОБАВЛЕНО: поддержка вложенных cmpPopup
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