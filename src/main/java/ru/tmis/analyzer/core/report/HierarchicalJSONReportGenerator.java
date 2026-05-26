// core/report/HierarchicalJSONReportGenerator.java
package ru.tmis.analyzer.core.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Генератор иерархического JSON отчёта с сохранением структуры FormInfo
 * Каждая форма сохраняется в отдельный файл в каталоге JSON_reports
 */
public class HierarchicalJSONReportGenerator {

    private final String outputDir;
    private final AppConfig config;
    private final SettingsModel settings;
    private final Gson gson;

    public HierarchicalJSONReportGenerator(String outputDir, AppConfig config) {
        this.outputDir = outputDir;
        this.config = config;
        this.settings = SettingsModel.getInstance();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    /**
     * Сохраняет FormInfo в иерархический JSON файл
     * @param formInfo информация о форме
     * @return путь к созданному файлу
     */
    public Path saveFormToJSON(FormInfo formInfo) throws IOException {
        // ИСПРАВЛЕНО: проверяем правильную настройку
        if (!config.isEnableHierarchicalJSONExport()) {
            System.out.println("[HierarchicalJSON] Иерархический JSON экспорт отключён в настройках");
            return null;
        }

        if (formInfo == null) {
            System.err.println("[HierarchicalJSON] formInfo is null");
            return null;
        }

        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        // Создаём подкаталог JSON_reports
        Path jsonSubDir = outputPath.resolve("JSON_reports");
        if (!Files.exists(jsonSubDir)) {
            Files.createDirectories(jsonSubDir);
            System.out.println("[HierarchicalJSON] Создана директория: " + jsonSubDir);
        }

        String fileName = getSafeFileName(formInfo.getFormPath());
        Path jsonPath = jsonSubDir.resolve(fileName);

        System.out.println("[HierarchicalJSON] Сохранение JSON для формы: " + formInfo.getFormPath());
        System.out.println("[HierarchicalJSON] Путь: " + jsonPath);

        JsonObject root = convertFormToHierarchicalJson(formInfo);

        String jsonString = gson.toJson(root);
        Files.writeString(jsonPath, jsonString, StandardCharsets.UTF_8);

        System.out.println("[HierarchicalJSON] ✅ JSON отчёт сохранён: " + jsonPath);
        System.out.println("[HierarchicalJSON] Размер: " + jsonString.length() + " байт");

        return jsonPath;
    }

    /**
     * Конвертирует FormInfo в иерархический JSON объект
     */
    private JsonObject convertFormToHierarchicalJson(FormInfo formInfo) {
        JsonObject formJson = new JsonObject();

        // ========== 1. ОСНОВНАЯ ИНФОРМАЦИЯ ==========
        formJson.addProperty("formPath", formInfo.getFormPath());
        formJson.addProperty("baseFormPath", getRelativePath(formInfo.getBaseFormPath()));
        formJson.addProperty("fullyReplaced", formInfo.isFullyReplaced());
        formJson.addProperty("formStyle", formInfo.getFormStyle().getName());
        formJson.addProperty("formStyleSyntax", formInfo.getFormStyle().getSyntax());

        if (formInfo.getReplacementPath() != null) {
            formJson.addProperty("replacementPath", getRelativePath(formInfo.getReplacementPath()));
        }

        // ========== 2. СТАТИСТИКА ==========
        JsonObject stats = new JsonObject();
        stats.addProperty("totalSqlQueries", formInfo.getSqlQueries().size());
        stats.addProperty("totalTablesViews", formInfo.getTablesViews().size());
        stats.addProperty("totalPackagesFunctions", formInfo.getPackagesFunctions().size());
        stats.addProperty("totalSubForms", formInfo.getSubForms().size());
        stats.addProperty("totalJsForms", formInfo.getJsForms().size());
        stats.addProperty("totalBrokers", formInfo.getBrokers().size());
        stats.addProperty("totalConstants", formInfo.getConstants().size());
        stats.addProperty("totalSystemOptions", formInfo.getSystemOptions().size());

        if (formInfo.getConversionStatistics() != null) {
            JsonObject convStats = new JsonObject();
            convStats.addProperty("totalQueries", formInfo.getConversionStatistics().getTotalQueries());
            convStats.addProperty("convertedQueries", formInfo.getConversionStatistics().getConvertedQueries());
            convStats.addProperty("conversionPercent", formInfo.getConversionStatistics().getConversionPercent());
            convStats.addProperty("fullyConverted", formInfo.getConversionStatistics().isFullyConverted());
            stats.add("conversionStatistics", convStats);
        }
        formJson.add("statistics", stats);

        // ========== 3. ЮЗЕРФОРМЫ (с иерархией по регионам) ==========
        JsonObject userForms = new JsonObject();

        if (formInfo.isFullyReplaced() && formInfo.getReplacementPath() != null) {
            userForms.addProperty("fullyReplaced", getRelativePath(formInfo.getReplacementPath()));
        }

        if (!formInfo.getOverrides().isEmpty()) {
            // Группируем по регионам
            Map<String, List<FormInfo.OverrideInfo>> overridesByRegion = new LinkedHashMap<>();
            for (FormInfo.OverrideInfo override : formInfo.getOverrides()) {
                overridesByRegion.computeIfAbsent(override.getRegionName(), k -> new ArrayList<>()).add(override);
            }

            JsonObject regions = new JsonObject();
            for (Map.Entry<String, List<FormInfo.OverrideInfo>> entry : overridesByRegion.entrySet()) {
                String region = entry.getKey();
                JsonObject regionObj = new JsonObject();

                JsonArray fullReplacements = new JsonArray();
                JsonArray partialOverrides = new JsonArray();
                JsonObject dotDCatalogs = new JsonObject();

                for (FormInfo.OverrideInfo override : entry.getValue()) {
                    String relativePath = getRelativePath(override.getOverridePath());

                    switch (override.getType()) {
                        case FULL_OVERRIDE:
                            fullReplacements.add(relativePath);
                            break;
                        case PARTIAL_OVERRIDE:
                            partialOverrides.add(relativePath);
                            break;
                        case DOT_D_OVERRIDE:
                            // Группируем по каталогу .d
                            if (relativePath.contains(".d/")) {
                                String catalogPath = relativePath.substring(0, relativePath.indexOf(".d/") + 2);
                                String fileName = relativePath.substring(relativePath.lastIndexOf("/") + 1);
                                JsonArray catalogFiles = dotDCatalogs.has(catalogPath)
                                        ? dotDCatalogs.getAsJsonArray(catalogPath)
                                        : new JsonArray();
                                catalogFiles.add(fileName);
                                dotDCatalogs.add(catalogPath, catalogFiles);
                            } else {
                                partialOverrides.add(relativePath);
                            }
                            break;
                    }
                }

                if (fullReplacements.size() > 0) regionObj.add("fullReplacements", fullReplacements);
                if (partialOverrides.size() > 0) regionObj.add("partialOverrides", partialOverrides);
                if (dotDCatalogs.size() > 0) regionObj.add("dotDCatalogs", dotDCatalogs);

                regions.add(region, regionObj);
            }
            userForms.add("regions", regions);
        }
        formJson.add("userForms", userForms);

        // ========== 4. SUBFORM (массив) ==========
        addStringArray(formJson, "subForms", formInfo.getSubForms());

        // ========== 5. JS ФОРМЫ (массив) ==========
        addStringArray(formJson, "jsForms", formInfo.getJsForms());

        // ========== 6. ОТЧЁТЫ (с деталями из БД) ==========
        JsonArray reportsArray = new JsonArray();
        for (String report : formInfo.getReports()) {
            JsonObject reportObj = parseReportString(report);
            reportsArray.add(reportObj);
        }
        formJson.add("reports", reportsArray);

        // ========== 7. ОТЧЁТЫ ИЗ AUTOPOPUP ==========
        if (formInfo.getReportsFromAutoPopup() != null && !formInfo.getReportsFromAutoPopup().isEmpty()) {
            JsonArray autoPopupReports = new JsonArray();
            for (FormInfo.ReportFromAutoPopupInfo report : formInfo.getReportsFromAutoPopup()) {
                JsonObject reportObj = new JsonObject();
                reportObj.addProperty("repCode", report.getRepCode());
                reportObj.addProperty("repType", report.getRepType());
                reportObj.addProperty("repTypeName", report.getRepTypeName());
                if (report.getRepFilename() != null && !report.getRepFilename().isEmpty()) {
                    reportObj.addProperty("repFilename", report.getRepFilename());
                }
                if (report.getFormPath() != null && !report.getFormPath().isEmpty()) {
                    reportObj.addProperty("formPath", report.getFormPath());
                }
                autoPopupReports.add(reportObj);
            }
            formJson.add("reportsFromAutoPopup", autoPopupReports);
        }

        // ========== 8. ВЬЮХИ И ТАБЛИЦЫ ==========
        Set<String> views = new LinkedHashSet<>();
        Set<String> tables = new LinkedHashSet<>();

        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_V_")) {
                views.add(tv);
            } else if (tv.startsWith("D_")) {
                tables.add(tv);
            }
        }

