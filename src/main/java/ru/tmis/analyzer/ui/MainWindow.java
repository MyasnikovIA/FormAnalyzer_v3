package ru.tmis.analyzer.ui;
import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.log.ILogger;
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
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath selectedPath = formsTreePanel.getSelectedPath();
                if (selectedPath != null) {
                    String formPath = formsTreePanel.getFormPathFromTreePath(selectedPath);
                    if (formPath != null) {
                        // Рекурсивно загружаем все дочерние формы
                        loadFormAndAllChildren(formPath, selectedPath);
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


    // MainWindow.java - добавить метод

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

                // ОБНОВЛЯЕМ ДОЧЕРНИЕ ФОРМЫ ПОСЛЕ ЗАГРУЗКИ ОТЧЁТА
                formsTreePanel.refreshChildForms(formPath);

            } catch (IOException e) {
                resultArea.setText("Ошибка загрузки отчёта: " + e.getMessage());
                appendLog("Ошибка загрузки отчёта для " + formPath + ": " + e.getMessage());
                // Очищаем дочерние узлы при ошибке
                formsTreePanel.clearChildNodes(formPath);
            }
        } else {
            resultArea.setText("Отчёт для формы не найден.\n\nПуть: " + reportPath + "\n\nЗапустите анализ для создания отчёта.");
            // Очищаем дочерние узлы, так как отчёт отсутствует
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

        // Нормализуем пути - убираем возможные дублирования и формируем корректные пути
        Set<String> normalizedForms = new LinkedHashSet<>();
        for (String form : selectedForms) {
            String normalized = form;
            // Если путь содержит родительский путь, извлекаем только дочернюю часть
            if (normalized.contains("/Forms/") && normalized.lastIndexOf("/Forms/") > 0) {
                normalized = normalized.substring(normalized.lastIndexOf("/Forms/") + 1);
            }
            // Убираем ведущий слеш
            if (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            // Убеждаемся, что путь начинается с Forms/ или UserForms
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
        statusLabel.setText("Статус: Анализ запущен");
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


}