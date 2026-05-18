// core/extractor/processors/UniversalCompositionProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Извлечение композиций из JS вызовов UniversalComposition и UniversalEditForm
 */
public class UniversalCompositionProcessor implements IXmlProcessor {

    // Паттерн для UniversalComposition (старый формат)
    private static final Pattern UNIVERSAL_COMPOSITION_PATTERN = Pattern.compile(
            "openWindow\\s*\\(\\s*\\{\\s*name\\s*:\\s*['\"]UniversalComposition/UniversalComposition['\"][^}]*?" +
                    "(?:unit|UNIT)\\s*:\\s*['\"]([^'\"]+)['\"][^}]*?" +
                    "(?:composition|COMPOSITION)\\s*:\\s*['\"]([^'\"]+)['\"][^}]*\\}\\s*(?:,|,?\\)?)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для UniversalEditForm (новый формат)
    private static final Pattern UNIVERSAL_EDIT_FORM_PATTERN = Pattern.compile(
            "open(?:Window|D3Form)?\\s*\\(\\s*\\{\\s*name\\s*:\\s*['\"]UniversalEditForm/UniversalEditForm['\"][^}]*?" +
                    "(?:unit|UNIT)\\s*:\\s*['\"]([^'\"]+)['\"][^}]*?" +
                    "(?:method|METHOD)\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );


    // Альтернативный паттерн для поиска в любом JS коде
    private static final Pattern JS_UNIVERSAL_PATTERN = Pattern.compile(
            "(?:unit|UNIT)\\s*:\\s*['\"]([^'\"]+)['\"][^}]*?(?:composition|COMPOSITION|method|METHOD)\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для поиска в CDATA секциях
    private static final Pattern CDATA_PATTERN = Pattern.compile(
            "<!\\[CDATA\\[(.*?)\\]\\]>",
            Pattern.DOTALL
    );

    @Override
    public String getName() {
        return "UniversalCompositionProcessor";
    }

    @Override
    public int getPriority() {
        return 55;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        String html = doc.html();
        Set<String> compositions = new LinkedHashSet<>();

        // Извлекаем все CDATA секции и обрабатываем их
        Matcher cdataMatcher = CDATA_PATTERN.matcher(html);
        while (cdataMatcher.find()) {
            String jsContent = cdataMatcher.group(1);
            compositions.addAll(extractFromJsContent(jsContent));
        }

        // Также обрабатываем весь HTML на случай, если JS не в CDATA
        compositions.addAll(extractFromJsContent(html));

        // Добавляем найденные композиции в FormInfo
        for (String composition : compositions) {
            formInfo.addJsUnitComposition(composition);
        }
    }

    private Set<String> extractFromJsContent(String content) {
        Set<String> result = new LinkedHashSet<>();

        // Поиск UniversalComposition
        Matcher ucMatcher = UNIVERSAL_COMPOSITION_PATTERN.matcher(content);
        while (ucMatcher.find()) {
            String unit = cleanValue(ucMatcher.group(1));
            String composition = cleanValue(ucMatcher.group(2));
            if (isValidUnit(unit) && isValidComposition(composition)) {
                result.add(String.format("unit:%s  composition:%s;", unit, composition));
            }
        }

        // Поиск UniversalEditForm
        Matcher ueMatcher = UNIVERSAL_EDIT_FORM_PATTERN.matcher(content);
        while (ueMatcher.find()) {
            String unit = cleanValue(ueMatcher.group(1));
            String method = cleanValue(ueMatcher.group(2));
            if (isValidUnit(unit) && isValidMethod(method)) {
                result.add(String.format("unit:%s  method:%s;", unit, method));
            }
        }

        // Общий поиск (для различных вариаций)
        Matcher generalMatcher = JS_UNIVERSAL_PATTERN.matcher(content);
        while (generalMatcher.find()) {
            String unit = cleanValue(generalMatcher.group(1));
            String value = cleanValue(generalMatcher.group(2));

            // Проверяем, не добавлена ли уже эта композиция
            String compositionKey = String.format("unit:%s  composition:%s;", unit, value);
            String methodKey = String.format("unit:%s  method:%s;", unit, value);

            if (isValidUnit(unit) && isValidComposition(value) &&
                    !result.contains(compositionKey) && !result.contains(methodKey)) {
                result.add(compositionKey);
            }
        }

        return result;
    }

    private String cleanValue(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        cleaned = cleaned.replaceAll("^['\"]|['\"]$", "");
        cleaned = cleaned.replaceAll("\\+.*$", ""); // Убираем конкатенацию
        return cleaned;
    }

    private boolean isValidUnit(String unit) {
        if (unit == null || unit.isEmpty()) return false;
        // Пропускаем переменные и выражения
        if (unit.contains("+") || unit.contains("getVar") ||
                unit.contains("getValue") || unit.contains("this.")) {
            return false;
        }
        // Пропускаем JavaScript ключевые слова
        if (unit.equals("null") || unit.equals("undefined") ||
                unit.equals("true") || unit.equals("false")) {
            return false;
        }
        return true;
    }

    private boolean isValidComposition(String composition) {
        if (composition == null || composition.isEmpty()) return false;
        // Пропускаем выражения
        if (composition.contains("+") || composition.contains("getVar") ||
                composition.contains("getValue") || composition.contains("this.")) {
            return false;
        }
        return true;
    }

    private boolean isValidMethod(String method) {
        if (method == null || method.isEmpty()) return false;
        // Методы обычно имеют формат LIKE_THIS или likeThis
        if (method.contains("+") || method.contains(" ")) return false;
        return true;
    }
}