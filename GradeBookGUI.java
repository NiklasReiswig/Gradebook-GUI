// GradeBookGUI.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
    private JMenu fileMenu, classMenu, helpMenu;
    private JMenuItem exitItem, addClassItem, addGradeItem, addHypotheticalGradesItem, calculateNeededGradesItem, saveItem;
    private JMenuItem editGradeItem, deleteClassItem, deleteAllDataItem, seeMoreInfoItem; // Updated menu items

    private JTable classesTable;
    private DefaultTableModel classesTableModel;
    private JPanel mainPanel;

    public GradeBookGUI() {
        // Initialize GUI components
        setTitle("Grade Book");
        setSize(800, 600);
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
        fileMenu.add(exitItem);

        // Class menu
        classMenu = new JMenu("Options");
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
        seeMoreInfoItem = new JMenuItem("See More Information"); // Updated menu item
        seeMoreInfoItem.addActionListener(e -> seeMoreInformation());

        classMenu.add(addClassItem);
        classMenu.add(addGradeItem);
        classMenu.add(addHypotheticalGradesItem);
        classMenu.add(calculateNeededGradesItem);
        classMenu.add(editGradeItem);
        classMenu.add(deleteClassItem);
        classMenu.add(deleteAllDataItem);
        classMenu.add(seeMoreInfoItem); // Add to menu

        // Help menu
        helpMenu = new JMenu("Help");
        JMenuItem helpItem = new JMenuItem("Help");
        helpItem.addActionListener(e -> showHelpDialog());
        helpMenu.add(helpItem);

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(classMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        // Main panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Classes table
        classesTableModel = new DefaultTableModel(new Object[]{"Class Name", "Category", "Grades"}, 0);
        classesTable = new JTable(classesTableModel);
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

    private void updateClassesTable() {
        // Clear existing data
        classesTableModel.setRowCount(0);

        for (ClassRecord classRecord : gradeBook.getClasses()) {
            String className = classRecord.getName();

            for (Category category : classRecord.getCategories()) {
                String categoryName = category.getName();
                String gradesStr = category.getGrades().toString();
                classesTableModel.addRow(new Object[]{className, categoryName, gradesStr});
            }
        }
    }

    private void addClass() {
        String className = JOptionPane.showInputDialog(this, "Enter the name of the new class:");
        if (className != null && !className.trim().isEmpty()) {
            ClassRecord newClass = new ClassRecord(className.trim());
            gradeBook.addClass(newClass);
            JOptionPane.showMessageDialog(this, "Class '" + className + "' added successfully.");
            configureClass(newClass);
            updateClassesTable(); // Update after adding a class
        }
    }

    private void configureClass(ClassRecord classRecord) {
        // Optionally configure categories
        int option = JOptionPane.showConfirmDialog(this, "Would you like to set up categories for this class?", "Setup Categories", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            addCategories(classRecord);
        }

        option = JOptionPane.showConfirmDialog(this, "Would you like to set a grading scale for this class?", "Setup Grading Scale", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            setGradingScale(classRecord);
        }

        option = JOptionPane.showConfirmDialog(this, "Would you like to enable rounding for this class?", "Enable Rounding", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            classRecord.setRoundingEnabled(true);
            String thresholdStr = JOptionPane.showInputDialog(this, "Enter the rounding threshold (e.g., 0.5):");
            try {
                double threshold = Double.parseDouble(thresholdStr);
                classRecord.setRoundingThreshold(threshold);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Rounding threshold set to 0.");
                classRecord.setRoundingThreshold(0.0);
            }
        }
    }

    private void addCategories(ClassRecord classRecord) {
        while (true) {
            String categoryName = JOptionPane.showInputDialog(this, "Enter category name:");
            if (categoryName == null || categoryName.trim().isEmpty()) {
                break;
            }
            String weightStr = JOptionPane.showInputDialog(this, "Enter weight (percentage) for this category:");
            try {
                double weight = Double.parseDouble(weightStr);
                Category newCategory = new Category(categoryName.trim(), weight);
                classRecord.addCategory(newCategory);

                String dropStr = JOptionPane.showInputDialog(this, "How many lowest grades to drop in this category?");
                int numDropped = Integer.parseInt(dropStr);
                newCategory.setNumDropped(numDropped);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter numerical values.");
            }
            int option = JOptionPane.showConfirmDialog(this, "Add another category?", "Add Category", JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.YES_OPTION) {
                break;
            }
        }
    }

    private void setGradingScale(ClassRecord classRecord) {
        GradingScale scale = new GradingScale();
        TreeMap<Double, String> newScale = new TreeMap<>();

        String[] grades = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F"};
        for (String grade : grades) {
            String cutoffStr = JOptionPane.showInputDialog(this, "Enter the lower cutoff for " + grade + " (or leave blank to skip):");
            if (cutoffStr == null || cutoffStr.trim().isEmpty()) {
                continue;
            }
            try {
                double cutoff = Double.parseDouble(cutoffStr);
                newScale.put(cutoff, grade);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a numerical value.");
            }
        }
        scale.setScale(newScale);
        classRecord.setGradingScale(scale);
    }

    private void seeMoreInformation() {
        // Allow the user to select a class to view more information
        ArrayList<ClassRecord> classes = gradeBook.getClasses();
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available.");
            return;
        }
        String[] classNames = classes.stream().map(ClassRecord::getName).toArray(String[]::new);
        String selectedClass = (String) JOptionPane.showInputDialog(this, "Select a class:", "See More Information", JOptionPane.PLAIN_MESSAGE, null, classNames, classNames[0]);
        if (selectedClass != null) {
            ClassRecord classRecord = gradeBook.getClassByName(selectedClass);
            showClassStatistics(classRecord);
        }
    }

    private void showClassStatistics(ClassRecord classRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append("Class: ").append(classRecord.getName()).append("\n");
        sb.append("Final Grade: ").append(String.format("%.2f", classRecord.calculateFinalGrade())).append("% ");
        sb.append("(").append(classRecord.getLetterGrade()).append(")\n\n");

        for (Category category : classRecord.getCategories()) {
            sb.append("Category: ").append(category.getName()).append("\n");
            sb.append("Weight: ").append(category.getWeight()).append("%\n");
            sb.append("Grades: ").append(category.getGrades()).append("\n");
            sb.append("Average: ").append(String.format("%.2f", category.calculateAverage())).append("%\n");

            // Add statistics
            sb.append("Median: ").append(String.format("%.2f", category.calculateMedian())).append("%\n");
            sb.append("Highest: ").append(String.format("%.2f", category.getHighestGrade())).append("%\n");
            sb.append("Lowest: ").append(String.format("%.2f", category.getLowestGrade())).append("%\n\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(this, scrollPane, "Statistics for " + classRecord.getName(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void addGrade() {
        // Select class
        ArrayList<ClassRecord> classes = gradeBook.getClasses();
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available.");
            return;
        }
        String[] classNames = classes.stream().map(ClassRecord::getName).toArray(String[]::new);
        String selectedClass = (String) JOptionPane.showInputDialog(this, "Select a class:", "Add Grade", JOptionPane.PLAIN_MESSAGE, null, classNames, classNames[0]);
        if (selectedClass == null) return;

        ClassRecord classRecord = gradeBook.getClassByName(selectedClass);

        // Select category
        ArrayList<Category> categories = classRecord.getCategories();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No categories available in this class.");
            return;
        }
        String[] categoryNames = categories.stream().map(Category::getName).toArray(String[]::new);
        String selectedCategory = (String) JOptionPane.showInputDialog(this, "Select a category:", "Add Grade", JOptionPane.PLAIN_MESSAGE, null, categoryNames, categoryNames[0]);
        if (selectedCategory == null) return;

        Category category = classRecord.getCategoryByName(selectedCategory);

        // Enter grade
        while (true) {
            String gradeStr = JOptionPane.showInputDialog(this, "Enter the grade to add:");
            if (gradeStr == null || gradeStr.trim().isEmpty()) break;
            try {
                double grade = Double.parseDouble(gradeStr);
                category.addGrade(grade);
                JOptionPane.showMessageDialog(this, "Grade added successfully.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid grade. Please enter a numerical value.");
            }

            int option = JOptionPane.showConfirmDialog(this, "Do you want to add another grade?", "Add Grade", JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.YES_OPTION) break;
        }
        updateClassesTable();
    }

    private void addHypotheticalGrades() {
        // Select class
        ArrayList<ClassRecord> classes = gradeBook.getClasses();
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available.");
            return;
        }
        String[] classNames = classes.stream().map(ClassRecord::getName).toArray(String[]::new);
        String selectedClass = (String) JOptionPane.showInputDialog(this, "Select a class:", "Hypothetical Grades", JOptionPane.PLAIN_MESSAGE, null, classNames, classNames[0]);
        if (selectedClass == null) return;

        ClassRecord classRecord = gradeBook.getClassByName(selectedClass);

        // Create a deep copy of the classRecord for hypothetical calculations
        ClassRecord hypotheticalClass = deepCopyClassRecord(classRecord);

        // Enter hypothetical grades
        while (true) {
            ArrayList<Category> categories = hypotheticalClass.getCategories();
            if (categories.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No categories available in this class.");
                return;
            }
            String[] categoryNames = categories.stream().map(Category::getName).toArray(String[]::new);
            String selectedCategory = (String) JOptionPane.showInputDialog(this, "Select a category:", "Hypothetical Grades", JOptionPane.PLAIN_MESSAGE, null, categoryNames, categoryNames[0]);
            if (selectedCategory == null) return;

            Category category = hypotheticalClass.getCategoryByName(selectedCategory);

            String gradeStr = JOptionPane.showInputDialog(this, "Enter the hypothetical grade to add:");
            if (gradeStr == null || gradeStr.trim().isEmpty()) break;
            try {
                double grade = Double.parseDouble(gradeStr);
                category.addGrade(grade);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid grade. Please enter a numerical value.");
                continue;
            }

            int option = JOptionPane.showConfirmDialog(this, "Do you want to add another hypothetical grade?", "Hypothetical Grades", JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.YES_OPTION) break;
        }

        // Calculate and display results
        double originalFinalGrade = classRecord.calculateFinalGrade();
        double hypotheticalFinalGrade = hypotheticalClass.calculateFinalGrade();
        String originalLetterGrade = classRecord.getLetterGrade();
        String hypotheticalLetterGrade = hypotheticalClass.getLetterGrade();

        StringBuilder sb = new StringBuilder();
        sb.append("Original Final Grade: ").append(String.format("%.2f", originalFinalGrade)).append("% ").append("(").append(originalLetterGrade).append(")\n");
        sb.append("Hypothetical Final Grade: ").append(String.format("%.2f", hypotheticalFinalGrade)).append("% ").append("(").append(hypotheticalLetterGrade).append(")\n");

        JOptionPane.showMessageDialog(this, sb.toString(), "Hypothetical Grades Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private void calculateNeededGrades() {
        // Select class
        ArrayList<ClassRecord> classes = gradeBook.getClasses();
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available.");
            return;
        }
        String[] classNames = classes.stream().map(ClassRecord::getName).toArray(String[]::new);
        String selectedClass = (String) JOptionPane.showInputDialog(this, "Select a class:", "Calculate Needed Grades", JOptionPane.PLAIN_MESSAGE, null, classNames, classNames[0]);
        if (selectedClass == null) return;

        ClassRecord classRecord = gradeBook.getClassByName(selectedClass);

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

        // Calculate current total and remaining weight
        double currentTotal = 0.0;
        double totalWeightCompleted = 0.0;
        ArrayList<Category> categoriesWithNoGrades = new ArrayList<>();

        for (Category category : classRecord.getCategories()) {
            double categoryAverage = category.calculateAverage();
            double weight = category.getWeight();
            if (!category.getGrades().isEmpty()) {
                currentTotal += categoryAverage * (weight / 100.0);
                totalWeightCompleted += weight;
            } else {
                categoriesWithNoGrades.add(category);
            }
        }
        double remainingWeight = 100.0 - totalWeightCompleted;

        if (remainingWeight <= 0.0) {
            JOptionPane.showMessageDialog(this, "All assignments are completed. Current final grade: " + classRecord.getLetterGrade());
            return;
        }

        // Calculate needed average on remaining assignments
        double neededTotal = desiredPercentage - currentTotal;
        if (neededTotal <= 0.0) {
            JOptionPane.showMessageDialog(this, "You have already achieved the desired grade!");
            return;
        }
        double neededAverage = neededTotal / (remainingWeight / 100.0);

        // Ask for number of remaining items in each category
        StringBuilder sb = new StringBuilder();
        sb.append("To achieve a final grade of ").append(desiredLetterGrade).append(" (").append(String.format("%.2f", desiredPercentage)).append("%),\n");
        sb.append("you need the following average grades in the remaining categories:\n\n");

        for (Category category : categoriesWithNoGrades) {
            String itemsLeftStr = JOptionPane.showInputDialog(this, "Enter the number of remaining items in category '" + category.getName() + "':");
            if (itemsLeftStr == null) return;
            int itemsLeft;
            try {
                itemsLeft = Integer.parseInt(itemsLeftStr.trim());
                if (itemsLeft <= 0) {
                    JOptionPane.showMessageDialog(this, "Number of items must be positive.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
                return;
            }
            // Calculate needed average per item
            double categoryWeight = category.getWeight();
            double categoryNeededTotal = neededAverage * (categoryWeight / remainingWeight);
            double neededGradePerItem = categoryNeededTotal;
            sb.append("Category '").append(category.getName()).append("' (").append(categoryWeight).append("% of total grade):\n");
            sb.append(" - Average grade needed per item: ").append(String.format("%.2f", neededGradePerItem)).append("%\n\n");
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Needed Grades Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private ClassRecord deepCopyClassRecord(ClassRecord original) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(original);
            out.flush();
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            return (ClassRecord) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void editGrade() {
        // Select class
        ArrayList<ClassRecord> classes = gradeBook.getClasses();
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available.");
            return;
        }
        String[] classNames = classes.stream().map(ClassRecord::getName).toArray(String[]::new);
        String selectedClass = (String) JOptionPane.showInputDialog(this, "Select a class:", "Edit or Delete Grades", JOptionPane.PLAIN_MESSAGE, null, classNames, classNames[0]);
        if (selectedClass == null) return;

        ClassRecord classRecord = gradeBook.getClassByName(selectedClass);

        // Select category
        ArrayList<Category> categories = classRecord.getCategories();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No categories available in this class.");
            return;
        }
        String[] categoryNames = categories.stream().map(Category::getName).toArray(String[]::new);
        String selectedCategory = (String) JOptionPane.showInputDialog(this, "Select a category:", "Edit or Delete Grades", JOptionPane.PLAIN_MESSAGE, null, categoryNames, categoryNames[0]);
        if (selectedCategory == null) return;

        Category category = classRecord.getCategoryByName(selectedCategory);

        // Display grades with indices
        ArrayList<Double> grades = category.getGrades();
        if (grades.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No grades available in this category.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < grades.size(); i++) {
            sb.append(i + 1).append(") ").append(grades.get(i)).append("\n");
        }
        String selectedGradeIndexStr = JOptionPane.showInputDialog(this, "Select the number of the grade to edit or delete:\n" + sb.toString());
        if (selectedGradeIndexStr == null) return;
        try {
            int index = Integer.parseInt(selectedGradeIndexStr.trim()) - 1;
            if (index < 0 || index >= grades.size()) {
                JOptionPane.showMessageDialog(this, "Invalid selection.");
                return;
            }
            Double selectedGrade = grades.get(index);
            // Choose to edit or delete
            String[] options = {"Edit", "Delete", "Cancel"};
            int choice = JOptionPane.showOptionDialog(this, "Selected Grade: " + selectedGrade, "Edit or Delete", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (choice == 0) {
                // Edit grade
                String newGradeStr = JOptionPane.showInputDialog(this, "Enter the new grade:", selectedGrade.toString());
                if (newGradeStr == null) return;
                try {
                    double newGrade = Double.parseDouble(newGradeStr);
                    category.editGrade(index, newGrade);
                    JOptionPane.showMessageDialog(this, "Grade updated successfully.");
                    updateClassesTable();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid grade. Please enter a numerical value.");
                }
            } else if (choice == 1) {
                // Delete grade
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this grade?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    category.deleteGrade(index);
                    JOptionPane.showMessageDialog(this, "Grade deleted successfully.");
                    updateClassesTable();
                }
            } else {
                // Cancel
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
        }
    }

    private void deleteClass() {
        ArrayList<ClassRecord> classes = gradeBook.getClasses();
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes available.");
            return;
        }
        String[] classNames = classes.stream().map(ClassRecord::getName).toArray(String[]::new);
        String selectedClass = (String) JOptionPane.showInputDialog(this, "Select a class to delete:", "Delete Class", JOptionPane.PLAIN_MESSAGE, null, classNames, classNames[0]);
        if (selectedClass == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the class '" + selectedClass + "' and all its data?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ClassRecord classRecord = gradeBook.getClassByName(selectedClass);
            gradeBook.getClasses().remove(classRecord);
            JOptionPane.showMessageDialog(this, "Class '" + selectedClass + "' deleted successfully.");
            updateClassesTable();
        }
    }

    private void deleteAllData() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete ALL data? This action cannot be undone.", "Confirm Delete All", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            gradeBook.getClasses().clear();
            JOptionPane.showMessageDialog(this, "All data has been deleted.");
            updateClassesTable();
            // Also delete the data file
            File dataFile = new File(DATA_FILE);
            if (dataFile.exists()) {
                dataFile.delete();
            }
        }
    }

    private void showHelpDialog() {
        String helpText = "Grade Book Application Help\n\n"
                + "This application allows you to manage your grades for multiple classes.\n\n"
                + "Features:\n"
                + "- Add Classes: Add new classes to your grade book.\n"
                + "- Configure Classes: Set up categories, weights, grading scales, and rounding options.\n"
                + "- Add Grades: Add grades to the categories in your classes.\n"
                + "- Grades Display: View all grades in the main window.\n"
                + "- See More Information: View detailed statistics for each class.\n"
                + "- Add Hypothetical Grades: See how hypothetical grades affect your final grade.\n"
                + "- Calculate Needed Grades: Determine what grades you need on remaining assignments to achieve a desired letter grade.\n"
                + "- Edit or Delete Grades: Modify or remove existing grades.\n"
                + "- Delete Classes and Data: Remove classes or all data from the application.\n"
                + "- Save Data: Your data is automatically saved when you exit the application or you can manually save using the 'Save' option in the File menu.\n\n"
                + "Input Limitations:\n"
                + "- Grades should be numerical values between 0 and 100.\n"
                + "- Category weights should sum up to 100% for accurate final grade calculations.\n"
                + "- When entering grading scales, ensure that the cutoff percentages are in descending order.\n"
                + "- Rounding thresholds should be numerical values (e.g., 0.5 for half a percent).\n\n"
                + "For further assistance, please refer to the user manual or contact support.";

        JTextArea textArea = new JTextArea(helpText);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(this, scrollPane, "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exitApplication() {
        int option = JOptionPane.showConfirmDialog(this, "Do you want to save changes before exiting?", "Exit Confirmation", JOptionPane.YES_NO_CANCEL_OPTION);
        if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
            // Do nothing, stay in the application
            return;
        }
        if (option == JOptionPane.YES_OPTION) {
            saveGradeBook();
        }
        // If NO is selected, proceed without saving
        System.exit(0);
    }

    private void saveGradeBook() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(gradeBook);
            System.out.println("GradeBook data saved to " + DATA_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadGradeBook() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            gradeBook = (GradeBook) in.readObject();
            System.out.println("GradeBook data loaded from " + DATA_FILE);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            gradeBook = new GradeBook();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GradeBookGUI());
    }
}