package model.dao;

import model.entity.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuizDaoTest {

    private UserDao userDao;
    private QuizDao quizDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        quizDao = new QuizDao();
    }

    private User newUser() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        User u = new User();
        u.setFirstName("Test");
        u.setLastName("User");
        u.setEmail("quiz+" + uid + "@test.com");
        u.setPassword("password123");
        u.setRole(1);
        return u;
    }

    private Quiz newQuiz(User user, int noOfQuestions) {
        Quiz q = new Quiz();
        q.setNoOfQuestions(noOfQuestions);
        q.setUser(user);
        return q;
    }

    @Test
    void crudQuiz() {
        User user = newUser();
        userDao.persist(user);

        Quiz q = newQuiz(user, 10);
        quizDao.persist(q);

        Integer id = q.getQuizId();

        Quiz found = quizDao.find(id);
        assertNotNull(found);
        assertEquals(10, found.getNoOfQuestions());

        found.setNoOfQuestions(15);
        quizDao.update(found);

        Quiz updated = quizDao.find(id);
        assertEquals(15, updated.getNoOfQuestions());

        quizDao.delete(updated);
        assertNull(quizDao.find(id));

        userDao.delete(user);
    }

    @Test
    void queryQuiz() {
        User user = newUser();
        userDao.persist(user);

        Quiz q = newQuiz(user, 20);
        quizDao.persist(q);

        List<Quiz> byUser = quizDao.findByUserId(user.getUserId());
        assertTrue(byUser.stream().anyMatch(x ->
                x.getQuizId().equals(q.getQuizId()) &&
                        x.getNoOfQuestions().equals(20)
        ));

        assertTrue(quizDao.existsByUserAndQuestionCount(user.getUserId(), 20));
        assertFalse(quizDao.existsByUserAndQuestionCount(user.getUserId(), 99));

        List<Quiz> all = quizDao.findAll();
        assertTrue(all.stream().anyMatch(x -> x.getQuizId().equals(q.getQuizId())));

        quizDao.delete(q);
        assertFalse(quizDao.existsByUserAndQuestionCount(user.getUserId(), 20));

        userDao.delete(user);
    }

    @Test
    void persist_invalidQuizRollsBackAndThrows() {
        Quiz invalid = new Quiz();
        invalid.setNoOfQuestions(10);
        invalid.setUser(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> quizDao.persist(invalid));
        assertNotNull(exception);
    }

    @Test
    void update_invalidQuizRollsBackAndLeavesStoredRecordUntouched() {
        User user = newUser();
        userDao.persist(user);

        Quiz quiz = newQuiz(user, 12);
        quizDao.persist(quiz);
        Integer quizId = quiz.getQuizId();

        quiz.setUser(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> quizDao.update(quiz));
        assertNotNull(exception);

        Quiz reloaded = quizDao.find(quizId);
        assertNotNull(reloaded);
        assertEquals(12, reloaded.getNoOfQuestions());
        assertNotNull(reloaded.getUser());

        quizDao.delete(reloaded);
        userDao.delete(user);
    }

    @Test
    void delete_invalidTransientQuizRollsBackAndThrows() {
        Quiz invalid = new Quiz();
        invalid.setNoOfQuestions(5);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> quizDao.delete(invalid));
        assertNotNull(exception);
    }

    @AfterEach
    void cleanupTestData() {

        QuizDetailsDao qdDao = new QuizDetailsDao();
        QuizDao localQuizDao = new QuizDao();
        FlashcardDao fDao = new FlashcardDao();
        StudyDao studyDao = new StudyDao();
        FlashcardSetDao fsDao = new FlashcardSetDao();
        ClassDetailsDao cdDao = new ClassDetailsDao();
        ClassModelDao classModelDao = new ClassModelDao();
        UserDao localUserDao = new UserDao();

        // 1) Delete quiz_details created by test
        for (QuizDetails qd : qdDao.findAll()) {
            if (qd.getFlashcard().getTerm().startsWith("Term-") ||
                    qd.getFlashcard().getTerm().startsWith("TestTerm-") ||
                    qd.getFlashcard().getTerm().startsWith("CreatorTerm-")) {
                qdDao.delete(qd);
            }
        }

        // 2) Delete quiz created by test
        for (Quiz q : localQuizDao.findAll()) {
            if (q.getUser().getEmail().startsWith("quiz+")) {
                localQuizDao.delete(q);
            }
        }

        // 3) Delete flashcard created by test
        for (Flashcard f : fDao.findAll()) {
            if (f.getTerm().startsWith("Term-") ||
                    f.getTerm().startsWith("TestTerm-") ||
                    f.getTerm().startsWith("CreatorTerm-")) {
                fDao.delete(f);
            }
        }

        // 4) Delete study created by test
        for (Study s : studyDao.findAll()) {
            if (s.getFlashcardSet().getSubject().startsWith("Subject-")) {
                studyDao.delete(s);
            }
        }

        // 5) Delete flashcardset created by test
        for (FlashcardSet fs : fsDao.findAll()) {
            if (fs.getSubject().startsWith("Subject-")) {
                fsDao.delete(fs);
            }
        }

        // 6) Delete classdetails created by test
        for (ClassDetails cd : cdDao.findAll()) {
            if (cd.getClassModel().getClassName().startsWith("Class-")) {
                cdDao.delete(cd);
            }
        }

        // 7) Delete classmodel created by test
        for (ClassModel c : classModelDao.findAll()) {
            if (c.getClassName().startsWith("Class-")) {
                classModelDao.delete(c);
            }
        }

        // 8) Delete ONLY test users
        for (User u : localUserDao.findAll()) {
            String email = u.getEmail();
            if (email.startsWith("teacher+") ||
                    email.startsWith("cardcreator+") ||
                    email.startsWith("creator+") ||
                    email.startsWith("student+") ||
                    email.startsWith("set+") ||
                    email.startsWith("quiz+") ||
                    email.startsWith("flashcard+") ||
                    email.startsWith("test+")) {
                localUserDao.delete(u);
            }
        }
    }

}
