package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import model.AppState;
import model.entity.Quiz;
import model.entity.User;
import model.service.QuizService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizDetailControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableQuizDetailController controller;
    private FakeHeaderController headerController;
    private Label termLabel;
    private Label pageLabel;
    private Button opt1;
    private Button opt2;
    private Button opt3;
    private Button opt4;
    private Button prevBtn;
    private Button nextBtn;
    private HBox navigationBox;
    private Button viewResultBtn;
    private Quiz previousSelectedQuiz;
    private User previousCurrentUser;
    private AppState.NavItem previousNavOverride;
    private int previousQuestionIndex;
    private int previousPoints;
    private Map<Integer, String> previousAnswers;
    private Map<Integer, Boolean> previousCorrectMap;

    private static final class TestableQuizDetailController extends QuizDetailController {
        private List<QuizService.QuizQuestion> questionsToLoad = List.of();
        private AppState.Screen lastNavigatedScreen;

        @Override
        List<QuizService.QuizQuestion> loadQuestions(int quizId, int userId) {
            return questionsToLoad;
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    private static final class FakeHeaderController extends HeaderController {
        private String title;
        private String subtitle;
        private boolean backVisible;
        private Runnable backAction;

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
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
        previousSelectedQuiz = AppState.selectedQuiz.get();
        previousCurrentUser = AppState.currentUser.get();
        previousNavOverride = AppState.navOverride.get();
        previousQuestionIndex = AppState.quizQuestionIndex.get();
        previousPoints = AppState.quizPoints.get();
        previousAnswers = new HashMap<>(AppState.quizAnswers);
        previousCorrectMap = new HashMap<>(AppState.quizCorrectMap);

        controller = new TestableQuizDetailController();
        headerController = new FakeHeaderController();
        termLabel = new Label();
        pageLabel = new Label();
        opt1 = new Button();
        opt2 = new Button();
        opt3 = new Button();
        opt4 = new Button();
        prevBtn = new Button();
        nextBtn = new Button();
        navigationBox = new HBox();
        viewResultBtn = new Button();

        setPrivate("header", new Parent() {});
        setPrivate("headerController", headerController);
        setPrivate("termLabel", termLabel);
        setPrivate("pageLabel", pageLabel);
        setPrivate("opt1", opt1);
        setPrivate("opt2", opt2);
        setPrivate("opt3", opt3);
        setPrivate("opt4", opt4);
        setPrivate("prevBtn", prevBtn);
        setPrivate("nextBtn", nextBtn);
        setPrivate("navigationBox", navigationBox);
        setPrivate("viewResultBtn", viewResultBtn);

        AppState.selectedQuiz.set(null);
        AppState.currentUser.set(createUser(5));
        AppState.navOverride.set(null);
        AppState.quizQuestionIndex.set(0);
        AppState.quizPoints.set(0);
        AppState.quizAnswers.clear();
        AppState.quizCorrectMap.clear();
    }

    @AfterEach
    void tearDown() {
        AppState.selectedQuiz.set(previousSelectedQuiz);
        AppState.currentUser.set(previousCurrentUser);
        AppState.navOverride.set(previousNavOverride);
        AppState.quizQuestionIndex.set(previousQuestionIndex);
        AppState.quizPoints.set(previousPoints);
        AppState.quizAnswers.clear();
        AppState.quizAnswers.putAll(previousAnswers);
        AppState.quizCorrectMap.clear();
        AppState.quizCorrectMap.putAll(previousCorrectMap);
    }

    @Test
    void initialize_withoutSelectedQuiz_navigatesToQuizzes() {
        callPrivate("initialize");

        assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
    }

    @Test
    void initialize_usesResourcesAndRendersFirstQuestion() {
        Quiz quiz = createQuiz(7);
        controller.questionsToLoad = List.of(
                question(1, "Capital of France?", "Paris", "Paris", "Rome", "Berlin", "Madrid"),
                question(2, "2 + 2", "4", "3", "4", "5", "6")
        );
        setPrivate("resources", new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{
                        {"quizDetail.header", "Quiz Number {0}"},
                        {"quizDetail.subtitle", "Points {0}"}
                };
            }
        });
        AppState.selectedQuiz.set(quiz);

        callPrivate("initialize");

        assertEquals("Quiz Number 7", headerController.title);
        assertEquals("Points 0", headerController.subtitle);
        assertTrue(headerController.backVisible);
        assertEquals(NodeOrientation.LEFT_TO_RIGHT, navigationBox.getNodeOrientation());
        assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
        assertEquals("Capital of France?", termLabel.getText());
        assertEquals("1 / 2", pageLabel.getText());
        assertEquals("Paris", opt1.getText());
        assertEquals("Rome", opt2.getText());
        assertEquals("Berlin", opt3.getText());
        assertEquals("Madrid", opt4.getText());
        assertTrue(opt1.isWrapText());
        assertTrue(prevBtn.isDisable());
        assertTrue(nextBtn.isVisible());
        assertFalse(viewResultBtn.isVisible());

        headerController.backAction.run();
        assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
    }

    @Test
    void initialize_withMissingResourceKeys_usesFallbackMessages() {
        AppState.selectedQuiz.set(createQuiz(12));
        controller.questionsToLoad = List.of(question(1, "Question", "A", "A", "B", "C", "D"));
        setPrivate("resources", new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{"other.key", "value"}};
            }
        });

        callPrivate("initialize");

        assertEquals("Quiz #12", headerController.title);
        assertEquals("Total points: 0", headerController.subtitle);
    }

    @Test
    void render_clampsIndexAndRestoresAnsweredWrongLastQuestionState() {
        AppState.selectedQuiz.set(createQuiz(9));
        controller.questionsToLoad = List.of(
                question(1, "Q1", "A", "A", "B", "C", "D"),
                question(2, "Q2", "Correct", "Wrong", "Correct", "Other", "Else")
        );
        callPrivate("initialize");

        AppState.quizQuestionIndex.set(99);
        AppState.quizAnswers.put(1, "Wrong");
        AppState.quizCorrectMap.put(1, false);
        AppState.quizPoints.set(0);

        callPrivate("render");

        assertEquals(1, AppState.quizQuestionIndex.get());
        assertEquals("Q2", termLabel.getText());
        assertEquals("2 / 2", pageLabel.getText());
        assertFalse(nextBtn.isVisible());
        assertTrue(viewResultBtn.isVisible());
        assertTrue(opt1.isDisable());
        assertTrue(opt2.isDisable());
        assertTrue(opt3.isDisable());
        assertTrue(opt4.isDisable());
        assertTrue(opt1.getStyle().contains("255,0,0"));
        assertTrue(opt2.getStyle().contains("#3D8FEF"));
    }

    @Test
    void chooseOption_correctAnswer_updatesStateAndSubtitle() {
        AppState.selectedQuiz.set(createQuiz(3));
        controller.questionsToLoad = List.of(question(1, "Q1", "A", "A", "B", "C", "D"));
        callPrivate("initialize");

        callChooseOption(opt1);

        assertEquals("A", AppState.quizAnswers.get(0));
        assertTrue(AppState.quizCorrectMap.get(0));
        assertEquals(1, AppState.quizPoints.get());
        assertTrue(opt1.getStyle().contains("#3D8FEF"));
        assertTrue(opt1.isDisable());
        assertTrue(viewResultBtn.isVisible());
        assertEquals("Total points: 1", headerController.subtitle);
    }

    @Test
    void chooseOption_wrongAnswer_marksWrongAndCorrectButtons() {
        AppState.selectedQuiz.set(createQuiz(4));
        controller.questionsToLoad = List.of(question(1, "Q1", "B", "A", "B", "C", "D"));
        callPrivate("initialize");

        callChooseOption(opt1);

        assertEquals("A", AppState.quizAnswers.get(0));
        assertFalse(AppState.quizCorrectMap.get(0));
        assertEquals(0, AppState.quizPoints.get());
        assertTrue(opt1.getStyle().contains("255,0,0"));
        assertTrue(opt2.getStyle().contains("#3D8FEF"));
    }

    @Test
    void chooseOption_whenAlreadyAnswered_doesNothing() {
        AppState.selectedQuiz.set(createQuiz(5));
        controller.questionsToLoad = List.of(question(1, "Q1", "A", "A", "B", "C", "D"));
        callPrivate("initialize");
        AppState.quizAnswers.put(0, "A");
        AppState.quizCorrectMap.put(0, true);
        AppState.quizPoints.set(1);
        opt1.setStyle("before");

        callChooseOption(opt2);

        assertEquals("A", AppState.quizAnswers.get(0));
        assertEquals(1, AppState.quizPoints.get());
        assertEquals("before", opt1.getStyle());
    }

    @Test
    void prevAndNext_updateQuestionIndexAndRender() {
        AppState.selectedQuiz.set(createQuiz(6));
        controller.questionsToLoad = List.of(
                question(1, "First", "A", "A", "B", "C", "D"),
                question(2, "Second", "B", "A", "B", "C", "D")
        );
        callPrivate("initialize");

        callPrivate("next");
        assertEquals(1, AppState.quizQuestionIndex.get());
        assertEquals("Second", termLabel.getText());
        assertFalse(prevBtn.isDisable());

        callPrivate("prev");
        assertEquals(0, AppState.quizQuestionIndex.get());
        assertEquals("First", termLabel.getText());
        assertTrue(prevBtn.isDisable());
    }

    @Test
    void viewResult_setsNavOverrideAndNavigates() {
        callPrivate("viewResult");

        assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
        assertEquals(AppState.Screen.QUIZ_RESULT, controller.lastNavigatedScreen);
    }

    @Test
    void render_withNullHeaderControllerSkipsSubtitleUpdate() {
        AppState.selectedQuiz.set(createQuiz(11));
        controller.questionsToLoad = List.of(question(1, "Q1", "A", "A", "B", "C", "D"));
        setPrivate("headerController", null);

        callPrivate("initialize");

        assertEquals("Q1", termLabel.getText());
        assertEquals("1 / 1", pageLabel.getText());
    }

    private User createUser(int userId) {
        User user = new User();
        setEntityField(User.class, user, "userId", userId);
        return user;
    }

    private Quiz createQuiz(int quizId) {
        Quiz quiz = new Quiz();
        setEntityField(Quiz.class, quiz, "quizId", quizId);
        return quiz;
    }

    private QuizService.QuizQuestion question(int flashcardId, String prompt, String correct, String... options) {
        return new QuizService.QuizQuestion(flashcardId, prompt, correct, List.of(options));
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

    private void setPrivate(String fieldName, Object value) {
        try {
            Field field = QuizDetailController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method method = QuizDetailController.class.getDeclaredMethod(methodName);
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

    private void callChooseOption(Button button) {
        try {
            Method method = QuizDetailController.class.getDeclaredMethod("chooseOption", ActionEvent.class);
            method.setAccessible(true);
            method.invoke(controller, new ActionEvent(button, null));
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
