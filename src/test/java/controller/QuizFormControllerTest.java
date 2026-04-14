package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuizFormControllerTest {

    private QuizFormController controller;

    @BeforeEach
    void setUp() {
        controller = new QuizFormController();
    }

    @Test
    void getMessage_returnsBundleValueWhenPresent() {
        setPrivate("resources", ResourceBundle.getBundle("Messages"));

        assertEquals("New Quiz", callGetMessage("quizForm.header", "fallback"));
    }

    @Test
    void getMessage_returnsFallbackWhenResourcesMissing() {
        setPrivate("resources", null);

        assertEquals("fallback", callGetMessage("quizForm.header", "fallback"));
    }

    @Test
    void getMessage_returnsFallbackWhenKeyMissing() {
        setPrivate("resources", ResourceBundle.getBundle("Messages"));

        assertEquals("fallback", callGetMessage("missing.key", "fallback"));
    }

    private String callGetMessage(String key, String fallback) {
        try {
            Method m = QuizFormController.class.getDeclaredMethod("getMessage", String.class, String.class);
            m.setAccessible(true);
            return (String) m.invoke(controller, key, fallback);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = QuizFormController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
