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

public class AccountController {
    private final UserService userService = new UserService();

    @FXML
    private MenuButton languageMenuButton;

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private void initialize() {
        ResourceBundle rb =  ResourceBundle.getBundle("Messages", LocaleManager.getLocale());
        headerController.setBackVisible(false);
        headerController.setTitle(rb.getString("account.header.title"));
        headerController.setSubtitle("");
        headerController.applyVariant(AppState.isTeacher()
                ? HeaderController.Variant.TEACHER
                : HeaderController.Variant.STUDENT);

        AppState.navOverride.set(AppState.NavItem.ACCOUNT);
    }

    @FXML
    private void onEditAccount() {
        Navigator.go(AppState.Screen.ACCOUNT_EDIT);
    }

    @FXML
    private void onChangePassword() {
        Navigator.go(AppState.Screen.ACCOUNT_PASSWORD);
    }

    @FXML
    private void onHelp() {
        Navigator.go(AppState.Screen.ACCOUNT_HELP);
    }

    @FXML
    private void onAbout() {
        Navigator.go(AppState.Screen.ACCOUNT_ABOUT);
    }

    @FXML
    private void onLogout() {
        LocaleManager.setLocaleByLanguage("en");
        AppState.currentUser.set(null);
        Navigator.go(AppState.Screen.LOGIN);
    }

    @FXML
    private void switchToEnglish() {
        saveLanguagePreference("en");
    }

    @FXML
    private void switchToArabic() {
        saveLanguagePreference("ar");
    }

    @FXML
    private void switchToFinnish() {
        saveLanguagePreference("fi");
    }

    @FXML
    private void switchToKorean() {
        saveLanguagePreference("ko");
    }

    @FXML
    private void switchToLao() {
        saveLanguagePreference("lo");
    }

    @FXML
    private void switchToVietnamese() {
        saveLanguagePreference("vi");
    }

    private void saveLanguagePreference(String language) {
        LocaleManager.setLocaleByLanguage(language);

        User currentUser = AppState.currentUser.get();
        if (currentUser != null) {
            currentUser.setLanguage(language);
            userService.update(currentUser);
        }

        Navigator.reloadCurrent();
    }
}
