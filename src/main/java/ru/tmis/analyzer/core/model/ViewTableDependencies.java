// core/model/ViewTableDependencies.java
package ru.tmis.analyzer.core.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class ViewTableDependencies {

    private final String viewName;
    private final Set<String> oracleTables;
    private boolean existsInOracle;
    private String oracleError;

    public ViewTableDependencies(String viewName) {
        this.viewName = viewName;
        this.oracleTables = new LinkedHashSet<>();
    }

    public String getViewName() {
        return viewName;
    }

    public Set<String> getOracleTables() {
        return oracleTables;
    }

    public void addOracleTable(String table) {
        this.oracleTables.add(table);
    }

    public void addAllOracleTables(Set<String> tables) {
        this.oracleTables.addAll(tables);
    }

    public boolean isExistsInOracle() {
        return existsInOracle;
    }

    public void setExistsInOracle(boolean existsInOracle) {
        this.existsInOracle = existsInOracle;
    }

    public String getOracleError() {
        return oracleError;
    }

    public void setOracleError(String oracleError) {
        this.oracleError = oracleError;
    }
}