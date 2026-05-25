// ui/CacheManagementDialog.java
package ru.tmis.analyzer.ui;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.core.cache.DatabaseCachePopulator;
import ru.tmis.analyzer.core.log.ILogger;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class CacheManagementDialog extends JDialog {

    private final SettingsModel settings;
    private ExecutorService executor;
    private Future<?> currentTask;
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private volatile boolean isRunning = false;
    private String currentOperation = "";

    private JTextArea logArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTextArea statsArea;

    // Кнопки для каждого типа кэша (параллельный запуск)
    private JButton reloadOracleViewsBtn;
    private JButton reloadPostgresViewsBtn;
    private JButton reloadOracleTablesBtn;
    private JButton reloadPostgresTablesBtn;
    private JButton reloadOracleFunctionsBtn;
    private JButton reloadPostgresFunctionsBtn;
    private JButton reloadOraclePackagesBtn;
    private JButton reloadPostgresPackagesBtn;
    private JButton reloadBrokersBtn;
    private JButton reloadOracleReportsBtn;
    private JButton reloadPostgresReportsBtn;
    private JButton reloadConstantsBtn;
    private JButton reloadOptionsBtn;
    private JButton reloadMetadataBtn;
    private JButton reloadSequencesBtn;
    private JButton reloadTriggersBtn;
    private JButton reloadSynonymsBtn;

    // Глобальные кнопки
    private JButton reloadAllBtn;
    private JButton saveCacheBtn;
    private JButton stopBtn;
    private JButton clearAllBtn;
    private JButton refreshStatsBtn;

    public CacheManagementDialog(JFrame parent, SettingsModel settings) {
        super(parent, "Управление кэшем БД", true);
        this.settings = settings;
        this.executor = Executors.newCachedThreadPool(); // Используем cached thread pool для параллельных задач

        initUI();
        setSize(1100, 750);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        JPanel buttonsPanel = createButtonsPanel();
        mainPanel.add(buttonsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        refreshStats();
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
       // panel.setBorder(BorderFactory.createTitledBorder("Информация"));
//
       // JTextArea infoArea = new JTextArea();
       // infoArea.setEditable(false);
       // infoArea.setBackground(panel.getBackground());
       // infoArea.setFont(new Font("Dialog", Font.PLAIN, 11));
       // infoArea.setText(
       //         "Управление кэшем базы данных\n\n" +
       //                 "• КАЖДАЯ КНОПКА ЗАПУСКАЕТ ЗАГРУЗКУ В ОТДЕЛЬНОМ ПАРАЛЛЕЛЬНОМ ПОТОКЕ\n" +
       //                 "• Вы можете запустить несколько типов загрузки одновременно\n" +
       //                 "• Процесс можно остановить в любой момент (останавливаются все потоки)\n" +
       //                 "• Уже загруженные данные сохраняются на диске\n" +
       //                 "• При повторной загрузке новые данные добавляются к существующим\n" +
       //                 "• Кнопка 'Сохранить кэш' принудительно сохраняет текущий кэш на диск\n" +
       //                 "• Кнопка 'Очистить всё' удаляет весь кэш (требуется подтверждение)"
       // );
       // infoArea.setMargin(new Insets(10, 10, 10, 10));
       // panel.add(infoArea, BorderLayout.CENTER);
//
        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Типы кэша"));

        JPanel statsPanel = createStatsPanel();
        panel.add(statsPanel, BorderLayout.NORTH);

        JPanel buttonsGridPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.weightx = 1.0;

        int row = 0;

        // Заголовок
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel headerLabel = new JLabel("═══════════════════════════════════════════════════════════════════════════════");
        headerLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        buttonsGridPanel.add(headerLabel, gbc);
        row++;

        gbc.gridwidth = 1;

        // Ряд 1: Oracle Views и PostgreSQL Views
        gbc.gridy = row;
        gbc.gridx = 0;
        reloadOracleViewsBtn = createCacheButton("🟠 Oracle: Перезагрузить вьюхи", "Загрузить DDL вьюх из Oracle (D_V_*)", new Color(33, 150, 243));
        buttonsGridPanel.add(reloadOracleViewsBtn, gbc);

        gbc.gridx = 1;
        reloadPostgresViewsBtn = createCacheButton("🐘 PostgreSQL: Перезагрузить вьюхи", "Загрузить DDL вьюх из PostgreSQL (D_V_*)", new Color(33, 150, 243));
        buttonsGridPanel.add(reloadPostgresViewsBtn, gbc);
        row++;

        // Ряд 2: Oracle Tables и PostgreSQL Tables
        gbc.gridy = row;
        gbc.gridx = 0;
        reloadOracleTablesBtn = createCacheButton("🟠 Oracle: Перезагрузить таблицы", "Загрузить DDL таблиц из Oracle (D_*)", new Color(76, 175, 80));
        buttonsGridPanel.add(reloadOracleTablesBtn, gbc);

        gbc.gridx = 1;
        reloadPostgresTablesBtn = createCacheButton("🐘 PostgreSQL: Перезагрузить таблицы", "Загрузить DDL таблиц из PostgreSQL", new Color(76, 175, 80));
        buttonsGridPanel.add(reloadPostgresTablesBtn, gbc);
        row++;

        // Ряд 3: Oracle Functions и PostgreSQL Functions
        gbc.gridy = row;
        gbc.gridx = 0;
        reloadOracleFunctionsBtn = createCacheButton("🟠 Oracle: Перезагрузить функции", "Загрузить тела функций из Oracle пакетов", new Color(255, 152, 0));
        buttonsGridPanel.add(reloadOracleFunctionsBtn, gbc);

        gbc.gridx = 1;
        reloadPostgresFunctionsBtn = createCacheButton("🐘 PostgreSQL: Перезагрузить функции", "Загрузить тела функций из PostgreSQL", new Color(255, 152, 0));
        buttonsGridPanel.add(reloadPostgresFunctionsBtn, gbc);
        row++;

        // Ряд 4: Oracle Packages и PostgreSQL Packages
        gbc.gridy = row;
        gbc.gridx = 0;
        reloadOraclePackagesBtn = createCacheButton("🟠 Oracle: Перезагрузить пакеты", "Загрузить спецификации пакетов Oracle (D_PKG_*)", new Color(103, 58, 183));
        buttonsGridPanel.add(reloadOraclePackagesBtn, gbc);

        gbc.gridx = 1;
        reloadPostgresPackagesBtn = createCacheButton("🐘 PostgreSQL: Перезагрузить пакеты", "Загрузить пакеты/функции PostgreSQL", new Color(103, 58, 183));
        buttonsGridPanel.add(reloadPostgresPackagesBtn, gbc);
        row++;

        // Ряд 5: Brokers и Oracle Reports
        gbc.gridy = row;
        gbc.gridx = 0;
        reloadBrokersBtn = createCacheButton("🔗 Перезагрузить брокеры", "Загрузить D_UNITBPS (unit + action -> execProc)", new Color(156, 39, 176));
        buttonsGridPanel.add(reloadBrokersBtn, gbc);

        gbc.gridx = 1;
        reloadOracleReportsBtn = createCacheButton("🟠 Oracle: Перезагрузить отчёты", "Загрузить отчёты из Oracle (D_REPORTS_LINKS)", new Color(0, 150, 136));
        buttonsGridPanel.add(reloadOracleReportsBtn, gbc);
        row++;

        // Ряд 6: PostgreSQL Reports и Constants
        gbc.gridy = row;
        gbc.gridx = 0;
        reloadPostgresReportsBtn = createCacheButton("🐘 PostgreSQL: Перезагрузить отчёты", "Загрузить отчёты из PostgreSQL", new Color(0, 150, 136));
        buttonsGridPanel.add(reloadPostgresReportsBtn, gbc);

        gbc.gridx = 1;
        reloadConstantsBtn = createCacheButton("📌 Перезагрузить константы", "Загрузить D_PKG_CONSTANTS", new Color(96, 125, 139));
        buttonsGridPanel.add(reloadConstantsBtn, gbc);
        row++;

        // Ряд 7: System Options и Metadata
        gbc.gridy = row;
        gbc.gridx = 0;
        reloadOptionsBtn = createCacheButton("⚙️ Перезагрузить системные опции", "Загрузить D_SYSTEM_OPTIONS", new Color(121, 85, 72));
        buttonsGridPanel.add(reloadOptionsBtn, gbc);

        gbc.gridx = 1;
        reloadMetadataBtn = createCacheButton("🏷️ Перезагрузить метаданные", "Загрузить колонки, индексы, внешние ключи", new Color(0, 188, 212));
        buttonsGridPanel.add(reloadMetadataBtn, gbc);
        row++;

        // Ряд 8: Sequences и Triggers
        gbc.gridy = row;
        gbc.gridx = 0;
        reloadSequencesBtn = createCacheButton("🔢 Перезагрузить последовательности", "Загрузить Oracle SEQUENCE", new Color(255, 193, 7));
        buttonsGridPanel.add(reloadSequencesBtn, gbc);

        gbc.gridx = 1;
        reloadTriggersBtn = createCacheButton("⚡ Перезагрузить триггеры", "Загрузить Oracle TRIGGER", new Color(233, 30, 99));
        buttonsGridPanel.add(reloadTriggersBtn, gbc);
        row++;

        // Ряд 9: Synonyms и (пусто)
        gbc.gridy = row;
        gbc.gridx = 0;
        reloadSynonymsBtn = createCacheButton("🔍 Перезагрузить синонимы", "Загрузить Oracle PUBLIC синонимы", new Color(158, 158, 158));
        buttonsGridPanel.add(reloadSynonymsBtn, gbc);

        gbc.gridx = 1;
        // пусто
        row++;

        // Ряд 10: Глобальные действия
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 5, 10);

        JPanel globalPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        reloadAllBtn = createCacheButton("🔄 ПЕРЕЗАГРУЗИТЬ ВСЁ", "Все типы данных (последовательно)", new Color(244, 67, 54));
        saveCacheBtn = createCacheButton("💾 СОХРАНИТЬ КЭШ", "Принудительно сохранить кэш на диск", new Color(0, 200, 83));
        stopBtn = createCacheButton("⏹️ ОСТАНОВИТЬ ВСЕ", "Прервать все текущие операции", new Color(158, 158, 158));
        clearAllBtn = createCacheButton("🗑️ ОЧИСТИТЬ ВСЁ", "Удалить весь кэш", new Color(244, 67, 54));
        refreshStatsBtn = createCacheButton("🔄 ОБНОВИТЬ СТАТИСТИКУ", "Обновить информацию о кэше", new Color(33, 150, 243));

        stopBtn.setEnabled(false);
        stopBtn.setBackground(new Color(0, 0, 0));

        globalPanel.add(reloadAllBtn);
        globalPanel.add(saveCacheBtn);
        globalPanel.add(stopBtn);
        globalPanel.add(clearAllBtn);
        globalPanel.add(refreshStatsBtn);
        buttonsGridPanel.add(globalPanel, gbc);

        panel.add(buttonsGridPanel, BorderLayout.CENTER);

        // Назначение обработчиков (каждая кнопка запускает свой поток)
        reloadOracleViewsBtn.addActionListener(e -> startReloadInNewThread("oracle_views"));
        reloadPostgresViewsBtn.addActionListener(e -> startReloadInNewThread("postgres_views"));
        reloadOracleTablesBtn.addActionListener(e -> startReloadInNewThread("oracle_tables"));
        reloadPostgresTablesBtn.addActionListener(e -> startReloadInNewThread("postgres_tables"));
        reloadOracleFunctionsBtn.addActionListener(e -> startReloadInNewThread("oracle_functions"));
        reloadPostgresFunctionsBtn.addActionListener(e -> startReloadInNewThread("postgres_functions"));
        reloadOraclePackagesBtn.addActionListener(e -> startReloadInNewThread("oracle_packages"));
        reloadPostgresPackagesBtn.addActionListener(e -> startReloadInNewThread("postgres_packages"));
        reloadBrokersBtn.addActionListener(e -> startReloadInNewThread("brokers"));
        reloadOracleReportsBtn.addActionListener(e -> startReloadInNewThread("oracle_reports"));
        reloadPostgresReportsBtn.addActionListener(e -> startReloadInNewThread("postgres_reports"));
        reloadConstantsBtn.addActionListener(e -> startReloadInNewThread("constants"));
        reloadOptionsBtn.addActionListener(e -> startReloadInNewThread("options"));
        reloadMetadataBtn.addActionListener(e -> startReloadInNewThread("metadata"));
        reloadSequencesBtn.addActionListener(e -> startReloadInNewThread("sequences"));
        reloadTriggersBtn.addActionListener(e -> startReloadInNewThread("triggers"));
        reloadSynonymsBtn.addActionListener(e -> startReloadInNewThread("synonyms"));
        reloadAllBtn.addActionListener(e -> startReloadAll());
        saveCacheBtn.addActionListener(e -> saveCacheToDisk());
        stopBtn.addActionListener(e -> stopAllReloads());
        clearAllBtn.addActionListener(e -> clearAllCache());
        refreshStatsBtn.addActionListener(e -> refreshStats());

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Текущая статистика кэша"));

        statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statsArea.setRows(8);
        JScrollPane scrollPane = new JScrollPane(statsArea);
        scrollPane.setPreferredSize(new Dimension(0, 140));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JButton createCacheButton(String text, String tooltip, Color color) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setBackground(color);
        button.setForeground(Color.black);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 10f));
        button.setFocusPainted(false);
        return button;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Лог загрузки"));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(0, 255, 0));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        statusLabel = new JLabel("Готов к работе");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(statusLabel, BorderLayout.SOUTH);
        panel.add(progressPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshStats() {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Oracle View DDL: ").append(getCacheSize("oracleView")).append("\n");
            sb.append("PostgreSQL View DDL: ").append(getCacheSize("postgresView")).append("\n");
            sb.append("Oracle Table DDL: ").append(getCacheSize("oracleTable")).append("\n");
            sb.append("PostgreSQL Table DDL: ").append(getCacheSize("postgresTable")).append("\n");
            sb.append("Oracle Function Body: ").append(getCacheSize("oracleFunction")).append("\n");
            sb.append("PostgreSQL Function Body: ").append(getCacheSize("postgresFunction")).append("\n");
            sb.append("Broker ExecProc: ").append(getCacheSize("broker")).append("\n");
            sb.append("Oracle Reports: ").append(getCacheSize("oracleReport")).append("\n");
            sb.append("PostgreSQL Reports: ").append(getCacheSize("postgresReport")).append("\n");
            sb.append("Constants: ").append(getCacheSize("constant")).append("\n");
            sb.append("System Options: ").append(getCacheSize("systemOption")).append("\n");
            statsArea.setText(sb.toString());
        });
    }

    private String getCacheSize(String cacheName) {
        // Здесь нужно получить реальную статистику из DatabaseCacheManager
        // Пока возвращаем заглушку
        return "?";
    }

    private void saveCacheToDisk() {
        appendLog("=== ПРИНУДИТЕЛЬНОЕ СОХРАНЕНИЕ КЭША НА ДИСК ===");
        try {
            DatabaseCacheManager.forceSaveToDisk();
            appendLog("✅ Кэш успешно сохранён на диск");
            JOptionPane.showMessageDialog(this,
                    "Кэш успешно сохранён на диск!\n\nДиректория: " + settings.getOutputDir() + "/DatabaseCache",
                    "Сохранение кэша", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            appendLog("❌ Ошибка сохранения кэша: " + e.getMessage());
        }
    }

    /**
     * Запускает загрузку в новом параллельном потоке
     */
    private void startReloadInNewThread(String type) {
        appendLog("🚀 Запуск загрузки: " + getTypeName(type) + " (параллельный поток)");

        executor.submit(() -> {
            try {
                DatabaseCachePopulator populator = new DatabaseCachePopulator(settings);
                populator.setLogger(new ILogger() {
                    @Override public void log(String message) { appendLog(message); }
                    @Override public void error(String message) { appendLog("ОШИБКА: " + message); }
                    @Override public void debug(String message) { appendLog("[DEBUG] " + message); }
                });
                populator.setStopRequested(stopRequested);

                DatabaseCachePopulator.PopulateResult result = null;
                String operationName = getTypeName(type);

                updateProgress("Загрузка: " + operationName);

                switch (type) {
                    case "oracle_views":
                        result = populator.loadOracleViewsOnly();
                        break;
                    case "postgres_views":
                        result = populator.loadPostgresViewsOnly();
                        break;
                    case "oracle_tables":
                        result = populator.loadOracleTablesOnly();
                        break;
                    case "postgres_tables":
                        result = populator.loadPostgresTablesOnly();
                        break;
                    case "oracle_functions":
                        result = populator.loadOracleFunctionsOnly();
                        break;
                    case "postgres_functions":
                        result = populator.loadPostgresFunctionsOnly();
                        break;
                    case "oracle_packages":
                        result = populator.loadOraclePackagesOnly();
                        break;
                    case "postgres_packages":
                        result = populator.loadPostgresPackagesOnly();
                        break;
                    case "brokers":
                        result = populator.loadBrokersOnly();
                        break;
                    case "oracle_reports":
                        result = populator.loadOracleReportsOnly();
                        break;
                    case "postgres_reports":
                        result = populator.loadPostgresReportsOnly();
                        break;
                    case "constants":
                        result = populator.loadConstantsOnly();
                        break;
                    case "options":
                        result = populator.loadOptionsOnly();
                        break;
                    case "metadata":
                        result = populator.loadMetadataOnly();
                        break;
                    case "sequences":
                        result = populator.loadSequencesOnly();
                        break;
                    case "triggers":
                        result = populator.loadTriggersOnly();
                        break;
                    case "synonyms":
                        result = populator.loadSynonymsOnly();
                        break;
                }

                if (result != null) {
                    appendLog("✅ Загрузка завершена: " + operationName);
                    refreshStats();
                }

            } catch (Exception e) {
                appendLog("❌ КРИТИЧЕСКАЯ ОШИБКА в " + getTypeName(type) + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                updateProgress("Готов");
            }
        });
    }

    /**
     * Запускает последовательную загрузку ВСЕХ типов
     */
    private void startReloadAll() {
        appendLog("=== ЗАПУСК ПОЛНОЙ ПЕРЕЗАГРУЗКИ ВСЕХ ДАННЫХ ===");

        executor.submit(() -> {
            try {
                DatabaseCachePopulator populator = new DatabaseCachePopulator(settings);
                populator.setLogger(new ILogger() {
                    @Override public void log(String message) { appendLog(message); }
                    @Override public void error(String message) { appendLog("ОШИБКА: " + message); }
                    @Override public void debug(String message) { appendLog("[DEBUG] " + message); }
                });
                populator.setStopRequested(stopRequested);

                DatabaseCachePopulator.PopulateResult result = populator.populateAll();

                if (result != null) {
                    appendLog("=== ПОЛНАЯ ПЕРЕЗАГРУЗКА ЗАВЕРШЕНА ===");
                    appendLog(result.toString());
                    refreshStats();
                }

            } catch (Exception e) {
                appendLog("КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void stopAllReloads() {
        appendLog("⏹️ Запрос на остановку всех операций...");
        stopRequested.set(true);
        stopBtn.setEnabled(false);
        statusLabel.setText("Статус: Остановка...");

        // Сбрасываем флаг через некоторое время
        executor.submit(() -> {
            try {
                Thread.sleep(3000);
                stopRequested.set(false);
                SwingUtilities.invokeLater(() -> {
                    stopBtn.setEnabled(true);
                    statusLabel.setText("Статус: Готов");
                    appendLog("✅ Все операции остановлены");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void clearAllCache() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "ВНИМАНИЕ! Это действие удалит ВЕСЬ кэш.\n\n" +
                        "После удаления все данные придётся загружать заново.\n" +
                        "Это может занять продолжительное время.\n\n" +
                        "Вы уверены?",
                "Подтверждение очистки кэша",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseCacheManager.clearAll();
            DatabaseCacheManager.forceSaveToDisk();
            appendLog("✅ Весь кэш очищен!");
            refreshStats();
            JOptionPane.showMessageDialog(this,
                    "Кэш очищен. При следующем анализе данные будут загружены заново.",
                    "Кэш очищен", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void updateProgress(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Статус: " + status);
        });
    }

    private String getTypeName(String type) {
        switch (type) {
            case "oracle_views": return "Oracle вьюхи";
            case "postgres_views": return "PostgreSQL вьюхи";
            case "oracle_tables": return "Oracle таблицы";
            case "postgres_tables": return "PostgreSQL таблицы";
            case "oracle_functions": return "Oracle функции";
            case "postgres_functions": return "PostgreSQL функции";
            case "oracle_packages": return "Oracle пакеты";
            case "postgres_packages": return "PostgreSQL пакеты";
            case "brokers": return "брокеры";
            case "oracle_reports": return "Oracle отчёты";
            case "postgres_reports": return "PostgreSQL отчёты";
            case "constants": return "константы";
            case "options": return "системные опции";
            case "metadata": return "метаданные";
            case "sequences": return "последовательности";
            case "triggers": return "триггеры";
            case "synonyms": return "синонимы";
            case "all": return "ВСЕХ ДАННЫХ";
            default: return type;
        }
    }
}