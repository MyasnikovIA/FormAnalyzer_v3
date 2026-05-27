// core/service/LLMDataLoader.java - исправленная версия

package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.core.db.OracleService;
import ru.tmis.analyzer.core.db.PostgresService;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Загружает DDL данные для LLM отчёта (вьюхи, таблицы)
 */
public class LLMDataLoader {

    private final SettingsModel settings;
    private final AppConfig config;
    private final OracleService oracleService;
    private final PostgresService postgresService;

    public LLMDataLoader(SettingsModel settings, AppConfig config) {
        this.settings = settings;
        this.config = config;
        this.oracleService = new OracleService(
                settings.getOracleUrl(),
                settings.getOracleUser(),
                settings.getOraclePassword()
        );
        this.postgresService = new PostgresService(
                settings.getPostgresUrl(),
                settings.getPostgresUser(),
                settings.getPostgresPassword(),
                settings.getMisUser()
        );
    }

    /**
     * Загружает DDL для всех вьюх и таблиц, используемых в форме
     * Вызывается только если включён экспорт LLM промпта
     */
    public void loadLLMData(FormInfo formInfo) {
        if (!config.isEnableLLMExport()) {
            System.out.println("[LLMDataLoader] LLM экспорт отключён");
            return;
        }

        System.out.println("[LLMDataLoader] Начало загрузки DDL данных для формы: " + formInfo.getFormPath());

        // 1. Загружаем DDL вьюх
        loadViewDDLs(formInfo);

        // 2. Загружаем DDL таблиц
        loadTableDDLs(formInfo);

        // 3. Загружаем DDL функции
        loadFunctionDDLs(formInfo);

        System.out.println("[LLMDataLoader] Итог: Oracle вьюх=" + formInfo.getOracleViewDDLs().size() +
                ", PostgreSQL вьюх=" + formInfo.getPostgresViewDDLs().size() +
                ", Oracle таблиц=" + formInfo.getOracleTableDDLs().size() +
                ", PostgreSQL таблиц=" + formInfo.getPostgresTableDDLs().size());
    }


