// core/service/ViewDependencyAnalyzer.java
package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.model.ViewTableDependencies;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewDependencyAnalyzer {

    private final SettingsModel settings;

    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "\\bFROM\\s+([A-Za-z0-9_]+)\\b|\\bJOIN\\s+([A-Za-z0-9_]+)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public ViewDependencyAnalyzer(SettingsModel settings) {
        this.settings = settings;
    }

    public ViewTableDependencies analyzeView(String viewName) {
        ViewTableDependencies deps = new ViewTableDependencies(viewName);

        String ddl = getViewDDL(viewName);
        if (ddl != null && !ddl.isEmpty()) {
            deps.setExistsInOracle(true);
            extractTablesFromDDL(ddl, deps);
        } else {
            deps.setExistsInOracle(false);
            deps.setOracleError("Вьюха не найдена в Oracle");
        }

        return deps;
    }

    private String getViewDDL(String viewName) {
        String sql = "SELECT TEXT FROM ALL_VIEWS WHERE VIEW_NAME = ?";

        Properties props = new Properties();
        props.setProperty("user", settings.getOracleUser());
        props.setProperty("password", settings.getOraclePassword());
        props.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
        props.setProperty("oracle.jdbc.ReadTimeout", "30000");

        try (Connection conn = DriverManager.getConnection(settings.getOracleUrl(), props);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, viewName.toUpperCase());
            pstmt.setQueryTimeout(30);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("TEXT");
            }
        } catch (SQLException e) {
            System.err.println("  Ошибка получения DDL вьюхи " + viewName + ": " + e.getMessage());
        }

        return null;
    }

    private void extractTablesFromDDL(String ddl, ViewTableDependencies deps) {
        if (ddl == null) return;

        String upperDdl = ddl.toUpperCase();
        Matcher matcher = TABLE_PATTERN.matcher(upperDdl);

        Set<String> sqlKeywords = Set.of(
                "SELECT", "FROM", "WHERE", "JOIN", "ON", "AND", "OR", "NOT",
                "IN", "EXISTS", "AS", "LEFT", "RIGHT", "INNER", "OUTER", "CROSS",
                "UNION", "INTERSECT", "MINUS", "WITH", "RECURSIVE"
        );

        while (matcher.find()) {
            String table = matcher.group(1);
            if (table == null) table = matcher.group(2);

            if (table != null && !sqlKeywords.contains(table) && !table.startsWith("D_V_")) {
                deps.addOracleTable(table);
            }
        }
    }
}