        addStringArray(formJson, "views", views);
        addStringArray(formJson, "tables", tables);

        // ========== 9. ТАБЛИЦЫ ИЗ ВЬЮХ ==========
        if (formInfo.getTablesFromViews() != null && !formInfo.getTablesFromViews().isEmpty()) {
            addStringArray(formJson, "tablesFromViews", formInfo.getTablesFromViews());
        }

        // ========== 10. ЗАВИСИМОСТИ ВЬЮХ (иерархически) ==========
        if (formInfo.getViewDependencies() != null && !formInfo.getViewDependencies().isEmpty()) {
            JsonObject viewDeps = new JsonObject();
            for (Map.Entry<String, ViewTableDependencies> entry : formInfo.getViewDependencies().entrySet()) {
                JsonObject depsJson = new JsonObject();
                depsJson.addProperty("existsInOracle", entry.getValue().isExistsInOracle());
                depsJson.addProperty("existsInPostgres", entry.getValue().isExistsInPostgres());
                addStringArray(depsJson, "oracleTables", entry.getValue().getOracleTables());
                addStringArray(depsJson, "postgresTables", entry.getValue().getPostgresTables());
                if (entry.getValue().getOracleError() != null) {
                    depsJson.addProperty("oracleError", entry.getValue().getOracleError());
                }
                if (entry.getValue().getPostgresError() != null) {
                    depsJson.addProperty("postgresError", entry.getValue().getPostgresError());
                }
                viewDeps.add(entry.getKey(), depsJson);
            }
            formJson.add("viewDependencies", viewDeps);
        }

