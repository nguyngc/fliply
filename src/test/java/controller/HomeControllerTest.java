package controller;

import controller.components.ClassCardController;
import controller.components.QuizCardController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.entity.Quiz;
import model.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.LocaleManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableHomeController controller;
    private Label nameLabel;
    private Label subtitleLabel;
    private StackPane latestClassHolder;
    private VBox latestQuizSection;
    private StackPane latestQuizCardWrapper;
    private FakeQuizCardController latestQuizCardController;
    private Locale previousLocale;
    private AppState.Role previousRole;
    private User previousUser;
    private ClassModel previousSelectedClass;
    private Quiz previousSelectedQuiz;
    private AppState.NavItem previousNavOverride;
    private int previousQuizQuestionIndex;
    private int previousQuizPoints;
    private Map<Integer, String> previousQuizAnswers;
    private Map<Integer, Boolean> previousQuizCorrectMap;

    private static final class TestableHomeController extends HomeController {
        private List<ClassModel> classesToLoad = new ArrayList<>();
        private final Map<Integer, ClassModel> reloadedClasses = new HashMap<>();
        private List<Quiz> quizzesToLoad = new ArrayList<>();
        private final Map<FlashcardSet, Integer> cardCounts = new IdentityHashMap<>();
        private final Map<FlashcardSet, Double> progressBySet = new IdentityHashMap<>();
        private LatestClassCardView latestClassCardView;
        private boolean throwOnLoadLatestClassCard;
        private int loadQuizCallCount;
        private int loadClassCardViewCallCount;
        private AppState.Screen lastNavigatedScreen;

        @Override
        List<ClassModel> loadClassesForUser(int userId) {
            return new ArrayList<>(classesToLoad);
        }

        @Override
        ClassModel loadClassWithRelations(int classId) {
            return reloadedClasses.get(classId);
        }

        @Override
        int getCardCount(FlashcardSet set) {
            return cardCounts.getOrDefault(set, 0);
        }

        @Override
        double getProgressPercent(User user, FlashcardSet set) {
            return progressBySet.getOrDefault(set, 0.0);
        }

        @Override
        LatestClassCardView loadLatestClassCardView() throws IOException {
            if (throwOnLoadLatestClassCard) {
                throw new IOException("boom");
            }
            loadClassCardViewCallCount++;
            return latestClassCardView;
        }

        @Override
        List<Quiz> loadQuizzesForUser(int userId) {
            loadQuizCallCount++;
            return quizzesToLoad;
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    private static final class FakeClassCardController extends ClassCardController {
        private boolean teacherCardConfigured;
        private boolean studentCardConfigured;
        private String classCode;
        private String teacherName;
        private int students;
        private int sets;
        private double progress;

        @Override
        public void setStudentCard(String classCode, String teacherName, double progress) {
            studentCardConfigured = true;
            this.classCode = classCode;
            this.teacherName = teacherName;
            this.progress = progress;
        }

        @Override
        public void setTeacherCard(String classCode, int students, int sets, double progress) {
            teacherCardConfigured = true;
            this.classCode = classCode;
            this.students = students;
            this.sets = sets;
            this.progress = progress;
        }
    }

    private static final class FakeQuizCardController extends QuizCardController {
        private Quiz lastQuiz;

        @Override
        public void setQuiz(Quiz quiz) {
            lastQuiz = quiz;
        }
    }

    @BeforeEach
    void setUp() {
        previousLocale = LocaleManager.getLocale();
        previousRole = AppState.getRole();
        previousUser = AppState.currentUser.get();
        previousSelectedClass = AppState.selectedClass.get();
        previousSelectedQuiz = AppState.selectedQuiz.get();
        previousNavOverride = AppState.navOverride.get();
        previousQuizQuestionIndex = AppState.quizQuestionIndex.get();
        previousQuizPoints = AppState.quizPoints.get();
        previousQuizAnswers = new HashMap<>(AppState.quizAnswers);
        previousQuizCorrectMap = new HashMap<>(AppState.quizCorrectMap);

        LocaleManager.setLocale("en", "US");
        controller = new TestableHomeController();
        nameLabel = new Label();
        subtitleLabel = new Label();
        latestClassHolder = new StackPane();
        latestQuizSection = new VBox();
        latestQuizCardWrapper = new StackPane();
        latestQuizCardController = new FakeQuizCardController();

        setPrivate("nameLabel", nameLabel);
        setPrivate("subtitleLabel", subtitleLabel);
        setPrivate("latestClassHolder", latestClassHolder);
        setPrivate("latestQuizSection", latestQuizSection);
        setPrivate("latestQuizCardWrapper", latestQuizCardWrapper);
        setPrivate("latestQuizCard", new StackPane());
        setPrivate("latestQuizCardController", latestQuizCardController);

        AppState.role.set(null);
        AppState.currentUser.set(null);
        AppState.selectedClass.set(null);
        AppState.selectedQuiz.set(null);
        AppState.navOverride.set(null);
        AppState.quizQuestionIndex.set(0);
        AppState.quizPoints.set(0);
        AppState.quizAnswers.clear();
        AppState.quizCorrectMap.clear();
    }

    @AfterEach
    void tearDown() {
        LocaleManager.setLocale(previousLocale);
        AppState.role.set(previousRole);
        AppState.currentUser.set(previousUser);
        AppState.selectedClass.set(previousSelectedClass);
        AppState.selectedQuiz.set(previousSelectedQuiz);
        AppState.navOverride.set(previousNavOverride);
        AppState.quizQuestionIndex.set(previousQuizQuestionIndex);
        AppState.quizPoints.set(previousQuizPoints);
        AppState.quizAnswers.clear();
        AppState.quizAnswers.putAll(previousQuizAnswers);
        AppState.quizCorrectMap.clear();
        AppState.quizCorrectMap.putAll(previousQuizCorrectMap);
    }

    @Test
    void initialize_withoutUser_doesNothing() {
        callPrivate("initialize");

        assertEquals("", nameLabel.getText());
        assertEquals("", subtitleLabel.getText());
        assertTrue(latestQuizSection.isVisible());
        assertEquals(0, controller.loadQuizCallCount);
        assertTrue(latestClassHolder.getChildren().isEmpty());
    }

    @Test
    void initialize_teacherSetsWelcomeHidesQuizAndRendersLatestClass() {
        User teacher = createUser(10, 1, "Alice", "Teacher");
        ClassModel olderClass = createClass(2, "Old Class", teacher);
        ClassModel newerClass = createClass(8, "Math 101", teacher);
        FakeClassCardController classCardController = new FakeClassCardController();
        StackPane classCardNode = new StackPane();

        controller.classesToLoad = new ArrayList<>(List.of(olderClass, newerClass));
        controller.reloadedClasses.put(8, newerClass);
        controller.latestClassCardView = new HomeController.LatestClassCardView(classCardNode, classCardController);
        AppState.currentUser.set(teacher);
        AppState.role.set(AppState.Role.TEACHER);

        callPrivate("initialize");
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        assertEquals("Alice!", nameLabel.getText());
        assertEquals(rb.getString("home.subtitle.teacher"), subtitleLabel.getText());
        assertFalse(latestQuizSection.isVisible());
        assertFalse(latestQuizSection.isManaged());
        assertEquals(1, latestClassHolder.getChildren().size());
        assertTrue(classCardController.teacherCardConfigured);
        assertEquals("Math 101", classCardController.classCode);
        assertEquals(0, classCardController.students);
        assertEquals(0, classCardController.sets);
        assertEquals(0.0, classCardController.progress);
        assertEquals(1, controller.loadClassCardViewCallCount);
        assertEquals(0, controller.loadQuizCallCount);

        Node renderedNode = latestClassHolder.getChildren().getFirst();
        renderedNode.getOnMouseClicked().handle(null);

        assertSame(newerClass, AppState.selectedClass.get());
        assertEquals(AppState.Screen.CLASSES, controller.lastNavigatedScreen);
    }

    @Test
    void initialize_studentRendersStudentCardAndLatestQuiz() {
        User teacher = createUser(20, 1, "Alice", "Teacher");
        User student = createUser(21, 0, "Bob", "Student");
        ClassModel visibleClass = createClass(4, "Biology", teacher);
        FlashcardSet firstSet = createSet("Cells");
        FlashcardSet secondSet = createSet("DNA");
        visibleClass.getFlashcardSets().add(firstSet);
        visibleClass.getFlashcardSets().add(secondSet);

        FakeClassCardController classCardController = new FakeClassCardController();
        controller.latestClassCardView = new HomeController.LatestClassCardView(new StackPane(), classCardController);
        controller.classesToLoad = new ArrayList<>(List.of(visibleClass));
        controller.reloadedClasses.put(4, visibleClass);
        controller.cardCounts.put(firstSet, 4);
        controller.cardCounts.put(secondSet, 6);
        controller.progressBySet.put(firstSet, 0.5);
        controller.progressBySet.put(secondSet, 0.25);
        Quiz olderQuiz = createQuiz(3, 5);
        Quiz latestQuiz = createQuiz(9, 8);
        controller.quizzesToLoad = new ArrayList<>(List.of(olderQuiz, latestQuiz));

        AppState.currentUser.set(student);
        AppState.role.set(AppState.Role.STUDENT);

        callPrivate("initialize");
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        assertEquals("Bob!", nameLabel.getText());
        assertEquals(rb.getString("home.subtitle.student"), subtitleLabel.getText());
        assertTrue(latestQuizSection.isVisible());
        assertTrue(latestQuizSection.isManaged());
        assertTrue(classCardController.studentCardConfigured);
        assertEquals("Biology", classCardController.classCode);
        assertEquals("Alice Teacher", classCardController.teacherName);
        assertEquals(0.3, classCardController.progress, 0.0001);
        assertSame(latestQuiz, latestQuizCardController.lastQuiz);
        assertEquals(1, controller.loadQuizCallCount);
    }

    @Test
    void renderLatestClass_returnsEarlyForMissingData() {
        User student = createUser(30, 0, "Sam", "Student");
        AppState.currentUser.set(student);
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        callPrivate("renderLatestClass");
        assertEquals(1, latestClassHolder.getChildren().size());
        assertTrue(containsLabel(latestClassHolder, rb.getString("home.latestClass.empty.title.student")));
        assertEquals(0, controller.loadClassCardViewCallCount);

        controller.classesToLoad = new ArrayList<>(List.of(new ClassModel()));
        callPrivate("renderLatestClass");
        assertEquals(1, latestClassHolder.getChildren().size());
        assertTrue(containsLabel(latestClassHolder, rb.getString("home.latestClass.empty.title.student")));
        assertEquals(0, controller.loadClassCardViewCallCount);

        ClassModel classWithId = createClass(40, "Physics", createUser(31, 1, "Tina", "Teacher"));
        controller.classesToLoad = new ArrayList<>(List.of(classWithId));
        controller.reloadedClasses.clear();
        callPrivate("renderLatestClass");
        assertEquals(1, latestClassHolder.getChildren().size());
        assertTrue(containsLabel(latestClassHolder, rb.getString("home.latestClass.empty.title.student")));
        assertEquals(0, controller.loadClassCardViewCallCount);
    }

    @Test
    void renderLatestQuiz_withoutQuizzes_showsEmptyState() {
        User student = createUser(32, 0, "Jamie", "Student");
        AppState.currentUser.set(student);
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        callPrivate("renderLatestQuiz");

        assertNull(latestQuizCardController.lastQuiz);
        assertEquals(1, latestQuizCardWrapper.getChildren().size());
        assertTrue(containsLabel(latestQuizCardWrapper, rb.getString("home.latestQuiz.empty.title")));
    }

    @Test
    void renderLatestClass_wrapsClassCardLoadFailure() {
        User teacher = createUser(41, 1, "Nina", "Teacher");
        ClassModel classModel = createClass(50, "Chemistry", teacher);
        controller.classesToLoad = new ArrayList<>(List.of(classModel));
        controller.reloadedClasses.put(50, classModel);
        controller.throwOnLoadLatestClassCard = true;
        AppState.currentUser.set(teacher);

        RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> callPrivate("renderLatestClass"));
        Throwable cause = thrown.getCause();

        assertTrue(cause instanceof IllegalStateException);
        assertEquals(ResourceBundle.getBundle("Messages", LocaleManager.getLocale()).getString("home.error"), cause.getMessage());
    }

    @Test
    void onLatestQuizClicked_teacherDoesNothing() {
        AppState.role.set(AppState.Role.TEACHER);
        AppState.selectedQuiz.set(createQuiz(60, 4));

        callQuizClick();

        assertNull(controller.lastNavigatedScreen);
        assertEquals(60, AppState.selectedQuiz.get().getQuizId());
    }

    @Test
    void onLatestQuizClicked_withoutLatestQuiz_navigatesToQuizzes() {
        AppState.role.set(AppState.Role.STUDENT);

        callQuizClick();

        assertEquals(AppState.Screen.QUIZZES, controller.lastNavigatedScreen);
    }

    @Test
    void onLatestQuizClicked_withLatestQuiz_initializesQuizStateAndNavigates() {
        Quiz quiz = createQuiz(77, 10);
        setPrivate("latestQuiz", quiz);
        AppState.role.set(AppState.Role.STUDENT);
        AppState.selectedQuiz.set(createQuiz(1, 1));
        AppState.quizQuestionIndex.set(4);
        AppState.quizPoints.set(9);
        AppState.quizAnswers.put(1, "A");
        AppState.quizCorrectMap.put(1, Boolean.TRUE);

        callQuizClick();

        assertSame(quiz, AppState.selectedQuiz.get());
        assertEquals(0, AppState.quizQuestionIndex.get());
        assertEquals(0, AppState.quizPoints.get());
        assertTrue(AppState.quizAnswers.isEmpty());
        assertTrue(AppState.quizCorrectMap.isEmpty());
        assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
        assertEquals(AppState.Screen.QUIZ_DETAIL, controller.lastNavigatedScreen);
    }

    private User createUser(int userId, int role, String firstName, String lastName) {
        User user = new User();
        setEntityId(User.class, user, "userId", userId);
        user.setRole(role);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }

    private ClassModel createClass(int classId, String className, User teacher) {
        ClassModel classModel = new ClassModel();
        setEntityId(ClassModel.class, classModel, "classId", classId);
        classModel.setClassName(className);
        classModel.setTeacher(teacher);
        return classModel;
    }

    private FlashcardSet createSet(String subject) {
        FlashcardSet flashcardSet = new FlashcardSet();
        flashcardSet.setSubject(subject);
        return flashcardSet;
    }

    private Quiz createQuiz(int quizId, int questionCount) {
        Quiz quiz = new Quiz();
        setEntityId(Quiz.class, quiz, "quizId", quizId);
        quiz.setNoOfQuestions(questionCount);
        return quiz;
    }

    private void setEntityId(Class<?> type, Object target, String fieldName, Object value) {
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
            Field field = HomeController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method method = HomeController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callQuizClick() {
        try {
            Method method = HomeController.class.getDeclaredMethod("onLatestQuizClicked", javafx.scene.input.MouseEvent.class);
            method.setAccessible(true);
            method.invoke(controller, new Object[]{null});
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean containsLabel(Node node, String expectedText) {
        if (node instanceof Label label) {
            return expectedText.equals(label.getText());
        }
        if (node instanceof javafx.scene.Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (containsLabel(child, expectedText)) {
                    return true;
                }
            }
        }
        return false;
    }
}
