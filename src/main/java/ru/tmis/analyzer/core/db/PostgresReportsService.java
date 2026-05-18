// core/db/PostgresReportsService.java
package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.model.DbReportInfo;


import java.sql.*;
import java.util.*;

public class PostgresReportsService {

    private final SettingsModel settings;

    public PostgresReportsService(SettingsModel settings) {
        this.settings = settings;
    }

    public List<DbReportInfo> getReportsByUnit(String unitCode) {
        List<DbReportInfo> result = new ArrayList<>();
        if (unitCode == null || unitCode.trim().isEmpty()) return result;

        String sql = "SELECT rep.id, drl.priv_name, rep.rep_type, rep.rep_data, " +
                "rep.rep_filename, rep.rep_name, rep.rep_code " +
                "FROM d_reports_links drl JOIN d_reports rep ON drl.pid = rep.id " +
                "WHERE drl.unitcode = ?";

        try (Connection conn = getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, unitCode);
            pstmt.setQueryTimeout(30);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DbReportInfo report = new DbReportInfo();
                report.setPrivName(rs.getString("priv_name"));
                report.setUnitCode(unitCode);
                report.setRepType(rs.getInt("rep_type"));
                report.setRepData(rs.getBytes("rep_data"));
                report.setRepFilename(rs.getString("rep_filename"));
                report.setRepName(rs.getString("rep_name"));
                report.setRepCode(rs.getString("rep_code"));
                report.setRepID(rs.getInt("id"));

                if (report.isComposite()) {
                    List<DbReportInfo> children = getCompositeReports(report.getRepID());
                    for (DbReportInfo child : children) report.addChild(child);
                }
                result.add(report);
            }
        } catch (SQLException e) {
            System.err.println("PostgreSQL ошибка при получении отчетов по unit=" + unitCode + ": " + e.getMessage());
        }
        return result;
    }

    private List<DbReportInfo> getCompositeReports(int parentReportId) {
        List<DbReportInfo> result = new ArrayList<>();
        String sql = "SELECT rep.id, rep.rep_code, rep.rep_name, rep.rep_type, rep.rep_filename, drl.priv_name " +
                "FROM d_reports_structure t JOIN d_reports rep ON rep.id = t.subreport " +
                "LEFT JOIN d_reports_links drl ON drl.pid = rep.id " +
                "WHERE t.pid = ? ORDER BY t.sort";

        try (Connection conn = getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parentReportId);
            pstmt.setQueryTimeout(30);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DbReportInfo report = new DbReportInfo();
                report.setRepID(rs.getInt("id"));
                report.setRepCode(rs.getString("rep_code"));
                report.setRepName(rs.getString("rep_name"));
                report.setRepType(rs.getInt("rep_type"));
                report.setRepFilename(rs.getString("rep_filename"));
                report.setPrivName(rs.getString("priv_name"));

                if (report.isComposite()) {
                    List<DbReportInfo> children = getCompositeReports(report.getRepID());
                    for (DbReportInfo child : children) report.addChild(child);
                }
                result.add(report);
            }
        } catch (SQLException e) {
            System.err.println("PostgreSQL ошибка при получении составных отчетов для ID=" + parentReportId + ": " + e.getMessage());
        }
        return result;
    }

    private Connection getPostgresConnection() throws SQLException {
        return DatabaseConnector.getPostgresConnectionWithContext(
                settings.getPostgresUrl(),
                settings.getPostgresUser(),
                settings.getPostgresPassword(),
                settings.getMisUser()
        );
    }

    public static List<String> formatReportsForDisplay(List<DbReportInfo> reports,
                                                       String autoPopupName,
                                                       String prefix,
                                                       boolean isLastList) {
        if (reports == null || reports.isEmpty()) return Collections.emptyList();

        int maxTypeLen = 0, maxCodeLen = 0;
        for (DbReportInfo report : reports) {
            String typeName = report.getRepTypeName();
            if (typeName.length() > maxTypeLen) maxTypeLen = typeName.length();
            String code = report.getRepCode();
            if (code != null && code.length() > maxCodeLen) maxCodeLen = code.length();
        }

        String autoPopupPrefix = "(AutoPopup \"" + autoPopupName + "\") ";
        List<String> result = new ArrayList<>();

        for (int i = 0; i < reports.size(); i++) {
            DbReportInfo report = reports.get(i);
            boolean isLast = (i == reports.size() - 1);
            String line;
            if (prefix.isEmpty()) {
                // Корневой уровень
                String connector = isLast ? "└── " : "├── ";
                String formatted = formatReportLine(report, maxTypeLen, maxCodeLen);
                line = "    " + connector + autoPopupPrefix + formatted;
            } else {
                String connector = isLast ? "└── " : "├── ";
                String formatted = formatShortReportLine(report, maxTypeLen, maxCodeLen);
                line = prefix + connector + formatted;
            }
            result.add(line);

            if (report.hasChildren()) {
                String childPrefix = prefix.isEmpty() ? (isLast ? "    " : "    │   ") : prefix + (isLast ? "    " : "    │   "); // ???
                List<String> childrenLines = formatReportsForDisplay(report.getChildren(), autoPopupName, childPrefix, isLast);
                result.addAll(childrenLines);
            }
        }
        return result;
    }

    private static String formatReportLine(DbReportInfo report, int maxTypeLen, int maxCodeLen) {
        String typeName = report.getRepTypeName();
        String code = report.getRepCode() != null ? report.getRepCode() : "?";
        String name = report.getRepName() != null ? report.getRepName() : "без названия";
        String formPath = report.getFormPath();
        StringBuilder sb = new StringBuilder();
        sb.append("- REP_TYPE=\"").append(typeName).append("\"");
        int typeSpaces = maxTypeLen - typeName.length();
        if (typeSpaces > 0) sb.append(" ".repeat(typeSpaces));
        sb.append(" - REP_CODE=\"").append(code).append("\"");
        int codeSpaces = maxCodeLen - code.length();
        if (codeSpaces > 0) sb.append(" ".repeat(codeSpaces));
        sb.append(" \"").append(name).append("\"");
        if (formPath != null) sb.append(" Form=\"").append(formPath).append("\"");
        return sb.toString();
    }

    private static String formatShortReportLine(DbReportInfo report, int maxTypeLen, int maxCodeLen) {
        String typeName = report.getRepTypeName();
        String code = report.getRepCode() != null ? report.getRepCode() : "?";
        String name = report.getRepName() != null ? report.getRepName() : "без названия";
        String formPath = report.getFormPath();
        StringBuilder sb = new StringBuilder();
        sb.append("- REP_TYPE=\"").append(typeName).append("\"");
        int typeSpaces = maxTypeLen - typeName.length();
        if (typeSpaces > 0) sb.append(" ".repeat(typeSpaces));
        sb.append(" - REP_CODE=\"").append(code).append("\"");
        int codeSpaces = maxCodeLen - code.length();
        if (codeSpaces > 0) sb.append(" ".repeat(codeSpaces));
        sb.append(" \"").append(name).append("\"");
        if (formPath != null) sb.append(" Form=\"").append(formPath).append("\"");
        return sb.toString();
    }

}