        // ========== 11. ПАКЕТЫ И ФУНКЦИИ ==========
        addStringArray(formJson, "packagesFunctions", formInfo.getPackagesFunctions());

        // ========== 12. СИСТЕМНЫЕ ОПЦИИ ==========
        addStringArray(formJson, "systemOptions", formInfo.getSystemOptions());

        // ========== 13. КОНСТАНТЫ ==========
        addStringArray(formJson, "constants", formInfo.getConstants());

        // ========== 14. УНИВЕРСАЛЬНЫЕ КОМПОЗИЦИИ ==========
        JsonObject compositions = new JsonObject();
        addStringArray(compositions, "unitCompositions", formInfo.getUnitCompositions());
        addStringArray(compositions, "jsUnitCompositions", formInfo.getJsUnitCompositions());
        addStringArray(compositions, "openFormCompositions", formInfo.getOpenFormCompositions());
        addStringArray(compositions, "openD3FormCompositions", formInfo.getOpenD3FormCompositions());
        addStringArray(compositions, "d3ApiShowFormCompositions", formInfo.getD3ApiShowFormCompositions());
        formJson.add("compositions", compositions);

        // ========== 15. БРОКЕРЫ ==========
        if (formInfo.getBrokers() != null && !formInfo.getBrokers().isEmpty()) {
            JsonArray brokersArray = new JsonArray();
            for (BrokerInfo broker : formInfo.getBrokers()) {
                JsonObject brokerJson = new JsonObject();
                brokerJson.addProperty("displayString", broker.getDisplayString());
                brokerJson.addProperty("type", broker.getType().name());

                if (broker.getUnit() != null) brokerJson.addProperty("unit", broker.getUnit());
                if (broker.getAction() != null) brokerJson.addProperty("action", broker.getAction());
                if (broker.getFunctionName() != null) brokerJson.addProperty("functionName", broker.getFunctionName());
                if (broker.getExecProc() != null) brokerJson.addProperty("execProc", broker.getExecProc());
                if (broker.getComponentName() != null) brokerJson.addProperty("componentName", broker.getComponentName());
                if (broker.getComponentType() != null) brokerJson.addProperty("componentType", broker.getComponentType());

                // Переменные
                if (broker.getVariables() != null && !broker.getVariables().isEmpty()) {
                    JsonArray varsArray = new JsonArray();
                    for (RouterVariable var : broker.getVariables()) {
                        JsonObject varJson = new JsonObject();
                        varJson.addProperty("name", var.getName());
                        if (var.getSrc() != null) varJson.addProperty("src", var.getSrc());
                        if (var.getSrcType() != null) varJson.addProperty("srctype", var.getSrcType());
                        if (var.getGet() != null && !var.getGet().isEmpty()) varJson.addProperty("get", var.getGet());
                        if (var.getPut() != null && !var.getPut().isEmpty()) varJson.addProperty("put", var.getPut());
                        if (var.getType() != null) varJson.addProperty("type", var.getType());
                        varsArray.add(varJson);
                    }
                    brokerJson.add("variables", varsArray);
                }

                brokersArray.add(brokerJson);
            }
            formJson.add("brokers", brokersArray);
        }

