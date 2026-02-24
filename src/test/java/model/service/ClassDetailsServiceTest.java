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

class ClassDetailsServiceTest {

    private UserDao userDao;
    private ClassModelDao classDao;
    private ClassDetailsDao classDetailsDao;
    private ClassDetailsService service;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        classDao = new ClassModelDao();
        classDetailsDao = new ClassDetailsDao();
        service = new ClassDetailsService();
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
    void getClassesOfUser() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        User student = newUser("Student");
        student.setRole(0);
        userDao.persist(student);

        ClassModel c = newClass(teacher);
        classDao.persist(c);

        ClassDetails cd = new ClassDetails(c, student);
        classDetailsDao.persist(cd);

        List<ClassModel> classes = service.getClassesOfUser(student.getUserId());
        assertTrue(classes.stream().anyMatch(x -> x.getClassId().equals(c.getClassId())));

        // cleanup
        classDetailsDao.delete(cd);
        classDao.delete(c);
        userDao.delete(student);
        userDao.delete(teacher);
    }

    @Test
    void getClassesByClassId() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        User student = newUser("Student");
        userDao.persist(student);

        ClassModel c = newClass(teacher);
        classDao.persist(c);

        ClassDetails cd = new ClassDetails(c, student);
        classDetailsDao.persist(cd);

        List<ClassDetails> list = service.getClassesByClassId(c.getClassId());
        assertEquals(1, list.size());
        assertEquals(student.getUserId(), list.get(0).getStudent().getUserId());

        // cleanup
        classDetailsDao.delete(cd);
        classDao.delete(c);
        userDao.delete(student);
        userDao.delete(teacher);
    }

    @Test
    void addStudentToClass() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        User student = newUser("Student");
        userDao.persist(student);

        ClassModel c = newClass(teacher);
        classDao.persist(c);

        ClassDetails cd = service.addStudentToClass(student, c);
        assertNotNull(cd.getClassDetailsId());
        assertEquals(student.getUserId(), cd.getStudent().getUserId());

        // cleanup
        classDetailsDao.delete(cd);
        classDao.delete(c);
        userDao.delete(student);
        userDao.delete(teacher);
    }

    @Test
    void update() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        User student1 = newUser("Student1");
        userDao.persist(student1);

        User student2 = newUser("Student2");
        userDao.persist(student2);

        ClassModel c = newClass(teacher);
        classDao.persist(c);

        ClassDetails cd = new ClassDetails(c, student1);
        classDetailsDao.persist(cd);

        cd.setStudent(student2);
        service.update(cd);

        ClassDetails updated = classDetailsDao.find(cd.getClassDetailsId());
        assertEquals(student2.getUserId(), updated.getStudent().getUserId());

        // cleanup
        classDetailsDao.delete(updated);
        classDao.delete(c);
        userDao.delete(student1);
        userDao.delete(student2);
        userDao.delete(teacher);
    }

    @Test
    void removeStudentFromClass() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        User student = newUser("Student");
        userDao.persist(student);

        ClassModel c = newClass(teacher);
        classDao.persist(c);

        ClassDetails cd = new ClassDetails(c, student);
        classDetailsDao.persist(cd);

        service.removeStudentFromClass(cd);

        assertNull(classDetailsDao.find(cd.getClassDetailsId()));

        // cleanup
        classDao.delete(c);
        userDao.delete(student);
        userDao.delete(teacher);
    }

    @Test
    void reloadClass() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        User student = newUser("Student");
        userDao.persist(student);

        ClassModel c = newClass(teacher);
        classDao.persist(c);

        ClassDetails cd = new ClassDetails(c, student);
        classDetailsDao.persist(cd);

        ClassModel loaded = service.reloadClass(c.getClassId());
        assertNotNull(loaded);
        assertEquals(c.getClassId(), loaded.getClassId());
        assertFalse(loaded.getStudents().isEmpty());

        // cleanup
        classDetailsDao.delete(cd);
        classDao.delete(c);
        userDao.delete(student);
        userDao.delete(teacher);
    }
}
