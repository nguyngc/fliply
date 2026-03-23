package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import model.AppState;
import model.entity.User;
import model.service.UserService;
import util.LocaleManager;
import view.Navigator;

import java.util.ResourceBundle;

public class AccountPasswordController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private PasswordField currentPwdField;
    @FXML
    private PasswordField newPwdField;
    @FXML
    private PasswordField confirmPwdField;

    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();

    private final ResourceBundle rb =  ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

    @FXML
    private void initialize() {
        headerController.setBackVisible(true);
        headerController.setTitle(rb.getString("pwd.header.title"));
        headerController.setSubtitle("");
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.ACCOUNT));
        headerController.applyVariant(AppState.isTeacher()
                ? HeaderController.Variant.TEACHER
                : HeaderController.Variant.STUDENT);

        AppState.navOverride.set(AppState.NavItem.ACCOUNT);
    }

    @FXML
    private void onSave() {
        hideError();

        String current = currentPwdField.getText() == null ? "" : currentPwdField.getText();
        String newPwd = newPwdField.getText() == null ? "" : newPwdField.getText();
        String confirm = confirmPwdField.getText() == null ? "" : confirmPwdField.getText();

//        // Demo validation
//        if (!current.equals(AppState.demoPassword.get())) {
//            showError("Current password is incorrect.");
//            return;
//        }
        // get current user
        User user = AppState.currentUser.get();
        if (user == null) {
            showError(rb.getString("pwd.error.user"));
            return;
        }
        // check old pass
        if (!current.equals(user.getPassword())) {
            showError(rb.getString("pwd.error.oldPwd"));
            return;
        }
        // check new pass
        if (newPwd.length() < 6) {
            showError(rb.getString("pwd.error.newPwd1"));
            return;
        }
        if (!newPwd.equals(confirm)) {
            showError(rb.getString("pwd.error.newPwd2"));
            return;
        }

//        // Demo save
//        AppState.demoPassword.set(newPwd);
//        Navigator.go(AppState.Screen.ACCOUNT);
        // Save pass
        user.setPassword(newPwd);
        userService.update(user);
        Navigator.go(AppState.Screen.ACCOUNT);

    }

    @FXML
    private void onCancel() {
        Navigator.go(AppState.Screen.ACCOUNT);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
