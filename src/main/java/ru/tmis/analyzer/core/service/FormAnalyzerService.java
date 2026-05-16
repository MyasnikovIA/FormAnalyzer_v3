// core/service/FormAnalyzerService.java (исправленный)
package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.extractor.ExtractorManager;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;
import ru.tmis.analyzer.utils.CommentRemover;
import ru.tmis.analyzer.utils.FormPathUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public class FormAnalyzerService {

    private final SettingsModel settings;
    private final FileScannerService scannerService;
    private final UserFormsResolver userFormsResolver;
    private final ExtractorManager extractorManager;

    private BooleanSupplier stopCondition = () -> false;
    private ProgressCallback progressCallback;
    private ILogger logger;  // <-- Добавить логгер

    public interface ProgressCallback {
        void onProgress(int processed, int total, String currentForm);
    }

    public FormAnalyzerService(SettingsModel settings) {
        this.settings = settings;
        this.scannerService = new FileScannerService(settings.getProjectPath());
        this.userFormsResolver = new UserFormsResolver(scannerService);
        this.extractorManager = new ExtractorManager();
    }
    public void setLogger(ILogger logger) {
        this.logger = logger;
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


    public void setStopCondition(BooleanSupplier condition) {
        this.stopCondition = condition;
    }

    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }

    public List<FormInfo> analyzeAllForms() throws IOException {
        Set<String> formsToAnalyze = getFormsToAnalyze();
        List<FormInfo> results = new ArrayList<>();

        System.out.println("Найдено форм для анализа: " + formsToAnalyze.size());

        int processed = 0;
        int total = formsToAnalyze.size();

        for (String formPath : formsToAnalyze) {
            if (stopCondition.getAsBoolean()) {
                System.out.println("Анализ остановлен пользователем");
                break;
            }

            processed++;
            if (progressCallback != null) {
                progressCallback.onProgress(processed, total, formPath);
            }

            System.out.print("Анализ [" + processed + "/" + total + "]: " + formPath + " ... ");

            try {
                FormInfo formInfo = analyzeForm(formPath);
                if (formInfo != null && formInfo.getSqlQueries() != null && !formInfo.getSqlQueries().isEmpty()) {
                    results.add(formInfo);
                    System.out.println("OK (SQL: " + formInfo.getSqlQueries().size() + ")");
                } else if (formInfo != null) {
                    System.out.println("OK (SQL: 0)");
                } else {
                    System.out.println("ПРОПУЩЕН");
                }
            } catch (Exception e) {
                System.err.println("ОШИБКА: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return results;
    }

    public FormInfo analyzeForm(String formPath) {
        String normalizedPath = FormPathUtils.normalizeFormPath(formPath);

        System.out.println("  Проверка формы: " + normalizedPath);

        if (!scannerService.baseFormExists(normalizedPath)) {
            System.err.println("Базовая форма не найдена: " + normalizedPath);
            return null;
        }

        FormInfo formInfo = userFormsResolver.resolveOverrides(normalizedPath);

        Path baseFormPathObj = scannerService.getBaseFormPath(normalizedPath);
        formInfo.setBaseFormPath(baseFormPathObj.toString());

        String baseContent = scannerService.readFileContent(baseFormPathObj);
        if (baseContent == null) {
            System.err.println("Не удалось прочитать содержимое формы: " + baseFormPathObj);
            return null;
        }

        baseContent = CommentRemover.removeAllComments(baseContent);

        extractorManager.process(baseContent, formInfo);

        // Собираем все вьюхи, используемые в этой форме
        Set<String> viewNames = new LinkedHashSet<>();
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_V_")) {
                viewNames.add(tv);
            }
        }

        // Загружаем зависимости вьюх
        if (!viewNames.isEmpty()) {
            Map<String, ViewTableDependencies> viewDeps = loadViewDependencies(viewNames);
            formInfo.setViewDependencies(viewDeps);
            log("  Сохранено зависимостей вьюх: " + viewDeps.size() + " шт.");

            // Отладочный вывод: какие таблицы найдены
            for (Map.Entry<String, ViewTableDependencies> entry : viewDeps.entrySet()) {
                log("    Вьюха " + entry.getKey() + " содержит " + entry.getValue().getOracleTables().size() + " таблиц");
            }
        }

        return formInfo;
    }

    private Set<String> getFormsToAnalyze() throws IOException {
        Set<String> forms = new LinkedHashSet<>();
        Path listFile = Paths.get("forms_list.txt");

        if (Files.exists(listFile)) {
            List<String> lines = Files.readAllLines(listFile);
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    // Нормализуем путь
                    String normalized = FormPathUtils.normalizeFormPath(trimmed);
                    forms.add(normalized);
                    System.out.println("  Добавлена форма из списка: " + normalized);
                }
            }
            if (!forms.isEmpty()) {
                return forms;
            }
        }

        // Если список пуст - сканируем все формы
        System.out.println("Список форм пуст, сканируем проект...");
        return scanAllForms();
    }

    private Set<String> scanAllForms() throws IOException {
        Set<String> allForms = new LinkedHashSet<>();
        Path rootPath = Paths.get(settings.getProjectPath());
        Path formsPath = rootPath.resolve("Forms");

        if (Files.exists(formsPath)) {
            try (Stream<Path> walk = Files.walk(formsPath)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".frm"))
                        .forEach(p -> {
                            String relativePath = formsPath.relativize(p).toString().replace("\\", "/");
                            allForms.add("/Forms/" + relativePath);
                            System.out.println("  Найдена форма: /Forms/" + relativePath);
                        });
            }
        } else {
            System.err.println("Каталог Forms не найден: " + formsPath);
        }

        System.out.println("Всего найдено форм: " + allForms.size());
        return allForms;
    }

    /**
     * Загрузка зависимостей вьюх (какие таблицы используются внутри каждой вьюхи)
     */
    private Map<String, ViewTableDependencies> loadViewDependencies(Set<String> viewNames) {
        if (viewNames.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, ViewTableDependencies> result = new LinkedHashMap<>();
        ViewDependencyAnalyzer analyzer = new ViewDependencyAnalyzer(settings);

        log("  Загрузка зависимостей для " + viewNames.size() + " вьюх...");

        int count = 0;
        for (String viewName : viewNames) {
            if (stopCondition.getAsBoolean()) {
                break;
            }
            count++;
            log("    [" + count + "/" + viewNames.size() + "] Анализ вьюхи: " + viewName + " ... ");

            try {
                ViewTableDependencies deps = analyzer.analyzeView(viewName);
                result.put(viewName, deps);
                log("      OK (таблиц: " + deps.getOracleTables().size() + ")");
            } catch (Exception e) {
                error("      ОШИБКА: " + e.getMessage());
                ViewTableDependencies errorDeps = new ViewTableDependencies(viewName);
                errorDeps.setExistsInOracle(false);
                errorDeps.setOracleError(e.getMessage());
                result.put(viewName, errorDeps);
            }
        }

        log("  Загружено зависимостей: " + result.size() + " вьюх");
        return result;
    }
}