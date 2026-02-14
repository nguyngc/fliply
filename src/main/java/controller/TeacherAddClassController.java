package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import model.AppState;
import view.Navigator;

public class TeacherAddClassController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private TextField classCodeField;

    @FXML
    private void initialize() {
        headerController.setTitle("New Class");
        headerController.setBackVisible(true);
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASSES));
    }

    @FXML
    private void onAdd() {
        String code = classCodeField.getText() == null ? "" : classCodeField.getText().trim();
        if (code.isBlank()) return;

        AppState.ClassItem newC = new AppState.ClassItem(code, "Teacher's Name");
        AppState.demoClasses.add(newC);

        Navigator.go(AppState.Screen.CLASSES);
    }

    @FXML
    private void onCancel() {
        Navigator.go(AppState.Screen.CLASSES);
    }
}
