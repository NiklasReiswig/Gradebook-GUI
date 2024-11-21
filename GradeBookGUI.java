// GradeBookGUI.java
import javax.swing.*;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class GradeBookGUI extends JFrame {
    private GradeBook gradeBook;
    private static final String DATA_FILE = System.getProperty("user.home") + File.separator + "gradebook.dat";

    // GUI Components
    private JMenuBar menuBar;
    private JMenu fileMenu, optionsMenu, helpMenu;
    private JMenuItem exitItem, addClassItem, addGradeItem, addHypotheticalGradesItem, calculateNeededGradesItem, saveItem;
    private JMenuItem editGradeItem, deleteClassItem, deleteAllDataItem, seeMoreInfoItem;
    private JMenuItem helpItem;

    private JTable classesTable;
    private DefaultTableModel classesTableModel;
    private JPanel mainPanel;

    public GradeBookGUI() {
        // Initialize GUI components
        setTitle("Grade Book");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);  // For custom close handling
        setLocationRelativeTo(null);

        // Handle window closing to save data
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        // Setup menu bar
        menuBar = new JMenuBar();

        // File menu
        fileMenu = new JMenu("File");
        saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> saveGradeBook());
        exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Options menu
        optionsMenu = new JMenu("Options");
        addClassItem = new JMenuItem("Add Class");
        addClassItem.addActionListener(e -> addClass());
        addGradeItem = new JMenuItem("Add Grade");
        addGradeItem.addActionListener(e -> addGrade());
        addHypotheticalGradesItem = new JMenuItem("Add Hypothetical Grades");
        addHypotheticalGradesItem.addActionListener(e -> addHypotheticalGrades());
        calculateNeededGradesItem = new JMenuItem("Calculate Needed Grades");
        calculateNeededGradesItem.addActionListener(e -> calculateNeededGrades());
        editGradeItem = new JMenuItem("Edit or Delete Grades");
        editGradeItem.addActionListener(e -> editGrade());
        deleteClassItem = new JMenuItem("Delete Class");
        deleteClassItem.addActionListener(e -> deleteClass());
        deleteAllDataItem = new JMenuItem("Delete All Data");
        deleteAllDataItem.addActionListener(e -> deleteAllData());
        seeMoreInfoItem = new JMenuItem("See More Information");
        seeMoreInfoItem.addActionListener(e -> seeMoreInformation());

        optionsMenu.add(addClassItem);
        optionsMenu.add(addGradeItem);
        optionsMenu.add(addHypotheticalGradesItem);
        optionsMenu.add(calculateNeededGradesItem);
        optionsMenu.add(editGradeItem);
        optionsMenu.add(deleteClassItem);
        optionsMenu.add(deleteAllDataItem);
        optionsMenu.addSeparator();
        optionsMenu.add(seeMoreInfoItem);

        // Help menu
        helpMenu = new JMenu("Help");
        helpItem = new JMenuItem("Help");
        helpItem.addActionListener(e -> showHelpDialog());
        helpMenu.add(helpItem);

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(optionsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        // Main panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Classes table
        classesTableModel = new DefaultTableModel(0, 3) { // Now 3 columns
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Object.class;
            }
        };
        classesTable = new JTable(classesTableModel);
        classesTable.setFillsViewportHeight(true);
        classesTable.setRowHeight(30); // Increased row height for better readability

        // Remove table headers
        classesTable.setTableHeader(null);

        // Adjust column widths
        classesTable.getColumnModel().getColumn(0).setPreferredWidth(400);
        classesTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        classesTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Hidden column

        // Hide the third column (rowType)
        classesTable.removeColumn(classesTable.getColumnModel().getColumn(2));

        // Custom cell renderer to handle formatting
        classesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            Font boldFont = new Font("Serif", Font.BOLD, 18); // Increased font size
            Font normalFont = new Font("Serif", Font.PLAIN, 16); // Increased font size

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                int modelRow = classesTable.convertRowIndexToModel(row);
                String rowType = (String) classesTableModel.getValueAt(modelRow, 2);

                if ("class".equals(rowType)) {
                    setFont(boldFont);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setBackground(new Color(220, 220, 220)); // Light gray background for class names
                } else if ("category".equals(rowType)) {
                    setFont(normalFont);
                    setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.LEFT);
                    setBackground(Color.WHITE);
                }
                return this;
            }
        });

        // Use a custom UI to merge cells for class names
        classesTable.setUI(new BasicTableUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Rectangle clipBounds = g.getClipBounds();
                int firstRow = classesTable.rowAtPoint(new Point(0, clipBounds.y));
                int lastRow = classesTable.rowAtPoint(new Point(0, clipBounds.y + clipBounds.height));
                if (firstRow == -1) {
                    firstRow = 0;
                }
                if (lastRow == -1) {
                    lastRow = classesTable.getRowCount() - 1;
                }
                for (int row = firstRow; row <= lastRow; row++) {
                    paintRow(g, row);
                }
            }

            private void paintRow(Graphics g, int row) {
                for (int column = 0; column < classesTable.getColumnCount(); column++) {
                    Rectangle cellRect = classesTable.getCellRect(row, column, true);
                    if (classesTable.isEditing() && classesTable.getEditingRow() == row && classesTable.getEditingColumn() == column) {
                        Component component = classesTable.getEditorComponent();
                        component.setBounds(cellRect);
                        component.validate();
                    } else {
                        int modelRow = classesTable.convertRowIndexToModel(row);
                        String rowType = (String) classesTableModel.getValueAt(modelRow, 2);

                        if ("class".equals(rowType)) {
                            if (column == 0) {
                                // Merge cells for class name
                                Rectangle fullRect = cellRect.union(classesTable.getCellRect(row, 1, true));
                                paintCell(g, fullRect, row, column);
                            }
                            // Skip the next cell since it's merged
                            column++;
                        } else {
                            paintCell(g, cellRect, row, column);
                        }
                    }
                }
            }

            private void paintCell(Graphics g, Rectangle cellRect, int row, int column) {
                if (classesTable.isEditing() && classesTable.getEditingRow() == row && classesTable.getEditingColumn() == column) {
                    return;
                }

                TableCellRenderer renderer = classesTable.getCellRenderer(row, column);
                Component component = classesTable.prepareRenderer(renderer, row, column);
                rendererPane.paintComponent(g, component, classesTable, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true);
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(classesTable);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        add(mainPanel);

        // Load data
        loadGradeBook();

        // Update classes table
        updateClassesTable();

        // Show the window
        setVisible(true);
    }

    /**
     * Updates the classes table to reflect the current state of the GradeBook.
     */
    private void updateClassesTable() {
        // Clear existing data
        classesTableModel.setRowCount(0);

        for (ClassRecord classRecord : gradeBook.getClasses()) {
            String className = classRecord.getName();

            // Calculate final grade and letter grade
            double finalGrade = classRecord.calculateFinalGrade();
            String letterGrade = classRecord.getLetterGrade();
            String finalGradeStr = String.format("Final Grade: %.2f%% (%s)", finalGrade, letterGrade);

            // Combine class name and final grade
            String classDisplay = String.format("%s - %s", className, finalGradeStr);

            // Add class name row (merged cells), rowType = "class"
            classesTableModel.addRow(new Object[]{classDisplay, "", "class"});

            for (Category category : classRecord.getCategories()) {
                String categoryName = category.getName();
                double categoryAverage = category.calculateAverage();
                String averageStr = category.getGrades().isEmpty() ? "N/A" : String.format("%.2f%%", categoryAverage);

                // Convert grades to a comma-separated string
                String gradesStr = category.getGrades().isEmpty() ? "No grades" : String.join(", ", category.getGrades().stream()
                        .map(grade -> String.format("%.2f", grade))
                        .toArray(String[]::new));

                // Combine category name and average
                String categoryDisplay = String.format("%s (Avg: %s):", categoryName, averageStr);

                // Add category and grades row, rowType = "category"
                classesTableModel.addRow(new Object[]{categoryDisplay, gradesStr, "category"});
            }
        }
    }

    /**
     * Configures the grading scale by prompting the user for cutoff percentages.
     * Allows the user to exclude certain letter grades by leaving inputs blank.
     * Ensures that cutoffs are in descending order.
     *
     * @return A GradingScale object based on user input, or null if canceled.
     */
    private GradingScale configureGradingScale() {
        TreeMap<Double, String> scaleMap = new TreeMap<>(java.util.Collections.reverseOrder());

        // Predefined list of letter grades excluding 'F'
        String[] letterGrades = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-"};

        double previousCutoff = 100.0;

        for (int i = 0; i < letterGrades.length; i++) {
            String grade = letterGrades[i];
            String message = String.format("Enter the minimum percentage cutoff for %s (less than %.2f%%):\nLeave blank to exclude %s.", grade, previousCutoff, grade);
            String cutoffStr = JOptionPane.showInputDialog(this, message);

            if (cutoffStr == null) {
                // User cancelled the grading scale configuration
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel grading scale configuration?", "Cancel", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    return null;
                } else {
                    // Restart grading scale configuration by repeating this grade
                    i--;
                    continue;
                }
            }

            cutoffStr = cutoffStr.trim();

            if (cutoffStr.isEmpty()) {
                // User chose to exclude this grade
                continue;
            }

            double cutoff = 0.0;
            try {
                cutoff = Double.parseDouble(cutoffStr);
                if (cutoff < 0.0 || cutoff >= previousCutoff) {
                    JOptionPane.showMessageDialog(this, String.format("Cutoff for %s must be between 0 and less than %.2f%%.", grade, previousCutoff));
                    // Re-prompt for this grade
                    i--;
                    continue;
                }
                scaleMap.put(cutoff, grade);
                previousCutoff = cutoff;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number for the cutoff.");
                // Re-prompt for this grade
                i--;
            }
        }

        // Add 'F' grade with cutoff 0%
        scaleMap.put(0.0, "F");

        if (scaleMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No grades were added to the grading scale. At least 'F' will be included.", "Grading Scale", JOptionPane.WARNING_MESSAGE);
            scaleMap.put(0.0, "F");
        }

        return new GradingScale(scaleMap);
    }

    /**
     * Adds a new class to the GradeBook.
     * Prompts the user for class name, rounding preferences, grading scale, and categories.
     */
    private void addClass() {
        String className = JOptionPane.showInputDialog(this, "Enter the name of the class:");
        if (className == null || className.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Class name cannot be empty.");
            return;
        }

        // Ask if the class uses rounding
        int roundingOption = JOptionPane.showConfirmDialog(this, "Does this class use rounding for final grades?", "Rounding Option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        boolean usesRounding = (roundingOption == JOptionPane.YES_OPTION);
        double roundingThreshold = 0.0;

        if (usesRounding) {
            while (true) {
                String thresholdStr = JOptionPane.showInputDialog(this, "Enter the rounding threshold (e.g., 0.5):\nFinal grade within this many points of the next letter grade's cutoff will be rounded up.");
                if (thresholdStr == null) {
                    // User cancelled entering the threshold
                    JOptionPane.showMessageDialog(this, "Rounding threshold is required if rounding is enabled.");
                    continue;
                }

                thresholdStr = thresholdStr.trim();

                try {
                    roundingThreshold = Double.parseDouble(thresholdStr);
                    if (roundingThreshold <= 0.0) {
                        JOptionPane.showMessageDialog(this, "Rounding threshold must be a positive number.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number for the rounding threshold.");
                }
            }
        }

        // Configure grading scale
        GradingScale gradingScale = configureGradingScale();
        if (gradingScale == null) return;

        // Create and add the class record
        ClassRecord classRecord = new ClassRecord(className, gradingScale, usesRounding, roundingThreshold);
        gradeBook.addClass(classRecord);

        // Add categories
        addCategories(classRecord);

        // Update table
        updateClassesTable();
    }

    /**
     * Adds one or more grades to a specific category within a class.
     * Allows the user to add multiple grades in a single operation.
     */
    private void addGrade() {
        // Select class
        ArrayList<ClassRecord> classes = gradeBook.getClasses();
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available.");
            return;
        }
        String[] classNames = classes.stream().map(ClassRecord::getName).toArray(String[]::new);
        String selectedClassName = (String) JOptionPane.showInputDialog(this, "Select a class:", "Add Grade", JOptionPane.PLAIN_MESSAGE, null, classNames, classNames[0]);
        if (selectedClassName == null) return;

        ClassRecord classRecord = gradeBook.getClassByName(selectedClassName);

        // Select category
        ArrayList<Category> categories = classRecord.getCategories();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No categories available in this class.");
            return;
        }
        String[] categoryNames = categories.stream().map(Category::getName).toArray(String[]::new);
        String selectedCategoryName = (String) JOptionPane.showInputDialog(this, "Select a category:", "Add Grade", JOptionPane.PLAIN_MESSAGE, null, categoryNames, categoryNames[0]);
        if (selectedCategoryName == null) return;

        Category selectedCategory = classRecord.getCategoryByName(selectedCategoryName);

        boolean addMore = true;
        while (addMore) {
            // Enter grade
            double grade = 0.0;
            while (true) {
                String gradeStr = JOptionPane.showInputDialog(this, "Enter the grade (0-100):");
                if (gradeStr == null) {
                    // User cancelled adding grades
                    return;
                }
                try {
                    grade = Double.parseDouble(gradeStr.trim());
                    if (grade < 0.0 || grade > 100.0) {
                        JOptionPane.showMessageDialog(this, "Grade must be between 0 and 100.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
                }
            }

            selectedCategory.addGrade(grade);

            // Ask if the user wants to add another grade
            int response = JOptionPane.showConfirmDialog(this, "Do you want to add another grade to '" + selectedCategoryName + "'?", "Add Another Grade", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response != JOptionPane.YES_OPTION) {
                addMore = false;
            }
        }

        // Update table
        updateClassesTable();
    }

    /**
     * Adds categories to a class by prompting the user for category details.
     * Ensures that the total weight of categories sums to 100%.
     *
     * @param classRecord The class to which categories are being added.
     */
    private void addCategories(ClassRecord classRecord) {
        int numCategories = 0;
        while (true) {
            String numCategoriesStr = JOptionPane.showInputDialog(this, "Enter the number of categories:");
            if (numCategoriesStr == null) return;
            try {
                numCategories = Integer.parseInt(numCategoriesStr.trim());
                if (numCategories <= 0) {
                    JOptionPane.showMessageDialog(this, "Number of categories must be positive.");
                    continue;
                }
                break;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
            }
        }

        double totalWeight = 0.0;
        for (int i = 0; i < numCategories; i++) {
            String categoryName = JOptionPane.showInputDialog(this, "Enter name for category " + (i + 1) + ":");
            if (categoryName == null || categoryName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Category name cannot be empty.");
                i--;
                continue;
            }

            double weight = 0.0;
            while (true) {
                String weightStr = JOptionPane.showInputDialog(this, "Enter weight (as percentage) for '" + categoryName + "':");
                if (weightStr == null) return;
                try {
                    weight = Double.parseDouble(weightStr.trim());
                    if (weight <= 0.0 || weight > 100.0) {
                        JOptionPane.showMessageDialog(this, "Weight must be between 0 and 100.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
                }
            }

            totalWeight += weight;

            // Prompt for number of lowest grades to drop
            int numGradesDropped = 0;
            while (true) {
                String droppedStr = JOptionPane.showInputDialog(this, "Enter the number of lowest grades to drop for '" + categoryName + "':", "0");
                if (droppedStr == null) return;
                try {
                    numGradesDropped = Integer.parseInt(droppedStr.trim());
                    if (numGradesDropped < 0) {
                        JOptionPane.showMessageDialog(this, "Number of dropped grades cannot be negative.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
                }
            }

            Category category = new Category(categoryName, weight, numGradesDropped);
            classRecord.addCategory(category);
        }

        if (Math.abs(totalWeight - 100.0) > 0.01) {
            JOptionPane.showMessageDialog(this, "Total weight of categories must equal 100%. Please re-enter categories.");
            classRecord.getCategories().clear();
            addCategories(classRecord);
        }
    }

    /**
     * Adds hypothetical grades to a category. (Feature Pending)
     */
    private void addHypotheticalGrades() {
        JOptionPane.showMessageDialog(this, "Feature not implemented yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Edits or deletes existing grades within a category.
     */
    private void editGrade() {
        // Select class
        ArrayList<ClassRecord> classes = gradeBook.getClasses();
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available.");
            return;
        }
        String[] classNames = classes.stream().map(ClassRecord::getName).toArray(String[]::new);
        String selectedClassName = (String) JOptionPane.showInputDialog(this, "Select a class:", "Edit or Delete Grades", JOptionPane.PLAIN_MESSAGE, null, classNames, classNames[0]);
        if (selectedClassName == null) return;

        ClassRecord classRecord = gradeBook.getClassByName(selectedClassName);

        // Select category
        ArrayList<Category> categories = classRecord.getCategories();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No categories available in this class.");
            return;
        }
        String[] categoryNames = categories.stream().map(Category::getName).toArray(String[]::new);
        String selectedCategoryName = (String) JOptionPane.showInputDialog(this, "Select a category:", "Edit or Delete Grades", JOptionPane.PLAIN_MESSAGE, null, categoryNames, categoryNames[0]);
        if (selectedCategoryName == null) return;

        Category selectedCategory = classRecord.getCategoryByName(selectedCategoryName);

        // Select grade to edit or delete
        ArrayList<Double> grades = selectedCategory.getGrades();
        if (grades.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No grades available in this category.");
            return;
        }
        String[] gradeOptions = new String[grades.size()];
        for (int i = 0; i < grades.size(); i++) {
            gradeOptions[i] = String.format("%d: %.2f", i + 1, grades.get(i));
        }
        String selectedGradeOption = (String) JOptionPane.showInputDialog(this, "Select a grade to edit or delete:", "Edit or Delete Grades", JOptionPane.PLAIN_MESSAGE, null, gradeOptions, gradeOptions[0]);
        if (selectedGradeOption == null) return;

        int selectedIndex = Integer.parseInt(selectedGradeOption.split(":")[0]) - 1;

        // Choose action
        String[] actions = {"Edit", "Delete"};
        String selectedAction = (String) JOptionPane.showInputDialog(this, "Select an action:", "Edit or Delete Grades", JOptionPane.PLAIN_MESSAGE, null, actions, actions[0]);
        if (selectedAction == null) return;

        if ("Edit".equals(selectedAction)) {
            // Edit grade
            while (true) {
                String gradeStr = JOptionPane.showInputDialog(this, "Enter the new grade (0-100):");
                if (gradeStr == null) return;
                try {
                    double newGrade = Double.parseDouble(gradeStr.trim());
                    if (newGrade < 0.0 || newGrade > 100.0) {
                        JOptionPane.showMessageDialog(this, "Grade must be between 0 and 100.");
                        continue;
                    }
                    selectedCategory.editGrade(selectedIndex, newGrade);
                    break;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
                }
            }
        } else if ("Delete".equals(selectedAction)) {
            // Delete grade
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this grade?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                selectedCategory.deleteGrade(selectedIndex);
            }
        }

        // Update table
        updateClassesTable();
    }

    /**
     * Deletes a selected class from the GradeBook.
     */
    private void deleteClass() {
        // Select class
        ArrayList<ClassRecord> classes = gradeBook.getClasses();
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available.");
            return;
        }
        String[] classNames = classes.stream().map(ClassRecord::getName).toArray(String[]::new);
        String selectedClassName = (String) JOptionPane.showInputDialog(this, "Select a class to delete:", "Delete Class", JOptionPane.PLAIN_MESSAGE, null, classNames, classNames[0]);
        if (selectedClassName == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the class '" + selectedClassName + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            gradeBook.getClasses().removeIf(c -> c.getName().equalsIgnoreCase(selectedClassName));
            updateClassesTable();
        }
    }

    /**
     * Deletes all data from the GradeBook after user confirmation.
     */
    private void deleteAllData() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete all data?", "Confirm Delete All", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            gradeBook = new GradeBook();
            updateClassesTable();
        }
    }

    /**
     * Displays more information about the application. (Feature Pending)
     */
    private void seeMoreInformation() {
        JOptionPane.showMessageDialog(this, "Feature not implemented yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays a help dialog with instructions and information about the application.
     */
    private void showHelpDialog() {
        String helpMessage = "Grade Book Application Help:\n\n" +
                "- **Add Class**: Create a new class with custom grading scales and categories.\n" +
                "- **Add Grade**: Add one or multiple grades to a specific category within a class.\n" +
                "- **Add Hypothetical Grades**: [Feature Pending]\n" +
                "- **Calculate Needed Grades**: Determine what average you need on remaining assignments to achieve a desired final grade.\n" +
                "- **Edit or Delete Grades**: Modify or remove existing grades in a category.\n" +
                "- **Delete Class**: Remove an entire class and all its data.\n" +
                "- **Delete All Data**: Remove all classes and associated data from the grade book.\n" +
                "- **See More Information**: [Feature Pending]\n" +
                "- **Help**: Display this help message.\n\n" +
                "Ensure that the total weight of all categories in a class sums up to 100%.\n\n" +
                "**New Features:**\n" +
                "- **Rounding:** If a class uses rounding, final grades within the specified threshold of the next letter grade's cutoff will be rounded up accordingly.\n" +
                "- **Handling Missing Grades:** Categories with no grades will have their average set to the average of existing categories, ensuring fair final grade calculations.";
        JOptionPane.showMessageDialog(this, helpMessage, "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Calculates the needed average grades to achieve a desired final grade.
     * Incorporates rounding preferences if enabled.
     */
    private void calculateNeededGrades() {
        // Select class
        ArrayList<ClassRecord> classes = gradeBook.getClasses();
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available.");
            return;
        }
        String[] classNames = classes.stream().map(ClassRecord::getName).toArray(String[]::new);
        String selectedClassName = (String) JOptionPane.showInputDialog(this, "Select a class:", "Calculate Needed Grades", JOptionPane.PLAIN_MESSAGE, null, classNames, classNames[0]);
        if (selectedClassName == null) return;

        ClassRecord classRecord = gradeBook.getClassByName(selectedClassName);

        // Select category
        ArrayList<Category> categories = classRecord.getCategories();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No categories available in this class.");
            return;
        }
        String[] categoryNames = categories.stream().map(Category::getName).toArray(String[]::new);
        String selectedCategoryName = (String) JOptionPane.showInputDialog(this, "Select a category:", "Calculate Needed Grades", JOptionPane.PLAIN_MESSAGE, null, categoryNames, categoryNames[0]);
        if (selectedCategoryName == null) return;

        Category selectedCategory = classRecord.getCategoryByName(selectedCategoryName);

        // Use the number of lowest grades dropped from the category configuration
        int numGradesDropped = selectedCategory.getNumGradesDropped();

        // Enter number of remaining assignments in the selected category
        int remainingAssignments = 0;
        while (true) {
            String remainingStr = JOptionPane.showInputDialog(this, "Enter the number of remaining assignments in '" + selectedCategoryName + "':");
            if (remainingStr == null) return;
            try {
                remainingAssignments = Integer.parseInt(remainingStr.trim());
                if (remainingAssignments < 0) {
                    JOptionPane.showMessageDialog(this, "Number of assignments cannot be negative.");
                    continue;
                }
                break;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
            }
        }

        // Get desired letter grade
        GradingScale gradingScale = classRecord.getGradingScale();
        TreeMap<Double, String> scaleMap = gradingScale.getScale();
        String[] gradeOptions = scaleMap.values().toArray(new String[0]);
        String desiredLetterGrade = (String) JOptionPane.showInputDialog(this, "Select the desired letter grade:", "Desired Letter Grade", JOptionPane.PLAIN_MESSAGE, null, gradeOptions, gradeOptions[0]);
        if (desiredLetterGrade == null) return;

        // Get cutoff percentage for desired letter grade
        Double desiredPercentage = null;
        for (Map.Entry<Double, String> entry : scaleMap.entrySet()) {
            if (entry.getValue().equals(desiredLetterGrade)) {
                desiredPercentage = entry.getKey();
                break;
            }
        }
        if (desiredPercentage == null) {
            JOptionPane.showMessageDialog(this, "Invalid letter grade selected.");
            return;
        }

        // Calculate current total grade excluding the selected category's contribution
        double currentTotal = 0.0;

        for (Category category : classRecord.getCategories()) {
            double weight = category.getWeight();
            if (!category.getGrades().isEmpty()) {
                double categoryAverage = category.calculateAverage();
                if (category == selectedCategory) {
                    // Exclude the selected category's contribution
                    continue;
                }
                currentTotal += categoryAverage * (weight / 100.0);
            }
        }

        // Check if desired grade is already achieved without the selected category
        if (currentTotal >= desiredPercentage) {
            JOptionPane.showMessageDialog(this, "You have already achieved the desired grade without the selected category!");
            return;
        }

        // Calculate the needed average in the selected category
        double categoryWeight = selectedCategory.getWeight();
        double neededDifference = desiredPercentage - currentTotal;
        double neededCategoryAverage = (neededDifference * 100.0) / categoryWeight;

        // Number of existing grades in the category
        ArrayList<Double> existingGrades = new ArrayList<>(selectedCategory.getGrades());
        int numExistingGrades = existingGrades.size();
        int totalAssignments = numExistingGrades + remainingAssignments;

        if (totalAssignments == 0) {
            JOptionPane.showMessageDialog(this, "No assignments in the selected category.");
            return;
        }

        // Adjust for dropped grades
        int numGradesToConsider = totalAssignments - numGradesDropped;
        if (numGradesToConsider <= 0) {
            JOptionPane.showMessageDialog(this, "The number of dropped grades exceeds the total number of assignments.");
            return;
        }

        // Combine existing grades and placeholders for remaining assignments
        ArrayList<Double> allGrades = new ArrayList<>(existingGrades);
        for (int i = 0; i < remainingAssignments; i++) {
            allGrades.add(null); // Placeholder for remaining grades
        }

        // Sort grades (existing grades, nulls treated as zeros for sorting)
        ArrayList<Double> gradesForDropping = new ArrayList<>();
        for (Double grade : allGrades) {
            gradesForDropping.add(grade != null ? grade : 0.0);
        }
        gradesForDropping.sort(Double::compare);

        // Sum of grades to be considered (after dropping lowest grades)
        double sumGradesConsidered = 0.0;
        for (int i = numGradesDropped; i < gradesForDropping.size(); i++) {
            Double grade = gradesForDropping.get(i);
            if (grade != null) {
                sumGradesConsidered += grade;
            }
        }

        // Number of grades considered
        int numGradesConsidered = gradesForDropping.size() - numGradesDropped;

        // Needed total sum of grades to achieve neededCategoryAverage
        double neededSumGrades = neededCategoryAverage * numGradesConsidered;

        // Sum of existing grades considered
        double sumExistingGradesConsidered = 0.0;
        ArrayList<Double> existingGradesForDropping = new ArrayList<>();
        for (Double grade : existingGrades) {
            existingGradesForDropping.add(grade != null ? grade : 0.0);
        }
        existingGradesForDropping.sort(Double::compare);
        for (int i = numGradesDropped; i < existingGradesForDropping.size(); i++) {
            Double grade = existingGradesForDropping.get(i);
            sumExistingGradesConsidered += grade;
        }

        // Needed sum of remaining grades considered
        double neededSumRemainingGrades = neededSumGrades - sumExistingGradesConsidered;

        // Number of remaining grades considered
        int numRemainingGradesConsidered = numGradesConsidered - (existingGradesForDropping.size() - numGradesDropped);

        if (numRemainingGradesConsidered <= 0) {
            JOptionPane.showMessageDialog(this, "You have already achieved the desired grade!");
            return;
        }

        double neededAverageRemainingGrades = neededSumRemainingGrades / numRemainingGradesConsidered;

        // Adjust for rounding if applicable
        if (classRecord.isUsesRounding()) {
            neededAverageRemainingGrades = Math.round(neededAverageRemainingGrades);
        } else {
            neededAverageRemainingGrades = Math.round(neededAverageRemainingGrades * 100.0) / 100.0; // Round to two decimal places
        }

        // Check if the needed average is achievable
        if (neededAverageRemainingGrades > 100.0) {
            JOptionPane.showMessageDialog(this, "It is not possible to achieve the desired grade with the remaining assignments.");
            return;
        }

        if (neededAverageRemainingGrades < 0.0) {
            JOptionPane.showMessageDialog(this, "You have already achieved the desired grade!");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("To achieve a final grade of ").append(desiredLetterGrade).append(" (").append(String.format("%.2f", desiredPercentage)).append("%),\n");
        sb.append("you need an average of ").append(String.format("%.2f", neededAverageRemainingGrades)).append("% on the remaining ").append(remainingAssignments).append(" assignment(s) in '").append(selectedCategoryName).append("'.\n");

        JOptionPane.showMessageDialog(this, sb.toString(), "Needed Grades Result", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Saves the GradeBook data to a file.
     */
    private void saveGradeBook() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(gradeBook);
            JOptionPane.showMessageDialog(this, "GradeBook data saved successfully.", "Save Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving GradeBook data.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads the GradeBook data from a file.
     */
    private void loadGradeBook() {
        File dataFile = new File(DATA_FILE);
        if (!dataFile.exists()) {
            gradeBook = new GradeBook();
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            gradeBook = (GradeBook) in.readObject();
        } catch (InvalidClassException e) {
            JOptionPane.showMessageDialog(this, "Data format is incompatible. Starting with a new GradeBook.", "Load Error", JOptionPane.ERROR_MESSAGE);
            gradeBook = new GradeBook();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading GradeBook data. Starting with a new GradeBook.", "Load Error", JOptionPane.ERROR_MESSAGE);
            gradeBook = new GradeBook();
        }
    }

    /**
     * Handles application exit by prompting the user to save changes.
     */
    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(this, "Do you want to save changes before exiting?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            saveGradeBook();
            System.exit(0);
        } else if (confirm == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
        // If CANCEL_OPTION, do nothing
    }

    /**
     * Entry point of the application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GradeBookGUI());
    }

}