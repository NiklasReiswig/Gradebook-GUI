// GradingScale.java
import java.io.Serializable;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class GradingScale implements Serializable {
    private static final long serialVersionUID = 1L;
    private TreeMap<Double, String> scale;

    public GradingScale() {
        scale = new TreeMap<>();
        // Default grading scale
        scale.put(97.0, "A+");
        scale.put(93.0, "A");
        scale.put(90.0, "A-");
        scale.put(87.0, "B+");
        scale.put(83.0, "B");
        scale.put(80.0, "B-");
        scale.put(77.0, "C+");
        scale.put(73.0, "C");
        scale.put(70.0, "C-");
        scale.put(67.0, "D+");
        scale.put(63.0, "D");
        scale.put(60.0, "D-");
        scale.put(0.0, "F");
    }

    public void setScale(TreeMap<Double, String> scale) {
        this.scale = scale;
    }

    public TreeMap<Double, String> getScale() {
        return scale;
    }

    public String getLetterGrade(double percentage) {
        for (Map.Entry<Double, String> entry : scale.descendingMap().entrySet()) {
            if (percentage >= entry.getKey()) {
                return entry.getValue();
            }
        }
        return "F";
    }

    public Double getNextHigherCutoff(double grade) {
        NavigableMap<Double, String> higherEntries = scale.tailMap(grade, false);
        if (!higherEntries.isEmpty()) {
            return higherEntries.firstKey();
        }
        return null;
    }
}