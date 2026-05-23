// core/db/DatabaseConnectionManager.java (НОВЫЙ ФАЙЛ)
package ru.tmis.analyzer.core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseConnectionManager {

    private static volatile ConnectionPool oraclePool;
    private static volatile ConnectionPool postgresPool;
    private static volatile boolean initialized = false;

    public static synchronized void init(String oracleUrl, String oracleUser, String oraclePassword,
                                         String postgresUrl, String postgresUser, String postgresPassword,
                                         String misUser) {
        if (initialized) return;

        int poolSize = Math.max(5, Runtime.getRuntime().availableProcessors());

        if (oracleUrl != null && !oracleUrl.isEmpty() &&
                oracleUser != null && !oracleUser.isEmpty()) {
            oraclePool = new ConnectionPool(oracleUrl, oracleUser, oraclePassword, poolSize);
            System.out.println("[DB] Oracle пул инициализирован, размер: " + poolSize);
        }

        if (postgresUrl != null && !postgresUrl.isEmpty() &&
                postgresUser != null && !postgresUser.isEmpty()) {
            postgresPool = new ConnectionPool(postgresUrl, postgresUser, postgresPassword, poolSize);
            System.out.println("[DB] PostgreSQL пул инициализирован, размер: " + poolSize);
        }

        initialized = true;
    }

    public static Connection getOracleConnection() throws SQLException {
        if (oraclePool == null) {
            throw new SQLException("Oracle pool not initialized");
        }
        return oraclePool.getConnection();
    }

    public static Connection getPostgresConnection() throws SQLException {
        if (postgresPool == null) {
            throw new SQLException("PostgreSQL pool not initialized");
        }
        return postgresPool.getConnection();
    }

    public static void releaseConnection(Connection conn, String dbType) {
        if (conn == null) return;
        if ("oracle".equalsIgnoreCase(dbType) && oraclePool != null) {
            oraclePool.releaseConnection(conn);
        } else if ("postgres".equalsIgnoreCase(dbType) && postgresPool != null) {
            postgresPool.releaseConnection(conn);
        } else {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    public static void shutdown() {
        if (oraclePool != null) oraclePool.close();
        if (postgresPool != null) postgresPool.close();
    }

    // Внутренний класс пула
    private static class ConnectionPool {
        private final BlockingQueue<Connection> pool;
        private final String url;
        private final String user;
        private final String password;
        private final AtomicInteger activeCount = new AtomicInteger(0);
        private volatile boolean closed = false;

        public ConnectionPool(String url, String user, String password, int maxSize) {
            this.url = url;
            this.user = user;
            this.password = password;
            this.pool = new LinkedBlockingQueue<>(maxSize);

            // Создаём минимум соединений (2-3 штуки)
            for (int i = 0; i < Math.min(maxSize, 3); i++) {
                try {
                    pool.offer(createConnection());
                } catch (SQLException e) {
                    System.err.println("[ConnectionPool] Ошибка создания: " + e.getMessage());
                }
            }
        }

        private Connection createConnection() throws SQLException {
            activeCount.incrementAndGet();
            return DriverManager.getConnection(url, user, password);
        }

        public Connection getConnection() throws SQLException {
            if (closed) throw new SQLException("Pool closed");

            try {
                Connection conn = pool.poll(10, TimeUnit.SECONDS);
                if (conn == null || conn.isClosed()) {
                    conn = createConnection();
                }
                return conn;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("Interrupted", e);
            }
        }

        public void releaseConnection(Connection conn) {
            if (conn == null || closed) {
                try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
                if (conn != null) activeCount.decrementAndGet();
                return;
            }

            try {
                if (!conn.isClosed() && pool.remainingCapacity() > 0) {
                    pool.offer(conn);
                } else {
                    conn.close();
                    activeCount.decrementAndGet();
                }
            } catch (SQLException e) {
                try { conn.close(); } catch (SQLException ignored) {}
                activeCount.decrementAndGet();
            }
        }

        public void close() {
            closed = true;
            Connection conn;
            while ((conn = pool.poll()) != null) {
                try { conn.close(); } catch (SQLException ignored) {}
                activeCount.decrementAndGet();
            }
        }
    }
}