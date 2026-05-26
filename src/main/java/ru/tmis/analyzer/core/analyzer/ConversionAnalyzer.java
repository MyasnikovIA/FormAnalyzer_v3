// core/analyzer/ConversionAnalyzer.java
package ru.tmis.analyzer.core.analyzer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.model.ConversionStatistics;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.RouterInfo;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Анализатор конвертации SQL запросов в ActionRouter/DataSetRouter
 */
public class ConversionAnalyzer {

    private static final Pattern CDATA_PATTERN = Pattern.compile("<!\\[CDATA\\[(.*?)\\]\\]>", Pattern.DOTALL);

    private final Map<String, ConversionStatistics> statisticsMap = new LinkedHashMap<>();

    // core/analyzer/ConversionAnalyzer.java - модифицированный метод analyzeForm()

    /**
     * Анализирует форму на наличие конвертированных запросов
     * Учитывает только роутеры с признаком converted = true
     */
    public ConversionStatistics analyzeForm(FormInfo formInfo, String xmlContent) {
        ConversionStatistics stats = new ConversionStatistics(formInfo.getFormPath());

        if (xmlContent == null || xmlContent.isEmpty()) {
            return stats;
        }

        Document doc = Jsoup.parse(xmlContent, "", org.jsoup.parser.Parser.xmlParser());

        // Анализируем все Action компоненты
        analyzeActions(doc, stats);

        // Анализируем все DataSet компоненты
        analyzeDataSets(doc, stats);

        // ========== НОВОЕ: обновляем статистику с учётом converted ==========
        updateStatisticsWithConvertedFlag(formInfo, stats);

        statisticsMap.put(formInfo.getFormPath(), stats);
        return stats;
    }

    /**
     * Обновляет статистику, учитывая признак converted у роутеров
     */
    private void updateStatisticsWithConvertedFlag(FormInfo formInfo, ConversionStatistics stats) {
        // Собираем имена компонентов, у которых есть Router с converted = true
        Set<String> convertedRouterNames = new HashSet<>();

        for (RouterInfo router : formInfo.getActionRouters()) {
            if (router.isConverted()) {
                convertedRouterNames.add(router.getName());
            }
        }

        for (RouterInfo router : formInfo.getDataSetRouters()) {
            if (router.isConverted()) {
                convertedRouterNames.add(router.getName());
            }
        }

        // Пересчитываем количество конвертированных запросов
        int convertedCount = 0;
        for (Map.Entry<String, ConversionStatistics.QueryConversionInfo> entry : stats.getQueryDetails().entrySet()) {
            String componentName = entry.getKey();
            ConversionStatistics.QueryConversionInfo info = entry.getValue();

            // Запрос считается конвертированным, если:
            // 1. У него есть Router в XML
            // 2. И этот Router имеет признак converted = true
            if (info.hasRouter() && convertedRouterNames.contains(componentName)) {
                convertedCount++;
            }
        }

        // Обновляем статистику (через рефлексию или добавив setter)
        // Так как поля private, используем рефлексию или добавляем метод в ConversionStatistics
        try {
            java.lang.reflect.Field convertedField = ConversionStatistics.class.getDeclaredField("convertedQueries");
            convertedField.setAccessible(true);
            convertedField.set(stats, convertedCount);
        } catch (Exception e) {
            System.err.println("[ConversionAnalyzer] Ошибка обновления статистики: " + e.getMessage());
        }
    }

    /**
     * Анализирует Action компоненты
     */
    private void analyzeActions(Document doc, ConversionStatistics stats) {
        // D3 синтаксис
        Elements d3Actions = doc.select("cmpAction");
        for (Element action : d3Actions) {
            analyzeQueryComponent(action, stats, "cmpAction");
        }

        // M2 синтаксис
        Elements m2Actions = doc.select("component[cmptype=Action]");
        for (Element action : m2Actions) {
            analyzeQueryComponent(action, stats, "component[cmptype=Action]");
        }
    }

    /**
     * Анализирует DataSet компоненты
     */
    private void analyzeDataSets(Document doc, ConversionStatistics stats) {
        // D3 синтаксис
        Elements d3DataSets = doc.select("cmpDataSet");
        for (Element dataSet : d3DataSets) {
            analyzeQueryComponent(dataSet, stats, "cmpDataSet");
        }

        // M2 синтаксис
        Elements m2DataSets = doc.select("component[cmptype=DataSet]");
        for (Element dataSet : m2DataSets) {
            analyzeQueryComponent(dataSet, stats, "component[cmptype=DataSet]");
        }
    }

