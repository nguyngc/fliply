package model.service;

import model.dao.*;
import model.entity.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuizServiceTest {

    private QuizService quizService;
    private UserDao userDao;
    private ClassModelDao classDao;
    private ClassDetailsDao classDetailsDao;
    private FlashcardSetDao setDao;
    private FlashcardDao flashcardDao;
    private QuizDao quizDao;
    private QuizDetailsDao quizDetailsDao;

    @BeforeEach
    void setUp() {
        quizService = new QuizService();
        userDao = new UserDao();
        classDao = new ClassModelDao();
        classDetailsDao = new ClassDetailsDao();
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

    private Flashcard newFlashcard(FlashcardSet set, User user, String term) {
        Flashcard f = new Flashcard();
        f.setTerm(term);
        f.setDefinition("Def-" + term);
        f.setFlashcardSet(set);
        f.setUser(user);
        return f;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void generateQuiz_andBuildQuestions() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        FlashcardSet fs = newSet(clazz);
        setDao.persist(fs);

        User creator = newUser("Creator");
        userDao.persist(creator);

        // create flashcards in class
        Flashcard f1 = newFlashcard(fs, creator, "T1");
        Flashcard f2 = newFlashcard(fs, creator, "T2");
        Flashcard f3 = newFlashcard(fs, creator, "T3");
        flashcardDao.persist(f1);
        flashcardDao.persist(f2);
        flashcardDao.persist(f3);

        User student = newUser("Student");
        userDao.persist(student);

        ClassDetails enrollment = new ClassDetails();
        enrollment.setStudent(student);
        enrollment.setClassModel(clazz);
        classDetailsDao.persist(enrollment);

        int requested = 2;
        Quiz quiz = quizService.generateQuiz(student, requested);
        assertNotNull(quiz);
        assertNotNull(quiz.getQuizId());
        assertTrue(quiz.getNoOfQuestions() <= requested);

        List<QuizDetails> details = quizDetailsDao.findByQuizId(quiz.getQuizId());
        assertEquals(quiz.getNoOfQuestions(), details.size());

        List<QuizService.QuizQuestion> questions = quizService.buildQuizQuestions(quiz.getQuizId(), student.getUserId());
        assertEquals(details.size(), questions.size());
        for (QuizService.QuizQuestion qq : questions) {
            assertNotNull(qq.getCorrectAnswer());
            assertTrue(qq.getOptions().contains(qq.getCorrectAnswer()));
            assertFalse(qq.getOptions().isEmpty());
            assertTrue(qq.getOptions().size() <= 4);
        }

        // cleanup
        for (QuizDetails d : details) {
            quizDetailsDao.delete(d);
        }
        quizDao.delete(quiz);
        flashcardDao.delete(f1);
        flashcardDao.delete(f2);
        flashcardDao.delete(f3);
        classDetailsDao.delete(enrollment);
        setDao.delete(fs);
        classDao.delete(clazz);
        userDao.delete(student);
        userDao.delete(creator);
        userDao.delete(teacher);
    }

    @Test
    void generateQuiz_noAvailableFlashcardsReturnsNull() {
        User user = newUser("Lonely");
        userDao.persist(user);

        Quiz quiz = quizService.generateQuiz(user, 5);
        assertNull(quiz);

        userDao.delete(user);
    }

    @Test
    void generateQuiz_invalidInputsReturnNull() {
        assertNull(quizService.generateQuiz(null, 3));

        User user = newUser("Empty");
        userDao.persist(user);

        assertNull(quizService.generateQuiz(user, 0));
        assertNull(quizService.generateQuiz(user, -1));

        userDao.delete(user);
    }

    @Test
    void buildQuizQuestions_missingQuizDetailsReturnsEmptyList() {
        User user = newUser("Reader");
        userDao.persist(user);

        assertTrue(quizService.buildQuizQuestions(123456, user.getUserId()).isEmpty());
        assertTrue(quizService.getQuizzesByUser(null).isEmpty());

        userDao.delete(user);
    }

    @Test
    void generateQuiz_userWithoutIdReturnsNull() {
        User user = newUser("NoId");

        assertNull(quizService.generateQuiz(user, 3));
    }

    @Test
    void getQuizzesByUser_returnsPersistedQuiz() {
        User teacher = newUser("Teacher");
        userDao.persist(teacher);

        ClassModel clazz = newClass(teacher);
        classDao.persist(clazz);

        FlashcardSet fs = newSet(clazz);
        setDao.persist(fs);

        Flashcard flashcard = newFlashcard(fs, teacher, "Term");
        flashcardDao.persist(flashcard);

        User student = newUser("Student");
        userDao.persist(student);

        ClassDetails enrollment = new ClassDetails();
        enrollment.setStudent(student);
        enrollment.setClassModel(clazz);
        classDetailsDao.persist(enrollment);

        Quiz quiz = quizService.generateQuiz(student, 1);
        assertNotNull(quiz);

        List<Quiz> quizzes = quizService.getQuizzesByUser(student.getUserId());
        assertEquals(1, quizzes.size());
        assertEquals(quiz.getQuizId(), quizzes.get(0).getQuizId());

        List<QuizDetails> details = quizDetailsDao.findByQuizId(quiz.getQuizId());
        for (QuizDetails detail : details) {
            quizDetailsDao.delete(detail);
        }
        quizDao.delete(quiz);
        classDetailsDao.delete(enrollment);
        flashcardDao.delete(flashcard);
        setDao.delete(fs);
        classDao.delete(clazz);
        userDao.delete(student);
        userDao.delete(teacher);
    }

    @Test
    void buildQuizQuestions_skipsNullFlashcards_andHandlesNullCorrectAndPoolEntries() throws Exception {
        QuizDetailsDao mockQuizDetailsDao = mock(QuizDetailsDao.class);
        FlashcardDao mockFlashcardDao = mock(FlashcardDao.class);

        QuizDetails nullFlashcardDetail = new QuizDetails();

        Flashcard promptCard = new Flashcard();
        setField(promptCard, "flashcardId", 99);
        promptCard.setTerm("Prompt");
        promptCard.setDefinition(null);

        QuizDetails validDetail = new QuizDetails();
        validDetail.setFlashcard(promptCard);

        when(mockQuizDetailsDao.findByQuizId(77)).thenReturn(List.of(nullFlashcardDetail, validDetail));
        when(mockFlashcardDao.findAvailableForUser(5)).thenReturn(java.util.Collections.singletonList(null));

        setField(quizService, "quizDetailsDao", mockQuizDetailsDao);
        setField(quizService, "flashcardDao", mockFlashcardDao);

        List<QuizService.QuizQuestion> questions = quizService.buildQuizQuestions(77, 5);

        assertEquals(1, questions.size());
        QuizService.QuizQuestion question = questions.get(0);
        assertEquals(99, question.getFlashcardId());
        assertEquals("Prompt", question.getPrompt());
        assertNull(question.getCorrectAnswer());
        assertTrue(question.getOptions().isEmpty());
    }

    @Test
    void buildQuizQuestions_handlesNullPoolByUsingEmptyOptionsBeyondCorrectAnswer() throws Exception {
        QuizDetailsDao mockQuizDetailsDao = mock(QuizDetailsDao.class);
        FlashcardDao mockFlashcardDao = mock(FlashcardDao.class);

        Flashcard promptCard = new Flashcard();
        setField(promptCard, "flashcardId", 100);
        promptCard.setTerm("CPU");
        promptCard.setDefinition("Central Processing Unit");

        QuizDetails detail = new QuizDetails();
        detail.setFlashcard(promptCard);

        when(mockQuizDetailsDao.findByQuizId(88)).thenReturn(List.of(detail));
        when(mockFlashcardDao.findAvailableForUser(6)).thenReturn(null);

        setField(quizService, "quizDetailsDao", mockQuizDetailsDao);
        setField(quizService, "flashcardDao", mockFlashcardDao);

        List<QuizService.QuizQuestion> questions = quizService.buildQuizQuestions(88, 6);

        assertEquals(1, questions.size());
        assertEquals(List.of("Central Processing Unit"), questions.get(0).getOptions());
    }
}
