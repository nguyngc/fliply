package controller;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import model.AppState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.LocaleManager;
import view.Navigator;

import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class WelcomeControllerTest {

    static { new JFXPanel(); }

    private WelcomeController controller;
    private Locale previousLocale;

    @BeforeEach
    void setUp() {
        previousLocale = LocaleManager.getLocale();
        controller = new WelcomeController();
        AppState.navOverride.set(null);
        setPrivate("languageMenuButton", new MenuButton());
        setPrivate("titleLabel", new Label());
        setPrivate("subtitleLabel", new Label());
        setPrivate("registerButton", new Button());
        setPrivate("loginButton", new Button());
        runOnFxThread(() -> Navigator.init(new Stage()));
    }

    @AfterEach
    void tearDown() {
        LocaleManager.setLocale(previousLocale.getLanguage(), previousLocale.getCountry());
    }

    private void callPrivate(String methodName) {
        try {
            Method m = WelcomeController.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testInitialize_setsLaoStyles() {
        LocaleManager.setLocale("lo", "LA");
        runOnFxThread(() -> callPrivate("initialize"));

        assertTrue(((MenuButton) getPrivate("languageMenuButton")).getStyle().contains("Noto Sans Lao"));
        assertTrue(((Label) getPrivate("titleLabel")).getStyle().contains("Noto Sans Lao"));
    }

    @Test
    void testLocaleSwitchMethodsUpdateLocale() {
        LocaleManager.setLocale("en", "US");

        runOnFxThread(() -> callPrivate("switchToFinnish"));
        assertEquals(new Locale("fi", "FI"), LocaleManager.getLocale());

        runOnFxThread(() -> callPrivate("switchToKorean"));
        assertEquals(new Locale("ko", "KR"), LocaleManager.getLocale());

        runOnFxThread(() -> callPrivate("switchToLao"));
        assertEquals(new Locale("lo", "LA"), LocaleManager.getLocale());

        runOnFxThread(() -> callPrivate("switchToVietnamese"));
        assertEquals(new Locale("vi", "VN"), LocaleManager.getLocale());

        runOnFxThread(() -> callPrivate("switchToEnglish"));
        assertEquals(new Locale("en", "US"), LocaleManager.getLocale());
    }

    @Test
    void testNavigationMethods_doNotThrow() {
        AppState.currentScreen.set(AppState.Screen.WELCOME);

        runOnFxThread(() -> callPrivate("goLogin"));
        runOnFxThread(() -> callPrivate("goRegister"));

        assertNotNull(AppState.currentScreen.get());
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = WelcomeController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = WelcomeController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
}
