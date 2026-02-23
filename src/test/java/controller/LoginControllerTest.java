package controller;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import model.AppState;
import model.dao.UserDao;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    private LoginController controller;

    // Fake UserDao
    private static class FakeUserDao extends UserDao {
        User expectedUser = null;
        String lastEmail = null;
        String lastPassword = null;

        @Override
        public User findByEmailAndPassword(String email, String password) {
            lastEmail = email;
            lastPassword = password;
            return expectedUser;
        }
    }

    @BeforeEach
    void setUp() {
        controller = new LoginController();

        // Inject UI components
        setPrivate("emailField", new TextField());
        setPrivate("passwordField", new PasswordField());
        setPrivate("passwordTextField", new TextField());
        setPrivate("eyeIcon", new ImageView());
        setPrivate("errorLabel", new Label());

        // Fake UserDao
        FakeUserDao fakeDao = new FakeUserDao();
        setPrivate("userDao", fakeDao);

        // Reset AppState
        AppState.currentUser.set(null);
        AppState.navOverride.set(null);

        // Call initialize()
        callPrivate("initialize");
    }

    // ---------------- Reflection Helpers ----------------

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

    // ---------------- Tests ----------------

    @Test
    void testInitialize_hidesPasswordTextField() {
        TextField txt = (TextField) getPrivate("passwordTextField");

        assertFalse(txt.isVisible());
        assertFalse(txt.isManaged());
    }

    @Test
    void testTogglePassword_showsTextField() {
        PasswordField pf = (PasswordField) getPrivate("passwordField");
        TextField tf = (TextField) getPrivate("passwordTextField");

        pf.setText("secret");

        callPrivate("togglePassword");

        assertTrue(tf.isVisible());
        assertTrue(tf.isManaged());
        assertFalse(pf.isVisible());
        assertFalse(pf.isManaged());
        assertEquals("secret", tf.getText());
    }

    @Test
    void testTogglePassword_hidesTextField() {
        // First toggle ON
        callPrivate("togglePassword");
        // Then toggle OFF
        callPrivate("togglePassword");

        PasswordField pf = (PasswordField) getPrivate("passwordField");
        TextField tf = (TextField) getPrivate("passwordTextField");

        assertTrue(pf.isVisible());
        assertTrue(pf.isManaged());
        assertFalse(tf.isVisible());
        assertFalse(tf.isManaged());
    }

    @Test
    void testLogin_success() {
        FakeUserDao fake = (FakeUserDao) getPrivate("userDao");

        User u = new User();
        u.setEmail("a@b.com");
        u.setPassword("123");
        u.setRole(1); // teacher
        fake.expectedUser = u;

        TextField email = (TextField) getPrivate("emailField");
        PasswordField pass = (PasswordField) getPrivate("passwordField");

        email.setText("a@b.com");
        pass.setText("123");

        callPrivate("login");

        assertEquals(u, AppState.currentUser.get());
        assertEquals(AppState.Screen.HOME, AppState.navOverride.get());
    }

    @Test
    void testLogin_fail() {
        FakeUserDao fake = (FakeUserDao) getPrivate("userDao");
        fake.expectedUser = null; // login fail

        TextField email = (TextField) getPrivate("emailField");
        PasswordField pass = (PasswordField) getPrivate("passwordField");

        email.setText("wrong");
        pass.setText("wrong");

        callPrivate("login");

        Label err = (Label) getPrivate("errorLabel");
        assertTrue(err.isVisible());
        assertEquals("Invalid email or password", err.getText());
        assertNull(AppState.currentUser.get());
    }

    @Test
    void testGoRegister_navigates() {
        callPrivate("goRegister");
        assertEquals(AppState.Screen.REGISTER, AppState.navOverride.get());
    }

    @Test
    void testOnGoogleLogin_navigatesHome() {
        callPrivate("onGoogleLogin");
        assertEquals(AppState.Screen.HOME, AppState.navOverride.get());
        assertEquals(AppState.Role.STUDENT, AppState.role.get());
    }
}
