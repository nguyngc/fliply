package model.dao;

import model.entity.ClassModel;
import model.entity.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClassModelDaoTest {

    private UserDao userDao;
    private ClassModelDao classDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        classDao = new ClassModelDao();
    }

    private User newUser() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        User u = new User();
        u.setFirstName("Test");
        u.setLastName("Teacher");
        u.setEmail("teacher+" + uid + "@test.com");
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

    @Test
    void crudClassModel() {
        // setup teacher
        User teacher = newUser();
        userDao.persist(teacher);
        assertNotNull(teacher.getUserId());

        // CREATE
        ClassModel c = newClass(teacher);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        Integer id = c.getClassId();

        // READ
        ClassModel found = classDao.find(id);
        assertNotNull(found);
        assertEquals(c.getClassName(), found.getClassName());
        assertEquals(teacher.getUserId(), found.getTeacher().getUserId());

        // UPDATE
        found.setClassName("UpdatedClass");
        classDao.update(found);

        ClassModel updated = classDao.find(id);
        assertNotNull(updated);
        assertEquals("UpdatedClass", updated.getClassName());

        // DELETE
        classDao.delete(updated);
        assertNull(classDao.find(id));

        // cleanup teacher
        userDao.delete(teacher);
    }

    @Test
    void queryClassModel() {
        // setup teacher
        User teacher = newUser();
        userDao.persist(teacher);
        assertNotNull(teacher.getUserId());

        String className = "TestClass-" + UUID.randomUUID().toString().substring(0, 6);

        // create class
        ClassModel c = new ClassModel();
        c.setClassName(className);
        c.setTeacher(teacher);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        // findByTeacherId
        List<ClassModel> byTeacher = classDao.findByTeacherId(teacher.getUserId());
        assertTrue(byTeacher.stream().anyMatch(x ->
                x.getClassId().equals(c.getClassId()) &&
                        x.getClassName().equals(className)
        ));

        // existsByNameAndTeacher
        assertTrue(classDao.existsByNameAndTeacher(className, teacher.getUserId()));

        // findAll
        List<ClassModel> all = classDao.findAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(x -> x.getClassId().equals(c.getClassId())));

        // cleanup
        classDao.delete(c);
        assertFalse(classDao.existsByNameAndTeacher(className, teacher.getUserId()));

        userDao.delete(teacher);
    }
}

