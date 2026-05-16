// core/db/ReportsFromDbService.java
package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.config.SettingsModel;

import java.sql.*;
import java.util.*;

/**
 * Сервис для получения отчетов из БД по unit'у из AutoPopupMenu
 */
public class ReportsFromDbService {

    private final SettingsModel settings;

    // Статический метод для преобразования типа отчета
    private static String getRepTypeNameStatic(int repType) {
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

    public ReportsFromDbService(SettingsModel settings) {
        this.settings = settings;
    }

    /**
     * Получить список отчетов по unit'у
     * @param unitCode код unit'а (например, 'HOSP_HISTORIES')
     * @return список отчетов
     */
    // core/db/ReportsFromDbService.java - исправленный метод

    public List<DbReportInfo> getReportsByUnit(String unitCode) {
        List<DbReportInfo> result = new ArrayList<>();

        if (unitCode == null || unitCode.trim().isEmpty()) {
            return result;
        }

        String sql =
                "SELECT t.ID, t.PRIV_NAME, r.REP_TYPE, r.REP_DATA, r.REP_FILENAME, r.REP_NAME, r.REP_CODE " +
                        "FROM D_REPORTS_LINKS t " +
                        "JOIN D_REPORTS r ON t.PID = r.ID " +
                        "WHERE t.UNITCODE = ?";

        Properties props = new Properties();
        props.setProperty("user", settings.getOracleUser());
        props.setProperty("password", settings.getOraclePassword());
        props.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
        props.setProperty("oracle.jdbc.ReadTimeout", "30000");
        props.setProperty("oracle.jdbc.defaultNChar", "true");

        try (Connection conn = DriverManager.getConnection(settings.getOracleUrl(), props);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, unitCode);
            pstmt.setQueryTimeout(30);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DbReportInfo report = new DbReportInfo();
                report.setPrivName(rs.getString("PRIV_NAME"));
                report.setUnitCode(unitCode);  // <-- СОХРАНЯЕМ UNITCODE
                report.setRepType(rs.getInt("REP_TYPE"));
                report.setRepData(rs.getBytes("REP_DATA"));
                report.setRepFilename(rs.getString("REP_FILENAME"));
                report.setRepName(rs.getString("REP_NAME"));
                report.setRepCode(rs.getString("REP_CODE"));
                report.setRepID(rs.getInt("ID"));
                result.add(report);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения отчетов по unit=" + unitCode + ": " + e.getMessage());
        }

        return result;
    }

    /**
     * Информация об отчете из БД
     */
    public static class DbReportInfo {
        private String privName;
        private String unitCode;
        private int repType;
        private byte[] repData;
        private String repFilename;
        private String repName;
        private String repCode;
        private int repID;
        // Getters and Setters
        public String getPrivName() { return privName; }
        public void setPrivName(String privName) { this.privName = privName; }

        public String getUnitCode() { return unitCode; }        // <-- ДОБАВИТЬ
        public void setUnitCode(String unitCode) { this.unitCode = unitCode; }  // <-- ДОБАВИТЬ

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

        public String getRepTypeName() {
            return getRepTypeNameStatic(repType);
        }

        public String getFormPath() {
            if (repFilename != null && !repFilename.isEmpty()) {
                return repFilename + ".frm";
            }
            return null;
        }

        public String getDisplayString() {
            StringBuilder sb = new StringBuilder();
            sb.append("(UNIT='");
            sb.append(unitCode != null && !unitCode.isEmpty() ? unitCode : "?");
            sb.append("') \"");
            sb.append(repName != null && !repName.isEmpty() ? repName : "без названия");
            sb.append("\" - REP_CODE=\"");
            sb.append(repCode != null && !repCode.isEmpty() ? repCode : "?");
            sb.append("\" - REP_TYPE=\"");
            sb.append(getRepTypeName());
            sb.append("\"");
            if (getFormPath() != null) {
                sb.append(" Form=\"").append(getFormPath()).append("\"");
            }
            return sb.toString();
        }
    }
    /**
     * Форматированный вывод списка отчетов с выравниванием по колонкам
     * @param reports список отчетов
     * @param autoPopupName имя AutoPopup
     * @param indent отступ перед каждой строкой (без учета символов дерева)
     * @return список отформатированных строк
     */
    // core/db/ReportsFromDbService.java

    public static List<String> formatReportsForDisplay(List<DbReportInfo> reports,
                                                       String autoPopupName,
                                                       String indent) {
        if (reports == null || reports.isEmpty()) {
            return Collections.emptyList();
        }

        String autoPopupPrefix = "(AutoPopup \"" + autoPopupName + "\") ";

        // Собираем данные для анализа
        List<ReportRowData> rows = new ArrayList<>();
        for (DbReportInfo report : reports) {
            ReportRowData row = new ReportRowData();
            row.autoPopupPart = autoPopupPrefix;
            row.unitPart = "(UNIT='" + report.getUnitCode() + "') ";
            row.typePart = "- REP_TYPE=\"" + report.getRepTypeName() + "\"";
            row.namePart = "\"" + report.getRepName() + "\"";
            row.codePart = "- REP_CODE=\"" + report.getRepCode() + "\"";
            row.formPart = (report.getFormPath() != null) ? " Form=\"" + report.getFormPath() + "\"" : "";
            rows.add(row);
        }

        // Вычисляем максимальную длину каждой колонки
        int maxAutoPopupPart = 0;
        int maxUnitPart = 0;
        int maxTypePart = 0;
        int maxNamePart = 0;
        int maxCodePart = 0;

        for (ReportRowData row : rows) {
            maxAutoPopupPart = Math.max(maxAutoPopupPart, row.autoPopupPart.length());
            maxUnitPart = Math.max(maxUnitPart, row.unitPart.length());
            maxTypePart = Math.max(maxTypePart, row.typePart.length());
            maxNamePart = Math.max(maxNamePart, row.namePart.length());
            maxCodePart = Math.max(maxCodePart, row.codePart.length());
        }

        // Формируем отформатированные строки
        List<String> result = new ArrayList<>();
        for (ReportRowData row : rows) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent);
            sb.append(padRight(row.autoPopupPart, maxAutoPopupPart));
            sb.append(padRight(row.unitPart, maxUnitPart));
            sb.append(padRight(row.typePart, maxTypePart));
            sb.append(" ");
            sb.append(padRight(row.namePart, maxNamePart));
            sb.append(" ");
            sb.append(padRight(row.codePart, maxCodePart));
            sb.append(row.formPart);
            result.add(sb.toString());
        }

        return result;
    }

    /**
     * Вспомогательный класс для хранения данных строки
     */
    private static class ReportRowData {
        String autoPopupPart;
        String unitPart;
        String typePart;
        String namePart;
        String codePart;
        String formPart;
    }

    /**
     * Дополнить строку пробелами справа до нужной длины
     */
    private static String padRight(String s, int length) {
        if (s == null) s = "";
        if (s.length() >= length) return s;
        StringBuilder sb = new StringBuilder(s);
        for (int i = s.length(); i < length; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
}