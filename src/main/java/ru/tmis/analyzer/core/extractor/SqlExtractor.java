// core/extractor/SqlExtractor.java
package ru.tmis.analyzer.core.extractor;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.tmis.analyzer.core.model.FormInfo;
import ru.tmis.analyzer.core.model.SqlInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Извлекает SQL запросы из XML компонентов форм и парсит все объекты БД:
 * - Таблицы и представления (D_*, D_V_*)
 * - Пакетные функции (D_PKG_*.*)
 * - Константы (D_PKG_CONSTANTS.SEARCH_*)
 * - Системные опции (D_PKG_OPTIONS.GET)
 * - Пользовательские процедуры (D_V_USERPROCS)
 * - Неизвестные объекты (требующие ручного разбора)
 */
public class SqlExtractor {


    // Паттерн для поиска CTE (WITH ... AS)
    private static final Pattern CTE_PATTERN = Pattern.compile(
            "\\bWITH\\s+([A-Za-z0-9_]+)\\s+AS\\s*\\(",
            Pattern.CASE_INSENSITIVE
    );

    // Паттерн для поиска псевдонимов таблиц (после FROM/JOIN)
    private static final Pattern TABLE_ALIAS_PATTERN = Pattern.compile(
            "\\b(?:FROM|JOIN)\\s+[A-Za-z0-9_.]+\\s+([A-Za-z0-9_]+)\\b",
            Pattern.CASE_INSENSITIVE
    );


    // Паттерн для поиска псевдонимов колонок в SELECT
    private static final Pattern COLUMN_ALIAS_PATTERN = Pattern.compile(
            "\\bAS\\s+([A-Za-z0-9_]+)\\b|\\b([A-Za-z0-9_]+)\\s+(?=FROM|WHERE|GROUP|ORDER|HAVING|UNION|INTERSECT|MINUS|$)",
            Pattern.CASE_INSENSITIVE
    );


    private static final Pattern CDATA_PATTERN = Pattern.compile("<!\\[CDATA\\[(.*?)\\]\\]>", Pattern.DOTALL);

