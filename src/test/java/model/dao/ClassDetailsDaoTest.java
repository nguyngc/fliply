package model.dao;

import model.entity.ClassDetails;
import model.entity.ClassModel;
import model.entity.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClassDetailsDaoTest {

    private UserDao userDao;
    private ClassModelDao classDao;
    private ClassDetailsDao classDetailsDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        classDao = new ClassModelDao();
        classDetailsDao = new ClassDetailsDao();
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

    private ClassDetails newClassDetails(User user, ClassModel classModel) {
        ClassDetails cd = new ClassDetails();
        cd.setUser(user);
        cd.setClassModel(classModel);
        return cd;
    }

    @Test
    void crudClassDetails() {
        // setup teacher and class
        User teacher = newUser("Teacher");
        userDao.persist(teacher);
        assertNotNull(teacher.getUserId());

        ClassModel c = newClass(teacher);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        // setup student
        User student = newUser("Student");
        userDao.persist(student);
        assertNotNull(student.getUserId());

        // CREATE
        ClassDetails cd = newClassDetails(student, c);
        classDetailsDao.persist(cd);
        assertNotNull(cd.getClassDetailsId());

        Integer id = cd.getClassDetailsId();

        // READ
        ClassDetails found = classDetailsDao.find(id);
        assertNotNull(found);
        assertEquals(student.getUserId(), found.getUser().getUserId());
        assertEquals(c.getClassId(), found.getClassModel().getClassId());

        // DELETE
        classDetailsDao.delete(found);
        assertNull(classDetailsDao.find(id));

        // cleanup
        classDao.delete(c);
        userDao.delete(student);
        userDao.delete(teacher);
    }

    @Test
    void queryClassDetails() {
        // setup teacher and class
        User teacher = newUser("Teacher");
        userDao.persist(teacher);
        assertNotNull(teacher.getUserId());

        ClassModel c = newClass(teacher);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        // setup student
        User student = newUser("Student");
        userDao.persist(student);
        assertNotNull(student.getUserId());

        // create class details
        ClassDetails cd = newClassDetails(student, c);
        classDetailsDao.persist(cd);
        assertNotNull(cd.getClassDetailsId());

        // findByClassId
        List<ClassDetails> byClass = classDetailsDao.findByClassId(c.getClassId());
        assertTrue(byClass.stream().anyMatch(x ->
                x.getUser().getUserId().equals(student.getUserId()) &&
                        x.getClassModel().getClassId().equals(c.getClassId())
        ));

        // findByUserId
        List<ClassDetails> byUser = classDetailsDao.findByUserId(student.getUserId());
        assertTrue(byUser.stream().anyMatch(x ->
                x.getUser().getUserId().equals(student.getUserId()) &&
                        x.getClassModel().getClassId().equals(c.getClassId())
        ));

        // findAll
        List<ClassDetails> all = classDetailsDao.findAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(x -> x.getClassDetailsId().equals(cd.getClassDetailsId())));

        // existsByUserAndClass
        assertTrue(classDetailsDao.existsByUserAndClass(student.getUserId(), c.getClassId()));
        assertFalse(classDetailsDao.existsByUserAndClass(999999, c.getClassId()));

        // cleanup
        classDetailsDao.delete(cd);
        classDao.delete(c);
        userDao.delete(student);
        userDao.delete(teacher);
    }

    @Test
    void deleteByClassId() {
        // setup teacher and class
        User teacher = newUser("Teacher");
        userDao.persist(teacher);
        assertNotNull(teacher.getUserId());

        ClassModel c = newClass(teacher);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        // setup students
        User student1 = newUser("Student1");
        userDao.persist(student1);
        User student2 = newUser("Student2");
        userDao.persist(student2);

        // create class details
        ClassDetails cd1 = newClassDetails(student1, c);
        classDetailsDao.persist(cd1);
        ClassDetails cd2 = newClassDetails(student2, c);
        classDetailsDao.persist(cd2);

        // verify created
        List<ClassDetails> before = classDetailsDao.findByClassId(c.getClassId());
        assertTrue(before.size() >= 2);

        // delete by class id
        classDetailsDao.deleteByClassId(c.getClassId());

        // verify deleted
        List<ClassDetails> after = classDetailsDao.findByClassId(c.getClassId());
        assertTrue(after.isEmpty());

        // cleanup
        classDao.delete(c);
        userDao.delete(student1);
        userDao.delete(student2);
        userDao.delete(teacher);
    }
}

