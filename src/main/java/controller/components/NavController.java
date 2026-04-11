package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.AppState;
import view.Navigator;

/**
 * Controller for the navigation bar component.
 * Manages navigation between different screens and displays role-based menu items.
 * Provides visual feedback through active/inactive icon states and text styling.
 */
public class NavController {

    // Toggle group to ensure only one nav button is selected at a time
    private final ToggleGroup group = new ToggleGroup();
    
    // ========== Preloaded Images ==========
    // Active/inactive icon states for home navigation
    private final Image homeActive = load("images/home_btn.png");
    private final Image homeInactive = load("images/home_btn_inactive.png");
    
    // Active/inactive icon states for classes navigation
    private final Image classActive = load("images/class_btn.png");
    private final Image classInactive = load("images/class_btn_inactive.png");
    
    // Active/inactive icon states for flashcards navigation
    private final Image flashcardActive = load("images/flashcard_btn.png");
    private final Image flashcardInactive = load("images/flashcard_btn_inactive.png");
    
    // Active/inactive icon states for quizzes navigation
    private final Image quizActive = load("images/quiz_btn.png");
    private final Image quizInactive = load("images/quiz_btn_inactive.png");
    
    // Active/inactive icon states for account navigation
    private final Image accountActive = load("images/account_btn.png");
    private final Image accountInactive = load("images/account_btn_inactive.png");

    // ========== UI Components - Icons ==========
    @FXML
    private ImageView homeIcon;
    @FXML
    private ImageView classIcon;
    @FXML
    private ImageView flashcardIcon;
    @FXML
    private ImageView quizIcon;
    @FXML
    private ImageView accountIcon;
    
    // ========== UI Components - Buttons ==========
    @FXML
    private ToggleButton homeBtn;
    @FXML
    private ToggleButton classBtn;
    @FXML
    private ToggleButton flashBtn;
    @FXML
    private ToggleButton quizBtn;
    @FXML
    private ToggleButton accountBtn;
    
    // ========== UI Components - Labels ==========
    @FXML
    private Label homeLabel;
    @FXML
    private Label classLabel;
    @FXML
    private Label flashLabel;
    @FXML
    private Label quizLabel;
    @FXML
    private Label accountLabel;
    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the toggle group, initial icon states, listeners for state changes, and role-based visibility.
     */
    @FXML
    private void initialize() {
        // Get current user role
        boolean isTeacher = AppState.isTeacher();

        // ========== Configure Toggle Group ==========
        // Add all toggle buttons to the same group to ensure only one is selected at a time
        homeBtn.setToggleGroup(group);
        classBtn.setToggleGroup(group);
        flashBtn.setToggleGroup(group);
        quizBtn.setToggleGroup(group);
        accountBtn.setToggleGroup(group);

        // ========== Set Initial Icon States ==========
        // All buttons start with inactive icons
        homeIcon.setImage(homeInactive);
        classIcon.setImage(classInactive);
        flashcardIcon.setImage(flashcardInactive);
        quizIcon.setImage(quizInactive);
        accountIcon.setImage(accountInactive);

        // ========== Listen to Button Selection Changes ==========
        // Update icon when home button selection changes
        homeBtn.selectedProperty().addListener((obs, oldV, selected) -> homeIcon.setImage(selected ? homeActive : homeInactive));

        // Update icon when classes button selection changes
        classBtn.selectedProperty().addListener((obs, oldV, selected) -> classIcon.setImage(selected ? classActive : classInactive));

        // Update icon when flashcards button selection changes
        flashBtn.selectedProperty().addListener((obs, oldV, selected) -> flashcardIcon.setImage(selected ? flashcardActive : flashcardInactive));

        // Update icon when quizzes button selection changes
        quizBtn.selectedProperty().addListener((obs, oldV, selected) -> quizIcon.setImage(selected ? quizActive : quizInactive));

        // Update icon when account button selection changes
        accountBtn.selectedProperty().addListener((obs, oldV, selected) -> accountIcon.setImage(selected ? accountActive : accountInactive));

        // ========== Sync with Global Navigation State ==========
        // Listen to changes in the active navigation item from app state
        AppState.activeNav.addListener((obs, oldV, newV) -> updateFromAppState(newV));
        // Initialize the UI based on current app state navigation
        updateFromAppState(AppState.activeNav.get());

        // ========== Apply Role-Based Visibility ==========
        // Listen to changes in user role
        AppState.role.addListener((obs, o, n) -> applyRole(n));
        // Initialize role-based visibility
        applyRole(AppState.getRole());
    }

