// ClassRecord.java
import java.io.Serializable;
import java.util.ArrayList;

public class ClassRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private ArrayList<Category> categories;
    private GradingScale gradingScale;
    private boolean roundingEnabled;
    private double roundingThreshold; // Rounding threshold

    public ClassRecord(String name) {
        this.name = name;
        categories = new ArrayList<>();
        gradingScale = new GradingScale();
        roundingEnabled = false;
        roundingThreshold = 0.0;
    }

    public String getName() {
        return name;
    }

    public void addCategory(Category category) {
        categories.add(category);
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public Category getCategoryByName(String categoryName) {
        for (Category cat : categories) {
            if (cat.getName().equalsIgnoreCase(categoryName)) {
                return cat;
            }
        }
        return null;
    }

    public void setGradingScale(GradingScale scale) {
        this.gradingScale = scale;
    }

    public GradingScale getGradingScale() {
        return gradingScale;
    }

    public void setRoundingEnabled(boolean enabled) {
        this.roundingEnabled = enabled;
    }

    public boolean isRoundingEnabled() {
        return roundingEnabled;
    }

    public void setRoundingThreshold(double threshold) {
        this.roundingThreshold = threshold;
    }

    public double getRoundingThreshold() {
        return roundingThreshold;
    }

    // Method to calculate final grade
    public double calculateFinalGrade() {
        double finalGrade = 0.0;
        ArrayList<Double> categoryAverages = new ArrayList<>();
        double totalWeight = 0.0;

        // Step 1: Calculate averages of categories with grades
        for (Category cat : categories) {
            double weight = cat.getWeight();
            totalWeight += weight;

            if (!cat.getGrades().isEmpty()) {
                double categoryAverage = cat.calculateAverage();
                categoryAverages.add(categoryAverage);
            }
        }

        // Step 2: Compute the average of category averages
        double averageOfAverages = 0.0;
        if (!categoryAverages.isEmpty()) {
            double sumOfAverages = 0.0;
            for (double avg : categoryAverages) {
                sumOfAverages += avg;
            }
            averageOfAverages = sumOfAverages / categoryAverages.size();
        } else {
            // If no categories have grades, default average is 0.0
            averageOfAverages = 0.0;
        }

        // Step 3 & 4: Calculate final grade using assigned averages
        for (Category cat : categories) {
            double categoryGrade;
            double weight = cat.getWeight();

            if (!cat.getGrades().isEmpty()) {
                categoryGrade = cat.calculateAverage();
            } else {
                categoryGrade = averageOfAverages;
            }

            finalGrade += categoryGrade * (weight / 100.0);
        }

        // Step 5: Apply rounding if enabled
        if (roundingEnabled) {
            finalGrade = applyRounding(finalGrade);
        }

        return finalGrade;
    }

    // Apply rounding logic
    private double applyRounding(double grade) {
        // Rounding based on threshold
        Double nextCutoff = gradingScale.getNextHigherCutoff(grade);
        if (nextCutoff != null && (nextCutoff - grade) <= roundingThreshold) {
            return nextCutoff;
        }
        return grade;
    }

    public String getLetterGrade() {
        double finalGrade = calculateFinalGrade();
        return gradingScale.getLetterGrade(finalGrade);
    }
}