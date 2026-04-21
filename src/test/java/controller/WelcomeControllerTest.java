package controller;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import model.AppState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.LocaleManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WelcomeControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableWelcomeController controller;
    private Locale previousLocale;
    private AppState.Screen previousScreen;
    private AppState.NavItem previousActiveNav;
    private AppState.NavItem previousNavOverride;

    private static class TestableWelcomeController extends WelcomeController {
        private AppState.Screen lastNavigatedScreen;
        private int reloadCount;

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
            AppState.currentScreen.set(screen);
            AppState.activeNav.set(screen.nav);
        }

        @Override
        void reloadCurrentView() {
            reloadCount++;
        }
    }

    @BeforeEach
    void setUp() {
        previousLocale = LocaleManager.getLocale();
        previousScreen = AppState.currentScreen.get();
        previousActiveNav = AppState.activeNav.get();
        previousNavOverride = AppState.navOverride.get();
        controller = new TestableWelcomeController();
    }

    @AfterEach
    void tearDown() {
        LocaleManager.setLocale(previousLocale.getLanguage(), previousLocale.getCountry());
        AppState.currentScreen.set(previousScreen);
        AppState.activeNav.set(previousActiveNav);
        AppState.navOverride.set(previousNavOverride);
    }

    @Test
    void updateLocale_changesLocaleWithoutNavigation() {
        controller.updateLocale("fi", "FI");
        assertEquals(new Locale("fi", "FI"), LocaleManager.getLocale());

        controller.updateLocale("vi", "VN");
        assertEquals(new Locale("vi", "VN"), LocaleManager.getLocale());
    }

    @Test
    void initialize_appliesLaoStylesWhenLocaleIsLao() {
        LocaleManager.setLocale("lo", "LA");

        MenuButton languageMenuButton = new MenuButton();
        Label titleLabel = new Label();
        Label subtitleLabel = new Label();
        Button registerButton = new Button();
        Button loginButton = new Button();

        runOnFxThread(() -> {
            setPrivate("languageMenuButton", languageMenuButton);
            setPrivate("titleLabel", titleLabel);
            setPrivate("subtitleLabel", subtitleLabel);
            setPrivate("registerButton", registerButton);
            setPrivate("loginButton", loginButton);
            new Scene(new VBox(languageMenuButton, titleLabel, subtitleLabel, registerButton, loginButton));
            controller.initialize();
        });

        assertEquals(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-family: 'Noto Sans Lao';",
                languageMenuButton.getStyle()
        );
        assertEquals("-fx-font-family: 'Noto Sans Lao';", titleLabel.getStyle());
        assertEquals("-fx-font-family: 'Noto Sans Lao';", subtitleLabel.getStyle());
        assertEquals("-fx-font-family: 'Noto Sans Lao';", registerButton.getStyle());
        assertEquals("-fx-font-family: 'Noto Sans Lao';", loginButton.getStyle());
    }

    @Test
    void initialize_leavesStylesUntouchedWhenLocaleIsNotLao() {
        LocaleManager.setLocale("en", "US");

        MenuButton languageMenuButton = new MenuButton();
        Label titleLabel = new Label();
        Label subtitleLabel = new Label();
        Button registerButton = new Button();
        Button loginButton = new Button();

        runOnFxThread(() -> {
            languageMenuButton.setStyle("menu-style");
            titleLabel.setStyle("title-style");
            subtitleLabel.setStyle("subtitle-style");
            registerButton.setStyle("register-style");
            loginButton.setStyle("login-style");
            setPrivate("languageMenuButton", languageMenuButton);
            setPrivate("titleLabel", titleLabel);
            setPrivate("subtitleLabel", subtitleLabel);
            setPrivate("registerButton", registerButton);
            setPrivate("loginButton", loginButton);
            new Scene(new VBox(languageMenuButton, titleLabel, subtitleLabel, registerButton, loginButton));
            controller.initialize();
        });

        assertEquals("menu-style", languageMenuButton.getStyle());
        assertEquals("title-style", titleLabel.getStyle());
        assertEquals("subtitle-style", subtitleLabel.getStyle());
        assertEquals("register-style", registerButton.getStyle());
        assertEquals("login-style", loginButton.getStyle());
    }

    @Test
    void goLogin_andGoRegister_navigateToExpectedScreens() {
        callPrivate("goLogin");
        assertEquals(AppState.Screen.LOGIN, controller.lastNavigatedScreen);
        assertEquals(AppState.Screen.LOGIN, AppState.currentScreen.get());

        callPrivate("goRegister");
        assertEquals(AppState.Screen.REGISTER, controller.lastNavigatedScreen);
        assertEquals(AppState.Screen.REGISTER, AppState.currentScreen.get());
    }

    @Test
    void languageSwitchers_updateLocaleAndReloadCurrentScreen() {
        assertLanguageSwitch("switchToEnglish", new Locale("en", "US"));
        assertLanguageSwitch("switchToArabic", new Locale("ar", "AR"));
        assertLanguageSwitch("switchToFinnish", new Locale("fi", "FI"));
        assertLanguageSwitch("switchToKorean", new Locale("ko", "KR"));
        assertLanguageSwitch("switchToLao", new Locale("lo", "LA"));
        assertLanguageSwitch("switchToVietnamese", new Locale("vi", "VN"));
    }

    private void assertLanguageSwitch(String methodName, Locale expectedLocale) {
        int previousReloadCount = controller.reloadCount;

        callPrivate(methodName);

        assertEquals(expectedLocale, LocaleManager.getLocale());
        assertEquals(previousReloadCount + 1, controller.reloadCount);
    }

    private void setPrivate(String fieldName, Object value) {
        try {
            Field field = WelcomeController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method method = WelcomeController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
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
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
    }
}
