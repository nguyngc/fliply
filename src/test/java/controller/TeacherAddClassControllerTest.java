package controller;

import controller.components.HeaderController;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import model.AppState;
import model.service.TeacherAddClassService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class TeacherAddClassControllerTest {

    private TeacherAddClassController controller;

    // Fake service to capture created class code
    private static class FakeTeacherAddClassService extends TeacherAddClassService {
        String lastCreatedCode = null;

        @Override
        public void createClass(String code) {
            if (code.equals("error")) {
                throw new IllegalArgumentException("Invalid code");
            }
            lastCreatedCode = code;
        }
    }

    @BeforeEach
    void setUp() {
        controller = new TeacherAddClassController();

        // Inject UI components
        setPrivate("header", new Parent() {});
        setPrivate("headerController", new HeaderController());
        setPrivate("classCodeField", new TextField());

        // Fake service
        FakeTeacherAddClassService fakeService = new FakeTeacherAddClassService();
        setPrivate("teacherAddClass", fakeService);

        // Reset navOverride
        AppState.navOverride.set(null);

        // Call initialize()
        callPrivate("initialize");
    }

    // ---------------- Reflection Helpers ----------------

    private void setPrivate(String field, Object value) {
        try {
            Field f = TeacherAddClassController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = TeacherAddClassController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method m = TeacherAddClassController.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Tests ----------------

    @Test
    void testInitialize_setsHeaderCorrectly() {
        HeaderController header = (HeaderController) getPrivate("headerController");

        String title = getHeaderLabel(header, "titleLabel");
        assertEquals("New Class", title);

        boolean backVisible = getBoolean(header, "backButton", "visible");
        assertTrue(backVisible);
    }

    @Test
    void testOnAdd_blankCode_doesNothing() {
        TextField field = (TextField) getPrivate("classCodeField");
        field.setText("   "); // blank

        callPrivate("onAdd");

        FakeTeacherAddClassService fake = (FakeTeacherAddClassService) getPrivate("teacherAddClass");
        assertNull(fake.lastCreatedCode);
        assertNull(AppState.navOverride.get());
    }

    @Test
    void testOnAdd_validCode_createsClassAndNavigates() {
        TextField field = (TextField) getPrivate("classCodeField");
        field.setText("MATH101");

        callPrivate("onAdd");

        FakeTeacherAddClassService fake = (FakeTeacherAddClassService) getPrivate("teacherAddClass");
        assertEquals("MATH101", fake.lastCreatedCode);
        assertEquals(AppState.Screen.CLASSES, AppState.navOverride.get());
    }

    @Test
    void testOnAdd_serviceThrowsError_doesNotNavigate() {
        TextField field = (TextField) getPrivate("classCodeField");
        field.setText("error"); // fake service throws

        callPrivate("onAdd");

        assertNull(AppState.navOverride.get());
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

    private boolean getBoolean(HeaderController header, String field, String property) {
        try {
            Field f = HeaderController.class.getDeclaredField(field);
            f.setAccessible(true);
            Object node = f.get(header);

            Method m = node.getClass().getMethod("is" + capitalize(property));
            return (boolean) m.invoke(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String capitalize(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
