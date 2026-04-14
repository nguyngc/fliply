package model.service;

import model.dao.ClassModelDao;
import model.dao.FlashcardDao;
import model.dao.FlashcardSetDao;
import model.dao.UserDao;
import model.entity.ClassModel;
import model.entity.Flashcard;
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
    private FlashcardDao flashcardDao;

    @BeforeEach
    void setUp() {
        setService = new FlashcardSetService();
        userDao = new UserDao();
        classDao = new ClassModelDao();
        setDao = new FlashcardSetDao();
        flashcardDao = new FlashcardDao();
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

    @Test
    void saveAndDeleteBranchesAreHandled() {
        User teacher = newTeacher();
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        FlashcardSet fs = setService.createSet("Original", clazz);
        fs.setSubject("Updated");
        setService.save(fs);

        FlashcardSet reloaded = setDao.find(fs.getFlashcardSetId());
        assertNotNull(reloaded);
        assertEquals("Updated", reloaded.getSubject());

        setService.deleteSet(null);
        FlashcardSet transientSet = new FlashcardSet();
        transientSet.setSubject("Transient");
        setService.deleteSet(transientSet);

        setDao.delete(reloaded);
        classDao.delete(clazz);
        userDao.delete(teacher);
    }

    @Test
    void save_persistsTransientSet_andDeleteSet_removesPersistedSet() {
        User teacher = newTeacher();
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        FlashcardSet transientSet = new FlashcardSet();
        transientSet.setSubject("Transient");
        transientSet.setClassModel(clazz);

        setService.save(transientSet);

        assertNotNull(transientSet.getFlashcardSetId());
        assertNotNull(setDao.find(transientSet.getFlashcardSetId()));

        setService.deleteSet(transientSet);

        assertNull(setDao.find(transientSet.getFlashcardSetId()));

        classDao.delete(clazz);
        userDao.delete(teacher);
    }

    @Test
    void getSetWithCards_andGetAllSets_returnPersistedData() {
        User teacher = newTeacher();
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        FlashcardSet fs = setService.createSet("Algorithms", clazz);

        Flashcard card = new Flashcard();
        card.setTerm("BFS");
        card.setDefinition("Breadth-first search");
        card.setFlashcardSet(fs);
        card.setUser(teacher);
        flashcardDao.persist(card);

        FlashcardSet loaded = setService.getSetWithCards(fs.getFlashcardSetId());
        assertNotNull(loaded);
        assertEquals(fs.getFlashcardSetId(), loaded.getFlashcardSetId());
        assertEquals(1, loaded.getCards().size());

        assertTrue(setService.getAllSets().stream()
                .anyMatch(set -> set.getFlashcardSetId().equals(fs.getFlashcardSetId())));

        flashcardDao.delete(card);
        setDao.delete(fs);
        classDao.delete(clazz);
        userDao.delete(teacher);
    }
}
