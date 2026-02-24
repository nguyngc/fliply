package controller.components;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import model.AppState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class NavControllerTest {

    private NavController controller;

    @BeforeEach
    void setUp() {
        controller = new NavController();

        // Inject UI components via reflection
        setPrivate("homeIcon", new ImageView());
        setPrivate("classIcon", new ImageView());
        setPrivate("flashcardIcon", new ImageView());
        setPrivate("quizIcon", new ImageView());
        setPrivate("accountIcon", new ImageView());

        setPrivate("homeBtn", new ToggleButton());
        setPrivate("classBtn", new ToggleButton());
        setPrivate("flashBtn", new ToggleButton());
        setPrivate("quizBtn", new ToggleButton());
        setPrivate("accountBtn", new ToggleButton());

        setPrivate("homeLabel", new Label());
        setPrivate("classLabel", new Label());
        setPrivate("flashLabel", new Label());
        setPrivate("quizLabel", new Label());
        setPrivate("accountLabel", new Label());

        // Reset AppState
        AppState.activeNav.set(AppState.NavItem.HOME);
        AppState.role.set(AppState.Role.STUDENT);

        // Call private initialize()
        callPrivate("initialize");
    }

    // ---------------- Reflection Helpers ----------------

    private void setPrivate(String field, Object value) {
        try {
            Field f = NavController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = NavController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method m = NavController.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Tests ----------------

    @Test
    void testInitialState_HomeSelected() {
        ToggleButton homeBtn = (ToggleButton) getPrivate("homeBtn");
        ToggleButton classBtn = (ToggleButton) getPrivate("classBtn");

        assertTrue(homeBtn.isSelected());
        assertFalse(classBtn.isSelected());
    }

    @Test
    void testActiveNav_ChangesSelection() {
        AppState.activeNav.set(AppState.NavItem.CLASSES);

        ToggleButton classBtn = (ToggleButton) getPrivate("classBtn");
        ToggleButton homeBtn = (ToggleButton) getPrivate("homeBtn");

        assertTrue(classBtn.isSelected());
        assertFalse(homeBtn.isSelected());
    }

    @Test
    void testLabelStyle_UpdatesCorrectly() {
        AppState.activeNav.set(AppState.NavItem.FLASHCARDS);

        Label flashLabel = (Label) getPrivate("flashLabel");
        Label homeLabel = (Label) getPrivate("homeLabel");

        assertTrue(flashLabel.getStyle().contains("#3D8FEF")); // active
        assertTrue(homeLabel.getStyle().contains("#8C8C8C"));  // inactive
    }

    @Test
    void testApplyRole_TeacherHidesFlashAndQuiz() {
        AppState.role.set(AppState.Role.TEACHER);

        ToggleButton flashBtn = (ToggleButton) getPrivate("flashBtn");
        ToggleButton quizBtn = (ToggleButton) getPrivate("quizBtn");

        assertFalse(flashBtn.isVisible());
        assertFalse(flashBtn.isManaged());
        assertFalse(quizBtn.isVisible());
        assertFalse(quizBtn.isManaged());
    }

    @Test
    void testApplyRole_StudentShowsFlashAndQuiz() {
        AppState.role.set(AppState.Role.STUDENT);

        ToggleButton flashBtn = (ToggleButton) getPrivate("flashBtn");
        ToggleButton quizBtn = (ToggleButton) getPrivate("quizBtn");

        assertTrue(flashBtn.isVisible());
        assertTrue(flashBtn.isManaged());
        assertTrue(quizBtn.isVisible());
        assertTrue(quizBtn.isManaged());
    }

    @Test
    void testToggleButtonChangesIcon() {
        ToggleButton classBtn = (ToggleButton) getPrivate("classBtn");
        ImageView classIcon = (ImageView) getPrivate("classIcon");

        classBtn.setSelected(true);

        assertNotNull(classIcon.getImage());
    }
}
