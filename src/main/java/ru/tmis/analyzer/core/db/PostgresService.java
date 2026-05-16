// core/db/PostgresService.java
package ru.tmis.analyzer.core.db;

import java.sql.*;
import java.util.*;

public class PostgresService {

    private final String url;
    private final String user;
    private final String password;
    private final String misUser;

    public PostgresService(String url, String user, String password, String misUser) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.misUser = misUser;
    }

    private Connection getConnection() throws SQLException {
        if (misUser != null && !misUser.trim().isEmpty()) {
            return DatabaseConnector.getPostgresConnectionWithContext(url, user, password, misUser);
        }
        return DatabaseConnector.getPostgresConnection(url, user, password);
    }

    public String getViewDDL(String viewName) {
        try (Connection conn = getConnection()) {

            String getOidSql = "SELECT oid FROM pg_class WHERE relname = ? AND relkind = 'v'";
            try (PreparedStatement oidStmt = conn.prepareStatement(getOidSql)) {
                oidStmt.setString(1, viewName.toLowerCase());
                ResultSet oidRs = oidStmt.executeQuery();

                if (oidRs.next()) {
                    int oid = oidRs.getInt("oid");

                    String sql = "SELECT pg_get_viewdef(?, true) as viewdef";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, oid);
                        ResultSet rs = pstmt.executeQuery();

                        if (rs.next()) {
                            String viewdef = rs.getString("viewdef");
                            if (viewdef != null) {
                                return "-- PostgreSQL View: " + viewName + "\n" + viewdef;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения DDL вьюхи " + viewName + ": " + e.getMessage());
        }

        return null;
    }

    public Map<String, String> getViewsDDL(Set<String> viewNames) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String viewName : viewNames) {
            String ddl = getViewDDL(viewName);
            if (ddl != null) {
                result.put(viewName, ddl);
            }
        }
        return result;
    }

    public String getTableDDL(String tableName) {
        StringBuilder ddl = new StringBuilder();

        try (Connection conn = getConnection()) {

            String columnsSql = "SELECT column_name, data_type, character_maximum_length, " +
                    "numeric_precision, numeric_scale, is_nullable, column_default " +
                    "FROM information_schema.columns WHERE table_name = ? ORDER BY ordinal_position";

            List<String> columns = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(columnsSql)) {
                pstmt.setString(1, tableName.toLowerCase());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String dataType = rs.getString("data_type");
                    int maxLength = rs.getInt("character_maximum_length");
                    int precision = rs.getInt("numeric_precision");
                    int scale = rs.getInt("numeric_scale");
                    String nullable = rs.getString("is_nullable");
                    String defaultValue = rs.getString("column_default");

                    StringBuilder colDef = new StringBuilder();
                    colDef.append("    ").append(colName).append(" ").append(dataType);

                    if ("character varying".equals(dataType) && maxLength > 0) {
                        colDef.append("(").append(maxLength).append(")");
                    } else if ("numeric".equals(dataType) && precision > 0) {
                        colDef.append("(").append(precision);
                        if (scale > 0) colDef.append(",").append(scale);
                        colDef.append(")");
                    }

                    if ("NO".equals(nullable)) {
                        colDef.append(" NOT NULL");
                    }

                    if (defaultValue != null && !defaultValue.isEmpty()) {
                        colDef.append(" DEFAULT ").append(defaultValue);
                    }

                    columns.add(colDef.toString());
                }
            }

            // Получаем первичный ключ
            String pkSql = "SELECT kcu.column_name FROM information_schema.table_constraints tc " +
                    "JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name " +
                    "WHERE tc.table_name = ? AND tc.constraint_type = 'PRIMARY KEY' ORDER BY kcu.ordinal_position";

            List<String> pkColumns = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(pkSql)) {
                pstmt.setString(1, tableName.toLowerCase());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    pkColumns.add(rs.getString("column_name"));
                }
            }

            ddl.append("CREATE TABLE ").append(tableName).append(" (\n");
            ddl.append(String.join(",\n", columns));

            if (!pkColumns.isEmpty()) {
                ddl.append(",\n    CONSTRAINT ").append(tableName).append("_pkey PRIMARY KEY (");
                ddl.append(String.join(", ", pkColumns));
                ddl.append(")");
            }

            ddl.append("\n);\n");

            return ddl.toString();

        } catch (SQLException e) {
            System.err.println("Ошибка получения DDL таблицы " + tableName + ": " + e.getMessage());
            return null;
        }
    }

    public Map<String, String> getTablesDDL(Set<String> tableNames) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String tableName : tableNames) {
            String ddl = getTableDDL(tableName);
            if (ddl != null) {
                result.put(tableName, ddl);
            }
        }
        return result;
    }

    public boolean testConnection() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            return rs.next();
        } catch (SQLException e) {
            System.err.println("PostgreSQL connection test failed: " + e.getMessage());
            return false;
        }
    }
}