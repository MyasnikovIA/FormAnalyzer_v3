// core/extractor/ExtractorManager.java
package ru.tmis.analyzer.core.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.analyzer.ConversionAnalyzer;
import ru.tmis.analyzer.core.extractor.processors.*;
import ru.tmis.analyzer.core.extractor.processors.BrokerProcessor;
import ru.tmis.analyzer.core.model.ConversionStatistics;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.SqlInfo;

import java.util.ArrayList;
import java.util.List;

public class ExtractorManager {

    private final List<IXmlProcessor> processors = new ArrayList<>();
    private final SqlExtractor sqlExtractor;
    private final SettingsModel settings;
    private final ConversionAnalyzer conversionAnalyzer = new ConversionAnalyzer();


    public ExtractorManager(SettingsModel settings) {
        this.settings = settings;
        this.sqlExtractor = new SqlExtractor();
        registerDefaultProcessors();
    }

    private void registerDefaultProcessors() {
        processors.add(new SubFormProcessor());           // SubForm
        processors.add(new BrokerProcessor());            // Брокеры
        processors.add(new PackageFromActionProcessor()); // Пакеты из Action
        processors.add(new JsFormProcessor());            // JS формы
        processors.add(new ConstantProcessor());          // Константы
        processors.add(new SystemOptionProcessor());      // Системные опции
        processors.add(new UnitCompositionProcessor());   // Unit композиции
        processors.add(new UniversalCompositionProcessor()); // UniversalComposition
        processors.add(new SystemCompositionProcessor()); // System/composition
        processors.add(new D3ApiShowFormProcessor());     // D3Api.showForm
        processors.add(new ReportProcessor());            // Отчёты
        processors.add(new AutoPopupMenuProcessor());     // AutoPopupMenu
        processors.add(new PopupMenuProcessor(settings)); // Контекстное меню Oracle
        processors.add(new PopupMenuProcessorPg(settings)); // Контекстное меню PostgreSQL
        processors.add(new UnknownObjectProcessor());     // Неизвестные объекты

        processors.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
    }

    public void registerProcessor(IXmlProcessor processor) {
        processors.add(processor);
        processors.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
    }

    // core/extractor/ExtractorManager.java

    public void process(String xmlContent, FormInfo formInfo) {
        if (xmlContent == null || xmlContent.isEmpty()) return;

        Document doc = Jsoup.parse(xmlContent, "", Parser.xmlParser());

        // Анализ конвертации SQL запросов
        ConversionStatistics stats = conversionAnalyzer.analyzeForm(formInfo, xmlContent);
        formInfo.setConversionStatistics(stats);


        List<SqlInfo> sqlList = sqlExtractor.extract(doc, formInfo);
        formInfo.setSqlQueries(sqlList);

        // Обогащаем FormInfo данными из SQL
        for (SqlInfo sql : sqlList) {
            for (String tv : sql.getTablesViews()) formInfo.addTableView(tv);
            for (String pf : sql.getPackagesFunctions()) formInfo.addPackageFunction(pf);
            for (String proc : sql.getUserProcedures()) formInfo.addUserProcedure(proc);
            for (String opt : sql.getSystemOptions()) formInfo.addSystemOption(opt);
            for (String constant : sql.getConstants()) formInfo.addConstant(constant);

            // ========== ДОБАВИТЬ ЭТУ СТРОКУ ==========
            for (String unknown : sql.getUnknownObjects()) formInfo.addUnknownObject(unknown);
        }

        // Обрабатываем остальные процессоры
        for (IXmlProcessor processor : processors) {
            if (processor instanceof UnknownObjectProcessor) continue;

            try {
                processor.process(doc, formInfo);
            } catch (Exception e) {
                System.err.println("Ошибка в процессоре " + processor.getName() + ": " + e.getMessage());
            }
        }
    }

    public SqlExtractor getSqlExtractor() {
        return sqlExtractor;
    }
    public ConversionAnalyzer getConversionAnalyzer() {
        return conversionAnalyzer;
    }
}