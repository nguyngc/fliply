package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import model.AppState;
import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.service.FlashcardSetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeacherAddSetControllerTest {

    static { new JFXPanel(); }

    private TeacherAddSetController controller;

    // ---------------- Fake HeaderController ----------------
    private static class FakeHeaderController extends HeaderController {
        public Label titleLabel = new Label();
        public Label subtitleLabel = new Label();
        public Label backButton = new Label(); // fake button

        @Override
        public void setTitle(String title) {
            titleLabel.setText(title);
        }

        @Override
        public void setSubtitle(String subtitle) {
            subtitleLabel.setText(subtitle);
        }

        @Override
        public void setBackVisible(boolean visible) {
            backButton.setVisible(visible);
            backButton.setManaged(visible);
        }
    }


    // ---------------- Fake FlashcardSetService ----------------
    private static class FakeFlashcardSetService extends FlashcardSetService {
        FlashcardSet savedSet = null;

        @Override
        public void save(FlashcardSet set) {
            this.savedSet = set;
        }
    }

    @BeforeEach
    void setUp() {
        controller = new TeacherAddSetController();

        FakeHeaderController fakeHeader = new FakeHeaderController();
        setPrivate("header", new StackPane());
        setPrivate("headerController", fakeHeader);

        setPrivate("subjectField", new TextField());
        setPrivate("fileStatusLabel", new Label());

        FakeFlashcardSetService fakeService = new FakeFlashcardSetService();

        ClassModel c = new ClassModel();
        setClassId(c);
        c.setClassName("Math");
        AppState.selectedClass.set(c);

        AppState.navOverride.set(null);

        callPrivate("initialize");
    }

    // ---------------- Reflection helpers ----------------

    private void setPrivate(String field, Object value) {
        try {
            Field f = TeacherAddSetController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = TeacherAddSetController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method m = TeacherAddSetController.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setClassId(ClassModel c) {
        try {
            Field f = ClassModel.class.getDeclaredField("classId");
            f.setAccessible(true);
            f.set(c, 10);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Tests ----------------

    @Test
    void testInitialize_setsHeaderCorrectly() {
        FakeHeaderController header = (FakeHeaderController) getPrivate("headerController");

        assertEquals("New Set of\nFlashcard", header.titleLabel.getText());
        assertTrue(header.backButton.isVisible());
    }
    @Disabled
    @Test
    void testOnUpload_validFile() throws Exception {
        File temp = File.createTempFile("test", ".csv");
        Files.write(temp.toPath(), List.of(
                "term,definition",
                "A,B",
                "C,D"
        ));

        setPrivate("selectedFile", temp);

        callPrivate("onUpload");

        int parsed = (int) getPrivate("parsedCount");
        assertEquals(2, parsed);

        Label label = (Label) getPrivate("fileStatusLabel");
        assertTrue(label.getText().contains("Loaded:"));
        assertTrue(label.getText().contains("(2 cards)"));
    }
    @Disabled
    @Test
    void testOnUpload_cannotReadFile() {
        File fake = new File("not_exist.csv");
        setPrivate("selectedFile", fake);

        callPrivate("onUpload");

        int parsed = (int) getPrivate("parsedCount");
        assertEquals(0, parsed);

        Label label = (Label) getPrivate("fileStatusLabel");
        assertEquals("Cannot read file.", label.getText());
    }
    @Disabled
    @Test
    void testOnAdd_validSet_createsAndNavigates() {
        TextField subject = (TextField) getPrivate("subjectField");
        subject.setText("Animals");

        callPrivate("onAdd");

        FakeFlashcardSetService fake = (FakeFlashcardSetService) getPrivate("flashcardSetService");

        assertNotNull(fake.savedSet);
        assertEquals("Animals", fake.savedSet.getSubject());
        assertEquals(AppState.Screen.TEACHER_CLASS_DETAIL, AppState.navOverride.get());
    }
    @Disabled
    @Test
    void testOnAdd_blankSubject_doesNothing() {
        TextField subject = (TextField) getPrivate("subjectField");
        subject.setText("   ");

        callPrivate("onAdd");

        FakeFlashcardSetService fake = (FakeFlashcardSetService) getPrivate("flashcardSetService");
        assertNull(fake.savedSet);
        assertNull(AppState.navOverride.get());
    }
}
