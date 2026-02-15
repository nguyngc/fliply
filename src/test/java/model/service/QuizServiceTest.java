package model.service;

import model.dao.*;
import model.entity.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuizServiceTest {

    private QuizService quizService;
    private UserDao userDao;
    private ClassModelDao classDao;
    private ClassDetailsDao classDetailsDao;
    private FlashcardSetDao setDao;
    private FlashcardDao flashcardDao;
    private QuizDao quizDao;
    private QuizDetailsDao quizDetailsDao;

    @BeforeEach
    void setUp() {
        quizService = new QuizService();
        userDao = new UserDao();
        classDao = new ClassModelDao();
        classDetailsDao = new ClassDetailsDao();
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

    private Flashcard newFlashcard(FlashcardSet set, User user, String term) {
        Flashcard f = new Flashcard();
        f.setTerm(term);
        f.setDefinition("Def-" + term);
        f.setFlashcardSet(set);
        f.setUser(user);
        return f;
    }

    @Test
    void generateQuiz_andBuildQuestions() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        FlashcardSet fs = newSet(clazz);
        setDao.persist(fs);

        User creator = newUser("Creator");
        userDao.persist(creator);

        // create flashcards in class
        Flashcard f1 = newFlashcard(fs, creator, "T1");
        Flashcard f2 = newFlashcard(fs, creator, "T2");
        Flashcard f3 = newFlashcard(fs, creator, "T3");
        flashcardDao.persist(f1);
        flashcardDao.persist(f2);
        flashcardDao.persist(f3);

        User student = newUser("Student");
        userDao.persist(student);

        ClassDetails enrollment = new ClassDetails();
        enrollment.setUser(student);
        enrollment.setClassModel(clazz);
        classDetailsDao.persist(enrollment);

        int requested = 2;
        Quiz quiz = quizService.generateQuiz(student, requested);
        assertNotNull(quiz);
        assertNotNull(quiz.getQuizId());
        assertTrue(quiz.getNoOfQuestions() <= requested);

        List<QuizDetails> details = quizDetailsDao.findByQuizId(quiz.getQuizId());
        assertEquals(quiz.getNoOfQuestions(), details.size());

        List<QuizService.QuizQuestion> questions = quizService.buildQuizQuestions(quiz.getQuizId(), student.getUserId());
        assertEquals(details.size(), questions.size());
        for (QuizService.QuizQuestion qq : questions) {
            assertNotNull(qq.getCorrectAnswer());
            assertTrue(qq.getOptions().contains(qq.getCorrectAnswer()));
            assertFalse(qq.getOptions().isEmpty());
            assertTrue(qq.getOptions().size() <= 4);
        }

        // cleanup
        for (QuizDetails d : details) {
            quizDetailsDao.delete(d);
        }
        quizDao.delete(quiz);
        flashcardDao.delete(f1);
        flashcardDao.delete(f2);
        flashcardDao.delete(f3);
        classDetailsDao.delete(enrollment);
        setDao.delete(fs);
        classDao.delete(clazz);
        userDao.delete(student);
        userDao.delete(creator);
        userDao.delete(teacher);
    }

    @Test
    void generateQuiz_noAvailableFlashcardsReturnsNull() {
        User user = newUser("Lonely");
        userDao.persist(user);

        Quiz quiz = quizService.generateQuiz(user, 5);
        assertNull(quiz);

        userDao.delete(user);
    }
}
