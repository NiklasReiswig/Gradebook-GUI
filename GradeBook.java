// GradeBook.java
import java.io.Serializable;
import java.util.ArrayList;

public class GradeBook implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<ClassRecord> classes;

    public GradeBook() {
        classes = new ArrayList<>();
    }

    public void addClass(ClassRecord classRecord) {
        classes.add(classRecord);
    }

    public ArrayList<ClassRecord> getClasses() {
        return classes;
    }

    public ClassRecord getClassByName(String name) {
        for (ClassRecord classRecord : classes) {
            if (classRecord.getName().equalsIgnoreCase(name)) {
                return classRecord;
            }
        }
        return null;
    }
}