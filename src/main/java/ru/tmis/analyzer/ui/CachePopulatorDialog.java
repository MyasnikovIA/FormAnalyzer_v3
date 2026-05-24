// ui/CachePopulatorDialog.java
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

public class CachePopulatorDialog extends JDialog {

    private final SettingsModel settings;
    private final DatabaseCachePopulator populator;

    private JProgressBar progressBar;
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton closeButton;
    private JLabel statusLabel;

    private ExecutorService executor;
    private Future<?> currentTask;
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private volatile boolean isRunning = false;

    public CachePopulatorDialog(JFrame parent, SettingsModel settings) {
        super(parent, "Заполнение кэша БД", true);
        this.settings = settings;
        this.populator = new DatabaseCachePopulator(settings);
        this.executor = Executors.newSingleThreadExecutor();

        initUI();
        setSize(700, 500);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Верхняя панель с информацией
        JPanel infoPanel = new JPanel(new BorderLayout());
        JLabel infoLabel = new JLabel(
                "<html>" +
                        "<b>Заполнение кэша данными из БД</b><br>" +
                        "Будут загружены:<br>" +
                        "• Все вьюхи (D_V_*) из Oracle и PostgreSQL<br>" +
                        "• DDL всех таблиц (D_*)<br>" +
                        "• Тела всех пакетных функций (D_PKG_*)<br>" +
                        "• Все брокеры (D_UNITBPS)<br>" +
                        "• Все отчёты (D_REPORTS_LINKS)<br><br>" +
                        "Это может занять продолжительное время (от 5 до 30 минут).<br>" +
                        "После загрузки кэш будет сохранён на диск.<br>" +
                        "Последующие запуски анализа будут работать значительно быстрее." +
                        "</html>"
        );
        infoLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Центральная панель с логом
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Лог загрузки"));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(0, 255, 0));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(0, 300));
        logPanel.add(logScroll, BorderLayout.CENTER);

        // Панель прогресса
        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(true);
        statusLabel = new JLabel("Готов к запуску");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(statusLabel, BorderLayout.SOUTH);
        logPanel.add(progressPanel, BorderLayout.SOUTH);

        mainPanel.add(logPanel, BorderLayout.CENTER);

        // Нижняя панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        startButton = new JButton("Начать загрузку");
        startButton.setBackground(new Color(76, 175, 80));
        startButton.addActionListener(e -> startPopulation());

        stopButton = new JButton("Остановить");
        stopButton.setEnabled(false);
        stopButton.setBackground(new Color(244, 67, 54));
        stopButton.addActionListener(e -> stopPopulation());

        closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Настройка логгера
        populator.setLogger(new ILogger() {
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

        populator.setProgressCallback(status -> {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Статус: " + status);
            });
        });
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void startPopulation() {
        if (isRunning) {
            appendLog("Загрузка уже выполняется");
            return;
        }

        // Проверка подключения к БД перед началом
        if (!checkDatabaseConnections()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Подключение к БД не настроено или недоступно.\n" +
                            "Проверьте настройки подключения в разделе 'Настройки'.\n\n" +
                            "Продолжить загрузку всё равно?",
                    "Предупреждение",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        stopRequested.set(false);
        isRunning = true;

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        closeButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Статус: Загрузка начата...");

        appendLog("=== НАЧАЛО ЗАГРУЗКИ КЭША ===");
        appendLog("Oracle URL: " + settings.getOracleUrl());
        appendLog("PostgreSQL URL: " + settings.getPostgresUrl());

        currentTask = executor.submit(() -> {
            try {
                populator.setStopRequested(stopRequested);
                DatabaseCachePopulator.PopulateResult result = populator.populateAll();

                SwingUtilities.invokeLater(() -> {
                    appendLog("");
                    appendLog("=== ЗАГРУЗКА ЗАВЕРШЕНА ===");
                    appendLog(result.toString());

                    JOptionPane.showMessageDialog(CachePopulatorDialog.this,
                            "Загрузка кэша завершена!\n\n" + result.toString(),
                            "Загрузка завершена",
                            JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    appendLog("КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
                    e.printStackTrace();
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    isRunning = false;
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    closeButton.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    if (!stopRequested.get()) {
                        statusLabel.setText("Статус: Загрузка завершена");
                    } else {
                        statusLabel.setText("Статус: Загрузка остановлена");
                    }
                });
            }
        });
    }

    private void stopPopulation() {
        if (!isRunning) {
            return;
        }

        appendLog("Запрос на остановку загрузки...");
        stopRequested.set(true);
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
        stopButton.setEnabled(false);
        statusLabel.setText("Статус: Остановка...");
    }

    private boolean checkDatabaseConnections() {
        boolean oracleOk = false;
        boolean postgresOk = false;

        // Быстрая проверка через NetworkUtils
        if (settings.getOracleUrl() != null && !settings.getOracleUrl().isEmpty()) {
            oracleOk = ru.tmis.analyzer.utils.NetworkUtils.isDatabaseServerAvailable(settings.getOracleUrl());
        }

        if (settings.getPostgresUrl() != null && !settings.getPostgresUrl().isEmpty()) {
            postgresOk = ru.tmis.analyzer.utils.NetworkUtils.isDatabaseServerAvailable(settings.getPostgresUrl());
        }

        return oracleOk || postgresOk;
    }
}