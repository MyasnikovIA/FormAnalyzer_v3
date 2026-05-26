// core/extractor/XmlNodeVisitor.java
package ru.tmis.analyzer.core.extractor;

import org.jsoup.nodes.Document;
import ru.tmis.analyzer.core.extractor.extractor.SqlExtractor;
import ru.tmis.analyzer.core.extractor.processors.*;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.SqlInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor для обхода XML дерева и извлечения информации
 */
public class XmlNodeVisitor {

    private final List<IXmlProcessor> processors = new ArrayList<>();
    private final SqlExtractor sqlExtractor;

    public XmlNodeVisitor() {
        this.sqlExtractor = new SqlExtractor();
        registerDefaultProcessors();
    }

    private void registerDefaultProcessors() {
        register(new SubFormProcessor());
        register(new BrokerProcessor());
        register(new UnitCompositionProcessor());
        register(new AutoPopupMenuProcessor());
        register(new UniversalCompositionProcessor());
        register(new ReportProcessor());
        register(new D3ApiShowFormProcessor());
        register(new SystemCompositionProcessor());
    }

    /**
     * Регистрация нового процессора (для расширения функционала)
     */
    public void register(IXmlProcessor processor) {
        processors.add(processor);
        // Сортируем по приоритету (меньше - раньше)
        processors.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
    }

    /**
     * Обработать XML содержимое формы
     */
    public void process(String xmlContent, FormInfo formInfo) {
        if (xmlContent == null || xmlContent.isEmpty()) return;

        Document doc = org.jsoup.Jsoup.parse(xmlContent, "", org.jsoup.parser.Parser.xmlParser());

        // 1. Извлекаем SQL запросы (специальная обработка)
        List<SqlInfo> sqlList = sqlExtractor.extract(doc, formInfo);
        formInfo.setSqlQueries(sqlList);

        // 2. Обрабатываем все зарегистрированные процессоры
        for (IXmlProcessor processor : processors) {
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