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

    private Set<String> allForms = new LinkedHashSet<>();
    private List<String> filteredForms = new ArrayList<>();
    private Map<String, Set<String>> childrenCache = new HashMap<>();

    private Runnable onFormsChanged;
    private Runnable onAnalysisRequested;
    private String outputDir = "SQL_info";

    // Карта для хранения соответствия между узлами дерева и путями форм
    private Map<String, DefaultMutableTreeNode> formNodeMap = new HashMap<>();

    public FormsTreePanel() {
        initUI();
        loadFormsFromFile();
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
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

    /**
     * Добавляет слушатель выбора в дерево
     */
    public void addTreeSelectionListener(TreeSelectionListener listener) {
        tree.addTreeSelectionListener(listener);
    }

    /**
     * Обновляет дочерние формы для конкретной формы
     */
    public void refreshChildForms(String formPath) {
        // Очищаем кэш для этой формы
        childrenCache.remove(formPath);

        // Находим узел в дереве
        DefaultMutableTreeNode node = formNodeMap.get(formPath);
        if (node != null) {
            // Удаляем все дочерние узлы
            node.removeAllChildren();

            // Загружаем и добавляем дочерние формы
            Set<String> childForms = loadChildFormsFromReport(formPath);
            childrenCache.put(formPath, childForms);

            for (String childForm : childForms) {
                String childDisplayPath = childForm;
                if (childDisplayPath.startsWith("/")) {
                    childDisplayPath = childDisplayPath.substring(1);
                }

                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childDisplayPath);
                childNode.setAllowsChildren(true);
                node.add(childNode);
                formNodeMap.put(childForm, childNode);
            }

            // Обновляем модель дерева
            treeModel.reload(node);

            // Разворачиваем узел, чтобы показать дочерние элементы
            TreePath nodePath = new TreePath(node.getPath());
            tree.expandPath(nodePath);
        }
    }

    /**
     * Обновляет все дочерние формы для всех форм в дереве
     */
    public void refreshAllChildForms() {
        for (String formPath : allForms) {
            refreshChildForms(formPath);
        }
    }

    /**
     * Преобразует путь формы в безопасное имя файла для отчёта
     */
    private String getSafeFileNameForReport(String formPath) {
        String normalized = formPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized.replace("/", "#").replace("\\", "#") + ".txt";
    }

    /**
     * Возвращает полный путь к файлу отчёта для формы
     */
    public String getReportFilePath(String formPath) {
        String safeName = getSafeFileNameForReport(formPath);
        return outputDir + File.separator + safeName;
    }

    /**
     * Загружает дочерние формы из файла отчёта
     */
    public Set<String> loadChildFormsFromReport(String formPath) {
        String reportPath = getReportFilePath(formPath);
        File reportFile = new File(reportPath);

        Set<String> childForms = new LinkedHashSet<>();

        if (reportFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(reportFile.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8);

                // Ищем секцию "Список вызываемых форм в JS:"
                int startIndex = content.indexOf("Список вызываемых форм в JS:");
                if (startIndex != -1) {
                    int endIndex = content.indexOf("\n\n", startIndex);
                    if (endIndex == -1) {
                        String[] nextHeaders = {"Коды подключаемого", "SQL ЗАПРОСЫ", "ИСПОЛЬЗУЕМЫЕ ТАБЛИЦЫ"};
                        for (String header : nextHeaders) {
                            int headerIndex = content.indexOf(header, startIndex + 10);
                            if (headerIndex != -1) {
                                endIndex = headerIndex;
                                break;
                            }
                        }
                    }
                    if (endIndex == -1) {
                        endIndex = content.length();
                    }

                    String section = content.substring(startIndex, endIndex);

                    // Ищем все строки с .frm
                    Pattern formPattern = Pattern.compile("\\s+([^\\s]+\\.frm)");
                    Matcher formMatcher = formPattern.matcher(section);
                    while (formMatcher.find()) {
                        String childForm = formMatcher.group(1).trim();
                        if (!childForm.isEmpty()) {
                            // Формируем полный путь к дочерней форме
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

    /**
     * Рекурсивно добавляет форму и все её дочерние формы в дерево
     */
    private void addFormWithChildrenToTree(String formPath, DefaultMutableTreeNode parentNode, Set<String> addedPaths) {
        if (addedPaths.contains(formPath)) {
            return;
        }
        addedPaths.add(formPath);

        String displayPath = formPath;
        if (displayPath.startsWith("/")) {
            displayPath = displayPath.substring(1);
        }

        // Проверяем, существует ли уже такой узел
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
            formNodeMap.put(formPath, formNode);
        }

        // Загружаем дочерние формы (только если файл отчёта существует)
        String reportPath = getReportFilePath(formPath);
        File reportFile = new File(reportPath);

        if (reportFile.exists()) {
            Set<String> childForms = loadChildFormsFromReport(formPath);
            childrenCache.put(formPath, childForms);

            for (String childForm : childForms) {
                addFormWithChildrenToTree(childForm, formNode, addedPaths);
            }
        }
    }

    private void createContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem addMenuItem = new JMenuItem("Добавить формы");
        addMenuItem.addActionListener(e -> showAddFormsDialog());

        JMenuItem removeMenuItem = new JMenuItem("Удалить выбранные");
        removeMenuItem.addActionListener(e -> removeSelectedForms());

        JMenuItem runAnalysisMenuItem = new JMenuItem("Запуск анализа");
        runAnalysisMenuItem.addActionListener(e -> {
            if (onAnalysisRequested != null) {
                onAnalysisRequested.run();
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
                    tree.setSelectionPath(path);
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

    private void expandAllNodes() {
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
            String formPath = getFormPathFromTreePath(path);
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

    /**
     * Извлекает полный путь формы из узла дерева
     */
    public String getFormPathFromTreePath(TreePath path) {
        if (path == null) return null;
        Object[] nodes = path.getPath();
        if (nodes.length < 2) return null;

        StringBuilder fullPath = new StringBuilder();
        for (int i = 1; i < nodes.length; i++) {
            if (i > 1) fullPath.append("/");
            fullPath.append(nodes[i].toString());
        }

        String result = fullPath.toString();

        if (result.endsWith(".frm") || result.endsWith(".dfrm")) {
            if (!result.startsWith("UserForms") && !result.startsWith("Forms/") && !result.startsWith("/")) {
                result = "Forms/" + result;
            }
            return result;
        }

        return null;
    }

    public TreePath getSelectedPath() {
        return tree.getSelectionPath();
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
}