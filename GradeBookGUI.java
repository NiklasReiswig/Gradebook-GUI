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
    private JMenuItem exitItem, addClassItem, addGradeItem, addExtraCreditItem, addHypotheticalGradesItem, calculateNeededGradesItem, saveItem;
    private JMenuItem editGradeItem, deleteClassItem, deleteAllDataItem, seeMoreInfoItem;
    private JMenuItem helpItem;

    private JTable classesTable;
    private DefaultTableModel classesTableModel;
    private JPanel mainPanel;

    /**
     * Constructs the GradeBookGUI and initializes all components.
     */
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
        addExtraCreditItem = new JMenuItem("Add Extra Credit");
        addExtraCreditItem.addActionListener(e -> addExtraCredit());
        addHypotheticalGradesItem = new JMenuItem("Hypothetical Grades");
        addHypotheticalGradesItem.addActionListener(e -> calculateHypotheticalGrades());
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
        optionsMenu.add(addExtraCreditItem);
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

            // Include Extra Credit in the display if applicable
            String extraCreditStr = classRecord.getExtraCredit() > 0.0 ? String.format(" | Extra Credit: %.2f points", classRecord.getExtraCredit()) : "";

            // Combine class name and final grade
            String classDisplay = String.format("%s - %s%s", className, finalGradeStr, extraCreditStr);

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
     * Configures the grading scale by allowing the user to choose between a default scale or a custom scale.
     * If the user chooses default, a predefined scale is used.
     * If the user chooses custom, they are prompted for cutoff percentages as before.
     * @return A GradingScale object based on user input or the default scale, or null if canceled.
     */
    private GradingScale configureGradingScale() {
        // Prompt the user to choose between default or custom scale
        String[] options = {"Use Default Scale", "Custom Scale", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Would you like to use a default grading scale or set up a custom one?",
                "Grading Scale Option",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
            // User canceled the dialog
            return null;
        } else if (choice == JOptionPane.YES_OPTION) {
            // Use default grading scale
            // Example default scale (Adjust as desired):
            // A:93%, A-:90%, B+:87%, B:83%, B-:80%, C+:77%, C:73%, C-:70%, D+:67%, D:63%, D-:60%, F:0%

            TreeMap<Double, String> defaultScaleMap = new TreeMap<>(java.util.Collections.reverseOrder());
            defaultScaleMap.put(93.0, "A");
            defaultScaleMap.put(90.0, "A-");
            defaultScaleMap.put(87.0, "B+");
            defaultScaleMap.put(83.0, "B");
            defaultScaleMap.put(80.0, "B-");
            defaultScaleMap.put(77.0, "C+");
            defaultScaleMap.put(73.0, "C");
            defaultScaleMap.put(70.0, "C-");
            defaultScaleMap.put(67.0, "D+");
            defaultScaleMap.put(63.0, "D");
            defaultScaleMap.put(60.0, "D-");
            defaultScaleMap.put(0.0, "F");

            return new GradingScale(defaultScaleMap);
        }

        // If user chose custom scale:
        TreeMap<Double, String> scaleMap = new TreeMap<>(java.util.Collections.reverseOrder());

        // Predefined list of letter grades excluding 'F' (customizable)
        String[] letterGrades = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-"};

        double previousCutoff = 100.0;

        for (int i = 0; i < letterGrades.length; i++) {
            String grade = letterGrades[i];
            String message = String.format("Enter the minimum percentage cutoff for %s (less than %.2f%%):\nLeave blank to exclude %s.",
                    grade, previousCutoff, grade);
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
     * Adds categories to a class by prompting the user for category details.
     * Ensures that the total weight of categories sums to 100%.
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
     * Adds extra credit points to a selected class.
     * Allows users to add or reset extra credit.
     */
    private void addExtraCredit() {
        // Select class
        ArrayList<ClassRecord> classes = gradeBook.getClasses();
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available.");
            return;
        }
        String[] classNames = classes.stream().map(ClassRecord::getName).toArray(String[]::new);
        String selectedClassName = (String) JOptionPane.showInputDialog(this, "Select a class to add Extra Credit:", "Add Extra Credit", JOptionPane.PLAIN_MESSAGE, null, classNames, classNames[0]);
        if (selectedClassName == null) return;

        ClassRecord classRecord = gradeBook.getClassByName(selectedClassName);

        // Inform the user of current extra credit
        String currentExtraCredit = String.format("Current Extra Credit: %.2f points", classRecord.getExtraCredit());
        JOptionPane.showMessageDialog(this, currentExtraCredit, "Current Extra Credit", JOptionPane.INFORMATION_MESSAGE);

        // Ask user if they want to add or reset extra credit
        String[] options = {"Add Extra Credit", "Reset Extra Credit", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, "Choose an action for Extra Credit:", "Extra Credit Options",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) { // Add Extra Credit
            // Enter extra credit points
            double extraCredit = 0.0;
            while (true) {
                String extraCreditStr = JOptionPane.showInputDialog(this, "Enter the amount of Extra Credit points to add:");
                if (extraCreditStr == null) return; // User cancelled
                try {
                    extraCredit = Double.parseDouble(extraCreditStr.trim());
                    if (extraCredit < 0.0) {
                        JOptionPane.showMessageDialog(this, "Extra Credit points cannot be negative.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
                }
            }

            // Update extra credit
            double newExtraCredit = classRecord.getExtraCredit() + extraCredit;
            classRecord.setExtraCredit(newExtraCredit);

            // Inform the user
            JOptionPane.showMessageDialog(this, String.format("Added %.2f points of Extra Credit to '%s'.\nTotal Extra Credit: %.2f points.", extraCredit, selectedClassName, newExtraCredit), "Extra Credit Added", JOptionPane.INFORMATION_MESSAGE);

        } else if (choice == 1) { // Reset Extra Credit
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to reset Extra Credit to 0?", "Confirm Reset", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                classRecord.resetExtraCredit();
                JOptionPane.showMessageDialog(this, String.format("Extra Credit for '%s' has been reset to 0.", selectedClassName), "Extra Credit Reset", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        // If Cancel or any other option, do nothing

        // Update table
        updateClassesTable();
    }

    /**
     * Launches the Hypothetical Grades dialog where users can input remaining assignments and hypothetical averages.
     */
    private void calculateHypotheticalGrades() {
        // Select class
        ArrayList<ClassRecord> classes = gradeBook.getClasses();
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available.");
            return;
        }
        String[] classNames = classes.stream().map(ClassRecord::getName).toArray(String[]::new);
        String selectedClassName = (String) JOptionPane.showInputDialog(this, "Select a class:", "Hypothetical Grades", JOptionPane.PLAIN_MESSAGE, null, classNames, classNames[0]);
        if (selectedClassName == null) return;

        ClassRecord classRecord = gradeBook.getClassByName(selectedClassName);

        // Collect hypothetical data for each category
        ArrayList<Category> categories = classRecord.getCategories();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No categories available in this class.");
            return;
        }

        // Create a panel to hold input fields
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Store user inputs
        ArrayList<HypotheticalInput> inputs = new ArrayList<>();

        for (Category category : categories) {
            JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JLabel categoryLabel = new JLabel("Category: " + category.getName());
            categoryLabel.setPreferredSize(new Dimension(150, 25));
            categoryPanel.add(categoryLabel);

            JLabel remainingLabel = new JLabel("Remaining Assignments:");
            JTextField remainingField = new JTextField("0", 5);
            categoryPanel.add(remainingLabel);
            categoryPanel.add(remainingField);

            JLabel averageLabel = new JLabel("Hypothetical Average (%):");
            JTextField averageField = new JTextField("0.0", 5);
            categoryPanel.add(averageLabel);
            categoryPanel.add(averageField);

            panel.add(categoryPanel);

            inputs.add(new HypotheticalInput(category, remainingField, averageField));
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Enter Hypothetical Grades", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        // Validate and collect inputs
        for (HypotheticalInput input : inputs) {
            String remainingStr = input.remainingField.getText().trim();
            String averageStr = input.averageField.getText().trim();

            int remaining = 0;
            double average = 0.0;

            try {
                remaining = Integer.parseInt(remainingStr);
                if (remaining < 0) throw new NumberFormatException("Negative assignments");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number of remaining assignments for category: " + input.category.getName());
                return;
            }

            try {
                average = Double.parseDouble(averageStr);
                if (average < 0.0 || average > 100.0) throw new NumberFormatException("Average out of range");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid hypothetical average for category: " + input.category.getName());
                return;
            }

            input.remaining = remaining;
            input.average = average;
        }

        // Perform hypothetical grade calculation
        double hypotheticalFinalGrade = calculateHypotheticalFinalGrade(classRecord, inputs);

        // Get corresponding letter grade
        String hypotheticalLetterGrade = classRecord.getGradingScale().getLetterGrade(hypotheticalFinalGrade);

        // Display the results
        String message = String.format("Hypothetical Final Grade: %.2f%% (%s)", hypotheticalFinalGrade, hypotheticalLetterGrade);
        JOptionPane.showMessageDialog(this, message, "Hypothetical Grades Result", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Helper class to store user inputs for hypothetical grades.
     */
    private class HypotheticalInput {
        Category category;
        JTextField remainingField;
        JTextField averageField;
        int remaining;
        double average;

        /**
         * Constructs a new HypotheticalInput.
         *
         * @param category        The category for which inputs are collected.
         * @param remainingField  The text field for remaining assignments.
         * @param averageField    The text field for hypothetical average.
         */
        HypotheticalInput(Category category, JTextField remainingField, JTextField averageField) {
            this.category = category;
            this.remainingField = remainingField;
            this.averageField = averageField;
        }
    }

    /**
     * Calculates the hypothetical final grade based on user inputs.
     *
     * @param classRecord The selected class record.
     * @param inputs      List of HypotheticalInput containing remaining assignments and hypothetical averages.
     * @return The calculated hypothetical final grade.
     */
    private double calculateHypotheticalFinalGrade(ClassRecord classRecord, ArrayList<HypotheticalInput> inputs) {
        double finalGrade = 0.0;
        double sumWeights = 0.0;

        // Collect averages of categories that have grades
        ArrayList<Double> existingAverages = new ArrayList<>();
        for (Category category : classRecord.getCategories()) {
            if (!category.getGrades().isEmpty()) {
                existingAverages.add(category.calculateAverage());
            }
        }

        // Compute the average of existing category averages
        double averageOfAverages = 0.0;
        if (!existingAverages.isEmpty()) {
            double sumAverages = 0.0;
            for (double avg : existingAverages) {
                sumAverages += avg;
            }
            averageOfAverages = sumAverages / existingAverages.size();
        }

        // Calculate the hypothetical final grade based on user inputs
        for (HypotheticalInput input : inputs) {
            Category category = input.category;
            double categoryAverage;

            if (!category.getGrades().isEmpty()) {
                categoryAverage = category.calculateAverage();
            } else {
                // Assign average of existing categories
                categoryAverage = averageOfAverages;
            }

            // Incorporate hypothetical grades
            if (input.remaining > 0) {
                // Calculate new average considering hypothetical grades
                ArrayList<Double> allGrades = new ArrayList<>(category.getGrades());

                // Add hypothetical grades
                for (int i = 0; i < input.remaining; i++) {
                    allGrades.add(input.average);
                }

                // Adjust for dropped grades
                ArrayList<Double> sortedGrades = new ArrayList<>(allGrades);
                sortedGrades.sort(Double::compare);

                int numGradesDropped = category.getNumGradesDropped();
                int numGradesToConsider = sortedGrades.size() - numGradesDropped;
                if (numGradesToConsider <= 0) {
                    categoryAverage = 0.0; // All grades dropped
                } else {
                    double sum = 0.0;
                    for (int i = numGradesDropped; i < sortedGrades.size(); i++) {
                        sum += sortedGrades.get(i);
                    }
                    categoryAverage = sum / numGradesToConsider;
                }
            }

            finalGrade += categoryAverage * (category.getWeight() / 100.0);
            sumWeights += category.getWeight();
        }

        // Handle categories with no grades and no hypothetical assignments
        for (Category category : classRecord.getCategories()) {
            boolean hasInput = inputs.stream().anyMatch(input -> input.category.equals(category));
            if (!category.getGrades().isEmpty() || hasInput) {
                continue; // Already handled
            }
            // Assign average of existing categories
            finalGrade += averageOfAverages * (category.getWeight() / 100.0);
            sumWeights += category.getWeight();
        }

        // Apply rounding if enabled
        if (classRecord.isUsesRounding()) {
            // Retrieve the grading scale map in descending order
            TreeMap<Double, String> scaleMapDesc = classRecord.getGradingScale().getScale();

            Double nextCutoff = null;
            for (Map.Entry<Double, String> entry : scaleMapDesc.entrySet()) {
                if (finalGrade < entry.getKey()) {
                    nextCutoff = entry.getKey();
                } else {
                    break; // Found the appropriate cutoff
                }
            }

            if (nextCutoff != null) {
                double difference = nextCutoff - finalGrade;
                if (difference <= classRecord.getRoundingThreshold()) {
                    finalGrade = nextCutoff;
                }
            }
        } else {
            // Round to two decimal places without rounding up
            finalGrade = Math.round(finalGrade * 100.0) / 100.0;
        }

        // Add extra credit
        finalGrade += classRecord.getExtraCredit();

        // Ensure final grade does not exceed 100%
        if (finalGrade > 100.0) {
            finalGrade = 100.0;
        }

        // Round again if necessary after adding extra credit
        finalGrade = Math.round(finalGrade * 100.0) / 100.0;

        return finalGrade;
    }

    /**
     * Calculates the needed grades to achieve a desired letter grade.
     * Steps:
     * 1. Prompt user to select class.
     * 2. Prompt for desired letter grade.
     * 3. Find numeric cutoff for that letter grade.
     * 4. Prompt for the number of remaining assignments in each category.
     * 5. Generate scenarios:
     *    - Scenario 1: Close to current trend
     *    - Scenario 2: Focus on Category 1
     *    - Scenario 3: Focus on Category 2
     *    - ... and so forth for all categories.
     */
    private void calculateNeededGrades() {
        // Select class
        ArrayList<ClassRecord> classes = gradeBook.getClasses();
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available.");
            return;
        }
        String[] classNames = classes.stream().map(ClassRecord::getName).toArray(String[]::new);
        String selectedClassName = (String) JOptionPane.showInputDialog(this, "Select a class:", "Calculate Needed Grades",
                JOptionPane.PLAIN_MESSAGE, null, classNames, classNames[0]);
        if (selectedClassName == null) return;

        ClassRecord classRecord = gradeBook.getClassByName(selectedClassName);
        if (classRecord == null) {
            JOptionPane.showMessageDialog(this, "Class not found.");
            return;
        }

        // Prompt for desired letter grade
        TreeMap<Double, String> scaleMap = classRecord.getGradingScale().getScale();
        java.util.Set<String> letterSet = new java.util.HashSet<>(scaleMap.values());
        java.util.List<String> letterGrades = new java.util.ArrayList<>(letterSet);
        String desiredLetterGrade = (String) JOptionPane.showInputDialog(this,
                "Select the desired letter grade:",
                "Desired Letter Grade",
                JOptionPane.PLAIN_MESSAGE,
                null,
                letterGrades.toArray(),
                letterGrades.get(0));
        if (desiredLetterGrade == null) return;

        // Find numeric cutoff for that letter grade
        double desiredCutoff = getCutoffForLetterGrade(desiredLetterGrade, scaleMap);
        if (desiredCutoff < 0) {
            JOptionPane.showMessageDialog(this, "Invalid letter grade selected.");
            return;
        }

        // Prompt for remaining assignments
        ArrayList<Category> categories = classRecord.getCategories();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No categories in this class.");
            return;
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        ArrayList<JTextField> remainingFields = new ArrayList<>();
        for (Category category : categories) {
            JPanel catPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            catPanel.add(new JLabel("Category: " + category.getName() + " | Weight: " + category.getWeight() + "%"));
            catPanel.add(new JLabel("Remaining Assignments:"));
            JTextField remainingField = new JTextField("0", 5);
            catPanel.add(remainingField);
            panel.add(catPanel);
            remainingFields.add(remainingField);
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Enter Remaining Assignments", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        int[] remainingAssignments = new int[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            try {
                int rem = Integer.parseInt(remainingFields.get(i).getText().trim());
                if (rem < 0) {
                    JOptionPane.showMessageDialog(this, "Remaining assignments cannot be negative.");
                    return;
                }
                remainingAssignments[i] = rem;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input for remaining assignments.");
                return;
            }
        }

        // Check feasibility: max out future assignments at 100%
        double maxPossibleFinal = calculateHypotheticalFinalWithGivenScores(classRecord, remainingAssignments, fillArray(categories.size(), 100.0));
        if (maxPossibleFinal < desiredCutoff) {
            JOptionPane.showMessageDialog(this, "It is not possible to achieve " + desiredLetterGrade + " even if all future assignments are perfect.");
            return;
        }

        java.util.List<String> scenarioReports = new java.util.ArrayList<>();
        // Lazy Scenario: only if current final with minimal effort (0% on all future) is already >= desiredCutoff.
        double lazyFinal = calculateHypotheticalFinalWithGivenScores(classRecord, remainingAssignments, fillArray(categories.size(), 0.0));
        if (lazyFinal >= desiredCutoff) {
            // We can do nothing and still achieve desired grade
            scenarioReports.add("Scenario L (Lazy):\n" + scenarioReport(classRecord, categories, remainingAssignments, fillArray(categories.size(), 0.0)));
        } else if (lazyFinal < desiredCutoff) {
            // We are currently above desired grade (as per your clarification)
            // but zero future assignments drop us below the cutoff.
            // Let's find minimal scores needed.
            double[] lazyScores = scenarioLazyMinimalEffort(classRecord, remainingAssignments, desiredCutoff);
            scenarioReports.add("Scenario L (Lazy):\n" + scenarioReport(classRecord, categories, remainingAssignments, lazyScores));
        }

        // Scenario 1: Close to current trend
        double[] scenario1Scores = scenarioCloseToCurrentTrend(classRecord, remainingAssignments, desiredCutoff);
        scenarioReports.add("Scenario 1 (Close to Current Trend):\n" + scenarioReport(classRecord, categories, remainingAssignments, scenario1Scores));

        // Additional scenarios: focus on each category (only if it has remaining assignments)
        int scenarioCounter = 2 + scenarioReports.size(); // Adjust scenario numbering based on how many we have so far
        for (int i = 0; i < categories.size(); i++) {
            if (remainingAssignments[i] > 0) {
                double[] focusedScores = scenarioFocusOnCategory(classRecord, remainingAssignments, desiredCutoff, i);
                scenarioReports.add("Scenario " + scenarioCounter++ + " (Focus on " + categories.get(i).getName() + "):\n"
                        + scenarioReport(classRecord, categories, remainingAssignments, focusedScores));
            }
        }

        String fullReport = "Desired Letter Grade: " + desiredLetterGrade + " (Cutoff: " + desiredCutoff + "%)\n\n";
        for (String rep : scenarioReports) {
            fullReport += rep + "\n\n";
        }

        JOptionPane.showMessageDialog(this, new JScrollPane(new JTextArea(fullReport, 20, 50)), "Needed Grades Results", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * This scenario tries to find minimal future scores needed if currently at/above desired grade but
     * going forward zero scores would drop below the cutoff.
     * We'll start from all zeros and increment all categories slightly until we reach the cutoff.
     * This is a simplistic approach; you can refine it using binary search if desired.
     */
    private double[] scenarioLazyMinimalEffort(ClassRecord classRecord, int[] remainingAssignments, double desiredCutoff) {
        ArrayList<Category> categories = classRecord.getCategories();
        double[] futureScores = fillArray(categories.size(), 0.0);

        // Simple increment approach: we try small increments across all categories until we reach the cutoff
        double increment = 0.5;
        double currentFinal = calculateHypotheticalFinalWithGivenScores(classRecord, remainingAssignments, futureScores);

        while (currentFinal < desiredCutoff) {
            boolean changed = false;
            for (int i = 0; i < futureScores.length; i++) {
                if (remainingAssignments[i] > 0 && futureScores[i] < 100.0) {
                    futureScores[i] = Math.min(100.0, futureScores[i] + increment);
                    changed = true;
                }
            }
            currentFinal = calculateHypotheticalFinalWithGivenScores(classRecord, remainingAssignments, futureScores);
            if (!changed) break;
        }

        // If needed, we could try to lower some categories again to find truly minimal scores,
        // but this at least ensures we meet the cutoff with small increments.
        return futureScores;
    }

    /**
     * Returns the cutoff percentage for a given letter grade.
     */
    private double getCutoffForLetterGrade(String letterGrade, TreeMap<Double, String> scaleMap) {
        double foundCutoff = -1.0;
        for (Map.Entry<Double, String> entry : scaleMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(letterGrade)) {
                foundCutoff = entry.getKey();
                break;
            }
        }
        return foundCutoff;
    }

    private double calculateHypotheticalFinalWithGivenScores(ClassRecord classRecord, int[] remainingAssignments, double[] futureScores) {
        double finalGrade = 0.0;
        for (int i = 0; i < classRecord.getCategories().size(); i++) {
            Category cat = classRecord.getCategories().get(i);
            ArrayList<Double> allGrades = new ArrayList<>(cat.getGrades());
            for (int r = 0; r < remainingAssignments[i]; r++) {
                allGrades.add(futureScores[i]);
            }

            double avg = calculateAverageWithDrops(allGrades, cat.getNumGradesDropped());
            finalGrade += avg * (cat.getWeight() / 100.0);
        }

        finalGrade += classRecord.getExtraCredit();
        if (finalGrade > 100.0) finalGrade = 100.0;

        if (classRecord.isUsesRounding()) {
            TreeMap<Double, String> scaleMapDesc = classRecord.getGradingScale().getScale();
            Double nextCutoff = null;
            for (Map.Entry<Double, String> entry : scaleMapDesc.entrySet()) {
                if (finalGrade < entry.getKey()) {
                    nextCutoff = entry.getKey();
                } else {
                    break;
                }
            }
            if (nextCutoff != null) {
                double difference = nextCutoff - finalGrade;
                if (difference <= classRecord.getRoundingThreshold()) {
                    finalGrade = nextCutoff;
                }
            }
        } else {
            finalGrade = Math.round(finalGrade * 100.0) / 100.0;
        }

        return finalGrade;
    }

    private double calculateAverageWithDrops(ArrayList<Double> grades, int numDrops) {
        if (grades.isEmpty()) return 0.0;
        grades.sort(Double::compareTo);
        int size = grades.size();
        int considered = size - numDrops;
        if (considered <= 0) return 0.0;
        double sum = 0.0;
        for (int i = numDrops; i < size; i++) {
            sum += grades.get(i);
        }
        return sum / considered;
    }

    private double[] fillArray(int length, double val) {
        double[] arr = new double[length];
        for (int i = 0; i < length; i++) arr[i] = val;
        return arr;
    }

    private double calcAverageOfAverages(ArrayList<Category> categories) {
        ArrayList<Double> existingAverages = new ArrayList<>();
        for (Category category : categories) {
            if (!category.getGrades().isEmpty()) {
                existingAverages.add(category.calculateAverage());
            }
        }
        if (existingAverages.isEmpty()) return 0.0;
        double sum = 0.0;
        for (double avg : existingAverages) sum += avg;
        return sum / existingAverages.size();
    }

    private double[] scenarioCloseToCurrentTrend(ClassRecord classRecord, int[] remainingAssignments, double desiredCutoff) {
        ArrayList<Category> categories = classRecord.getCategories();
        double[] futureScores = new double[categories.size()];
        double averageOfAverages = calcAverageOfAverages(categories);

        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            double avg = cat.getGrades().isEmpty() ? averageOfAverages : cat.calculateAverage();
            futureScores[i] = avg;
        }

        double currentFinal = calculateHypotheticalFinalWithGivenScores(classRecord, remainingAssignments, futureScores);
        if (currentFinal >= desiredCutoff) {
            return futureScores;
        }

        double increment = 0.5;
        while (currentFinal < desiredCutoff) {
            boolean canIncrease = false;
            for (int i = 0; i < futureScores.length; i++) {
                if (futureScores[i] < 100.0) {
                    futureScores[i] = Math.min(100.0, futureScores[i] + increment);
                    canIncrease = true;
                }
            }
            currentFinal = calculateHypotheticalFinalWithGivenScores(classRecord, remainingAssignments, futureScores);
            if (!canIncrease) break;
        }
        return futureScores;
    }

    private double[] scenarioFocusOnCategory(ClassRecord classRecord, int[] remainingAssignments, double desiredCutoff, int focusIndex) {
        ArrayList<Category> categories = classRecord.getCategories();
        double averageOfAverages = calcAverageOfAverages(categories);

        double[] futureScores = new double[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            if (i == focusIndex) {
                futureScores[i] = 100.0;
            } else {
                Category cat = categories.get(i);
                double avg = cat.getGrades().isEmpty() ? averageOfAverages : cat.calculateAverage();
                futureScores[i] = avg;
            }
        }

        double currentFinal = calculateHypotheticalFinalWithGivenScores(classRecord, remainingAssignments, futureScores);
        if (currentFinal >= desiredCutoff) return futureScores;

        double increment = 1.0;
        boolean improved = true;
        while (currentFinal < desiredCutoff && improved) {
            improved = false;
            for (int i = 0; i < futureScores.length; i++) {
                if (i != focusIndex && futureScores[i] < 100.0) {
                    futureScores[i] = Math.min(100.0, futureScores[i] + increment);
                    improved = true;
                }
            }
            currentFinal = calculateHypotheticalFinalWithGivenScores(classRecord, remainingAssignments, futureScores);
        }
        return futureScores;
    }

    private String scenarioReport(ClassRecord classRecord, ArrayList<Category> categories, int[] remainingAssignments, double[] scores) {
        double finalGrade = calculateHypotheticalFinalWithGivenScores(classRecord, remainingAssignments, scores);
        String finalLetter = classRecord.getGradingScale().getLetterGrade(finalGrade);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Final Grade: %.2f%% (%s)\n", finalGrade, finalLetter));
        sb.append("Needed future assignment averages:\n");
        for (int i = 0; i < categories.size(); i++) {
            if (remainingAssignments[i] > 0) {
                sb.append(String.format("  %s: %.2f%% on each of the %d remaining assignments\n",
                        categories.get(i).getName(), scores[i], remainingAssignments[i]));
            } else {
                sb.append(String.format("  %s: No remaining assignments\n", categories.get(i).getName()));
            }
        }
        return sb.toString();
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
                "- **Add Extra Credit**: Add or reset extra credit points to a class.\n" +
                "- **Hypothetical Grades**: Input remaining assignments and hypothetical averages to see how they affect your final grade.\n" +
                "- **Calculate Needed Grades**: Determine what average you need on remaining assignments to achieve a desired final grade.\n" +
                "- **Edit or Delete Grades**: Modify or remove existing grades in a category.\n" +
                "- **Delete Class**: Remove an entire class and all its data.\n" +
                "- **Delete All Data**: Remove all classes and associated data from the grade book.\n" +
                "- **See More Information**: [Feature Pending]\n" +
                "- **Help**: Display this help message.\n\n" +
                "Ensure that the total weight of all categories in a class sums up to 100%.\n\n" +
                "**New Features:**\n" +
                "- **Rounding:** If a class uses rounding, final grades within the specified threshold of the next letter grade's cutoff will be rounded up accordingly.\n" +
                "- **Handling Missing Grades:** Categories with no grades will have their average set to the average of existing categories, ensuring fair final grade calculations.\n" +
                "- **Extra Credit:** Add or reset extra credit points to boost your final grade directly.\n" +
                "- **Hypothetical Grades:** Plan your future performance by inputting hypothetical averages and see their impact on your final grade.";
        JOptionPane.showMessageDialog(this, helpMessage, "Help", JOptionPane.INFORMATION_MESSAGE);
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
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GradeBookGUI());
    }

}