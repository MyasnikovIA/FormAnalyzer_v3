package ru.tmis.analyzer.core.db;

import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.cache.OracleDataCache;
import ru.tmis.analyzer.core.model.DbReportInfo;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReportsFromDbService {

    private final SettingsModel settings;
    private final OracleDataCache dataCache;
    private final AtomicBoolean oracleAvailable = new AtomicBoolean(true);
    private long lastFailureTime = 0;
    private static final long RETRY_DELAY_MS = 30000; // 30 секунд до повторной попытки

    public ReportsFromDbService(SettingsModel settings) {
        this.settings = settings;
        this.dataCache = OracleDataCache.getInstance();
        // Проверяем доступность Oracle через DatabaseCacheManager
        this.oracleAvailable.set(ru.tmis.analyzer.core.cache.DatabaseCacheManager.isOracleAvailable());
    }

    /**
     * Получить список отчетов по unit'у (с использованием кэша)
     */
    public List<DbReportInfo> getReportsByUnit(String unitCode) {
        // Проверяем, нужно ли повторить попытку подключения
        if (!oracleAvailable.get()) {
            if (System.currentTimeMillis() - lastFailureTime > RETRY_DELAY_MS) {
                System.out.println("[ReportsFromDbService] Повторная проверка доступности Oracle...");
                oracleAvailable.set(ru.tmis.analyzer.core.cache.DatabaseCacheManager.isOracleAvailable());
                if (oracleAvailable.get()) {
                    System.out.println("[ReportsFromDbService] Oracle снова доступна!");
                }
            }
        }

        if (!oracleAvailable.get()) {
            System.out.println("[ReportsFromDbService] Oracle недоступна, возвращаем пустой список");
            return Collections.emptyList();
        }

        if (unitCode == null || unitCode.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Используем кэш
        return dataCache.getReportsByUnit(unitCode, () -> {
            System.out.println("[ReportsFromDbService] Реальный запрос к БД для unit=" + unitCode);
            return fetchReportsByUnitFromDb(unitCode);
        });
    }

    /**
     * Реальный запрос к БД для получения отчётов по unit
     */
    private List<DbReportInfo> fetchReportsByUnitFromDb(String unitCode) {
        List<DbReportInfo> result = new ArrayList<>();

        String sql =
                "SELECT rep.ID,\n" +
                        "       drl.PRIV_NAME,\n" +
                        "       rep.REP_TYPE,\n" +
                        "       rep.REP_DATA,\n" +
                        "       rep.REP_FILENAME,\n" +
                        "       rep.REP_NAME,\n" +
                        "       rep.REP_CODE,\n" +
                        "       rep.LPU\n" +
                        "  FROM D_REPORTS_LINKS drl\n" +
                        "  JOIN D_REPORTS rep ON drl.PID = rep.ID\n" +
                        " WHERE drl.UNITCODE = ?\n";

        try (Connection conn = DatabaseConnector.getOracleConnection(
                settings.getOracleUrl(),
                settings.getOracleUser(),
                settings.getOraclePassword());
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
                report.setRepName(rs.getString("REP_NAME"));
                report.setRepCode(rs.getString("REP_CODE"));
                report.setRepID(rs.getInt("ID"));

                String lpu = rs.getString("LPU");
                if (lpu != null) {
                    report.setUnitCode(lpu);
                }

                if (report.isComposite()) {
                    List<DbReportInfo> children = getCompositeReports(report.getRepID());
                    for (DbReportInfo child : children) {
                        report.addChild(child);
                    }
                }

                result.add(report);
            }

            System.out.println("[ReportsFromDbService] Всего найдено отчётов: " + result.size());

            // Если запрос успешен, сбрасываем флаг ошибки
            oracleAvailable.set(true);

        } catch (SQLException e) {
            System.err.println("[ReportsFromDbService] Ошибка получения отчетов по unit=" + unitCode + ": " + e.getMessage());

            // Не устанавливаем oracleAvailable=false при InterruptedException (остановка пользователем)
            if (!(e instanceof SQLException && e.getMessage() != null && e.getMessage().contains("Interrupted"))) {
                oracleAvailable.set(false);
                lastFailureTime = System.currentTimeMillis();
            }
        }

        return result;
    }

    /**
     * Загрузить составные части отчета (с использованием кэша)
     */
    public List<DbReportInfo> getCompositeReports(int parentReportId) {
        if (!oracleAvailable.get()) {
            System.out.println("[ReportsFromDbService] Oracle недоступна, возвращаем пустой список");
            return Collections.emptyList();
        }

        return dataCache.getCompositeReports(parentReportId, () -> {
            System.out.println("[ReportsFromDbService] Реальный запрос к БД для parentReportId=" + parentReportId);
            return fetchCompositeReportsFromDb(parentReportId);
        });
    }

    /**
     * Реальный запрос к БД для получения составных отчётов
     */
    private List<DbReportInfo> fetchCompositeReportsFromDb(int parentReportId) {
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

        try (Connection conn = DatabaseConnector.getOracleConnection(
                settings.getOracleUrl(),
                settings.getOracleUser(),
                settings.getOraclePassword());
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
            if (!(e instanceof SQLException && e.getMessage() != null && e.getMessage().contains("Interrupted"))) {
                oracleAvailable.set(false);
                lastFailureTime = System.currentTimeMillis();
            }
        }

        return result;
    }

    /**
     * Получить отчет по коду (с использованием кэша)
     */
    public DbReportInfo getReportByCode(String repCode) {
        if (!oracleAvailable.get()) {
            System.out.println("[ReportsFromDbService] Oracle недоступна, возвращаем null");
            DbReportInfo errorReport = new DbReportInfo();
            errorReport.setRepCode(repCode);
            errorReport.setRepName("Oracle недоступна");
            errorReport.setRepType(-1);
            return errorReport;
        }

        if (repCode == null || repCode.trim().isEmpty()) {
            return null;
        }

        return dataCache.getReportByCode(repCode, () -> {
            System.out.println("[ReportsFromDbService] Реальный запрос к БД для repCode=" + repCode);
            return fetchReportByCodeFromDb(repCode);
        });
    }

    /**
     * Реальный запрос к БД для получения отчёта по коду
     */
    private DbReportInfo fetchReportByCodeFromDb(String repCode) {
        String sql =
                "SELECT ID, LPU, REP_CODE, REP_NAME, REP_TYPE, REP_FILENAME, IS_SHARE " +
                        "FROM D_REPORTS WHERE REP_CODE = ? AND IS_SHARE = 1";

        try (Connection conn = DatabaseConnector.getOracleConnection(
                settings.getOracleUrl(),
                settings.getOracleUser(),
                settings.getOraclePassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, repCode);
            pstmt.setQueryTimeout(30);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                DbReportInfo report = new DbReportInfo();
                report.setRepID(rs.getInt("ID"));
                report.setRepCode(rs.getString("REP_CODE"));
                report.setRepName(rs.getString("REP_NAME"));
                report.setRepType(rs.getInt("REP_TYPE"));
                report.setRepFilename(rs.getString("REP_FILENAME"));
                report.setUnitCode(rs.getString("LPU"));
                return report;
            }
        } catch (SQLException e) {
            System.err.println("[ReportsFromDbService] Ошибка получения отчета по коду " + repCode + ": " + e.getMessage());
            if (!(e instanceof SQLException && e.getMessage() != null && e.getMessage().contains("Interrupted"))) {
                oracleAvailable.set(false);
                lastFailureTime = System.currentTimeMillis();
            }
        }
        return null;
    }

    // ==================== ФОРМАТИРОВАНИЕ ОТЧЕТОВ ====================

    /**
     * Форматирует список отчетов для отображения в виде дерева
     */
    public static List<String> formatReportsForDisplay(List<DbReportInfo> reports,
                                                       String autoPopupName,
                                                       String prefix,
                                                       boolean isLastList) {
        if (reports == null || reports.isEmpty()) {
            return Collections.emptyList();
        }

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
                String childPrefix;
                if (prefix.isEmpty()) {
                    childPrefix = "    " + (isLast ? "    " : "│   ");
                } else {
                    childPrefix = prefix + (isLast ? "    " : "│   ");
                }

                List<String> childrenLines = formatReportsForDisplay(
                        report.getChildren(), autoPopupName, childPrefix, isLast);
                result.addAll(childrenLines);
            }
        }
        return result;
    }

    private static String formatReportLine(DbReportInfo report, int maxTypeLen, int maxCodeLen) {
        String typeName = report.getRepTypeName();
        String code = report.getRepCode() != null ? report.getRepCode() : "?";
        String name = report.getRepName() != null ? report.getRepName() : "без названия";
        String privName = report.getPrivName() != null ? report.getPrivName() : "";
        String formPath = report.getFormPath();

        StringBuilder sb = new StringBuilder();
        sb.append("- REP_TYPE=\"").append(typeName).append("\"");
        int typeSpaces = maxTypeLen - typeName.length();
        if (typeSpaces > 0) sb.append(" ".repeat(typeSpaces));
        sb.append(" - REP_CODE=\"").append(code).append("\"");
        int codeSpaces = maxCodeLen - code.length();
        if (codeSpaces > 0) sb.append(" ".repeat(codeSpaces));
        sb.append(" \"").append(privName).append("\" (\"").append(name).append("\") ");
        if (formPath != null) {
            sb.append(" Form=\"").append(formPath).append("\"");
        }
        return sb.toString();
    }

    private static String formatShortReportLine(DbReportInfo report, int maxTypeLen, int maxCodeLen) {
        String typeName = report.getRepTypeName();
        String code = report.getRepCode() != null ? report.getRepCode() : "?";
        String name = report.getRepName() != null ? report.getRepName() : "без названия";
        String privName = report.getPrivName() != null ? report.getPrivName() : "";
        String formPath = report.getFormPath();

        StringBuilder sb = new StringBuilder();
        sb.append("- REP_TYPE=\"").append(typeName).append("\"");
        int typeSpaces = maxTypeLen - typeName.length();
        if (typeSpaces > 0) sb.append(" ".repeat(typeSpaces));
        sb.append(" - REP_CODE=\"").append(code).append("\"");
        int codeSpaces = maxCodeLen - code.length();
        if (codeSpaces > 0) sb.append(" ".repeat(codeSpaces));
        sb.append(" \"").append(privName).append("\" (\"").append(name).append("\") ");
        if (formPath != null) {
            sb.append(" Form=\"").append(formPath).append("\"");
        }
        return sb.toString();
    }

    /**
     * Очистить кэш отчётов
     */
    public static void clearReportByCodeCache() {
        OracleDataCache.getInstance().clearAll();
    }
}