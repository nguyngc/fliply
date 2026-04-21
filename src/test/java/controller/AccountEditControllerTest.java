package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
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

class AccountEditControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableAccountEditController controller;
    private FakeHeaderController headerController;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private Locale previousLocale;
    private AppState.Role previousRole;
    private User previousUser;
    private AppState.NavItem previousNavOverride;

    private static final class TestableAccountEditController extends AccountEditController {
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
        controller = new TestableAccountEditController();
        headerController = new FakeHeaderController();
        firstNameField = new TextField();
        lastNameField = new TextField();
        emailField = new TextField();

        setPrivate("header", new Parent() {});
        setPrivate("headerController", headerController);
        setPrivate("firstNameField", firstNameField);
        setPrivate("lastNameField", lastNameField);
        setPrivate("emailField", emailField);

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
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
    void initialize_loadsUserDataAndConfiguresStudentHeader() {
        callPrivate("initialize");
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        assertEquals("John", firstNameField.getText());
        assertEquals("Doe", lastNameField.getText());
        assertEquals("john@example.com", emailField.getText());
        assertTrue(headerController.backVisible);
        assertEquals(rb.getString("edit.header.title"), headerController.title);
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
    void onSave_trimsValuesUpdatesUserAndNavigatesBack() {
        callPrivate("initialize");

        firstNameField.setText("  Jane  ");
        lastNameField.setText("  Smith ");
        emailField.setText(" jane@example.com  ");

        callPrivate("onSave");

        User currentUser = AppState.currentUser.get();
        assertEquals("Jane", currentUser.getFirstName());
        assertEquals("Smith", currentUser.getLastName());
        assertEquals("jane@example.com", currentUser.getEmail());
        assertEquals(1, controller.updateCount);
        assertSame(currentUser, controller.lastUpdatedUser);
        assertEquals(AppState.Screen.ACCOUNT, controller.lastNavigatedScreen);
    }

    @Test
    void onSave_convertsNullFieldsToEmptyStrings() {
        callPrivate("initialize");

        firstNameField.setText(null);
        lastNameField.setText(null);
        emailField.setText(null);

        callPrivate("onSave");

        User currentUser = AppState.currentUser.get();
        assertEquals("", currentUser.getFirstName());
        assertEquals("", currentUser.getLastName());
        assertEquals("", currentUser.getEmail());
    }

    @Test
    void onCancel_navigatesBackWithoutUpdate() {
        callPrivate("onCancel");

        assertEquals(AppState.Screen.ACCOUNT, controller.lastNavigatedScreen);
        assertEquals(0, controller.updateCount);
        assertNull(controller.lastUpdatedUser);
    }

    private void setPrivate(String fieldName, Object value) {
        try {
            Field field = AccountEditController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method method = AccountEditController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
