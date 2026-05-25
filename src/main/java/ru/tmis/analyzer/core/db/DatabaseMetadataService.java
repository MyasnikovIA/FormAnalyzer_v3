// core/db/DatabaseMetadataService.java
package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;

import java.sql.*;
import java.util.*;

public class DatabaseMetadataService {

    private final SettingsModel settings;

    public DatabaseMetadataService(SettingsModel settings) {
        this.settings = settings;
    }

    /**
     * Получить значение константы из D_PKG_CONSTANTS
     */
    public String getConstantValue(String constCode) {
        return DatabaseCacheManager.getConstant(constCode, () -> {
            String sql = "SELECT const_value FROM D_PKG_CONSTANTS WHERE const_code = ?";
            try (Connection conn = getOracleConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, constCode);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("const_value");
                }
            } catch (SQLException e) {
                System.err.println("Ошибка получения константы " + constCode + ": " + e.getMessage());
            }
            return null;
        });
    }

    /**
     * Получить значение системной опции
     */
    public String getSystemOptionValue(String optionCode) {
        return DatabaseCacheManager.getSystemOption(optionCode, () -> {
            String sql = "SELECT option_value FROM D_SYSTEM_OPTIONS WHERE option_code = ?";
            try (Connection conn = getOracleConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, optionCode);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("option_value");
                }
            } catch (SQLException e) {
                System.err.println("Ошибка получения опции " + optionCode + ": " + e.getMessage());
            }
            return null;
        });
    }

    /**
     * Получить список колонок таблицы
     */
    public List<DatabaseCacheManager.ColumnInfo> getTableColumns(String tableName) {
        return DatabaseCacheManager.getTableColumns(tableName, () -> {
            List<DatabaseCacheManager.ColumnInfo> columns = new ArrayList<>();

            // ИСПРАВЛЕННЫЙ SQL - убираем COMMENTS из ALL_TAB_COLUMNS
            String sql = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, " +
                    "DATA_SCALE, NULLABLE, DATA_DEFAULT " +
                    "FROM ALL_TAB_COLUMNS WHERE OWNER = ? AND TABLE_NAME = ? " +
                    "ORDER BY COLUMN_ID";

            try (Connection conn = getOracleConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, settings.getOracleUser().toUpperCase());
                pstmt.setString(2, tableName.toUpperCase());
                ResultSet rs = pstmt.executeQuery();

                // Отдельно получаем комментарии
                Map<String, String> comments = getColumnComments(tableName);

                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    String comment = comments.get(columnName);

                    columns.add(new DatabaseCacheManager.ColumnInfo(
                            columnName,
                            rs.getString("DATA_TYPE"),
                            rs.getInt("DATA_LENGTH"),
                            rs.getInt("DATA_PRECISION") > 0 ? rs.getInt("DATA_PRECISION") : null,
                            rs.getInt("DATA_SCALE") > 0 ? rs.getInt("DATA_SCALE") : null,
                            "N".equals(rs.getString("NULLABLE")),
                            rs.getString("DATA_DEFAULT"),
                            comment  // Используем комментарий из отдельного запроса
                    ));
                }
            } catch (SQLException e) {
                System.err.println("Ошибка получения колонок таблицы " + tableName + ": " + e.getMessage());
            }
            return columns;
        });
    }

    /**
     * Получить комментарии к колонкам таблицы
     */
    private Map<String, String> getColumnComments(String tableName) {
        Map<String, String> comments = new HashMap<>();

        String sql = "SELECT COLUMN_NAME, COMMENTS FROM ALL_COL_COMMENTS " +
                "WHERE OWNER = ? AND TABLE_NAME = ? AND COMMENTS IS NOT NULL";

        try (Connection conn = getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, settings.getOracleUser().toUpperCase());
            pstmt.setString(2, tableName.toUpperCase());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                comments.put(rs.getString("COLUMN_NAME"), rs.getString("COMMENTS"));
            }
        } catch (SQLException e) {
            // Комментарии не критичны, просто логируем
            System.err.println("Ошибка получения комментариев для таблицы " + tableName + ": " + e.getMessage());
        }

        return comments;
    }

    /**
     * Получить внешние ключи таблицы
     */
    public List<DatabaseCacheManager.ForeignKeyInfo> getForeignKeys(String tableName) {
        return DatabaseCacheManager.getForeignKeys(tableName, () -> {
            List<DatabaseCacheManager.ForeignKeyInfo> fks = new ArrayList<>();
            String sql = "SELECT CONSTRAINT_NAME, R_CONSTRAINT_NAME, DELETE_RULE " +
                    "FROM ALL_CONSTRAINTS WHERE OWNER = ? AND TABLE_NAME = ? " +
                    "AND CONSTRAINT_TYPE = 'R'";

            try (Connection conn = getOracleConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, settings.getOracleUser().toUpperCase());
                pstmt.setString(2, tableName.toUpperCase());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String constraintName = rs.getString("CONSTRAINT_NAME");
                    String referencedConstraint = rs.getString("R_CONSTRAINT_NAME");
                    String deleteRule = rs.getString("DELETE_RULE");

                    // Получаем колонки для этого FK
                    String colSql = "SELECT COLUMN_NAME, POSITION FROM ALL_CONS_COLUMNS " +
                            "WHERE OWNER = ? AND CONSTRAINT_NAME = ? ORDER BY POSITION";
                    try (PreparedStatement colStmt = conn.prepareStatement(colSql)) {
                        colStmt.setString(1, settings.getOracleUser().toUpperCase());
                        colStmt.setString(2, constraintName);
                        ResultSet colRs = colStmt.executeQuery();
                        while (colRs.next()) {
                            fks.add(new DatabaseCacheManager.ForeignKeyInfo(
                                    constraintName, tableName, colRs.getString("COLUMN_NAME"),
                                    null, null, deleteRule
                            ));
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Ошибка получения FK таблицы " + tableName + ": " + e.getMessage());
            }
            return fks;
        });
    }

    /**
     * Получить индексы таблицы
     */
    public List<DatabaseCacheManager.IndexInfo> getIndexes(String tableName) {
        return DatabaseCacheManager.getIndexes(tableName, () -> {
            List<DatabaseCacheManager.IndexInfo> indexes = new ArrayList<>();
            String sql = "SELECT INDEX_NAME, UNIQUENESS, INDEX_TYPE " +
                    "FROM ALL_INDEXES WHERE OWNER = ? AND TABLE_NAME = ?";

            try (Connection conn = getOracleConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, settings.getOracleUser().toUpperCase());
                pstmt.setString(2, tableName.toUpperCase());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    boolean unique = "UNIQUE".equals(rs.getString("UNIQUENESS"));
                    String indexType = rs.getString("INDEX_TYPE");

                    String colSql = "SELECT COLUMN_NAME, COLUMN_POSITION FROM ALL_IND_COLUMNS " +
                            "WHERE INDEX_OWNER = ? AND INDEX_NAME = ? ORDER BY COLUMN_POSITION";
                    try (PreparedStatement colStmt = conn.prepareStatement(colSql)) {
                        colStmt.setString(1, settings.getOracleUser().toUpperCase());
                        colStmt.setString(2, indexName);
                        ResultSet colRs = colStmt.executeQuery();
                        while (colRs.next()) {
                            indexes.add(new DatabaseCacheManager.IndexInfo(
                                    indexName, colRs.getString("COLUMN_NAME"),
                                    unique, indexType, colRs.getInt("COLUMN_POSITION")
                            ));
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Ошибка получения индексов таблицы " + tableName + ": " + e.getMessage());
            }
            return indexes;
        });
    }

    private Connection getOracleConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", settings.getOracleUser());
        props.setProperty("password", settings.getOraclePassword());
        props.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
        props.setProperty("oracle.jdbc.ReadTimeout", "30000");
        return DriverManager.getConnection(settings.getOracleUrl(), props);
    }
}