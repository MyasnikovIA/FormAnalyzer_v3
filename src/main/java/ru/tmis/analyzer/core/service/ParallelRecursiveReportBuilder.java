package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.report.ReportGenerator;
import ru.tmis.analyzer.ui.FormsTreePanel;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ParallelRecursiveReportBuilder {

    private final SettingsModel settings;
    private final AppConfig config;
    private final FormsTreePanel formsTreePanel;
    private ILogger logger;

    private ExecutorService executor;
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Set<String> processedForms = ConcurrentHashMap.newKeySet();
    private final BlockingQueue<String> formsQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private final AtomicInteger totalProcessed = new AtomicInteger(0);

    private int parallelThreads = Runtime.getRuntime().availableProcessors(); // по умолчанию все ядра

    private Runnable onComplete;
    private Consumer<String> onError;
    private Consumer<String> onFormAnalyzed;
    private Consumer<String> onLevelStart;
    private Consumer<Integer> onLevelComplete;

    public ParallelRecursiveReportBuilder(SettingsModel settings, AppConfig config, FormsTreePanel formsTreePanel) {
        this.settings = settings;
        this.config = config;
        this.formsTreePanel = formsTreePanel;
    }

    /**
     * Установить количество параллельных потоков
     */
    public void setParallelThreads(int threads) {
        if (threads > 0) {
            this.parallelThreads = threads;
        }
    }

    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

    public void setOnComplete(Runnable callback) {
        this.onComplete = callback;
    }

    public void setOnError(Consumer<String> callback) {
        this.onError = callback;
    }

    public void setOnFormAnalyzed(Consumer<String> callback) {
        this.onFormAnalyzed = callback;
    }

    public void setOnLevelStart(Consumer<String> callback) {
        this.onLevelStart = callback;
    }

    public void setOnLevelComplete(Consumer<Integer> callback) {
        this.onLevelComplete = callback;
    }

    private void log(String message) {
        if (logger != null) {
            logger.log(message);
        }
        System.out.println(message);
    }

    private void error(String message) {
        if (logger != null) {
            logger.error(message);
        }
        System.err.println(message);
    }

    /**
     * Запуск параллельного рекурсивного построения
     */
    public void startRecursiveBuild(List<String> startForms) {
        if (isRunning.get()) {
            log("Параллельное построение уже выполняется");
            return;
        }

        List<String> finalStartForms;
        if (startForms == null || startForms.isEmpty()) {
            finalStartForms = formsTreePanel.getAllRootForms();
            if (finalStartForms.isEmpty()) {
                log("Нет форм для рекурсивного анализа");
                if (onError != null) onError.accept("Нет форм для анализа");
                return;
            }
            log("Стартовые формы не указаны. Используем все корневые формы (" + finalStartForms.size() + " шт.)");
        } else {
            finalStartForms = new ArrayList<>(startForms);
            log("Стартовые формы: " + finalStartForms.size() + " шт.");
        }

        stopRequested.set(false);
        isRunning.set(true);
        processedForms.clear();
        formsQueue.clear();
        totalProcessed.set(0);
        activeTasks.set(0);

        // Создаем пул потоков
        executor = Executors.newFixedThreadPool(parallelThreads);

        log("=== ЗАПУСК ПАРАЛЛЕЛЬНОГО РЕКУРСИВНОГО ПОСТРОЕНИЯ ОТЧЁТОВ ===");
        log("Количество параллельных потоков: " + parallelThreads);
        log("Начальный уровень форм: " + finalStartForms.size() + " шт.");

        // Добавляем стартовые формы в очередь
        for (String form : finalStartForms) {
            if (!processedForms.contains(form)) {
                formsQueue.offer(form);
            }
        }

        // Запускаем обработку
        for (int i = 0; i < parallelThreads; i++) {
            executor.submit(new FormWorker());
        }
    }

    /**
     * Воркер для обработки форм
     */
    private class FormWorker implements Runnable {
        @Override
        public void run() {
            while (!stopRequested.get() && isRunning.get()) {
                try {
                    String formPath = formsQueue.poll(500, TimeUnit.MILLISECONDS);
                    if (formPath == null) {
                        // Если очередь пуста и нет активных задач - завершаем
                        if (activeTasks.get() == 0 && formsQueue.isEmpty()) {
                            break;
                        }
                        continue;
                    }

                    // Проверяем, не обработана ли уже форма
                    if (!processedForms.add(formPath)) {
                        continue;
                    }

                    activeTasks.incrementAndGet();

                    try {
                        log("  [Поток " + Thread.currentThread().getId() + "] Обработка формы: " + formPath);

                        if (onFormAnalyzed != null) {
                            javax.swing.SwingUtilities.invokeLater(() -> onFormAnalyzed.accept(formPath));
                        }

                        // Анализируем форму
                        FormInfo formInfo = analyzeForm(formPath);

                        if (formInfo != null) {
                            totalProcessed.incrementAndGet();

                            // Сохраняем отчёт
                            saveReport(formInfo);

                            // Загружаем дочерние формы
                            Set<String> childForms = formsTreePanel.loadChildFormsFromReport(formPath);

                            // Добавляем необработанные дочерние формы в очередь
                            int newChildren = 0;
                            for (String childForm : childForms) {
                                String actualChildPath = childForm.startsWith("(sub)_") ? childForm.substring(6) : childForm;
                                if (!processedForms.contains(actualChildPath) && !formsQueue.contains(actualChildPath)) {
                                    formsQueue.offer(actualChildPath);
                                    newChildren++;
                                }
                            }

                            log("  [Поток " + Thread.currentThread().getId() + "] Форма " + getShortName(formPath) +
                                    " обработана, добавлено детей: " + newChildren);
                        }

                    } catch (Exception e) {
                        error("  [Поток " + Thread.currentThread().getId() + "] Ошибка обработки " + formPath + ": " + e.getMessage());
                    } finally {
                        activeTasks.decrementAndGet();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // Последний поток завершает работу
            if (activeTasks.get() == 0 && formsQueue.isEmpty() && !stopRequested.get()) {
                complete();
            }
        }
    }

    /**
     * Анализ одной формы
     */
    private FormInfo analyzeForm(String formPath) {
        FormAnalyzerService analyzer = new FormAnalyzerService(settings, config);
        analyzer.setFormsToAnalyze(new LinkedHashSet<>(Collections.singletonList(formPath)));
        analyzer.setStopCondition(() -> stopRequested.get());

        try {
            List<FormInfo> results = analyzer.analyzeAllForms();
            analyzer.clearFormsToAnalyze();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            error("  Ошибка анализа формы " + formPath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Сохранение отчёта
     */
    private void saveReport(FormInfo formInfo) {
        try {
            ReportGenerator reportGen = new ReportGenerator(settings.getOutputDir(), config);
            reportGen.createMainReportHeader();
            reportGen.appendFormToMainReport(formInfo);

            javax.swing.SwingUtilities.invokeLater(() -> {
                formsTreePanel.refreshChildForms(formInfo.getFormPath());
            });
        } catch (IOException e) {
            error("  Ошибка сохранения отчёта для " + formInfo.getFormPath() + ": " + e.getMessage());
        }
    }

    /**
     * Завершение обработки
     */
    private void complete() {
        if (isRunning.compareAndSet(true, false)) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            log("");
            log("=== ПАРАЛЛЕЛЬНОЕ РЕКУРСИВНОЕ ПОСТРОЕНИЕ ЗАВЕРШЕНО ===");
            log("Всего обработано форм: " + totalProcessed.get());

            if (onComplete != null) {
                javax.swing.SwingUtilities.invokeLater(onComplete);
            }
        }
    }

    /**
     * Остановка обработки
     */
    public void stop() {
        if (isRunning.get()) {
            log("Запрос на остановку параллельного построения...");
            stopRequested.set(true);
            if (executor != null) {
                executor.shutdownNow();
            }
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    private String getShortName(String formPath) {
        if (formPath.contains("/")) {
            return formPath.substring(formPath.lastIndexOf("/") + 1);
        }
        return formPath;
    }
}