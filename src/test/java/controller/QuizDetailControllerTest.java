package controller;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.AppState;
import model.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizDetailControllerTest {

    static {
        new JFXPanel();
    }

    private QuizDetailController controller;

    @BeforeEach
    void setUp() {
        controller = new QuizDetailController();
        AppState.quizAnswers.clear();
        AppState.quizCorrectMap.clear();
        AppState.quizQuestionIndex.set(0);
        AppState.quizPoints.set(0);

        setPrivate("termLabel", new Label());
        setPrivate("pageLabel", new Label());
        setPrivate("opt1", new Button());
        setPrivate("opt2", new Button());
        setPrivate("opt3", new Button());
        setPrivate("opt4", new Button());
        setPrivate("prevBtn", new Button());
        setPrivate("nextBtn", new Button());
        setPrivate("viewResultBtn", new Button());
    }

    @Test
    void render_clampsIndexAndHandlesLessThanFourOptions() {
        List<QuizService.QuizQuestion> questions = List.of(
                new QuizService.QuizQuestion(1, "Q1", "A", List.of("A", "B")),
                new QuizService.QuizQuestion(2, "Q2", "C", List.of("C", "D"))
        );
        setPrivate("questions", questions);
        AppState.quizQuestionIndex.set(2);

        assertDoesNotThrow(this::callRender);

        assertEquals(1, AppState.quizQuestionIndex.get());
        assertEquals("2 / 2", ((Label) getPrivate("pageLabel")).getText());
        assertTrue(((Button) getPrivate("opt3")).isDisable());
        assertTrue(((Button) getPrivate("opt4")).isDisable());
    }

    private void callRender() {
        try {
            Method m = QuizDetailController.class.getDeclaredMethod("render");
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = QuizDetailController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = QuizDetailController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

