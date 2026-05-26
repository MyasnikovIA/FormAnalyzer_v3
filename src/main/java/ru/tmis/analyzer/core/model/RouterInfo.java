// core/model/RouterInfo.java
package ru.tmis.analyzer.core.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Информация о Router компонентах (ActionRouter, DataSetRouter)
 * Поддерживает иерархическую структуру: BeforeAction/BeforeSelect, SubAction/SubSelect
 */
public class RouterInfo {

    // Типы родительских компонентов
    public enum ParentType {
        ACTION("Action"),
        BEFORE_ACTION("BeforeAction"),
        DATASET("DataSet"),
        BEFORE_SELECT("BeforeSelect"),
        SUB_ACTION("SubAction"),
        SUB_SELECT("SubSelect");

        private final String displayName;
        ParentType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    // Типы Router компонентов
    public enum RouterType {
        ACTION_ROUTER("ActionRouter"),
        DATASET_ROUTER("DataSetRouter");

        private final String tagName;
        RouterType(String tagName) { this.tagName = tagName; }
        public String getTagName() { return tagName; }
    }

    private final String name;                    // Имя родительского компонента
    private final ParentType parentType;          // Тип родителя
    private final RouterType routerType;          // Тип роутера
    private final List<RouterItem> routers;       // Список роутеров (в порядке объявления)
    private final List<RouterVariable> variables; // Переменные компонента
    private final List<SubRouterInfo> subRouters; // Вложенные SubAction/SubSelect
    private FormInfo.FormStyle formStyle = FormInfo.FormStyle.UNKNOWN;

    public RouterInfo(String name, ParentType parentType, RouterType routerType) {
        this.name = name;
        this.parentType = parentType;
        this.routerType = routerType;
        this.routers = new ArrayList<>();
        this.variables = new ArrayList<>();
        this.subRouters = new ArrayList<>();
    }

    // Геттеры
    public String getName() { return name; }
    public ParentType getParentType() { return parentType; }
    public RouterType getRouterType() { return routerType; }
    public List<RouterItem> getRouters() { return routers; }
    public List<RouterVariable> getVariables() { return variables; }
    public List<SubRouterInfo> getSubRouters() { return subRouters; }

    public void addRouter(RouterItem router) { this.routers.add(router); }
    public void addVariable(RouterVariable variable) { this.variables.add(variable); }
    public void addSubRouter(SubRouterInfo subRouter) { this.subRouters.add(subRouter); }

    /**
     * Проверяет, есть ли роутер для указанного условия
     */
    public boolean hasRouterForCondition(String condition) {
        return routers.stream().anyMatch(r -> condition.equals(r.getCondition()));
    }

    /**
     * Получить роутер по условию
     */
    public RouterItem getRouterByCondition(String condition) {
        return routers.stream()
                .filter(r -> condition.equals(r.getCondition()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(parentType.getDisplayName()).append(": ").append(name)
                .append(" [").append(routerType.getTagName()).append("]");
        if (!routers.isEmpty()) {
            sb.append(", routers=").append(routers.size());
        }
        if (!variables.isEmpty()) {
            sb.append(", vars=").append(variables.size());
        }
        if (!subRouters.isEmpty()) {
            sb.append(", sub=").append(subRouters.size());
        }
        return sb.toString();
    }
    public FormInfo.FormStyle getFormStyle() { return formStyle; }
    public void setFormStyle(FormInfo.FormStyle formStyle) { this.formStyle = formStyle; }

    // Вспомогательные методы
    public boolean isM2Style() { return formStyle == FormInfo.FormStyle.M2; }
    public boolean isD3Style() { return formStyle == FormInfo.FormStyle.D3; }

}