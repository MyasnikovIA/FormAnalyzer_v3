// core/service/RouterGeneratorService.java

package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.core.model.*;
import ru.tmis.analyzer.core.db.OracleService;
import ru.tmis.analyzer.config.SettingsModel;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис для динамического создания Router компонентов
 * на основе существующих sqlQueries и брокеров
 */
public class RouterGeneratorService {

    private final SettingsModel settings;
    private final OracleService oracleService;

    // Паттерн для извлечения параметров из SQL
    private static final Pattern PARAM_PATTERN = Pattern.compile(":([a-zA-Z][a-zA-Z0-9_]*)");

    public RouterGeneratorService(SettingsModel settings) {
        this.settings = settings;
        this.oracleService = new OracleService(
                settings.getOracleUrl(),
                settings.getOracleUser(),
                settings.getOraclePassword()
        );
    }

    /**
     * Генерирует Router компоненты для всех sqlQueries (без Router)
     * @param formInfo информация о форме
     * @return список сгенерированных RouterInfo
     */
    public List<RouterInfo> generateRoutersFromSqlQueries(FormInfo formInfo) {
        List<RouterInfo> generatedRouters = new ArrayList<>();

        for (SqlInfo sql : formInfo.getSqlQueries()) {
            if (sql.getComponentType() == null) continue;

            RouterInfo router = createRouterFromSql(sql, formInfo);
            if (router != null) {
                generatedRouters.add(router);
            }
        }

        return generatedRouters;
    }

    /**
     * Генерирует Router компоненты для всех брокеров
     * @param formInfo информация о форме
     * @return список сгенерированных RouterInfo
     */
    public List<RouterInfo> generateRoutersFromBrokers(FormInfo formInfo) {
        List<RouterInfo> generatedRouters = new ArrayList<>();

        for (BrokerInfo broker : formInfo.getBrokers()) {
            RouterInfo router = createRouterFromBroker(broker, formInfo);
            if (router != null) {
                generatedRouters.add(router);
            }
        }

        return generatedRouters;
    }

    /**
     * Создаёт RouterInfo из SqlInfo
     */
    private RouterInfo createRouterFromSql(SqlInfo sql, FormInfo formInfo) {
        // Определяем тип родителя
        RouterInfo.ParentType parentType;
        RouterInfo.RouterType routerType;

        if (sql.getComponentType().contains("DataSet")) {
            parentType = RouterInfo.ParentType.DATASET;
            routerType = RouterInfo.RouterType.DATASET_ROUTER;
        } else if (sql.getComponentType().contains("Action")) {
            parentType = RouterInfo.ParentType.ACTION;
            routerType = RouterInfo.RouterType.ACTION_ROUTER;
        } else {
            return null;
        }

        RouterInfo router = new RouterInfo(sql.getComponentName(), parentType, routerType);
        router.setFormStyle(formInfo.getFormStyle());
        router.setConverted(false);  // <-- Важно: converted = false

        // 1. Oracle роутер (оригинальный SQL)
        RouterItem oracleRouter = new RouterItem(
                "TYPE_DATABASE=ORACLE",
                sql.getSqlContent(),
                0,
                null,
                null
        );
        router.addRouter(oracleRouter);

        // 2. PostgreSQL роутер (заглушка с комментарием)
        String postgresComment = generatePostgresStubComment(sql);
        RouterItem postgresRouter = new RouterItem(
                "TYPE_DATABASE=POSTGRE",
                postgresComment,
                1,
                null,
                null
        );
        router.addRouter(postgresRouter);

        // 3. Копируем переменные из SqlInfo
        for (RouterVariable var : sql.getVariables()) {
            router.addVariable(var);
        }

        // Также извлекаем переменные из SQL (bind-параметры)
        extractVariablesFromSql(sql.getSqlContent(), router);

        return router;
    }

    /**
     * Создаёт RouterInfo из BrokerInfo
     */
    private RouterInfo createRouterFromBroker(BrokerInfo broker, FormInfo formInfo) {
        RouterInfo router = new RouterInfo(
                broker.getComponentName() != null ? broker.getComponentName() : "unnamed",
                RouterInfo.ParentType.ACTION,
                RouterInfo.RouterType.ACTION_ROUTER
        );
        router.setFormStyle(formInfo.getFormStyle());
        router.setConverted(false);  // <-- Важно: converted = false

        // 1. Oracle роутер (оригинальный вызов)
        String oracleCondition = "TYPE_DATABASE=ORACLE";
        String oracleSql = generateOracleCallSql(broker);
        RouterItem oracleRouter = new RouterItem(
                oracleCondition,
                oracleSql,
                0,
                broker.getUnit(),
                broker.getAction()
        );
        router.addRouter(oracleRouter);

        // 2. PostgreSQL роутер (сгенерированный вызов функции)
        String postgresCondition = "TYPE_DATABASE=POSTGRE && MODE_DATABASE=tmis";
        String postgresSql = generatePostgresCallSql(broker);
        RouterItem postgresRouter = new RouterItem(
                postgresCondition,
                postgresSql,
                1,
                broker.getUnit(),
                broker.getAction()
        );
        router.addRouter(postgresRouter);

        // 3. Копируем переменные из брокера
        for (RouterVariable var : broker.getVariables()) {
            router.addVariable(var);
        }

        return router;
    }

