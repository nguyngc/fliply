package model.service;

import model.dao.ClassDetailsDao;
import model.dao.ClassModelDao;
import model.dao.UserDao;
import model.entity.ClassDetails;
import model.entity.ClassModel;
import model.entity.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentServiceTest {

    private EnrollmentService enrollmentService;
    private ClassDetailsDao cdDao;
    private ClassModelDao classDao;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        enrollmentService = new EnrollmentService();
        cdDao = new ClassDetailsDao();
        classDao = new ClassModelDao();
        userDao = new UserDao();
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

    @Test
    void enroll_andPreventDuplicate() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        User student = newUser("Student");
        userDao.persist(student);

        ClassDetails first = enrollmentService.enroll(student, clazz);
        assertNotNull(first);
        assertNotNull(first.getClassDetailsId());

        ClassDetails duplicate = enrollmentService.enroll(student, clazz);
        assertNull(duplicate);

        List<ClassDetails> students = enrollmentService.getStudentsInClass(clazz.getClassId());
        assertTrue(students.stream().anyMatch(cd -> cd.getStudent().getUserId().equals(student.getUserId())));

        cdDao.deleteByClassId(clazz.getClassId());
        classDao.delete(clazz);
        userDao.delete(student);
        userDao.delete(teacher);
    }

    @Test
    void resetClassStudents() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        User s1 = newUser("Student1");
        User s2 = newUser("Student2");
        userDao.persist(s1);
        userDao.persist(s2);

        ClassDetails cd1 = enrollmentService.enroll(s1, clazz);
        ClassDetails cd2 = enrollmentService.enroll(s2, clazz);
        assertNotNull(cd1);
        assertNotNull(cd2);

        List<ClassDetails> before = enrollmentService.getStudentsInClass(clazz.getClassId());
        assertTrue(before.size() >= 2);

        enrollmentService.resetClassStudents(clazz.getClassId());

        List<ClassDetails> after = enrollmentService.getStudentsInClass(clazz.getClassId());
        assertTrue(after.isEmpty());

        classDao.delete(clazz);
        userDao.delete(s1);
        userDao.delete(s2);
        userDao.delete(teacher);
    }
}

