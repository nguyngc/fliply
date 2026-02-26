package model.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class QuizDetailsTest {

    // Helper: set private ID via reflection
    private void setId(Object obj, String field, Integer value) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getQuizDetailsId() {
        QuizDetails qd = new QuizDetails();
        setId(qd, "quizDetailsId", 10);
        assertEquals(10, qd.getQuizDetailsId());
    }

    @Test
    void getQuiz() {
        Quiz quiz = new Quiz();
        QuizDetails qd = new QuizDetails();
        qd.setQuiz(quiz);
        assertEquals(quiz, qd.getQuiz());
    }

    @Test
    void getFlashcard() {
        Flashcard f = new Flashcard();
        QuizDetails qd = new QuizDetails();
        qd.setFlashcard(f);
        assertEquals(f, qd.getFlashcard());
    }

    @Test
    void setQuiz() {
        Quiz quiz = new Quiz();
        QuizDetails qd = new QuizDetails();
        qd.setQuiz(quiz);
        assertEquals(quiz, qd.getQuiz());
    }

    @Test
    void setFlashcard() {
        Flashcard f = new Flashcard();
        QuizDetails qd = new QuizDetails();
        qd.setFlashcard(f);
        assertEquals(f, qd.getFlashcard());
    }

    @Test
    void testToString() {
        QuizDetails qd = new QuizDetails();

        Quiz quiz = new Quiz();
        Flashcard flashcard = new Flashcard();

        setId(qd, "quizDetailsId", 7);
        setId(quiz, "quizId", 3);
        setId(flashcard, "flashcardId", 15);

        qd.setQuiz(quiz);
        qd.setFlashcard(flashcard);

        String s = qd.toString();

        assertTrue(s.contains("quizDetailsId=7"));
        assertTrue(s.contains("quizId=3"));
        assertTrue(s.contains("flashcardId=15"));
    }
}
