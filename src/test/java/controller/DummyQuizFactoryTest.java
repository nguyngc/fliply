package controller;

import model.AppState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DummyQuizFactoryTest {

    @AfterEach
    void tearDown() {
        AppState.myQuizzes.clear();
    }

    @Test
    void quizFactoriesCreateExpectedShape() {
        AppState.myQuizzes.clear();
        AppState.myQuizzes.addAll(
                new model.entity.Quiz(),
                new model.entity.Quiz()
        );

        AppState.QuizItem quiz1 = DummyQuizFactory.quiz1();
        AppState.QuizItem quiz2 = DummyQuizFactory.quiz2();
        AppState.QuizItem generated = DummyQuizFactory.generatedQuiz(3);

        assertEquals("Quiz 1", quiz1.getTitle());
        assertEquals(10, quiz1.getTotalQuestions());
        assertEquals(90, quiz1.getProgressPercent());
        assertEquals(10, quiz1.getQuestions().size());

        assertEquals("Quiz 2", quiz2.getTitle());
        assertEquals(100, quiz2.getProgressPercent());
        assertEquals(10, quiz2.getQuestions().size());

        assertEquals("Quiz 3", generated.getTitle());
        assertEquals(3, generated.getTotalQuestions());
        assertEquals(0, generated.getProgressPercent());
        assertEquals(3, generated.getQuestions().size());
        assertEquals("Term 1", generated.getQuestions().getFirst().getTerm());
        assertEquals(4, generated.getQuestions().getFirst().getOptions().length);
    }
}

