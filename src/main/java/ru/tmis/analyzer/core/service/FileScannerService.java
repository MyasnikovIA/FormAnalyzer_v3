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
    private Set<String> cachedAllForms = null;
    private long lastScanTime = 0;
    private static final long CACHE_DURATION = 60000; // 1 минута

    public FileScannerService(String projectRoot) {
        this.projectRoot = projectRoot;
        this.rootPath = Paths.get(projectRoot);
    }

    /**
     * ЕДИНЫЙ МЕТОД для поиска всех форм в проекте
     */
    public Set<String> findAllForms() {
        long now = System.currentTimeMillis();
        if (cachedAllForms != null && (now - lastScanTime) < CACHE_DURATION) {
            return cachedAllForms;
        }

        Set<String> allForms = new LinkedHashSet<>();

        // 1. Сканируем Forms
        Path formsPath = rootPath.resolve("Forms");
        if (Files.exists(formsPath)) {
            try (Stream<Path> walk = Files.walk(formsPath)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> isFormFile(p))
                        .forEach(p -> {
                            String relativePath = formsPath.relativize(p).toString().replace("\\", "/");
                            allForms.add("Forms/" + relativePath);
                        });
            } catch (IOException e) {
                System.err.println("Ошибка сканирования Forms: " + e.getMessage());
            }
        }

        // 2. Сканируем UserForms
        try (Stream<Path> list = Files.list(rootPath)) {
            list.filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("UserForms"))
                    .forEach(userFormsDir -> {
                        String dirName = userFormsDir.getFileName().toString();
                        try (Stream<Path> walk = Files.walk(userFormsDir)) {
                            walk.filter(Files::isRegularFile)
                                    .filter(p -> isFormFile(p))
                                    .forEach(p -> {
                                        String relativePath = userFormsDir.relativize(p).toString().replace("\\", "/");
                                        allForms.add(dirName + "/" + relativePath);
                                    });
                        } catch (IOException e) {
                            System.err.println("Ошибка сканирования " + dirName + ": " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Ошибка сканирования UserForms: " + e.getMessage());
        }

        cachedAllForms = allForms;
        lastScanTime = now;

        System.out.println("[FileScanner] Найдено форм: " + allForms.size());
        return allForms;
    }

    /**
     * Возвращает список всех регионов UserForms в проекте
     */
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

    /**
     * Проверка, является ли файл формой
     */
    private boolean isFormFile(Path path) {
        String name = path.toString().toLowerCase();
        return name.endsWith(".frm") || name.endsWith(".dfrm");
    }

    /**
     * Очистить кэш
     */
    public void clearCache() {
        cachedAllForms = null;
        lastScanTime = 0;
    }

    /**
     * @deprecated Используйте findAllForms()
     */
    @Deprecated
    public Set<String> findAllBaseForms() {
        return findAllForms();
    }

    public boolean baseFormExists(String formPath) {
        String fsPath = FormPathUtils.normalizeFormPathForFs(formPath);
        boolean isUserForm = formPath.matches("^UserForms[A-Za-z0-9_]*/.*");
        Path fullPath;
        if (isUserForm) {
            fullPath = rootPath.resolve(formPath.replace("/", File.separator));
        } else {
            fullPath = rootPath.resolve(fsPath);
        }
        return Files.exists(fullPath);
    }

    public Path getBaseFormPath(String formPath) {
        String fsPath = FormPathUtils.normalizeFormPathForFs(formPath);
        boolean isUserForm = formPath.matches("^UserForms[A-Za-z0-9_]*/.*");
        if (isUserForm) {
            return rootPath.resolve(formPath.replace("/", File.separator));
        }
        return rootPath.resolve(fsPath);
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