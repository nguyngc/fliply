package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.layout.GridPane;
import model.AppState;
import model.dao.ClassModelDao;
import model.dao.FlashcardDao;
import model.dao.FlashcardSetDao;
import model.dao.UserDao;
import model.entity.ClassModel;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.entity.User;
import model.service.FlashcardSetService;
import org.junit.jupiter.api.Test;
import util.LocaleManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardSetControllerTest {

    static { new JFXPanel(); }

    private final FlashcardSetService setService = new FlashcardSetService();
    private final UserDao userDao = new UserDao();
    private final ClassModelDao classDao = new ClassModelDao();
    private final FlashcardSetDao setDao = new FlashcardSetDao();
    private final FlashcardDao flashcardDao = new FlashcardDao();

    private static class FakeHeaderController extends HeaderController {
        String title;
        String subtitle;
        boolean backVisible;

        @Override public void setTitle(String titleText) { title = titleText; }
        @Override public void setSubtitle(String text) { subtitle = text; }
        @Override public void setBackVisible(boolean visible) { backVisible = visible; }
    }

    private User newTeacher() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        User u = new User();
        u.setFirstName("Teacher");
        u.setLastName("User");
        u.setEmail("teacher+" + uid + "@test.com");
        u.setPassword("password123");
        u.setRole(1);
        return u;
    }

    private ClassModel newClass(User teacher) {
        ClassModel c = new ClassModel();
        c.setClassName("Class-" + UUID.randomUUID().toString().substring(0, 6));
        c.setTeacher(teacher);
        return c;
    }

    @Test
    void initializeLoadsSetAndRendersTiles() throws Exception {
        var originalLocale = LocaleManager.getLocale();
        User teacher = newTeacher();
        ClassModel clazz = null;
        FlashcardSet set = null;
        Flashcard c1 = null;
        Flashcard c2 = null;

        try {
            LocaleManager.setLocale("en", "US");
            userDao.persist(teacher);
            classDao.persist(clazz = newClass(teacher));
            set = setService.createSet("Biology", clazz);

            c1 = new Flashcard();
            c1.setTerm("Cell");
            c1.setDefinition("Basic unit of life");
            c1.setFlashcardSet(set);
            c1.setUser(teacher);
            flashcardDao.persist(c1);

            c2 = new Flashcard();
            c2.setTerm("DNA");
            c2.setDefinition("Genetic material");
            c2.setFlashcardSet(set);
            c2.setUser(teacher);
            flashcardDao.persist(c2);

            AppState.selectedFlashcardSet.set(set);
            AppState.detailHeaderTitle.set("");
            AppState.detailHeaderSubtitle.set("");

            FlashcardSetController controller = new FlashcardSetController();
            FakeHeaderController header = new FakeHeaderController();
            GridPane grid = new GridPane();

            setPrivate(controller, "headerController", header);
            setPrivate(controller, "termGrid", grid);

            callInitialize(controller);

            assertEquals("Biology", header.title);
            assertEquals("Total: 2", header.subtitle);
            assertTrue(header.backVisible);
            assertEquals(2, grid.getChildren().size());
            assertEquals("Biology", AppState.detailHeaderTitle.get());
            assertEquals("Total: 2", AppState.detailHeaderSubtitle.get());
        } finally {
            AppState.selectedFlashcardSet.set(null);
            LocaleManager.setLocale(originalLocale);

            if (c2 != null) flashcardDao.delete(c2);
            if (c1 != null) flashcardDao.delete(c1);
            if (set != null) setDao.delete(set);
            if (clazz != null) classDao.delete(clazz);
            userDao.delete(teacher);
        }
    }

    private void setPrivate(Object controller, String field, Object value) {
        try {
            Field f = FlashcardSetController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callInitialize(Object controller) throws Exception {
        Method m = FlashcardSetController.class.getDeclaredMethod("initialize");
        m.setAccessible(true);
        m.invoke(controller);
    }
}


