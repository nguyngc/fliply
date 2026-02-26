package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.AppState;
import model.dao.UserDao;
import model.entity.User;
import view.Navigator;

public class RegisterController {

    private final Image eyeOpen = new Image(getClass().getResourceAsStream("/images/eye_open.png"));
    private final Image eyeClosed = new Image(getClass().getResourceAsStream("/images/eye_closed.png"));

    private boolean passwordVisible = false;
    private final UserDao userDao = new UserDao();

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

        // 1) read inputs
        String first = firstNameField.getText() == null ? "" : firstNameField.getText().trim();
        String last = lastNameField.getText() == null ? "" : lastNameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pw = passwordVisible ? passwordTextField.getText() : passwordField.getText();
        pw = (pw == null) ? "" : pw;

        boolean isTeacher = teacherYes != null && teacherYes.isSelected();
        int role = isTeacher ? 1 : 0; // 1 teacher, 0 student

        // 2) basic validation
        if (!termsCheck.isSelected()) {
            showWarning("Please accept terms & conditions.");
            return;
        }
        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || pw.isEmpty()) {
            showWarning("Please fill in all fields.");
            return;
        }
        if (!email.contains("@")) {
            showWarning("Email is not valid.");
            return;
        }
        if (pw.length() < 6) {
            showWarning("Password must be at least 6 characters.");
            return;
        }

        // 3) DB check + insert
        try {
            if (userDao.existsByEmail(email)) {
                showWarning("Email already exists.");
                return;
            }

            User user = new User();
            user.setFirstName(first);
            user.setLastName(last);
            user.setEmail(email);
            user.setPassword(pw);
            user.setRole(role);

            userDao.persist(user); // DB will generate userId

            // 4) save to session and go home
            AppState.currentUser.set(user);
            AppState.setRole(user.isTeacher() ? AppState.Role.TEACHER : AppState.Role.STUDENT);

            Navigator.go(AppState.Screen.HOME);

        } catch (Exception e) {
            e.printStackTrace();
            showWarning("Register failed (DB error).");
        }
    }

    private void showWarning(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Register");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    public void goLogin() {
        Navigator.go(AppState.Screen.LOGIN);
    }
}