// core/extractor/processors/PackageFromActionProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.model.FormInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Извлечение пакетов из Action/SubAction компонентов без unit
 */
public class PackageFromActionProcessor implements IXmlProcessor {

    private static final Pattern ACTION_PATTERN = Pattern.compile(
            "\\baction\\s*=\\s*['\"]([^'\"]+\\.[^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    @Override
    public String getName() {
        return "PackageFromActionProcessor";
    }

    @Override
    public int getPriority() {
        return 22;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        Set<String> packages = new LinkedHashSet<>();

        // D3 Action без unit
        Elements d3Actions = doc.select("cmpAction:not([unit]), cmpSubAction:not([unit])");
        for (Element el : d3Actions) {
            if (!hasSql(el)) {
                extractActionPackage(el, packages);
            }
        }

        // M2 Action без unit
        Elements m2Actions = doc.select("component[cmptype=Action]:not([unit]), component[cmptype=SubAction]:not([unit])");
        for (Element el : m2Actions) {
            if (!hasSql(el)) {
                extractActionPackage(el, packages);
            }
        }

        for (String pkg : packages) {
            if (pkg.contains(".") && !pkg.startsWith("D_PKG_CONSTANTS") &&
                    !pkg.startsWith("D_PKG_OPTIONS") && !pkg.startsWith("D_PKG_OPTION_SPECS")) {
                formInfo.addPackageFunction(pkg);
            }
        }
    }

    private void extractActionPackage(Element element, Set<String> packages) {
        String action = element.attr("action");
        if (action != null && !action.isEmpty() && action.contains(".")) {
            packages.add(action);
        }
    }

    private boolean hasSql(Element element) {
        String html = element.html();
        return html != null && html.contains("CDATA") &&
                (html.contains("SELECT") || html.contains("INSERT") ||
                        html.contains("UPDATE") || html.contains("DELETE") || html.contains("BEGIN"));
    }
}