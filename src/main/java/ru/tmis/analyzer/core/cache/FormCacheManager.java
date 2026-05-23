package ru.tmis.analyzer.core.cache;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.service.FileScannerService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import ru.tmis.analyzer.core.report.CSVReportGenerator;
import ru.tmis.analyzer.core.report.JSONReportGenerator;

/**
 * Менеджер кэша форм с ленивой загрузкой
 */
public class FormCacheManager {

    private static final FormCacheManager instance = new FormCacheManager();
    private final AtomicBoolean isLoading = new AtomicBoolean(false);
    private final AtomicBoolean isLoaded = new AtomicBoolean(false);



    private final AtomicReference<String> lastLoadedProjectPath = new AtomicReference<>(null);

    private FormCacheManager() {}

    public static FormCacheManager getInstance() {
        return instance;
    }

    /**
     * Проверяет, изменился ли путь проекта
     * @param currentProjectPath текущий путь из настроек
     * @return true если путь изменился
     */
    public boolean hasProjectPathChanged(String currentProjectPath) {
        String lastPath = lastLoadedProjectPath.get();
        if (lastPath == null) return true;
        return !normalizePath(lastPath).equals(normalizePath(currentProjectPath));
    }

    /**
     * Проверяет, загружены ли формы в кэш
     */
    public boolean isFormsLoaded() {
        return isLoaded.get() && FormCache.getCachedFormsCount() > 0;
    }

    /**
     * Проверяет, нужно ли загружать формы
     * @param currentProjectPath текущий путь проекта
     * @return true если нужно загрузить
     */
    public boolean needsLoading(String currentProjectPath) {
        if (currentProjectPath == null || currentProjectPath.trim().isEmpty()) {
            return false;
        }
        return !isFormsLoaded() || hasProjectPathChanged(currentProjectPath);
    }

    /**
     * Загружает все формы проекта в кэш (синхронно)
     * @param projectPath путь к проекту
     * @return количество загруженных форм
     */
    // core/cache/FormCacheManager.java

