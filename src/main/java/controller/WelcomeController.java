package controller;

import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import model.AppState;
import util.LocaleManager;
import view.Navigator;

public class WelcomeController {
    @FXML
    private MenuButton languageMenuButton;

    @FXML
    private void goLogin() {
        Navigator.go(AppState.Screen.LOGIN);
    }

    @FXML
    private void goRegister() {
        Navigator.go(AppState.Screen.REGISTER);
    }

    @FXML
    private void switchToEnglish() {
        LocaleManager.setLocale("en", "US");
        Navigator.reloadCurrent();
    }

    @FXML
    private void switchToFinnish() {
        LocaleManager.setLocale("fi", "FI");
        Navigator.reloadCurrent();
    }

    @FXML
    private void switchToKorea() {
        LocaleManager.setLocale("ko", "KR");
        Navigator.reloadCurrent();
    }

    @FXML
    private void switchToLaos() {
        LocaleManager.setLocale("lo", "LA");
        Navigator.reloadCurrent();
    }

    @FXML
    private void switchToVietnamese() {
        LocaleManager.setLocale("vi", "VN");
        Navigator.reloadCurrent();
    }
}
