package ru.tmis.analyzer;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.db.DatabaseAvailabilityService;
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

                // ========== ПРОВЕРКА ДОСТУПНОСТИ БД ПРИ СТАРТЕ ==========
                DatabaseAvailabilityService dbChecker = new DatabaseAvailabilityService(settings);
                boolean hasConnection = dbChecker.checkAllConnections();

                // Показываем диалог с результатами
                boolean continueStartup = dbChecker.showResultsDialog(null);

                if (!continueStartup) {
                    System.out.println("Приложение закрыто пользователем из-за проблем с подключением к БД");
                    System.exit(0);
                }

                // Дополнительное предупреждение если Oracle недоступна (ключевая БД)
                if (!dbChecker.isOracleAvailable()) {
                    System.err.println("ВНИМАНИЕ: Oracle недоступна. Анализ будет ограничен.");
                }
                // ========================================================

                MainWindow window = new MainWindow(settings, config);
                window.setVisible(true);

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