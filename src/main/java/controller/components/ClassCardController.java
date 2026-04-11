package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import model.AppState;
import util.LocaleManager;

import java.util.ResourceBundle;

/**
 * Controller for a class card component.
 * Displays class information differently based on user role (teacher or student).
 * Includes progress tracking and handles card interactions.
 */
public class ClassCardController {

    // Common UI components
    @FXML
    private Label classNameLabel;

    // Student variant UI components - visible only for student users
    @FXML
    private VBox studentInfoBox;
    @FXML
    private Label teacherNameLabel;

    // Teacher variant UI components - visible only for teacher users
    @FXML
    private VBox teacherInfoBox;
    @FXML
    private Label studentsCountLabel;
    @FXML
    private Label setsCountLabel;

    // Progress tracking UI components
    @FXML
    private Label progressTextLabel;
    @FXML
    private ProgressBar progressBar;

    // Callback for card click events
    private Runnable onClick;

    // Resource bundle for localized strings
    private final ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

    /**
     * Applies the appropriate UI variant based on the current user's role.
     * Shows teacher-specific information for teachers, student-specific information for students.
     */
    public void applyRoleVariant() {
        // Check if the current user is a teacher
        boolean isTeacher = AppState.isTeacher();

        // Show/hide teacher info box based on user role
        if (teacherInfoBox != null) {
            teacherInfoBox.setVisible(isTeacher);
            teacherInfoBox.setManaged(isTeacher);
        }
        
        // Show/hide student info box based on user role
        if (studentInfoBox != null) {
            studentInfoBox.setVisible(!isTeacher);
            studentInfoBox.setManaged(!isTeacher);
        }
    }

    /**
     * Configures the card to display student view information.
     * Shows the class name, teacher name, and progress.
     *
     * @param classCode The name/code of the class
     * @param teacherName The full name of the teacher
     * @param progress The progress value (0.0 to 1.0)
     */
    public void setStudentCard(String classCode, String teacherName, double progress) {
        // Set the class name/code
        classNameLabel.setText(classCode);
        
        // Set the teacher name label
        if (teacherNameLabel != null) teacherNameLabel.setText(teacherName);
        
        // Set the progress bar
        setProgress(progress);
        
        // Apply the student variant UI
        applyRoleVariant();
    }

    /**
     * Configures the card to display teacher view information.
     * Shows the class name, number of students, number of flashcard sets, and progress.
     *
     * @param classCode The name/code of the class
     * @param students The number of students in the class
     * @param sets The number of flashcard sets in the class
     * @param progress The progress value (0.0 to 1.0)
     */
    public void setTeacherCard(String classCode, int students, int sets, double progress) {
        // Set the class name/code
        classNameLabel.setText(classCode);
        
        // Set the student count with localized string
        if (studentsCountLabel != null) studentsCountLabel.setText(students + " " + rb.getString("classDetail.students"));
        
        // Set the flashcard set count with localized string
        if (setsCountLabel != null) setsCountLabel.setText(sets + " " + rb.getString("classDetail.sets"));
        
        // Set the progress bar
        setProgress(progress);
        
        // Apply the teacher variant UI
        applyRoleVariant();
    }

    /**
     * Updates the progress bar and progress text label.
     * Displays the progress as a percentage value.
     *
     * @param value The progress value as a decimal (0.0 to 1.0)
     */
    public void setProgress(double value) {
        // Update the progress bar with the value
        if (progressBar != null) progressBar.setProgress(value);
        
        // Update the progress text label with percentage
        if (progressTextLabel != null) progressTextLabel.setText((int) (value * 100) + "% Completed");
    }

    /**
     * Sets the callback to be invoked when the card is clicked/fired.
     *
     * @param r The Runnable to execute on card click
     */
    public void setOnClick(Runnable r) {
        this.onClick = r;
    }

    /**
     * Initializes the controller when the FXML is loaded.
     * Currently serves as a placeholder for future initialization logic.
     */
    @FXML
    private void initialize() {
        // Placeholder for initialization logic
    }

    /**
     * Invokes the onClick callback if one has been set.
     * Used to trigger the card's associated action when clicked.
     */
    public void fire() {
        if (onClick != null) onClick.run();
    }
}
