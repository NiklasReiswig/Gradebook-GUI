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
    private double extraCredit; // Extra credit points added to final grade

    public ClassRecord(String name, GradingScale gradingScale, boolean usesRounding, double roundingThreshold) {
        this.name = name;
        this.categories = new ArrayList<>();
        this.gradingScale = gradingScale;
        this.usesRounding = usesRounding;
        this.roundingThreshold = roundingThreshold;
        this.extraCredit = 0.0;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public ArrayList<Category> getCategories() {
        return categories;
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

    public void addCategory(Category category) {
        categories.add(category);
    }

    public Category getCategoryByName(String categoryName) {
        for (Category category : categories) {
            if (category.getName().equalsIgnoreCase(categoryName)) {
                return category;
            }
        }
        return null;
    }

    // Extra Credit Methods
    public double getExtraCredit() {
        return extraCredit;
    }

    public void setExtraCredit(double extraCredit) {
        if (extraCredit < 0.0) {
            throw new IllegalArgumentException("Extra credit cannot be negative.");
        }
        this.extraCredit = extraCredit;
    }

    /**
     * Resets the extra credit to zero.
     */
    public void resetExtraCredit() {
        this.extraCredit = 0.0;
    }

    /**
     * Calculates the final grade based on category averages, weights, and extra credit.
     * For categories with no grades, assigns the average of existing category averages.
     * Applies rounding if enabled and within the specified threshold.
     * Ensures the final grade does not exceed 100%.
     *
     * @return The final grade after applying extra credit and rounding logic.
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
            // Retrieve the grading scale map in descending order
            TreeMap<Double, String> scaleMapDesc = gradingScale.getScale();

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
                if (difference <= roundingThreshold) {
                    finalGrade = nextCutoff;
                }
            }
        } else {
            // Round to two decimal places without rounding up
            finalGrade = Math.round(finalGrade * 100.0) / 100.0;
        }

        // Add extra credit
        finalGrade += extraCredit;

        // Ensure final grade does not exceed 100%
        if (finalGrade > 100.0) {
            finalGrade = 100.0;
        }

        // Round again if necessary after adding extra credit
        finalGrade = Math.round(finalGrade * 100.0) / 100.0;

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