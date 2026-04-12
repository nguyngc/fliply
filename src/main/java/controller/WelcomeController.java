package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import model.AppState;
import util.LocaleManager;
import view.Navigator;

public class WelcomeController {
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

    @FXML
    public void initialize() {
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