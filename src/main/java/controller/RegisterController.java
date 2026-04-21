package controller;

import controller.components.PasswordVisibilitySupport;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.AppState;
import model.dao.UserDao;
import model.entity.User;
import util.Dialogs;
import util.LocaleManager;
import view.Navigator;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the registration screen.
 * Handles new user account creation with form validation.
 * Supports role selection (teacher/student) and password visibility toggle.
 * Validates email format, password strength, and checks for existing accounts.
 */
public class RegisterController {
    private static final Logger LOGGER = Logger.getLogger(RegisterController.class.getName());

    // ========== Password Visibility Images ==========
    // Image for the "eye open" icon (password visible)
    private final Image eyeOpen = new Image(getClass().getResourceAsStream("/images/eye_open.png"));
    
    // Image for the "eye closed" icon (password hidden)
    private final Image eyeClosed = new Image(getClass().getResourceAsStream("/images/eye_closed.png"));

    // Flag to track whether password is currently visible
    private boolean passwordVisible = false;
    
    // ========== Database Access ==========
    // Data access object for user registration
    private final UserDao userDao = new UserDao();

    // ========== Resources ==========
    @FXML
    private ResourceBundle resources;

    // ========== Personal Information Fields ==========
    // Text field for entering first name
    @FXML
    private TextField firstNameField;
    
    // Text field for entering last name
    @FXML
    private TextField lastNameField;
    
    // Text field for entering email address
    @FXML
    private TextField emailField;

    // ========== Password Fields ==========
    // Password field for masked password input
    @FXML
    private PasswordField passwordField;
    
    // Text field for visible password input (when toggle is on)
    @FXML
    private TextField passwordTextField;

    // ========== Role Selection ==========
    // Radio button for selecting teacher role
    @FXML
    private RadioButton teacherYes;
    
    // Radio button for selecting student role
    @FXML
    private RadioButton teacherNo;

    // ========== Terms and Conditions ==========
    // Checkbox for accepting terms and conditions
    @FXML
    private CheckBox termsCheck;
    
    // Icon for password visibility toggle button
    @FXML
    private ImageView eyeIcon;

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the password field visibility and eye icon.
     */
    @FXML
    private void initialize() {
        // Hide the password text field initially (password field is shown instead)
        PasswordVisibilitySupport.initializeHidden(passwordTextField, eyeIcon, eyeClosed);
        
        // Set the eye icon to show "closed" state (password is hidden)
        eyeIcon.setImage(eyeClosed);
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
     * Handles the register button click event.
     * Validates all registration form inputs and creates a new user account.
     * Performs checks for: terms acceptance, required fields, email format, password strength, and duplicate emails.
     * On success, logs in the user and navigates to home screen.
     * On failure, displays appropriate error messages.
     */
    @FXML
    public void register() {
        // ========== Get and Trim Input Values ==========
        // Get first name and trim whitespace
        String first = firstNameField.getText() == null ? "" : firstNameField.getText().trim();
        
        // Get last name and trim whitespace
        String last = lastNameField.getText() == null ? "" : lastNameField.getText().trim();
        
        // Get email and trim whitespace
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        
        // Get password from the appropriate field (visible or masked)
        String pw = passwordVisible ? passwordTextField.getText() : passwordField.getText();
        pw = (pw == null) ? "" : pw.trim();

        // ========== Get Selected Role ==========
        // Determine if user selected teacher role
        boolean isTeacher = teacherYes != null && teacherYes.isSelected();
        // Convert to role code (1 for teacher, 0 for student)
        int role = isTeacher ? 1 : 0;

        // ========== Validate Terms Acceptance ==========
        if (!termsCheck.isSelected()) {
            showWarning(resources.getString("register.warning.acceptTerms"));
            return;
        }

        // ========== Validate Required Fields ==========
        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || pw.isEmpty()) {
            showWarning(resources.getString("register.warning.fillAllFields"));
            return;
        }

        // ========== Validate Email Format ==========
        if (!email.contains("@")) {
            showWarning(resources.getString("register.warning.invalidEmail"));
            return;
        }

        // ========== Validate Password Strength ==========
        if (pw.length() < 6) {
            showWarning(resources.getString("register.warning.passwordTooShort"));
            return;
        }

        // ========== Check Email Uniqueness and Register ==========
        try {
            // Check if email is already registered
            if (emailExists(email)) {
                showWarning(resources.getString("register.warning.emailExists"));
                return;
            }

            // ========== Create New User ==========
            // Create a new user entity with the provided information
            User user = new User();
            user.setFirstName(first);
            user.setLastName(last);
            user.setEmail(email);
            user.setPassword(pw);
            user.setRole(role);
            // Set the user's preferred language from locale manager
            user.setLanguage(LocaleManager.getCurrentLanguageCode());

            // ========== Save to Database ==========
            // Persist the new user to the database
            persistUser(user);

            // ========== Authenticate and Navigate ==========
            // Store the newly created user in app state (auto-login)
            AppState.currentUser.set(user);
            
            // Set the user's role in app state
            AppState.setRole(user.isTeacher() ? AppState.Role.TEACHER : AppState.Role.STUDENT);

            // Navigate to the home screen
            navigateTo(AppState.Screen.HOME);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Registration failed due to persistence error", e);
            // Show database error message
            showWarning(resources.getString("register.warning.dbError"));
        }
    }

    /**
     * Displays a warning alert dialog to the user.
     * Used for displaying validation errors and other warning messages.
     *
     * @param msg The warning message to display
     */
    void showWarning(String msg) {
        // Create a warning alert
        Dialogs.show(Alert.AlertType.WARNING, resources.getString("register.alertTitle"), msg);
    }

    /**
     * Handles the login link click event.
     * Navigates back to the login screen for existing users.
     */
    @FXML
    public void goLogin() {
        navigateTo(AppState.Screen.LOGIN);
    }

    boolean emailExists(String email) {
        return userDao.existsByEmail(email);
    }

    void persistUser(User user) {
        userDao.persist(user);
    }

    void navigateTo(AppState.Screen screen) {
        Navigator.go(screen);
    }
}
