// Launcher.java
package ru.tmis.analyzer;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.ui.MainWindow;

import javax.swing.*;
import java.io.File;

public class MainForm {

    public static void main(String[] args) {
        // Настройки для Windows (Oracle Instant Client)
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            configureWindowsOracle();
        }

        // Запуск UI в EDT потоке
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SettingsModel settings = SettingsModel.load();
                AppConfig config = AppConfig.load();

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

    private static void configureWindowsOracle() {
        String oracleHome = "C:\\instantclient_19_6";
        if (new File(oracleHome).exists()) {
            System.setProperty("java.library.path",
                    System.getProperty("java.library.path") + ";" + oracleHome);
        }
        System.setProperty("oracle.jdbc.defaultNChar", "true");
        System.setProperty("oracle.net.disableOob", "true");
    }
}