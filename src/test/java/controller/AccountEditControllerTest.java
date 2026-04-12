package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import model.AppState;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AccountEditControllerTest {

    static { new JFXPanel(); }

    private AccountEditController controller;
    private HeaderController header;

    @BeforeEach
    void setUp() {
        controller = new AccountEditController();

        header = Mockito.mock(HeaderController.class);

        // Inject mock vào controller
        setPrivate("header", new Parent() {});
        setPrivate("headerController", header);

        // Inject UI components
        setPrivate("firstNameField", new TextField());
        setPrivate("lastNameField", new TextField());
        setPrivate("emailField", new TextField());

        // Fake logged-in user
        User u = new User();
        u.setFirstName("John");
        u.setLastName("Doe");
        u.setEmail("john@example.com");
        AppState.currentUser.set(u);

        // Reset navOverride
        AppState.navOverride.set(null);

        // Gọi initialize()
        callPrivate("initialize");
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = AccountEditController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = AccountEditController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method m = AccountEditController.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Tests ----------------

    @Test
    void testInitialize_loadsUserData() {
        TextField first = (TextField) getPrivate("firstNameField");
        TextField last = (TextField) getPrivate("lastNameField");
        TextField email = (TextField) getPrivate("emailField");

        assertEquals("John", first.getText());
        assertEquals("Doe", last.getText());
        assertEquals("john@example.com", email.getText());
    }

}
