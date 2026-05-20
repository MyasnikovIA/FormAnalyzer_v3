// core/extractor/processors/ReportProcessor.java

package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.db.ReportsFromDbService;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.DbReportInfo;
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

    private static final Pattern PRINT_REPORT_PATTERN = Pattern.compile(
            "printReportByCode\\s*\\(\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PRINT_REPORT_PATTERN_DOUBLE = Pattern.compile(
            "printReportByCode\\s*\\(\\s*\"([^\"]+)\"",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern REPORTS_PATH_PATTERN = Pattern.compile(
            "['\"]Reports/([^'\"]+\\.frm)['\"]",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CDATA_PATTERN = Pattern.compile(
            "<!\\[CDATA\\[(.*?)\\]\\]>",
            Pattern.DOTALL
    );

    private final SettingsModel settings;
    private final ReportsFromDbService reportsService;

    // Конструктор по умолчанию
    public ReportProcessor() {
        this.settings = SettingsModel.getInstance();
        this.reportsService = new ReportsFromDbService(settings);
    }

    // Конструктор с параметром
    public ReportProcessor(SettingsModel settings) {
        this.settings = settings;
        this.reportsService = new ReportsFromDbService(settings);
    }

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

        // 1. Ищем отчеты в SQL запросах
        for (SqlInfo sql : formInfo.getSqlQueries()) {
            String sqlContent = sql.getSqlContent();
            if (sqlContent != null) {
                reports.addAll(extractFromText(sqlContent));
            }
        }

        // 2. Ищем отчеты в Action компонентах
        Elements actions = doc.select("component[cmptype=Action], cmpAction");
        for (Element action : actions) {
            if (hasSqlContent(action)) {
                continue;
            }
            String actionHtml = action.html();
            if (actionHtml != null) {
                reports.addAll(extractFromText(actionHtml));
            }
        }

        // 3. Ищем отчеты в CDATA секциях
        String html = doc.html();
        Matcher cdataMatcher = CDATA_PATTERN.matcher(html);
        while (cdataMatcher.find()) {
            String jsContent = cdataMatcher.group(1);
            if (jsContent != null) {
                reports.addAll(extractFromText(jsContent));
            }
        }

        // 4. Ищем отчеты во всем HTML
        reports.addAll(extractFromText(html));

        // 5. Форматируем отчеты с информацией из БД
        for (String report : reports) {
            String formattedReport = formatReportWithDbInfo(report);
            formInfo.addReport(formattedReport);
        }
    }

    /**
     * Форматирует отчёт с информацией из БД, если это код
     */
    private String formatReportWithDbInfo(String report) {
        // Если это путь к файлу (содержит / или .frm) - возвращаем как есть
        if (report.contains("/") || report.endsWith(".frm")) {
            return report;
        }

        // Пытаемся получить информацию из БД
        try {
            DbReportInfo dbReport = reportsService.getReportByCode(report);
            if (dbReport != null) {
                String typeName = dbReport.getRepTypeName();
                StringBuilder sb = new StringBuilder();
                sb.append(report).append(" (").append(typeName).append(")");

                // Если REP_TYPE = 1 (WEB-форма) и есть REP_FILENAME
                if (dbReport.getRepType() == 1 && dbReport.getRepFilename() != null && !dbReport.getRepFilename().isEmpty()) {
                    String formPath = dbReport.getRepFilename();
                    if (!formPath.endsWith(".frm")) {
                        formPath = formPath + ".frm";
                    }
                    if (!formPath.startsWith("Reports/")) {
                        formPath = "Reports/" + formPath;
                    }
                    sb.append(" ").append(formPath);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            System.err.println("[ReportProcessor] Ошибка получения информации об отчёте " + report + ": " + e.getMessage());
        }

        return report;
    }

    private boolean hasSqlContent(Element element) {
        String html = element.html();
        if (html == null) return false;

        Matcher cdataMatcher = CDATA_PATTERN.matcher(html);
        while (cdataMatcher.find()) {
            String content = cdataMatcher.group(1);
            if (isSqlContent(content)) {
                return true;
            }
        }

        String text = element.ownText();
        return isSqlContent(text);
    }

    private boolean isSqlContent(String content) {
        if (content == null) return false;
        String lower = content.toLowerCase().trim();
        return lower.startsWith("select") || lower.startsWith("insert") ||
                lower.startsWith("update") || lower.startsWith("delete") ||
                lower.startsWith("begin") || lower.startsWith("declare") ||
                lower.contains("into") || lower.contains("from");
    }

    private Set<String> extractFromText(String text) {
        Set<String> result = new LinkedHashSet<>();

        if (text == null || text.isEmpty()) {
            return result;
        }

        Matcher printMatcher = PRINT_REPORT_PATTERN.matcher(text);
        while (printMatcher.find()) {
            String reportCode = cleanValue(printMatcher.group(1));
            if (isValidReportCode(reportCode)) {
                result.add(reportCode);
            }
        }

        Matcher printMatcherDouble = PRINT_REPORT_PATTERN_DOUBLE.matcher(text);
        while (printMatcherDouble.find()) {
            String reportCode = cleanValue(printMatcherDouble.group(1));
            if (isValidReportCode(reportCode)) {
                result.add(reportCode);
            }
        }

        Matcher pathMatcher = REPORTS_PATH_PATTERN.matcher(text);
        while (pathMatcher.find()) {
            String reportPath = pathMatcher.group(1);
            if (reportPath != null && !reportPath.isEmpty()) {
                result.add("/Reports/" + reportPath);
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
        if (code.contains("+") || code.contains("getVar") ||
                code.contains("getValue") || code.contains("this.") ||
                code.contains("function") || code.contains("return") ||
                code.contains("undefined") || code.contains("null")) {
            return false;
        }
        return true;
    }
}