        // ========== 16. ПОЛЬЗОВАТЕЛЬСКИЕ ПРОЦЕДУРЫ ==========
        addStringArray(formJson, "userProcedures", formInfo.getUserProcedures());

        // ========== 17. AUTOPOPUP МЕНЮ ==========
        addStringArray(formJson, "autoPopupMenus", formInfo.getAutoPopupMenus());

        // ========== 18. КОНТЕКСТНОЕ МЕНЮ (POPUP MENU) - ИЕРАРХИЧЕСКИ ==========
        if (formInfo.getPopupMenus() != null && !formInfo.getPopupMenus().isEmpty()) {
            JsonArray popupMenus = new JsonArray();
            for (PopupMenuInfo menu : formInfo.getPopupMenus()) {
                popupMenus.add(convertPopupMenuToJson(menu, "Oracle"));
            }
            formJson.add("popupMenus", popupMenus);
        }

        // ========== 19. КОНТЕКСТНОЕ МЕНЮ POSTGRESQL ==========
        if (formInfo.getPopupMenusPg() != null && !formInfo.getPopupMenusPg().isEmpty()) {
            JsonArray popupMenusPg = new JsonArray();
            for (PopupMenuInfo menu : formInfo.getPopupMenusPg()) {
                popupMenusPg.add(convertPopupMenuToJson(menu, "PostgreSQL"));
            }
            formJson.add("popupMenusPostgres", popupMenusPg);
        }

        // ========== 20. НЕОПРЕДЕЛЁННЫЕ ОБЪЕКТЫ ==========
        addStringArray(formJson, "unknownObjects", formInfo.getUnknownObjects());

