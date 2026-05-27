// core/service/LLMDataLoader.java
package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.core.db.OracleService;
import ru.tmis.analyzer.core.db.PostgresService;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;
import ru.tmis.analyzer.ui.ViewDependencyAnalyzer;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Загружает DDL данные для LLM отчёта (вьюхи, таблицы)
 */
public class LLMDataLoader {

    private final SettingsModel settings;
    private final AppConfig config;

    public LLMDataLoader(SettingsModel settings, AppConfig config) {
        this.settings = settings;
        this.config = config;
    }

    /**
     * Загружает DDL для всех вьюх и таблиц, используемых в форме
     * Вызывается только если включён экспорт LLM промпта
     */
    public void loadLLMData(FormInfo formInfo) {
        if (!config.isEnableLLMExport()) {
            return;
        }

        // 1. Загружаем DDL вьюх
        loadViewDDLs(formInfo);

        // 2. Загружаем DDL таблиц
        loadTableDDLs(formInfo);
    }

    private void loadViewDDLs(FormInfo formInfo) {
        // Получаем уже загруженные зависимости
        Map<String, ViewTableDependencies> viewDeps = formInfo.getViewDependencies();

        if (viewDeps == null || viewDeps.isEmpty()) {
            System.out.println("[LLMDataLoader] Нет зависимостей вьюх для формы: " + formInfo.getFormPath());
            return;
        }

        System.out.println("[LLMDataLoader] Загружено зависимостей вьюх: " + viewDeps.size());

        // Загружаем Oracle вьюхи и таблицы
        if (config.isIncludeOracleViews()) {
            for (Map.Entry<String, ViewTableDependencies> entry : viewDeps.entrySet()) {
                String viewName = entry.getKey();
                ViewTableDependencies deps = entry.getValue();

                String ddl = DatabaseCacheManager.getOracleViewDDL(viewName);
                if (ddl != null && !ddl.isEmpty()) {
                    formInfo.addOracleViewDDL(viewName, ddl);
                    System.out.println("[LLMDataLoader] Загружена Oracle вьюха: " + viewName);
                }

                // Сохраняем таблицы из Oracle
                if (deps != null && deps.getOracleTables() != null && !deps.getOracleTables().isEmpty()) {
                    formInfo.addViewTableDependency(viewName, deps.getOracleTables());
                    System.out.println("[LLMDataLoader] Таблицы Oracle для " + viewName + ": " + deps.getOracleTables());
                }
            }
        }

        // Загружаем PostgreSQL вьюхи
        if (config.isIncludePostgresViews()) {
            for (Map.Entry<String, ViewTableDependencies> entry : viewDeps.entrySet()) {
                String viewName = entry.getKey();

                String ddl = DatabaseCacheManager.getPostgresViewDDL(viewName);
                if (ddl != null && !ddl.isEmpty()) {
                    formInfo.addPostgresViewDDL(viewName, ddl);
                    System.out.println("[LLMDataLoader] Загружена PostgreSQL вьюха: " + viewName);

                    // Сохраняем зависимости таблиц
                    ViewTableDependencies deps = entry.getValue();
                    if (deps != null && deps.getOracleTables() != null && !deps.getOracleTables().isEmpty()) {
                        formInfo.addViewTableDependency(viewName, deps.getOracleTables());
                        System.out.println("[LLMDataLoader] Таблицы для " + viewName + ": " + deps.getOracleTables());
                    }
                }
            }
        }
    }

    // core/service/LLMDataLoader.java

    private void loadTableDDLs(FormInfo formInfo) {
        // Собираем все таблицы из viewTableDependencies
        Set<String> allTables = new LinkedHashSet<>();

        for (Map.Entry<String, Set<String>> entry : formInfo.getViewTableDependencies().entrySet()) {
            allTables.addAll(entry.getValue());
            System.out.println("[LLMDataLoader] Таблицы из вьюхи " + entry.getKey() + ": " + entry.getValue());
        }

        // Также добавляем прямые таблицы из tablesViews (не вьюхи)
        for (String tv : formInfo.getTablesViews()) {
            if (!tv.startsWith("D_V_")) {
                allTables.add(tv);
            }
        }

        System.out.println("[LLMDataLoader] Всего таблиц для загрузки: " + allTables.size());

        // Загружаем Oracle таблицы
        if (config.isIncludeOracleTables()) {
            for (String tableName : allTables) {
                String ddl = DatabaseCacheManager.getOracleTableDDL(tableName);
                if (ddl != null && !ddl.isEmpty()) {
                    formInfo.addOracleTableDDL(tableName, ddl);
                    System.out.println("[LLMDataLoader] Загружена Oracle таблица: " + tableName);
                } else {
                    System.out.println("[LLMDataLoader] Oracle таблица не найдена: " + tableName);
                }
            }
        }

        // Загружаем PostgreSQL таблицы
        if (config.isIncludePostgresTables()) {
            for (String tableName : allTables) {
                String ddl = DatabaseCacheManager.getPostgresTableDDL(tableName);
                if (ddl != null && !ddl.isEmpty()) {
                    formInfo.addPostgresTableDDL(tableName, ddl);
                    System.out.println("[LLMDataLoader] Загружена PostgreSQL таблица: " + tableName);
                } else {
                    System.out.println("[LLMDataLoader] PostgreSQL таблица не найдена: " + tableName);
                }
            }
        }
    }
}