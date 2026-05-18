// core/db/ReportsFromDbService.java
package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.model.DbReportInfo;
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
        // --Тип (по виду продукта): 0 - Crystal Reports; 1 - WEB-форма; 2 - Crystal Reports(PDF); 3 - Бланк; 5 - WEB-конструктор; 6 - Составной\n" +
        List<DbReportInfo> result = new ArrayList<>();

        if (unitCode == null || unitCode.trim().isEmpty()) {
            return result;
        }

        // Исправленный SQL запрос для Oracle
        String sql =
                "SELECT rep.ID,\n" +
                        "       drl.PRIV_NAME,\n" +
                        "       rep.REP_TYPE,\n" +
                        "       rep.REP_DATA,\n" +
                        "       rep.REP_FILENAME,\n" +
                        "       rep.REP_NAME,\n" +
                        "       rep.REP_CODE,\n" +
                        "       rep.LPU\n" +  // Добавляем LPU
                        "  FROM D_REPORTS_LINKS drl\n" +
                        "  JOIN D_REPORTS rep ON drl.PID = rep.ID\n" +
                        " WHERE drl.UNITCODE = ?\n";  // Добавляем сортировку

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

            System.out.println("[ReportsFromDbService] Загрузка отчётов для unit=" + unitCode);

            while (rs.next()) {
                DbReportInfo report = new DbReportInfo();
                report.setPrivName(rs.getString("PRIV_NAME"));
                report.setUnitCode(unitCode);
                report.setRepType(rs.getInt("REP_TYPE"));
                report.setRepData(rs.getBytes("REP_DATA"));
                report.setRepFilename(rs.getString("REP_FILENAME"));
                report.setRepName(rs.getString("REP_NAME") );
                report.setRepCode(rs.getString("REP_CODE"));
                report.setRepID(rs.getInt("ID"));

                // Устанавливаем LPU (если нужно для контекста)
                String lpu = rs.getString("LPU");
                if (lpu != null) {
                    report.setUnitCode(lpu); // или другое поле
                }

                // Если отчет составной, загружаем его структуру
                if (report.isComposite()) {
                    List<DbReportInfo> children = getCompositeReports(report.getRepID());
                    for (DbReportInfo child : children) {
                        report.addChild(child);
                    }
                }

                result.add(report);
                System.out.println("  [ReportsFromDbService] Найден отчёт: " + report.getRepCode() + " - " + report.getRepName());
            }

            System.out.println("[ReportsFromDbService] Всего найдено отчётов: " + result.size());

        } catch (SQLException e) {
            System.err.println("[ReportsFromDbService] Ошибка получения отчетов по unit=" + unitCode + ": " + e.getMessage());
            e.printStackTrace();
        }

        return result;
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

        // Вычисляем максимальную длину REP_TYPE и REP_CODE для текущего уровня
        int maxTypeLen = 0;
        int maxCodeLen = 0;
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
                // Корневой уровень: используем └── для последнего, иначе ├──
                String connector = isLast ? "└── " : "├── ";
                String formatted = formatReportLine(report, maxTypeLen, maxCodeLen);
                line = "    " + connector + autoPopupPrefix + formatted;
            } else {
                // Вложенный уровень: prefix уже содержит отступ и вертикальные линии
                String connector = isLast ? "└── " : "├── ";
                String formatted = formatShortReportLine(report, maxTypeLen, maxCodeLen);
                line = prefix + connector + formatted;
            }
            result.add(line);

            if (report.hasChildren()) {
                // Рассчитываем отступ для детей (как раньше)
                String childPrefix;
                if (prefix.isEmpty()) {
                    childPrefix = "    " + (isLast ? "    " : "│   ");
                } else {
                    childPrefix = prefix + (isLast ? "    " : "│   ");
                }

                // Выравнивание для составных отчётов (под символ '=')
                if (report.isComposite()) {
                    int equalPos = -1;
                    int idx = line.indexOf("REP_TYPE=\"");
                    if (idx >= 0) {
                        idx = line.indexOf('=', idx);
                        if (idx >= 0) equalPos = idx;
                    }
                    if (equalPos >= 0) {
                        int needed = equalPos - childPrefix.length();
                        if (needed > 0) childPrefix = childPrefix + " ".repeat(needed);
                    } else {
                        int repTypeIndex = line.indexOf("REP_TYPE=");
                        if (repTypeIndex > 0) {
                            int needed = repTypeIndex - childPrefix.length();
                            if (needed > 0) childPrefix = childPrefix + " ".repeat(needed);
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

    /**
     * Форматирует строку для корневого отчёта (с AutoPopupPrefix)
     * Выравнивает REP_TYPE, REP_CODE и название
     */
    private static String formatReportLine(DbReportInfo report, int maxTypeLen, int maxCodeLen) {
        String typeName = report.getRepTypeName();
        String code = report.getRepCode() != null ? report.getRepCode() : "?";
        String name = report.getRepName() != null ? report.getRepName() : "без названия";
        String privName = report.getPrivName() != null ? report.getPrivName() : "";
        String formPath = report.getFormPath();

        StringBuilder sb = new StringBuilder();
        sb.append("- REP_TYPE=\"");
        sb.append(typeName);
        sb.append("\"");
        // Добавляем пробелы до maxTypeLen + 2 (кавычки уже есть)
        int typeSpaces = maxTypeLen - typeName.length();
        if (typeSpaces > 0) sb.append(" ".repeat(typeSpaces));

        sb.append(" - REP_CODE=\"");
        sb.append(code);
        sb.append("\"");
        int codeSpaces = maxCodeLen - code.length();
        if (codeSpaces > 0) sb.append(" ".repeat(codeSpaces));
        sb.append(" \"").append(privName).append("\" (\"").append(name).append("\") ").append(" ");
        if (formPath != null) {
            sb.append(" Form=\"").append(formPath).append("\"");
        }
        return sb.toString();
    }

    /**
     * Форматирует строку для вложенного отчёта (без AutoPopupPrefix)
     * Выравнивает REP_TYPE, REP_CODE и название
     */
    private static String formatShortReportLine(DbReportInfo report, int maxTypeLen, int maxCodeLen) {
        String typeName = report.getRepTypeName();
        String code = report.getRepCode() != null ? report.getRepCode() : "?";
        String name = report.getRepName() != null ? report.getRepName() : "без названия";
        String privName = report.getPrivName() != null ? report.getPrivName() : "";
        String formPath = report.getFormPath();

        StringBuilder sb = new StringBuilder();
        sb.append("- REP_TYPE=\"");
        sb.append(typeName);
        sb.append("\"");
        int typeSpaces = maxTypeLen - typeName.length();
        if (typeSpaces > 0) sb.append(" ".repeat(typeSpaces));

        sb.append(" - REP_CODE=\"");
        sb.append(code);
        sb.append("\"");
        int codeSpaces = maxCodeLen - code.length();
        if (codeSpaces > 0) sb.append(" ".repeat(codeSpaces));

        sb.append(" \"").append(privName).append("\" (\"").append(name).append("\") ").append(" ");
        if (formPath != null) {
            sb.append(" Form=\"").append(formPath).append("\"");
        }
        return sb.toString();
    }


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
}