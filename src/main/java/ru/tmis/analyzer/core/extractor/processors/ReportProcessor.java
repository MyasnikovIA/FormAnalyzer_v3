// core/extractor/processors/ReportProcessor.java - обновленная версия
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
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
 */
public class ReportProcessor implements IXmlProcessor {

    // Паттерн для printReportByCode
    private static final Pattern PRINT_REPORT_PATTERN = Pattern.compile(
            "printReportByCode\\s*\\(\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для printReport
    private static final Pattern PRINT_REPORT_SIMPLE_PATTERN = Pattern.compile(
            "printReport\\s*\\(\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для путей Reports/
    private static final Pattern REPORTS_PATH_PATTERN = Pattern.compile(
            "['\"]Reports/([^'\"]+\\.frm)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для showReport
    private static final Pattern SHOW_REPORT_PATTERN = Pattern.compile(
            "showReport\\s*\\(\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // Паттерн для вызова отчета через action
    private static final Pattern ACTION_REPORT_PATTERN = Pattern.compile(
            "action\\s*=\\s*['\"]printReportByCode['\"][^>]*?\\s*params\\s*=\\s*['\"][^'\"]*?code['\"]?\\s*:\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // CDATA паттерн
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
        String html = doc.html();
        Set<String> reports = new LinkedHashSet<>();

        // 1. Поиск printReportByCode в CDATA
        Matcher cdataMatcher = CDATA_PATTERN.matcher(html);
        while (cdataMatcher.find()) {
            String jsContent = cdataMatcher.group(1);
            reports.addAll(extractFromJs(jsContent));
        }

        // 2. Поиск в основном HTML
        reports.addAll(extractFromJs(html));

        // 3. Поиск в атрибутах компонентов
        Elements components = doc.select("[onclick], [onchange], [onload]");
        for (var element : components) {
            String onclick = element.attr("onclick");
            if (onclick != null && !onclick.isEmpty()) {
                reports.addAll(extractFromJs(onclick));
            }
        }

        // 4. ПОИСК ОТЧЕТОВ В SQL ЗАПРОСАХ
        for (SqlInfo sql : formInfo.getSqlQueries()) {
            String sqlContent = sql.getSqlContent();
            if (sqlContent != null) {
                reports.addAll(extractFromJs(sqlContent));
            }
        }

        // 5. Добавляем найденные отчеты
        for (String report : reports) {
            formInfo.addReport(report);
            System.out.println("  [ReportProcessor] Найден отчет: " + report);
        }

        if (!reports.isEmpty()) {
            System.out.println("  [ReportProcessor] Всего отчетов: " + reports.size());
        }
    }

    private Set<String> extractFromJs(String jsContent) {
        Set<String> result = new LinkedHashSet<>();

        if (jsContent == null || jsContent.isEmpty()) {
            return result;
        }

        // printReportByCode('REPORT_CODE')
        Matcher printMatcher = PRINT_REPORT_PATTERN.matcher(jsContent);
        while (printMatcher.find()) {
            String reportCode = cleanValue(printMatcher.group(1));
            if (isValidReportCode(reportCode)) {
                result.add(reportCode);
            }
        }

        // printReport('REPORT_CODE')
        Matcher simpleMatcher = PRINT_REPORT_SIMPLE_PATTERN.matcher(jsContent);
        while (simpleMatcher.find()) {
            String reportCode = cleanValue(simpleMatcher.group(1));
            if (isValidReportCode(reportCode)) {
                result.add(reportCode);
            }
        }

        // showReport('REPORT_CODE')
        Matcher showMatcher = SHOW_REPORT_PATTERN.matcher(jsContent);
        while (showMatcher.find()) {
            String reportCode = cleanValue(showMatcher.group(1));
            if (isValidReportCode(reportCode)) {
                result.add(reportCode);
            }
        }

        // Reports/some_report.frm
        Matcher pathMatcher = REPORTS_PATH_PATTERN.matcher(jsContent);
        while (pathMatcher.find()) {
            String reportPath = pathMatcher.group(1);
            if (reportPath != null && !reportPath.isEmpty()) {
                result.add("/Reports/" + reportPath);
            }
        }

        // В XML атрибутах
        Matcher actionMatcher = ACTION_REPORT_PATTERN.matcher(jsContent);
        while (actionMatcher.find()) {
            String reportCode = cleanValue(actionMatcher.group(1));
            if (isValidReportCode(reportCode)) {
                result.add(reportCode);
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
                code.contains("function") || code.contains("return")) {
            return false;
        }
        // Пропускаем слишком длинные (вероятно не код отчета)
        if (code.length() > 100) return false;
        return true;
    }
}