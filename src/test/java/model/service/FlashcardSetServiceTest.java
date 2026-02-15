package model.service;

import model.dao.ClassModelDao;
import model.dao.FlashcardSetDao;
import model.dao.UserDao;
import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.entity.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardSetServiceTest {

    private FlashcardSetService setService;
    private UserDao userDao;
    private ClassModelDao classDao;
    private FlashcardSetDao setDao;

    @BeforeEach
    void setUp() {
        setService = new FlashcardSetService();
        userDao = new UserDao();
        classDao = new ClassModelDao();
        setDao = new FlashcardSetDao();
    }

    private User newTeacher() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        User t = new User();
        t.setFirstName("Teacher");
        t.setLastName("User");
        t.setEmail("teacher+" + uid + "@test.com");
        t.setPassword("password123");
        t.setRole(1);
        return t;
    }

    private ClassModel newClass(User teacher) {
        ClassModel c = new ClassModel();
        c.setClassName("Class-" + UUID.randomUUID().toString().substring(0, 6));
        c.setTeacher(teacher);
        return c;
    }

    @Test
    void createSet_andGetByClass() {
        User teacher = newTeacher();
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        String subject = "Subject-" + UUID.randomUUID().toString().substring(0, 6);
        FlashcardSet fs = setService.createSet(subject, clazz);

        assertNotNull(fs);
        assertNotNull(fs.getFlashcardSetId());
        assertEquals(subject, fs.getSubject());
        assertEquals(clazz.getClassId(), fs.getClassModel().getClassId());

        List<FlashcardSet> sets = setService.getSetsByClass(clazz.getClassId());
        assertTrue(sets.stream().anyMatch(x -> x.getFlashcardSetId().equals(fs.getFlashcardSetId())));

        setDao.delete(fs);
        classDao.delete(clazz);
        userDao.delete(teacher);
    }
}

