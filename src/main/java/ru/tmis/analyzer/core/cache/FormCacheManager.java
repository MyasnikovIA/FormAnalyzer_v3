package ru.tmis.analyzer.core.cache;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.service.FileScannerService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
    public int loadAllForms(String projectPath, java.util.function.Consumer<String> logCallback) {
        if (projectPath == null || projectPath.trim().isEmpty()) {
            if (logCallback != null) logCallback.accept("Путь проекта не указан, загрузка форм невозможна");
            return 0;
        }

        // ========== ПРОВЕРКА: загружаем только если кэширование включено ==========
        if (!FormCache.isEnabled()) {
            if (logCallback != null) {
                logCallback.accept("Режим кэширования форм ВЫКЛЮЧЕН. Формы НЕ загружаются в память.");
                logCallback.accept("Формы будут читаться с диска по мере необходимости.");
            }
            return 0;
        }

        // Проверяем, нужно ли загружать
        if (!needsLoading(projectPath)) {
            if (logCallback != null) logCallback.accept("Формы уже загружены в память (" + FormCache.getCachedFormsCount() + " шт.)");
            return FormCache.getCachedFormsCount();
        }

        // Если загрузка уже выполняется, ждём
        if (isLoading.get()) {
            if (logCallback != null) logCallback.accept("Загрузка форм уже выполняется, ожидание...");
            waitForLoading();
            return FormCache.getCachedFormsCount();
        }

        isLoading.set(true);

        try {
            if (logCallback != null) {
                logCallback.accept("");
                logCallback.accept("=== НАЧАЛО ЗАГРУЗКИ ФОРМ В ПАМЯТЬ ===");
                logCallback.accept("Путь проекта: " + projectPath);
            }

            long startTime = System.currentTimeMillis();

            // Очищаем старый кэш если путь изменился
            if (hasProjectPathChanged(projectPath)) {
                if (logCallback != null) logCallback.accept("Путь проекта изменился, очищаем старый кэш...");
                FormCache.clear();
            }

            // Сканируем и загружаем все формы
            FileScannerService scanner = new FileScannerService(projectPath);
            Set<String> allForms = scanner.findAllBaseForms();
            int totalForms = allForms.size();
            int loaded = 0;
            int errors = 0;

            if (logCallback != null) logCallback.accept("Всего форм для загрузки: " + totalForms);

            for (String formPath : allForms) {
                try {
                    Path physicalPath = scanner.getBaseFormPath(formPath);
                    String content = FormCache.getFormContent(physicalPath, formPath);
                    if (content != null) {
                        loaded++;
                    } else {
                        errors++;
                    }

                    // Прогресс каждые 100 форм
                    if (loaded % 100 == 0 && logCallback != null) {
                        long elapsed = System.currentTimeMillis() - startTime;
                        logCallback.accept(String.format("  Загружено: %d/%d (%.1f%%) за %d сек",
                                loaded, totalForms, (loaded * 100.0) / totalForms, elapsed / 1000));
                    }
                } catch (Exception e) {
                    errors++;
                    if (logCallback != null) logCallback.accept("  Ошибка загрузки: " + formPath + " - " + e.getMessage());
                }
            }

            long totalTime = System.currentTimeMillis() - startTime;

            if (logCallback != null) {
                logCallback.accept("=== ЗАГРУЗКА ФОРМ ЗАВЕРШЕНА ===");
                logCallback.accept("Загружено форм: " + loaded);
                logCallback.accept("Ошибок: " + errors);
                logCallback.accept("Всего: " + totalForms);
                logCallback.accept("Время: " + totalTime / 1000 + " сек");
                logCallback.accept("Форм в кэше: " + FormCache.getCachedFormsCount());

                long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
                logCallback.accept("Использовано RAM: " + usedMemory + " МБ");
                logCallback.accept("");
            }

            lastLoadedProjectPath.set(normalizePath(projectPath));
            isLoaded.set(true);

            return loaded;

        } catch (Exception e) {
            if (logCallback != null) logCallback.accept("Ошибка загрузки форм: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            isLoading.set(false);
        }
    }

    /**
     * Асинхронная загрузка форм
     */
    public void loadAllFormsAsync(String projectPath, java.util.function.Consumer<String> logCallback, Runnable onComplete) {
        Thread loaderThread = new Thread(() -> {
            int loaded = loadAllForms(projectPath, logCallback);
            if (onComplete != null) {
                onComplete.run();
            }
        }, "FormCacheLoader");
        loaderThread.setDaemon(true);
        loaderThread.start();
    }

    /**
     * Ожидание завершения загрузки
     */
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
}