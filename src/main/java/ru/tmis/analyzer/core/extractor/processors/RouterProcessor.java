// core/extractor/processors/RouterProcessor.java
package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.extractor.IXmlProcessor;
import ru.tmis.analyzer.core.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Извлечение Router компонентов (ActionRouter, DataSetRouter)
 * Поддерживает D3 и M2 синтаксис
 */
public class RouterProcessor implements IXmlProcessor {

    private static final Pattern CDATA_PATTERN = Pattern.compile("<!\\[CDATA\\[(.*?)\\]\\]>", Pattern.DOTALL);
    private static final Pattern CONDITION_PATTERN = Pattern.compile("condition\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    private static final Pattern UNIT_PATTERN = Pattern.compile("unit\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACTION_PATTERN = Pattern.compile("action\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAME_PATTERN = Pattern.compile("name\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);

    @Override
    public String getName() {
        return "RouterProcessor";
    }

    @Override
    public int getPriority() {
        return 15; // Высокий приоритет, чтобы обработать до других процессоров
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        // Получаем стиль формы из FormInfo
        FormInfo.FormStyle formStyle = formInfo.getFormStyle();

        // 1. Обработка Action компонентов (D3 синтаксис)
        if (formStyle.isD3() || formStyle == FormInfo.FormStyle.UNKNOWN) {
            processActions(doc, formInfo, "cmpAction", RouterInfo.RouterType.ACTION_ROUTER, false, formStyle);
        }

        // 2. Обработка Action компонентов (M2 синтаксис)
        if (formStyle.isM2() || formStyle == FormInfo.FormStyle.UNKNOWN) {
            processActions(doc, formInfo, "component[cmptype=Action]", RouterInfo.RouterType.ACTION_ROUTER, false, formStyle);
        }

        // 3. Обработка DataSet компонентов (D3 синтаксис)
        if (formStyle.isD3() || formStyle == FormInfo.FormStyle.UNKNOWN) {
            processDataSets(doc, formInfo, "cmpDataSet", RouterInfo.RouterType.DATASET_ROUTER, formStyle);
        }

        // 4. Обработка DataSet компонентов (M2 синтаксис)
        if (formStyle.isM2() || formStyle == FormInfo.FormStyle.UNKNOWN) {
            processDataSets(doc, formInfo, "component[cmptype=DataSet]", RouterInfo.RouterType.DATASET_ROUTER, formStyle);
        }
    }

    /**
     * Обработка Action компонентов
     */
    private void processActions(Document doc, FormInfo formInfo, String selector,
                                RouterInfo.RouterType routerType, boolean isBeforeAction,
                                FormInfo.FormStyle formStyle) {
        Elements actions = doc.select(selector);

        for (Element action : actions) {
            String actionName = action.attr("name");
            if (actionName == null || actionName.isEmpty()) continue;

            // Обработка BeforeAction внутри Action
            Elements beforeActions = action.select("cmpBeforeAction, component[cmptype=BeforeAction]");
            for (Element beforeAction : beforeActions) {
                String beforeName = beforeAction.attr("name");
                if (beforeName == null || beforeName.isEmpty()) beforeName = actionName + "_BEFORE";

                RouterInfo beforeRouter = extractRouterInfo(beforeAction, beforeName,
                        RouterInfo.ParentType.BEFORE_ACTION, routerType, formStyle);
                if (beforeRouter != null && !beforeRouter.getRouters().isEmpty()) {
                    formInfo.addActionRouter(beforeRouter);
                }
            }

            // Обработка основного Action
            RouterInfo mainRouter = extractRouterInfo(action, actionName,
                    RouterInfo.ParentType.ACTION, routerType, formStyle);
            if (mainRouter != null && !mainRouter.getRouters().isEmpty()) {
                formInfo.addActionRouter(mainRouter);
            }

            // Обработка SubAction внутри Action
            processSubActions(action, formInfo, actionName);
        }
    }

    /**
     * Обработка DataSet компонентов
     */
    private void processDataSets(Document doc, FormInfo formInfo, String selector,
                                 RouterInfo.RouterType routerType, FormInfo.FormStyle formStyle) {
        Elements dataSets = doc.select(selector);

        for (Element dataSet : dataSets) {
            String dataSetName = dataSet.attr("name");
            if (dataSetName == null || dataSetName.isEmpty()) continue;

            // Обработка BeforeSelect внутри DataSet
            Elements beforeSelects = dataSet.select("component[cmptype=BeforeSelect], cmpBeforeSelect");
            for (Element beforeSelect : beforeSelects) {
                String beforeName = beforeSelect.attr("name");
                if (beforeName == null || beforeName.isEmpty()) beforeName = dataSetName + "_BEFORE";

                RouterInfo beforeRouter = extractRouterInfo(beforeSelect, beforeName,
                        RouterInfo.ParentType.BEFORE_SELECT, routerType, formStyle);

                if (beforeRouter != null && !beforeRouter.getRouters().isEmpty()) {
                    formInfo.addDataSetRouter(beforeRouter);
                }
            }

            // Обработка основного DataSet
            RouterInfo mainRouter = extractRouterInfo(dataSet, dataSetName,
                    RouterInfo.ParentType.DATASET, routerType, formStyle);
            if (mainRouter != null && !mainRouter.getRouters().isEmpty()) {
                formInfo.addDataSetRouter(mainRouter);
            }

            // Обработка SubSelect внутри DataSet
            processSubSelects(dataSet, formInfo, dataSetName);
        }
    }

    /**
     * Извлечение информации о Router из элемента
     */
    private RouterInfo extractRouterInfo(Element element, String name,
                                         RouterInfo.ParentType parentType,
                                         RouterInfo.RouterType routerType,
                                         FormInfo.FormStyle formStyle) {
        RouterInfo routerInfo = new RouterInfo(name, parentType, routerType);
        routerInfo.setFormStyle(formStyle);

        // Определяем селектор для роутеров в зависимости от типа
        String routerSelector;
        if (routerType == RouterInfo.RouterType.ACTION_ROUTER) {
            routerSelector = "cmpActionRouter, component[cmptype=ActionRouter]";
        } else {
            routerSelector = "cmpDataSetRouter, component[cmptype=DataSetRouter]";
        }

        Elements routers = element.select(routerSelector);
        int order = 0;

        for (Element router : routers) {
            String condition = router.attr("condition");
            String unit = router.attr("unit");
            String action = router.attr("action");
            String sqlContent = extractCdataContent(router.html());

            if (sqlContent != null && !sqlContent.trim().isEmpty()) {
                RouterItem routerItem = new RouterItem(condition, sqlContent, order++, unit, action);
                routerInfo.addRouter(routerItem);
            }
        }

        // Извлекаем переменные
        extractVariables(element, routerInfo, routerType);

        return routerInfo;
    }

    /**
     * Извлечение переменных (ActionVar, DataSetVar, Variable)
     */
    private void extractVariables(Element element, RouterInfo routerInfo, RouterInfo.RouterType routerType) {
        String varSelector;
        if (routerType == RouterInfo.RouterType.ACTION_ROUTER) {
            varSelector = "cmpActionVar, component[cmptype=ActionVar]";
        } else {
            varSelector = "cmpDataSetVar, cmpVariable, component[cmptype=Variable]";
        }

        Elements variables = element.select(varSelector);

        for (Element var : variables) {
            String name = var.attr("name");
            if (name == null || name.isEmpty()) continue;

            RouterVariable variable = new RouterVariable.Builder(name)
                    .setSrc(var.attr("src"))
                    .setSrcType(var.attr("srctype"))
                    .setGet(var.attr("get"))
                    .setPut(var.attr("put"))
                    .setType(var.attr("type"))
                    .setLen(var.attr("len"))
                    .setDefaultValue(var.attr("default"))
                    .build();

            routerInfo.addVariable(variable);
        }
    }

    /**
     * Обработка SubAction внутри Action
     */
    private void processSubActions(Element parent, FormInfo formInfo, String parentName) {
        Elements subActions = parent.select("component[cmptype=SubAction], cmpSubAction");

        for (Element subAction : subActions) {
            String name = subAction.attr("name");
            if (name == null || name.isEmpty()) continue;

            SubRouterInfo.Builder builder = new SubRouterInfo.Builder(name, RouterInfo.ParentType.SUB_ACTION);

            String groupName = subAction.attr("groupname");
            if (groupName == null || groupName.isEmpty()) {
                groupName = subAction.attr("repeatername");
            }
            builder.setGroupName(groupName);
            builder.setExecon(subAction.attr("execon"));
            builder.setMode(subAction.attr("mode"));
            builder.setSavepoint("true".equalsIgnoreCase(subAction.attr("savepoint")));

            // Извлекаем роутеры внутри SubAction
            String routerSelector = "cmpActionRouter, component[cmptype=ActionRouter]";
            Elements routers = subAction.select(routerSelector);
            int order = 0;

            for (Element router : routers) {
                String condition = router.attr("condition");
                String unit = router.attr("unit");
                String action = router.attr("action");
                String sqlContent = extractCdataContent(router.html());

                if (sqlContent != null && !sqlContent.trim().isEmpty()) {
                    builder.addRouter(new RouterItem(condition, sqlContent, order++, unit, action));
                }
            }

            // Извлекаем переменные SubActionVar
            Elements variables = subAction.select("component[cmptype=SubActionVar], cmpSubActionVar");
            for (Element var : variables) {
                String varName = var.attr("name");
                if (varName == null || varName.isEmpty()) continue;

                RouterVariable variable = new RouterVariable.Builder(varName)
                        .setSrc(var.attr("src"))
                        .setSrcType(var.attr("srctype"))
                        .setGet(var.attr("get"))
                        .setPut(var.attr("put"))
                        .setType(var.attr("type"))
                        .setLen(var.attr("len"))
                        .build();
                builder.addVariable(variable);
            }

            SubRouterInfo subRouterInfo = builder.build();

            // Находим соответствующий RouterInfo и добавляем SubRouter
            for (RouterInfo ri : formInfo.getActionRouters()) {
                if (ri.getName().equals(parentName) ||
                        (ri.getParentType() == RouterInfo.ParentType.ACTION && ri.getName().equals(parentName))) {
                    ri.addSubRouter(subRouterInfo);
                    break;
                }
            }
        }
    }

    /**
     * Обработка SubSelect внутри DataSet
     */
    private void processSubSelects(Element parent, FormInfo formInfo, String parentName) {
        Elements subSelects = parent.select("component[cmptype=SubSelect], cmpSubSelect");

        for (Element subSelect : subSelects) {
            String name = subSelect.attr("name");
            if (name == null || name.isEmpty()) continue;

            SubRouterInfo.Builder builder = new SubRouterInfo.Builder(name, RouterInfo.ParentType.SUB_SELECT);

            String groupName = subSelect.attr("groupname");
            if (groupName == null || groupName.isEmpty()) {
                groupName = subSelect.attr("repeatername");
            }
            builder.setGroupName(groupName);
            builder.setExecon(subSelect.attr("execon"));
            builder.setMode(subSelect.attr("mode"));

            // Извлекаем роутеры внутри SubSelect
            String routerSelector = "cmpDataSetRouter, component[cmptype=DataSetRouter]";
            Elements routers = subSelect.select(routerSelector);
            int order = 0;

            for (Element router : routers) {
                String condition = router.attr("condition");
                String sqlContent = extractCdataContent(router.html());

                if (sqlContent != null && !sqlContent.trim().isEmpty()) {
                    builder.addRouter(new RouterItem(condition, sqlContent, order++));
                }
            }

            // Извлекаем переменные
            Elements variables = subSelect.select("cmpDataSetVar, cmpVariable, component[cmptype=Variable]");
            for (Element var : variables) {
                String varName = var.attr("name");
                if (varName == null || varName.isEmpty()) continue;

                RouterVariable variable = new RouterVariable.Builder(varName)
                        .setSrc(var.attr("src"))
                        .setSrcType(var.attr("srctype"))
                        .setGet(var.attr("get"))
                        .setPut(var.attr("put"))
                        .setType(var.attr("type"))
                        .setLen(var.attr("len"))
                        .setDefaultValue(var.attr("default"))
                        .build();
                builder.addVariable(variable);
            }

            SubRouterInfo subRouterInfo = builder.build();

            // Находим соответствующий RouterInfo и добавляем SubRouter
            for (RouterInfo ri : formInfo.getDataSetRouters()) {
                if (ri.getName().equals(parentName)) {
                    ri.addSubRouter(subRouterInfo);
                    break;
                }
            }
        }
    }

    /**
     * Извлечение содержимого CDATA секции
     */
    private String extractCdataContent(String html) {
        if (html == null) return null;
        Matcher matcher = CDATA_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}