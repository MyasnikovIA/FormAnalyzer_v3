// core/model/SqlInfo.java
package ru.tmis.analyzer.core.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SqlInfo {
    private String sourceType;
    private String sourcePath;
    private String baseFormPath;
    private String componentType;
    private String componentName;
    private String sqlContent;
    private String cleanSql;
    private Set<String> tablesViews;
    private Set<String> packagesFunctions;
    private Set<String> userProcedures;
    private Set<String> systemOptions;
    private Set<String> unknownObjects;
    private Set<String> constants;

    private List<RouterVariable> variables = new ArrayList<>();

    public SqlInfo() {
        this.tablesViews = new LinkedHashSet<>();
        this.packagesFunctions = new LinkedHashSet<>();
        this.userProcedures = new LinkedHashSet<>();
        this.systemOptions = new LinkedHashSet<>();
        this.unknownObjects = new LinkedHashSet<>();
        this.constants = new LinkedHashSet<>();
    }

    // Getters and Setters
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }

    public String getBaseFormPath() { return baseFormPath; }
    public void setBaseFormPath(String baseFormPath) { this.baseFormPath = baseFormPath; }

    public String getComponentType() { return componentType; }
    public void setComponentType(String componentType) { this.componentType = componentType; }

    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }

    public String getSqlContent() { return sqlContent; }
    public void setSqlContent(String sqlContent) { this.sqlContent = sqlContent; }

    public String getCleanSql() { return cleanSql; }
    public void setCleanSql(String cleanSql) { this.cleanSql = cleanSql; }

    public Set<String> getTablesViews() { return tablesViews; }
    public void addTableView(String tv) { this.tablesViews.add(tv); }

    public Set<String> getPackagesFunctions() { return packagesFunctions; }
    public void addPackageFunction(String pf) { this.packagesFunctions.add(pf); }

    public Set<String> getUserProcedures() { return userProcedures; }
    public void addUserProcedure(String proc) { this.userProcedures.add(proc); }

    public Set<String> getSystemOptions() { return systemOptions; }
    public void addSystemOption(String option) { this.systemOptions.add(option); }

    public Set<String> getUnknownObjects() { return unknownObjects; }
    public void addUnknownObject(String obj) { this.unknownObjects.add(obj); }

    public Set<String> getConstants() { return constants; }
    public void addConstant(String constant) { this.constants.add(constant); }
    public List<RouterVariable> getVariables() { return variables; }
    public void setVariables(List<RouterVariable> variables) { this.variables = variables; }
    public void addVariable(RouterVariable variable) {
        if (this.variables == null) {
            this.variables = new ArrayList<>();
        }
        this.variables.add(variable);
    }
}