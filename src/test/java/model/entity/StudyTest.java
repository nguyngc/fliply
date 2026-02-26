package model.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class StudyTest {

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
    void getStudyId() {
        Study s = new Study();
        setId(s, "studyId", 10);
        assertEquals(10, s.getStudyId());
    }

    @Test
    void getStatistic() {
        Study s = new Study();
        s.setStatistic(5);
        assertEquals(5, s.getStatistic());
    }

    @Test
    void getUser() {
        User u = new User();
        Study s = new Study();
        s.setUser(u);
        assertEquals(u, s.getUser());
    }

    @Test
    void getFlashcardSet() {
        FlashcardSet fs = new FlashcardSet();
        Study s = new Study();
        s.setFlashcardSet(fs);
        assertEquals(fs, s.getFlashcardSet());
    }

    @Test
    void setStatistic() {
        Study s = new Study();
        s.setStatistic(9);
        assertEquals(9, s.getStatistic());
    }

    @Test
    void setUser() {
        User u = new User();
        Study s = new Study();
        s.setUser(u);
        assertEquals(u, s.getUser());
    }

    @Test
    void setFlashcardSet() {
        FlashcardSet fs = new FlashcardSet();
        Study s = new Study();
        s.setFlashcardSet(fs);
        assertEquals(fs, s.getFlashcardSet());
    }

    @Test
    void testToString() {
        Study s = new Study();
        User u = new User();
        FlashcardSet fs = new FlashcardSet();

        setId(s, "studyId", 7);
        setId(u, "userId", 3);
        setId(fs, "flashcardSetId", 15);

        s.setStatistic(12);
        s.setUser(u);
        s.setFlashcardSet(fs);

        String str = s.toString();

        assertTrue(str.contains("studyId=7"));
        assertTrue(str.contains("statistic=12"));
        assertTrue(str.contains("userId=3"));
        assertTrue(str.contains("flashcardSetId=15"));
    }
}
