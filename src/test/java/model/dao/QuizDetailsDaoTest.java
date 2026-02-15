package model.dao;

import model.entity.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuizDetailsDaoTest {

    private UserDao userDao;
    private ClassModelDao classDao;
    private FlashcardSetDao setDao;
    private FlashcardDao flashcardDao;
    private QuizDao quizDao;
    private QuizDetailsDao quizDetailsDao;

    @BeforeEach
    void setUp() {
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

    private Flashcard newFlashcard(FlashcardSet set, User user, String term) {
        Flashcard f = new Flashcard();
        f.setTerm(term);
        f.setDefinition("Definition of " + term);
        f.setFlashcardSet(set);
        f.setUser(user);
        return f;
    }

    private Quiz newQuiz(User user, int noOfQuestions) {
        Quiz q = new Quiz();
        q.setNoOfQuestions(noOfQuestions);
        q.setUser(user);
        return q;
    }

    private QuizDetails newQuizDetails(Quiz quiz, Flashcard flashcard) {
        QuizDetails qd = new QuizDetails();
        qd.setQuiz(quiz);
        qd.setFlashcard(flashcard);
        return qd;
    }

    @Test
    void crudQuizDetails() {
        // setup teacher and class
        User teacher = newUser("Teacher");
        userDao.persist(teacher);
        assertNotNull(teacher.getUserId());

        ClassModel c = newClass(teacher);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        // setup flashcard set
        FlashcardSet fs = newSet(c);
        setDao.persist(fs);
        assertNotNull(fs.getFlashcardSetId());

        // setup flashcard
        User cardCreator = newUser("CardCreator");
        userDao.persist(cardCreator);
        String term = "Term-" + UUID.randomUUID().toString().substring(0, 6);
        Flashcard f = newFlashcard(fs, cardCreator, term);
        flashcardDao.persist(f);
        assertNotNull(f.getFlashcardId());

        // setup quiz
        User quizTaker = newUser("QuizTaker");
        userDao.persist(quizTaker);
        Quiz q = newQuiz(quizTaker, 5);
        quizDao.persist(q);
        assertNotNull(q.getQuizId());

        // CREATE
        QuizDetails qd = newQuizDetails(q, f);
        quizDetailsDao.persist(qd);
        assertNotNull(qd.getQuizDetailsId());

        Integer id = qd.getQuizDetailsId();

        // READ
        QuizDetails found = quizDetailsDao.find(id);
        assertNotNull(found);
        assertEquals(q.getQuizId(), found.getQuiz().getQuizId());
        assertEquals(f.getFlashcardId(), found.getFlashcard().getFlashcardId());

        // DELETE
        quizDetailsDao.delete(found);
        assertNull(quizDetailsDao.find(id));

        // cleanup
        quizDao.delete(q);
        flashcardDao.delete(f);
        setDao.delete(fs);
        classDao.delete(c);
        userDao.delete(quizTaker);
        userDao.delete(cardCreator);
        userDao.delete(teacher);
    }

    @Test
    void queryQuizDetails() {
        // setup teacher and class
        User teacher = newUser("Teacher");
        userDao.persist(teacher);
        assertNotNull(teacher.getUserId());

        ClassModel c = newClass(teacher);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        // setup flashcard set
        FlashcardSet fs = newSet(c);
        setDao.persist(fs);
        assertNotNull(fs.getFlashcardSetId());

        // setup flashcard
        User cardCreator = newUser("CardCreator");
        userDao.persist(cardCreator);
        String term = "QueryTerm-" + UUID.randomUUID().toString().substring(0, 6);
        Flashcard f = newFlashcard(fs, cardCreator, term);
        flashcardDao.persist(f);
        assertNotNull(f.getFlashcardId());

        // setup quiz
        User quizTaker = newUser("QuizTaker");
        userDao.persist(quizTaker);
        Quiz q = newQuiz(quizTaker, 10);
        quizDao.persist(q);
        assertNotNull(q.getQuizId());

        // create quiz details
        QuizDetails qd = newQuizDetails(q, f);
        quizDetailsDao.persist(qd);
        assertNotNull(qd.getQuizDetailsId());

        // findByQuizId
        List<QuizDetails> byQuiz = quizDetailsDao.findByQuizId(q.getQuizId());
        assertTrue(byQuiz.stream().anyMatch(x ->
                x.getQuizDetailsId().equals(qd.getQuizDetailsId()) &&
                        x.getFlashcard().getFlashcardId().equals(f.getFlashcardId())
        ));

        // exists
        assertTrue(quizDetailsDao.exists(q.getQuizId(), f.getFlashcardId()));
        assertFalse(quizDetailsDao.exists(q.getQuizId(), 999999));

        // findAll
        List<QuizDetails> all = quizDetailsDao.findAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(x -> x.getQuizDetailsId().equals(qd.getQuizDetailsId())));

        // cleanup
        quizDetailsDao.delete(qd);
        assertFalse(quizDetailsDao.exists(q.getQuizId(), f.getFlashcardId()));

        quizDao.delete(q);
        flashcardDao.delete(f);
        setDao.delete(fs);
        classDao.delete(c);
        userDao.delete(quizTaker);
        userDao.delete(cardCreator);
        userDao.delete(teacher);
    }
}

