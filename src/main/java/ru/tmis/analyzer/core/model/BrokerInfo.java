// core/model/BrokerInfo.java

package ru.tmis.analyzer.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Информация о брокере (Action/SubAction с unit/action или прямым указанием функции)
 */
public class BrokerInfo {

    public enum BrokerType {
        TYPE1_UNIT_ACTION,   // unit="XXX" action="YYY" - требует поиска в D_UNITBPS
        TYPE2_DIRECT_FUNCTION // action="D_PKG_XXX.YYY" - функция уже известна
    }

    // Основные поля
    private BrokerType type;
    private String unit;              // Для TYPE1
    private String action;            // Для TYPE1 - название действия (INSERT, UPDATE, DELETE и т.д.)
    private String functionName;      // Для TYPE2 - полное имя функции (D_PKG_XXX.YYY)
    private String execProc;          // Найденная функция из D_UNITBPS (для TYPE1)

    // Переменные (ActionVar / cmpActionVar)
    private List<RouterVariable> variables = new ArrayList<>();

    // Дополнительная информация
    private String componentName;      // Имя компонента (например, "CancelCloseBull")
    private String componentType;      // Тип компонента ("Action" или "SubAction")
    private String sourcePath;         // Путь к форме, где найден брокер
    private String baseFormPath;       // Базовая форма

    // Конструкторы
    public BrokerInfo() {
        this.type = BrokerType.TYPE1_UNIT_ACTION;
    }

    // Конструктор для TYPE1 (unit + action)
    public BrokerInfo(String unit, String action) {
        this.type = BrokerType.TYPE1_UNIT_ACTION;
        this.unit = unit;
        this.action = action;
    }

    // Конструктор для TYPE2 (прямое указание функции)
    public BrokerInfo(String functionName) {
        this.type = BrokerType.TYPE2_DIRECT_FUNCTION;
        this.functionName = functionName;
        this.execProc = functionName;
    }

    // Геттеры и сеттеры
    public BrokerType getType() { return type; }
    public void setType(BrokerType type) { this.type = type; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public String getExecProc() { return execProc; }
    public void setExecProc(String execProc) { this.execProc = execProc; }

    public List<RouterVariable> getVariables() { return variables; }
    public void setVariables(List<RouterVariable> variables) { this.variables = variables; }
    public void addVariable(RouterVariable variable) { this.variables.add(variable); }

    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }

    public String getComponentType() { return componentType; }
    public void setComponentType(String componentType) { this.componentType = componentType; }

    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }

    public String getBaseFormPath() { return baseFormPath; }
    public void setBaseFormPath(String baseFormPath) { this.baseFormPath = baseFormPath; }

    /**
     * Возвращает строковое представление для отображения в отчётах (совместимость со старым форматом)
     */
    public String getDisplayString() {
        if (type == BrokerType.TYPE1_UNIT_ACTION) {
            return String.format("unit:%s action:%s;", unit, action);
        } else {
            return String.format("action:%s;", functionName);
        }
    }

    /**
     * Возвращает строку с детальной информацией (включая переменные)
     */
    public String getDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDisplayString());

        if (!variables.isEmpty()) {
            sb.append("\n        Переменные:");
            for (RouterVariable var : variables) {
                sb.append("\n            - ").append(var.getName());
                if (var.getSrc() != null) sb.append(" src=").append(var.getSrc());
                if (var.getSrcType() != null) sb.append(" srctype=").append(var.getSrcType());
                if (var.getGet() != null && !var.getGet().isEmpty()) sb.append(" get=").append(var.getGet());
                if (var.getPut() != null && !var.getPut().isEmpty()) sb.append(" put=").append(var.getPut());
            }
        }

        if (execProc != null && !execProc.isEmpty() && type == BrokerType.TYPE1_UNIT_ACTION) {
            sb.append("\n        execProc: ").append(execProc);
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrokerInfo that = (BrokerInfo) o;
        if (type == BrokerType.TYPE1_UNIT_ACTION && that.type == BrokerType.TYPE1_UNIT_ACTION) {
            return Objects.equals(unit, that.unit) && Objects.equals(action, that.action);
        } else if (type == BrokerType.TYPE2_DIRECT_FUNCTION && that.type == BrokerType.TYPE2_DIRECT_FUNCTION) {
            return Objects.equals(functionName, that.functionName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (type == BrokerType.TYPE1_UNIT_ACTION) {
            return Objects.hash(unit, action, type);
        } else {
            return Objects.hash(functionName, type);
        }
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}