// core/extractor/processors/SubFormProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;

public class SubFormProcessor implements IXmlProcessor {

    @Override
    public String getName() {
        return "SubFormProcessor";
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        // D3 синтаксис
        Elements d3SubForms = doc.select("cmpSubForm");
        for (var element : d3SubForms) {
            String path = element.attr("path");
            if (path != null && !path.isEmpty()) {
                formInfo.addSubForm(path);
            }
        }

        // M2 синтаксис
        Elements m2SubForms = doc.select("component[cmptype=SubForm]");
        for (var element : m2SubForms) {
            String path = element.attr("path");
            if (path != null && !path.isEmpty()) {
                formInfo.addSubForm(path);
            }
        }
    }
}