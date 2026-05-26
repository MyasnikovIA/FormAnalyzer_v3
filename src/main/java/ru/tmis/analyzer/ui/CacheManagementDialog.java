// ui/CacheManagementDialog.java
package ru.tmis.analyzer.ui;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.core.cache.DatabaseCachePopulator;
import ru.tmis.analyzer.core.log.ILogger;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheManagementDialog extends JDialog {

    private final SettingsModel settings;
    private ExecutorService executor;
    private Future<?> currentTask;
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private volatile boolean isRunning = false;
    private volatile boolean isResumeMode = false;
    private final ConcurrentHashMap<String, Boolean> completedTasks = new ConcurrentHashMap<>();

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
    private JButton reloadAllNewBtn;
    private JButton reloadAllResumeBtn;
    private JButton saveCacheBtn;
    private JButton stopBtn;
    private JButton clearAllBtn;
    private JButton refreshStatsBtn;

    public CacheManagementDialog(JFrame parent, SettingsModel settings) {
        super(parent, "Управление кэшем БД", true);
        this.settings = settings;
        this.executor = Executors.newCachedThreadPool();

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

        // Панель с кнопками с прокруткой
        JPanel buttonsPanel = createButtonsPanel();
        JScrollPane buttonsScrollPane = new JScrollPane(buttonsPanel);
        buttonsScrollPane.setBorder(null);
        buttonsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        buttonsScrollPane.setPreferredSize(new Dimension(0, 400));
        mainPanel.add(buttonsScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        refreshStats();
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
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

        JPanel globalPanel = new JPanel(new GridLayout(2, 3, 10, 5));
        reloadAllNewBtn = createCacheButton("🔄 ЗАГРУЗИТЬ ВСЁ (С НАЧАЛА)", "Очистить кэш и загрузить все данные заново", new Color(244, 67, 54));
        reloadAllResumeBtn = createCacheButton("▶️ ПРОДОЛЖИТЬ ЗАГРУЗКУ", "Продолжить незавершённую загрузку", new Color(76, 175, 80));
        saveCacheBtn = createCacheButton("💾 СОХРАНИТЬ КЭШ", "Принудительно сохранить кэш на диск", new Color(0, 200, 83));
        stopBtn = createCacheButton("⏹️ ОСТАНОВИТЬ ВСЕ", "Прервать все текущие операции", new Color(158, 158, 158));
        clearAllBtn = createCacheButton("🗑️ ОЧИСТИТЬ ВСЁ", "Удалить весь кэш", new Color(244, 67, 54));
        refreshStatsBtn = createCacheButton("🔄 ОБНОВИТЬ СТАТИСТИКУ", "Обновить информацию о кэше", new Color(33, 150, 243));

        stopBtn.setEnabled(false);
        stopBtn.setBackground(new Color(158, 158, 158));
        stopBtn.setForeground(Color.BLACK);

        globalPanel.add(reloadAllNewBtn);
        globalPanel.add(reloadAllResumeBtn);
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
        reloadAllNewBtn.addActionListener(e -> startReloadAll(false));
        reloadAllResumeBtn.addActionListener(e -> startReloadAll(true));
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
        button.setForeground(Color.BLACK);
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

    private int getCacheSize(String cacheName) {
        switch (cacheName) {
            case "oracleView": return DatabaseCacheManager.getOracleViewDDLCacheSize();
            case "postgresView": return DatabaseCacheManager.getPostgresViewDDLCacheSize();
            case "oracleTable": return DatabaseCacheManager.getOracleTableDDLCacheSize();
            case "postgresTable": return DatabaseCacheManager.getPostgresTableDDLCacheSize();
            case "oracleFunction": return DatabaseCacheManager.getOracleFunctionBodyCacheSize();
            case "postgresFunction": return DatabaseCacheManager.getPostgresFunctionBodyCacheSize();
            case "broker": return DatabaseCacheManager.getBrokerExecProcCacheSize();
            case "oracleReport": return DatabaseCacheManager.getOracleReportsCacheSize();
            case "postgresReport": return DatabaseCacheManager.getPostgresReportsCacheSize();
            default: return 0;
        }
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

    // CacheManagementDialog.java

    /**
     * Запускает загрузку в новом параллельном потоке
     */
    private void startReloadInNewThread(String type) {
        appendLog("🚀 Запуск загрузки: " + getTypeName(type) + " (параллельный поток)");

        // Запускаем в отдельном потоке из пула
        executor.submit(() -> {
            Thread.currentThread().setName("CacheLoader-" + type);
            long startTime = System.currentTimeMillis();

            try {
                DatabaseCachePopulator populator = new DatabaseCachePopulator(settings);
                populator.setLogger(new ILogger() {
                    @Override
                    public void log(String message) {
                        appendLog("[" + type + "] " + message);
                    }
                    @Override
                    public void error(String message) {
                        appendLog("[" + type + "] ОШИБКА: " + message);
                    }
                    @Override
                    public void debug(String message) {
                        appendLog("[" + type + "] DEBUG: " + message);
                    }
                });
                populator.setStopRequested(stopRequested);
                populator.setProgressCallback(status -> {
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Статус: " + status));
                });

                DatabaseCachePopulator.PopulateResult result = null;
                String operationName = getTypeName(type);

                updateProgress("Загрузка: " + operationName);

                switch (type) {
                    // Oracle
                    case "oracle_views":
                        result = populator.loadOracleViewsOnly();
                        break;
                    case "oracle_tables":
                        result = populator.loadOracleTablesOnly();
                        break;
                    case "oracle_functions":
                        result = populator.loadOracleFunctionsOnly();
                        break;
                    case "oracle_packages":
                        result = populator.loadOraclePackagesOnly();
                        break;
                    case "oracle_reports":
                        result = populator.loadOracleReportsOnly();
                        break;

                    // PostgreSQL
                    case "postgres_views":
                        result = populator.loadPostgresViewsOnly();
                        break;
                    case "postgres_tables":
                        result = populator.loadPostgresTablesOnly();
                        break;
                    case "postgres_functions":
                        result = populator.loadPostgresFunctionsOnly();
                        break;
                    case "postgres_packages":
                        result = populator.loadPostgresPackagesOnly();
                        break;
                    case "postgres_reports":
                        result = populator.loadPostgresReportsOnly();
                        break;

                    // Общие
                    case "brokers":
                        result = populator.loadBrokersOnly();
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

                    default:
                        appendLog("❌ Неизвестный тип: " + type);
                        return;
                }

                long elapsed = System.currentTimeMillis() - startTime;

                if (result != null) {
                    appendLog("✅ Загрузка завершена: " + operationName + " за " + (elapsed / 1000) + " сек");
                    refreshStats();
                } else {
                    appendLog("⚠️ Загрузка завершена с пустым результатом: " + operationName);
                }

            } catch (Exception e) {
                if (stopRequested.get()) {
                    appendLog("⏹️ Загрузка прервана пользователем: " + getTypeName(type));
                } else {
                    appendLog("❌ КРИТИЧЕСКАЯ ОШИБКА в " + getTypeName(type) + ": " + e.getMessage());
                    e.printStackTrace();
                }
            } finally {
                updateProgress("Готов");
            }
        });
    }

    /**
     * Запускает параллельную загрузку ВСЕХ типов данных
     * @param resumeMode true - продолжает незавершённые загрузки, false - начинает заново
     */
    private void startReloadAll(boolean resumeMode) {
        if (isRunning && !resumeMode) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Загрузка уже выполняется. Остановить текущую и начать заново?",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            stopAllReloads();
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        this.isResumeMode = resumeMode;
        completedTasks.clear();

        appendLog("=== ЗАПУСК ПАРАЛЛЕЛЬНОЙ ЗАГРУЗКИ " + (resumeMode ? "(ПРОДОЛЖЕНИЕ)" : "(С НАЧАЛА)") + " ===");

        String[] allTypes = {
                "oracle_views", "postgres_views",
                "oracle_tables", "postgres_tables",
                "oracle_functions", "postgres_functions",
                "oracle_packages", "postgres_packages",
                "brokers",
                "oracle_reports", "postgres_reports",
                "constants", "options",
                "metadata", "sequences", "triggers", "synonyms"
        };

        AtomicInteger completedCount = new AtomicInteger(0);
        int totalTasks = allTypes.length;

        for (String type : allTypes) {
            // Проверяем, нужно ли запускать этот тип
            if (resumeMode && isTypeFullyLoaded(type)) {
                appendLog("⏭️ [" + type + "] Пропущен (уже полностью загружен)");
                int completed = completedCount.incrementAndGet();
                appendLog("📊 Прогресс: " + completed + " из " + totalTasks + " потоков завершено");
                continue;
            }

            executor.submit(() -> {
                Thread.currentThread().setName("CacheLoader-" + type);
                long startTime = System.currentTimeMillis();

                try {
                    appendLog("🚀 [" + type + "] Начало загрузки " + (resumeMode ? "(продолжение)" : ""));

                    DatabaseCachePopulator populator = new DatabaseCachePopulator(settings);
                    populator.setLogger(new ILogger() {
                        @Override public void log(String message) { appendLog("[" + type + "] " + message); }
                        @Override public void error(String message) { appendLog("[" + type + "] ОШИБКА: " + message); }
                        @Override public void debug(String message) { appendLog("[" + type + "] DEBUG: " + message); }
                    });
                    populator.setStopRequested(stopRequested);

                    DatabaseCachePopulator.PopulateResult result = null;

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

                    long elapsed = System.currentTimeMillis() - startTime;
                    completedTasks.put(type, true);
                    appendLog("✅ [" + type + "] Загрузка завершена за " + (elapsed / 1000) + " сек");

                } catch (Exception e) {
                    if (stopRequested.get()) {
                        appendLog("⏹️ [" + type + "] Загрузка прервана пользователем");
                        completedTasks.put(type, false);
                    } else {
                        appendLog("❌ [" + type + "] КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
                    }
                } finally {
                    int completed = completedCount.incrementAndGet();
                    appendLog("📊 Прогресс: " + completed + " из " + totalTasks + " потоков завершено");

                    if (completed == totalTasks) {
                        SwingUtilities.invokeLater(() -> {
                            appendLog("🎉 ВСЕ ПОТОКИ ЗАВЕРШЕНЫ!");
                            appendLog("=== ПАРАЛЛЕЛЬНАЯ ЗАГРУЗКА ЗАВЕРШЕНА ===");
                            refreshStats();
                            progressBar.setIndeterminate(false);
                            progressBar.setValue(100);
                            statusLabel.setText("Статус: Все потоки завершены");
                            isRunning = false;
                        });
                    }
                }
            });
        }

        updateProgress("Запущено " + totalTasks + " параллельных потоков...");
        progressBar.setIndeterminate(true);
        isRunning = true;
    }

    /**
     * Проверяет, полностью ли загружен данный тип
     */
    private boolean isTypeFullyLoaded(String type) {
        switch (type) {
            case "oracle_views":
                return getCacheSize("oracleView") > 0;
            case "postgres_views":
                return getCacheSize("postgresView") > 0;
            case "oracle_tables":
                return getCacheSize("oracleTable") > 0;
            case "postgres_tables":
                return getCacheSize("postgresTable") > 0;
            case "oracle_functions":
                return getCacheSize("oracleFunction") > 0;
            case "postgres_functions":
                return getCacheSize("postgresFunction") > 0;
            case "oracle_packages":
                return getCacheSize("oraclePackage") > 0;
            case "brokers":
                return getCacheSize("broker") > 0;
            case "oracle_reports":
                return getCacheSize("oracleReport") > 0;
            case "postgres_reports":
                return getCacheSize("postgresReport") > 0;
            case "constants":
                return getCacheSize("constant") > 0;
            case "options":
                return getCacheSize("systemOption") > 0;
            default:
                return false;
        }
    }

    private void stopAllReloads() {
        if (!isRunning) return;

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
                    isRunning = false;
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