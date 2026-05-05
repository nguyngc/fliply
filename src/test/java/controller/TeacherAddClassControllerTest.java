package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.AppState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class TeacherAddClassControllerTest {

    static { new JFXPanel(); }

    private TestableTeacherAddClassController controller;

    private static class TestableTeacherAddClassController extends TeacherAddClassController {
        private boolean blankCodeWarningShown;
        private String createdCode;
        private RuntimeException createFailure;
        private AppState.Screen lastNavigatedScreen;

        @Override
        void showBlankCodeWarning() {
            blankCodeWarningShown = true;
        }

        @Override
        void createClass(String code) {
            if (createFailure != null) {
                throw createFailure;
            }
            createdCode = code;
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

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

        @Override
        public void setOnBack(Runnable action) {
            backButton.setOnAction(event -> action.run());
        }
    }

    @BeforeEach
    void setUp() {
        controller = new TestableTeacherAddClassController();

        // Inject fake header
        FakeHeaderController fakeHeader = new FakeHeaderController();
        setPrivate("headerController", fakeHeader);

        // Inject UI field
        setPrivate("classCodeField", new TextField());

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

        assertEquals("New class", header.titleLabel.getText());
        assertTrue(header.backButton.isVisible());
    }

    @Test
    void testOnAdd_blankCode_showsWarningAndDoesNotNavigate() {
        TextField field = (TextField) getPrivate("classCodeField");
        field.setText("   ");

        callPrivate("onAdd");

        assertTrue(controller.blankCodeWarningShown);
        assertNull(controller.createdCode);
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void testOnAdd_validCode_trimsCreatesClassAndNavigates() {
        TextField field = (TextField) getPrivate("classCodeField");
        field.setText("  SEP-2026  ");

        callPrivate("onAdd");

        assertEquals("SEP-2026", controller.createdCode);
        assertEquals(AppState.Screen.CLASSES, controller.lastNavigatedScreen);
    }

    @Test
    void testOnAdd_serviceThrowsError_doesNotNavigate() {
        TextField field = (TextField) getPrivate("classCodeField");
        field.setText("error");
        controller.createFailure = new IllegalArgumentException("Invalid code");

        Logger logger = Logger.getLogger(TeacherAddClassController.class.getName());
        Level previousLevel = logger.getLevel();
        try {
            logger.setLevel(Level.OFF);
            callPrivate("onAdd");
        } finally {
            logger.setLevel(previousLevel);
        }

        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void testOnCancel_navigatesToClasses() {
        callPrivate("onCancel");

        assertEquals(AppState.Screen.CLASSES, controller.lastNavigatedScreen);
    }

    @Test
    void testBackAction_navigatesToClasses() {
        FakeHeaderController header = (FakeHeaderController) getPrivate("headerController");

        header.backButton.getOnAction().handle(null);

        assertEquals(AppState.Screen.CLASSES, controller.lastNavigatedScreen);
    }
}
