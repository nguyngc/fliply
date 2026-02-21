package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.AppState;
import model.dao.UserDao;
import model.entity.User;
import view.Navigator;

public class LoginController {

    private final Image eyeOpen = new Image(getClass().getResourceAsStream("/images/eye_open.png"));
    private final Image eyeClosed = new Image(getClass().getResourceAsStream("/images/eye_closed.png"));
    public Label errorLabel;
    private boolean passwordVisible = false;
    private final UserDao userDao = new UserDao();

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
        //hide error
        emailField.textProperty().addListener((o, old, n) -> errorLabel.setVisible(false));
        passwordField.textProperty().addListener((o, old, n) -> errorLabel.setVisible(false));
        passwordTextField.textProperty().addListener((o, old, n) -> errorLabel.setVisible(false));
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

    @FXML public void login() {
        String email = emailField.getText();
        String password = passwordVisible ? passwordTextField.getText() : passwordField.getText();
        User user = userDao.findByEmailAndPassword(email, password);
        if (user != null) {
            errorLabel.setVisible(false);
            AppState.currentUser.set(user);
            AppState.setRole(user.isTeacher() ? AppState.Role.TEACHER : AppState.Role.STUDENT);
            Navigator.go(AppState.Screen.HOME);
        } else {
            errorLabel.setText("Invalid email or password");
            errorLabel.setVisible(true);
            System.out.println("Invalid email or password");
        }
//        // DEMO: no validation/auth yet
//        AppState.setRole(AppState.Role.STUDENT);
//        Navigator.go(AppState.Screen.HOME);
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
        AppState.setRole(AppState.Role.STUDENT);
        Navigator.go(AppState.Screen.HOME);
    }
}
