package ru.tmis.analyzer.ui;
import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.report.ReportGenerator;
import ru.tmis.analyzer.core.service.RecursiveReportBuilder;

import javax.swing.*;
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
        redirectSystemOutToLog();
       // Добавляем слушатель для восстановления при закрытии
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                restoreSystemOut();
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
            private boolean isLoadingChildren = false;  // Флаг для предотвращения рекурсии

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                // Игнорируем события во время загрузки дочерних элементов
                if (isLoadingChildren) return;

                TreePath selectedPath = formsTreePanel.getSelectedPath();
                if (selectedPath != null) {
                    String formPath = formsTreePanel.getFormPathFromTreePath(selectedPath);
                    if (formPath != null) {
                        isLoadingChildren = true;
                        try {
                            // Загружаем отчёт текущей формы
                            loadFormResultToPanel(formPath);

                            // Загружаем дочерние формы (только один уровень)
                            Set<String> childForms = formsTreePanel.loadChildFormsFromReport(formPath);
                            if (!childForms.isEmpty()) {
                                formsTreePanel.expandPath(selectedPath);
                                // Обновляем дочерние узлы в дереве
                                formsTreePanel.refreshChildForms(formPath);
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
            if (!recursiveBuilder.isRunning()) {
                startRecursiveAnalysis();
            } else {
                appendLog("Рекурсивный анализ уже выполняется");
                JOptionPane.showMessageDialog(this,
                        "Рекурсивный анализ уже выполняется. Дождитесь завершения или нажмите Стоп.",
                        "Анализ запущен",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

       // В конструкторе или initUI() после создания formsTreePanel
        recursiveBuilder = new RecursiveReportBuilder(settings, config, formsTreePanel);
        recursiveBuilder.setLogger(new ILogger() {
            @Override
            public void log(String message) { appendLog(message); }
            @Override
            public void error(String message) { appendLog("ОШИБКА: " + message); }
            @Override
            public void debug(String message) { appendLog("[DEBUG] " + message); }
        });

        recursiveBuilder.setOnLevelStart(message -> {
            appendLog(message);
            statusLabel.setText("Статус: " + message);
        });

        recursiveBuilder.setOnLevelComplete(count -> {
            appendLog("  Уровень завершён. Обработано форм: " + count);
            progressBar.setValue(0);
        });

        recursiveBuilder.setOnFormAnalyzed(formPath -> {
            appendLog("  Анализ формы: " + formPath);
        });

        recursiveBuilder.setOnError(message -> {
            appendLog("ОШИБКА: " + message);
            statusLabel.setText("Статус: Ошибка");
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            settingsButton.setEnabled(true);

            // Обновляем дерево с сохранением состояния
            SwingUtilities.invokeLater(() -> {
                formsTreePanel.refreshTreeWithState();
            });
        });

        recursiveBuilder.setOnComplete(() -> {
            appendLog("Рекурсивное построение завершено!");
            statusLabel.setText("Статус: Готов");
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
            progressBar.setString("Готово");
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            settingsButton.setEnabled(true);

            // Обновляем дерево с сохранением состояния
            SwingUtilities.invokeLater(() -> {
                // Сохраняем состояние до обновления
                FormsTreePanel.TreeState state = formsTreePanel.saveTreeState();

                // Очищаем неактуальные узлы и обновляем дочерние формы
                formsTreePanel.refreshAllChildFormsWithCleanup();

                // Раскрываем все узлы, которые были развёрнуты
                if (state != null && state.expandedPaths != null) {
                    for (String pathStr : state.expandedPaths) {
                        formsTreePanel.expandPathByDisplayString(pathStr);
                    }
                }

                // Восстанавливаем выбранный элемент
                if (state != null && state.selectedPath != null) {
                    formsTreePanel.restoreSelectedPath(state.selectedPath);
                }

                appendLog("Дерево форм обновлено, состояние восстановлено");
            });
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
        JButton saveResultBtn = new JButton("Сохранить результат в файл");
        saveResultBtn.addActionListener(e -> saveResultToFile());
        resultButtons.add(saveResultBtn);
        resultButtons.add(clearResultBtn);
        resultPanel.add(resultButtons, BorderLayout.NORTH);

        tabbedPane.addTab("Результат", resultPanel);

        // Tab 3: LLM промпт (НОВАЯ ВКЛАДКА)
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

        tabbedPane.addTab("LLM промпт", llmPanel);

        rightPanel.add(tabbedPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(400);

        return splitPane;
    }


    // MainWindow.java - исправленный метод loadFormAndAllChildren

    /**
     * Рекурсивная загрузка формы и всех её дочерних элементов
     * @param formPath путь к форме
     * @param treePath путь в дереве (для раскрытия узлов)
     */
    private void loadFormAndAllChildren(String formPath, TreePath treePath) {
        // Загружаем отчёт текущей формы
        loadFormResultToPanel(formPath);

        // Получаем дочерние формы
        Set<String> childForms = formsTreePanel.loadChildFormsFromReport(formPath);

        if (!childForms.isEmpty()) {
            // Раскрываем текущий узел
            formsTreePanel.expandPath(treePath);

            // Рекурсивно загружаем каждую дочернюю форму
            for (String childForm : childForms) {
                // Убираем маркер SubForm если есть
                String actualChildPath = childForm;
                if (actualChildPath.startsWith("(sub)_")) {
                    actualChildPath = actualChildPath.substring(6);
                }

                // Находим узел дочерней формы в дереве
                DefaultMutableTreeNode childNode = formsTreePanel.findNodeByFormPath(actualChildPath);
                if (childNode != null) {
                    TreePath childPath = formsTreePanel.getTreePathForNode(childNode);
                    // Рекурсивно загружаем дочернюю форму
                    loadFormAndAllChildren(actualChildPath, childPath);
                }
            }
        }

        // Восстанавливаем выбор на исходной форме (после загрузки всех дочерних)
        // Используем final переменную для лямбды
        final String originalFormPath = formPath;
        SwingUtilities.invokeLater(() -> {
            TreePath currentPath = formsTreePanel.getSelectedPath();
            if (currentPath != null) {
                String currentFormPath = formsTreePanel.getFormPathFromTreePath(currentPath);
                if (currentFormPath == null || !currentFormPath.equals(originalFormPath)) {
                    // Возвращаем выбор на исходную форму
                    DefaultMutableTreeNode node = formsTreePanel.findNodeByFormPath(originalFormPath);
                    if (node != null) {
                        TreePath originalPath = formsTreePanel.getTreePathForNode(node);
                        formsTreePanel.setSelectedPath(originalPath);
                    }
                }
            } else {
                // Если нет выбранного пути, выбираем исходную форму
                DefaultMutableTreeNode node = formsTreePanel.findNodeByFormPath(originalFormPath);
                if (node != null) {
                    TreePath originalPath = formsTreePanel.getTreePathForNode(node);
                    formsTreePanel.setSelectedPath(originalPath);
                }
            }
        });
    }

    private void startRecursiveAnalysis() {
        if (recursiveBuilder.isRunning()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Рекурсивный анализ уже выполняется. Остановить?",
                    "Анализ запущен",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                recursiveBuilder.stop();
            }
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
            startForms = null; // будет использовать getAllRootForms()
        } else {
            startForms = selectedForms;
        }

        // БЛОК УПРАВЛЕНИЯ КНОПКАМИ - ВАЖНО!
        startButton.setEnabled(false);
        stopButton.setEnabled(true);      // Активируем кнопку остановки
        settingsButton.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setIndeterminate(true);  // Показываем, что процесс идёт
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

        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Загружает сохранённый отчёт для выбранной формы в панель результата
     */
    // MainWindow.java - добавить вызов загрузки MD файла в конец метода

    private void loadFormResultToPanel(String formPath) {
        String reportPath = formsTreePanel.getReportFilePath(formPath);
        File reportFile = new File(reportPath);

        if (reportFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(reportFile.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8);
                resultArea.setText(content);
                resultArea.setCaretPosition(0);
                appendLog("Загружен отчёт для формы: " + formPath);

                formsTreePanel.refreshChildForms(formPath);

            } catch (IOException e) {
                resultArea.setText("Ошибка загрузки отчёта: " + e.getMessage());
                appendLog("Ошибка загрузки отчёта для " + formPath + ": " + e.getMessage());
                formsTreePanel.clearChildNodes(formPath);
            }
        } else {
            resultArea.setText("Отчёт для формы не найден.\n\nПуть: " + reportPath + "\n\nЗапустите анализ для создания отчёта.");
            formsTreePanel.clearChildNodes(formPath);
            appendLog("Отчёт не найден для формы: " + formPath + ", дочерние элементы очищены");
        }
        loadLlmPromptToPanel(formPath);
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

    // ui/MainWindow.java

    /**
     * Запускает анализ выбранных форм
     */
    private void startAnalysis() {
        if (isRunning.get()) {
            appendLog("Анализ уже выполняется");
            return;
        }

        // Получаем выбранные формы из дерева
        List<String> selectedForms = formsTreePanel.getSelectedForms();

        // Нормализуем пути
        Set<String> normalizedForms = new LinkedHashSet<>();
        for (String form : selectedForms) {
            String normalized = form;
            if (normalized.contains("/Forms/") && normalized.lastIndexOf("/Forms/") > 0) {
                normalized = normalized.substring(normalized.lastIndexOf("/Forms/") + 1);
            }
            if (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            if (!normalized.startsWith("Forms/") && !normalized.startsWith("UserForms")) {
                normalized = "Forms/" + normalized;
            }
            normalizedForms.add(normalized);
        }

        if (normalizedForms.isEmpty()) {
            appendLog("Нет выбранных форм для анализа. Выберите формы в дереве.");
            JOptionPane.showMessageDialog(this,
                    "Нет выбранных форм для анализа.\nВыберите формы в дереве (можно несколько с Ctrl/Shift).",
                    "Нет выбранных форм",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        appendLog("Выбрано форм для анализа: " + normalizedForms.size());
        for (String form : normalizedForms) {
            appendLog("  - " + form);
        }

        stopRequested = false;
        isRunning.set(true);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        settingsButton.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Статус: Анализ запущен");

        // Запускаем анализ в отдельном потоке
        currentTask = executorService.submit(() -> {
            try {
                runAnalysis(new ArrayList<>(normalizedForms));
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
                    if (!stopRequested) {
                        progressBar.setString("Анализ завершен");
                    } else {
                        progressBar.setString("Анализ остановлен");
                    }
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
                new ru.tmis.analyzer.core.service.FormAnalyzerService(settings,config);

        Set<String> formsSet = new LinkedHashSet<>(formsToAnalyze);
        analyzer.setFormsToAnalyze(formsSet);

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
                progressBar.setString(String.format("%d из %d (%d%%)", processed, total, percent));
                statusLabel.setText("Статус: " + currentForm);
            });
        });

        analyzer.setFormAnalyzedCallback(formInfo -> {
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

        List<?> results = analyzer.analyzeAllForms();
        analyzer.clearFormsToAnalyze();

        if (stopRequested) {
            appendLog("Анализ остановлен пользователем");
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
    }
    private void stopAnalysis() {
        // Сначала проверяем рекурсивный анализатор
        if (recursiveBuilder != null && recursiveBuilder.isRunning()) {
            recursiveBuilder.stop();
            appendLog("Запрос на остановку рекурсивного анализа...");
            stopButton.setEnabled(false);
            startButton.setEnabled(true);
            settingsButton.setEnabled(true);
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            statusLabel.setText("Статус: Остановка...");
            return;
        }

        // Обычный анализ
        if (currentTask != null && !currentTask.isDone()) {
            appendLog("Запрос на остановку анализа...");
            stopRequested = true;
            currentTask.cancel(true);
            stopButton.setEnabled(false);
            statusLabel.setText("Статус: Остановка...");
        }
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

    /**
     * Перенаправляет System.out и System.err в лог-панель
     */
    private void redirectSystemOutToLog() {
        try {
            // Сохраняем оригинальные потоки
            originalOut = System.out;
            originalErr = System.err;

            // Создаем Piped потоки
            pipeIn = new PipedInputStream();
            PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);
            PrintStream customOut = new PrintStream(pipeOut, true);

            // Перенаправляем вывод
            System.setOut(customOut);
            System.setErr(customOut);

            // Запускаем поток чтения
            logReader = new Thread(() -> {
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

        } catch (IOException e) {
            appendLog("Не удалось перенаправить вывод: " + e.getMessage());
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
     * @param formPath путь к форме
     */
    private void loadLlmPromptToPanel(String formPath) {
        if (llmPromptArea == null) return;

        // Формируем путь к MD файлу (аналогично TXT, но с .md)
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
            appendLog("MD файл не найден для формы: " + formPath + "\n" + mdFilePath);
        }
    }

    /**
     * Формирует путь к MD файлу промпта
     */
    private String getLlmPromptFilePath(String formPath) {
        String actualPath = formPath;
        if (actualPath.startsWith("/")) {
            actualPath = actualPath.substring(1);
        }
        String safeName = actualPath.replace("/", "#").replace("\\", "#");
        String outputDir = settings.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }
        return outputDir + File.separator + safeName + ".md";
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

}