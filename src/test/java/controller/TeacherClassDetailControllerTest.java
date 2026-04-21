package controller;

import controller.components.FlashcardSetCardController;
import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.ClassDetails;
import model.entity.ClassModel;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeacherClassDetailControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableTeacherClassDetailController controller;
    private FakeHeaderController headerController;
    private Label studentsSectionLabel;
    private VBox enrolledStudentsBox;
    private VBox studentSearchBox;
    private TextField studentSearchField;
    private VBox searchResultsBox;
    private Label setsSectionLabel;
    private VBox setListBox;
    private Locale previousLocale;
    private ClassModel previousSelectedClass;
    private User previousSelectedStudent;
    private FlashcardSet previousSelectedSet;

    private static final class TestableTeacherClassDetailController extends TeacherClassDetailController {
        private ClassModel reloadedClass;
        private final List<User> allStudents = new ArrayList<>();
        private FlashcardSetCardView setCardView;
        private boolean throwOnLoadSetCardView;
        private int reloadCount;
        private Integer lastReloadedClassId;
        private int removeCount;
        private ClassDetails lastRemovedEnrollment;
        private int addCount;
        private User lastAddedStudent;
        private ClassModel lastAddedClass;
        private AppState.Screen lastNavigatedScreen;

        @Override
        ClassModel reloadClass(int classId) {
            reloadCount++;
            lastReloadedClassId = classId;
            return reloadedClass;
        }

        @Override
        void removeStudentFromClass(ClassDetails classDetails) {
            removeCount++;
            lastRemovedEnrollment = classDetails;
            reloadedClass.getStudents().remove(classDetails);
        }

        @Override
        ClassDetails addStudentToClass(User student, ClassModel classModel) {
            addCount++;
            lastAddedStudent = student;
            lastAddedClass = classModel;
            ClassDetails enrollment = new ClassDetails(classModel, student);
            reloadedClass.getStudents().add(enrollment);
            return enrollment;
        }

        @Override
        List<User> loadAllStudents() {
            return new ArrayList<>(allStudents);
        }

        @Override
        FlashcardSetCardView loadSetCardView() throws Exception {
            if (throwOnLoadSetCardView) {
                throw new IOException("boom");
            }
            return setCardView;
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    private static final class FakeHeaderController extends HeaderController {
        private boolean backVisible;
        private String title;
        private Runnable backAction;
        private Variant variant;

        @Override
        public void setBackVisible(boolean visible) {
            backVisible = visible;
        }

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public void setOnBack(Runnable action) {
            backAction = action;
        }

        @Override
        public void applyVariant(Variant variant) {
            this.variant = variant;
        }
    }

    private static final class FakeFlashcardSetCardController extends FlashcardSetCardController {
        private String subject;
        private int cardCount;
        private boolean showProgress = true;

        @Override
        public void setSubject(String subject) {
            this.subject = subject;
        }

        @Override
        public void setCardCount(int count) {
            cardCount = count;
        }

        @Override
        public void setShowProgress(boolean show) {
            showProgress = show;
        }
    }

    @BeforeEach
    void setUp() {
        previousLocale = LocaleManager.getLocale();
        previousSelectedClass = AppState.selectedClass.get();
        previousSelectedStudent = AppState.selectedStudent.get();
        previousSelectedSet = AppState.selectedSet.get();

        LocaleManager.setLocale("en", "US");
        controller = new TestableTeacherClassDetailController();
        headerController = new FakeHeaderController();
        studentsSectionLabel = new Label();
        enrolledStudentsBox = new VBox();
        studentSearchBox = new VBox();
        studentSearchField = new TextField();
        searchResultsBox = new VBox();
        setsSectionLabel = new Label();
        setListBox = new VBox();

        setPrivate("header", new Parent() {});
        setPrivate("headerController", headerController);
        setPrivate("studentsSectionLabel", studentsSectionLabel);
        setPrivate("enrolledStudentsBox", enrolledStudentsBox);
        setPrivate("showSearchBtn", new Button());
        setPrivate("studentSearchBox", studentSearchBox);
        setPrivate("studentSearchField", studentSearchField);
        setPrivate("searchResultsBox", searchResultsBox);
        setPrivate("setsSectionLabel", setsSectionLabel);
        setPrivate("setListBox", setListBox);

        AppState.selectedClass.set(null);
        AppState.selectedStudent.set(null);
        AppState.selectedSet.set(null);
    }

    @AfterEach
    void tearDown() {
        LocaleManager.setLocale(previousLocale);
        AppState.selectedClass.set(previousSelectedClass);
        AppState.selectedStudent.set(previousSelectedStudent);
        AppState.selectedSet.set(previousSelectedSet);
    }

    @Test
    void initialize_withoutSelectedClass_navigatesToClasses() {
        callPrivate("initialize");

        assertEquals(AppState.Screen.CLASSES, controller.lastNavigatedScreen);
        assertEquals(0, controller.reloadCount);
    }

    @Test
    void initialize_withClassWithoutId_navigatesToClasses() {
        AppState.selectedClass.set(new ClassModel());

        callPrivate("initialize");

        assertEquals(AppState.Screen.CLASSES, controller.lastNavigatedScreen);
        assertEquals(0, controller.reloadCount);
    }

    @Test
    void initialize_configuresHeaderAndRendersStudentsAndSets() {
        User teacher = createUser(1, "Alice", "Teacher", "alice@example.com");
        User student = createUser(2, "Bob", "Student", "bob@example.com");
        ClassModel initialClass = createClass(10, "Physics", teacher);
        ClassModel fullClass = createClass(10, "Physics", teacher);
        ClassDetails enrollment = new ClassDetails(fullClass, student);
        fullClass.getStudents().add(enrollment);
        FlashcardSet set = createSet("Waves", 2);
        fullClass.getFlashcardSets().add(set);
        FakeFlashcardSetCardController setCardController = new FakeFlashcardSetCardController();
        controller.reloadedClass = fullClass;
        controller.setCardView = new TeacherClassDetailController.FlashcardSetCardView(new StackPane(), setCardController);
        AppState.selectedClass.set(initialClass);

        callPrivate("initialize");
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        assertSame(fullClass, AppState.selectedClass.get());
        assertEquals(1, controller.reloadCount);
        assertEquals(10, controller.lastReloadedClassId);
        assertTrue(headerController.backVisible);
        assertEquals("Physics", headerController.title);
        assertEquals(HeaderController.Variant.TEACHER, headerController.variant);
        assertFalse(studentSearchBox.isVisible());
        assertFalse(studentSearchBox.isManaged());
        assertEquals(rb.getString("classDetail.students") + " (1)", studentsSectionLabel.getText());
        assertEquals(rb.getString("classDetail.sets") + " (1)", setsSectionLabel.getText());
        assertEquals(1, enrolledStudentsBox.getChildren().size());
        assertEquals(1, setListBox.getChildren().size());
        assertEquals("Waves", setCardController.subject);
        assertEquals(2, setCardController.cardCount);
        assertFalse(setCardController.showProgress);

        headerController.backAction.run();
        assertEquals(AppState.Screen.CLASSES, controller.lastNavigatedScreen);
    }

    @Test
    void enrolledStudentRow_clickNavigatesToStudentDetail() {
        User teacher = createUser(1, "Alice", "Teacher", "alice@example.com");
        User student = createUser(2, "Bob", "Student", "bob@example.com");
        ClassModel fullClass = createClass(10, "Physics", teacher);
        ClassDetails enrollment = new ClassDetails(fullClass, student);
        fullClass.getStudents().add(enrollment);
        controller.reloadedClass = fullClass;
        controller.setCardView = new TeacherClassDetailController.FlashcardSetCardView(new StackPane(), new FakeFlashcardSetCardController());
        AppState.selectedClass.set(createClass(10, "Physics", teacher));

        callPrivate("initialize");

        HBox row = (HBox) enrolledStudentsBox.getChildren().getFirst();
        row.getOnMouseClicked().handle(null);

        assertSame(student, AppState.selectedStudent.get());
        assertEquals(AppState.Screen.TEACHER_STUDENT_DETAIL, controller.lastNavigatedScreen);
    }

    @Test
    void enrolledStudentDelete_removesStudentAndRefreshesCounts() {
        User teacher = createUser(1, "Alice", "Teacher", "alice@example.com");
        User student = createUser(2, "Bob", "Student", "bob@example.com");
        ClassModel fullClass = createClass(10, "Physics", teacher);
        ClassDetails enrollment = new ClassDetails(fullClass, student);
        fullClass.getStudents().add(enrollment);
        controller.reloadedClass = fullClass;
        controller.setCardView = new TeacherClassDetailController.FlashcardSetCardView(new StackPane(), new FakeFlashcardSetCardController());
        AppState.selectedClass.set(createClass(10, "Physics", teacher));

        callPrivate("initialize");

        HBox row = (HBox) enrolledStudentsBox.getChildren().getFirst();
        Button deleteButton = (Button) row.getChildren().get(1);
        deleteButton.fire();

        assertEquals(1, controller.removeCount);
        assertSame(enrollment, controller.lastRemovedEnrollment);
        assertTrue(enrolledStudentsBox.getChildren().isEmpty());
        assertEquals("Students (0)", studentsSectionLabel.getText());
    }

    @Test
    void showAndHideStudentSearch_toggleVisibilityAndClearState() {
        User teacher = createUser(1, "Alice", "Teacher", "alice@example.com");
        controller.reloadedClass = createClass(10, "Physics", teacher);
        controller.setCardView = new TeacherClassDetailController.FlashcardSetCardView(new StackPane(), new FakeFlashcardSetCardController());
        AppState.selectedClass.set(createClass(10, "Physics", teacher));

        callPrivate("initialize");
        callPrivate("onShowStudentSearch");

        assertTrue(studentSearchBox.isVisible());
        assertTrue(studentSearchBox.isManaged());
        assertTrue(searchResultsBox.getChildren().isEmpty());

        studentSearchField.setText("bob");
        searchResultsBox.getChildren().add(new Label("temp"));

        callPrivate("onHideStudentSearch");

        assertFalse(studentSearchBox.isVisible());
        assertFalse(studentSearchBox.isManaged());
        assertEquals("", studentSearchField.getText());
        assertTrue(searchResultsBox.getChildren().isEmpty());
    }

    @Test
    void searchResults_filterAvailableStudentsAndAddStudent() {
        User teacher = createUser(1, "Alice", "Teacher", "alice@example.com");
        User enrolled = createUser(2, "Bob", "Student", "bob@example.com");
        User match = createUser(3, "Mark", "Jones", "mark@example.com");
        User other = createUser(4, "Nina", "Stone", "nina@example.com");
        ClassModel fullClass = createClass(10, "Physics", teacher);
        fullClass.getStudents().add(new ClassDetails(fullClass, enrolled));
        controller.reloadedClass = fullClass;
        controller.allStudents.add(enrolled);
        controller.allStudents.add(match);
        controller.allStudents.add(other);
        controller.setCardView = new TeacherClassDetailController.FlashcardSetCardView(new StackPane(), new FakeFlashcardSetCardController());
        AppState.selectedClass.set(createClass(10, "Physics", teacher));

        callPrivate("initialize");
        callPrivate("onShowStudentSearch");
        studentSearchField.setText("mark");

        assertEquals(1, searchResultsBox.getChildren().size());
        HBox resultRow = (HBox) searchResultsBox.getChildren().getFirst();
        VBox left = (VBox) resultRow.getChildren().getFirst();
        assertEquals("Mark Jones", ((Label) left.getChildren().get(0)).getText());
        assertEquals("mark@example.com", ((Label) left.getChildren().get(1)).getText());

        Button addButton = (Button) resultRow.getChildren().get(1);
        addButton.fire();

        assertEquals(1, controller.addCount);
        assertSame(match, controller.lastAddedStudent);
        assertSame(fullClass, controller.lastAddedClass);
        assertEquals(2, enrolledStudentsBox.getChildren().size());
        assertEquals("Students (2)", studentsSectionLabel.getText());
        assertTrue(searchResultsBox.getChildren().isEmpty());
    }

    @Test
    void setCardClick_selectsSetAndNavigatesToDetail() {
        User teacher = createUser(1, "Alice", "Teacher", "alice@example.com");
        ClassModel fullClass = createClass(10, "Physics", teacher);
        FlashcardSet set = createSet("Waves", 1);
        fullClass.getFlashcardSets().add(set);
        StackPane node = new StackPane();
        controller.reloadedClass = fullClass;
        controller.setCardView = new TeacherClassDetailController.FlashcardSetCardView(node, new FakeFlashcardSetCardController());
        AppState.selectedClass.set(createClass(10, "Physics", teacher));

        callPrivate("initialize");
        Node renderedNode = setListBox.getChildren().getFirst();
        renderedNode.getOnMouseClicked().handle(null);

        assertSame(set, AppState.selectedSet.get());
        assertEquals(AppState.Screen.TEACHER_FLASHCARD_SET_DETAIL, controller.lastNavigatedScreen);
    }

    @Test
    void renderSets_wrapsLoadFailure() {
        User teacher = createUser(1, "Alice", "Teacher", "alice@example.com");
        ClassModel fullClass = createClass(10, "Physics", teacher);
        fullClass.getFlashcardSets().add(createSet("Waves", 1));
        controller.throwOnLoadSetCardView = true;
        setControllerClass(fullClass);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> callPrivate("renderSets"));
        assertEquals("Failed to load flashcard_set_card.fxml", thrown.getMessage());
    }

    @Test
    void onAddSet_navigatesToTeacherAddSet() {
        callPrivate("onAddSet");
        assertEquals(AppState.Screen.TEACHER_ADD_SET, controller.lastNavigatedScreen);
    }

    private User createUser(int userId, String firstName, String lastName, String email) {
        User user = new User();
        setEntityField(User.class, user, "userId", userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRole(0);
        return user;
    }

    private ClassModel createClass(int classId, String className, User teacher) {
        ClassModel classModel = new ClassModel();
        setEntityField(ClassModel.class, classModel, "classId", classId);
        classModel.setClassName(className);
        classModel.setTeacher(teacher);
        return classModel;
    }

    private FlashcardSet createSet(String subject, int cardCount) {
        FlashcardSet set = new FlashcardSet();
        set.setSubject(subject);
        Collection<Flashcard> cards = new ArrayList<>();
        for (int i = 0; i < cardCount; i++) {
            cards.add(new Flashcard());
        }
        setEntityField(FlashcardSet.class, set, "cards", cards);
        return set;
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

    private void setControllerClass(ClassModel classModel) {
        try {
            Field field = TeacherClassDetailController.class.getDeclaredField("c");
            field.setAccessible(true);
            field.set(controller, classModel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setPrivate(String fieldName, Object value) {
        try {
            Field field = TeacherClassDetailController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method method = TeacherClassDetailController.class.getDeclaredMethod(methodName);
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
}
