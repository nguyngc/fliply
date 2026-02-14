package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import model.AppState;

public class ClassCardController {

    @FXML
    private Label classNameLabel;

    // student variant
    @FXML
    private VBox studentInfoBox;
    @FXML
    private Label teacherNameLabel;

    // teacher variant
    @FXML
    private VBox teacherInfoBox;
    @FXML
    private Label studentsCountLabel;
    @FXML
    private Label setsCountLabel;

    @FXML
    private Label progressTextLabel;
    @FXML
    private ProgressBar progressBar;

    private Runnable onClick;

    public void applyRoleVariant() {
        boolean isTeacher = AppState.isTeacher();

        if (teacherInfoBox != null) {
            teacherInfoBox.setVisible(isTeacher);
            teacherInfoBox.setManaged(isTeacher);
        }
        if (studentInfoBox != null) {
            studentInfoBox.setVisible(!isTeacher);
            studentInfoBox.setManaged(!isTeacher);
        }
    }

    public void setStudentCard(String classCode, String teacherName, double progress) {
        classNameLabel.setText(classCode);
        if (teacherNameLabel != null) teacherNameLabel.setText(teacherName);
        setProgress(progress);
        applyRoleVariant();
    }

    public void setTeacherCard(String classCode, int students, int sets, double progress) {
        classNameLabel.setText(classCode);
        if (studentsCountLabel != null) studentsCountLabel.setText(students + " students");
        if (setsCountLabel != null) setsCountLabel.setText(sets + " set of flashcards");
        setProgress(progress);
        applyRoleVariant();
    }

    public void setProgress(double value) {
        if (progressBar != null) progressBar.setProgress(value);
        if (progressTextLabel != null) progressTextLabel.setText((int) (value * 100) + "% Completed");
    }

    public void setOnClick(Runnable r) {
        this.onClick = r;
    }

    @FXML
    private void initialize() {
        // optional
    }

    public void fire() {
        if (onClick != null) onClick.run();
    }
}
