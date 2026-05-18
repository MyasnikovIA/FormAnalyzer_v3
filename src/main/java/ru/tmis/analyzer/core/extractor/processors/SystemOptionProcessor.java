package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Процессор для извлечения системных опций из D_PKG_OPTIONS.GET и D_PKG_OPTION_SPECS.GET
 */
public class SystemOptionProcessor implements IXmlProcessor {

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

        // 1. D_PKG_OPTIONS.GET('OPTION', ...) - позиционные параметры
        Pattern optPattern1 = Pattern.compile(
                "D_PKG_OPTIONS\\.GET\\s*\\(\\s*'([^']+)'",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher m1 = optPattern1.matcher(html);
        while (m1.find()) {
            addOption(m1.group(1), options);
        }

        // 2. D_PKG_OPTIONS.GET с => или =&gt; (именованные параметры)
        Pattern optPattern2 = Pattern.compile(
                "D_PKG_OPTIONS\\.GET[\\s\\S]*?(?:psSO_CODE|PS_SO_CODE|PSSO_CODE|PS_SO_CD)\\s*(?:=>|=&gt;)\\s*'([^']+)'",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher m2 = optPattern2.matcher(html);
        while (m2.find()) {
            addOption(m2.group(1), options);
        }

        // 3. D_PKG_OPTION_SPECS.GET('OPTION', ...) - позиционные параметры
        Pattern optSpecPattern1 = Pattern.compile(
                "D_PKG_OPTION_SPECS\\.GET\\s*\\(\\s*'([^']+)'",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher m3 = optSpecPattern1.matcher(html);
        while (m3.find()) {
            addOption(m3.group(1), options);
        }

        // 4. D_PKG_OPTION_SPECS.GET с => или =&gt; (именованные параметры)
        Pattern optSpecPattern2 = Pattern.compile(
                "D_PKG_OPTION_SPECS\\.GET[\\s\\S]*?(?:psSO_CODE|PS_SO_CODE|PSSO_CODE|PS_SO_CD)\\s*(?:=>|=&gt;)\\s*'([^']+)'",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher m4 = optSpecPattern2.matcher(html);
        while (m4.find()) {
            addOption(m4.group(1), options);
        }

        // 5. Универсальный паттерн (запасной) - ищем любую строку в кавычках после => или =&gt;
        Pattern universalPattern = Pattern.compile(
                "(?:D_PKG_OPTIONS|D_PKG_OPTION_SPECS)\\.GET[\\s\\S]*?(?:=>|=&gt;)\\s*'([^']+)'",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher m5 = universalPattern.matcher(html);
        while (m5.find()) {
            addOption(m5.group(1), options);
        }

        // Сохраняем найденные опции в FormInfo
        for (String option : options) {
            formInfo.addSystemOption(option);
            System.out.println("[SystemOptionProcessor] Найдена системная опция: " + option);
        }
    }

    private void addOption(String value, Set<String> options) {
        if (value != null && !value.isEmpty()) {
            options.add(value);
        }
    }
}