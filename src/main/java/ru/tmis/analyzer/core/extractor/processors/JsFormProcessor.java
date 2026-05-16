// core/extractor/processors/JsFormProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Извлечение JS форм (openWindow, openD3Form, Form.showModal)
 */
public class JsFormProcessor implements IXmlProcessor {

    private static final Pattern OPEND3FORM_PATTERN = Pattern.compile(
            "openD3Form\\s*\\(\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OPENWINDOW_PATTERN = Pattern.compile(
            "openWindow\\s*\\(\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OPEND3FORM_OBJECT_PATTERN = Pattern.compile(
            "openD3Form\\s*\\(\\s*\\{\\s*name\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OPENWINDOW_OBJECT_PATTERN = Pattern.compile(
            "openWindow\\s*\\(\\s*\\{\\s*name\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SHOWMODAL_PATTERN = Pattern.compile(
            "Form\\.showModal\\s*\\(\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SHOWMODAL_OBJECT_PATTERN = Pattern.compile(
            "Form\\.showModal\\s*\\(\\s*\\{\\s*name\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ANY_FORM_PATTERN = Pattern.compile(
            "['\"]([A-Za-z0-9_/]+\\.frm)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CDATA_PATTERN = Pattern.compile(
            "<!\\[CDATA\\[(.*?)\\]\\]>",
            Pattern.DOTALL
    );

    @Override
    public String getName() {
        return "JsFormProcessor";
    }

    @Override
    public int getPriority() {
        return 25;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        String html = doc.html();
        Set<String> foundForms = new LinkedHashSet<>();
        Set<String> reportForms = new LinkedHashSet<>();

        // Извлекаем все CDATA секции
        Matcher cdataMatcher = CDATA_PATTERN.matcher(html);
        while (cdataMatcher.find()) {
            String jsContent = cdataMatcher.group(1);
            processJsContent(jsContent, foundForms, reportForms);
        }

        // Обрабатываем весь HTML
        processJsContent(html, foundForms, reportForms);

        // Добавляем обычные формы
        for (String form : foundForms) {
            String normalizedPath = normalizeFormPath(form);
            if (normalizedPath != null && !normalizedPath.isEmpty()) {
                formInfo.addJsForm(normalizedPath);
            }
        }

        // Добавляем формы отчетов
        for (String reportForm : reportForms) {
            String normalizedPath = reportForm;
            if (!normalizedPath.endsWith(".frm") && !normalizedPath.endsWith(".dfrm")) {
                normalizedPath = normalizedPath + ".frm";
            }
            formInfo.addReport(normalizedPath);
        }
    }

    private void processJsContent(String jsContent, Set<String> foundForms, Set<String> reportForms) {
        if (jsContent == null) return;

        // openD3Form('...')
        findAndProcess(OPEND3FORM_PATTERN, jsContent, p -> processFormPath(p, foundForms, reportForms));
        // openWindow('...')
        findAndProcess(OPENWINDOW_PATTERN, jsContent, p -> processFormPath(p, foundForms, reportForms));
        // openD3Form({name: '...'})
        findAndProcess(OPEND3FORM_OBJECT_PATTERN, jsContent, p -> processFormPath(p, foundForms, reportForms));
        // openWindow({name: '...'})
        findAndProcess(OPENWINDOW_OBJECT_PATTERN, jsContent, p -> processFormPath(p, foundForms, reportForms));
        // Form.showModal('...')
        findAndProcess(SHOWMODAL_PATTERN, jsContent, p -> processFormPath(p, foundForms, reportForms));
        // Form.showModal({name: '...'})
        findAndProcess(SHOWMODAL_OBJECT_PATTERN, jsContent, p -> processFormPath(p, foundForms, reportForms));
        // Любые .frm в кавычках
        findAndProcess(ANY_FORM_PATTERN, jsContent, p -> processFormPath(p, foundForms, reportForms));
    }

    private void findAndProcess(Pattern pattern, String content, java.util.function.Consumer<String> processor) {
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String value = matcher.group(1).trim();
            if (value != null && !value.isEmpty()) {
                processor.accept(value);
            }
        }
    }

    private void processFormPath(String formPath, Set<String> foundForms, Set<String> reportForms) {
        if (!isValidFormPath(formPath)) return;

        if (isReportFormPath(formPath)) {
            reportForms.add(formPath);
        } else if (!isUniversalForm(formPath)) {
            foundForms.add(formPath);
        }
    }

    private boolean isValidFormPath(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        String lowerPath = path.toLowerCase();

        String[] invalidPatterns = {
                "function", "setvalue", "executeaction", "getcontrol", "printreportbycode",
                "sysdate", "refreshdataset", "getdataset", "showalert", "confirm",
                "closewindow", "addlistener", "getvar", "setvar", "modalresult",
                "getproperty", "sunit", "composition", "height", "width", "onclose",
                "typeof", "undefined", "null", "true", "false", "components_m2", "components_d3"
        };

        for (String pattern : invalidPatterns) {
            if (lowerPath.contains(pattern)) return false;
        }
        return true;
    }

    private boolean isSystemCall(String path) {
        String lowerPath = path.toLowerCase();
        return lowerPath.contains("components_m2") || lowerPath.contains("components_d3") ||
                lowerPath.contains("executeaction") || lowerPath.contains("refreshdataset");
    }

    private boolean isUniversalForm(String path) {
        return path.contains("UniversalEditForm/UniversalEditForm") ||
                path.contains("UniversalComposition/UniversalComposition");
    }

    private boolean isReportFormPath(String formPath) {
        if (formPath == null) return false;
        return formPath.toLowerCase().replace('\\', '/').startsWith("reports/");
    }

    private String normalizeFormPath(String formPath) {
        if (formPath == null || formPath.trim().isEmpty()) return null;
        String normalized = formPath.trim().replaceAll("^['\"]|['\"]$", "");
        if (normalized.startsWith("/")) normalized = normalized.substring(1);
        if (normalized.contains("+") || normalized.contains("getVar") || normalized.contains("$")) return null;
        if (!normalized.endsWith(".frm") && !normalized.endsWith(".dfrm")) {
            normalized = normalized + ".frm";
        }
        if (!normalized.contains("/")) return null;
        return normalized;
    }
}