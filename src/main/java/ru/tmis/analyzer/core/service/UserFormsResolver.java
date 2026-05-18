// core/service/UserFormsResolver.java
package ru.tmis.analyzer.core.service;

import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.extractor.DfmOverrideProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class UserFormsResolver {

    private final FileScannerService scannerService;
    private final DfmOverrideProcessor dfmProcessor;

    public UserFormsResolver(FileScannerService scannerService) {
        this.scannerService = scannerService;
        this.dfmProcessor = new DfmOverrideProcessor();
    }

    public FormInfo resolveOverrides(String formPath) {
        FormInfo formInfo = new FormInfo(formPath);

        // Приводим путь к относительному от каталога Forms
        String relativePath = formPath;
        if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
        if (relativePath.startsWith("Forms/")) relativePath = relativePath.substring(6);
        // Если остался префикс Forms без слеша
        if (relativePath.startsWith("Forms")) relativePath = relativePath.substring(5);
        if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);

        Path projectRoot = scannerService.getProjectRoot();
        List<String> regions = scannerService.findAllUserFormsRegions();
        regions.sort(String::compareTo);

        for (String region : regions) {
            Path regionPath = projectRoot.resolve(region);

            // 1. Полное переопределение (.frm файл)
            Path regionFullOverride = regionPath.resolve(relativePath);
            if (Files.exists(regionFullOverride) && relativePath.endsWith(".frm")) {
                formInfo.setFullyReplaced(true);
                formInfo.setReplacementPath(regionFullOverride.toString());
                formInfo.addOverride(new FormInfo.OverrideInfo(
                        region, regionFullOverride.toString(),
                        FormInfo.OverrideInfo.OverrideType.FULL_OVERRIDE
                ));
                System.out.println("Найдены регионы UserForms: " + regions+' '+relativePath);
            }

            // 2. .d каталог
            String formPathWithoutExt = relativePath.replace(".frm", "");
            Path dotDPath = regionPath.resolve(formPathWithoutExt + ".d");
            if (Files.exists(dotDPath) && Files.isDirectory(dotDPath)) {
                try (Stream<Path> walk = Files.walk(dotDPath)) {
                    walk.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".dfrm"))
                            .forEach(dfrmFile -> {
                                formInfo.addOverride(new FormInfo.OverrideInfo(
                                        region, dfrmFile.toString(),
                                        FormInfo.OverrideInfo.OverrideType.DOT_D_OVERRIDE,
                                        null, null
                                ));
                            });
                } catch (IOException e) {
                    System.err.println("Error scanning .d directory: " + e.getMessage());
                }
            }

            // 3. Прямой .dfrm файл
            Path directDfrm = regionPath.resolve(relativePath.replace(".frm", ".dfrm"));
            if (Files.exists(directDfrm) && Files.isRegularFile(directDfrm)) {
                formInfo.addOverride(new FormInfo.OverrideInfo(
                        region, directDfrm.toString(),
                        FormInfo.OverrideInfo.OverrideType.PARTIAL_OVERRIDE,
                        null, null
                ));
            }
        }

        return formInfo;
    }

    public String getFinalFormContent(FormInfo formInfo) {
        if (formInfo.isFullyReplaced() && formInfo.getReplacementPath() != null) {
            Path replacementPath = Path.of(formInfo.getReplacementPath());
            return scannerService.readFileContent(replacementPath);
        }

        Path baseFormPath = scannerService.getBaseFormPath(formInfo.getFormPath());
        String baseContent = scannerService.readFileContent(baseFormPath);

        if (baseContent == null) {
            return null;
        }

        String finalContent = baseContent;
        for (FormInfo.OverrideInfo override : formInfo.getOverrides()) {
            if (override.getType() == FormInfo.OverrideInfo.OverrideType.DOT_D_OVERRIDE) {
                Path dfrmPath = Path.of(override.getOverridePath());
                String dfrmContent = scannerService.readFileContent(dfrmPath);
                if (dfrmContent != null && !dfrmContent.isEmpty()) {
                    String applied = dfmProcessor.applyOverrides(finalContent, dfrmContent);
                    if (applied != null) {
                        finalContent = applied;
                    }
                }
            }
        }

        return finalContent;
    }
}