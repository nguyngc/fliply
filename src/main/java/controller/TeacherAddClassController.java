package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import model.AppState;
import model.dao.ClassModelDao;
import model.entity.ClassModel;
import model.service.TeacherAddClassService;
import util.LocaleManager;
import view.Navigator;

import java.util.ResourceBundle;

public class TeacherAddClassController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private TextField classCodeField;

    private final TeacherAddClassService teacherAddClass = new TeacherAddClassService();

    private final ResourceBundle rb =  ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

    @FXML
    private void initialize() {
        headerController.setTitle(rb.getString("addClass.new"));
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
            System.out.println(rb.getString("addClass.error") + e.getMessage());}
    }

    @FXML
    private void onCancel() {
        Navigator.go(AppState.Screen.CLASSES);
    }
}
