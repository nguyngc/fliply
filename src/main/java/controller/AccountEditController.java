package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import model.AppState;
import model.entity.User;
import model.service.UserService;
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

    private final UserService userService = new UserService();

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

//        // Demo binding
//        firstNameField.setText(AppState.currentFirstName.get());
//        lastNameField.setText(AppState.currentLastName.get());
//        emailField.setText(AppState.currentEmail.get());
        // Load user
        User user = AppState.currentUser.get();
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());


    }

    @FXML
    private void onSave() {
        User user = AppState.currentUser.get();
        user.setFirstName(firstNameField.getText() == null ? "" : firstNameField.getText().trim());
        user.setLastName(lastNameField.getText() == null ? "" : lastNameField.getText().trim());
        user.setEmail(emailField.getText() == null ? "" : emailField.getText().trim());
        userService.update(user);

        Navigator.go(AppState.Screen.ACCOUNT);
    }

    @FXML
    private void onCancel() {
        Navigator.go(AppState.Screen.ACCOUNT);
    }
}
