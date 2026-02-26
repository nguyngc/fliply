package model.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardSetTest {

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
    void getCards() {
        FlashcardSet fs = new FlashcardSet();
        List<Flashcard> list = new ArrayList<>();
        list.add(new Flashcard());

        // set private field via reflection
        try {
            Field f = FlashcardSet.class.getDeclaredField("cards");
            f.setAccessible(true);
            f.set(fs, list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertEquals(1, fs.getCards().size());
    }

    @Test
    void getFlashcardSetId() {
        FlashcardSet fs = new FlashcardSet();
        setId(fs, "flashcardSetId", 10);
        assertEquals(10, fs.getFlashcardSetId());
    }

    @Test
    void getSubject() {
        FlashcardSet fs = new FlashcardSet();
        fs.setSubject("Math");
        assertEquals("Math", fs.getSubject());
    }

    @Test
    void getClassModel() {
        ClassModel cm = new ClassModel();
        FlashcardSet fs = new FlashcardSet();
        fs.setClassModel(cm);
        assertEquals(cm, fs.getClassModel());
    }

    @Test
    void getTotalCards() {
        FlashcardSet fs = new FlashcardSet();
        List<Flashcard> list = new ArrayList<>();
        list.add(new Flashcard());
        list.add(new Flashcard());

        try {
            Field f = FlashcardSet.class.getDeclaredField("cards");
            f.setAccessible(true);
            f.set(fs, list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertEquals(2, fs.getTotalCards().size());
    }

    @Test
    void setSubject() {
        FlashcardSet fs = new FlashcardSet();
        fs.setSubject("Science");
        assertEquals("Science", fs.getSubject());
    }

    @Test
    void setClassModel() {
        ClassModel cm = new ClassModel();
        FlashcardSet fs = new FlashcardSet();
        fs.setClassModel(cm);
        assertEquals(cm, fs.getClassModel());
    }

    @Test
    void testToString() {
        FlashcardSet fs = new FlashcardSet();
        ClassModel cm = new ClassModel();

        setId(fs, "flashcardSetId", 5);
        setId(cm, "classId", 20);

        fs.setSubject("History");
        fs.setClassModel(cm);

        String s = fs.toString();

        assertTrue(s.contains("flashcardSetId=5"));
        assertTrue(s.contains("subject='History'"));
        assertTrue(s.contains("classId=20"));
    }
}
