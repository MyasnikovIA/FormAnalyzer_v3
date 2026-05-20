// core/extractor/processors/ReportProcessor.java

package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.SqlInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Извлечение отчетов из форм
 * Отчеты извлекаются из:
 * 1. SQL запросов (внутри DataSet/Action компонентов)
 * 2. JS вызовов внутри Action компонентов
 * 3. CDATA секций (JS код)
 * 4. Прямых вызовов printReportByCode в любом тексте
 */
public class ReportProcessor implements IXmlProcessor {

    // Паттерн для printReportByCode('CODE')
    private static final Pattern PRINT_REPORT_PATTERN = Pattern.compile(
            "printReportByCode\\s*\\(\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для printReportByCode("CODE")
    private static final Pattern PRINT_REPORT_PATTERN_DOUBLE = Pattern.compile(
            "printReportByCode\\s*\\(\\s*\"([^\"]+)\"",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для Reports/путь/к/отчёту.frm
    private static final Pattern REPORTS_PATH_PATTERN = Pattern.compile(
            "['\"]Reports/([^'\"]+\\.frm)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для CDATA секций
    private static final Pattern CDATA_PATTERN = Pattern.compile(
            "<!\\[CDATA\\[(.*?)\\]\\]>",
            Pattern.DOTALL
    );

    @Override
    public String getName() {
        return "ReportProcessor";
    }

    @Override
    public int getPriority() {
        return 70;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        Set<String> reports = new LinkedHashSet<>();

        // 1. Ищем отчеты в SQL запросах (уже сохранены в formInfo)
        for (SqlInfo sql : formInfo.getSqlQueries()) {
            String sqlContent = sql.getSqlContent();
            if (sqlContent != null) {
                reports.addAll(extractFromText(sqlContent));
            }
        }

        // 2. Ищем отчеты в Action компонентах (которые не являются SQL)
        Elements actions = doc.select("component[cmptype=Action], cmpAction");
        for (Element action : actions) {
            // Пропускаем Action, которые содержат SQL (уже обработаны выше)
            if (hasSqlContent(action)) {
                continue;
            }
            String actionHtml = action.html();
            if (actionHtml != null) {
                reports.addAll(extractFromText(actionHtml));
            }
        }

        // 3. Ищем отчеты в CDATA секциях (JS код)
        String html = doc.html();
        Matcher cdataMatcher = CDATA_PATTERN.matcher(html);
        while (cdataMatcher.find()) {
            String jsContent = cdataMatcher.group(1);
            if (jsContent != null) {
                reports.addAll(extractFromText(jsContent));
            }
        }

        // 4. Ищем отчеты во всем HTML (на случай, если вызовы вне CDATA)
        reports.addAll(extractFromText(html));

        // 5. Добавляем найденные отчеты
        for (String report : reports) {
            formInfo.addReport(report);
        }

        if (!reports.isEmpty()) {
            System.out.println("[ReportProcessor] Найдено отчетов: " + reports.size() + " " + reports);
        }
    }

    /**
     * Проверка, содержит ли компонент SQL запрос
     */
    private boolean hasSqlContent(Element element) {
        String html = element.html();
        if (html == null) return false;

        // Проверяем наличие CDATA с SQL
        Matcher cdataMatcher = CDATA_PATTERN.matcher(html);
        while (cdataMatcher.find()) {
            String content = cdataMatcher.group(1);
            if (isSqlContent(content)) {
                return true;
            }
        }

        // Проверяем прямой текст (без CDATA)
        String text = element.ownText();
        return isSqlContent(text);
    }

    /**
     * Проверка, является ли содержимое SQL запросом
     */
    private boolean isSqlContent(String content) {
        if (content == null) return false;
        String lower = content.toLowerCase().trim();
        return lower.startsWith("select") || lower.startsWith("insert") ||
                lower.startsWith("update") || lower.startsWith("delete") ||
                lower.startsWith("begin") || lower.startsWith("declare") ||
                lower.contains("into") || lower.contains("from");
    }

    /**
     * Извлечение отчетов из текста
     */
    private Set<String> extractFromText(String text) {
        Set<String> result = new LinkedHashSet<>();

        if (text == null || text.isEmpty()) {
            return result;
        }

        // printReportByCode('REPORT_CODE')
        Matcher printMatcher = PRINT_REPORT_PATTERN.matcher(text);
        while (printMatcher.find()) {
            String reportCode = cleanValue(printMatcher.group(1));
            if (isValidReportCode(reportCode)) {
                result.add(reportCode);
                System.out.println("[ReportProcessor] Найден отчет (одинарные кавычки): " + reportCode);
            }
        }

        // printReportByCode("REPORT_CODE")
        Matcher printMatcherDouble = PRINT_REPORT_PATTERN_DOUBLE.matcher(text);
        while (printMatcherDouble.find()) {
            String reportCode = cleanValue(printMatcherDouble.group(1));
            if (isValidReportCode(reportCode)) {
                result.add(reportCode);
                System.out.println("[ReportProcessor] Найден отчет (двойные кавычки): " + reportCode);
            }
        }

        // Reports/some_report.frm
        Matcher pathMatcher = REPORTS_PATH_PATTERN.matcher(text);
        while (pathMatcher.find()) {
            String reportPath = pathMatcher.group(1);
            if (reportPath != null && !reportPath.isEmpty()) {
                result.add("/Reports/" + reportPath);
                System.out.println("[ReportProcessor] Найден отчет по пути: /Reports/" + reportPath);
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

    private boolean isValidReportCode(String code) {
        if (code == null || code.isEmpty()) return false;
        // Пропускаем переменные и выражения
        if (code.contains("+") || code.contains("getVar") ||
                code.contains("getValue") || code.contains("this.") ||
                code.contains("function") || code.contains("return") ||
                code.contains("undefined") || code.contains("null")) {
            return false;
        }
        return true;
    }
}