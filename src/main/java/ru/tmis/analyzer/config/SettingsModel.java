// config/SettingsModel.java
package ru.tmis.analyzer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SettingsModel {
    private static final String SETTINGS_FILE = "analyzer_settings.json";

    // Пути и подключения
    private String projectPath = "/var/www/t-mis/mis";
    private String outputDir = "SQL_info";
    private String oracleUrl = "jdbc:oracle:thin:@192.168.241.141:1521/med2dev";
    private String oracleUser = "dev";
    private String oraclePassword = "def";
    private String postgresUrl = "jdbc:postgresql://192.168.241.137:5432/med2dev";
    private String postgresUser = "DEV";
    private String postgresPassword = "def";
    private String misUser = "$TSTESTZON";
    private boolean checkPostgresPackages = false;
    private boolean checkPostgresPK = false;
    private boolean checkNotNullConstraints = false;

    // Singleton
    private static SettingsModel instance;

    private SettingsModel() {}

    public void save() {
        try (Writer writer = new FileWriter(SETTINGS_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this, writer);
            System.out.println("Настройки сохранены в файл: " + SETTINGS_FILE);

            // Обновляем конфигурацию в DatabaseCacheManager
            DatabaseCacheManager.initDbConfig(
                    getOracleUrl(), getOracleUser(), getOraclePassword(),
                    getPostgresUrl(), getPostgresUser(), getPostgresPassword(),
                    getMisUser()
            );
        } catch (IOException e) {
            System.err.println("Ошибка сохранения настроек: " + e.getMessage());
        }
    }

    // Getters and Setters
    public String getProjectPath() { return projectPath; }
    public void setProjectPath(String projectPath) { this.projectPath = projectPath; }

    public String getOutputDir() { return outputDir; }
    public void setOutputDir(String outputDir) { this.outputDir = outputDir; }

    public String getOracleUrl() { return oracleUrl; }
    public void setOracleUrl(String oracleUrl) { this.oracleUrl = oracleUrl; }

    public String getOracleUser() { return oracleUser; }
    public void setOracleUser(String oracleUser) { this.oracleUser = oracleUser; }

    public String getOraclePassword() { return oraclePassword; }
    public void setOraclePassword(String oraclePassword) { this.oraclePassword = oraclePassword; }

    public String getPostgresUrl() { return postgresUrl; }
    public void setPostgresUrl(String postgresUrl) { this.postgresUrl = postgresUrl; }

    public String getPostgresUser() { return postgresUser; }
    public void setPostgresUser(String postgresUser) { this.postgresUser = postgresUser; }

    public String getPostgresPassword() { return postgresPassword; }
    public void setPostgresPassword(String postgresPassword) { this.postgresPassword = postgresPassword; }

    public String getMisUser() { return misUser; }
    public void setMisUser(String misUser) { this.misUser = misUser; }

    public boolean isCheckPostgresPackages() { return checkPostgresPackages; }
    public void setCheckPostgresPackages(boolean checkPostgresPackages) { this.checkPostgresPackages = checkPostgresPackages; }

    public boolean isCheckPostgresPK() { return checkPostgresPK; }
    public void setCheckPostgresPK(boolean checkPostgresPK) { this.checkPostgresPK = checkPostgresPK; }

    public boolean isCheckNotNullConstraints() { return checkNotNullConstraints; }
    public void setCheckNotNullConstraints(boolean checkNotNullConstraints) { this.checkNotNullConstraints = checkNotNullConstraints; }


    // Добавить этот метод
    public static SettingsModel createDefault() {
        return new SettingsModel();
    }

    public static SettingsModel getInstance() {
        if (instance == null) {
            instance = load();
            // Если load() вернул null (что не должно происходить), создаём новый
            if (instance == null) {
                instance = createDefault();
                instance.save(); // Сохраняем настройки по умолчанию
            }
        }
        return instance;
    }

    public static SettingsModel load() {
        Path configPath = Paths.get(SETTINGS_FILE);
        if (Files.exists(configPath)) {
            try (Reader reader = new FileReader(configPath.toFile())) {
                Gson gson = new Gson();
                SettingsModel model = gson.fromJson(reader, SettingsModel.class);
                if (model != null) {
                    System.out.println("Настройки загружены из файла: " + SETTINGS_FILE);
                    return model;
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки настроек: " + e.getMessage());
            }
        } else {
            System.out.println("Файл настроек не найден, будут использованы настройки по умолчанию");
        }
        // Всегда возвращаем новый экземпляр, а не null
        return createDefault();
    }
}