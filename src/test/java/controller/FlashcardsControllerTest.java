package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import model.AppState;
import model.dao.FlashcardDao;
import model.dao.UserDao;
import model.entity.Flashcard;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.LocaleManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardsControllerTest {

    static { new JFXPanel(); }

    private FlashcardsController controller;
    private final UserDao userDao = new UserDao();
    private final FlashcardDao flashcardDao = new FlashcardDao();
    private Locale previousLocale;
    private ResourceBundle messages;

    private static class FakeHeaderController extends HeaderController {
        Label title = new Label();
        Label subtitle = new Label();

        @Override public void setTitle(String titleText) { title.setText(titleText); }
        @Override public void setSubtitle(String text) { subtitle.setText(text); }
        @Override public void setBackVisible(boolean visible) { /* no-op for test double */ }
    }

    @BeforeEach
    void setUp() {
        previousLocale = LocaleManager.getLocale();
        LocaleManager.setLocale("en", "US");
        controller = new FlashcardsController();
        messages = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());
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

        Method m = FlashcardsController.class.getDeclaredMethod("renderGrid");
        m.setAccessible(true);
        m.invoke(controller);

        Field f = FlashcardsController.class.getDeclaredField("termGrid");
        f.setAccessible(true);
        GridPane grid = (GridPane) f.get(controller);
        assertEquals(2, grid.getChildren().size());
    }

    @Test
    void renderGridWithoutCardsShowsEmptyStateAndAddButton() throws Exception {
        Method m = FlashcardsController.class.getDeclaredMethod("renderGrid");
        m.setAccessible(true);
        m.invoke(controller);

        GridPane grid = (GridPane) getPrivate("termGrid");
        assertEquals(2, grid.getChildren().size());
        assertTrue(containsLabel(grid, messages.getString("flashcards.empty.title")));
    }

    @Test
    void initializeLoadsUserCardsAndSetsHeader() throws Exception {
        var originalLocale = LocaleManager.getLocale();
        User user = new User();
        user.setFirstName("Flash");
        user.setLastName("Card");
        user.setEmail("flash+" + UUID.randomUUID().toString().substring(0, 8) + "@test.com");
        user.setPassword("password123");
        user.setRole(0);

        Flashcard card = new Flashcard();
        card.setTerm("CPU");
        card.setDefinition("Central Processing Unit");
        card.setUser(user);

        try {
            LocaleManager.setLocale("en", "US");
            userDao.persist(user);
            flashcardDao.persist(card);
            AppState.currentUser.set(user);

            Method m = FlashcardsController.class.getDeclaredMethod("initialize");
            m.setAccessible(true);
            m.invoke(controller);

            FakeHeaderController header = (FakeHeaderController) getPrivate("headerController");
            assertEquals("My Flashcards", header.title.getText());
            assertTrue(header.subtitle.getText().contains("Total:"));
            assertEquals(1, AppState.myFlashcards.size());
        } finally {
            AppState.currentUser.set(null);
            AppState.myFlashcards.clear();
            LocaleManager.setLocale(originalLocale);
            flashcardDao.delete(card);
            userDao.delete(user);
        }
    }

    @Test
    void loadTileReturnsNode() throws Exception {
        Method m = FlashcardsController.class.getDeclaredMethod("loadTile", String.class, Runnable.class);
        m.setAccessible(true);
        Object node = m.invoke(controller, "A", (Runnable) () -> {});
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

    private boolean containsLabel(Node node, String expectedText) {
        if (node instanceof Label label) {
            return expectedText.equals(label.getText());
        }
        if (node instanceof javafx.scene.Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (containsLabel(child, expectedText)) {
                    return true;
                }
            }
        }
        return false;
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        LocaleManager.setLocale(previousLocale);
    }

}
