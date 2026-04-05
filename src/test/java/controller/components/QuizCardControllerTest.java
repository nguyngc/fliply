package controller.components;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import model.entity.Quiz;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class QuizCardControllerTest {

    static { new JFXPanel(); }

    private QuizCardController controller;

    @BeforeEach
    void setUp() {
        controller = new QuizCardController();
        setPrivate("quizTitleLabel", new Label());
        setPrivate("questionCountLabel", new Label());
        setPrivate("progressTextLabel", new Label());
        setPrivate("progressBar", new ProgressBar());
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = QuizCardController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void setQuiz_updatesLabelsAndHidesProgress() {
        Quiz quiz = new Quiz();
        setQuizId(quiz, 42);
        quiz.setNoOfQuestions(7);

        controller.setQuiz(quiz);

        assertEquals("Quiz 42", ((Label) getPrivate("quizTitleLabel")).getText());
        assertEquals("7 questions", ((Label) getPrivate("questionCountLabel")).getText());
        assertFalse(((Label) getPrivate("progressTextLabel")).isVisible());
        assertFalse(((ProgressBar) getPrivate("progressBar")).isVisible());
    }

    @Test
    void setQuiz_nullDoesNothing() {
        controller.setQuiz(null);
        assertEquals("", ((Label) getPrivate("quizTitleLabel")).getText());
    }

    @Test
    void setQuiz_handlesNullQuestionCount() {
        Quiz quiz = new Quiz();
        setQuizId(quiz, 7);

        controller.setQuiz(quiz);
        assertEquals("0 questions", ((Label) getPrivate("questionCountLabel")).getText());
    }

    private Object getPrivate(String field) {
        try {
            Field f = QuizCardController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setQuizId(Quiz quiz, int id) {
        try {
            Field f = Quiz.class.getDeclaredField("quizId");
            f.setAccessible(true);
            f.set(quiz, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

