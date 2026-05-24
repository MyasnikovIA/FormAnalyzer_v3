// core/service/ParallelRecursiveReportBuilder.java
package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.InMemoryReportBuffer;
import ru.tmis.analyzer.core.db.DatabaseConnectionManager;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.report.FormCSVWriter;
import ru.tmis.analyzer.core.report.ReportGenerator;
import ru.tmis.analyzer.ui.FormsTreePanel;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final AtomicInteger totalFound = new AtomicInteger(0);
    private final Object pauseLock = new Object();

    private int parallelThreads = Runtime.getRuntime().availableProcessors();
    private List<String> allFormsList;
    private ExecutorService executor;
    private List<Future<?>> futures;

    private Runnable onComplete;
    private Consumer<String> onError;
    private Consumer<String> onFormAnalyzed;
    private ProgressCallback onProgress;
    private Set<String> skipForms = new HashSet<>();
    private FormCSVWriter formCSVWriter;


    public ParallelRecursiveReportBuilder(SettingsModel settings, AppConfig config, FormsTreePanel formsTreePanel) {
        this.settings = settings;
        this.config = config;
        this.formsTreePanel = formsTreePanel;
        this.formCSVWriter = new FormCSVWriter(settings.getOutputDir());  // ← ДОБАВИТЬ
    }

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


    public void setFullProjectScan(boolean fullScan) {
        // Для совместимости
    }

    /**
     * Установить состояние паузы
     */
    public void setPaused(boolean pause) {
        if (pause == paused.get()) return;

        paused.set(pause);
        if (!pause) {
            // Возобновляем - пробуждаем все ожидающие потоки
            synchronized (pauseLock) {
                pauseLock.notifyAll();
            }
            log("▶ Процесс возобновлён");
        } else {
            log("⏸ Процесс поставлен на паузу");
        }
    }

    public boolean isPaused() {
        return paused.get();
    }

    /**
     * Проверка паузы (вызывается в рабочих потоках)
     */
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

    private void log(String message) {
        if (logger != null) logger.log(message);
        System.out.println(message);
    }

    private void error(String message) {
        if (logger != null) logger.error(message);
        System.err.println(message);
    }

    /**
     * Запуск параллельного построения отчётов
     */
    public void startRecursiveBuild(List<String> startForms) {
        if (isRunning.get()) {
            log("Построение уже выполняется");
            return;
        }

        stopRequested.set(false);
        paused.set(false);
        isRunning.set(true);
        totalProcessed.set(0);
        totalFound.set(0);

        log("=== ЗАПУСК ПАРАЛЛЕЛЬНОГО ПОСТРОЕНИЯ ОТЧЁТОВ ===");
        log("Количество параллельных потоков: " + parallelThreads);
        log("Доступно ядер: " + Runtime.getRuntime().availableProcessors());

        // Получаем список всех форм для обработки
        List<String> formsToProcess;

        if (startForms == null || startForms.isEmpty()) {
            // Полное сканирование проекта
            FileScannerService scanner = new FileScannerService(settings.getProjectPath());
            Set<String> allForms = scanner.findAllForms();
            allFormsList = new ArrayList<>(allForms);

            // Фильтруем: оставляем только формы без отчётов И не в списке пропуска
            allFormsList.removeIf(formPath -> hasReport(formPath) || shouldSkipForm(formPath));

            formsToProcess = allFormsList;
            totalFound.set(formsToProcess.size());
            log("Всего форм в проекте: " + allForms.size());
            log("Форм без отчётов: " + totalFound.get());
            if (!skipForms.isEmpty()) {
                log("Пропущено уже обработанных форм: " + skipForms.size());
            }
        } else {
            // Рекурсивный анализ выбранных форм
            formsToProcess = new ArrayList<>(startForms);
            formsToProcess.removeIf(formPath -> shouldSkipForm(formPath));
            totalFound.set(formsToProcess.size());
            log("Выбрано форм для анализа: " + totalFound.get());
            if (!skipForms.isEmpty()) {
                log("Из них уже обработано (пропущено): " + (startForms.size() - formsToProcess.size()));
            }
        }

        if (formsToProcess.isEmpty()) {
            log("Нет форм для анализа");
            complete();
            return;
        }

        // Разделяем формы между потоками
        int formsPerThread = (int) Math.ceil((double) formsToProcess.size() / parallelThreads);
        executor = Executors.newFixedThreadPool(parallelThreads);
        futures = new ArrayList<>();

        for (int threadId = 0; threadId < parallelThreads; threadId++) {
            int startIdx = threadId * formsPerThread;
            int endIdx = Math.min(startIdx + formsPerThread, formsToProcess.size());

            if (startIdx >= formsToProcess.size()) break;

            List<String> threadForms = formsToProcess.subList(startIdx, endIdx);

            log("Поток " + threadId + ": получил " + threadForms.size() + " форм");

            futures.add(executor.submit(new FormProcessor(threadId, threadForms)));
        }

        // Мониторинг завершения
        executor.submit(() -> {
            try {
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (Exception e) {
                        error("Ошибка в потоке: " + e.getMessage());
                    }
                }

                executor.shutdown();
                while (!executor.isTerminated()) {
                    Thread.sleep(1000);
                }

                complete();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    // core/service/ParallelRecursiveReportBuilder.java

    /**
     * Воркер для обработки группы форм (ОДИН НА ПОТОК)
     */
    private class FormProcessor implements Runnable {
        private final int threadId;
        private final List<String> forms;
        private final FormAnalyzerService analyzer;
        private int localProcessed = 0;  // локальный счётчик для этого потока

        public FormProcessor(int threadId, List<String> forms) {
            this.threadId = threadId;
            this.forms = forms;
            this.analyzer = new FormAnalyzerService(settings, config);
            this.analyzer.setLogger(new ILogger() {
                @Override public void log(String msg) { ParallelRecursiveReportBuilder.this.log("[Поток " + threadId + "] " + msg); }
                @Override public void error(String msg) { ParallelRecursiveReportBuilder.this.error("[Поток " + threadId + "] " + msg); }
                @Override public void debug(String msg) { ParallelRecursiveReportBuilder.this.log("[Поток " + threadId + "] [DEBUG] " + msg); }
            });
            this.analyzer.setStopCondition(() -> stopRequested.get());
            this.analyzer.setPaused(() -> paused.get());

            // Увеличиваем счётчик активных задач
            activeTasks.incrementAndGet();
        }

        @Override
        public void run() {
            log("Поток " + threadId + " запущен, обработает " + forms.size() + " форм");

            try {
                for (String formPath : forms) {
                    if (stopRequested.get()) {
                        log("Поток " + threadId + " остановлен");
                        break;
                    }

                    if (paused.get()) {
                        checkPause();
                    }

                    try {
                        if (onFormAnalyzed != null) {
                            SwingUtilities.invokeLater(() -> onFormAnalyzed.accept(formPath));
                        }

                        log("Обработка: " + formPath);

                        FormInfo formInfo = analyzer.analyzeForm(formPath);

                        if (formInfo != null) {
                            saveReport(formInfo);
                            SwingUtilities.invokeLater(() -> {
                                formsTreePanel.refreshChildForms(formInfo.getFormPath());
                            });
                        }

                        localProcessed++;
                        int totalProcessedSoFar = totalProcessed.incrementAndGet();

                        // Обновляем прогресс
                        updateProgress(totalProcessedSoFar);

                    } catch (Exception e) {
                        error("Ошибка обработки " + formPath + ": " + e.getMessage());
                    }
                }
            } finally {
                analyzer.shutdown();
                // Уменьшаем счётчик активных задач
                int remaining = activeTasks.decrementAndGet();
                log("Поток " + threadId + " завершил работу, обработано " + localProcessed +
                        " форм, осталось активных потоков: " + remaining);

                // Финальное обновление прогресса
                updateProgress(totalProcessed.get());
            }
        }

        private void updateProgress(int processed) {
            if (onProgress != null) {
                final int current = processed;
                final int total = totalFound.get();
                final int percent = total > 0 ? (current * 100) / total : 0;
                final int active = activeTasks.get();
                final int remaining = total - current;

                SwingUtilities.invokeLater(() -> {
                    onProgress.onProgress(current, total, percent, active, remaining);
                });
            }
        }
    }

    private void saveReport(FormInfo formInfo) {
        try {
            // ВСЕГДА сохраняем CSV для формы
            formCSVWriter.saveFormCSV(formInfo);

            ReportGenerator reportGen = new ReportGenerator(settings.getOutputDir(), config);
            reportGen.createMainReportHeader();
            reportGen.appendFormToMainReport(formInfo);
        } catch (IOException e) {
            error("Ошибка сохранения отчёта для " + formInfo.getFormPath() + ": " + e.getMessage());
        }
    }

    private boolean hasReport(String formPath) {
        String outputDir = settings.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }

        String normalized = formPath;
        if (normalized.startsWith("/")) normalized = normalized.substring(1);
        if (normalized.startsWith("(sub)_")) normalized = normalized.substring(6);

        String safeName = getSafeFileNameForReport(normalized);
        Path reportPath = Paths.get(outputDir, "Forms", safeName);

        return Files.exists(reportPath);
    }

    private String getSafeFileNameForReport(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) normalized = normalized.substring(1);
        if (normalized.endsWith(".frm")) normalized = normalized.substring(0, normalized.length() - 4);
        if (normalized.endsWith(".dfrm")) normalized = normalized.substring(0, normalized.length() - 5);
        return normalized.replace("/", "#").replace("\\", "#") + ".txt";
    }

    private void complete() {
        if (isRunning.compareAndSet(true, false)) {
            log("");
            log("=== ПАРАЛЛЕЛЬНОЕ ПОСТРОЕНИЕ ЗАВЕРШЕНО ===");
            log("Всего обработано форм: " + totalProcessed.get());
            log("Всего найдено форм: " + totalFound.get());

            if (config != null && config.isUseMemoryCache()) {
                InMemoryReportBuffer.flushToDisk(settings.getOutputDir());
            }

            SwingUtilities.invokeLater(() -> {
                formsTreePanel.refreshAllChildFormsWithCleanup();
                formsTreePanel.refreshTreeWithState();
            });

            if (onComplete != null) {
                SwingUtilities.invokeLater(onComplete);
            }
        }
    }

    public void stop() {
        if (isRunning.get()) {
            log("Запрос на остановку...");
            stopRequested.set(true);
            paused.set(false);
            synchronized (pauseLock) {
                pauseLock.notifyAll();
            }
        }
    }

    public void forceStop() {
        stop();
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public int getTotalProcessed() {
        return totalProcessed.get();
    }

    public int getTotalFound() {
        return totalFound.get();
    }

    public int getQueueSize() {
        return 0;
    }

    public int getActiveTasks() {
        return activeTasks.get();
    }
    public interface ProgressCallback {
        void onProgress(int processed, int total, int percent, int activeTasks, int remaining);
    }

    public void setOnProgress(ProgressCallback callback) {
        this.onProgress = callback;
    }

    /**
     * Установить список форм, которые нужно пропустить (уже обработаны)
     */
    public void setSkipProcessedForms(Set<String> processedForms) {
        if (processedForms != null) {
            this.skipForms = new HashSet<>(processedForms);
            log("Будут пропущены уже обработанные формы: " + skipForms.size() + " шт.");
        }
    }

    /**
     * Проверить, нужно ли пропустить форму
     */
    private boolean shouldSkipForm(String formPath) {
        return skipForms.contains(formPath);
    }
}