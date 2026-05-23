package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;

import java.sql.*;
import java.util.*;

public class DatabaseObjectChecker {

    private final SettingsModel settings;

    public DatabaseObjectChecker(SettingsModel settings) {
        this.settings = settings;
    }

    // ==================== PK CHECK METHODS ====================

    public static class PrimaryKeyInfo {
        // ... без изменений ...
        private final String tableName;
        private final boolean hasPKInOracle;
        private final boolean hasPKInPostgres;
        private final List<String> oracleColumns;
        private final List<String> postgresColumns;
        private final String oracleConstraintName;
        private final String postgresConstraintName;

        public PrimaryKeyInfo(String tableName, boolean hasPKInOracle, boolean hasPKInPostgres,
                              List<String> oracleColumns, List<String> postgresColumns,
                              String oracleConstraintName, String postgresConstraintName) {
            this.tableName = tableName;
            this.hasPKInOracle = hasPKInOracle;
            this.hasPKInPostgres = hasPKInPostgres;
            this.oracleColumns = oracleColumns;
            this.postgresColumns = postgresColumns;
            this.oracleConstraintName = oracleConstraintName;
            this.postgresConstraintName = postgresConstraintName;
        }

        public String getTableName() { return tableName; }
        public boolean hasPKInOracle() { return hasPKInOracle; }
        public boolean hasPKInPostgres() { return hasPKInPostgres; }
        public List<String> getOracleColumns() { return oracleColumns; }
        public List<String> getPostgresColumns() { return postgresColumns; }
        public String getOracleConstraintName() { return oracleConstraintName; }
        public String getPostgresConstraintName() { return postgresConstraintName; }
        public boolean isMatch() { return hasPKInOracle == hasPKInPostgres; }

        public boolean isColumnsMatch() {
            if (!hasPKInOracle || !hasPKInPostgres) return false;
            if (oracleColumns.size() != postgresColumns.size()) return false;
            for (int i = 0; i < oracleColumns.size(); i++) {
                if (!oracleColumns.get(i).equalsIgnoreCase(postgresColumns.get(i))) return false;
            }
            return true;
        }

        public String getStatus() {
            if (hasPKInOracle && !hasPKInPostgres) return "ОШИБКА: PK есть в Oracle, но отсутствует в PostgreSQL";
            if (!hasPKInOracle && hasPKInPostgres) return "ПРЕДУПРЕЖДЕНИЕ: PK есть в PostgreSQL, но отсутствует в Oracle";
            if (hasPKInOracle && hasPKInPostgres) {
                if (isColumnsMatch()) return "OK (PK присутствует, поля совпадают)";
                else return "ПРЕДУПРЕЖДЕНИЕ: PK присутствует, но поля различаются";
            }
            return "INFO (PK отсутствует в обеих БД)";
        }
    }

    /**
     * Получение соединения с Oracle через пул
     */
    private Connection getOracleConnection() throws SQLException {
        return DatabaseConnector.getOracleConnection(
                settings.getOracleUrl(),
                settings.getOracleUser(),
                settings.getOraclePassword()
        );
    }

    /**
     * Получение соединения с PostgreSQL через пул с контекстом МИС
     */
    private Connection getPostgresConnection() throws SQLException {
        // Используем DatabaseConnector с контекстом МИС
        return DatabaseConnector.getPostgresConnectionWithContext(
                settings.getPostgresUrl(),
                settings.getPostgresUser(),
                settings.getPostgresPassword(),
                settings.getMisUser()
        );
    }

