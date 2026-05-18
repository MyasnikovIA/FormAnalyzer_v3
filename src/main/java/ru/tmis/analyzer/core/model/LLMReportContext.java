package ru.tmis.analyzer.core.model;

import java.util.*;

public class LLMReportContext {

    private Map<String, BrokerInfo> brokersMap = new LinkedHashMap<>();
    private Map<String, String> oracleBrokerFunctions = new LinkedHashMap<>();
    private Map<String, String> postgresBrokerFunctions = new LinkedHashMap<>();

    private List<FormInfo> analyzedForms = new ArrayList<>();
    private List<SqlInfo> allSqlQueries = new ArrayList<>();
    private Set<String> allViews = new LinkedHashSet<>();
    private Map<String, String> postgresViewDDL = new LinkedHashMap<>();
    private Map<String, String> oracleViewDDL = new LinkedHashMap<>();
    private Map<String, Set<String>> postgresViewTables = new LinkedHashMap<>();
    private Map<String, Set<String>> oracleViewTables = new LinkedHashMap<>();
    private Map<String, String> postgresTableDDL = new LinkedHashMap<>();
    private Map<String, String> oracleTableDDL = new LinkedHashMap<>();
    private Map<String, Set<String>> viewUsageInForms = new LinkedHashMap<>();
    private Map<String, String> oracleFunctionBodies = new LinkedHashMap<>();
    private Map<String, String> postgresFunctionBodies = new LinkedHashMap<>();
    private int totalForms = 0;
    private int totalSqlQueries = 0;


    public List<FormInfo> getAnalyzedForms() { return analyzedForms; }
    public void setAnalyzedForms(List<FormInfo> forms) { this.analyzedForms = forms; }
    public List<SqlInfo> getAllSqlQueries() { return allSqlQueries; }
    public void setAllSqlQueries(List<SqlInfo> queries) { this.allSqlQueries = queries; }
    public Set<String> getAllViews() { return allViews; }
    public void setAllViews(Set<String> views) { this.allViews = views; }
    public Map<String, String> getPostgresViewDDL() { return postgresViewDDL; }
    public void setPostgresViewDDL(Map<String, String> ddl) { this.postgresViewDDL = ddl; }
    public Map<String, String> getOracleViewDDL() { return oracleViewDDL; }
    public void setOracleViewDDL(Map<String, String> ddl) { this.oracleViewDDL = ddl; }


    public Map<String, Set<String>> getPostgresViewTables() {
        return postgresViewTables;
    }

    public void setPostgresViewTables(Map<String, Set<String>> postgresViewTables) {
        this.postgresViewTables = postgresViewTables;
    }


    public Map<String, Set<String>> getOracleViewTables() {
        return oracleViewTables;
    }

    public void setOracleViewTables(Map<String, Set<String>> oracleViewTables) {
        this.oracleViewTables = oracleViewTables;
    }

    public Map<String, String> getPostgresTableDDL() {
        return postgresTableDDL;
    }

    public void setPostgresTableDDL(Map<String, String> postgresTableDDL) {
        this.postgresTableDDL = postgresTableDDL;
    }

    public Map<String, String> getOracleTableDDL() {
        return oracleTableDDL;
    }

    public void setOracleTableDDL(Map<String, String> oracleTableDDL) {
        this.oracleTableDDL = oracleTableDDL;
    }

    public Map<String, Set<String>> getViewUsageInForms() {
        return viewUsageInForms;
    }

    public void setViewUsageInForms(Map<String, Set<String>> viewUsageInForms) {
        this.viewUsageInForms = viewUsageInForms;
    }

    public Map<String, String> getOracleFunctionBodies() {
        return oracleFunctionBodies;
    }

    public void setOracleFunctionBodies(Map<String, String> oracleFunctionBodies) {
        this.oracleFunctionBodies = oracleFunctionBodies;
    }

    public Map<String, String> getPostgresFunctionBodies() {
        return postgresFunctionBodies;
    }

    public void setPostgresFunctionBodies(Map<String, String> postgresFunctionBodies) {
        this.postgresFunctionBodies = postgresFunctionBodies;
    }

    public Map<String, BrokerInfo> getBrokersMap() {
        return brokersMap;
    }

    public void setBrokersMap(Map<String, BrokerInfo> brokersMap) {
        this.brokersMap = brokersMap;
    }

    public Map<String, String> getOracleBrokerFunctions() {
        return oracleBrokerFunctions;
    }

    public void setOracleBrokerFunctions(Map<String, String> oracleBrokerFunctions) {
        this.oracleBrokerFunctions = oracleBrokerFunctions;
    }

    public Map<String, String> getPostgresBrokerFunctions() {
        return postgresBrokerFunctions;
    }

    public void setPostgresBrokerFunctions(Map<String, String> postgresBrokerFunctions) {
        this.postgresBrokerFunctions = postgresBrokerFunctions;
    }

    public int getTotalForms() {
        return totalForms;
    }

    public void setTotalForms(int totalForms) {
        this.totalForms = totalForms;
    }

    public int getTotalSqlQueries() {
        return totalSqlQueries;
    }

    public void setTotalSqlQueries(int totalSqlQueries) {
        this.totalSqlQueries = totalSqlQueries;
    }

}