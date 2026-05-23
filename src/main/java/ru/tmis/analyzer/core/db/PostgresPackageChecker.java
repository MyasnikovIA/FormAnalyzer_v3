package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.core.cache.DatabaseCacheManager;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

public class PostgresPackageChecker {

    private final String url;
    private final String user;
    private final String password;
    private Consumer<String> logCallback;

    private static final Map<String, FunctionInfo> cache = new LinkedHashMap<>();

    public PostgresPackageChecker(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public void setLogCallback(Consumer<String> callback) {
        this.logCallback = callback;
    }

    private void log(String msg) {
        if (logCallback != null) logCallback.accept(msg);
        else System.out.println(msg);
    }

    public static class FunctionInfo {
        private final String name;
        private final String signature;
        private final List<String> errors;
        private final List<String> warnings;
        private final boolean exists;

        public FunctionInfo(String name, String signature, List<String> errors, List<String> warnings, boolean exists) {
            this.name = name;
            this.signature = signature;
            this.errors = errors;
            this.warnings = warnings;
            this.exists = exists;
        }

        public String getName() { return name; }
        public String getSignature() { return signature; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public boolean isExists() { return exists; }
        public boolean hasErrors() { return !errors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }

        public String getStatus() {
            if (!exists) return "НЕ СУЩЕСТВУЕТ";
            if (hasErrors()) return "СУЩЕСТВУЕТ (ОШИБКИ)";
            if (hasWarnings()) return "СУЩЕСТВУЕТ (ПРЕДУПРЕЖДЕНИЯ)";
            return "СУЩЕСТВУЕТ (OK)";
        }
    }

    private Connection getConnection() throws SQLException {
        return DatabaseConnector.getPostgresConnection(url, user, password);
    }

    private String cleanFunctionSignature(String arguments) {
        if (arguments == null || arguments.isEmpty()) return "";
        String[] params = arguments.split(",");
        List<String> clean = new ArrayList<>();
        for (String p : params) {
            String trimmed = p.trim();
            trimmed = trimmed.replaceAll("(?i)\\s+DEFAULT\\s+.*$", "");
            trimmed = trimmed.replaceAll("(?i)^\\s*(INOUT|IN|OUT)\\s+", "");
            trimmed = trimmed.replaceAll("^[a-z_][a-z0-9_]*\\s+", ""); // remove param name
            String norm = normalizeType(trimmed);
            if (!norm.isEmpty()) clean.add(norm);
        }
        return String.join(", ", clean);
    }

    private String normalizeType(String type) {
        String t = type.toLowerCase().trim().replaceAll("\\s+", " ");
        if (t.equals("numeric") || t.equals("decimal")) return "numeric";
        if (t.equals("integer") || t.equals("int") || t.equals("int4")) return "int";
        if (t.equals("bigint") || t.equals("int8")) return "bigint";
        if (t.equals("smallint") || t.equals("int2")) return "smallint";
        if (t.equals("varchar") || t.equals("character varying")) return "varchar";
        if (t.equals("char") || t.equals("character")) return "char";
        if (t.equals("text")) return "text";
        if (t.equals("timestamp") || t.equals("timestamp without time zone")) return "timestamp";
        if (t.equals("timestamptz") || t.equals("timestamp with time zone")) return "timestamptz";
        if (t.equals("date")) return "date";
        if (t.equals("boolean") || t.equals("bool")) return "boolean";
        return t.replaceAll("^[a-z_][a-z0-9_]*\\s+", "");
    }



    public FunctionInfo checkFunction(String functionName) {
        return DatabaseCacheManager.getPostgresFunctionCheck(functionName, () -> {
            return doCheckFunction(functionName);
        });
    }


    private FunctionInfo doCheckFunction(String functionName) {
        String cleanName = functionName.toLowerCase();
        try (Connection conn = getConnection()) {
            // Получить сигнатуру
            String sql = "SELECT n.nspname || '.' || p.proname AS func_name, " +
                    "pg_get_function_identity_arguments(p.oid) AS identity_args " +
                    "FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid " +
                    "WHERE LOWER(n.nspname || '.' || p.proname) = ?";
            String signature = null;
            String identityArgs = null;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cleanName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    signature = rs.getString("func_name");
                    identityArgs = rs.getString("identity_args");
                } else {
                    // Поиск без схемы
                    String simple = cleanName.contains(".") ? cleanName.substring(cleanName.lastIndexOf('.') + 1) : cleanName;
                    sql = "SELECT n.nspname || '.' || p.proname AS func_name, " +
                            "pg_get_function_identity_arguments(p.oid) AS identity_args " +
                            "FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid " +
                            "WHERE LOWER(p.proname) = ? LIMIT 1";
                    try (PreparedStatement stmt2 = conn.prepareStatement(sql)) {
                        stmt2.setString(1, simple);
                        ResultSet rs2 = stmt2.executeQuery();
                        if (rs2.next()) {
                            signature = rs2.getString("func_name");
                            identityArgs = rs2.getString("identity_args");
                        }
                    }
                }
            }

            if (signature == null) {
                return new FunctionInfo(functionName, null, List.of("Функция не найдена"), List.of(), false);
            }

            String cleanArgs = cleanFunctionSignature(identityArgs);
            String fullSignature = signature + (cleanArgs.isEmpty() ? "()" : "(" + cleanArgs + ")");

            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            boolean hasFatal = false;

            // Проверка через plpgsql_check_function
            try (PreparedStatement stmt = conn.prepareStatement("SELECT plpgsql_check_function(?)")) {
                stmt.setString(1, fullSignature);
                stmt.setQueryTimeout(30);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String msg = rs.getString(1);
                    if (msg == null) {
                        hasFatal = true;
                        errors.add("Критическая ошибка (plpgsql_check_function вернул NULL)");
                    } else if (!msg.isEmpty()) {
                        for (String line : msg.split("\\n")) {
                            line = line.trim();
                            if (line.isEmpty()) continue;
                            if (line.toLowerCase().contains("error") || line.contains("does not exist")) {
                                errors.add(line);
                            } else if (line.toLowerCase().contains("warning")) {
                                warnings.add(line);
                            } else {
                                errors.add(line);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                if (e.getMessage().contains("plpgsql_check_function")) {
                    warnings.add("plpgsql_check_function не установлен. Установите: CREATE EXTENSION IF NOT EXISTS plpgsql_check;");
                } else {
                    errors.add("Ошибка проверки: " + e.getMessage());
                }
            }

            boolean exists = !hasFatal;
            return new FunctionInfo(functionName, fullSignature, errors, warnings, exists);

        } catch (SQLException e) {
            return new FunctionInfo(functionName, null, List.of("Ошибка БД: " + e.getMessage()), List.of(), false);
        }
    }

    public Map<String, FunctionInfo> checkFunctions(Set<String> functionNames) {
        Map<String, FunctionInfo> result = new LinkedHashMap<>();
        log("Проверка пакетов/функций в PostgreSQL (" + functionNames.size() + " шт.)...");
        int i = 0;
        for (String name : functionNames) {
            i++;
            log("  [" + i + "/" + functionNames.size() + "] " + name);
            FunctionInfo info = checkFunction(name);
            result.put(name, info);
            log("    " + info.getStatus());
            for (String err : info.getErrors()) log("      ОШИБКА: " + err);
            for (String warn : info.getWarnings()) log("      ПРЕДУПРЕЖДЕНИЕ: " + warn);
        }
        return result;
    }

    public static void clearCache() { cache.clear(); }
}