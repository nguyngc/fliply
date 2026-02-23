package model.service;

import model.AppState;
import model.dao.ClassModelDao;
import model.dao.UserDao;
import model.entity.ClassModel;
import model.entity.User;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TeacherAddClassServiceTest {

    private UserDao userDao;
    private ClassModelDao classDao;
    private TeacherAddClassService service;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        classDao = new ClassModelDao();
        service = new TeacherAddClassService();
    }

    private User newTeacher() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        User u = new User();
        u.setFirstName("Teacher");
        u.setLastName("User");
        u.setEmail("teacher+" + uid + "@test.com");
        u.setPassword("password123");
        u.setRole(1);
        return u;
    }

    @Test
    void createClass_success() {
        // setup teacher
        User teacher = newTeacher();
        userDao.persist(teacher);
        AppState.currentUser.set(teacher);

        String className = "Class-" + UUID.randomUUID().toString().substring(0, 6);

        // create class
        service.createClass(className);

        // verify
        assertTrue(classDao.existsByNameAndTeacher(className, teacher.getUserId()));

        // cleanup
        ClassModel created = classDao.findByTeacherId(teacher.getUserId())
                .stream().filter(c -> c.getClassName().equals(className))
                .findFirst().orElse(null);

        if (created != null) classDao.delete(created);
        userDao.delete(teacher);
    }

    @Test
    void createClass_duplicate_throwsException() {
        // setup teacher
        User teacher = newTeacher();
        userDao.persist(teacher);
        AppState.currentUser.set(teacher);

        String className = "DuplicateClass";

        // create first class
        ClassModel c = new ClassModel();
        c.setClassName(className);
        c.setTeacher(teacher);
        classDao.persist(c);

        // attempt duplicate
        assertThrows(IllegalArgumentException.class, () -> {
            service.createClass(className);
        });

        // cleanup
        classDao.delete(c);
        userDao.delete(teacher);
    }

    @Test
    void createClass_assignsCorrectTeacher() {
        // setup teacher
        User teacher = newTeacher();
        userDao.persist(teacher);
        AppState.currentUser.set(teacher);

        String className = "Class-" + UUID.randomUUID().toString().substring(0, 6);

        // create class
        service.createClass(className);

        // verify teacher assigned
        ClassModel created = classDao.findByTeacherId(teacher.getUserId())
                .stream().filter(c -> c.getClassName().equals(className))
                .findFirst().orElse(null);

        assertNotNull(created);
        assertEquals(teacher.getUserId(), created.getTeacher().getUserId());

        // cleanup
        classDao.delete(created);
        userDao.delete(teacher);
    }
}