    /**
     * Анализирует один компонент (Action или DataSet)
     */
    private void analyzeQueryComponent(Element component, ConversionStatistics stats, String componentType) {
        String name = component.attr("name");
        if (name == null || name.isEmpty()) {
            name = "unnamed";
        }

        // Проверяем наличие Router элементов
        boolean hasRouter = false;
        boolean hasOracleSql = false;
        boolean hasPostgresSql = false;

        // Проверяем D3 синтаксис (cmpActionRouter / cmpDataSetRouter)
        Elements routers = component.select("cmpActionRouter, cmpDataSetRouter");
        if (routers.isEmpty()) {
            // Проверяем M2 синтаксис
            routers = component.select("component[cmptype=ActionRouter], component[cmptype=DataSetRouter]");
        }

        if (!routers.isEmpty()) {
            hasRouter = true;

            for (Element router : routers) {
                String condition = router.attr("condition");
                String cdata = extractCdata(router.html());

                if (condition != null && condition.toUpperCase().contains("ORACLE")) {
                    hasOracleSql = hasOracleSql || (cdata != null && !cdata.trim().isEmpty());
                }
                if (condition != null && condition.toUpperCase().contains("POSTGRE")) {
                    hasPostgresSql = hasPostgresSql || (cdata != null && !cdata.trim().isEmpty());
                }
            }
        } else {
            // Если нет Router, проверяем наличие прямого SQL
            String html = component.html();
            String cdata = extractCdata(html);
            if (cdata != null && !cdata.trim().isEmpty()) {
                hasOracleSql = true;
            }
        }

        stats.addQuery(name, componentType, hasRouter, hasOracleSql, hasPostgresSql);
    }

    /**
     * Извлекает содержимое CDATA секции
     */
    private String extractCdata(String html) {
        if (html == null) return null;
        Matcher matcher = CDATA_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }




    /**
     * Получить статистику по всем формам
     */
    public Map<String, ConversionStatistics> getAllStatistics() {
        return statisticsMap;
    }

    /**
     * Получить общую статистику по всем формам
     */
    public OverallConversionStats getOverallStats() {
        int totalForms = 0;
        int convertedForms = 0;
        int notConvertedForms = 0;
        int totalQueries = 0;
        int convertedQueries = 0;

        for (ConversionStatistics stats : statisticsMap.values()) {
            totalForms++;
            totalQueries += stats.getTotalQueries();
            convertedQueries += stats.getConvertedQueries();

            if (stats.isFullyConverted()) {
                convertedForms++;
            } else if (stats.isNotConverted()) {
                notConvertedForms++;
            }
        }

        return new OverallConversionStats(totalForms, convertedForms, notConvertedForms,
                totalQueries, convertedQueries);
    }

    /**
     * Общая статистика по всем формам
     */
    public static class OverallConversionStats {
        private final int totalForms;
        private final int convertedForms;
        private final int notConvertedForms;
        private final int totalQueries;
        private final int convertedQueries;

        public OverallConversionStats(int totalForms, int convertedForms, int notConvertedForms,
                                      int totalQueries, int convertedQueries) {
            this.totalForms = totalForms;
            this.convertedForms = convertedForms;
            this.notConvertedForms = notConvertedForms;
            this.totalQueries = totalQueries;
            this.convertedQueries = convertedQueries;
        }

        public int getTotalForms() { return totalForms; }
        public int getConvertedForms() { return convertedForms; }
        public int getNotConvertedForms() { return notConvertedForms; }
        public int getPartialConvertedForms() {
            return totalForms - convertedForms - notConvertedForms;
        }
        public int getTotalQueries() { return totalQueries; }
        public int getConvertedQueries() { return convertedQueries; }

        public double getFormsConversionPercent() {
            if (totalForms == 0) return 0;
            return (convertedForms * 100.0) / totalForms;
        }

        public double getQueriesConversionPercent() {
            if (totalQueries == 0) return 0;
            return (convertedQueries * 100.0) / totalQueries;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== ОБЩАЯ СТАТИСТИКА КОНВЕРТАЦИИ ===\n");
            sb.append("Формы:\n");
            sb.append("  Всего форм: ").append(totalForms).append("\n");
            sb.append("  Полностью конвертировано: ").append(convertedForms)
                    .append(" (").append(String.format("%.1f", getFormsConversionPercent())).append("%)\n");
            sb.append("  Частично конвертировано: ").append(getPartialConvertedForms()).append("\n");
            sb.append("  Не конвертировано: ").append(notConvertedForms).append("\n");
            sb.append("\nSQL запросы:\n");
            sb.append("  Всего запросов: ").append(totalQueries).append("\n");
            sb.append("  Конвертировано: ").append(convertedQueries)
                    .append(" (").append(String.format("%.1f", getQueriesConversionPercent())).append("%)\n");
            return sb.toString();
        }
    }
}