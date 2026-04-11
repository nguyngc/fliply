package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import model.AppState;
import util.LocaleManager;
import view.Navigator;

import java.util.ResourceBundle;

/**
 * Controller for displaying the "About" information screen.
 * Shows application details and information to the user.
 */
public class AccountAboutController {

    // UI Components injected from FXML
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the header, configures navigation, and applies styling based on user role.
     */
    @FXML
    private void initialize() {
        // Get localized string resources
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());
        
        // Configure back button
        headerController.setBackVisible(true);
        headerController.setTitle(rb.getString("about.header.title"));
        headerController.setSubtitle("");
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.ACCOUNT));
        
        // Apply styling variant based on whether the user is a teacher or student
        headerController.applyVariant(AppState.isTeacher()
                ? HeaderController.Variant.TEACHER
                : HeaderController.Variant.STUDENT);

        // Set the active navigation item to ACCOUNT
        AppState.navOverride.set(AppState.NavItem.ACCOUNT);
    }
}
