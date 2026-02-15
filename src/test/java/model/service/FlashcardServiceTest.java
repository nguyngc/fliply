package model.service;

import model.dao.ClassModelDao;
import model.dao.FlashcardDao;
import model.dao.FlashcardSetDao;
import model.dao.UserDao;
import model.entity.ClassModel;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.entity.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardServiceTest {

    private FlashcardService flashcardService;
    private UserDao userDao;
    private ClassModelDao classDao;
    private FlashcardSetDao setDao;
    private FlashcardDao flashcardDao;

    @BeforeEach
    void setUp() {
        flashcardService = new FlashcardService();
        userDao = new UserDao();
        classDao = new ClassModelDao();
        setDao = new FlashcardSetDao();
        flashcardDao = new FlashcardDao();
    }

    private User newUser(String prefix) {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        User u = new User();
        u.setFirstName(prefix);
        u.setLastName("User");
        u.setEmail(prefix.toLowerCase() + "+" + uid + "@test.com");
        u.setPassword("password123");
        u.setRole(1);
        return u;
    }

    private ClassModel newClass(User teacher) {
        ClassModel c = new ClassModel();
        c.setClassName("Class-" + UUID.randomUUID().toString().substring(0, 6));
        c.setTeacher(teacher);
        return c;
    }

    private FlashcardSet newSet(ClassModel clazz) {
        FlashcardSet fs = new FlashcardSet();
        fs.setSubject("Subject-" + UUID.randomUUID().toString().substring(0, 6));
        fs.setClassModel(clazz);
        return fs;
    }

    @Test
    void createFlashcard_andPreventDuplicate() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        FlashcardSet fs = newSet(clazz);
        setDao.persist(fs);

        User creator = newUser("Creator");
        userDao.persist(creator);

        String term = "Term-" + UUID.randomUUID().toString().substring(0, 6);
        Flashcard first = flashcardService.createFlashcard(term, "Def1", fs, creator);
        assertNotNull(first);
        assertNotNull(first.getFlashcardId());

        Flashcard dup = flashcardService.createFlashcard(term, "Def1 again", fs, creator);
        assertNull(dup);

        List<Flashcard> bySet = flashcardService.getFlashcardsBySet(fs.getFlashcardSetId());
        assertTrue(bySet.stream().anyMatch(x -> x.getFlashcardId().equals(first.getFlashcardId())));

        flashcardDao.delete(first);
        setDao.delete(fs);
        classDao.delete(clazz);
        userDao.delete(creator);
        userDao.delete(teacher);
    }
}

