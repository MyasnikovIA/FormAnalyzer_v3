package ru.tmis.analyzer.core.llm;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.core.db.OracleService;
import ru.tmis.analyzer.core.db.PostgresService;
import ru.tmis.analyzer.core.model.*;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LLMPromptGenerator {

    private final AppConfig config;
    private final SettingsModel settings;
    private final OracleService oracleService;
    private final PostgresService postgresService;
    private LLMReportContext context;
    private BooleanSupplier stopCondition = () -> false;

    public LLMPromptGenerator(AppConfig config) {
        this.config = config;
        this.settings = SettingsModel.getInstance();
        this.oracleService = new OracleService(settings.getOracleUrl(), settings.getOracleUser(), settings.getOraclePassword());
        this.postgresService = new PostgresService(settings.getPostgresUrl(), settings.getPostgresUser(), settings.getPostgresPassword(), settings.getMisUser());
    }

    public void setStopCondition(BooleanSupplier condition) {
        this.stopCondition = condition;
    }

    public LLMReportContext prepareContext(List<FormInfo> forms) {
        System.out.println("[LLM] Начало подготовки контекста для " + forms.size() + " форм...");
        context = new LLMReportContext();
        context.setAnalyzedForms(forms);
        context.setTotalForms(forms.size());

        List<SqlInfo> allSql = new ArrayList<>();
        Set<String> allViews = new LinkedHashSet<>();
        Map<String, Set<String>> viewUsage = new LinkedHashMap<>();
        System.out.println("[LLM] Сбор SQL запросов и вьюх из форм...");
        int sqlCount = 0;
        for (FormInfo form : forms) {
            for (SqlInfo sql : form.getSqlQueries()) {
                allSql.add(sql);
                sqlCount++;
                for (String tv : sql.getTablesViews()) {
                    if (tv.startsWith("D_V_")) {
                        allViews.add(tv);
                        viewUsage.computeIfAbsent(tv, k -> new LinkedHashSet<>()).add(form.getFormPath());
                    }
                }
            }
        }
        System.out.println("[LLM] Собрано SQL запросов: " + sqlCount);
        System.out.println("[LLM] Найдено уникальных вьюх: " + allViews.size());

        context.setAllSqlQueries(allSql);
        context.setAllViews(allViews);
        context.setViewUsageInForms(viewUsage);
        context.setTotalSqlQueries(allSql.size());

        // Загрузка данных из БД
        if (config.isIncludePostgresViews() || config.isIncludePostgresTables()) {
            System.out.println("[LLM] Загрузка данных из PostgreSQL...");
            loadPostgresData();
            System.out.println("[LLM] Загрузка данных из PostgreSQL завершена.");
        }
        if (config.isIncludeOracleViews() || config.isIncludeOracleTables()) {
            System.out.println("[LLM] Загрузка данных из Oracle...");
            loadOracleData();
            System.out.println("[LLM] Загрузка данных из Oracle завершена.");
        }
        if (config.isIncludeOracleFunctions()) {
            System.out.println("[LLM] Загрузка тел функций Oracle...");
            loadOracleFunctions();
            System.out.println("[LLM] Загрузка тел функций Oracle завершена.");
        }
        if (config.isIncludePostgresFunctions()) {
            System.out.println("[LLM] Загрузка тел функций PostgreSQL...");
            loadPostgresFunctions();
            System.out.println("[LLM] Загрузка тел функций PostgreSQL завершена.");
        }
        if (config.isIncludeBrokerFunctions()) {
            System.out.println("[LLM] Загрузка брокеров...");
            loadBrokerFunctions();
            System.out.println("[LLM] Загрузка брокеров завершена.");
        }

        System.out.println("[LLM] Подготовка контекста завершена.");
        return context;
    }

    private void loadPostgresData() {
        if (!DatabaseCacheManager.isPostgresServerAvailable()) {
            System.out.println("[PostgreSQL] Сервер недоступен, пропускаем загрузку данных");
            context.setPostgresViewDDL(Collections.emptyMap());
            context.setPostgresViewTables(Collections.emptyMap());
            return;
        }
        Map<String, String> pgViewsDDL = new LinkedHashMap<>();
        Map<String, Set<String>> pgViewTables = new LinkedHashMap<>();
        int count = 0;
        int total = context.getAllViews().size();
        System.out.println("[PostgreSQL] Загрузка DDL вьюх (" + total + " шт.)...");

        for (String view : context.getAllViews()) {
            if (stopCondition.getAsBoolean()) {
                System.out.println("[PostgreSQL] Загрузка прервана пользователем");
                break;
            }
            count++;
            System.out.println("[PostgreSQL]  [" + count + "/" + total + "] Загрузка вьюхи: " + view);
            String ddl = postgresService.getViewDDL(view);
            if (ddl != null) {
                pgViewsDDL.put(view, ddl);
                pgViewTables.put(view, extractTablesFromDDL(ddl));
                System.out.println("[PostgreSQL]    OK (" + pgViewTables.get(view).size() + " таблиц)");
            } else {
                System.out.println("[PostgreSQL]    НЕ НАЙДЕНА");
            }
        }
        context.setPostgresViewDDL(pgViewsDDL);
        context.setPostgresViewTables(pgViewTables);
        System.out.println("[PostgreSQL] Загружено DDL вьюх: " + pgViewsDDL.size());

        if (config.isIncludePostgresTables() && !pgViewTables.isEmpty()) {
            Set<String> allTables = new LinkedHashSet<>();
            for (Set<String> tables : pgViewTables.values()) {
                allTables.addAll(tables);
            }
            System.out.println("[PostgreSQL] Загрузка DDL таблиц (" + allTables.size() + " шт.)...");
            Map<String, String> tableDDL = new LinkedHashMap<>();
            int tableCount = 0;
            for (String table : allTables) {
                if (stopCondition.getAsBoolean()) break;
                tableCount++;
                System.out.println("[PostgreSQL]   [" + tableCount + "/" + allTables.size() + "] Загрузка таблицы: " + table);
                String ddl = postgresService.getTableDDL(table);
                if (ddl != null) {
                    tableDDL.put(table, ddl);
                    System.out.println("[PostgreSQL]      OK");
                } else {
                    System.out.println("[PostgreSQL]      НЕ НАЙДЕНА");
                }
            }
            context.setPostgresTableDDL(tableDDL);
            System.out.println("[PostgreSQL] Загружено DDL таблиц: " + tableDDL.size());
        }
    }

    private void loadOracleData() {
        if (!DatabaseCacheManager.isOracleServerAvailable()) {
            System.out.println("[Oracle] Сервер недоступен, пропускаем загрузку данных");
            context.setOracleViewDDL(Collections.emptyMap());
            context.setOracleViewTables(Collections.emptyMap());
            return;
        }
        Map<String, String> oraViewsDDL = new LinkedHashMap<>();
        Map<String, Set<String>> oraViewTables = new LinkedHashMap<>();

        Set<String> allViews = context.getAllViews();
        int total = allViews.size();
        int count = 0;

        System.out.println("[Oracle] Загрузка DDL вьюх (" + total + " шт.)...");

        for (String view : allViews) {
            if (stopCondition.getAsBoolean()) {
                System.out.println("[Oracle] Загрузка прервана пользователем");
                break;
            }
            count++;
            System.out.println("[Oracle]  [" + count + "/" + total + "] Загрузка вьюхи: " + view);
            String ddl = oracleService.getViewDDL(view);
            if (ddl != null) {
                oraViewsDDL.put(view, ddl);
                oraViewTables.put(view, extractTablesFromDDL(ddl));
                System.out.println("[Oracle]    OK (" + oraViewTables.get(view).size() + " таблиц)");
            } else {
                System.out.println("[Oracle]    НЕ НАЙДЕНА");
            }
        }
        context.setOracleViewDDL(oraViewsDDL);
        context.setOracleViewTables(oraViewTables);
        System.out.println("[Oracle] Загружено DDL вьюх: " + oraViewsDDL.size());

        if (config.isIncludeOracleTables() && !oraViewTables.isEmpty()) {
            Set<String> allTables = new LinkedHashSet<>();
            for (Set<String> tables : oraViewTables.values()) {
                allTables.addAll(tables);
            }
            System.out.println("[Oracle] Загрузка DDL таблиц (" + allTables.size() + " шт.)...");

            Map<String, String> tableDDL = new LinkedHashMap<>();
            int tableCount = 0;
            for (String table : allTables) {
                if (stopCondition.getAsBoolean()) break;
                tableCount++;
                System.out.println("[Oracle]   [" + tableCount + "/" + allTables.size() + "] Загрузка таблицы: " + table);
                String ddl = oracleService.getTableDDL(table);
                if (ddl != null) {
                    tableDDL.put(table, ddl);
                    System.out.println("[Oracle]      OK");
                } else {
                    System.out.println("[Oracle]      НЕ НАЙДЕНА");
                }
            }
            context.setOracleTableDDL(tableDDL);
            System.out.println("[Oracle] Загружено DDL таблиц: " + tableDDL.size());
        }
    }

    private void loadOracleFunctions() {
        Set<String> funcs = new LinkedHashSet<>();
        for (SqlInfo sql : context.getAllSqlQueries()) {
            for (String pf : sql.getPackagesFunctions()) {
                if (pf.contains(".") && !pf.startsWith("D_PKG_CONSTANTS") &&
                        !pf.startsWith("D_PKG_OPTIONS") && !pf.startsWith("D_PKG_OPTION_SPECS")) {
                    funcs.add(pf);
                }
            }
        }

        if (funcs.isEmpty()) {
            System.out.println("[Oracle] Нет функций для загрузки");
            context.setOracleFunctionBodies(new LinkedHashMap<>());
            return;
        }

        System.out.println("[Oracle] Загрузка тел функций (" + funcs.size() + " шт.)...");

        Map<String, String> bodies = new LinkedHashMap<>();
        int count = 0;
        for (String fullName : funcs) {
            if (stopCondition.getAsBoolean()) break;
            count++;
            System.out.println("[Oracle]   [" + count + "/" + funcs.size() + "] Загрузка функции: " + fullName);
            int dot = fullName.lastIndexOf('.');
            if (dot > 0) {
                String pkg = fullName.substring(0, dot);
                String func = fullName.substring(dot + 1);
                String body = oracleService.getFunctionBody(pkg, func);
                if (body != null && !body.isEmpty()) {
                    bodies.put(fullName, body);
                    System.out.println("[Oracle]      OK (" + body.length() + " симв.)");
                } else {
                    System.out.println("[Oracle]      НЕ НАЙДЕНА");
                }
            }
        }
        context.setOracleFunctionBodies(bodies);
        System.out.println("[Oracle] Загружено тел функций: " + bodies.size());
    }

    private void loadPostgresFunctions() {
        Set<String> funcs = new LinkedHashSet<>();
        for (SqlInfo sql : context.getAllSqlQueries()) {
            for (String pf : sql.getPackagesFunctions()) {
                String name = pf.toLowerCase();
                if (name.contains("(")) {
                    name = name.substring(0, name.indexOf("("));
                }
                funcs.add(name);
            }
        }

        if (funcs.isEmpty()) {
            System.out.println("[PostgreSQL] Нет функций для загрузки");
            context.setPostgresFunctionBodies(new LinkedHashMap<>());
            return;
        }

        System.out.println("[PostgreSQL] Загрузка тел функций (" + funcs.size() + " шт.)...");

        Map<String, String> bodies = new LinkedHashMap<>();
        int count = 0;
        for (String name : funcs) {
            if (stopCondition.getAsBoolean()) break;
            count++;
            System.out.println("[PostgreSQL]   [" + count + "/" + funcs.size() + "] Загрузка функции: " + name);
            String body = postgresService.getFunctionBody(name);
            if (body != null && !body.isEmpty()) {
                bodies.put(name, body);
                System.out.println("[PostgreSQL]      OK (" + body.length() + " симв.)");
            } else {
                System.out.println("[PostgreSQL]      НЕ НАЙДЕНА");
            }
        }
        context.setPostgresFunctionBodies(bodies);
        System.out.println("[PostgreSQL] Загружено тел функций: " + bodies.size());
    }

    /**
     * Загружает брокеры и соответствующие им функции из БД
     */
    private void loadBrokerFunctions() {
        Set<BrokerInfo> brokers = new LinkedHashSet<>();

        System.out.println("[Broker] Сбор брокеров из форм...");

        // 1. Собираем брокеры из всех форм
        for (FormInfo form : context.getAnalyzedForms()) {
            for (BrokerInfo broker : form.getBrokers()) {
                // Добавляем брокер с информацией о форме
                broker.setSourcePath(form.getFormPath());
                broker.setBaseFormPath(form.getBaseFormPath());
                brokers.add(broker);
            }
        }

        if (brokers.isEmpty()) {
            System.out.println("[Broker] Брокеры не найдены");
            context.setBrokersMap(new LinkedHashMap<>());
            context.setOracleBrokerFunctions(new LinkedHashMap<>());
            context.setPostgresBrokerFunctions(new LinkedHashMap<>());
            return;
        }

        System.out.println("[Broker] Найдено брокеров: " + brokers.size());
        System.out.println("[Broker] Разрешение брокеров (поиск execProc)...");

        // 2. Разрешаем брокеры (ищем execProc)
        Map<String, BrokerInfo> resolvedBrokers = new LinkedHashMap<>();
        Map<String, String> oracleBrokerFuncs = new LinkedHashMap<>();
        Map<String, String> postgresBrokerFuncs = new LinkedHashMap<>();

        int count = 0;
        for (BrokerInfo broker : brokers) {
            if (stopCondition.getAsBoolean()) break;
            count++;

            if (broker.getType() == BrokerInfo.BrokerType.TYPE1_UNIT_ACTION) {
                // Тип 1: unit + action - нужен поиск в D_UNITBPS
                System.out.println("[Broker]   [" + count + "/" + brokers.size() +
                        "] Поиск: unit=" + broker.getUnit() +
                        ", action=" + broker.getAction());

                String execProc = findExecProc(broker.getUnit(), broker.getAction());
                if (execProc != null) {
                    broker.setExecProc(execProc);
                    String key = broker.getUnit() + "_" + broker.getAction();
                    resolvedBrokers.put(key, broker);
                    System.out.println("[Broker]      Найден execProc: " + execProc);

                    // Загружаем Oracle тело функции
                    if (config.isIncludeOracleFunctions() && execProc.contains(".")) {
                        int dot = execProc.lastIndexOf('.');
                        String pkg = execProc.substring(0, dot);
                        String func = execProc.substring(dot + 1);
                        String body = oracleService.getFunctionBody(pkg, func);
                        if (body != null && !body.isEmpty()) {
                            oracleBrokerFuncs.put(execProc, body);
                            System.out.println("[Broker]        Oracle тело функции загружено (" + body.length() + " симв.)");
                        } else {
                            System.out.println("[Broker]        Oracle тело функции НЕ НАЙДЕНО");
                        }
                    }

                    // Загружаем PostgreSQL тело функции
                    if (config.isIncludePostgresFunctions()) {
                        String body = postgresService.getFunctionBody(execProc.toLowerCase());
                        if (body != null && !body.isEmpty()) {
                            postgresBrokerFuncs.put(execProc.toLowerCase(), body);
                            System.out.println("[Broker]        PostgreSQL тело функции загружено (" + body.length() + " симв.)");
                        } else {
                            System.out.println("[Broker]        PostgreSQL тело функции НЕ НАЙДЕНО");
                        }
                    }
                } else {
                    System.out.println("[Broker]      execProc НЕ НАЙДЕН");
                }

            } else {
                // Тип 2: прямое указание функции
                System.out.println("[Broker]   [" + count + "/" + brokers.size() +
                        "] Прямая функция: " + broker.getFunctionName());

                String execProc = broker.getFunctionName();
                broker.setExecProc(execProc);
                resolvedBrokers.put(execProc, broker);

                // Загружаем Oracle тело функции
                if (config.isIncludeOracleFunctions() && execProc.contains(".")) {
                    int dot = execProc.lastIndexOf('.');
                    String pkg = execProc.substring(0, dot);
                    String func = execProc.substring(dot + 1);
                    String body = oracleService.getFunctionBody(pkg, func);
                    if (body != null && !body.isEmpty()) {
                        oracleBrokerFuncs.put(execProc, body);
                        System.out.println("[Broker]        Oracle тело функции загружено (" + body.length() + " симв.)");
                    } else {
                        System.out.println("[Broker]        Oracle тело функции НЕ НАЙДЕНО");
                    }
                }

                // Загружаем PostgreSQL тело функции
                if (config.isIncludePostgresFunctions()) {
                    String body = postgresService.getFunctionBody(execProc.toLowerCase());
                    if (body != null && !body.isEmpty()) {
                        postgresBrokerFuncs.put(execProc.toLowerCase(), body);
                        System.out.println("[Broker]        PostgreSQL тело функции загружено (" + body.length() + " симв.)");
                    } else {
                        System.out.println("[Broker]        PostgreSQL тело функции НЕ НАЙДЕНО");
                    }
                }
            }
        }

        context.setBrokersMap(resolvedBrokers);
        context.setOracleBrokerFunctions(oracleBrokerFuncs);
        context.setPostgresBrokerFunctions(postgresBrokerFuncs);

        System.out.println("[Broker] Разрешено брокеров: " + resolvedBrokers.size());
        System.out.println("[Broker] Загружено Oracle тел функций: " + oracleBrokerFuncs.size());
        System.out.println("[Broker] Загружено PostgreSQL тел функций: " + postgresBrokerFuncs.size());
    }

    private String extractValue(String str, String key) {
        Pattern pattern = Pattern.compile(key + "[:]([^\\s;]+)");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


    private Set<String> extractTablesFromDDL(String ddl) {
        Set<String> tables = new LinkedHashSet<>();
        if (ddl == null) return tables;
        String upper = ddl.toUpperCase();
        Pattern p = Pattern.compile("\\b(FROM|JOIN)\\s+([A-Z_][A-Z0-9_]+)\\b");
        Matcher m = p.matcher(upper);
        Set<String> sqlKeywords = new HashSet<>(Arrays.asList(
                "SELECT", "FROM", "WHERE", "AND", "OR", "ON", "IN", "NOT", "EXISTS",
                "AS", "LEFT", "RIGHT", "INNER", "OUTER", "CROSS", "FULL", "JOIN",
                "UNION", "INTERSECT", "MINUS", "WITH", "RECURSIVE", "ORDER", "GROUP",
                "HAVING", "BY", "ASC", "DESC", "NULLS", "FIRST", "LAST", "CASE",
                "WHEN", "THEN", "ELSE", "END", "DISTINCT", "ALL", "ANY", "SOME"
        ));
        while (m.find()) {
            String table = m.group(2);
            if (table.startsWith("D_") && !table.startsWith("D_V_") && !sqlKeywords.contains(table)) {
                tables.add(table);
            }
        }
        return tables;
    }

    public String generateSingleFile() throws Exception {
        System.out.println("[LLM] Генерация единого промпта...");

        StringBuilder sb = new StringBuilder();

        // Заголовок
        if (context.getAnalyzedForms().size() == 1) {
            FormInfo form = context.getAnalyzedForms().get(0);
            sb.append("# ЗАПРОС К LLM: АНАЛИЗ ФОРМЫ ").append(form.getFormPath()).append("\n\n");

            // Добавляем информацию о стиле в заголовок
            if (form.isD3Style()) {
                sb.append("> **⚠️ ВАЖНО:** Эта форма использует **D3 синтаксис** (компоненты с префиксом `cmp`).\n");
                sb.append("> Для открытия формы используется метод `openD3Form()`.\n\n");
            } else if (form.isM2Style()) {
                sb.append("> **ℹ️ ИНФОРМАЦИЯ:** Эта форма использует **M2 синтаксис** (компоненты с `cmptype`).\n");
                sb.append("> Для открытия формы используется метод `openWindow()`.\n\n");
            }
        }

        sb.append("> **Обозначения:** 🟠 — Oracle Database, 🐘 — PostgreSQL\n\n");

        sb.append("## Контекст задачи\n\n");
        sb.append("Перед тобой техническая документация по форме(ам) системы T-MIS. ");
        sb.append("Формы содержат SQL запросы, которые обращаются к представлениям (вьюхам) ");
        sb.append("и таблицам в базах данных Oracle и PostgreSQL.\n\n");

        if (context.getAnalyzedForms().size() == 1) {
            FormInfo form = context.getAnalyzedForms().get(0);
            sb.append("**Анализируемая форма:** ").append(form.getFormPath()).append("\n");
            sb.append("**Базовая форма:** ").append(form.getBaseFormPath()).append("\n");
            if (form.isFullyReplaced()) {
                sb.append("**Статус:** ПОЛНОСТЬЮ ЗАМЕНЕНА\n");
                sb.append("**Файл замены:** ").append(form.getReplacementPath()).append("\n");
            } else if (!form.getOverrides().isEmpty()) {
                sb.append("**Статус:** ЧАСТИЧНО ПЕРЕОПРЕДЕЛЕНА\n");
            }
            sb.append("\n");

            // Статистика формы
            appendFormStatistics(sb, form);
        }

        sb.append("**Задача:** Проанализировать предоставленные SQL запросы, вьюхи и DDL таблиц, ");
        sb.append("чтобы понять бизнес-логику системы и взаимосвязи между объектами.\n\n");
        sb.append("**Дата генерации:** ").append(new Date()).append("\n\n");
        sb.append("---\n\n");

        // Генерация блоков
        System.out.println("[LLM] Генерация блока: Информация о форме");
        sb.append(generateFormInfoBlock());

        System.out.println("[LLM] Генерация блока: UserForms");
        sb.append(generateUserFormsBlock());

        System.out.println("[LLM] Генерация блока: SubForm и JS формы");
        sb.append(generateSubFormsAndJsFormsBlock());

        System.out.println("[LLM] Генерация блока: Отчёты");
        sb.append(generateReportsBlock());

        System.out.println("[LLM] Генерация блока: SQL запросы");
        sb.append(generateSqlQueriesBlock());

        System.out.println("[LLM] Генерация блока: PostgreSQL вьюхи");
        sb.append(generatePostgresViewsBlock());

        System.out.println("[LLM] Генерация блока: Oracle вьюхи");
        sb.append(generateOracleViewsBlock());

        System.out.println("[LLM] Генерация блока: Router компоненты");
        sb.append(generateRoutersBlock());

        System.out.println("[LLM] Генерация блока: Константы и системные опции");
        sb.append(generateConstantsAndOptionsBlock());

        System.out.println("[LLM] Генерация блока: Композиции");
        sb.append(generateCompositionsBlock());

        System.out.println("[LLM] Генерация блока: Брокеры");
        sb.append(generateBrokerFunctionsBlock());

        System.out.println("[LLM] Генерация блока: PostgreSQL таблицы");
        sb.append(generatePostgresTablesBlock());

        System.out.println("[LLM] Генерация блока: Oracle таблицы");
        sb.append(generateOracleTablesBlock());

        System.out.println("[LLM] Генерация блока: Oracle функции");
        sb.append(generateOracleFunctionsBlock());

        System.out.println("[LLM] Генерация блока: PostgreSQL функции");
        sb.append(generatePostgresFunctionsBlock());

        System.out.println("[LLM] Генерация блока: Контекстное меню");
        sb.append(generatePopupMenusBlock());

        System.out.println("[LLM] Генерация блока: Неопределённые объекты");
        sb.append(generateUnknownObjectsBlock());

        sb.append("\n\n---\n\n");
        String instruction = config.getLlmInstructionText();
        if (instruction == null || instruction.trim().isEmpty()) {
            instruction = getDefaultInstruction();
        }
        sb.append(instruction);

        System.out.println("[LLM] Генерация промпта завершена. Размер: " + sb.length() + " симв.");
        return sb.toString();
    }

    /**
     * Добавляет статистику формы в отчёт
     */
    private void appendFormStatistics(StringBuilder sb, FormInfo form) {
        sb.append("\n**Статистика формы:**\n");
        sb.append("- SQL запросов: ").append(form.getSqlQueries().size()).append("\n");
        sb.append("- Вьюх и таблиц: ").append(form.getTablesViews().size()).append("\n");
        sb.append("- Пакетов и функций: ").append(form.getPackagesFunctions().size()).append("\n");
        sb.append("- SubForm: ").append(form.getSubForms().size()).append("\n");
        sb.append("- JS форм: ").append(form.getJsForms().size()).append("\n");
        sb.append("- Констант: ").append(form.getConstants().size()).append("\n");
        sb.append("- Системных опций: ").append(form.getSystemOptions().size()).append("\n");
        sb.append("- Брокеров: ").append(form.getBrokers().size()).append("\n");

        if (form.getConversionStatistics() != null) {
            sb.append("- Конвертация SQL: ");
            sb.append(form.getConversionStatistics().getConvertedQueries()).append("/")
                    .append(form.getConversionStatistics().getTotalQueries())
                    .append(" (").append(String.format("%.1f", form.getConversionStatistics().getConversionPercent()))
                    .append("%)\n");
        }
        sb.append("\n");
    }

    /**
     * Генерирует блок с основной информацией о форме
     */
    // core/llm/LLMPromptGenerator.java - в generateFormInfoBlock()

    private String generateFormInfoBlock() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n## 1. ИНФОРМАЦИЯ О ФОРМЕ\n\n");

        if (context.getAnalyzedForms().size() == 1) {
            FormInfo form = context.getAnalyzedForms().get(0);
            sb.append("| Параметр | Значение |\n");
            sb.append("|----------|----------|\n");
            sb.append("| Путь к форме | `").append(form.getFormPath()).append("` |\n");
            sb.append("| Базовая форма | `").append(form.getBaseFormPath()).append("` |\n");

            // НОВОЕ: информация о стиле формы
            sb.append("| **Стиль формы** | **").append(form.getFormStyle().getName()).append("** |\n");
            sb.append("| **Синтаксис компонентов** | `").append(form.getFormStyle().getSyntax()).append("` |\n");
            sb.append("| **Метод открытия** | `").append(form.getFormStyle().getOpenMethod()).append("` |\n");

            if (form.isFullyReplaced()) {
                sb.append("| Статус | **ПОЛНОСТЬЮ ЗАМЕНЕНА** |\n");
                sb.append("| Файл замены | `").append(form.getReplacementPath()).append("` |\n");
            } else if (!form.getOverrides().isEmpty()) {
                sb.append("| Статус | **ЧАСТИЧНО ПЕРЕОПРЕДЕЛЕНА** |\n");
            } else {
                sb.append("| Статус | БАЗОВАЯ ФОРМА |\n");
            }
            sb.append("\n");
        } else {
            sb.append("Анализируется ").append(context.getAnalyzedForms().size()).append(" форм.\n\n");
        }

        return sb.toString();
    }

    /**
     * Генерирует блок UserForms
     */
    private String generateUserFormsBlock() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n## 2. ЮЗЕРФОРМЫ (ПЕРЕОПРЕДЕЛЕНИЯ)\n\n");

        boolean hasAny = false;
        for (FormInfo form : context.getAnalyzedForms()) {
            if (form.getOverrides().isEmpty() && !form.isFullyReplaced()) continue;

            hasAny = true;
            sb.append("### Форма: ").append(form.getFormPath()).append("\n\n");

            if (form.isFullyReplaced() && form.getReplacementPath() != null) {
                sb.append("**ПОЛНАЯ ЗАМЕНА:** `").append(form.getReplacementPath()).append("`\n\n");
            }

            if (!form.getOverrides().isEmpty()) {
                Map<String, List<FormInfo.OverrideInfo>> byRegion = new LinkedHashMap<>();
                for (FormInfo.OverrideInfo ov : form.getOverrides()) {
                    byRegion.computeIfAbsent(ov.getRegionName(), k -> new ArrayList<>()).add(ov);
                }

                for (Map.Entry<String, List<FormInfo.OverrideInfo>> entry : byRegion.entrySet()) {
                    sb.append("**Регион:** `").append(entry.getKey()).append("`\n\n");

                    for (FormInfo.OverrideInfo ov : entry.getValue()) {
                        String typeDesc;
                        switch (ov.getType()) {
                            case FULL_OVERRIDE: typeDesc = "🔵 ПОЛНАЯ ЗАМЕНА"; break;
                            case PARTIAL_OVERRIDE: typeDesc = "🟡 ЧАСТИЧНОЕ ПЕРЕОПРЕДЕЛЕНИЕ"; break;
                            case DOT_D_OVERRIDE: typeDesc = "🟢 .d КАТАЛОГ"; break;
                            default: typeDesc = "⚪ " + ov.getType().name();
                        }
                        sb.append("- **").append(typeDesc).append(":** `").append(ov.getOverridePath()).append("`\n");
                    }
                    sb.append("\n");
                }
            }
            sb.append("\n");
        }

        if (!hasAny) {
            sb.append("Переопределения (UserForms) не найдены.\n\n");
        }

        return sb.toString();
    }

    /**
     * Генерирует блок SubForm и JS форм
     */
    private String generateSubFormsAndJsFormsBlock() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n## 3. СВЯЗИ С ДРУГИМИ ФОРМАМИ\n\n");

        boolean hasAny = false;
        for (FormInfo form : context.getAnalyzedForms()) {
            boolean hasSubForms = form.getSubForms() != null && !form.getSubForms().isEmpty();
            boolean hasJsForms = form.getJsForms() != null && !form.getJsForms().isEmpty();

            if (!hasSubForms && !hasJsForms) continue;

            hasAny = true;
            sb.append("### Форма: ").append(form.getFormPath()).append("\n\n");

            if (hasSubForms) {
                sb.append("**SubForm (вложенные подформы):**\n");
                for (String subForm : form.getSubForms()) {
                    sb.append("- `").append(subForm).append("`\n");
                }
                sb.append("\n");
            }

            if (hasJsForms) {
                sb.append("**JS формы (вызываемые из JavaScript):**\n");
                for (String jsForm : form.getJsForms()) {
                    sb.append("- `").append(jsForm).append("`\n");
                }
                sb.append("\n");
            }
        }

        if (!hasAny) {
            sb.append("Связи с другими формами не найдены.\n\n");
        }

        return sb.toString();
    }

    /**
     * Генерирует блок отчётов
     */
    private String generateReportsBlock() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n## 4. ОТЧЁТЫ\n\n");

        boolean hasAny = false;
        for (FormInfo form : context.getAnalyzedForms()) {
            boolean hasReports = form.getReports() != null && !form.getReports().isEmpty();
            boolean hasAutoPopupReports = form.getReportsFromAutoPopup() != null && !form.getReportsFromAutoPopup().isEmpty();

            if (!hasReports && !hasAutoPopupReports) continue;

            hasAny = true;
            sb.append("### Форма: ").append(form.getFormPath()).append("\n\n");

            if (hasReports) {
                sb.append("**Отчёты, вызываемые на форме:**\n");
                for (String report : form.getReports()) {
                    sb.append("- `").append(report).append("`\n");
                }
                sb.append("\n");
            }

            if (hasAutoPopupReports) {
                sb.append("**Отчёты из AutoPopupMenu:**\n");
                for (FormInfo.ReportFromAutoPopupInfo report : form.getReportsFromAutoPopup()) {
                    sb.append("- `").append(report.getRepCode()).append("` (")
                            .append(report.getRepTypeName()).append(")");
                    if (report.getFormPath() != null && !report.getFormPath().isEmpty()) {
                        sb.append(" → `").append(report.getFormPath()).append("`");
                    }
                    sb.append("\n");
                }
                sb.append("\n");
            }
        }

        if (!hasAny) {
            sb.append("Отчёты не найдены.\n\n");
        }

        return sb.toString();
    }

    private String generateSqlQueriesBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n## 5. SQL ЗАПРОСЫ С ТЭГАМИ\n\n");

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

    private String generatePostgresViewsBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n## 6. ТЕКСТ ВЬЮХ ИЗ POSTGRESQL 🐘\n\n");

        Map<String, String> viewDDL = context.getPostgresViewDDL();
        Map<String, Set<String>> viewUsage = context.getViewUsageInForms();

        if (viewDDL == null || viewDDL.isEmpty()) {
            sb.append("Вьюхи в PostgreSQL не найдены или не удалось получить DDL.\n\n");
            return sb.toString();
        }

        sb.append("Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, ");
        sb.append("извлеченные из базы данных PostgreSQL.\n\n");

        sb.append("**Статистика:**\n");
        sb.append("- Всего вьюх: ").append(viewDDL.size()).append("\n\n");

        int viewNum = 1;
        for (Map.Entry<String, String> entry : viewDDL.entrySet()) {
            String viewName = entry.getKey();
            String ddl = entry.getValue();
            Set<String> usedInForms = viewUsage.get(viewName);

            sb.append("---\n\n");
            sb.append("### Вьюха №").append(viewNum).append(": ").append(viewName).append("\n\n");

            if (usedInForms != null && !usedInForms.isEmpty()) {
                sb.append("**Используется в формах:**\n");
                for (String form : usedInForms) {
                    sb.append("- ").append(form).append("\n");
                }
                sb.append("\n");
            }

            sb.append("**DDL определение:**\n\n");
            sb.append("```sql\n");
            sb.append(ddl);
            if (!ddl.endsWith("\n")) {
                sb.append("\n");
            }
            sb.append("```\n\n");

            viewNum++;
        }

        return sb.toString();
    }

    private String generateOracleViewsBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n## 7. ТЕКСТ ВЬЮХ ИЗ ORACLE 🟠\n\n");
        Map<String, String> viewDDL = context.getOracleViewDDL();
        Map<String, Set<String>> viewUsage = context.getViewUsageInForms();

        if (viewDDL == null || viewDDL.isEmpty()) {
            sb.append("Вьюхи в Oracle не найдены или не удалось получить DDL.\n\n");
            return sb.toString();
        }

        sb.append("Ниже представлены определения всех вьюх (D_V_*), найденных в SQL запросах форм, ");
        sb.append("извлеченные из базы данных Oracle.\n\n");

        sb.append("**Статистика:**\n");
        sb.append("- Всего вьюх: ").append(viewDDL.size()).append("\n\n");

        int viewNum = 1;
        for (Map.Entry<String, String> entry : viewDDL.entrySet()) {
            String viewName = entry.getKey();
            String ddl = entry.getValue();
            Set<String> usedInForms = viewUsage.get(viewName);

            sb.append("---\n\n");
            sb.append("### Вьюха №").append(viewNum).append(": ").append(viewName).append("\n\n");

            if (usedInForms != null && !usedInForms.isEmpty()) {
                sb.append("**Используется в формах:**\n");
                for (String form : usedInForms) {
                    sb.append("- ").append(form).append("\n");
                }
                sb.append("\n");
            }

            sb.append("**DDL определение:**\n\n");
            sb.append("```sql\n");
            sb.append(ddl);
            if (!ddl.endsWith("\n")) {
                sb.append("\n");
            }
            sb.append("```\n\n");

            viewNum++;
        }

        return sb.toString();
    }

    private String generateRoutersBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n## 8. ROUTER КОМПОНЕНТЫ (ActionRouter / DataSetRouter)\n\n");

        if (context == null || context.getAnalyzedForms() == null) {
            sb.append("Данные о Router компонентах не найдены.\n\n");
            return sb.toString();
        }

        boolean hasAnyRouters = false;

        for (FormInfo form : context.getAnalyzedForms()) {
            // Фильтруем только converted = true
            List<RouterInfo> convertedActionRouters = new ArrayList<>();
            for (RouterInfo router : form.getActionRouters()) {
                if (router.isConverted()) {
                    convertedActionRouters.add(router);
                }
            }

            List<RouterInfo> convertedDataSetRouters = new ArrayList<>();
            for (RouterInfo router : form.getDataSetRouters()) {
                if (router.isConverted()) {
                    convertedDataSetRouters.add(router);
                }
            }

            boolean hasActionRouters = !convertedActionRouters.isEmpty();
            boolean hasDataSetRouters = !convertedDataSetRouters.isEmpty();

            if (!hasActionRouters && !hasDataSetRouters) {
                continue;
            }

            hasAnyRouters = true;

            sb.append("### Форма: ").append(form.getFormPath()).append("\n\n");

            // ActionRouters
            if (hasActionRouters) {
                sb.append("#### ActionRouter (Action / BeforeAction)\n\n");
                for (RouterInfo router : convertedActionRouters) {
                    appendRouterInfo(sb, router, 0);
                }
            }

            // DataSetRouters
            if (hasDataSetRouters) {
                sb.append("#### DataSetRouter (DataSet / BeforeSelect)\n\n");
                for (RouterInfo router : convertedDataSetRouters) {
                    appendRouterInfo(sb, router, 0);
                }
            }

            sb.append("\n---\n\n");
        }

        if (!hasAnyRouters) {
            sb.append("Router компоненты (ActionRouter/DataSetRouter) не найдены в анализируемых формах.\n\n");
        }

        return sb.toString();
    }

    private void appendRouterInfo(StringBuilder sb, RouterInfo router, int level) {
        String indent = "  ".repeat(level);
        String childIndent = "  ".repeat(level + 1);

        sb.append(indent).append("**").append(router.getParentType().getDisplayName())
                .append(":** `").append(router.getName()).append("`");

        if (router.getRouterType() == RouterInfo.RouterType.ACTION_ROUTER) {
            sb.append(" [ActionRouter]");
        } else {
            sb.append(" [DataSetRouter]");
        }
        sb.append(" *(").append(router.getFormStyle().getName()).append(" стиль)*");
        sb.append("\n\n");

        // Роутеры
        if (!router.getRouters().isEmpty()) {
            sb.append(childIndent).append("**Условия и SQL блоки:**\n\n");
            for (RouterItem item : router.getRouters()) {
                sb.append(childIndent).append("- **Условие:** `").append(item.getCondition() != null ? item.getCondition() : "default")
                        .append("`");

                if (item.getUnit() != null && !item.getUnit().isEmpty()) {
                    sb.append(", **unit:** `").append(item.getUnit()).append("`");
                }
                if (item.getAction() != null && !item.getAction().isEmpty()) {
                    sb.append(", **action:** `").append(item.getAction()).append("`");
                }
                sb.append("\n");

                if (item.getSqlContent() != null && !item.getSqlContent().isEmpty()) {
                    sb.append(childIndent).append("  **SQL/PLSQL код:**\n\n");
                    sb.append(childIndent).append("  ```sql\n");
                    String[] lines = item.getSqlContent().split("\\r?\\n");
                    for (String line : lines) {
                        sb.append(childIndent).append("  ").append(line).append("\n");
                    }
                    sb.append(childIndent).append("  ```\n\n");
                }
            }
        }

        // Переменные
        if (!router.getVariables().isEmpty()) {
            sb.append(childIndent).append("**Переменные:**\n\n");
            sb.append(childIndent).append("| Имя | Источник (src) | Тип источника | GET | PUT | Тип |\n");
            sb.append(childIndent).append("|-----|----------------|---------------|-----|-----|-----|\n");
            for (RouterVariable var : router.getVariables()) {
                sb.append(childIndent).append("| `").append(var.getName()).append("` | ")
                        .append(var.getSrc() != null ? "`" + var.getSrc() + "`" : "-").append(" | ")
                        .append(var.getSrcType() != null ? var.getSrcType() : "-").append(" | ")
                        .append(var.getGet() != null && !var.getGet().isEmpty() ? var.getGet() : "-").append(" | ")
                        .append(var.getPut() != null && !var.getPut().isEmpty() ? var.getPut() : "-").append(" | ")
                        .append(var.getType() != null ? var.getType() : "-").append(" |\n");
            }
            sb.append("\n");
        }

        // Вложенные SubAction/SubSelect
        if (!router.getSubRouters().isEmpty()) {
            sb.append(childIndent).append("**Вложенные компоненты:**\n\n");
            for (SubRouterInfo subRouter : router.getSubRouters()) {
                appendSubRouterInfo(sb, subRouter, level + 2);
            }
        }
    }

    private void appendSubRouterInfo(StringBuilder sb, SubRouterInfo subRouter, int level) {
        String indent = "  ".repeat(level);
        String childIndent = "  ".repeat(level + 1);

        sb.append(indent).append("**").append(subRouter.getType().getDisplayName())
                .append(":** `").append(subRouter.getName()).append("`");

        if (subRouter.getGroupName() != null && !subRouter.getGroupName().isEmpty()) {
            sb.append(" (groupname: `").append(subRouter.getGroupName()).append("`)");
        }
        if (subRouter.getExecon() != null && !subRouter.getExecon().isEmpty()) {
            sb.append(" [execon: ").append(subRouter.getExecon()).append("]");
        }
        if (subRouter.getMode() != null && !subRouter.getMode().isEmpty()) {
            sb.append(" [mode: ").append(subRouter.getMode()).append("]");
        }
        if (subRouter.isSavepoint()) {
            sb.append(" [savepoint]");
        }
        sb.append("\n\n");

        // Роутеры внутри SubAction/SubSelect
        if (!subRouter.getRouters().isEmpty()) {
            sb.append(childIndent).append("**Условия и SQL блоки:**\n\n");
            for (RouterItem item : subRouter.getRouters()) {
                sb.append(childIndent).append("- **Условие:** `").append(item.getCondition() != null ? item.getCondition() : "default")
                        .append("`\n");

                if (item.getSqlContent() != null && !item.getSqlContent().isEmpty()) {
                    sb.append(childIndent).append("  **SQL/PLSQL код:**\n\n");
                    sb.append(childIndent).append("  ```sql\n");
                    String[] lines = item.getSqlContent().split("\\r?\\n");
                    for (String line : lines) {
                        sb.append(childIndent).append("  ").append(line).append("\n");
                    }
                    sb.append(childIndent).append("  ```\n\n");
                }
            }
        }

        // Переменные SubActionVar
        if (!subRouter.getVariables().isEmpty()) {
            sb.append(childIndent).append("**Переменные:**\n\n");
            sb.append(childIndent).append("| Имя | Источник (src) | Тип источника | PUT |\n");
            sb.append(childIndent).append("|-----|----------------|---------------|-----|\n");
            for (RouterVariable var : subRouter.getVariables()) {
                sb.append(childIndent).append("| `").append(var.getName()).append("` | ")
                        .append(var.getSrc() != null ? "`" + var.getSrc() + "`" : "-").append(" | ")
                        .append(var.getSrcType() != null ? var.getSrcType() : "-").append(" | ")
                        .append(var.getPut() != null && !var.getPut().isEmpty() ? var.getPut() : "-").append(" |\n");
            }
            sb.append("\n");
        }
    }

    /**
     * Генерирует блок констант и системных опций
     */
    private String generateConstantsAndOptionsBlock() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n## 9. КОНСТАНТЫ И СИСТЕМНЫЕ ОПЦИИ\n\n");

        boolean hasAny = false;
        for (FormInfo form : context.getAnalyzedForms()) {
            boolean hasConstants = form.getConstants() != null && !form.getConstants().isEmpty();
            boolean hasOptions = form.getSystemOptions() != null && !form.getSystemOptions().isEmpty();

            if (!hasConstants && !hasOptions) continue;

            hasAny = true;
            sb.append("### Форма: ").append(form.getFormPath()).append("\n\n");

            if (hasConstants) {
                sb.append("**Константы (D_PKG_CONSTANTS.SEARCH_*):**\n");
                for (String constant : form.getConstants()) {
                    sb.append("- `").append(constant).append("`\n");
                }
                sb.append("\n");
            }

            if (hasOptions) {
                sb.append("**Системные опции (D_PKG_OPTIONS.GET):**\n");
                for (String option : form.getSystemOptions()) {
                    sb.append("- `").append(option).append("`\n");
                }
                sb.append("\n");
            }
        }

        if (!hasAny) {
            sb.append("Константы и системные опции не найдены.\n\n");
        }

        return sb.toString();
    }

    /**
     * Генерирует блок композиций
     */
    private String generateCompositionsBlock() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n## 10. КОМПОЗИЦИИ (UnitEdit / UniversalComposition)\n\n");

        boolean hasAny = false;
        for (FormInfo form : context.getAnalyzedForms()) {
            boolean hasUnitComps = form.getUnitCompositions() != null && !form.getUnitCompositions().isEmpty();
            boolean hasJsUnitComps = form.getJsUnitCompositions() != null && !form.getJsUnitCompositions().isEmpty();
            boolean hasOpenFormComps = form.getOpenFormCompositions() != null && !form.getOpenFormCompositions().isEmpty();
            boolean hasOpenD3FormComps = form.getOpenD3FormCompositions() != null && !form.getOpenD3FormCompositions().isEmpty();

            if (!hasUnitComps && !hasJsUnitComps && !hasOpenFormComps && !hasOpenD3FormComps) continue;

            hasAny = true;
            sb.append("### Форма: ").append(form.getFormPath()).append("\n\n");

            if (hasUnitComps) {
                sb.append("**Unit композиции (из тегов):**\n");
                for (String comp : form.getUnitCompositions()) {
                    sb.append("- `").append(comp).append("`\n");
                }
                sb.append("\n");
            }

            if (hasJsUnitComps) {
                sb.append("**JS Unit композиции (из вызовов openWindow/openD3Form):**\n");
                for (String comp : form.getJsUnitCompositions()) {
                    sb.append("- `").append(comp).append("`\n");
                }
                sb.append("\n");
            }

            if (hasOpenFormComps) {
                sb.append("**openForm композиции (System/composition):**\n");
                for (String comp : form.getOpenFormCompositions()) {
                    sb.append("- `").append(comp).append("`\n");
                }
                sb.append("\n");
            }

            if (hasOpenD3FormComps) {
                sb.append("**openD3Form композиции (System/composition):**\n");
                for (String comp : form.getOpenD3FormCompositions()) {
                    sb.append("- `").append(comp).append("`\n");
                }
                sb.append("\n");
            }
        }

        if (!hasAny) {
            sb.append("Композиции не найдены.\n\n");
        }

        return sb.toString();
    }

    /**
     * Генерирует блок брокеров и вызываемых функций для LLM промпта
     */
    private String generateBrokerFunctionsBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n## 11. БРОКЕРЫ И ВЫЗЫВАЕМЫЕ ФУНКЦИИ\n\n");

        Map<String, BrokerInfo> brokersMap = context.getBrokersMap();
        Map<String, String> oracleBrokerFunctions = context.getOracleBrokerFunctions();
        Map<String, String> postgresBrokerFunctions = context.getPostgresBrokerFunctions();

        if (brokersMap == null || brokersMap.isEmpty()) {
            sb.append("Брокеры для анализа не найдены.\n\n");
            return sb.toString();
        }

        sb.append("Ниже представлены брокеры (Action с атрибутами unit/action или прямым указанием функции), ");
        sb.append("соответствующие им функции, а также DDL этих функций.\n\n");

        sb.append("**Статистика:**\n");
        sb.append("- Всего брокеров: ").append(brokersMap.size()).append("\n\n");

        long type1Count = brokersMap.values().stream()
                .filter(b -> b.getType() == BrokerInfo.BrokerType.TYPE1_UNIT_ACTION)
                .count();
        long type2Count = brokersMap.values().stream()
                .filter(b -> b.getType() == BrokerInfo.BrokerType.TYPE2_DIRECT_FUNCTION)
                .count();

        sb.append("**Типы брокеров:**\n");
        sb.append("- Тип 1 (unit + action, требуется поиск): ").append(type1Count).append("\n");
        sb.append("- Тип 2 (прямое указание функции): ").append(type2Count).append("\n\n");

        int brokerNum = 1;
        for (Map.Entry<String, BrokerInfo> entry : brokersMap.entrySet()) {
            BrokerInfo broker = entry.getValue();

            sb.append("---\n\n");
            sb.append("### Брокер №").append(brokerNum).append(": ");

            if (broker.getType() == BrokerInfo.BrokerType.TYPE1_UNIT_ACTION) {
                sb.append("unit=").append(broker.getUnit()).append(", action=").append(broker.getAction());
            } else {
                sb.append("action=").append(broker.getFunctionName());
            }
            sb.append("\n\n");

            // ========== ИНФОРМАЦИЯ О КОМПОНЕНТЕ ==========
            if (broker.getComponentName() != null && !broker.getComponentName().isEmpty()) {
                sb.append("**Компонент:** `").append(broker.getComponentName()).append("`");
                if (broker.getComponentType() != null && !broker.getComponentType().isEmpty()) {
                    sb.append(" (").append(broker.getComponentType()).append(")");
                }
                sb.append("\n\n");
            }

            // ========== ПЕРЕМЕННЫЕ (ActionVar) ==========
            if (broker.getVariables() != null && !broker.getVariables().isEmpty()) {
                sb.append("**Переменные (ActionVar/DataSetVar):**\n\n");
                sb.append("| Имя | Источник (src) | Тип источника | GET | PUT | Тип |\n");
                sb.append("|-----|----------------|---------------|-----|-----|-----|\n");
                for (RouterVariable var : broker.getVariables()) {
                    sb.append("| `").append(var.getName()).append("` | ");
                    sb.append(var.getSrc() != null ? "`" + var.getSrc() + "`" : "-").append(" | ");
                    sb.append(var.getSrcType() != null ? var.getSrcType() : "-").append(" | ");
                    sb.append(var.getGet() != null && !var.getGet().isEmpty() ? var.getGet() : "-").append(" | ");
                    sb.append(var.getPut() != null && !var.getPut().isEmpty() ? var.getPut() : "-").append(" | ");
                    sb.append(var.getType() != null ? var.getType() : "-").append(" |\n");
                }
                sb.append("\n");
            }

            // ========== ТИП БРОКЕРА ==========
            sb.append("**Тип брокера:** ");
            sb.append(broker.getType() == BrokerInfo.BrokerType.TYPE1_UNIT_ACTION ?
                    "Требуется поиск в D_UNITBPS" : "Прямое указание функции");
            sb.append("\n\n");

            // ========== ВЫЗЫВАЕМАЯ ФУНКЦИЯ ==========
            sb.append("**Вызываемая функция (execProc):**\n");
            sb.append("```\n");
            sb.append(broker.getExecProc() != null ? broker.getExecProc() : "не найдена");
            sb.append("\n```\n\n");

            if (broker.getExecProc() != null) {
                // Oracle функция
                String oracleBody = oracleBrokerFunctions != null ?
                        oracleBrokerFunctions.get(broker.getExecProc()) : null;
                if (oracleBody != null && !oracleBody.isEmpty()) {
                    sb.append("**Oracle SQL тело функции 🟠:**\n\n");
                    sb.append("```sql\n");
                    sb.append(oracleBody);
                    if (!oracleBody.endsWith("\n")) {
                        sb.append("\n");
                    }
                    sb.append("```\n\n");
                } else {
                    sb.append("**Oracle SQL тело функции не найдено.**\n\n");
                }

                // PostgreSQL функция
                String postgresBody = postgresBrokerFunctions != null ?
                        postgresBrokerFunctions.get(broker.getExecProc().toLowerCase()) : null;
                if (postgresBody != null && !postgresBody.isEmpty()) {
                    sb.append("**PostgreSQL тело функции 🐘:**\n\n");
                    sb.append("```sql\n");
                    sb.append(postgresBody);
                    if (!postgresBody.endsWith("\n")) {
                        sb.append("\n");
                    }
                    sb.append("```\n\n");
                } else {
                    sb.append("**PostgreSQL тело функции не найдено.**\n\n");
                }
            }

            brokerNum++;
        }

        return sb.toString();
    }

    private String generatePostgresTablesBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n## 12. DDL ТАБЛИЦ ИЗ POSTGRESQL ВЬЮХ\n\n");

        Map<String, Set<String>> viewTables = context.getPostgresViewTables();
        Map<String, String> tableDDL = context.getPostgresTableDDL();

        if (viewTables == null || viewTables.isEmpty()) {
            sb.append("Таблицы из PostgreSQL вьюх не найдены.\n\n");
            return sb.toString();
        }

        sb.append("Ниже представлен список всех таблиц, которые используются внутри вьюх PostgreSQL, ");
        sb.append("а также их DDL определения.\n\n");

        Set<String> allTables = new LinkedHashSet<>();
        for (Set<String> tables : viewTables.values()) {
            allTables.addAll(tables);
        }

        sb.append("**Статистика:**\n");
        sb.append("- Всего вьюх с таблицами: ").append(viewTables.size()).append("\n");
        sb.append("- Всего уникальных таблиц: ").append(allTables.size()).append("\n\n");

        // Связь вьюх и таблиц
        sb.append("### Связь вьюх и таблиц\n\n");
        for (Map.Entry<String, Set<String>> entry : viewTables.entrySet()) {
            String viewName = entry.getKey();
            Set<String> tables = entry.getValue();

            sb.append("**").append(viewName).append("** использует таблицы:\n");
            for (String table : tables) {
                sb.append("- ").append(table).append("\n");
            }
            sb.append("\n");
        }

        // DDL таблиц
        sb.append("### DDL определения таблиц\n\n");

        int tableNum = 1;
        for (String tableName : allTables) {
            String ddl = tableDDL != null ? tableDDL.get(tableName) : null;

            sb.append("---\n\n");
            sb.append("#### Таблица №").append(tableNum).append(": ").append(tableName).append("\n\n");

            if (ddl != null && !ddl.isEmpty()) {
                sb.append("```sql\n");
                sb.append(ddl);
                if (!ddl.endsWith("\n")) {
                    sb.append("\n");
                }
                sb.append("```\n\n");
            } else {
                sb.append("DDL не найден для таблицы ").append(tableName).append("\n\n");
            }

            tableNum++;
        }

        return sb.toString();
    }

    private String generateOracleTablesBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n## 13. DDL ТАБЛИЦ ИЗ ORACLE ВЬЮХ\n\n");

        Map<String, Set<String>> viewTables = context.getOracleViewTables();
        Map<String, String> tableDDL = context.getOracleTableDDL();

        if (viewTables == null || viewTables.isEmpty()) {
            sb.append("Таблицы из Oracle вьюх не найдены.\n\n");
            return sb.toString();
        }

        sb.append("Ниже представлен список всех таблиц, которые используются внутри вьюх Oracle, ");
        sb.append("а также их DDL определения.\n\n");

        Set<String> allTables = new LinkedHashSet<>();
        for (Set<String> tables : viewTables.values()) {
            allTables.addAll(tables);
        }

        sb.append("**Статистика:**\n");
        sb.append("- Всего вьюх с таблицами: ").append(viewTables.size()).append("\n");
        sb.append("- Всего уникальных таблиц: ").append(allTables.size()).append("\n\n");

        // Связь вьюх и таблиц
        sb.append("### Связь вьюх и таблиц\n\n");
        for (Map.Entry<String, Set<String>> entry : viewTables.entrySet()) {
            String viewName = entry.getKey();
            Set<String> tables = entry.getValue();

            sb.append("**").append(viewName).append("** использует таблицы:\n");
            for (String table : tables) {
                sb.append("- ").append(table).append("\n");
            }
            sb.append("\n");
        }

        // DDL таблиц
        sb.append("### DDL определения таблиц\n\n");

        int tableNum = 1;
        for (String tableName : allTables) {
            String ddl = tableDDL != null ? tableDDL.get(tableName) : null;

            sb.append("---\n\n");
            sb.append("#### Таблица №").append(tableNum).append(": ").append(tableName).append("\n\n");

            if (ddl != null && !ddl.isEmpty()) {
                sb.append("```sql\n");
                sb.append(ddl);
                if (!ddl.endsWith("\n")) {
                    sb.append("\n");
                }
                sb.append("```\n\n");
            } else {
                sb.append("DDL не найден для таблицы ").append(tableName).append("\n\n");
            }

            tableNum++;
        }

        return sb.toString();
    }

    private String generateOracleFunctionsBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n## 14. ТЕЛА ФУНКЦИЙ ИЗ ORACLE ПАКЕТОВ 🟠\n\n");

        Map<String, String> functionBodies = context.getOracleFunctionBodies();

        Set<String> allPackageFunctions = new LinkedHashSet<>();
        for (SqlInfo sql : context.getAllSqlQueries()) {
            for (String pf : sql.getPackagesFunctions()) {
                if (pf.contains(".") &&
                        !pf.toUpperCase().contains("D_PKG_CONSTANTS") &&
                        !pf.toUpperCase().contains("D_PKG_OPTIONS") &&
                        !pf.toUpperCase().contains("D_PKG_OPTION_SPECS")) {
                    allPackageFunctions.add(pf);
                }
            }
        }

        if (allPackageFunctions.isEmpty()) {
            sb.append("Пакетные функции для анализа не найдены.\n\n");
            return sb.toString();
        }

        sb.append("Ниже представлены тела функций из Oracle пакетов, ");
        sb.append("которые используются в SQL запросах форм.\n\n");

        sb.append("**Статистика:**\n");
        sb.append("- Всего уникальных пакетных функций: ").append(allPackageFunctions.size()).append("\n");
        sb.append("- Загружено тел функций: ").append(functionBodies != null ? functionBodies.size() : 0).append("\n\n");

        int funcNum = 1;
        for (String funcName : allPackageFunctions) {
            sb.append("---\n\n");
            sb.append("### Функция №").append(funcNum).append(": ").append(funcName).append("\n\n");

            String body = (functionBodies != null) ? functionBodies.get(funcName) : null;

            if (body != null && !body.isEmpty()) {
                sb.append("```sql\n");
                sb.append(body);
                if (!body.endsWith("\n")) {
                    sb.append("\n");
                }
                sb.append("```\n\n");
            } else {
                sb.append("Тело функции не найдено в Oracle (возможно функция в спецификации пакета или нет доступа).\n\n");
            }

            funcNum++;
        }

        return sb.toString();
    }

    private String generatePostgresFunctionsBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n## 15. ТЕЛА ФУНКЦИЙ И ПРОЦЕДУР ИЗ POSTGRESQL 🐘\n\n");

        Map<String, String> functionBodies = context.getPostgresFunctionBodies();

        Set<String> allFunctions = new LinkedHashSet<>();
        for (SqlInfo sql : context.getAllSqlQueries()) {
            for (String pf : sql.getPackagesFunctions()) {
                String name = pf.toLowerCase();
                if (name.contains("(")) {
                    name = name.substring(0, name.indexOf("("));
                }
                allFunctions.add(name);
            }
        }

        if (allFunctions.isEmpty()) {
            sb.append("Функции для анализа не найдены.\n\n");
            return sb.toString();
        }

        sb.append("Ниже представлены тела функций и процедур из PostgreSQL, ");
        sb.append("которые используются в SQL запросах форм.\n\n");

        sb.append("**Статистика:**\n");
        sb.append("- Всего уникальных функций/процедур: ").append(allFunctions.size()).append("\n");
        sb.append("- Загружено тел функций: ").append(functionBodies != null ? functionBodies.size() : 0).append("\n\n");

        int funcNum = 1;
        for (String funcName : allFunctions) {
            sb.append("---\n\n");
            sb.append("### Функция №").append(funcNum).append(": ").append(funcName).append("\n\n");

            String body = (functionBodies != null) ? functionBodies.get(funcName) : null;

            if (body != null && !body.isEmpty()) {
                sb.append("```sql\n");
                sb.append(body);
                if (!body.endsWith("\n")) {
                    sb.append("\n");
                }
                sb.append("```\n\n");
            } else {
                sb.append("Тело функции/процедуры не найдено в PostgreSQL.\n\n");
            }

            funcNum++;
        }

        return sb.toString();
    }

    /**
     * Генерирует блок контекстного меню (PopupMenu)
     */
    private String generatePopupMenusBlock() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n## 16. КОНТЕКСТНОЕ МЕНЮ (ПКМ)\n\n");

        boolean hasAny = false;
        for (FormInfo form : context.getAnalyzedForms()) {
            boolean hasOracleMenus = form.getPopupMenus() != null && !form.getPopupMenus().isEmpty();
            boolean hasPostgresMenus = form.getPopupMenusPg() != null && !form.getPopupMenusPg().isEmpty();
            boolean hasAutoPopup = form.getAutoPopupMenus() != null && !form.getAutoPopupMenus().isEmpty();

            if (!hasOracleMenus && !hasPostgresMenus && !hasAutoPopup) continue;

            hasAny = true;
            sb.append("### Форма: ").append(form.getFormPath()).append("\n\n");

            if (hasAutoPopup) {
                sb.append("**AutoPopupMenu (коды подключаемого меню):**\n");
                for (String unit : form.getAutoPopupMenus()) {
                    sb.append("- `").append(unit).append("`\n");
                }
                sb.append("\n");
            }

            if (hasOracleMenus) {
                sb.append("**Контекстное меню (Oracle):**\n\n");
                for (PopupMenuInfo menu : form.getPopupMenus()) {
                    appendPopupMenuTree(sb, menu, 0, false);
                }
            }

            if (hasPostgresMenus) {
                sb.append("**Контекстное меню (PostgreSQL):**\n\n");
                for (PopupMenuInfo menu : form.getPopupMenusPg()) {
                    appendPopupMenuTree(sb, menu, 0, true);
                }
            }
        }

        if (!hasAny) {
            sb.append("Контекстное меню не найдено.\n\n");
        }

        return sb.toString();
    }

    /**
     * Рекурсивный вывод дерева контекстного меню
     */
    private void appendPopupMenuTree(StringBuilder sb, PopupMenuInfo menu, int level, boolean isPostgres) {
        String indent = "  ".repeat(level);
        String prefix = level == 0 ? "📁 " : "  ";

        sb.append(indent).append(prefix).append("**").append(menu.getName()).append("**");
        if (isPostgres) {
            sb.append(" (PostgreSQL)");
        }
        sb.append("\n");

        for (PopupMenuInfo.MenuItem item : menu.getRootItems()) {
            appendMenuItemTree(sb, item, level + 1);
        }
        sb.append("\n");
    }

    /**
     * Рекурсивный вывод пункта меню
     */
    private void appendMenuItemTree(StringBuilder sb, PopupMenuInfo.MenuItem item, int level) {
        String indent = "  ".repeat(level);
        String prefix = item.hasChildren() ? "📂 " : "📄 ";

        String displayText;
        if (item.isDbReport() && item.getCaption() != null) {
            displayText = item.getCaption();
        } else if (item.getCaption() != null && !item.getCaption().isEmpty()) {
            displayText = "\"" + item.getCaption() + "\"";
        } else if (item.getName() != null && !item.getName().isEmpty()) {
            displayText = "name=\"" + item.getName() + "\"";
        } else {
            displayText = "(без названия)";
        }

        if (item.isFromAutoPopup() && item.getAutoPopupName() != null) {
            displayText = "(AutoPopup \"" + item.getAutoPopupName() + "\") " + displayText;
        }

        sb.append(indent).append(prefix).append(displayText).append("\n");

        for (PopupMenuInfo.MenuItem child : item.getChildren()) {
            appendMenuItemTree(sb, child, level + 1);
        }
    }

    /**
     * Генерирует блок неопределённых объектов
     */
    private String generateUnknownObjectsBlock() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n## 17. НЕОПРЕДЕЛЁННЫЕ ОБЪЕКТЫ (ТРЕБУЮТ РАЗБОРА)\n\n");

        boolean hasAny = false;
        for (FormInfo form : context.getAnalyzedForms()) {
            if (form.getUnknownObjects() == null || form.getUnknownObjects().isEmpty()) continue;

            hasAny = true;
            sb.append("### Форма: ").append(form.getFormPath()).append("\n\n");
            for (String obj : form.getUnknownObjects()) {
                sb.append("- `").append(obj).append("`\n");
            }
            sb.append("\n");
        }

        if (!hasAny) {
            sb.append("Неопределённые объекты не найдены.\n\n");
        }

        return sb.toString();
    }

    private String getDefaultInstruction() {
        return "## ИНСТРУКЦИЯ ДЛЯ АНАЛИЗА\n\n" +
                "Пожалуйста, проанализируй предоставленную информацию и ответь на следующие вопросы:\n\n" +
                "1. Какие основные бизнес-сущности используются в системе?\n" +
                "2. Какие связи между таблицами можно выделить?\n" +
                "3. Есть ли потенциальные проблемы с производительностью в SQL запросах?\n" +
                "4. Какие вьюхи наиболее часто используются и почему?\n" +
                "5. Есть ли дублирование логики между разными SQL запросами?\n" +
                "6. Какие рекомендации по оптимизации можно дать?\n\n" +
                "При анализе обращай особое внимание на:\n" +
                "- Префиксы объектов: D_V_* (вьюхи), D_* (таблицы), D_PKG_* (пакеты/функции)\n" +
                "- Константы из D_PKG_CONSTANTS.SEARCH_*\n" +
                "- Системные опции из D_PKG_OPTIONS.GET\n\n";
    }



    /**
     * Генерирует промпт для одной формы и сохраняет в MD файл
     * @param formInfo информация о форме
     * @param outputDir директория для сохранения
     * @return путь к созданному файлу
     */
    public String generateForSingleForm(FormInfo formInfo, String outputDir) throws Exception {
        // Создаём временный контекст для одной формы
        LLMReportContext singleContext = new LLMReportContext();
        List<FormInfo> singleForm = Collections.singletonList(formInfo);
        singleContext.setAnalyzedForms(singleForm);
        singleContext.setTotalForms(1);
        singleContext.setAllSqlQueries(formInfo.getSqlQueries());
        singleContext.setTotalSqlQueries(formInfo.getSqlQueries().size());

        // Копируем необходимые данные из глобального контекста если есть
        if (context != null) {
            // Вьюхи для этой формы
            Set<String> formViews = new LinkedHashSet<>();
            for (String tv : formInfo.getTablesViews()) {
                if (tv.startsWith("D_V_")) {
                    formViews.add(tv);
                }
            }

            Map<String, String> pgViewsDDL = new LinkedHashMap<>();
            Map<String, String> oraViewsDDL = new LinkedHashMap<>();
            Map<String, Set<String>> pgViewTables = new LinkedHashMap<>();
            Map<String, Set<String>> oraViewTables = new LinkedHashMap<>();

            for (String view : formViews) {
                if (context.getPostgresViewDDL() != null && context.getPostgresViewDDL().containsKey(view)) {
                    pgViewsDDL.put(view, context.getPostgresViewDDL().get(view));
                    if (context.getPostgresViewTables() != null) {
                        pgViewTables.put(view, context.getPostgresViewTables().get(view));
                    }
                }
                if (context.getOracleViewDDL() != null && context.getOracleViewDDL().containsKey(view)) {
                    oraViewsDDL.put(view, context.getOracleViewDDL().get(view));
                    if (context.getOracleViewTables() != null) {
                        oraViewTables.put(view, context.getOracleViewTables().get(view));
                    }
                }
            }
            singleContext.setPostgresViewDDL(pgViewsDDL);
            singleContext.setPostgresViewTables(pgViewTables);
            singleContext.setOracleViewDDL(oraViewsDDL);
            singleContext.setOracleViewTables(oraViewTables);

            // Таблицы для этой формы
            Set<String> formTables = new LinkedHashSet<>();
            for (String tv : formInfo.getTablesViews()) {
                if (!tv.startsWith("D_V_")) {
                    formTables.add(tv);
                }
            }
            for (Set<String> tables : pgViewTables.values()) {
                formTables.addAll(tables);
            }
            for (Set<String> tables : oraViewTables.values()) {
                formTables.addAll(tables);
            }

            Map<String, String> pgTableDDL = new LinkedHashMap<>();
            Map<String, String> oraTableDDL = new LinkedHashMap<>();
            for (String table : formTables) {
                if (context.getPostgresTableDDL() != null && context.getPostgresTableDDL().containsKey(table)) {
                    pgTableDDL.put(table, context.getPostgresTableDDL().get(table));
                }
                if (context.getOracleTableDDL() != null && context.getOracleTableDDL().containsKey(table)) {
                    oraTableDDL.put(table, context.getOracleTableDDL().get(table));
                }
            }
            singleContext.setPostgresTableDDL(pgTableDDL);
            singleContext.setOracleTableDDL(oraTableDDL);

            // Функции для этой формы
            Set<String> formFuncs = new LinkedHashSet<>();
            for (SqlInfo sql : formInfo.getSqlQueries()) {
                formFuncs.addAll(sql.getPackagesFunctions());
            }

            Map<String, String> oraFuncBodies = new LinkedHashMap<>();
            Map<String, String> pgFuncBodies = new LinkedHashMap<>();
            for (String func : formFuncs) {
                if (context.getOracleFunctionBodies() != null && context.getOracleFunctionBodies().containsKey(func)) {
                    oraFuncBodies.put(func, context.getOracleFunctionBodies().get(func));
                }
                String lowerFunc = func.toLowerCase();
                if (context.getPostgresFunctionBodies() != null && context.getPostgresFunctionBodies().containsKey(lowerFunc)) {
                    pgFuncBodies.put(lowerFunc, context.getPostgresFunctionBodies().get(lowerFunc));
                }
            }
            singleContext.setOracleFunctionBodies(oraFuncBodies);
            singleContext.setPostgresFunctionBodies(pgFuncBodies);
        }

        // Временно заменяем контекст
        LLMReportContext originalContext = this.context;
        this.context = singleContext;

        // Генерируем промпт
        String prompt = generateSingleFile();

        // Восстанавливаем контекст
        this.context = originalContext;

        // Добавляем исходный текст формы
        String formSourceCode = getFormSourceCode(formInfo.getFormPath());

        // Создаём итоговый промпт с исходным кодом формы
        StringBuilder finalPrompt = new StringBuilder();
        finalPrompt.append(prompt);
        finalPrompt.append("\n\n");
        finalPrompt.append("---\n\n");
        finalPrompt.append("## ИСХОДНЫЙ ТЕКСТ ФОРМЫ\n\n");
        finalPrompt.append("Ниже представлен исходный XML код анализируемой формы:\n\n");
        finalPrompt.append(formSourceCode);
        finalPrompt.append("\n\n");

        // Формируем имя файла (аналогично отчёту, но с .md)
        String safeName = getSafeFileNameForMD(formInfo.getFormPath());

        // Сохраняем в подкаталог MD_reports
        Path mdSubDir = Paths.get(outputDir, "MD_reports");
        if (!Files.exists(mdSubDir)) {
            Files.createDirectories(mdSubDir);
        }
        Path mdFilePath = mdSubDir.resolve(safeName);

        // Сохраняем файл
        Files.writeString(mdFilePath, finalPrompt.toString());

        System.out.println("[LLM] MD промпт сохранён: " + mdFilePath);
        return mdFilePath.toString();
    }

    /**
     * Формирует безопасное имя файла для MD промпта
     */
    private String getSafeFileNameForMD(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        // Убираем маркер SubForm если есть
        if (normalized.startsWith("(sub)_")) {
            normalized = normalized.substring(6);
        }
        String safeName = normalized.replace("/", "#").replace("\\", "#");
        return safeName + ".md";
    }

    /**
     * Получает исходный текст формы
     * @param formPath путь к форме
     * @return исходный текст формы
     */
    private String getFormSourceCode(String formPath) {
        try {
            Path formFilePath = Paths.get(settings.getProjectPath(), formPath);
            if (Files.exists(formFilePath)) {
                String content = Files.readString(formFilePath);
                // Экранируем для Markdown
                return "```xml\n" + content + "\n```";
            } else {
                return "Файл формы не найден: " + formPath;
            }
        } catch (IOException e) {
            return "Ошибка чтения файла формы: " + e.getMessage();
        }
    }
    private FormInfo.FormStyle detectFormStyleFromForm(FormInfo form) {
        // Проверяем по наличию Router компонентов
        for (RouterInfo router : form.getActionRouters()) {
            if (router.getFormStyle() != FormInfo.FormStyle.UNKNOWN) {
                return router.getFormStyle();
            }
        }
        for (RouterInfo router : form.getDataSetRouters()) {
            if (router.getFormStyle() != FormInfo.FormStyle.UNKNOWN) {
                return router.getFormStyle();
            }
        }

        // Если не определили по Router, можно по содержимому формы
        // (но это потребовало бы загрузки исходного кода)
        return FormInfo.FormStyle.UNKNOWN;
    }

    /**
     * Поиск execProc в таблице D_UNITBPS по unit и action
     */
    private String findExecProc(String unit, String action) {
        return DatabaseCacheManager.getBrokerExecProc(unit, action, () -> {
            String sql = "SELECT execproc FROM D_UNITBPS " +
                    "WHERE UPPER(unitbpcode) LIKE ? AND UPPER(standard_action) LIKE ? AND ROWNUM = 1";
            Properties props = new Properties();
            props.setProperty("user", settings.getOracleUser());
            props.setProperty("password", settings.getOraclePassword());
            props.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
            props.setProperty("oracle.jdbc.ReadTimeout", "30000");

            try (Connection conn = DriverManager.getConnection(settings.getOracleUrl(), props);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, "%" + unit.toUpperCase() + "%");
                pstmt.setString(2, "%" + action.toUpperCase() + "%");
                pstmt.setQueryTimeout(30);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return rs.getString("execproc");
                }
            } catch (SQLException e) {
                System.err.println("[Broker] Ошибка поиска execProc: " + e.getMessage());
            }
            return null;
        });
    }
}