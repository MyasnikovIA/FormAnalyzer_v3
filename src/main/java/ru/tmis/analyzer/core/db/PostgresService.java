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

            // Получаем колонки вместе с комментариями и default значениями
            String columnsSql =
                    "SELECT a.attname AS column_name, " +
                            "       pg_catalog.format_type(a.atttypid, a.atttypmod) AS data_type, " +
                            "       a.attnotnull AS not_null, " +
                            "       pg_catalog.pg_get_expr(d.adbin, d.adrelid) AS default_value, " +
                            "       pg_catalog.col_description(a.attrelid, a.attnum) AS comment " +
                            "FROM pg_catalog.pg_class c " +
                            "JOIN pg_catalog.pg_attribute a ON a.attrelid = c.oid " +
                            "LEFT JOIN pg_catalog.pg_attrdef d ON d.adrelid = a.attrelid AND d.adnum = a.attnum " +
                            "WHERE c.relname = ? " +
                            "  AND a.attnum > 0 " +
                            "  AND NOT a.attisdropped " +
                            "ORDER BY a.attnum";

            List<String> columns = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(columnsSql)) {
                pstmt.setString(1, tableName.toLowerCase());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String dataType = rs.getString("data_type");
                    boolean notNull = rs.getBoolean("not_null");
                    String defaultValue = rs.getString("default_value");
                    String comment = rs.getString("comment");

                    StringBuilder colDef = new StringBuilder();
                    colDef.append("    ").append(colName).append(" ").append(dataType);

                    if (notNull) {
                        colDef.append(" NOT NULL");
                    }

                    if (defaultValue != null && !defaultValue.isEmpty() && !defaultValue.equals("null")) {
                        colDef.append(" DEFAULT ").append(defaultValue);
                    }

                    // Добавляем комментарий к колонке
                    if (comment != null && !comment.trim().isEmpty()) {
                        String cleanComment = comment.replace("\n", " ").replace("\r", " ").trim();
                        colDef.append("  -- ").append(cleanComment);
                    }

                    columns.add(colDef.toString());
                }
            }

            // Получаем первичный ключ
            String pkSql = "SELECT kcu.column_name " +
                    "FROM information_schema.table_constraints tc " +
                    "JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name " +
                    "WHERE tc.table_name = ? AND tc.constraint_type = 'PRIMARY KEY' " +
                    "ORDER BY kcu.ordinal_position";

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

            String constraints = getTableConstraints(tableName);
            if (constraints != null && !constraints.isEmpty()) {
                ddl.append("\n").append(constraints);
            }

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
    /**
     * Получить количество записей в таблице/вьюхе PostgreSQL
     * @param objectName имя таблицы или представления
     * @return количество записей, -1 в случае ошибки
     */
    public long getTableCount(String objectName) {
        String sql = "SELECT COUNT(*) FROM " + objectName;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(30);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка подсчёта записей в PostgreSQL " + objectName + ": " + e.getMessage());
        }
        return -1;
    }
    public String getFunctionBody(String functionName) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT pg_get_functiondef(p.oid) as funcdef " +
                    "FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE n.nspname || '.' || p.proname = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, functionName.toLowerCase());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("funcdef");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения тела функции " + functionName + " из PostgreSQL: " + e.getMessage());
        }
        return null;
    }
    public String getTableConstraints(String tableName) {
        StringBuilder sb = new StringBuilder();

        String sql =
                "SELECT con.conname AS constraint_name,\n" +
                        "       con.contype,\n" +
                        "       a.attname AS column_name,\n" +
                        "       cl2.relname AS ref_table_name,\n" +
                        "       a2.attname AS ref_column_name,\n" +
                        "       pg_get_constraintdef(con.oid) AS constraint_def\n" +
                        "FROM pg_constraint con\n" +
                        "JOIN pg_class cl ON con.conrelid = cl.oid\n" +
                        "LEFT JOIN pg_attribute a ON a.attrelid = cl.oid AND a.attnum = ANY(con.conkey)\n" +
                        "LEFT JOIN pg_class cl2 ON con.confrelid = cl2.oid\n" +
                        "LEFT JOIN pg_attribute a2 ON a2.attrelid = cl2.oid AND a2.attnum = ANY(con.confkey)\n" +
                        "WHERE cl.relname = ? AND con.contype IN ('f', 'c', 'u')\n" +
                        "ORDER BY con.conname";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tableName.toLowerCase());
            ResultSet rs = pstmt.executeQuery();

            Map<String, List<String>> fkColumns = new LinkedHashMap<>();
            Map<String, String> fkRefs = new LinkedHashMap<>();
            Set<String> uniqueConstraints = new LinkedHashSet<>();
            Map<String, String> checkConstraints = new LinkedHashMap<>();

            while (rs.next()) {
                String constrName = rs.getString("constraint_name");
                String constrType = rs.getString("contype");
                String columnName = rs.getString("column_name");
                String constraintDef = rs.getString("constraint_def");

                if ("f".equals(constrType)) {
                    String refTable = rs.getString("ref_table_name");
                    String refColumn = rs.getString("ref_column_name");
                    fkColumns.computeIfAbsent(constrName, k -> new ArrayList<>()).add(columnName);
                    if (refTable != null && refColumn != null) {
                        fkRefs.put(constrName, refTable + "(" + refColumn + ")");
                    }
                } else if ("u".equals(constrType)) {
                    uniqueConstraints.add(constrName + "(" + columnName + ")");
                } else if ("c".equals(constrType) && constraintDef != null) {
                    checkConstraints.put(constrName, constraintDef);
                }
            }

            // Вывод FOREIGN KEY
            for (Map.Entry<String, List<String>> entry : fkColumns.entrySet()) {
                String name = entry.getKey();
                String columns = String.join(", ", entry.getValue());
                String ref = fkRefs.get(name);
                sb.append("ALTER TABLE ").append(tableName).append(" ADD CONSTRAINT ").append(name)
                        .append(" FOREIGN KEY (").append(columns).append(") REFERENCES ").append(ref).append(";\n");
            }

            // Вывод UNIQUE
            for (String uk : uniqueConstraints) {
                sb.append("ALTER TABLE ").append(tableName).append(" ADD CONSTRAINT ").append(uk).append(";\n");
            }

            // Вывод CHECK
            for (Map.Entry<String, String> entry : checkConstraints.entrySet()) {
                sb.append("ALTER TABLE ").append(tableName).append(" ADD CONSTRAINT ").append(entry.getKey())
                        .append(" CHECK (").append(entry.getValue()).append(");\n");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения констрейнтов для " + tableName + ": " + e.getMessage());
        }

        return sb.toString();
    }
}