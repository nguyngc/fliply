package model.dao;

import model.entity.ClassModel;
import model.entity.User;
import model.entity.ClassDetails;
import model.entity.FlashcardSet;
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
        ClassModel found = classDao.findById(id);
        assertNotNull(found);
        assertEquals(c.getClassName(), found.getClassName());
        assertEquals(teacher.getUserId(), found.getTeacher().getUserId());

        // UPDATE
        found.setClassName("UpdatedClass");
        classDao.update(found);

        ClassModel updated = classDao.findById(id);
        assertNotNull(updated);
        assertEquals("UpdatedClass", updated.getClassName());

        // DELETE
        classDao.delete(updated);
        assertNull(classDao.findById(id));

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

    @Test
    void findClassesOfUser() {
        // CREATE teacher
        User teacher = newUser();
        userDao.persist(teacher);

        // CREATE student
        User student = newUser();
        student.setRole(0); // student role
        userDao.persist(student);

        // CREATE class
        ClassModel c = newClass(teacher);
        classDao.persist(c);

        // ADD student to class (ClassDetails)
        ClassDetails cd = new ClassDetails();
        cd.setClassModel(c);
        cd.setStudent(student);
        new ClassDetailsDao().persist(cd);

        // QUERY: student should see this class
        List<ClassModel> classes = classDao.findClassesOfUser(student.getUserId());

        assertFalse(classes.isEmpty());
        assertTrue(classes.stream().anyMatch(x -> x.getClassId().equals(c.getClassId())));

        // QUERY: teacher should also see this class
        List<ClassModel> teacherClasses = classDao.findClassesOfUser(teacher.getUserId());
        assertTrue(teacherClasses.stream().anyMatch(x -> x.getClassId().equals(c.getClassId())));

        // cleanup
        new ClassDetailsDao().delete(cd);
        classDao.delete(c);
        userDao.delete(student);
        userDao.delete(teacher);
    }
    @Test
    void findByIdWithRelations() {
        // CREATE teacher
        User teacher = newUser();
        userDao.persist(teacher);

        // CREATE student
        User student = newUser();
        student.setRole(0);
        userDao.persist(student);

        // CREATE class
        ClassModel c = newClass(teacher);
        classDao.persist(c);

        // ADD student to class
        ClassDetails cd = new ClassDetails();
        cd.setClassModel(c);
        cd.setStudent(student);
        new ClassDetailsDao().persist(cd);

        // ADD flashcard set
        FlashcardSet fs = new FlashcardSet();
        fs.setSubject("TestSet");
        fs.setClassModel(c);
        new FlashcardSetDao().persist(fs);

        // QUERY with relations
        ClassModel result = classDao.findByIdWithRelations(c.getClassId());

        assertNotNull(result);
        assertEquals(c.getClassId(), result.getClassId());

        // Students loaded?
        assertNotNull(result.getStudents());
        assertFalse(result.getStudents().isEmpty());
        ClassDetails first = result.getStudents().iterator().next();
        assertEquals(student.getUserId(), first.getStudent().getUserId());

        // Flashcard sets loaded?
        assertNotNull(result.getFlashcardSets());
        assertFalse(result.getFlashcardSets().isEmpty());
        FlashcardSet firstSet = result.getFlashcardSets().iterator().next();
        assertEquals("TestSet", firstSet.getSubject());

        // cleanup
        new FlashcardSetDao().delete(fs);
        new ClassDetailsDao().delete(cd);
        classDao.delete(c);
        userDao.delete(student);
        userDao.delete(teacher);
    }

}

