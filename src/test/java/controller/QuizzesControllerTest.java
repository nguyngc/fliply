package controller;

import controller.components.HeaderController;
import controller.components.QuizCardController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.Quiz;
import model.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizzesControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableQuizzesController controller;
    private FakeHeaderController headerController;
    private VBox listBox;
    private Label totalLabel;
    private User previousCurrentUser;
    private AppState.NavItem previousNavOverride;
    private List<Quiz> previousQuizList;
    private Quiz previousSelectedQuiz;
    private int previousQuestionIndex;
    private int previousPoints;
    private Map<Integer, String> previousAnswers;
    private Map<Integer, Boolean> previousCorrectMap;
    private ResourceBundle messages;

    private static final class TestableQuizzesController extends QuizzesController {
        private final List<Quiz> quizzesToReturn = new ArrayList<>();
        private Integer loadedUserId;
        private boolean failLoadingCard;
        private final List<FakeQuizCardController> createdCardControllers = new ArrayList<>();
        private AppState.Screen lastNavigatedScreen;

        @Override
        List<Quiz> loadQuizzesForUser(int userId) {
            loadedUserId = userId;
            return new ArrayList<>(quizzesToReturn);
        }

        @Override
        LoadedQuizCard loadQuizCardNode() throws java.io.IOException {
            if (failLoadingCard) {
                throw new java.io.IOException("boom");
            }
            FakeQuizCardController fakeController = new FakeQuizCardController();
            createdCardControllers.add(fakeController);
            return new LoadedQuizCard(new StackPane(), fakeController);
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    private static final class FakeHeaderController extends HeaderController {
        private String title;
        private String subtitle;

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }
    }

    private static final class FakeQuizCardController extends QuizCardController {
        private Quiz quiz;

        @Override
        public void setQuiz(Quiz quiz) {
            this.quiz = quiz;
        }
    }

    @BeforeEach
    void setUp() {
        previousCurrentUser = AppState.currentUser.get();
        previousNavOverride = AppState.navOverride.get();
        previousQuizList = new ArrayList<>(AppState.quizList);
        previousSelectedQuiz = AppState.selectedQuiz.get();
        previousQuestionIndex = AppState.quizQuestionIndex.get();
        previousPoints = AppState.quizPoints.get();
        previousAnswers = new HashMap<>(AppState.quizAnswers);
        previousCorrectMap = new HashMap<>(AppState.quizCorrectMap);

        controller = new TestableQuizzesController();
        headerController = new FakeHeaderController();
        listBox = new VBox();
        totalLabel = new Label();
        messages = ResourceBundle.getBundle("Messages");

        setPrivate("header", new StackPane());
        setPrivate("headerController", headerController);
        setPrivate("listBox", listBox);
        setPrivate("totalLabel", totalLabel);
        setPrivate("resources", messages);

        AppState.currentUser.set(null);
        AppState.navOverride.set(null);
        AppState.quizList.clear();
        AppState.selectedQuiz.set(null);
        AppState.quizQuestionIndex.set(7);
        AppState.quizPoints.set(3);
        AppState.quizAnswers.clear();
        AppState.quizAnswers.put(1, "A");
        AppState.quizCorrectMap.clear();
        AppState.quizCorrectMap.put(1, true);
    }

    @AfterEach
    void tearDown() {
        AppState.currentUser.set(previousCurrentUser);
        AppState.navOverride.set(previousNavOverride);
        AppState.quizList.clear();
        AppState.quizList.addAll(previousQuizList);
        AppState.selectedQuiz.set(previousSelectedQuiz);
        AppState.quizQuestionIndex.set(previousQuestionIndex);
        AppState.quizPoints.set(previousPoints);
        AppState.quizAnswers.clear();
        AppState.quizAnswers.putAll(previousAnswers);
        AppState.quizCorrectMap.clear();
        AppState.quizCorrectMap.putAll(previousCorrectMap);
    }

    @Test
    void initialize_withoutCurrentUser_returnsEarly() {
        listBox.getChildren().add(new Label("existing"));
        totalLabel.setText("old");

        runOnFxThread(() -> callPrivate("initialize"));

        assertNull(controller.loadedUserId);
        assertEquals(1, listBox.getChildren().size());
        assertEquals("old", totalLabel.getText());
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void initialize_loadsQuizzesUpdatesHeaderAndHandlesCardAndAddTileClicks() {
        Quiz quiz1 = createQuiz(11, 4);
        Quiz quiz2 = createQuiz(12, 6);
        AppState.currentUser.set(createUser(9));
        controller.quizzesToReturn.add(quiz1);
        controller.quizzesToReturn.add(quiz2);

        runOnFxThread(() -> callPrivate("initialize"));

        assertEquals(9, controller.loadedUserId);
        assertEquals(List.of(quiz1, quiz2), new ArrayList<>(AppState.quizList));
        assertEquals("My Quizzes", headerController.title);
        assertEquals("Total: 2", headerController.subtitle);
        assertEquals("Total: 2", totalLabel.getText());
        assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
        assertEquals(3, listBox.getChildren().size());
        assertSame(quiz1, controller.createdCardControllers.get(0).quiz);
        assertSame(quiz2, controller.createdCardControllers.get(1).quiz);

        Node firstCard = listBox.getChildren().get(0);
        firstCard.getOnMouseClicked().handle(null);
        assertSame(quiz1, AppState.selectedQuiz.get());
        assertEquals(0, AppState.quizQuestionIndex.get());
        assertEquals(0, AppState.quizPoints.get());
        assertTrue(AppState.quizAnswers.isEmpty());
        assertTrue(AppState.quizCorrectMap.isEmpty());
        assertEquals(AppState.Screen.QUIZ_DETAIL, controller.lastNavigatedScreen);

        Node addTile = listBox.getChildren().get(2);
        addTile.getOnMouseClicked().handle(null);
        assertEquals(AppState.Screen.QUIZ_FORM, controller.lastNavigatedScreen);
    }

    @Test
    void initialize_withMissingResourceKeys_usesFallbackMessages() {
        AppState.currentUser.set(createUser(10));
        controller.quizzesToReturn.add(createQuiz(13, 2));
        setPrivate("resources", new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{"other.key", "value"}};
            }
        });

        runOnFxThread(() -> callPrivate("initialize"));

        assertEquals("My Quizzes", headerController.title);
        assertEquals("Total: 1", headerController.subtitle);
        assertEquals("Total: 1", totalLabel.getText());
    }

    @Test
    void initialize_withoutQuizzesShowsEmptyStateAndAddTile() {
        AppState.currentUser.set(createUser(14));

        runOnFxThread(() -> callPrivate("initialize"));

        assertEquals("Total: 0", headerController.subtitle);
        assertEquals("Total: 0", totalLabel.getText());
        assertEquals(2, listBox.getChildren().size());
        assertTrue(collectLabelTexts(listBox.getChildren().get(0)).contains(messages.getString("quizzes.empty.title")));
    }

    @Test
    void loadQuizCard_whenLoaderFails_wrapsException() {
        controller.failLoadingCard = true;
        Quiz quiz = createQuiz(22, 3);

        IllegalStateException exception = runOnFxThreadWithResult(() ->
                assertThrows(IllegalStateException.class,
                        () -> callPrivate("loadQuizCard", new Class<?>[]{Quiz.class}, quiz))
        );

        assertEquals("Failed to load quiz_card.fxml", exception.getMessage());
    }

    private User createUser(int userId) {
        User user = new User();
        setField(User.class, user, "userId", userId);
        return user;
    }

    private Quiz createQuiz(int quizId, int noOfQuestions) {
        Quiz quiz = new Quiz();
        setField(Quiz.class, quiz, "quizId", quizId);
        quiz.setNoOfQuestions(noOfQuestions);
        return quiz;
    }

    private void setPrivate(String fieldName, Object value) {
        setField(QuizzesController.class, controller, fieldName, value);
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

    private Object callPrivate(String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = QuizzesController.class.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(controller, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(cause);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        callPrivate(methodName, new Class<?>[0]);
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

    private List<String> collectLabelTexts(Node node) {
        List<String> texts = new ArrayList<>();
        collect(node, texts);
        return texts;
    }

    private void collect(Node node, List<String> texts) {
        if (node instanceof Label label) {
            texts.add(label.getText());
        }
        if (node instanceof javafx.scene.Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collect(child, texts);
            }
        }
    }
}
