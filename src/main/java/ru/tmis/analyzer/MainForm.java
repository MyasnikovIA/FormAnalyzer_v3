// MainForm.java
package ru.tmis.analyzer;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.ui.MainWindow;

import javax.swing.*;
import java.io.File;

public class MainForm {

    public static void main(String[] args) {
        // Настройки для Windows
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            configureWindowsOracle();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Быстрая загрузка настроек (без подключения к БД)
                SettingsModel settings = SettingsModel.getInstance();
                AppConfig config = AppConfig.load();

                // Сразу создаём и показываем окно
                MainWindow window = new MainWindow(settings, config);
                window.setVisible(true);

                // Запускаем фоновую загрузку кэша
                startBackgroundCacheLoading(settings, config, window);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Ошибка запуска приложения: " + e.getMessage(),
                        "Критическая ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Фоновая загрузка кэша (не блокирует GUI)
     */
    private static void startBackgroundCacheLoading(SettingsModel settings, AppConfig config, MainWindow window) {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Инициализация подключений к БД (может быть долгой)
                publish("Инициализация подключений к БД...");
                DatabaseCacheManager.initDbConfig(
                        settings.getOracleUrl(),
                        settings.getOracleUser(),
                        settings.getOraclePassword(),
                        settings.getPostgresUrl(),
                        settings.getPostgresUser(),
                        settings.getPostgresPassword(),
                        settings.getMisUser()
                );

                // Загрузка кэша с диска
                publish("Загрузка кэша с диска...");
                DatabaseCacheManager.setCacheOutputDir(settings.getOutputDir());
                DatabaseCacheManager.loadFromDisk();

                // Запуск автосохранения
                publish("Запуск автосохранения кэша...");
                DatabaseCacheManager.initAutoSave();

                publish("Кэш загружен");
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                String lastMessage = chunks.get(chunks.size() - 1);
                window.appendLog(lastMessage);
                window.setStatusMessage(lastMessage);
            }

            @Override
            protected void done() {
                window.setStatusMessage("Готов к работе");
                window.appendLog("✅ Фоновая загрузка кэша завершена");
                try {
                    get(); // Проверяем наличие ошибок
                } catch (Exception e) {
                    window.appendLog("❌ Ошибка фоновой загрузки: " + e.getMessage());
                    window.setStatusMessage("Ошибка загрузки кэша");
                }
            }
        };

        worker.execute();
    }

    private static void configureWindowsOracle() {
        String oracleHome = "C:\\instantclient_19_6";
        if (new File(oracleHome).exists()) {
            System.setProperty("java.library.path",
                    System.getProperty("java.library.path") + ";" + oracleHome);
        }
        System.setProperty("oracle.jdbc.defaultNChar", "true");
        System.setProperty("oracle.net.disableOob", "true");
        System.setProperty("user.timezone", "Europe/Moscow");

        if (System.getProperty("user.timezone") == null) {
            // System.setProperty("user.timezone", "Europe/Moscow");
            System.setProperty("user.timezone", "GMT");
            // Отключаем использование региональных настроек для timezone
            System.setProperty("oracle.jdbc.timezoneAsRegion", "false");
        }
    }
}