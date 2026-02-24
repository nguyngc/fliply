package model.dao;

import model.entity.ClassModel;
import model.entity.ClassDetails;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.entity.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardDaoTest {

    private UserDao userDao;
    private ClassModelDao classDao;
    private ClassDetailsDao classDetailsDao;
    private FlashcardSetDao setDao;
    private FlashcardDao flashcardDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        classDao = new ClassModelDao();
        classDetailsDao = new ClassDetailsDao();
        setDao = new FlashcardSetDao();
        flashcardDao = new FlashcardDao();
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

    private FlashcardSet newSet(ClassModel clazz) {
        FlashcardSet fs = new FlashcardSet();
        fs.setSubject("Subject-" + UUID.randomUUID().toString().substring(0, 6));
        fs.setClassModel(clazz);
        return fs;
    }

    private Flashcard newFlashcard(String term, String definition, FlashcardSet set, User user) {
        Flashcard f = new Flashcard();
        f.setTerm(term);
        f.setDefinition(definition);
        f.setFlashcardSet(set);
        f.setUser(user);
        return f;
    }

    @Test
    void crudFlashcard() {
        // setup teacher and class
        User teacher = newUser("Teacher");
        userDao.persist(teacher);
        assertNotNull(teacher.getUserId());

        ClassModel c = newClass(teacher);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        // setup flashcard set
        FlashcardSet fs = newSet(c);
        setDao.persist(fs);
        assertNotNull(fs.getFlashcardSetId());

        // setup user
        User user = newUser("CardCreator");
        userDao.persist(user);
        assertNotNull(user.getUserId());

        // CREATE
        String term = "Term-" + UUID.randomUUID().toString().substring(0, 6);
        String definition = "Definition of " + term;
        Flashcard f = newFlashcard(term, definition, fs, user);
        flashcardDao.persist(f);
        assertNotNull(f.getFlashcardId());

        Integer id = f.getFlashcardId();

        // READ
        Flashcard found = flashcardDao.find(id);
        assertNotNull(found);
        assertEquals(term, found.getTerm());
        assertEquals(definition, found.getDefinition());
        assertEquals(fs.getFlashcardSetId(), found.getFlashcardSet().getFlashcardSetId());
        assertEquals(user.getUserId(), found.getUser().getUserId());

        // UPDATE
        found.setTerm("UpdatedTerm");
        found.setDefinition("UpdatedDefinition");
        flashcardDao.update(found);

        Flashcard updated = flashcardDao.find(id);
        assertNotNull(updated);
        assertEquals("UpdatedTerm", updated.getTerm());
        assertEquals("UpdatedDefinition", updated.getDefinition());

        // DELETE
        flashcardDao.delete(updated);
        assertNull(flashcardDao.find(id));

        // cleanup
        setDao.delete(fs);
        classDao.delete(c);
        userDao.delete(user);
        userDao.delete(teacher);
    }

    @Test
    void queryFlashcard() {
        // setup teacher and class
        User teacher = newUser("Teacher");
        userDao.persist(teacher);
        assertNotNull(teacher.getUserId());

        ClassModel c = newClass(teacher);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        // setup flashcard set
        FlashcardSet fs = newSet(c);
        setDao.persist(fs);
        assertNotNull(fs.getFlashcardSetId());

        // setup user
        User user = newUser("CardCreator");
        userDao.persist(user);
        assertNotNull(user.getUserId());

        // create flashcard
        String term = "TestTerm-" + UUID.randomUUID().toString().substring(0, 6);
        String definition = "TestDefinition";
        Flashcard f = newFlashcard(term, definition, fs, user);
        flashcardDao.persist(f);
        assertNotNull(f.getFlashcardId());

        // findByFlashcardSetId
        List<Flashcard> bySet = flashcardDao.findByFlashcardSetId(fs.getFlashcardSetId());
        assertTrue(bySet.stream().anyMatch(x ->
                x.getFlashcardId().equals(f.getFlashcardId()) &&
                        x.getTerm().equals(term)
        ));

        // findByUserId
        List<Flashcard> byUser = flashcardDao.findByUserId(user.getUserId());
        assertTrue(byUser.stream().anyMatch(x ->
                x.getFlashcardId().equals(f.getFlashcardId()) &&
                        x.getTerm().equals(term)
        ));

        // existsByTermInSet
        assertTrue(flashcardDao.existsByTermInSet(term, fs.getFlashcardSetId()));

        // findAll
        List<Flashcard> all = flashcardDao.findAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(x -> x.getFlashcardId().equals(f.getFlashcardId())));

        // cleanup
        flashcardDao.delete(f);
        assertFalse(flashcardDao.existsByTermInSet(term, fs.getFlashcardSetId()));

        setDao.delete(fs);
        classDao.delete(c);
        userDao.delete(user);
        userDao.delete(teacher);
    }

    @Test
    void findAvailableForUser() {
        // setup teacher and class
        User teacher = newUser("Teacher");
        userDao.persist(teacher);
        assertNotNull(teacher.getUserId());

        ClassModel c = newClass(teacher);
        classDao.persist(c);
        assertNotNull(c.getClassId());

        // setup flashcard set
        FlashcardSet fs = newSet(c);
        setDao.persist(fs);
        assertNotNull(fs.getFlashcardSetId());

        // setup user who creates flashcard
        User creator = newUser("Creator");
        userDao.persist(creator);
        assertNotNull(creator.getUserId());

        // create flashcard by creator
        String term1 = "CreatorTerm-" + UUID.randomUUID().toString().substring(0, 6);
        Flashcard f1 = newFlashcard(term1, "Definition1", fs, creator);
        flashcardDao.persist(f1);
        assertNotNull(f1.getFlashcardId());

        // setup student who joins class
        User student = newUser("Student");
        userDao.persist(student);
        assertNotNull(student.getUserId());
        ClassDetails cd = new ClassDetails();
        cd.setStudent(student);
        cd.setClassModel(c);
        classDetailsDao.persist(cd);
        assertNotNull(cd.getClassDetailsId());

        // findAvailableForUser - creator should see their own flashcard
        List<Flashcard> available = flashcardDao.findAvailableForUser(creator.getUserId());
        assertTrue(available.stream().anyMatch(x -> x.getFlashcardId().equals(f1.getFlashcardId())));

        // findAvailableForUser - student should see flashcard via class enrollment
        List<Flashcard> availableForStudent = flashcardDao.findAvailableForUser(student.getUserId());
        assertTrue(availableForStudent.stream().anyMatch(x -> x.getFlashcardId().equals(f1.getFlashcardId())));

        // cleanup
        classDetailsDao.delete(cd);
        flashcardDao.delete(f1);
        setDao.delete(fs);
        classDao.delete(c);
        userDao.delete(student);
        userDao.delete(creator);
        userDao.delete(teacher);
    }
}