    private void loadFunctionDDLs(FormInfo formInfo) {
        // Загружаем Oracle функции
        if (config.isIncludeOracleFunctions() && oracleService != null) {
            for (String pf : formInfo.getPackagesFunctions()) {
                if (pf.contains(".") && !pf.startsWith("D_PKG_CONSTANTS")
                        && !pf.startsWith("D_PKG_OPTIONS")) {
                    int dotIdx = pf.indexOf('.');
                    String pkg = pf.substring(0, dotIdx);
                    String func = pf.substring(dotIdx + 1);
                    String body = oracleService.getFunctionBody(pkg, func);
                    if (body != null && !body.isEmpty()) {
                        formInfo.addOracleFunctionBody(pf, body);
                    }
                }
            }
        }

        // Загружаем PostgreSQL функции
        if (config.isIncludePostgresFunctions() && postgresService != null) {
            for (String pf : formInfo.getPackagesFunctions()) {
                if (pf.toLowerCase().startsWith("d_pkg_") || pf.toLowerCase().startsWith("f_")) {
                    String body = postgresService.getFunctionBody(pf.toLowerCase());
                    if (body != null && !body.isEmpty()) {
                        formInfo.addPostgresFunctionBody(pf.toLowerCase(), body);
                    }
                }
            }
        }
    }
    private void loadViewDDLs(FormInfo formInfo) {
        // Получаем уже загруженные зависимости
        Map<String, ViewTableDependencies> viewDeps = formInfo.getViewDependencies();

        if (viewDeps == null || viewDeps.isEmpty()) {
            System.out.println("[LLMDataLoader] Нет зависимостей вьюх для формы: " + formInfo.getFormPath());
            return;
        }

        System.out.println("[LLMDataLoader] Загружено зависимостей вьюх: " + viewDeps.size());

        // Загружаем Oracle вьюхи
        if (config.isIncludeOracleViews()) {
            System.out.println("[LLMDataLoader] Загрузка Oracle вьюх...");
            for (Map.Entry<String, ViewTableDependencies> entry : viewDeps.entrySet()) {
                String viewName = entry.getKey();

                // Загружаем DDL вьюхи через ленивый метод (создаст в кэше)
                String ddl = DatabaseCacheManager.getOracleViewDDLLazy(viewName, () -> {
                    System.out.println("[LLMDataLoader] Загрузка Oracle вьюхи из БД: " + viewName);
                    return oracleService.getViewDDL(viewName);
                });

                if (ddl != null && !ddl.isEmpty()) {
                    formInfo.addOracleViewDDL(viewName, ddl);
                    System.out.println("[LLMDataLoader] ✅ Загружена Oracle вьюха: " + viewName);
                } else {
                    System.out.println("[LLMDataLoader] ❌ Oracle вьюха не найдена: " + viewName);
                }
            }
        }

        // Загружаем PostgreSQL вьюхи
        if (config.isIncludePostgresViews()) {
            System.out.println("[LLMDataLoader] Загрузка PostgreSQL вьюх...");
            for (Map.Entry<String, ViewTableDependencies> entry : viewDeps.entrySet()) {
                String viewName = entry.getKey();
                ViewTableDependencies deps = entry.getValue();

                String ddl = DatabaseCacheManager.getPostgresViewDDLLazy(viewName, () -> {
                    System.out.println("[LLMDataLoader] Загрузка PostgreSQL вьюхи из БД: " + viewName);
                    return postgresService.getViewDDL(viewName);
                });

                if (ddl != null && !ddl.isEmpty()) {
                    formInfo.addPostgresViewDDL(viewName, ddl);
                    System.out.println("[LLMDataLoader] ✅ Загружена PostgreSQL вьюха: " + viewName);

                    // Сохраняем зависимости таблиц из Oracle (для отображения связей)
                    if (deps != null && deps.getOracleTables() != null && !deps.getOracleTables().isEmpty()) {
                        formInfo.addViewTableDependency(viewName, deps.getOracleTables());
                        System.out.println("[LLMDataLoader] Таблицы для " + viewName + ": " + deps.getOracleTables());
                    }
                } else {
                    System.out.println("[LLMDataLoader] ❌ PostgreSQL вьюха не найдена: " + viewName);
                }
            }
        }
    }

