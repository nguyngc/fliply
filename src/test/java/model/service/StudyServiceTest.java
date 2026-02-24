package model.service;

import model.dao.*;
import model.entity.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
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

    private void initCardList(FlashcardSet set) {
        try {
            Field f = FlashcardSet.class.getDeclaredField("cards");
            f.setAccessible(true);
            f.set(set, new ArrayList<Flashcard>());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private Flashcard newCard(String term, FlashcardSet set, User user) {
        Flashcard f = new Flashcard();
        f.setTerm(term);
        f.setDefinition("Def-" + term);
        f.setFlashcardSet(set);
        f.setUser(user);
        return f;
    }

    @Test
    void getProgressPercent() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel c = newClass(teacher);
        classDao.persist(c);

        User student = newUser("Student");
        userDao.persist(student);

        FlashcardSet set = newSet(c);
        setDao.persist(set);

        // FIX: khởi tạo list cards bằng reflection
        initCardList(set);

        Flashcard f1 = newCard("A", set, student);
        Flashcard f2 = newCard("B", set, student);
        Flashcard f3 = newCard("C", set, student);

        flashcardDao.persist(f1);
        flashcardDao.persist(f2);
        flashcardDao.persist(f3);

        set.getCards().add(f1);
        set.getCards().add(f2);
        set.getCards().add(f3);

        Study study = new Study(2, student, set);
        studyDao.persist(study);

        double percent = service.getProgressPercent(student, set);
        assertEquals(2.0 / 3.0, percent, 0.0001);
    }

    @Test
    void getClassProgress() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel c = newClass(teacher);
        classDao.persist(c);

        User student = newUser("Student");
        userDao.persist(student);

        FlashcardSet set1 = newSet(c);
        setDao.persist(set1);
        initCardList(set1);

        Flashcard s1c1 = newCard("A", set1, student);
        Flashcard s1c2 = newCard("B", set1, student);
        Flashcard s1c3 = newCard("C", set1, student);

        flashcardDao.persist(s1c1);
        flashcardDao.persist(s1c2);
        flashcardDao.persist(s1c3);

        set1.getCards().add(s1c1);
        set1.getCards().add(s1c2);
        set1.getCards().add(s1c3);

        Study st1 = new Study(2, student, set1);
        studyDao.persist(st1);

        FlashcardSet set2 = newSet(c);
        setDao.persist(set2);
        initCardList(set2);

        Flashcard s2c1 = newCard("X", set2, student);
        Flashcard s2c2 = newCard("Y", set2, student);

        flashcardDao.persist(s2c1);
        flashcardDao.persist(s2c2);

        set2.getCards().add(s2c1);
        set2.getCards().add(s2c2);

        Study st2 = new Study(1, student, set2);
        studyDao.persist(st2);

        double progress = service.getClassProgress(student, c);
        assertEquals(3.0 / 5.0, progress, 0.0001);
    }
}
