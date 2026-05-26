// core/model/FormInfo.java
package ru.tmis.analyzer.core.model;
import java.util.*;

/**
 * Информация о форме после применения всех переопределений
 */
public class FormInfo {

    /**
     * Стиль формы (M2 или D3)
     */
    public enum FormStyle {
        M2("M2", "component cmptype=\"...\"", "openWindow()"),
        D3("D3", "cmp...", "openD3Form()"),
        UNKNOWN("Unknown", "не определен", "не определен");

        private final String name;
        private final String syntax;
        private final String openMethod;

        FormStyle(String name, String syntax, String openMethod) {
            this.name = name;
            this.syntax = syntax;
            this.openMethod = openMethod;
        }

        public String getName() { return name; }
        public String getSyntax() { return syntax; }
        public String getOpenMethod() { return openMethod; }

        public boolean isM2() { return this == M2; }
        public boolean isD3() { return this == D3; }

        @Override
        public String toString() { return name; }
    }

    private FormStyle formStyle = FormStyle.UNKNOWN;
    private ConversionStatistics conversionStatistics;
    private Map<String, ConversionStatistics.QueryConversionInfo> queryConversionMap = new LinkedHashMap<>();
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
    private List<BrokerInfo> brokers = new ArrayList<>();
    private Set<String> autoPopupMenus;
    private Set<String> reports;
    private Set<String> openFormCompositions;
    private Set<String> d3ApiShowFormCompositions;
    private Set<String> openD3FormCompositions;
    private Map<String, ViewTableDependencies> viewDependencies;
    private List<PopupMenuInfo> popupMenus;
    private List<PopupMenuInfo> popupMenusPg;
    private Set<String> tablesFromViews;  // Таблицы, используемые через вьюхи
    private List<ReportFromAutoPopupInfo> reportsFromAutoPopup = new ArrayList<>();
    private List<RouterInfo> actionRouters;      // Action и BeforeAction с роутерами
    private List<RouterInfo> dataSetRouters;     // DataSet и BeforeSelect с роутерами


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
        this.brokers = new ArrayList<>();
        this.autoPopupMenus = new LinkedHashSet<>();
        this.reports = new LinkedHashSet<>();
        this.openFormCompositions = new LinkedHashSet<>();
        this.d3ApiShowFormCompositions = new LinkedHashSet<>();
        this.openD3FormCompositions = new LinkedHashSet<>();
        this.tablesFromViews = new LinkedHashSet<>();
        this.actionRouters = new ArrayList<>();
        this.dataSetRouters = new ArrayList<>();
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

    public List<BrokerInfo> getBrokers() { return brokers; }
    public void setBrokers(List<BrokerInfo> brokers) { this.brokers = brokers; }
    public void addBroker(BrokerInfo broker) {
        if (this.brokers == null) {
            this.brokers = new ArrayList<>();
        }
        this.brokers.add(broker);
    }
    @Deprecated
    public Set<String> getBrokersAsStrings() {
        Set<String> result = new LinkedHashSet<>();
        for (BrokerInfo broker : brokers) {
            result.add(broker.getDisplayString());
        }
        return result;
    }

    public Set<String> getAutoPopupMenus() { return autoPopupMenus; }
    public void addAutoPopupMenu(String unit) { this.autoPopupMenus.add(unit); }

    public Set<String> getReports() { return reports; }
    public void addReport(String report) { this.reports.add(report); }

    public int getTotalSqlQueries() { return sqlQueries.size(); }
    public Map<String, ViewTableDependencies> getViewDependencies() { return viewDependencies; }
    public void setViewDependencies(Map<String, ViewTableDependencies> deps) { this.viewDependencies = deps; }
    public List<PopupMenuInfo> getPopupMenus() {
        return popupMenus;
    }

    public void setPopupMenus(List<PopupMenuInfo> popupMenus) {
        this.popupMenus = popupMenus;
    }
    public List<PopupMenuInfo> getPopupMenusPg() {
        return popupMenusPg;
    }

    public void setPopupMenusPg(List<PopupMenuInfo> popupMenusPg) {
        this.popupMenusPg = popupMenusPg;
    }

    public ConversionStatistics getConversionStatistics() {
        return conversionStatistics;
    }

    public void setConversionStatistics(ConversionStatistics conversionStatistics) {
        this.conversionStatistics = conversionStatistics;
    }
    // Геттер и сеттер
    public List<ReportFromAutoPopupInfo> getReportsFromAutoPopup() {
        return reportsFromAutoPopup;
    }

    public void setReportsFromAutoPopup(List<ReportFromAutoPopupInfo> reports) {
        this.reportsFromAutoPopup = reports;
    }


    public List<RouterInfo> getActionRouters() { return actionRouters; }
    public void setActionRouters(List<RouterInfo> actionRouters) { this.actionRouters = actionRouters; }
    public void addActionRouter(RouterInfo router) { this.actionRouters.add(router); }

    public List<RouterInfo> getDataSetRouters() { return dataSetRouters; }
    public void setDataSetRouters(List<RouterInfo> dataSetRouters) { this.dataSetRouters = dataSetRouters; }
    public void addDataSetRouter(RouterInfo router) { this.dataSetRouters.add(router); }


    public void addReportFromAutoPopup(ReportFromAutoPopupInfo report) {
        if (this.reportsFromAutoPopup == null) {
            this.reportsFromAutoPopup = new ArrayList<>();
        }
        this.reportsFromAutoPopup.add(report);
    }

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

    public Set<String> getTablesFromViews() {
        return tablesFromViews;
    }

    public void setTablesFromViews(Set<String> tablesFromViews) {
        this.tablesFromViews = tablesFromViews;
    }

    public void addTableFromView(String table) {
        if (this.tablesFromViews == null) {
            this.tablesFromViews = new LinkedHashSet<>();
        }
        this.tablesFromViews.add(table);
    }

    public void addAllTablesFromViews(Set<String> tables) {
        if (this.tablesFromViews == null) {
            this.tablesFromViews = new LinkedHashSet<>();
        }
        this.tablesFromViews.addAll(tables);
    }


    /**
     * Информация об отчёте из AutoPopup для JSON экспорта
     */
    public static class ReportFromAutoPopupInfo {
        private final String repCode;
        private final String repType;
        private final String repTypeName;
        private final String repFilename;
        private final String formPath;

        public ReportFromAutoPopupInfo(String repCode, String repType, String repTypeName,
                                       String repFilename, String formPath) {
            this.repCode = repCode;
            this.repType = repType;
            this.repTypeName = repTypeName;
            this.repFilename = repFilename;
            this.formPath = formPath;
        }

        public String getRepCode() { return repCode; }
        public String getRepType() { return repType; }
        public String getRepTypeName() { return repTypeName; }
        public String getRepFilename() { return repFilename; }
        public String getFormPath() { return formPath; }
    }
    // Геттер и сеттер
    public FormStyle getFormStyle() { return formStyle; }
    public void setFormStyle(FormStyle formStyle) { this.formStyle = formStyle; }

    // Вспомогательные методы
    public boolean isM2Style() { return formStyle == FormStyle.M2; }
    public boolean isD3Style() { return formStyle == FormStyle.D3; }

    public Map<String, ConversionStatistics.QueryConversionInfo> getQueryConversionMap() {
        return queryConversionMap;
    }

    public void setQueryConversionMap(Map<String, ConversionStatistics.QueryConversionInfo> map) {
        this.queryConversionMap = map;
    }
}