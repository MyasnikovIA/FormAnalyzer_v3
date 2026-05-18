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

/**
 * Панель для отображения списка форм в виде дерева с поддержкой:
 * - фильтрации по поисковому запросу
 * - множественного выбора
 * - добавления форм через модальное окно
 * - удаления выбранных форм
 * - контекстного меню
 */
public class FormsTreePanel extends JPanel {

    private JTree tree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private JTextField searchField;
    private JButton addButton;
    private JButton removeButton;
    private JButton selectAllButton;
    private JButton deselectAllButton;

    // Хранилище всех форм (оригинальные пути)
    private Set<String> allForms = new LinkedHashSet<>();
    // Кэш отфильтрованных форм
    private List<String> filteredForms = new ArrayList<>();

    private Runnable onFormsChanged;
    private Runnable onAnalysisRequested;

    public FormsTreePanel() {
        initUI();
        loadFormsFromFile();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));

        // Панель поиска
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

        // Дерево форм (плоский список - все формы на одном уровне)
        rootNode = new DefaultMutableTreeNode("Формы для анализа");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        // Добавляем обработчик для поиска
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilter();
            }
        });

        // Добавляем контекстное меню
        createContextMenu();

        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setBorder(BorderFactory.createTitledBorder("Список форм"));
        add(treeScroll, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        addButton = new JButton("➕ Добавить формы");
        addButton.setToolTipText("Добавить новые формы для анализа (поддерживается многострочный ввод)");
        addButton.addActionListener(e -> showAddFormsDialog());

        removeButton = new JButton("🗑 Удалить выбранные");
        removeButton.setToolTipText("Удалить выбранные формы из списка");
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
     * Создаёт контекстное меню для дерева
     */
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
                if (row != -1) {
                    TreePath path = tree.getPathForRow(row);
                    if (tree.isPathSelected(path)) {
                        popupMenu.show(tree, e.getX(), e.getY());
                    } else {
                        tree.setSelectionPath(path);
                        popupMenu.show(tree, e.getX(), e.getY());
                    }
                } else {
                    tree.clearSelection();
                    popupMenu.show(tree, e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * Устанавливает обработчик запроса на запуск анализа
     */
    public void setOnAnalysisRequested(Runnable callback) {
        this.onAnalysisRequested = callback;
    }

    /**
     * Применяет фильтр к дереву на основе текста поиска
     */
    private void applyFilter() {
        String filter = searchField.getText().trim().toLowerCase();

        rootNode.removeAllChildren();

        if (filter.isEmpty()) {
            // Без фильтра - показываем все формы (плоский список)
            for (String formPath : allForms) {
                String displayPath = formPath;
                if (displayPath.startsWith("/")) {
                    displayPath = displayPath.substring(1);
                }
                DefaultMutableTreeNode formNode = new DefaultMutableTreeNode(displayPath);
                formNode.setAllowsChildren(false);
                rootNode.add(formNode);
            }
        } else {
            // С фильтром - показываем только подходящие формы
            filteredForms.clear();
            for (String formPath : allForms) {
                if (formPath.toLowerCase().contains(filter)) {
                    filteredForms.add(formPath);
                    String displayPath = formPath;
                    if (displayPath.startsWith("/")) {
                        displayPath = displayPath.substring(1);
                    }
                    DefaultMutableTreeNode formNode = new DefaultMutableTreeNode(displayPath);
                    formNode.setAllowsChildren(false);
                    rootNode.add(formNode);
                }
            }
        }

        treeModel.reload(rootNode);
        expandAllNodes();
    }

    /**
     * Разворачивает все узлы дерева
     */
    private void expandAllNodes() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    /**
     * Показывает диалог добавления форм
     */
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
        examples.append("# Forms/ArmPatientsInDep/SubForms/hh_mp_prescribes.frm\n");
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

    /**
     * Парсит введённый текст и извлекает пути форм
     */
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

    /**
     * Нормализует путь формы
     */
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

    /**
     * Добавляет новые формы в набор
     */
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

    /**
     * Удаляет выбранные формы
     */
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
                // Нормализуем путь для удаления из allForms
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
            // Удаляем формы из набора
            allForms.removeAll(toRemove);

            // Сохраняем в файл
            saveFormsToFile();

            // Обновляем дерево (применяем фильтр заново)
            applyFilter();

            // Очищаем выделение
            tree.clearSelection();

            // Уведомляем об изменении
            if (onFormsChanged != null) {
                onFormsChanged.run();
            }

            JOptionPane.showMessageDialog(this,
                    "Удалено форм: " + toRemove.size() + "\nОсталось форм: " + allForms.size(),
                    "Формы удалены",
                    JOptionPane.INFORMATION_MESSAGE);

            // Для отладки - выводим оставшиеся формы в консоль
            System.out.println("Осталось форм в allForms: " + allForms.size());
            for (String f : allForms) {
                System.out.println("  " + f);
            }
        }
    }

    /**
     * Извлекает полный путь формы из узла дерева
     */
    public String getFormPathFromTreePath(TreePath path) {
        if (path == null) return null;
        Object[] nodes = path.getPath();
        if (nodes.length < 2) return null;

        String result = nodes[nodes.length - 1].toString();

        if (result.endsWith(".frm") || result.endsWith(".dfrm")) {
            if (!result.startsWith("UserForms") && !result.startsWith("/")) {
                result = "/" + result;
            }
            return result;
        }

        return null;
    }

    /**
     * Возвращает выбранный путь в дереве
     */
    public TreePath getSelectedPath() {
        return tree.getSelectionPath();
    }

    /**
     * Добавляет слушатель выбора в дерево
     */
    public void addTreeSelectionListener(TreeSelectionListener listener) {
        tree.addTreeSelectionListener(listener);
    }

    /**
     * Выделяет все узлы дерева
     */
    private void selectAllNodes() {
        List<TreePath> allPaths = new ArrayList<>();
        collectAllLeafPaths(rootNode, new TreePath(rootNode), allPaths);

        TreePath[] pathsArray = allPaths.toArray(new TreePath[0]);
        tree.setSelectionPaths(pathsArray);
    }

    /**
     * Собирает все пути к листьям дерева
     */
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

    /**
     * Загружает формы из файла
     */
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
                System.out.println("Загружено форм из файла: " + allForms.size());
            } catch (IOException e) {
                System.err.println("Ошибка загрузки списка форм: " + e.getMessage());
            }
        } else {
            System.out.println("Файл forms_list.txt не найден, создан новый список");
        }
    }

    /**
     * Сохраняет формы в файл
     */
    private void saveFormsToFile() {
        try {
            StringBuilder sb = new StringBuilder();
            for (String form : allForms) {
                sb.append(form).append("\n");
            }
            Files.writeString(Paths.get("forms_list.txt"), sb.toString(),
                    java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("Сохранено форм в файл: " + allForms.size());
        } catch (IOException e) {
            System.err.println("Ошибка сохранения списка форм: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Возвращает список всех форм для анализа
     */
    public List<String> getFormsList() {
        return new ArrayList<>(allForms);
    }

    /**
     * Устанавливает обработчик изменения списка форм
     */
    public void setOnFormsChanged(Runnable callback) {
        this.onFormsChanged = callback;
    }
}