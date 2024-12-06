// Category.java
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private double weight; // Weight as a percentage
    private int numGradesDropped; // Number of lowest grades to drop
    private ArrayList<Double> grades;

    public Category(String name, double weight, int numGradesDropped) {
        this.name = name;
        this.weight = weight;
        this.numGradesDropped = numGradesDropped;
        this.grades = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public int getNumGradesDropped() {
        return numGradesDropped;
    }

    public ArrayList<Double> getGrades() {
        return grades;
    }

    public void addGrade(double grade) {
        grades.add(grade);
    }

    public void editGrade(int index, double newGrade) {
        if (index >= 0 && index < grades.size()) {
            grades.set(index, newGrade);
        }
    }

    public void deleteGrade(int index) {
        if (index >= 0 && index < grades.size()) {
            grades.remove(index);
        }
    }

    /**
     * Calculates the average grade for this category, considering the number of lowest grades to drop.
     *
     * @return The average grade after dropping the lowest grades. Returns 0.0 if no grades are present.
     */
    public double calculateAverage() {
        if (grades.isEmpty()) {
            return 0.0;
        }

        ArrayList<Double> sortedGrades = new ArrayList<>(grades);
        Collections.sort(sortedGrades);

        int gradesToConsider = sortedGrades.size() - numGradesDropped;
        if (gradesToConsider <= 0) {
            return 0.0; // All grades are dropped
        }

        double sum = 0.0;
        for (int i = numGradesDropped; i < sortedGrades.size(); i++) {
            sum += sortedGrades.get(i);
        }

        return sum / gradesToConsider;
    }
}