package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.LocaleManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassDetailControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableClassDetailController controller;
    private FakeHeaderController headerController;
    private VBox flashcardSetListBox;
    private Locale previousLocale;
    private User previousUser;
    private ClassModel previousSelectedClass;
    private FlashcardSet previousSelectedSet;

    private static final class TestableClassDetailController extends ClassDetailController {
        private final List<FlashcardSet> setsToLoad = new ArrayList<>();
        private Integer lastRequestedClassId;
        private int loadCallCount;
        private AppState.Screen lastNavigatedScreen;

        @Override
        List<FlashcardSet> loadSetsForClass(int classId) {
            lastRequestedClassId = classId;
            loadCallCount++;
            return new ArrayList<>(setsToLoad);
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    private static final class FakeHeaderController extends HeaderController {
        private String title;
        private String meta;
        private boolean backVisible;
        private Runnable backAction;
        private Variant variant;

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public void setMeta(String text) {
            meta = text;
        }

        @Override
        public void setBackVisible(boolean visible) {
            backVisible = visible;
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

    @BeforeEach
    void setUp() {
        previousLocale = LocaleManager.getLocale();
        previousUser = AppState.currentUser.get();
        previousSelectedClass = AppState.selectedClass.get();
        previousSelectedSet = AppState.selectedFlashcardSet.get();

        LocaleManager.setLocale("en", "US");
        controller = new TestableClassDetailController();
        headerController = new FakeHeaderController();
        flashcardSetListBox = new VBox();

        setPrivate("header", new StackPane());
        setPrivate("headerController", headerController);
        setPrivate("flashcardSetListBox", flashcardSetListBox);

        AppState.currentUser.set(null);
        AppState.selectedClass.set(null);
        AppState.selectedFlashcardSet.set(null);
    }

    @AfterEach
    void tearDown() {
        LocaleManager.setLocale(previousLocale);
        AppState.currentUser.set(previousUser);
        AppState.selectedClass.set(previousSelectedClass);
        AppState.selectedFlashcardSet.set(previousSelectedSet);
    }

    @Test
    void initialize_withoutSelectedClass_setsFallbackHeaderAndBackNavigation() {
        callPrivate("initialize");
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        assertEquals(rb.getString("class.title"), headerController.title);
        assertTrue(headerController.backVisible);
        assertNull(headerController.meta);
        assertEquals(0, controller.loadCallCount);

        headerController.backAction.run();
        assertEquals(AppState.Screen.CLASSES, controller.lastNavigatedScreen);
    }

    @Test
    void initialize_withTeacherUser_setsHeaderLoadsSetsAndClearsList() {
        User teacher = createUser(1, "Alice", "Teacher");
        ClassModel selectedClass = createClass(10, "Math 101", teacher);
        AppState.currentUser.set(teacher);
        AppState.selectedClass.set(selectedClass);
        flashcardSetListBox.getChildren().add(new Button("Old"));

        callPrivate("initialize");
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        assertEquals("Math 101", headerController.title);
        assertEquals(rb.getString("class.teacher") + " Alice Teacher", headerController.meta);
        assertTrue(headerController.backVisible);
        assertEquals(HeaderController.Variant.TEACHER, headerController.variant);
        assertEquals(1, controller.loadCallCount);
        assertEquals(10, controller.lastRequestedClassId);
        assertTrue(flashcardSetListBox.getChildren().isEmpty());

        headerController.backAction.run();
        assertEquals(AppState.Screen.CLASSES, controller.lastNavigatedScreen);
    }

    @Test
    void initialize_withStudentUser_rendersSetButtonsAndOpensSelectedSet() {
        User teacher = createUser(1, "Alice", "Teacher");
        User student = createUser(0, "Bob", "Student");
        ClassModel selectedClass = createClass(20, "Biology", teacher);
        FlashcardSet firstSet = createSet("Cells");
        FlashcardSet secondSet = createSet("DNA");

        AppState.currentUser.set(student);
        AppState.selectedClass.set(selectedClass);
        controller.setsToLoad.add(firstSet);
        controller.setsToLoad.add(secondSet);
        flashcardSetListBox.getChildren().add(new Button("Placeholder"));

        callPrivate("initialize");

        assertEquals(HeaderController.Variant.STUDENT, headerController.variant);
        assertEquals(2, flashcardSetListBox.getChildren().size());
        assertEquals("Cells", ((Button) flashcardSetListBox.getChildren().get(0)).getText());
        assertEquals("DNA", ((Button) flashcardSetListBox.getChildren().get(1)).getText());

        ((Button) flashcardSetListBox.getChildren().get(0)).fire();

        assertSame(firstSet, AppState.selectedFlashcardSet.get());
        assertEquals(AppState.Screen.FLASHCARD_SET, controller.lastNavigatedScreen);
    }

    private User createUser(int role, String firstName, String lastName) {
        User user = new User();
        user.setRole(role);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }

    private ClassModel createClass(int classId, String name, User teacher) {
        ClassModel classModel = new ClassModel();
        try {
            Field classIdField = ClassModel.class.getDeclaredField("classId");
            classIdField.setAccessible(true);
            classIdField.set(classModel, classId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        classModel.setClassName(name);
        classModel.setTeacher(teacher);
        return classModel;
    }

    private FlashcardSet createSet(String subject) {
        FlashcardSet flashcardSet = new FlashcardSet();
        flashcardSet.setSubject(subject);
        return flashcardSet;
    }

    private void setPrivate(String fieldName, Object value) {
        try {
            Field field = ClassDetailController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method method = ClassDetailController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
