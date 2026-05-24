package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.core.cache.DatabaseCacheManager;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        String key = viewName.toUpperCase();
        return DatabaseCacheManager.getOracleViewDDL(key, () -> fetchViewDDL(viewName));
    }

    public String getTableDDL(String tableName) {
        String key = tableName.toUpperCase();
        return DatabaseCacheManager.getOracleTableDDL(key, () -> fetchTableDDL(tableName));
    }

    public String getFunctionBody(String packageName, String functionName) {
        String key = (packageName + "." + functionName).toUpperCase();
        return DatabaseCacheManager.getOracleFunctionBody(key, () -> fetchFunctionBody(packageName, functionName));
    }

    public long getTableCount(String objectName) {
        String key = objectName.toUpperCase();
        return DatabaseCacheManager.getOracleCount(key, () -> fetchTableCount(objectName));
    }

    // ==================== РЕАЛЬНЫЕ ЗАПРОСЫ К БД ====================

    private String fetchViewDDL(String viewName) {
        String sql = "SELECT TEXT FROM ALL_VIEWS WHERE VIEW_NAME = ?";

        System.out.println("[OracleService] ========== SQL ЗАПРОС ==========");
        System.out.println("[OracleService] Цель: Получение DDL вьюхи");
        System.out.println("[OracleService] Параметры: viewName = " + viewName);
        System.out.println("[OracleService] SQL: " + sql.replace("?", "'" + viewName.toUpperCase() + "'"));
        System.out.println("[OracleService] ==================================");

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

    private String fetchTableDDL(String tableName) {
        StringBuilder ddl = new StringBuilder();
        try (Connection conn = DatabaseConnector.getOracleConnection(url, user, password)) {
            // Получаем колонки
            String columnsSql = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, DATA_SCALE, NULLABLE " +
                    "FROM ALL_TAB_COLUMNS WHERE OWNER = ? AND TABLE_NAME = ? ORDER BY COLUMN_ID";
            List<String> columns = new ArrayList<>();
            Map<String, String> columnComments = new LinkedHashMap<>();

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

            // Получаем комментарии к колонкам
            String commentsSql = "SELECT COLUMN_NAME, COMMENTS FROM ALL_COL_COMMENTS " +
                    "WHERE OWNER = ? AND TABLE_NAME = ? AND COMMENTS IS NOT NULL";
            try (PreparedStatement pstmt = conn.prepareStatement(commentsSql)) {
                pstmt.setString(1, user.toUpperCase());
                pstmt.setString(2, tableName.toUpperCase());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    columnComments.put(rs.getString("COLUMN_NAME"), rs.getString("COMMENTS"));
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

            // Добавляем комментарии к колонкам
            if (!columnComments.isEmpty()) {
                ddl.append("\n-- Комментарии к колонкам:\n");
                for (Map.Entry<String, String> entry : columnComments.entrySet()) {
                    String comment = entry.getValue().replace("'", "''");
                    ddl.append("COMMENT ON COLUMN ").append(tableName).append(".").append(entry.getKey())
                            .append(" IS '").append(comment).append("';\n");
                }
            }

            // Добавляем комментарий к таблице
            String tableCommentSql = "SELECT COMMENTS FROM ALL_TAB_COMMENTS " +
                    "WHERE OWNER = ? AND TABLE_NAME = ? AND COMMENTS IS NOT NULL";
            try (PreparedStatement pstmt = conn.prepareStatement(tableCommentSql)) {
                pstmt.setString(1, user.toUpperCase());
                pstmt.setString(2, tableName.toUpperCase());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String tableComment = rs.getString("COMMENTS").replace("'", "''");
                    ddl.append("\nCOMMENT ON TABLE ").append(tableName).append(" IS '").append(tableComment).append("';\n");
                }
            }

            return ddl.toString();
        } catch (SQLException e) {
            System.err.println("Ошибка получения DDL таблицы " + tableName + ": " + e.getMessage());
            return null;
        }
    }

    private String fetchFunctionBody(String packageName, String functionName) {
        String sql = "SELECT TEXT FROM ALL_SOURCE " +
                "WHERE OWNER = ? AND TYPE = 'PACKAGE BODY' " +
                "AND NAME = UPPER(?) ORDER BY LINE";
        try (Connection conn = DatabaseConnector.getOracleConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.toUpperCase());
            pstmt.setString(2, packageName.toUpperCase());
            pstmt.setQueryTimeout(30);
            ResultSet rs = pstmt.executeQuery();
            StringBuilder body = new StringBuilder();
            while (rs.next()) {
                body.append(rs.getString("TEXT"));
            }
            String fullBody = body.toString();
            if (fullBody.isEmpty()) return null;
            String pattern = "(?i)(FUNCTION|PROCEDURE)\\s+" + functionName + "\\s*\\([^)]*\\)\\s+(RETURN\\s+\\w+\\s+)?(IS|AS)\\s+(.*?)(END\\s+" + functionName + "\\s*;|END\\s*;)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher m = p.matcher(fullBody);
            if (m.find()) {
                return formatFunctionBody(m.group(0), functionName, "PACKAGE", m.group(2));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения тела функции " + packageName + "." + functionName + ": " + e.getMessage());
        }
        return null;
    }

    private long fetchTableCount(String objectName) {
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

    private String formatFunctionBody(String body, String functionName, String type, String returnType) {
        StringBuilder sb = new StringBuilder();
        sb.append("-- Oracle ").append(type).append(": ").append(functionName).append("\n");
        if (returnType != null && !returnType.isEmpty()) {
            sb.append("-- Возвращает: ").append(returnType.trim()).append("\n");
        }
        sb.append("--").append("=".repeat(70)).append("\n");
        sb.append(body);
        if (!body.endsWith("\n")) sb.append("\n");
        return sb.toString();
    }

    // ==================== МЕТОДЫ ДЛЯ КЭШИРОВАНИЯ ====================
    // Временное хранилище (позже заменим на DatabaseCacheManager)
    private static final Map<String, String> viewDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> tableDDLCache = new ConcurrentHashMap<>();
    private static final Map<String, String> functionBodyCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> tableCountCache = new ConcurrentHashMap<>();

    private String getCachedViewDDL(String key) { return viewDDLCache.get(key); }
    private void putCachedViewDDL(String key, String ddl) { viewDDLCache.put(key, ddl); }
    private String getCachedTableDDL(String key) { return tableDDLCache.get(key); }
    private void putCachedTableDDL(String key, String ddl) { tableDDLCache.put(key, ddl); }
    private String getCachedFunctionBody(String key) { return functionBodyCache.get(key); }
    private void putCachedFunctionBody(String key, String body) { functionBodyCache.put(key, body); }
    private Long getCachedTableCount(String key) { return tableCountCache.get(key); }
    private void putCachedTableCount(String key, long count) { tableCountCache.put(key, count); }

    public static void clearCache() {
        viewDDLCache.clear();
        tableDDLCache.clear();
        functionBodyCache.clear();
        tableCountCache.clear();
        System.out.println("[КЭШ] OracleService: все кэши очищены");
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

    public Connection getConnection() throws SQLException {
        return DatabaseConnector.getOracleConnection(url, user, password);
    }
}