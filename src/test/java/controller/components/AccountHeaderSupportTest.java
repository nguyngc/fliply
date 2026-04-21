package controller.components;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.AppState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class AccountHeaderSupportTest {

    static { new JFXPanel(); }

    private HeaderController header;

    @BeforeEach
    void setUp() {
        header = new HeaderController();
        injectHeaderField("titleLabel", new Label());
        injectHeaderField("subtitleLabel", new Label());
        injectHeaderField("backButton", new Button());
        AppState.role.set(AppState.Role.STUDENT);
    }

    @Test
    void configureWithNullHeaderDoesNothing() {
        assertDoesNotThrow(() -> AccountHeaderSupport.configure(null, null, "about.header.title", null));
    }

    @Test
    void configureUsesFallbackBundleAndBackAction() {
        final boolean[] called = {false};

        AccountHeaderSupport.configure(header, null, "about.header.title", () -> called[0] = true);

        assertEquals("About us", ((Label) getHeaderField("titleLabel")).getText());
        assertEquals("", ((Label) getHeaderField("subtitleLabel")).getText());
        assertTrue(((Button) getHeaderField("backButton")).isVisible());
        invokeHeaderPrivate("onBack");
        assertTrue(called[0]);
    }

    @Test
    void configureUsesTeacherVariantWhenTeacherAndNullBackAction() {
        AppState.role.set(AppState.Role.TEACHER);

        AccountHeaderSupport.configure(header, null, "help.header.title", null);

        assertEquals("Help", ((Label) getHeaderField("titleLabel")).getText());
        assertTrue(((Button) getHeaderField("backButton")).isVisible());
    }

    @Test
    void configureUsesProvidedResourcesWhenPresent() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{"custom.header.title", "Custom Header"}};
            }
        };

        AccountHeaderSupport.configure(header, bundle, "custom.header.title", null);

        assertEquals("Custom Header", ((Label) getHeaderField("titleLabel")).getText());
    }

    @Test
    void utilityConstructorThrows() throws Exception {
        Constructor<AccountHeaderSupport> constructor = AccountHeaderSupport.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, thrown.getCause());
    }

    private void injectHeaderField(String field, Object value) {
        try {
            Field f = HeaderController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(header, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getHeaderField(String field) {
        try {
            Field f = HeaderController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(header);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeHeaderPrivate(String method) {
        try {
            Method m = HeaderController.class.getDeclaredMethod(method, javafx.event.ActionEvent.class);
            m.setAccessible(true);
            m.invoke(header, new javafx.event.ActionEvent());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


