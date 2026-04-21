package controller;

import controller.components.HeaderController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TextField;
import model.AppState;
import model.entity.Quiz;
import model.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.I18n;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizFormControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableQuizFormController controller;
    private FakeHeaderController headerController;
    private TextField countField;
    private User previousCurrentUser;
    private Quiz previousSelectedQuiz;
    private AppState.NavItem previousNavOverride;
    private int previousQuestionIndex;
    private int previousPoints;
    private Map<Integer, String> previousAnswers;
    private Map<Integer, Boolean> previousCorrectMap;

    private static final class TestableQuizFormController extends QuizFormController {
        private int availableCount;
        private Quiz generatedQuiz;
        private User generatedForUser;
        private Integer generatedCount;
        private String warningMessage;
        private AppState.Screen lastNavigatedScreen;

        @Override
        int getAvailableFlashcardCount(User user) {
            return availableCount;
        }

        @Override
        Quiz generateQuiz(User user, int count) {
            generatedForUser = user;
            generatedCount = count;
            return generatedQuiz;
        }

        @Override
        void showWarning(String message) {
            warningMessage = message;
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    private static final class FakeHeaderController extends HeaderController {
        private String title;
        private boolean backVisible;
        private Runnable backAction;

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public void setBackVisible(boolean visible) {
            backVisible = visible;
        }

        @Override
        public void setOnBack(Runnable action) {
            backAction = action;
        }
    }

    @BeforeEach
    void setUp() {
        previousCurrentUser = AppState.currentUser.get();
        previousSelectedQuiz = AppState.selectedQuiz.get();
        previousNavOverride = AppState.navOverride.get();
        previousQuestionIndex = AppState.quizQuestionIndex.get();
        previousPoints = AppState.quizPoints.get();
        previousAnswers = new HashMap<>(AppState.quizAnswers);
        previousCorrectMap = new HashMap<>(AppState.quizCorrectMap);

        controller = new TestableQuizFormController();
        headerController = new FakeHeaderController();
        countField = new TextField();

        setPrivate("headerController", headerController);
        setPrivate("countField", countField);
        setPrivate("resources", ResourceBundle.getBundle("Messages"));

        AppState.currentUser.set(null);
        AppState.selectedQuiz.set(null);
        AppState.navOverride.set(null);
        AppState.quizQuestionIndex.set(0);
        AppState.quizPoints.set(0);
        AppState.quizAnswers.clear();
        AppState.quizCorrectMap.clear();
    }

    @AfterEach
    void tearDown() {
        AppState.currentUser.set(previousCurrentUser);
        AppState.selectedQuiz.set(previousSelectedQuiz);
        AppState.navOverride.set(previousNavOverride);
        AppState.quizQuestionIndex.set(previousQuestionIndex);
        AppState.quizPoints.set(previousPoints);
        AppState.quizAnswers.clear();
        AppState.quizAnswers.putAll(previousAnswers);
        AppState.quizCorrectMap.clear();
        AppState.quizCorrectMap.putAll(previousCorrectMap);
    }

    @Test
    void i18nMessage_returnsBundleValueOrFallback() {
        ResourceBundle bundle = ResourceBundle.getBundle("Messages");

        assertEquals("New Quiz", I18n.message(bundle, "quizForm.header", "fallback"));
        assertEquals("fallback", I18n.message(null, "quizForm.header", "fallback"));
        assertEquals("fallback", I18n.message(bundle, "missing.key", "fallback"));
    }

    @Test
    void initialize_setsHeaderAndBackAction() {
        runOnFxThread(() -> callPrivate("initialize"));

        assertEquals("New Quiz", headerController.title);
        assertTrue(headerController.backVisible);
        assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());

        headerController.backAction.run();
        assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
    }

    @Test
    void generate_withoutCurrentUser_doesNothing() {
        countField.setText("3");

        callPrivate("generate");

        assertNull(controller.generatedCount);
        assertNull(controller.warningMessage);
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void generate_withInvalidInputShowsWarningAndSkipsQuizGeneration() {
        AppState.currentUser.set(createUser(8));
        controller.availableCount = 3;
        countField.setText("4");

        runOnFxThread(() -> callPrivate("generate"));

        assertEquals("Only 3 flashcards are available. Please enter a smaller number.", controller.warningMessage);
        assertNull(controller.generatedCount);
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void generate_whenServiceReturnsNullShowsWarning() {
        AppState.currentUser.set(createUser(8));
        controller.availableCount = 5;
        countField.setText("2");

        runOnFxThread(() -> callPrivate("generate"));

        assertEquals("There are no flashcards available.", controller.warningMessage);
        assertEquals(2, controller.generatedCount);
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void generate_successStoresQuizResetsStateAndNavigates() {
        User user = createUser(9);
        Quiz quiz = createQuiz(14, 3);
        AppState.currentUser.set(user);
        AppState.quizQuestionIndex.set(5);
        AppState.quizPoints.set(4);
        AppState.quizAnswers.put(1, "A");
        AppState.quizCorrectMap.put(1, true);
        controller.availableCount = 5;
        controller.generatedQuiz = quiz;
        countField.setText("3");

        runOnFxThread(() -> callPrivate("generate"));

        assertSame(user, controller.generatedForUser);
        assertEquals(3, controller.generatedCount);
        assertSame(quiz, AppState.selectedQuiz.get());
        assertEquals(0, AppState.quizQuestionIndex.get());
        assertEquals(0, AppState.quizPoints.get());
        assertTrue(AppState.quizAnswers.isEmpty());
        assertTrue(AppState.quizCorrectMap.isEmpty());
        assertEquals(AppState.Screen.QUIZ_DETAIL, controller.lastNavigatedScreen);
    }

    @Test
    void validateQuestionCount_returnsExpectedMessages() {
        assertEquals("Please enter the number of questions.",
                callValidateQuestionCount("   ", 5));
        assertEquals("Please enter a valid positive number.",
                callValidateQuestionCount("abc", 5));
        assertEquals("Please enter a valid positive number.",
                callValidateQuestionCount("0", 5));
        assertEquals("There are no flashcards available.",
                callValidateQuestionCount("1", 0));
        assertNull(callValidateQuestionCount("3", 5));
    }

    @Test
    void cancel_navigatesBackToQuizzes() {
        runOnFxThread(() -> callPrivate("cancel"));

        assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
        assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
    }

    private User createUser(int userId) {
        User user = new User();
        setField(User.class, user, "userId", userId);
        return user;
    }

    private Quiz createQuiz(int quizId, int questionCount) {
        Quiz quiz = new Quiz();
        setField(Quiz.class, quiz, "quizId", quizId);
        quiz.setNoOfQuestions(questionCount);
        return quiz;
    }

    private String callValidateQuestionCount(String rawCount, int availableCount) {
        try {
            Method method = QuizFormController.class.getDeclaredMethod("validateQuestionCount", String.class, int.class);
            method.setAccessible(true);
            return (String) method.invoke(controller, rawCount, availableCount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setPrivate(String fieldName, Object value) {
        setField(QuizFormController.class, controller, fieldName, value);
    }

    private void setField(Class<?> type, Object target, String fieldName, Object value) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method method = QuizFormController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runOnFxThread(Runnable action) {
        runOnFxThreadWithResult(() -> {
            action.run();
            return null;
        });
    }

    private <T> T runOnFxThreadWithResult(Callable<T> action) {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(action.call());
            } catch (Throwable throwable) {
                error.set(throwable);
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
        return result.get();
    }
}
