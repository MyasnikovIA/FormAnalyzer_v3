// core/db/ReportsFromDbService.java
package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.model.PopupMenuInfo;

import java.io.PrintWriter;
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
    public List<DbReportInfo> getReportsByUnit(String unitCode) {
        List<DbReportInfo> result = new ArrayList<>();

        if (unitCode == null || unitCode.trim().isEmpty()) {
            return result;
        }

        String sql = "SELECT rep.ID,\n" +
                     "       drl.PRIV_NAME,\n" +
                     "       rep.REP_TYPE, --Тип (по виду продукта): 0 - Crystal Reports; 1 - WEB-форма; 2 - Crystal Reports(PDF); 3 - Бланк; 5 - WEB-конструктор; 6 - Составной\n" +
                     "       rep.REP_DATA,\n" +
                     "       rep.REP_FILENAME ,\n" +
                     "       rep.REP_NAME,\n" +
                     "       rep.REP_CODE\n" +
                     "  from D_REPORTS_LINKS drl\n" +
                     "       join d_reports rep on drl.pid = rep.id\n" +
                     "  where drl.unitcode = ?\n";
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
                report.setUnitCode(unitCode);
                report.setRepType(rs.getInt("REP_TYPE"));
                report.setRepData(rs.getBytes("REP_DATA"));
                report.setRepFilename(rs.getString("REP_FILENAME"));
                report.setRepName(rs.getString("REP_NAME"));
                report.setRepCode(rs.getString("REP_CODE"));
                report.setRepID(rs.getInt("ID"));
                // Если отчет составной, загружаем его структуру
                if (report.isComposite()) {
                     int tmp = report.getRepID();
                    List<DbReportInfo> children = getCompositeReports(report.getRepID());
                    for (DbReportInfo child : children) {
                        report.addChild(child);
                    }
                }

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
        private List<DbReportInfo> children;

        public DbReportInfo() {
            this.children = new ArrayList<>();
        }

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

        public List<DbReportInfo> getChildren() { return children; }
        public void setChildren(List<DbReportInfo> children) { this.children = children; }
        public void addChild(DbReportInfo child) { this.children.add(child); }
        public boolean hasChildren() { return !children.isEmpty(); }
        public boolean isComposite() { return repType == 6; }


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
            sb.append("') - REP_TYPE=\"");
            sb.append(getRepTypeName());
            sb.append("\" - REP_CODE=\"");
            sb.append(repCode != null && !repCode.isEmpty() ? repCode : "?");
            sb.append("\" \"");
            sb.append(repName != null && !repName.isEmpty() ? repName : "без названия");
            sb.append("\"");
            if (getFormPath() != null) {
                sb.append(" Form=\"").append(getFormPath()).append("\"");
            }
            return sb.toString();
        }
        public String getShortDisplayString() {
            StringBuilder sb = new StringBuilder();
            sb.append("- REP_TYPE=\"");
            sb.append(getRepTypeName());
            sb.append("\" - REP_CODE=\"");
            sb.append(repCode != null && !repCode.isEmpty() ? repCode : "?");
            sb.append("\" \"");
            sb.append(repName != null && !repName.isEmpty() ? repName : "без названия");
            sb.append("\"");
            if (getFormPath() != null) {
                sb.append(" Form=\"").append(getFormPath()).append("\"");
            }
            return sb.toString();
        }
    }

    /**
     * Форматированный вывод списка отчетов с правильной иерархической версткой.
     * Возвращает строки, уже содержащие все символы дерева (├──, └──, │) и отступы.
     * Эти строки можно выводить напрямую, без дополнительных префиксов.
     */
    public static List<String> formatReportsForDisplay(List<DbReportInfo> reports,
                                                       String autoPopupName,
                                                       String prefix,
                                                       boolean isLastList) {
        if (reports == null || reports.isEmpty()) {
            return Collections.emptyList();
        }

        String autoPopupPrefix = "(AutoPopup \"" + autoPopupName + "\") ";
        List<String> result = new ArrayList<>();

        for (int i = 0; i < reports.size(); i++) {
            DbReportInfo report = reports.get(i);
            boolean isLast = (i == reports.size() - 1);

            String line;
            if (prefix.isEmpty()) {
                // Корневой уровень: только символ ветки, без начальных пробелов
                line = "├── " + autoPopupPrefix + report.getDisplayString();
            } else {
                // Вложенный уровень: prefix уже содержит отступ (пробелы и вертикальные линии)
                String connector = isLast ? "└── " : "├── ";
                line = prefix + connector + report.getShortDisplayString();
            }
            result.add(line);

            if (report.hasChildren()) {
                // Рассчитываем отступ для детей
                String childPrefix;
                if (prefix.isEmpty()) {
                    childPrefix = "    ";  // для корневого уровня дети будут с отступом 4 пробела
                } else {
                    childPrefix = prefix + (isLast ? "    " : "│   ");
                }

                // Для составного отчета добавляем выравнивание до позиции (AutoPopup ...
                if (report.isComposite() && !prefix.isEmpty()) {
                    int autoPopupIndex = line.indexOf(autoPopupPrefix);
                    if (autoPopupIndex >= 0) {
                        int needed = autoPopupIndex - childPrefix.length();
                        if (needed > 0) {
                            childPrefix = childPrefix + " ".repeat(needed);
                        }
                    } else {
                        int repTypeIndex = line.indexOf("REP_TYPE=");
                        if (repTypeIndex > 0) {
                            int needed = repTypeIndex - childPrefix.length();
                            if (needed > 0) {
                                childPrefix = childPrefix + " ".repeat(needed);
                            }
                        }
                    }
                }

                List<String> childrenLines = formatReportsForDisplay(
                        report.getChildren(), autoPopupName, childPrefix, isLast);
                result.addAll(childrenLines);
            }
        }
        return result;
    }


    private void writeMenuTree(PrintWriter writer, List<PopupMenuInfo.MenuItem> items, String indent) {
        for (int i = 0; i < items.size(); i++) {
            PopupMenuInfo.MenuItem item = items.get(i);
            boolean isLast = (i == items.size() - 1);

            String branch = isLast ? "└── " : "├── ";
            String childIndent = indent + (isLast ? "    " : "│   ");

            if (item.isDbReport()) {
                // Для отчётов из БД caption уже содержит полную строку с деревом
                // Выводим как есть, без добавления indent и branch
                writer.println(indent + item.getCaption());
            } else {
                String displayText = item.getPrefix() + item.getDisplayCaption();
                writer.println(indent + branch + displayText);
            }

            if (item.hasChildren()) {
                writeMenuTree(writer, item.getChildren(), childIndent);
            }
        }
    }

    /**
     * Вспомогательный класс для хранения данных строки
     */
    private static class ReportRowData {
        String autoPopupPart;
        String unitPart;
        String typePart;
        String codePart;
        String namePart;
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
    /**
     * Загрузить составные части отчета (для REP_TYPE = 6)
     * @param parentReportId ID родительского отчета
     * @return список вложенных отчетов
     */
    /**
     * Загрузить составные части отчета (для REP_TYPE = 6) с рекурсивным обходом
     * @param parentReportId ID родительского отчета
     * @return список вложенных отчетов
     */
    public List<DbReportInfo> getCompositeReports(int parentReportId) {
        List<DbReportInfo> result = new ArrayList<>();

        String sql =
                "SELECT rep.ID, " +
                        "       rep.REP_CODE, " +
                        "       rep.REP_NAME, " +
                        "       rep.REP_TYPE, " +
                        "       rep.REP_FILENAME, " +
                        "       rep.LPU, " +
                        "       drl.PRIV_NAME " +
                        "FROM D_REPORTS_STRUCTURE t " +
                        "JOIN D_REPORTS rep ON rep.ID = t.SUBREPORT " +
                        "LEFT JOIN D_REPORTS_LINKS drl ON drl.PID = rep.ID " +
                        "WHERE t.PID = ? " +
                        "ORDER BY t.SORT";

        Properties props = new Properties();
        props.setProperty("user", settings.getOracleUser());
        props.setProperty("password", settings.getOraclePassword());
        props.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
        props.setProperty("oracle.jdbc.ReadTimeout", "30000");
        props.setProperty("oracle.jdbc.defaultNChar", "true");

        try (Connection conn = DriverManager.getConnection(settings.getOracleUrl(), props);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, parentReportId);
            pstmt.setQueryTimeout(30);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DbReportInfo report = new DbReportInfo();
                report.setRepID(rs.getInt("ID"));
                report.setRepCode(rs.getString("REP_CODE"));
                report.setRepName(rs.getString("REP_NAME"));
                report.setRepType(rs.getInt("REP_TYPE"));
                report.setRepFilename(rs.getString("REP_FILENAME"));
                report.setPrivName(rs.getString("PRIV_NAME"));
                report.setUnitCode(rs.getString("LPU") != null ? String.valueOf(rs.getInt("LPU")) : null);

                // Рекурсивно загружаем дочерние отчеты, если текущий тоже составной
                if (report.isComposite()) {
                    List<DbReportInfo> children = getCompositeReports(report.getRepID());
                    for (DbReportInfo child : children) {
                        report.addChild(child);
                    }
                }

                result.add(report);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения составных отчетов для ID=" + parentReportId + ": " + e.getMessage());
        }

        return result;
    }
    /**
     * Рекурсивное форматирование дерева отчетов
     */
    public static List<String> formatReportTree(List<DbReportInfo> reports,
                                                String autoPopupName,
                                                String indent,
                                                boolean isLastList) {
        List<String> result = new ArrayList<>();

        String autoPopupPrefix = "(AutoPopup \"" + autoPopupName + "\") ";

        for (int i = 0; i < reports.size(); i++) {
            DbReportInfo report = reports.get(i);
            boolean isLast = (i == reports.size() - 1);

            // Формируем строку отчета
            String line;
            if (indent.isEmpty()) {
                // Корневой уровень
                line = autoPopupPrefix + report.getDisplayString();
            } else {
                // Вложенный уровень
                line = report.getShortDisplayString();
            }

            result.add(line);

            // Рекурсивно обрабатываем дочерние отчеты
            if (report.hasChildren()) {
                String childIndent = indent + (isLast ? "    " : "│   ");
                List<String> childLines = formatReportTree(
                        report.getChildren(), autoPopupName, childIndent, isLast);
                for (String childLine : childLines) {
                    result.add(childIndent + "├── " + childLine);
                }
            }
        }

        return result;
    }
}