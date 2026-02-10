package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import model.AppState;
import view.Navigator;

public class HomeController {
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private void initialize() {
        // Configure header
        if (headerController != null) {
            headerController.setTitle("Hi, Student!");
            headerController.setSubtitle("Let's start learning");

            headerController.applyVariant(HeaderController.Variant.STUDENT); // or TEACHER
        }
    }

    @FXML
    private void goClasses() {
        Navigator.go(AppState.Screen.CLASSES);
    }

    @FXML
    private void goQuizzes() {
        Navigator.go(AppState.Screen.QUIZZES);
    }
}
