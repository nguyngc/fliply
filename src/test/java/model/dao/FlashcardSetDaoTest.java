package model.dao;

import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.entity.User;
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
        // setup teacher and class
        User teacher = newUser();
        userDao.persist(teacher);
        assertNotNull(teacher.getUserId());

        ClassModel c = newClass(teacher);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        // CREATE
        String subject = "Math-" + UUID.randomUUID().toString().substring(0, 6);
        FlashcardSet fs = newSet(c, subject);
        setDao.persist(fs);
        assertNotNull(fs.getFlashcardSetId());

        Integer id = fs.getFlashcardSetId();

        // READ
        FlashcardSet found = setDao.find(id);
        assertNotNull(found);
        assertEquals(subject, found.getSubject());
        assertEquals(c.getClassId(), found.getClassModel().getClassId());

        // UPDATE
        found.setSubject("UpdatedSubject");
        setDao.update(found);

        FlashcardSet updated = setDao.find(id);
        assertNotNull(updated);
        assertEquals("UpdatedSubject", updated.getSubject());

        // DELETE
        setDao.delete(updated);
        assertNull(setDao.find(id));

        // cleanup
        classDao.delete(c);
        userDao.delete(teacher);
    }

    @Test
    void queryFlashcardSet() {
        // setup teacher and class
        User teacher = newUser();
        userDao.persist(teacher);
        assertNotNull(teacher.getUserId());

        ClassModel c = newClass(teacher);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        // create flashcard set
        String subject = "Science-" + UUID.randomUUID().toString().substring(0, 6);
        FlashcardSet fs = newSet(c, subject);
        setDao.persist(fs);
        assertNotNull(fs.getFlashcardSetId());

        // findByClassId
        List<FlashcardSet> byClass = setDao.findByClassId(c.getClassId());
        assertTrue(byClass.stream().anyMatch(x ->
                x.getFlashcardSetId().equals(fs.getFlashcardSetId()) &&
                        x.getSubject().equals(subject)
        ));

        // existsBySubjectInClass
        assertTrue(setDao.existsBySubjectInClass(subject, c.getClassId()));

        // findAll
        List<FlashcardSet> all = setDao.findAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(x -> x.getFlashcardSetId().equals(fs.getFlashcardSetId())));

        // cleanup
        setDao.delete(fs);
        assertFalse(setDao.existsBySubjectInClass(subject, c.getClassId()));

        classDao.delete(c);
        userDao.delete(teacher);
    }
}

