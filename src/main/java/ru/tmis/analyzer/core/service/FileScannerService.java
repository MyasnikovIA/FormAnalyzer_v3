// core/service/FileScannerService.java
package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.utils.FormPathUtils;

import java.io.File;
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

        // Сканируем каталог Forms
        Path formsPath = rootPath.resolve("Forms");
        if (Files.exists(formsPath)) {
            try (Stream<Path> walk = Files.walk(formsPath)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".frm") || p.toString().endsWith(".dfrm"))
                        .forEach(p -> {
                            String relativePath = formsPath.relativize(p).toString().replace("\\", "/");
                            allBaseForms.add("Forms/" + relativePath);
                            System.out.println("  Найдена форма: Forms/" + relativePath);
                        });
            } catch (IOException e) {
                System.err.println("Ошибка сканирования Forms: " + e.getMessage());
            }
        }

        // Сканируем все каталоги UserForms***
        try (Stream<Path> list = Files.list(rootPath)) {
            list.filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("UserForms"))
                    .forEach(userFormsDir -> {
                        String dirName = userFormsDir.getFileName().toString();
                        System.out.println("Сканирование UserForms: " + dirName);
                        try (Stream<Path> walk = Files.walk(userFormsDir)) {
                            walk.filter(Files::isRegularFile)
                                    .filter(p -> p.toString().endsWith(".frm") || p.toString().endsWith(".dfrm"))
                                    .forEach(p -> {
                                        String relativePath = userFormsDir.relativize(p).toString().replace("\\", "/");
                                        String formPath = dirName + "/" + relativePath;
                                        allBaseForms.add(formPath);
                                        System.out.println("  Найдена форма в UserForms: " + formPath);
                                    });
                        } catch (IOException e) {
                            System.err.println("Ошибка сканирования " + dirName + ": " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Ошибка сканирования UserForms: " + e.getMessage());
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

        // Проверяем, является ли это UserForms
        boolean isUserForm = formPath.matches("^UserForms[A-Za-z0-9_]*/.*");

        Path fullPath;
        if (isUserForm) {
            // Для UserForms путь уже полный от корня проекта
            fullPath = rootPath.resolve(formPath.replace("/", File.separator));
        } else {
            // Для обычных форм
            fullPath = rootPath.resolve(fsPath);
        }

        if (Files.exists(fullPath)) {
            System.out.println("  Форма найдена: " + fullPath);
            return true;
        }

        // Пробуем альтернативные варианты
        String[] possiblePaths = {
                fsPath,
                "Forms/" + formPath.replace("/Forms/", "").replace("\\", "/"),
                formPath.replace("/Forms/", "").replace("\\", "/")
        };

        for (String path : possiblePaths) {
            Path testPath = rootPath.resolve(path);
            if (Files.exists(testPath)) {
                System.out.println("  Форма найдена: " + testPath);
                return true;
            }
        }

        System.err.println("  Форма не найдена: " + formPath);
        return false;
    }

    public Path getBaseFormPath(String formPath) {
        String fsPath = FormPathUtils.normalizeFormPathForFs(formPath);

        boolean isUserForm = formPath.matches("^UserForms[A-Za-z0-9_]*/.*");

        if (isUserForm) {
            Path fullPath = rootPath.resolve(formPath.replace("/", File.separator));
            if (Files.exists(fullPath)) {
                return fullPath;
            }
        }

        String[] possiblePaths = {
                fsPath,
                "Forms/" + formPath.replace("/Forms/", "").replace("\\", "/"),
                formPath.replace("/Forms/", "").replace("\\", "/")
        };

        for (String path : possiblePaths) {
            Path testPath = rootPath.resolve(path);
            if (Files.exists(testPath)) {
                return testPath;
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