// core/db/OracleService.java
package ru.tmis.analyzer.core.db;

import java.sql.*;
import java.util.*;

public class OracleService {

    private final String url;
    private final String user;
    private final String password;

    public OracleService(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public String getViewDDL(String viewName) {
        String sql = "SELECT TEXT FROM ALL_VIEWS WHERE VIEW_NAME = ?";

        try (Connection conn = DatabaseConnector.getOracleConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, viewName.toUpperCase());
            pstmt.setQueryTimeout(30);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String text = rs.getString("TEXT");
                if (text != null) {
                    return "-- Oracle View: " + viewName + "\n" + text;
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

        try (Connection conn = DatabaseConnector.getOracleConnection(url, user, password)) {

            // Получаем колонки
            String columnsSql = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, DATA_SCALE, NULLABLE " +
                    "FROM ALL_TAB_COLUMNS WHERE OWNER = ? AND TABLE_NAME = ? ORDER BY COLUMN_ID";

            List<String> columns = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(columnsSql)) {
                pstmt.setString(1, user.toUpperCase());
                pstmt.setString(2, tableName.toUpperCase());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    String dataType = rs.getString("DATA_TYPE");
                    int dataLength = rs.getInt("DATA_LENGTH");
                    int precision = rs.getInt("DATA_PRECISION");
                    int scale = rs.getInt("DATA_SCALE");
                    String nullable = rs.getString("NULLABLE");

                    StringBuilder colDef = new StringBuilder();
                    colDef.append("    ").append(colName).append(" ").append(dataType);

                    if ("VARCHAR2".equals(dataType) || "CHAR".equals(dataType)) {
                        colDef.append("(").append(dataLength).append(")");
                    } else if ("NUMBER".equals(dataType) && precision > 0) {
                        colDef.append("(").append(precision);
                        if (scale > 0) colDef.append(",").append(scale);
                        colDef.append(")");
                    }

                    if ("N".equals(nullable)) {
                        colDef.append(" NOT NULL");
                    }

                    columns.add(colDef.toString());
                }
            }

            // Получаем первичный ключ
            String pkSql = "SELECT cols.COLUMN_NAME FROM ALL_CONSTRAINTS cons " +
                    "JOIN ALL_CONS_COLUMNS cols ON cons.CONSTRAINT_NAME = cols.CONSTRAINT_NAME " +
                    "WHERE cons.OWNER = ? AND cons.CONSTRAINT_TYPE = 'P' AND cons.TABLE_NAME = ? " +
                    "ORDER BY cols.POSITION";

            List<String> pkColumns = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(pkSql)) {
                pstmt.setString(1, user.toUpperCase());
                pstmt.setString(2, tableName.toUpperCase());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    pkColumns.add(rs.getString("COLUMN_NAME"));
                }
            }

            ddl.append("CREATE TABLE ").append(tableName).append(" (\n");
            ddl.append(String.join(",\n", columns));

            if (!pkColumns.isEmpty()) {
                ddl.append(",\n    CONSTRAINT PK_").append(tableName).append(" PRIMARY KEY (");
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
        try (Connection conn = DatabaseConnector.getOracleConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL")) {
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Oracle connection test failed: " + e.getMessage());
            return false;
        }
    }
    // core/db/OracleService.java

    /**
     * Получить количество записей в таблице/вьюхе Oracle
     * @param objectName имя таблицы или представления
     * @return количество записей, -1 в случае ошибки
     */
    public long getTableCount(String objectName) {
        String sql = "SELECT COUNT(*) FROM " + objectName;
        try (Connection conn = DatabaseConnector.getOracleConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(30);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка подсчёта записей в Oracle " + objectName + ": " + e.getMessage());
        }
        return -1;
    }
}