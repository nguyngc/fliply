package controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.AppState;
import view.Navigator;

public class LoginController {

    private final Image eyeOpen = new Image(getClass().getResourceAsStream("/images/eye_open.png"));
    private final Image eyeClosed = new Image(getClass().getResourceAsStream("/images/eye_closed.png"));
    private boolean passwordVisible = false;

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;   // NEW
    @FXML
    private ImageView eyeIcon;

    @FXML
    private void initialize() {
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);
    }

    @FXML
    private void togglePassword() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);

            eyeIcon.setImage(eyeOpen);
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);

            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);

            eyeIcon.setImage(eyeClosed);
        }
    }

    @FXML
    public void login() {
        // DEMO: no validation/auth yet
        AppState.role.set(AppState.Role.STUDENT);
        Navigator.go(AppState.Screen.HOME);
    }

    @FXML
    public void goRegister() {
        Navigator.go(AppState.Screen.REGISTER);
    }

    @FXML
    public void onForgotPassword() {
        // DEMO only
        System.out.println("Forgot password clicked (not implemented)");
    }

    @FXML
    public void onGoogleLogin() {
        // DEMO only
        System.out.println("Google login clicked (not implemented)");
        AppState.role.set(AppState.Role.STUDENT);
        Navigator.go(AppState.Screen.HOME);
    }
}
