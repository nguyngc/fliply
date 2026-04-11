package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import model.AppState;
import model.entity.User;
import model.service.UserService;
import util.LocaleManager;
import view.Navigator;

import java.util.ResourceBundle;

/**
 * Controller for editing user account information.
 * Allows users to update their first name, last name, and email address.
 */
public class AccountEditController {

    // UI Components injected from FXML
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    // Text fields for user information
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;

    // Service for updating user information in the database
    private final UserService userService = new UserService();

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the header, configures navigation, applies styling based on user role,
     * and populates the text fields with the current user's information.
     */
    @FXML
    private void initialize() {
        // Get localized string resources
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());
        
        // Configure back button
        headerController.setBackVisible(true);
        headerController.setTitle(rb.getString("edit.header.title"));
        headerController.setSubtitle("");
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.ACCOUNT));
        
        // Apply styling variant based on whether the user is a teacher or student
        headerController.applyVariant(AppState.isTeacher()
                ? HeaderController.Variant.TEACHER
                : HeaderController.Variant.STUDENT);

        // Set the active navigation item to ACCOUNT
        AppState.navOverride.set(AppState.NavItem.ACCOUNT);

        // Load current user and populate text fields with existing information
        User user = AppState.currentUser.get();
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
    }

    /**
     * Handles the save button click event.
     * Updates the current user's first name, last name, and email with trimmed values
     * from the text fields, saves to the database, then navigates back to the account screen.
     */
    @FXML
    private void onSave() {
        // Get the current logged-in user
        User user = AppState.currentUser.get();
        
        // Update user information with values from text fields, trim whitespace
        user.setFirstName(firstNameField.getText() == null ? "" : firstNameField.getText().trim());
        user.setLastName(lastNameField.getText() == null ? "" : lastNameField.getText().trim());
        user.setEmail(emailField.getText() == null ? "" : emailField.getText().trim());
        
        // Save the updated user information to the database
        userService.update(user);

        // Navigate back to account screen after successful update
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
}
