package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.config.AppConfig;
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
    private boolean oracleConfigured = false;
    private boolean postgresConfigured = false;

    public DatabaseAvailabilityService(SettingsModel settings) {
        this.settings = settings;
    }

    /**
     * Проверяет доступность обеих БД и инициализирует DatabaseCacheManager
     * @return true если хотя бы одна БД доступна (или ни одна не настроена)
     */
    public boolean checkAllConnections() {
        // Проверяем, настроены ли подключения
        oracleConfigured = isOracleConfiguredInternal();
        postgresConfigured = isPostgresConfiguredInternal();

        System.out.println("[DatabaseAvailability] Настройки подключений:");
        System.out.println("[DatabaseAvailability]   Oracle: " + (oracleConfigured ? "настроен" : "НЕ НАСТРОЕН"));
        System.out.println("[DatabaseAvailability]   PostgreSQL: " + (postgresConfigured ? "настроен" : "НЕ НАСТРОЕН"));

        // Инициализируем конфигурацию в DatabaseCacheManager (даже если пустая)
        DatabaseCacheManager.initDbConfig(
                settings.getOracleUrl() != null ? settings.getOracleUrl() : "",
                settings.getOracleUser() != null ? settings.getOracleUser() : "",
                settings.getOraclePassword() != null ? settings.getOraclePassword() : "",
                settings.getPostgresUrl() != null ? settings.getPostgresUrl() : "",
                settings.getPostgresUser() != null ? settings.getPostgresUser() : "",
                settings.getPostgresPassword() != null ? settings.getPostgresPassword() : "",
                settings.getMisUser() != null ? settings.getMisUser() : ""
        );

        // Проверяем Oracle только если настроен
        if (oracleConfigured) {
            oracleAvailable = testOracleConnection();
        } else {
            oracleAvailable = false;
            oracleError = "Подключение не настроено (заполните настройки)";
        }

        // Проверяем PostgreSQL только если настроен
        if (postgresConfigured) {
            postgresAvailable = testPostgresConnection();
        } else {
            postgresAvailable = false;
            postgresError = "Подключение не настроено (заполните настройки)";
        }

        // Выводим статистику
        System.out.println("[DatabaseAvailability] === РЕЗУЛЬТАТЫ ПРОВЕРКИ ПОДКЛЮЧЕНИЙ ===");
        System.out.println("[DatabaseAvailability] Oracle: " + (oracleAvailable ? "ДОСТУПНА" : "НЕДОСТУПНА") +
                (oracleError != null ? " (" + oracleError + ")" : ""));
        System.out.println("[DatabaseAvailability] PostgreSQL: " + (postgresAvailable ? "ДОСТУПНА" : "НЕДОСТУПНА") +
                (postgresError != null ? " (" + postgresError + ")" : ""));

        return true; // Всегда возвращаем true, чтобы приложение запустилось
    }

    /**
     * Проверяет, настроено ли подключение к Oracle
     */
    private boolean isOracleConfiguredInternal() {
        String url = settings.getOracleUrl();
        String user = settings.getOracleUser();
        String password = settings.getOraclePassword();

        if (url == null || url.trim().isEmpty()) return false;
        if (user == null || user.trim().isEmpty()) return false;
        if (password == null) return false;

        // Проверяем, что это не значения по умолчанию-заглушки
        if (url.equals("jdbc:oracle:thin:@localhost:1521/XE") &&
                user.equals("user") && password.equals("password")) {
            return false;
        }

        return true;
    }

    /**
     * Проверяет, настроено ли подключение к PostgreSQL
     */
    private boolean isPostgresConfiguredInternal() {
        String url = settings.getPostgresUrl();
        String user = settings.getPostgresUser();
        String password = settings.getPostgresPassword();

        if (url == null || url.trim().isEmpty()) return false;
        if (user == null || user.trim().isEmpty()) return false;
        if (password == null) return false;

        // Проверяем, что это не значения по умолчанию-заглушки
        if (url.equals("jdbc:postgresql://localhost:5432/postgres") &&
                user.equals("postgres") && password.equals("password")) {
            return false;
        }

        return true;
    }

    /**
     * Проверка подключения к Oracle
     */
    private boolean testOracleConnection() {
        String url = settings.getOracleUrl();
        String user = settings.getOracleUser();
        String password = settings.getOraclePassword();

        // Дополнительная проверка на валидность URL
        if (url == null || !url.toLowerCase().contains("jdbc:oracle:thin:@")) {
            oracleError = "Неверный формат URL (должен быть jdbc:oracle:thin:@хост:порт/сервис)";
            return false;
        }

        // Проверяем доступность сети
        if (!NetworkUtils.isDatabaseServerAvailable(url)) {
            oracleError = "Сервер недоступен по сети (проверьте хост и порт)";
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

        // Дополнительная проверка на валидность URL
        if (url == null || !url.toLowerCase().contains("jdbc:postgresql://")) {
            postgresError = "Неверный формат URL (должен быть jdbc:postgresql://хост:порт/база)";
            return false;
        }

        // Проверяем доступность сети
        if (!NetworkUtils.isDatabaseServerAvailable(url)) {
            postgresError = "Сервер недоступен по сети (проверьте хост и порт)";
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
        } else if (message.contains("UnknownHost")) {
            return "Неизвестный хост: проверьте имя сервера";
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
     * @return true если пользователь решил продолжить
     */
    public boolean showResultsDialog(Component parent) {
        StringBuilder message = new StringBuilder();
        message.append("=== ПРОВЕРКА ПОДКЛЮЧЕНИЯ К БАЗАМ ДАННЫХ ===\n\n");

        // Oracle
        message.append("🟠 ORACLE:\n");
        if (!oracleConfigured) {
            message.append("   ⚠ НЕ НАСТРОЕН\n");
            message.append("     Заполните настройки подключения в меню 'Настройки'\n");
        } else if (oracleAvailable) {
            message.append("   ✓ ДОСТУПНА\n");
        } else {
            message.append("   ✗ НЕДОСТУПНА\n");
            if (oracleError != null) {
                message.append("     Причина: ").append(oracleError).append("\n");
            }
        }

        // PostgreSQL
        message.append("\n🐘 POSTGRESQL:\n");
        if (!postgresConfigured) {
            message.append("   ⚠ НЕ НАСТРОЕН\n");
            message.append("     Заполните настройки подключения в меню 'Настройки'\n");
        } else if (postgresAvailable) {
            message.append("   ✓ ДОСТУПНА\n");
        } else {
            message.append("   ✗ НЕДОСТУПНА\n");
            if (postgresError != null) {
                message.append("     Причина: ").append(postgresError).append("\n");
            }
        }

        message.append("\n");
        message.append("─".repeat(45)).append("\n\n");

        // Определяем режим работы
        if (!oracleConfigured && !postgresConfigured) {
            message.append("ℹ НИ ОДНА БАЗА ДАННЫХ НЕ НАСТРОЕНА\n\n");
            message.append("Анализ форм будет выполняться ТОЛЬКО на основе XML файлов.\n");
            message.append("Следующие функции будут недоступны:\n");
            message.append("  • Детальный анализ вьюх (DDL, количество записей)\n");
            message.append("  • Проверка пакетов/функций\n");
            message.append("  • Загрузка отчётов из БД\n");
            message.append("  • Проверка первичных ключей и NOT NULL\n");
            message.append("  • LLM промпт с DDL объектов\n\n");
            message.append("Нажмите 'Продолжить' для запуска приложения,\n");
            message.append("или 'Настройки' для заполнения параметров подключения.\n\n");

            Object[] options = {"Продолжить", "Открыть настройки", "Выход"};
            int result = JOptionPane.showOptionDialog(parent, message.toString(),
                    "Подключения к БД не настроены",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);

            if (result == 1) { // Открыть настройки
                openSettingsDialog(parent);
                return true; // После настроек продолжаем
            } else if (result == 2) { // Выход
                return false;
            }
            return true;

        } else if (!oracleAvailable && !postgresAvailable && oracleConfigured && postgresConfigured) {
            message.append("⚠ ВНИМАНИЕ: Обе базы данных недоступны!\n\n");
            message.append("Проверьте:\n");
            message.append("  • Доступность серверов по сети\n");
            message.append("  • Правильность URL, логина и пароля\n");
            message.append("  • Работу firewall\n\n");
            message.append("Анализ форм будет сильно ограничен.\n");
            message.append("Продолжить?");

            int result = JOptionPane.showConfirmDialog(parent, message.toString(),
                    "Ошибка подключения к БД",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            return result == JOptionPane.YES_OPTION;

        } else if (!oracleAvailable && oracleConfigured) {
            message.append("⚠ ВНИМАНИЕ: Oracle недоступна!\n\n");
            message.append("Следующие функции будут недоступны:\n");
            message.append("  • Детальный анализ вьюх (DDL, количество записей)\n");
            message.append("  • Проверка пакетов/функций\n");
            message.append("  • Загрузка отчётов из D_REPORTS_LINKS\n");
            message.append("  • Проверка первичных ключей и NOT NULL\n");
            message.append("  • Oracle часть LLM промпта\n\n");

            if (postgresAvailable) {
                message.append("PostgreSQL доступна. Продолжить?");
            } else {
                message.append("PostgreSQL также недоступна. Продолжить?");
            }

            int result = JOptionPane.showConfirmDialog(parent, message.toString(),
                    "Oracle недоступна",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            return result == JOptionPane.YES_OPTION;

        } else if (!postgresAvailable && postgresConfigured) {
            message.append("ℹ ИНФОРМАЦИЯ: PostgreSQL недоступна\n\n");
            message.append("Следующие функции будут недоступны:\n");
            message.append("  • Детальный анализ вьюх из PostgreSQL\n");
            message.append("  • Проверка первичных ключей (PostgreSQL часть)\n");
            message.append("  • PostgreSQL контекстное меню\n");
            message.append("  • PostgreSQL часть LLM промпта\n\n");
            message.append("Oracle доступна. Продолжить?");

            int result = JOptionPane.showConfirmDialog(parent, message.toString(),
                    "PostgreSQL недоступна",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            return result == JOptionPane.YES_OPTION;
        } else {
            // Всё хорошо
            message.append("✓ Обе базы данных настроены и доступны!\n");
            message.append("Анализ будет выполнен в полном объёме.");
            JOptionPane.showMessageDialog(parent, message.toString(),
                    "Подключения к БД",
                    JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
    }

    /**
     * Открывает диалог настроек
     */
    private void openSettingsDialog(Component parent) {
        try {
            Class<?> settingsDialogClass = Class.forName("ru.tmis.analyzer.ui.SettingsDialog");
            java.lang.reflect.Constructor<?> constructor = settingsDialogClass.getConstructor(
                    JFrame.class, SettingsModel.class, AppConfig.class);

            JFrame parentFrame = parent instanceof JFrame ? (JFrame) parent :
                    (JFrame) SwingUtilities.getWindowAncestor(parent);

            AppConfig config = AppConfig.load();
            Object dialog = constructor.newInstance(parentFrame, settings, config);
            java.lang.reflect.Method showMethod = dialog.getClass().getMethod("setVisible", boolean.class);
            showMethod.invoke(dialog, true);

        } catch (Exception e) {
            System.err.println("Не удалось открыть диалог настроек: " + e.getMessage());
            JOptionPane.showMessageDialog(parent,
                    "Пожалуйста, настройте подключения к БД в меню 'Настройки'",
                    "Настройка БД",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Public Getters
    public boolean isOracleAvailable() { return oracleAvailable; }
    public boolean isPostgresAvailable() { return postgresAvailable; }
    public String getOracleError() { return oracleError; }
    public String getPostgresError() { return postgresError; }
    public boolean isOracleConfigured() { return oracleConfigured; }
    public boolean isPostgresConfigured() { return postgresConfigured; }
}