package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import model.AppState;
import util.LocaleManager;
import view.Navigator;

import java.util.ResourceBundle;

public class AccountAboutController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private void initialize() {
        ResourceBundle rb =  ResourceBundle.getBundle("Messages", LocaleManager.getLocale());
        headerController.setBackVisible(true);
        headerController.setTitle(rb.getString("about.header.title"));
        headerController.setSubtitle("");
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.ACCOUNT));
        headerController.applyVariant(AppState.isTeacher()
                ? HeaderController.Variant.TEACHER
                : HeaderController.Variant.STUDENT);

        AppState.navOverride.set(AppState.NavItem.ACCOUNT);
    }
}
