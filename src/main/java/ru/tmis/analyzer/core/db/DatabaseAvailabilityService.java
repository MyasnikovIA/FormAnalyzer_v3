package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.utils.NetworkUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Сервис для проверки доступности баз данных при старте приложения
 */
public class DatabaseAvailabilityService {

    private final SettingsModel settings;
    private boolean oracleAvailable = false;
    private boolean postgresAvailable = false;
    private String oracleError = null;
    private String postgresError = null;

    public DatabaseAvailabilityService(SettingsModel settings) {
        this.settings = settings;
    }

    /**
     * Проверяет доступность обеих БД и инициализирует DatabaseCacheManager
     * @return true если хотя бы одна БД доступна
     */
    public boolean checkAllConnections() {
        // Инициализируем конфигурацию в DatabaseCacheManager
        DatabaseCacheManager.initDbConfig(
                settings.getOracleUrl(),
                settings.getOracleUser(),
                settings.getOraclePassword(),
                settings.getPostgresUrl(),
                settings.getPostgresUser(),
                settings.getPostgresPassword(),
                settings.getMisUser()
        );

        // Проверяем Oracle
        oracleAvailable = testOracleConnection();

        // Проверяем PostgreSQL
        postgresAvailable = testPostgresConnection();

        // Выводим статистику
        System.out.println("[DatabaseAvailability] === РЕЗУЛЬТАТЫ ПРОВЕРКИ ПОДКЛЮЧЕНИЙ ===");
        System.out.println("[DatabaseAvailability] Oracle: " + (oracleAvailable ? "ДОСТУПНА" : "НЕДОСТУПНА") +
                (oracleError != null ? " (" + oracleError + ")" : ""));
        System.out.println("[DatabaseAvailability] PostgreSQL: " + (postgresAvailable ? "ДОСТУПНА" : "НЕДОСТУПНА") +
                (postgresError != null ? " (" + postgresError + ")" : ""));

        return oracleAvailable || postgresAvailable;
    }

