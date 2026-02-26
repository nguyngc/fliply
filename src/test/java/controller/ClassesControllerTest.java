package controller;

import controller.components.ClassCardController;
import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.ClassModel;
import model.entity.User;
import model.service.ClassDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassesControllerTest {

    static { new JFXPanel(); }

    private ClassesController controller;

    // Fake HeaderController (không dùng FXML)
    private static class FakeHeaderController extends HeaderController {
        public Label titleLabel = new Label();
        public Label subtitleLabel = new Label();

        @Override
        public void setTitle(String title) {
            titleLabel.setText(title);
        }

        @Override
        public void setSubtitle(String subtitle) {
            subtitleLabel.setText(subtitle);
        }
    }

    // Fake service
    private static class FakeClassDetailsService extends ClassDetailsService {
        List<ClassModel> fakeClasses;

        @Override
        public List<ClassModel> getClassesOfUser(int userId) {
            return fakeClasses;
        }
    }

    @BeforeEach
    void setUp() {
        controller = new ClassesController();

        // Inject fake header
        FakeHeaderController fakeHeader = new FakeHeaderController();
        setPrivate("header", new Parent() {});
        setPrivate("headerController", fakeHeader);

        // Inject VBox
        setPrivate("classListBox", new VBox());

        // Fake logged-in user
        User u = new User();
        setUserId(u);
        u.setRole(1); // teacher
        u.setFirstName("A");
        u.setLastName("B");
        AppState.currentUser.set(u);

        // Fake service
        FakeClassDetailsService fakeService = new FakeClassDetailsService();
        setPrivate("classDetailsService", fakeService);

        // Fake classes
        ClassModel c1 = new ClassModel();
        c1.setClassName("Math");
        c1.setTeacher(u);

        ClassModel c2 = new ClassModel();
        c2.setClassName("Physics");
        c2.setTeacher(u);

        fakeService.fakeClasses = List.of(c1, c2);

        // Call initialize()
        callPrivate("initialize");
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

    private void setPrivate(String field, Object value) {
        try {
            Field f = ClassesController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = ClassesController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method m = ClassesController.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Tests ----------------

    @Test
    void testInitialize_setsHeaderCorrectly() {
        FakeHeaderController header = (FakeHeaderController) getPrivate("headerController");

        assertEquals("My Classes", header.titleLabel.getText());
        assertEquals("Manage your classes", header.subtitleLabel.getText());
    }

    @Test
    void testRender_addsClassCards() {
        VBox box = (VBox) getPrivate("classListBox");

        // Teacher → 2 class cards + 1 "Add more class" tile
        assertEquals(3, box.getChildren().size());
    }
}
