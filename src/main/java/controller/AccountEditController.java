package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import model.AppState;
import view.Navigator;

public class AccountEditController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;

    @FXML
    private void initialize() {
        headerController.setBackVisible(true);
        headerController.setTitle("Edit Account");
        headerController.setSubtitle("");
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.ACCOUNT));
        headerController.applyVariant(AppState.isTeacher()
                ? HeaderController.Variant.TEACHER
                : HeaderController.Variant.STUDENT);

        AppState.navOverride.set(AppState.NavItem.ACCOUNT);

        // Demo binding
        firstNameField.setText(AppState.currentFirstName.get());
        lastNameField.setText(AppState.currentLastName.get());
        emailField.setText(AppState.currentEmail.get());
    }

    @FXML
    private void onSave() {
        AppState.currentFirstName.set(firstNameField.getText() == null ? "" : firstNameField.getText().trim());
        AppState.currentLastName.set(lastNameField.getText() == null ? "" : lastNameField.getText().trim());
        Navigator.go(AppState.Screen.ACCOUNT);
    }

    @FXML
    private void onCancel() {
        Navigator.go(AppState.Screen.ACCOUNT);
    }
}