    /**
     * Генерирует SQL для Oracle вызова брокера
     */
    private String generateOracleCallSql(BrokerInfo broker) {
        StringBuilder sb = new StringBuilder();

        if (broker.getType() == BrokerInfo.BrokerType.TYPE1_UNIT_ACTION) {
            // Для unit+action используем execProc, если найден
            if (broker.getExecProc() != null && !broker.getExecProc().isEmpty()) {
                sb.append("BEGIN\n");
                sb.append("  ").append(broker.getExecProc()).append("(");
                appendParameters(sb, broker.getVariables());
                sb.append(");\n");
                sb.append("END;");
            } else {
                sb.append("-- execProc не найден для unit=").append(broker.getUnit())
                        .append(", action=").append(broker.getAction());
            }
        } else {
            // Прямое указание функции
            sb.append("BEGIN\n");
            sb.append("  ").append(broker.getFunctionName()).append("(");
            appendParameters(sb, broker.getVariables());
            sb.append(");\n");
            sb.append("END;");
        }

        return sb.toString();
    }

    /**
     * Генерирует SQL для PostgreSQL вызова брокера
     */
    private String generatePostgresCallSql(BrokerInfo broker) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN\n");
        sb.append("  -- ⚠️ ВНИМАНИЕ: Требуется конвертация Oracle PL/SQL в PostgreSQL\n");
        sb.append("  -- \n");
        sb.append("  -- Исходный вызов:\n");

        if (broker.getType() == BrokerInfo.BrokerType.TYPE1_UNIT_ACTION) {
            sb.append("  --   unit=").append(broker.getUnit())
                    .append(", action=").append(broker.getAction()).append("\n");
            if (broker.getExecProc() != null) {
                sb.append("  --   execProc=").append(broker.getExecProc()).append("\n");
            }
        } else {
            sb.append("  --   function=").append(broker.getFunctionName()).append("\n");
        }

        sb.append("  -- \n");
        sb.append("  -- Ожидаемый PostgreSQL синтаксис:\n");
        sb.append("  --   PERFORM ");

        if (broker.getType() == BrokerInfo.BrokerType.TYPE1_UNIT_ACTION && broker.getExecProc() != null) {
            sb.append(broker.getExecProc());
        } else if (broker.getType() == BrokerInfo.BrokerType.TYPE2_DIRECT_FUNCTION) {
            sb.append(broker.getFunctionName());
        } else {
            sb.append("function_name");
        }

        sb.append("(\n");

        // Добавляем параметры с комментариями
        List<RouterVariable> vars = broker.getVariables();
        for (int i = 0; i < vars.size(); i++) {
            RouterVariable var = vars.get(i);
            sb.append("  --     ").append(var.getName()).append(" => :").append(var.getName());
            if (var.getSrc() != null) {
                sb.append("  -- источник: ").append(var.getSrc());
            }
            if (i < vars.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("  --   );\n");
        sb.append("  -- \n");
        sb.append("  -- TODO: Заменить на корректный PostgreSQL вызов\n");
        sb.append("  RAISE NOTICE 'Требуется конвертация брокера %', '")
                .append(broker.getDisplayString()).append("';\n");
        sb.append("END;");

        return sb.toString();
    }

    /**
     * Генерирует заглушку для PostgreSQL для SQL запроса
     */
    private String generatePostgresStubComment(SqlInfo sql) {
        StringBuilder sb = new StringBuilder();
        sb.append("-- ⚠️ ВНИМАНИЕ: Требуется конвертация Oracle SQL в PostgreSQL\n");
        sb.append("-- \n");
        sb.append("-- Исходный Oracle SQL:\n");
        sb.append("-- ").append(sql.getCleanSql() != null ? sql.getCleanSql().replace("\n", "\n-- ") : "SQL не найден");
        sb.append("\n-- \n");
        sb.append("-- Ожидаемый PostgreSQL синтаксис:\n");
        sb.append("--   SELECT ... FROM ... WHERE ...\n");
        sb.append("-- \n");
        sb.append("-- TODO: Сконвертировать Oracle SQL в PostgreSQL синтаксис\n");
        sb.append("SELECT 'Требуется конвертация SQL запроса' AS message;\n");

        return sb.toString();
    }

    /**
     * Добавляет параметры в строку вызова
     */
    private void appendParameters(StringBuilder sb, List<RouterVariable> variables) {
        for (int i = 0; i < variables.size(); i++) {
            RouterVariable var = variables.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(var.getName()).append(" => :").append(var.getName());
        }
    }

    /**
     * Извлекает переменные из SQL (bind-параметры)
     */
    private void extractVariablesFromSql(String sqlContent, RouterInfo router) {
        if (sqlContent == null) return;

        Matcher matcher = PARAM_PATTERN.matcher(sqlContent);
        Set<String> paramNames = new LinkedHashSet<>();

        while (matcher.find()) {
            paramNames.add(matcher.group(1));
        }

        for (String paramName : paramNames) {
            // Проверяем, нет ли уже такой переменной
            boolean exists = router.getVariables().stream()
                    .anyMatch(v -> v.getName().equals(paramName));

            if (!exists) {
                RouterVariable var = new RouterVariable.Builder(paramName)
                        .setSrc(paramName)
                        .setSrcType("var")
                        .build();
                router.addVariable(var);
            }
        }
    }

    /**
     * Получает комментарий к параметру функции из Oracle
     */
    private String getParameterComment(String packageName, String functionName, String paramName) {
        // Этот метод может использовать OracleService для получения информации о параметрах
        // Пока возвращаем заглушку
        return "-- комментарий к параметру";
    }
}