    /**
     * Проверка подключения к Oracle
     */
    private boolean testOracleConnection() {
        String url = settings.getOracleUrl();
        String user = settings.getOracleUser();
        String password = settings.getOraclePassword();

        if (url == null || url.trim().isEmpty()) {
            oracleError = "URL не настроен";
            return false;
        }

        if (user == null || user.trim().isEmpty()) {
            oracleError = "Пользователь не настроен";
            return false;
        }

        // Сначала проверяем доступность сети
        if (!NetworkUtils.isDatabaseServerAvailable(url)) {
            oracleError = "Сервер недоступен по сети";
            return false;
        }

        // Пробуем подключиться
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

    /**
     * Проверка подключения к PostgreSQL
     */
    private boolean testPostgresConnection() {
        String url = settings.getPostgresUrl();
        String user = settings.getPostgresUser();
        String password = settings.getPostgresPassword();

        if (url == null || url.trim().isEmpty()) {
            postgresError = "URL не настроен";
            return false;
        }

        if (user == null || user.trim().isEmpty()) {
            postgresError = "Пользователь не настроен";
            return false;
        }

        // Сначала проверяем доступность сети
        if (!NetworkUtils.isDatabaseServerAvailable(url)) {
            postgresError = "Сервер недоступен по сети";
            return false;
        }

        // Пробуем подключиться
        try (Connection conn = DriverManager.getConnection(url, user, password);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT 1")) {

            boolean connected = rs.next();
            if (connected) {
                // Если указан пользователь МИС, пробуем установить контекст
                String misUser = settings.getMisUser();
                if (misUser != null && !misUser.trim().isEmpty()) {
                    try (var ctxStmt = conn.createStatement()) {
                        ctxStmt.execute("SELECT set_config('mis.user', '" + misUser + "', false)");
                        System.out.println("[DatabaseAvailability] Контекст МИС установлен: " + misUser);
                    } catch (SQLException e) {
                        System.err.println("[DatabaseAvailability] Предупреждение: не удалось установить контекст МИС: " + e.getMessage());
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

    /**
     * Преобразует SQLException в понятное пользователю сообщение
     */
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
        }

        // Укорачиваем слишком длинные сообщения
        if (message.length() > 100) {
            message = message.substring(0, 97) + "...";
        }
        return message;
    }

    /**
     * Показывает диалог с результатами проверки
     * @param parent родительский компонент
     * @return true если пользователь решил продолжить (или нажал "Продолжить")
     */
    public boolean showResultsDialog(Component parent) {
        StringBuilder message = new StringBuilder();
        message.append("=== РЕЗУЛЬТАТЫ ПРОВЕРКИ ПОДКЛЮЧЕНИЙ ===\n\n");

        // Oracle
        message.append("🟠 ORACLE:\n");
        if (oracleAvailable) {
            message.append("   ✓ ДОСТУПНА\n");
        } else {
            message.append("   ✗ НЕДОСТУПНА\n");
            if (oracleError != null) {
                message.append("     Причина: ").append(oracleError).append("\n");
            }
        }

        // PostgreSQL
        message.append("\n🐘 POSTGRESQL:\n");
        if (postgresAvailable) {
            message.append("   ✓ ДОСТУПНА\n");
        } else {
            message.append("   ✗ НЕДОСТУПНА\n");
            if (postgresError != null) {
                message.append("     Причина: ").append(postgresError).append("\n");
            }
        }

        message.append("\n");
        message.append("─".repeat(40)).append("\n\n");

        if (!oracleAvailable && !postgresAvailable) {
            message.append("⚠ ВНИМАНИЕ: Обе базы данных недоступны!\n");
            message.append("Анализ форм будет сильно ограничен.\n");
            message.append("Рекомендуется проверить настройки подключения.\n\n");
            message.append("Продолжить работу?");

            int result = JOptionPane.showConfirmDialog(parent, message.toString(),
                    "Проверка подключения к БД",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            return result == JOptionPane.YES_OPTION;

        } else if (!oracleAvailable) {
            message.append("⚠ ВНИМАНИЕ: Oracle недоступна!\n");
            message.append("Следующие функции будут недоступны:\n");
            message.append("  • Детальный анализ вьюх (DDL, количество записей)\n");
            message.append("  • Проверка пакетов/функций\n");
            message.append("  • Загрузка отчётов из D_REPORTS_LINKS\n");
            message.append("  • Проверка первичных ключей и NOT NULL\n\n");
            message.append("PostgreSQL доступна. Продолжить?");

            int result = JOptionPane.showConfirmDialog(parent, message.toString(),
                    "Проверка подключения к БД",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            return result == JOptionPane.YES_OPTION;

        } else if (!postgresAvailable) {
            message.append("ℹ ИНФОРМАЦИЯ: PostgreSQL недоступна\n");
            message.append("Следующие функции будут недоступны:\n");
            message.append("  • Детальный анализ вьюх из PostgreSQL\n");
            message.append("  • Проверка первичных ключей (PostgreSQL часть)\n");
            message.append("  • PostgreSQL контекстное меню\n\n");
            message.append("Oracle доступна. Продолжить?");

            int result = JOptionPane.showConfirmDialog(parent, message.toString(),
                    "Проверка подключения к БД",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            return result == JOptionPane.YES_OPTION;
        } else {
            message.append("✓ Обе базы данных доступны!\n");
            message.append("Анализ будет выполнен в полном объёме.");
            JOptionPane.showMessageDialog(parent, message.toString(),
                    "Проверка подключения к БД",
                    JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
    }

    // Getters
    public boolean isOracleAvailable() { return oracleAvailable; }
    public boolean isPostgresAvailable() { return postgresAvailable; }
    public String getOracleError() { return oracleError; }
    public String getPostgresError() { return postgresError; }
}