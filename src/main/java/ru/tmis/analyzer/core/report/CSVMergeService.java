// core/report/CSVMergeService.java
package ru.tmis.analyzer.core.report;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Сервис для склеивания отдельных CSV отчетов в общий файл
 */
public class CSVMergeService {

    private final String outputDir;

    public CSVMergeService(String outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Сканирует папку CSV_reports и склеивает все CSV файлы в общий forms_export.csv
     * @return количество склеенных файлов
     */
    public int mergeAllCsvReports() throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            return 0;
        }

        Path csvReportsDir = outputPath.resolve("CSV_reports");
        if (!Files.exists(csvReportsDir)) {
            return 0;
        }

        // Собираем все CSV файлы
        List<Path> csvFiles = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(csvReportsDir)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .forEach(csvFiles::add);
        }

        if (csvFiles.isEmpty()) {
            System.out.println("[CSV] Нет отдельных CSV файлов для склеивания");
            return 0;
        }

        System.out.println("[CSV] Найдено CSV файлов для склеивания: " + csvFiles.size());

        // Сортируем по имени файла для предсказуемого порядка
        csvFiles.sort(Comparator.comparing(Path::toString));

        Path commonCsvPath = outputPath.resolve("forms_export.csv");

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(commonCsvPath))) {
            // Заголовок
            writer.println("ФОРМА;БЛОК;ЗНАЧЕНИЕ");

            int totalRows = 0;
            Set<String> processedForms = new LinkedHashSet<>();

            for (Path csvFile : csvFiles) {
                String formName = extractFormNameFromFile(csvFile);
                if (formName == null) {
                    System.out.println("[CSV] Не удалось извлечь имя формы из файла: " + csvFile.getFileName());
                    continue;
                }

                if (processedForms.contains(formName)) {
                    System.out.println("[CSV] Пропущен дубликат: " + formName);
                    continue;
                }

                processedForms.add(formName);

                int rowsFromFile = appendCsvContent(writer, csvFile, formName);
                totalRows += rowsFromFile;
                System.out.println("[CSV] Добавлена форма: " + formName + " (" + rowsFromFile + " строк)");
            }

            System.out.println("[CSV] Склеивание завершено. Всего форм: " + processedForms.size() + ", строк: " + totalRows);
        }

        return csvFiles.size();
    }

    /**
     * Извлекает имя формы из имени CSV файла
     * Формат: Forms#Path#To#Form.frm.csv -> Forms/Path/To/Form.frm
     */
    private String extractFormNameFromFile(Path csvFile) {
        String fileName = csvFile.getFileName().toString();
        if (!fileName.endsWith(".csv")) {
            return null;
        }

        // Убираем расширение .csv
        String withoutExt = fileName.substring(0, fileName.length() - 4);

        // Заменяем # на /
        String formPath = withoutExt.replace("#", "/");

        // Восстанавливаем путь (могли потерять Forms/ в начале)
        if (!formPath.startsWith("Forms/") && !formPath.startsWith("UserForms")) {
            // Проверяем, не содержит ли путь Forms/
            if (formPath.contains("Forms/")) {
                int idx = formPath.indexOf("Forms/");
                formPath = formPath.substring(idx);
            } else {
                formPath = "Forms/" + formPath;
            }
        }

        return formPath;
    }

    /**
     * Добавляет содержимое CSV файла в общий writer, пропуская заголовок
     * @return количество добавленных строк
     */
    private int appendCsvContent(PrintWriter writer, Path csvFile, String formName) throws IOException {
        int rowCount = 0;
        boolean firstLine = true;

        try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Пропускаем заголовок
                if (firstLine) {
                    firstLine = false;
                    if (line.startsWith("ФОРМА;БЛОК;ЗНАЧЕНИЕ")) {
                        continue;
                    }
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                // Проверяем, что строка соответствует формату (содержит точку с запятой)
                if (line.contains(";")) {
                    writer.println(line);
                    rowCount++;
                }
            }
        }

        return rowCount;
    }

    /**
     * Проверяет, существует ли уже общий CSV отчет
     */
    public boolean commonCsvExists() {
        Path commonCsvPath = Paths.get(outputDir, "forms_export.csv");
        return Files.exists(commonCsvPath);
    }

    /**
     * Получает список уже обработанных форм из общего CSV отчета
     * @return множество путей форм, которые уже есть в общем отчете
     */
    public Set<String> getProcessedFormsFromCommonCsv() throws IOException {
        Set<String> processedForms = new LinkedHashSet<>();

        Path commonCsvPath = Paths.get(outputDir, "forms_export.csv");
        if (!Files.exists(commonCsvPath)) {
            return processedForms;
        }

        try (BufferedReader reader = Files.newBufferedReader(commonCsvPath)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // пропускаем заголовок
                }
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(";", 3);
                if (parts.length >= 1) {
                    String formName = parts[0];
                    // Убираем кавычки если есть
                    if (formName.startsWith("\"") && formName.endsWith("\"")) {
                        formName = formName.substring(1, formName.length() - 1);
                    }
                    processedForms.add(formName);
                }
            }
        }

        return processedForms;
    }

    /**
     * Получает список уже обработанных форм из отдельных CSV файлов
     * @return множество путей форм, для которых есть отдельные CSV файлы
     */
    public Set<String> getProcessedFormsFromSingleCsv() throws IOException {
        Set<String> processedForms = new LinkedHashSet<>();

        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            return processedForms;
        }

        Path csvReportsDir = outputPath.resolve("CSV_reports");
        if (!Files.exists(csvReportsDir)) {
            return processedForms;
        }

        try (Stream<Path> walk = Files.walk(csvReportsDir)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .forEach(p -> {
                        String formName = extractFormNameFromFile(p);
                        if (formName != null) {
                            processedForms.add(formName);
                        }
                    });
        }

        return processedForms;
    }
}