        // ========== 21. SQL ЗАПРОСЫ (с сохранением структуры) ==========
        JsonArray sqlQueries = new JsonArray();
        for (SqlInfo sql : formInfo.getSqlQueries()) {
            JsonObject sqlJson = new JsonObject();
            sqlJson.addProperty("componentType", sql.getComponentType());
            sqlJson.addProperty("componentName", sql.getComponentName());
            sqlJson.addProperty("sourceType", sql.getSourceType());
            sqlJson.addProperty("sourcePath", sql.getSourcePath());
            if (sql.getBaseFormPath() != null) {
                sqlJson.addProperty("baseFormPath", getRelativePath(sql.getBaseFormPath()));
            }
            sqlJson.addProperty("sqlContent", sql.getSqlContent());

            addStringArray(sqlJson, "tablesViews", sql.getTablesViews());
            addStringArray(sqlJson, "packagesFunctions", sql.getPackagesFunctions());
            addStringArray(sqlJson, "constants", sql.getConstants());
            addStringArray(sqlJson, "systemOptions", sql.getSystemOptions());
            addStringArray(sqlJson, "userProcedures", sql.getUserProcedures());
            addStringArray(sqlJson, "unknownObjects", sql.getUnknownObjects());

            sqlQueries.add(sqlJson);
        }
        formJson.add("sqlQueries", sqlQueries);

        // ========== НОВЫЙ БЛОК: ACTION ROUTERS ==========
        if (formInfo.getActionRouters() != null && !formInfo.getActionRouters().isEmpty()) {
            JsonArray actionRoutersArray = new JsonArray();
            for (RouterInfo router : formInfo.getActionRouters()) {
                if (router.isConverted()) {  // <-- ФИЛЬТРАЦИЯ
                    actionRoutersArray.add(convertRouterToJson(router));
                }
            }
            if (actionRoutersArray.size() > 0) {
                formJson.add("actionRouters", actionRoutersArray);
            }
        }


        // ========== НОВЫЙ БЛОК: DATASET ROUTERS ==========
        if (formInfo.getDataSetRouters() != null && !formInfo.getDataSetRouters().isEmpty()) {
            JsonArray dataSetRoutersArray = new JsonArray();
            for (RouterInfo router : formInfo.getDataSetRouters()) {
                if (router.isConverted()) {  // <-- ФИЛЬТРАЦИЯ
                    dataSetRoutersArray.add(convertRouterToJson(router));
                }
            }
            if (dataSetRoutersArray.size() > 0) {
                formJson.add("dataSetRouters", dataSetRoutersArray);
            }
        }

