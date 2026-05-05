package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.AppState;
import model.dao.ClassDetailsDao;
import model.dao.ClassModelDao;
import model.dao.FlashcardDao;
import model.dao.FlashcardSetDao;
import model.dao.QuizDao;
import model.dao.QuizDetailsDao;
import model.dao.UserDao;
import model.entity.ClassDetails;
import model.entity.ClassModel;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.entity.Quiz;
import model.entity.QuizDetails;
import model.entity.User;
import model.service.QuizService;
import org.junit.jupiter.api.Test;
import util.LocaleManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuizResultControllerTest {

    static { new JFXPanel(); }

    private final QuizService quizService = new QuizService();
    private final UserDao userDao = new UserDao();
    private final ClassModelDao classDao = new ClassModelDao();
    private final ClassDetailsDao classDetailsDao = new ClassDetailsDao();
    private final FlashcardSetDao setDao = new FlashcardSetDao();
    private final FlashcardDao flashcardDao = new FlashcardDao();
    private final QuizDao quizDao = new QuizDao();
    private final QuizDetailsDao quizDetailsDao = new QuizDetailsDao();

    private static class TestableQuizResultController extends QuizResultController {
        List<QuizService.QuizQuestion> questionsToLoad = List.of();
        AppState.Screen lastNavigatedScreen;
        String lastLanguage;
        int lastQuizId;
        int lastUserId;

        @Override
        List<QuizService.QuizQuestion> loadQuestions(int quizId, int userId, String language) {
            lastQuizId = quizId;
            lastUserId = userId;
            lastLanguage = language;
            return questionsToLoad;
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    private static class FakeHeaderController extends HeaderController {
        String title;
        String subtitle;
        boolean backVisible;
        Runnable onBack;

        @Override public void setTitle(String titleText) { title = titleText; }
        @Override public void setSubtitle(String text) { subtitle = text; }
        @Override public void setBackVisible(boolean visible) { backVisible = visible; }
        @Override public void setOnBack(Runnable action) { onBack = action; }
    }

    @Test
    void initialize_usesStudentLanguageAndRendersLocalizedStatusLabels() throws Exception {
        AppStateSnapshot snapshot = snapshotAppState();
        try {
            TestableQuizResultController controller = new TestableQuizResultController();
            FakeHeaderController header = new FakeHeaderController();
            VBox resultBox = new VBox();
            Quiz quiz = createQuizWithId(21);
            User student = createUserWithId(8);
            student.setLanguage("lo");

            controller.questionsToLoad = List.of(
                    new QuizService.QuizQuestion(1, "CPU", "A", List.of("A", "B", "C", "D")),
                    new QuizService.QuizQuestion(2, "RAM", "B", List.of("A", "B", "C", "D")),
                    new QuizService.QuizQuestion(3, "ROM", "C", List.of("A", "B", "C", "D"))
            );
            AppState.selectedQuiz.set(quiz);
            AppState.currentUser.set(student);
            AppState.quizPoints.set(2);
            AppState.quizCorrectMap.put(0, true);
            AppState.quizCorrectMap.put(1, false);

            setPrivate(controller, "headerController", header);
            setPrivate(controller, "resultBox", resultBox);
            setPrivate(controller, "resources", new ListResourceBundle() {
                @Override
                protected Object[][] getContents() {
                    return new Object[][]{
                            {"quizResult.header", "Review"},
                            {"quizResult.subtitle", "Score {0}"},
                            {"quizResult.correct", "ຖືກ"},
                            {"quizResult.incorrect", "ຜິດ"},
                            {"quizResult.notAnswered", "ຍັງບໍ່ໄດ້ຕອບ"}
                    };
                }
            });

            callPrivate(controller, "initialize");

            assertEquals(21, controller.lastQuizId);
            assertEquals(8, controller.lastUserId);
            assertEquals("lo", controller.lastLanguage);
            assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
            assertEquals("Review", header.title);
            assertEquals("Score 2", header.subtitle);
            assertTrue(header.backVisible);
            assertEquals(3, resultBox.getChildren().size());
            assertEquals("ຖືກ", rightText(resultBox, 0));
            assertEquals("ຜິດ", rightText(resultBox, 1));
            assertEquals("ຍັງບໍ່ໄດ້ຕອບ", rightText(resultBox, 2));

            header.onBack.run();
            assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
        } finally {
            restoreAppState(snapshot);
        }
    }

    @Test
    void initialize_withoutSelectedQuizNavigatesBackWithoutLoadingQuestions() throws Exception {
        AppStateSnapshot snapshot = snapshotAppState();
        try {
            TestableQuizResultController controller = new TestableQuizResultController();
            setPrivate(controller, "resultBox", new VBox());
            AppState.selectedQuiz.set(null);

            callPrivate(controller, "initialize");

            assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
            assertEquals(0, controller.lastQuizId);
            assertNull(controller.lastLanguage);
        } finally {
            restoreAppState(snapshot);
        }
    }

    @Test
    void initialize_withoutCurrentUserNavigatesBackWithoutLoadingQuestions() throws Exception {
        AppStateSnapshot snapshot = snapshotAppState();
        try {
            TestableQuizResultController controller = new TestableQuizResultController();
            setPrivate(controller, "headerController", new FakeHeaderController());
            setPrivate(controller, "resultBox", new VBox());
            AppState.selectedQuiz.set(createQuizWithId(22));
            AppState.currentUser.set(null);

            callPrivate(controller, "initialize");

            assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
            assertEquals(0, controller.lastQuizId);
        } finally {
            restoreAppState(snapshot);
        }
    }

    @Test
    void restartClearsQuizStateAndNavigatesToQuizDetail() throws Exception {
        AppStateSnapshot snapshot = snapshotAppState();
        try {
            TestableQuizResultController controller = new TestableQuizResultController();
            AppState.quizAnswers.put(0, "A");
            AppState.quizCorrectMap.put(0, true);
            AppState.quizPoints.set(1);
            AppState.quizQuestionIndex.set(3);

            callPrivate(controller, "restart");

            assertTrue(AppState.quizAnswers.isEmpty());
            assertTrue(AppState.quizCorrectMap.isEmpty());
            assertEquals(0, AppState.quizPoints.get());
            assertEquals(0, AppState.quizQuestionIndex.get());
            assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
            assertEquals(AppState.Screen.QUIZ_DETAIL, controller.lastNavigatedScreen);
        } finally {
            restoreAppState(snapshot);
        }
    }

    @Test
    void backToList_setsQuizNavAndNavigatesToQuizzes() throws Exception {
        AppStateSnapshot snapshot = snapshotAppState();
        try {
            TestableQuizResultController controller = new TestableQuizResultController();

            callPrivate(controller, "backToList");

            assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
            assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
        } finally {
            restoreAppState(snapshot);
        }
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

    private Quiz createQuizWithId(int quizId) {
        Quiz quiz = new Quiz();
        setEntityField(Quiz.class, quiz, "quizId", quizId);
        return quiz;
    }

    private User createUserWithId(int userId) {
        User user = new User();
        user.setRole(0);
        setEntityField(User.class, user, "userId", userId);
        return user;
    }

    @Test
    void initializeRendersStatusesAndHeader() throws Exception {
        var originalLocale = LocaleManager.getLocale();
        User teacher = newUser("Teacher");
        User creator = newUser("Creator");
        User student = newUser("Student");
        ClassModel clazz = null;
        FlashcardSet fs = null;
        Flashcard f1 = null;
        Flashcard f2 = null;
        Flashcard f3 = null;
        ClassDetails enrollment = null;
        Quiz quiz = null;

        try {
            LocaleManager.setLocale("en", "US");
            userDao.persist(teacher);
            classDao.persist(clazz = newClass(teacher));
            setDao.persist(fs = newSet(clazz));
            userDao.persist(creator);
            userDao.persist(student);

            f1 = newFlashcard(fs, creator, "T1");
            f2 = newFlashcard(fs, creator, "T2");
            f3 = newFlashcard(fs, creator, "T3");
            flashcardDao.persist(f1);
            flashcardDao.persist(f2);
            flashcardDao.persist(f3);

            enrollment = new ClassDetails();
            enrollment.setStudent(student);
            enrollment.setClassModel(clazz);
            classDetailsDao.persist(enrollment);

            quiz = quizService.generateQuiz(student, 3);
            assertNotNull(quiz);

            AppState.currentUser.set(student);
            AppState.selectedQuiz.set(quiz);
            AppState.quizPoints.set(2);
            AppState.quizCorrectMap.clear();
            AppState.quizCorrectMap.put(0, true);
            AppState.quizCorrectMap.put(1, false);

            QuizResultController controller = new QuizResultController();
            FakeHeaderController header = new FakeHeaderController();
            VBox resultBox = new VBox();

            setPrivate(controller, "headerController", header);
            setPrivate(controller, "resultBox", resultBox);
            setPrivate(controller, "resources", ResourceBundle.getBundle("Messages", LocaleManager.getLocale()));

            Method m = QuizResultController.class.getDeclaredMethod("initialize");
            m.setAccessible(true);
            m.invoke(controller);

            assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
            assertTrue(header.backVisible);
            assertEquals("Result", header.title);
            assertEquals("Total points: 2", header.subtitle);
            assertEquals(3, resultBox.getChildren().size());

            assertEquals("Correct", rightText(resultBox, 0));
            assertEquals("Incorrect", rightText(resultBox, 1));
            assertEquals("Not answered", rightText(resultBox, 2));
        } finally {
            AppState.quizCorrectMap.clear();
            AppState.quizAnswers.clear();
            AppState.quizPoints.set(0);
            AppState.quizQuestionIndex.set(0);
            AppState.selectedQuiz.set(null);
            AppState.currentUser.set(null);
            LocaleManager.setLocale(originalLocale);

            if (quiz != null) {
                List<QuizDetails> details = quizDetailsDao.findByQuizId(quiz.getQuizId());
                for (QuizDetails d : details) quizDetailsDao.delete(d);
                quizDao.delete(quiz);
            }
            if (f3 != null) flashcardDao.delete(f3);
            if (f2 != null) flashcardDao.delete(f2);
            if (f1 != null) flashcardDao.delete(f1);
            if (enrollment != null) classDetailsDao.delete(enrollment);
            if (fs != null) setDao.delete(fs);
            if (clazz != null) classDao.delete(clazz);
            userDao.delete(student);
            userDao.delete(creator);
            userDao.delete(teacher);
        }
    }

    private void setPrivate(Object controller, String field, Object value) {
        try {
            Field f = QuizResultController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setEntityField(Class<?> type, Object target, String fieldName, Object value) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(Object controller, String methodName) {
        try {
            Method method = QuizResultController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(cause);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AppStateSnapshot snapshotAppState() {
        return new AppStateSnapshot(
                AppState.selectedQuiz.get(),
                AppState.currentUser.get(),
                AppState.navOverride.get(),
                AppState.quizQuestionIndex.get(),
                AppState.quizPoints.get(),
                new HashMap<>(AppState.quizAnswers),
                new HashMap<>(AppState.quizCorrectMap)
        );
    }

    private void restoreAppState(AppStateSnapshot snapshot) {
        AppState.selectedQuiz.set(snapshot.selectedQuiz());
        AppState.currentUser.set(snapshot.currentUser());
        AppState.navOverride.set(snapshot.navOverride());
        AppState.quizQuestionIndex.set(snapshot.quizQuestionIndex());
        AppState.quizPoints.set(snapshot.quizPoints());
        AppState.quizAnswers.clear();
        AppState.quizAnswers.putAll(snapshot.quizAnswers());
        AppState.quizCorrectMap.clear();
        AppState.quizCorrectMap.putAll(snapshot.quizCorrectMap());
    }

    private record AppStateSnapshot(
            Quiz selectedQuiz,
            User currentUser,
            AppState.NavItem navOverride,
            int quizQuestionIndex,
            int quizPoints,
            Map<Integer, String> quizAnswers,
            Map<Integer, Boolean> quizCorrectMap
    ) {}

    private String rightText(VBox box, int index) {
        HBox row = (HBox) box.getChildren().get(index);
        return ((Label) row.getChildren().get(1)).getText();
    }
}
