// config/AppConfig.java
package ru.tmis.analyzer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Конфигурация приложения (UI состояние, настройки)
 */
public class AppConfig {

    private static final String CONFIG_FILE = "app_config.json";

    // Настройки окна
    private int windowWidth = 1200;
    private int windowHeight = 800;
    private int windowX = 100;
    private int windowY = 100;
    private int windowExtendedState = 0;
    private int splitDividerLocation = 400;

    // Настройки отчета
    private boolean includeSqlContent = false;
    private boolean includeJsForms = true;
    private boolean includeTablesViews = true;
    private boolean includeViewTables = true;
    private boolean includeJsUnitCompositions = true;
    private boolean includeViewDetails = false;
    private boolean includePopupMenus = true;
    private boolean includePostgresPopupMenus = false;
    private boolean enableCSVExport = false;

    // Проверка БД
    private boolean checkDbObjects = false;
    private boolean checkDbData = false;
    private boolean checkPostgresPackages = false;
    private boolean checkPostgresTables = false;
    private boolean checkPostgresPK = false;
    private boolean checkNotNullConstraints = false;

    // JSON Export
    private boolean enableJSONExport = false;

    // LLM Export
    private boolean enableLLMExport = false;
    private String llmExportMode = "single_file";
    private String llmInstructionText = "";

    // LLM блоки (какие данные включать)
    private boolean includeSqlQueries = true;
    private boolean includePostgresViews = true;
    private boolean includeOracleViews = true;
    private boolean includePostgresTables = true;
    private boolean includeOracleTables = true;
    private boolean includeOracleFunctions = true;
    private boolean includePostgresFunctions = true;
    private boolean includeBrokerFunctions = true;

    private boolean skipDbOnError = true;


    // ==================== GETTERS AND SETTERS ====================

    // Настройки окна
    public int getWindowWidth() { return windowWidth; }
    public void setWindowWidth(int windowWidth) { this.windowWidth = windowWidth; }

    public int getWindowHeight() { return windowHeight; }
    public void setWindowHeight(int windowHeight) { this.windowHeight = windowHeight; }

    public int getWindowX() { return windowX; }
    public void setWindowX(int windowX) { this.windowX = windowX; }

    public int getWindowY() { return windowY; }
    public void setWindowY(int windowY) { this.windowY = windowY; }

    public int getWindowExtendedState() { return windowExtendedState; }
    public void setWindowExtendedState(int windowExtendedState) { this.windowExtendedState = windowExtendedState; }

    public int getSplitDividerLocation() { return splitDividerLocation; }
    public void setSplitDividerLocation(int splitDividerLocation) { this.splitDividerLocation = splitDividerLocation; }

    // Настройки отчета
    public boolean isIncludeSqlContent() { return includeSqlContent; }
    public void setIncludeSqlContent(boolean includeSqlContent) { this.includeSqlContent = includeSqlContent; }

    public boolean isIncludeJsForms() { return includeJsForms; }
    public void setIncludeJsForms(boolean includeJsForms) { this.includeJsForms = includeJsForms; }

    public boolean isIncludeTablesViews() { return includeTablesViews; }
    public void setIncludeTablesViews(boolean includeTablesViews) { this.includeTablesViews = includeTablesViews; }

    public boolean isIncludeViewTables() { return includeViewTables; }
    public void setIncludeViewTables(boolean includeViewTables) { this.includeViewTables = includeViewTables; }

    public boolean isIncludeJsUnitCompositions() { return includeJsUnitCompositions; }
    public void setIncludeJsUnitCompositions(boolean includeJsUnitCompositions) { this.includeJsUnitCompositions = includeJsUnitCompositions; }

    public boolean isIncludeViewDetails() { return includeViewDetails; }
    public void setIncludeViewDetails(boolean includeViewDetails) { this.includeViewDetails = includeViewDetails; }

    public boolean isIncludePopupMenus() { return includePopupMenus; }
    public void setIncludePopupMenus(boolean includePopupMenus) { this.includePopupMenus = includePopupMenus; }

    public boolean isIncludePostgresPopupMenus() { return includePostgresPopupMenus; }
    public void setIncludePostgresPopupMenus(boolean includePostgresPopupMenus) { this.includePostgresPopupMenus = includePostgresPopupMenus; }

    // Проверка БД
    public boolean isCheckDbObjects() { return checkDbObjects; }
    public void setCheckDbObjects(boolean checkDbObjects) { this.checkDbObjects = checkDbObjects; }

