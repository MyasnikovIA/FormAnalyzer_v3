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

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Поиск форм"));

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Введите путь к форме для фильтрации...");

        JButton clearSearchButton = new JButton("Очистить");
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            applyFilter();
        });

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(clearSearchButton, BorderLayout.EAST);
        add(searchPanel, BorderLayout.NORTH);

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
        treeScroll.setBorder(BorderFactory.createTitledBorder("Список форм"));
        add(treeScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        addButton = new JButton("➕ Добавить формы");
        addButton.addActionListener(e -> showAddFormsDialog());

        removeButton = new JButton("🗑 Удалить выбранные");
        removeButton.addActionListener(e -> removeSelectedForms());

        selectAllButton = new JButton("✓ Выбрать всё");
        selectAllButton.addActionListener(e -> selectAllNodes());

        deselectAllButton = new JButton("✗ Снять выделение");
        deselectAllButton.addActionListener(e -> tree.clearSelection());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(selectAllButton);
        buttonPanel.add(deselectAllButton);

        add(buttonPanel, BorderLayout.SOUTH);
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
        return outputDir + File.separator + safeName;
    }

    public Set<String> loadChildFormsFromReport(String formPath) {
        // Убираем маркер SubForm если есть (для родительской формы)
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

                    Pattern subFormPattern = Pattern.compile("^\\s+([^\\s]+)$", Pattern.MULTILINE);
                    Matcher subFormMatcher = subFormPattern.matcher(section);

                    while (subFormMatcher.find()) {
                        String subForm = subFormMatcher.group(1).trim();
                        if (!subForm.isEmpty() && !subForm.equals("SubForm:")) {
                            String subFormFullPath;
                            if (subForm.startsWith("Forms/") || subForm.startsWith("UserForms")) {
                                subFormFullPath = subForm;
                            } else if (subForm.contains("/")) {
                                subFormFullPath = "Forms/" + subForm;
                            } else {
                                subFormFullPath = "Forms/" + subForm;
                            }
                            // Добавляем с маркером для визуализации, но путь сохраняем без маркера
                            // Маркер будет использоваться только при отображении в дереве
                            childForms.add("(sub)_" + subFormFullPath);
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
                    Pattern formPattern = Pattern.compile("\\s+([^\\s]+\\.frm)");
                    Matcher formMatcher = formPattern.matcher(section);
                    while (formMatcher.find()) {
                        String childForm = formMatcher.group(1).trim();
                        if (!childForm.isEmpty()) {
                            String childFullPath;
                            if (childForm.startsWith("Forms/") || childForm.startsWith("UserForms")) {
                                childFullPath = childForm;
                            } else {
                                childFullPath = "Forms/" + childForm;
                            }
                            childForms.add(childFullPath);
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Ошибка чтения файла отчёта: " + e.getMessage());
            }
        }

        return childForms;
    }

    private void addFormWithChildrenToTree(String formPath, DefaultMutableTreeNode parentNode, Set<String> addedPaths) {
        if (addedPaths.contains(formPath)) {
            return;
        }
        addedPaths.add(formPath);

        // Определяем, является ли это SubForm (имеет маркер)
        boolean isSubForm = formPath.startsWith("(sub)_");
        String actualFormPath = isSubForm ? formPath.substring(6) : formPath;

        // Для отображения используем полный путь, начинающийся с Forms/
        String displayPath;
        if (actualFormPath.startsWith("/")) {
            displayPath = actualFormPath.substring(1);
        } else {
            displayPath = actualFormPath;
        }
        if (!displayPath.startsWith("Forms/") && !displayPath.startsWith("UserForms")) {
            displayPath = "Forms/" + displayPath;
        }

        // Добавляем префикс для визуализации SubForm
        if (isSubForm) {
            displayPath = "(sub)_" + displayPath;
        }

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
            // Сохраняем в map без маркера
            formNodeMap.put(actualFormPath, formNode);
        }

        String reportPath = getReportFilePath(actualFormPath);
        File reportFile = new File(reportPath);

        if (reportFile.exists()) {
            Set<String> childForms = loadChildFormsFromReport(actualFormPath);
            childrenCache.put(actualFormPath, childForms);

            for (String childForm : childForms) {
                addFormWithChildrenToTree(childForm, formNode, addedPaths);
            }
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

    public void refreshAllChildForms() {
        refreshAllChildFormsPreservingState();
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

        JMenuItem selectAllMenuItem = new JMenuItem("Выбрать всё");
        selectAllMenuItem.addActionListener(e -> selectAllNodes());

        JMenuItem deselectAllMenuItem = new JMenuItem("Снять выделение");
        deselectAllMenuItem.addActionListener(e -> tree.clearSelection());

        popupMenu.add(addMenuItem);
        popupMenu.add(removeMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(runAnalysisMenuItem);
        popupMenu.add(recursiveAnalysisMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(selectAllMenuItem);
        popupMenu.add(deselectAllMenuItem);


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

   private void applyFilter() {
        String filter = searchField.getText().trim().toLowerCase();
        rootNode.removeAllChildren();
        formNodeMap.clear();
        Set<String> addedPaths = new HashSet<>();

        if (filter.isEmpty()) {
            for (String formPath : allForms) {
                addFormWithChildrenToTree(formPath, rootNode, addedPaths);
            }
        } else {
            filteredForms.clear();
            for (String formPath : allForms) {
                if (formPath.toLowerCase().contains(filter)) {
                    filteredForms.add(formPath);
                    addFormWithChildrenToTree(formPath, rootNode, addedPaths);
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

        StringBuilder examples = new StringBuilder();
        examples.append("# Примеры:\n");
        examples.append("# Forms/Path/To/Form.frm\n");
        examples.append("# UserFormsRegion/Path/To/Form.frm\n");
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

            String normalized = normalizeFormPath(trimmed);
            if (normalized != null && !normalized.isEmpty()) {
                result.add(normalized);
            }
        }

        return result;
    }

    private String normalizeFormPath(String path) {
        if (path == null || path.isEmpty()) return null;

        String normalized = path.trim();

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

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
        collectAllLeafPaths(rootNode, new TreePath(rootNode), allPaths);
        tree.setSelectionPaths(allPaths.toArray(new TreePath[0]));
    }

    private void collectAllLeafPaths(DefaultMutableTreeNode node, TreePath parentPath, List<TreePath> paths) {
        if (node.isLeaf()) {
            paths.add(parentPath);
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                TreePath childPath = parentPath.pathByAddingChild(child);
                collectAllLeafPaths(child, childPath, paths);
            }
        }
    }

    private void loadFormsFromFile() {
        File file = new File("forms_list.txt");
        if (file.exists()) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8);
                Set<String> loadedForms = parseFormPaths(content);
                allForms.clear();
                allForms.addAll(loadedForms);
                applyFilter();
            } catch (IOException e) {
                System.err.println("Ошибка загрузки списка форм: " + e.getMessage());
            }
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
     * @param path путь для раскрытия
     */
    public void expandPath(TreePath path) {
        if (path != null) {
            tree.expandPath(path);
        }
    }

    /**
     * Находит узел дерева по пути формы
     * @param formPath путь к форме (без маркера)
     * @return узел дерева или null
     */
    public DefaultMutableTreeNode findNodeByFormPath(String formPath) {
        return formNodeMap.get(formPath);
    }

    /**
     * Получает TreePath для узла
     * @param node узел дерева
     * @return TreePath или null
     */
    public TreePath getTreePathForNode(DefaultMutableTreeNode node) {
        if (node == null) return null;
        return new TreePath(node.getPath());
    }

    /**
     * Рекурсивно загружает все дочерние формы для указанного пути
     * @param formPath путь к форме
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
}