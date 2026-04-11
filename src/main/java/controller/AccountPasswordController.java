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

/**
 * Controller for managing password change functionality.
 * Allows users to change their account password with validation for security.
 */
public class AccountPasswordController {

    // UI Components injected from FXML
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    // Password input fields
    @FXML
    private PasswordField currentPwdField;
    @FXML
    private PasswordField newPwdField;
    @FXML
    private PasswordField confirmPwdField;

    // Label for displaying error messages
    @FXML
    private Label errorLabel;

    // Service for updating user information in the database
    private final UserService userService = new UserService();

    // Resource bundle for localized strings
    private final ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the header, configures navigation, and applies styling based on user role.
     */
    @FXML
    private void initialize() {
        // Configure back button
        headerController.setBackVisible(true);
        headerController.setTitle(rb.getString("pwd.header.title"));
        headerController.setSubtitle("");
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.ACCOUNT));
        
        // Apply styling variant based on whether the user is a teacher or student
        headerController.applyVariant(AppState.isTeacher()
                ? HeaderController.Variant.TEACHER
                : HeaderController.Variant.STUDENT);

        // Set the active navigation item to ACCOUNT
        AppState.navOverride.set(AppState.NavItem.ACCOUNT);
    }

    /**
     * Handles the save button click event.
     * Validates the current password, checks that new passwords match and meet requirements,
     * then updates the user's password in the database.
     */
    @FXML
    private void onSave() {
        // Clear any previous error messages
        hideError();

        // Get text from password fields, default to empty string if null
        String current = currentPwdField.getText() == null ? "" : currentPwdField.getText();
        String newPwd = newPwdField.getText() == null ? "" : newPwdField.getText();
        String confirm = confirmPwdField.getText() == null ? "" : confirmPwdField.getText();

        // Get the currently logged-in user
        User user = AppState.currentUser.get();
        if (user == null) {
            showError(rb.getString("pwd.error.user"));
            return;
        }
        
        // Validate that the current password matches the user's actual password
        if (!current.equals(user.getPassword())) {
            showError(rb.getString("pwd.error.oldPwd"));
            return;
        }
        
        // Validate that new password is at least 6 characters long
        if (newPwd.length() < 6) {
            showError(rb.getString("pwd.error.newPwd1"));
            return;
        }
        
        // Validate that new password and confirmation password match
        if (!newPwd.equals(confirm)) {
            showError(rb.getString("pwd.error.newPwd2"));
            return;
        }

        // Update the user's password and save to database
        user.setPassword(newPwd);
        userService.update(user);
        
        // Navigate back to account screen after successful password change
        Navigator.go(AppState.Screen.ACCOUNT);

    }

    /**
     * Handles the cancel button click event.
     * Navigates back to the account screen without saving any changes.
     */
    @FXML
    private void onCancel() {
        Navigator.go(AppState.Screen.ACCOUNT);
    }

    /**
     * Displays an error message to the user.
     * Makes the error label visible and displays the provided message.
     *
     * @param msg The error message to display
     */
    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Hides the error message from the user.
     * Clears the error label text and makes it invisible.
     */
    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
