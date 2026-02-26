package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.ClassModel;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ClassDetailControllerTest {

    static { new JFXPanel(); }

    private ClassDetailController controller;

    // ---------- Fake HeaderController ----------
    private static class FakeHeaderController extends HeaderController {
        public Label titleLabel = new Label();
        public Label subtitleLabel = new Label();
        public Label metaLabel = new Label();
        public Button backButton = new Button();

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

        @Override
        public void setMeta(String text) {
            metaLabel.setText(text);
        }
    }

    @BeforeEach
    void setUp() {
        controller = new ClassDetailController();

        FakeHeaderController fakeHeader = new FakeHeaderController();

        //  header = StackPane
        setPrivate("header", new StackPane());

        // headerController = fakeHeader
        setPrivate("headerController", fakeHeader);
        // create fake VBox to ignored NPE
        setPrivate("flashcardSetListBox", new VBox());

        // Fake logged-in user
        User teacher = new User();
        setUserId(teacher);
        teacher.setRole(1);
        teacher.setFirstName("Alice");
        teacher.setLastName("Teacher");
        AppState.currentUser.set(teacher);

        // Fake selected class
        ClassModel c = new ClassModel();
        setClassId(c);
        c.setClassName("Math 101");
        c.setTeacher(teacher);
        AppState.selectedClass.set(c);

        AppState.navOverride.set(null);

        callPrivateInitialize();
    }


    private void setUserId(User user) {
        try {
            Field f = User.class.getDeclaredField("userId");
            f.setAccessible(true);
            f.set(user, 1);
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

    private void setPrivate(String field, Object value) {
        try {
            Field f = ClassDetailController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getHeader() {
        try {
            Field f = ClassDetailController.class.getDeclaredField("headerController");
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivateInitialize() {
        try {
            Method m = ClassDetailController.class.getDeclaredMethod("initialize");
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callOpenFlashcardSet(ActionEvent e) {
        try {
            Method m = ClassDetailController.class.getDeclaredMethod("openFlashcardSet", ActionEvent.class);
            m.setAccessible(true);
            m.invoke(controller, e);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // ---------- Tests ----------

    @Test
    void testInitialize_setsHeaderCorrectly() {
        FakeHeaderController header = (FakeHeaderController) getHeader();

        assertEquals("Math 101", header.titleLabel.getText());
        assertEquals("Teacher: Alice Teacher", header.metaLabel.getText());
    }

    @Test
    void testInitialize_setsBackButton() {
        FakeHeaderController header = (FakeHeaderController) getHeader();
        assertTrue(header.backButton.isVisible());
    }

    @Disabled
    @Test
    void testOpenFlashcardSet_defaultName() {
        Button btn = new Button();
        ActionEvent event = new ActionEvent(btn, null);

        callOpenFlashcardSet(event);

        assertEquals("Flashcard Set", AppState.selectedFlashcardSetName.get());
        assertEquals(AppState.Screen.FLASHCARD_SET, AppState.navOverride.get());
    }
}
