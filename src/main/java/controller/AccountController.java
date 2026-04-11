package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import model.AppState;
import model.entity.User;
import model.service.UserService;
import util.LocaleManager;
import view.Navigator;

import java.util.ResourceBundle;

/**
 * Controller for the main account screen.
 * Manages user account settings, language preferences, and navigation to account-related screens.
 */
public class AccountController {
    // Service for updating user information in the database
    private final UserService userService = new UserService();

    // Menu button for language selection
    @FXML
    private MenuButton languageMenuButton;

    // UI Components injected from FXML
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the header, configures styling based on user role, and sets the active navigation item.
     */
    @FXML
    private void initialize() {
        // Get localized string resources
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());
        
        // Configure header without back button since this is a main screen
        headerController.setBackVisible(false);
        headerController.setTitle(rb.getString("account.header.title"));
        headerController.setSubtitle("");
        
        // Apply styling variant based on whether the user is a teacher or student
        headerController.applyVariant(AppState.isTeacher()
                ? HeaderController.Variant.TEACHER
                : HeaderController.Variant.STUDENT);

        // Set the active navigation item to ACCOUNT
        AppState.navOverride.set(AppState.NavItem.ACCOUNT);
    }

    /**
     * Handles the edit account button click event.
     * Navigates to the account edit screen where users can update their profile information.
     */
    @FXML
    private void onEditAccount() {
        Navigator.go(AppState.Screen.ACCOUNT_EDIT);
    }

    /**
     * Handles the change password button click event.
     * Navigates to the password change screen.
     */
    @FXML
    private void onChangePassword() {
        Navigator.go(AppState.Screen.ACCOUNT_PASSWORD);
    }

    /**
     * Handles the help button click event.
     * Navigates to the help/support screen.
     */
    @FXML
    private void onHelp() {
        Navigator.go(AppState.Screen.ACCOUNT_HELP);
    }

    /**
     * Handles the about button click event.
     * Navigates to the about screen with application information.
     */
    @FXML
    private void onAbout() {
        Navigator.go(AppState.Screen.ACCOUNT_ABOUT);
    }

    /**
     * Handles the logout button click event.
     * Resets language to English, clears the current user from app state,
     * and navigates back to the login screen.
     */
    @FXML
    private void onLogout() {
        // Reset language preference to English
        LocaleManager.setLocaleByLanguage("en");
        
        // Clear the current user from app state
        AppState.currentUser.set(null);
        
        // Navigate to login screen
        Navigator.go(AppState.Screen.LOGIN);
    }

    /**
     * Switches the application language to English.
     */
    @FXML
    private void switchToEnglish() {
        saveLanguagePreference("en");
    }

    /**
     * Switches the application language to Arabic.
     */
    @FXML
    private void switchToArabic() {
        saveLanguagePreference("ar");
    }

    /**
     * Switches the application language to Finnish.
     */
    @FXML
    private void switchToFinnish() {
        saveLanguagePreference("fi");
    }

    /**
     * Switches the application language to Korean.
     */
    @FXML
    private void switchToKorean() {
        saveLanguagePreference("ko");
    }

    /**
     * Switches the application language to Lao.
     */
    @FXML
    private void switchToLao() {
        saveLanguagePreference("lo");
    }

    /**
     * Switches the application language to Vietnamese.
     */
    @FXML
    private void switchToVietnamese() {
        saveLanguagePreference("vi");
    }

    /**
     * Saves the language preference for the current user.
     * Updates both the LocaleManager with the new language and saves the preference to the database.
     * Then reloads the current screen to apply the language changes.
     *
     * @param language The language code (e.g., "en", "ar", "fi", "ko", "lo", "vi")
     */
    private void saveLanguagePreference(String language) {
        // Set the locale manager to use the new language
        LocaleManager.setLocaleByLanguage(language);

        // Get the current logged-in user
        User currentUser = AppState.currentUser.get();
        if (currentUser != null) {
            // Update the user's language preference
            currentUser.setLanguage(language);
            
            // Save the updated preference to the database
            userService.update(currentUser);
        }

        // Reload the current screen to display content in the new language
        Navigator.reloadCurrent();
    }
}
