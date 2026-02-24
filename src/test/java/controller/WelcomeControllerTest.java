package controller;

import model.AppState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class WelcomeControllerTest {

    private WelcomeController controller;

    @BeforeEach
    void setUp() {
        controller = new WelcomeController();
        AppState.navOverride.set(null);
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
    void testGoLogin_navigatesToLogin() {
        callPrivate("goLogin");
        assertEquals(AppState.Screen.LOGIN, AppState.navOverride.get());
    }

    @Test
    void testGoRegister_navigatesToRegister() {
        callPrivate("goRegister");
        assertEquals(AppState.Screen.REGISTER, AppState.navOverride.get());
    }
}
