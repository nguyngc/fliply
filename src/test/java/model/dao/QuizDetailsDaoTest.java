package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import model.entity.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    void findByFlashcardId_andDeleteByFlashcardId() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel c = newClass(teacher);
        classDao.persist(c);

        FlashcardSet fs = newSet(c);
        setDao.persist(fs);

        User cardCreator = newUser("CardCreator");
        userDao.persist(cardCreator);
        Flashcard f = newFlashcard(fs, cardCreator, "DeleteByFlash-" + UUID.randomUUID().toString().substring(0, 6));
        flashcardDao.persist(f);

        User quizTaker = newUser("QuizTaker");
        userDao.persist(quizTaker);
        Quiz q1 = newQuiz(quizTaker, 3);
        Quiz q2 = newQuiz(quizTaker, 4);
        quizDao.persist(q1);
        quizDao.persist(q2);

        QuizDetails qd1 = newQuizDetails(q1, f);
        QuizDetails qd2 = newQuizDetails(q2, f);
        quizDetailsDao.persist(qd1);
        quizDetailsDao.persist(qd2);

        List<QuizDetails> byFlashcard = quizDetailsDao.findByFlashcardId(f.getFlashcardId());
        assertEquals(2, byFlashcard.size());
        assertTrue(byFlashcard.stream().allMatch(qd -> qd.getFlashcard().getFlashcardId().equals(f.getFlashcardId())));

        quizDetailsDao.deleteByFlashcardId(f.getFlashcardId());

        assertTrue(quizDetailsDao.findByFlashcardId(f.getFlashcardId()).isEmpty());
        assertFalse(quizDetailsDao.exists(q1.getQuizId(), f.getFlashcardId()));
        assertFalse(quizDetailsDao.exists(q2.getQuizId(), f.getFlashcardId()));

        quizDao.delete(q1);
        quizDao.delete(q2);
        flashcardDao.delete(f);
        setDao.delete(fs);
        classDao.delete(c);
        userDao.delete(quizTaker);
        userDao.delete(cardCreator);
        userDao.delete(teacher);
    }

    @Test
    void emptyQueriesReturnNoMatches() {
        assertTrue(quizDetailsDao.findByQuizId(999999).isEmpty());
        assertFalse(quizDetailsDao.exists(999999, 999999));
    }

    @Test
    void persist_invalidQuizDetailsRollsBackAndThrows() {
        QuizDetails invalid = new QuizDetails();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> quizDetailsDao.persist(invalid));
        assertNotNull(exception);
    }

    @Test
    void delete_invalidTransientQuizDetailsRollsBackAndThrows() {
        QuizDetails invalid = new QuizDetails();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> quizDetailsDao.delete(invalid));
        assertNotNull(exception);
    }

    @Test
    void delete_managedQuizDetails_skipsMergeAndCommits() {
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        QuizDetails managed = new QuizDetails();
        QuizDetailsDao dao = new QuizDetailsDao() {
            @Override
            EntityManager createEntityManager() {
                return em;
            }
        };

        when(em.getTransaction()).thenReturn(tx);
        when(em.contains(managed)).thenReturn(true);

        dao.delete(managed);

        verify(tx).begin();
        verify(em, never()).merge(any());
        verify(em).remove(managed);
        verify(tx).commit();
        verify(tx, never()).rollback();
        verify(em).close();
    }

    @Test
    void deleteByFlashcardId_whenNoRowsExist_doesNotThrow() {
        assertDoesNotThrow(() -> quizDetailsDao.deleteByFlashcardId(999999));
        assertTrue(quizDetailsDao.findByFlashcardId(999999).isEmpty());
    }

    @Test
    void deleteByFlashcardId_queryFailure_rollsBackAndThrows() {
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        Query deleteQuery = mock(Query.class);
        RuntimeException failure = new RuntimeException("delete failed");
        QuizDetailsDao dao = new QuizDetailsDao() {
            @Override
            EntityManager createEntityManager() {
                return em;
            }
        };

        when(em.getTransaction()).thenReturn(tx);
        when(em.createQuery("DELETE FROM QuizDetails qd WHERE qd.flashcard.flashcardId = :fid"))
                .thenReturn(deleteQuery);
        when(deleteQuery.setParameter("fid", 77)).thenReturn(deleteQuery);
        when(deleteQuery.executeUpdate()).thenThrow(failure);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> dao.deleteByFlashcardId(77));
        assertSame(failure, thrown);

        verify(tx).begin();
        verify(tx).rollback();
        verify(tx, never()).commit();
        verify(em).close();
    }

    @SuppressWarnings("unchecked")
    @Test
    void exists_nullCount_returnsFalse() {
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        QuizDetailsDao dao = new QuizDetailsDao() {
            @Override
            EntityManager createEntityManager() {
                return em;
            }
        };

        when(em.createQuery(
                "SELECT COUNT(qd) FROM QuizDetails qd " +
                        "WHERE qd.quiz.quizId = :qid AND qd.flashcard.flashcardId = :fid",
                Long.class
        )).thenReturn(countQuery);
        when(countQuery.setParameter("qid", 13)).thenReturn(countQuery);
        when(countQuery.setParameter("fid", 29)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(null);

        assertFalse(dao.exists(13, 29));

        verify(em).close();
    }

    @AfterEach
    void cleanupTestData() {

        QuizDetailsDao qdDao = new QuizDetailsDao();
        QuizDao localQuizDao = new QuizDao();
        FlashcardDao fDao = new FlashcardDao();
        FlashcardSetDao fsDao = new FlashcardSetDao();
        ClassModelDao classModelDao = new ClassModelDao();
        UserDao localUserDao = new UserDao();

        // 1) Delete quiz_details created by test
        for (QuizDetails qd : qdDao.findAll()) {
            if (qd.getQuiz().getUser().getEmail().startsWith("quiz+")) {
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
            if (f.getTerm().startsWith("Term-") || f.getTerm().startsWith("QueryTerm-")) {
                fDao.delete(f);
            }
        }

        // 4) Delete flashcardset created by test
        for (FlashcardSet fs : fsDao.findAll()) {
            if (fs.getSubject().startsWith("Subject-")) {
                fsDao.delete(fs);
            }
        }

        // 5) Delete classmodel created by test
        for (ClassModel c : classModelDao.findAll()) {
            if (c.getClassName().startsWith("Class-")) {
                classModelDao.delete(c);
            }
        }

        // 6) Delete test users only
        for (User u : localUserDao.findAll()) {
            String email = u.getEmail();
            if (email.startsWith("teacher+") ||
                    email.startsWith("cardcreator+") ||
                    email.startsWith("quiz+") ||
                    email.startsWith("student+") ||
                    email.startsWith("set+") ||
                    email.startsWith("study+")) {
                localUserDao.delete(u);
            }
        }
    }

}
