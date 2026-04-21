package controller;

import model.entity.FlashcardSet;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FlashcardFormControllerTest {

    private FlashcardFormController controller;

    @BeforeEach
    void setUp() {
        controller = new FlashcardFormController();
    }

    @Test
    void validateInput_returnsWarningWhenSubjectMissing() {
        User user = new User();
        user.setEmail("student@test.com");

        assertEquals("Please select a subject and fill in term and definition.",
                callValidateInput(null, "Term", "Definition", user));
    }

    @Test
    void validateInput_returnsWarningWhenTermOrDefinitionMissing() {
        FlashcardSet set = new FlashcardSet();
        User user = new User();
        user.setEmail("student@test.com");

        assertEquals("Please select a subject and fill in term and definition.",
                callValidateInput(set, "   ", "Definition", user));
        assertEquals("Please select a subject and fill in term and definition.",
                callValidateInput(set, "Term", "", user));
    }

    @Test
    void validateInput_returnsNullWhenValid() {
        FlashcardSet set = new FlashcardSet();
        User user = new User();
        user.setEmail("student@test.com");

        assertNull(callValidateInput(set, "Term", "Definition", user));
    }

    private String callValidateInput(FlashcardSet set, String term, String def, User user) {
        try {
            Method method = FlashcardFormController.class.getDeclaredMethod(
                    "validateInput", FlashcardSet.class, String.class, String.class, User.class);
            method.setAccessible(true);
            return (String) method.invoke(controller, set, term, def, user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

