package controller;

import controller.components.HeaderController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import model.AppState;
import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.FlashcardFileParser;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeacherAddSetControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableTeacherAddSetController controller;
    private FakeHeaderController headerController;
    private TextField subjectField;
    private Label fileStatusLabel;
    private ClassModel previousSelectedClass;
    private User previousCurrentUser;

    private static final class TestableTeacherAddSetController extends TeacherAddSetController {
        private Map<String, String> strings = Map.of(
                "teacherAddSet.title", "New Set of Flashcard",
                "teacherAddSet.fileStatusSuccess", "Loaded: {0} ({1} cards)",
                "teacherAddSet.fileStatusError", "Cannot read file."
        );
        private File chosenFile;
        private List<String> linesToRead = List.of();
        private Exception readFailure;
        private List<FlashcardFileParser.ParsedCard> parsedCards = List.of();
        private Exception parseFailure;
        private String createdSubject;
        private ClassModel createdClass;
        private FlashcardSet createdSet = new FlashcardSet();
        private final List<String> createdFlashcards = new ArrayList<>();
        private FlashcardSet lastCreatedFlashcardSet;
        private User lastTeacher;
        private Integer reloadedClassId;
        private ClassModel reloadedClass;
        private AppState.Screen lastNavigatedScreen;

        @Override
        Map<String, String> loadLocalizedStrings() {
            return strings;
        }

        @Override
        File chooseFile() {
            return chosenFile;
        }

        @Override
        List<String> readAllLines(File file) throws Exception {
            if (readFailure != null) {
                throw readFailure;
            }
            return linesToRead;
        }

        @Override
        List<FlashcardFileParser.ParsedCard> parseSelectedFile(File file) throws Exception {
            if (parseFailure != null) {
                throw parseFailure;
            }
            return parsedCards;
        }

        @Override
        FlashcardSet createSet(String subject, ClassModel classModel) {
            createdSubject = subject;
            createdClass = classModel;
            return createdSet;
        }

        @Override
        void createFlashcard(String term, String definition, FlashcardSet set, User teacher) {
            createdFlashcards.add(term + "=" + definition);
            lastCreatedFlashcardSet = set;
            lastTeacher = teacher;
        }

        @Override
        ClassModel reloadClass(int classId) {
            reloadedClassId = classId;
            return reloadedClass;
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
        previousSelectedClass = AppState.selectedClass.get();
        previousCurrentUser = AppState.currentUser.get();

        controller = new TestableTeacherAddSetController();
        headerController = new FakeHeaderController();
        subjectField = new TextField();
        fileStatusLabel = new Label();

        setPrivate("header", new StackPane());
        setPrivate("headerController", headerController);
        setPrivate("subjectField", subjectField);
        setPrivate("fileStatusLabel", fileStatusLabel);

        AppState.selectedClass.set(null);
        AppState.currentUser.set(null);
    }

    @AfterEach
    void tearDown() {
        AppState.selectedClass.set(previousSelectedClass);
        AppState.currentUser.set(previousCurrentUser);
    }

    @Test
    void initialize_setsHeaderAndBackAction() {
        runOnFxThread(() -> callPrivate("initialize"));

        assertEquals("New Set of Flashcard", headerController.title);
        assertTrue(headerController.backVisible);

        headerController.backAction.run();
        assertEquals(AppState.Screen.TEACHER_CLASS_DETAIL, controller.lastNavigatedScreen);
    }

    @Test
    void onUpload_whenFileCanBeRead_updatesStatusAndCount() {
        controller.chosenFile = new File("cards.csv");
        controller.linesToRead = List.of("term,definition", "A,B", "C,D");
        runOnFxThread(() -> callPrivate("initialize"));

        runOnFxThread(() -> callPrivate("onUpload"));

        assertSame(controller.chosenFile, getPrivate("selectedFile"));
        assertEquals(2, getPrivate("parsedCount"));
        assertEquals("Loaded: cards.csv (2 cards)", fileStatusLabel.getText());
    }

    @Test
    void onUpload_whenReadFails_setsErrorMessage() {
        controller.chosenFile = new File("broken.csv");
        controller.readFailure = new Exception("boom");
        runOnFxThread(() -> callPrivate("initialize"));

        runOnFxThread(() -> callPrivate("onUpload"));

        assertEquals(0, getPrivate("parsedCount"));
        assertEquals("Cannot read file.", fileStatusLabel.getText());
    }

    @Test
    void onAdd_withBlankSubject_doesNothing() {
        ClassModel selectedClass = createClass(10);
        AppState.selectedClass.set(selectedClass);
        setPrivate("selectedFile", new File("cards.csv"));
        controller.parsedCards = List.of(new FlashcardFileParser.ParsedCard("Cell", "Basic unit"));
        subjectField.setText("   ");

        runOnFxThread(() -> callPrivate("onAdd"));

        assertNull(controller.createdSubject);
        assertNull(controller.lastNavigatedScreen);
        assertEquals(List.of(), controller.createdFlashcards);
    }

    @Test
    void onAdd_happyPath_createsSetImportsCardsReloadsClassAndNavigates() {
        ClassModel selectedClass = createClass(15);
        ClassModel reloadedClass = createClass(15);
        User teacher = createUser(4);
        AppState.selectedClass.set(selectedClass);
        AppState.currentUser.set(teacher);
        setPrivate("selectedFile", new File("science.csv"));
        subjectField.setText("Science");
        controller.parsedCards = List.of(
                new FlashcardFileParser.ParsedCard("Cell", "Basic unit"),
                new FlashcardFileParser.ParsedCard("DNA", "Genetic code")
        );
        controller.reloadedClass = reloadedClass;

        runOnFxThread(() -> callPrivate("onAdd"));

        assertEquals("Science", controller.createdSubject);
        assertSame(selectedClass, controller.createdClass);
        assertEquals(List.of("Cell=Basic unit", "DNA=Genetic code"), controller.createdFlashcards);
        assertSame(controller.createdSet, controller.lastCreatedFlashcardSet);
        assertSame(teacher, controller.lastTeacher);
        assertEquals(15, controller.reloadedClassId);
        assertSame(reloadedClass, AppState.selectedClass.get());
        assertEquals(AppState.Screen.TEACHER_CLASS_DETAIL, controller.lastNavigatedScreen);
    }

    @Test
    void onCancel_navigatesBackToTeacherClassDetail() {
        runOnFxThread(() -> callPrivate("onCancel"));

        assertEquals(AppState.Screen.TEACHER_CLASS_DETAIL, controller.lastNavigatedScreen);
    }

    private ClassModel createClass(int classId) {
        ClassModel classModel = new ClassModel();
        setField(ClassModel.class, classModel, "classId", classId);
        return classModel;
    }

    private User createUser(int userId) {
        User user = new User();
        setField(User.class, user, "userId", userId);
        return user;
    }

    private Object getPrivate(String fieldName) {
        try {
            Field field = TeacherAddSetController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setPrivate(String fieldName, Object value) {
        setField(TeacherAddSetController.class, controller, fieldName, value);
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
            Method method = TeacherAddSetController.class.getDeclaredMethod(methodName);
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
