// core/extractor/ExtractorManager.java
package ru.tmis.analyzer.core.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.extractor.processors.*;
import ru.tmis.analyzer.core.extractor.processors.BrokerProcessor;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.SqlInfo;

import java.util.ArrayList;
import java.util.List;

public class ExtractorManager {

    private final List<IXmlProcessor> processors = new ArrayList<>();
    private final SqlExtractor sqlExtractor;
    private final SettingsModel settings;

    public ExtractorManager(SettingsModel settings) {
        this.settings = settings;
        this.sqlExtractor = new SqlExtractor();
        registerDefaultProcessors();
    }

    private void registerDefaultProcessors() {
        // 1. SubForm (priority 10)
        processors.add(new SubFormProcessor());

        // 2. Brokers (priority 20)
        processors.add(new BrokerProcessor());

        // 3. PackageFromAction (priority 22)
        processors.add(new PackageFromActionProcessor());

        // 4. JsForms (priority 25)
        processors.add(new JsFormProcessor());

        // 5. Constants (priority 35)
        processors.add(new ConstantProcessor());

        // 6. SystemOptions (priority 36)
        processors.add(new SystemOptionProcessor());

        // 7. UnitCompositions (priority 50)
        processors.add(new UnitCompositionProcessor());

        // 8. UniversalCompositions (priority 55)
        processors.add(new UniversalCompositionProcessor());

        // 9. SystemCompositions (priority 60)
        processors.add(new SystemCompositionProcessor());

        // 10. D3ApiShowForm (priority 65)
        processors.add(new D3ApiShowFormProcessor());

        // 11. Reports (priority 70)
        processors.add(new ReportProcessor());

        // 12. AutoPopupMenu (priority 80)
        processors.add(new AutoPopupMenuProcessor());

        // 12.5. PopupMenu (priority 85) - для Oracle
        processors.add(new PopupMenuProcessor(settings));

        // 12.6. PopupMenuPg (priority 86) - для PostgreSQL
        processors.add(new PopupMenuProcessorPg(settings));

        // 13. UnknownObjects (priority 200)
        processors.add(new UnknownObjectProcessor());

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
}