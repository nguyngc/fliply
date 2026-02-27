package model.dao;

import model.entity.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardSetDaoTest {

    private UserDao userDao;
    private ClassModelDao classDao;
    private FlashcardSetDao setDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        classDao = new ClassModelDao();
        setDao = new FlashcardSetDao();
    }

    private User newUser() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        User u = new User();
        u.setFirstName("Test");
        u.setLastName("User");
        u.setEmail("set+" + uid + "@test.com");
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

    private FlashcardSet newSet(ClassModel clazz, String subject) {
        FlashcardSet fs = new FlashcardSet();
        fs.setSubject(subject);
        fs.setClassModel(clazz);
        return fs;
    }

    @Test
    void crudFlashcardSet() {
        User teacher = newUser();
        userDao.persist(teacher);

        ClassModel c = newClass(teacher);
        classDao.persist(c);

        String subject = "Math-" + UUID.randomUUID().toString().substring(0, 6);
        FlashcardSet fs = newSet(c, subject);
        setDao.persist(fs);

        Integer id = fs.getFlashcardSetId();

        FlashcardSet found = setDao.find(id);
        assertNotNull(found);
        assertEquals(subject, found.getSubject());

        found.setSubject("UpdatedSubject");
        setDao.update(found);

        FlashcardSet updated = setDao.find(id);
        assertEquals("UpdatedSubject", updated.getSubject());

        setDao.delete(updated);
        assertNull(setDao.find(id));

        classDao.delete(c);
        userDao.delete(teacher);
    }

    @Test
    void queryFlashcardSet() {
        User teacher = newUser();
        userDao.persist(teacher);

        ClassModel c = newClass(teacher);
        classDao.persist(c);

        String subject = "Science-" + UUID.randomUUID().toString().substring(0, 6);
        FlashcardSet fs = newSet(c, subject);
        setDao.persist(fs);

        List<FlashcardSet> byClass = setDao.findByClassId(c.getClassId());
        assertTrue(byClass.stream().anyMatch(x ->
                x.getFlashcardSetId().equals(fs.getFlashcardSetId())
        ));

        assertTrue(setDao.existsBySubjectInClass(subject, c.getClassId()));

        List<FlashcardSet> all = setDao.findAll();
        assertTrue(all.stream().anyMatch(x -> x.getFlashcardSetId().equals(fs.getFlashcardSetId())));

        setDao.delete(fs);
        assertFalse(setDao.existsBySubjectInClass(subject, c.getClassId()));

        classDao.delete(c);
        userDao.delete(teacher);
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
                    email.startsWith("set+") ||
                    email.startsWith("teacher+") ||
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
            if (fs != null && (
                    fs.getSubject().startsWith("Math-") ||
                            fs.getSubject().startsWith("Science-") ||
                            fs.getSubject().startsWith("Subject-")
            )) {
                studyDao.delete(s);
            }
        }

        // 5) Delete flashcardset created by test
        for (FlashcardSet fs : fsDao.findAll()) {
            if (fs.getSubject().startsWith("Math-") ||
                    fs.getSubject().startsWith("Science-") ||
                    fs.getSubject().startsWith("Subject-")) {
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
                    email.startsWith("student+") ||
                    email.startsWith("test+")) {
                userDao.delete(u);
            }
        }
    }

}
