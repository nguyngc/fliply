package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.AppState;
import view.Navigator;

public class RegisterController {

    private final ToggleGroup teacherGroup = new ToggleGroup();
    private final Image eyeOpen = new Image(getClass().getResourceAsStream("/images/eye_open.png"));
    private final Image eyeClosed = new Image(getClass().getResourceAsStream("/images/eye_closed.png"));
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private RadioButton teacherYes;
    @FXML
    private RadioButton teacherNo;
    @FXML
    private CheckBox termsCheck;
    @FXML
    private ImageView eyeIcon;
    private boolean passwordVisible = false;

    @FXML
    private void initialize() {
        // keep your existing ToggleGroup setup here too
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);
        eyeIcon.setImage(eyeClosed);
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
    public void register() {
        // DEMO: no validation/auth yet
        // TODO: Block register if terms not checked
        if (termsCheck != null && !termsCheck.isSelected()) {
            System.out.println("Please accept terms (demo)");
            return;
        }

        boolean isTeacher = teacherYes != null && teacherYes.isSelected();
        AppState.role.set(isTeacher ? AppState.Role.TEACHER : AppState.Role.STUDENT);

        Navigator.go(AppState.Screen.HOME);
    }

    @FXML
    public void goLogin() {
        Navigator.go(AppState.Screen.LOGIN);
    }
}
