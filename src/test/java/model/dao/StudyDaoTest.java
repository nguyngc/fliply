package model.dao;

import model.entity.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudyDaoTest {

    private UserDao userDao;
    private ClassModelDao classDao;
    private FlashcardSetDao setDao;
    private StudyDao studyDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        classDao = new ClassModelDao();
        setDao = new FlashcardSetDao();
        studyDao = new StudyDao();
    }

    private User newUser() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        User u = new User();
        u.setFirstName("Test");
        u.setLastName("User");
        u.setEmail("study+" + uid + "@test.com");
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

    private FlashcardSet newSet(ClassModel clazz) {
        FlashcardSet fs = new FlashcardSet();
        fs.setSubject("Subject-" + UUID.randomUUID().toString().substring(0, 6));
        fs.setClassModel(clazz);
        return fs;
    }

    private Study newStudy(User user, FlashcardSet set, int statistic) {
        Study s = new Study();
        s.setUser(user);
        s.setFlashcardSet(set);
        s.setStatistic(statistic);
        return s;
    }

    @Test
    void crudStudy() {
        // setup parents
        User u = newUser();
        userDao.persist(u);
        assertNotNull(u.getUserId());

        ClassModel c = newClass(u);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        FlashcardSet fs = newSet(c);
        setDao.persist(fs);
        assertNotNull(fs.getFlashcardSetId());

        // CREATE
        Study s = newStudy(u, fs, 5);
        studyDao.persist(s);
        assertNotNull(s.getStudyId());

        Integer id = s.getStudyId();

        // READ
        Study found = studyDao.find(id);
        assertNotNull(found);
        assertEquals(5, found.getStatistic());
        assertEquals(u.getUserId(), found.getUser().getUserId());
        assertEquals(fs.getFlashcardSetId(), found.getFlashcardSet().getFlashcardSetId());

        // UPDATE
        found.setStatistic(9);
        studyDao.update(found);

        Study updated = studyDao.find(id);
        assertNotNull(updated);
        assertEquals(9, updated.getStatistic());

        // DELETE
        studyDao.delete(updated);
        assertNull(studyDao.find(id));
    }

    @Test
    void queryStudy() {
        // setup parents
        User u = newUser();
        userDao.persist(u);
        assertNotNull(u.getUserId());

        ClassModel c = newClass(u);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        FlashcardSet fs = newSet(c);
        setDao.persist(fs);
        assertNotNull(fs.getFlashcardSetId());

        // create study
        Study s = newStudy(u, fs, 3);
        studyDao.persist(s);
        assertNotNull(s.getStudyId());

        // exists
        assertTrue(studyDao.existsByUserAndSet(u.getUserId(), fs.getFlashcardSetId()));

        // findByUserId
        List<Study> byUser = studyDao.findByUserId(u.getUserId());
        assertTrue(byUser.stream().anyMatch(x ->
                x.getUser().getUserId().equals(u.getUserId()) &&
                        x.getFlashcardSet().getFlashcardSetId().equals(fs.getFlashcardSetId())
        ));

        // findByFlashcardSetId
        List<Study> bySet = studyDao.findByFlashcardSetId(fs.getFlashcardSetId());
        assertTrue(bySet.stream().anyMatch(x ->
                x.getUser().getUserId().equals(u.getUserId()) &&
                        x.getFlashcardSet().getFlashcardSetId().equals(fs.getFlashcardSetId())
        ));

        // cleanup
        studyDao.delete(s);
        assertFalse(studyDao.existsByUserAndSet(u.getUserId(), fs.getFlashcardSetId()));
    }
}
