package controller;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
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
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableRegisterController controller;
    private Locale previousLocale;
    private User previousUser;
    private AppState.Role previousRole;

    private static class TestableRegisterController extends RegisterController {
        private boolean emailAlreadyExists;
        private RuntimeException persistException;
        private User persistedUser;
        private String checkedEmail;
        private String lastWarningMessage;
        private AppState.Screen lastNavigatedScreen;

        @Override
        boolean emailExists(String email) {
            checkedEmail = email;
            return emailAlreadyExists;
        }

        @Override
        void persistUser(User user) {
            if (persistException != null) {
                throw persistException;
            }
            persistedUser = user;
        }

        @Override
        void showWarning(String msg) {
            lastWarningMessage = msg;
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    @BeforeEach
    void setUp() {
        previousLocale = LocaleManager.getLocale();
        previousUser = AppState.currentUser.get();
        previousRole = AppState.role.get();

        LocaleManager.setLocale("en", "US");
        controller = new TestableRegisterController();

        setPrivate("resources", ResourceBundle.getBundle("Messages", LocaleManager.getLocale()));
        setPrivate("firstNameField", new TextField());
        setPrivate("lastNameField", new TextField());
        setPrivate("emailField", new TextField());
        setPrivate("passwordField", new PasswordField());
        setPrivate("passwordTextField", new TextField());
        setPrivate("teacherYes", new RadioButton());
        setPrivate("teacherNo", new RadioButton());
        setPrivate("termsCheck", new CheckBox());
        setPrivate("eyeIcon", new ImageView());

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
    void initialize_hidesVisiblePasswordField() {
        TextField passwordTextField = (TextField) getPrivate("passwordTextField");
        ImageView eyeIcon = (ImageView) getPrivate("eyeIcon");

        assertFalse(passwordTextField.isVisible());
        assertFalse(passwordTextField.isManaged());
        assertNotNull(eyeIcon.getImage());
    }

    @Test
    void togglePassword_showsAndHidesVisibleField() {
        PasswordField passwordField = (PasswordField) getPrivate("passwordField");
        TextField passwordTextField = (TextField) getPrivate("passwordTextField");

        passwordField.setText("secret123");
        callPrivate("togglePassword");

        assertTrue(passwordTextField.isVisible());
        assertTrue(passwordTextField.isManaged());
        assertFalse(passwordField.isVisible());
        assertFalse(passwordField.isManaged());
        assertEquals("secret123", passwordTextField.getText());

        passwordTextField.setText("updated-secret");
        callPrivate("togglePassword");

        assertTrue(passwordField.isVisible());
        assertTrue(passwordField.isManaged());
        assertFalse(passwordTextField.isVisible());
        assertFalse(passwordTextField.isManaged());
        assertEquals("updated-secret", passwordField.getText());
    }

    @Test
    void register_requiresAcceptedTerms() {
        fillValidForm();

        controller.register();

        assertEquals("Please accept the terms and conditions.", controller.lastWarningMessage);
        assertNull(controller.persistedUser);
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void register_requiresAllFields() {
        fillValidForm();
        ((CheckBox) getPrivate("termsCheck")).setSelected(true);
        ((TextField) getPrivate("firstNameField")).setText(" ");

        controller.register();

        assertEquals("Please fill in all fields.", controller.lastWarningMessage);
        assertNull(controller.persistedUser);
    }

    @Test
    void register_rejectsInvalidEmail() {
        fillValidForm();
        ((CheckBox) getPrivate("termsCheck")).setSelected(true);
        ((TextField) getPrivate("emailField")).setText("invalid-email");

        controller.register();

        assertEquals("Email is not valid.", controller.lastWarningMessage);
        assertNull(controller.persistedUser);
    }

    @Test
    void register_rejectsShortPassword() {
        fillValidForm();
        ((CheckBox) getPrivate("termsCheck")).setSelected(true);
        ((PasswordField) getPrivate("passwordField")).setText("12345");

        controller.register();

        assertEquals("Password must be at least 6 characters.", controller.lastWarningMessage);
        assertNull(controller.persistedUser);
    }

    @Test
    void register_rejectsExistingEmail() {
        fillValidForm();
        ((CheckBox) getPrivate("termsCheck")).setSelected(true);
        controller.emailAlreadyExists = true;

        controller.register();

        assertEquals("jane@example.com", controller.checkedEmail);
        assertEquals("Email already exists.", controller.lastWarningMessage);
        assertNull(controller.persistedUser);
    }

    @Test
    void register_persistsStudentAndNavigatesHome() {
        fillValidForm();
        ((CheckBox) getPrivate("termsCheck")).setSelected(true);

        controller.register();

        assertNotNull(controller.persistedUser);
        assertEquals("Jane", controller.persistedUser.getFirstName());
        assertEquals("Doe", controller.persistedUser.getLastName());
        assertEquals("jane@example.com", controller.persistedUser.getEmail());
        assertEquals("secret123", controller.persistedUser.getPassword());
        assertEquals(0, controller.persistedUser.getRole());
        assertEquals("en", controller.persistedUser.getLanguage());
        assertEquals(controller.persistedUser, AppState.currentUser.get());
        assertEquals(AppState.Role.STUDENT, AppState.role.get());
        assertEquals(AppState.Screen.HOME, controller.lastNavigatedScreen);
        assertNull(controller.lastWarningMessage);
    }

    @Test
    void register_persistsTeacherUsingVisiblePassword() {
        fillValidForm();
        ((CheckBox) getPrivate("termsCheck")).setSelected(true);
        ((RadioButton) getPrivate("teacherYes")).setSelected(true);

        callPrivate("togglePassword");
        ((TextField) getPrivate("passwordTextField")).setText("visible-secret");

        controller.register();

        assertNotNull(controller.persistedUser);
        assertEquals(1, controller.persistedUser.getRole());
        assertEquals("visible-secret", controller.persistedUser.getPassword());
        assertEquals(AppState.Role.TEACHER, AppState.role.get());
        assertEquals(AppState.Screen.HOME, controller.lastNavigatedScreen);
    }

    @Test
    void register_handlesPersistErrorWithWarning() {
        fillValidForm();
        ((CheckBox) getPrivate("termsCheck")).setSelected(true);
        controller.persistException = new RuntimeException("db down");

        Logger logger = Logger.getLogger(RegisterController.class.getName());
        Level previousLevel = logger.getLevel();
        try {
            logger.setLevel(Level.OFF);
            controller.register();
        } finally {
            logger.setLevel(previousLevel);
        }

        assertEquals("Register failed (database error).", controller.lastWarningMessage);
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void goLogin_navigatesToLogin() {
        controller.goLogin();
        assertEquals(AppState.Screen.LOGIN, controller.lastNavigatedScreen);
    }

    private void fillValidForm() {
        ((TextField) getPrivate("firstNameField")).setText("  Jane ");
        ((TextField) getPrivate("lastNameField")).setText(" Doe  ");
        ((TextField) getPrivate("emailField")).setText("  jane@example.com  ");
        ((PasswordField) getPrivate("passwordField")).setText("  secret123  ");
        ((RadioButton) getPrivate("teacherYes")).setSelected(false);
        ((RadioButton) getPrivate("teacherNo")).setSelected(true);
        ((CheckBox) getPrivate("termsCheck")).setSelected(false);
        controller.lastWarningMessage = null;
        controller.lastNavigatedScreen = null;
        controller.persistedUser = null;
        controller.persistException = null;
        controller.emailAlreadyExists = false;
        controller.checkedEmail = null;
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = RegisterController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = RegisterController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method m = RegisterController.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
