package controller;

import controller.components.ClassCardController;
import model.AppState;
import model.entity.Flashcard;
import model.entity.Quiz;
import model.entity.User;
import model.service.QuizService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerGuardLogicTest {

    private Quiz previousSelectedQuiz;
    private model.entity.ClassModel previousSelectedClass;
    private User previousCurrentUser;
    private AppState.Role previousRole;
    private AppState.NavItem previousNavOverride;
    private int previousQuestionIndex;
    private int previousPoints;
    private Map<Integer, String> previousAnswers;
    private Map<Integer, Boolean> previousCorrectMap;

    private static final class TestableQuizDetailController extends QuizDetailController {
        AppState.Screen lastNavigatedScreen;
        String lastLanguage;
        int renderCalls;

        @Override
        List<QuizService.QuizQuestion> loadQuestions(int quizId, int userId, String language) {
            lastLanguage = language;
            return List.of(new QuizService.QuizQuestion(1, "CPU", "A", List.of("A", "B", "C", "D")));
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }

        @Override
        void render() {
            renderCalls++;
        }
    }

    private static final class TestableQuizResultController extends QuizResultController {
        AppState.Screen lastNavigatedScreen;
        String lastLanguage;
        int renderCalls;

        @Override
        List<QuizService.QuizQuestion> loadQuestions(int quizId, int userId, String language) {
            lastLanguage = language;
            return List.of(new QuizService.QuizQuestion(1, "CPU", "A", List.of("A", "B", "C", "D")));
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }

        @Override
        void renderResults() {
            renderCalls++;
        }
    }

    private static final class TestableTeacherAddClassController extends TeacherAddClassController {
        boolean blankWarningShown;
        String createdCode;
        RuntimeException createFailure;
        AppState.Screen lastNavigatedScreen;

        @Override
        void showBlankCodeWarning() {
            blankWarningShown = true;
        }

        @Override
        void createClass(String code) {
            if (createFailure != null) {
                throw createFailure;
            }
            createdCode = code;
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    private static final class TestableHomeController extends HomeController {
        AppState.Screen lastNavigatedScreen;

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    @BeforeEach
    void setUp() {
        previousSelectedQuiz = AppState.selectedQuiz.get();
        previousSelectedClass = AppState.selectedClass.get();
        previousCurrentUser = AppState.currentUser.get();
        previousRole = AppState.role.get();
        previousNavOverride = AppState.navOverride.get();
        previousQuestionIndex = AppState.quizQuestionIndex.get();
        previousPoints = AppState.quizPoints.get();
        previousAnswers = new HashMap<>(AppState.quizAnswers);
        previousCorrectMap = new HashMap<>(AppState.quizCorrectMap);

        AppState.selectedQuiz.set(null);
        AppState.selectedClass.set(null);
        AppState.currentUser.set(null);
        AppState.role.set(AppState.Role.STUDENT);
        AppState.navOverride.set(null);
        AppState.quizQuestionIndex.set(0);
        AppState.quizPoints.set(0);
        AppState.quizAnswers.clear();
        AppState.quizCorrectMap.clear();
    }

    @AfterEach
    void tearDown() {
        AppState.selectedQuiz.set(previousSelectedQuiz);
        AppState.selectedClass.set(previousSelectedClass);
        AppState.currentUser.set(previousCurrentUser);
        AppState.role.set(previousRole);
        AppState.navOverride.set(previousNavOverride);
        AppState.quizQuestionIndex.set(previousQuestionIndex);
        AppState.quizPoints.set(previousPoints);
        AppState.quizAnswers.clear();
        AppState.quizAnswers.putAll(previousAnswers);
        AppState.quizCorrectMap.clear();
        AppState.quizCorrectMap.putAll(previousCorrectMap);
    }

    @Test
    void quizDetailInitialize_withoutSelectedQuizNavigatesBackBeforeLoadingQuestions() {
        TestableQuizDetailController controller = new TestableQuizDetailController();

        callPrivate(QuizDetailController.class, controller, "initialize");

        assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
        assertNull(controller.lastLanguage);
    }

    @Test
    void quizDetailInitialize_withoutCurrentUserNavigatesBackBeforeLoadingQuestions() {
        TestableQuizDetailController controller = new TestableQuizDetailController();
        AppState.selectedQuiz.set(createQuizWithId(3));

        callPrivate(QuizDetailController.class, controller, "initialize");

        assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
        assertNull(controller.lastLanguage);
    }

    @Test
    void quizDetailInitialize_withCurrentUserPassesLanguageAndRenders() {
        TestableQuizDetailController controller = new TestableQuizDetailController();
        User student = createUserWithId(5);
        student.setLanguage("vi");
        AppState.selectedQuiz.set(createQuizWithId(3));
        AppState.currentUser.set(student);

        callPrivate(QuizDetailController.class, controller, "initialize");

        assertEquals("vi", controller.lastLanguage);
        assertEquals(1, controller.renderCalls);
        assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
    }

    @Test
    void quizDetailViewResult_setsQuizNavAndNavigatesToResult() {
        TestableQuizDetailController controller = new TestableQuizDetailController();

        callPrivate(QuizDetailController.class, controller, "viewResult");

        assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
        assertEquals(AppState.Screen.QUIZ_RESULT, controller.lastNavigatedScreen);
    }

    @Test
    void quizResultInitialize_withoutSelectedQuizNavigatesBackBeforeLoadingQuestions() {
        TestableQuizResultController controller = new TestableQuizResultController();

        callPrivate(QuizResultController.class, controller, "initialize");

        assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
        assertNull(controller.lastLanguage);
    }

    @Test
    void quizResultInitialize_withoutCurrentUserNavigatesBackBeforeLoadingQuestions() {
        TestableQuizResultController controller = new TestableQuizResultController();
        AppState.selectedQuiz.set(createQuizWithId(4));

        callPrivate(QuizResultController.class, controller, "initialize");

        assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
        assertNull(controller.lastLanguage);
    }

    @Test
    void quizResultInitialize_withCurrentUserPassesLanguageAndRenders() {
        TestableQuizResultController controller = new TestableQuizResultController();
        User student = createUserWithId(6);
        student.setLanguage("lo");
        AppState.selectedQuiz.set(createQuizWithId(4));
        AppState.currentUser.set(student);

        callPrivate(QuizResultController.class, controller, "initialize");

        assertEquals("lo", controller.lastLanguage);
        assertEquals(1, controller.renderCalls);
        assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
    }

    @Test
    void quizResultRestart_clearsQuizStateAndNavigatesToQuizDetail() {
        TestableQuizResultController controller = new TestableQuizResultController();
        AppState.quizAnswers.put(0, "A");
        AppState.quizCorrectMap.put(0, true);
        AppState.quizPoints.set(1);
        AppState.quizQuestionIndex.set(2);

        callPrivate(QuizResultController.class, controller, "restart");

        assertTrue(AppState.quizAnswers.isEmpty());
        assertTrue(AppState.quizCorrectMap.isEmpty());
        assertEquals(0, AppState.quizPoints.get());
        assertEquals(0, AppState.quizQuestionIndex.get());
        assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
        assertEquals(AppState.Screen.QUIZ_DETAIL, controller.lastNavigatedScreen);
    }

    @Test
    void quizResultBackToList_setsQuizNavAndNavigatesToQuizzes() {
        TestableQuizResultController controller = new TestableQuizResultController();

        callPrivate(QuizResultController.class, controller, "backToList");

        assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
        assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
    }

    @Test
    void flashcardDetailStudentLanguage_defaultsToEnglishWhenUserOrLanguageIsMissing() {
        FlashcardDetailController controller = new FlashcardDetailController();

        assertEquals("en", controller.getStudentLanguage());

        User user = createUserWithId(9);
        AppState.currentUser.set(user);
        assertEquals("en", controller.getStudentLanguage());

        user.setLanguage("   ");
        assertEquals("en", controller.getStudentLanguage());
    }

    @Test
    void flashcardDetailStudentLanguage_usesCurrentUserLanguage() {
        FlashcardDetailController controller = new FlashcardDetailController();
        User user = createUserWithId(10);
        user.setLanguage("vi");
        AppState.currentUser.set(user);

        assertEquals("vi", controller.getStudentLanguage());
    }

    @Test
    void flashcardDetailMatchesCard_usesIdWhenAvailableAndIdentityOtherwise() {
        FlashcardDetailController controller = new FlashcardDetailController();
        Flashcard target = new Flashcard();
        Flashcard sameId = new Flashcard();
        Flashcard differentId = new Flashcard();

        setEntityField(Flashcard.class, target, "flashcardId", 5);
        setEntityField(Flashcard.class, sameId, "flashcardId", 5);
        setEntityField(Flashcard.class, differentId, "flashcardId", 6);

        assertTrue(controller.matchesCard(sameId, target, 5));
        assertFalse(controller.matchesCard(differentId, target, 5));
        assertFalse(controller.matchesCard(null, target, 5));
        assertFalse(controller.matchesCard(sameId, null, 5));
        assertTrue(controller.matchesCard(target, target, null));
        assertFalse(controller.matchesCard(new Flashcard(), target, null));
    }

    @Test
    void accountHelpRoleKeyAndMessageUseRoleSuffixesAndFallbacks() {
        AccountHelpController controller = new AccountHelpController();
        setEntityField(AccountHelpController.class, controller, "effectiveResources", new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{"present.key", "Localized value"}};
            }
        });

        assertEquals("help.roleGuide.body.teacher", controller.roleKey("help.roleGuide.body", true));
        assertEquals("help.roleGuide.body.student", controller.roleKey("help.roleGuide.body", false));
        assertEquals("Localized value", controller.message("present.key", "Fallback"));
        assertEquals("Fallback", controller.message("missing.key", "Fallback"));
    }

    @Test
    void accountHelpPopulateContentRunsTeacherAndStudentBranchesWithoutLabels() {
        AccountHelpController controller = new AccountHelpController();
        setEntityField(AccountHelpController.class, controller, "effectiveResources", new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{
                        {"help.quickStart.title", "Quick"},
                        {"help.quickStart.body", "Start"},
                        {"help.roleGuide.title.teacher", "Teacher"},
                        {"help.roleGuide.body.teacher", "Teacher body"},
                        {"help.roleGuide.title.student", "Student"},
                        {"help.roleGuide.body.student", "Student body"},
                        {"help.commonTasks.title", "Tasks"},
                        {"help.commonTasks.body.teacher", "Teacher tasks"},
                        {"help.commonTasks.body.student", "Student tasks"},
                        {"help.faq.title", "FAQ"},
                        {"help.faq.body.teacher", "Teacher faq"},
                        {"help.faq.body.student", "Student faq"}
                };
            }
        });

        AppState.role.set(AppState.Role.TEACHER);
        controller.populateHelpContent();

        AppState.role.set(AppState.Role.STUDENT);
        controller.populateHelpContent();
    }

    @Test
    void teacherAddSetValidationAndMessagesUseFallbacksWhenLocalizationIsMissing() {
        TeacherAddSetController controller = new TeacherAddSetController();

        assertEquals(
                "Please enter a subject and upload a flashcard file.",
                controller.validateRequiredInput(null, "Science")
        );
        assertEquals(
                "Please enter a subject and upload a flashcard file.",
                controller.validateRequiredInput(new File("cards.csv"), "   ")
        );
        assertNull(controller.validateRequiredInput(new File("cards.csv"), "Science"));
        assertEquals("Fallback", controller.message("missing.key", "Fallback"));
    }

    @Test
    void teacherAddSetValidationAndMessagesUseLocalizedStringsWhenAvailable() {
        TeacherAddSetController controller = new TeacherAddSetController();
        setEntityField(TeacherAddSetController.class, controller, "localizedStrings", Map.of(
                "teacherAddSet.warning.missingInfo", "Localized missing info",
                "teacherAddSet.alertTitle", "Localized title"
        ));

        assertEquals("Localized missing info", controller.validateRequiredInput(null, "Science"));
        assertEquals("Localized title", controller.message("teacherAddSet.alertTitle", "Fallback"));
    }

    @Test
    void teacherAddClassAddClass_blankInputShowsWarningWithoutCreatingClass() {
        TestableTeacherAddClassController controller = new TestableTeacherAddClassController();

        controller.addClass("   ");

        assertTrue(controller.blankWarningShown);
        assertNull(controller.createdCode);
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void teacherAddClassAddClass_trimsCreatesClassAndNavigates() {
        TestableTeacherAddClassController controller = new TestableTeacherAddClassController();

        controller.addClass("  SEP-2026  ");

        assertEquals("SEP-2026", controller.createdCode);
        assertEquals(AppState.Screen.CLASSES, controller.lastNavigatedScreen);
    }

    @Test
    void teacherAddClassAddClass_whenServiceRejectsCodeDoesNotNavigate() {
        TestableTeacherAddClassController controller = new TestableTeacherAddClassController();
        controller.createFailure = new IllegalArgumentException("Duplicate");

        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TeacherAddClassController.class.getName());
        java.util.logging.Level previousLevel = logger.getLevel();
        try {
            logger.setLevel(java.util.logging.Level.OFF);
            controller.addClass("SEP-2026");
        } finally {
            logger.setLevel(previousLevel);
        }

        assertNull(controller.createdCode);
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void homeOpenClassStoresSelectedClassAndNavigatesToClasses() {
        TestableHomeController controller = new TestableHomeController();
        model.entity.ClassModel classModel = new model.entity.ClassModel();

        controller.openClass(classModel);

        assertEquals(classModel, AppState.selectedClass.get());
        assertEquals(AppState.Screen.CLASSES, controller.lastNavigatedScreen);
    }

    @Test
    void homeLatestClassCardViewReturnsNodeAndController() {
        ClassCardController cardController = new ClassCardController();
        HomeController.LatestClassCardView view = new HomeController.LatestClassCardView(null, cardController);

        assertNull(view.node());
        assertEquals(cardController, view.controller());
    }

    private Quiz createQuizWithId(int quizId) {
        Quiz quiz = new Quiz();
        setEntityField(Quiz.class, quiz, "quizId", quizId);
        return quiz;
    }

    private User createUserWithId(int userId) {
        User user = new User();
        setEntityField(User.class, user, "userId", userId);
        return user;
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

    private void callPrivate(Class<?> type, Object target, String methodName) {
        try {
            Method method = type.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(target);
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
}
