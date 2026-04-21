package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import model.AppState;
import util.LocaleManager;
import view.Navigator;

/**
 * Controller for the welcome screen of the application.
 * Handles language selection and navigation to login/register screens.
 */
public class WelcomeController {
    // Lao text needs an explicit font override to render consistently on this screen.
    private static final String LAO_FONT_STYLE = "-fx-font-family: 'Noto Sans Lao';";

    @FXML
    private MenuButton languageMenuButton;

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Button loginButton;

    /**
     * Initializes the welcome screen. Applies Lao-specific font styling if the current locale is Lao.
     * This ensures that Lao text renders correctly on this screen, while allowing other screens to use their default fonts.
     */
    @FXML
    public void initialize() {
        // Apply Lao-specific font styling only when Lao locale is active.
        if (LocaleManager.getLocale().getLanguage().equals("lo")) {
            languageMenuButton.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 12; " +
                            "-fx-cursor: hand; " +
                            LAO_FONT_STYLE
            );

            titleLabel.setStyle(LAO_FONT_STYLE);
            subtitleLabel.setStyle(LAO_FONT_STYLE);
            registerButton.setStyle(LAO_FONT_STYLE);
            loginButton.setStyle(LAO_FONT_STYLE);
        }
    }

    /** Navigates to the login screen when the login button is clicked. */
    @FXML
    private void goLogin() {
        navigateTo(AppState.Screen.LOGIN);
    }

    /** Navigates to the registration screen when the register button is clicked. */
    @FXML
    private void goRegister() {
        navigateTo(AppState.Screen.REGISTER);
    }

    /** Switches the application locale to English and reloads the current view to apply changes. */
    @FXML
    private void switchToEnglish() {
        updateLocale("en", "US");
        // Reload to re-resolve localized strings for the current view.
        reloadCurrentView();
    }

    /** Switches the application locale to Arabic and reloads the current view to apply changes. */
    @FXML
    private void switchToArabic() {
        updateLocale("ar", "AR");
        reloadCurrentView();
    }

    /** Switches the application locale to Finnish and reloads the current view to apply changes. */
    @FXML
    private void switchToFinnish() {
        updateLocale("fi", "FI");
        reloadCurrentView();
    }

    /** Switches the application locale to Korean and reloads the current view to apply changes. */
    @FXML
    private void switchToKorean() {
        updateLocale("ko", "KR");
        reloadCurrentView();
    }

    /** Switches the application locale to Lao and reloads the current view to apply changes. */
    @FXML
    private void switchToLao() {
        updateLocale("lo", "LA");
        reloadCurrentView();
    }

    /** Switches the application locale to Vietnamese and reloads the current view to apply changes. */
    @FXML
    private void switchToVietnamese() {
        updateLocale("vi", "VN");
        reloadCurrentView();
    }

    void updateLocale(String language, String country) {
        LocaleManager.setLocale(language, country);
    }

    void navigateTo(AppState.Screen screen) {
        Navigator.go(screen);
    }

    void reloadCurrentView() {
        Navigator.reloadCurrent();
    }
}