    public boolean isCheckDbData() { return checkDbData; }
    public void setCheckDbData(boolean checkDbData) { this.checkDbData = checkDbData; }

    public boolean isCheckPostgresPackages() { return checkPostgresPackages; }
    public void setCheckPostgresPackages(boolean checkPostgresPackages) { this.checkPostgresPackages = checkPostgresPackages; }

    public boolean isCheckPostgresTables() { return checkPostgresTables; }
    public void setCheckPostgresTables(boolean checkPostgresTables) { this.checkPostgresTables = checkPostgresTables; }

    public boolean isCheckPostgresPK() { return checkPostgresPK; }
    public void setCheckPostgresPK(boolean checkPostgresPK) { this.checkPostgresPK = checkPostgresPK; }

    public boolean isCheckNotNullConstraints() { return checkNotNullConstraints; }
    public void setCheckNotNullConstraints(boolean checkNotNullConstraints) { this.checkNotNullConstraints = checkNotNullConstraints; }

    // LLM Export
    public boolean isEnableLLMExport() { return enableLLMExport; }
    public void setEnableLLMExport(boolean enableLLMExport) { this.enableLLMExport = enableLLMExport; }

    public String getLlmExportMode() { return llmExportMode; }
    public void setLlmExportMode(String llmExportMode) { this.llmExportMode = llmExportMode; }

    public String getLlmInstructionText() { return llmInstructionText; }
    public void setLlmInstructionText(String llmInstructionText) { this.llmInstructionText = llmInstructionText; }

    // LLM блоки
    public boolean isIncludeSqlQueries() { return includeSqlQueries; }
    public void setIncludeSqlQueries(boolean includeSqlQueries) { this.includeSqlQueries = includeSqlQueries; }

    public boolean isIncludePostgresViews() { return includePostgresViews; }
    public void setIncludePostgresViews(boolean includePostgresViews) { this.includePostgresViews = includePostgresViews; }

    public boolean isIncludeOracleViews() { return includeOracleViews; }
    public void setIncludeOracleViews(boolean includeOracleViews) { this.includeOracleViews = includeOracleViews; }

    public boolean isIncludePostgresTables() { return includePostgresTables; }
    public void setIncludePostgresTables(boolean includePostgresTables) { this.includePostgresTables = includePostgresTables; }

    public boolean isIncludeOracleTables() { return includeOracleTables; }
    public void setIncludeOracleTables(boolean includeOracleTables) { this.includeOracleTables = includeOracleTables; }

    public boolean isIncludeOracleFunctions() { return includeOracleFunctions; }
    public void setIncludeOracleFunctions(boolean includeOracleFunctions) { this.includeOracleFunctions = includeOracleFunctions; }

    public boolean isIncludePostgresFunctions() { return includePostgresFunctions; }
    public void setIncludePostgresFunctions(boolean includePostgresFunctions) { this.includePostgresFunctions = includePostgresFunctions; }

    public boolean isIncludeBrokerFunctions() { return includeBrokerFunctions; }
    public void setIncludeBrokerFunctions(boolean includeBrokerFunctions) { this.includeBrokerFunctions = includeBrokerFunctions; }

    public boolean isEnableCSVExport() { return enableCSVExport; }
    public void setEnableCSVExport(boolean enableCSVExport) { this.enableCSVExport = enableCSVExport; }
    public boolean isEnableJSONExport() { return enableJSONExport; }
    public void setEnableJSONExport(boolean enableJSONExport) { this.enableJSONExport = enableJSONExport; }

    public boolean isSkipDbOnError() { return skipDbOnError; }
    public void setSkipDbOnError(boolean skip) { this.skipDbOnError = skip; }

    /**
     * Загрузить конфигурацию из файла
     */
    public static AppConfig load() {
        Path configPath = Paths.get(CONFIG_FILE);
        if (Files.exists(configPath)) {
            try (Reader reader = new FileReader(configPath.toFile())) {
                Gson gson = new Gson();
                return gson.fromJson(reader, AppConfig.class);
            } catch (IOException e) {
                System.err.println("Ошибка загрузки конфигурации: " + e.getMessage());
            }
        }
        return new AppConfig();
    }

    /**
     * Сохранить конфигурацию в файл
     */
    public void save() {
        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this, writer);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения конфигурации: " + e.getMessage());
        }
    }
}