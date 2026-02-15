package model.dao;

import model.entity.Quiz;
import model.entity.User;
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
        // setup user
        User user = newUser();
        userDao.persist(user);
        assertNotNull(user.getUserId());

        // CREATE
        Quiz q = newQuiz(user, 10);
        quizDao.persist(q);
        assertNotNull(q.getQuizId());

        Integer id = q.getQuizId();

        // READ
        Quiz found = quizDao.find(id);
        assertNotNull(found);
        assertEquals(10, found.getNoOfQuestions());
        assertEquals(user.getUserId(), found.getUser().getUserId());

        // UPDATE
        found.setNoOfQuestions(15);
        quizDao.update(found);

        Quiz updated = quizDao.find(id);
        assertNotNull(updated);
        assertEquals(15, updated.getNoOfQuestions());

        // DELETE
        quizDao.delete(updated);
        assertNull(quizDao.find(id));

        // cleanup
        userDao.delete(user);
    }

    @Test
    void queryQuiz() {
        // setup user
        User user = newUser();
        userDao.persist(user);
        assertNotNull(user.getUserId());

        // create quiz
        Quiz q = newQuiz(user, 20);
        quizDao.persist(q);
        assertNotNull(q.getQuizId());

        // findByUserId
        List<Quiz> byUser = quizDao.findByUserId(user.getUserId());
        assertTrue(byUser.stream().anyMatch(x ->
                x.getQuizId().equals(q.getQuizId()) &&
                        x.getNoOfQuestions().equals(20)
        ));

        // existsByUserAndQuestionCount
        assertTrue(quizDao.existsByUserAndQuestionCount(user.getUserId(), 20));
        assertFalse(quizDao.existsByUserAndQuestionCount(user.getUserId(), 99));

        // findAll
        List<Quiz> all = quizDao.findAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(x -> x.getQuizId().equals(q.getQuizId())));

        // cleanup
        quizDao.delete(q);
        assertFalse(quizDao.existsByUserAndQuestionCount(user.getUserId(), 20));

        userDao.delete(user);
    }
}

