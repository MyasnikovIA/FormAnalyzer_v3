// core/report/FormCSVWriter.java
package ru.tmis.analyzer.core.report;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.model.FormInfo;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Управляет созданием отдельных CSV-файлов для каждой формы
 * и их объединением в общий CSV-отчёт
 */
public class FormCSVWriter {

    private final String outputDir;
    private final String csvSubDir = "CSV";
    private final CSVReportGenerator csvGenerator;
    private final SettingsModel settings;

    public FormCSVWriter(String outputDir) {
        this.outputDir = outputDir;
        this.settings = SettingsModel.getInstance();
        this.csvGenerator = new CSVReportGenerator(outputDir);
        initSubdirectory();
    }

    /**
     * Инициализация директории для CSV-файлов форм
     */
    private void initSubdirectory() {
        try {
            Path csvDir = Paths.get(outputDir, csvSubDir);
            if (!Files.exists(csvDir)) {
                Files.createDirectories(csvDir);
                System.out.println("[FormCSVWriter] Создана директория: " + csvDir);
            }
        } catch (IOException e) {
            System.err.println("[FormCSVWriter] Ошибка создания директории: " + e.getMessage());
        }
    }

    /**
     * Генерирует содержимое CSV для формы (используя единый генератор)
     * @param formInfo информация о форме
     * @return содержимое CSV файла с заголовком
     */
    public String generateCsvContent(FormInfo formInfo) {
        if (formInfo == null) return "";
        return csvGenerator.generateFormCsvContent(formInfo);
    }

