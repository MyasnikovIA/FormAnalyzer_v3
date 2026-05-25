// core/model/DbReportInfo.java
package ru.tmis.analyzer.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Информация об отчёте из базы данных (Oracle/PostgreSQL)
 */
public class DbReportInfo {
    private String privName;
    private String unitCode;
    private int repType;
    private transient byte[] repData;
    private String repFilename;
    private String repName;
    private String repCode;
    private int repID;
    private List<DbReportInfo> children;

    public DbReportInfo() {
        this.children = new ArrayList<>();
    }

    // ---------- Getters and Setters ----------
    public String getPrivName() { return privName; }
    public void setPrivName(String privName) { this.privName = privName; }

    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }

    public int getRepType() { return repType; }
    public void setRepType(int repType) { this.repType = repType; }

    public byte[] getRepData() { return repData; }
    public void setRepData(byte[] repData) { this.repData = repData; }

    public String getRepFilename() { return repFilename; }
    public void setRepFilename(String repFilename) { this.repFilename = repFilename; }

    public String getRepName() { return repName; }
    public void setRepName(String repName) { this.repName = repName; }

    public String getRepCode() { return repCode; }
    public void setRepCode(String repCode) { this.repCode = repCode; }

    public int getRepID() { return repID; }
    public void setRepID(int repID) { this.repID = repID; }

    public List<DbReportInfo> getChildren() { return children; }
    public void setChildren(List<DbReportInfo> children) { this.children = children; }
    public void addChild(DbReportInfo child) { this.children.add(child); }
    public boolean hasChildren() { return !children.isEmpty(); }
    public boolean isComposite() { return repType == 6; }

    // ---------- Методы для отображения ----------
    public String getRepTypeName() {
        switch (repType) {
            case 0: return "Crystal Reports";
            case 1: return "WEB-форма";
            case 2: return "Crystal Reports(PDF)";
            case 3: return "Бланк";
            case 5: return "WEB-конструктор";
            case 6: return "Составной";
            default: return "Неизвестный тип (" + repType + ")";
        }
    }

    public String getFormPath() {
        if (repFilename != null && !repFilename.isEmpty()) {
            return repFilename + ".frm";
        }
        return null;
    }

    /**
     * Полная строка для отображения отчёта (с UNIT и Form)
     */
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(UNIT='");
        sb.append(unitCode != null ? unitCode : "?");
        sb.append("') - REP_TYPE=\"");
        sb.append(getRepTypeName());
        sb.append("\" - REP_CODE=\"");
        sb.append(repCode != null ? repCode : "?");
        sb.append("\" \"");
        sb.append(repName != null ? repName : "без названия");
        sb.append("\"");
        if (getFormPath() != null) {
            sb.append(" Form=\"").append(getFormPath()).append("\"");
        }
        return sb.toString();
    }

    /**
     * Краткая строка для вложенных отчётов (без UNIT)
     */
    public String getShortDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append("- REP_TYPE=\"");
        sb.append(getRepTypeName());
        sb.append("\" - REP_CODE=\"");
        sb.append(repCode != null ? repCode : "?");
        sb.append("\" \"");
        sb.append(repName != null ? repName : "без названия");
        sb.append("\"");
        if (getFormPath() != null) {
            sb.append(" Form=\"").append(getFormPath()).append("\"");
        }
        return sb.toString();
    }
}