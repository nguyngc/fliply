package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import util.I18n;

class QuizFormControllerTest {

    private QuizFormController controller;

    @BeforeEach
    void setUp() {
        controller = new QuizFormController();
    }

    @Test
    void i18nMessage_returnsBundleValueOrFallback() {
        ResourceBundle bundle = ResourceBundle.getBundle("Messages");

        assertEquals("New Quiz", I18n.message(bundle, "quizForm.header", "fallback"));
        assertEquals("fallback", I18n.message(null, "quizForm.header", "fallback"));
        assertEquals("fallback", I18n.message(bundle, "missing.key", "fallback"));
    }

    @Test
    void validateQuestionCount_returnsEmptyInputError() {
        setPrivate("resources", ResourceBundle.getBundle("Messages"));

        assertEquals("Please enter the number of questions.",
                callValidateQuestionCount("   ", 5));
    }

    @Test
    void validateQuestionCount_returnsInvalidNumberError() {
        setPrivate("resources", ResourceBundle.getBundle("Messages"));

        assertEquals("Please enter a valid positive number.",
                callValidateQuestionCount("abc", 5));
        assertEquals("Please enter a valid positive number.",
                callValidateQuestionCount("0", 5));
    }

    @Test
    void validateQuestionCount_returnsTooManyQuestionsError() {
        setPrivate("resources", ResourceBundle.getBundle("Messages"));

        assertEquals("Only 3 flashcards are available. Please enter a smaller number.",
                callValidateQuestionCount("4", 3));
    }

    @Test
    void validateQuestionCount_returnsNullWhenValid() {
        setPrivate("resources", ResourceBundle.getBundle("Messages"));

        assertNull(callValidateQuestionCount("3", 5));
    }

    private String callValidateQuestionCount(String rawCount, int availableCount) {
        try {
            Method m = QuizFormController.class.getDeclaredMethod("validateQuestionCount", String.class, int.class);
            m.setAccessible(true);
            return (String) m.invoke(controller, rawCount, availableCount);
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
