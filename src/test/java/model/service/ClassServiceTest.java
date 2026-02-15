package model.service;

import model.dao.ClassModelDao;
import model.dao.UserDao;
import model.entity.ClassModel;
import model.entity.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClassServiceTest {

    private ClassService classService;
    private UserDao userDao;
    private ClassModelDao classDao;

    @BeforeEach
    void setUp() {
        classService = new ClassService();
        userDao = new UserDao();
        classDao = new ClassModelDao();
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

    @Test
    void createClass_andGetByTeacher() {
        User teacher = newTeacher();
        userDao.persist(teacher);
        assertNotNull(teacher.getUserId());

        String className = "Class-" + UUID.randomUUID().toString().substring(0, 6);
        ClassModel c = classService.createClass(className, teacher);

        assertNotNull(c);
        assertNotNull(c.getClassId());
        assertEquals(className, c.getClassName());
        assertEquals(teacher.getUserId(), c.getTeacher().getUserId());

        List<ClassModel> byTeacher = classService.getClassesByTeacher(teacher.getUserId());
        assertTrue(byTeacher.stream().anyMatch(x -> x.getClassId().equals(c.getClassId())));

        classDao.delete(c);
        userDao.delete(teacher);
    }
}

