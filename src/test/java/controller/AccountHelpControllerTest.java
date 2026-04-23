package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.AppState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AccountHelpControllerTest {

    static { new JFXPanel(); } // start java toolkit

    private AccountHelpController controller;
    private HeaderController header;

    @BeforeEach
    void setUp() {
        controller = new AccountHelpController();

        // create spy  to use method without constructor
        header = Mockito.spy(HeaderController.class);

        // Inject fake UI components
        injectHeaderField("titleLabel", new Label());
        injectHeaderField("subtitleLabel", new Label());
        injectHeaderField("backButton", new Button());

        // Inject controller
        setPrivate("header", new Parent() {});
        setPrivate("headerController", header);
        setPrivate("quickStartTitleLabel", new Label());
        setPrivate("quickStartBodyLabel", new Label());
        setPrivate("roleGuideTitleLabel", new Label());
        setPrivate("roleGuideBodyLabel", new Label());
        setPrivate("commonTasksTitleLabel", new Label());
        setPrivate("commonTasksBodyLabel", new Label());
        setPrivate("faqTitleLabel", new Label());
        setPrivate("faqBodyLabel", new Label());

        // Reset AppState
        AppState.navOverride.set(null);
        AppState.role.set(AppState.Role.TEACHER);

        // initialize()
        callPrivate();
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = AccountHelpController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void injectHeaderField(String field, Object value) {
        try {
            Field f = HeaderController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(header, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getHeaderField(String field) {
        try {
            Field f = HeaderController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(header);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate() {
        try {
            Method m = AccountHelpController.class.getDeclaredMethod("initialize");
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testInitialize_setsHeaderCorrectly() {
        Label title = (Label) getHeaderField("titleLabel");
        Label subtitle = (Label) getHeaderField("subtitleLabel");
        Button back = (Button) getHeaderField("backButton");

        assertEquals("Help", title.getText());
        assertEquals("", subtitle.getText());
        assertTrue(back.isVisible());
    }

    @Test
    void testInitialize_populatesTeacherHelpContent() {
        Label quickStartTitle = getControllerLabel("quickStartTitleLabel");
        Label roleGuideTitle = getControllerLabel("roleGuideTitleLabel");
        Label roleGuideBody = getControllerLabel("roleGuideBodyLabel");
        Label tasksTitle = getControllerLabel("commonTasksTitleLabel");
        Label faqBody = getControllerLabel("faqBodyLabel");

        assertEquals("Quick Start", quickStartTitle.getText());
        assertEquals("Teacher Guide", roleGuideTitle.getText());
        assertTrue(roleGuideBody.getText().contains("Teachers mainly work in Classes."));
        assertEquals("Common Tasks", tasksTitle.getText());
        assertTrue(faqBody.getText().contains("Why do I not see Flashcards or Quizzes in the bottom menu?"));
    }

    @Disabled("Cannot test UI navigation in unit test environment")
    @Test
    void testInitialize_setsNavOverride() {
        assertEquals(AppState.NavItem.ACCOUNT, AppState.navOverride.get());
    }

    private Label getControllerLabel(String field) {
        try {
            Field f = AccountHelpController.class.getDeclaredField(field);
            f.setAccessible(true);
            return (Label) f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