    public PrimaryKeyInfo checkPrimaryKey(String tableName) {
        return DatabaseCacheManager.getPrimaryKeyInfo(tableName, () -> {
            System.out.println("      Oracle PK query for " + tableName + "...");
            boolean oraclePK = false;
            boolean postgresPK = false;
            List<String> oracleCols = new ArrayList<>();
            List<String> postgresCols = new ArrayList<>();
            String oracleConstraintName = null;
            String postgresConstraintName = null;

            // Oracle
            String oracleSql = "SELECT c.CONSTRAINT_NAME, col.COLUMN_NAME " +
                    "FROM ALL_CONSTRAINTS c JOIN ALL_CONS_COLUMNS col ON c.CONSTRAINT_NAME = col.CONSTRAINT_NAME " +
                    "WHERE c.CONSTRAINT_TYPE = 'P' AND c.OWNER = ? AND c.TABLE_NAME = ? ORDER BY col.POSITION";
            try (Connection conn = getOracleConnection();
                 PreparedStatement pstmt = conn.prepareStatement(oracleSql)) {
                pstmt.setString(1, settings.getOracleUser().toUpperCase());
                pstmt.setString(2, tableName.toUpperCase());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    oraclePK = true;
                    oracleConstraintName = rs.getString("CONSTRAINT_NAME");
                    oracleCols.add(rs.getString("COLUMN_NAME"));
                }
            } catch (SQLException e) {
                System.err.println("Oracle PK error for " + tableName + ": " + e.getMessage());
            }

            // PostgreSQL
            String postgresSql = "SELECT tc.constraint_name, kcu.column_name " +
                    "FROM information_schema.table_constraints tc " +
                    "JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name " +
                    "WHERE tc.constraint_type = 'PRIMARY KEY' AND tc.table_schema = 'public' AND tc.table_name = ? " +
                    "ORDER BY kcu.ordinal_position";
            try (Connection conn = getPostgresConnection();
                 PreparedStatement pstmt = conn.prepareStatement(postgresSql)) {
                pstmt.setString(1, tableName.toLowerCase());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    postgresPK = true;
                    postgresConstraintName = rs.getString("constraint_name");
                    postgresCols.add(rs.getString("column_name"));
                }
            } catch (SQLException e) {
                System.err.println("PostgreSQL PK error for " + tableName + ": " + e.getMessage());
            }
            System.out.println("      PostgreSQL PK query for " + tableName + "...");
            return new PrimaryKeyInfo(tableName, oraclePK, postgresPK, oracleCols, postgresCols,
                    oracleConstraintName, postgresConstraintName);
        });
    }

    // ==================== NOT NULL CHECK METHODS ====================

    public static class NotNullConstraintInfo {
        private final String tableName;
        private final String columnName;
        private final boolean isNotNullInOracle;
        private final boolean isNotNullInPostgres;

        public NotNullConstraintInfo(String tableName, String columnName, boolean isNotNullInOracle, boolean isNotNullInPostgres) {
            this.tableName = tableName;
            this.columnName = columnName;
            this.isNotNullInOracle = isNotNullInOracle;
            this.isNotNullInPostgres = isNotNullInPostgres;
        }

        public String getTableName() { return tableName; }
        public String getColumnName() { return columnName; }
        public boolean isNotNullInOracle() { return isNotNullInOracle; }
        public boolean isNotNullInPostgres() { return isNotNullInPostgres; }

        public boolean isMatch() { return isNotNullInOracle == isNotNullInPostgres; }

        public String getStatus() {
            if (isNotNullInOracle && !isNotNullInPostgres)
                return "ОШИБКА: В Oracle NOT NULL, в PostgreSQL NULL разрешен";
            if (!isNotNullInOracle && isNotNullInPostgres)
                return "ПРЕДУПРЕЖДЕНИЕ: В Oracle NULL разрешен, в PostgreSQL NOT NULL";
            return "OK";
        }

        public String getRecommendation() {
            if (isNotNullInOracle && !isNotNullInPostgres) {
                return "ALTER TABLE " + tableName.toLowerCase() + " ALTER COLUMN " + columnName.toLowerCase() + " SET NOT NULL;";
            } else if (!isNotNullInOracle && isNotNullInPostgres) {
                return "ALTER TABLE " + tableName.toLowerCase() + " ALTER COLUMN " + columnName.toLowerCase() + " DROP NOT NULL;";
            }
            return null;
        }
    }

    public List<NotNullConstraintInfo> checkNotNullConstraints(String tableName) {
        return DatabaseCacheManager.getNotNullConstraints(tableName, () -> {
            List<NotNullConstraintInfo> result = new ArrayList<>();
            Map<String, Boolean> oracleNotNull = new HashMap<>();
            Map<String, Boolean> postgresNotNull = new HashMap<>();

            // Oracle
            String oracleSql = "SELECT COLUMN_NAME, NULLABLE FROM ALL_TAB_COLUMNS WHERE OWNER = ? AND TABLE_NAME = ?";
            try (Connection conn = getOracleConnection();
                 PreparedStatement pstmt = conn.prepareStatement(oracleSql)) {
                pstmt.setString(1, settings.getOracleUser().toUpperCase());
                pstmt.setString(2, tableName.toUpperCase());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String col = rs.getString("COLUMN_NAME");
                    boolean notNull = "N".equals(rs.getString("NULLABLE"));
                    oracleNotNull.put(col, notNull);
                }
            } catch (SQLException e) {
                System.err.println("Oracle NOT NULL error for " + tableName + ": " + e.getMessage());
            }

            // PostgreSQL
            String postgresSql = "SELECT column_name, is_nullable FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ?";
            try (Connection conn = getPostgresConnection();
                 PreparedStatement pstmt = conn.prepareStatement(postgresSql)) {
                pstmt.setString(1, tableName.toLowerCase());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String col = rs.getString("column_name");
                    boolean notNull = "NO".equalsIgnoreCase(rs.getString("is_nullable"));
                    postgresNotNull.put(col, notNull);
                }
            } catch (SQLException e) {
                System.err.println("PostgreSQL NOT NULL error for " + tableName + ": " + e.getMessage());
            }

            // Объединяем все колонки из Oracle и PostgreSQL
            Set<String> allColumns = new LinkedHashSet<>();
            allColumns.addAll(oracleNotNull.keySet());
            allColumns.addAll(postgresNotNull.keySet());
            for (String col : allColumns) {
                boolean oracleNN = oracleNotNull.getOrDefault(col, false);
                boolean pgNN = postgresNotNull.getOrDefault(col, false);
                result.add(new NotNullConstraintInfo(tableName, col, oracleNN, pgNN));
            }
            return result;
        });
    }
}