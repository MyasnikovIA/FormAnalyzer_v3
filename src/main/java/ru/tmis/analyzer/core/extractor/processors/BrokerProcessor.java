// core/extractor/processors/BrokerProcessor.java

package ru.tmis.analyzer.core.extractor.processors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.model.BrokerInfo;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.RouterVariable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class BrokerProcessor implements IXmlProcessor {

    @Override
    public String getName() {
        return "BrokerProcessor";
    }

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public void process(Document doc, FormInfo formInfo) {
        Set<BrokerInfo> brokers = new LinkedHashSet<>();

        // ========== 1. M2 Action с unit (component cmptype="Action") ==========
        Elements m2Actions = doc.select("component[cmptype=Action][unit]");
        for (Element el : m2Actions) {
            String unit = el.attr("unit");
            String action = el.attr("action");
            String name = el.attr("name");

            if (!unit.isEmpty() && !hasSqlContent(el)) {
                BrokerInfo broker = new BrokerInfo(unit, action != null ? action : "");
                broker.setComponentName(name);
                broker.setComponentType("Action");
                broker.setSourcePath(formInfo.getFormPath());
                broker.setBaseFormPath(formInfo.getBaseFormPath());

                // Извлекаем переменные
                extractVariables(el, broker);

                brokers.add(broker);
            }
        }

        // ========== 2. M2 SubAction с unit ==========
        Elements m2SubActions = doc.select("component[cmptype=SubAction][unit]");
        for (Element el : m2SubActions) {
            String unit = el.attr("unit");
            String action = el.attr("action");
            String name = el.attr("name");

            if (!unit.isEmpty() && !hasSqlContent(el)) {
                BrokerInfo broker = new BrokerInfo(unit, action != null ? action : "");
                broker.setComponentName(name);
                broker.setComponentType("SubAction");
                broker.setSourcePath(formInfo.getFormPath());
                broker.setBaseFormPath(formInfo.getBaseFormPath());

                extractVariables(el, broker);

                brokers.add(broker);
            }
        }

        // ========== 3. D3 Action с unit (cmpAction) ==========
        Elements d3Actions = doc.select("cmpAction[unit]");
        for (Element el : d3Actions) {
            String unit = el.attr("unit");
            String action = el.attr("action");
            String name = el.attr("name");

            if (!unit.isEmpty() && !hasSqlContent(el)) {
                BrokerInfo broker = new BrokerInfo(unit, action != null ? action : "");
                broker.setComponentName(name);
                broker.setComponentType("cmpAction");
                broker.setSourcePath(formInfo.getFormPath());
                broker.setBaseFormPath(formInfo.getBaseFormPath());

                extractVariables(el, broker);

                brokers.add(broker);
            }
        }

        // ========== 4. D3 SubAction с unit (cmpSubAction) ==========
        Elements d3SubActions = doc.select("cmpSubAction[unit]");
        for (Element el : d3SubActions) {
            String unit = el.attr("unit");
            String action = el.attr("action");
            String name = el.attr("name");

            if (!unit.isEmpty() && !hasSqlContent(el)) {
                BrokerInfo broker = new BrokerInfo(unit, action != null ? action : "");
                broker.setComponentName(name);
                broker.setComponentType("cmpSubAction");
                broker.setSourcePath(formInfo.getFormPath());
                broker.setBaseFormPath(formInfo.getBaseFormPath());

                extractVariables(el, broker);

                brokers.add(broker);
            }
        }

        // ========== 5. Прямое указание функции (без unit) - M2 ==========
        Elements directM2Actions = doc.select("component[cmptype=Action][action^=D_PKG_]:not([unit]), " +
                "component[cmptype=SubAction][action^=D_PKG_]:not([unit])");
        for (Element el : directM2Actions) {
            String action = el.attr("action");
            String name = el.attr("name");

            if (!action.isEmpty() && action.startsWith("D_PKG_") && !hasSqlContent(el)) {
                BrokerInfo broker = new BrokerInfo(action);
                broker.setComponentName(name);
                broker.setComponentType(el.tagName().equals("component") ? "Action" : "SubAction");
                broker.setSourcePath(formInfo.getFormPath());
                broker.setBaseFormPath(formInfo.getBaseFormPath());

                extractVariables(el, broker);

                brokers.add(broker);
            }
        }

        // ========== 6. Прямое указание функции (без unit) - D3 ==========
        Elements directD3Actions = doc.select("cmpAction[action^=D_PKG_]:not([unit]), " +
                "cmpSubAction[action^=D_PKG_]:not([unit])");
        for (Element el : directD3Actions) {
            String action = el.attr("action");
            String name = el.attr("name");

            if (!action.isEmpty() && action.startsWith("D_PKG_") && !hasSqlContent(el)) {
                BrokerInfo broker = new BrokerInfo(action);
                broker.setComponentName(name);
                broker.setComponentType(el.tagName());
                broker.setSourcePath(formInfo.getFormPath());
                broker.setBaseFormPath(formInfo.getBaseFormPath());

                extractVariables(el, broker);

                brokers.add(broker);
            }
        }

        // Добавляем все брокеры в FormInfo
        for (BrokerInfo broker : brokers) {
            formInfo.addBroker(broker);
        }
    }

    /**
     * Извлекает переменные (ActionVar) из элемента и добавляет их в брокер
     */
    private void extractVariables(Element element, BrokerInfo brokerInfo) {
        // M2 синтаксис
        Elements m2Vars = element.select("component[cmptype=ActionVar], component[cmptype=DataSetVar]");
        for (Element var : m2Vars) {
            RouterVariable variable = createRouterVariable(var);
            if (variable != null) {
                brokerInfo.addVariable(variable);
            }
        }

        // D3 синтаксис
        Elements d3Vars = element.select("cmpActionVar, cmpDataSetVar");
        for (Element var : d3Vars) {
            RouterVariable variable = createRouterVariable(var);
            if (variable != null) {
                brokerInfo.addVariable(variable);
            }
        }
    }

    /**
     * Создаёт объект RouterVariable из элемента
     */
    private RouterVariable createRouterVariable(Element var) {
        String name = var.attr("name");
        if (name == null || name.isEmpty()) return null;

        return new RouterVariable.Builder(name)
                .setSrc(var.attr("src"))
                .setSrcType(var.attr("srctype"))
                .setGet(var.attr("get"))
                .setPut(var.attr("put"))
                .setType(var.attr("type"))
                .setLen(var.attr("len"))
                .setDefaultValue(var.attr("default"))
                .build();
    }

    /**
     * Проверяет, есть ли в компоненте SQL содержимое
     */
    private boolean hasSqlContent(Element element) {
        String html = element.html();
        if (html == null) return false;

        // Проверяем наличие CDATA с SQL
        java.util.regex.Pattern cdataPattern = java.util.regex.Pattern.compile(
                "<!\\[CDATA\\[(.*?)\\]\\]>", java.util.regex.Pattern.DOTALL
        );
        java.util.regex.Matcher matcher = cdataPattern.matcher(html);

        while (matcher.find()) {
            String content = matcher.group(1);
            if (content != null) {
                String lower = content.toLowerCase().trim();
                if (lower.startsWith("select") || lower.startsWith("insert") ||
                        lower.startsWith("update") || lower.startsWith("delete") ||
                        lower.startsWith("begin")) {
                    return true;
                }
            }
        }

        return false;
    }
}