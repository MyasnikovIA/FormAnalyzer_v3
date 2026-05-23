package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.utils.NetworkUtils;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        return DatabaseConnector.getPostgresConnectionWithContext(
                url, user, password, misUser
        );
    }

    // ==================== ОСНОВНЫЕ МЕТОДЫ С КЭШИРОВАНИЕМ ====================

    public String getViewDDL(String viewName) {
        String key = viewName.toLowerCase();
        String cached = getCachedViewDDL(key);
        if (cached != null) return cached;

        String ddl = fetchViewDDL(viewName);
        if (ddl != null) {
            putCachedViewDDL(key, ddl);
        }
        return ddl;
    }

    public String getTableDDL(String tableName) {
        String key = tableName.toLowerCase();
        String cached = getCachedTableDDL(key);
        if (cached != null) return cached;

        String ddl = fetchTableDDL(tableName);
        if (ddl != null) {
            putCachedTableDDL(key, ddl);
        }
        return ddl;
    }

    public String getFunctionBody(String functionName) {
        String key = functionName.toLowerCase();
        String cached = getCachedFunctionBody(key);
        if (cached != null) return cached;

        String body = fetchFunctionBody(functionName);
        if (body != null) {
            putCachedFunctionBody(key, body);
        }
        return body;
    }

    public long getTableCount(String objectName) {
        String key = objectName.toLowerCase();
        Long cached = getCachedTableCount(key);
        if (cached != null) return cached;

        long count = fetchTableCount(objectName);
        putCachedTableCount(key, count);
        return count;
    }

    // ==================== РЕАЛЬНЫЕ ЗАПРОСЫ К БД ====================

    private String fetchViewDDL(String viewName) {
        // Быстрая проверка доступности сети перед запросом
        if (!DatabaseConnectionManager.isPostgresNetworkAvailable()) {
            System.out.println("[PostgresService] Сервер недоступен по сети, пропускаем запрос для " + viewName);
            return null;
        }
        try (Connection conn = getConnection()) {
            int oid = DatabaseCacheManager.getPostgresViewOid(viewName, () -> {
                String getOidSql = "SELECT oid FROM pg_class WHERE relname = ? AND relkind = 'v'";
                try (PreparedStatement oidStmt = conn.prepareStatement(getOidSql)) {
                    oidStmt.setString(1, viewName.toLowerCase());
                    ResultSet oidRs = oidStmt.executeQuery();
                    if (oidRs.next()) {
                        return oidRs.getInt("oid");
                    }
                } catch (SQLException e) {
                    System.err.println("Ошибка получения OID: " + e.getMessage());
                }
                return -1;
            });

            if (oid > 0) {
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
        } catch (SQLException e) {
            System.err.println("Ошибка получения DDL вьюхи " + viewName + ": " + e.getMessage());
        }
        return null;
    }

    private String fetchTableDDL(String tableName) {
        StringBuilder ddl = new StringBuilder();
        try (Connection conn = getConnection()) {
            String columnsSql = "SELECT column_name, data_type, character_maximum_length, " +
                    "numeric_precision, numeric_scale, is_nullable, column_default, " +
                    "pg_catalog.col_description(c.oid, cols.ordinal_position) as column_comment " +
                    "FROM information_schema.columns cols " +
                    "JOIN pg_class c ON c.relname = cols.table_name " +
                    "JOIN pg_namespace n ON n.oid = c.relnamespace AND n.nspname = cols.table_schema " +
                    "WHERE cols.table_name = ? ORDER BY cols.ordinal_position";

            List<String> columns = new ArrayList<>();
            Map<String, String> columnComments = new LinkedHashMap<>();

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
                    String comment = rs.getString("column_comment");

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
                    if (defaultValue != null && !defaultValue.isEmpty() && !defaultValue.equals("null")) {
                        colDef.append(" DEFAULT ").append(defaultValue);
                    }
                    columns.add(colDef.toString());

                    if (comment != null && !comment.isEmpty()) {
                        columnComments.put(colName, comment);
                    }
                }
            }

            String pkSql = "SELECT kcu.column_name FROM information_schema.table_constraints tc " +
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
            String tableCommentSql = "SELECT obj_description(c.oid) as table_comment " +
                    "FROM pg_class c JOIN pg_namespace n ON c.relnamespace = n.oid " +
                    "WHERE c.relname = ? AND n.nspname = current_schema()";
            try (PreparedStatement pstmt = conn.prepareStatement(tableCommentSql)) {
                pstmt.setString(1, tableName.toLowerCase());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String tableComment = rs.getString("table_comment");
                    if (tableComment != null && !tableComment.isEmpty()) {
                        ddl.append("\nCOMMENT ON TABLE ").append(tableName).append(" IS '")
                                .append(tableComment.replace("'", "''")).append("';\n");
                    }
                }
            }

            return ddl.toString();
        } catch (SQLException e) {
            System.err.println("Ошибка получения DDL таблицы " + tableName + ": " + e.getMessage());
            return null;
        }
    }

    private String fetchFunctionBody(String functionName) {
        String sql = "SELECT pg_get_functiondef(p.oid) as funcdef " +
                "FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid " +
                "WHERE LOWER(n.nspname || '.' || p.proname) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, functionName);
            pstmt.setQueryTimeout(30);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String body = rs.getString("funcdef");
                if (body != null && !body.isEmpty()) {
                    return body;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения тела функции " + functionName + " из PostgreSQL: " + e.getMessage());
        }
        return null;
    }

    private long fetchTableCount(String objectName) {
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

    // ==================== МЕТОДЫ ДЛЯ КЭШИРОВАНИЯ ====================
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
        System.out.println("[КЭШ] PostgresService: все кэши очищены");
    }

    public boolean testConnection() {
        // Сначала проверяем доступность сервера
        if (!NetworkUtils.isDatabaseServerAvailable(url)) {
            System.err.println("[PostgreSQL] Сервер недоступен, подключение не выполняется");
            return false;
        }

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