package controller;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import model.AppState;
import model.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.LocaleManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableLoginController controller;
    private Locale previousLocale;
    private User previousUser;
    private AppState.Role previousRole;

    private static class TestableLoginController extends LoginController {
        private User authenticatedUser;
        private String lastEmail;
        private String lastPassword;
        private AppState.Screen lastNavigatedScreen;
        private String lastDialogTitle;
        private String lastDialogMessage;

        @Override
        User authenticate(String email, String password) {
            lastEmail = email;
            lastPassword = password;
            return authenticatedUser;
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }

        @Override
        void showInfoDialog(String title, String message) {
            lastDialogTitle = title;
            lastDialogMessage = message;
        }
    }

    @BeforeEach
    void setUp() {
        previousLocale = LocaleManager.getLocale();
        previousUser = AppState.currentUser.get();
        previousRole = AppState.role.get();

        LocaleManager.setLocale("en", "US");
        controller = new TestableLoginController();

        setPrivate("emailField", new TextField());
        setPrivate("passwordField", new PasswordField());
        setPrivate("passwordTextField", new TextField());
        setPrivate("eyeIcon", new ImageView());
        setPrivate("errorLabel", new Label());

        AppState.currentUser.set(null);
        AppState.role.set(null);

        callPrivate("initialize");
    }

    @AfterEach
    void tearDown() {
        LocaleManager.setLocale(previousLocale);
        AppState.currentUser.set(previousUser);
        AppState.role.set(previousRole);
    }

    @Test
    void initialize_hidesPasswordTextField() {
        TextField txt = (TextField) getPrivate("passwordTextField");
        assertFalse(txt.isVisible());
        assertFalse(txt.isManaged());
    }

    @Test
    void initialize_hidesErrorLabelWhenTyping() {
        Label errorLabel = (Label) getPrivate("errorLabel");
        TextField emailField = (TextField) getPrivate("emailField");
        PasswordField passwordField = (PasswordField) getPrivate("passwordField");
        TextField passwordTextField = (TextField) getPrivate("passwordTextField");

        errorLabel.setVisible(true);
        emailField.setText("user@example.com");
        assertFalse(errorLabel.isVisible());

        errorLabel.setVisible(true);
        passwordField.setText("secret1");
        assertFalse(errorLabel.isVisible());

        errorLabel.setVisible(true);
        passwordTextField.setText("secret2");
        assertFalse(errorLabel.isVisible());
    }

    @Test
    void togglePassword_showsTextField() {
        PasswordField passwordField = (PasswordField) getPrivate("passwordField");
        TextField passwordTextField = (TextField) getPrivate("passwordTextField");

        passwordField.setText("secret");
        callPrivate("togglePassword");

        assertTrue(passwordTextField.isVisible());
        assertTrue(passwordTextField.isManaged());
        assertFalse(passwordField.isVisible());
        assertFalse(passwordField.isManaged());
        assertEquals("secret", passwordTextField.getText());
    }

    @Test
    void togglePassword_hidesTextField() {
        callPrivate("togglePassword");
        callPrivate("togglePassword");

        PasswordField passwordField = (PasswordField) getPrivate("passwordField");
        TextField passwordTextField = (TextField) getPrivate("passwordTextField");

        assertTrue(passwordField.isVisible());
        assertTrue(passwordField.isManaged());
        assertFalse(passwordTextField.isVisible());
        assertFalse(passwordTextField.isManaged());
    }

    @Test
    void login_fail_showsError() {
        TextField emailField = (TextField) getPrivate("emailField");
        PasswordField passwordField = (PasswordField) getPrivate("passwordField");

        emailField.setText("wrong@example.com");
        passwordField.setText("wrong");

        controller.login();

        Label errorLabel = (Label) getPrivate("errorLabel");
        assertTrue(errorLabel.isVisible());
        assertEquals("Invalid email or password", errorLabel.getText());
        assertEquals("wrong@example.com", controller.lastEmail);
        assertEquals("wrong", controller.lastPassword);
        assertNull(AppState.currentUser.get());
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void login_success_setsLocaleStateAndNavigatesHome() {
        TextField emailField = (TextField) getPrivate("emailField");
        PasswordField passwordField = (PasswordField) getPrivate("passwordField");
        Label errorLabel = (Label) getPrivate("errorLabel");

        User user = new User();
        user.setEmail("teacher@example.com");
        user.setPassword("password123");
        user.setRole(1);
        user.setLanguage("fi");
        controller.authenticatedUser = user;

        emailField.setText("teacher@example.com");
        passwordField.setText("password123");
        errorLabel.setVisible(true);

        controller.login();

        assertFalse(errorLabel.isVisible());
        assertEquals(Locale.of("fi", "FI"), LocaleManager.getLocale());
        assertEquals(user, AppState.currentUser.get());
        assertEquals(AppState.Role.TEACHER, AppState.role.get());
        assertEquals(AppState.Screen.HOME, controller.lastNavigatedScreen);
        assertEquals("teacher@example.com", controller.lastEmail);
        assertEquals("password123", controller.lastPassword);
    }

    @Test
    void login_success_usesVisiblePasswordWhenShown() {
        TextField emailField = (TextField) getPrivate("emailField");
        TextField passwordTextField = (TextField) getPrivate("passwordTextField");

        User user = new User();
        user.setRole(0);
        user.setLanguage("ko");
        controller.authenticatedUser = user;

        callPrivate("togglePassword");
        emailField.setText("student@example.com");
        passwordTextField.setText("visible-secret");

        controller.login();

        assertEquals("student@example.com", controller.lastEmail);
        assertEquals("visible-secret", controller.lastPassword);
        assertEquals(AppState.Role.STUDENT, AppState.role.get());
        assertEquals(Locale.of("ko", "KR"), LocaleManager.getLocale());
        assertEquals(AppState.Screen.HOME, controller.lastNavigatedScreen);
    }

    @Test
    void goRegister_navigatesToRegister() {
        controller.goRegister();
        assertEquals(AppState.Screen.REGISTER, controller.lastNavigatedScreen);
    }

    @Test
    void onForgotPassword_showsInformationDialog() {
        controller.onForgotPassword();

        assertEquals("Forget password?", controller.lastDialogTitle);
        assertEquals("Forget password?", controller.lastDialogMessage);
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = LoginController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = LoginController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method m = LoginController.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
