package controller;

import javafx.application.Platform;
import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import model.AppState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.LocaleManager;
import view.Navigator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class QuizFormControllerTest {

    static { new JFXPanel(); }

    private QuizFormController controller;
    private Locale previousLocale;

    private static class FakeHeaderController extends HeaderController {
        String title;
        @Override public void setTitle(String titleText) { this.title = titleText; }
        @Override public void setBackVisible(boolean visible) {}
        @Override public void setOnBack(Runnable action) {}
    }

    @BeforeEach
    void setUp() {
        previousLocale = LocaleManager.getLocale();
        LocaleManager.setLocale("en", "US");

        controller = new QuizFormController();
        setPrivate("header", new StackPane());
        setPrivate("headerController", new FakeHeaderController());
        setPrivate("countField", new TextField());
        setPrivate("resources", ResourceBundle.getBundle("Messages", LocaleManager.getLocale()));
        AppState.currentUser.set(null);
        AppState.selectedQuiz.set(null);
        AppState.quizQuestionIndex.set(0);
        AppState.quizPoints.set(0);
        AppState.quizAnswers.clear();
        AppState.quizCorrectMap.clear();
        runOnFxThread(() -> Navigator.init(new javafx.stage.Stage()));
    }

    @AfterEach
    void tearDown() {
        LocaleManager.setLocale(previousLocale.getLanguage(), previousLocale.getCountry());
        AppState.selectedQuiz.set(null);
        AppState.quizQuestionIndex.set(0);
        AppState.quizPoints.set(0);
        AppState.quizAnswers.clear();
        AppState.quizCorrectMap.clear();
    }

    @Test
    void initialize_setsHeaderAndNavOverride() {
        runOnFxThread(() -> callPrivate("initialize"));
        assertEquals(AppState.NavItem.QUIZZES, AppState.navOverride.get());
        assertEquals("New Quiz", ((FakeHeaderController) getPrivate("headerController")).title);
    }

    @Test
    void generate_withoutUserDoesNothing() {
        ((TextField) getPrivate("countField")).setText("5");
        runOnFxThread(() -> callPrivate("generate"));
        assertNull(AppState.selectedQuiz.get());
    }

    @Test
    void cancel_setsNavOverride() {
        runOnFxThread(() -> callPrivate("cancel"));
        assertEquals(AppState.Screen.QUIZZES, AppState.currentScreen.get());
        assertNull(AppState.navOverride.get());
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = QuizFormController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = QuizFormController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String method) {
        try {
            Method m = QuizFormController.class.getDeclaredMethod(method);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timed out waiting for JavaFX task");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        if (failure.get() != null) {
            throw new RuntimeException(failure.get());
        }
    }
}




