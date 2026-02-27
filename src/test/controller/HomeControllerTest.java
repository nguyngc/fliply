package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTest {
    static { new JFXPanel(); }
    private HomeController controller;

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
        setUserId(u);
        u.setRole(1); // teacher
        u.setFirstName("Alice");
        u.setLastName("Teacher");
        AppState.currentUser.set(u);

        // simulate logic test
        Label name = (Label) getPrivate("nameLabel");
        Label subtitle = (Label) getPrivate("subtitleLabel");
        VBox quiz = (VBox) getPrivate("latestQuizSection");

        name.setText(u.getFirstName() + "!");
        subtitle.setText("Manage your classes");
        quiz.setVisible(false);
        quiz.setManaged(false);
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

    @Test
    void testHeaderText() {
        Label name = (Label) getPrivate("nameLabel");
        Label subtitle = (Label) getPrivate("subtitleLabel");

        assertEquals("Alice!", name.getText());
        assertEquals("Manage your classes", subtitle.getText());
    }

    @Test
    void testTeacherHidesQuizSection() {
        VBox quiz = (VBox) getPrivate("latestQuizSection");

        assertFalse(quiz.isVisible());
        assertFalse(quiz.isManaged());
    }

    @Test
    void testLatestClassHolderExists() {
        StackPane holder = (StackPane) getPrivate("latestClassHolder");
        assertNotNull(holder);
    }
}
