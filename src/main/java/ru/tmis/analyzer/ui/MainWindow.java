// ui/MainWindow.java
package ru.tmis.analyzer.ui;
import javax.swing.Timer;
import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.llm.LLMPromptGenerator;
import ru.tmis.analyzer.core.model.LLMReportContext;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.service.FormAnalyzerService;
import ru.tmis.analyzer.core.report.ReportGenerator;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public MainWindow(SettingsModel settings, AppConfig config) {
        this.settings = settings;
        this.config = config;
        this.executor = Executors.newSingleThreadExecutor();
        this.executorService = Executors.newSingleThreadExecutor();

        initUI();
        loadWindowState();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveState();
            }
        });
    }

    private void initUI() {
        setTitle("TMIS Form Analyzer v2.0.3 (от 18-05-2026)");
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

        stopButton = new JButton("Остановка");
        stopButton.setEnabled(false);
        stopButton.setBackground(new Color(244, 67, 54));
        stopButton.addActionListener(e -> stopAnalysis());

        JButton openOutputButton = new JButton("Открыть отчеты");
        openOutputButton.addActionListener(e -> openOutputDirectory());

        buttonPanel.add(settingsButton);
        buttonPanel.add(openOutputButton);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JSplitPane createCenterPanel() {
        // Left panel - forms tree
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Список форм для анализа"));

        formsTreePanel = new FormsTreePanel();
        formsTreePanel.setOnFormsChanged(() -> {});

        // Добавляем слушатель выбора формы в дереве
        formsTreePanel.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath selectedPath = formsTreePanel.getSelectedPath();
                if (selectedPath != null) {
                    String formPath = formsTreePanel.getFormPathFromTreePath(selectedPath);
                    if (formPath != null) {
                        loadFormResultToPanel(formPath);
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

        // Right panel with tabs
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Результаты"));

        JTabbedPane tabbedPane = new JTabbedPane();

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

        // Tab 2: Результат
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
        JButton saveResultBtn = new JButton("Сохранить результат в файл");
        saveResultBtn.addActionListener(e -> saveResultToFile());
        resultButtons.add(saveResultBtn);
        resultButtons.add(clearResultBtn);
        resultPanel.add(resultButtons, BorderLayout.NORTH);

        tabbedPane.addTab("Результат", resultPanel);

        rightPanel.add(tabbedPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(400);

        return splitPane;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Прогресс"));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        statusLabel = new JLabel("Готов к работе");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Загружает сохранённый отчёт для выбранной формы в панель результата
     */
    private void loadFormResultToPanel(String formPath) {
        // Получаем правильный путь к файлу отчёта через FormsTreePanel
        String reportPath = formsTreePanel.getReportFilePath(formPath);
        File reportFile = new File(reportPath);

        if (reportFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(reportFile.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8);
                resultArea.setText(content);
                resultArea.setCaretPosition(0);
                appendLog("Загружен отчёт для формы: " + formPath);
            } catch (IOException e) {
                resultArea.setText("Ошибка загрузки отчёта: " + e.getMessage());
                appendLog("Ошибка загрузки отчёта для " + formPath + ": " + e.getMessage());
            }
        } else {
            resultArea.setText("Отчёт для формы не найден.\n\nПуть: " + reportPath + "\n\nЗапустите анализ для создания отчёта.");
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

    private void startAnalysis() {
        if (isRunning.get()) {
            appendLog("Анализ уже выполняется");
            return;
        }

        List<String> formsList = formsTreePanel.getFormsList();
        if (formsList.isEmpty()) {
            appendLog("Нет форм для анализа. Добавьте формы в список.");
            JOptionPane.showMessageDialog(this,
                    "Список форм пуст. Добавьте формы для анализа.",
                    "Нет форм",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        saveFormsList();

        stopRequested = false;
        isRunning.set(true);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        settingsButton.setEnabled(false);
        progressBar.setValue(0);
        statusLabel.setText("Статус: Анализ запущен");

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        try {
            PipedInputStream pipeIn = new PipedInputStream();
            PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);
            PrintStream customOut = new PrintStream(pipeOut, true);
            System.setOut(customOut);
            System.setErr(customOut);

            Thread logReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pipeIn))) {
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

            currentTask = executorService.submit(() -> {
                try {
                    runAnalysis();
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> appendLog("ОШИБКА: " + e.getMessage()));
                    e.printStackTrace();
                } finally {
                    System.setOut(originalOut);
                    System.setErr(originalErr);
                    logReader.interrupt();
                    isRunning.set(false);
                    SwingUtilities.invokeLater(() -> {
                        startButton.setEnabled(true);
                        stopButton.setEnabled(false);
                        settingsButton.setEnabled(true);
                        statusLabel.setText("Статус: Готов");
                        if (!stopRequested) {
                            progressBar.setString("Анализ завершен");
                        } else {
                            progressBar.setString("Анализ остановлен");
                        }
                    });
                }
            });
        } catch (IOException e) {
            appendLog("Не удалось перенаправить вывод: " + e.getMessage());
            System.setOut(originalOut);
            System.setErr(originalErr);
            isRunning.set(false);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            settingsButton.setEnabled(true);
        }
    }

    private void stopAnalysis() {
        if (currentTask != null && !currentTask.isDone()) {
            appendLog("Запрос на остановку анализа...");
            stopRequested = true;
            currentTask.cancel(true);
            stopButton.setEnabled(false);
            statusLabel.setText("Статус: Остановка...");
        }
    }

    private void runAnalysis() throws Exception {
        appendLog("=".repeat(80));
        appendLog("=== ЗАПУСК АНАЛИЗА ФОРМ ===");
        appendLog("=".repeat(80));
        appendLog("Путь к проекту: " + settings.getProjectPath());
        appendLog("");

        FormAnalyzerService analyzer = new FormAnalyzerService(settings);

        analyzer.setLogger(new ILogger() {
            @Override
            public void log(String message) { appendLog(message); }
            @Override
            public void error(String message) { appendLog("ОШИБКА: " + message); }
            @Override
            public void debug(String message) { appendLog("[DEBUG] " + message); }
        });

        analyzer.setStopCondition(() -> stopRequested);
        analyzer.setProgressCallback((processed, total, currentForm) -> {
            SwingUtilities.invokeLater(() -> {
                int percent = total > 0 ? (processed * 100 / total) : 0;
                progressBar.setValue(percent);
                progressBar.setString(String.format("%d из %d (%.1f%%)", processed, total, (double) percent));
                statusLabel.setText("Статус: " + currentForm);
            });
        });
        analyzer.setFormAnalyzedCallback(formInfo -> {
            try {
                ReportGenerator reportGen = new ReportGenerator(settings.getOutputDir(), config);
                reportGen.createMainReportHeader();
                reportGen.appendFormToMainReport(formInfo);
            } catch (IOException e) {
                appendLog("Ошибка сохранения промежуточного отчёта: " + e.getMessage());
            }
        });

        List<FormInfo> results = analyzer.analyzeAllForms();

        if (stopRequested) {
            appendLog("Анализ остановлен пользователем");
            if (!results.isEmpty()) {
                appendLog("Сохранено частичных результатов: " + results.size() + " форм");
            }
            return;
        }

        appendLog("");
        appendLog("=== ГЕНЕРАЦИЯ ОТЧЕТОВ ===");

        ReportGenerator reportGen = new ReportGenerator(settings.getOutputDir(), config);
        reportGen.addAllForms(results);
        reportGen.generateMainReport();
        reportGen.generateSummaryReport();
        reportGen.createMainReportHeader();

        appendLog("");
        appendLog("=== АНАЛИЗ ЗАВЕРШЕН ===");
        appendLog("Обработано форм: " + results.size());
        appendLog("Отчеты сохранены в: " + settings.getOutputDir());

        if (config.isEnableLLMExport()) {
            appendLog("=== ГЕНЕРАЦИЯ LLM ПРОМПТА ===");
            LLMPromptGenerator llmGen = new LLMPromptGenerator(config);
            llmGen.setStopCondition(() -> stopRequested);
            LLMReportContext ctx = llmGen.prepareContext(results);
            if (config.getLlmExportMode().equals("single_file")) {
                String prompt = llmGen.generateSingleFile();
                Path llmPath = Paths.get(settings.getOutputDir(), "llm_prompt_all_forms.md");
                Files.writeString(llmPath, prompt);
                appendLog("LLM промпт сохранен: " + llmPath);
            } else {
                List<String> files = llmGen.generateForEachForm();
                appendLog("Создано промптов для форм: " + files.size());
            }
        }

        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "Анализ завершен! Открыть папку с отчетами?",
                    "Завершено", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                openOutputDirectory();
            }
        });
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

    private void saveFormsList() {
        // Данные уже сохраняются в FormsTreePanel при изменениях
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
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
        setSize(config.getWindowWidth(), config.getWindowHeight());
        setLocation(config.getWindowX(), config.getWindowY());
        if (config.getWindowExtendedState() != 0) {
            setExtendedState(config.getWindowExtendedState());
        }
    }

    private void saveState() {
        config.setWindowWidth(getWidth());
        config.setWindowHeight(getHeight());
        config.setWindowX(getX());
        config.setWindowY(getY());
        config.setWindowExtendedState(getExtendedState());
        config.save();
        settings.save();
    }
}