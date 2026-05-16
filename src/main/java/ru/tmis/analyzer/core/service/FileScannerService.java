// core/service/FileScannerService.java (исправленный)
package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.utils.FormPathUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class FileScannerService {

    private final String projectRoot;
    private final Path rootPath;
    private Set<String> allBaseForms;

    public FileScannerService(String projectRoot) {
        this.projectRoot = projectRoot;
        this.rootPath = Paths.get(projectRoot);
    }

    public Set<String> findAllBaseForms() {
        if (allBaseForms != null) {
            return allBaseForms;
        }

        allBaseForms = new LinkedHashSet<>();
        Path formsPath = rootPath.resolve("Forms");

        if (!Files.exists(formsPath)) {
            System.err.println("Каталог Forms не найден: " + formsPath);
            return allBaseForms;
        }

        try (Stream<Path> walk = Files.walk(formsPath)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".frm") || p.toString().endsWith(".dfrm"))
                    .forEach(p -> {
                        // Получаем относительный путь от каталога Forms
                        String relativePath = formsPath.relativize(p).toString().replace("\\", "/");
                        // Сохраняем в формате /Forms/путь/к/файлу
                        allBaseForms.add("/Forms/" + relativePath);
                        System.out.println("  Найдена форма: /Forms/" + relativePath);
                    });
        } catch (IOException e) {
            System.err.println("Ошибка сканирования Forms: " + e.getMessage());
        }

        System.out.println("Всего найдено базовых форм: " + allBaseForms.size());
        return allBaseForms;
    }

    public List<String> findAllUserFormsRegions() {
        List<String> regions = new ArrayList<>();

        try (Stream<Path> list = Files.list(rootPath)) {
            list.filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("UserForms"))
                    .forEach(p -> regions.add(p.getFileName().toString()));
        } catch (IOException e) {
            System.err.println("Ошибка сканирования UserForms: " + e.getMessage());
        }

        return regions;
    }

    public boolean baseFormExists(String formPath) {
        // Нормализуем путь для поиска в файловой системе
        String fsPath = FormPathUtils.normalizeFormPathForFs(formPath);

        // Пробуем разные варианты
        String[] possiblePaths = {
                fsPath,
                "Forms/" + formPath.replace("/Forms/", "").replace("\\", "/"),
                formPath.replace("/Forms/", "").replace("\\", "/")
        };

        for (String path : possiblePaths) {
            Path baseFormFile = rootPath.resolve(path);
            if (Files.exists(baseFormFile)) {
                System.out.println("  Форма найдена: " + baseFormFile);
                return true;
            }
        }

        System.err.println("  Форма не найдена: " + formPath);
        System.err.println("  Пробовали пути: " + Arrays.toString(possiblePaths));
        return false;
    }

    public Path getBaseFormPath(String formPath) {
        String fsPath = FormPathUtils.normalizeFormPathForFs(formPath);

        String[] possiblePaths = {
                fsPath,
                "Forms/" + formPath.replace("/Forms/", "").replace("\\", "/"),
                formPath.replace("/Forms/", "").replace("\\", "/")
        };

        for (String path : possiblePaths) {
            Path baseFormFile = rootPath.resolve(path);
            if (Files.exists(baseFormFile)) {
                return baseFormFile;
            }
        }

        // Возвращаем путь по умолчанию
        return rootPath.resolve("Forms").resolve(formPath.replace("/Forms/", "").replace("\\", "/"));
    }

    public String readFileContent(Path filePath) {
        try {
            if (!Files.exists(filePath)) {
                System.err.println("Файл не существует: " + filePath);
                return null;
            }
            byte[] bytes = Files.readAllBytes(filePath);
            return new String(bytes);
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + filePath + " - " + e.getMessage());
            return null;
        }
    }

    public Path getProjectRoot() {
        return rootPath;
    }
}