// core/extractor/processors/AutoPopupMenuProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoPopupMenuProcessor implements IXmlProcessor {

    @Override
    public String getName() {
        return "AutoPopupMenuProcessor";
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        // Паттерн 1: D3 синтаксис - cmpAutoPopupMenu
        Elements d3Menus = doc.select("cmpAutoPopupMenu");
        for (var element : d3Menus) {
            String unit = element.attr("unit");
            if (unit != null && !unit.isEmpty()) {
                formInfo.addAutoPopupMenu(unit);
                System.out.println("  [AutoPopupMenu] D3: unit=" + unit);
            }
        }

        // Паттерн 2: M2 синтаксис - component с cmptype="AutoPopupMenu"
        Elements m2Menus = doc.select("component[cmptype=AutoPopupMenu]");
        for (var element : m2Menus) {
            String unit = element.attr("unit");
            if (unit != null && !unit.isEmpty()) {
                formInfo.addAutoPopupMenu(unit);
                System.out.println("  [AutoPopupMenu] M2: unit=" + unit);
            }
        }

        // Паттерн 3: Дополнительный поиск через регулярные выражения для случаев,
        // когда Jsoup не может распарсить из-за проблем с XML
        String html = doc.html();

        // D3 с пробелами и другими атрибутами
        Pattern d3Pattern = Pattern.compile(
                "<cmpAutoPopupMenu\\s+[^>]*?\\bunit\\s*=\\s*['\"]([^'\"]+)['\"][^>]*/?>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher d3Matcher = d3Pattern.matcher(html);
        while (d3Matcher.find()) {
            String unit = d3Matcher.group(1);
            if (unit != null && !unit.isEmpty()) {
                formInfo.addAutoPopupMenu(unit);
                System.out.println("  [AutoPopupMenu] D3 (regex): unit=" + unit);
            }
        }

        // M2 с обратным порядком атрибутов
        Pattern m2Pattern = Pattern.compile(
                "<component\\s+[^>]*?\\bunit\\s*=\\s*['\"]([^'\"]+)['\"][^>]*?cmptype\\s*=\\s*['\"]AutoPopupMenu['\"][^>]*/?>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher m2Matcher = m2Pattern.matcher(html);
        while (m2Matcher.find()) {
            String unit = m2Matcher.group(1);
            if (unit != null && !unit.isEmpty()) {
                formInfo.addAutoPopupMenu(unit);
                System.out.println("  [AutoPopupMenu] M2 (regex reverse): unit=" + unit);
            }
        }
    }
}