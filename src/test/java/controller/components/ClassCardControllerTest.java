package controller.components;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import model.AppState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ClassCardControllerTest {

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
    void testSetStudentCard() {
        AppState.setRole(AppState.Role.STUDENT);

        controller.setStudentCard("MATH101", "Mr. John", 0.75);

        Label className = (Label) getPrivate("classNameLabel");
        Label teacherName = (Label) getPrivate("teacherNameLabel");
        Label progressText = (Label) getPrivate("progressTextLabel");
        ProgressBar bar = (ProgressBar) getPrivate("progressBar");

        assertEquals("MATH101", className.getText());
        assertEquals("Mr. John", teacherName.getText());
        assertEquals("75% Completed", progressText.getText());
        assertEquals(0.75, bar.getProgress());
    }

    @Test
    void testSetTeacherCard() {
        AppState.setRole(AppState.Role.TEACHER);

        controller.setTeacherCard("SCIENCE", 12, 3, 0.5);

        Label className = (Label) getPrivate("classNameLabel");
        Label students = (Label) getPrivate("studentsCountLabel");
        Label sets = (Label) getPrivate("setsCountLabel");
        Label progressText = (Label) getPrivate("progressTextLabel");
        ProgressBar bar = (ProgressBar) getPrivate("progressBar");

        assertEquals("SCIENCE", className.getText());
        assertEquals("12 students", students.getText());
        assertEquals("3 set of flashcards", sets.getText());
        assertEquals("50% Completed", progressText.getText());
        assertEquals(0.5, bar.getProgress());
    }

    @Test
    void testSetProgress() {
        controller.setProgress(0.33);

        Label progressText = (Label) getPrivate("progressTextLabel");
        ProgressBar bar = (ProgressBar) getPrivate("progressBar");

        assertEquals("33% Completed", progressText.getText());
        assertEquals(0.33, bar.getProgress());
    }

    @Test
    void testOnClick() {
        final boolean[] clicked = {false};

        controller.setOnClick(() -> clicked[0] = true);

        controller.fire();

        assertTrue(clicked[0]);
    }
}
