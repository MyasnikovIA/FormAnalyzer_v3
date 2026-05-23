package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.report.ReportGenerator;
import ru.tmis.analyzer.ui.FormsTreePanel;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;


public class RecursiveReportBuilder {

    private final SettingsModel settings;
    private final AppConfig config;
    private final FormsTreePanel formsTreePanel;
    private ILogger logger;

    private ExecutorService executor;
    private Future<?> currentTask;
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private Runnable onTreeRefreshRequested;

    private Consumer<String> onLevelStart;
    private Consumer<Integer> onLevelComplete;
    private Consumer<String> onFormAnalyzed;
    private Runnable onComplete;
    private Consumer<String> onError;

    // Статистика
    private int currentLevel = 0;
    private int totalFormsProcessed = 0;
    private Map<Integer, Integer> formsPerLevel = new LinkedHashMap<>();

    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final Object pauseLock = new Object();


    public RecursiveReportBuilder(SettingsModel settings, AppConfig config, FormsTreePanel formsTreePanel) {
        this.settings = settings;
        this.config = config;  // должно быть сохранено
        this.formsTreePanel = formsTreePanel;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

    public void setOnLevelStart(Consumer<String> callback) {
        this.onLevelStart = callback;
    }

    public void setOnLevelComplete(Consumer<Integer> callback) {
        this.onLevelComplete = callback;
    }

    public void setOnFormAnalyzed(Consumer<String> callback) {
        this.onFormAnalyzed = callback;
    }

    public void setOnComplete(Runnable callback) {
        this.onComplete = callback;
    }

    public void setOnError(Consumer<String> callback) {
        this.onError = callback;
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
     * Запуск рекурсивного построения отчётов
     * @param startForms список форм для старта (если null - используются все корневые формы)
     */
    public void startRecursiveBuild(List<String> startForms) {
        // Проверка на повторный запуск
        if (isRunning.get()) {
            System.err.println("RecursiveReportBuilder уже запущен, повторный запуск игнорируется");
            return;
        }

        // Определяем стартовые формы
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
        currentLevel = 0;
        totalFormsProcessed = 0;
        formsPerLevel.clear();

        final Set<String> processedForms = new HashSet<>();

        log("=== ЗАПУСК РЕКУРСИВНОГО ПОСТРОЕНИЯ ОТЧЁТОВ ===");
        log("Начальный уровень форм: " + finalStartForms.size() + " шт.");

        // Отменяем предыдущую задачу если есть
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }

        currentTask = executor.submit(() -> {
            try {
                processLevel(finalStartForms, 1, processedForms);
            } catch (Exception e) {
                error("Ошибка при рекурсивном построении: " + e.getMessage());
                e.printStackTrace();
                if (onError != null) {
                    javax.swing.SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
                }
            } finally {
                isRunning.set(false);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
                log("");
                if (stopRequested.get()) {
                    log("=== РЕКУРСИВНОЕ ПОСТРОЕНИЕ ОСТАНОВЛЕНО ПОЛЬЗОВАТЕЛЕМ ===");
                } else {
                    log("=== РЕКУРСИВНОЕ ПОСТРОЕНИЕ ЗАВЕРШЕНО ===");
                }
                log("Всего обработано форм: " + totalFormsProcessed);
                log("Уровней: " + formsPerLevel.size());
                for (Map.Entry<Integer, Integer> entry : formsPerLevel.entrySet()) {
                    log("  Уровень " + entry.getKey() + ": " + entry.getValue() + " форм");
                }
            }
        });
    }

    /**
     * Рекурсивная обработка уровня
     * @param formsToAnalyze формы для анализа на текущем уровне
     * @param level номер уровня (1 - корневой)
     * @param processedForms множество уже обработанных форм (для предотвращения зацикливания)
     */
    private void processLevel(List<String> formsToAnalyze, int level, Set<String> processedForms) throws Exception {

        checkPause();
        if (stopRequested.get()) {
            log("Построение остановлено пользователем на уровне " + level);
            return;
        }

        if (formsToAnalyze == null || formsToAnalyze.isEmpty()) {
            log("Уровень " + level + ": нет форм для анализа");
            return;
        }

        // Убираем дубликаты и уже обработанные формы
        Set<String> uniqueForms = new LinkedHashSet<>(formsToAnalyze);
        uniqueForms.removeAll(processedForms);

        if (uniqueForms.isEmpty()) {
            log("Уровень " + level + ": все формы уже обработаны ранее");
            return;
        }

        currentLevel = level;
        final List<String> formList = new ArrayList<>(uniqueForms);

        log("");
        log("┌" + "─".repeat(70));
        log("│ УРОВЕНЬ " + level + " (форм: " + formList.size() + ")");
        log("└" + "─".repeat(70));

        if (onLevelStart != null) {
            javax.swing.SwingUtilities.invokeLater(() ->
                    onLevelStart.accept("Уровень " + level + ": " + formList.size() + " форм"));
        }

        // 1. Анализируем формы текущего уровня
        final List<FormInfo> analyzedForms = analyzeForms(formList);
        totalFormsProcessed += analyzedForms.size();
        formsPerLevel.put(level, analyzedForms.size());

        // Добавляем проанализированные формы в множество обработанных
        processedForms.addAll(formList);

        if (stopRequested.get()) {
            log("Построение остановлено после анализа уровня " + level);
            return;
        }

        log("Уровень " + level + " обработан. Проанализировано форм: " + analyzedForms.size());

        if (onLevelComplete != null) {
            final int analyzedCount = analyzedForms.size();
            javax.swing.SwingUtilities.invokeLater(() ->
                    onLevelComplete.accept(analyzedCount));
        }

        // 2. Собираем дочерние формы из отчётов текущего уровня
        final Set<String> allChildForms = new LinkedHashSet<>();
        for (String formPath : formList) {
            if (stopRequested.get()) break;

            Set<String> children = formsTreePanel.loadChildFormsFromReport(formPath);
            if (!children.isEmpty()) {
                // Фильтруем уже обработанные формы
                Set<String> newChildren = new LinkedHashSet<>(children);
                newChildren.removeAll(processedForms);
                if (!newChildren.isEmpty()) {
                    log("  Форма " + getShortName(formPath) + " -> дочерних: " + newChildren.size() + " (новых: " + newChildren.size() + ")");
                    allChildForms.addAll(newChildren);
                } else {
                    log("  Форма " + getShortName(formPath) + " -> дочерних: " + children.size() + " (все уже обработаны)");
                }
            }
        }

        // 3. Рекурсивно обрабатываем следующий уровень
        if (!allChildForms.isEmpty() && !stopRequested.get()) {
            final List<String> childList = new ArrayList<>(allChildForms);
            log("");
            log("Переход на уровень " + (level + 1) + " (найдено новых дочерних форм: " + childList.size() + ")");
            processLevel(childList, level + 1, processedForms);
        }
    }

    /**
     * Анализ списка форм с сохранением отчётов
     * @param formPaths список путей к формам для анализа
     * @return список проанализированных FormInfo
     */
    private List<FormInfo> analyzeForms(List<String> formPaths) throws Exception {
        final List<FormInfo> results = new ArrayList<>();

        if (formPaths == null || formPaths.isEmpty()) {
            log("  Нет форм для анализа");
            return results;
        }

        log("  Анализ " + formPaths.size() + " форм...");

        FormAnalyzerService analyzer = new FormAnalyzerService(settings);

        // Устанавливаем прямой список форм
        Set<String> formsSet = new LinkedHashSet<>(formPaths);
        analyzer.setFormsToAnalyze(formsSet);

        // Сохраняем ссылки для использования в лямбдах
        final FormsTreePanel treePanel = this.formsTreePanel;
        final Consumer<String> formAnalyzedCallback = this.onFormAnalyzed;
        final AtomicBoolean stopFlag = this.stopRequested;
        final SettingsModel settings = this.settings;
        final AppConfig config = this.config;
        final String outputDir = settings.getOutputDir();

        // Устанавливаем логгер
        analyzer.setLogger(new ILogger() {
            @Override
            public void log(String message) {
                RecursiveReportBuilder.this.log("  " + message);
            }
            @Override
            public void error(String message) {
                RecursiveReportBuilder.this.error("  " + message);
            }
            @Override
            public void debug(String message) {
                RecursiveReportBuilder.this.log("  [DEBUG] " + message);
            }
        });

        // Устанавливаем условие остановки
        analyzer.setStopCondition(() -> stopFlag.get());

        // Устанавливаем callback для прогресса
        analyzer.setProgressCallback((processed, total, currentForm) -> {
            if (formAnalyzedCallback != null) {
                javax.swing.SwingUtilities.invokeLater(() ->
                        formAnalyzedCallback.accept(currentForm));
            }
        });

        // RecursiveReportBuilder.java - метод analyzeForms (фрагмент)

        analyzer.setFormAnalyzedCallback(formInfo -> {
            try {
                // Используем config из конструктора
                ReportGenerator reportGen = new ReportGenerator(settings.getOutputDir(), config);
                reportGen.createMainReportHeader();
                reportGen.appendFormToMainReport(formInfo);
            } catch (IOException e) {
                error("    Ошибка сохранения отчёта для " + formInfo.getFormPath() + ": " + e.getMessage());
            }

            if (formAnalyzedCallback != null) {
                javax.swing.SwingUtilities.invokeLater(() ->
                        formAnalyzedCallback.accept(formInfo.getFormPath()));
            }
            // Обновляем дерево
            javax.swing.SwingUtilities.invokeLater(() ->
                    treePanel.refreshChildForms(formInfo.getFormPath()));
        });

        // Запускаем анализ (ТОТ ЖЕ МЕТОД, что и в обычном анализе)
        final List<FormInfo> analyzed = analyzer.analyzeAllForms();
        results.addAll(analyzed);
        analyzer.clearFormsToAnalyze();

        log("  Проанализировано форм: " + results.size());

        return results;
    }

    /**
     * Остановка рекурсивного построения
     */
    public void stop() {
        if (isRunning.get()) {
            log("Запрос на остановку рекурсивного построения...");
            stopRequested.set(true);
            if (currentTask != null && !currentTask.isDone()) {
                currentTask.cancel(true);
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

    public void setPaused(boolean paused) {
        this.paused.set(paused);
        if (!paused) {
            synchronized (pauseLock) {
                pauseLock.notifyAll();
            }
        }
    }

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

}