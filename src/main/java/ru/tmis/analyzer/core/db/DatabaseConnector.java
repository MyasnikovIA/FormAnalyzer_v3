// core/db/DatabaseConnector.java
package ru.tmis.analyzer.core.db;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnector {

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;

    /**
     * Получить соединение с Oracle из пула
     */
    public static Connection getOracleConnection(String url, String user, String password) throws SQLException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle JDBC driver not found", e);
        }
        // Используем пул
        return DatabaseConnectionManager.getOracleConnection();
    }

    /**
     * Получить соединение с PostgreSQL из пула
     */
    public static Connection getPostgresConnection(String url, String user, String password) throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC driver not found", e);
        }
        // Используем пул
        Connection conn = DatabaseConnectionManager.getPostgresConnection();
        conn.setAutoCommit(true);
        return conn;
    }

    public static Connection getPostgresConnectionWithContext(String url, String user, String password, String misUser) throws SQLException {
        Connection conn = getPostgresConnection(url, user, password);

        if (misUser != null && !misUser.trim().isEmpty()) {
            try (var stmt = conn.createStatement()) {
                stmt.execute("SELECT set_config('mis.user', '" + misUser + "', false)");
            } catch (SQLException e) {
                System.err.println("  [PostgreSQL] Ошибка установки контекста: " + e.getMessage());
            }
        }

        return conn;
    }
}