// Category.java
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Category implements Serializable {
    private String name;
    private double weight;
    private ArrayList<Double> grades;
    private int numGradesDropped; // Number of lowest grades to drop

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

    public ArrayList<Double> getGrades() {
        return grades;
    }

    public int getNumGradesDropped() {
        return numGradesDropped;
    }

    public void setNumGradesDropped(int numGradesDropped) {
        this.numGradesDropped = numGradesDropped;
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

    public double calculateAverage() {
        if (grades.isEmpty()) {
            return 0.0;
        }

        // Sort grades in ascending order
        ArrayList<Double> sortedGrades = new ArrayList<>(grades);
        Collections.sort(sortedGrades);

        // Drop the lowest 'numGradesDropped' grades
        int numGradesToConsider = sortedGrades.size() - numGradesDropped;
        if (numGradesToConsider <= 0) {
            // All grades are dropped
            return 0.0;
        }

        double sum = 0.0;
        for (int i = numGradesDropped; i < sortedGrades.size(); i++) {
            sum += sortedGrades.get(i);
        }

        return sum / numGradesToConsider;
    }

    public double calculateMedian() {
        if (grades.isEmpty()) {
            return 0.0;
        }

        ArrayList<Double> sortedGrades = new ArrayList<>(grades);
        Collections.sort(sortedGrades);

        int size = sortedGrades.size();
        if (size % 2 == 1) {
            // Odd number of grades
            return sortedGrades.get(size / 2);
        } else {
            // Even number of grades
            return (sortedGrades.get(size / 2 - 1) + sortedGrades.get(size / 2)) / 2.0;
        }
    }

    public double getHighestGrade() {
        if (grades.isEmpty()) {
            return 0.0;
        }
        return Collections.max(grades);
    }

    public double getLowestGrade() {
        if (grades.isEmpty()) {
            return 0.0;
        }
        return Collections.min(grades);
    }
}