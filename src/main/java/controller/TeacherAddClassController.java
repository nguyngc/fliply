package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import model.AppState;
import model.dao.ClassModelDao;
import model.entity.ClassModel;
import model.service.TeacherAddClassService;
import view.Navigator;

public class TeacherAddClassController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private TextField classCodeField;

    private final TeacherAddClassService teacherAddClass = new TeacherAddClassService();

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
        // create class
        try {
        teacherAddClass.createClass(code);
        Navigator.go(AppState.Screen.CLASSES);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());}
    }

    @FXML
    private void onCancel() {
        Navigator.go(AppState.Screen.CLASSES);
    }
}
