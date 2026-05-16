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
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Настройки отчета");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(10));

        JCheckBox includeSqlCheckbox = new JCheckBox("Показывать SQL запросы");
        includeSqlCheckbox.setSelected(config.isIncludeSqlContent());
        includeSqlCheckbox.addActionListener(e -> config.setIncludeSqlContent(includeSqlCheckbox.isSelected()));

        JCheckBox includeTablesCheckbox = new JCheckBox("Показывать таблицы и вьюхи");
        includeTablesCheckbox.setSelected(config.isIncludeTablesViews());
        includeTablesCheckbox.addActionListener(e -> config.setIncludeTablesViews(includeTablesCheckbox.isSelected()));

        JCheckBox includeCompositionsCheckbox = new JCheckBox("Показывать композиции из JS");
        includeCompositionsCheckbox.setSelected(config.isIncludeJsUnitCompositions());
        includeCompositionsCheckbox.addActionListener(e -> config.setIncludeJsUnitCompositions(includeCompositionsCheckbox.isSelected()));

        JCheckBox includeBrokersCheckbox = new JCheckBox("Показывать брокеров");
        includeBrokersCheckbox.setSelected(config.isIncludeBrokerFunctions());
        includeBrokersCheckbox.addActionListener(e -> config.setIncludeBrokerFunctions(includeBrokersCheckbox.isSelected()));

        content.add(includeSqlCheckbox);
        content.add(Box.createVerticalStrut(5));
        content.add(includeTablesCheckbox);
        content.add(Box.createVerticalStrut(5));
        content.add(includeCompositionsCheckbox);
        content.add(Box.createVerticalStrut(5));
        content.add(includeBrokersCheckbox);

        JScrollPane scroll = new JScrollPane(content);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLLMPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        enableLLMExportCheckbox = new JCheckBox("Включить экспорт LLM промпта");
        enableLLMExportCheckbox.setFont(enableLLMExportCheckbox.getFont().deriveFont(Font.BOLD));
        content.add(enableLLMExportCheckbox);
        content.add(Box.createVerticalStrut(10));

        // Mode
        JPanel modePanel = new JPanel();
        modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));
        modePanel.setBorder(BorderFactory.createTitledBorder("Режим генерации"));

        ButtonGroup modeGroup = new ButtonGroup();
        singleFileRadio = new JRadioButton("Один общий промпт", true);
        perFormRadio = new JRadioButton("Отдельный промпт для каждой формы");
        modeGroup.add(singleFileRadio);
        modeGroup.add(perFormRadio);

        modePanel.add(singleFileRadio);
        modePanel.add(perFormRadio);
        content.add(modePanel);
        content.add(Box.createVerticalStrut(10));

        // Data selection
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
        dataPanel.setBorder(BorderFactory.createTitledBorder("Выбор данных"));

        includeSqlQueriesCheckbox = new JCheckBox("SQL запросы", true);
        includePostgresViewsCheckbox = new JCheckBox("PostgreSQL вьюхи", true);
        includeOracleViewsCheckbox = new JCheckBox("Oracle вьюхи", true);
        includePostgresTablesCheckbox = new JCheckBox("PostgreSQL таблицы", true);
        includeOracleTablesCheckbox = new JCheckBox("Oracle таблицы", true);
        includeOracleFunctionsCheckbox = new JCheckBox("Oracle функции", true);
        includePostgresFunctionsCheckbox = new JCheckBox("PostgreSQL функции", true);
        includeBrokerFunctionsCheckbox = new JCheckBox("Брокеры", true);

        dataPanel.add(includeSqlQueriesCheckbox);
        dataPanel.add(includePostgresViewsCheckbox);
        dataPanel.add(includeOracleViewsCheckbox);
        dataPanel.add(includePostgresTablesCheckbox);
        dataPanel.add(includeOracleTablesCheckbox);
        dataPanel.add(includeOracleFunctionsCheckbox);
        dataPanel.add(includePostgresFunctionsCheckbox);
        dataPanel.add(includeBrokerFunctionsCheckbox);

        content.add(dataPanel);
        content.add(Box.createVerticalStrut(10));

        // Instruction
        JPanel instructionPanel = new JPanel();
        instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.Y_AXIS));
        instructionPanel.setBorder(BorderFactory.createTitledBorder("Инструкция для LLM"));

        instructionTextArea = new JTextArea(10, 60);
        instructionTextArea.setLineWrap(true);
        instructionTextArea.setWrapStyleWord(true);
        instructionTextArea.setText(getDefaultInstruction());

        JScrollPane instructionScroll = new JScrollPane(instructionTextArea);
        instructionPanel.add(instructionScroll);

        JButton resetButton = new JButton("Стандартная инструкция");
        resetButton.addActionListener(e -> instructionTextArea.setText(getDefaultInstruction()));
        instructionPanel.add(resetButton);

        content.add(instructionPanel);

        JScrollPane scroll = new JScrollPane(content);
        panel.add(scroll, BorderLayout.CENTER);

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

    public boolean isSaved() { return saved; }
}