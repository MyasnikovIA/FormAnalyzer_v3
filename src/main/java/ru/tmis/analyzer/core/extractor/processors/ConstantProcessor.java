// core/extractor/processors/ConstantProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConstantProcessor implements IXmlProcessor {

    private static final Pattern SEARCH_STR_POSITIONAL = Pattern.compile(
            "D_PKG_CONSTANTS\\.SEARCH_STR\\s*\\(\\s*'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SEARCH_STR_NAMED = Pattern.compile(
            "D_PKG_CONSTANTS\\.SEARCH_STR\\s*\\(\\s*psCONST_CODE\\s*=>\\s*'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SEARCH_NUM_POSITIONAL = Pattern.compile(
            "D_PKG_CONSTANTS\\.SEARCH_NUM\\s*\\(\\s*'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SEARCH_NUM_NAMED = Pattern.compile(
            "D_PKG_CONSTANTS\\.SEARCH_NUM\\s*\\(\\s*psCONST_CODE\\s*=>\\s*'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SEARCH_DATE_POSITIONAL = Pattern.compile(
            "D_PKG_CONSTANTS\\.SEARCH_DATE\\s*\\(\\s*'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SEARCH_DATE_NAMED = Pattern.compile(
            "D_PKG_CONSTANTS\\.SEARCH_DATE\\s*\\(\\s*psCONST_CODE\\s*=>\\s*'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern UNIVERSAL_CONSTANT_PATTERN = Pattern.compile(
            "D_PKG_CONSTANTS\\.SEARCH_(?:STR|NUM|DATE)\\s*\\(\\s*(?:psCONST_CODE\\s*=>\\s*)?'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    @Override
    public String getName() {
        return "ConstantProcessor";
    }

    @Override
    public int getPriority() {
        return 35;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        String html = doc.html();
        Set<String> constants = new LinkedHashSet<>();

        // Все 7 паттернов как в старом проекте
        Matcher m1 = SEARCH_STR_POSITIONAL.matcher(html);
        while (m1.find()) addConstant(m1.group(1), constants);

        Matcher m2 = SEARCH_STR_NAMED.matcher(html);
        while (m2.find()) addConstant(m2.group(1), constants);

        Matcher m3 = SEARCH_NUM_POSITIONAL.matcher(html);
        while (m3.find()) addConstant(m3.group(1), constants);

        Matcher m4 = SEARCH_NUM_NAMED.matcher(html);
        while (m4.find()) addConstant(m4.group(1), constants);

        Matcher m5 = SEARCH_DATE_POSITIONAL.matcher(html);
        while (m5.find()) addConstant(m5.group(1), constants);

        Matcher m6 = SEARCH_DATE_NAMED.matcher(html);
        while (m6.find()) addConstant(m6.group(1), constants);

        if (constants.isEmpty()) {
            Matcher m7 = UNIVERSAL_CONSTANT_PATTERN.matcher(html);
            while (m7.find()) addConstant(m7.group(1), constants);
        }

        for (String constant : constants) {
            formInfo.addConstant(constant);
        }
    }

    private void addConstant(String value, Set<String> constants) {
        if (value != null && !value.isEmpty()) {
            constants.add(value);
        }
    }
}