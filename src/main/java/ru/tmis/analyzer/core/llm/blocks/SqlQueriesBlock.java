package ru.tmis.analyzer.core.llm.blocks;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.core.llm.model.LLMReportContext;
import ru.tmis.analyzer.core.model.SqlInfo;

public class SqlQueriesBlock implements IReportBlock {

    @Override
    public String getBlockName() {
        return "SQL ЗАПРОСЫ С ТЭГАМИ";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public boolean isEnabled(AppConfig config) {
        return config.isIncludeSqlQueries();
    }

    @Override
    public String generate(LLMReportContext context) throws Exception {
        StringBuilder sb = new StringBuilder();

        sb.append("\n## 1. SQL ЗАПРОСЫ С ТЭГАМИ\n\n");

        if (context.getAllSqlQueries().isEmpty()) {
            sb.append("SQL запросы не найдены.\n\n");
            return sb.toString();
        }

        sb.append("Ниже представлены все SQL запросы, извлеченные из форм. ");
        sb.append("Каждый запрос включает XML-теги компонента (DataSet или Action) ");
        sb.append("и содержит информацию об источнике.\n\n");

        sb.append("**Статистика:**\n");
        sb.append("- Всего SQL запросов: ").append(context.getTotalSqlQueries()).append("\n");
        sb.append("- Всего форм: ").append(context.getTotalForms()).append("\n\n");

        int queryNum = 1;
        for (SqlInfo sql : context.getAllSqlQueries()) {
            if (sql.getSqlContent() == null || sql.getSqlContent().trim().isEmpty()) continue;

            sb.append("---\n\n");
            sb.append("### Запрос №").append(queryNum).append("\n\n");
            sb.append("**Тип компонента:** ").append(sql.getSourceType()).append("\n");
            sb.append("**Имя компонента:** ").append(sql.getComponentName()).append("\n");
            sb.append("**Источник:** ").append(sql.getSourcePath()).append("\n");
            if (sql.getBaseFormPath() != null && !sql.getBaseFormPath().isEmpty()) {
                sb.append("**Базовая форма:** ").append(sql.getBaseFormPath()).append("\n");
            }
            sb.append("\n**SQL код:**\n\n");
            sb.append("```xml\n");
            sb.append(sql.getSqlContent());
            if (!sql.getSqlContent().endsWith("\n")) {
                sb.append("\n");
            }
            sb.append("```\n\n");

            if (!sql.getTablesViews().isEmpty()) {
                sb.append("**Используемые таблицы/вьюхи:** ");
                sb.append(String.join(", ", sql.getTablesViews())).append("\n");
            }
            if (!sql.getPackagesFunctions().isEmpty()) {
                sb.append("**Используемые пакеты/функции:** ");
                sb.append(String.join(", ", sql.getPackagesFunctions())).append("\n");
            }
            sb.append("\n");
            queryNum++;
        }

        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "Полные SQL запросы из всех DataSet и Action компонентов в формате XML с тэгами";
    }
}