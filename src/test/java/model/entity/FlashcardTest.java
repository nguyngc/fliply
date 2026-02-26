package model.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardTest {

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
    void getFlashcardId() {
        Flashcard f = new Flashcard();
        setId(f, "flashcardId", 10);
        assertEquals(10, f.getFlashcardId());
    }

    @Test
    void getTerm() {
        Flashcard f = new Flashcard();
        f.setTerm("Hello");
        assertEquals("Hello", f.getTerm());
    }

    @Test
    void getDefinition() {
        Flashcard f = new Flashcard();
        f.setDefinition("World");
        assertEquals("World", f.getDefinition());
    }

    @Test
    void getFlashcardSet() {
        FlashcardSet set = new FlashcardSet();
        Flashcard f = new Flashcard();
        f.setFlashcardSet(set);
        assertEquals(set, f.getFlashcardSet());
    }

    @Test
    void getUser() {
        User u = new User();
        Flashcard f = new Flashcard();
        f.setUser(u);
        assertEquals(u, f.getUser());
    }

    @Test
    void setTerm() {
        Flashcard f = new Flashcard();
        f.setTerm("TermX");
        assertEquals("TermX", f.getTerm());
    }

    @Test
    void setDefinition() {
        Flashcard f = new Flashcard();
        f.setDefinition("DefX");
        assertEquals("DefX", f.getDefinition());
    }

    @Test
    void setFlashcardSet() {
        FlashcardSet set = new FlashcardSet();
        Flashcard f = new Flashcard();
        f.setFlashcardSet(set);
        assertEquals(set, f.getFlashcardSet());
    }

    @Test
    void setUser() {
        User u = new User();
        Flashcard f = new Flashcard();
        f.setUser(u);
        assertEquals(u, f.getUser());
    }

    @Test
    void testToString() {
        Flashcard f = new Flashcard();
        FlashcardSet set = new FlashcardSet();
        User u = new User();

        setId(f, "flashcardId", 7);
        setId(set, "flashcardSetId", 20);
        setId(u, "userId", 5);

        f.setTerm("A");
        f.setDefinition("B");
        f.setFlashcardSet(set);
        f.setUser(u);

        String s = f.toString();

        assertTrue(s.contains("flashcardId=7"));
        assertTrue(s.contains("term='A'"));
        assertTrue(s.contains("definition='B'"));
        assertTrue(s.contains("flashcardSetId=20"));
        assertTrue(s.contains("userId=5"));
    }
}
