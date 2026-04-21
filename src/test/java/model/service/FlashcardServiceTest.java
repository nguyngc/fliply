package model.service;

import model.dao.ClassModelDao;
import model.dao.FlashcardDao;
import model.dao.FlashcardSetDao;
import model.dao.QuizDao;
import model.dao.QuizDetailsDao;
import model.dao.UserDao;
import model.entity.ClassModel;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.entity.Quiz;
import model.entity.QuizDetails;
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
    private QuizDao quizDao;
    private QuizDetailsDao quizDetailsDao;

    @BeforeEach
    void setUp() {
        flashcardService = new FlashcardService();
        userDao = new UserDao();
        classDao = new ClassModelDao();
        setDao = new FlashcardSetDao();
        flashcardDao = new FlashcardDao();
        quizDao = new QuizDao();
        quizDetailsDao = new QuizDetailsDao();
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

    @Test
    void update_delete_save_and_getByUser() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        FlashcardSet fs = newSet(clazz);
        setDao.persist(fs);

        User creator = newUser("Creator");
        userDao.persist(creator);

        Flashcard card = flashcardService.createFlashcard("Term-A", "Def-A", fs, creator);
        assertNotNull(card);

        card.setDefinition("Def-Updated");
        flashcardService.update(card);

        Flashcard reloaded = flashcardDao.find(card.getFlashcardId());
        assertEquals("Def-Updated", reloaded.getDefinition());

        List<Flashcard> byUser = flashcardService.getFlashcardsByUser(creator.getUserId());
        assertTrue(byUser.stream().anyMatch(x -> x.getFlashcardId().equals(card.getFlashcardId())));

        Flashcard saved = new Flashcard();
        saved.setTerm("Saved-Term");
        saved.setDefinition("Saved-Def");
        saved.setFlashcardSet(fs);
        saved.setUser(creator);
        flashcardService.save(saved);
        assertNotNull(saved.getFlashcardId());

        flashcardService.delete(saved);
        flashcardService.delete(reloaded);

        setDao.delete(fs);
        classDao.delete(clazz);
        userDao.delete(creator);
        userDao.delete(teacher);
    }

    @Test
    void delete_removesQuizDetailsBeforeDeletingFlashcard() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        FlashcardSet fs = newSet(clazz);
        setDao.persist(fs);

        Flashcard card = flashcardService.createFlashcard("Term-Q", "Def-Q", fs, teacher);
        assertNotNull(card);

        Quiz quiz = new Quiz();
        quiz.setUser(teacher);
        quiz.setNoOfQuestions(1);
        quizDao.persist(quiz);

        QuizDetails details = new QuizDetails(quiz, card);
        quizDetailsDao.persist(details);
        assertFalse(quizDetailsDao.findByFlashcardId(card.getFlashcardId()).isEmpty());

        flashcardService.delete(card);

        assertNull(flashcardDao.find(card.getFlashcardId()));
        assertTrue(quizDetailsDao.findByFlashcardId(card.getFlashcardId()).isEmpty());

        quizDao.delete(quiz);
        setDao.delete(fs);
        classDao.delete(clazz);
        userDao.delete(teacher);
    }
}

