package ru.tmis.analyzer.core.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class ViewTableDependencies {

    private final String viewName;
    private final Set<String> oracleTables;
    private final Set<String> postgresTables;
    private boolean existsInOracle;
    private boolean existsInPostgres;
    private String oracleError;
    private String postgresError;

    public ViewTableDependencies(String viewName) {
        this.viewName = viewName;
        this.oracleTables = new LinkedHashSet<>();
        this.postgresTables = new LinkedHashSet<>();
    }

    public String getViewName() {
        return viewName;
    }

    // Oracle
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

    // PostgreSQL - ДОБАВИТЬ
    public Set<String> getPostgresTables() {
        return postgresTables;
    }

    public void addPostgresTable(String table) {
        this.postgresTables.add(table);
    }

    public void addAllPostgresTables(Set<String> tables) {
        this.postgresTables.addAll(tables);
    }

    public boolean isExistsInPostgres() {
        return existsInPostgres;
    }

    public void setExistsInPostgres(boolean existsInPostgres) {
        this.existsInPostgres = existsInPostgres;
    }

    public String getPostgresError() {
        return postgresError;
    }

    public void setPostgresError(String postgresError) {
        this.postgresError = postgresError;
    }

}