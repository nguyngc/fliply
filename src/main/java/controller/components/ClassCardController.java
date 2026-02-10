package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ClassCardController {

    @FXML
    private Label classNameLabel;
    @FXML
    private Label teacherNameLabel;
    @FXML
    private Label progressTextLabel;
    @FXML
    private ProgressBar progressBar;

    public void setClassName(String name) {
        classNameLabel.setText(name);
    }

    public void setTeacherName(String name) {
        teacherNameLabel.setText(name);
    }

    public void setProgress(double value) {
        progressBar.setProgress(value);
        progressTextLabel.setText((int) (value * 100) + "% Completed");
    }
}
