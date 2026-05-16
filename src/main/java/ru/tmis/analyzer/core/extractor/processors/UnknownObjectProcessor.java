// core/extractor/processors/UnknownObjectProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.SqlInfo;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Извлечение неизвестных объектов (требующих разбора аналитиком)
 *
 * ВНИМАНИЕ: Этот процессор НЕ парсит HTML/JS.
 * Он только собирает объекты, которые были помечены как unknown
 * при разборе SQL запросов в SqlExtractor.
 *
 * Это гарантирует, что в "РАЗОБРАТЬ АНАЛИТИКОМ" попадают только объекты,
 * реально встречающиеся в SQL, а не JavaScript переменные.
 */
public class UnknownObjectProcessor implements IXmlProcessor {

    @Override
    public String getName() {
        return "UnknownObjectProcessor";
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        // Этот метод теперь может быть пустым, так как unknown объекты
        // уже добавлены из SQL через ExtractorManager
        // Но оставим его для совместимости - просто ничего не делаем

       //Set<String> unknown = new LinkedHashSet<>();

       //// Собираем unknown объекты ТОЛЬКО из SQL запросов
       //for (SqlInfo sql : formInfo.getSqlQueries()) {
       //    unknown.addAll(sql.getUnknownObjects());
       //}

       //for (String obj : unknown) {
       //    formInfo.addUnknownObject(obj);
       //}
    }
}