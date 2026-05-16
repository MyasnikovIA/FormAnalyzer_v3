// core/extractor/processors/AutoPopupMenuProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;

public class AutoPopupMenuProcessor implements IXmlProcessor {

    @Override
    public String getName() {
        return "AutoPopupMenuProcessor";
    }

    @Override
    public int getPriority() {
        return 60;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        // D3 синтаксис
        Elements d3Menus = doc.select("cmpAutoPopupMenu");
        for (var element : d3Menus) {
            String unit = element.attr("unit");
            if (unit != null && !unit.isEmpty()) {
                formInfo.addAutoPopupMenu(unit);
            }
        }

        // M2 синтаксис
        Elements m2Menus = doc.select("component[cmptype=AutoPopupMenu]");
        for (var element : m2Menus) {
            String unit = element.attr("unit");
            if (unit != null && !unit.isEmpty()) {
                formInfo.addAutoPopupMenu(unit);
            }
        }
    }
}