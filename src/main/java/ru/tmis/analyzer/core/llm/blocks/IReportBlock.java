package ru.tmis.analyzer.core.llm.blocks;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.core.llm.model.LLMReportContext;

public interface IReportBlock {
    String getBlockName();
    int getOrder();
    boolean isEnabled(AppConfig config);
    String generate(LLMReportContext context) throws Exception;
    default String getDescription() { return getBlockName(); }
}