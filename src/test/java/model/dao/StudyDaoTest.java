package model.dao;

import model.entity.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudyDaoTest {

    private UserDao userDao;
    private ClassModelDao classDao;
    private FlashcardSetDao setDao;
    private StudyDao studyDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        classDao = new ClassModelDao();
        setDao = new FlashcardSetDao();
        studyDao = new StudyDao();
    }

    private User newUser() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        User u = new User();
        u.setFirstName("Test");
        u.setLastName("User");
        u.setEmail("study+" + uid + "@test.com");
        u.setPassword("password123");
        u.setRole(0);
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

    private Study newStudy(User user, FlashcardSet set, int statistic) {
        Study s = new Study();
        s.setUser(user);
        s.setFlashcardSet(set);
        s.setStatistic(statistic);
        return s;
    }

    @Test
    void crudStudy() {
        User u = newUser();
        userDao.persist(u);

        ClassModel c = newClass(u);
        classDao.persist(c);

        FlashcardSet fs = newSet(c);
        setDao.persist(fs);

        Study s = newStudy(u, fs, 5);
        studyDao.persist(s);

        Integer id = s.getStudyId();

        Study found = studyDao.find(id);
        assertNotNull(found);
        assertEquals(5, found.getStatistic());

        found.setStatistic(9);
        studyDao.update(found);

        Study updated = studyDao.find(id);
        assertEquals(9, updated.getStatistic());

        studyDao.delete(updated);
        assertNull(studyDao.find(id));
    }

    @Test
    void queryStudy() {
        User u = newUser();
        userDao.persist(u);

        ClassModel c = newClass(u);
        classDao.persist(c);

        FlashcardSet fs = newSet(c);
        setDao.persist(fs);

        Study s = newStudy(u, fs, 3);
        studyDao.persist(s);

        assertTrue(studyDao.existsByUserAndSet(u.getUserId(), fs.getFlashcardSetId()));

        List<Study> byUser = studyDao.findByUserId(u.getUserId());
        assertTrue(byUser.stream().anyMatch(x ->
                x.getUser().getUserId().equals(u.getUserId()) &&
                        x.getFlashcardSet().getFlashcardSetId().equals(fs.getFlashcardSetId())
        ));

        List<Study> bySet = studyDao.findByFlashcardSetId(fs.getFlashcardSetId());
        assertTrue(bySet.stream().anyMatch(x ->
                x.getUser().getUserId().equals(u.getUserId()) &&
                        x.getFlashcardSet().getFlashcardSetId().equals(fs.getFlashcardSetId())
        ));

        studyDao.delete(s);
        assertFalse(studyDao.existsByUserAndSet(u.getUserId(), fs.getFlashcardSetId()));
    }

    @AfterEach
    void cleanupTestData() {

        QuizDetailsDao qdDao = new QuizDetailsDao();
        QuizDao quizDao = new QuizDao();
        FlashcardDao fDao = new FlashcardDao();
        StudyDao studyDao = new StudyDao();
        FlashcardSetDao fsDao = new FlashcardSetDao();
        ClassDetailsDao cdDao = new ClassDetailsDao();
        ClassModelDao classDao = new ClassModelDao();
        UserDao userDao = new UserDao();

        // 1) Delete quiz_details created by test
        for (QuizDetails qd : qdDao.findAll()) {
            Flashcard f = qd.getFlashcard();
            if (f != null) {
                String term = f.getTerm();
                if (term.startsWith("Term-") ||
                        term.startsWith("TestTerm-") ||
                        term.startsWith("CreatorTerm-")) {
                    qdDao.delete(qd);
                }
            }
        }

        // 2) Delete quiz created by test
        for (Quiz q : quizDao.findAll()) {
            String email = q.getUser().getEmail();
            if (email.startsWith("quiz+") ||
                    email.startsWith("teacher+") ||
                    email.startsWith("cardcreator+") ||
                    email.startsWith("creator+") ||
                    email.startsWith("student+")) {
                quizDao.delete(q);
            }
        }

        // 3) Delete flashcard created by test
        for (Flashcard f : fDao.findAll()) {
            String term = f.getTerm();
            if (term.startsWith("Term-") ||
                    term.startsWith("TestTerm-") ||
                    term.startsWith("CreatorTerm-")) {
                fDao.delete(f);
            }
        }

        // 4) Delete study created by test
        for (Study s : studyDao.findAll()) {
            FlashcardSet fs = s.getFlashcardSet();
            if (fs != null && fs.getSubject().startsWith("Subject-")) {
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
            ClassModel c = cd.getClassModel();
            if (c != null && c.getClassName().startsWith("Class-")) {
                cdDao.delete(cd);
            }
        }

        // 7) Delete classmodel created by test
        for (ClassModel c : classDao.findAll()) {
            if (c.getClassName().startsWith("Class-")) {
                classDao.delete(c);
            }
        }

        // 8) Delete ONLY test users
        for (User u : userDao.findAll()) {
            String email = u.getEmail();
            if (email.startsWith("set+") ||
                    email.startsWith("teacher+") ||
                    email.startsWith("cardcreator+") ||
                    email.startsWith("creator+") ||
                    email.startsWith("student+") ||
                    email.startsWith("quiz+") ||
                    email.startsWith("study+") ||
                    email.startsWith("test+")) {
                userDao.delete(u);
            }
        }
    }

}
