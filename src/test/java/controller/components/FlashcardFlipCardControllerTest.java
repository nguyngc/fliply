package controller.components;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardFlipCardControllerTest {

    static { new JFXPanel(); }

    private FlashcardFlipCardController controller;

    @BeforeEach
    void setUp() {
        controller = new FlashcardFlipCardController();
        setPrivate("root", new StackPane());
        setPrivate("termPane", new StackPane());
        setPrivate("definitionPane", new StackPane());
        setPrivate("termLabel", new Label());
        setPrivate("definitionLabel", new Label());
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = FlashcardFlipCardController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = FlashcardFlipCardController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void initializeAndSideSwitchesWork() throws Exception {
        callPrivate("initialize");
        assertTrue(((StackPane) getPrivate("termPane")).isVisible());
        assertFalse(((StackPane) getPrivate("definitionPane")).isVisible());

        controller.showDefinition();
        assertFalse(((StackPane) getPrivate("termPane")).isVisible());
        assertTrue(((StackPane) getPrivate("definitionPane")).isVisible());

        controller.showTerm();
        assertTrue(((StackPane) getPrivate("termPane")).isVisible());
        assertFalse(((StackPane) getPrivate("definitionPane")).isVisible());
    }

    @Test
    void setTextMethodsUpdateLabels() {
        controller.setTerm("CPU");
        controller.setDefinition("Central Processing Unit");
        assertEquals("CPU", ((Label) getPrivate("termLabel")).getText());
        assertEquals("Central Processing Unit", ((Label) getPrivate("definitionLabel")).getText());
    }

    @Test
    void flipCanBeInvokedAfterInitialization() throws Exception {
        callPrivate("initialize");
        controller.flip();
        assertNotNull(getPrivate("root"));
    }

    private void callPrivate(String method) throws Exception {
        Method m = FlashcardFlipCardController.class.getDeclaredMethod(method);
        m.setAccessible(true);
        m.invoke(controller);
    }
}

