package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import model.AppState;
import view.Navigator;

public class AccountController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private void initialize() {
        headerController.setBackVisible(false);
        headerController.setTitle("My Account");
        headerController.setSubtitle("");
        headerController.applyVariant(AppState.isTeacher()
                ? HeaderController.Variant.TEACHER
                : HeaderController.Variant.STUDENT);

        AppState.navOverride.set(AppState.NavItem.ACCOUNT);
    }

    @FXML
    private void onEditAccount() {
        Navigator.go(AppState.Screen.ACCOUNT_EDIT);
    }

    @FXML
    private void onChangePassword() {
        Navigator.go(AppState.Screen.ACCOUNT_PASSWORD);
    }

    @FXML
    private void onHelp() {
        Navigator.go(AppState.Screen.ACCOUNT_HELP);
    }

    @FXML
    private void onAbout() {
        Navigator.go(AppState.Screen.ACCOUNT_ABOUT);
    }
}
