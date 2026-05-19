// core/service/RecursiveReportBuilder.java
package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.ui.FormsTreePanel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private Consumer<String> onLevelStart;
    private Consumer<Integer> onLevelComplete;
    private Consumer<String> onFormAnalyzed;
    private Runnable onComplete;
    private Consumer<String> onError;

    // Статистика
    private int currentLevel = 0;
    private int totalFormsProcessed = 0;
    private Map<Integer, Integer> formsPerLevel = new LinkedHashMap<>();

    public RecursiveReportBuilder(SettingsModel settings, AppConfig config, FormsTreePanel formsTreePanel) {
        this.settings = settings;
        this.config = config;
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

    public void startRecursiveBuild(List<String> startForms) {
        if (isRunning.get()) {
            log("Рекурсивное построение уже выполняется");
            return;
        }

        if (startForms == null || startForms.isEmpty()) {
            startForms = formsTreePanel.getAllRootForms();
        }

        if (startForms.isEmpty()) {
            log("Нет форм для рекурсивного анализа");
            if (onError != null) onError.accept("Нет форм для анализа");
            return;
        }

        stopRequested.set(false);
        isRunning.set(true);
        currentLevel = 0;
        totalFormsProcessed = 0;
        formsPerLevel.clear();

        log("=== ЗАПУСК РЕКУРСИВНОГО ПОСТРОЕНИЯ ОТЧЁТОВ ===");
        log("Начальный уровень форм: " + startForms.size() + " шт.");

        // Создаем финальную копию списка для использования в lambda
        final List<String> finalStartForms = new ArrayList<>(startForms);

        currentTask = executor.submit(() -> {
            try {
                processLevel(finalStartForms, 1);
            } catch (Exception e) {
                error("Ошибка при рекурсивном построении: " + e.getMessage());
                e.printStackTrace();
                if (onError != null) onError.accept(e.getMessage());
            } finally {
                isRunning.set(false);
                if (onComplete != null) {
                    javax.swing.SwingUtilities.invokeLater(onComplete);
                }
                log("=== РЕКУРСИВНОЕ ПОСТРОЕНИЕ ЗАВЕРШЕНО ===");
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
     */
    private void processLevel(List<String> formsToAnalyze, int level) throws Exception {
        if (stopRequested.get()) {
            log("Построение остановлено пользователем на уровне " + level);
            return;
        }

        if (formsToAnalyze == null || formsToAnalyze.isEmpty()) {
            log("Уровень " + level + ": нет форм для анализа");
            return;
        }

        currentLevel = level;

        // Убираем дубликаты
        Set<String> uniqueForms = new LinkedHashSet<>(formsToAnalyze);
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
                log("  Форма " + getShortName(formPath) + " -> дочерних: " + children.size());
                allChildForms.addAll(children);
            }
        }

        // 3. Рекурсивно обрабатываем следующий уровень
        if (!allChildForms.isEmpty() && !stopRequested.get()) {
            final List<String> childList = new ArrayList<>(allChildForms);
            log("");
            log("Переход на уровень " + (level + 1) + " (найдено дочерних форм: " + childList.size() + ")");
            processLevel(childList, level + 1);
        } else {
            if (allChildForms.isEmpty()) {
                log("Дочерние формы не найдены. Рекурсия завершена.");
            }
        }
    }

    /**
     * Анализ списка форм
     */
    private List<FormInfo> analyzeForms(List<String> formPaths) throws Exception {
        final List<FormInfo> results = new ArrayList<>();

        FormAnalyzerService analyzer = new FormAnalyzerService(settings);

        // Устанавливаем прямой список форм
        Set<String> formsSet = new LinkedHashSet<>(formPaths);
        analyzer.setFormsToAnalyze(formsSet);

        // Сохраняем ссылки для использования в лямбдах
        final FormsTreePanel treePanel = this.formsTreePanel;
        final Consumer<String> formAnalyzedCallback = this.onFormAnalyzed;
        final AtomicBoolean stopFlag = this.stopRequested;

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

        // Callback для каждой проанализированной формы
        analyzer.setFormAnalyzedCallback(formInfo -> {
            if (formAnalyzedCallback != null) {
                javax.swing.SwingUtilities.invokeLater(() ->
                        formAnalyzedCallback.accept(formInfo.getFormPath()));
            }
            // Обновляем дерево
            javax.swing.SwingUtilities.invokeLater(() ->
                    treePanel.refreshChildForms(formInfo.getFormPath()));
        });

        // Запускаем анализ
        final List<FormInfo> analyzed = analyzer.analyzeAllForms();
        results.addAll(analyzed);
        analyzer.clearFormsToAnalyze();

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

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getTotalFormsProcessed() {
        return totalFormsProcessed;
    }

    public Map<Integer, Integer> getFormsPerLevel() {
        return new LinkedHashMap<>(formsPerLevel);
    }

    private String getShortName(String formPath) {
        if (formPath.contains("/")) {
            return formPath.substring(formPath.lastIndexOf("/") + 1);
        }
        return formPath;
    }
}