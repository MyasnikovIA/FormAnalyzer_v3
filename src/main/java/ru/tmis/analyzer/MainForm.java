// MainForm.java
package ru.tmis.analyzer;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.FormCache;
import ru.tmis.analyzer.core.cache.InMemoryReportBuffer;
import ru.tmis.analyzer.core.db.DatabaseConnectionManager;
import ru.tmis.analyzer.ui.MainWindow;

import javax.swing.*;

public class MainForm {
    static {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Драйвер БД не найден: " + e.getMessage());
        }
    }
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                SettingsModel settings = SettingsModel.load();
                AppConfig config = AppConfig.load();

                // ========== ИНИЦИАЛИЗАЦИЯ ПУЛА СОЕДИНЕНИЙ ==========
                DatabaseConnectionManager.init(
                        settings.getOracleUrl(),
                        settings.getOracleUser(),
                        settings.getOraclePassword(),
                        settings.getPostgresUrl(),
                        settings.getPostgresUser(),
                        settings.getPostgresPassword(),
                        settings.getMisUser()
                );
                // =================================================

                MainWindow window = new MainWindow(settings, config);
                window.setVisible(true);

                // При закрытии окна закрываем пулы
                window.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        DatabaseConnectionManager.shutdown();
                    }
                });
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("Закрытие соединений с БД...");
                    DatabaseConnectionManager.shutdown();
                    FormCache.clear();
                    InMemoryReportBuffer.clear();
                }));

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Ошибка запуска приложения: " + e.getMessage(),
                        "Критическая ошибка",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}