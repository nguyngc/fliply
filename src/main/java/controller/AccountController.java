package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import model.AppState;
import util.LocaleManager;
import view.Navigator;

import java.util.ResourceBundle;

public class AccountController {
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
        Navigator.go(AppState.Screen.LOGIN);
    }

    @FXML
    private void switchToEnglish() {
        LocaleManager.setLocale("en", "US");
        Navigator.reloadCurrent();
    }

    @FXML
    private void switchToArabic() {
        LocaleManager.setLocale("ar", "AR");
        Navigator.reloadCurrent();
    }

    @FXML
    private void switchToFinnish() {
        LocaleManager.setLocale("fi", "FI");
        Navigator.reloadCurrent();
    }

    @FXML
    private void switchToKorean() {
        LocaleManager.setLocale("ko", "KR");
        Navigator.reloadCurrent();
    }

    @FXML
    private void switchToLao() {
        LocaleManager.setLocale("lo", "LA");
        Navigator.reloadCurrent();
    }

    @FXML
    private void switchToVietnamese() {
        LocaleManager.setLocale("vi", "VN");
        Navigator.reloadCurrent();
    }
}
