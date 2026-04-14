package model.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class QuizTest {

    @Test
    void getQuizId() {
        Quiz q = new Quiz();
        try {
            Field f = Quiz.class.getDeclaredField("quizId");
            f.setAccessible(true);
            f.set(q, 10);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertEquals(10, q.getQuizId());
    }

    @Test
    void getNoOfQuestions() {
        Quiz q = new Quiz();
        q.setNoOfQuestions(15);
        assertEquals(15, q.getNoOfQuestions());
    }

    @Test
    void getUser() {
        User u = new User();
        Quiz q = new Quiz();
        q.setUser(u);
        assertEquals(u, q.getUser());
    }

    @Test
    void setNoOfQuestions() {
        Quiz q = new Quiz();
        q.setNoOfQuestions(20);
        assertEquals(20, q.getNoOfQuestions());
    }

    @Test
    void constructorAssignsFields() {
        User u = new User();

        Quiz q = new Quiz(12, u);

        assertEquals(12, q.getNoOfQuestions());
        assertSame(u, q.getUser());
    }

    @Test
    void testToString() {
        Quiz q = new Quiz();
        assertEquals("", q.toString());
    }
}
