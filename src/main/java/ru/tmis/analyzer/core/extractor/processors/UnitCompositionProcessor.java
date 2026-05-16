// core/extractor/processors/UnitCompositionProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnitCompositionProcessor implements IXmlProcessor {

    // Полный паттерн из старого проекта
    private static final Pattern UNIT_COMPOSITION_PATTERN = Pattern.compile(
            "<(?:cmp|com)?(?:UnitEdit|ButtonEdit|ButtonUnitEdit|UnitButtonEdit)\\b[^>]*?\\b(?:unit|UNIT)\\s*=\\s*['\"]([^'\"]+)['\"][^>]*?\\b(?:composition|COMPOSITION)\\s*=\\s*['\"]([^'\"]+)['\"]|" +
                    "<(?:cmp|com)?(?:UnitEdit|ButtonEdit|ButtonUnitEdit|UnitButtonEdit)\\b[^>]*?\\b(?:composition|COMPOSITION)\\s*=\\s*['\"]([^'\"]+)['\"][^>]*?\\b(?:unit|UNIT)\\s*=\\s*['\"]([^'\"]+)['\"]|" +
                    "<component\\s+[^>]*?\\bcmptype\\s*=\\s*['\"](?:UnitEdit|ButtonEdit|ButtonUnitEdit|UnitButtonEdit)['\"][^>]*?\\b(?:unit|UNIT)\\s*=\\s*['\"]([^'\"]+)['\"][^>]*?\\b(?:composition|COMPOSITION)\\s*=\\s*['\"]([^'\"]+)['\"]|" +
                    "<component\\s+[^>]*?\\bcmptype\\s*=\\s*['\"](?:UnitEdit|ButtonEdit|ButtonUnitEdit|UnitButtonEdit)['\"][^>]*?\\b(?:composition|COMPOSITION)\\s*=\\s*['\"]([^'\"]+)['\"][^>]*?\\b(?:unit|UNIT)\\s*=\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SIMPLE_UNIT_PATTERN = Pattern.compile(
            "\\b(?:unit|UNIT)\\s*=\\s*['\"]([^'\"]+)['\"]\\s*.*?\\b(?:composition|COMPOSITION)\\s*=\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SIMPLE_COMPOSITION_PATTERN = Pattern.compile(
            "\\b(?:composition|COMPOSITION)\\s*=\\s*['\"]([^'\"]+)['\"]\\s*.*?\\b(?:unit|UNIT)\\s*=\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    @Override
    public String getName() {
        return "UnitCompositionProcessor";
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        String html = doc.html();
        Set<String> compositions = new LinkedHashSet<>();

        // Метод 1: Полный паттерн
        Matcher matcher = UNIT_COMPOSITION_PATTERN.matcher(html);
        while (matcher.find()) {
            String unit = null, composition = null;
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (group != null) {
                    if (unit == null && (group.matches("^[A-Z][A-Z0-9_]*$") || group.contains("_"))) {
                        unit = group;
                    } else if (composition == null) {
                        composition = group;
                    }
                }
            }
            if (unit != null && !unit.isEmpty()) {
                compositions.add(String.format("unit=\"%s\" composition=\"%s\"", unit, composition != null ? composition : "DEFAULT"));
            }
        }

        // Метод 2: Упрощенный паттерн
        Matcher simpleMatcher = SIMPLE_UNIT_PATTERN.matcher(html);
        while (simpleMatcher.find()) {
            String unit = simpleMatcher.group(1);
            String composition = simpleMatcher.group(2);
            if (unit != null && !unit.isEmpty()) {
                compositions.add(String.format("unit=\"%s\" composition=\"%s\"", unit, composition));
            }
        }

        // Метод 3: Обратный порядок
        Matcher simpleMatcher2 = SIMPLE_COMPOSITION_PATTERN.matcher(html);
        while (simpleMatcher2.find()) {
            String composition = simpleMatcher2.group(1);
            String unit = simpleMatcher2.group(2);
            if (unit != null && !unit.isEmpty()) {
                compositions.add(String.format("unit=\"%s\" composition=\"%s\"", unit, composition));
            }
        }

        for (String comp : compositions) {
            formInfo.addUnitComposition(comp);
        }
    }
}