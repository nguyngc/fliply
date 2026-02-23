package model.service;

import model.dao.*;
import model.entity.*;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudyServiceTest {

    private UserDao userDao;
    private ClassModelDao classDao;
    private FlashcardSetDao setDao;
    private FlashcardDao flashcardDao;
    private StudyDao studyDao;
    private StudyService service;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        classDao = new ClassModelDao();
        setDao = new FlashcardSetDao();
        flashcardDao = new FlashcardDao();
        studyDao = new StudyDao();
        service = new StudyService();
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

    private FlashcardSet newSet(ClassModel c) {
        FlashcardSet fs = new FlashcardSet();
        fs.setSubject("Set-" + UUID.randomUUID().toString().substring(0, 5));
        fs.setClassModel(c);
        return fs;
    }

    private Flashcard newCard(String term, FlashcardSet set) {
        Flashcard f = new Flashcard();
        f.setTerm(term);
        f.setDefinition("Def-" + term);
        f.setFlashcardSet(set);
        return f;
    }

    @Test
    void getProgressPercent() {
        // setup teacher + class
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel c = newClass(teacher);
        classDao.persist(c);

        // setup student
        User student = newUser("Student");
        userDao.persist(student);

        // setup flashcard set
        FlashcardSet set = newSet(c);
        setDao.persist(set);

        // add flashcards
        Flashcard f1 = newCard("A", set);
        Flashcard f2 = newCard("B", set);
        Flashcard f3 = newCard("C", set);
        flashcardDao.persist(f1);
        flashcardDao.persist(f2);
        flashcardDao.persist(f3);

        // create study record: learned 2/3
        Study study = new Study(2, student, set);
        studyDao.persist(study);

        double percent = service.getProgressPercent(student, set);
        assertEquals(2.0 / 3.0, percent, 0.0001);

        // cleanup
        studyDao.delete(study);
        flashcardDao.delete(f1);
        flashcardDao.delete(f2);
        flashcardDao.delete(f3);
        setDao.delete(set);
        classDao.delete(c);
        userDao.delete(student);
        userDao.delete(teacher);
    }

    @Test
    void getClassProgress() {
        // setup teacher + class
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel c = newClass(teacher);
        classDao.persist(c);

        // setup student
        User student = newUser("Student");
        userDao.persist(student);

        // SET 1: 3 cards, learned 2
        FlashcardSet set1 = newSet(c);
        setDao.persist(set1);

        Flashcard s1c1 = newCard("A", set1);
        Flashcard s1c2 = newCard("B", set1);
        Flashcard s1c3 = newCard("C", set1);
        flashcardDao.persist(s1c1);
        flashcardDao.persist(s1c2);
        flashcardDao.persist(s1c3);

        Study st1 = new Study(2, student, set1);
        studyDao.persist(st1);

        // SET 2: 2 cards, learned 1
        FlashcardSet set2 = newSet(c);
        setDao.persist(set2);

        Flashcard s2c1 = newCard("X", set2);
        Flashcard s2c2 = newCard("Y", set2);
        flashcardDao.persist(s2c1);
        flashcardDao.persist(s2c2);

        Study st2 = new Study(1, student, set2);
        studyDao.persist(st2);

        // total = 3 + 2 = 5 cards
        // learned = 2 + 1 = 3 cards
        double progress = service.getClassProgress(student, c);
        assertEquals(3.0 / 5.0, progress, 0.0001);

        // cleanup
        studyDao.delete(st1);
        studyDao.delete(st2);

        flashcardDao.delete(s1c1);
        flashcardDao.delete(s1c2);
        flashcardDao.delete(s1c3);
        flashcardDao.delete(s2c1);
        flashcardDao.delete(s2c2);

        setDao.delete(set1);
        setDao.delete(set2);

        classDao.delete(c);
        userDao.delete(student);
        userDao.delete(teacher);
    }
}
