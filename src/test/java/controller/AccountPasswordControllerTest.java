package controller;

import controller.components.HeaderController;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import model.AppState;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AccountPasswordControllerTest {

    private AccountPasswordController controller;

    @BeforeEach
    void setUp() {
        controller = new AccountPasswordController();

        // Inject UI components
        setPrivate("header", new Parent() {});
        setPrivate("headerController", new HeaderController());

        setPrivate("currentPwdField", new PasswordField());
        setPrivate("newPwdField", new PasswordField());
        setPrivate("confirmPwdField", new PasswordField());
        setPrivate("errorLabel", new Label());

        // Reset AppState
        AppState.navOverride.set(null);

        // Fake logged-in user
        User u = new User();
        u.setPassword("oldpass");
        AppState.currentUser.set(u);

        // Call private initialize()
        callPrivate("initialize");
    }

    // ---------------- Reflection Helpers ----------------

    private void setPrivate(String field, Object value) {
        try {
            Field f = AccountPasswordController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = AccountPasswordController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method m = AccountPasswordController.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Tests ----------------

    @Test
    void testInitialize_setsNavOverride() {
        assertEquals(AppState.NavItem.ACCOUNT, AppState.navOverride.get());
    }

    @Test
    void testOnSave_wrongCurrentPassword_showsError() {
        PasswordField current = (PasswordField) getPrivate("currentPwdField");
        PasswordField newPwd = (PasswordField) getPrivate("newPwdField");
        PasswordField confirm = (PasswordField) getPrivate("confirmPwdField");

        current.setText("wrongpass");
        newPwd.setText("123456");
        confirm.setText("123456");

        callPrivate("onSave");

        Label error = (Label) getPrivate("errorLabel");
        assertTrue(error.isVisible());
        assertEquals("Current password is incorrect.", error.getText());
    }

    @Test
    void testOnSave_newPasswordTooShort() {
        PasswordField current = (PasswordField) getPrivate("currentPwdField");
        PasswordField newPwd = (PasswordField) getPrivate("newPwdField");
        PasswordField confirm = (PasswordField) getPrivate("confirmPwdField");

        current.setText("oldpass");
        newPwd.setText("123");
        confirm.setText("123");

        callPrivate("onSave");

        Label error = (Label) getPrivate("errorLabel");
        assertTrue(error.isVisible());
        assertEquals("New password must be at least 6 characters.", error.getText());
    }

    @Test
    void testOnSave_confirmMismatch() {
        PasswordField current = (PasswordField) getPrivate("currentPwdField");
        PasswordField newPwd = (PasswordField) getPrivate("newPwdField");
        PasswordField confirm = (PasswordField) getPrivate("confirmPwdField");

        current.setText("oldpass");
        newPwd.setText("123456");
        confirm.setText("654321");

        callPrivate("onSave");

        Label error = (Label) getPrivate("errorLabel");
        assertTrue(error.isVisible());
        assertEquals("New password and confirm password do not match.", error.getText());
    }

    @Test
    void testOnSave_success_updatesPasswordAndNavigates() {
        PasswordField current = (PasswordField) getPrivate("currentPwdField");
        PasswordField newPwd = (PasswordField) getPrivate("newPwdField");
        PasswordField confirm = (PasswordField) getPrivate("confirmPwdField");

        current.setText("oldpass");
        newPwd.setText("newpassword");
        confirm.setText("newpassword");

        callPrivate("onSave");

        // Check password updated
        assertEquals("newpassword", AppState.currentUser.get().getPassword());

        // Check navigation
        assertEquals(AppState.Screen.ACCOUNT, AppState.navOverride.get());
    }

    @Test
    void testOnCancel_navigatesBack() {
        callPrivate("onCancel");
        assertEquals(AppState.Screen.ACCOUNT, AppState.navOverride.get());
    }
}
