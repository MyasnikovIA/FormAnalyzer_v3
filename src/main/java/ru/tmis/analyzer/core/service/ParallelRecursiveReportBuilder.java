package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.cache.InMemoryReportBuffer;
import ru.tmis.analyzer.core.report.ReportGenerator;
import ru.tmis.analyzer.ui.FormsTreePanel;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ParallelRecursiveReportBuilder {

    private final SettingsModel settings;
    private final AppConfig config;
    private final FormsTreePanel formsTreePanel;
    private ILogger logger;

    private ExecutorService executor;
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Set<String> processedForms = ConcurrentHashMap.newKeySet();
    private final BlockingQueue<String> formsQueue = new LinkedBlockingQueue<>(10000);
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final AtomicInteger totalFound = new AtomicInteger(0);

    private int parallelThreads = Runtime.getRuntime().availableProcessors();
    private boolean fullProjectScan = false;  // Режим полного сканирования проекта

    private Runnable onComplete;
    private Consumer<String> onError;
    private Consumer<String> onFormAnalyzed;
    private Consumer<String> onLevelStart;
    private Consumer<Integer> onLevelComplete;

    // Сканер для поиска новых форм в фоне
    private ScheduledExecutorService scannerExecutor;
    private ScheduledFuture<?> scannerTask;
    private Path projectRoot;
    private long startTime;
    private int totalFormsEstimated = 0;


    public ParallelRecursiveReportBuilder(SettingsModel settings, AppConfig config, FormsTreePanel formsTreePanel) {
        this.settings = settings;
        this.config = config;
        this.formsTreePanel = formsTreePanel;
        this.projectRoot = Paths.get(settings.getProjectPath());
    }

    public void setParallelThreads(int threads) {
        if (threads > 0) {
            this.parallelThreads = threads;
        }
    }

    public void setFullProjectScan(boolean fullScan) {
        this.fullProjectScan = fullScan;
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
        startTime = System.currentTimeMillis();
        if (isRunning.get()) {
            log("Параллельное построение уже выполняется, сначала остановим");
            forceStop();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Сброс состояния
        stopRequested.set(false);
        isRunning.set(true);
        processedForms.clear();
        formsQueue.clear();
        totalProcessed.set(0);
        totalFound.set(0);
        activeTasks.set(0);

        // Создаём новый пул потоков
        executor = Executors.newFixedThreadPool(parallelThreads);

        log("=== ЗАПУСК ПАРАЛЛЕЛЬНОГО РЕКУРСИВНОГО ПОСТРОЕНИЯ ОТЧЁТОВ ===");
        log("Количество параллельных потоков: " + parallelThreads);
        log("Доступно ядер: " + Runtime.getRuntime().availableProcessors());

        // Режим полного сканирования проекта
        if (fullProjectScan || startForms == null || startForms.isEmpty()) {
            log("Режим: СКАНИРОВАНИЕ ВСЕГО ПРОЕКТА");
            startFullProjectScan();
        } else {
            log("Режим: РЕКУРСИВНЫЙ АНАЛИЗ ВЫБРАННЫХ ФОРМ");
            log("Стартовые формы: " + startForms.size() + " шт.");

            // Добавляем стартовые формы в очередь
            for (String form : startForms) {
                if (!processedForms.contains(form)) {
                    formsQueue.offer(form);
                    totalFound.incrementAndGet();
                }
            }

            // Запускаем обработку
            for (int i = 0; i < parallelThreads; i++) {
                executor.submit(new FormWorker());
            }
        }
    }

    /**
     * Режим полного сканирования проекта
     */
    private void startFullProjectScan() {
        // Запускаем фоновый сканер для поиска новых форм
        scannerExecutor = Executors.newSingleThreadScheduledExecutor();
        scannerTask = scannerExecutor.scheduleAtFixedRate(() -> {
            if (stopRequested.get()) {
                return;
            }
            try {
                scanForNewForms();
            } catch (Exception e) {
                error("Ошибка сканирования: " + e.getMessage());
            }
        }, 0, 2, TimeUnit.SECONDS);  // Сканируем каждые 2 секунды

        // Запускаем рабочие потоки
        for (int i = 0; i < parallelThreads; i++) {
            executor.submit(new FormWorker());
        }

        // Запускаем поток мониторинга завершения
        executor.submit(() -> {
            while (!stopRequested.get() && isRunning.get()) {
                try {
                    Thread.sleep(1000);
                    // Если очередь пуста, нет активных задач и сканер завершил первый проход
                    if (activeTasks.get() == 0 && formsQueue.isEmpty()) {
                        // Останавливаем сканер
                        if (scannerTask != null) {
                            scannerTask.cancel(false);
                            scannerExecutor.shutdown();
                        }
                        complete();
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    /**
     * Сканирование файловой системы для поиска новых форм
     */
    private void scanForNewForms() {
        Set<String> foundForms = new LinkedHashSet<>();

        // Сканируем каталог Forms
        Path formsPath = projectRoot.resolve("Forms");
        if (Files.exists(formsPath)) {
            try (Stream<Path> walk = Files.walk(formsPath)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".frm") || p.toString().endsWith(".dfrm"))
                        .forEach(p -> {
                            String relativePath = formsPath.relativize(p).toString().replace("\\", "/");
                            // ПОЛНЫЙ путь: Forms/относительный/путь
                            String formPath = "Forms/" + relativePath;

                            if (!hasReport(formPath) && !processedForms.contains(formPath)) {
                                foundForms.add(formPath);
                            }
                        });
            } catch (IOException e) {
                error("Ошибка сканирования Forms: " + e.getMessage());
            }
        }

        // Сканируем каталоги UserForms
        try (Stream<Path> list = Files.list(projectRoot)) {
            list.filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("UserForms"))
                    .forEach(userFormsDir -> {
                        String dirName = userFormsDir.getFileName().toString();
                        try (Stream<Path> walk = Files.walk(userFormsDir)) {
                            walk.filter(Files::isRegularFile)
                                    .filter(p -> p.toString().endsWith(".frm") || p.toString().endsWith(".dfrm"))
                                    .forEach(p -> {
                                        String relativePath = userFormsDir.relativize(p).toString().replace("\\", "/");
                                        // ПОЛНЫЙ путь: UserFormsXXX/относительный/путь
                                        String formPath = dirName + "/" + relativePath;

                                        if (!hasReport(formPath) && !processedForms.contains(formPath)) {
                                            foundForms.add(formPath);
                                        }
                                    });
                        } catch (IOException e) {
                            error("Ошибка сканирования " + dirName + ": " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            error("Ошибка сканирования UserForms: " + e.getMessage());
        }

        // Добавляем найденные формы в очередь
        if (!foundForms.isEmpty()) {
            int newCount = 0;
            for (String form : foundForms) {
                if (!processedForms.contains(form) && !formsQueue.contains(form)) {
                    formsQueue.offer(form);
                    newCount++;
                    totalFound.incrementAndGet();
                }
            }
            if (newCount > 0) {
                log("  [Сканер] Найдено новых форм: " + newCount + " (всего в очереди: " + formsQueue.size() + ")");
                if (onLevelStart != null) {
                    SwingUtilities.invokeLater(() -> onLevelStart.accept("Найдено форм: " + totalFound.get()));
                }
            }
        }
    }

    /**
     * Проверяет, существует ли уже отчёт для формы
     */
    private boolean hasReport(String formPath) {
        String outputDir = settings.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }

        String normalized = formPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("(sub)_")) {
            normalized = normalized.substring(6);
        }

        // Формируем имя файла как в ReportGenerator
        String safeName = getSafeFileNameForReport(normalized);
        Path reportPath = Paths.get(outputDir, "Forms", safeName);

        return Files.exists(reportPath);
    }

    /**
     * Формирует безопасное имя файла отчёта (как в ReportGenerator)
     */
    private String getSafeFileNameForReport(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        // Убираем расширение .frm или .dfrm для формирования имени
        if (normalized.endsWith(".frm")) {
            normalized = normalized.substring(0, normalized.length() - 4);
        }
        if (normalized.endsWith(".dfrm")) {
            normalized = normalized.substring(0, normalized.length() - 5);
        }
        String safeName = normalized.replace("/", "#").replace("\\", "#");
        return safeName + ".txt";
    }

    /**
     * Воркер для обработки форм
     */
    private class FormWorker implements Runnable {
        @Override
        public void run() {
            while (!stopRequested.get() && isRunning.get() && !Thread.currentThread().isInterrupted()) {
                // Проверка прерывания потока
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                try {
                    String formPath = formsQueue.poll(500, TimeUnit.MILLISECONDS);
                    if (formPath == null) {
                        continue;
                    }

                    // Проверка на остановку перед обработкой
                    if (stopRequested.get()) {
                        break;
                    }

                    activeTasks.incrementAndGet();
                    if (onLevelComplete != null) {
                        int processed = totalProcessed.get();
                        int found = totalFound.get();
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            if (found > 0) {
                                onLevelComplete.accept((processed * 100) / found);
                            } else {
                                onLevelComplete.accept(processed);
                            }
                        });
                    }
                    try {
                        log("  [Поток " + Thread.currentThread().threadId() + "] Обработка формы: " + formPath);

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

                            log("  [Поток " + Thread.currentThread().threadId() + "] Форма " + getShortName(formPath) +
                                    " обработана, добавлено детей: " + newChildren);

                            // Обновляем прогресс
                            if (onLevelComplete != null) {
                                javax.swing.SwingUtilities.invokeLater(() -> onLevelComplete.accept(totalProcessed.get()));
                            }
                        }

                    } catch (Exception e) {
                        error("  [Поток " + Thread.currentThread().threadId() + "] Ошибка обработки " + formPath + ": " + e.getMessage());
                    } finally {
                        activeTasks.decrementAndGet();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
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
        if (formInfo == null) return;

        try {
            // Синхронизация для предотвращения одновременной записи
            synchronized (ReportGenerator.class) {
                ReportGenerator reportGen = new ReportGenerator(settings.getOutputDir(), config);
                reportGen.createMainReportHeader();
                reportGen.appendFormToMainReport(formInfo);
            }

            // Обновляем дерево после сохранения
            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    // Обновляем дочерние формы для только что обработанной формы
                    formsTreePanel.refreshChildForms(formInfo.getFormPath());

                    // Также обновляем родительскую форму (если есть)
                    String parentPath = getParentPath(formInfo.getFormPath());
                    if (parentPath != null) {
                        formsTreePanel.refreshChildForms(parentPath);
                    }
                } catch (Exception e) {
                    error("  Ошибка обновления дерева для " + formInfo.getFormPath() + ": " + e.getMessage());
                }
            });

        } catch (IOException e) {
            error("  Ошибка сохранения отчёта для " + formInfo.getFormPath() + ": " + e.getMessage());
        }
    }

    /**
     * Получить путь родительской формы
     */
    private String getParentPath(String formPath) {
        if (formPath == null) return null;
        int lastSlash = formPath.lastIndexOf("/");
        if (lastSlash > 0) {
            return formPath.substring(0, lastSlash);
        }
        return null;
    }



    /**
     * Остановка обработки
     */
    public void stop() {
        if (isRunning.get()) {
            log("Запрос на остановку параллельного построения...");
            stopRequested.set(true);

            // Прерываем все рабочие потоки
            if (executor != null) {
                executor.shutdownNow();  // immediately interrupt
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        log("Некоторые потоки не завершились, принудительное завершение");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Останавливаем сканер
            if (scannerExecutor != null) {
                scannerExecutor.shutdownNow();
            }

            // Очищаем очередь
            formsQueue.clear();
            activeTasks.set(0);

            // Сбрасываем флаг выполнения
            isRunning.set(false);

            log("Параллельное построение остановлено");
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
    public int getTotalProcessed() {
        return totalProcessed.get();
    }

    public int getTotalFound() {
        return totalFound.get();
    }

    public int getQueueSize() {
        return formsQueue.size();
    }

    public int getActiveTasks() {
        return activeTasks.get();
    }
    public void forceStop() {
        log("Принудительная остановка...");
        stopRequested.set(true);

        // Прерываем все рабочие потоки (без ожидания)
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        if (scannerExecutor != null) {
            scannerExecutor.shutdownNow();
            scannerExecutor = null;
        }

        // Очищаем данные
        formsQueue.clear();
        activeTasks.set(0);
        processedForms.clear();
        totalProcessed.set(0);
        totalFound.set(0);

        // Сбрасываем флаг выполнения
        isRunning.set(false);

        log("Параллельное построение принудительно остановлено");
    }
    private void updateProgress() {
        long elapsed = System.currentTimeMillis() - startTime;
        int processed = totalProcessed.get();
        if (processed > 0 && elapsed > 0) {
            long estimatedTotal = (long) (elapsed * totalFound.get() / (double) processed);
            long remaining = estimatedTotal - elapsed;
            // Показать оставшееся время
        }
    }
    private void complete() {
        if (isRunning.compareAndSet(true, false)) {
            // Останавливаем сканер
            if (scannerTask != null) {
                scannerTask.cancel(false);
                if (scannerExecutor != null) {
                    scannerExecutor.shutdown();
                }
            }

            // Останавливаем рабочие потоки
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            log("");
            log("=== ПАРАЛЛЕЛЬНОЕ РЕКУРСИВНОЕ ПОСТРОЕНИЕ ЗАВЕРШЕНО ===");
            log("Всего обработано форм: " + totalProcessed.get());
            log("Всего найдено форм: " + totalFound.get());

            // ========== ВЫГРУЗКА БУФЕРА НА ДИСК ==========
            log("Выгрузка отчётов на диск...");
            try {
                InMemoryReportBuffer.flushToDisk(settings.getOutputDir());
            } catch (IOException e) {
                error("Ошибка выгрузки буфера: " + e.getMessage());
                e.printStackTrace();
            }
            // ============================================

            // Принудительно обновляем всё дерево
            javax.swing.SwingUtilities.invokeLater(() -> {
                formsTreePanel.refreshAllChildFormsWithCleanup();
                formsTreePanel.refreshTreeWithState();
            });

            if (onComplete != null) {
                javax.swing.SwingUtilities.invokeLater(onComplete);
            }
        }
    }
}