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

        String fileName = nodes[nodes.length - 1].toString();

        if (fileName.startsWith("Forms/") || fileName.startsWith("UserForms")) {
            return fileName;
        }

        for (Map.Entry<String, DefaultMutableTreeNode> entry : formNodeMap.entrySet()) {
            if (entry.getValue() == nodes[nodes.length - 1]) {
                return entry.getKey();
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
        String normalized = formPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        String safeName = normalized.replace("/", "#").replace("\\", "#") + ".txt";
        return outputDir + File.separator + safeName;
    }

    public Set<String> loadChildFormsFromReport(String formPath) {
        String reportPath = getReportFilePath(formPath);
        File reportFile = new File(reportPath);

        Set<String> childForms = new LinkedHashSet<>();

        if (reportFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(reportFile.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8);

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

        String displayPath;
        if (formPath.startsWith("/")) {
            displayPath = formPath.substring(1);
        } else {
            displayPath = formPath;
        }
        // Убеждаемся, что путь начинается с Forms/ или UserForms
        if (!displayPath.startsWith("Forms/") && !displayPath.startsWith("UserForms")) {
            displayPath = "Forms/" + displayPath;
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
            formNodeMap.put(formPath, formNode);
        }

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

    public void refreshChildForms(String formPath) {
        childrenCache.remove(formPath);

        DefaultMutableTreeNode node = formNodeMap.get(formPath);
        if (node != null) {
            node.removeAllChildren();

            Set<String> childForms = loadChildFormsFromReport(formPath);
            childrenCache.put(formPath, childForms);

            for (String childForm : childForms) {
                String childDisplayPath;
                if (childForm.startsWith("/")) {
                    childDisplayPath = childForm.substring(1);
                } else {
                    childDisplayPath = childForm;
                }
                if (!childDisplayPath.startsWith("Forms/") && !childDisplayPath.startsWith("UserForms")) {
                    childDisplayPath = "Forms/" + childDisplayPath;
                }

                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childDisplayPath);
                childNode.setAllowsChildren(true);
                node.add(childNode);
                formNodeMap.put(childForm, childNode);
            }

            treeModel.reload(node);
            TreePath nodePath = new TreePath(node.getPath());
            tree.expandPath(nodePath);
        }
    }

    public void refreshAllChildForms() {
        for (String formPath : allForms) {
            refreshChildForms(formPath);
        }
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
        return new ArrayList<>(allForms);
    }
}