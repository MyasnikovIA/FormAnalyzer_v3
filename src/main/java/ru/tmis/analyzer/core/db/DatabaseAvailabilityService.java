package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.utils.NetworkUtils;

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
     * @return true всегда (автоматически продолжаем выполнение)
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

        // Выводим статистику в консоль
        printResultsToConsole();

        return true; // Всегда возвращаем true, продолжаем выполнение
    }

    /**
     * Выводит результаты проверки в консоль (без диалогов)
     */
    private void printResultsToConsole() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("ПРОВЕРКА ПОДКЛЮЧЕНИЯ К БАЗАМ ДАННЫХ");
        System.out.println("========================================");

        // Oracle
        System.out.print("🟠 ORACLE: ");
        if (!oracleConfigured) {
            System.out.println("НЕ НАСТРОЕН");
            System.out.println("     Заполните настройки подключения в меню 'Настройки'");
        } else if (oracleAvailable) {
            System.out.println("ДОСТУПНА ✓");
        } else {
            System.out.println("НЕДОСТУПНА ✗");
            if (oracleError != null) {
                System.out.println("     Причина: " + oracleError);
            }
        }

        // PostgreSQL
        System.out.print("🐘 POSTGRESQL: ");
        if (!postgresConfigured) {
            System.out.println("НЕ НАСТРОЕН");
            System.out.println("     Заполните настройки подключения в меню 'Настройки'");
        } else if (postgresAvailable) {
            System.out.println("ДОСТУПНА ✓");
        } else {
            System.out.println("НЕДОСТУПНА ✗");
            if (postgresError != null) {
                System.out.println("     Причина: " + postgresError);
            }
        }

        System.out.println("----------------------------------------");

        // Определяем режим работы и выводим предупреждения в консоль
        if (!oracleConfigured && !postgresConfigured) {
            System.out.println("⚠ ВНИМАНИЕ: НИ ОДНА БАЗА ДАННЫХ НЕ НАСТРОЕНА");
            System.out.println("   Анализ форм будет выполняться ТОЛЬКО на основе XML файлов.");
            System.out.println("   Следующие функции будут недоступны:");
            System.out.println("     • Детальный анализ вьюх (DDL, количество записей)");
            System.out.println("     • Проверка пакетов/функций");
            System.out.println("     • Загрузка отчётов из БД");
            System.out.println("     • Проверка первичных ключей и NOT NULL");
            System.out.println("     • LLM промпт с DDL объектов");

        } else if (!oracleAvailable && !postgresAvailable && oracleConfigured && postgresConfigured) {
            System.out.println("⚠ ВНИМАНИЕ: Обе базы данных недоступны!");
            System.out.println("   Проверьте:");
            System.out.println("     • Доступность серверов по сети");
            System.out.println("     • Правильность URL, логина и пароля");
            System.out.println("     • Работу firewall");
            System.out.println("   Анализ форм будет сильно ограничен.");

        } else if (!oracleAvailable && oracleConfigured) {
            System.out.println("⚠ ВНИМАНИЕ: Oracle недоступна!");
            System.out.println("   Следующие функции будут недоступны:");
            System.out.println("     • Детальный анализ вьюх (DDL, количество записей)");
            System.out.println("     • Проверка пакетов/функций");
            System.out.println("     • Загрузка отчётов из D_REPORTS_LINKS");
            System.out.println("     • Проверка первичных ключей и NOT NULL");
            System.out.println("     • Oracle часть LLM промпта");
            if (postgresAvailable) {
                System.out.println("   PostgreSQL доступна. Продолжаем анализ.");
            } else {
                System.out.println("   PostgreSQL также недоступна. Анализ будет ограничен.");
            }

        } else if (!postgresAvailable && postgresConfigured) {
            System.out.println("ℹ ИНФОРМАЦИЯ: PostgreSQL недоступна");
            System.out.println("   Следующие функции будут недоступны:");
            System.out.println("     • Детальный анализ вьюх из PostgreSQL");
            System.out.println("     • Проверка первичных ключей (PostgreSQL часть)");
            System.out.println("     • PostgreSQL контекстное меню");
            System.out.println("     • PostgreSQL часть LLM промпта");
            System.out.println("   Oracle доступна. Продолжаем анализ.");

        } else {
            System.out.println("✓ Обе базы данных настроены и доступны!");
            System.out.println("   Анализ будет выполнен в полном объёме.");
        }

        System.out.println("========================================");
        System.out.println();
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
     * Заглушка для совместимости (больше не показывает диалоги)
     * @param parent родительский компонент (игнорируется)
     * @return всегда true
     */
    public boolean showResultsDialog(Component parent) {
        // Ничего не показываем, просто возвращаем true
        // Результаты уже выведены в консоль в checkAllConnections()
        return true;
    }

    // Public Getters
    public boolean isOracleAvailable() { return oracleAvailable; }
    public boolean isPostgresAvailable() { return postgresAvailable; }
    public String getOracleError() { return oracleError; }
    public String getPostgresError() { return postgresError; }
    public boolean isOracleConfigured() { return oracleConfigured; }
    public boolean isPostgresConfigured() { return postgresConfigured; }
}