package model.service;

import model.dao.ClassModelDao;
import model.dao.FlashcardDao;
import model.dao.FlashcardSetDao;
import model.dao.QuizDao;
import model.dao.QuizDetailsDao;
import model.dao.UserDao;
import model.entity.ClassModel;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.entity.Quiz;
import model.entity.QuizDetails;
import model.entity.User;
import org.junit.jupiter.api.*;
import util.FlashcardFileParser;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentCaptor.forClass;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FlashcardServiceTest {

    private FlashcardService flashcardService;
    private UserDao userDao;
    private ClassModelDao classDao;
    private FlashcardSetDao setDao;
    private FlashcardDao flashcardDao;
    private QuizDao quizDao;
    private QuizDetailsDao quizDetailsDao;

    @BeforeEach
    void setUp() {
        flashcardService = new FlashcardService();
        userDao = new UserDao();
        classDao = new ClassModelDao();
        setDao = new FlashcardSetDao();
        flashcardDao = new FlashcardDao();
        quizDao = new QuizDao();
        quizDetailsDao = new QuizDetailsDao();
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

    @Test
    void createFlashcard_andPreventDuplicate() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        FlashcardSet fs = newSet(clazz);
        setDao.persist(fs);

        User creator = newUser("Creator");
        userDao.persist(creator);

        String term = "Term-" + UUID.randomUUID().toString().substring(0, 6);
        Flashcard first = flashcardService.createFlashcard(term, "Def1", fs, creator);
        assertNotNull(first);
        assertNotNull(first.getFlashcardId());

        Flashcard dup = flashcardService.createFlashcard(term, "Def1 again", fs, creator);
        assertNull(dup);

        List<Flashcard> bySet = flashcardService.getFlashcardsBySet(fs.getFlashcardSetId());
        assertTrue(bySet.stream().anyMatch(x -> x.getFlashcardId().equals(first.getFlashcardId())));

        flashcardDao.delete(first);
        setDao.delete(fs);
        classDao.delete(clazz);
        userDao.delete(creator);
        userDao.delete(teacher);
    }

    @Test
    void update_delete_save_and_getByUser() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        FlashcardSet fs = newSet(clazz);
        setDao.persist(fs);

        User creator = newUser("Creator");
        userDao.persist(creator);

        Flashcard card = flashcardService.createFlashcard("Term-A", "Def-A", fs, creator);
        assertNotNull(card);

        card.setDefinition("Def-Updated");
        flashcardService.update(card);

        Flashcard reloaded = flashcardDao.find(card.getFlashcardId());
        assertEquals("Def-Updated", reloaded.getDefinition());

        List<Flashcard> byUser = flashcardService.getFlashcardsByUser(creator.getUserId());
        assertTrue(byUser.stream().anyMatch(x -> x.getFlashcardId().equals(card.getFlashcardId())));

        Flashcard saved = new Flashcard();
        saved.setTerm("Saved-Term");
        saved.setDefinition("Saved-Def");
        saved.setFlashcardSet(fs);
        saved.setUser(creator);
        flashcardService.save(saved);
        assertNotNull(saved.getFlashcardId());

        flashcardService.delete(saved);
        flashcardService.delete(reloaded);

        setDao.delete(fs);
        classDao.delete(clazz);
        userDao.delete(creator);
        userDao.delete(teacher);
    }

    @Test
    void delete_removesQuizDetailsBeforeDeletingFlashcard() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        FlashcardSet fs = newSet(clazz);
        setDao.persist(fs);

        Flashcard card = flashcardService.createFlashcard("Term-Q", "Def-Q", fs, teacher);
        assertNotNull(card);

        Quiz quiz = new Quiz();
        quiz.setUser(teacher);
        quiz.setNoOfQuestions(1);
        quizDao.persist(quiz);

        QuizDetails details = new QuizDetails(quiz, card);
        quizDetailsDao.persist(details);
        assertFalse(quizDetailsDao.findByFlashcardId(card.getFlashcardId()).isEmpty());

        flashcardService.delete(card);

        assertNull(flashcardDao.find(card.getFlashcardId()));
        assertTrue(quizDetailsDao.findByFlashcardId(card.getFlashcardId()).isEmpty());

        quizDao.delete(quiz);
        setDao.delete(fs);
        classDao.delete(clazz);
        userDao.delete(teacher);
    }

    @Test
    void delete_nullCard_doesNothing() throws Exception {
        FlashcardDao flashcardDaoMock = mock(FlashcardDao.class);
        QuizDetailsDao quizDetailsDaoMock = mock(QuizDetailsDao.class);
        injectDao(flashcardService, "flashDao", flashcardDaoMock);
        injectDao(flashcardService, "quizDetailsDao", quizDetailsDaoMock);

        flashcardService.delete(null);

        verify(quizDetailsDaoMock, never()).deleteByFlashcardId(org.mockito.ArgumentMatchers.anyInt());
        verify(flashcardDaoMock, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void delete_transientCardWithoutId_doesNothing() throws Exception {
        FlashcardDao flashcardDaoMock = mock(FlashcardDao.class);
        QuizDetailsDao quizDetailsDaoMock = mock(QuizDetailsDao.class);
        injectDao(flashcardService, "flashDao", flashcardDaoMock);
        injectDao(flashcardService, "quizDetailsDao", quizDetailsDaoMock);

        Flashcard transientCard = new Flashcard();
        transientCard.setTerm("Transient");

        flashcardService.delete(transientCard);

        verify(quizDetailsDaoMock, never()).deleteByFlashcardId(org.mockito.ArgumentMatchers.anyInt());
        verify(flashcardDaoMock, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getFlashcardsBySet_delegatesToDao() throws Exception {
        FlashcardDao flashcardDaoMock = mock(FlashcardDao.class);
        injectDao(flashcardService, "flashDao", flashcardDaoMock);

        Flashcard flashcard = new Flashcard();
        List<Flashcard> expected = List.of(flashcard);
        when(flashcardDaoMock.findByFlashcardSetId(12)).thenReturn(expected);

        List<Flashcard> actual = flashcardService.getFlashcardsBySet(12);

        assertSame(expected, actual);
        verify(flashcardDaoMock).findByFlashcardSetId(12);
    }

    @Test
    void createFlashcard_fromParsedCardCopiesLocalizedDefinitionsAndPersists() throws Exception {
        FlashcardDao flashcardDaoMock = mock(FlashcardDao.class);
        injectDao(flashcardService, "flashDao", flashcardDaoMock);

        FlashcardSet set = new FlashcardSet();
        setId(set, "flashcardSetId", 44);
        User user = new User();
        FlashcardFileParser.ParsedCard parsedCard = new FlashcardFileParser.ParsedCard(
                "Algorithm",
                "Step-by-step instructions",
                Map.of(
                        "en", "Step-by-step instructions",
                        "ar", "تعليمات خطوة بخطوة",
                        "fi", "Vaiheittaiset ohjeet",
                        "ko", "단계별 지침",
                        "lo", "ຄໍາແນະນໍາເປັນຂັ້ນຕອນ",
                        "vi", "Hướng dẫn từng bước"
                )
        );

        when(flashcardDaoMock.existsByTermInSet("Algorithm", 44)).thenReturn(false);

        Flashcard created = flashcardService.createFlashcard(parsedCard, set, user);

        assertNotNull(created);
        assertEquals("Algorithm", created.getTerm());
        assertEquals("Step-by-step instructions", created.getDefinition());
        assertEquals("تعليمات خطوة بخطوة", created.getDefinitionAr());
        assertEquals("Vaiheittaiset ohjeet", created.getDefinitionFi());
        assertEquals("단계별 지침", created.getDefinitionKo());
        assertEquals("ຄໍາແນະນໍາເປັນຂັ້ນຕອນ", created.getDefinitionLo());
        assertEquals("Hướng dẫn từng bước", created.getDefinitionVi());
        assertSame(set, created.getFlashcardSet());
        assertSame(user, created.getUser());

        ArgumentCaptor<Flashcard> persisted = forClass(Flashcard.class);
        verify(flashcardDaoMock).persist(persisted.capture());
        assertSame(created, persisted.getValue());
    }

    @Test
    void createFlashcard_nullParsedCardReturnsNullWithoutDaoCalls() throws Exception {
        FlashcardDao flashcardDaoMock = mock(FlashcardDao.class);
        injectDao(flashcardService, "flashDao", flashcardDaoMock);

        assertNull(flashcardService.createFlashcard((FlashcardFileParser.ParsedCard) null, new FlashcardSet(), new User()));

        verify(flashcardDaoMock, never()).existsByTermInSet(anyString(), anyInt());
        verify(flashcardDaoMock, never()).persist(any());
    }

    @Test
    void createFlashcard_duplicateLocalizedCardReturnsNullWithoutPersisting() throws Exception {
        FlashcardDao flashcardDaoMock = mock(FlashcardDao.class);
        injectDao(flashcardService, "flashDao", flashcardDaoMock);

        FlashcardSet set = new FlashcardSet();
        setId(set, "flashcardSetId", 55);
        FlashcardFileParser.ParsedCard parsedCard = new FlashcardFileParser.ParsedCard(
                "Cache",
                "Temporary storage",
                Map.of("en", "Temporary storage", "vi", "Bộ nhớ tạm")
        );

        when(flashcardDaoMock.existsByTermInSet("Cache", 55)).thenReturn(true);

        assertNull(flashcardService.createFlashcard(parsedCard, set, new User()));

        verify(flashcardDaoMock).existsByTermInSet(eq("Cache"), eq(55));
        verify(flashcardDaoMock, never()).persist(any());
    }

    private void injectDao(Object target, String fieldName, Object value) throws Exception {
        Field field = FlashcardService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void setId(Object target, String fieldName, Integer value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
