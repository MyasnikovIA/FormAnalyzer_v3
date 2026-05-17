// ui/SettingsDialog.java
package ru.tmis.analyzer.ui;

import ru.tmis.analyzer.config.AppConfig;
import ru.tmis.analyzer.config.SettingsModel;
import ru.tmis.analyzer.core.db.OracleService;
import ru.tmis.analyzer.core.db.PostgresService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SettingsDialog extends JDialog {

    private final SettingsModel settings;
    private final AppConfig config;
    private boolean saved = false;

    // Connection fields
    private JTextField projectPathField;
    private JTextField outputDirField;
    private JTextField oracleUrlField;
    private JTextField oracleUserField;
    private JPasswordField oraclePasswordField;
    private JTextField postgresUrlField;
    private JTextField postgresUserField;
    private JPasswordField postgresPasswordField;
    private JTextField misUserField;

    // LLM Export
    private JCheckBox enableLLMExportCheckbox;
    private JRadioButton singleFileRadio;
    private JRadioButton perFormRadio;
    private JTextArea instructionTextArea;

    // LLM Blocks
    private JCheckBox includeSqlQueriesCheckbox;
    private JCheckBox includePostgresViewsCheckbox;
    private JCheckBox includeOracleViewsCheckbox;
    private JCheckBox includePostgresTablesCheckbox;
    private JCheckBox includeOracleTablesCheckbox;
    private JCheckBox includeOracleFunctionsCheckbox;
    private JCheckBox includePostgresFunctionsCheckbox;
    private JCheckBox includeBrokerFunctionsCheckbox;

    // Report settings
    private JCheckBox includeSqlContentCheckbox;
    private JCheckBox includeTablesViewsCheckbox;
    private JCheckBox includeJsUnitCompositionsCheckbox;
    private JCheckBox includeBrokerFunctionsReportCheckbox;
    private JCheckBox includePopupMenusCheckbox;
    private JCheckBox includePostgresPopupMenusCheckbox;
    private JCheckBox checkPostgresPackagesCheckbox;

    public SettingsDialog(JFrame parent, SettingsModel settings, AppConfig config) {
        super(parent, "Настройки", true);
        this.settings = settings;
        this.config = config;
        initUI();
        loadSettings();
    }

    private void initUI() {
        setSize(800, 700);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Подключения", createConnectionPanel());
        tabbedPane.addTab("Отчеты", createReportPanel());
        tabbedPane.addTab("LLM Экспорт", createLLMPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");

        saveButton.addActionListener(e -> {
            saveSettings();
            saved = true;
            dispose();
        });
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        int row = 0;

        // Project Path
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Путь к проекту:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        projectPathField = new JTextField();
        panel.add(projectPathField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        JButton browseButton = new JButton("Обзор...");
        browseButton.addActionListener(e -> browseFolder(projectPathField));
        panel.add(browseButton, gbc);
        row++;

        // Output Directory
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Каталог отчетов:"), gbc);
        gbc.gridx = 1;
        outputDirField = new JTextField();
        panel.add(outputDirField, gbc);
        gbc.gridx = 2;
        JButton browseOutputButton = new JButton("Обзор...");
        browseOutputButton.addActionListener(e -> browseFolder(outputDirField));
        panel.add(browseOutputButton, gbc);
        row++;

        // Oracle separator
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        panel.add(createSeparator("Oracle Database"), gbc);
        row++; gbc.gridwidth = 1;

        // Oracle URL
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Oracle URL:"), gbc);
        gbc.gridx = 1;
        oracleUrlField = new JTextField();
        panel.add(oracleUrlField, gbc);
        gbc.gridx = 2;
        JButton testOracleButton = new JButton("Проверить");
        testOracleButton.addActionListener(e -> testOracle());
        panel.add(testOracleButton, gbc);
        row++;

        // Oracle User
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Oracle User:"), gbc);
        gbc.gridx = 1;
        oracleUserField = new JTextField();
        panel.add(oracleUserField, gbc);
        row++;

        // Oracle Password
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Oracle Password:"), gbc);
        gbc.gridx = 1;
        oraclePasswordField = new JPasswordField();
        panel.add(oraclePasswordField, gbc);
        row++;

        // PostgreSQL separator
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        panel.add(createSeparator("PostgreSQL Database"), gbc);
        row++; gbc.gridwidth = 1;

        // PostgreSQL URL
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("PostgreSQL URL:"), gbc);
        gbc.gridx = 1;
        postgresUrlField = new JTextField();
        panel.add(postgresUrlField, gbc);
        gbc.gridx = 2;
        JButton testPostgresButton = new JButton("Проверить");
        testPostgresButton.addActionListener(e -> testPostgres());
        panel.add(testPostgresButton, gbc);
        row++;

        // PostgreSQL User
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("PostgreSQL User:"), gbc);
        gbc.gridx = 1;
        postgresUserField = new JTextField();
        panel.add(postgresUserField, gbc);
        row++;

        // PostgreSQL Password
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("PostgreSQL Password:"), gbc);
        gbc.gridx = 1;
        postgresPasswordField = new JPasswordField();
        panel.add(postgresPasswordField, gbc);
        row++;

        // MIS User
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Пользователь МИС:"), gbc);
        gbc.gridx = 1;
        misUserField = new JTextField();
        panel.add(misUserField, gbc);
        gbc.gridx = 2;
        JLabel hintLabel = new JLabel("(контекст PostgreSQL)");
        hintLabel.setForeground(Color.GRAY);
        panel.add(hintLabel, gbc);

        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Настройки отчета");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(15));

        // SQL запросы
        includeSqlContentCheckbox = new JCheckBox("Показывать SQL запросы");
        includeSqlContentCheckbox.setSelected(config.isIncludeSqlContent());
        includeSqlContentCheckbox.addActionListener(e -> config.setIncludeSqlContent(includeSqlContentCheckbox.isSelected()));
        contentPanel.add(createCheckboxWithDescription(includeSqlContentCheckbox,
                "Выводить полное содержимое SQL запросов в отчете.\n" +
                        "Включает: SELECT, INSERT, UPDATE, DELETE, BEGIN...END блоки"));
        contentPanel.add(Box.createVerticalStrut(5));

        // Таблицы и вьюхи
        includeTablesViewsCheckbox = new JCheckBox("Показывать таблицы и вьюхи");
        includeTablesViewsCheckbox.setSelected(config.isIncludeTablesViews());
        includeTablesViewsCheckbox.addActionListener(e -> config.setIncludeTablesViews(includeTablesViewsCheckbox.isSelected()));
        contentPanel.add(createCheckboxWithDescription(includeTablesViewsCheckbox,
                "Выводить список всех таблиц (D_*) и представлений (D_V_*),\n" +
                        "используемых в SQL запросах формы"));
        contentPanel.add(Box.createVerticalStrut(5));

        // Композиции из JS
        includeJsUnitCompositionsCheckbox = new JCheckBox("Показывать композиции из JS");
        includeJsUnitCompositionsCheckbox.setSelected(config.isIncludeJsUnitCompositions());
        includeJsUnitCompositionsCheckbox.addActionListener(e -> config.setIncludeJsUnitCompositions(includeJsUnitCompositionsCheckbox.isSelected()));
        contentPanel.add(createCheckboxWithDescription(includeJsUnitCompositionsCheckbox,
                "Извлекать композиции UnitEdit из JS вызовов\n" +
                        "UniversalComposition в openWindow/openD3Form"));
        contentPanel.add(Box.createVerticalStrut(5));

        // Брокеры
        includeBrokerFunctionsReportCheckbox = new JCheckBox("Показывать брокеров");
        includeBrokerFunctionsReportCheckbox.setSelected(config.isIncludeBrokerFunctions());
        includeBrokerFunctionsReportCheckbox.addActionListener(e -> config.setIncludeBrokerFunctions(includeBrokerFunctionsReportCheckbox.isSelected()));
        contentPanel.add(createCheckboxWithDescription(includeBrokerFunctionsReportCheckbox,
                "Извлекать брокеры (Action/SubAction с атрибутами unit/action)\n" +
                        "и соответствующие им функции из базы данных Oracle"));
        contentPanel.add(Box.createVerticalStrut(5));

        // Контекстное меню (ПКМ) - Oracle
        includePopupMenusCheckbox = new JCheckBox("Показывать контекстное меню (ПКМ) из Oracle");
        includePopupMenusCheckbox.setSelected(config.isIncludePopupMenus());
        includePopupMenusCheckbox.addActionListener(e -> config.setIncludePopupMenus(includePopupMenusCheckbox.isSelected()));
        contentPanel.add(createCheckboxWithDescription(includePopupMenusCheckbox,
                "Выводить в отчет дерево контекстного меню (PopupMenu),\n" +
                        "включая пункты, добавленные через AutoPopupMenu,\n" +
                        "а также отчеты из базы данных Oracle (D_REPORTS_LINKS)"));
        contentPanel.add(Box.createVerticalStrut(5));

        // Контекстное меню (ПКМ) - PostgreSQL
        includePostgresPopupMenusCheckbox = new JCheckBox("Показывать контекстное меню из PostgreSQL");
        includePostgresPopupMenusCheckbox.setSelected(config.isIncludePostgresPopupMenus());
        includePostgresPopupMenusCheckbox.addActionListener(e ->
                config.setIncludePostgresPopupMenus(includePostgresPopupMenusCheckbox.isSelected()));
        contentPanel.add(createCheckboxWithDescription(includePostgresPopupMenusCheckbox,
                "Выводить в отчет дерево контекстного меню (PopupMenu) с отчетами,\n" +
                        "загруженными из базы данных PostgreSQL (аналогично Oracle).\n" +
                        "Требует подключения к PostgreSQL и наличия представлений D_REPORTS_LINKS и D_REPORTS."));
        contentPanel.add(Box.createVerticalStrut(5));

        // Детальное содержимое вьюх
        JCheckBox includeViewDetailsCheckbox = new JCheckBox("Детальное содержимое вьюх (с количеством записей)");
        includeViewDetailsCheckbox.setSelected(config.isIncludeViewDetails());
        includeViewDetailsCheckbox.addActionListener(e -> config.setIncludeViewDetails(includeViewDetailsCheckbox.isSelected()));
        contentPanel.add(createCheckboxWithDescription(includeViewDetailsCheckbox,
                "Выводить для каждой вьюхи список таблиц, которые в ней используются,\n" +
                        "а также количество записей во вьюхе и в каждой таблице (Oracle и PostgreSQL).\n" +
                        "Требует подключения к обеим базам данных."));

        JCheckBox checkPostgresPackagesCheckbox = new JCheckBox("Проверять пакеты/функции в PostgreSQL");
        checkPostgresPackagesCheckbox.setSelected(config.isCheckPostgresPackages());
        checkPostgresPackagesCheckbox.addActionListener(e -> config.setCheckPostgresPackages(checkPostgresPackagesCheckbox.isSelected()));
        contentPanel.add(createCheckboxWithDescription(checkPostgresPackagesCheckbox,
                "Проверять существование пакетов и функций D_PKG_* в PostgreSQL.\n" +
                        "Использует расширение plpgsql_check_function для анализа.\n" +
                        "Требует установленного расширения plpgsql_check в PostgreSQL."));

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLLMPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Настройки экспорта LLM промпта");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));

        JLabel descLabel = new JLabel("<html>Экспорт данных в формате, готовом для передачи в LLM (ChatGPT, Claude и др.).<br>" +
                "Будут включены: SQL запросы, DDL вьюх, DDL таблиц и тела функций из Oracle и PostgreSQL.</html>");
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(descLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Чекбокс включения экспорта
        enableLLMExportCheckbox = new JCheckBox("Включить экспорт LLM промпта после анализа", false);
        enableLLMExportCheckbox.setFont(enableLLMExportCheckbox.getFont().deriveFont(Font.BOLD));
        enableLLMExportCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(enableLLMExportCheckbox);
        contentPanel.add(Box.createVerticalStrut(15));

        // Режим генерации
        JPanel modePanel = new JPanel();
        modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));
        modePanel.setBorder(BorderFactory.createTitledBorder("Режим генерации"));
        modePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        ButtonGroup modeGroup = new ButtonGroup();
        singleFileRadio = new JRadioButton("Один общий промпт для всех форм", true);
        perFormRadio = new JRadioButton("Отдельный промпт для каждой формы");

        singleFileRadio.setToolTipText("Все SQL запросы и DDL объектов из всех выбранных форм будут в одном файле");
        perFormRadio.setToolTipText("Для каждой выбранной формы будет создан отдельный файл промпта");

        modeGroup.add(singleFileRadio);
        modeGroup.add(perFormRadio);

        modePanel.add(singleFileRadio);
        modePanel.add(perFormRadio);

        contentPanel.add(modePanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // Выбор данных для экспорта
        JPanel blocksPanel = new JPanel();
        blocksPanel.setLayout(new BoxLayout(blocksPanel, BoxLayout.Y_AXIS));
        blocksPanel.setBorder(BorderFactory.createTitledBorder("Выбор данных для экспорта"));
        blocksPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        includeSqlQueriesCheckbox = new JCheckBox("SQL запросы с тэгами", true);
        includePostgresViewsCheckbox = new JCheckBox("Текст вьюх из PostgreSQL", true);
        includeOracleViewsCheckbox = new JCheckBox("Текст вьюх из Oracle", true);
        includePostgresTablesCheckbox = new JCheckBox("DDL таблиц из PostgreSQL вьюх", true);
        includeOracleTablesCheckbox = new JCheckBox("DDL таблиц из Oracle вьюх", true);
        includeOracleFunctionsCheckbox = new JCheckBox("Тела функций из Oracle пакетов", true);
        includePostgresFunctionsCheckbox = new JCheckBox("Тела функций и процедур из PostgreSQL", true);
        includeBrokerFunctionsCheckbox = new JCheckBox("Брокеры (Action/SubAction с unit)", true);

        blocksPanel.add(createBlockCheckbox(includeSqlQueriesCheckbox,
                "Полные SQL запросы из всех DataSet и Action компонентов в формате XML с тэгами"));
        blocksPanel.add(Box.createVerticalStrut(5));
        blocksPanel.add(createBlockCheckbox(includePostgresViewsCheckbox,
                "DDL определение всех вьюх (D_V_*), найденных в SQL запросах, из базы PostgreSQL"));
        blocksPanel.add(Box.createVerticalStrut(5));
        blocksPanel.add(createBlockCheckbox(includeOracleViewsCheckbox,
                "DDL определение всех вьюх (D_V_*), найденных в SQL запросах, из базы Oracle"));
        blocksPanel.add(Box.createVerticalStrut(5));
        blocksPanel.add(createBlockCheckbox(includePostgresTablesCheckbox,
                "Для каждой вьюхи из PostgreSQL - список используемых таблиц и их DDL"));
        blocksPanel.add(Box.createVerticalStrut(5));
        blocksPanel.add(createBlockCheckbox(includeOracleTablesCheckbox,
                "Для каждой вьюхи из Oracle - список используемых таблиц и их DDL"));
        blocksPanel.add(Box.createVerticalStrut(5));
        blocksPanel.add(createBlockCheckbox(includeOracleFunctionsCheckbox,
                "DDL определение тел функций из Oracle пакетов (D_PKG_*.FUNCTION_NAME)"));
        blocksPanel.add(Box.createVerticalStrut(5));
        blocksPanel.add(createBlockCheckbox(includePostgresFunctionsCheckbox,
                "DDL определение тел функций и процедур из PostgreSQL"));
        blocksPanel.add(Box.createVerticalStrut(5));
        blocksPanel.add(createBlockCheckbox(includeBrokerFunctionsCheckbox,
                "Извлечение брокеров из Action/SubAction компонентов с unit/action"));

        // Кнопки выбора всех/снятия всех
        JPanel blocksControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectAllBlocksButton = new JButton("Выбрать все");
        JButton deselectAllBlocksButton = new JButton("Снять все");

        selectAllBlocksButton.addActionListener(e -> {
            includeSqlQueriesCheckbox.setSelected(true);
            includePostgresViewsCheckbox.setSelected(true);
            includeOracleViewsCheckbox.setSelected(true);
            includePostgresTablesCheckbox.setSelected(true);
            includeOracleTablesCheckbox.setSelected(true);
            includeOracleFunctionsCheckbox.setSelected(true);
            includePostgresFunctionsCheckbox.setSelected(true);
            includeBrokerFunctionsCheckbox.setSelected(true);
        });

        deselectAllBlocksButton.addActionListener(e -> {
            includeSqlQueriesCheckbox.setSelected(false);
            includePostgresViewsCheckbox.setSelected(false);
            includeOracleViewsCheckbox.setSelected(false);
            includePostgresTablesCheckbox.setSelected(false);
            includeOracleTablesCheckbox.setSelected(false);
            includeOracleFunctionsCheckbox.setSelected(false);
            includePostgresFunctionsCheckbox.setSelected(false);
            includeBrokerFunctionsCheckbox.setSelected(false);
        });

        blocksControlPanel.add(selectAllBlocksButton);
        blocksControlPanel.add(deselectAllBlocksButton);
        blocksPanel.add(Box.createVerticalStrut(10));
        blocksPanel.add(blocksControlPanel);

        contentPanel.add(blocksPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // Инструкция для LLM
        JPanel instructionPanel = new JPanel();
        instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.Y_AXIS));
        instructionPanel.setBorder(BorderFactory.createTitledBorder("Инструкция для LLM"));
        instructionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel instructionLabel = new JLabel("Задача для LLM (будет добавлена в конец промпта):");
        instructionLabel.setFont(new Font("Dialog", Font.BOLD, 11));
        instructionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        instructionPanel.add(instructionLabel);
        instructionPanel.add(Box.createVerticalStrut(5));

        instructionTextArea = new JTextArea(15, 60);
        instructionTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        instructionTextArea.setLineWrap(true);
        instructionTextArea.setWrapStyleWord(true);
        instructionTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JScrollPane instructionScrollPane = new JScrollPane(instructionTextArea);
        instructionScrollPane.setPreferredSize(new Dimension(0, 200));
        instructionPanel.add(instructionScrollPane);

        JPanel instructionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton resetInstructionButton = new JButton("Восстановить стандартную инструкцию");
        resetInstructionButton.addActionListener(e -> instructionTextArea.setText(getDefaultInstruction()));
        instructionButtonPanel.add(resetInstructionButton);
        instructionPanel.add(instructionButtonPanel);

        contentPanel.add(instructionPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadSettings() {
        projectPathField.setText(settings.getProjectPath());
        outputDirField.setText(settings.getOutputDir());
        oracleUrlField.setText(settings.getOracleUrl());
        oracleUserField.setText(settings.getOracleUser());
        oraclePasswordField.setText(settings.getOraclePassword());
        postgresUrlField.setText(settings.getPostgresUrl());
        postgresUserField.setText(settings.getPostgresUser());
        postgresPasswordField.setText(settings.getPostgresPassword());
        misUserField.setText(settings.getMisUser());

        // Report settings
        if (includeSqlContentCheckbox != null) {
            includeSqlContentCheckbox.setSelected(config.isIncludeSqlContent());
        }
        if (includeTablesViewsCheckbox != null) {
            includeTablesViewsCheckbox.setSelected(config.isIncludeTablesViews());
        }
        if (includeJsUnitCompositionsCheckbox != null) {
            includeJsUnitCompositionsCheckbox.setSelected(config.isIncludeJsUnitCompositions());
        }
        if (includeBrokerFunctionsReportCheckbox != null) {
            includeBrokerFunctionsReportCheckbox.setSelected(config.isIncludeBrokerFunctions());
        }
        if (includePopupMenusCheckbox != null) {
            includePopupMenusCheckbox.setSelected(config.isIncludePopupMenus());
        }

        if (checkPostgresPackagesCheckbox != null) {
            checkPostgresPackagesCheckbox.setSelected(config.isCheckPostgresPackages());
        }

        // LLM settings
        enableLLMExportCheckbox.setSelected(config.isEnableLLMExport());
        if ("per_form".equals(config.getLlmExportMode())) {
            perFormRadio.setSelected(true);
        } else {
            singleFileRadio.setSelected(true);
        }

        includeSqlQueriesCheckbox.setSelected(config.isIncludeSqlQueries());
        includePostgresViewsCheckbox.setSelected(config.isIncludePostgresViews());
        includeOracleViewsCheckbox.setSelected(config.isIncludeOracleViews());
        includePostgresTablesCheckbox.setSelected(config.isIncludePostgresTables());
        includeOracleTablesCheckbox.setSelected(config.isIncludeOracleTables());
        includeOracleFunctionsCheckbox.setSelected(config.isIncludeOracleFunctions());
        includePostgresFunctionsCheckbox.setSelected(config.isIncludePostgresFunctions());
        includeBrokerFunctionsCheckbox.setSelected(config.isIncludeBrokerFunctions());
        includePostgresPopupMenusCheckbox.setSelected(config.isIncludePostgresPopupMenus());

        String instruction = config.getLlmInstructionText();
        if (instruction == null || instruction.isEmpty()) {
            instructionTextArea.setText(getDefaultInstruction());
        } else {
            instructionTextArea.setText(instruction);
        }
    }

    private void saveSettings() {
        settings.setProjectPath(projectPathField.getText());
        settings.setOutputDir(outputDirField.getText());
        settings.setOracleUrl(oracleUrlField.getText());
        settings.setOracleUser(oracleUserField.getText());
        settings.setOraclePassword(new String(oraclePasswordField.getPassword()));
        settings.setPostgresUrl(postgresUrlField.getText());
        settings.setPostgresUser(postgresUserField.getText());
        settings.setPostgresPassword(new String(postgresPasswordField.getPassword()));
        settings.setMisUser(misUserField.getText());
        settings.save();

        // Report settings
        if (includeSqlContentCheckbox != null) {
            config.setIncludeSqlContent(includeSqlContentCheckbox.isSelected());
        }
        if (includeTablesViewsCheckbox != null) {
            config.setIncludeTablesViews(includeTablesViewsCheckbox.isSelected());
        }
        if (includeJsUnitCompositionsCheckbox != null) {
            config.setIncludeJsUnitCompositions(includeJsUnitCompositionsCheckbox.isSelected());
        }
        if (includeBrokerFunctionsReportCheckbox != null) {
            config.setIncludeBrokerFunctions(includeBrokerFunctionsReportCheckbox.isSelected());
        }
        if (includePopupMenusCheckbox != null) {
            config.setIncludePopupMenus(includePopupMenusCheckbox.isSelected());
        }
        if (checkPostgresPackagesCheckbox != null) {
            config.setCheckPostgresPackages(checkPostgresPackagesCheckbox.isSelected());
        }

        // LLM settings
        config.setEnableLLMExport(enableLLMExportCheckbox.isSelected());
        config.setLlmExportMode(perFormRadio.isSelected() ? "per_form" : "single_file");
        config.setIncludeSqlQueries(includeSqlQueriesCheckbox.isSelected());
        config.setIncludePostgresViews(includePostgresViewsCheckbox.isSelected());
        config.setIncludeOracleViews(includeOracleViewsCheckbox.isSelected());
        config.setIncludePostgresTables(includePostgresTablesCheckbox.isSelected());
        config.setIncludeOracleTables(includeOracleTablesCheckbox.isSelected());
        config.setIncludeOracleFunctions(includeOracleFunctionsCheckbox.isSelected());
        config.setIncludePostgresFunctions(includePostgresFunctionsCheckbox.isSelected());
        config.setIncludeBrokerFunctions(includeBrokerFunctionsCheckbox.isSelected());
        config.setLlmInstructionText(instructionTextArea.getText());
        config.setIncludePostgresPopupMenus(includePostgresPopupMenusCheckbox.isSelected());
        config.save();

        JOptionPane.showMessageDialog(this, "Настройки сохранены", "Успешно", JOptionPane.INFORMATION_MESSAGE);
    }

    private void testOracle() {
        String url = oracleUrlField.getText();
        String user = oracleUserField.getText();
        String password = new String(oraclePasswordField.getPassword());

        OracleService service = new OracleService(url, user, password);
        boolean ok = service.testConnection();

        JOptionPane.showMessageDialog(this,
                ok ? "Подключение к Oracle успешно!" : "Ошибка подключения к Oracle",
                "Результат", ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    private void testPostgres() {
        String url = postgresUrlField.getText();
        String user = postgresUserField.getText();
        String password = new String(postgresPasswordField.getPassword());
        String misUser = misUserField.getText();

        PostgresService service = new PostgresService(url, user, password, misUser);
        boolean ok = service.testConnection();

        JOptionPane.showMessageDialog(this,
                ok ? "Подключение к PostgreSQL успешно!" : "Ошибка подключения к PostgreSQL",
                "Результат", ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    private void browseFolder(JTextField field) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private JPanel createSeparator(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(new Color(0, 100, 200));
        panel.add(label, BorderLayout.WEST);
        return panel;
    }

    private String getDefaultInstruction() {
        return "## ИНСТРУКЦИЯ ДЛЯ АНАЛИЗА\n\n" +
                "Проанализируй предоставленную информацию и ответь на вопросы:\n\n" +
                "1. Какие основные бизнес-сущности используются?\n" +
                "2. Какие связи между таблицами можно выделить?\n" +
                "3. Есть ли потенциальные проблемы с производительностью?\n" +
                "4. Какие вьюхи наиболее часто используются?\n" +
                "5. Есть ли дублирование логики?\n" +
                "6. Какие рекомендации по оптимизации?\n\n" +
                "Обращай внимание на:\n" +
                "- Префиксы: D_V_* (вьюхи), D_* (таблицы), D_PKG_* (пакеты)\n" +
                "- Константы из D_PKG_CONSTANTS.SEARCH_*\n" +
                "- Системные опции из D_PKG_OPTIONS.GET\n";
    }

    public boolean isSaved() {
        return saved;
    }

    private JPanel createCheckboxWithDescription(JCheckBox checkBox, String description) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Жирный шрифт для чекбокса
        checkBox.setFont(checkBox.getFont().deriveFont(Font.BOLD));

        // Текст описания (серый, с переносом строк)
        JTextArea descArea = new JTextArea(description);
        descArea.setEditable(false);
        descArea.setBackground(panel.getBackground());
        descArea.setFont(new Font("Dialog", Font.PLAIN, 11));
        descArea.setForeground(Color.GRAY);
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);

        panel.add(checkBox, BorderLayout.NORTH);
        panel.add(descArea, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBlockCheckbox(JCheckBox checkBox, String description) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        checkBox.setFont(checkBox.getFont().deriveFont(Font.PLAIN));

        JTextArea descArea = new JTextArea(description);
        descArea.setEditable(false);
        descArea.setBackground(panel.getBackground());
        descArea.setFont(new Font("Dialog", Font.PLAIN, 10));
        descArea.setForeground(Color.GRAY);
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setRows(2);

        panel.add(checkBox, BorderLayout.NORTH);
        panel.add(descArea, BorderLayout.CENTER);
        return panel;
    }
}