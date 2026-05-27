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
    /**
     * Генерирует блок Router компонентов с заданием для LLM по конвертации
     */
    private String generateRoutersBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n## 8. ROUTER КОМПОНЕНТЫ (ActionRouter / DataSetRouter)\n\n");

        if (context == null || context.getAnalyzedForms() == null) {
            sb.append("Данные о Router компонентах не найдены.\n\n");
            return sb.toString();
        }

        // Собираем все роутеры для анализа
        List<RouterInfo> allRouters = new ArrayList<>();
        List<RouterInfo> convertedRouters = new ArrayList<>();
        List<RouterInfo> nonConvertedRouters = new ArrayList<>();

        for (FormInfo form : context.getAnalyzedForms()) {
            for (RouterInfo router : form.getActionRouters()) {
                allRouters.add(router);
                if (router.isConverted()) {
                    convertedRouters.add(router);
                } else {
                    nonConvertedRouters.add(router);
                }
            }
            for (RouterInfo router : form.getDataSetRouters()) {
                allRouters.add(router);
                if (router.isConverted()) {
                    convertedRouters.add(router);
                } else {
                    nonConvertedRouters.add(router);
                }
            }
        }

        if (allRouters.isEmpty()) {
            sb.append("Router компоненты (ActionRouter/DataSetRouter) не найдены в анализируемых формах.\n\n");
            return sb.toString();
        }

        // ========== 1. СТАТИСТИКА ==========
        sb.append("### Статистика Router компонентов\n\n");
        sb.append("| Тип | Количество |\n");
        sb.append("|-----|------------|\n");
        sb.append("| ✅ Конвертированные роутеры (converted=true) | ").append(convertedRouters.size()).append(" |\n");
        sb.append("| ⚠️ Неконвертированные роутеры (converted=false) | ").append(nonConvertedRouters.size()).append(" |\n");
        sb.append("| **Всего** | **").append(allRouters.size()).append("** |\n\n");

        // ========== 2. ЗАДАНИЕ ДЛЯ LLM ==========
        if (!nonConvertedRouters.isEmpty()) {
            sb.append("---\n\n");
            sb.append("## 🎯 ЗАДАНИЕ ДЛЯ LLM: КОНВЕРТАЦИЯ ORACLE SQL В POSTGRESQL\n\n");

            sb.append("### Контекст\n\n");
            sb.append("В системе T-MIS正在进行 миграция с Oracle на PostgreSQL. ");
            sb.append("Ниже представлены **несконвертированные роутеры** (`converted = false`), ");
            sb.append("которые содержат Oracle SQL запросы или брокеры.\n\n");

            sb.append("**Для SQL запросов:**\n");
            sb.append("1. Проанализировать Oracle SQL запрос в блоке `TYPE_DATABASE=ORACLE`\n");
            sb.append("2. Создать PostgreSQL версию этого запроса\n");
            sb.append("3. Вставить результат в блок `TYPE_DATABASE=POSTGRE && MODE_DATABASE=tmis`\n\n");

            sb.append("**Для брокеров (Action с unit/action):**\n");
            sb.append("1. Сохранить оригинальный Oracle вызов в блоке `TYPE_DATABASE=ORACLE`\n");
            sb.append("2. Выполнить SQL запрос к таблице D_UNITBPS для получения CALL_PROCEDURE\n");
            sb.append("3. Создать PostgreSQL блок с вызовом полученной функции через `CALL`\n");
            sb.append("4. Перенести все переменные (`cmpActionVar`) без изменений\n");
            sb.append("5. Добавить комментарии к каждому параметру (источник данных)\n");
            sb.append("6. Сохранить отступы по одной вертикальной линии\n\n");
        }

        // ========== 3. НЕКОНВЕРТИРОВАННЫЕ РОУТЕРЫ (converted=false) - ЗАДАНИЕ ==========
        if (!nonConvertedRouters.isEmpty()) {
            sb.append("---\n\n");
            sb.append("## ⚠️ НЕКОНВЕРТИРОВАННЫЕ РОУТЕРЫ (converted=false) - ТРЕБУЮТ КОНВЕРТАЦИИ\n\n");
            sb.append("Для каждого из следующих роутеров необходимо создать PostgreSQL версию.\n\n");

            for (FormInfo form : context.getAnalyzedForms()) {
                List<RouterInfo> formNonConvertedRouters = new ArrayList<>();
                for (RouterInfo router : form.getActionRouters()) {
                    if (!router.isConverted()) formNonConvertedRouters.add(router);
                }
                for (RouterInfo router : form.getDataSetRouters()) {
                    if (!router.isConverted()) formNonConvertedRouters.add(router);
                }

                if (formNonConvertedRouters.isEmpty()) continue;

                sb.append("### Форма: ").append(form.getFormPath()).append("\n\n");

                int taskNum = 1;
                for (RouterInfo router : formNonConvertedRouters) {
                    sb.append("#### Задание ").append(taskNum++).append(": ")
                            .append(router.getParentType().getDisplayName())
                            .append(" `").append(router.getName()).append("` (⚠️ converted=false)\n\n");

                    // Проверяем, является ли это брокером (имеет unit/action или прямую функцию)
                    boolean isBroker = false;
                    String unit = null;
                    String action = null;
                    String functionName = null;

                    for (RouterItem item : router.getRouters()) {
                        if (item.getUnit() != null && !item.getUnit().isEmpty()) {
                            isBroker = true;
                            unit = item.getUnit();
                            action = item.getAction();
                            break;
                        }
                        if (item.getAction() != null && item.getAction().startsWith("D_PKG_")) {
                            isBroker = true;
                            functionName = item.getAction();
                            break;
                        }
                    }

                    // Находим Oracle SQL или вызов
                    String oracleContent = null;
                    for (RouterItem item : router.getRouters()) {
                        if (item.isOracleRouter() && item.getSqlContent() != null) {
                            oracleContent = item.getSqlContent();
                            break;
                        }
                    }

                    if (isBroker && (unit != null || functionName != null)) {
                        // ========== ВЫПОЛНЯЕМ ЗАПРОС К D_UNITBPS ==========
                        List<Map<String, String>> queryResults = executeBrokerQuery(unit, action);

                        // ========== ВЫЗОВ generateBrokerBlock ==========
                        generateBrokerBlock(sb, router, unit, action, functionName, oracleContent, queryResults);
                        // ==============================================

                    } else if (oracleContent != null) {
                        // ========== ВЫВОД ДЛЯ SQL ЗАПРОСА ==========
                        sb.append("**Тип:** SQL запрос\n\n");

                        sb.append("**Исходный Oracle SQL:**\n\n");
                        sb.append("```sql\n");
                        sb.append(oracleContent);
                        sb.append("\n```\n\n");

                        if (!router.getVariables().isEmpty()) {
                            sb.append("**Переменные (bind-параметры):**\n\n");
                            for (RouterVariable var : router.getVariables()) {
                                sb.append("- `:").append(var.getName()).append("`");
                                if (var.getSrc() != null) sb.append(" → ").append(var.getSrc());
                                sb.append("\n");
                            }
                            sb.append("\n");
                        }

                        sb.append("**Ожидаемый результат после конвертации:**\n\n");
                        sb.append("```xml\n");
                        sb.append("<").append(router.getRouterType().getTagName())
                                .append(" condition=\"TYPE_DATABASE=POSTGRE && MODE_DATABASE=tmis\">\n");
                        sb.append("    <![CDATA[\n");
                        sb.append("    -- TODO: Вставить сконвертированный PostgreSQL SQL\n");
                        sb.append("    -- Требуется конвертация Oracle → PostgreSQL\n");
                        sb.append("    ]]>\n");
                        sb.append("</").append(router.getRouterType().getTagName()).append(">\n");
                        sb.append("```\n\n");
                    }
                }
            }
        }

        // ========== 4. КОНВЕРТИРОВАННЫЕ РОУТЕРЫ (converted=true) ==========
        if (!convertedRouters.isEmpty()) {
            sb.append("---\n\n");
            sb.append("## ✅ КОНВЕРТИРОВАННЫЕ РОУТЕРЫ (converted=true)\n\n");
            sb.append("Эти роутеры уже сконвертированы и готовы к использованию.\n\n");

            for (FormInfo form : context.getAnalyzedForms()) {
                List<RouterInfo> formConvertedRouters = new ArrayList<>();
                for (RouterInfo router : form.getActionRouters()) {
                    if (router.isConverted()) formConvertedRouters.add(router);
                }
                for (RouterInfo router : form.getDataSetRouters()) {
                    if (router.isConverted()) formConvertedRouters.add(router);
                }

                if (formConvertedRouters.isEmpty()) continue;

                sb.append("### Форма: ").append(form.getFormPath()).append("\n\n");

                for (RouterInfo router : formConvertedRouters) {
                    sb.append("#### ").append(router.getParentType().getDisplayName())
                            .append(": `").append(router.getName()).append("` (✅ converted=true)\n\n");

                    // Проверяем, является ли это брокером
                    boolean isBroker = false;
                    String unit = null;
                    String action = null;
                    String functionName = null;

                    for (RouterItem item : router.getRouters()) {
                        if (item.getUnit() != null && !item.getUnit().isEmpty()) {
                            isBroker = true;
                            unit = item.getUnit();
                            action = item.getAction();
                            break;
                        }
                        if (item.getAction() != null && item.getAction().startsWith("D_PKG_")) {
                            isBroker = true;
                            functionName = item.getAction();
                            break;
                        }
                    }

                    if (isBroker && (unit != null || functionName != null)) {
                        // ========== ВЫВОД БРОКЕРА В XML ФОРМАТЕ ==========
                        sb.append("```xml\n");
                        sb.append("<component cmptype=\"").append(router.getParentType().getDisplayName())
                                .append("\" name=\"").append(router.getName()).append("\">\n");

                        // Oracle роутер
                        sb.append("    <cmpActionRouter");
                        if (unit != null) {
                            sb.append(" unit=\"").append(unit).append("\"");
                        }
                        if (action != null && !action.isEmpty()) {
                            sb.append(" action=\"").append(action).append("\"");
                        }
                        sb.append(" condition=\"TYPE_DATABASE=ORACLE\"/>\n");

                        // PostgreSQL роутер с CDATA
                        sb.append("    <cmpActionRouter condition=\"TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis\">\n");
                        sb.append("        <![CDATA[\n");

                        // Находим PostgreSQL SQL
                        String postgresSql = null;
                        for (RouterItem item : router.getRouters()) {
                            if (item.isPostgresRouter() && item.getSqlContent() != null) {
                                postgresSql = item.getSqlContent();
                                break;
                            }
                        }

                        if (postgresSql != null) {
                            // Выводим существующий PostgreSQL SQL с сохранением форматирования
                            String[] lines = postgresSql.split("\\r?\\n");
                            for (String line : lines) {
                                sb.append("        ").append(line).append("\n");
                            }
                        } else {
                            sb.append("        -- PostgreSQL SQL не найден\n");
                        }

                        sb.append("        ]]>\n");
                        sb.append("    </cmpActionRouter>\n");

                        // Переменные
                        for (RouterVariable var : router.getVariables()) {
                            sb.append("    <cmpActionVar");
                            sb.append(" name=\"").append(var.getName()).append("\"");
                            if (var.getSrc() != null) sb.append(" src=\"").append(var.getSrc()).append("\"");
                            if (var.getSrcType() != null) sb.append(" srctype=\"").append(var.getSrcType()).append("\"");

                            boolean hasPut = var.getPut() != null && !var.getPut().isEmpty();
                            boolean hasGet = var.getGet() != null && !var.getGet().isEmpty();

                            if (hasGet && !hasPut) {
                                // get без put - не выводим
                            } else if (hasGet) {
                                sb.append(" get=\"").append(var.getGet()).append("\"");
                            }

                            if (hasPut) {
                                sb.append(" put=\"").append(var.getPut()).append("\"");
                            }
                            if (var.getLen() != null && !var.getLen().isEmpty()) {
                                sb.append(" len=\"").append(var.getLen()).append("\"");
                            }
                            sb.append("/>\n");
                        }

                        sb.append("</component>\n");
                        sb.append("```\n\n");

                    } else {
                        // ========== ВЫВОД SQL ЗАПРОСА ==========
                        // Находим Oracle и PostgreSQL SQL
                        String oracleSql = null;
                        String postgresSql = null;

                        for (RouterItem item : router.getRouters()) {
                            if (item.isOracleRouter() && item.getSqlContent() != null) {
                                oracleSql = item.getSqlContent();
                            }
                            if (item.isPostgresRouter() && item.getSqlContent() != null) {
                                postgresSql = item.getSqlContent();
                            }
                        }

                        if (oracleSql != null) {
                            sb.append("**Oracle SQL:**\n\n");
                            sb.append("```sql\n");
                            sb.append(oracleSql);
                            sb.append("\n```\n\n");
                        }

                        if (postgresSql != null) {
                            sb.append("**PostgreSQL SQL:**\n\n");
                            sb.append("```sql\n");
                            sb.append(postgresSql);
                            sb.append("\n```\n\n");
                        }
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * Выполняет запрос к таблице D_UNITBPS для получения CALL_PROCEDURE
     * @param unit код unit'а
     * @param action действие (может быть null)
     * @return список результатов запроса
     */
    private List<Map<String, String>> executeBrokerQuery(String unit, String action) {
        List<Map<String, String>> results = new ArrayList<>();

        if (unit == null || unit.isEmpty()) {
            return results;
        }

        String sql;
        if (action != null && !action.isEmpty()) {
            sql = "SELECT unitbpcode, standard_action, execproc FROM D_UNITBPS WHERE unitbpcode = ? AND standard_action = ?";
        } else {
            sql = "SELECT unitbpcode, standard_action, execproc FROM D_UNITBPS WHERE unitbpcode = ?";
        }

        Properties props = new Properties();
        props.setProperty("user", settings.getOracleUser());
        props.setProperty("password", settings.getOraclePassword());
        props.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
        props.setProperty("oracle.jdbc.ReadTimeout", "30000");

        try (Connection conn = DriverManager.getConnection(settings.getOracleUrl(), props);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, unit);
            if (action != null && !action.isEmpty()) {
                pstmt.setString(2, action);
            }
            pstmt.setQueryTimeout(30);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("unit", rs.getString("unitbpcode"));
                row.put("action", rs.getString("standard_action"));
                row.put("call_procedure", rs.getString("execproc"));
                results.add(row);
            }

        } catch (SQLException e) {
            System.err.println("[LLM] Ошибка выполнения запроса к D_UNITBPS: " + e.getMessage());
            // Возвращаем пустой результат, не прерывая генерацию отчёта
        }

        return results;
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

        if (functionBodies == null || functionBodies.isEmpty()) {
            sb.append("Пакетные функции для анализа не найдены.\n\n");
            return sb.toString();
        }

        sb.append("Ниже представлены тела функций из Oracle пакетов.\n\n");
        sb.append("**Статистика:**\n");
        sb.append("- Всего функций: ").append(functionBodies.size()).append("\n\n");

        int funcNum = 1;
        for (Map.Entry<String, String> entry : functionBodies.entrySet()) {
            String funcName = entry.getKey();
            String body = entry.getValue();

            sb.append("---\n\n");
            sb.append("### Функция №").append(funcNum).append(": ").append(funcName).append("\n\n");
            sb.append("```sql\n");
            sb.append(body);
            if (!body.endsWith("\n")) sb.append("\n");
            sb.append("```\n\n");

            funcNum++;
        }

        return sb.toString();
    }

    private String generatePostgresFunctionsBlock() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n## 15. ТЕЛА ФУНКЦИЙ И ПРОЦЕДУР ИЗ POSTGRESQL 🐘\n\n");

        Map<String, String> functionBodies = context.getPostgresFunctionBodies();

        if (functionBodies == null || functionBodies.isEmpty()) {
            sb.append("Функции для анализа не найдены.\n\n");
            return sb.toString();
        }

        sb.append("Ниже представлены тела функций и процедур из PostgreSQL, ");
        sb.append("которые используются в SQL запросах форм.\n\n");

        sb.append("**Статистика:**\n");
        sb.append("- Всего уникальных функций/процедур: ").append(functionBodies.size()).append("\n\n");

        int funcNum = 1;
        for (Map.Entry<String, String> entry : functionBodies.entrySet()) {
            String funcName = entry.getKey();
            String body = entry.getValue();

            sb.append("---\n\n");
            sb.append("### Функция №").append(funcNum).append(": ").append(funcName).append("\n\n");

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


    public String generateForSingleForm(FormInfo formInfo, String outputDir) throws Exception {
        // Создаём временный контекст для одной формы
        LLMReportContext singleContext = new LLMReportContext();
        List<FormInfo> singleForm = Collections.singletonList(formInfo);
        singleContext.setAnalyzedForms(singleForm);
        singleContext.setTotalForms(1);
        singleContext.setAllSqlQueries(formInfo.getSqlQueries());
        singleContext.setTotalSqlQueries(formInfo.getSqlQueries().size());

        // Копируем тела функций
        if (formInfo.getOracleFunctionBodies() != null) {
            singleContext.setOracleFunctionBodies(new LinkedHashMap<>(formInfo.getOracleFunctionBodies()));
        }
        if (formInfo.getPostgresFunctionBodies() != null) {
            singleContext.setPostgresFunctionBodies(new LinkedHashMap<>(formInfo.getPostgresFunctionBodies()));
        }

        // ========== НОВЫЙ КОД: КОПИРУЕМ DDL ДАННЫЕ ИЗ FORMINFO ==========
        // Копируем DDL вьюх
        if (formInfo.getOracleViewDDLs() != null && !formInfo.getOracleViewDDLs().isEmpty()) {
            singleContext.setOracleViewDDL(new LinkedHashMap<>(formInfo.getOracleViewDDLs()));
            System.out.println("[LLM] Скопировано Oracle вьюх: " + formInfo.getOracleViewDDLs().size());
        }
        if (formInfo.getPostgresViewDDLs() != null && !formInfo.getPostgresViewDDLs().isEmpty()) {
            singleContext.setPostgresViewDDL(new LinkedHashMap<>(formInfo.getPostgresViewDDLs()));
            System.out.println("[LLM] Скопировано PostgreSQL вьюх: " + formInfo.getPostgresViewDDLs().size());
        }

        // Копируем DDL таблиц
        if (formInfo.getOracleTableDDLs() != null && !formInfo.getOracleTableDDLs().isEmpty()) {
            singleContext.setOracleTableDDL(new LinkedHashMap<>(formInfo.getOracleTableDDLs()));
            System.out.println("[LLM] Скопировано Oracle таблиц: " + formInfo.getOracleTableDDLs().size());
        }
        if (formInfo.getPostgresTableDDLs() != null && !formInfo.getPostgresTableDDLs().isEmpty()) {
            singleContext.setPostgresTableDDL(new LinkedHashMap<>(formInfo.getPostgresTableDDLs()));
            System.out.println("[LLM] Скопировано PostgreSQL таблиц: " + formInfo.getPostgresTableDDLs().size());
        }

        // Копируем зависимости вьюх (для отображения связи вьюха -> таблицы)
        if (formInfo.getViewDependencies() != null) {
            Map<String, Set<String>> oracleViewTables = new LinkedHashMap<>();
            Map<String, Set<String>> postgresViewTables = new LinkedHashMap<>();
            for (Map.Entry<String, ViewTableDependencies> entry : formInfo.getViewDependencies().entrySet()) {
                if (entry.getValue() != null) {
                    if (entry.getValue().getOracleTables() != null && !entry.getValue().getOracleTables().isEmpty()) {
                        oracleViewTables.put(entry.getKey(), entry.getValue().getOracleTables());
                    }
                    if (entry.getValue().getPostgresTables() != null && !entry.getValue().getPostgresTables().isEmpty()) {
                        postgresViewTables.put(entry.getKey(), entry.getValue().getPostgresTables());
                    }
                }
            }
            singleContext.setOracleViewTables(oracleViewTables);
            singleContext.setPostgresViewTables(postgresViewTables);
        }
        // ================================================================

        // Временно заменяем контекст
        LLMReportContext originalContext = this.context;
        this.context = singleContext;

        // Генерируем промпт
        String prompt = generateSingleFile();

        // Восстанавливаем контекст
        this.context = originalContext;

        // Добавляем исходный текст формы
        String formSourceCode = getFormSourceCode(formInfo.getFormPath());

        // Создаём итоговый промпт
        StringBuilder finalPrompt = new StringBuilder();
        finalPrompt.append(prompt);
        finalPrompt.append("\n\n---\n\n## ИСХОДНЫЙ ТЕКСТ ФОРМЫ\n\n");
        finalPrompt.append("Ниже представлен исходный XML код анализируемой формы:\n\n");
        finalPrompt.append(formSourceCode);
        finalPrompt.append("\n\n");

        // Сохраняем файл
        String safeName = getSafeFileNameForMD(formInfo.getFormPath());
        Path mdSubDir = Paths.get(outputDir, "MD_reports");
        if (!Files.exists(mdSubDir)) {
            Files.createDirectories(mdSubDir);
        }
        Path mdFilePath = mdSubDir.resolve(safeName);
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


    /**
     * Генерирует блок для брокера с правильным форматированием
     */
    /**
     * Генерирует блок для брокера с правильным форматированием
     */
    private void generateBrokerBlock(StringBuilder sb, RouterInfo router, String unit, String action,
                                     String functionName, String oracleContent,
                                     List<Map<String, String>> queryResults) {
        sb.append("**Тип:** Брокер (Action с unit/action)\n\n");

        // Исходный брокер
        sb.append("#### Исходный брокер (converted=false):\n\n");
        sb.append("```xml\n");
        sb.append("<component cmptype=\"").append(router.getParentType().getDisplayName())
                .append("\" name=\"").append(router.getName()).append("\"");
        if (unit != null) {
            sb.append(" unit=\"").append(unit).append("\"");
        }
        if (action != null && !action.isEmpty()) {
            sb.append(" action=\"").append(action).append("\"");
        }
        if (functionName != null) {
            sb.append(" action=\"").append(functionName).append("\"");
        }
        sb.append(">\n");

        // Выводим переменные с правильным форматированием
        for (RouterVariable var : router.getVariables()) {
            sb.append("    <component cmptype=\"ActionVar\"");
            sb.append(" name=\"").append(var.getName()).append("\"");
            if (var.getSrc() != null) sb.append(" src=\"").append(var.getSrc()).append("\"");
            if (var.getSrcType() != null) sb.append(" srctype=\"").append(var.getSrcType()).append("\"");

            boolean hasPut = var.getPut() != null && !var.getPut().isEmpty();
            boolean hasGet = var.getGet() != null && !var.getGet().isEmpty();

            if (hasGet && !hasPut) {
                // get без put - не выводим
            } else if (hasGet) {
                sb.append(" get=\"").append(var.getGet()).append("\"");
            }

            if (hasPut) {
                sb.append(" put=\"").append(var.getPut()).append("\"");
            }
            if (var.getLen() != null && !var.getLen().isEmpty()) {
                sb.append(" len=\"").append(var.getLen()).append("\"");
            }
            sb.append("/>\n");
        }
        sb.append("</component>\n");
        sb.append("```\n\n");

        // Запрос к D_UNITBPS
        sb.append("#### Делаем запрос для получения ожидаемой функции ");
        if (unit != null) {
            sb.append("unit=\"").append(unit).append("\"");
            if (action != null && !action.isEmpty()) {
                sb.append(" action=\"").append(action).append("\"");
            }
        } else if (functionName != null) {
            sb.append("action=\"").append(functionName).append("\"");
        }
        sb.append("\n\n");

        sb.append("**Запрос:**\n\n");
        sb.append("```sql\n");
        sb.append("SELECT t.unitbpcode AS \"unit\",\n");
        sb.append("       t.standard_action AS \"action\",\n");
        sb.append("       t.execproc AS \"CALL_PROCEDURE\"\n");
        sb.append("FROM D_UNITBPS t\n");
        sb.append("WHERE 1=1\n");
        if (unit != null) {
            sb.append("  AND t.unitbpcode = '").append(unit).append("' -- unit\n");
        }
        if (action != null && !action.isEmpty()) {
            sb.append("  AND t.standard_action = '").append(action).append("' -- action\n");
        }
        sb.append("```\n\n");

        // Результат запроса
        sb.append("**Результат запроса:**\n\n");
        if (!queryResults.isEmpty()) {
            sb.append("```\n");
            sb.append(String.format("%-20s | %-10s | %-25s", "unit", "action", "CALL_PROCEDURE"));
            sb.append("\n");
            sb.append(String.format("%-20s-+-%-10s-+-%-25s", "-".repeat(20), "-".repeat(10), "-".repeat(25)));
            sb.append("\n");
            for (Map<String, String> row : queryResults) {
                String unitVal = row.getOrDefault("unit", "");
                String actionVal = row.getOrDefault("action", "");
                String callProc = row.getOrDefault("call_procedure", "");
                sb.append(String.format("%-20s | %-10s | %-25s",
                        unitVal.length() > 20 ? unitVal.substring(0, 17) + "..." : unitVal,
                        actionVal.length() > 10 ? actionVal.substring(0, 7) + "..." : actionVal,
                        callProc.length() > 25 ? callProc.substring(0, 22) + "..." : callProc));
                sb.append("\n");
            }
            sb.append("```\n\n");
        } else {
            sb.append("```\n(нет данных - функция не найдена в D_UNITBPS)\n```\n\n");
        }

        // Результат конвертации с правильным форматированием
        sb.append("#### Результат конвертации (converted=true) с обязательным сохранением стиля написания\n");
        sb.append("и указанием комментариев к каждому атрибуту (отступы тоже надо сохранить по одной линии по вертикали):\n\n");

        sb.append("```xml\n");
        sb.append("<component cmptype=\"").append(router.getParentType().getDisplayName())
                .append("\" name=\"").append(router.getName()).append("\">\n");

        // Oracle роутер
        sb.append("    <cmpActionRouter");
        if (unit != null) {
            sb.append(" unit=\"").append(unit).append("\"");
        }
        if (action != null && !action.isEmpty()) {
            sb.append(" action=\"").append(action).append("\"");
        }
        sb.append(" condition=\"TYPE_DATABASE=ORACLE\"/>\n");

        // PostgreSQL роутер с правильным форматированием
        sb.append("    <cmpActionRouter condition=\"TYPE_DATABASE=POSTGRE&amp;&amp;MODE_DATABASE=tmis\">\n");
        sb.append("        <![CDATA[\n");
        sb.append("        begin\n");
        sb.append("          call ");

        // Определяем имя вызываемой процедуры
        String callProcedure = null;
        if (!queryResults.isEmpty()) {
            callProcedure = queryResults.get(0).get("call_procedure");
        }
        if (callProcedure == null || callProcedure.isEmpty()) {
            if (functionName != null) {
                callProcedure = functionName;
            } else if (unit != null) {
                callProcedure = unit;
                if (action != null && !action.isEmpty()) {
                    callProcedure += "." + action;
                }
            }
        }

        if (callProcedure != null) {
            sb.append(callProcedure);
        }
        sb.append("(");

        // Форматируем параметры с правильным выравниванием
        List<RouterVariable> vars = router.getVariables();

        // Находим максимальную длину имени параметра для выравнивания
        int maxParamNameLength = 0;
        for (RouterVariable var : vars) {
            int len = var.getName().length();
            if (len > maxParamNameLength) maxParamNameLength = len;
        }

        // Вычисляем позицию для комментариев (максимальная длина + 20 пробелов)
        int commentPosition = maxParamNameLength + 20;

        for (int i = 0; i < vars.size(); i++) {
            RouterVariable var = vars.get(i);
            String paramName = var.getName();
            String src = var.getSrc() != null ? var.getSrc() : "параметр";

            if (i == 0) {
                // Первый параметр - на той же строке, что и открывающая скобка
                sb.append(paramName);
            } else {
                // Последующие параметры - с новой строки с отступом
                sb.append("\n                                ").append(paramName);
            }

            // Добиваем пробелами до максимальной длины
            int spaces = maxParamNameLength - paramName.length();
            if (spaces > 0) sb.append(" ".repeat(spaces));

            sb.append(" => :").append(paramName);

            // Добавляем запятую для всех кроме последнего
            if (i < vars.size() - 1) {
                sb.append(",");
            }

            // Выравнивание комментариев по вертикали
            int currentPos = maxParamNameLength + (i == 0 ? 20 : 32);
            int commentSpaces = commentPosition - currentPos;
            if (commentSpaces > 0) sb.append(" ".repeat(commentSpaces));

            sb.append(" -- ").append(src);
        }

        sb.append("\n          );\n");
        sb.append("        end;\n");
        sb.append("        ]]>\n");
        sb.append("    </cmpActionRouter>\n");

        // Переменные с выравниванием атрибутов по вертикали
        // Находим максимальную длину для каждого атрибута
        int maxNameLen = 0;
        int maxSrcLen = 0;
        int maxSrcTypeLen = 0;
        int maxPutLen = 0;

        for (RouterVariable var : vars) {
            maxNameLen = Math.max(maxNameLen, var.getName().length());
            maxSrcLen = Math.max(maxSrcLen, var.getSrc() != null ? var.getSrc().length() : 0);
            maxSrcTypeLen = Math.max(maxSrcTypeLen, var.getSrcType() != null ? var.getSrcType().length() : 0);
            String put = (var.getPut() != null && !var.getPut().isEmpty()) ? var.getPut() : "";
            maxPutLen = Math.max(maxPutLen, put.length());
        }

        for (RouterVariable var : vars) {
            sb.append("    <cmpActionVar");

            // name с выравниванием
            sb.append(" name=\"").append(var.getName()).append("\"");
            int nameSpaces = maxNameLen - var.getName().length();
            if (nameSpaces > 0) sb.append(" ".repeat(nameSpaces));

            // src с выравниванием
            if (var.getSrc() != null) {
                sb.append(" src=\"").append(var.getSrc()).append("\"");
                int srcSpaces = maxSrcLen - var.getSrc().length();
                if (srcSpaces > 0) sb.append(" ".repeat(srcSpaces));
            } else {
                sb.append(" src=\"\"");
                sb.append(" ".repeat(maxSrcLen));
            }

            // srctype с выравниванием
            if (var.getSrcType() != null) {
                sb.append(" srctype=\"").append(var.getSrcType()).append("\"");
                int srcTypeSpaces = maxSrcTypeLen - var.getSrcType().length();
                if (srcTypeSpaces > 0) sb.append(" ".repeat(srcTypeSpaces));
            } else {
                sb.append(" srctype=\"\"");
                sb.append(" ".repeat(maxSrcTypeLen));
            }

            boolean hasPut = var.getPut() != null && !var.getPut().isEmpty();
            boolean hasGet = var.getGet() != null && !var.getGet().isEmpty();

            if (hasGet && !hasPut) {
                // get без put - не выводим
            } else if (hasGet) {
                sb.append(" get=\"").append(var.getGet()).append("\"");
            }

            if (hasPut) {
                sb.append(" put=\"").append(var.getPut()).append("\"");
                int putSpaces = maxPutLen - var.getPut().length();
                if (putSpaces > 0) sb.append(" ".repeat(putSpaces));
            }

            if (var.getLen() != null && !var.getLen().isEmpty()) {
                sb.append(" len=\"").append(var.getLen()).append("\"");
            }
            sb.append("/>\n");
        }

        sb.append("</component>\n");
        sb.append("```\n\n");
    }

    /**
     * Форматирует переменные согласно правилам №30 и №31
     */
    private void formatActionVar(StringBuilder sb, RouterVariable var) {
        sb.append("    <cmpActionVar");
        sb.append(" name=\"").append(var.getName()).append("\"");
        if (var.getSrc() != null) sb.append(" src=\"").append(var.getSrc()).append("\"");
        if (var.getSrcType() != null) sb.append(" srctype=\"").append(var.getSrcType()).append("\"");

        // Правило №31: удаляем get без put
        boolean hasPut = var.getPut() != null && !var.getPut().isEmpty();
        boolean hasGet = var.getGet() != null && !var.getGet().isEmpty();

        if (hasGet && !hasPut) {
            // не выводим get
        } else if (hasGet) {
            sb.append(" get=\"").append(var.getGet()).append("\"");
        }

        // Правило №30: put и len в конце
        if (hasPut) {
            sb.append(" put=\"").append(var.getPut()).append("\"");
        }
        if (var.getLen() != null && !var.getLen().isEmpty()) {
            sb.append(" len=\"").append(var.getLen()).append("\"");
        }
        sb.append("/>\n");
    }

    /**
     * Форматирует параметры вызова процедуры с выравниванием по вертикали
     */
    private void formatCallParameters(StringBuilder sb, List<RouterVariable> vars, String indent) {
        if (vars == null || vars.isEmpty()) return;

        // Находим максимальную длину имени параметра
        int maxNameLen = 0;
        for (RouterVariable var : vars) {
            int len = var.getName().length();
            if (len > maxNameLen) maxNameLen = len;
        }

        for (int i = 0; i < vars.size(); i++) {
            RouterVariable var = vars.get(i);
            String paramName = var.getName();
            String src = var.getSrc() != null ? var.getSrc() : "параметр";

            // Выравнивание по вертикали
            sb.append(indent).append(paramName);

            // Добиваем пробелами до максимальной длины
            int spaces = maxNameLen - paramName.length();
            if (spaces > 0) sb.append(" ".repeat(spaces));

            sb.append(" => :").append(paramName);
            if (i < vars.size() - 1) {
                sb.append(",");
            }

            // Комментарий к параметру
            sb.append("  -- ").append(src);
            sb.append("\n");
        }
    }


    /**
     * Загружает тела функций Oracle из уже собранных данных в FormInfo
     * (никаких запросов к БД!)
     */
    private void loadOracleFunctions() {
        Map<String, String> allBodies = new LinkedHashMap<>();

        for (FormInfo form : context.getAnalyzedForms()) {
            // ПРЯМОЕ КОПИРОВАНИЕ из FormInfo
            if (form.getOracleFunctionBodies() != null) {
                allBodies.putAll(form.getOracleFunctionBodies());
                System.out.println("[LLM] Взято из FormInfo Oracle функций: " + form.getOracleFunctionBodies().size());
            }
        }

        context.setOracleFunctionBodies(allBodies);
        System.out.println("[Oracle] Загружено тел функций в контекст: " + allBodies.size());
    }

    /**
     * Загружает тела функций PostgreSQL из уже собранных данных в FormInfo
     * (никаких запросов к БД!)
     */
    private void loadPostgresFunctions() {
        Map<String, String> allBodies = new LinkedHashMap<>();

        for (FormInfo form : context.getAnalyzedForms()) {
            // 1. Данные уже загружены в FormInfo при анализе
            if (form.getPostgresFunctionBodies() != null) {
                allBodies.putAll(form.getPostgresFunctionBodies());
            }

            // 2. Собираем функции из роутеров
            collectPostgresFunctionsFromRouters(form, allBodies);

            // 3. Функции из брокеров
            for (BrokerInfo broker : form.getBrokers()) {
                if (broker.getExecProc() != null && !broker.getExecProc().isEmpty()) {
                    String key = broker.getExecProc().toLowerCase();
                    if (!allBodies.containsKey(key)) {
                        String body = DatabaseCacheManager.getPostgresFunctionBody(key, () -> null);
                        if (body != null && !body.isEmpty()) {
                            allBodies.put(key, body);
                            System.out.println("[LLM] Добавлено тело PostgreSQL функции из брокера: " + key);
                        }
                    }
                }
            }
        }

        context.setPostgresFunctionBodies(allBodies);
        System.out.println("[PostgreSQL] Загружено тел функций из контекста: " + allBodies.size());
    }

    /**
     * Собирает Oracle функции из RouterInfo и добавляет в карту тел
     */
    private void collectFunctionsFromRouters(FormInfo form, Map<String, String> bodies) {
        Pattern packagePattern = Pattern.compile(
                "\\b(D_PKG_[A-Z0-9_]+\\.[A-Z0-9_]+)\\b",
                Pattern.CASE_INSENSITIVE
        );

        // ActionRouters
        for (RouterInfo router : form.getActionRouters()) {
            for (RouterItem item : router.getRouters()) {
                if (item.getSqlContent() != null) {
                    Matcher m = packagePattern.matcher(item.getSqlContent());
                    while (m.find()) {
                        String func = m.group(1);
                        // Исключаем константы и опции
                        if (!func.toUpperCase().contains("D_PKG_CONSTANTS") &&
                                !func.toUpperCase().contains("D_PKG_OPTIONS") &&
                                !func.toUpperCase().contains("D_PKG_OPTION_SPECS")) {

                            if (!bodies.containsKey(func)) {
                                String body = DatabaseCacheManager.getOracleFunctionBody(func, () -> null);
                                if (body != null && !body.isEmpty()) {
                                    bodies.put(func, body);
                                    System.out.println("[LLM] Добавлено тело функции из роутера: " + func);
                                }
                            }
                        }
                    }
                }
            }

            // SubRouters
            for (SubRouterInfo sub : router.getSubRouters()) {
                for (RouterItem item : sub.getRouters()) {
                    if (item.getSqlContent() != null) {
                        Matcher m = packagePattern.matcher(item.getSqlContent());
                        while (m.find()) {
                            String func = m.group(1);
                            if (!bodies.containsKey(func)) {
                                String body = DatabaseCacheManager.getOracleFunctionBody(func, () -> null);
                                if (body != null && !body.isEmpty()) {
                                    bodies.put(func, body);
                                }
                            }
                        }
                    }
                }
            }
        }

        // DataSetRouters
        for (RouterInfo router : form.getDataSetRouters()) {
            for (RouterItem item : router.getRouters()) {
                if (item.getSqlContent() != null) {
                    Matcher m = packagePattern.matcher(item.getSqlContent());
                    while (m.find()) {
                        String func = m.group(1);
                        if (!bodies.containsKey(func)) {
                            String body = DatabaseCacheManager.getOracleFunctionBody(func, () -> null);
                            if (body != null && !body.isEmpty()) {
                                bodies.put(func, body);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Собирает PostgreSQL функции из RouterInfo
     */
    private void collectPostgresFunctionsFromRouters(FormInfo form, Map<String, String> bodies) {
        Pattern functionPattern = Pattern.compile(
                "\\b([a-z][a-z0-9_]*\\.[a-z][a-z0-9_]*)\\b|\\b([a-z][a-z0-9_]*)\\s*\\(",
                Pattern.CASE_INSENSITIVE
        );

        Set<String> foundFunctions = new LinkedHashSet<>();

        // ActionRouters
        for (RouterInfo router : form.getActionRouters()) {
            for (RouterItem item : router.getRouters()) {
                if (item.getSqlContent() != null) {
                    String sql = item.getSqlContent().toLowerCase();
                    Matcher m = functionPattern.matcher(sql);
                    while (m.find()) {
                        String func = m.group(1) != null ? m.group(1) : m.group(2);
                        if (func != null && !func.startsWith("d_pkg_constants") &&
                                !func.startsWith("d_pkg_options") && func.length() > 3) {
                            foundFunctions.add(func);
                        }
                    }
                }
            }
        }

        // DataSetRouters
        for (RouterInfo router : form.getDataSetRouters()) {
            for (RouterItem item : router.getRouters()) {
                if (item.getSqlContent() != null) {
                    String sql = item.getSqlContent().toLowerCase();
                    Matcher m = functionPattern.matcher(sql);
                    while (m.find()) {
                        String func = m.group(1) != null ? m.group(1) : m.group(2);
                        if (func != null && !func.startsWith("d_pkg_constants") &&
                                !func.startsWith("d_pkg_options") && func.length() > 3) {
                            foundFunctions.add(func);
                        }
                    }
                }
            }
        }

        for (String func : foundFunctions) {
            if (!bodies.containsKey(func)) {
                String body = DatabaseCacheManager.getPostgresFunctionBody(func, () -> null);
                if (body != null && !body.isEmpty()) {
                    bodies.put(func, body);
                    System.out.println("[LLM] Добавлено тело PostgreSQL функции из роутера: " + func);
                }
            }
        }
    }
    /**
     * Загружает DDL таблиц, используемых во вьюхах Oracle
     */
    private void loadOracleTableDDL() {
        Map<String, Set<String>> viewTables = context.getOracleViewTables();
        if (viewTables == null || viewTables.isEmpty()) {
            System.out.println("[Oracle] Нет вьюх для получения таблиц");
            context.setOracleTableDDL(Collections.emptyMap());
            return;
        }

        // Собираем все уникальные таблицы из всех вьюх
        Set<String> allTables = new LinkedHashSet<>();
        for (Set<String> tables : viewTables.values()) {
            allTables.addAll(tables);
        }

        if (allTables.isEmpty()) {
            System.out.println("[Oracle] Нет таблиц для загрузки");
            context.setOracleTableDDL(Collections.emptyMap());
            return;
        }

        System.out.println("[Oracle] Загрузка DDL таблиц (" + allTables.size() + " шт.)...");

        Map<String, String> tableDDL = new LinkedHashMap<>();
        int count = 0;
        for (String tableName : allTables) {
            if (stopCondition.getAsBoolean()) break;
            count++;
            System.out.println("[Oracle]   [" + count + "/" + allTables.size() + "] Загрузка таблицы: " + tableName);

            String ddl = DatabaseCacheManager.getOracleTableDDL(tableName, () ->
                    oracleService.getTableDDL(tableName)
            );

            if (ddl != null && !ddl.isEmpty()) {
                tableDDL.put(tableName, ddl);
                System.out.println("[Oracle]      OK (" + ddl.length() + " симв.)");
            } else {
                System.out.println("[Oracle]      НЕ НАЙДЕНА");
            }
        }

        context.setOracleTableDDL(tableDDL);
        System.out.println("[Oracle] Загружено DDL таблиц: " + tableDDL.size());
    }
    /**
     * Загружает DDL таблиц, используемых во вьюхах PostgreSQL
     */
    private void loadPostgresTableDDL() {
        Map<String, Set<String>> viewTables = context.getPostgresViewTables();
        if (viewTables == null || viewTables.isEmpty()) {
            System.out.println("[PostgreSQL] Нет вьюх для получения таблиц");
            context.setPostgresTableDDL(Collections.emptyMap());
            return;
        }

        // Собираем все уникальные таблицы из всех вьюх
        Set<String> allTables = new LinkedHashSet<>();
        for (Set<String> tables : viewTables.values()) {
            allTables.addAll(tables);
        }

        if (allTables.isEmpty()) {
            System.out.println("[PostgreSQL] Нет таблиц для загрузки");
            context.setPostgresTableDDL(Collections.emptyMap());
            return;
        }

        System.out.println("[PostgreSQL] Загрузка DDL таблиц (" + allTables.size() + " шт.)...");

        Map<String, String> tableDDL = new LinkedHashMap<>();
        int count = 0;
        for (String tableName : allTables) {
            if (stopCondition.getAsBoolean()) break;
            count++;
            System.out.println("[PostgreSQL]   [" + count + "/" + allTables.size() + "] Загрузка таблицы: " + tableName);

            String ddl = DatabaseCacheManager.getPostgresTableDDL(tableName, () ->
                    postgresService.getTableDDL(tableName)
            );

            if (ddl != null && !ddl.isEmpty()) {
                tableDDL.put(tableName, ddl);
                System.out.println("[PostgreSQL]      OK (" + ddl.length() + " симв.)");
            } else {
                System.out.println("[PostgreSQL]      НЕ НАЙДЕНА");
            }
        }

        context.setPostgresTableDDL(tableDDL);
        System.out.println("[PostgreSQL] Загружено DDL таблиц: " + tableDDL.size());
    }
}