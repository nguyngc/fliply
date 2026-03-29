package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.AppState;
import model.dao.UserDao;
import model.entity.User;
import view.Navigator;

import java.util.ResourceBundle;

public class RegisterController {

    private final Image eyeOpen = new Image(getClass().getResourceAsStream("/images/eye_open.png"));
    private final Image eyeClosed = new Image(getClass().getResourceAsStream("/images/eye_closed.png"));

    private boolean passwordVisible = false;
    private final UserDao userDao = new UserDao();

    @FXML
    private ResourceBundle resources;

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
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

    @FXML
    private void initialize() {
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
        String first = firstNameField.getText() == null ? "" : firstNameField.getText().trim();
        String last = lastNameField.getText() == null ? "" : lastNameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pw = passwordVisible ? passwordTextField.getText() : passwordField.getText();
        pw = (pw == null) ? "" : pw.trim();

        boolean isTeacher = teacherYes != null && teacherYes.isSelected();
        int role = isTeacher ? 1 : 0;

        if (!termsCheck.isSelected()) {
            showWarning(resources.getString("register.warning.acceptTerms"));
            return;
        }

        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || pw.isEmpty()) {
            showWarning(resources.getString("register.warning.fillAllFields"));
            return;
        }

        if (!email.contains("@")) {
            showWarning(resources.getString("register.warning.invalidEmail"));
            return;
        }

        if (pw.length() < 6) {
            showWarning(resources.getString("register.warning.passwordTooShort"));
            return;
        }

        try {
            if (userDao.existsByEmail(email)) {
                showWarning(resources.getString("register.warning.emailExists"));
                return;
            }

            User user = new User();
            user.setFirstName(first);
            user.setLastName(last);
            user.setEmail(email);
            user.setPassword(pw);
            user.setRole(role);

            userDao.persist(user);

            AppState.currentUser.set(user);
            AppState.setRole(user.isTeacher() ? AppState.Role.TEACHER : AppState.Role.STUDENT);

            Navigator.go(AppState.Screen.HOME);

        } catch (Exception e) {
            e.printStackTrace();
            showWarning(resources.getString("register.warning.dbError"));
        }
    }

    private void showWarning(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(resources.getString("register.alertTitle"));
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    public void goLogin() {
        Navigator.go(AppState.Screen.LOGIN);
    }
}