    private void loadTableDDLs(FormInfo formInfo) {
        // Собираем все таблицы из viewTableDependencies
        Set<String> allTables = new LinkedHashSet<>();

        if (formInfo.getViewTableDependencies() != null) {
            for (Map.Entry<String, Set<String>> entry : formInfo.getViewTableDependencies().entrySet()) {
                allTables.addAll(entry.getValue());
                System.out.println("[LLMDataLoader] Таблицы из вьюхи " + entry.getKey() + ": " + entry.getValue());
            }
        }

        // Также добавляем прямые таблицы из tablesViews (не вьюхи)
        for (String tv : formInfo.getTablesViews()) {
            if (!tv.startsWith("D_V_")) {
                allTables.add(tv);
            }
        }

        if (allTables.isEmpty()) {
            System.out.println("[LLMDataLoader] Нет таблиц для загрузки");
            return;
        }

        System.out.println("[LLMDataLoader] Всего таблиц для загрузки: " + allTables.size());

        // Загружаем Oracle таблицы
        if (config.isIncludeOracleTables()) {
            System.out.println("[LLMDataLoader] Загрузка Oracle таблиц...");
            int loadedCount = 0;
            for (String tableName : allTables) {
                // Используем ленивую загрузку с реальным запросом к БД
                String ddl = DatabaseCacheManager.getOracleTableDDLLazy(tableName, () -> {
                    System.out.println("[LLMDataLoader] Загрузка Oracle таблицы из БД: " + tableName);
                    return oracleService.getTableDDL(tableName);
                });

                if (ddl != null && !ddl.isEmpty()) {
                    formInfo.addOracleTableDDL(tableName, ddl);
                    loadedCount++;
                    System.out.println("[LLMDataLoader] ✅ Загружена Oracle таблица: " + tableName + " (" + ddl.length() + " симв.)");
                } else {
                    System.out.println("[LLMDataLoader] ❌ Oracle таблица не найдена: " + tableName);
                }
            }
            System.out.println("[LLMDataLoader] Загружено Oracle таблиц: " + loadedCount + " из " + allTables.size());
        }

        // Загружаем PostgreSQL таблицы
        if (config.isIncludePostgresTables()) {
            System.out.println("[LLMDataLoader] Загрузка PostgreSQL таблиц...");
            int loadedCount = 0;
            for (String tableName : allTables) {
                String ddl = DatabaseCacheManager.getPostgresTableDDLLazy(tableName, () -> {
                    System.out.println("[LLMDataLoader] Загрузка PostgreSQL таблицы из БД: " + tableName);
                    return postgresService.getTableDDL(tableName);
                });

                if (ddl != null && !ddl.isEmpty()) {
                    formInfo.addPostgresTableDDL(tableName, ddl);
                    loadedCount++;
                    System.out.println("[LLMDataLoader] ✅ Загружена PostgreSQL таблица: " + tableName);
                } else {
                    System.out.println("[LLMDataLoader] ❌ PostgreSQL таблица не найдена: " + tableName);
                }
            }
            System.out.println("[LLMDataLoader] Загружено PostgreSQL таблиц: " + loadedCount + " из " + allTables.size());
        }
    }
    /**
     * Загружает тела функций (Oracle и PostgreSQL) для LLM отчёта
     */
    private void loadFunctionBodies(FormInfo formInfo) {
        // Загружаем Oracle функции
        if (config.isIncludeOracleFunctions()) {
            Set<String> oracleFunctions = extractOracleFunctions(formInfo);
            System.out.println("[LLMDataLoader] Загрузка Oracle функций (" + oracleFunctions.size() + " шт.)...");

            for (String funcName : oracleFunctions) {
                // Парсим имя: D_PKG_XXX.FUNCTION_NAME
                int dotIdx = funcName.indexOf('.');
                if (dotIdx > 0) {
                    String packageName = funcName.substring(0, dotIdx);
                    String functionName = funcName.substring(dotIdx + 1);
                    String body = oracleService.getFunctionBody(packageName, functionName);
                    if (body != null && !body.isEmpty()) {
                        formInfo.addOracleFunctionBody(funcName, body);
                        System.out.println("[LLMDataLoader] ✅ Загружена Oracle функция: " + funcName);
                    }
                }
            }
        }

        // Загружаем PostgreSQL функции
        if (config.isIncludePostgresFunctions()) {
            Set<String> postgresFunctions = extractPostgresFunctions(formInfo);
            System.out.println("[LLMDataLoader] Загрузка PostgreSQL функций (" + postgresFunctions.size() + " шт.)...");

            for (String funcName : postgresFunctions) {
                String body = postgresService.getFunctionBody(funcName);
                if (body != null && !body.isEmpty()) {
                    formInfo.addPostgresFunctionBody(funcName, body);
                    System.out.println("[LLMDataLoader] ✅ Загружена PostgreSQL функция: " + funcName);
                }
            }
        }
    }

    /**
     * Извлекает имена Oracle функций из FormInfo
     */
    private Set<String> extractOracleFunctions(FormInfo formInfo) {
        Set<String> functions = new LinkedHashSet<>();

        // Из packagesFunctions
        for (String pf : formInfo.getPackagesFunctions()) {
            if (pf.contains(".") && !pf.startsWith("D_PKG_CONSTANTS")
                    && !pf.startsWith("D_PKG_OPTIONS") && !pf.startsWith("D_PKG_OPTION_SPECS")) {
                functions.add(pf);
            }
        }

        return functions;
    }

    /**
     * Извлекает имена PostgreSQL функций из FormInfo
     */
    private Set<String> extractPostgresFunctions(FormInfo formInfo) {
        Set<String> functions = new LinkedHashSet<>();

        // Из packagesFunctions (PostgreSQL функции в нижнем регистре)
        for (String pf : formInfo.getPackagesFunctions()) {
            if (pf.startsWith("d_pkg_") || pf.startsWith("f_") || pf.startsWith("d_")) {
                functions.add(pf.toLowerCase());
            }
        }

        return functions;
    }
}