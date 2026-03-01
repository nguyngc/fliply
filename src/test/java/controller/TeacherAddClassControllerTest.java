package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import model.AppState;
import model.service.TeacherAddClassService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class TeacherAddClassControllerTest {

    static { new JFXPanel(); }

    private TeacherAddClassController controller;

    // Fake HeaderController with real UI nodes
    private static class FakeHeaderController extends HeaderController {
        public Label titleLabel = new Label();
        public Label subtitleLabel = new Label();
        public Label metaLabel = new Label();
        public Button backButton = new Button();

        @Override
        public void setTitle(String title) { titleLabel.setText(title); }

        @Override
        public void setSubtitle(String subtitle) { subtitleLabel.setText(subtitle); }

        @Override
        public void setBackVisible(boolean visible) {
            backButton.setVisible(visible);
            backButton.setManaged(visible);
        }

        @Override
        public void setMeta(String text) { metaLabel.setText(text); }
    }

    // Fake service
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

        // Inject fake header
        FakeHeaderController fakeHeader = new FakeHeaderController();
        setPrivate("header", new StackPane());
        setPrivate("headerController", fakeHeader);

        // Inject UI field
        setPrivate("classCodeField", new TextField());

        // Inject fake service
        FakeTeacherAddClassService fakeService = new FakeTeacherAddClassService();
        setPrivate("teacherAddClass", fakeService);

        // Reset navOverride
        AppState.navOverride.set(null);

        // Call initialize()
        callPrivate("initialize");
    }

    // Reflection helpers
    private void setPrivate(String field, Object value) {
        try {
            Field f = TeacherAddClassController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private Object getPrivate(String field) {
        try {
            Field f = TeacherAddClassController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private void callPrivate(String methodName) {
        try {
            Method m = TeacherAddClassController.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // Tests
    @Test
    void testInitialize_setsHeaderCorrectly() {
        FakeHeaderController header = (FakeHeaderController) getPrivate("headerController");

        assertEquals("New Class", header.titleLabel.getText());
        assertTrue(header.backButton.isVisible());
    }

    @Test
    void testOnAdd_blankCode_doesNothing() {
        TextField field = (TextField) getPrivate("classCodeField");
        field.setText("   ");

        callPrivate("onAdd");

        FakeTeacherAddClassService fake = (FakeTeacherAddClassService) getPrivate("teacherAddClass");
        assertNull(fake.lastCreatedCode);
        assertNull(AppState.navOverride.get());
    }

    @Disabled
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
        field.setText("error");

        callPrivate("onAdd");

        assertNull(AppState.navOverride.get());
    }
}
