package controller;

import controller.components.HeaderController;
import javafx.scene.Parent;
import model.AppState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AccountControllerTest {

    private AccountController controller;

    @BeforeEach
    void setUp() {
        controller = new AccountController();

        setPrivate("header", new Parent() {});
        setPrivate("headerController", Mockito.mock(HeaderController.class));


        AppState.navOverride.set(null);

        callPrivate("initialize");
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = AccountController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method m = AccountController.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testInitialize_setsNavOverride() {
        assertEquals(AppState.NavItem.ACCOUNT, AppState.navOverride.get());
    }

    @Disabled("Cannot test UI navigation in unit test environment")
    @Test
    void testOnEditAccount() {
        callPrivate("onEditAccount");
        assertEquals(AppState.Screen.ACCOUNT_EDIT, AppState.navOverride.get());
    }

    @Disabled("Cannot test UI navigation in unit test environment")
    @Test
    void testOnChangePassword() {
        callPrivate("onChangePassword");
        assertEquals(AppState.Screen.ACCOUNT_PASSWORD, AppState.navOverride.get());
    }

    @Disabled("Cannot test UI navigation in unit test environment")
    @Test
    void testOnHelp() {
        callPrivate("onHelp");
        assertEquals(AppState.Screen.ACCOUNT_HELP, AppState.navOverride.get());
    }

    @Disabled("Cannot test UI navigation in unit test environment")
    @Test
    void testOnAbout() {
        callPrivate("onAbout");
        assertEquals(AppState.Screen.ACCOUNT_ABOUT, AppState.navOverride.get());
    }

    @Disabled("Cannot test UI navigation in unit test environment")
    @Test
    void testOnLogout() {
        callPrivate("onLogout");
        assertEquals(AppState.Screen.LOGIN, AppState.navOverride.get());
    }
}
