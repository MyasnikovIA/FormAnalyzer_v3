// core/extractor/ExtractorManager.java
package ru.tmis.analyzer.core.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.analyzer.ConversionAnalyzer;
import ru.tmis.analyzer.core.extractor.extractor.SqlExtractor;
import ru.tmis.analyzer.core.extractor.processors.*;
import ru.tmis.analyzer.core.extractor.processors.BrokerProcessor;
import ru.tmis.analyzer.core.extractor.processors.RouterProcessor;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.ConversionStatistics;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.SqlInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExtractorManager {

    private final List<IXmlProcessor> processors = new ArrayList<>();
    private final SqlExtractor sqlExtractor;
    private final SettingsModel settings;
    private final ConversionAnalyzer conversionAnalyzer = new ConversionAnalyzer();

    // Поля для остановки
    private AtomicBoolean stopRequested = null;
    private ILogger logger;

    public ExtractorManager(SettingsModel settings) {
        this.settings = settings;
        this.sqlExtractor = new SqlExtractor();
        registerDefaultProcessors();
    }

    private void registerDefaultProcessors() {
        processors.add(new RouterProcessor());
        processors.add(new SubFormProcessor());
        processors.add(new BrokerProcessor());
        processors.add(new PackageFromActionProcessor());
        processors.add(new JsFormProcessor());
        processors.add(new ConstantProcessor());
        processors.add(new SystemOptionProcessor());
        processors.add(new UnitCompositionProcessor());
        processors.add(new UniversalCompositionProcessor());
        processors.add(new SystemCompositionProcessor());
        processors.add(new D3ApiShowFormProcessor());
        processors.add(new ReportProcessor());
        processors.add(new AutoPopupMenuProcessor());
        processors.add(new PopupMenuProcessor(settings));
        processors.add(new PopupMenuProcessorPg(settings));
        processors.add(new UnknownObjectProcessor());

        processors.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
    }

    public void registerProcessor(IXmlProcessor processor) {
        processors.add(processor);
        processors.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
    }

    /**
     * Установка флага остановки
     */
    public void setStopRequested(AtomicBoolean stopRequested) {
        this.stopRequested = stopRequested;
        if (this.sqlExtractor != null) {
            this.sqlExtractor.setStopRequested(stopRequested);
        }
    }

    /**
     * Установка логгера
     */
    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

    private void log(String message) {
        if (logger != null) {
            logger.log(message);
        } else {
            System.out.println(message);
        }
    }

    private boolean isStopped() {
        return stopRequested != null && stopRequested.get();
    }

    public void process(String xmlContent, FormInfo formInfo) {
        if (xmlContent == null || xmlContent.isEmpty()) return;

        if (isStopped()) {
            log("Извлечение данных остановлено пользователем (до парсинга)");
            return;
        }

        Document doc = Jsoup.parse(xmlContent, "", Parser.xmlParser());

        if (isStopped()) {
            log("Извлечение данных остановлено пользователем (перед анализом конвертации)");
            return;
        }

        ConversionStatistics stats = conversionAnalyzer.analyzeForm(formInfo, xmlContent);
        formInfo.setConversionStatistics(stats);

        if (isStopped()) {
            log("Извлечение данных остановлено пользователем (перед извлечением SQL)");
            return;
        }

        List<SqlInfo> sqlList = sqlExtractor.extract(doc, formInfo);
        formInfo.setSqlQueries(sqlList);

        if (isStopped()) {
            log("Извлечение данных остановлено пользователем (после извлечения SQL)");
            return;
        }

        for (SqlInfo sql : sqlList) {
            if (isStopped()) {
                log("Извлечение данных остановлено пользователем (в цикле SQL)");
                return;
            }

            for (String tv : sql.getTablesViews()) formInfo.addTableView(tv);
            for (String pf : sql.getPackagesFunctions()) formInfo.addPackageFunction(pf);
            for (String proc : sql.getUserProcedures()) formInfo.addUserProcedure(proc);
            for (String opt : sql.getSystemOptions()) formInfo.addSystemOption(opt);
            for (String constant : sql.getConstants()) formInfo.addConstant(constant);
            for (String unknown : sql.getUnknownObjects()) formInfo.addUnknownObject(unknown);
        }

        if (isStopped()) {
            log("Извлечение данных остановлено пользователем (перед запуском процессоров)");
            return;
        }

        for (IXmlProcessor processor : processors) {
            if (isStopped()) {
                log("Извлечение данных остановлено пользователем в процессоре " + processor.getName());
                break;
            }

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