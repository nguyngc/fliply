package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardsControllerTest {

    static { new JFXPanel(); }

    private FlashcardsController controller;

    private static class FakeHeaderController extends HeaderController {
        Label title = new Label();
        Label subtitle = new Label();

        @Override public void setTitle(String titleText) { title.setText(titleText); }
        @Override public void setSubtitle(String text) { subtitle.setText(text); }
        @Override public void setBackVisible(boolean visible) {}
    }

    @BeforeEach
    void setUp() {
        controller = new FlashcardsController();
        AppState.myFlashcards.clear();
        setPrivate("termGrid", new GridPane());
        setPrivate("headerController", new FakeHeaderController());
    }

    @Test
    void renderGridCreatesTileAndAddButton() throws Exception {
        Flashcard card = new Flashcard();
        card.setTerm("CPU");
        card.setDefinition("Central Processing Unit");
        AppState.myFlashcards.add(card);

        callPrivate("renderGrid");

        GridPane grid = (GridPane) getPrivate("termGrid");
        assertEquals(2, grid.getChildren().size());
    }

    @Test
    void loadTileReturnsNode() throws Exception {
        Object node = callPrivate("loadTile", "A", false, (Runnable) () -> {});
        assertNotNull(node);
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = FlashcardsController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = FlashcardsController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String method) throws Exception {
        Method m = FlashcardsController.class.getDeclaredMethod(method);
        m.setAccessible(true);
        m.invoke(controller);
    }

    private Object callPrivate(String method, Object... args) throws Exception {
        for (Method m : FlashcardsController.class.getDeclaredMethods()) {
            if (m.getName().equals(method) && m.getParameterCount() == args.length) {
                m.setAccessible(true);
                return m.invoke(controller, args);
            }
        }
        throw new NoSuchMethodException(method);
    }
}



