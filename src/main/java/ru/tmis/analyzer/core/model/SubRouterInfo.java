// core/model/SubRouterInfo.java
package ru.tmis.analyzer.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Информация о вложенных компонентах (SubAction, SubSelect)
 */
public class SubRouterInfo {

    private final String name;                    // Имя SubAction/SubSelect
    private final RouterInfo.ParentType type;     // SUB_ACTION или SUB_SELECT
    private final String groupName;               // groupname (для SubAction) или repeatername (для SubSelect)
    private final String execon;                  // execon атрибут
    private final String mode;                    // mode атрибут (execlast и т.д.)
    private final boolean savepoint;              // savepoint атрибут
    private final List<RouterItem> routers;       // Роутеры внутри SubAction/SubSelect
    private final List<RouterVariable> variables; // Переменные (SubActionVar)

    private SubRouterInfo(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.groupName = builder.groupName;
        this.execon = builder.execon;
        this.mode = builder.mode;
        this.savepoint = builder.savepoint;
        this.routers = builder.routers;
        this.variables = builder.variables;
    }

    // Геттеры
    public String getName() { return name; }
    public RouterInfo.ParentType getType() { return type; }
    public String getGroupName() { return groupName; }
    public String getExecon() { return execon; }
    public String getMode() { return mode; }
    public boolean isSavepoint() { return savepoint; }
    public List<RouterItem> getRouters() { return routers; }
    public List<RouterVariable> getVariables() { return variables; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.getDisplayName()).append(": ").append(name);
        if (groupName != null) sb.append(", groupname='").append(groupName).append("'");
        if (!routers.isEmpty()) sb.append(", routers=").append(routers.size());
        if (!variables.isEmpty()) sb.append(", vars=").append(variables.size());
        return sb.toString();
    }

    public static class Builder {
        private String name;
        private RouterInfo.ParentType type;
        private String groupName;
        private String execon;
        private String mode;
        private boolean savepoint = false;
        private List<RouterItem> routers = new ArrayList<>();
        private List<RouterVariable> variables = new ArrayList<>();

        public Builder(String name, RouterInfo.ParentType type) {
            this.name = name;
            this.type = type;
        }

        public Builder setGroupName(String groupName) { this.groupName = groupName; return this; }
        public Builder setExecon(String execon) { this.execon = execon; return this; }
        public Builder setMode(String mode) { this.mode = mode; return this; }
        public Builder setSavepoint(boolean savepoint) { this.savepoint = savepoint; return this; }
        public Builder setRouters(List<RouterItem> routers) { this.routers = routers; return this; }
        public Builder addRouter(RouterItem router) { this.routers.add(router); return this; }
        public Builder setVariables(List<RouterVariable> variables) { this.variables = variables; return this; }
        public Builder addVariable(RouterVariable variable) { this.variables.add(variable); return this; }

        public SubRouterInfo build() {
            return new SubRouterInfo(this);
        }
    }
}