package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import model.AppState;
import view.Navigator;

public class AccountAboutController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private void initialize() {
        headerController.setBackVisible(true);
        headerController.setTitle("About Us");
        headerController.setSubtitle("");
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.ACCOUNT));
        headerController.applyVariant(AppState.isTeacher()
                ? HeaderController.Variant.TEACHER
                : HeaderController.Variant.STUDENT);

        AppState.navOverride.set(AppState.NavItem.ACCOUNT);
    }
}
