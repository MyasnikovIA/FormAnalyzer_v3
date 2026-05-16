// core/extractor/processors/D3ApiShowFormProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Извлечение форм из D3Api.showForm вызовов
 */
public class D3ApiShowFormProcessor implements IXmlProcessor {

    // Паттерн для D3Api.showForm со строковым параметром: D3Api.showForm('form_path', ...)
    private static final Pattern D3API_STRING_PATTERN = Pattern.compile(
            "D3Api\\.showForm\\s*\\(\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для D3Api.showForm с объектом: D3Api.showForm({name: 'form_path', ...})
    private static final Pattern D3API_OBJECT_PATTERN = Pattern.compile(
            "D3Api\\.showForm\\s*\\(\\s*\\{\\s*name\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для D3Api.showForm с переменной в параметрах
    private static final Pattern D3API_COMPLEX_PATTERN = Pattern.compile(
            "D3Api\\.showForm\\s*\\(\\s*\\{[^}]*\\}\\s*,\\s*\\{[^}]*\\}\\s*\\)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для извлечения имени формы из сложного вызова
    private static final Pattern NAME_IN_OBJECT_PATTERN = Pattern.compile(
            "name\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.CASE_INSENSITIVE
    );

    // CDATA паттерн
    private static final Pattern CDATA_PATTERN = Pattern.compile(
            "<!\\[CDATA\\[(.*?)\\]\\]>",
            Pattern.DOTALL
    );

    @Override
    public String getName() {
        return "D3ApiShowFormProcessor";
    }

    @Override
    public int getPriority() {
        return 65;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        String html = doc.html();
        Set<String> forms = new LinkedHashSet<>();

        // Извлекаем все CDATA секции
        Matcher cdataMatcher = CDATA_PATTERN.matcher(html);
        while (cdataMatcher.find()) {
            String jsContent = cdataMatcher.group(1);
            forms.addAll(extractFromJs(jsContent));
        }

        // Также обрабатываем весь HTML
        forms.addAll(extractFromJs(html));

        // Добавляем найденные формы
        for (String form : forms) {
            String normalizedPath = normalizeFormPath(form);
            if (normalizedPath != null && !normalizedPath.isEmpty()) {
                formInfo.addSubForm(normalizedPath);
            }
        }
    }

    private Set<String> extractFromJs(String jsContent) {
        Set<String> result = new LinkedHashSet<>();

        if (jsContent == null || jsContent.isEmpty()) {
            return result;
        }

        // Поиск простых строковых вызовов
        Matcher stringMatcher = D3API_STRING_PATTERN.matcher(jsContent);
        while (stringMatcher.find()) {
            String formPath = cleanPath(stringMatcher.group(1));
            if (isValidFormPath(formPath)) {
                result.add(formPath);
            }
        }

        // Поиск вызовов с объектом
        Matcher objectMatcher = D3API_OBJECT_PATTERN.matcher(jsContent);
        while (objectMatcher.find()) {
            String formPath = cleanPath(objectMatcher.group(1));
            if (isValidFormPath(formPath)) {
                result.add(formPath);
            }
        }

        // Поиск сложных вызовов
        Matcher complexMatcher = D3API_COMPLEX_PATTERN.matcher(jsContent);
        while (complexMatcher.find()) {
            String fullMatch = complexMatcher.group();
            Matcher nameMatcher = NAME_IN_OBJECT_PATTERN.matcher(fullMatch);
            if (nameMatcher.find()) {
                String formPath = cleanPath(nameMatcher.group(1));
                if (isValidFormPath(formPath)) {
                    result.add(formPath);
                }
            }
        }

        return result;
    }

    private String cleanPath(String path) {
        if (path == null) return null;
        String cleaned = path.trim();
        cleaned = cleaned.replaceAll("^['\"]|['\"]$", "");
        cleaned = cleaned.replaceAll("\\+.*$", "");
        return cleaned;
    }

    private boolean isValidFormPath(String path) {
        if (path == null || path.isEmpty()) return false;

        // Пропускаем переменные
        if (path.contains("+") || path.contains("getVar") ||
                path.contains("getValue") || path.contains("this.") ||
                path.contains("$")) {
            return false;
        }

        // Пропускаем системные вызовы
        if (path.contains("function") || path.contains("return") ||
                path.contains("undefined") || path.contains("null")) {
            return false;
        }

        return true;
    }

    private String normalizeFormPath(String formPath) {
        if (formPath == null || formPath.isEmpty()) return null;

        String normalized = formPath;

        // Убираем ведущий слеш
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        // Добавляем .frm если нет расширения
        if (!normalized.endsWith(".frm") && !normalized.endsWith(".dfrm")) {
            normalized = normalized + ".frm";
        }

        // Добавляем префикс Forms/ если это не UserForms
        if (!normalized.startsWith("UserForms") && !normalized.startsWith("Forms/")) {
            normalized = "Forms/" + normalized;
        }

        return "/" + normalized;
    }
}