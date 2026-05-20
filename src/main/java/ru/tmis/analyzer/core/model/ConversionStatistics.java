// core/model/ConversionStatistics.java
package ru.tmis.analyzer.core.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Статистика конвертации SQL запросов в ActionRouter/DataSetRouter
 */
public class ConversionStatistics {

    private final String formPath;
    private int totalQueries = 0;
    private int convertedQueries = 0;
    private final Map<String, QueryConversionInfo> queryDetails = new LinkedHashMap<>();

    public ConversionStatistics(String formPath) {
        this.formPath = formPath;
    }

    public void addQuery(String componentName, String componentType, boolean hasRouter, boolean hasOracleSql, boolean hasPostgresSql) {
        totalQueries++;
        if (hasRouter) {
            convertedQueries++;
        }
        queryDetails.put(componentName, new QueryConversionInfo(
                componentName, componentType, hasRouter, hasOracleSql, hasPostgresSql
        ));
    }

    public double getConversionPercent() {
        if (totalQueries == 0) return 0;
        return (convertedQueries * 100.0) / totalQueries;
    }

    public boolean isFullyConverted() {
        return totalQueries > 0 && convertedQueries == totalQueries;
    }

    public boolean isNotConverted() {
        return totalQueries > 0 && convertedQueries == 0;
    }

    // Getters
    public String getFormPath() { return formPath; }
    public int getTotalQueries() { return totalQueries; }
    public int getConvertedQueries() { return convertedQueries; }
    public Map<String, QueryConversionInfo> getQueryDetails() { return queryDetails; }

    @Override
    public String toString() {
        return String.format("Форма: %s, Всего: %d, Конвертировано: %d (%.1f%%)",
                formPath, totalQueries, convertedQueries, getConversionPercent());
    }

    /**
     * Информация о конвертации одного запроса
     */
    public static class QueryConversionInfo {
        private final String componentName;
        private final String componentType;
        private final boolean hasRouter;
        private final boolean hasOracleSql;
        private final boolean hasPostgresSql;

        public QueryConversionInfo(String componentName, String componentType,
                                   boolean hasRouter, boolean hasOracleSql, boolean hasPostgresSql) {
            this.componentName = componentName;
            this.componentType = componentType;
            this.hasRouter = hasRouter;
            this.hasOracleSql = hasOracleSql;
            this.hasPostgresSql = hasPostgresSql;
        }

        public String getComponentName() { return componentName; }
        public String getComponentType() { return componentType; }
        public boolean hasRouter() { return hasRouter; }
        public boolean hasOracleSql() { return hasOracleSql; }
        public boolean hasPostgresSql() { return hasPostgresSql; }
        public boolean isFullyConverted() {
            return hasRouter && hasOracleSql && hasPostgresSql;
        }

        public String getStatus() {
            if (hasRouter && hasOracleSql && hasPostgresSql) {
                return "✓ КОНВЕРТИРОВАН";
            } else if (hasRouter) {
                return "⚠ ЧАСТИЧНО (есть Router, но не хватает SQL)";
            } else {
                return "✗ НЕ КОНВЕРТИРОВАН";
            }
        }

        @Override
        public String toString() {
            return String.format("    %s [%s]: %s", componentName, componentType, getStatus());
        }
    }
}