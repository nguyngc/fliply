package controller;

import controller.components.AccountHeaderSupport;
import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import model.AppState;
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
    @FXML
    private ResourceBundle resources;

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the header, configures navigation, and applies styling based on user role.
     */
    @FXML
    private void initialize() {
        AccountHeaderSupport.configure(headerController, resources, "about.header.title", () -> Navigator.go(AppState.Screen.ACCOUNT));

        // Set the active navigation item to ACCOUNT
        AppState.navOverride.set(AppState.NavItem.ACCOUNT);
    }
}
