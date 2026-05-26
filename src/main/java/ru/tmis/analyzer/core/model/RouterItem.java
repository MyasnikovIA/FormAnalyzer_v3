// core/model/RouterItem.java
package ru.tmis.analyzer.core.model;

/**
 * Отдельный роутер (ActionRouter или DataSetRouter)
 */
public class RouterItem {

    private final String condition;      // Условие выбора (TYPE_DATABASE=ORACLE и т.д.)
    private final String sqlContent;     // SQL/PLSQL содержимое в CDATA
    private final String unit;           // Для ActionRouter - unit (опционально)
    private final String action;         // Для ActionRouter - action (опционально)
    private final int order;             // Порядок объявления в XML

    public RouterItem(String condition, String sqlContent, int order) {
        this(condition, sqlContent, order, null, null);
    }

    public RouterItem(String condition, String sqlContent, int order, String unit, String action) {
        this.condition = condition;
        this.sqlContent = sqlContent;
        this.order = order;
        this.unit = unit;
        this.action = action;
    }

    // Геттеры
    public String getCondition() { return condition; }
    public String getSqlContent() { return sqlContent; }
    public int getOrder() { return order; }
    public String getUnit() { return unit; }
    public String getAction() { return action; }

    public boolean hasUnitAction() {
        return unit != null && !unit.isEmpty() && action != null && !action.isEmpty();
    }

    public boolean isOracleRouter() {
        return condition != null && condition.toUpperCase().contains("ORACLE");
    }

    public boolean isPostgresRouter() {
        return condition != null && condition.toUpperCase().contains("POSTGRE");
    }

    public boolean isTmisMode() {
        return condition != null && condition.toUpperCase().contains("TMIS");
    }

    public boolean isNmisMode() {
        return condition != null && condition.toUpperCase().contains("NMIS");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RouterItem{condition='").append(condition).append("'");
        if (unit != null) sb.append(", unit='").append(unit).append("'");
        if (action != null) sb.append(", action='").append(action).append("'");
        sb.append(", order=").append(order);
        sb.append(", sqlLength=").append(sqlContent != null ? sqlContent.length() : 0);
        sb.append("}");
        return sb.toString();
    }
}