package controller.components;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardSetCardControllerTest {

    static { new JFXPanel(); }

    private FlashcardSetCardController controller;

    @BeforeEach
    void setUp() {
        controller = new FlashcardSetCardController();
        setPrivate("subjectLabel", new Label());
        setPrivate("countLabel", new Label());
        setPrivate("progressTextLabel", new Label());
        setPrivate("progressBar", new ProgressBar());
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = FlashcardSetCardController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void settersUpdateLabelsAndProgress() {
        controller.setSubject("Math");
        controller.setCardCount(12);
        controller.setProgress(0.75);

        Label subject = (Label) getPrivate("subjectLabel");
        Label count = (Label) getPrivate("countLabel");
        Label progressText = (Label) getPrivate("progressTextLabel");
        ProgressBar bar = (ProgressBar) getPrivate("progressBar");

        assertEquals("Math", subject.getText());
        assertEquals("12 cards", count.getText());
        assertEquals(0.75, bar.getProgress());
        assertEquals("75% Completed", progressText.getText());
        assertTrue(progressText.isVisible());
        assertTrue(bar.isVisible());
    }

    @Test
    void setShowProgress_hidesAndShowsWidgets() {
        controller.setShowProgress(false);
        assertFalse(((Label) getPrivate("progressTextLabel")).isVisible());
        assertFalse(((ProgressBar) getPrivate("progressBar")).isVisible());

        controller.setShowProgress(true);
        assertTrue(((Label) getPrivate("progressTextLabel")).isVisible());
        assertTrue(((ProgressBar) getPrivate("progressBar")).isVisible());
    }

    private Object getPrivate(String field) {
        try {
            Field f = FlashcardSetCardController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