    public int loadAllForms(String projectPath, java.util.function.Consumer<String> logCallback) {
        if (!FormCache.isEnabled()) {
            if (logCallback != null) {
                logCallback.accept("Проверка пути: " + projectPath);
                logCallback.accept("Папка существует: " + Files.exists(Paths.get(projectPath)));
                logCallback.accept("Папка Forms существует: " + Files.exists(Paths.get(projectPath, "Forms")));
            }
            return 0;
        }

        // ========== ИСПРАВЛЕНИЕ: проверяем реальное состояние кэша ==========
        if (FormCache.getCachedFormsCount() > 0) {
            if (logCallback != null) {
                logCallback.accept("Формы уже загружены в память (" + FormCache.size() + " шт.)");
            }
            return FormCache.size();
        }
        // ===================================================================

        if (!isLoading.compareAndSet(false, true)) {
            if (logCallback != null) {
                logCallback.accept("Загрузка форм уже выполняется, ожидание...");
            }
            waitForLoading();
            return FormCache.size();
        }

        try {
            long startTime = System.currentTimeMillis();

            if (logCallback != null) {
                logCallback.accept("");
                logCallback.accept("========================================");
                logCallback.accept("ЗАГРУЗКА ФОРМ В ОПЕРАТИВНУЮ ПАМЯТЬ");
                logCallback.accept("========================================");
                logCallback.accept("Путь проекта: " + projectPath);
            }

            // Очищаем старый кэш
            FormCache.clear();

            // Сканируем ВСЕ формы
            FileScannerService scanner = new FileScannerService(projectPath);
            Set<String> allForms = scanner.findAllForms();
            int totalForms = allForms.size();
            int loaded = 0;
            int errors = 0;

            if (logCallback != null) {
                logCallback.accept("Всего форм для загрузки: " + totalForms);
            }

            if (totalForms == 0) {
                if (logCallback != null) {
                    logCallback.accept("ОШИБКА: Не найдено ни одной формы по пути: " + projectPath);
                    logCallback.accept("Проверьте, что путь указан правильно и содержит папку Forms/");
                }
                isLoaded.set(false);  // Сбрасываем флаг
                return 0;
            }

            for (String formPath : allForms) {
                try {
                    Path physicalPath = scanner.getBaseFormPath(formPath);
                    if (Files.exists(physicalPath)) {
                        String content = Files.readString(physicalPath);
                        FormCache.putFormContent(formPath, content);
                        loaded++;
                    } else {
                        errors++;
                    }
                } catch (Exception e) {
                    errors++;
                    if (logCallback != null) {
                        logCallback.accept("  [ОШИБКА] " + formPath + ": " + e.getMessage());
                    }
                }

                if (logCallback != null && loaded % 100 == 0 && loaded > 0) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    double percent = (loaded * 100.0) / totalForms;
                    logCallback.accept(String.format("  Прогресс: %d/%d (%.1f%%) за %d сек",
                            loaded, totalForms, percent, elapsed / 1000));
                }
            }

            long totalTime = System.currentTimeMillis() - startTime;
            long memoryMB = FormCache.getMemoryUsageBytes() / (1024 * 1024);

            if (logCallback != null) {
                logCallback.accept("");
                logCallback.accept("========================================");
                logCallback.accept("ЗАГРУЗКА ЗАВЕРШЕНА");
                logCallback.accept("========================================");
                logCallback.accept("Загружено форм: " + loaded);
                logCallback.accept("Ошибок: " + errors);
                logCallback.accept("Всего: " + totalForms);
                logCallback.accept("Время: " + totalTime / 1000 + " сек");
                logCallback.accept("RAM использовано: ~" + memoryMB + " МБ");
                logCallback.accept("========================================");
                logCallback.accept("");
            }

            isLoaded.set(loaded > 0);
            lastLoadedProjectPath.set(normalizePath(projectPath));
            return loaded;

        } catch (Exception e) {
            if (logCallback != null) {
                logCallback.accept("Ошибка загрузки форм: " + e.getMessage());
            }
            isLoaded.set(false);  // Сбрасываем флаг при ошибке
            return 0;
        } finally {
            isLoading.set(false);
        }
    }

    /**
     * Асинхронная предзагрузка
     */
    public CompletableFuture<Integer> loadAllFormsAsync(String projectPath,
                                                        java.util.function.Consumer<String> logCallback) {
        return CompletableFuture.supplyAsync(() -> loadAllForms(projectPath, logCallback));
    }

    public boolean isLoaded() {
        return isLoaded.get();
    }

    public int getLoadedCount() {
        return FormCache.size();
    }

    private void waitForLoading() {
        while (isLoading.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Принудительная перезагрузка форм
     */
    public void reloadForms(String projectPath, java.util.function.Consumer<String> logCallback) {
        if (logCallback != null) logCallback.accept("Принудительная перезагрузка форм...");
        FormCache.clear();
        isLoaded.set(false);
        lastLoadedProjectPath.set(null);
        loadAllForms(projectPath, logCallback);
    }

    /**
     * Очищает кэш форм
     */
    public void clearCache() {
        FormCache.clear();
        isLoaded.set(false);
        lastLoadedProjectPath.set(null);
    }

    /**
     * Нормализует путь для сравнения
     */
    private String normalizePath(String path) {
        if (path == null) return null;
        return Paths.get(path).normalize().toString().replace("\\", "/");
    }

    // Getters
    public boolean isLoading() { return isLoading.get(); }
    public String getLastLoadedProjectPath() { return lastLoadedProjectPath.get(); }
    public int getCachedFormsCount() { return FormCache.getCachedFormsCount(); }

    /**
     * Параллельная предзагрузка всех форм в память (многопоточная)
     * @param projectPath путь к проекту
     * @param logCallback callback для логирования
     * @param parallelThreads количество потоков для загрузки (0 = все ядра)
     * @return количество загруженных форм
     */
    public int loadAllFormsParallel(String projectPath, java.util.function.Consumer<String> logCallback, int parallelThreads) {
        if (!FormCache.isEnabled()) {
            if (logCallback != null) {
                logCallback.accept("Режим кэширования ВЫКЛЮЧЕН. Формы НЕ загружаются в память.");
            }
            return 0;
        }

        if (isLoaded.get()) {
            if (logCallback != null) {
                logCallback.accept("Формы уже загружены в память (" + FormCache.size() + " шт.)");
            }
            return FormCache.size();
        }

        if (!isLoading.compareAndSet(false, true)) {
            if (logCallback != null) {
                logCallback.accept("Загрузка форм уже выполняется, ожидание...");
            }
            waitForLoading();
            return FormCache.size();
        }

        try {
            long startTime = System.currentTimeMillis();

            if (logCallback != null) {
                logCallback.accept("");
                logCallback.accept("========================================");
                logCallback.accept("ПАРАЛЛЕЛЬНАЯ ЗАГРУЗКА ФОРМ В ОПЕРАТИВНУЮ ПАМЯТЬ");
                logCallback.accept("========================================");
                logCallback.accept("Путь проекта: " + projectPath);
            }

            // Очищаем старый кэш
            FormCache.clear();

            // Сканируем ВСЕ формы
            FileScannerService scanner = new FileScannerService(projectPath);
            Set<String> allForms = scanner.findAllForms();
            List<String> formsList = new ArrayList<>(allForms);
            int totalForms = formsList.size();

            if (logCallback != null) {
                logCallback.accept("Всего форм для загрузки: " + totalForms);
            }

            // Определяем количество потоков
            int threads = parallelThreads > 0 ? parallelThreads : Runtime.getRuntime().availableProcessors();
            int formsPerThread = (int) Math.ceil((double) totalForms / threads);

            if (logCallback != null) {
                logCallback.accept("Используется потоков: " + threads);
                logCallback.accept("Форм на поток: ~" + formsPerThread);
            }

            // Создаём пул потоков
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<LoadResult>> futures = new ArrayList<>();
            AtomicInteger totalLoaded = new AtomicInteger(0);
            AtomicInteger totalErrors = new AtomicInteger(0);

            // Запускаем загрузку в параллельных потоках
            for (int i = 0; i < threads; i++) {
                int startIdx = i * formsPerThread;
                int endIdx = Math.min(startIdx + formsPerThread, totalForms);

                if (startIdx >= totalForms) break;

                List<String> batch = formsList.subList(startIdx, endIdx);
                final int threadId = i;

                futures.add(executor.submit(() -> {
                    int loaded = 0;
                    int errors = 0;
                    for (String formPath : batch) {
                        try {
                            Path physicalPath = scanner.getBaseFormPath(formPath);
                            if (Files.exists(physicalPath)) {
                                String content = Files.readString(physicalPath);
                                FormCache.putFormContent(formPath, content);
                                loaded++;
                            } else {
                                errors++;
                            }
                        } catch (Exception e) {
                            errors++;
                            if (logCallback != null) {
                                logCallback.accept("  [Поток " + threadId + "] Ошибка: " + formPath + " - " + e.getMessage());
                            }
                        }
                    }
                    totalLoaded.addAndGet(loaded);
                    totalErrors.addAndGet(errors);
                    return new LoadResult(loaded, errors);
                }));
            }

            // Ожидаем завершения всех потоков
            for (Future<LoadResult> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    if (logCallback != null) {
                        logCallback.accept("Ошибка в потоке загрузки: " + e.getMessage());
                    }
                }
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);

            long totalTime = System.currentTimeMillis() - startTime;
            long memoryMB = FormCache.getMemoryUsageBytes() / (1024 * 1024);

            if (logCallback != null) {
                logCallback.accept("");
                logCallback.accept("========================================");
                logCallback.accept("ЗАГРУЗКА ЗАВЕРШЕНА");
                logCallback.accept("========================================");
                logCallback.accept("Загружено форм: " + totalLoaded.get());
                logCallback.accept("Ошибок: " + totalErrors.get());
                logCallback.accept("Всего: " + totalForms);
                logCallback.accept("Время: " + totalTime / 1000 + " сек");
                logCallback.accept("Скорость: " + (totalLoaded.get() * 1000 / Math.max(1, totalTime)) + " форм/сек");
                logCallback.accept("RAM использовано: ~" + memoryMB + " МБ");
                logCallback.accept("========================================");
                logCallback.accept("");
            }

            isLoaded.set(totalLoaded.get() > 0);
            lastLoadedProjectPath.set(normalizePath(projectPath));
            return totalLoaded.get();

        } catch (Exception e) {
            if (logCallback != null) {
                logCallback.accept("Ошибка загрузки форм: " + e.getMessage());
            }
            return 0;
        } finally {
            isLoading.set(false);
        }
    }

    private static class LoadResult {
        final int loaded;
        final int errors;
        LoadResult(int loaded, int errors) { this.loaded = loaded; this.errors = errors; }
    }
}