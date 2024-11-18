// Category.java
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Category implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private double weight; // Percentage weight of this category
    private ArrayList<Double> grades;
    private int numDropped; // Number of lowest grades to drop

    public Category(String name, double weight) {
        this.name = name;
        this.weight = weight;
        grades = new ArrayList<>();
        numDropped = 0;
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
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

    public ArrayList<Double> getGrades() {
        return grades;
    }

    public void setNumDropped(int numDropped) {
        this.numDropped = numDropped;
    }

    public int getNumDropped() {
        return numDropped;
    }

    public double calculateAverage() {
        if (grades.isEmpty()) {
            return 0.0;
        }
        ArrayList<Double> gradesCopy = new ArrayList<>(grades);
        Collections.sort(gradesCopy);
        // Drop the lowest grades
        for (int i = 0; i < numDropped && !gradesCopy.isEmpty(); i++) {
            gradesCopy.remove(0);
        }
        // Calculate average
        double sum = 0.0;
        for (double grade : gradesCopy) {
            sum += grade;
        }
        return sum / gradesCopy.size();
    }

    // New methods to calculate median, highest, and lowest grades
    public double calculateMedian() {
        if (grades.isEmpty()) {
            return 0.0;
        }
        ArrayList<Double> gradesCopy = new ArrayList<>(grades);
        Collections.sort(gradesCopy);
        int middle = gradesCopy.size() / 2;
        if (gradesCopy.size() % 2 == 0) {
            return (gradesCopy.get(middle - 1) + gradesCopy.get(middle)) / 2.0;
        } else {
            return gradesCopy.get(middle);
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