package controller;

import controller.components.ClassCardController;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTest {

    private HomeController controller;

    // Fake ClassDetailsService
    private static class FakeClassDetailsService extends ClassDetailsService {
        List<ClassModel> fakeClasses = new ArrayList<>();

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
        controller = new HomeController();

        // Inject UI components
        setPrivate("nameLabel", new Label());
        setPrivate("subtitleLabel", new Label());
        setPrivate("latestClassHolder", new StackPane());
        setPrivate("latestQuizSection", new VBox());

        // Fake logged-in user
        User u = new User();
        setUserId(u, 1);
        u.setRole(1); // teacher
        u.setFirstName("Alice");
        u.setLastName("Teacher");
        AppState.currentUser.set(u);

        // Fake service
        FakeClassDetailsService fakeService = new FakeClassDetailsService();
        setPrivate("classDetailsService", fakeService);

        // Fake class list
        ClassModel c1 = new ClassModel();
        setClassId(c1, 10);
        c1.setClassName("Math");
        c1.setTeacher(u);

        ClassModel c2 = new ClassModel();
        setClassId(c2, 20);
        c2.setClassName("Physics");
        c2.setTeacher(u);

        fakeService.fakeClasses.add(c1);
        fakeService.fakeClasses.add(c2);

        // Call initialize()
        callPrivate();
    }

    // ---------------- Helper: set userId ----------------
    private void setUserId(User user, int id) {
        try {
            Field f = User.class.getDeclaredField("userId");
            f.setAccessible(true);
            f.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Helper: set classId ----------------
    private void setClassId(ClassModel c, int id) {
        try {
            Field f = ClassModel.class.getDeclaredField("classId");
            f.setAccessible(true);
            f.set(c, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Reflection Helpers ----------------

    private void setPrivate(String field, Object value) {
        try {
            Field f = HomeController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = HomeController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate() {
        try {
            Method m = HomeController.class.getDeclaredMethod("initialize");
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Tests ----------------

    @Test
    void testInitialize_setsHeaderText() {
        Label name = (Label) getPrivate("nameLabel");
        Label subtitle = (Label) getPrivate("subtitleLabel");

        assertEquals("Alice!", name.getText());
        assertEquals("Manage your classes", subtitle.getText());
    }

    @Test
    void testInitialize_teacherHidesQuizSection() {
        VBox quiz = (VBox) getPrivate("latestQuizSection");

        assertFalse(quiz.isVisible());
        assertFalse(quiz.isManaged());
    }

    @Test
    void testRenderLatestClass_addsLatestClassCard() {
        StackPane holder = (StackPane) getPrivate("latestClassHolder");

        assertEquals(1, holder.getChildren().size());
    }

    @Test
    void testRenderLatestClass_clickNavigates() {
        StackPane holder = (StackPane) getPrivate("latestClassHolder");
        Node card = holder.getChildren().getFirst();

        // Simulate click
        card.getOnMouseClicked().handle(null);
        assertEquals(AppState.Screen.CLASSES, AppState.navOverride.get());
        assertNotNull(AppState.selectedClass.get());
    }
}
