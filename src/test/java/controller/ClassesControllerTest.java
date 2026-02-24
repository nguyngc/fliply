package controller;

import controller.components.ClassCardController;
import controller.components.HeaderController;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
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
    private ClassesController controller;

    // Fake ClassDetailsService
    private static class FakeClassDetailsService extends ClassDetailsService {
        List<ClassModel> fakeClasses;

        @Override
        public List<ClassModel> getClassesOfUser(int userId) {
            return fakeClasses;
        }
    }

    // Fake ClassCardController
    private static class FakeClassCardController extends ClassCardController {
        String title;
        double progress;
        boolean teacherMode;

        @Override
        public void setTeacherCard(String name, int students, int sets, double progress) {
            this.title = name;
            this.progress = progress;
            this.teacherMode = true;
        }

        @Override
        public void setStudentCard(String name, String teacherName, double progress) {
            this.title = name;
            this.progress = progress;
            this.teacherMode = false;
        }
    }

    @BeforeEach
    void setUp() {
        controller = new ClassesController();

        // Inject UI components
        setPrivate("header", new Parent() {});
        setPrivate("headerController", new HeaderController());
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

        // Fake class list
        ClassModel c1 = new ClassModel();
        c1.setClassName("Math");
        c1.setTeacher(u);

        ClassModel c2 = new ClassModel();
        c2.setClassName("Physics");
        c2.setTeacher(u);

        fakeService.fakeClasses = List.of(c1, c2);

        // Call initialize()
        callPrivate();
    }
    // Helper set userId
    private void setUserId(User user) {
        try { Field f = User.class.getDeclaredField("userId");
            f.setAccessible(true); f.set(user, 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // ---------------- Reflection Helpers ----------------

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

    private void callPrivate() {
        try {
            Method m = ClassesController.class.getDeclaredMethod("initialize");
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Tests ----------------

    @Test
    void testInitialize_setsHeaderCorrectly() {
        HeaderController header = (HeaderController) getPrivate("headerController");

        // Title
        String title = getHeaderLabel(header, "titleLabel");
        assertEquals("My Classes", title);

        // Subtitle
        String subtitle = getHeaderLabel(header, "subtitleLabel");
        assertEquals("Manage your classes", subtitle);
    }

    @Test
    void testRender_addsClassCards() {
        VBox box = (VBox) getPrivate("classListBox");

        // Teacher → 2 class cards + 1 "Add more class" tile
        assertEquals(3, box.getChildren().size());
    }

    @Test
    void testBuildAddMoreClassTile() {
        VBox box = (VBox) getPrivate("classListBox");

        Node last = box.getChildren().getLast();
        assertInstanceOf(StackPane.class, last);

        StackPane tile = (StackPane) last;
        Label label = (Label) tile.getChildren().getFirst();

        assertEquals("+ Add more class", label.getText());
    }

    // Helper to read HeaderController labels
    private String getHeaderLabel(HeaderController header, String field) {
        try {
            Field f = HeaderController.class.getDeclaredField(field);
            f.setAccessible(true);
            Label label = (Label) f.get(header);
            return label.getText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
