package controller.components;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import model.AppState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.LocaleManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class ClassCardControllerTest {
    static { new JFXPanel(); }

    private ClassCardController controller;

    @BeforeEach
    void setUp() {
        controller = new ClassCardController();

        // Inject UI components
        setPrivate("classNameLabel", new Label());
        setPrivate("teacherNameLabel", new Label());
        setPrivate("studentsCountLabel", new Label());
        setPrivate("setsCountLabel", new Label());
        setPrivate("progressTextLabel", new Label());
        setPrivate("progressBar", new ProgressBar());

        setPrivate("studentInfoBox", new VBox());
        setPrivate("teacherInfoBox", new VBox());

        // Reset AppState
        AppState.role.set(null);
    }

    // ---------------- Reflection Helpers ----------------

    private void setPrivate(String field, Object value) {
        try {
            Field f = ClassCardController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = ClassCardController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate() {
        try {
            Method m = ClassCardController.class.getDeclaredMethod("applyRoleVariant");
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Tests ----------------

    @Test
    void testApplyRoleVariant_teacher() {
        AppState.setRole(AppState.Role.TEACHER);

        callPrivate();

        VBox teacherBox = (VBox) getPrivate("teacherInfoBox");
        VBox studentBox = (VBox) getPrivate("studentInfoBox");

        assertTrue(teacherBox.isVisible());
        assertTrue(teacherBox.isManaged());
        assertFalse(studentBox.isVisible());
        assertFalse(studentBox.isManaged());
    }

    @Test
    void testApplyRoleVariant_student() {
        AppState.setRole(AppState.Role.STUDENT);

        callPrivate();

        VBox teacherBox = (VBox) getPrivate("teacherInfoBox");
        VBox studentBox = (VBox) getPrivate("studentInfoBox");

        assertFalse(teacherBox.isVisible());
        assertFalse(teacherBox.isManaged());
        assertTrue(studentBox.isVisible());
        assertTrue(studentBox.isManaged());
    }

    @Test
    void testOnClick() {
        final boolean[] clicked = {false};

        controller.setOnClick(() -> clicked[0] = true);

        controller.fire();

        assertTrue(clicked[0]);
    }

    @Test
    void testSetStudentCard_andProgress() {
        AppState.setRole(AppState.Role.STUDENT);

        controller.setStudentCard("CLS-101", "Teacher Name", 0.42);

        assertEquals("CLS-101", ((Label) getPrivate("classNameLabel")).getText());
        assertEquals("Teacher Name", ((Label) getPrivate("teacherNameLabel")).getText());
        assertEquals(0.42, ((ProgressBar) getPrivate("progressBar")).getProgress(), 0.0001);
        assertEquals("42% Completed", ((Label) getPrivate("progressTextLabel")).getText());
    }

    @Test
    void testSetTeacherCard_andProgress() {
        AppState.setRole(AppState.Role.TEACHER);
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        controller.setTeacherCard("CLS-202", 12, 3, 0.75);

        assertEquals("CLS-202", ((Label) getPrivate("classNameLabel")).getText());
        assertEquals("12 " + rb.getString("classDetail.students"), ((Label) getPrivate("studentsCountLabel")).getText());
        assertEquals("3 " + rb.getString("classDetail.sets"), ((Label) getPrivate("setsCountLabel")).getText());
        assertEquals(0.75, ((ProgressBar) getPrivate("progressBar")).getProgress(), 0.0001);
        assertEquals("75% Completed", ((Label) getPrivate("progressTextLabel")).getText());
    }
}