        return formJson;
    }

    /**
     * Конвертирует PopupMenu в JSON с сохранением иерархии
     */
    private JsonObject convertPopupMenuToJson(PopupMenuInfo menu, String source) {
        JsonObject menuJson = new JsonObject();
        menuJson.addProperty("name", menu.getName());
        menuJson.addProperty("source", source);

        JsonArray itemsArray = new JsonArray();
        for (PopupMenuInfo.MenuItem item : menu.getRootItems()) {
            itemsArray.add(convertMenuItemToJson(item));
        }
        menuJson.add("items", itemsArray);

        return menuJson;
    }

    /**
     * Конвертирует пункт меню в JSON с сохранением иерархии
     */
    private JsonObject convertMenuItemToJson(PopupMenuInfo.MenuItem item) {
        JsonObject itemJson = new JsonObject();

        if (item.isDbReport() && item.getCaption() != null) {
            // Отчёт из БД - сохраняем как есть
            itemJson.addProperty("caption", item.getCaption());
            itemJson.addProperty("isDbReport", true);
        } else {
            if (item.getCaption() != null && !item.getCaption().isEmpty()) {
                itemJson.addProperty("caption", item.getCaption());
            }
            if (item.getName() != null && !item.getName().isEmpty()) {
                itemJson.addProperty("name", item.getName());
            }
            if (item.isFromAutoPopup() && item.getAutoPopupName() != null) {
                itemJson.addProperty("autoPopupName", item.getAutoPopupName());
            }
        }

        // Рекурсивно обрабатываем дочерние элементы
        if (item.hasChildren()) {
            JsonArray childrenArray = new JsonArray();
            for (PopupMenuInfo.MenuItem child : item.getChildren()) {
                childrenArray.add(convertMenuItemToJson(child));
            }
            itemJson.add("children", childrenArray);
        }

        return itemJson;
    }

    /**
     * Парсит строку отчёта в JSON объект
     */
    private JsonObject parseReportString(String report) {
        JsonObject result = new JsonObject();

        // Формат: "REP_CODE (REP_TYPE) Reports/path/form.frm"
        if (report == null) return result;

        // Извлекаем REP_CODE (до пробела или открывающей скобки)
        int spaceIdx = report.indexOf(' ');
        int parenIdx = report.indexOf('(');
        int endIdx = Math.min(spaceIdx > 0 ? spaceIdx : Integer.MAX_VALUE,
                parenIdx > 0 ? parenIdx : Integer.MAX_VALUE);

        if (endIdx != Integer.MAX_VALUE && endIdx > 0) {
            String repCode = report.substring(0, endIdx).trim();
            result.addProperty("repCode", repCode);

            // Извлекаем REP_TYPE
            int closeParen = report.indexOf(')');
            if (parenIdx > 0 && closeParen > parenIdx) {
                String repType = report.substring(parenIdx + 1, closeParen).trim();
                result.addProperty("repType", repType);
            }

            // Извлекаем путь к форме
            if (report.contains("Reports/")) {
                int formStart = report.indexOf("Reports/");
                String formPath = report.substring(formStart).trim();
                if (formPath.endsWith(";")) {
                    formPath = formPath.substring(0, formPath.length() - 1);
                }
                result.addProperty("formPath", formPath);
            }
        } else {
            result.addProperty("original", report);
        }

        return result;
    }

    /**
     * Добавляет массив строк в JSON объект
     */
    private void addStringArray(JsonObject json, String key, Set<String> values) {
        if (values != null && !values.isEmpty()) {
            JsonArray array = new JsonArray();
            for (String value : values) {
                array.add(value);
            }
            json.add(key, array);
        }
    }

    /**
     * Формирует безопасное имя файла
     */
    private String getSafeFileName(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("(sub)_")) {
            normalized = normalized.substring(6);
        }
        String safeName = normalized.replace("/", "#").replace("\\", "#");
        return safeName + ".json";
    }

    /**
     * Получить относительный путь от корня проекта
     */
    private String getRelativePath(String absolutePath) {
        if (absolutePath == null || absolutePath.isEmpty()) return "";

        String projectPath = settings.getProjectPath();
        if (projectPath == null || projectPath.isEmpty()) {
            return absolutePath;
        }

        String normalizedProject = projectPath.replace("\\", "/");
        String normalizedPath = absolutePath.replace("\\", "/");

        if (normalizedPath.startsWith(normalizedProject)) {
            String relative = normalizedPath.substring(normalizedProject.length());
            if (relative.startsWith("/")) {
                relative = relative.substring(1);
            }
            return relative;
        }

        return absolutePath;
    }
    /**
     * Конвертирует RouterInfo в JSON объект
     */
    private JsonObject convertRouterToJson(RouterInfo router) {
        JsonObject routerJson = new JsonObject();

        routerJson.addProperty("name", router.getName());
        routerJson.addProperty("parentType", router.getParentType().getDisplayName());
        routerJson.addProperty("routerType", router.getRouterType().getTagName());
        routerJson.addProperty("formStyle", router.getFormStyle().getName());

        // Роутеры (ActionRouter/DataSetRouter)
        if (!router.getRouters().isEmpty()) {
            JsonArray routersArray = new JsonArray();
            for (RouterItem item : router.getRouters()) {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("order", item.getOrder());
                if (item.getCondition() != null && !item.getCondition().isEmpty()) {
                    itemJson.addProperty("condition", item.getCondition());
                }
                if (item.getUnit() != null && !item.getUnit().isEmpty()) {
                    itemJson.addProperty("unit", item.getUnit());
                }
                if (item.getAction() != null && !item.getAction().isEmpty()) {
                    itemJson.addProperty("action", item.getAction());
                }
                if (item.getSqlContent() != null && !item.getSqlContent().isEmpty()) {
                    // Ограничиваем длину для читаемости JSON
                    String sqlPreview = item.getSqlContent().length() > 500 ?
                            item.getSqlContent().substring(0, 500) + "..." :
                            item.getSqlContent();
                    itemJson.addProperty("sqlContent", sqlPreview);
                    itemJson.addProperty("sqlLength", item.getSqlContent().length());
                }
                routersArray.add(itemJson);
            }
            routerJson.add("routers", routersArray);
        }

        // Переменные
        if (!router.getVariables().isEmpty()) {
            JsonArray variablesArray = new JsonArray();
            for (RouterVariable variable : router.getVariables()) {
                JsonObject varJson = new JsonObject();
                varJson.addProperty("name", variable.getName());
                if (variable.getSrc() != null) varJson.addProperty("src", variable.getSrc());
                if (variable.getSrcType() != null) varJson.addProperty("srctype", variable.getSrcType());
                if (variable.getGet() != null && !variable.getGet().isEmpty()) {
                    varJson.addProperty("get", variable.getGet());
                }
                if (variable.getPut() != null && !variable.getPut().isEmpty()) {
                    varJson.addProperty("put", variable.getPut());
                }
                if (variable.getType() != null) varJson.addProperty("type", variable.getType());
                if (variable.getLen() != null) varJson.addProperty("len", variable.getLen());
                variablesArray.add(varJson);
            }
            routerJson.add("variables", variablesArray);
        }

        // Вложенные SubAction/SubSelect
        if (!router.getSubRouters().isEmpty()) {
            JsonArray subRoutersArray = new JsonArray();
            for (SubRouterInfo subRouter : router.getSubRouters()) {
                subRoutersArray.add(convertSubRouterToJson(subRouter));
            }
            routerJson.add("subRouters", subRoutersArray);
        }

        return routerJson;
    }

    /**
     * Конвертирует SubRouterInfo в JSON объект
     */
    /**
     * Конвертирует SubRouterInfo в JSON объект
     */
    private JsonObject convertSubRouterToJson(SubRouterInfo subRouter) {
        JsonObject subJson = new JsonObject();

        subJson.addProperty("name", subRouter.getName());
        subJson.addProperty("type", subRouter.getType().getDisplayName());

        if (subRouter.getGroupName() != null && !subRouter.getGroupName().isEmpty()) {
            subJson.addProperty("groupName", subRouter.getGroupName());
        }
        if (subRouter.getExecon() != null && !subRouter.getExecon().isEmpty()) {
            subJson.addProperty("execon", subRouter.getExecon());
        }
        if (subRouter.getMode() != null && !subRouter.getMode().isEmpty()) {
            subJson.addProperty("mode", subRouter.getMode());
        }
        subJson.addProperty("savepoint", subRouter.isSavepoint());

        // Роутеры внутри SubAction/SubSelect
        if (!subRouter.getRouters().isEmpty()) {
            JsonArray routersArray = new JsonArray();
            for (RouterItem item : subRouter.getRouters()) {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("order", item.getOrder());
                if (item.getCondition() != null && !item.getCondition().isEmpty()) {
                    itemJson.addProperty("condition", item.getCondition());
                }
                if (item.getSqlContent() != null && !item.getSqlContent().isEmpty()) {
                    String sqlPreview = item.getSqlContent().length() > 500 ?
                            item.getSqlContent().substring(0, 500) + "..." :
                            item.getSqlContent();
                    itemJson.addProperty("sqlContent", sqlPreview);
                    itemJson.addProperty("sqlLength", item.getSqlContent().length());
                }
                routersArray.add(itemJson);
            }
            subJson.add("routers", routersArray);
        }

        // Переменные (SubActionVar)
        if (!subRouter.getVariables().isEmpty()) {
            JsonArray variablesArray = new JsonArray();
            for (RouterVariable variable : subRouter.getVariables()) {
                JsonObject varJson = new JsonObject();
                varJson.addProperty("name", variable.getName());
                if (variable.getSrc() != null) {
                    varJson.addProperty("src", variable.getSrc());
                }
                if (variable.getSrcType() != null) {
                    varJson.addProperty("srctype", variable.getSrcType());
                }
                if (variable.getPut() != null && !variable.getPut().isEmpty()) {
                    varJson.addProperty("put", variable.getPut());
                }
                variablesArray.add(varJson);
            }
            subJson.add("variables", variablesArray);
        }

        return subJson;
    }
}