package controller;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import model.AppState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import util.LocaleManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegisterControllerTest {

    static { new JFXPanel(); }

    private RegisterController controller;

    @BeforeEach
    void setUp() {
        controller = new RegisterController();
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
        runOnFxThread(() -> callPrivate("initialize"));
    }

    @AfterEach
    void tearDown() {
        resetDialogsFactory();
    }

    @Test
    @Order(1)
    void initialize_hidesVisiblePasswordField() {
        assertFalse(((TextField) getPrivate("passwordTextField")).isVisible());
        assertFalse(((TextField) getPrivate("passwordTextField")).isManaged());
        assertNotNull(((ImageView) getPrivate("eyeIcon")).getImage());
    }

    @Test
    @Order(2)
    void togglePassword_switchesBetweenMaskedAndVisible() {
        PasswordField passwordField = (PasswordField) getPrivate("passwordField");
        TextField passwordTextField = (TextField) getPrivate("passwordTextField");
        passwordField.setText("secret123");

        runOnFxThread(() -> callPrivate("togglePassword"));
        assertTrue(passwordTextField.isVisible());
        assertEquals("secret123", passwordTextField.getText());
        assertFalse(passwordField.isVisible());

        passwordTextField.setText("changed");
        runOnFxThread(() -> callPrivate("togglePassword"));
        assertTrue(passwordField.isVisible());
        assertEquals("changed", passwordField.getText());
        assertFalse(passwordTextField.isVisible());
    }

    @Test
    @Order(3)
    void register_withoutTermsShowsWarning() {
        AtomicReference<Alert> alertRef = new AtomicReference<>();
        CountDownLatch created = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);

        setDialogsFactory(type -> {
            Alert alert = new Alert(type);
            alertRef.set(alert);
            created.countDown();
            return alert;
        });

        ((TextField) getPrivate("firstNameField")).setText("John");
        ((TextField) getPrivate("lastNameField")).setText("Doe");
        ((TextField) getPrivate("emailField")).setText("john@example.com");
        ((PasswordField) getPrivate("passwordField")).setText("secret12");
        ((CheckBox) getPrivate("termsCheck")).setSelected(false);

        Platform.runLater(() -> {
            callPrivate("register");
            finished.countDown();
        });

        await(created);
        Platform.runLater(() -> {
            Alert alert = alertRef.get();
            if (alert != null) {
                alert.hide();
            }
        });
        await(finished);

        assertNull(AppState.currentUser.get());
    }

    private void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timed out waiting for JavaFX task");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        if (failure.get() != null) {
            throw new RuntimeException(failure.get());
        }
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

    private void callPrivate(String method) {
        try {
            Method m = RegisterController.class.getDeclaredMethod(method);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setDialogsFactory(java.util.function.Function<Alert.AlertType, Alert> factory) {
        try {
            Class<?> dialogs = Class.forName("util.Dialogs");
            Method m = dialogs.getDeclaredMethod("setAlertFactory", java.util.function.Function.class);
            m.setAccessible(true);
            m.invoke(null, factory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void resetDialogsFactory() {
        try {
            Class<?> dialogs = Class.forName("util.Dialogs");
            Method m = dialogs.getDeclaredMethod("resetAlertFactory");
            m.setAccessible(true);
            m.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out waiting for JavaFX task");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}

