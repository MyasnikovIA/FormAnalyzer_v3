// core/db/DatabaseConnectionManager.java
package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.utils.NetworkUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseConnectionManager {



    private static final ThreadLocal<Connection> oracleConnectionHolder = new ThreadLocal<>();
    private static final ThreadLocal<Connection> postgresConnectionHolder = new ThreadLocal<>();

    // Флаги доступности сети
    private static volatile boolean oracleNetworkAvailable = false;
    private static volatile boolean postgresNetworkAvailable = false;

    private static volatile ConnectionPool oraclePool;
    private static volatile ConnectionPool postgresPool;

    // ThreadLocal соединения для каждого потока
    private static final ThreadLocal<Connection> oracleThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Connection> postgresThreadLocal = new ThreadLocal<>();

    private static volatile boolean initialized = false;
    private static ScheduledExecutorService statsScheduler;


    public static synchronized void init(String oracleUrl, String oracleUser, String oraclePassword,
                                         String postgresUrl, String postgresUser, String postgresPassword,
                                         String misUser) {
        if (initialized) return;

        int poolSize = Math.max(5, Runtime.getRuntime().availableProcessors());

        // ========== СНАЧАЛА ПРОВЕРЯЕМ СЕТЬ ==========
        if (oracleUrl != null && !oracleUrl.isEmpty() &&
                oracleUser != null && !oracleUser.isEmpty()) {
            oracleNetworkAvailable = NetworkUtils.isDatabaseServerAvailable(oracleUrl);
            if (oracleNetworkAvailable) {
                oraclePool = new ConnectionPool(oracleUrl, oracleUser, oraclePassword, poolSize, false);
                System.out.println("[DB] Oracle пул инициализирован, размер: " + poolSize);
            } else {
                System.out.println("[DB] Oracle сервер НЕДОСТУПЕН по сети, пул НЕ создан");
            }
        }

        if (postgresUrl != null && !postgresUrl.isEmpty() &&
                postgresUser != null && !postgresUser.isEmpty()) {
            postgresNetworkAvailable = NetworkUtils.isDatabaseServerAvailable(postgresUrl);
            if (postgresNetworkAvailable) {
                postgresPool = new ConnectionPool(postgresUrl, postgresUser, postgresPassword, poolSize, false);
                System.out.println("[DB] PostgreSQL пул инициализирован, размер: " + poolSize);
            } else {
                System.out.println("[DB] PostgreSQL сервер НЕДОСТУПЕН по сети, пул НЕ создан");
            }
        }

        initialized = true;
        startStatsLogging(30);
    }

    public static Connection getOracleConnection() throws SQLException {
        if (oraclePool == null) {
            throw new SQLException("Oracle pool not initialized - server unreachable");
        }
        return oraclePool.getConnection();
    }

    public static Connection getPostgresConnection() throws SQLException {
        if (postgresPool == null) {
            throw new SQLException("PostgreSQL pool not initialized - server unreachable");
        }
        return postgresPool.getConnection();
    }

    public static boolean isOracleNetworkAvailable() {
        return oracleNetworkAvailable;
    }

    public static boolean isPostgresNetworkAvailable() {
        return postgresNetworkAvailable;
    }

    public static void shutdown() {
        if (oraclePool != null) oraclePool.close();
        if (postgresPool != null) postgresPool.close();
        if (statsScheduler != null && !statsScheduler.isShutdown()) {
            statsScheduler.shutdown();
        }
    }

    // Внутренний класс пула
    private static class ConnectionPool {
        private final BlockingQueue<Connection> pool;
        private final String url;
        private final String user;
        private final String password;
        private final AtomicInteger activeCount = new AtomicInteger(0);
        private volatile boolean closed = false;

        public ConnectionPool(String url, String user, String password, int maxSize, boolean preCreate) {
            this.url = url;
            this.user = user;
            this.password = password;
            this.pool = new LinkedBlockingQueue<>(maxSize);

            if (preCreate) {
                for (int i = 0; i < Math.min(maxSize, 3); i++) {
                    try {
                        pool.offer(createConnection());
                    } catch (SQLException e) {
                        System.err.println("[ConnectionPool] Ошибка создания: " + e.getMessage());
                    }
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

        // ========== ДОБАВЬТЕ ЭТИ МЕТОДЫ ==========
        public int getActiveCount() {
            return activeCount.get();
        }

        public int getPoolSize() {
            return pool.size();
        }
    }

    /**
     * Выводит статистику пулов соединений в лог
     */
    public static void printStats() {
        if (oraclePool != null) {
            System.out.println("[DB Pool] Oracle: активных=" + oraclePool.getActiveCount() +
                    ", в пуле=" + oraclePool.getPoolSize() +
                    ", всего=" + (oraclePool.getActiveCount() + oraclePool.getPoolSize()));
        } else {
            System.out.println("[DB Pool] Oracle: пул не инициализирован");
        }

        if (postgresPool != null) {
            System.out.println("[DB Pool] PostgreSQL: активных=" + postgresPool.getActiveCount() +
                    ", в пуле=" + postgresPool.getPoolSize() +
                    ", всего=" + (postgresPool.getActiveCount() + postgresPool.getPoolSize()));
        } else {
            System.out.println("[DB Pool] PostgreSQL: пул не инициализирован");
        }
    }

    /**
     * Периодический вывод статистики (каждые N секунд)
     */

    /**
     * Периодический вывод статистики (каждые N секунд)
     */
    public static void startStatsLogging(int intervalSeconds) {
        if (statsScheduler != null && !statsScheduler.isShutdown()) {
            return;
        }
        statsScheduler = Executors.newSingleThreadScheduledExecutor();
        statsScheduler.scheduleAtFixedRate(() -> {
            if (oraclePool != null || postgresPool != null) {
                System.out.println("--- СТАТИСТИКА ПУЛОВ СОЕДИНЕНИЙ ---");
                printStats();
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    public static Connection getOracleConnectionForCurrentThread() throws SQLException {
        Connection conn = oracleConnectionHolder.get();
        if (conn == null || conn.isClosed()) {
            conn = oraclePool.getConnection();
            oracleConnectionHolder.set(conn);
        }
        return conn;
    }


    /**
     * Получить Oracle соединение для текущего потока (одно на поток)
     */
    public static Connection getOracleConnectionForThread() throws SQLException {
        if (oraclePool == null) {
            throw new SQLException("Oracle pool not initialized");
        }

        Connection conn = oracleThreadLocal.get();
        if (conn == null || conn.isClosed()) {
            conn = oraclePool.getConnection();
            oracleThreadLocal.set(conn);
            System.out.println("[DB] Поток " + Thread.currentThread().threadId() +
                    " получил Oracle соединение");
        }
        return conn;
    }

    /**
     * Получить PostgreSQL соединение для текущего потока (одно на поток)
     */
    public static Connection getPostgresConnectionForThread() throws SQLException {
        if (postgresPool == null) {
            throw new SQLException("PostgreSQL pool not initialized");
        }

        Connection conn = postgresThreadLocal.get();
        if (conn == null || conn.isClosed()) {
            conn = postgresPool.getConnection();
            postgresThreadLocal.set(conn);
            System.out.println("[DB] Поток " + Thread.currentThread().threadId() +
                    " получил PostgreSQL соединение");
        }
        return conn;
    }

    /**
     * Закрыть все соединения текущего потока
     */
    public static void closeThreadConnections() {
        try {
            Connection conn = oracleThreadLocal.get();
            if (conn != null && !conn.isClosed()) {
                conn.close();
                oracleThreadLocal.remove();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка закрытия Oracle соединения: " + e.getMessage());
        }

        try {
            Connection conn = postgresThreadLocal.get();
            if (conn != null && !conn.isClosed()) {
                conn.close();
                postgresThreadLocal.remove();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка закрытия PostgreSQL соединения: " + e.getMessage());
        }
    }

}