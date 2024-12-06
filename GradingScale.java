// GradingScale.java
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class GradingScale implements Serializable {
    private static final long serialVersionUID = 1L;

    private TreeMap<Double, String> scale;

    public GradingScale(TreeMap<Double, String> scale) {
        this.scale = scale;
    }

    public TreeMap<Double, String> getScale() {
        return scale;
    }

    /**
     * Retrieves the letter grade corresponding to a given percentage.
     * @param percentage The percentage to evaluate.
     * @return The corresponding letter grade.
     */
    public String getLetterGrade(double percentage) {
        for (Map.Entry<Double, String> entry : scale.entrySet()) {
            if (percentage >= entry.getKey()) {
                return entry.getValue();
            }
        }
        return "F"; // Default to "F" if no other grade matches
    }
}