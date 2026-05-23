// MainForm.java
package ru.tmis.analyzer;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.db.DatabaseConnectionManager;
import ru.tmis.analyzer.ui.MainWindow;

import javax.swing.*;

public class MainForm {

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