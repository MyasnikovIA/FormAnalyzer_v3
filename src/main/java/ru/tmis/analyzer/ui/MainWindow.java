// ui/MainWindow.java
package ru.tmis.analyzer.ui;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.service.FormAnalyzerService;
import ru.tmis.analyzer.core.report.ReportGenerator;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class MainWindow extends JFrame {

    private final SettingsModel settings;
    private final AppConfig config;

    private JTextArea formsListArea;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton startButton;
    private JButton stopButton;
    private JButton settingsButton;

    private ExecutorService executor;
    private Future<?> currentTask;
    private volatile boolean stopRequested = false;

    public MainWindow(SettingsModel settings, AppConfig config) {
        this.settings = settings;
        this.config = config;
        this.executor = Executors.newSingleThreadExecutor();

        initUI();
        loadFormsList();
        loadWindowState();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveState();
            }
        });
    }

    private void initUI() {
        setTitle("TMIS Form Analyzer v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel
        mainPanel.add(createTopPanel(), BorderLayout.NORTH);

        // Center split pane
        JSplitPane splitPane = createCenterPanel();
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Bottom panel
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
        // Left panel - forms list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Список форм для анализа"));

        formsListArea = new JTextArea();
        formsListArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        formsListArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private javax.swing.Timer timer;
            {
                timer = new javax.swing.Timer(2000, e -> saveFormsList());
                timer.setRepeats(false);
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { timer.restart(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { timer.restart(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { timer.restart(); }
        });

        JScrollPane formsScroll = new JScrollPane(formsListArea);
        formsScroll.setPreferredSize(new Dimension(400, 0));
        leftPanel.add(formsScroll, BorderLayout.CENTER);

        // Right panel - log
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Лог процесса"));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(0, 255, 0));

        JScrollPane logScroll = new JScrollPane(logArea);
        rightPanel.add(logScroll, BorderLayout.CENTER);

        JPanel logButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearLogBtn = new JButton("Очистить");
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        JButton saveLogBtn = new JButton("Сохранить лог");
        saveLogBtn.addActionListener(e -> saveLog());
        logButtons.add(saveLogBtn);
        logButtons.add(clearLogBtn);
        rightPanel.add(logButtons, BorderLayout.NORTH);

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

    private void startAnalysis() {
        if (currentTask != null && !currentTask.isDone()) {
            appendLog("Анализ уже выполняется");
            return;
        }

        stopRequested = false;

        saveFormsList();

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        settingsButton.setEnabled(false);
        progressBar.setValue(0);
        statusLabel.setText("Статус: Анализ запущен");

        currentTask = executor.submit(() -> {
            try {
                runAnalysis();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    appendLog("ОШИБКА: " + e.getMessage());
                    e.printStackTrace();
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    settingsButton.setEnabled(true);
                    statusLabel.setText("Статус: Готов");
                });
            }
        });
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

        // Устанавливаем логгер для вывода в UI
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
                // Для отладки - можно закомментировать
                appendLog("[DEBUG] " + message);
            }
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

        List<FormInfo> results = analyzer.analyzeAllForms();

        if (stopRequested) {
            appendLog("Анализ остановлен пользователем");
            return;
        }

        appendLog("");
        appendLog("=== ГЕНЕРАЦИЯ ОТЧЕТОВ ===");

        ReportGenerator reportGen = new ReportGenerator(settings.getOutputDir(), config);
        reportGen.addAllForms(results);
        reportGen.generateMainReport();
        reportGen.generateSummaryReport();

        appendLog("");
        appendLog("=== АНАЛИЗ ЗАВЕРШЕН ===");
        appendLog("Обработано форм: " + results.size());
        appendLog("Отчеты сохранены в: " + settings.getOutputDir());

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

    private void loadFormsList() {
        File file = new File("forms_list.txt");
        if (file.exists()) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                formsListArea.setText(content);
            } catch (IOException e) {
                appendLog("Ошибка загрузки списка форм: " + e.getMessage());
            }
        } else {
            formsListArea.setText("# Список форм для анализа (каждая с новой строки)\n" +
                    "# Пример: /Forms/ARMMainDoc/arm_director.frm\n" +
                    "#         UserFormsSaratov/ARMMainDoc/stac_pat_in_hpk_plan.frm\n");
        }
    }

    private void saveFormsList() {
        try {
            Files.writeString(Paths.get("forms_list.txt"), formsListArea.getText());
        } catch (IOException e) {
            appendLog("Ошибка сохранения списка форм: " + e.getMessage());
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