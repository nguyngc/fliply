package controller;

import controller.components.HeaderController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.ClassModel;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeacherStudentDetailControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableTeacherStudentDetailController controller;
    private FakeHeaderController headerController;
    private VBox progressListBox;
    private User previousSelectedStudent;
    private ClassModel previousSelectedClass;
    private AppState.NavItem previousNavOverride;

    private static final class TestableTeacherStudentDetailController extends TeacherStudentDetailController {
        private Integer reloadedClassId;
        private ClassModel reloadedClass;
        private final Map<FlashcardSet, Double> progressBySet = new IdentityHashMap<>();
        private AppState.Screen lastNavigatedScreen;

        @Override
        ClassModel reloadClass(int classId) {
            reloadedClassId = classId;
            return reloadedClass;
        }

        @Override
        double getProgressPercent(User student, FlashcardSet set) {
            return progressBySet.getOrDefault(set, 0.0);
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    private static final class FakeHeaderController extends HeaderController {
        private boolean backVisible;
        private String title;
        private String subtitle;
        private Variant variant;
        private Runnable backAction;

        @Override
        public void setBackVisible(boolean visible) {
            backVisible = visible;
        }

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        @Override
        public void applyVariant(Variant variant) {
            this.variant = variant;
        }

        @Override
        public void setOnBack(Runnable action) {
            backAction = action;
        }
    }

    @BeforeEach
    void setUp() {
        previousSelectedStudent = AppState.selectedStudent.get();
        previousSelectedClass = AppState.selectedClass.get();
        previousNavOverride = AppState.navOverride.get();

        controller = new TestableTeacherStudentDetailController();
        headerController = new FakeHeaderController();
        progressListBox = new VBox();

        setPrivate("header", new StackPane());
        setPrivate("headerController", headerController);
        setPrivate("progressListBox", progressListBox);

        AppState.selectedStudent.set(null);
        AppState.selectedClass.set(null);
        AppState.navOverride.set(null);
    }

    @AfterEach
    void tearDown() {
        AppState.selectedStudent.set(previousSelectedStudent);
        AppState.selectedClass.set(previousSelectedClass);
        AppState.navOverride.set(previousNavOverride);
    }

    @Test
    void initialize_withoutSelectedStudent_navigatesBack() {
        AppState.selectedClass.set(createClass(4, "Biology", createUser(1, "Alice", "Teacher", "alice@test.com")));

        runOnFxThread(() -> callPrivate("initialize"));

        assertEquals(AppState.Screen.TEACHER_CLASS_DETAIL, controller.lastNavigatedScreen);
        assertNull(controller.reloadedClassId);
    }

    @Test
    void initialize_withNullClassId_navigatesBack() {
        AppState.selectedStudent.set(createUser(2, "Bob", "Student", "bob@test.com"));
        AppState.selectedClass.set(createClass(null, "Biology", createUser(1, "Alice", "Teacher", "alice@test.com")));

        runOnFxThread(() -> callPrivate("initialize"));

        assertEquals(AppState.Screen.TEACHER_CLASS_DETAIL, controller.lastNavigatedScreen);
        assertNull(controller.reloadedClassId);
    }

    @Test
    void initialize_withReloadedClass_configuresHeaderAndRendersProgressRows() {
        User student = createUser(2, "Bob", "Student", "bob@test.com");
        User teacher = createUser(1, "Alice", "Teacher", "alice@test.com");
        FlashcardSet biology = createSet("Biology", 4);
        ClassModel selectedClass = createClass(9, "Science", teacher);
        ClassModel reloadedClass = createClass(9, "Science", teacher);
        reloadedClass.getFlashcardSets().add(biology);

        AppState.selectedStudent.set(student);
        AppState.selectedClass.set(selectedClass);
        controller.reloadedClass = reloadedClass;
        controller.progressBySet.put(biology, 0.5);

        runOnFxThread(() -> callPrivate("initialize"));

        assertEquals(9, controller.reloadedClassId);
        assertSame(reloadedClass, AppState.selectedClass.get());
        assertTrue(headerController.backVisible);
        assertEquals("Bob Student", headerController.title);
        assertEquals("bob@test.com", headerController.subtitle);
        assertEquals(HeaderController.Variant.TEACHER, headerController.variant);
        assertEquals(AppState.NavItem.CLASSES, AppState.navOverride.get());
        assertEquals(1, progressListBox.getChildren().size());

        VBox row = (VBox) progressListBox.getChildren().get(0);
        HBox top = (HBox) row.getChildren().get(0);
        Label left = (Label) top.getChildren().get(0);
        Label right = (Label) row.getChildren().get(1);
        ProgressBar bar = (ProgressBar) row.getChildren().get(2);

        assertEquals("Biology (2/4)", left.getText());
        assertEquals("50% Completed", right.getText());
        assertEquals(0.5, bar.getProgress(), 0.0001);

        headerController.backAction.run();
        assertEquals(AppState.Screen.TEACHER_CLASS_DETAIL, controller.lastNavigatedScreen);
    }

    @Test
    void normalizeProgress_clampsOutOfRangeValues() {
        assertEquals(0.0, controller.normalizeProgress(-1.0));
        assertEquals(1.0, controller.normalizeProgress(1.5));
        assertEquals(0.0, controller.normalizeProgress(Double.NaN));
    }

    private User createUser(Integer userId, String firstName, String lastName, String email) {
        User user = new User();
        if (userId != null) {
            setField(User.class, user, "userId", userId);
        }
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        return user;
    }

    private ClassModel createClass(Integer classId, String className, User teacher) {
        ClassModel classModel = new ClassModel();
        if (classId != null) {
            setField(ClassModel.class, classModel, "classId", classId);
        }
        classModel.setClassName(className);
        classModel.setTeacher(teacher);
        return classModel;
    }

    private FlashcardSet createSet(String subject, int totalCards) {
        FlashcardSet set = new FlashcardSet();
        set.setSubject(subject);
        List<Flashcard> cards = new ArrayList<>();
        for (int i = 0; i < totalCards; i++) {
            cards.add(new Flashcard());
        }
        setField(FlashcardSet.class, set, "cards", cards);
        return set;
    }

    private void setPrivate(String fieldName, Object value) {
        setField(TeacherStudentDetailController.class, controller, fieldName, value);
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
            Method method = TeacherStudentDetailController.class.getDeclaredMethod(methodName);
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
