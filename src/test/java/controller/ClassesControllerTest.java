package controller;

import controller.components.HeaderController;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.ClassDetails;
import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.LocaleManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassesControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized by another test.
        }
    }

    private TestableClassesController controller;
    private FakeHeaderController headerController;
    private VBox classListBox;
    private Locale previousLocale;
    private User previousUser;
    private AppState.Role previousRole;
    private ClassModel previousSelectedClass;

    private static final class TestableClassesController extends ClassesController {
        private final List<ClassModel> classesToReturn = new ArrayList<>();
        private final Map<Integer, ClassModel> reloadedClasses = new HashMap<>();
        private int loadClassesCalls;
        private final List<Integer> reloadedIds = new ArrayList<>();
        private double progressToReturn;
        private AppState.Screen lastNavigatedScreen;
        private boolean failLoadingCard;
        private final List<FakeClassCardController> createdCardControllers = new ArrayList<>();

        @Override
        List<ClassModel> loadClassesForUser(int userId) {
            loadClassesCalls++;
            return new ArrayList<>(classesToReturn);
        }

        @Override
        ClassModel reloadClass(int classId) {
            reloadedIds.add(classId);
            return reloadedClasses.get(classId);
        }

        @Override
        double calculateProgress(User user, ClassModel classModel) {
            return progressToReturn;
        }

        @Override
        LoadedClassCard loadClassCard() throws Exception {
            if (failLoadingCard) {
                throw new IllegalStateException("boom");
            }
            FakeClassCardController fakeController = new FakeClassCardController();
            createdCardControllers.add(fakeController);
            return new LoadedClassCard(new StackPane(), fakeController);
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    private static final class FakeClassCardController extends controller.components.ClassCardController {
        private String className;
        private String teacherName;
        private Integer students;
        private Integer sets;
        private Double progress;
        private boolean teacherCard;
        private boolean studentCard;

        @Override
        public void setStudentCard(String classCode, String teacherName, double progress) {
            this.className = classCode;
            this.teacherName = teacherName;
            this.progress = progress;
            studentCard = true;
        }

        @Override
        public void setTeacherCard(String classCode, int students, int sets, double progress) {
            this.className = classCode;
            this.students = students;
            this.sets = sets;
            this.progress = progress;
            teacherCard = true;
        }
    }

    private static final class FakeHeaderController extends HeaderController {
        private String title;
        private String subtitle;
        private Variant variant;

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
    }

    @BeforeEach
    void setUp() {
        previousLocale = LocaleManager.getLocale();
        previousUser = AppState.currentUser.get();
        previousRole = AppState.getRole();
        previousSelectedClass = AppState.selectedClass.get();

        LocaleManager.setLocale("en", "US");
        controller = new TestableClassesController();
        headerController = new FakeHeaderController();
        classListBox = new VBox();

        setPrivate("header", new StackPane());
        setPrivate("headerController", headerController);
        setPrivate("classListBox", classListBox);

        AppState.currentUser.set(null);
        AppState.setRole(AppState.Role.STUDENT);
        AppState.selectedClass.set(null);
    }

    @AfterEach
    void tearDown() {
        LocaleManager.setLocale(previousLocale);
        AppState.currentUser.set(previousUser);
        AppState.setRole(previousRole);
        AppState.selectedClass.set(previousSelectedClass);
    }

    @Test
    void initialize_teacherConfiguresHeaderSkipsInvalidClassesAndNavigatesFromCardAndAddTile() {
        User teacher = createUser(10, 1, "Alice", "Teacher");
        AppState.currentUser.set(teacher);
        AppState.setRole(AppState.Role.TEACHER);
        controller.progressToReturn = 0.4;

        ClassModel noIdClass = createClass(null, "No Id", teacher);
        ClassModel reloadMissing = createClass(20, "Missing", teacher);
        ClassModel renderable = createClass(30, "Math 101", teacher);
        renderable.getStudents().add(new ClassDetails());
        renderable.getStudents().add(new ClassDetails());
        renderable.getFlashcardSets().add(new FlashcardSet());

        controller.classesToReturn.add(noIdClass);
        controller.classesToReturn.add(reloadMissing);
        controller.classesToReturn.add(renderable);
        controller.reloadedClasses.put(30, renderable);

        runOnFxThread(() -> callPrivate("initialize"));
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        assertEquals(rb.getString("class.title"), headerController.title);
        assertEquals(rb.getString("class.subtitle.teacher"), headerController.subtitle);
        assertEquals(HeaderController.Variant.TEACHER, headerController.variant);
        assertEquals(1, controller.loadClassesCalls);
        assertEquals(List.of(20, 30), controller.reloadedIds);
        assertEquals(2, classListBox.getChildren().size());

        Node teacherCard = classListBox.getChildren().getFirst();
        FakeClassCardController teacherCardController = controller.createdCardControllers.getFirst();
        assertTrue(teacherCardController.teacherCard);
        assertFalse(teacherCardController.studentCard);
        assertEquals("Math 101", teacherCardController.className);
        assertEquals(2, teacherCardController.students);
        assertEquals(1, teacherCardController.sets);
        assertEquals(0.4, teacherCardController.progress);

        teacherCard.getOnMouseClicked().handle(null);
        assertSame(renderable, AppState.selectedClass.get());
        assertEquals(AppState.Screen.TEACHER_CLASS_DETAIL, controller.lastNavigatedScreen);

        Node addTile = classListBox.getChildren().get(1);
        assertEquals(List.of(rb.getString("class.addClass")), extractLabelTexts(addTile));
        addTile.getOnMouseClicked().handle(null);
        assertEquals(AppState.Screen.TEACHER_ADD_CLASS, controller.lastNavigatedScreen);
    }

    @Test
    void initialize_studentConfiguresHeaderRendersCardAndNavigatesToStudentDetail() {
        User teacher = createUser(11, 1, "Alice", "Teacher");
        User student = createUser(12, 0, "Bob", "Student");
        AppState.currentUser.set(student);
        AppState.setRole(AppState.Role.STUDENT);
        controller.progressToReturn = 0.75;

        ClassModel renderable = createClass(40, "Biology", teacher);
        controller.classesToReturn.add(renderable);
        controller.reloadedClasses.put(40, renderable);

        runOnFxThread(() -> callPrivate("initialize"));
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        assertEquals(rb.getString("class.title"), headerController.title);
        assertEquals(rb.getString("class.subtitle.student"), headerController.subtitle);
        assertEquals(HeaderController.Variant.STUDENT, headerController.variant);
        assertEquals(1, classListBox.getChildren().size());

        Node studentCard = classListBox.getChildren().getFirst();
        FakeClassCardController studentCardController = controller.createdCardControllers.getFirst();
        assertTrue(studentCardController.studentCard);
        assertFalse(studentCardController.teacherCard);
        assertEquals("Biology", studentCardController.className);
        assertEquals("Alice Teacher", studentCardController.teacherName);
        assertEquals(0.75, studentCardController.progress);

        studentCard.getOnMouseClicked().handle(null);
        assertSame(renderable, AppState.selectedClass.get());
        assertEquals(AppState.Screen.CLASS_DETAIL, controller.lastNavigatedScreen);
    }

    @Test
    void render_withNullUserOrNullUserId_clearsListAndDoesNotLoadClasses() {
        runOnFxThread(() -> {
            classListBox.getChildren().add(new Label("placeholder"));
            AppState.currentUser.set(null);
            callPrivate("render");
            assertTrue(classListBox.getChildren().isEmpty());

            classListBox.getChildren().add(new Label("placeholder"));
            AppState.currentUser.set(createUser(null, 0, "No", "Id"));
            callPrivate("render");
            assertTrue(classListBox.getChildren().isEmpty());
        });

        assertEquals(0, controller.loadClassesCalls);
    }

    @Test
    void buildClassCard_whenLoaderFails_wrapsExceptionWithLocalizedMessage() {
        User teacher = createUser(50, 1, "Alice", "Teacher");
        AppState.currentUser.set(teacher);
        AppState.setRole(AppState.Role.TEACHER);
        controller.failLoadingCard = true;
        ClassModel classModel = createClass(60, "Physics", teacher);
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        IllegalArgumentException exception = runOnFxThreadWithResult(() ->
                assertThrows(IllegalArgumentException.class,
                        () -> callPrivate("buildClassCard", ClassModel.class, boolean.class, classModel, true))
        );

        assertEquals(rb.getString("class.error"), exception.getMessage());
        assertNotNull(exception.getCause());
    }

    private User createUser(Integer userId, int role, String firstName, String lastName) {
        User user = new User();
        if (userId != null) {
            setField(User.class, user, "userId", userId);
        }
        user.setRole(role);
        user.setFirstName(firstName);
        user.setLastName(lastName);
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

    private List<String> extractLabelTexts(Node node) {
        List<String> texts = new ArrayList<>();
        collectLabelTexts(node, texts);
        return texts;
    }

    private void collectLabelTexts(Node node, List<String> texts) {
        if (node instanceof Label label) {
            texts.add(label.getText());
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectLabelTexts(child, texts);
            }
        }
    }

    private void setPrivate(String fieldName, Object value) {
        setField(ClassesController.class, controller, fieldName, value);
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
            Method method = ClassesController.class.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(controller, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new RuntimeException(cause);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object callPrivate(String methodName, Class<?> parameterType1, Class<?> parameterType2, Object arg1, Object arg2) {
        return callPrivate(methodName, new Class<?>[]{parameterType1, parameterType2}, arg1, arg2);
    }

    private Object callPrivate(String methodName) {
        return callPrivate(methodName, new Class<?>[0]);
    }

    private void runOnFxThread(Runnable action) {
        runOnFxThreadWithResult(() -> {
            action.run();
            return null;
        });
    }

    private <T> T runOnFxThreadWithResult(FxSupplier<T> action) {
        if (Platform.isFxApplicationThread()) {
            return action.get();
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                result.set(action.get());
            } catch (Throwable t) {
                error.set(t);
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
            if (error.get() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(error.get());
        }

        return result.get();
    }

    @FunctionalInterface
    private interface FxSupplier<T> {
        T get();
    }
}
