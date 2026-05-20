// core/report/JSONReportGenerator.java
package ru.tmis.analyzer.core.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.PopupMenuInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class JSONReportGenerator {

    private final String outputDir;
    private final AppConfig config;
    private final Gson gson;

    public JSONReportGenerator(String outputDir, AppConfig config) {
        this.outputDir = outputDir;
        this.config = config;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Добавляет данные формы в JSON файл (дозапись в массив)
     */
    public void appendFormToJSON(FormInfo formInfo) throws IOException {
        if (!config.isEnableJSONExport()) return;

        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        Path jsonPath = outputPath.resolve("forms_export.json");

        JsonArray formsArray = new JsonArray();

        // Если файл существует, читаем существующий массив
        if (Files.exists(jsonPath)) {
            String content = Files.readString(jsonPath);
            if (!content.isEmpty()) {
                try {
                    JsonObject root = gson.fromJson(content, JsonObject.class);
                    if (root.has("forms")) {
                        formsArray = root.getAsJsonArray("forms");
                    }
                } catch (Exception e) {
                    // Если файл поврежден, создаем новый массив
                    formsArray = new JsonArray();
                }
            }
        }

        // Добавляем новую форму
        formsArray.add(convertFormToJson(formInfo));

        // Создаем корневой объект
        JsonObject root = new JsonObject();
        root.add("forms", formsArray);
        root.addProperty("totalForms", formsArray.size());
        root.addProperty("exportDate", new Date().toString());

        // Сохраняем файл
        Files.writeString(jsonPath, gson.toJson(root));
    }

    /**
     * Конвертирует FormInfo в JSON объект
     */
    private JsonObject convertFormToJson(FormInfo formInfo) {
        JsonObject formJson = new JsonObject();

        // Основная информация
        formJson.addProperty("formPath", formInfo.getFormPath());
        formJson.addProperty("baseFormPath", formInfo.getBaseFormPath());
        formJson.addProperty("fullyReplaced", formInfo.isFullyReplaced());
        if (formInfo.getReplacementPath() != null) {
            formJson.addProperty("replacementPath", formInfo.getReplacementPath());
        }

        // Overrides (UserForms)
        JsonArray overridesArray = new JsonArray();
        for (FormInfo.OverrideInfo override : formInfo.getOverrides()) {
            JsonObject overrideJson = new JsonObject();
            overrideJson.addProperty("regionName", override.getRegionName());
            overrideJson.addProperty("overridePath", override.getOverridePath());
            overrideJson.addProperty("type", override.getType().getDescription());
            overrideJson.addProperty("typeCode", override.getType().name());
            overridesArray.add(overrideJson);
        }
        formJson.add("overrides", overridesArray);

        // SubForm
        addSetToJson(formJson, "subForms", formInfo.getSubForms());

        // JS Forms
        addSetToJson(formJson, "jsForms", formInfo.getJsForms());

        // Tables and Views
        addSetToJson(formJson, "tablesViews", formInfo.getTablesViews());

        // Packages and Functions
        addSetToJson(formJson, "packagesFunctions", formInfo.getPackagesFunctions());

        // System Options
        addSetToJson(formJson, "systemOptions", formInfo.getSystemOptions());

        // Constants
        addSetToJson(formJson, "constants", formInfo.getConstants());

        // User Procedures
        addSetToJson(formJson, "userProcedures", formInfo.getUserProcedures());

        // AutoPopup Menus
        addSetToJson(formJson, "autoPopupMenus", formInfo.getAutoPopupMenus());

        // Brokers
        addSetToJson(formJson, "brokers", formInfo.getBrokers());

        // Unit Compositions
        addSetToJson(formJson, "unitCompositions", formInfo.getUnitCompositions());

        // JS Unit Compositions
        addSetToJson(formJson, "jsUnitCompositions", formInfo.getJsUnitCompositions());

        // Unknown Objects
        addSetToJson(formJson, "unknownObjects", formInfo.getUnknownObjects());

        // Reports
        addSetToJson(formJson, "reports", formInfo.getReports());

        // View Dependencies
        if (formInfo.getViewDependencies() != null && !formInfo.getViewDependencies().isEmpty()) {
            JsonObject viewDepsJson = new JsonObject();
            for (Map.Entry<String, ViewTableDependencies> entry : formInfo.getViewDependencies().entrySet()) {
                JsonObject depsJson = new JsonObject();
                depsJson.addProperty("existsInOracle", entry.getValue().isExistsInOracle());
                depsJson.addProperty("existsInPostgres", entry.getValue().isExistsInPostgres());
                addSetToJson(depsJson, "oracleTables", entry.getValue().getOracleTables());
                addSetToJson(depsJson, "postgresTables", entry.getValue().getPostgresTables());
                if (entry.getValue().getOracleError() != null) {
                    depsJson.addProperty("oracleError", entry.getValue().getOracleError());
                }
                if (entry.getValue().getPostgresError() != null) {
                    depsJson.addProperty("postgresError", entry.getValue().getPostgresError());
                }
                viewDepsJson.add(entry.getKey(), depsJson);
            }
            formJson.add("viewDependencies", viewDepsJson);
        }

        // Popup Menus
        if (formInfo.getPopupMenus() != null && !formInfo.getPopupMenus().isEmpty()) {
            JsonArray popupMenusArray = new JsonArray();
            for (PopupMenuInfo menu : formInfo.getPopupMenus()) {
                popupMenusArray.add(convertPopupMenuToJson(menu));
            }
            formJson.add("popupMenus", popupMenusArray);
        }

        // Popup Menus PostgreSQL
        if (formInfo.getPopupMenusPg() != null && !formInfo.getPopupMenusPg().isEmpty()) {
            JsonArray popupMenusPgArray = new JsonArray();
            for (PopupMenuInfo menu : formInfo.getPopupMenusPg()) {
                popupMenusPgArray.add(convertPopupMenuToJson(menu));
            }
            formJson.add("popupMenusPg", popupMenusPgArray);
        }

        // SQL Queries count
        formJson.addProperty("totalSqlQueries", formInfo.getSqlQueries().size());

        return formJson;
    }

    /**
     * Добавляет Set в JSON объект
     */
    private void addSetToJson(JsonObject json, String key, Set<String> values) {
        if (values != null && !values.isEmpty()) {
            JsonArray array = new JsonArray();
            for (String value : values) {
                array.add(value);
            }
            json.add(key, array);
        }
    }

    /**
     * Конвертирует PopupMenuInfo в JSON
     */
    private JsonObject convertPopupMenuToJson(PopupMenuInfo menu) {
        JsonObject menuJson = new JsonObject();
        menuJson.addProperty("name", menu.getName());

        JsonArray itemsArray = new JsonArray();
        for (PopupMenuInfo.MenuItem item : menu.getRootItems()) {
            itemsArray.add(convertMenuItemToJson(item));
        }
        menuJson.add("items", itemsArray);

        return menuJson;
    }

    /**
     * Конвертирует MenuItem в JSON
     */
    private JsonObject convertMenuItemToJson(PopupMenuInfo.MenuItem item) {
        JsonObject itemJson = new JsonObject();

        if (item.getCaption() != null) {
            itemJson.addProperty("caption", item.getCaption());
        }
        if (item.getName() != null) {
            itemJson.addProperty("name", item.getName());
        }
        itemJson.addProperty("fromAutoPopup", item.isFromAutoPopup());
        if (item.getAutoPopupName() != null) {
            itemJson.addProperty("autoPopupName", item.getAutoPopupName());
        }
        itemJson.addProperty("isDbReport", item.isDbReport());

        if (item.hasChildren()) {
            JsonArray childrenArray = new JsonArray();
            for (PopupMenuInfo.MenuItem child : item.getChildren()) {
                childrenArray.add(convertMenuItemToJson(child));
            }
            itemJson.add("children", childrenArray);
        }

        return itemJson;
    }
}