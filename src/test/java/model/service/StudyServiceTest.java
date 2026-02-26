package model.service;

import model.dao.StudyDao;
import model.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudyServiceTest {

    private StudyService service;
    private StudyDao mockDao;

    @BeforeEach
    void setUp() {
        service = new StudyService();

        // Mock StudyDao
        mockDao = mock(StudyDao.class);

        // Inject mock StudyService
        try {
            Field f = StudyService.class.getDeclaredField("dao");
            f.setAccessible(true);
            f.set(service, mockDao);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FlashcardSet newSet(int id, int cardCount) {
        FlashcardSet fs = new FlashcardSet();
        try {
            Field f = FlashcardSet.class.getDeclaredField("flashcardSetId");
            f.setAccessible(true);
            f.set(fs, id);

            Field cardsField = FlashcardSet.class.getDeclaredField("cards");
            cardsField.setAccessible(true);
            ArrayList<Flashcard> list = new ArrayList<>();
            for (int i = 0; i < cardCount; i++) list.add(new Flashcard());
            cardsField.set(fs, list);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return fs;
    }

    private User newUser() {
        User u = new User();
        try {
            Field f = User.class.getDeclaredField("userId");
            f.setAccessible(true);
            f.set(u, 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return u;
    }

    // ---------------- Tests ----------------

    @Test
    void testGetProgressPercent_noStudyRecord() {
        User u = newUser();
        FlashcardSet fs = newSet(10, 3);

        when(mockDao.findByStudentAndSet(1, 10)).thenReturn(null);

        assertEquals(0.0, service.getProgressPercent(u, fs));
    }

    @Test
    void testGetProgressPercent_zeroCards() {
        User u = newUser();
        FlashcardSet fs = newSet(10, 0);

        when(mockDao.findByStudentAndSet(1, 10))
                .thenReturn(new Study(5, u, fs));

        assertEquals(0.0, service.getProgressPercent(u, fs));
    }

    @Test
    void testGetProgressPercent_normalCase() {
        User u = newUser();
        FlashcardSet fs = newSet(10, 3);

        when(mockDao.findByStudentAndSet(1, 10))
                .thenReturn(new Study(2, u, fs));

        assertEquals(2.0 / 3.0, service.getProgressPercent(u, fs), 0.0001);
    }

    @Test
    void testGetClassProgress_noSets() {
        User u = newUser();
        ClassModel c = new ClassModel();

        assertEquals(0.0, service.getClassProgress(u, c));
    }

    @Test
    void testGetClassProgress_setsButNoCard() {
        User u = newUser();
        ClassModel c = new ClassModel();

        FlashcardSet fs = newSet(10, 0);
        c.getFlashcardSets().add(fs);

        when(mockDao.findByStudentAndSet(1, 10)).thenReturn(null);

        assertEquals(0.0, service.getClassProgress(u, c));
    }

    @Test
    void testGetClassProgress_mixedSets() {
        User u = newUser();
        ClassModel c = new ClassModel();

        FlashcardSet s1 = newSet(1, 3);
        FlashcardSet s2 = newSet(2, 2);

        c.getFlashcardSets().add(s1);
        c.getFlashcardSets().add(s2);

        when(mockDao.findByStudentAndSet(1, 1)).thenReturn(new Study(2, u, s1));
        when(mockDao.findByStudentAndSet(1, 2)).thenReturn(new Study(1, u, s2));

        assertEquals(3.0 / 5.0, service.getClassProgress(u, c), 0.0001);
    }

    @Test
    void testGetClassProgress_partialMissingStudy() {
        User u = newUser();
        ClassModel c = new ClassModel();

        FlashcardSet s1 = newSet(1, 3);
        FlashcardSet s2 = newSet(2, 2);

        c.getFlashcardSets().add(s1);
        c.getFlashcardSets().add(s2);

        when(mockDao.findByStudentAndSet(1, 1)).thenReturn(new Study(2, u, s1));
        when(mockDao.findByStudentAndSet(1, 2)).thenReturn(null);

        assertEquals(2.0 / 5.0, service.getClassProgress(u, c), 0.0001);
    }
}
