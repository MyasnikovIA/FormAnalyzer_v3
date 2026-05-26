// core/extractor/processors/SystemCompositionProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import ru.tmis.analyzer.core.model.FormInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Извлечение композиций System/composition из различных вызовов
 */
public class SystemCompositionProcessor implements IXmlProcessor {

    // Паттерн для openForm('System/composition', { request: { unit: '...', composition: '...' } })
    private static final Pattern OPEN_FORM_PATTERN = Pattern.compile(
            "openForm\\s*\\(\\s*['\"]System/composition['\"],\\s*\\{\\s*request:\\s*\\{\\s*unit:\\s*['\"]([^'\"]+)['\"],\\s*composition:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для D3Api.showForm('System/composition', ..., { request: { unit: '...', composition: '...' } })
    private static final Pattern D3API_SHOWFORM_PATTERN = Pattern.compile(
            "D3Api\\.showForm\\s*\\(\\s*['\"]System/composition['\"],\\s*[^,]*,\\s*\\{\\s*request:\\s*\\{\\s*unit:\\s*['\"]([^'\"]+)['\"],\\s*composition:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для openD3Form('System/composition', ..., { request: { unit: '...', composition: '...' } })
    private static final Pattern OPEND3FORM_PATTERN = Pattern.compile(
            "openD3Form\\s*\\(\\s*['\"]System/composition['\"],\\s*[^,]*,\\s*\\{\\s*request:\\s*\\{\\s*unit:\\s*['\"]([^'\"]+)['\"],\\s*composition:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для D3Api.UnitEditCtrl.callComposition
    private static final Pattern CALL_COMPOSITION_PATTERN = Pattern.compile(
            "D3Api\\.UnitEditCtrl\\.callComposition\\s*\\([^,]+,\\s*['\"]System/composition['\"],\\s*\\{[^}]*request:\\s*\\{\\s*unit:\\s*['\"]([^'\"]+)['\"],\\s*composition:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для более простых вызовов
    private static final Pattern SIMPLE_SYSTEM_PATTERN = Pattern.compile(
            "['\"]System/composition['\"][^}]*?(?:unit|UNIT)\\s*:\\s*['\"]([^'\"]+)['\"][^}]*?(?:composition|COMPOSITION)\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для поиска в CDATA
    private static final Pattern CDATA_PATTERN = Pattern.compile(
            "<!\\[CDATA\\[(.*?)\\]\\]>",
            Pattern.DOTALL
    );

    @Override
    public String getName() {
        return "SystemCompositionProcessor";
    }

    @Override
    public int getPriority() {
        return 60;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        String html = doc.html();

        Set<SystemCompositionInfo> compositions = new LinkedHashSet<>();

        // Извлекаем все CDATA секции
        Matcher cdataMatcher = CDATA_PATTERN.matcher(html);
        while (cdataMatcher.find()) {
            String jsContent = cdataMatcher.group(1);
            compositions.addAll(extractFromJs(jsContent));
        }

        // Обрабатываем весь HTML
        compositions.addAll(extractFromJs(html));

        // Добавляем найденные композиции в FormInfo
        for (SystemCompositionInfo info : compositions) {
            String formatted = info.format();
            switch (info.getType()) {
                case OPEN_FORM:
                    formInfo.addOpenFormComposition(formatted);
                    break;
                case D3API_SHOWFORM:
                    formInfo.addD3ApiShowFormComposition(formatted);
                    break;
                case OPEND3FORM:
                    formInfo.addOpenD3FormComposition(formatted);
                    break;
                case CALL_COMPOSITION:
                case SIMPLE:
                default:
                    formInfo.addJsUnitComposition(formatted);
                    break;
            }
        }
    }

    private Set<SystemCompositionInfo> extractFromJs(String jsContent) {
        Set<SystemCompositionInfo> result = new LinkedHashSet<>();

        if (jsContent == null || jsContent.isEmpty()) {
            return result;
        }

        // openForm System/composition
        Matcher openFormMatcher = OPEN_FORM_PATTERN.matcher(jsContent);
        while (openFormMatcher.find()) {
            String unit = cleanValue(openFormMatcher.group(1));
            String composition = cleanValue(openFormMatcher.group(2));
            if (isValidUnit(unit) && isValidComposition(composition)) {
                result.add(new SystemCompositionInfo(
                        SystemCompositionType.OPEN_FORM, unit, composition, null
                ));
            }
        }

        // D3Api.showForm System/composition
        Matcher d3ApiMatcher = D3API_SHOWFORM_PATTERN.matcher(jsContent);
        while (d3ApiMatcher.find()) {
            String unit = cleanValue(d3ApiMatcher.group(1));
            String composition = cleanValue(d3ApiMatcher.group(2));
            if (isValidUnit(unit) && isValidComposition(composition)) {
                result.add(new SystemCompositionInfo(
                        SystemCompositionType.D3API_SHOWFORM, unit, composition, null
                ));
            }
        }

        // openD3Form System/composition
        Matcher openD3FormMatcher = OPEND3FORM_PATTERN.matcher(jsContent);
        while (openD3FormMatcher.find()) {
            String unit = cleanValue(openD3FormMatcher.group(1));
            String composition = cleanValue(openD3FormMatcher.group(2));
            if (isValidUnit(unit) && isValidComposition(composition)) {
                result.add(new SystemCompositionInfo(
                        SystemCompositionType.OPEND3FORM, unit, composition, null
                ));
            }
        }

        // callComposition
        Matcher callMatcher = CALL_COMPOSITION_PATTERN.matcher(jsContent);
        while (callMatcher.find()) {
            String unit = cleanValue(callMatcher.group(1));
            String composition = cleanValue(callMatcher.group(2));
            if (isValidUnit(unit) && isValidComposition(composition)) {
                result.add(new SystemCompositionInfo(
                        SystemCompositionType.CALL_COMPOSITION, unit, composition, null
                ));
            }
        }

        // Простые вызовы
        Matcher simpleMatcher = SIMPLE_SYSTEM_PATTERN.matcher(jsContent);
        while (simpleMatcher.find()) {
            String unit = cleanValue(simpleMatcher.group(1));
            String composition = cleanValue(simpleMatcher.group(2));
            if (isValidUnit(unit) && isValidComposition(composition)) {
                result.add(new SystemCompositionInfo(
                        SystemCompositionType.SIMPLE, unit, composition, null
                ));
            }
        }

        return result;
    }

    private String cleanValue(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        cleaned = cleaned.replaceAll("^['\"]|['\"]$", "");
        cleaned = cleaned.replaceAll("\\+.*$", "");
        return cleaned;
    }

    private boolean isValidUnit(String unit) {
        if (unit == null || unit.isEmpty()) return false;
        if (unit.contains("+") || unit.contains("getVar") ||
                unit.contains("getValue") || unit.contains("this.")) {
            return false;
        }
        return true;
    }

    private boolean isValidComposition(String composition) {
        if (composition == null || composition.isEmpty()) return false;
        if (composition.contains("+") || composition.contains("getVar") ||
                composition.contains("getValue")) {
            return false;
        }
        return true;
    }

    /**
     * Типы вызовов System/composition
     */
    public enum SystemCompositionType {
        OPEN_FORM("openForm"),
        D3API_SHOWFORM("D3Api.showForm"),
        OPEND3FORM("openD3Form"),
        CALL_COMPOSITION("callComposition"),
        SIMPLE("simple");

        private final String displayName;
        SystemCompositionType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    /**
     * Информация о композиции System/composition
     */
    public static class SystemCompositionInfo {
        private final SystemCompositionType type;
        private final String unit;
        private final String composition;
        private final String method;

        public SystemCompositionInfo(SystemCompositionType type, String unit, String composition, String method) {
            this.type = type;
            this.unit = unit;
            this.composition = composition;
            this.method = method;
        }

        public SystemCompositionType getType() { return type; }
        public String getUnit() { return unit; }
        public String getComposition() { return composition; }
        public String getMethod() { return method; }

        public String format() {
            if (composition != null && !composition.isEmpty()) {
                return String.format("        unit:%s  composition:%s;", unit, composition);
            } else if (method != null && !method.isEmpty()) {
                return String.format("        unit:%s  method:%s;", unit, method);
            }
            return String.format("        unit:%s;", unit);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SystemCompositionInfo that = (SystemCompositionInfo) o;
            return java.util.Objects.equals(unit, that.unit) &&
                    java.util.Objects.equals(composition, that.composition) &&
                    java.util.Objects.equals(method, that.method);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(unit, composition, method);
        }
    }
}