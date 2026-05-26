// core/extractor/IXmlProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import ru.tmis.analyzer.core.model.FormInfo;

/**
 * Интерфейс для процессоров XML компонентов
 * Добавление нового типа извлечения = реализация этого интерфейса
 */
public interface IXmlProcessor {

    /**
     * @return Уникальное имя процессора
     */
    String getName();

    /**
     * @return Приоритет выполнения (меньше = раньше)
     */
    default int getPriority() {
        return 100;
    }

    /**
     * Обработать XML документ и извлечь информацию в FormInfo
     */
    void process(Document doc, FormInfo formInfo);
}