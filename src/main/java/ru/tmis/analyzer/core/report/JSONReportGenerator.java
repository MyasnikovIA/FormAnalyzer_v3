// core/report/JSONReportGenerator.java

package ru.tmis.analyzer.core.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.model.DbReportInfo;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.PopupMenuInfo;
import ru.tmis.analyzer.core.model.ViewTableDependencies;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONReportGenerator {

    private final String outputDir;
    private final AppConfig config;
    private final SettingsModel settings;
    private final Gson gson;

    // Паттерны для разбора строки отчёта
    private static final Pattern AUTO_POPUP_PATTERN = Pattern.compile(
            "\\(AutoPopup \"([^\"]+)\"\\)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern REP_TYPE_PATTERN = Pattern.compile(
            "REP_TYPE=\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern REP_CODE_PATTERN = Pattern.compile(
            "REP_CODE=\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PRIV_NAME_PATTERN = Pattern.compile(
            "\"([^\"]+)\"\\s*\\([\"']([^\"']+)[\"']\\)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern FORM_PATH_PATTERN = Pattern.compile(
            "Form=\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
    );

    public JSONReportGenerator(String outputDir, AppConfig config) {
        this.outputDir = outputDir;
        this.config = config;
        this.settings = SettingsModel.getInstance();
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

        if (Files.exists(jsonPath)) {
            String content = Files.readString(jsonPath);
            if (!content.isEmpty()) {
                try {
                    JsonObject root = gson.fromJson(content, JsonObject.class);
                    if (root.has("forms")) {
                        formsArray = root.getAsJsonArray("forms");
                    }
                } catch (Exception e) {
                    formsArray = new JsonArray();
                }
            }
        }

        formsArray.add(convertFormToJson(formInfo));

        JsonObject root = new JsonObject();
        root.add("forms", formsArray);
        root.addProperty("totalForms", formsArray.size());
        root.addProperty("exportDate", new Date().toString());

        Files.writeString(jsonPath, gson.toJson(root));
    }

    /**
     * Конвертирует FormInfo в JSON объект
     */
    public JsonObject convertFormToJson(FormInfo formInfo) {
        JsonObject formJson = new JsonObject();

        // Основная информация
        formJson.addProperty("formPath", formInfo.getFormPath());
        formJson.addProperty("baseFormPath", getRelativePath(formInfo.getBaseFormPath()));
        formJson.addProperty("fullyReplaced", formInfo.isFullyReplaced());
        if (formInfo.getReplacementPath() != null) {
            formJson.addProperty("replacementPath", getRelativePath(formInfo.getReplacementPath()));
        }

        // Overrides (UserForms)
        JsonArray overridesArray = new JsonArray();
        for (FormInfo.OverrideInfo override : formInfo.getOverrides()) {
            JsonObject overrideJson = new JsonObject();
            overrideJson.addProperty("regionName", override.getRegionName());
            overrideJson.addProperty("overridePath", getRelativePath(override.getOverridePath()));
            overrideJson.addProperty("type", override.getType().getDescription());
            overrideJson.addProperty("typeCode", override.getType().name());
            overridesArray.add(overrideJson);
        }
        formJson.add("Юзерформы", overridesArray);

        // subForm
        addSetToJson(formJson, "subForm", formInfo.getSubForms());

        // формы JS
        addSetToJson(formJson, "формы JS", formInfo.getJsForms());

        // Отчеты, вызываемые на форме
        addSetToJson(formJson, "Отчеты, вызываемые на форме", formInfo.getReports());

        // Отчеты из AutoPopup с отдельными атрибутами
        addReportsFromAutoPopupToJson(formJson, formInfo.getReportsFromAutoPopup());

        // Вьюхи
        Set<String> views = new LinkedHashSet<>();
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_V_")) {
                views.add(tv);
            }
        }
        addSetToJson(formJson, "Вьюхи", views);

        // Таблицы
        Set<String> tables = new LinkedHashSet<>();
        for (String tv : formInfo.getTablesViews()) {
            if (tv.startsWith("D_") && !tv.startsWith("D_V_")) {
                tables.add(tv);
            }
        }
        addSetToJson(formJson, "Таблицы", tables);

        // Пакеты и функции
        addSetToJson(formJson, "Пакеты и функции", formInfo.getPackagesFunctions());

        // СО
        addSetToJson(formJson, "СО", formInfo.getSystemOptions());

        // Универсальные композиции
        addSetToJson(formJson, "Универсальные композиции", formInfo.getJsUnitCompositions());

        // Пользовательские процедуры
        addSetToJson(formJson, "Пользовательские процедуры", formInfo.getUserProcedures());

        // Константы
        addSetToJson(formJson, "Константы", formInfo.getConstants());

        // Брокеры
        addSetToJson(formJson, "Брокеры", formInfo.getBrokers());

        // Неопределенные
        addSetToJson(formJson, "Неопределенные", formInfo.getUnknownObjects());

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

        // Popup Menus - НОВАЯ ИЕРАРХИЧЕСКАЯ СТРУКТУРА
        if (formInfo.getPopupMenus() != null && !formInfo.getPopupMenus().isEmpty()) {
            JsonArray popupMenusArray = new JsonArray();
            for (PopupMenuInfo menu : formInfo.getPopupMenus()) {
                popupMenusArray.add(convertPopupMenuToHierarchicalJson(menu, false));
            }
            formJson.add("popupMenus", popupMenusArray);
        }

        // Popup Menus PostgreSQL
        if (formInfo.getPopupMenusPg() != null && !formInfo.getPopupMenusPg().isEmpty()) {
            JsonArray popupMenusPgArray = new JsonArray();
            for (PopupMenuInfo menu : formInfo.getPopupMenusPg()) {
                popupMenusPgArray.add(convertPopupMenuToHierarchicalJson(menu, true));
            }
            formJson.add("popupMenusPg", popupMenusPgArray);
        }

        // SQL Queries count
        formJson.addProperty("totalSqlQueries", formInfo.getSqlQueries().size());

        return formJson;
    }

    /**
     * НОВЫЙ МЕТОД: Конвертирует PopupMenuInfo в иерархический JSON
     */
    private JsonObject convertPopupMenuToHierarchicalJson(PopupMenuInfo menu, boolean isPostgres) {
        JsonObject menuJson = new JsonObject();
        menuJson.addProperty("name", menu.getName());
        if (isPostgres) {
            menuJson.addProperty("source", "PostgreSQL");
        } else {
            menuJson.addProperty("source", "Oracle");
        }

        JsonArray itemsArray = new JsonArray();
        for (PopupMenuInfo.MenuItem item : menu.getRootItems()) {
            itemsArray.add(convertMenuItemToHierarchicalJson(item));
        }
        menuJson.add("items", itemsArray);

        return menuJson;
    }

    /**
     * НОВЫЙ МЕТОД: Конвертирует MenuItem в иерархический JSON
     */
    private JsonObject convertMenuItemToHierarchicalJson(PopupMenuInfo.MenuItem item) {
        JsonObject itemJson = new JsonObject();

        if (item.isDbReport() && item.getCaption() != null) {
            // Это отчёт из БД - парсим строку caption
            parseDbReportCaption(item.getCaption(), itemJson, item.isFromAutoPopup(), item.getAutoPopupName());
        } else {
            // Обычный пункт меню
            if (item.getCaption() != null && !item.getCaption().isEmpty()) {
                itemJson.addProperty("caption", item.getCaption());
            } else if (item.getName() != null && !item.getName().isEmpty()) {
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
                childrenArray.add(convertMenuItemToHierarchicalJson(child));
            }
            itemJson.add("children", childrenArray);
        }

        return itemJson;
    }

    /**
     * Парсит строку caption отчёта из БД и создаёт структурированный JSON объект
     */
    private void parseDbReportCaption(String caption, JsonObject itemJson, boolean fromAutoPopup, String autoPopupName) {
        if (caption == null || caption.isEmpty()) return;

        // Извлекаем AutoPopup имя
        String autoPopup = null;
        Matcher autoMatcher = AUTO_POPUP_PATTERN.matcher(caption);
        if (autoMatcher.find()) {
            autoPopup = autoMatcher.group(1);
        }

        // Извлекаем REP_TYPE
        String repType = null;
        Matcher typeMatcher = REP_TYPE_PATTERN.matcher(caption);
        if (typeMatcher.find()) {
            repType = typeMatcher.group(1);
        }

        // Извлекаем REP_CODE
        String repCode = null;
        Matcher codeMatcher = REP_CODE_PATTERN.matcher(caption);
        if (codeMatcher.find()) {
            repCode = codeMatcher.group(1);
        }

        // Извлекаем PRIV_NAME и REP_NAME
        String privName = null;
        String repName = null;
        Matcher nameMatcher = PRIV_NAME_PATTERN.matcher(caption);
        if (nameMatcher.find()) {
            privName = nameMatcher.group(1);
            repName = nameMatcher.group(2);
        }

        // Извлекаем Form путь
        String formPath = null;
        Matcher formMatcher = FORM_PATH_PATTERN.matcher(caption);
        if (formMatcher.find()) {
            formPath = formMatcher.group(1);
        }

        // Создаём структурированный объект
        if (autoPopup != null) {
            // Если это пункт из AutoPopup, создаём объект с autoPopupName
            JsonObject reportJson = new JsonObject();
            reportJson.addProperty("autoPopupName", autoPopup);

            JsonObject reportData = new JsonObject();
            if (repType != null) reportData.addProperty("REP_TYPE", repType);
            if (repCode != null) reportData.addProperty("REP_CODE", repCode);
            if (privName != null) reportData.addProperty("PRIV_NAME", privName);
            if (repName != null) reportData.addProperty("REP_NAME", repName);
            if (formPath != null) reportData.addProperty("REP_FILENAME", formPath);

            reportJson.add("report", reportData);

            // Добавляем все поля в itemJson
            itemJson.addProperty("autoPopupName", autoPopup);
            itemJson.add("report", reportData);
        } else {
            // Обычный отчёт
            if (repType != null) itemJson.addProperty("REP_TYPE", repType);
            if (repCode != null) itemJson.addProperty("REP_CODE", repCode);
            if (privName != null) itemJson.addProperty("PRIV_NAME", privName);
            if (repName != null) itemJson.addProperty("REP_NAME", repName);
            if (formPath != null) itemJson.addProperty("REP_FILENAME", formPath);
        }

        // Добавляем информацию об источнике
        if (fromAutoPopup && autoPopup != null) {
            itemJson.addProperty("fromAutoPopup", true);
        }
        itemJson.addProperty("isDbReport", true);
    }

    /**
     * Добавляет информацию об отчётах из AutoPopup в JSON
     */
    private void addReportsFromAutoPopupToJson(JsonObject formJson, List<FormInfo.ReportFromAutoPopupInfo> reports) {
        if (reports == null || reports.isEmpty()) {
            return;
        }

        JsonArray reportsArray = new JsonArray();
        for (FormInfo.ReportFromAutoPopupInfo report : reports) {
            JsonObject reportJson = new JsonObject();
            reportJson.addProperty("repCode", report.getRepCode());
            reportJson.addProperty("repType", report.getRepType());
            reportJson.addProperty("repTypeName", report.getRepTypeName());
            if (report.getRepFilename() != null && !report.getRepFilename().isEmpty()) {
                reportJson.addProperty("repFilename", report.getRepFilename());
            }
            if (report.getFormPath() != null && !report.getFormPath().isEmpty()) {
                reportJson.addProperty("formPath", report.getFormPath());
            }
            reportsArray.add(reportJson);
        }
        formJson.add("отчетыИзAutoPopup", reportsArray);
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
    public String convertFormToJsonString(FormInfo formInfo) {
        JsonObject formJson = convertFormToJson(formInfo);
        return gson.toJson(formJson);
    }

}