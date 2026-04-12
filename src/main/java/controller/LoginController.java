package controller;

import controller.components.PasswordVisibilitySupport;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.AppState;
import model.dao.UserDao;
import model.entity.User;
import util.Dialogs;
import util.LocaleManager;
import view.Navigator;

import java.util.ResourceBundle;

/**
 * Controller for the login screen.
 * Handles user authentication with email and password validation.
 * Supports password visibility toggle and multiple login options (email, Google, register, forgot password).
 */
public class LoginController {

    // ========== Password Visibility Images ==========
    // Image for the "eye open" icon (password visible)
    private final Image eyeOpen = new Image(getClass().getResourceAsStream("/images/eye_open.png"));
    
    // Image for the "eye closed" icon (password hidden)
    private final Image eyeClosed = new Image(getClass().getResourceAsStream("/images/eye_closed.png"));
    
    // ========== Error Display ==========
    // Label for displaying login error messages
    @FXML
    private Label errorLabel;
    
    // Flag to track whether password is currently visible
    private boolean passwordVisible = false;
    
    // ========== Database Access ==========
    // Data access object for user authentication
    private final UserDao userDao = new UserDao();

    // ========== UI Components ==========
    // Text field for email input
    @FXML
    private TextField emailField;
    
    // Password field for masked password input
    @FXML
    private PasswordField passwordField;
    
    // Text field for visible password input (when toggle is on)
    @FXML
    private TextField passwordTextField;
    
    // Icon for password visibility toggle button
    @FXML
    private ImageView eyeIcon;

    // Resource bundle for localized strings
    private final ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the password visibility toggle mechanism and error label behavior.
     */
    @FXML
    private void initialize() {
        // ========== Hide Password Text Field Initially ==========
        // The password text field is hidden by default (password field is shown instead)
        PasswordVisibilitySupport.initializeHidden(passwordTextField, eyeIcon, eyeClosed);
        
        // ========== Error Label Auto-Hide ==========
        // Listen for changes in email field and hide error label when user types
        emailField.textProperty().addListener((o, old, n) -> errorLabel.setVisible(false));
        
        // Listen for changes in password field and hide error label when user types
        passwordField.textProperty().addListener((o, old, n) -> errorLabel.setVisible(false));
        
        // Listen for changes in password text field and hide error label when user types
        passwordTextField.textProperty().addListener((o, old, n) -> errorLabel.setVisible(false));
    }

    /**
     * Toggles password visibility between masked and visible states.
     * Switches between PasswordField (masked) and TextField (visible) for a better UX.
     * Updates the eye icon to reflect the current visibility state.
     */
    @FXML
    private void togglePassword() {
        // Toggle the visibility flag
        passwordVisible = !passwordVisible;
        PasswordVisibilitySupport.apply(passwordVisible, passwordField, passwordTextField, eyeIcon, eyeOpen, eyeClosed);
    }

    /**
     * Handles the login button click event.
     * Validates user credentials against the database.
     * On success, sets user role and navigates to the home screen.
     * On failure, displays an error message.
     */
    @FXML 
    public void login() {
        // ========== Get Input Values ==========
        // Retrieve email from the input field
        String email = emailField.getText();
        
        // Get password from the appropriate field (visible or masked)
        String password = passwordVisible ? passwordTextField.getText() : passwordField.getText();
        
        // ========== Authenticate User ==========
        // Query the database for user with matching email and password
        User user = userDao.findByEmailAndPassword(email, password);
        
        if (user != null) {
            // ========== Login Success ==========
            // Hide the error label
            errorLabel.setVisible(false);
            
            // Set the user's language preference
            LocaleManager.setLocaleByLanguage(user.getLanguage());
            
            // Store the authenticated user in app state
            AppState.currentUser.set(user);
            
            // Set the user's role (teacher or student)
            AppState.setRole(user.isTeacher() ? AppState.Role.TEACHER : AppState.Role.STUDENT);
            
            // Navigate to the home screen
            Navigator.go(AppState.Screen.HOME);
        } else {
            // ========== Login Failed ==========
            // Display error message with localized string
            errorLabel.setText(rb.getString("login.error"));
            errorLabel.setVisible(true);
        }
    }

    /**
     * Handles the register button click event.
     * Navigates to the registration screen.
     */
    @FXML
    public void goRegister() {
        Navigator.go(AppState.Screen.REGISTER);
    }

    /**
     * Handles the forgot password button click event.
     * Displays an information dialog with password recovery instructions.
     */
    @FXML
    public void onForgotPassword() {
        // Get localized strings for the alert dialog
        String title = rb.getString("login.forgot");
        String message = rb.getString("login.forgot");

        // Create and display an information alert
        Dialogs.show(Alert.AlertType.INFORMATION, title, message);
    }
}
