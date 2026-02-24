package controller;

import controller.components.HeaderController;
import javafx.scene.Parent;
import model.AppState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AccountAboutControllerTest {

    private AccountAboutController controller;

    @BeforeEach
    void setUp() {
        controller = new AccountAboutController();

        // Inject header + headerController
        setPrivate("header", new Parent() {});
        setPrivate("headerController", new HeaderController());

        // Reset AppState
        AppState.navOverride.set(null);
        AppState.role.set(AppState.Role.STUDENT);

        // Call initialize()
        callPrivate();
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = AccountAboutController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate() {
        try {
            Field f = AccountAboutController.class.getDeclaredField("headerController");
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate() {
        try {
            Method m = AccountAboutController.class.getDeclaredMethod("initialize");
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to read HeaderController fields
    private String getHeaderLabel(HeaderController header, String field) {
        try {
            Field f = HeaderController.class.getDeclaredField(field);
            f.setAccessible(true);
            return ((javafx.scene.control.Label) f.get(header)).getText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testInitialize_setsHeaderCorrectly() {
        HeaderController header = (HeaderController) getPrivate();

        assertEquals("About Us", getHeaderLabel(header, "titleLabel"));
        assertEquals("", getHeaderLabel(header, "subtitleLabel"));
        boolean visible = getBoolean(header);
        assertTrue(visible);
        
    }

    private boolean getBoolean(HeaderController header) {
        try {
            // get field from HeaderController (backButton)
            Field f = HeaderController.class.getDeclaredField("backButton");
            f.setAccessible(true);
            Object node = f.get(header);

            // getter ("isVisible")
            String methodName = "is" + "visible".substring(0,1).toUpperCase() + "visible".substring(1);

            // call method
            Method m = node.getClass().getMethod(methodName);
            return (boolean) m.invoke(node);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testInitialize_setsNavOverride() {
        assertEquals(AppState.NavItem.ACCOUNT, AppState.navOverride.get());
    }
}
