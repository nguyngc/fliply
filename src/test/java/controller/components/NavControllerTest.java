package controller.components;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.AppState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class NavControllerTest {
    static { new JFXPanel(); }

    private TestableNavController controller;
    private AppState.NavItem previousNav;
    private AppState.Role previousRole;

    @BeforeEach
    void setUp() throws Exception {
        previousNav = AppState.activeNav.get();
        previousRole = AppState.getRole();
        controller = new TestableNavController();

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

        AppState.activeNav.set(AppState.NavItem.HOME);
        AppState.role.set(AppState.Role.STUDENT);
        callPrivate("initialize");
    }

    @AfterEach
    void tearDown() {
        AppState.activeNav.set(previousNav);
        AppState.role.set(previousRole);
    }

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

        assertTrue(flashLabel.getStyle().contains("#3D8FEF"));
        assertTrue(homeLabel.getStyle().contains("#8C8C8C"));
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
        Image classActive = (Image) getPrivate("classActive");
        Image classInactive = (Image) getPrivate("classInactive");

        classBtn.setSelected(true);
        assertSame(classActive, classIcon.getImage());

        classBtn.setSelected(false);
        assertSame(classInactive, classIcon.getImage());
    }

    @Test
    void testQuizAndAccountNavUpdateSelectionAndIcons() {
        ToggleButton quizBtn = (ToggleButton) getPrivate("quizBtn");
        ToggleButton accountBtn = (ToggleButton) getPrivate("accountBtn");
        ImageView quizIcon = (ImageView) getPrivate("quizIcon");
        ImageView accountIcon = (ImageView) getPrivate("accountIcon");
        Label quizLabel = (Label) getPrivate("quizLabel");
        Label accountLabel = (Label) getPrivate("accountLabel");

        AppState.activeNav.set(AppState.NavItem.QUIZZES);

        assertTrue(quizBtn.isSelected());
        assertFalse(accountBtn.isSelected());
        assertSame(getPrivate("quizActive"), quizIcon.getImage());
        assertTrue(quizLabel.getStyle().contains("#3D8FEF"));

        AppState.activeNav.set(AppState.NavItem.ACCOUNT);

        assertTrue(accountBtn.isSelected());
        assertFalse(quizBtn.isSelected());
        assertSame(getPrivate("accountActive"), accountIcon.getImage());
        assertTrue(accountLabel.getStyle().contains("#3D8FEF"));
    }

    @Test
    void testNavigationHandlersUseNavigateToSeam() {
        callPrivate("goHome");
        assertEquals(AppState.Screen.HOME, controller.lastScreen);

        callPrivate("goClass");
        assertEquals(AppState.Screen.CLASSES, controller.lastScreen);

        callPrivate("goFlash");
        assertEquals(AppState.Screen.FLASHCARDS, controller.lastScreen);

        callPrivate("goQuiz");
        assertEquals(AppState.Screen.QUIZZES, controller.lastScreen);

        callPrivate("goAccount");
        assertEquals(AppState.Screen.ACCOUNT, controller.lastScreen);
    }

    private static final class TestableNavController extends NavController {
        private AppState.Screen lastScreen;

        @Override
        void navigateTo(AppState.Screen screen) {
            lastScreen = screen;
        }
    }
}
