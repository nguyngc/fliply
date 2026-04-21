package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableAccountController controller;
    private FakeHeaderController headerController;
    private Locale previousLocale;
    private AppState.Role previousRole;
    private User previousUser;
    private AppState.NavItem previousNavOverride;

    private static final class TestableAccountController extends AccountController {
        private AppState.Screen lastNavigatedScreen;
        private User lastUpdatedUser;
        private int updateCount;
        private int reloadCount;

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }

        @Override
        void updateUser(User user) {
            lastUpdatedUser = user;
            updateCount++;
        }

        @Override
        void reloadCurrentView() {
            reloadCount++;
        }
    }

    private static final class FakeHeaderController extends HeaderController {
        private boolean backVisible;
        private String title;
        private String subtitle;
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
        controller = new TestableAccountController();
        headerController = new FakeHeaderController();

        setPrivate("languageMenuButton", new MenuButton());
        setPrivate("header", new Parent() {});
        setPrivate("headerController", headerController);

        AppState.role.set(AppState.Role.STUDENT);
        AppState.currentUser.set(null);
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
    void initialize_setsStudentHeaderAndNavOverride() {
        callPrivate("initialize");
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        assertFalse(headerController.backVisible);
        assertEquals(rb.getString("account.header.title"), headerController.title);
        assertEquals("", headerController.subtitle);
        assertEquals(HeaderController.Variant.STUDENT, headerController.variant);
        assertEquals(AppState.NavItem.ACCOUNT, AppState.navOverride.get());
    }

    @Test
    void initialize_setsTeacherVariant() {
        AppState.role.set(AppState.Role.TEACHER);

        callPrivate("initialize");

        assertEquals(HeaderController.Variant.TEACHER, headerController.variant);
    }

    @Test
    void accountActions_navigateToExpectedScreens() {
        callPrivate("onEditAccount");
        assertEquals(AppState.Screen.ACCOUNT_EDIT, controller.lastNavigatedScreen);

        callPrivate("onChangePassword");
        assertEquals(AppState.Screen.ACCOUNT_PASSWORD, controller.lastNavigatedScreen);

        callPrivate("onHelp");
        assertEquals(AppState.Screen.ACCOUNT_HELP, controller.lastNavigatedScreen);

        callPrivate("onAbout");
        assertEquals(AppState.Screen.ACCOUNT_ABOUT, controller.lastNavigatedScreen);
    }

    @Test
    void onLogout_resetsLocaleClearsUserAndNavigatesToLogin() {
        User user = new User();
        user.setLanguage("vi");
        AppState.currentUser.set(user);
        LocaleManager.setLocale("vi", "VN");

        callPrivate("onLogout");

        assertEquals(Locale.of("en", "US"), LocaleManager.getLocale());
        assertNull(AppState.currentUser.get());
        assertEquals(AppState.Screen.LOGIN, controller.lastNavigatedScreen);
    }

    @Test
    void languageSwitchers_updateUserPreferenceAndReload() {
        User user = new User();
        user.setLanguage("en");
        AppState.currentUser.set(user);

        assertLanguageSwitch("switchToEnglish", Locale.of("en", "US"), user);
        assertLanguageSwitch("switchToArabic", Locale.of("ar", "AR"), user);
        assertLanguageSwitch("switchToFinnish", Locale.of("fi", "FI"), user);
        assertLanguageSwitch("switchToKorean", Locale.of("ko", "KR"), user);
        assertLanguageSwitch("switchToLao", Locale.of("lo", "LA"), user);
        assertLanguageSwitch("switchToVietnamese", Locale.of("vi", "VN"), user);
    }

    @Test
    void languageSwitcher_withoutCurrentUserStillReloadsAndSkipsUpdate() {
        AppState.currentUser.set(null);
        int previousReloadCount = controller.reloadCount;

        callPrivate("switchToKorean");

        assertEquals(Locale.of("ko", "KR"), LocaleManager.getLocale());
        assertEquals(previousReloadCount + 1, controller.reloadCount);
        assertEquals(0, controller.updateCount);
        assertNull(controller.lastUpdatedUser);
    }

    private void assertLanguageSwitch(String methodName, Locale expectedLocale, User user) {
        int previousUpdateCount = controller.updateCount;
        int previousReloadCount = controller.reloadCount;

        callPrivate(methodName);

        assertEquals(expectedLocale, LocaleManager.getLocale());
        assertEquals(expectedLocale.getLanguage(), user.getLanguage());
        assertEquals(previousUpdateCount + 1, controller.updateCount);
        assertEquals(user, controller.lastUpdatedUser);
        assertEquals(previousReloadCount + 1, controller.reloadCount);
    }

    private void setPrivate(String fieldName, Object value) {
        try {
            Field field = AccountController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method method = AccountController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
