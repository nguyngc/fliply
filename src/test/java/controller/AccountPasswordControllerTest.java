package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountPasswordControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableAccountPasswordController controller;
    private FakeHeaderController headerController;
    private PasswordField currentPwdField;
    private PasswordField newPwdField;
    private PasswordField confirmPwdField;
    private Label errorLabel;
    private Locale previousLocale;
    private AppState.Role previousRole;
    private User previousUser;
    private AppState.NavItem previousNavOverride;

    private static final class TestableAccountPasswordController extends AccountPasswordController {
        private User lastUpdatedUser;
        private int updateCount;
        private AppState.Screen lastNavigatedScreen;

        @Override
        void updateUser(User user) {
            lastUpdatedUser = user;
            updateCount++;
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    private static final class FakeHeaderController extends HeaderController {
        private boolean backVisible;
        private String title;
        private String subtitle;
        private Runnable backAction;
        private Variant variant;

        @Override
        public void setBackVisible(boolean visible) {
            backVisible = visible;
        }

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        @Override
        public void setOnBack(Runnable action) {
            backAction = action;
        }

        @Override
        public void applyVariant(Variant variant) {
            this.variant = variant;
        }
    }

    @BeforeEach
    void setUp() {
        previousLocale = LocaleManager.getLocale();
        previousRole = AppState.getRole();
        previousUser = AppState.currentUser.get();
        previousNavOverride = AppState.navOverride.get();

        LocaleManager.setLocale("en", "US");
        controller = new TestableAccountPasswordController();
        headerController = new FakeHeaderController();
        currentPwdField = new PasswordField();
        newPwdField = new PasswordField();
        confirmPwdField = new PasswordField();
        errorLabel = new Label();

        setPrivate("header", new Parent() {});
        setPrivate("headerController", headerController);
        setPrivate("currentPwdField", currentPwdField);
        setPrivate("newPwdField", newPwdField);
        setPrivate("confirmPwdField", confirmPwdField);
        setPrivate("errorLabel", errorLabel);

        User user = new User();
        user.setPassword("oldpass");
        AppState.currentUser.set(user);
        AppState.role.set(AppState.Role.STUDENT);
        AppState.navOverride.set(null);
    }

    @AfterEach
    void tearDown() {
        LocaleManager.setLocale(previousLocale);
        AppState.role.set(previousRole);
        AppState.currentUser.set(previousUser);
        AppState.navOverride.set(previousNavOverride);
    }

    @Test
    void initialize_configuresStudentHeaderAndNavOverride() {
        callPrivate("initialize");
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        assertTrue(headerController.backVisible);
        assertEquals(rb.getString("pwd.header.title"), headerController.title);
        assertEquals("", headerController.subtitle);
        assertEquals(HeaderController.Variant.STUDENT, headerController.variant);
        assertEquals(AppState.NavItem.ACCOUNT, AppState.navOverride.get());
    }

    @Test
    void initialize_setsTeacherVariantAndBackActionNavigates() {
        AppState.role.set(AppState.Role.TEACHER);

        callPrivate("initialize");
        headerController.backAction.run();

        assertEquals(HeaderController.Variant.TEACHER, headerController.variant);
        assertEquals(AppState.Screen.ACCOUNT, controller.lastNavigatedScreen);
    }

    @Test
    void onSave_withoutUser_showsError() {
        AppState.currentUser.set(null);

        callPrivate("onSave");

        assertTrue(errorLabel.isVisible());
        assertEquals(ResourceBundle.getBundle("Messages", LocaleManager.getLocale()).getString("pwd.error.user"), errorLabel.getText());
        assertEquals(0, controller.updateCount);
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void onSave_wrongCurrentPassword_showsError() {
        currentPwdField.setText("wrongpass");
        newPwdField.setText("123456");
        confirmPwdField.setText("123456");

        callPrivate("onSave");

        assertTrue(errorLabel.isVisible());
        assertEquals(ResourceBundle.getBundle("Messages", LocaleManager.getLocale()).getString("pwd.error.oldPwd"), errorLabel.getText());
    }

    @Test
    void onSave_newPasswordTooShort_showsError() {
        currentPwdField.setText("oldpass");
        newPwdField.setText("123");
        confirmPwdField.setText("123");

        callPrivate("onSave");

        assertTrue(errorLabel.isVisible());
        assertEquals(ResourceBundle.getBundle("Messages", LocaleManager.getLocale()).getString("pwd.error.newPwd1"), errorLabel.getText());
    }

    @Test
    void onSave_confirmMismatch_showsError() {
        currentPwdField.setText("oldpass");
        newPwdField.setText("123456");
        confirmPwdField.setText("654321");

        callPrivate("onSave");

        assertTrue(errorLabel.isVisible());
        assertEquals(ResourceBundle.getBundle("Messages", LocaleManager.getLocale()).getString("pwd.error.newPwd2"), errorLabel.getText());
    }

    @Test
    void onSave_successUpdatesPasswordHidesErrorAndNavigates() {
        User user = AppState.currentUser.get();
        errorLabel.setText("old error");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        currentPwdField.setText("oldpass");
        newPwdField.setText("123456");
        confirmPwdField.setText("123456");

        callPrivate("onSave");

        assertEquals("123456", user.getPassword());
        assertEquals(1, controller.updateCount);
        assertSame(user, controller.lastUpdatedUser);
        assertEquals("", errorLabel.getText());
        assertTrue(!errorLabel.isVisible());
        assertEquals(AppState.Screen.ACCOUNT, controller.lastNavigatedScreen);
    }

    @Test
    void onCancel_navigatesBackWithoutUpdating() {
        callPrivate("onCancel");

        assertEquals(AppState.Screen.ACCOUNT, controller.lastNavigatedScreen);
        assertEquals(0, controller.updateCount);
        assertNull(controller.lastUpdatedUser);
    }

    private void setPrivate(String fieldName, Object value) {
        try {
            Field field = AccountPasswordController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method method = AccountPasswordController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
