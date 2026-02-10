package controller;

import controller.components.HeaderController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import model.AppState;
import view.Navigator;

public class ClassesController {
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private void initialize() {
        // Configure header
        if (headerController != null) {
            headerController.setTitle("My Classes");

            headerController.applyVariant(HeaderController.Variant.STUDENT); // or TEACHER
        }
    }

    @FXML
    private void openClassDetail(ActionEvent event) {
        Button btn = (Button) event.getSource();
        Object data = btn.getUserData();

        String classCode = "Class 00001-A";
        String teacherName = "Teacher’s Name";

        if (data != null) {
            String[] parts = data.toString().split("\\|");
            if (parts.length >= 2) {
                classCode = parts[0];
                teacherName = parts[1];
            }
        }

        AppState.selectedClassCode.set(classCode);
        AppState.selectedTeacherName.set(teacherName);

        Navigator.go(AppState.Screen.CLASS_DETAIL);
    }
}
