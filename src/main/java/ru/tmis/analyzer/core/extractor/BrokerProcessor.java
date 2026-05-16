// core/extractor/processors/BrokerProcessor.java (полная версия)
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class BrokerProcessor implements IXmlProcessor {

    private static final Pattern ACTION_PATTERN = Pattern.compile(
            "action\\s*=\\s*['\"]([^'\"]+)['\"]",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String getName() {
        return "BrokerProcessor";
    }

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        Set<String> brokers = new LinkedHashSet<>();

        // D3 Action с unit и action
        Elements d3Actions = doc.select("cmpAction[unit]");
        for (Element el : d3Actions) {
            String unit = el.attr("unit");
            String action = el.attr("action");
            if (!unit.isEmpty() && !hasSqlContent(el)) {
                if (!action.isEmpty()) {
                    brokers.add(String.format("unit:%s  action:%s;", unit, action));
                } else {
                    brokers.add(String.format("unit:%s;", unit));
                }
            }
        }

        // D3 SubAction
        Elements d3SubActions = doc.select("cmpSubAction[unit]");
        for (Element el : d3SubActions) {
            String unit = el.attr("unit");
            String action = el.attr("action");
            if (!unit.isEmpty() && !hasSqlContent(el)) {
                if (!action.isEmpty()) {
                    brokers.add(String.format("unit:%s  action:%s;", unit, action));
                } else {
                    brokers.add(String.format("unit:%s;", unit));
                }
            }
        }

        // M2 Action
        Elements m2Actions = doc.select("component[cmptype=Action][unit]");
        for (Element el : m2Actions) {
            String unit = el.attr("unit");
            String action = el.attr("action");
            if (!unit.isEmpty() && !hasSqlContent(el)) {
                if (!action.isEmpty()) {
                    brokers.add(String.format("unit:%s  action:%s;", unit, action));
                } else {
                    brokers.add(String.format("unit:%s;", unit));
                }
            }
        }

        // M2 SubAction
        Elements m2SubActions = doc.select("component[cmptype=SubAction][unit]");
        for (Element el : m2SubActions) {
            String unit = el.attr("unit");
            String action = el.attr("action");
            if (!unit.isEmpty() && !hasSqlContent(el)) {
                if (!action.isEmpty()) {
                    brokers.add(String.format("unit:%s  action:%s;", unit, action));
                } else {
                    brokers.add(String.format("unit:%s;", unit));
                }
            }
        }

        // Прямое указание функции (без unit)
        Elements directActions = doc.select("cmpAction[action^=D_PKG_], cmpSubAction[action^=D_PKG_], " +
                "component[cmptype=Action][action^=D_PKG_], component[cmptype=SubAction][action^=D_PKG_]");
        for (Element el : directActions) {
            String action = el.attr("action");
            if (!action.isEmpty() && action.startsWith("D_PKG_") && !hasSqlContent(el)) {
                brokers.add(String.format("action:%s;", action));
            }
        }

        for (String broker : brokers) {
            formInfo.addBroker(broker);
        }
    }

    private boolean hasSqlContent(Element element) {
        String html = element.html();
        return html != null && html.contains("CDATA") &&
                (html.contains("SELECT") || html.contains("INSERT") ||
                        html.contains("UPDATE") || html.contains("DELETE"));
    }
}