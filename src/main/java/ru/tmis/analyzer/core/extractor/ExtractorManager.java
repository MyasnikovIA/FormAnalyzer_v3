// core/extractor/ExtractorManager.java

package ru.tmis.analyzer.core.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.analyzer.ConversionAnalyzer;
import ru.tmis.analyzer.core.extractor.extractor.SqlExtractor;
import ru.tmis.analyzer.core.extractor.processors.*;
import ru.tmis.analyzer.core.log.ILogger;
import ru.tmis.analyzer.core.model.*;
import ru.tmis.analyzer.core.service.LLMDataLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExtractorManager {

    private final List<IXmlProcessor> processors = new ArrayList<>();
    private final SqlExtractor sqlExtractor;
    private final SettingsModel settings;
    private final ConversionAnalyzer conversionAnalyzer = new ConversionAnalyzer();
    private final AppConfig config;
    private LLMDataLoader llmDataLoader;

    // Поля для остановки
    private AtomicBoolean stopRequested = null;
    private ILogger logger;

    public ExtractorManager(SettingsModel settings, AppConfig config) {
        this.settings = settings;
        this.sqlExtractor = new SqlExtractor();
        this.config = config;
        this.llmDataLoader = new LLMDataLoader(settings, config);
        registerDefaultProcessors();
    }

    private void registerDefaultProcessors() {
        processors.add(new RouterProcessor());
        processors.add(new SubFormProcessor());
        processors.add(new BrokerProcessor());
        processors.add(new PackageFromActionProcessor());
        processors.add(new JsFormProcessor());
        processors.add(new ConstantProcessor());
        processors.add(new SystemOptionProcessor());
        processors.add(new UnitCompositionProcessor());
        processors.add(new UniversalCompositionProcessor());
        processors.add(new SystemCompositionProcessor());
        processors.add(new D3ApiShowFormProcessor());
        processors.add(new ReportProcessor());
        processors.add(new AutoPopupMenuProcessor());
        processors.add(new PopupMenuProcessor(settings));
        processors.add(new PopupMenuProcessorPg(settings));
        processors.add(new UnknownObjectProcessor());

        processors.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
    }

    public void registerProcessor(IXmlProcessor processor) {
        processors.add(processor);
        processors.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
    }

    /**
     * Установка флага остановки
     */
    public void setStopRequested(AtomicBoolean stopRequested) {
        this.stopRequested = stopRequested;
        if (this.sqlExtractor != null) {
            this.sqlExtractor.setStopRequested(stopRequested);
        }
    }

    /**
     * Установка логгера
     */
    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

    private void log(String message) {
        if (logger != null) {
            logger.log(message);
        } else {
            System.out.println(message);
        }
    }

    private boolean isStopped() {
        return stopRequested != null && stopRequested.get();
    }

    public void process(String xmlContent, FormInfo formInfo) {
        if (xmlContent == null || xmlContent.isEmpty()) return;

        if (isStopped()) {
            log("Извлечение данных остановлено пользователем (до парсинга)");
            return;
        }

        Document doc = Jsoup.parse(xmlContent, "", Parser.xmlParser());

        if (isStopped()) {
            log("Извлечение данных остановлено пользователем (перед анализом конвертации)");
            return;
        }

        // 1. Анализ конвертации
        ConversionStatistics stats = conversionAnalyzer.analyzeForm(formInfo, xmlContent);
        formInfo.setConversionStatistics(stats);
        formInfo.setQueryConversionMap(stats.getQueryDetails());  // если сделать getter для всей карты

        if (isStopped()) {
            log("Извлечение данных остановлено пользователем (перед извлечением SQL)");
            return;
        }

        // 2. Извлекаем ВСЕ SQL запросы (включая те, у которых есть Router)
        List<SqlInfo> allSqlList = sqlExtractor.extract(doc, formInfo);

        for (SqlInfo sql : allSqlList) {
            if (isStopped()) return;
            for (String tv : sql.getTablesViews()) {
                formInfo.addTableView(tv);
                System.out.println("[ExtractorManager] Добавлена вьюха/таблица ДО фильтрации: " + tv);
            }
            for (String pf : sql.getPackagesFunctions()) {
                formInfo.addPackageFunction(pf);
            }
        }

        if (isStopped()) {
            log("Извлечение данных остановлено пользователем (после извлечения SQL)");
            return;
        }

        // 3. Запускаем все процессоры (они заполнят RouterInfo, SubForm, Broker и т.д.)
        for (IXmlProcessor processor : processors) {
            if (isStopped()) {
                log("Извлечение данных остановлено пользователем в процессоре " + processor.getName());
                break;
            }

            if (processor instanceof UnknownObjectProcessor) continue;

            try {
                processor.process(doc, formInfo);
            } catch (Exception e) {
                System.err.println("Ошибка в процессоре " + processor.getName() + ": " + e.getMessage());
            }
        }

        if (isStopped()) {
            log("Извлечение данных остановлено пользователем (после процессоров)");
            return;
        }

        // 4. ФИЛЬТРАЦИЯ: удаляем из sqlQueries те запросы, у которых есть Router
        List<SqlInfo> filteredSqlList = filterQueriesWithRouters(allSqlList, formInfo);
        formInfo.setSqlQueries(filteredSqlList);

        log("  SQL запросов до фильтрации: " + allSqlList.size() +
                ", после фильтрации (без Router): " + filteredSqlList.size());

        if (filteredSqlList.size() < allSqlList.size()) {
            log("  Исключено запросов с Router: " + (allSqlList.size() - filteredSqlList.size()));
        }

        if (isStopped()) {
            log("Извлечение данных остановлено пользователем (в цикле SQL)");
            return;
        }

        // 5. Добавляем извлечённые объекты из SQL в FormInfo
        for (SqlInfo sql : filteredSqlList) {
            if (isStopped()) return;
            for (String tv : sql.getTablesViews()) formInfo.addTableView(tv);
            for (String pf : sql.getPackagesFunctions()) formInfo.addPackageFunction(pf);
            for (String proc : sql.getUserProcedures()) formInfo.addUserProcedure(proc);
            for (String opt : sql.getSystemOptions()) formInfo.addSystemOption(opt);
            for (String constant : sql.getConstants()) formInfo.addConstant(constant);
            for (String unknown : sql.getUnknownObjects()) formInfo.addUnknownObject(unknown);
        }

        // 6. Собираем функции из роутеров и добавляем в packagesFunctions
        // (только если включен экспорт LLM промпта)
        if (config != null && config.isEnableLLMExport()) {
            for (RouterInfo router : formInfo.getActionRouters()) {
                for (RouterItem item : router.getRouters()) {
                    if (item.getSqlContent() != null) {
                        extractFunctionsFromSql(item.getSqlContent(), formInfo);
                    }
                }
            }
            for (RouterInfo router : formInfo.getDataSetRouters()) {
                for (RouterItem item : router.getRouters()) {
                    if (item.getSqlContent() != null) {
                        extractFunctionsFromSql(item.getSqlContent(), formInfo);
                    }
                }
            }

            // Также проверяем SubRouters
            for (RouterInfo router : formInfo.getActionRouters()) {
                for (SubRouterInfo subRouter : router.getSubRouters()) {
                    for (RouterItem item : subRouter.getRouters()) {
                        if (item.getSqlContent() != null) {
                            extractFunctionsFromSql(item.getSqlContent(), formInfo);
                        }
                    }
                }
            }
            for (RouterInfo router : formInfo.getDataSetRouters()) {
                for (SubRouterInfo subRouter : router.getSubRouters()) {
                    for (RouterItem item : subRouter.getRouters()) {
                        if (item.getSqlContent() != null) {
                            extractFunctionsFromSql(item.getSqlContent(), formInfo);
                        }
                    }
                }
            }

            log("  [ExtractorManager] Всего функций после обработки роутеров: " +
                    formInfo.getPackagesFunctions().size());
        }

    }

    /**
     * Фильтрует SQL запросы, удаляя те, для которых существуют Router компоненты
     *
     * @param allSqlList все извлечённые SQL запросы
     * @param formInfo информация о форме (содержит RouterInfo)
     * @return отфильтрованный список SQL запросов (без тех, у которых есть Router)
     */
    private List<SqlInfo> filterQueriesWithRouters(List<SqlInfo> allSqlList, FormInfo formInfo) {
        if (allSqlList == null || allSqlList.isEmpty()) {
            return allSqlList;
        }

        // Собираем имена компонентов, у которых есть Router
        Set<String> routerComponentNames = new HashSet<>();

        // ActionRouters
        for (RouterInfo router : formInfo.getActionRouters()) {
            if (router.getName() != null && !router.getName().isEmpty()) {
                routerComponentNames.add(router.getName());
            }
        }

        // DataSetRouters
        for (RouterInfo router : formInfo.getDataSetRouters()) {
            if (router.getName() != null && !router.getName().isEmpty()) {
                routerComponentNames.add(router.getName());
            }
        }

        // Также проверяем BeforeAction и BeforeSelect (они могут иметь свои имена)
        for (RouterInfo router : formInfo.getActionRouters()) {
            if (router.getParentType() == RouterInfo.ParentType.BEFORE_ACTION) {
                if (router.getName() != null && !router.getName().isEmpty()) {
                    routerComponentNames.add(router.getName());
                }
            }
        }

        for (RouterInfo router : formInfo.getDataSetRouters()) {
            if (router.getParentType() == RouterInfo.ParentType.BEFORE_SELECT) {
                if (router.getName() != null && !router.getName().isEmpty()) {
                    routerComponentNames.add(router.getName());
                }
            }
        }

        if (routerComponentNames.isEmpty()) {
            // Нет Router компонентов - возвращаем все запросы
            return allSqlList;
        }

        log("  Найдены Router компоненты: " + routerComponentNames);

        // Фильтруем: оставляем только те SQL запросы, чьи имена НЕ входят в routerComponentNames
        return allSqlList.stream()
                .filter(sql -> {
                    String componentName = sql.getComponentName();
                    boolean hasRouter = routerComponentNames.contains(componentName);
                    if (hasRouter) {
                        log("    Исключён запрос с Router: " + componentName + " (" + sql.getSourceType() + ")");
                    }
                    return !hasRouter;
                })
                .collect(Collectors.toList());
    }

    public SqlExtractor getSqlExtractor() {
        return sqlExtractor;
    }

    public ConversionAnalyzer getConversionAnalyzer() {
        return conversionAnalyzer;
    }

    /**
     * Извлекает имена пакетных функций из SQL текста и добавляет их в FormInfo
     * (только если включен экспорт LLM промпта)
     */
    private void extractFunctionsFromSql(String sqlContent, FormInfo formInfo) {
        if (sqlContent == null || sqlContent.isEmpty()) return;

        // Проверяем, включен ли экспорт LLM
        if (config == null || !config.isEnableLLMExport()) {
            return;
        }

        Pattern funcPattern = Pattern.compile(
                "\\b(D_PKG_[A-Z0-9_]+\\.[A-Z0-9_]+)\\b",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = funcPattern.matcher(sqlContent);
        while (m.find()) {
            String func = m.group(1);
            formInfo.addPackageFunction(func);
            log("    [LLM] Найдена функция: " + func);
        }
    }
}