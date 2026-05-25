// ui/FormsTreePanel.java
package ru.tmis.analyzer.ui;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormsTreePanel extends JPanel {

    private JTree tree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private JTextField searchField;
    private JButton addButton;
    private JButton removeButton;
    private JButton selectAllButton;
    private JButton deselectAllButton;
    private Runnable onRecursiveAnalysisRequested;

    private Set<String> allForms = new LinkedHashSet<>();
    private List<String> filteredForms = new ArrayList<>();
    private Map<String, Set<String>> childrenCache = new HashMap<>();
    private Map<String, DefaultMutableTreeNode> formNodeMap = new HashMap<>();

    private Runnable onFormsChanged;
    private Runnable onAnalysisRequested;
    private String outputDir = "SQL_info";
    private String projectPath;

    public FormsTreePanel() {
        initUI();
        loadFormsFromFile();
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));

       // JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
       // searchPanel.setBorder(BorderFactory.createTitledBorder("Поиск форм"));

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Введите путь к форме для фильтрации...");

        JButton clearSearchButton = new JButton("Очистить");
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            applyFilter();
        });

        //searchPanel.add(searchField, BorderLayout.CENTER);
        //searchPanel.add(clearSearchButton, BorderLayout.EAST);
        //add(searchPanel, BorderLayout.NORTH);

        rootNode = new DefaultMutableTreeNode("Формы для анализа");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilter();
            }
        });

        createContextMenu();

        JScrollPane treeScroll = new JScrollPane(tree);
       // treeScroll.setBorder(BorderFactory.createTitledBorder("Список форм"));
        add(treeScroll, BorderLayout.CENTER);

        // JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        // addButton = new JButton("➕ Добавить формы");
        // addButton.addActionListener(e -> showAddFormsDialog());
        // removeButton = new JButton("🗑 Удалить выбранные");
        // removeButton.addActionListener(e -> removeSelectedForms());
        //selectAllButton = new JButton("✓ Выбрать всё");
        //selectAllButton.addActionListener(e -> selectAllNodes());
        //deselectAllButton = new JButton("✗ Снять выделение");
        //deselectAllButton.addActionListener(e -> tree.clearSelection());
        //buttonPanel.add(addButton);
        //buttonPanel.add(removeButton);
        //buttonPanel.add(selectAllButton);
        //buttonPanel.add(deselectAllButton);
        // add(buttonPanel, BorderLayout.SOUTH);
    }

    public void addTreeSelectionListener(TreeSelectionListener listener) {
        tree.addTreeSelectionListener(listener);
    }

    public List<String> getSelectedForms() {
        List<String> selectedForms = new ArrayList<>();
        TreePath[] selectedPaths = tree.getSelectionPaths();

        if (selectedPaths != null) {
            for (TreePath path : selectedPaths) {
                String formPath = getFullFormPathFromTreePath(path);
                if (formPath != null) {
                    // Убираем маркер SubForm при получении пути для анализа
                    if (formPath.startsWith("(sub)_")) {
                        formPath = formPath.substring(6);
                    }
                    selectedForms.add(formPath);
                }
            }
        }

        return selectedForms;
    }

    public int getSelectedFormsCount() {
        TreePath[] selectedPaths = tree.getSelectionPaths();
        if (selectedPaths == null) return 0;

        int count = 0;
        for (TreePath path : selectedPaths) {
            if (getFullFormPathFromTreePath(path) != null) {
                count++;
            }
        }
        return count;
    }

    public String getFullFormPathFromTreePath(TreePath path) {
        if (path == null) return null;
        Object[] nodes = path.getPath();
        if (nodes.length < 2) return null;

        String displayName = nodes[nodes.length - 1].toString();

        // Убираем маркер SubForm если есть (только для визуализации)
        String fileName = displayName;
        boolean isSubForm = fileName.startsWith("(sub)_");
        if (isSubForm) {
            fileName = fileName.substring(6);
        }

        if (fileName.startsWith("Forms/") || fileName.startsWith("UserForms")) {
            return fileName;  // Возвращаем путь без маркера
        }

        for (Map.Entry<String, DefaultMutableTreeNode> entry : formNodeMap.entrySet()) {
            if (entry.getValue() == nodes[nodes.length - 1]) {
                return entry.getKey();  // Возвращаем путь без маркера
            }
        }

        if (fileName.endsWith(".frm") || fileName.endsWith(".dfrm")) {
            return "Forms/" + fileName;
        }

        return null;
    }

    public String getFormPathFromTreePath(TreePath path) {
        return getFullFormPathFromTreePath(path);
    }

    public TreePath getSelectedPath() {
        return tree.getSelectionPath();
    }

    public String getReportFilePath(String formPath) {
        // Убираем маркер SubForm если есть
        String actualPath = formPath;
        if (actualPath.startsWith("(sub)_")) {
            actualPath = actualPath.substring(6);
        }

        String normalized = actualPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        String safeName = normalized.replace("/", "#").replace("\\", "#") + ".txt";

        // Возвращаем путь внутри подкаталога Forms
        return outputDir + File.separator + "Forms" + File.separator + safeName;
    }
    public Set<String> loadChildFormsFromReport(String formPath) {
        String actualParentPath = formPath;
        if (actualParentPath.startsWith("(sub)_")) {
            actualParentPath = actualParentPath.substring(6);
        }

        String reportPath = getReportFilePath(actualParentPath);
        File reportFile = new File(reportPath);

        Set<String> childForms = new LinkedHashSet<>();

        if (reportFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(reportFile.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8);

                // ========== 1. ОБРАБОТКА БЛОКА "SubForm:" ==========
                int subFormStartIndex = content.indexOf("SubForm:");
                if (subFormStartIndex != -1) {
                    int subFormEndIndex = content.indexOf("\n\n", subFormStartIndex);
                    if (subFormEndIndex == -1) {
                        String[] nextHeaders = {"Список вызываемых форм в JS:", "Коды подключаемого", "SQL ЗАПРОСЫ", "ИСПОЛЬЗУЕМЫЕ ТАБЛИЦЫ"};
                        for (String header : nextHeaders) {
                            int headerIndex = content.indexOf(header, subFormStartIndex + 10);
                            if (headerIndex != -1) {
                                subFormEndIndex = headerIndex;
                                break;
                            }
                        }
                    }
                    if (subFormEndIndex == -1) {
                        subFormEndIndex = content.length();
                    }

                    String section = content.substring(subFormStartIndex, subFormEndIndex);
                    // Ищем строки с путями (могут быть с расширением .frm или без)
                    Pattern subFormPattern = Pattern.compile("^\\s+([A-Za-z0-9_/]+)(?:\\.frm)?$", Pattern.MULTILINE);
                    Matcher subFormMatcher = subFormPattern.matcher(section);

                    while (subFormMatcher.find()) {
                        String subForm = subFormMatcher.group(1).trim();
                        if (!subForm.isEmpty() && !subForm.equals("SubForm:")) {
                            // Пропускаем вьюхи (D_V_*) и пакеты (D_PKG_*)
                            if (subForm.startsWith("D_V_") || subForm.startsWith("D_PKG_")) {
                                continue;
                            }

                            // Нормализуем путь: добавляем Forms/ если нужно и .frm если нет расширения
                            String subFormFullPath = normalizeFormPathForStorage(subForm);
                            if (subFormFullPath != null) {
                                childForms.add("(sub)_" + subFormFullPath);
                                System.out.println("[SubForm] Добавлена: " + subFormFullPath);
                            }
                        }
                    }
                }

                // ========== 2. ОБРАБОТКА БЛОКА "Список вызываемых форм в JS:" ==========
                int jsStartIndex = content.indexOf("Список вызываемых форм в JS:");
                if (jsStartIndex != -1) {
                    int endIndex = content.indexOf("\n\n", jsStartIndex);
                    if (endIndex == -1) {
                        String[] nextHeaders = {"Коды подключаемого", "SQL ЗАПРОСЫ", "ИСПОЛЬЗУЕМЫЕ ТАБЛИЦЫ"};
                        for (String header : nextHeaders) {
                            int headerIndex = content.indexOf(header, jsStartIndex + 10);
                            if (headerIndex != -1) {
                                endIndex = headerIndex;
                                break;
                            }
                        }
                    }
                    if (endIndex == -1) {
                        endIndex = content.length();
                    }

                    String section = content.substring(jsStartIndex, endIndex);
                    Pattern formPattern = Pattern.compile("\\s+([A-Za-z0-9_/]+\\.frm)");
                    Matcher formMatcher = formPattern.matcher(section);
                    while (formMatcher.find()) {
                        String childForm = formMatcher.group(1).trim();
                        if (!childForm.isEmpty()) {
                            // Пропускаем вьюхи (D_V_*) и пакеты (D_PKG_*)
                            if (childForm.startsWith("D_V_") || childForm.startsWith("D_PKG_")) {
                                continue;
                            }

                            String childFullPath = normalizeFormPathForStorage(childForm);
                            if (childFullPath != null) {
                                childForms.add(childFullPath);
                            }
                        }
                    }
                }

                // ========== 3. ОБРАБОТКА БЛОКА "Отчеты вызываемые на форме" ==========
                int reportStartIndex = content.indexOf("Отчеты вызываемые на форме (коды/формы отчета):");
                if (reportStartIndex != -1) {
                    int reportEndIndex = content.indexOf("\n\n", reportStartIndex);
                    if (reportEndIndex == -1) {
                        reportEndIndex = content.length();
                    }

                    String section = content.substring(reportStartIndex, reportEndIndex);
                    Pattern reportPattern = Pattern.compile("Reports/([^\\s;]+\\.frm)");
                    Matcher reportMatcher = reportPattern.matcher(section);
                    while (reportMatcher.find()) {
                        String reportForm = reportMatcher.group(1);
                        if (!reportForm.isEmpty()) {
                            String reportFullPath = "Reports/" + reportForm;
                            childForms.add(reportFullPath);
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Ошибка чтения файла отчёта: " + e.getMessage());
            }
        }

        return childForms;
    }

    /**
     * Нормализация пути формы для хранения в allForms
     * Пример: "GenRegistry/date_functions" -> "Forms/GenRegistry/date_functions.frm"
     *         "Forms/GenRegistry/date_functions" -> "Forms/GenRegistry/date_functions.frm"
     *         "Forms/GenRegistry/date_functions.frm" -> "Forms/GenRegistry/date_functions.frm"
     */
    private String normalizeFormPathForStorage(String formPath) {
        if (formPath == null || formPath.isEmpty()) return null;

        String normalized = formPath.trim();

        // Убираем ведущий слеш
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        // Убираем маркер SubForm если есть
        if (normalized.startsWith("(sub)_")) {
            normalized = normalized.substring(6);
        }

        // Добавляем префикс Forms/ если путь не начинается с Forms/ или UserForms или Reports
        if (!normalized.startsWith("Forms/") && !normalized.startsWith("UserForms") && !normalized.startsWith("Reports/")) {
            normalized = "Forms/" + normalized;
        }

        // Добавляем расширение .frm если нет расширения
        if (!normalized.endsWith(".frm") && !normalized.endsWith(".dfrm")) {
            normalized = normalized + ".frm";
        }

        return normalized;
    }

    /**
     * Нормализация пути дочерней формы
     */
    private String normalizeChildFormPath(String formPath) {
        if (formPath == null || formPath.trim().isEmpty()) return null;

        String normalized = formPath.trim();

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (!normalized.endsWith(".frm") && !normalized.endsWith(".dfrm")) {
            normalized = normalized + ".frm";
        }

        if (!normalized.startsWith("UserForms") && !normalized.startsWith("Forms/") && !normalized.startsWith("Reports/")) {
            normalized = "Forms/" + normalized;
        }

        return normalized;
    }

    private void addFormWithChildrenToTree(String formPath, DefaultMutableTreeNode parentNode, Set<String> addedPaths) {
        // Пропускаем вьюхи и пакеты
        if (formPath.startsWith("D_V_") || formPath.startsWith("D_PKG_")) {
            return;
        }

        // Пропускаем если не .frm или .dfrm
        if (!formPath.endsWith(".frm") && !formPath.endsWith(".dfrm")) {
            return;
        }

        if (addedPaths.contains(formPath)) {
            return;
        }
        addedPaths.add(formPath);

        // Определяем, является ли это SubForm (имеет маркер)
        boolean isSubForm = formPath.startsWith("(sub)_");
        String actualFormPath = isSubForm ? formPath.substring(6) : formPath;

        // Для отображения используем ПОЛНЫЙ путь
        String displayPath = actualFormPath;
        if (displayPath.startsWith("/")) {
            displayPath = displayPath.substring(1);
        }

        // Добавляем префикс для визуализации SubForm
        if (isSubForm) {
            displayPath = "(sub)_" + displayPath;
        }

        // Проверяем, есть ли уже такой узел
        DefaultMutableTreeNode formNode = null;
        for (int i = 0; i < parentNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parentNode.getChildAt(i);
            if (child.getUserObject().toString().equals(displayPath)) {
                formNode = child;
                break;
            }
        }

        if (formNode == null) {
            formNode = new DefaultMutableTreeNode(displayPath);
            formNode.setAllowsChildren(true);
            parentNode.add(formNode);
            formNodeMap.put(actualFormPath, formNode);
        }
    }

    public void refreshChildForms(String formPath) {
        // Убираем маркер SubForm если есть
        String actualFormPath = formPath;
        if (actualFormPath.startsWith("(sub)_")) {
            actualFormPath = actualFormPath.substring(6);
        }

        // Проверяем существование отчёта
        String reportPath = getReportFilePath(actualFormPath);
        File reportFile = new File(reportPath);

        DefaultMutableTreeNode node = formNodeMap.get(actualFormPath);
        if (node == null) {
            return;
        }

        if (!reportFile.exists()) {
            // Отчёт не существует - очищаем дочерние узлы
            node.removeAllChildren();
            treeModel.reload(node);
            childrenCache.remove(actualFormPath);
            return;
        }

        // Отчёт существует - обновляем дочерние формы
        node.removeAllChildren();

        Set<String> childForms = loadChildFormsFromReport(actualFormPath);
        childrenCache.put(actualFormPath, childForms);

        for (String childForm : childForms) {
            boolean isSubForm = childForm.startsWith("(sub)_");
            String actualChildPath = isSubForm ? childForm.substring(6) : childForm;

            String childDisplayPath;
            if (actualChildPath.startsWith("/")) {
                childDisplayPath = actualChildPath.substring(1);
            } else {
                childDisplayPath = actualChildPath;
            }
            if (!childDisplayPath.startsWith("Forms/") && !childDisplayPath.startsWith("UserForms")) {
                childDisplayPath = "Forms/" + childDisplayPath;
            }
            if (isSubForm) {
                childDisplayPath = "(sub)_" + childDisplayPath;
            }

            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childDisplayPath);
            childNode.setAllowsChildren(true);
            node.add(childNode);
            formNodeMap.put(actualChildPath, childNode);
        }

        treeModel.reload(node);
        TreePath nodePath = new TreePath(node.getPath());
        tree.expandPath(nodePath);
    }


    private void createContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem addMenuItem = new JMenuItem("Добавить формы");
        addMenuItem.addActionListener(e -> showAddFormsDialog());

        JMenuItem removeMenuItem = new JMenuItem("Удалить выбранные");
        removeMenuItem.addActionListener(e -> removeSelectedForms());

        JMenuItem runAnalysisMenuItem = new JMenuItem("Запуск анализа выбранных форм");
        runAnalysisMenuItem.addActionListener(e -> {
            int selectedCount = getSelectedFormsCount();
            if (selectedCount == 0) {
                JOptionPane.showMessageDialog(this,
                        "Не выбрано ни одной формы для анализа",
                        "Нет выбранных форм",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (onAnalysisRequested != null) {
                onAnalysisRequested.run();
            }
        });

        JMenuItem recursiveAnalysisMenuItem = new JMenuItem("Рекурсивный анализ всех форм");
        recursiveAnalysisMenuItem.addActionListener(e -> {
            if (onRecursiveAnalysisRequested != null) {
                onRecursiveAnalysisRequested.run();
            }
        });

        // ========== НОВЫЙ ПУНКТ: ОТКРЫТЬ В STASH ==========
        JMenuItem openInStashMenuItem = new JMenuItem("🌐 Открыть в Stash");
        openInStashMenuItem.addActionListener(e -> openSelectedFormsInStash());

        JMenuItem selectAllMenuItem = new JMenuItem("Выбрать всё");
        selectAllMenuItem.addActionListener(e -> selectAllNodes());

        JMenuItem deselectAllMenuItem = new JMenuItem("Снять выделение");
        deselectAllMenuItem.addActionListener(e -> tree.clearSelection());

        JMenuItem expandAllMenuItem = new JMenuItem("Развернуть всё дерево");
        expandAllMenuItem.addActionListener(e -> {
            TreePath selectedPath = tree.getSelectionPath();
            if (selectedPath != null) {
                String formPath = getFormPathFromTreePath(selectedPath);
                if (formPath != null) {
                    SwingUtilities.invokeLater(() -> {
                        expandAllChildrenRecursive(formPath, selectedPath);
                    });
                }
            }
        });

        popupMenu.add(addMenuItem);
        popupMenu.add(removeMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(runAnalysisMenuItem);
        popupMenu.add(recursiveAnalysisMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(openInStashMenuItem);  // <-- ДОБАВИТЬ СЮДА
        popupMenu.addSeparator();
        popupMenu.add(selectAllMenuItem);
        popupMenu.add(deselectAllMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(expandAllMenuItem);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            private void showContextMenu(MouseEvent e) {
                int row = tree.getClosestRowForLocation(e.getX(), e.getY());
                TreePath path = tree.getPathForRow(row);
                if (path != null) {
                    if (!tree.isPathSelected(path)) {
                        tree.setSelectionPath(path);
                    }
                }
                popupMenu.show(tree, e.getX(), e.getY());
            }
        });
    }

    /**
     * Открывает выбранные формы в Stash (отдельные вкладки браузера)
     */
    private void openSelectedFormsInStash() {
        TreePath[] selectedPaths = tree.getSelectionPaths();
        if (selectedPaths == null || selectedPaths.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Не выбрано ни одной формы",
                    "Нет выбранных форм",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (TreePath path : selectedPaths) {
            String formPath = getFullFormPathFromTreePath(path);
            if (formPath == null || formPath.isEmpty()) continue;

            // Убираем маркер SubForm если есть
            String actualPath = formPath;
            if (actualPath.startsWith("(sub)_")) {
                actualPath = actualPath.substring(6);
            }

            String stashUrl = "https://stash-medmis.bars-open.ru/projects/MED/repos/mis/browse/" + actualPath;

            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new java.net.URI(stashUrl));
                    Thread.sleep(500); // Небольшая задержка между открытием вкладок
                }
            } catch (Exception e) {
                System.err.println("[FormsTreePanel] Ошибка открытия URL для " + formPath + ": " + e.getMessage());
            }
        }
    }

    private void applyFilter() {
        String filter = searchField.getText().trim().toLowerCase();
        rootNode.removeAllChildren();
        formNodeMap.clear();

        // Фильтруем allForms перед отображением
        Set<String> validForms = new LinkedHashSet<>();
        for (String formPath : allForms) {
            // Пропускаем вьюхи и пакеты
            if (formPath.startsWith("D_V_") || formPath.startsWith("D_PKG_")) {
                continue;
            }
            if (!formPath.endsWith(".frm") && !formPath.endsWith(".dfrm")) {
                continue;
            }
            validForms.add(formPath);
        }

        if (filter.isEmpty()) {
            Set<String> addedPaths = new HashSet<>();
            for (String formPath : validForms) {
                addFormWithChildrenToTree(formPath, rootNode, addedPaths);
            }
        } else {
            List<String> matchedForms = new ArrayList<>();
            for (String formPath : validForms) {
                if (formPath.toLowerCase().contains(filter)) {
                    matchedForms.add(formPath);
                }
            }
            matchedForms.sort(String::compareToIgnoreCase);

            for (String formPath : matchedForms) {
                String displayPath = formPath;
                if (displayPath.startsWith("/")) {
                    displayPath = displayPath.substring(1);
                }

                boolean exists = false;
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                    if (child.getUserObject().toString().equals(displayPath)) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    DefaultMutableTreeNode formNode = new DefaultMutableTreeNode(displayPath);
                    formNode.setAllowsChildren(false);
                    rootNode.add(formNode);
                    formNodeMap.put(formPath, formNode);
                }
            }
        }

        treeModel.reload(rootNode);
        expandAllNodes();
    }

    public void expandAllNodes() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    private void showAddFormsDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Добавление форм", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel instructionLabel = new JLabel("Введите пути к формам (каждый путь с новой строки):");
        instructionLabel.setFont(instructionLabel.getFont().deriveFont(Font.BOLD));
        panel.add(instructionLabel, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setRows(15);
        textArea.setLineWrap(false);

        // Определяем разделитель для примера в зависимости от ОС
        String separator = System.getProperty("os.name").toLowerCase().contains("win") ? "\\" : "/";

        StringBuilder examples = new StringBuilder();
        examples.append("# Примеры (можно использовать / или ").append(separator).append("):\n");
        examples.append("# Forms").append(separator).append("Path").append(separator).append("To").append(separator).append("Form.frm\n");
        examples.append("# UserFormsRegion").append(separator).append("Path").append(separator).append("To").append(separator).append("Form.frm\n");
        examples.append("# Или в формате с /: Forms/Path/To/Form.frm\n");
        textArea.setText(examples.toString());
        textArea.selectAll();

        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Добавить");
        JButton cancelButton = new JButton("Отмена");

        addBtn.addActionListener(e -> {
            String input = textArea.getText();
            Set<String> newForms = parseFormPaths(input);
            if (!newForms.isEmpty()) {
                addForms(newForms);
                saveFormsToFile();
                applyFilter();
            }
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addBtn);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private Set<String> parseFormPaths(String input) {
        Set<String> result = new LinkedHashSet<>();
        String[] lines = input.split("\\r?\\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            // Заменяем обратные слеши на прямые (поддержка Windows)
            trimmed = trimmed.replace("\\", "/");

            String normalized = normalizeFormPath(trimmed);
            if (normalized != null && !normalized.isEmpty()) {
                result.add(normalized);
            }
        }

        return result;
    }

    private String normalizeFormPath(String path) {
        if (path == null || path.isEmpty()) return null;

        // Сначала заменяем обратные слеши на прямые (если пользователь ввёл с \)
        String normalized = path.trim().replace("\\", "/");

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        // Проверяем, является ли это UserForms
        boolean isUserForm = normalized.matches("^UserForms[A-Za-z0-9_]*/.*");

        if (!isUserForm) {
            if (!normalized.startsWith("Forms/")) {
                normalized = "Forms/" + normalized;
            }
            if (!normalized.endsWith(".frm") && !normalized.endsWith(".dfrm")) {
                normalized = normalized + ".frm";
            }
        } else {
            if (!normalized.endsWith(".frm") && !normalized.endsWith(".dfrm") && !normalized.contains(".d/")) {
                normalized = normalized + ".frm";
            }
        }

        return normalized;
    }

    private void addForms(Set<String> newForms) {
        int addedCount = 0;
        for (String form : newForms) {
            String normalizedForm = form;
            if (normalizedForm.startsWith("/")) {
                normalizedForm = normalizedForm.substring(1);
            }
            if (allForms.add(normalizedForm)) {
                addedCount++;
            }
        }

        if (addedCount > 0) {
            JOptionPane.showMessageDialog(this,
                    "Добавлено форм: " + addedCount + "\nВсего форм: " + allForms.size(),
                    "Формы добавлены",
                    JOptionPane.INFORMATION_MESSAGE);

            if (onFormsChanged != null) {
                onFormsChanged.run();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Не добавлено ни одной новой формы",
                    "Нет изменений",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeSelectedForms() {
        TreePath[] selectedPaths = tree.getSelectionPaths();
        if (selectedPaths == null || selectedPaths.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Не выбрано ни одной формы для удаления",
                    "Нет выделения",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Set<String> toRemove = new HashSet<>();

        for (TreePath path : selectedPaths) {
            String formPath = getFullFormPathFromTreePath(path);
            if (formPath != null) {
                String normalizedPath = formPath;
                if (normalizedPath.startsWith("/")) {
                    normalizedPath = normalizedPath.substring(1);
                }
                toRemove.add(normalizedPath);
            }
        }

        if (toRemove.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Выберите конкретные формы для удаления",
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить выбранные формы (" + toRemove.size() + " шт.)?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            allForms.removeAll(toRemove);
            childrenCache.clear();
            formNodeMap.clear();
            saveFormsToFile();
            applyFilter();
            tree.clearSelection();

            if (onFormsChanged != null) {
                onFormsChanged.run();
            }

            JOptionPane.showMessageDialog(this,
                    "Удалено форм: " + toRemove.size(),
                    "Формы удалены",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void selectAllNodes() {
        List<TreePath> allPaths = new ArrayList<>();
        // Собираем ВСЕ узлы (включая родительские и корневые)
        collectAllPaths(rootNode, new TreePath(rootNode), allPaths);
        tree.setSelectionPaths(allPaths.toArray(new TreePath[0]));
    }

    /**
     * Рекурсивно собирает ВСЕ пути в дереве (включая родительские узлы)
     */
    private void collectAllPaths(DefaultMutableTreeNode node, TreePath parentPath, List<TreePath> paths) {
        // Добавляем текущий узел (даже если это не лист)
        paths.add(parentPath);

        // Рекурсивно обходим всех детей
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            TreePath childPath = parentPath.pathByAddingChild(child);
            collectAllPaths(child, childPath, paths);
        }
    }



    private void saveFormsToFile() {
        try {
            StringBuilder sb = new StringBuilder();
            for (String form : allForms) {
                sb.append(form).append("\n");
            }
            Files.writeString(Paths.get("forms_list.txt"), sb.toString(),
                    java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения списка форм: " + e.getMessage());
        }
    }

    public List<String> getFormsList() {
        return new ArrayList<>(allForms);
    }

    public void setOnFormsChanged(Runnable callback) {
        this.onFormsChanged = callback;
    }

    public void setOnAnalysisRequested(Runnable callback) {
        this.onAnalysisRequested = callback;
    }

    public void setOnRecursiveAnalysisRequested(Runnable callback) {
        this.onRecursiveAnalysisRequested = callback;
    }

    /**
     * Получить все корневые формы (без учёта иерархии)
     */
    public List<String> getAllRootForms() {
        List<String> allRootForms = new ArrayList<>();
        for (String formPath : allForms) {
            allRootForms.add(formPath);
        }
        return allRootForms;
    }


    /**
     * Сохраняет текущее состояние развёрнутости всех узлов дерева
     *
     * @return карта: полный путь к форме -> был ли узел развёрнут
     */
    public Map<String, Boolean> saveExpandedState() {
        Map<String, Boolean> expandedState = new LinkedHashMap<>();
        saveExpandedStateRecursive(rootNode, new TreePath(rootNode), expandedState);
        return expandedState;
    }

    /**
     * Рекурсивное сохранение состояния развёрнутости
     */
    private void saveExpandedStateRecursive(DefaultMutableTreeNode node, TreePath path, Map<String, Boolean> state) {
        // Сохраняем состояние текущего узла (если это не корневой узел)
        if (node != rootNode && node.getUserObject() != null) {
            String formPath = getFormPathFromNode(node);
            if (formPath != null) {
                boolean isExpanded = tree.isExpanded(path);
                state.put(formPath, isExpanded);
            }
        }

        // Рекурсивно обрабатываем детей
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            TreePath childPath = path.pathByAddingChild(child);
            saveExpandedStateRecursive(child, childPath, state);
        }
    }

    /**
     * Восстанавливает состояние развёрнутости узлов дерева
     *
     * @param expandedState карта с сохранённым состоянием
     */
    public void restoreExpandedState(Map<String, Boolean> expandedState) {
        if (expandedState == null || expandedState.isEmpty()) {
            return;
        }

        // Сначала разворачиваем все узлы, которые были развёрнуты
        for (Map.Entry<String, Boolean> entry : expandedState.entrySet()) {
            if (entry.getValue()) {
                String formPath = entry.getKey();
                DefaultMutableTreeNode node = formNodeMap.get(formPath);
                if (node != null) {
                    TreePath path = new TreePath(node.getPath());
                    tree.expandPath(path);
                }
            }
        }

        // Обновляем отображение
        tree.revalidate();
        tree.repaint();
    }

    /**
     * Получить путь формы из узла дерева
     */
    private String getFormPathFromNode(DefaultMutableTreeNode node) {
        if (node == null || node.getUserObject() == null) return null;

        String displayPath = node.getUserObject().toString();

        // Ищем соответствие в formNodeMap
        for (Map.Entry<String, DefaultMutableTreeNode> entry : formNodeMap.entrySet()) {
            if (entry.getValue() == node) {
                return entry.getKey();
            }
        }

        // Если не нашли, пробуем восстановить из displayPath
        if (displayPath.startsWith("Forms/") || displayPath.startsWith("UserForms")) {
            return displayPath;
        }

        return null;
    }

    /**
     * Полный рефреш дерева с сохранением состояния
     */
    public void refreshTreePreservingState() {
        // Сохраняем текущее состояние
        Map<String, Boolean> expandedState = saveExpandedState();
        Set<String> selectedForms = new HashSet<>(getSelectedForms());

        // Перестраиваем дерево
        applyFilter();

        // Восстанавливаем выделение
        restoreSelection(selectedForms);

        // Восстанавливаем развёрнутость
        restoreExpandedState(expandedState);
    }

    /**
     * Восстанавливает выделенные формы
     */
    private void restoreSelection(Set<String> selectedForms) {
        if (selectedForms.isEmpty()) return;

        List<TreePath> pathsToSelect = new ArrayList<>();
        for (String formPath : selectedForms) {
            DefaultMutableTreeNode node = formNodeMap.get(formPath);
            if (node != null) {
                pathsToSelect.add(new TreePath(node.getPath()));
            }
        }

        if (!pathsToSelect.isEmpty()) {
            tree.setSelectionPaths(pathsToSelect.toArray(new TreePath[0]));
        }
    }

    /**
     * Обновить дочерние формы для конкретной формы с сохранением состояния дерева
     */
    public void refreshChildFormsPreservingState(String formPath) {
        // Сохраняем состояние только для поддерева этой формы
        Map<String, Boolean> expandedState = new LinkedHashMap<>();
        DefaultMutableTreeNode node = formNodeMap.get(formPath);
        if (node != null) {
            saveExpandedStateRecursive(node, new TreePath(node.getPath()), expandedState);
            Set<String> selectedForms = new HashSet<>(getSelectedForms());

            // Обновляем
            refreshChildForms(formPath);

            // Восстанавливаем
            restoreExpandedState(expandedState);
            restoreSelection(selectedForms);
        } else {
            refreshChildForms(formPath);
        }
    }

    /**
     * Проверяет существование отчёта для формы и очищает дочерние узлы если отчёт удалён
     *
     * @param formPath путь к форме
     * @return true если отчёт существует, false если нет
     */
    public boolean checkAndCleanIfReportMissing(String formPath) {
        String reportPath = getReportFilePath(formPath);
        File reportFile = new File(reportPath);

        if (!reportFile.exists()) {
            // Отчёт не найден - очищаем дочерние узлы
            DefaultMutableTreeNode node = formNodeMap.get(formPath);
            if (node != null) {
                node.removeAllChildren();
                treeModel.reload(node);
            }
            // Также очищаем кэш
            childrenCache.remove(formPath);
            return false;
        }
        return true;
    }

    public void clearChildNodes(String formPath) {
        DefaultMutableTreeNode node = formNodeMap.get(formPath);
        if (node != null) {
            node.removeAllChildren();
            treeModel.reload(node);
        }
        childrenCache.remove(formPath);
    }

    /**
     * Обновляет все дочерние формы, удаляя узлы для которых нет отчётов
     * С сохранением состояния дерева
     */
    public void refreshAllChildFormsWithCleanup() {
        TreeState state = saveTreeState();

        Set<String> formsToRemove = new HashSet<>();

        // Проверяем все формы в map
        for (Map.Entry<String, DefaultMutableTreeNode> entry : formNodeMap.entrySet()) {
            String formPath = entry.getKey();
            String reportPath = getReportFilePath(formPath);
            File reportFile = new File(reportPath);

            if (!reportFile.exists()) {
                formsToRemove.add(formPath);
            }
        }

        // Очищаем узлы для которых нет отчётов
        for (String formPath : formsToRemove) {
            clearChildNodes(formPath);
        }

        // Обновляем остальные формы (только те, у которых есть отчёты)
        for (String formPath : allForms) {
            String reportPath = getReportFilePath(formPath);
            File reportFile = new File(reportPath);
            if (reportFile.exists()) {
                refreshChildForms(formPath);
            }
        }

        // Перестраиваем корневой узел
        treeModel.reload(rootNode);

        // Восстанавливаем состояние
        restoreTreeState(state);
    }

    /**
     * Сохраняет полное состояние дерева (развёрнутость узлов и выбранный элемент)
     *
     * @return объект состояния дерева
     */
    public TreeState saveTreeState() {
        TreeState state = new TreeState();
        state.selectedPath = saveSelectedPath();
        state.expandedPaths = saveExpandedPaths();
        return state;
    }

    /**
     * Восстанавливает состояние дерева
     *
     * @param state сохранённое состояние
     */
    public void restoreTreeState(TreeState state) {
        if (state == null) return;

        // Сначала раскрываем все сохранённые пути
        if (state.expandedPaths != null) {
            for (String pathStr : state.expandedPaths) {
                TreePath path = findTreePathByDisplayString(pathStr);
                if (path != null) {
                    tree.expandPath(path);
                }
            }
        }

        // Затем восстанавливаем выбранный элемент
        if (state.selectedPath != null && !state.selectedPath.isEmpty()) {
            restoreSelectedPath(state.selectedPath);
        }
    }

    /**
     * Сохраняет список развёрнутых путей в дереве
     */
    private Set<String> saveExpandedPaths() {
        Set<String> expandedPaths = new LinkedHashSet<>();
        saveExpandedPathsRecursive(rootNode, new TreePath(rootNode), expandedPaths);
        return expandedPaths;
    }

    /**
     * Рекурсивное сохранение развёрнутых путей
     */
    private void saveExpandedPathsRecursive(DefaultMutableTreeNode node, TreePath path, Set<String> expandedPaths) {
        if (tree.isExpanded(path)) {
            // Сохраняем путь как строку отображаемых имён
            StringBuilder sb = new StringBuilder();
            Object[] nodes = path.getPath();
            for (int i = 1; i < nodes.length; i++) { // пропускаем корневой узел
                if (sb.length() > 0) sb.append("||");
                sb.append(nodes[i].toString());
            }
            if (sb.length() > 0) {
                expandedPaths.add(sb.toString());
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            TreePath childPath = path.pathByAddingChild(child);
            saveExpandedPathsRecursive(child, childPath, expandedPaths);
        }
    }

    /**
     * Находит TreePath по строке отображаемых имён
     */
    private TreePath findTreePathByDisplayString(String pathStr) {
        if (pathStr == null || pathStr.isEmpty()) return null;

        String[] pathParts = pathStr.split("\\|\\|");
        if (pathParts.length == 0) return null;

        DefaultMutableTreeNode currentNode = rootNode;
        TreePath currentPath = new TreePath(rootNode);

        for (String part : pathParts) {
            DefaultMutableTreeNode foundChild = null;
            for (int i = 0; i < currentNode.getChildCount(); i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) currentNode.getChildAt(i);
                if (child.getUserObject().toString().equals(part)) {
                    foundChild = child;
                    break;
                }
            }
            if (foundChild != null) {
                currentNode = foundChild;
                currentPath = currentPath.pathByAddingChild(foundChild);
            } else {
                return null;
            }
        }

        return currentPath;
    }

    /**
     * Сохраняет текущий выбранный путь в дереве
     */
    public String saveSelectedPath() {
        TreePath selectedPath = tree.getSelectionPath();
        if (selectedPath == null) return null;

        StringBuilder sb = new StringBuilder();
        Object[] nodes = selectedPath.getPath();
        for (int i = 1; i < nodes.length; i++) {
            if (sb.length() > 0) sb.append("||");
            sb.append(nodes[i].toString());
        }
        return sb.toString();
    }

    /**
     * Восстанавливает выбранный путь в дереве
     */
    public void restoreSelectedPath(String savedPath) {
        if (savedPath == null || savedPath.isEmpty()) return;

        TreePath path = findTreePathByDisplayString(savedPath);
        if (path != null) {
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        }
    }

    /**
     * Полное обновление дерева с сохранением состояния
     */
    public void refreshTreeWithState() {
        TreeState state = saveTreeState();

        // Перестраиваем дерево (сохраняем все формы)
        applyFilter();

        // Восстанавливаем состояние
        restoreTreeState(state);
    }

    /**
     * Класс для хранения состояния дерева
     */
    public static class TreeState {
        public String selectedPath;
        public Set<String> expandedPaths;
    }

    /**
     * Раскрывает путь по строке отображаемых имён
     */
    public void expandPathByDisplayString(String pathStr) {
        TreePath path = findTreePathByDisplayString(pathStr);
        if (path != null) {
            tree.expandPath(path);
        }
    }


    // FormsTreePanel.java - добавить методы

    /**
     * Раскрывает указанный путь в дереве
     *
     * @param path путь для раскрытия
     */
    public void expandPath(TreePath path) {
        if (path != null) {
            tree.expandPath(path);
        }
    }

    /**
     * Находит узел дерева по пути формы
     *
     * @param formPath путь к форме (без маркера)
     * @return узел дерева или null
     */
    public DefaultMutableTreeNode findNodeByFormPath(String formPath) {
        return formNodeMap.get(formPath);
    }

    /**
     * Получает TreePath для узла
     *
     * @param node узел дерева
     * @return TreePath или null
     */
    public TreePath getTreePathForNode(DefaultMutableTreeNode node) {
        if (node == null) return null;
        return new TreePath(node.getPath());
    }

    /**
     * Рекурсивно загружает все дочерние формы для указанного пути
     *
     * @param formPath        путь к форме
     * @param expandAfterLoad раскрывать ли узлы после загрузки
     */
    public void loadAllChildrenRecursively(String formPath, boolean expandAfterLoad) {
        // Загружаем дочерние формы из отчёта
        Set<String> childForms = loadChildFormsFromReport(formPath);

        if (childForms.isEmpty()) return;

        // Обновляем дочерние узлы в дереве
        refreshChildForms(formPath);

        // Находим узел текущей формы
        DefaultMutableTreeNode node = formNodeMap.get(formPath);
        if (node == null) return;

        if (expandAfterLoad) {
            TreePath path = new TreePath(node.getPath());
            tree.expandPath(path);
        }

        // Рекурсивно загружаем дочерние формы
        for (String childForm : childForms) {
            String actualChildPath = childForm.startsWith("(sub)_") ? childForm.substring(6) : childForm;
            loadAllChildrenRecursively(actualChildPath, expandAfterLoad);
        }
    }

    /**
     * Загружает все дочерние формы для выбранного пути и раскрывает дерево
     *
     * @param formPath путь к форме
     */
    public void loadFullTree(String formPath) {
        // Сначала обновляем текущий узел
        refreshChildForms(formPath);

        // Затем рекурсивно загружаем всех детей
        loadAllChildrenRecursively(formPath, true);

        // Находим и раскрываем узел
        DefaultMutableTreeNode node = formNodeMap.get(formPath);
        if (node != null) {
            TreePath path = new TreePath(node.getPath());
            tree.expandPath(path);
            tree.setSelectionPath(path);
        }
    }

    /**
     * Устанавливает выбранный путь в дереве
     *
     * @param path путь для выбора
     */
    public void setSelectedPath(TreePath path) {
        if (path != null) {
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        }
    }

    /**
     * Обновить все дочерние формы (без сохранения состояния)
     */
    public void refreshAllChildForms() {
        for (String formPath : allForms) {
            refreshChildForms(formPath);
        }
    }

    /**
     * Обновить все дочерние формы с сохранением состояния
     */
    public void refreshAllChildFormsPreservingState() {
        Map<String, Boolean> expandedState = saveExpandedState();
        Set<String> selectedForms = new HashSet<>(getSelectedForms());

        refreshAllChildForms();

        restoreExpandedState(expandedState);
        restoreSelection(selectedForms);
    }

    /**
     * Рекурсивно разворачивает все дочерние узлы для указанного пути
     *
     * @param parentPath путь к родительскому узлу
     */
    public void expandAllChildren(TreePath parentPath) {
        if (parentPath == null) return;

        Object lastComponent = parentPath.getLastPathComponent();
        if (lastComponent instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastComponent;

            // Разворачиваем текущий узел
            tree.expandPath(parentPath);

            // Рекурсивно разворачиваем всех детей
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                TreePath childPath = parentPath.pathByAddingChild(child);
                expandAllChildren(childPath);
            }
        }
    }

    /**
     * Рекурсивно разворачивает все дочерние узлы для формы по пути
     *
     * @param formPath путь к форме
     */
    public void expandAllChildrenForForm(String formPath) {
        DefaultMutableTreeNode node = formNodeMap.get(formPath);
        if (node != null) {
            TreePath path = new TreePath(node.getPath());
            expandAllChildren(path);
        }
    }

    /**
     * Рекурсивно загружает и разворачивает все дочерние формы для указанного пути
     *
     * @param formPath путь к форме
     * @param treePath путь в дереве
     */
    public void expandAllChildrenRecursive(String formPath, TreePath treePath) {
        // Загружаем дочерние формы из отчёта
        Set<String> childForms = loadChildFormsFromReport(formPath);

        if (childForms.isEmpty()) return;

        // Обновляем дочерние узлы в дереве
        refreshChildForms(formPath);

        // Раскрываем текущий узел
        if (treePath != null) {
            tree.expandPath(treePath);
        }

        // Рекурсивно обрабатываем каждую дочернюю форму
        for (String childForm : childForms) {
            String actualChildPath = childForm;
            if (actualChildPath.startsWith("(sub)_")) {
                actualChildPath = actualChildPath.substring(6);
            }

            DefaultMutableTreeNode childNode = formNodeMap.get(actualChildPath);
            if (childNode != null) {
                TreePath childPath = new TreePath(childNode.getPath());
                // Рекурсивный вызов для дочерней формы
                expandAllChildrenRecursive(actualChildPath, childPath);
            }
        }
    }



    /**
     * Загружает формы из файла forms_list.txt
     */
    private void loadFormsFromFile() {
        File file = new File("forms_list.txt");
        if (file.exists()) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8);
                Set<String> loadedForms = parseFormPaths(content);
                allForms.clear();

                for (String form : loadedForms) {
                    // Фильтруем: только формы (.frm, .dfrm), исключаем вьюхи D_V_* и пакеты D_PKG_*
                    if (isValidFormFile(form)) {
                        allForms.add(form);
                    }
                }
                applyFilter();
            } catch (IOException e) {
                System.err.println("Ошибка загрузки списка форм: " + e.getMessage());
            }
        }

        // Если после загрузки из файла форм нет - загружаем из отчётов
        if (allForms.isEmpty()) {
            loadFormsFromReports();
        }
    }


    /**
     * Проверяет, является ли путь валидным файлом формы (не вьюхой и не пакетом)
     */
    private boolean isValidFormFile(String path) {
        if (path == null || path.isEmpty()) return false;

        // Исключаем вьюхи (D_V_*)
        if (path.contains("/D_V_") || path.startsWith("D_V_")) {
            return false;
        }

        // Исключаем пакеты (D_PKG_*)
        if (path.contains("/D_PKG_") || path.startsWith("D_PKG_")) {
            return false;
        }

        // Должно быть расширение .frm или .dfrm
        return path.endsWith(".frm") || path.endsWith(".dfrm");
    }

    /**
     * Загружает формы из отчётов (только из блоков SubForm и JS Forms)
     */
    public void loadFormsFromReports() {
        allForms.clear();
        Set<String> collectedForms = new LinkedHashSet<>();

        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "SQL_info";
        }

        Path formsDir = Paths.get(outputDir, "Forms");
        if (!Files.exists(formsDir)) {
            System.out.println("[FormsTreePanel] Директория отчётов не найдена: " + formsDir);
            return;
        }

        try (Stream<Path> walk = Files.walk(formsDir)) {
            List<Path> reportFiles = walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .collect(Collectors.toList());

            System.out.println("[FormsTreePanel] Найдено отчётов: " + reportFiles.size());

            for (Path reportFile : reportFiles) {
                try {
                    String content = Files.readString(reportFile);

                    // Извлекаем формы из блока "SubForm:"
                    int subFormStart = content.indexOf("SubForm:");
                    if (subFormStart != -1) {
                        int subFormEnd = content.indexOf("\n\n", subFormStart);
                        if (subFormEnd == -1) subFormEnd = content.length();
                        String subFormSection = content.substring(subFormStart, subFormEnd);

                        Pattern pattern = Pattern.compile("^\\s+([A-Za-z0-9_/]+\\.(?:frm|dfrm))", Pattern.MULTILINE);
                        Matcher matcher = pattern.matcher(subFormSection);
                        while (matcher.find()) {
                            String formPath = matcher.group(1);
                            String fullPath = normalizeFormPathForStorage(formPath);
                            if (fullPath != null && isValidFormFile(fullPath)) {
                                collectedForms.add(fullPath);
                                System.out.println("[FormsTreePanel] Найдена SubForm: " + fullPath);
                            }
                        }
                    }

                    // Извлекаем формы из блока "Список вызываемых форм в JS:"
                    int jsStart = content.indexOf("Список вызываемых форм в JS:");
                    if (jsStart != -1) {
                        int jsEnd = content.indexOf("\n\n", jsStart);
                        if (jsEnd == -1) jsEnd = content.length();
                        String jsSection = content.substring(jsStart, jsEnd);

                        Pattern pattern = Pattern.compile("\\s+([A-Za-z0-9_/]+\\.(?:frm|dfrm))");
                        Matcher matcher = pattern.matcher(jsSection);
                        while (matcher.find()) {
                            String formPath = matcher.group(1);
                            String fullPath = normalizeFormPathForStorage(formPath);
                            if (fullPath != null && isValidFormFile(fullPath)) {
                                collectedForms.add(fullPath);
                                System.out.println("[FormsTreePanel] Найдена JS форма: " + fullPath);
                            }
                        }
                    }

                    // Извлекаем формы отчётов из блока "Отчеты вызываемые на форме"
                    int reportStart = content.indexOf("Отчеты вызываемые на форме");
                    if (reportStart != -1) {
                        int reportEnd = content.indexOf("\n\n", reportStart);
                        if (reportEnd == -1) reportEnd = content.length();
                        String reportSection = content.substring(reportStart, reportEnd);

                        Pattern pattern = Pattern.compile("Reports/([A-Za-z0-9_/]+\\.frm)");
                        Matcher matcher = pattern.matcher(reportSection);
                        while (matcher.find()) {
                            String formPath = "Reports/" + matcher.group(1);
                            if (isValidFormFile(formPath)) {
                                collectedForms.add(formPath);
                                System.out.println("[FormsTreePanel] Найдена форма отчёта: " + formPath);
                            }
                        }
                    }

                } catch (IOException e) {
                    System.err.println("Ошибка чтения файла: " + reportFile);
                }
            }

            allForms.addAll(collectedForms);
            System.out.println("[FormsTreePanel] Всего собрано форм из отчётов: " + allForms.size());

            // Сохраняем в файл для будущих запусков
            saveFormsToFile();
            applyFilter();

        } catch (IOException e) {
            System.err.println("Ошибка сканирования отчётов: " + e.getMessage());
        }
    }


    /**
     * Добавляет форму в дерево (создаёт узел) - используется для ручного добавления
     */
    public void addFormToTree(String formPath) {
        if (!isValidFormFile(formPath)) {
            System.out.println("[FormsTreePanel] Неверный путь формы: " + formPath);
            return;
        }

        String normalizedPath = normalizeFormPathForStorage(formPath);
        if (normalizedPath != null && allForms.add(normalizedPath)) {
            addFormWithChildrenToTree(normalizedPath, rootNode, new HashSet<>());
            treeModel.reload(rootNode);
            saveFormsToFile();
        }
    }
}