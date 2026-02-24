package controller;

import controller.components.HeaderController;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import model.AppState;
import model.entity.ClassModel;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ClassDetailControllerTest {

    private ClassDetailController controller;

    @BeforeEach
    void setUp() {
        controller = new ClassDetailController();

        // Inject UI components
        setPrivate("header", new Parent() {});
        setPrivate("headerController", new HeaderController());

        // Fake logged-in user
        User teacher = new User();
        setUserId(teacher);
        teacher.setRole(1); // teacher
        teacher.setFirstName("Alice");
        teacher.setLastName("Teacher");
        AppState.currentUser.set(teacher);

        // Fake selected class
        ClassModel c = new ClassModel();
        c.setClassName("Math 101");
        c.setTeacher(teacher);
        AppState.selectedClass.set(c);

        // Reset navOverride
        AppState.navOverride.set(null);

        // Call initialize()
        callPrivate();
    }

    // ---------------- Helper: set userId ----------------
    private void setUserId(User user) {
        try {
            Field f = User.class.getDeclaredField("userId");
            f.setAccessible(true);
            f.set(user, 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Reflection Helpers ----------------

    private void setPrivate(String field, Object value) {
        try {
            Field f = ClassDetailController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate() {
        try {
            Field f = ClassDetailController.class.getDeclaredField("headerController");
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate() {
        try {
            Method m = ClassDetailController.class.getDeclaredMethod("initialize");
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(Object arg) {
        try {
            Method m = ClassDetailController.class.getDeclaredMethod("openFlashcardSet", ActionEvent.class);
            m.setAccessible(true);
            m.invoke(controller,arg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Tests ----------------

    @Test
    void testInitialize_setsHeaderCorrectly() {
        HeaderController header = (HeaderController) getPrivate();

        String title = getHeaderLabel(header, "titleLabel");
        assertEquals("Math 101", title);

        String meta = getHeaderLabel(header, "metaLabel");
        assertEquals("Teacher: Alice Teacher", meta);
    }

    @Test
    void testInitialize_setsBackButton() {
        HeaderController header = (HeaderController) getPrivate();

        boolean visible = getBoolean(header);
        assertTrue(visible);
    }

    @Test
    void testOpenFlashcardSet_defaultName() {
        Button btn = new Button();
        ActionEvent event = new ActionEvent(btn, null);

        callPrivate(event);

        assertEquals("Flashcard Set", AppState.selectedFlashcardSetName.get());
        assertEquals(AppState.Screen.FLASHCARD_SET, AppState.navOverride.get());
    }

    @Test
    void testOpenFlashcardSet_withUserData() {
        Button btn = new Button();
        btn.setUserData("Algebra Set");
        ActionEvent event = new ActionEvent(btn, null);

        callPrivate(event);

        assertEquals("Algebra Set", AppState.selectedFlashcardSetName.get());
        assertEquals(AppState.Screen.FLASHCARD_SET, AppState.navOverride.get());
    }

    // ---------------- Helper to read HeaderController labels ----------------

    private String getHeaderLabel(HeaderController header, String field) {
        try {
            Field f = HeaderController.class.getDeclaredField(field);
            f.setAccessible(true);
            return ((javafx.scene.control.Label) f.get(header)).getText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getBoolean(HeaderController header) {
        try {
            Field f = HeaderController.class.getDeclaredField("backButton");
            f.setAccessible(true);
            Object node = f.get(header);

            Method m = node.getClass().getMethod("is" + capitalize());
            return (boolean) m.invoke(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String capitalize() {
        return "visible".substring(0,1).toUpperCase() + "visible".substring(1);
    }
}
