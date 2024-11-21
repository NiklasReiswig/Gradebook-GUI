// ClassRecord.java
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class ClassRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private ArrayList<Category> categories;
    private GradingScale gradingScale;
    private boolean usesRounding; // Indicates if rounding is used
    private double roundingThreshold; // Number of points for rounding

    public ClassRecord(String name, GradingScale gradingScale, boolean usesRounding, double roundingThreshold) {
        this.name = name;
        this.categories = new ArrayList<>();
        this.gradingScale = gradingScale;
        this.usesRounding = usesRounding;
        this.roundingThreshold = roundingThreshold;
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

    public Category getCategoryByName(String name) {
        for (Category category : categories) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }

    public GradingScale getGradingScale() {
        return gradingScale;
    }

    public boolean isUsesRounding() {
        return usesRounding;
    }

    public double getRoundingThreshold() {
        return roundingThreshold;
    }

    public void setUsesRounding(boolean usesRounding) {
        this.usesRounding = usesRounding;
    }

    public void setRoundingThreshold(double roundingThreshold) {
        this.roundingThreshold = roundingThreshold;
    }

    /**
     * Calculates the final grade based on category averages and weights.
     * For categories with no grades, assigns the average of existing category averages.
     * Applies rounding if enabled and within the specified threshold.
     *
     * @return The final grade after applying rounding logic.
     */
    public double calculateFinalGrade() {
        double finalGrade = 0.0;
        double sumWeights = 0.0;

        // First, collect averages of categories that have grades
        ArrayList<Double> existingAverages = new ArrayList<>();
        for (Category category : categories) {
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

        // Now, calculate the final grade
        for (Category category : categories) {
            double categoryAverage;
            if (!category.getGrades().isEmpty()) {
                categoryAverage = category.calculateAverage();
            } else {
                // Assign average of existing categories
                categoryAverage = averageOfAverages;
            }
            finalGrade += categoryAverage * (category.getWeight() / 100.0);
            sumWeights += category.getWeight();
        }

        // Ensure that sumWeights is approximately 100%
        if (Math.abs(sumWeights - 100.0) > 0.01) {
            // Optionally, handle weight normalization or alert the user
            // For now, we'll proceed as is
        }

        // Apply rounding if enabled
        if (usesRounding) {
            // Retrieve the grading scale map in ascending order
            TreeMap<Double, String> scaleMapAsc = new TreeMap<>(gradingScale.getScale());

            Double nextCutoff = null;
            for (Map.Entry<Double, String> entry : scaleMapAsc.entrySet()) {
                if (finalGrade < entry.getKey()) {
                    nextCutoff = entry.getKey();
                    break;
                }
            }

            if (nextCutoff != null) {
                double difference = nextCutoff - finalGrade;
                if (difference <= roundingThreshold) {
                    finalGrade = nextCutoff;
                }
            }
        } else {
            // Round to two decimal places without rounding up
            finalGrade = Math.round(finalGrade * 100.0) / 100.0;
        }

        return finalGrade;
    }

    /**
     * Retrieves the letter grade based on the final grade.
     *
     * @return The corresponding letter grade.
     */
    public String getLetterGrade() {
        double finalGrade = calculateFinalGrade();
        return gradingScale.getLetterGrade(finalGrade);
    }
}