    /** Таблицы и представления: D_V_XXX или D_XXX */
    private static final Pattern TABLE_VIEW_PATTERN = Pattern.compile(
            "\\b(D_V_[A-Z0-9_]+|D_[A-Z][A-Z0-9_]*)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /** Поиск таблиц/вьюх по контексту FROM/JOIN */
    private static final Pattern TABLE_IN_FROM_JOIN_PATTERN = Pattern.compile(
            "\\b(?:FROM|JOIN|INTO|USING)\\s+([A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)?)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /** Пакетные функции: D_PKG_XXXX.YYYY */
    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
            "\\b(D_PKG_[A-Z0-9_]+\\.[A-Z0-9_]+)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /** Константы: D_PKG_CONSTANTS.SEARCH_*('NAME', ...) */
    private static final Pattern CONSTANT_PATTERN = Pattern.compile(
            "D_PKG_CONSTANTS\\.SEARCH_(?:STR|NUM|DATE)\\s*\\(\\s*(?:psCONST_CODE\\s*=>\\s*)?'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Системные опции: D_PKG_OPTIONS.GET('NAME', ...) */
    private static final Pattern OPTION_PATTERN = Pattern.compile(
            "D_PKG_OPTIONS\\.GET\\s*\\(\\s*(?:psSO_CODE\\s*=>\\s*)?'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Пользовательские процедуры: D_V_USERPROCS ... PR_CODE = 'CODE' */
    private static final Pattern USERPROC_PATTERN = Pattern.compile(
            "D_V_USERPROCS\\s+.*?PR_CODE\\s*=\\s*'([^']+)'",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Вьюхи в вызовах функций: D_PKG_XXX.YYYY(... D_V_ZZZ ...) */
    private static final Pattern VIEW_IN_FUNCTION_PATTERN = Pattern.compile(
            "D_PKG_[A-Z_]+\\.[A-Z_]+\\([^)]*\\b(D_V_[A-Z0-9_]+)\\b",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Вьюхи после FROM/JOIN */
    private static final Pattern FROM_JOIN_VIEW_PATTERN = Pattern.compile(
            "(?:FROM|JOIN)\\s+(D_V_[A-Z0-9_]+)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Неизвестные объекты: D_XXX (не PKG, не V_) */
    private static final Pattern UNKNOWN_OBJECT_PATTERN = Pattern.compile(
            "\\b(D_(?!PKG_|V_)[A-Z][A-Z0-9_]*)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // SQL ключевые слова для исключения
    private static final Set<String> SQL_KEYWORDS = Set.of(
            "SELECT", "FROM", "WHERE", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER",
            "ON", "AND", "OR", "NOT", "IN", "EXISTS", "AS", "BY", "ORDER", "GROUP",
            "HAVING", "UNION", "INTERSECT", "MINUS", "WITH", "RECURSIVE",
            "SYSDATE", "TO_DATE", "TO_CHAR", "TRUNC", "NVL", "COALESCE", "CASE",
            "WHEN", "THEN", "ELSE", "END", "COUNT", "SUM", "AVG", "MAX", "MIN",
            "ROW_NUMBER", "RANK", "DENSE_RANK", "LAG", "LEAD"
    );

    // Системные объекты Oracle/SQL, которые не являются таблицами пользователя
    private static final Set<String> SYSTEM_OBJECTS = Set.of(
            "DUAL",      // Системная таблица Oracle
            "TABLE",     // Оператор TABLE для работы с коллекциями
            "SYS_DUMMY"  // Системная
    );


    /**
     * Извлечь все SQL запросы из документа
     */
    public List<SqlInfo> extract(Document doc, FormInfo formInfo) {
        List<SqlInfo> result = new ArrayList<>();

        result.addAll(extractFromElements(doc.select("cmpDataSet"), "D3 DataSet", formInfo));
        result.addAll(extractFromElements(doc.select("cmpAction"), "D3 Action", formInfo));
        result.addAll(extractFromElements(doc.select("component[cmptype=DataSet]"), "M2 DataSet", formInfo));
        result.addAll(extractFromElements(doc.select("component[cmptype=Action]"), "M2 Action", formInfo));

        return result;
    }

    // ==================== ПРИВАТНЫЕ МЕТОДЫ ====================

    /**
     * Извлечь SQL из группы однотипных элементов
     */
    private List<SqlInfo> extractFromElements(Elements elements, String type, FormInfo formInfo) {
        List<SqlInfo> result = new ArrayList<>();

        for (Element element : elements) {
            String name = element.attr("name");
            if (name == null || name.isEmpty()) continue;

            String sqlContent = extractSqlContent(element);
            if (sqlContent == null || sqlContent.trim().isEmpty()) continue;
            if (!isSqlContent(sqlContent)) continue;

            SqlInfo sqlInfo = createSqlInfo(type, name, element, sqlContent, formInfo);
            parseSqlContent(sqlContent, sqlInfo);

            result.add(sqlInfo);
        }

        return result;
    }

    /**
     * Создать объект SqlInfo с базовой информацией
     */
    private SqlInfo createSqlInfo(String type, String name, Element element, String sqlContent, FormInfo formInfo) {
        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.setSourceType(type);
        sqlInfo.setSourcePath(formInfo.getFormPath());
        sqlInfo.setBaseFormPath(formInfo.getBaseFormPath());
        sqlInfo.setComponentName(name);
        sqlInfo.setSqlContent(element.outerHtml());
        sqlInfo.setCleanSql(cleanSql(sqlContent));
        return sqlInfo;
    }

    /**
     * Разобрать SQL содержимое и извлечь все типы объектов
     */
    private void parseSqlContent(String sql, SqlInfo sqlInfo) {
        String upperSql = sql.toUpperCase();
        String originalSql = sql;

        // 1. Пакетные функции (D_PKG_XXXX.YYYY) - определяются по имени
        extractPackages(upperSql, sqlInfo);

        // 2. Константы (D_PKG_CONSTANTS.SEARCH_*)
        extractConstants(originalSql, sqlInfo);

        // 3. Системные опции (D_PKG_OPTIONS.GET)
        extractOptions(originalSql, sqlInfo);

        // 4. Пользовательские процедуры (D_V_USERPROCS)
        extractUserProcedures(originalSql, sqlInfo);

        // 5. ТАБЛИЦЫ И ВЬЮХИ - определяются по контексту FROM/JOIN
        extractTablesAndViewsByContext(upperSql, sqlInfo);

        // 6. Все остальные D_* объекты - в UNKNOWN
        extractUnknownObjects(upperSql, sqlInfo);

        // 7.
        extractSystemOptions(sql, sqlInfo);
    }

    /**
     * Извлечение таблиц и представлений по контексту FROM/JOIN
     */
    private void extractTablesAndViewsByContext(String upperSql, SqlInfo sqlInfo) {
        // 1. Извлекаем CTE
        Set<String> cteNames = extractCTENames(upperSql);

        // 2. Извлекаем псевдонимы таблиц
        Set<String> tableAliases = extractTableAliases(upperSql);

        if (!cteNames.isEmpty()) {
            System.out.println("      [DEBUG] Найдены CTE: " + cteNames);
        }
        if (!tableAliases.isEmpty()) {
            System.out.println("      [DEBUG] Найдены псевдонимы таблиц: " + tableAliases);
        }

        Matcher matcher = TABLE_IN_FROM_JOIN_PATTERN.matcher(upperSql);
        while (matcher.find()) {
            String name = matcher.group(1);

            // Убираем схему если есть (например, "DEV.D_TABLE" -> "D_TABLE")
            if (name.contains(".")) {
                name = name.substring(name.lastIndexOf(".") + 1);
            }

            // Проверяем, является ли это реальным объектом БД
            if (isRealDatabaseObject(name, upperSql, cteNames, tableAliases)) {
                sqlInfo.addTableView(name);
                System.out.println("      [DEBUG] Добавлена таблица/вьюха: " + name);
            } else {
                System.out.println("      [SKIP] Пропущен объект: " + name);
            }
        }
    }


    /**
     * Извлечение пакетных функций (D_PKG_XXXX.YYYY)
     */
    private void extractPackages(String upperSql, SqlInfo sqlInfo) {
        Matcher matcher = PACKAGE_PATTERN.matcher(upperSql);
        while (matcher.find()) {
            String name = matcher.group(1);

            // Исключаем константы и опции (они обрабатываются отдельно)
            if (name.contains("D_PKG_CONSTANTS") ||
                    name.contains("D_PKG_OPTIONS") ||
                    name.contains("D_PKG_OPTION_SPECS")) {
                continue;
            }

            sqlInfo.addPackageFunction(name);
        }
    }

    /**
     * Извлечение констант (D_PKG_CONSTANTS.SEARCH_*)
     */
    private void extractConstants(String sql, SqlInfo sqlInfo) {
        Matcher matcher = CONSTANT_PATTERN.matcher(sql);
        while (matcher.find()) {
            String constant = matcher.group(1);
            if (constant != null && !constant.isEmpty()) {
                sqlInfo.addConstant(constant);
            }
        }
    }

    /**
     * Извлечение системных опций (D_PKG_OPTIONS.GET)
     */
    private void extractOptions(String sql, SqlInfo sqlInfo) {
        Matcher matcher = OPTION_PATTERN.matcher(sql);
        while (matcher.find()) {
            String option = matcher.group(1);
            if (option != null && !option.isEmpty()) {
                sqlInfo.addSystemOption(option);
            }
        }
    }

    /**
     * Извлечение пользовательских процедур (D_V_USERPROCS)
     */
    private void extractUserProcedures(String sql, SqlInfo sqlInfo) {
        Matcher matcher = USERPROC_PATTERN.matcher(sql);
        while (matcher.find()) {
            String prCode = matcher.group(1);
            if (prCode != null && !prCode.isEmpty()) {
                sqlInfo.addUserProcedure(prCode);
                System.out.println("[DEBUG] Найдена пользовательская процедура: " + prCode);
            }
        }
    }

    /**
     * Извлечение дополнительных вьюх (в вызовах функций и после FROM/JOIN)
     */
    private void extractAdditionalViews(String upperSql, SqlInfo sqlInfo) {
        // Вьюхи в вызовах функций
        Matcher funcMatcher = VIEW_IN_FUNCTION_PATTERN.matcher(upperSql);
        while (funcMatcher.find()) {
            sqlInfo.addTableView(funcMatcher.group(1));
        }

        // Вьюхи после FROM/JOIN
        Matcher fromMatcher = FROM_JOIN_VIEW_PATTERN.matcher(upperSql);
        while (fromMatcher.find()) {
            sqlInfo.addTableView(fromMatcher.group(1));
        }
    }

    /**
     * Извлечение неизвестных объектов
     * Сюда попадают все D_* объекты, которые:
     * - Не являются пакетами (уже обработано)
     * - Не являются таблицами/вьюхами (не найдены в FROM/JOIN)
     * - Встречаются в SELECT, WHERE, CASE, вызовах функций и т.д.
     */
    private void extractUnknownObjects(String upperSql, SqlInfo sqlInfo) {
        Set<String> cteNames = extractCTENames(upperSql);
        Set<String> tableAliases = extractTableAliases(upperSql);

        Matcher matcher = UNKNOWN_OBJECT_PATTERN.matcher(upperSql);
        while (matcher.find()) {
            String name = matcher.group(1);

            // Пропускаем SQL ключевые слова
            if (SQL_KEYWORDS.contains(name)) continue;

            // Пропускаем системные объекты
            if (SYSTEM_OBJECTS.contains(name)) continue;

            // Пропускаем CTE
            if (cteNames.contains(name)) continue;

            // Пропускаем псевдонимы таблиц
            if (tableAliases.contains(name)) continue;

            // Если это уже известная таблица/вьюха - пропускаем
            if (sqlInfo.getTablesViews().contains(name)) continue;

            // Если это уже известный пакет - пропускаем
            if (sqlInfo.getPackagesFunctions().contains(name)) continue;

            // Имена без префикса D_ не добавляем в unknown (это псевдонимы полей)
            if (!name.startsWith("D_")) {
                System.out.println("      [SKIP] Пропущен в unknown (нет префикса D_): " + name);
                continue;
            }

            sqlInfo.addUnknownObject(name);
        }
    }



    /**
     * Извлечь SQL содержимое из элемента (поддерживает CDATA и прямые тексты)
     */
    private String extractSqlContent(Element element) {
        String html = element.html();
        Matcher matcher = CDATA_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return cleanXmlBody(html);
    }

    /**
     * Очистить тело от XML тегов
     */
    private String cleanXmlBody(String body) {
        if (body == null) return null;
        return body.replaceAll("<[^>]+>", "").trim();
    }

    /**
     * Проверить, является ли содержимое SQL запросом
     */
    private boolean isSqlContent(String content) {
        if (content == null) return false;
        String lower = content.toLowerCase().trim();
        return lower.startsWith("select") || lower.startsWith("insert") ||
                lower.startsWith("update") || lower.startsWith("delete") ||
                lower.startsWith("begin") || lower.startsWith("with") ||
                lower.startsWith("declare");
    }

    /**
     * Очистить SQL от лишних пробелов
     */
    private String cleanSql(String sql) {
        if (sql == null) return "";
        return sql.replaceAll("\\s+", " ").trim();
    }

    /**
     * Проверка, является ли имя реальной таблицей/вьюхой
     * Признаки:
     * 1. Имеет префикс D_ или D_V_ (схема T-MIS)
     * 2. Не является CTE
     * 3. Не является псевдонимом таблицы
     * 4. Не является SQL ключевым словом
     * 5. Не является псевдонимом колонки (определяется по контексту)
     * Извлечение имён CTE (Common Table Expressions)
     * Пример: WITH DPC AS (SELECT ...) -> DPC
     */
    private Set<String> extractCTENames(String upperSql) {
        Set<String> cteNames = new LinkedHashSet<>();
        Matcher matcher = CTE_PATTERN.matcher(upperSql);
        while (matcher.find()) {
            String cteName = matcher.group(1);
            if (cteName != null && !cteName.isEmpty()) {
                cteNames.add(cteName);
            }
        }
        return cteNames;
    }

    /**
     * Извлечение псевдонимов таблиц из FROM/JOIN
     * Пример: FROM D_TABLE t -> t
     */
    private Set<String> extractTableAliases(String upperSql) {
        Set<String> aliases = new LinkedHashSet<>();
        Matcher matcher = TABLE_ALIAS_PATTERN.matcher(upperSql);
        while (matcher.find()) {
            String alias = matcher.group(1);
            if (alias != null && !alias.isEmpty()) {
                // Пропускаем SQL ключевые слова
                if (!SQL_KEYWORDS.contains(alias)) {
                    aliases.add(alias);
                }
            }
        }
        return aliases;
    }

    /**
     * Проверка, является ли имя реальной таблицей/вьюхой
     */
    private boolean isRealDatabaseObject(String name, String sqlContext, Set<String> cteNames, Set<String> tableAliases) {
        if (name == null || name.isEmpty()) return false;

        String upperName = name.toUpperCase();

        // 1. Если имеет префикс D_ или D_V_ - это точно объект БД
        if (upperName.startsWith("D_") || upperName.startsWith("D_V_")) {
            return true;
        }

        // 2. Пропускаем SQL ключевые слова
        if (SQL_KEYWORDS.contains(upperName)) return false;

        // 3. Пропускаем системные объекты
        if (SYSTEM_OBJECTS.contains(upperName)) return false;

        // 4. Пропускаем CTE
        if (cteNames.contains(upperName)) return false;

        // 5. Пропускаем псевдонимы таблиц
        if (tableAliases.contains(upperName)) return false;

        // 6. Имена без префикса D_ - не являются таблицами/вьюхами в T-MIS
        if (!upperName.startsWith("D_")) {
            return false;
        }
        // 7. Если имя содержит только буквы и цифры без префикса D_ - вероятно не таблица
        if (upperName.matches("^[A-Z][A-Z0-9]*$") && !upperName.startsWith("D_")) {
            return false;
        }

        return true;
    }

    /**
     * Проверка, является ли имя псевдонимом колонки
     * Анализирует контекст: встречается ли имя после SELECT ... AS или в конце выражения
     */
    private boolean isColumnAlias(String name, String sqlContext) {
        if (name == null || name.isEmpty()) return false;

        // Паттерн: SELECT ... AS NAME или SELECT ... NAME (неявный псевдоним)
        Pattern aliasPattern = Pattern.compile(
                "\\bAS\\s+" + name + "\\b|\\b" + name + "\\s+(?=FROM|WHERE|GROUP|ORDER|HAVING|UNION|$)",
                Pattern.CASE_INSENSITIVE
        );

        // Проверяем в SELECT части запроса
        String selectPart = extractSelectPart(sqlContext);
        if (selectPart != null && aliasPattern.matcher(selectPart).find()) {
            return true;
        }

        return false;
    }

    /**
     * Извлечение SELECT части SQL запроса (до FROM)
     */
    private String extractSelectPart(String sql) {
        Pattern selectPattern = Pattern.compile(
                "\\bSELECT\\b(.*?)\\bFROM\\b",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = selectPattern.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Проверка, является ли имя функцией или выражением
     */
    private boolean isFunctionOrExpression(String name, String sqlContext) {
        if (name == null || name.isEmpty()) return false;

        // Если после имени идёт открывающая скобка - это функция
        Pattern functionPattern = Pattern.compile(
                "\\b" + name + "\\s*\\(",
                Pattern.CASE_INSENSITIVE
        );
        if (functionPattern.matcher(sqlContext).find()) {
            return true;
        }

        return false;
    }

    /**
     * Извлечь системные опции из D_PKG_OPTIONS.GET и D_PKG_OPTION_SPECS.GET
     * Поддерживает:
     * - D_PKG_OPTIONS.GET('OPTION_NAME', ...)
     * - D_PKG_OPTIONS.GET(psSO_CODE => 'OPTION_NAME', ...)
     * - D_PKG_OPTIONS.GET(psSO_CODE =&gt; 'OPTION_NAME', ...) - HTML-сущность
     * - D_PKG_OPTION_SPECS.GET('OPTION_NAME', ...)
     * - D_PKG_OPTION_SPECS.GET(psSO_CODE => 'OPTION_NAME', ...)
     * - D_PKG_OPTION_SPECS.GET(psSO_CODE =&gt; 'OPTION_NAME', ...) - HTML-сущность
     */
    private void extractSystemOptions(String sql, SqlInfo sqlInfo) {
        if (sql == null || sql.isEmpty()) return;

        Set<String> options = new LinkedHashSet<>();

        // Паттерн 1: D_PKG_OPTIONS.GET('OPTION', ...) - позиционные параметры
        Pattern optPattern1 = Pattern.compile(
                "D_PKG_OPTIONS\\.GET\\s*\\(\\s*'([^']+)'",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher m1 = optPattern1.matcher(sql);
        while (m1.find()) addOption(m1.group(1), options);

        // Паттерн 2: D_PKG_OPTIONS.GET с => или =&gt; (именованные параметры)
        Pattern optPattern2 = Pattern.compile(
                "D_PKG_OPTIONS\\.GET[\\s\\S]*?(?:psSO_CODE|PS_SO_CODE|PSSO_CODE|PS_SO_CD)\\s*(?:=>|=&gt;)\\s*'([^']+)'",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher m2 = optPattern2.matcher(sql);
        while (m2.find()) addOption(m2.group(1), options);

        // Паттерн 3: D_PKG_OPTION_SPECS.GET('OPTION', ...) - позиционные
        Pattern optSpecPattern1 = Pattern.compile(
                "D_PKG_OPTION_SPECS\\.GET\\s*\\(\\s*'([^']+)'",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher m3 = optSpecPattern1.matcher(sql);
        while (m3.find()) addOption(m3.group(1), options);

        // Паттерн 4: D_PKG_OPTION_SPECS.GET с => или =&gt; (именованные параметры)
        Pattern optSpecPattern2 = Pattern.compile(
                "D_PKG_OPTION_SPECS\\.GET[\\s\\S]*?(?:psSO_CODE|PS_SO_CODE|PSSO_CODE|PS_SO_CD)\\s*(?:=>|=&gt;)\\s*'([^']+)'",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher m4 = optSpecPattern2.matcher(sql);
        while (m4.find()) addOption(m4.group(1), options);

        // Паттерн 5: Универсальный (запасной) - ищем любую строку в кавычках после => или =&gt;
        Pattern universalPattern = Pattern.compile(
                "(?:D_PKG_OPTIONS|D_PKG_OPTION_SPECS)\\.GET[\\s\\S]*?(?:=>|=&gt;)\\s*'([^']+)'",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher m5 = universalPattern.matcher(sql);
        while (m5.find()) addOption(m5.group(1), options);

        for (String option : options) {
            sqlInfo.addSystemOption(option);
        }

        if (!options.isEmpty()) {
            System.out.println("      [DEBUG] Найдено системных опций: " + options);
        }
    }

    private void addOption(String value, Set<String> options) {
        if (value != null && !value.isEmpty()) {
            options.add(value);
        }
    }

}