    /**
     * Сохранить CSV формы на диск (синхронно)
     * @param formInfo информация о форме
     */
    public void saveFormCSV(FormInfo formInfo) {
        if (formInfo == null) return;

        String csvContent = generateCsvContent(formInfo);
        String safeFileName = getSafeFileName(formInfo.getFormPath());
        Path csvFilePath = Paths.get(outputDir, csvSubDir, safeFileName + ".csv");

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(csvFilePath.toFile(), false)))) {
            writer.print(csvContent);
            writer.flush();
            System.out.println("[FormCSVWriter] Сохранён CSV для: " + formInfo.getFormPath());
        } catch (IOException e) {
            System.err.println("[FormCSVWriter] Ошибка сохранения CSV для " + formInfo.getFormPath() + ": " + e.getMessage());
        }
    }

    /**
     * Получить безопасное имя файла из пути формы
     * @param formPath путь к форме
     * @return безопасное имя файла (без расширения .csv)
     */
    private String getSafeFileName(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("(sub)_")) {
            normalized = normalized.substring(6);
        }
        if (normalized.startsWith("Forms/")) {
            normalized = normalized.substring(6);
        }
        if (normalized.endsWith(".frm")) {
            normalized = normalized.substring(0, normalized.length() - 4);
        }
        if (normalized.endsWith(".dfrm")) {
            normalized = normalized.substring(0, normalized.length() - 5);
        }
        return normalized.replace("/", "#").replace("\\", "#");
    }

    /**
     * Извлечь имя формы из имени CSV-файла
     * @param fileName имя файла (например, "Forms#SomeForm#subform.csv")
     * @return путь формы (например, "Forms/SomeForm/subform.frm")
     */
    private String getFormNameFromFileName(String fileName) {
        String name = fileName.replace(".csv", "");
        String formPath = name.replace("#", "/");
        if (!formPath.endsWith(".frm") && !formPath.endsWith(".dfrm")) {
            formPath = formPath + ".frm";
        }
        if (!formPath.startsWith("Forms/") && !formPath.startsWith("UserForms")) {
            formPath = "Forms/" + formPath;
        }
        return formPath;
    }

    /**
     * Получить список всех форм, для которых есть CSV-файлы
     * @return множество путей к формам
     */
    public Set<String> getProcessedForms() {
        Set<String> forms = new HashSet<>();
        Path csvDir = Paths.get(outputDir, csvSubDir);
        if (!Files.exists(csvDir)) return forms;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(csvDir, "*.csv")) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                forms.add(getFormNameFromFileName(fileName));
            }
        } catch (IOException e) {
            System.err.println("[FormCSVWriter] Ошибка чтения директории: " + e.getMessage());
        }
        return forms;
    }

    /**
     * Проверить, есть ли уже CSV-файл для формы
     * @param formPath путь к форме
     * @return true если файл существует
     */
    public boolean hasFormCSV(String formPath) {
        String safeFileName = getSafeFileName(formPath);
        Path csvFilePath = Paths.get(outputDir, csvSubDir, safeFileName + ".csv");
        return Files.exists(csvFilePath);
    }

    /**
     * Загрузить имена уже обработанных форм из основного CSV
     * @param csvPath путь к основному CSV файлу
     * @return множество имён форм
     */
    private Set<String> loadExistingFormNames(Path csvPath) throws IOException {
        Set<String> forms = new HashSet<>();
        List<String> lines = Files.readAllLines(csvPath);
        // Пропускаем заголовок
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line != null && !line.trim().isEmpty()) {
                int firstSemi = line.indexOf(';');
                if (firstSemi > 0) {
                    String formName = line.substring(0, firstSemi);
                    // Убираем кавычки
                    if (formName.startsWith("\"") && formName.endsWith("\"")) {
                        formName = formName.substring(1, formName.length() - 1);
                    }
                    forms.add(formName);
                }
            }
        }
        return forms;
    }

    /**
     * Объединить все CSV-файлы форм в один общий отчёт (перезапись)
     * @return количество объединённых строк
     */
    public int mergeAllToMainCSV() {
        return mergeAllToMainCSV(false);
    }

    /**
     * Объединить все CSV-файлы форм в один общий отчёт
     * @param preserveExisting сохранять существующий файл (true - дозапись только новых, false - полная перезапись)
     * @return количество объединённых строк
     */
    public int mergeAllToMainCSV(boolean preserveExisting) {
        Path csvDir = Paths.get(outputDir, csvSubDir);
        if (!Files.exists(csvDir)) {
            System.out.println("[FormCSVWriter] Директория CSV не найдена: " + csvDir);
            return 0;
        }

        try {
            Path mainCsvPath = Paths.get(outputDir, "forms_export.csv");
            Set<String> existingForms = new HashSet<>();

            if (preserveExisting && Files.exists(mainCsvPath)) {
                existingForms = loadExistingFormNames(mainCsvPath);
                System.out.println("[FormCSVWriter] Загружено существующих форм в CSV: " + existingForms.size());
            }

            // Собираем все CSV-файлы форм
            List<Path> formCsvFiles = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(csvDir, "*.csv")) {
                for (Path entry : stream) {
                    formCsvFiles.add(entry);
                }
            }

            System.out.println("[FormCSVWriter] Найдено CSV-файлов форм: " + formCsvFiles.size());

            // Сортируем по имени для предсказуемого порядка
            formCsvFiles.sort(Comparator.comparing(p -> p.getFileName().toString()));

            int totalLines = 0;
            int formsAdded = 0;

            // Перезаписываем или создаём новый общий CSV
            try (PrintWriter mainWriter = new PrintWriter(new BufferedWriter(new FileWriter(mainCsvPath.toFile(), false)))) {
                mainWriter.println("ФОРМА;БЛОК;ЗНАЧЕНИЕ");

                for (Path csvFile : formCsvFiles) {
                    String formNameFromFile = getFormNameFromFileName(csvFile.getFileName().toString());

                    // Если форма уже есть в существующем CSV и мы сохраняем данные - пропускаем
                    if (preserveExisting && existingForms.contains(formNameFromFile)) {
                        System.out.println("  Пропуск (уже есть): " + formNameFromFile);
                        continue;
                    }

                    List<String> lines = Files.readAllLines(csvFile);
                    // Пропускаем заголовок (первая строка)
                    for (int i = 1; i < lines.size(); i++) {
                        String line = lines.get(i);
                        if (line != null && !line.trim().isEmpty()) {
                            mainWriter.println(line);
                            totalLines++;
                        }
                    }
                    formsAdded++;
                }
                mainWriter.flush();
            }

            System.out.println("[FormCSVWriter] Объединено форм: " + formsAdded + ", строк: " + totalLines);
            System.out.println("[FormCSVWriter] Итоговый CSV сохранён: " + mainCsvPath);
            return totalLines;

        } catch (IOException e) {
            System.err.println("[FormCSVWriter] Ошибка объединения CSV: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Получить содержимое CSV файла для формы (если существует)
     * @param formPath путь к форме
     * @return содержимое CSV файла или null
     */
    public String getFormCSVContent(String formPath) {
        String safeFileName = getSafeFileName(formPath);
        Path csvFilePath = Paths.get(outputDir, csvSubDir, safeFileName + ".csv");

        if (!Files.exists(csvFilePath)) {
            return null;
        }

        try {
            return Files.readString(csvFilePath, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[FormCSVWriter] Ошибка чтения CSV для " + formPath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Очистить все CSV-файлы форм
     */
    public void clearFormCSVs() {
        Path csvDir = Paths.get(outputDir, csvSubDir);
        if (!Files.exists(csvDir)) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(csvDir, "*.csv")) {
            int count = 0;
            for (Path entry : stream) {
                Files.delete(entry);
                count++;
            }
            System.out.println("[FormCSVWriter] Очищено CSV-файлов форм: " + count);
        } catch (IOException e) {
            System.err.println("[FormCSVWriter] Ошибка очистки: " + e.getMessage());
        }
    }

    /**
     * Получить размер директории с CSV-файлами
     * @return количество файлов
     */
    public int getFormCSVCount() {
        Path csvDir = Paths.get(outputDir, csvSubDir);
        if (!Files.exists(csvDir)) return 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(csvDir, "*.csv")) {
            int count = 0;
            for (@SuppressWarnings("unused") Path entry : stream) {
                count++;
            }
            return count;
        } catch (IOException e) {
            System.err.println("[FormCSVWriter] Ошибка подсчёта: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Получить статистику по CSV-файлам
     */
    public void printStats() {
        Path csvDir = Paths.get(outputDir, csvSubDir);
        if (!Files.exists(csvDir)) {
            System.out.println("[FormCSVWriter] Статистика: директория CSV не существует");
            return;
        }

        try {
            long totalSize = 0;
            int fileCount = 0;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(csvDir, "*.csv")) {
                for (Path entry : stream) {
                    fileCount++;
                    totalSize += Files.size(entry);
                }
            }
            System.out.println("[FormCSVWriter] Статистика: " + fileCount + " файлов, " +
                    String.format("%.2f", totalSize / 1024.0 / 1024.0) + " МБ");
        } catch (IOException e) {
            System.err.println("[FormCSVWriter] Ошибка статистики: " + e.getMessage());
        }
    }
}