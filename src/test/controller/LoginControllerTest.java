package controller;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import model.AppState;
import model.dao.UserDao;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    static { new JFXPanel(); }

    private LoginController controller;

    // Fake DAO test logic login
    private static class FakeUserDao extends UserDao {
        User expectedUser = null;

        @Override
        public User findByEmailAndPassword(String email, String password) {
            return expectedUser;
        }
    }

    @BeforeEach
    void setUp() {
        controller = new LoginController();

        // Inject UI fields
        setPrivate("emailField", new TextField());
        setPrivate("passwordField", new PasswordField());
        setPrivate("passwordTextField", new TextField());
        setPrivate("eyeIcon", new ImageView());
        setPrivate("errorLabel", new Label());

        // Inject fake DAO
        FakeUserDao fakeDao = new FakeUserDao();
        setPrivate("userDao", fakeDao);

        // Reset AppState
        AppState.currentUser.set(null);
        AppState.role.set(null);

        //  initialize()
        callPrivate("initialize");
    }

    // ---------------- Reflection helpers ----------------

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
        callPrivate("togglePassword");
        callPrivate("togglePassword");

        PasswordField pf = (PasswordField) getPrivate("passwordField");
        TextField tf = (TextField) getPrivate("passwordTextField");

        assertTrue(pf.isVisible());
        assertTrue(pf.isManaged());
        assertFalse(tf.isVisible());
        assertFalse(tf.isManaged());
    }
    @Disabled
    @Test
    void testLogin_success_setsUserAndRole() {
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

        // test logic
        callPrivate("login");

        assertEquals(u, AppState.currentUser.get());
        assertEquals(AppState.Role.TEACHER, AppState.role.get());
    }

    @Test
    void testLogin_fail_showsError() {
        FakeUserDao fake = (FakeUserDao) getPrivate("userDao");
        fake.expectedUser = null;

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
}
