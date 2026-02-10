package controller;

import javafx.fxml.FXML;
import model.AppState;
import view.Navigator;

public class WelcomeController {
    @FXML
    private void goLogin() {
        Navigator.go(AppState.Screen.LOGIN);
    }

    @FXML
    private void goRegister() {
        Navigator.go(AppState.Screen.REGISTER);
    }
}
