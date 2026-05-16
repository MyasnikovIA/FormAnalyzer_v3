// core/extractor/processors/SystemOptionProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Извлечение системных опций из D_PKG_OPTIONS.GET и D_PKG_OPTION_SPECS.GET
 */
public class SystemOptionProcessor implements IXmlProcessor {

    private static final Pattern OPTION_PATTERN_POSITIONAL = Pattern.compile(
            "D_PKG_OPTIONS\\.GET\\s*\\(\\s*'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OPTION_PATTERN_NAMED = Pattern.compile(
            "D_PKG_OPTIONS\\.GET\\s*\\(\\s*psSO_CODE\\s*=>\\s*'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OPTION_SPEC_POSITIONAL = Pattern.compile(
            "D_PKG_OPTION_SPECS\\.GET\\s*\\(\\s*'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OPTION_SPEC_NAMED = Pattern.compile(
            "D_PKG_OPTION_SPECS\\.GET\\s*\\(\\s*psSO_CODE\\s*=>\\s*'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    @Override
    public String getName() {
        return "SystemOptionProcessor";
    }

    @Override
    public int getPriority() {
        return 36;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        String html = doc.html();
        Set<String> options = new LinkedHashSet<>();

        Matcher m1 = OPTION_PATTERN_POSITIONAL.matcher(html);
        while (m1.find()) addOption(m1.group(1), options);

        Matcher m2 = OPTION_PATTERN_NAMED.matcher(html);
        while (m2.find()) addOption(m2.group(1), options);

        Matcher m3 = OPTION_SPEC_POSITIONAL.matcher(html);
        while (m3.find()) addOption(m3.group(1), options);

        Matcher m4 = OPTION_SPEC_NAMED.matcher(html);
        while (m4.find()) addOption(m4.group(1), options);

        for (String option : options) {
            formInfo.addSystemOption(option);
        }
    }

    private void addOption(String value, Set<String> options) {
        if (value != null && !value.isEmpty()) {
            options.add(value);
        }
    }
}