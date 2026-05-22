package ru.tmis.analyzer;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.ui.MainWindow;

import javax.swing.*;

public class MainForm {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                SettingsModel settings = SettingsModel.load();
                AppConfig config = AppConfig.load();

                if (config == null) {
                    System.err.println("ОШИБКА: config = null, создаём новый");
                    config = new AppConfig();
                }
                if (settings == null) {
                    System.err.println("ОШИБКА: settings = null, создаём новый");
                    settings = new SettingsModel();
                }

                MainWindow window = new MainWindow(settings, config);
                window.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Ошибка запуска приложения: " + e.getMessage(),
                        "Критическая ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}