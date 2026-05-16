// core/model/FormInfo.java
package ru.tmis.analyzer.core.model;

import java.util.*;

/**
 * Информация о форме после применения всех переопределений
 */
public class FormInfo {
    private String formPath;
    private String baseFormPath;
    private List<OverrideInfo> overrides;
    private boolean fullyReplaced;
    private String replacementPath;
    private List<SqlInfo> sqlQueries;
    private Set<String> tablesViews;
    private Set<String> packagesFunctions;
    private Set<String> userProcedures;
    private Set<String> systemOptions;
    private Set<String> subForms;
    private Set<String> jsForms;
    private Set<String> unknownObjects;
    private Set<String> constants;
    private Set<String> unitCompositions;
    private Set<String> jsUnitCompositions;
    private Set<String> brokers;
    private Set<String> autoPopupMenus;
    private Set<String> reports;
    private Set<String> openFormCompositions;
    private Set<String> d3ApiShowFormCompositions;
    private Set<String> openD3FormCompositions;

    private Map<String, ViewTableDependencies> viewDependencies;

    public FormInfo(String formPath) {
        this.formPath = formPath;
        this.overrides = new ArrayList<>();
        this.sqlQueries = new ArrayList<>();
        this.tablesViews = new LinkedHashSet<>();
        this.packagesFunctions = new LinkedHashSet<>();
        this.userProcedures = new LinkedHashSet<>();
        this.systemOptions = new LinkedHashSet<>();
        this.subForms = new LinkedHashSet<>();
        this.jsForms = new LinkedHashSet<>();
        this.unknownObjects = new LinkedHashSet<>();
        this.constants = new LinkedHashSet<>();
        this.unitCompositions = new LinkedHashSet<>();
        this.jsUnitCompositions = new LinkedHashSet<>();
        this.brokers = new LinkedHashSet<>();
        this.autoPopupMenus = new LinkedHashSet<>();
        this.reports = new LinkedHashSet<>();
        this.openFormCompositions = new LinkedHashSet<>();
        this.d3ApiShowFormCompositions = new LinkedHashSet<>();
        this.openD3FormCompositions = new LinkedHashSet<>();
    }

    // Getters and Setters
    public String getFormPath() { return formPath; }
    public void setFormPath(String formPath) { this.formPath = formPath; }

    public String getBaseFormPath() { return baseFormPath; }
    public void setBaseFormPath(String baseFormPath) { this.baseFormPath = baseFormPath; }

    public List<OverrideInfo> getOverrides() { return overrides; }
    public void addOverride(OverrideInfo override) { this.overrides.add(override); }

    public boolean isFullyReplaced() { return fullyReplaced; }
    public void setFullyReplaced(boolean fullyReplaced) { this.fullyReplaced = fullyReplaced; }

    public String getReplacementPath() { return replacementPath; }
    public void setReplacementPath(String replacementPath) { this.replacementPath = replacementPath; }

    public List<SqlInfo> getSqlQueries() { return sqlQueries; }
    public void setSqlQueries(List<SqlInfo> sqlQueries) { this.sqlQueries = sqlQueries; }

    public Set<String> getTablesViews() { return tablesViews; }
    public void addTableView(String tv) { this.tablesViews.add(tv); }

    public Set<String> getPackagesFunctions() { return packagesFunctions; }
    public void addPackageFunction(String pf) { this.packagesFunctions.add(pf); }

    public Set<String> getUserProcedures() { return userProcedures; }
    public void addUserProcedure(String proc) { this.userProcedures.add(proc); }

    public Set<String> getSystemOptions() { return systemOptions; }
    public void addSystemOption(String option) { this.systemOptions.add(option); }

    public Set<String> getSubForms() { return subForms; }
    public void addSubForm(String subForm) { this.subForms.add(subForm); }

    public Set<String> getJsForms() { return jsForms; }
    public void addJsForm(String jsForm) { this.jsForms.add(jsForm); }

    public Set<String> getUnknownObjects() { return unknownObjects; }
    public void addUnknownObject(String obj) { this.unknownObjects.add(obj); }

    public Set<String> getConstants() { return constants; }
    public void addConstant(String constant) { this.constants.add(constant); }

    public Set<String> getUnitCompositions() { return unitCompositions; }
    public void addUnitComposition(String composition) { this.unitCompositions.add(composition); }

    public Set<String> getJsUnitCompositions() { return jsUnitCompositions; }
    public void addJsUnitComposition(String composition) { this.jsUnitCompositions.add(composition); }

    public Set<String> getBrokers() { return brokers; }
    public void addBroker(String broker) { this.brokers.add(broker); }

    public Set<String> getAutoPopupMenus() { return autoPopupMenus; }
    public void addAutoPopupMenu(String unit) { this.autoPopupMenus.add(unit); }

    public Set<String> getReports() { return reports; }
    public void addReport(String report) { this.reports.add(report); }

    public int getTotalSqlQueries() { return sqlQueries.size(); }
    public Map<String, ViewTableDependencies> getViewDependencies() { return viewDependencies; }
    public void setViewDependencies(Map<String, ViewTableDependencies> deps) { this.viewDependencies = deps; }

    /**
     * Информация о переопределении формы
     */
    public static class OverrideInfo {
        private String regionName;
        private String overridePath;
        private OverrideType type;
        private String baseTarget;
        private String position;

        public enum OverrideType {
            FULL_OVERRIDE(".frm - полная замена"),
            PARTIAL_OVERRIDE(".dfrm - частичное переопределение"),
            DOT_D_OVERRIDE(".d/*.dfrm - переопределение из каталога .d");

            private String description;
            OverrideType(String description) { this.description = description; }
            public String getDescription() { return description; }
        }

        public OverrideInfo(String regionName, String overridePath, OverrideType type) {
            this.regionName = regionName;
            this.overridePath = overridePath;
            this.type = type;
        }

        public OverrideInfo(String regionName, String overridePath, OverrideType type,
                            String baseTarget, String position) {
            this(regionName, overridePath, type);
            this.baseTarget = baseTarget;
            this.position = position;
        }

        // Getters
        public String getRegionName() { return regionName; }
        public String getOverridePath() { return overridePath; }
        public OverrideType getType() { return type; }
        public String getBaseTarget() { return baseTarget; }
        public String getPosition() { return position; }
    }

    public Set<String> getOpenFormCompositions() {
        return openFormCompositions;
    }

    public void setOpenFormCompositions(Set<String> openFormCompositions) {
        this.openFormCompositions = openFormCompositions;
    }

    public void addOpenFormComposition(String composition) {
        if (composition != null && !composition.isEmpty()) {
            this.openFormCompositions.add(composition);
        }
    }

    public Set<String> getD3ApiShowFormCompositions() {
        return d3ApiShowFormCompositions;
    }

    public void setD3ApiShowFormCompositions(Set<String> d3ApiShowFormCompositions) {
        this.d3ApiShowFormCompositions = d3ApiShowFormCompositions;
    }

    public void addD3ApiShowFormComposition(String composition) {
        if (composition != null && !composition.isEmpty()) {
            this.d3ApiShowFormCompositions.add(composition);
        }
    }

    public Set<String> getOpenD3FormCompositions() {
        return openD3FormCompositions;
    }

    public void setOpenD3FormCompositions(Set<String> openD3FormCompositions) {
        this.openD3FormCompositions = openD3FormCompositions;
    }

    public void addOpenD3FormComposition(String composition) {
        if (composition != null && !composition.isEmpty()) {
            this.openD3FormCompositions.add(composition);
        }
    }
}