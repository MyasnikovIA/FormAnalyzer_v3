// core/service/ViewDependencyAnalyzer.java
package ru.tmis.analyzer.ui;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.DatabaseCacheManager;
import ru.tmis.analyzer.core.model.ViewTableDependencies;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewDependencyAnalyzer {

    private final SettingsModel settings;
    private static final Map<String, ViewTableDependencies> globalViewCache = new ConcurrentHashMap<>();

    // Кэш статуса доступности PostgreSQL
    private static Boolean postgresAvailable = null;
    private static long lastPostgresCheck = 0;
    private static final long CHECK_INTERVAL = 60000; // проверять раз в минуту

    // Статические поля для хранения конфигурации (для использования без экземпляра SettingsModel)
    private static String staticOracleUrl;
    private static String staticOracleUser;
    private static String staticOraclePassword;
    private static String staticPostgresUrl;
    private static String staticPostgresUser;
    private static String staticPostgresPassword;
    private static String staticPostgresMisUser;

    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "\\bFROM\\s+([A-Za-z0-9_]+)\\b|\\bJOIN\\s+([A-Za-z0-9_]+)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public ViewDependencyAnalyzer(SettingsModel settings) {
        this.settings = settings;
    }

    /**
     * Установка конфигурации Oracle для статического использования
     */
    public static void setOracleConfig(String url, String user, String password) {
        staticOracleUrl = url;
        staticOracleUser = user;
        staticOraclePassword = password;
    }

    /**
     * Установка конфигурации PostgreSQL для статического использования
     */
    public static void setPostgresConfig(String url, String user, String password) {
        staticPostgresUrl = url;
        staticPostgresUser = user;
        staticPostgresPassword = password;
    }

    /**
     * Установка пользователя МИС для контекста PostgreSQL
     */
    public static void setPostgresMisUser(String misUser) {
        staticPostgresMisUser = misUser;
    }

    public ViewTableDependencies analyzeView(String viewName) {
        return DatabaseCacheManager.getViewDependencies(viewName, () -> {
            ViewTableDependencies deps = new ViewTableDependencies(viewName);

            String ddl = getViewDDL(viewName);
            if (ddl != null && !ddl.isEmpty()) {
                deps.setExistsInOracle(true);
                extractTablesFromDDL(ddl, deps);
            } else {
                deps.setExistsInOracle(false);
                deps.setOracleError("Вьюха не найдена в Oracle");
            }

            return doAnalyzeView(viewName);
        });
    }

    private ViewTableDependencies doAnalyzeView(String viewName) {
        ViewTableDependencies deps = new ViewTableDependencies(viewName);

        // Получаем DDL вьюхи из Oracle
        String ddl = getViewDDL(viewName);
        if (ddl != null && !ddl.isEmpty()) {
            deps.setExistsInOracle(true);
            extractTablesFromDDL(ddl, deps);
        } else {
            deps.setExistsInOracle(false);
            deps.setOracleError("Вьюха не найдена в Oracle");
        }

        // Получаем DDL вьюхи из PostgreSQL
        String postgresDDL = getPostgresViewDDL(viewName);
        if (postgresDDL != null && !postgresDDL.isEmpty()) {
            deps.setExistsInPostgres(true);
            Set<String> postgresTables = extractTablesFromPostgresDDL(postgresDDL);
            deps.addAllPostgresTables(postgresTables);
        } else {
            deps.setExistsInPostgres(false);
            deps.setPostgresError("Вьюха не найдена в PostgreSQL");
        }

        return deps;
    }


    private Connection getPostgresConnection() throws SQLException {
        // Используем статические настройки, если они установлены, иначе берем из settings
        String url, user, password, misUser;

        if (staticPostgresUrl != null && !staticPostgresUrl.isEmpty()) {
            url = staticPostgresUrl;
            user = staticPostgresUser;
            password = staticPostgresPassword;
            misUser = staticPostgresMisUser;
        } else if (settings != null) {
            url = settings.getPostgresUrl();
            user = settings.getPostgresUser();
            password = settings.getPostgresPassword();
            misUser = settings.getMisUser();
        } else {
            throw new SQLException("PostgreSQL конфигурация не установлена");
        }

        DriverManager.setLoginTimeout(10);
        Connection conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(true);

        // Устанавливаем контекст МИС
        if (misUser != null && !misUser.trim().isEmpty()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT set_config('mis.user', '" + misUser + "', false)");
            } catch (SQLException e) {
                System.err.println("  [PostgreSQL] Ошибка установки контекста: " + e.getMessage());
            }
        }

        return conn;
    }

    private Set<String> extractTablesFromPostgresDDL(String ddl) {
        Set<String> tables = new LinkedHashSet<>();
        if (ddl == null) return tables;

        String upperDdl = ddl.toUpperCase();
        Matcher matcher = TABLE_PATTERN.matcher(upperDdl);

        Set<String> sqlKeywords = Set.of(
                "SELECT", "FROM", "WHERE", "JOIN", "ON", "AND", "OR", "NOT",
                "IN", "EXISTS", "AS", "LEFT", "RIGHT", "INNER", "OUTER", "CROSS",
                "UNION", "INTERSECT", "MINUS", "WITH", "RECURSIVE"
        );

        while (matcher.find()) {
            String table = matcher.group(1);
            if (table == null) table = matcher.group(2);

            if (table != null &&
                    !sqlKeywords.contains(table) &&
                    !table.startsWith("D_V_") &&
                    table.startsWith("D_")) {
                tables.add(table);
            }
        }

        return tables;
    }

    private String getViewDDL(String viewName) {
        String sql = "SELECT TEXT FROM ALL_VIEWS WHERE VIEW_NAME = ?";

        // Используем статические настройки, если они установлены, иначе берем из settings
        String url, user, password;

        if (staticOracleUrl != null && !staticOracleUrl.isEmpty()) {
            url = staticOracleUrl;
            user = staticOracleUser;
            password = staticOraclePassword;
        } else if (settings != null) {
            url = settings.getOracleUrl();
            user = settings.getOracleUser();
            password = settings.getOraclePassword();
        } else {
            System.err.println("  Oracle конфигурация не установлена");
            return null;
        }

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
        props.setProperty("oracle.jdbc.ReadTimeout", "30000");

        try (Connection conn = DriverManager.getConnection(url, props);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, viewName.toUpperCase());
            pstmt.setQueryTimeout(30);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("TEXT");
            }
        } catch (SQLException e) {
            System.err.println("  Ошибка получения DDL вьюхи " + viewName + ": " + e.getMessage());
        }

        return null;
    }

    private void extractTablesFromDDL(String ddl, ViewTableDependencies deps) {
        if (ddl == null) return;

        String upperDdl = ddl.toUpperCase();
        Matcher matcher = TABLE_PATTERN.matcher(upperDdl);

        Set<String> sqlKeywords = Set.of(
                "SELECT", "FROM", "WHERE", "JOIN", "ON", "AND", "OR", "NOT",
                "IN", "EXISTS", "AS", "LEFT", "RIGHT", "INNER", "OUTER", "CROSS",
                "UNION", "INTERSECT", "MINUS", "WITH", "RECURSIVE"
        );

        // Системные объекты Oracle, которые не являются таблицами пользователя
        Set<String> systemObjects = Set.of(
                "DUAL",      // Системная таблица Oracle
                "USER",      // Системная информация
                "ALL_USERS", // Системная информация
                "USER_TABLES" // Системная информация
        );

        while (matcher.find()) {
            String table = matcher.group(1);
            if (table == null) table = matcher.group(2);

            if (table != null &&
                    !sqlKeywords.contains(table) &&
                    !table.startsWith("D_V_") &&
                    !systemObjects.contains(table)) {
                deps.addOracleTable(table);
            }
        }
    }

    public ViewTableDependencies analyzeViewPublic(String viewName) {
        String key = viewName.toUpperCase();

        // Проверяем глобальный кэш
        if (globalViewCache.containsKey(key)) {
            System.out.println("[КЭШ] Вьюха " + viewName + " взята из глобального кэша");
            return globalViewCache.get(key);
        }

        ViewTableDependencies deps = analyzeView(viewName);
        if (deps != null) {
            globalViewCache.put(key, deps);
            System.out.println("[КЭШ] Вьюха " + viewName + " сохранена в глобальный кэш");
        }
        return deps;
    }

    public static boolean isInCache(String viewName) {
        return globalViewCache.containsKey(viewName.toUpperCase());
    }
    /**
     * Проверяет доступность PostgreSQL
     */
    private boolean isPostgresAvailable() {
        long now = System.currentTimeMillis();
        if (postgresAvailable != null && (now - lastPostgresCheck) < CHECK_INTERVAL) {
            return postgresAvailable;
        }

        try {
            String url, user, password;
            if (settings != null) {
                url = settings.getPostgresUrl();
                user = settings.getPostgresUser();
                password = settings.getPostgresPassword();
            } else {
                postgresAvailable = false;
                return false;
            }

            DriverManager.setLoginTimeout(3);
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                postgresAvailable = conn.isValid(2);
            }
        } catch (SQLException e) {
            System.err.println("[PostgreSQL] Недоступна: " + e.getMessage());
            postgresAvailable = false;
        }
        lastPostgresCheck = now;
        return postgresAvailable;
    }

    private String getPostgresViewDDL(String viewName) {
        // Быстрая проверка доступности PostgreSQL
        if (!isPostgresAvailable()) {
            System.err.println("  [PostgreSQL] База недоступна, пропускаем запрос для " + viewName);
            return null;
        }

        String sql = "SELECT pg_get_viewdef(p.oid, true) as viewdef " +
                "FROM pg_class p " +
                "WHERE p.relname = ? AND p.relkind = 'v'";

        try (Connection conn = getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, viewName.toLowerCase());
            pstmt.setQueryTimeout(5);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("viewdef");
            }
        } catch (SQLException e) {
            System.err.println("  Ошибка получения DDL вьюхи " + viewName + " из PostgreSQL: " + e.getMessage());
        }
        return null;
    }

}