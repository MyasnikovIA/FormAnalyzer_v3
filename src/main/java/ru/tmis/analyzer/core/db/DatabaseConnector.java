// core/db/DatabaseConnector.java
package ru.tmis.analyzer.core.db;

import java.sql.*;
import java.util.Properties;

public class DatabaseConnector {

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;

    public static Connection getOracleConnection(String url, String user, String password) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("oracle.net.CONNECT_TIMEOUT", String.valueOf(CONNECT_TIMEOUT));
        props.setProperty("oracle.jdbc.ReadTimeout", String.valueOf(READ_TIMEOUT));

        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle JDBC driver not found", e);
        }

        return DriverManager.getConnection(url, props);
    }

    public static Connection getPostgresConnection(String url, String user, String password) throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC driver not found", e);
        }

        DriverManager.setLoginTimeout(10);
        Connection conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(true);

        return conn;
    }

    public static Connection getPostgresConnectionWithContext(String url, String user, String password, String misUser) throws SQLException {
        Connection conn = getPostgresConnection(url, user, password);

        if (misUser != null && !misUser.trim().isEmpty()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT set_config('mis.user', '" + misUser + "', false)");
            } catch (SQLException e) {
                System.err.println("  [PostgreSQL] Ошибка установки контекста: " + e.getMessage());
            }
        }

        return conn;
    }
}