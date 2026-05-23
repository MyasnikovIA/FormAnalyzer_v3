// core/db/DatabaseAvailabilityService.java
package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.utils.NetworkUtils;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseAvailabilityService {

    private final SettingsModel settings;
    private boolean oracleAvailable = false;
    private boolean postgresAvailable = false;
    private String oracleError = null;
    private String postgresError = null;
    private boolean oracleConfigured = false;
    private boolean postgresConfigured = false;

    // Флаги доступности по сети (ping)
    private boolean oracleNetworkReachable = false;
    private boolean postgresNetworkReachable = false;

    public DatabaseAvailabilityService(SettingsModel settings) {
        this.settings = settings;
    }

    public boolean checkAllConnections() {
        oracleConfigured = isOracleConfiguredInternal();
        postgresConfigured = isPostgresConfiguredInternal();

        System.out.println("[DatabaseAvailability] Настройки подключений:");
        System.out.println("[DatabaseAvailability]   Oracle: " + (oracleConfigured ? "настроен" : "НЕ НАСТРОЕН"));
        System.out.println("[DatabaseAvailability]   PostgreSQL: " + (postgresConfigured ? "настроен" : "НЕ НАСТРОЕН"));

        // ========== СНАЧАЛА ПРОВЕРЯЕМ СЕТЬ (ПИНГ) ==========
        if (oracleConfigured) {
            oracleNetworkReachable = NetworkUtils.isDatabaseServerAvailable(settings.getOracleUrl());
            if (!oracleNetworkReachable) {
                oracleAvailable = false;
                oracleError = "Сервер недоступен по сети (не отвечает на ping/порт)";
                System.out.println("[DatabaseAvailability] Oracle: ❌ сервер НЕДОСТУПЕН по сети, подключение не требуется");
            }
        }

        if (postgresConfigured) {
            postgresNetworkReachable = NetworkUtils.isDatabaseServerAvailable(settings.getPostgresUrl());
            if (!postgresNetworkReachable) {
                postgresAvailable = false;
                postgresError = "Сервер недоступен по сети (не отвечает на ping/порт)";
                System.out.println("[DatabaseAvailability] PostgreSQL: ❌ сервер НЕДОСТУПЕН по сети, подключение не требуется");
            }
        }

        // ========== ТОЛЬКО ПОСЛЕ ПРОВЕРКИ СЕТИ ИНИЦИАЛИЗИРУЕМ ПУЛЫ ==========
        DatabaseCacheManager.initDbConfig(
                settings.getOracleUrl() != null ? settings.getOracleUrl() : "",
                settings.getOracleUser() != null ? settings.getOracleUser() : "",
                settings.getOraclePassword() != null ? settings.getOraclePassword() : "",
                settings.getPostgresUrl() != null ? settings.getPostgresUrl() : "",
                settings.getPostgresUser() != null ? settings.getPostgresUser() : "",
                settings.getPostgresPassword() != null ? settings.getPostgresPassword() : "",
                settings.getMisUser() != null ? settings.getMisUser() : ""
        );

        // ========== ПЫТАЕМСЯ ПОДКЛЮЧИТЬСЯ ТОЛЬКО ЕСЛИ СЕТЬ ДОСТУПНА ==========
        if (oracleConfigured && oracleNetworkReachable) {
            oracleAvailable = testOracleConnection();
        } else if (oracleConfigured && !oracleNetworkReachable) {
            oracleAvailable = false;
        }

        if (postgresConfigured && postgresNetworkReachable) {
            postgresAvailable = testPostgresConnection();
        } else if (postgresConfigured && !postgresNetworkReachable) {
            postgresAvailable = false;
        }

        printResultsToConsole();
        return true;
    }

    private void printResultsToConsole() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("ПРОВЕРКА ПОДКЛЮЧЕНИЯ К БАЗАМ ДАННЫХ");
        System.out.println("========================================");

        // Oracle
        System.out.print("🟠 ORACLE: ");
        if (!oracleConfigured) {
            System.out.println("НЕ НАСТРОЕН");
        } else if (!oracleNetworkReachable) {
            System.out.println("СЕТЬ НЕДОСТУПНА ✗");
            if (oracleError != null) {
                System.out.println("     " + oracleError);
            }
        } else if (oracleAvailable) {
            System.out.println("ДОСТУПНА ✓");
        } else {
            System.out.println("НЕДОСТУПНА ✗");
            if (oracleError != null) {
                System.out.println("     " + oracleError);
            }
        }

        // PostgreSQL
        System.out.print("🐘 POSTGRESQL: ");
        if (!postgresConfigured) {
            System.out.println("НЕ НАСТРОЕН");
        } else if (!postgresNetworkReachable) {
            System.out.println("СЕТЬ НЕДОСТУПНА ✗");
            if (postgresError != null) {
                System.out.println("     " + postgresError);
            }
        } else if (postgresAvailable) {
            System.out.println("ДОСТУПНА ✓");
        } else {
            System.out.println("НЕДОСТУПНА ✗");
            if (postgresError != null) {
                System.out.println("     " + postgresError);
            }
        }

        System.out.println("----------------------------------------");

        if (!oracleNetworkReachable && !postgresNetworkReachable && oracleConfigured && postgresConfigured) {
            System.out.println("⚠ ВНИМАНИЕ: Оба сервера БД недоступны по сети!");
            System.out.println("   Проверьте сетевое подключение, firewall и доступность серверов.");
        } else if (!oracleNetworkReachable && oracleConfigured) {
            System.out.println("⚠ ВНИМАНИЕ: Oracle сервер недоступен по сети!");
            System.out.println("   Проверьте хост " + NetworkUtils.extractHostFromUrl(settings.getOracleUrl()));
        } else if (!postgresNetworkReachable && postgresConfigured) {
            System.out.println("⚠ ВНИМАНИЕ: PostgreSQL сервер недоступен по сети!");
            System.out.println("   Проверьте хост " + NetworkUtils.extractHostFromUrl(settings.getPostgresUrl()));
        }

        System.out.println("========================================");
        System.out.println();
    }

    private boolean isOracleConfiguredInternal() {
        String url = settings.getOracleUrl();
        String user = settings.getOracleUser();
        String password = settings.getOraclePassword();

        if (url == null || url.trim().isEmpty()) return false;
        if (user == null || user.trim().isEmpty()) return false;
        if (password == null) return false;

        if (url.equals("jdbc:oracle:thin:@localhost:1521/XE") &&
                user.equals("user") && password.equals("password")) {
            return false;
        }
        return true;
    }

    private boolean isPostgresConfiguredInternal() {
        String url = settings.getPostgresUrl();
        String user = settings.getPostgresUser();
        String password = settings.getPostgresPassword();

        if (url == null || url.trim().isEmpty()) return false;
        if (user == null || user.trim().isEmpty()) return false;
        if (password == null) return false;

        if (url.equals("jdbc:postgresql://localhost:5432/postgres") &&
                user.equals("postgres") && password.equals("password")) {
            return false;
        }
        return true;
    }

    private boolean testOracleConnection() {
        String url = settings.getOracleUrl();
        String user = settings.getOracleUser();
        String password = settings.getOraclePassword();

        if (url == null || !url.toLowerCase().contains("jdbc:oracle:thin:@")) {
            oracleError = "Неверный формат URL";
            return false;
        }

        // Сетевая доступность уже проверена в checkAllConnections, но проверим ещё раз
        if (!NetworkUtils.isDatabaseServerAvailable(url)) {
            oracleError = "Сервер недоступен по сети";
            return false;
        }

        try (Connection conn = DriverManager.getConnection(url, user, password);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT 1 FROM DUAL")) {
            boolean connected = rs.next();
            if (connected) {
                oracleError = null;
                return true;
            } else {
                oracleError = "Не удалось выполнить тестовый запрос";
                return false;
            }
        } catch (SQLException e) {
            oracleError = getFriendlyErrorMessage(e);
            return false;
        }
    }

    private boolean testPostgresConnection() {
        String url = settings.getPostgresUrl();
        String user = settings.getPostgresUser();
        String password = settings.getPostgresPassword();

        if (url == null || !url.toLowerCase().contains("jdbc:postgresql://")) {
            postgresError = "Неверный формат URL";
            return false;
        }

        // Сетевая доступность уже проверена в checkAllConnections, но проверим ещё раз
        if (!NetworkUtils.isDatabaseServerAvailable(url)) {
            postgresError = "Сервер недоступен по сети";
            return false;
        }

        try (Connection conn = DriverManager.getConnection(url, user, password);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT 1")) {
            boolean connected = rs.next();
            if (connected) {
                String misUser = settings.getMisUser();
                if (misUser != null && !misUser.trim().isEmpty()) {
                    try (var ctxStmt = conn.createStatement()) {
                        ctxStmt.execute("SELECT set_config('mis.user', '" + misUser + "', false)");
                    } catch (SQLException e) {
                        System.err.println("[DatabaseAvailability] Предупреждение: не удалось установить контекст МИС");
                    }
                }
                postgresError = null;
                return true;
            } else {
                postgresError = "Не удалось выполнить тестовый запрос";
                return false;
            }
        } catch (SQLException e) {
            postgresError = getFriendlyErrorMessage(e);
            return false;
        }
    }

    private String getFriendlyErrorMessage(SQLException e) {
        String message = e.getMessage();
        if (message == null) return "Неизвестная ошибка";

        if (message.contains("IO Exception") || message.contains("Network")) {
            return "Сетевая ошибка: сервер недоступен";
        } else if (message.contains("ORA-12505")) {
            return "Ошибка: SID/сервис не найден";
        } else if (message.contains("ORA-12514")) {
            return "Ошибка: слушатель не знает о сервисе";
        } else if (message.contains("ORA-01017")) {
            return "Ошибка: неверное имя пользователя или пароль";
        } else if (message.contains("password authentication failed")) {
            return "Ошибка: неверный пароль PostgreSQL";
        } else if (message.contains("does not exist") && message.contains("database")) {
            return "Ошибка: база данных не существует";
        } else if (message.contains("Connection refused")) {
            return "Соединение отклонено: проверьте порт и firewall";
        } else if (message.contains("timed out")) {
            return "Таймаут подключения: сервер не отвечает";
        } else if (message.contains("UnknownHost")) {
            return "Неизвестный хост: проверьте имя сервера";
        }

        if (message.length() > 100) {
            message = message.substring(0, 97) + "...";
        }
        return message;
    }

    public boolean showResultsDialog(Component parent) {
        return true;
    }

    // Getters
    public boolean isOracleAvailable() { return oracleAvailable; }
    public boolean isPostgresAvailable() { return postgresAvailable; }
    public String getOracleError() { return oracleError; }
    public String getPostgresError() { return postgresError; }
    public boolean isOracleConfigured() { return oracleConfigured; }
    public boolean isPostgresConfigured() { return postgresConfigured; }
    public boolean isOracleNetworkReachable() { return oracleNetworkReachable; }
    public boolean isPostgresNetworkReachable() { return postgresNetworkReachable; }
}