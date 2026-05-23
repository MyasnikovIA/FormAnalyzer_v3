package ru.tmis.analyzer.ui;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.core.cache.FormCache;
import ru.tmis.analyzer.core.cache.FormCacheManager;
import ru.tmis.analyzer.core.cache.InMemoryReportBuffer;
import ru.tmis.analyzer.core.db.DatabaseAvailabilityService;
import ru.tmis.analyzer.core.db.DatabaseConnectionManager;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.report.CSVReportGenerator;
import ru.tmis.analyzer.core.report.ReportGenerator;
import ru.tmis.analyzer.core.service.FileScannerService;
import ru.tmis.analyzer.core.service.FormAnalyzerService;
import ru.tmis.analyzer.core.service.ParallelRecursiveReportBuilder;
import ru.tmis.analyzer.core.service.RecursiveReportBuilder;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MainWindow extends JFrame {

    private final SettingsModel settings;
    private final AppConfig config;

    private FormsTreePanel formsTreePanel;
    private JTextArea logArea;
    private JTextArea resultArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton startButton;
    private JButton stopButton;
    private JButton settingsButton;

    private ExecutorService executor;
    private Future<?> currentTask;
    private volatile boolean stopRequested = false;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private ExecutorService executorService;
    private RecursiveReportBuilder recursiveBuilder;

    private JTextArea llmPromptArea;  // Текстовая область для LLM промпта
    private JTabbedPane tabbedPane;   // Переместить в поле класса

    private PrintStream originalOut;
    private PrintStream originalErr;
    private PipedInputStream pipeIn;
    private Thread logReader;
    private ParallelRecursiveReportBuilder parallelBuilder;
    private Timer progressTimer;
    private long scanStartTime;
    private final FormCacheManager formCacheManager = FormCacheManager.getInstance();
    private JButton pauseButton;
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final Object pauseLock = new Object();


    public MainWindow(SettingsModel settings, AppConfig config) {
        this.settings = settings;
        this.config = config;
        this.executor = Executors.newSingleThreadExecutor();
        this.executorService = Executors.newSingleThreadExecutor();

        initUI();
        loadWindowState();

        preloadFormsToCache();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveState();
            }
        });
        redirectSystemOutToLog();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                restoreSystemOut();
                saveState();
            }
        });
    }

    private void initUI() {
        setTitle("TMIS Form Analyzer v2.0.16 (от 22-05-2026) server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Анализатор форм T-MIS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        settingsButton = new JButton("Настройки");
        settingsButton.addActionListener(e -> openSettings());

        startButton = new JButton("Запуск анализа");
        startButton.setBackground(new Color(76, 175, 80));
        startButton.addActionListener(e -> startAnalysis());

        // НОВАЯ КНОПКА ПАУЗЫ
        pauseButton = new JButton("⏸ Пауза");
        pauseButton.setEnabled(false);
        pauseButton.setBackground(new Color(255, 193, 7));
        pauseButton.addActionListener(e -> togglePause());

        // КНОПКА ПАРАЛЛЕЛЬНОГО СКАНИРОВАНИЯ ПРОЕКТА
        JButton parallelScanButton = new JButton("🚀 Параллельное сканирование проекта");
        parallelScanButton.setBackground(new Color(33, 150, 243));
        parallelScanButton.addActionListener(e -> startParallelFullProjectScan());

        stopButton = new JButton("Остановка");
        stopButton.setEnabled(false);
        stopButton.setBackground(new Color(244, 67, 54));
        stopButton.addActionListener(e -> stopAnalysis());

        JButton openOutputButton = new JButton("Открыть отчеты");
        openOutputButton.addActionListener(e -> openOutputDirectory());

        // ДОБАВЛЯЕМ ВСЕ КНОПКИ В ПАНЕЛЬ
        buttonPanel.add(settingsButton);
        buttonPanel.add(openOutputButton);
        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(parallelScanButton);  // ← ЭТА СТРОЧКА БЫЛА ПРОПУЩЕНА!
        buttonPanel.add(stopButton);

        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }


    private JSplitPane createCenterPanel() {
        // Left panel - forms tree
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Список форм для анализа"));

        formsTreePanel = new FormsTreePanel();
        formsTreePanel.setOnFormsChanged(() -> {
        });

        formsTreePanel.addTreeSelectionListener(new TreeSelectionListener() {
            private boolean isLoadingChildren = false;

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (isLoadingChildren) return;

                TreePath selectedPath = formsTreePanel.getSelectedPath();
                if (selectedPath != null) {
                    String formPath = formsTreePanel.getFormPathFromTreePath(selectedPath);
                    if (formPath != null) {
                        isLoadingChildren = true;
                        try {
                            // Загружаем отчёт текущей формы
                            loadFormResultToPanel(formPath);

                            // ТОЛЬКО ОДИН УРОВЕНЬ - НЕ РЕКУРСИВНО!
                            formsTreePanel.refreshChildForms(formPath);
                            if (selectedPath != null) {
                                formsTreePanel.expandPath(selectedPath);
                            }

                            if (tabbedPane.getSelectedIndex() == 2) {
                                loadLlmPromptToPanel(formPath);
                            }

                        } finally {
                            isLoadingChildren = false;
                        }
                    } else {
                        resultArea.setText("");
                    }
                } else {
                    resultArea.setText("");
                }
            }
        });
        formsTreePanel.setOutputDir(settings.getOutputDir());
        formsTreePanel.setOnAnalysisRequested(() -> {
            if (!isRunning.get()) {
                startAnalysis();
            } else {
                appendLog("Анализ уже выполняется");
                JOptionPane.showMessageDialog(this,
                        "Анализ уже выполняется. Дождитесь завершения.",
                        "Анализ запущен",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        leftPanel.add(formsTreePanel, BorderLayout.CENTER);

        formsTreePanel.setOnRecursiveAnalysisRequested(() -> {
            // Используем параллельный билдер вместо обычного
            if (!parallelBuilder.isRunning()) {
                startParallelRecursiveAnalysis();
            } else {
                appendLog("Параллельный рекурсивный анализ уже выполняется");
                JOptionPane.showMessageDialog(this,
                        "Параллельный рекурсивный анализ уже выполняется. Дождитесь завершения или нажмите Стоп.",
                        "Анализ запущен",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // ИНИЦИАЛИЗАЦИЯ ПАРАЛЛЕЛЬНОГО БИЛДЕРА
        parallelBuilder = new ParallelRecursiveReportBuilder(settings, config, formsTreePanel);
        parallelBuilder.setLogger(new ILogger() {
            @Override
            public void log(String message) {
                appendLog(message);
            }

            @Override
            public void error(String message) {
                appendLog("ОШИБКА: " + message);
            }

            @Override
            public void debug(String message) {
                appendLog("[DEBUG] " + message);
            }
        });

        // Устанавливаем количество потоков из настроек (по умолчанию все ядра)
        int threads = Runtime.getRuntime().availableProcessors(); // значение по умолчанию
        if (config != null) {
            threads = config.getParallelThreads();
            if (threads <= 0) {
                threads = Runtime.getRuntime().availableProcessors();
            }
        }
        parallelBuilder.setParallelThreads(threads);
        appendLog("Параллельный анализатор инициализирован. Потоков: " + threads);

        parallelBuilder.setOnFormAnalyzed(formPath -> {
            appendLog("  [Поток] Анализ формы: " + formPath);
        });

        parallelBuilder.setOnComplete(() -> {
            SwingUtilities.invokeLater(() -> {
                if (progressTimer != null) {
                    progressTimer.stop();
                }
                appendLog("Параллельный рекурсивный анализ завершён!");
                statusLabel.setText("Статус: Готов");
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                progressBar.setString("Готово");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                settingsButton.setEnabled(true);

                // Обновляем дерево с сохранением состояния
                FormsTreePanel.TreeState state = formsTreePanel.saveTreeState();
                formsTreePanel.refreshAllChildFormsWithCleanup();
                if (state != null && state.expandedPaths != null) {
                    for (String pathStr : state.expandedPaths) {
                        formsTreePanel.expandPathByDisplayString(pathStr);
                    }
                }
                if (state != null && state.selectedPath != null) {
                    formsTreePanel.restoreSelectedPath(state.selectedPath);
                }
                appendLog("Дерево форм обновлено, состояние восстановлено");
            });
        });

        parallelBuilder.setOnError(message -> {
            SwingUtilities.invokeLater(() -> {
                appendLog("ОШИБКА: " + message);
                statusLabel.setText("Статус: Ошибка");
                progressBar.setIndeterminate(false);
                progressBar.setValue(0);
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                settingsButton.setEnabled(true);
            });
        });

        // Обычный рекурсивный билдер больше не нужен, но оставим для совместимости
        recursiveBuilder = new RecursiveReportBuilder(settings, config, formsTreePanel);
        recursiveBuilder.setLogger(new ILogger() {
            @Override
            public void log(String message) {
                appendLog(message);
            }

            @Override
            public void error(String message) {
                appendLog("ОШИБКА: " + message);
            }

            @Override
            public void debug(String message) {
                appendLog("[DEBUG] " + message);
            }
        });

        // Right panel with tabs
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Результаты"));

        tabbedPane = new JTabbedPane();

        // Tab 1: Лог процесса
        JPanel logPanel = new JPanel(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(0, 255, 0));
        JScrollPane logScroll = new JScrollPane(logArea);
        logPanel.add(logScroll, BorderLayout.CENTER);

        JPanel logButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearLogBtn = new JButton("Очистить лог");
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        JButton saveLogBtn = new JButton("Сохранить лог");
        saveLogBtn.addActionListener(e -> saveLog());
        logButtons.add(saveLogBtn);
        logButtons.add(clearLogBtn);
        logPanel.add(logButtons, BorderLayout.NORTH);

        tabbedPane.addTab("Лог процесса", logPanel);

        // Tab 2: Результат (отчёт)
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBackground(Color.WHITE);
        resultArea.setForeground(Color.BLACK);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultPanel.add(resultScroll, BorderLayout.CENTER);

        JPanel resultButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearResultBtn = new JButton("Очистить результат");
        clearResultBtn.addActionListener(e -> resultArea.setText(""));

        JButton deleteReportBtn = new JButton("Удалить отчёт");
        deleteReportBtn.setBackground(new Color(244, 67, 54));
        deleteReportBtn.addActionListener(e -> deleteReportForCurrentForm());

        JButton saveResultBtn = new JButton("Сохранить результат в файл");
        saveResultBtn.addActionListener(e -> saveResultToFile());

        resultButtons.add(deleteReportBtn);
        resultButtons.add(saveResultBtn);
        resultButtons.add(clearResultBtn);
        resultPanel.add(resultButtons, BorderLayout.NORTH);

        tabbedPane.addTab("Результат", resultPanel);

        // Tab 3: LLM промпт
        JPanel llmPanel = new JPanel(new BorderLayout());
        llmPromptArea = new JTextArea();
        llmPromptArea.setEditable(false);
        llmPromptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        llmPromptArea.setBackground(new Color(245, 245, 245));
        llmPromptArea.setForeground(Color.BLACK);
        JScrollPane llmScroll = new JScrollPane(llmPromptArea);
        llmPanel.add(llmScroll, BorderLayout.CENTER);

        JPanel llmButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearLlmBtn = new JButton("Очистить");
        clearLlmBtn.addActionListener(e -> llmPromptArea.setText(""));
        JButton saveLlmBtn = new JButton("Сохранить MD файл");
        saveLlmBtn.addActionListener(e -> saveLlmPromptToFile());
        JButton regenerateLlmBtn = new JButton("Перегенерировать промпт");
        regenerateLlmBtn.addActionListener(e -> regenerateLlmPrompt());
        llmButtons.add(saveLlmBtn);
        llmButtons.add(regenerateLlmBtn);
        llmButtons.add(clearLlmBtn);
        llmPanel.add(llmButtons, BorderLayout.NORTH);

        if (config != null && config.isEnableLLMExport() && config.isLlmPanelVisible()) {
            tabbedPane.addTab("LLM промпт", llmPanel);
        }

        tabbedPane.addChangeListener(e -> {
            TreePath selectedPath = formsTreePanel.getSelectedPath();
            if (selectedPath != null && tabbedPane.getSelectedIndex() == 2) {
                String formPath = formsTreePanel.getFormPathFromTreePath(selectedPath);
                if (formPath != null) {
                    loadLlmPromptToPanel(formPath);
                }
            }
        });

        rightPanel.add(tabbedPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(400);

        return splitPane;
    }

    private void startRecursiveAnalysis() {
        // Блокируем повторный запуск
        if (recursiveBuilder.isRunning()) {
            appendLog("Рекурсивный анализ уже выполняется");
            return;
        }

        // Дополнительная проверка по флагу isRunning из MainWindow
        if (isRunning.get()) {
            appendLog("Анализ уже выполняется, дождитесь завершения");
            return;
        }

        // Получаем выбранные формы или все корневые
        List<String> selectedForms = formsTreePanel.getSelectedForms();
        List<String> startForms;

        if (selectedForms.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Не выбрано ни одной формы.\nЗапустить рекурсивный анализ для всех форм?",
                    "Нет выбранных форм",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            startForms = null;
        } else {
            startForms = selectedForms;
        }

        // Управление кнопками
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        settingsButton.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Статус: Рекурсивный анализ...");

        recursiveBuilder.startRecursiveBuild(startForms);
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Прогресс"));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        statusLabel = new JLabel("Готов к работе");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel pauseIndicator = new JLabel();
        pauseIndicator.setFont(new Font("Monospaced", Font.BOLD, 12));

        // Таймер для обновления индикатора паузы
        Timer pauseIndicatorTimer = new Timer(500, e -> {
            if (paused.get()) {
                pauseIndicator.setText("⏸ ПАУЗА");
                pauseIndicator.setForeground(new Color(255, 140, 0));
            } else {
                pauseIndicator.setText("");
            }
        });
        pauseIndicatorTimer.start();

        panel.add(progressBar, BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statusLabel, BorderLayout.WEST);
        southPanel.add(pauseIndicator, BorderLayout.EAST);
        panel.add(southPanel, BorderLayout.SOUTH);

        panel.add(statusLabel, BorderLayout.SOUTH);
        JLabel memoryLabel = new JLabel();
        memoryLabel.setFont(new Font("Monospaced", Font.PLAIN, 10));
        memoryLabel.setForeground(Color.GRAY);

        // Таймер обновления памяти каждые 5 секунд
        Timer memoryTimer = new Timer(5000, e -> {
            Runtime rt = Runtime.getRuntime();
            long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
            long max = rt.maxMemory() / (1024 * 1024);
            memoryLabel.setText(String.format("RAM: %d/%d MB", used, max));
        });
        memoryTimer.start();

        panel.add(memoryLabel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Загружает сохранённый отчёт для выбранной формы в панель результата
     */
    private void loadFormResultToPanel(String formPath) {
        String reportPath = formsTreePanel.getReportFilePath(formPath);
        File reportFile = new File(reportPath);

        if (reportFile.exists()) {
            // Загружаем в отдельном потоке с таймаутом
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    return new String(Files.readAllBytes(reportFile.toPath()),
                            java.nio.charset.StandardCharsets.UTF_8);
                }

                @Override
                protected void done() {
                    try {
                        String content = get();
                        resultArea.setText(content);
                        resultArea.setCaretPosition(0);
                        appendLog("Загружен отчёт для формы: " + formPath);
                        formsTreePanel.refreshChildForms(formPath);
                    } catch (Exception e) {
                        resultArea.setText("Ошибка загрузки отчёта: " + e.getMessage());
                        appendLog("Ошибка загрузки отчёта: " + e.getMessage());
                    }
                }
            };
            worker.execute();
        } else {
            resultArea.setText("Отчёт для формы не найден.\n\nПуть: " + reportPath + "\n\nЗапустите анализ для создания отчёта.");
            formsTreePanel.clearChildNodes(formPath);
            appendLog("Отчёт не найден для формы: " + formPath + ", дочерние элементы очищены");
        }
    }

    /**
     * Преобразует путь формы в безопасное имя файла
     */
    private String getSafeFileName(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        String safeName = normalized.replace("/", "#").replace("\\", "#");
        return safeName + ".txt";
    }

    /**
     * Запускает анализ выбранных форм
     */


    // Вспомогательный метод для короткого имени формы
    private String getShortFormName(String formPath) {
        if (formPath == null) return "";
        int lastSlash = Math.max(formPath.lastIndexOf("/"), formPath.lastIndexOf("\\"));
        if (lastSlash >= 0) {
            return formPath.substring(lastSlash + 1);
        }
        return formPath;
    }
    /**
     * Запускает анализ выбранных форм
     */
    /**
     * Запускает анализ выбранных форм
     */
    private void startAnalysis() {
        ensureFormsPreloaded();
        paused.set(false);
        pauseButton.setText("⏸ Пауза");
        pauseButton.setEnabled(true);
        pauseButton.setBackground(new Color(255, 193, 7));
        if (!checkDatabaseAvailabilityBeforeAnalysis()) {
            return;
        }

        if (isRunning.get()) {
            appendLog("Анализ уже выполняется");
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Анализ уже выполняется. Остановить текущий и запустить новый?",
                    "Анализ запущен",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                stopAnalysis();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                return;
            }
        }
        stopRequested = false;

        // Получаем выбранные формы
        List<String> selectedForms = formsTreePanel.getSelectedForms();
        Set<String> normalizedForms = new LinkedHashSet<>();

        for (String form : selectedForms) {
            String normalized = form;
            if (normalized.contains("/Forms/") && normalized.lastIndexOf("/Forms/") > 0) {
                normalized = normalized.substring(normalized.lastIndexOf("/Forms/") + 1);
            }
            if (normalized.startsWith("/")) normalized = normalized.substring(1);
            if (!normalized.startsWith("Forms/") && !normalized.startsWith("UserForms")) {
                normalized = "Forms/" + normalized;
            }
            normalizedForms.add(normalized);
        }

        if (normalizedForms.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Не выбрано ни одной формы.\n\nВыделить все формы в дереве и запустить анализ?",
                    "Выделить все формы",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                appendLog("Анализ отменён пользователем");
                return;
            }
            formsTreePanel.selectAllNodes();
            selectedForms = formsTreePanel.getSelectedForms();
            normalizedForms.clear();
            for (String form : selectedForms) {
                String normalized = form;
                if (normalized.contains("/Forms/") && normalized.lastIndexOf("/Forms/") > 0) {
                    normalized = normalized.substring(normalized.lastIndexOf("/Forms/") + 1);
                }
                if (normalized.startsWith("/")) normalized = normalized.substring(1);
                if (!normalized.startsWith("Forms/") && !normalized.startsWith("UserForms")) {
                    normalized = "Forms/" + normalized;
                }
                normalizedForms.add(normalized);
            }
            if (normalizedForms.isEmpty()) {
                appendLog("Нет форм для анализа");
                return;
            }
            appendLog("Выделено форм для анализа: " + normalizedForms.size());
        }

        appendLog("Выбрано форм для анализа: " + normalizedForms.size());
        for (String form : normalizedForms) appendLog("  - " + form);

        // ========== ОПТИМИЗИРОВАННЫЙ ПАРАЛЛЕЛЬНЫЙ АНАЛИЗ ==========
        // Получаем список форм для анализа
        List<String> formsList = new ArrayList<>(normalizedForms);

        // Количество потоков из настроек
        int threads = config.getParallelThreads();
        if (threads <= 0) {
            threads = Runtime.getRuntime().availableProcessors();
        }

        appendLog("=== ПАРАЛЛЕЛЬНЫЙ АНАЛИЗ ===");
        appendLog("Доступно ядер: " + Runtime.getRuntime().availableProcessors());
        appendLog("Используется потоков: " + threads);
        appendLog("Всего форм: " + formsList.size());

        stopRequested = false;
        isRunning.set(true);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        settingsButton.setEnabled(false);
        pauseButton.setEnabled(true);

        progressBar.setValue(0);
        progressBar.setIndeterminate(false);
        statusLabel.setText("Статус: Анализ...");

        long startTime = System.currentTimeMillis();

        // ✅ ОДИН анализатор для всех форм
        FormAnalyzerService analyzer = new FormAnalyzerService(settings, config);
        analyzer.setStopCondition(() -> stopRequested);
        analyzer.setPaused(() -> MainWindow.this.paused.get());

        analyzer.setLogger(new ILogger() {
            @Override public void log(String msg) { appendLog(msg); }
            @Override public void error(String msg) { appendLog("ОШИБКА: " + msg); }
            @Override public void debug(String msg) { appendLog("[DEBUG] " + msg); }
        });

        // Устанавливаем формы для анализа
        Set<String> formsSet = new LinkedHashSet<>(formsList);
        analyzer.setFormsToAnalyze(formsSet);

        // Устанавливаем callback для прогресса
        analyzer.setProgressCallback((processed, total, currentForm) -> {
            SwingUtilities.invokeLater(() -> {
                int percent = (processed * 100) / total;
                progressBar.setValue(percent);
                progressBar.setString(String.format("%d из %d (%d%%) - %s",
                        processed, total, percent, getShortFormName(currentForm)));
                statusLabel.setText("Статус: " + getShortFormName(currentForm));
            });
        });

        // Устанавливаем callback для сохранения отчётов
        analyzer.setFormAnalyzedCallback(formInfo -> {
            try {
                ReportGenerator reportGen = new ReportGenerator(settings.getOutputDir(), config);
                reportGen.createMainReportHeader();
                reportGen.appendFormToMainReport(formInfo);
                SwingUtilities.invokeLater(() -> {
                    formsTreePanel.refreshChildForms(formInfo.getFormPath());
                });
            } catch (IOException e) {
                appendLog("Ошибка сохранения отчёта: " + e.getMessage());
            }
        });

        // Запускаем анализ в отдельном потоке
        final int finalThreads = threads;
        currentTask = executorService.submit(() -> {
            try {
                List<FormInfo> results = analyzer.analyzeAllForms(finalThreads);

                long elapsed = (System.currentTimeMillis() - startTime) / 1000;

                if (stopRequested) {
                    appendLog("Анализ остановлен пользователем");
                    return;
                }

                // Генерация CSV отчета
                try {
                    CSVReportGenerator csvGen = new CSVReportGenerator(settings.getOutputDir());
                    Path csvPath = csvGen.generateCSVReport(results);
                    appendLog("CSV отчет сохранен: " + csvPath);
                } catch (IOException e) {
                    appendLog("Ошибка сохранения CSV отчета: " + e.getMessage());
                }

                appendLog("");
                appendLog("=".repeat(60));
                appendLog("=== АНАЛИЗ ЗАВЕРШЕН ===");
                appendLog("Время выполнения: " + elapsed + " сек");
                appendLog("Обработано форм: " + results.size());
                appendLog("=".repeat(60));
                appendLog("Отчеты сохранены в: " + settings.getOutputDir());

                SwingUtilities.invokeLater(() -> {
                    formsTreePanel.refreshAllChildForms();
                    formsTreePanel.refreshTreeWithState();
                });

            } catch (Exception e) {
                appendLog("ОШИБКА: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isRunning.set(false);
                analyzer.shutdown();
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    settingsButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                    statusLabel.setText("Статус: Готов");
                    progressBar.setString("Анализ завершен");
                });
            }
        });
    }

    /**
     * Запускает полное сканирование проекта и анализ всех форм
     */
    /**
     * Запускает полное сканирование проекта и анализ новых форм
     */
    private void startFullProjectScan() {
        // ========== ПРОВЕРКА ДОСТУПНОСТИ БД ==========
        if (!checkDatabaseAvailabilityBeforeAnalysis()) {
            return; // Пользователь отменил анализ
        }
        // =============================================

        stopRequested = false;
        isRunning.set(true);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        settingsButton.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Статус: Сканирование проекта...");

        currentTask = executorService.submit(() -> {
            try {
                appendLog("=== СКАНИРОВАНИЕ ВСЕГО ПРОЕКТА ===");
                appendLog("Будут обработаны только формы, для которых ещё нет отчётов");

                FormAnalyzerService analyzer = new FormAnalyzerService(settings, config);

                analyzer.setLogger(new ILogger() {
                    @Override
                    public void log(String message) {
                        appendLog(message);
                    }

                    @Override
                    public void error(String message) {
                        appendLog("ОШИБКА: " + message);
                    }

                    @Override
                    public void debug(String message) {
                        appendLog("[DEBUG] " + message);
                    }
                });

                analyzer.setStopCondition(() -> stopRequested);
                analyzer.setProgressCallback((processed, total, currentForm) -> {
                    SwingUtilities.invokeLater(() -> {
                        int percent = total > 0 ? (processed * 100 / total) : 0;
                        progressBar.setValue(percent);
                        progressBar.setString(String.format("%d из %d (%d%%)", processed, total, percent));
                        statusLabel.setText("Статус: " + currentForm);
                    });
                });

                analyzer.setFormAnalyzedCallback(formInfo -> {
                    try {
                        ReportGenerator reportGen = new ReportGenerator(settings.getOutputDir(), config);
                        reportGen.createMainReportHeader();
                        reportGen.appendFormToMainReport(formInfo);
                        SwingUtilities.invokeLater(() -> {
                            formsTreePanel.refreshChildForms(formInfo.getFormPath());
                        });
                    } catch (IOException e) {
                        appendLog("Ошибка сохранения отчёта: " + e.getMessage());
                    }
                });

                // Сканируем и анализируем только новые формы
                List<FormInfo> results = analyzer.scanAllFormsAndAnalyze();
                analyzer.clearFormsToAnalyze();

                if (stopRequested) {
                    appendLog("Анализ остановлен пользователем");
                    return;
                }

                // Генерация CSV отчета
                try {
                    CSVReportGenerator csvGen = new CSVReportGenerator(settings.getOutputDir());
                    Path csvPath = csvGen.generateCSVReport(results);
                    appendLog("CSV отчет сохранен: " + csvPath);
                } catch (IOException e) {
                    appendLog("Ошибка сохранения CSV отчета: " + e.getMessage());
                }

                appendLog("");
                appendLog("=== ГЕНЕРАЦИЯ ОТЧЕТОВ ===");
                appendLog("=== АНАЛИЗ ЗАВЕРШЕН ===");
                appendLog("Обработано новых форм: " + results.size());
                appendLog("Отчеты сохранены в: " + settings.getOutputDir());

                SwingUtilities.invokeLater(() -> {
                    formsTreePanel.refreshAllChildForms();
                });

                // Показываем сообщение о завершении
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Анализ завершен!\n" +
                                    "Обработано новых форм: " + results.size(),
                            "Анализ завершен",
                            JOptionPane.INFORMATION_MESSAGE);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    appendLog("ОШИБКА: " + e.getMessage());
                    e.printStackTrace();
                });
            } finally {
                isRunning.set(false);
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    settingsButton.setEnabled(true);
                    statusLabel.setText("Статус: Готов");
                    progressBar.setIndeterminate(false);
                    progressBar.setString("Анализ завершен");
                });
            }
        });
    }

    private void runAnalysis(List<String> formsToAnalyze) throws Exception {
        appendLog("=".repeat(80));
        appendLog("=== ЗАПУСК АНАЛИЗА ФОРМ ===");
        appendLog("=".repeat(80));
        appendLog("Путь к проекту: " + settings.getProjectPath());
        appendLog("Всего форм для анализа: " + formsToAnalyze.size());
        appendLog("");


        ru.tmis.analyzer.core.service.FormAnalyzerService analyzer =
                new ru.tmis.analyzer.core.service.FormAnalyzerService(settings, config);

        Set<String> formsSet = new LinkedHashSet<>(formsToAnalyze);
        analyzer.setFormsToAnalyze(formsSet);

        analyzer.setLogger(new ILogger() {
            @Override
            public void log(String message) {
                appendLog(message);
            }

            @Override
            public void error(String message) {
                appendLog("ОШИБКА: " + message);
            }

            @Override
            public void debug(String message) {
                appendLog("[DEBUG] " + message);
            }
        });

        // Устанавливаем условие остановки
        analyzer.setStopCondition(() -> stopRequested);

        analyzer.setProgressCallback((processed, total, currentForm) -> {
            // Проверка остановки
            if (stopRequested) return;

            SwingUtilities.invokeLater(() -> {
                int percent = total > 0 ? (processed * 100 / total) : 0;
                progressBar.setValue(percent);
                progressBar.setString(String.format("%d из %d (%d%%)", processed, total, percent));
                statusLabel.setText("Статус: " + currentForm);
            });
        });

        analyzer.setFormAnalyzedCallback(formInfo -> {
            // Проверка остановки
            if (stopRequested) return;

            try {
                ru.tmis.analyzer.core.report.ReportGenerator reportGen =
                        new ru.tmis.analyzer.core.report.ReportGenerator(settings.getOutputDir(), config);
                reportGen.createMainReportHeader();
                reportGen.appendFormToMainReport(formInfo);

                SwingUtilities.invokeLater(() -> {
                    formsTreePanel.refreshChildForms(formInfo.getFormPath());
                });
            } catch (IOException e) {
                appendLog("Ошибка сохранения отчёта: " + e.getMessage());
            }
        });

        List<FormInfo> results = analyzer.analyzeAllForms();
        try {
            analyzer.clearFormsToAnalyze();

            // Проверка остановки перед генерацией CSV
            if (stopRequested) {
                appendLog("Анализ остановлен пользователем");
                return;
            }

            try {
                CSVReportGenerator csvGen = new CSVReportGenerator(settings.getOutputDir());
                Path csvPath = csvGen.generateCSVReport(results);
                appendLog("CSV отчет сохранен: " + csvPath);
            } catch (IOException e) {
                appendLog("Ошибка сохранения CSV отчета: " + e.getMessage());
            }

            if (stopRequested) {
                appendLog("Анализ остановлен пользователем");
                if (!results.isEmpty()) {
                    appendLog("Сохранено частичных результатов: " + results.size() + " форм");
                }
                return;
            }

            appendLog("");
            appendLog("=== ГЕНЕРАЦИЯ ОТЧЕТОВ ===");
            appendLog("=== АНАЛИЗ ЗАВЕРШЕН ===");
            appendLog("Обработано форм: " + results.size());
            appendLog("Отчеты сохранены в: " + settings.getOutputDir());

            SwingUtilities.invokeLater(() -> {
                formsTreePanel.refreshAllChildForms();
            });

        } finally {
            // Гарантированно сбрасываем флаг при любом исходе
            isRunning.set(false);
            analyzer.clearFormsToAnalyze();
        }
    }

    private void stopAnalysis() {
        paused.set(false);
        pauseButton.setEnabled(false);
        pauseButton.setText("⏸ Пауза");
        // Останавливаем таймер прогресса
        if (progressTimer != null && progressTimer.isRunning()) {
            progressTimer.stop();
        }

        // Сначала проверяем параллельный рекурсивный анализатор
        if (parallelBuilder != null && parallelBuilder.isRunning()) {
            parallelBuilder.forceStop();
            appendLog("Остановка параллельного рекурсивного анализа...");

            // Сбрасываем флаги MainWindow
            isRunning.set(false);
            stopRequested = true;

            SwingUtilities.invokeLater(() -> {
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                settingsButton.setEnabled(true);
                progressBar.setIndeterminate(false);
                progressBar.setValue(0);
                progressBar.setString("Анализ остановлен");
                statusLabel.setText("Статус: Анализ остановлен");
            });
            return;
        }

        // Затем проверяем обычный анализ (через executorService)
        if (isRunning.get()) {
            appendLog("Запрос на остановку анализа...");
            stopRequested = true;

            // Останавливаем текущую задачу
            if (currentTask != null && !currentTask.isDone()) {
                currentTask.cancel(true);
            }

            // Останавливаем executorService
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdownNow();
            }

            // Создаём новый executorService для следующих запусков
            executorService = Executors.newSingleThreadExecutor();

            // Сбрасываем флаги
            isRunning.set(false);

            SwingUtilities.invokeLater(() -> {
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                settingsButton.setEnabled(true);
                progressBar.setIndeterminate(false);
                progressBar.setValue(0);
                progressBar.setString("Анализ остановлен");
                statusLabel.setText("Статус: Анализ остановлен");
            });
            return;
        }

        // Затем проверяем обычный рекурсивный анализатор
        if (recursiveBuilder != null && recursiveBuilder.isRunning()) {
            recursiveBuilder.stop();
            appendLog("Остановка рекурсивного анализа...");

            SwingUtilities.invokeLater(() -> {
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                settingsButton.setEnabled(true);
                progressBar.setIndeterminate(false);
                progressBar.setValue(0);
                statusLabel.setText("Статус: Анализ остановлен");
            });
            return;
        }

        appendLog("Нет активных процессов для остановки");
    }

    private void openSettings() {
        SettingsDialog dialog = new SettingsDialog(this, settings, config);
        dialog.setVisible(true);
    }

    private void openOutputDirectory() {
        String dir = settings.getOutputDir();
        if (dir == null || dir.isEmpty()) dir = "SQL_info";

        try {
            File file = new File(dir);
            if (!file.exists()) file.mkdirs();
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            appendLog("Ошибка открытия директории: " + e.getMessage());
        }
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            // Добавляем сообщение с временной меткой
            String timestamp = "[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()) + "] ";
            logArea.append(timestamp + message + "\n");

            // Автоматическая прокрутка вниз
            // Метод 1: установка каретки в конец
            logArea.setCaretPosition(logArea.getDocument().getLength());

            // Метод 2: дополнительная прокрутка JScrollPane (на случай, если каретка не сработала)
            JScrollPane scrollPane = (JScrollPane) logArea.getParent().getParent();
            if (scrollPane != null) {
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                if (verticalBar != null) {
                    verticalBar.setValue(verticalBar.getMaximum());
                }
            }
        });
    }

    private void saveLog() {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "analysis_log_" + timestamp + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(logArea.getText());
            appendLog("Лог сохранен: " + filename);
        } catch (IOException e) {
            appendLog("Ошибка сохранения лога: " + e.getMessage());
        }
    }

    private void saveResultToFile() {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "analysis_result_" + timestamp + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(resultArea.getText());
            appendLog("Результат сохранен: " + filename);
            JOptionPane.showMessageDialog(this,
                    "Результат сохранен в файл: " + filename,
                    "Сохранение результата",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            appendLog("Ошибка сохранения результата: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Ошибка сохранения результата: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadWindowState() {
        if (config != null) {
            setSize(config.getWindowWidth(), config.getWindowHeight());
            setLocation(config.getWindowX(), config.getWindowY());
            if (config.getWindowExtendedState() != 0) {
                setExtendedState(config.getWindowExtendedState());
            }
        } else {
            // Значения по умолчанию
            setSize(1200, 800);
            setLocationRelativeTo(null);
        }
    }

    private void saveState() {
        if (config != null) {
            config.setWindowWidth(getWidth());
            config.setWindowHeight(getHeight());
            config.setWindowX(getX());
            config.setWindowY(getY());
            config.setWindowExtendedState(getExtendedState());
            config.save();
        }
        settings.save();
    }

    /**
     * Перенаправляет System.out и System.err в лог-панель
     */
    /**
     * Перенаправляет System.out и System.err в лог-панель с правильной кодировкой
     */
    private void redirectSystemOutToLog() {
        try {
            // Сохраняем оригинальные потоки
            originalOut = System.out;
            originalErr = System.err;

            // Создаем Piped потоки с UTF-8
            pipeIn = new PipedInputStream();
            PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);

            // Используем PrintStream с UTF-8
            PrintStream customOut = new PrintStream(pipeOut, true, "UTF-8");

            // Перенаправляем вывод
            System.setOut(customOut);
            System.setErr(customOut);

            // Запускаем поток чтения с UTF-8
            logReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(pipeIn, "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String finalLine = line;
                        SwingUtilities.invokeLater(() -> appendLog(finalLine));
                    }
                } catch (IOException e) {
                    if (!Thread.currentThread().isInterrupted()) {
                        SwingUtilities.invokeLater(() -> appendLog("Ошибка чтения лога: " + e.getMessage()));
                    }
                }
            });
            logReader.start();

        } catch (IOException e) {
            appendLog("Не удалось перенаправить вывод: " + e.getMessage());
        }
    }

    // Внутренний класс для захвата вывода
    private class LogOutputStream extends OutputStream {
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void write(int b) throws IOException {
            char c = (char) b;
            if (c == '\n') {
                final String line = buffer.toString();
                SwingUtilities.invokeLater(() -> appendLog(line));
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            for (int i = off; i < off + len; i++) {
                write(b[i]);
            }
        }
    }

    /**
     * Восстанавливает оригинальные потоки вывода
     */
    private void restoreSystemOut() {
        if (originalOut != null) {
            System.setOut(originalOut);
        }
        if (originalErr != null) {
            System.setErr(originalErr);
        }
        if (logReader != null && logReader.isAlive()) {
            logReader.interrupt();
        }
    }


    /**
     * Загружает MD файл промпта для выбранной формы
     *
     * @param formPath путь к форме
     */
    private void loadLlmPromptToPanel(String formPath) {
        if (llmPromptArea == null) return;

        // Загружаем только если вкладка LLM промпт активна
        if (tabbedPane.getSelectedIndex() != 2) {
            return;
        }

        String mdFilePath = getLlmPromptFilePath(formPath);
        File mdFile = new File(mdFilePath);

        String instructionText =
                "=== ИНСТРУКЦИЯ ===\n\n" +
                        "Для генерации LLM промпта необходимо:\n" +
                        "1. Убедиться, что в настройках включена опция 'Включить экспорт LLM промпта после анализа'\n" +
                        "2. Запустить анализ формы (кнопка 'Запуск анализа' или 'Рекурсивный анализ')\n" +
                        "3. После завершения анализа MD файл будет создан автоматически\n\n" +
                        "Файл промпта будет сохранён с тем же именем, что и отчёт, но с расширением .md\n" +
                        "Пример: Forms#HospitPlanning#hospit_planning.frm.md\n\n" +
                        "=== ОЖИДАНИЕ ФАЙЛА ===\n" +
                        "Файл не найден: " + mdFilePath;

        if (mdFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(mdFile.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8);
                llmPromptArea.setText(content);
                llmPromptArea.setCaretPosition(0);
                appendLog("Загружен LLM промпт для формы: " + formPath);
            } catch (IOException e) {
                llmPromptArea.setText("Ошибка загрузки MD файла: " + e.getMessage() + "\n\n" + instructionText);
                appendLog("Ошибка загрузки MD файла для " + formPath + ": " + e.getMessage());
            }
        } else {
            llmPromptArea.setText(instructionText);
            appendLog("MD файл не найден для формы: " + formPath);
        }
    }

    /**
     * Сохраняет текущий LLM промпт в файл
     */
    private void saveLlmPromptToFile() {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "llm_prompt_export_" + timestamp + ".md";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(llmPromptArea.getText());
            appendLog("LLM промпт сохранен: " + filename);
            JOptionPane.showMessageDialog(this,
                    "LLM промпт сохранен в файл: " + filename,
                    "Сохранение",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            appendLog("Ошибка сохранения LLM промпта: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Ошибка сохранения: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Перегенерировать LLM промпт для текущей формы
     */
    private void regenerateLlmPrompt() {
        TreePath selectedPath = formsTreePanel.getSelectedPath();
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(this,
                    "Не выбрана форма для генерации промпта",
                    "Нет выбранной формы",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String formPath = formsTreePanel.getFormPathFromTreePath(selectedPath);
        if (formPath == null) {
            return;
        }

        appendLog("Запрос на перегенерацию LLM промпта для формы: " + formPath);

        // Запускаем анализ только для этой формы
        Set<String> formsSet = new LinkedHashSet<>();
        formsSet.add(formPath);

        // Используем существующий механизм анализа
        startAnalysisForForms(formsSet);
    }

    /**
     * Запускает анализ для указанных форм
     */
    private void startAnalysisForForms(Set<String> formsSet) {
        ensureFormsPreloaded();
        // ========== ПРОВЕРКА ДОСТУПНОСТИ БД ==========
        if (!checkDatabaseAvailabilityBeforeAnalysis()) {
            return; // Пользователь отменил анализ
        }
        // =============================================

        if (isRunning.get()) {
            appendLog("Анализ уже выполняется");
            return;
        }


        stopRequested = false;
        isRunning.set(true);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        settingsButton.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Статус: Генерация LLM промпта...");

        currentTask = executorService.submit(() -> {
            try {
                runAnalysis(new ArrayList<>(formsSet));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    appendLog("ОШИБКА: " + e.getMessage());
                    e.printStackTrace();
                });
            } finally {
                isRunning.set(false);
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    settingsButton.setEnabled(true);
                    statusLabel.setText("Статус: Готов");
                    progressBar.setIndeterminate(false);
                });
            }
        });
    }
    /**
     * Удаляет файлы отчёта и MD промпта для текущей выбранной формы
     */
    /**
     * Удаляет файлы отчёта и MD промпта для текущей выбранной формы
     */
    private void deleteReportForCurrentForm() {
        TreePath selectedPath = formsTreePanel.getSelectedPath();
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(this,
                    "Не выбрана форма для удаления отчёта",
                    "Нет выбранной формы",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String formPath = formsTreePanel.getFormPathFromTreePath(selectedPath);
        if (formPath == null) {
            return;
        }

        // Получаем базовую директорию отчётов
        String outputDir = settings.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }

        // Формируем безопасное имя файла
        String safeFileName = getSafeFileNameForDelete(formPath);

        // Формируем пути к файлам внутри подкаталога Forms
        String txtPath = outputDir + File.separator + "Forms" + File.separator + safeFileName + ".txt";
        String mdPath = outputDir + File.separator + "Forms" + File.separator + safeFileName + ".md";

        File txtFile = new File(txtPath);
        File mdFile = new File(mdPath);

        // Подтверждение удаления
        StringBuilder message = new StringBuilder();
        message.append("Удалить отчёты для формы:\n");
        message.append(formPath).append("\n\n");

        if (txtFile.exists()) {
            message.append("✓ ").append(txtFile.getName()).append("\n");
        }
        if (mdFile.exists()) {
            message.append("✓ ").append(mdFile.getName()).append("\n");
        }
        if (!txtFile.exists() && !mdFile.exists()) {
            message.append("(файлы отчётов не найдены)");
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                message.toString(),
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Удаляем файлы
        boolean deleted = false;
        if (txtFile.exists()) {
            if (txtFile.delete()) {
                appendLog("Удалён файл отчёта: " + txtPath);
                deleted = true;
            } else {
                appendLog("Ошибка удаления: " + txtPath);
            }
        }

        if (mdFile.exists()) {
            if (mdFile.delete()) {
                appendLog("Удалён MD файл: " + mdPath);
                deleted = true;
            } else {
                appendLog("Ошибка удаления: " + mdPath);
            }
        }

        if (deleted) {
            // Очищаем панель результата
            resultArea.setText("");
            // Очищаем дочерние узлы в дереве
            formsTreePanel.clearChildNodes(formPath);
            // Если вкладка LLM активна, показываем инструкцию
            if (tabbedPane.getSelectedIndex() == 2) {
                loadLlmPromptToPanel(formPath);
            }

            JOptionPane.showMessageDialog(this,
                    "Файлы отчётов удалены",
                    "Удаление завершено",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Не удалось удалить файлы",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Формирует безопасное имя файла для удаления (без расширения)
     */
    private String getSafeFileNameForDelete(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        // Убираем маркер SubForm если есть
        if (normalized.startsWith("(sub)_")) {
            normalized = normalized.substring(6);
        }
        return normalized.replace("/", "#").replace("\\", "#");
    }

    /**
     * Формирует путь к MD файлу промпта (в подкаталоге Forms)
     */
    private String getLlmPromptFilePath(String formPath) {
        String actualPath = formPath;
        if (actualPath.startsWith("/")) {
            actualPath = actualPath.substring(1);
        }
        if (actualPath.startsWith("(sub)_")) {
            actualPath = actualPath.substring(6);
        }
        String safeName = actualPath.replace("/", "#").replace("\\", "#");
        String outputDir = settings.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }
        return outputDir + File.separator + "Forms" + File.separator + safeName + ".md";
    }

    private void startParallelRecursiveAnalysis() {
        ensureFormsPreloaded();
        paused.set(false);
        pauseButton.setText("⏸ Пауза");
        pauseButton.setEnabled(true);

        if (!checkDatabaseAvailabilityBeforeAnalysis()) {
            return;
        }

        // ========== ЗАГРУЗКА ФОРМ В ПАМЯТЬ ==========
        String projectPath = settings.getProjectPath();
        if (formCacheManager.needsLoading(projectPath)) {
            appendLog("Загрузка форм в оперативную память...");
            int loaded = formCacheManager.loadAllForms(projectPath, this::appendLog);  // ← ОШИБКА ЗДЕСЬ
            if (loaded == 0) {
                appendLog("ОШИБКА: Не удалось загрузить формы. Проверьте путь к проекту.");
                JOptionPane.showMessageDialog(this,
                        "Не удалось загрузить формы.\nПроверьте путь к проекту в настройках.",
                        "Ошибка загрузки",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            appendLog("Загружено " + loaded + " форм в оперативную память");
        } else {
            appendLog("Используем формы из кэша (" + formCacheManager.getCachedFormsCount() + " шт.)");
        }

        if (parallelBuilder.isRunning()) {
            appendLog("Параллельный анализ уже выполняется");
            return;
        }

        List<String> selectedForms = formsTreePanel.getSelectedForms();
        List<String> startForms = selectedForms.isEmpty() ? null : selectedForms;

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        settingsButton.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Статус: Параллельный рекурсивный анализ...");

        parallelBuilder.startRecursiveBuild(startForms);
    }

    private void startParallelFullProjectScan() {
        // ========== ПРОВЕРКА ДОСТУПНОСТИ БД ==========
        if (!checkDatabaseAvailabilityBeforeAnalysis()) {
            return;
        }
        // =============================================

        // ========== ЗАГРУЗКА ФОРМ В ПАМЯТЬ ==========
        String projectPath = settings.getProjectPath();
        if (formCacheManager.needsLoading(projectPath)) {
            appendLog("Загрузка форм в оперативную память...");
            int loaded = formCacheManager.loadAllForms(projectPath, this::appendLog);
            if (loaded == 0) {
                appendLog("ОШИБКА: Не удалось загрузить формы. Проверьте путь к проекту.");
                JOptionPane.showMessageDialog(this,
                        "Не удалось загрузить формы.\nПроверьте путь к проекту в настройках.",
                        "Ошибка загрузки",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            appendLog("Загружено " + loaded + " форм в оперативную память");
        } else {
            appendLog("Используем формы из кэша (" + formCacheManager.getCachedFormsCount() + " шт.)");
        }

        // Проверяем, не запущен ли уже анализ
        if (parallelBuilder != null && parallelBuilder.isRunning()) {
            appendLog("Параллельный анализ уже выполняется");
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Анализ уже выполняется. Остановить текущий и запустить новый?",
                    "Анализ запущен",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                parallelBuilder.forceStop();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                return;
            }
        }

        // Сброс флагов
        stopRequested = false;
        isRunning.set(false);


        int threads = (config != null) ? config.getParallelThreads() : Runtime.getRuntime().availableProcessors();
        appendLog("ПАРАЛЛЕЛЬНОЕ СКАНИРОВАНИЕ ВСЕГО ПРОЕКТА\n");
        appendLog("Будут обработаны ТОЛЬКО формы, для которых ещё нет отчётов.");
        appendLog("Количество потоков: " + threads + "");
        appendLog("Доступно ядер: " + Runtime.getRuntime().availableProcessors() + "\n");
        appendLog("Это может занять продолжительное время.");
        appendLog("Начинаем параллельное сканирование всего проекта...");
        appendLog("Количество потоков: " + threads);
        parallelBuilder.setFullProjectScan(true);

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        settingsButton.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setIndeterminate(false);
        progressBar.setString("0 форм обработано");
        statusLabel.setText("Статус: Параллельное сканирование проекта...");

        scanStartTime = System.currentTimeMillis();

        // Запускаем таймер для обновления прогресса (каждую секунду)
        if (progressTimer != null) {
            progressTimer.stop();
        }
        progressTimer = new Timer(1000, e -> updateScanProgress());
        progressTimer.start();

        parallelBuilder.startRecursiveBuild(null);
    }

    private void updateScanProgress() {
        if (!parallelBuilder.isRunning()) {
            if (progressTimer != null) {
                progressTimer.stop();
            }
            return;
        }

        int processed = parallelBuilder.getTotalProcessed();
        int found = parallelBuilder.getTotalFound();
        int queueSize = parallelBuilder.getQueueSize();
        int active = parallelBuilder.getActiveTasks();
        long elapsed = (System.currentTimeMillis() - scanStartTime) / 1000;

        StringBuilder sb = new StringBuilder();
        if (found > 0) {
            int percent = (processed * 100) / found;
            sb.append(percent).append("%");
            progressBar.setValue(percent);
        } else {
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
        }

        sb.append(" | Обработано: ").append(processed);
        if (found > 0) {
            sb.append("/").append(found);
        }
        sb.append(" | Очередь: ").append(queueSize);
        sb.append(" | Активных: ").append(active);
        sb.append(" | ").append(formatTime(elapsed));

        progressBar.setString(sb.toString());
        statusLabel.setText("Статус: " + sb.toString());
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        if (minutes > 0) {
            return String.format("%d мин %d сек", minutes, secs);
        }
        return String.format("%d сек", secs);
    }

    /**
     * Корректное завершение всех потоков при закрытии окна
     */
    private void shutdownAllExecutors() {
        // Останавливаем параллельный билдер (без ожидания)
        if (parallelBuilder != null) {
            parallelBuilder.forceStop();
        }

        // Останавливаем рекурсивный билдер
        if (recursiveBuilder != null) {
            recursiveBuilder.stop();
        }

        // Завершаем executorService без ожидания
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }

        // Завершаем executor без ожидания
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }

        // Останавливаем таймер
        if (progressTimer != null && progressTimer.isRunning()) {
            progressTimer.stop();
        }

        // Останавливаем поток чтения лога
        if (logReader != null && logReader.isAlive()) {
            logReader.interrupt();
        }

        if (config != null && config.isUseMemoryCache() && !parallelBuilder.isRunning()) {
            try {
                System.out.println("Выгрузка буфера на диск перед закрытием...");
                InMemoryReportBuffer.flushToDisk(settings.getOutputDir());
            } catch (IOException e) {
                System.err.println("Ошибка выгрузки буфера: " + e.getMessage());
            }
        }
    }

    /**
     * Предзагрузка всех форм в оперативную память (использует 124 ГБ RAM)
     */
    private void preloadFormsToCache() {
        // Загрузка форм перенесена в момент начала анализа
        // Этот метод оставлен для совместимости, но ничего не делает
        appendLog("Кэширование форм будет выполнено при запуске анализа");
    }

    /**
     * Проверяет доступность баз данных перед началом анализа
     *
     * @return true если можно продолжать анализ, false если отменено
     */
    private boolean checkDatabaseAvailabilityBeforeAnalysis() {
        DatabaseAvailabilityService dbChecker = new DatabaseAvailabilityService(settings);
        dbChecker.checkAllConnections();
        DatabaseConnectionManager.printStats();

        // Показываем диалог с результатами
        boolean continueAnalysis = dbChecker.showResultsDialog(this);

        if (!continueAnalysis) {
            appendLog("Анализ отменён пользователем из-за проблем с подключением к БД");
            return false;
        }

        // Дополнительные предупреждения в лог
        if (!dbChecker.isOracleConfigured()) {
            appendLog("ВНИМАНИЕ: Oracle не настроен. Анализ будет выполняться ТОЛЬКО на основе XML файлов.");
            appendLog("  Функции, требующие Oracle, будут недоступны.");
        } else if (!dbChecker.isOracleAvailable()) {
            appendLog("ВНИМАНИЕ: Oracle недоступен. Функции, требующие Oracle, будут ограничены.");
            String error = dbChecker.getOracleError();
            if (error != null) appendLog("  Причина: " + error);
        } else {
            appendLog("Oracle доступен.");
        }

        if (!dbChecker.isPostgresConfigured()) {
            appendLog("ВНИМАНИЕ: PostgreSQL не настроен.");
        } else if (!dbChecker.isPostgresAvailable()) {
            appendLog("ВНИМАНИЕ: PostgreSQL недоступен.");
            String error = dbChecker.getPostgresError();
            if (error != null) appendLog("  Причина: " + error);
        } else {
            appendLog("PostgreSQL доступен.");
        }

        return true;
    }

    // Метод для переключения паузы
    private void togglePause() {
        if (paused.get()) {
            // Возобновляем
            synchronized (pauseLock) {
                paused.set(false);
                pauseLock.notifyAll();
            }

            // Если используется parallelBuilder, возобновляем его
            if (parallelBuilder != null) {
                parallelBuilder.setPaused(false);
            }

            pauseButton.setText("⏸ Пауза");
            pauseButton.setBackground(new Color(255, 193, 7));
            appendLog("▶ Процесс возобновлён");
            statusLabel.setText("Статус: Возобновлён");
        } else {
            // Ставим на паузу
            paused.set(true);

            // Если используется parallelBuilder, ставим его на паузу
            if (parallelBuilder != null) {
                parallelBuilder.setPaused(true);
            }

            pauseButton.setText("▶ Возобновить");
            pauseButton.setBackground(new Color(76, 175, 80));
            appendLog("⏸ Процесс поставлен на паузу");
            statusLabel.setText("Статус: На паузе");
        }
    }

    // Метод для проверки паузы (должен вызываться в рабочих потоках)
    private void checkPause() {
        if (paused.get()) {
            synchronized (pauseLock) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


    /**
     * Гарантированная предзагрузка форм в память (если включен режим)
     */
    private void ensureFormsPreloaded() {
        if (!config.isUseMemoryCache()) {
            appendLog("Режим оперативной памяти ВЫКЛЮЧЕН. Формы читаются с диска.");
            return;
        }

        String projectPath = settings.getProjectPath();
        if (projectPath == null || projectPath.trim().isEmpty()) {
            appendLog("ОШИБКА: Путь к проекту не указан!");
            return;
        }

        // ========== ИСПРАВЛЕНИЕ: проверяем реальное состояние кэша ==========
        if (FormCache.getCachedFormsCount() > 0) {
            appendLog("Формы уже в памяти (" + FormCache.getCachedFormsCount() + " шт.)");
            return;
        }
        // ===================================================================

        appendLog("");
        appendLog("=== ПРЕДЗАГРУЗКА ФОРМ В ОПЕРАТИВНУЮ ПАМЯТЬ ===");

        progressBar.setIndeterminate(true);
        statusLabel.setText("Статус: Загрузка форм в память...");

        int loaded = formCacheManager.loadAllForms(projectPath, this::appendLog);

        progressBar.setIndeterminate(false);
        statusLabel.setText("Статус: Готов к анализу");

        if (loaded == 0) {
            appendLog("ПРЕДУПРЕЖДЕНИЕ: Не удалось загрузить формы в память!");
            appendLog("Проверьте путь к проекту: " + projectPath);
            appendLog("Будет использован режим прямого чтения с диска.");
            config.setUseMemoryCache(false);
        } else {
            appendLog("✓ Формы успешно загружены в оперативную память");
            long memoryMB = FormCache.getMemoryUsageBytes() / (1024 * 1024);
            appendLog("  Использовано RAM: ~" + memoryMB + " МБ");
        }
    }

}