package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import model.AppState;
import util.LocaleManager;
import view.Navigator;

public class WelcomeController {
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
                            "-fx-font-family: 'Noto Sans Lao';"
            );

            titleLabel.setStyle("-fx-font-family: 'Noto Sans Lao';");
            subtitleLabel.setStyle("-fx-font-family: 'Noto Sans Lao';");
            registerButton.setStyle("-fx-font-family: 'Noto Sans Lao';");
            loginButton.setStyle("-fx-font-family: 'Noto Sans Lao';");
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