    /**
     * Updates the navigation UI based on the current app state navigation item.
     * Synchronizes button selection and label styling with the active navigation state.
     *
     * @param nav The current active navigation item from app state
     */
    private void updateFromAppState(AppState.NavItem nav) {
        // Update toggle button selection based on current navigation item
        homeBtn.setSelected(nav == AppState.NavItem.HOME);
        classBtn.setSelected(nav == AppState.NavItem.CLASSES);
        flashBtn.setSelected(nav == AppState.NavItem.FLASHCARDS);
        quizBtn.setSelected(nav == AppState.NavItem.QUIZZES);
        accountBtn.setSelected(nav == AppState.NavItem.ACCOUNT);

        // Update label styling to reflect active/inactive state
        homeLabel.setStyle(labelStyle(nav == AppState.NavItem.HOME));
        classLabel.setStyle(labelStyle(nav == AppState.NavItem.CLASSES));
        flashLabel.setStyle(labelStyle(nav == AppState.NavItem.FLASHCARDS));
        quizLabel.setStyle(labelStyle(nav == AppState.NavItem.QUIZZES));
        accountLabel.setStyle(labelStyle(nav == AppState.NavItem.ACCOUNT));
    }

    /**
     * Generates the CSS style for a navigation label based on whether it's active or inactive.
     * Active labels are blue with bold font, inactive labels are gray with normal weight.
     *
     * @param active True if the label should display as active (selected), false for inactive
     * @return CSS style string for the label
     */
    private String labelStyle(boolean active) {
        return active
                ? "-fx-text-fill: #3D8FEF; -fx-font-size: 11px; -fx-font-weight: 600;"
                : "-fx-text-fill: #8C8C8C; -fx-font-size: 11px; -fx-font-weight: 500;";
    }

    /**
     * Loads an image from the classpath resources.
     *
     * @param path The resource path relative to the classpath (e.g., "images/home_btn.png")
     * @return The loaded Image object
     */
    private Image load(String path) {
        return new Image(getClass().getClassLoader().getResourceAsStream(path));
    }

    /**
     * Applies role-based visibility to navigation items.
     * Teachers see: Home, Classes, Account
     * Students see: Home, Classes, Flashcards, Quizzes, Account
     *
     * @param role The current user role
     */
    private void applyRole(AppState.Role role) {
        // Hide flashcards and quizzes for teachers
        boolean teacher = AppState.isTeacher();
        flashBtn.setVisible(!teacher);
        flashBtn.setManaged(!teacher);
        quizBtn.setVisible(!teacher);
        quizBtn.setManaged(!teacher);
    }

    // ========== Navigation Action Methods ==========

    /**
     * Handles the home button click event.
     * Navigates to the home screen.
     */
    @FXML
    private void goHome() {
        Navigator.go(AppState.Screen.HOME);
    }

    /**
     * Handles the classes button click event.
     * Navigates to the classes screen.
     */
    @FXML
    private void goClass() {
        Navigator.go(AppState.Screen.CLASSES);
    }

    /**
     * Handles the flashcards button click event.
     * Navigates to the flashcards screen (student only).
     */
    @FXML
    private void goFlash() {
        Navigator.go(AppState.Screen.FLASHCARDS);
    }

    /**
     * Handles the quizzes button click event.
     * Navigates to the quizzes screen (student only).
     */
    @FXML
    private void goQuiz() {
        Navigator.go(AppState.Screen.QUIZZES);
    }

    /**
     * Handles the account button click event.
     * Navigates to the account screen.
     */
    @FXML
    private void goAccount() {
        Navigator.go(AppState.Screen.ACCOUNT);
    }
}
