// core/extractor/processors/PopupMenuProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.PopupMenuInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Извлечение контекстного меню (PopupMenu) из форм
 * Поддерживает:
 * - <cmpPopupMenu>
 * - <component cmptype="Popup">
 * - <cmpPopupItem>
 * - <component cmptype="PopupItem">
 */
public class PopupMenuProcessor implements IXmlProcessor {

    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("^[-]+$");

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
        List<PopupMenuInfo> menus = new ArrayList<>();

        // 1. Поиск D3 PopupMenu: <cmpPopupMenu>
        Elements d3Popups = doc.select("cmpPopupMenu");
        for (Element popup : d3Popups) {
            String name = popup.attr("name");
            PopupMenuInfo menu = new PopupMenuInfo(name);
            parseMenuItems(popup, menu.getRootItems());
            menus.add(menu);
        }

        // 2. Поиск M2 PopupMenu: <component cmptype="Popup">
        Elements m2Popups = doc.select("component[cmptype=Popup]");
        for (Element popup : m2Popups) {
            String name = popup.attr("name");
            PopupMenuInfo menu = new PopupMenuInfo(name);
            parseMenuItems(popup, menu.getRootItems());
            menus.add(menu);
        }

        // Сохраняем в FormInfo
        formInfo.setPopupMenus(menus);
    }

    /**
     * Рекурсивный парсинг пунктов меню
     */
    private void parseMenuItems(Element parent, List<PopupMenuInfo.MenuItem> items) {
        // Поиск дочерних элементов меню
        Elements children = parent.children();

        for (Element child : children) {
            String tagName = child.tagName().toLowerCase();
            boolean isPopupItem = false;

            // Проверяем тип элемента
            if (tagName.equals("cmppopupitem") ||
                    tagName.equals("component") && "PopupItem".equalsIgnoreCase(child.attr("cmptype"))) {
                isPopupItem = true;
            }

            if (isPopupItem) {
                String caption = child.attr("caption");
                String name = child.attr("name");

                // Пропускаем разделители (caption = "-")
                if (caption != null && SEPARATOR_PATTERN.matcher(caption).matches()) {
                    continue;
                }

                PopupMenuInfo.MenuItem item = new PopupMenuInfo.MenuItem();
                item.setCaption(caption);
                item.setName(name);

                // Рекурсивно обрабатываем вложенные пункты
                parseMenuItems(child, item.getChildren());

                items.add(item);
            } else {
                // Если не PopupItem, но может содержать вложенные пункты меню
                parseMenuItems(child, items);
            }
        }
    }
}