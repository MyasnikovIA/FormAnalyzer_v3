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

    // ========== СТАРЫЙ ФОРМАТ (UniversalComposition) ==========

    // Паттерн 1: unit сначала, composition потом
    private static final Pattern UNIVERSAL_COMPOSITION_PATTERN = Pattern.compile(
            "openWindow\\s*\\(\\s*\\{\\s*name\\s*:\\s*['\"]UniversalComposition/UniversalComposition['\"][^}]*?" +
                    "(?:unit|UNIT)\\s*:\\s*([^,}]+)[^}]*?" +
                    "(?:composition|COMPOSITION)\\s*:\\s*([^,}]+)[^}]*\\}\\s*(?:,|,?\\)?)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн 2: composition сначала, unit потом (обратный порядок)
    private static final Pattern UNIVERSAL_COMPOSITION_PATTERN2 = Pattern.compile(
            "openWindow\\s*\\(\\s*\\{\\s*name\\s*:\\s*['\"]UniversalComposition/UniversalComposition['\"][^}]*?" +
                    "(?:composition|COMPOSITION)\\s*:\\s*([^,}]+)[^}]*?" +
                    "(?:unit|UNIT)\\s*:\\s*([^,}]+)[^}]*\\}\\s*(?:,|,?\\)?)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн 3: упрощённый поиск (без openWindow)
    private static final Pattern UNIVERSAL_COMPOSITION_SIMPLE = Pattern.compile(
            "UniversalComposition/UniversalComposition[^}]*?(?:unit|UNIT)\\s*:\\s*([^,}]+)[^}]*?(?:composition|COMPOSITION)\\s*:\\s*([^,}]+)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // ========== НОВЫЙ ФОРМАТ (UniversalEditForm) ==========

    // Паттерн для openForm с UniversalEditForm
    private static final Pattern UNIVERSAL_EDIT_FORM_OPENFORM_PATTERN = Pattern.compile(
            "openForm\\s*\\(\\s*['\"]UniversalEditForm/UniversalEditForm['\"],\\s*\\{[^}]*?(?:unit|UNIT)\\s*:\\s*['\"]([^'\"]+)['\"][^}]*?(?:method|METHOD)\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для openWindow с UniversalEditForm (объектный синтаксис, unit сначала)
    private static final Pattern UNIVERSAL_EDIT_FORM_OPENWINDOW_PATTERN = Pattern.compile(
            "openWindow\\s*\\(\\s*\\{\\s*name\\s*:\\s*['\"]UniversalEditForm/UniversalEditForm['\"][^}]*?(?:unit|UNIT)\\s*:\\s*['\"]([^'\"]+)['\"][^}]*?(?:method|METHOD)\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для openWindow с UniversalEditForm (method сначала)
    private static final Pattern UNIVERSAL_EDIT_FORM_OPENWINDOW_PATTERN2 = Pattern.compile(
            "openWindow\\s*\\(\\s*\\{\\s*name\\s*:\\s*['\"]UniversalEditForm/UniversalEditForm['\"][^}]*?(?:method|METHOD)\\s*:\\s*['\"]([^'\"]+)['\"][^}]*?(?:unit|UNIT)\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для openD3Form с UniversalEditForm
    private static final Pattern UNIVERSAL_EDIT_FORM_OPEND3FORM_PATTERN = Pattern.compile(
            "openD3Form\\s*\\(\\s*\\{\\s*name\\s*:\\s*['\"]UniversalEditForm/UniversalEditForm['\"][^}]*?(?:unit|UNIT)\\s*:\\s*['\"]([^'\"]+)['\"][^}]*?(?:method|METHOD)\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для openD3Form с UniversalEditForm (method сначала)
    private static final Pattern UNIVERSAL_EDIT_FORM_OPEND3FORM_PATTERN2 = Pattern.compile(
            "openD3Form\\s*\\(\\s*\\{\\s*name\\s*:\\s*['\"]UniversalEditForm/UniversalEditForm['\"][^}]*?(?:method|METHOD)\\s*:\\s*['\"]([^'\"]+)['\"][^}]*?(?:unit|UNIT)\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Упрощённый паттерн для поиска любых упоминаний UniversalEditForm
    private static final Pattern UNIVERSAL_EDIT_FORM_SIMPLE = Pattern.compile(
            "UniversalEditForm/UniversalEditForm[^}]*?(?:unit|UNIT)\\s*:\\s*['\"]([^'\"]+)['\"][^}]*?(?:method|METHOD)\\s*:\\s*['\"]([^'\"]+)['\"]",
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

        // 1. CDATA секции
        Matcher cdataMatcher = CDATA_PATTERN.matcher(html);
        while (cdataMatcher.find()) {
            compositions.addAll(extractFromJsContent(cdataMatcher.group(1)));
        }

        // 2. Все теги <component cmptype="Script"> без CDATA
        Pattern scriptPattern = Pattern.compile(
                "<component\\s+cmptype\\s*=\\s*[\"']Script[\"'][^>]*>(.*?)</component>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher scriptMatcher = scriptPattern.matcher(html);
        while (scriptMatcher.find()) {
            String scriptBody = scriptMatcher.group(1);
            if (!scriptBody.contains("CDATA")) {
                compositions.addAll(extractFromJsContent(scriptBody));
            }
        }

        // 3. Прямой поиск по всему HTML (на случай, если JS вне компонентов)
        compositions.addAll(extractFromJsContent(html));

        // Добавляем в FormInfo (дублируем в оба поля для совместимости с отчётом)
        for (String comp : compositions) {
            formInfo.addJsUnitComposition(comp);
            // formInfo.addUnitComposition(comp);   // <-- чтобы попали в существующий раздел отчёта
        }
    }

    private Set<String> extractFromJsContent(String content) {
        Set<String> result = new LinkedHashSet<>();

        if (content == null || content.isEmpty()) {
            return result;
        }

        // ========== 1. СТАРЫЙ ФОРМАТ (UniversalComposition) ==========

        // Паттерн 1: unit сначала
        Matcher ucMatcher = UNIVERSAL_COMPOSITION_PATTERN.matcher(content);
        while (ucMatcher.find()) {
            String unit = cleanValue(ucMatcher.group(1));
            String composition = cleanValue(ucMatcher.group(2));
            if (isValidUnit(unit)) {
                result.add(String.format("unit:%s  composition:%s;", unit,
                        isValidComposition(composition) ? composition : "DEFAULT"));
                System.out.println("[DEBUG] Найдена UniversalComposition (unit first): unit=" + unit + ", composition=" + composition);
            }
        }

        // Паттерн 2: composition сначала
        Matcher ucMatcher2 = UNIVERSAL_COMPOSITION_PATTERN2.matcher(content);
        while (ucMatcher2.find()) {
            String composition = cleanValue(ucMatcher2.group(1));
            String unit = cleanValue(ucMatcher2.group(2));
            if (isValidUnit(unit)) {
                result.add(String.format("unit:%s  composition:%s;", unit,
                        isValidComposition(composition) ? composition : "DEFAULT"));
                System.out.println("[DEBUG] Найдена UniversalComposition (composition first): unit=" + unit + ", composition=" + composition);
            }
        }

        // Паттерн 3: упрощённый поиск
        if (result.isEmpty()) {
            Matcher simpleMatcher = UNIVERSAL_COMPOSITION_SIMPLE.matcher(content);
            while (simpleMatcher.find()) {
                String unit = cleanValue(simpleMatcher.group(1));
                String composition = cleanValue(simpleMatcher.group(2));
                if (isValidUnit(unit)) {
                    result.add(String.format("unit:%s  composition:%s;", unit,
                            isValidComposition(composition) ? composition : "DEFAULT"));
                    System.out.println("[DEBUG] Найдена UniversalComposition (simple): unit=" + unit + ", composition=" + composition);
                }
            }
        }

        // ========== 2. НОВЫЙ ФОРМАТ (UniversalEditForm) ==========

        // openForm
        Matcher openFormMatcher = UNIVERSAL_EDIT_FORM_OPENFORM_PATTERN.matcher(content);
        while (openFormMatcher.find()) {
            String unit = cleanValue(openFormMatcher.group(1));
            String method = cleanValue(openFormMatcher.group(2));
            if (isValidUnit(unit) && isValidMethod(method)) {
                result.add(String.format("unit:%s  method:%s;", unit, method));
                System.out.println("[DEBUG] Найдена UniversalEditForm (openForm): unit=" + unit + ", method=" + method);
            }
        }

        // openWindow с unit сначала
        Matcher openWindowMatcher = UNIVERSAL_EDIT_FORM_OPENWINDOW_PATTERN.matcher(content);
        while (openWindowMatcher.find()) {
            String unit = cleanValue(openWindowMatcher.group(1));
            String method = cleanValue(openWindowMatcher.group(2));
            if (isValidUnit(unit) && isValidMethod(method)) {
                result.add(String.format("unit:%s  method:%s;", unit, method));
                System.out.println("[DEBUG] Найдена UniversalEditForm (openWindow, unit first): unit=" + unit + ", method=" + method);
            }
        }

        // openWindow с method сначала
        Matcher openWindowMatcher2 = UNIVERSAL_EDIT_FORM_OPENWINDOW_PATTERN2.matcher(content);
        while (openWindowMatcher2.find()) {
            String method = cleanValue(openWindowMatcher2.group(1));
            String unit = cleanValue(openWindowMatcher2.group(2));
            if (isValidUnit(unit) && isValidMethod(method)) {
                result.add(String.format("unit:%s  method:%s;", unit, method));
                System.out.println("[DEBUG] Найдена UniversalEditForm (openWindow, method first): unit=" + unit + ", method=" + method);
            }
        }

        // openD3Form с unit сначала
        Matcher openD3FormMatcher = UNIVERSAL_EDIT_FORM_OPEND3FORM_PATTERN.matcher(content);
        while (openD3FormMatcher.find()) {
            String unit = cleanValue(openD3FormMatcher.group(1));
            String method = cleanValue(openD3FormMatcher.group(2));
            if (isValidUnit(unit) && isValidMethod(method)) {
                result.add(String.format("unit:%s  method:%s;", unit, method));
                System.out.println("[DEBUG] Найдена UniversalEditForm (openD3Form, unit first): unit=" + unit + ", method=" + method);
            }
        }

        // openD3Form с method сначала
        Matcher openD3FormMatcher2 = UNIVERSAL_EDIT_FORM_OPEND3FORM_PATTERN2.matcher(content);
        while (openD3FormMatcher2.find()) {
            String method = cleanValue(openD3FormMatcher2.group(1));
            String unit = cleanValue(openD3FormMatcher2.group(2));
            if (isValidUnit(unit) && isValidMethod(method)) {
                result.add(String.format("unit:%s  method:%s;", unit, method));
                System.out.println("[DEBUG] Найдена UniversalEditForm (openD3Form, method first): unit=" + unit + ", method=" + method);
            }
        }

        // Упрощённый поиск UniversalEditForm
        if (result.isEmpty()) {
            Matcher simpleMatcher = UNIVERSAL_EDIT_FORM_SIMPLE.matcher(content);
            while (simpleMatcher.find()) {
                String unit = cleanValue(simpleMatcher.group(1));
                String method = cleanValue(simpleMatcher.group(2));
                if (isValidUnit(unit) && isValidMethod(method)) {
                    result.add(String.format("unit:%s  method:%s;", unit, method));
                    System.out.println("[DEBUG] Найдена UniversalEditForm (simple): unit=" + unit + ", method=" + method);
                }
            }
        }

        return result;
    }

    private String cleanValue(String value) {
        if (value == null) return null;
        String cleaned = value.trim();

        // Убираем кавычки
        if (cleaned.startsWith("'") && cleaned.endsWith("'")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        // Убираем конкатенацию и лишние пробелы
        cleaned = cleaned.replaceAll("\\+.*$", "");
        cleaned = cleaned.replaceAll("\\s+", " ");

        // Убираем точку с запятой в конце
        if (cleaned.endsWith(";")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        return cleaned.trim();
    }

    private boolean isValidUnit(String unit) {
        if (unit == null || unit.isEmpty()) return false;

        // Пропускаем переменные и выражения
        if (unit.contains("+") || unit.contains("getVar") ||
                unit.contains("getValue") || unit.contains("this.") ||
                unit.contains("||") || unit.contains("&&")) {
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
                composition.contains("getValue") || composition.contains("this.") ||
                composition.contains("||") || composition.contains("&&")) {
            return false;
        }

        // Убираем точку с запятой для проверки
        String clean = composition.replaceAll(";$", "");
        return !clean.isEmpty();
    }

    private boolean isValidMethod(String method) {
        if (method == null || method.isEmpty()) return false;

        // Методы обычно имеют формат LIKE_THIS или likeThis
        if (method.contains("+") || method.contains(" ") ||
                method.contains("getVar") || method.contains("getValue")) {
            return false;
        }

        // Убираем точку с запятой для проверки
        String clean = method.replaceAll(";$", "");
        return !clean.isEmpty();
    }
}