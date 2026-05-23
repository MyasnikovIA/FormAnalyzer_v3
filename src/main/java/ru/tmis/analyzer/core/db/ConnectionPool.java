// core/db/ConnectionPool.java
package ru.tmis.analyzer.core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPool {

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

        // Предварительное создание соединений
        for (int i = 0; i < Math.min(maxSize, 5); i++) {
            try {
                pool.offer(createConnection());
                System.out.println("[ConnectionPool] Создано соединение " + (i + 1));
            } catch (SQLException e) {
                System.err.println("[ConnectionPool] Ошибка создания соединения: " + e.getMessage());
            }
        }
    }

    private Connection createConnection() throws SQLException {
        activeCount.incrementAndGet();
        return DriverManager.getConnection(url, user, password);
    }

    public Connection getConnection() throws SQLException {
        if (closed) {
            throw new SQLException("Connection pool is closed");
        }

        try {
            Connection conn = pool.poll(30, TimeUnit.SECONDS);
            if (conn == null) {
                // Создаём новое, если пул пуст
                conn = createConnection();
            } else if (conn.isClosed()) {
                conn = createConnection();
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection", e);
        }
    }

    public void releaseConnection(Connection conn) {
        if (conn != null && !closed) {
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
    }

    public void close() {
        closed = true;
        Connection conn;
        while ((conn = pool.poll()) != null) {
            try {
                conn.close();
                activeCount.decrementAndGet();
            } catch (SQLException ignored) {}
        }
    }

    public int getActiveCount() { return activeCount.get(); }
    public int getPoolSize() { return pool.size(); }
}