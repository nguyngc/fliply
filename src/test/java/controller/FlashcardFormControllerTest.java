package controller;

import controller.components.HeaderController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import model.AppState;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlashcardFormControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableFlashcardFormController controller;
    private FakeHeaderController headerController;
    private ComboBox<FlashcardSet> subjectCombo;
    private TextField termField;
    private TextArea definitionArea;
    private User previousCurrentUser;
    private FlashcardSet previousSelectedFlashcardSet;
    private AppState.FormMode previousFormMode;
    private int previousEditingIndex;
    private String previousSelectedTerm;
    private String previousSelectedDefinition;
    private boolean previousIsFromFlashcardSet;
    private AppState.NavItem previousNavOverride;
    private List<Flashcard> previousCurrentDetailList;
    private List<Flashcard> previousMyFlashcards;

    private static final class TestableFlashcardFormController extends FlashcardFormController {
        private final List<FlashcardSet> setsToLoad = new ArrayList<>();
        private Flashcard savedCard;
        private Flashcard updatedCard;
        private String warningMessage;
        private AppState.Screen lastNavigatedScreen;

        @Override
        List<FlashcardSet> loadAllSets() {
            return new ArrayList<>(setsToLoad);
        }

        @Override
        void updateFlashcard(Flashcard card) {
            updatedCard = card;
        }

        @Override
        void saveFlashcard(Flashcard newCard) {
            savedCard = newCard;
        }

        @Override
        void showWarning(String message) {
            warningMessage = message;
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    private static final class FakeHeaderController extends HeaderController {
        private String title;
        private boolean backVisible;
        private Runnable backAction;

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public void setBackVisible(boolean visible) {
            backVisible = visible;
        }

        @Override
        public void setOnBack(Runnable action) {
            backAction = action;
        }
    }

    @BeforeEach
    void setUp() {
        previousCurrentUser = AppState.currentUser.get();
        previousSelectedFlashcardSet = AppState.selectedFlashcardSet.get();
        previousFormMode = AppState.flashcardFormMode.get();
        previousEditingIndex = AppState.editingIndex.get();
        previousSelectedTerm = AppState.selectedTerm.get();
        previousSelectedDefinition = AppState.selectedDefinition.get();
        previousIsFromFlashcardSet = AppState.isFromFlashcardSet.get();
        previousNavOverride = AppState.navOverride.get();
        previousCurrentDetailList = new ArrayList<>(AppState.currentDetailList);
        previousMyFlashcards = new ArrayList<>(AppState.myFlashcards);

        controller = new TestableFlashcardFormController();
        headerController = new FakeHeaderController();
        subjectCombo = new ComboBox<>();
        termField = new TextField();
        definitionArea = new TextArea();

        setPrivate("header", new StackPane());
        setPrivate("headerController", headerController);
        setPrivate("subjectCombo", subjectCombo);
        setPrivate("termField", termField);
        setPrivate("definitionArea", definitionArea);

        AppState.currentUser.set(null);
        AppState.selectedFlashcardSet.set(null);
        AppState.flashcardFormMode.set(AppState.FormMode.ADD);
        AppState.editingIndex.set(-1);
        AppState.selectedTerm.set("");
        AppState.selectedDefinition.set("");
        AppState.isFromFlashcardSet.set(false);
        AppState.navOverride.set(null);
        AppState.currentDetailList.clear();
        AppState.myFlashcards.clear();
    }

    @AfterEach
    void tearDown() {
        AppState.currentUser.set(previousCurrentUser);
        AppState.selectedFlashcardSet.set(previousSelectedFlashcardSet);
        AppState.flashcardFormMode.set(previousFormMode);
        AppState.editingIndex.set(previousEditingIndex);
        AppState.selectedTerm.set(previousSelectedTerm);
        AppState.selectedDefinition.set(previousSelectedDefinition);
        AppState.isFromFlashcardSet.set(previousIsFromFlashcardSet);
        AppState.navOverride.set(previousNavOverride);
        AppState.currentDetailList.clear();
        AppState.currentDetailList.addAll(previousCurrentDetailList);
        AppState.myFlashcards.clear();
        AppState.myFlashcards.addAll(previousMyFlashcards);
    }

    @Test
    void initialize_addModeLoadsSetsClearsFieldsAndConfiguresBackAction() {
        FlashcardSet biology = createSet(10, "Biology");
        FlashcardSet chemistry = createSet(11, "Chemistry");
        controller.setsToLoad.add(biology);
        controller.setsToLoad.add(chemistry);
        termField.setText("Old term");
        definitionArea.setText("Old definition");

        runOnFxThread(() -> callPrivate("initialize"));

        assertEquals(List.of(biology, chemistry), new ArrayList<>(subjectCombo.getItems()));
        assertEquals("New Flashcard", headerController.title);
        assertTrue(headerController.backVisible);
        assertEquals("", termField.getText());
        assertEquals("", definitionArea.getText());
        assertEquals(AppState.NavItem.FLASHCARDS, AppState.navOverride.get());

        headerController.backAction.run();
        assertEquals(AppState.Screen.FLASHCARDS, controller.lastNavigatedScreen);
        assertEquals(AppState.NavItem.FLASHCARDS, AppState.navOverride.get());
    }

    @Test
    void initialize_editModePrefillsFormAndSelectsCurrentSet() {
        FlashcardSet biology = createSet(10, "Biology");
        FlashcardSet chemistry = createSet(11, "Chemistry");
        controller.setsToLoad.add(biology);
        controller.setsToLoad.add(chemistry);
        AppState.flashcardFormMode.set(AppState.FormMode.EDIT);
        AppState.selectedFlashcardSet.set(chemistry);
        AppState.selectedTerm.set("Cell");
        AppState.selectedDefinition.set("Basic unit");

        runOnFxThread(() -> callPrivate("initialize"));

        assertEquals("Edit Flashcard", headerController.title);
        assertSame(chemistry, subjectCombo.getSelectionModel().getSelectedItem());
        assertEquals("Cell", termField.getText());
        assertEquals("Basic unit", definitionArea.getText());
    }

    @Test
    void save_withInvalidInputShowsWarningAndSkipsPersistence() {
        AppState.currentUser.set(createUser(1));
        controller.setsToLoad.add(createSet(10, "Biology"));
        runOnFxThread(() -> {
            callPrivate("initialize");
            termField.setText("Term");
            definitionArea.setText(" ");
            callPrivate("save");
        });

        assertEquals("Please select a subject and fill in term and definition.", controller.warningMessage);
        assertNull(controller.savedCard);
        assertNull(controller.updatedCard);
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void save_addModePersistsCardAddsToListAndNavigatesBackToSet() {
        FlashcardSet set = createSet(10, "Biology");
        User user = createUser(5);
        AppState.currentUser.set(user);
        AppState.isFromFlashcardSet.set(true);
        controller.setsToLoad.add(set);
        runOnFxThread(() -> {
            callPrivate("initialize");
            subjectCombo.getSelectionModel().select(set);
            termField.setText("DNA");
            definitionArea.setText("Genetic code");
            callPrivate("save");
        });

        assertEquals("DNA", controller.savedCard.getTerm());
        assertEquals("Genetic code", controller.savedCard.getDefinition());
        assertSame(set, controller.savedCard.getFlashcardSet());
        assertSame(user, controller.savedCard.getUser());
        assertSame(controller.savedCard, AppState.myFlashcards.get(0));
        assertEquals(AppState.Screen.FLASHCARD_SET, controller.lastNavigatedScreen);
    }

    @Test
    void save_editModeUpdatesExistingCardAndNavigatesToFlashcards() {
        FlashcardSet oldSet = createSet(10, "Biology");
        FlashcardSet newSet = createSet(11, "Chemistry");
        Flashcard existing = new Flashcard("Old", "Before", oldSet, createUser(7));
        AppState.currentUser.set(createUser(8));
        controller.setsToLoad.add(oldSet);
        controller.setsToLoad.add(newSet);
        AppState.flashcardFormMode.set(AppState.FormMode.EDIT);
        AppState.selectedFlashcardSet.set(oldSet);
        AppState.selectedTerm.set("Old");
        AppState.selectedDefinition.set("Before");
        AppState.editingIndex.set(0);
        AppState.currentDetailList.add(existing);

        runOnFxThread(() -> {
            callPrivate("initialize");
            subjectCombo.getSelectionModel().select(newSet);
            termField.setText("Updated");
            definitionArea.setText("After");
            callPrivate("save");
        });

        assertSame(existing, controller.updatedCard);
        assertEquals("Updated", existing.getTerm());
        assertEquals("After", existing.getDefinition());
        assertSame(newSet, existing.getFlashcardSet());
        assertSame(existing, AppState.currentDetailList.get(0));
        assertEquals(AppState.Screen.FLASHCARDS, controller.lastNavigatedScreen);
    }

    @Test
    void cancel_navigatesToFlashcards() {
        runOnFxThread(() -> callPrivate("cancel"));

        assertEquals(AppState.NavItem.FLASHCARDS, AppState.navOverride.get());
        assertEquals(AppState.Screen.FLASHCARDS, controller.lastNavigatedScreen);
    }

    private FlashcardSet createSet(int setId, String subject) {
        FlashcardSet set = new FlashcardSet();
        setField(FlashcardSet.class, set, "flashcardSetId", setId);
        set.setSubject(subject);
        return set;
    }

    private User createUser(int userId) {
        User user = new User();
        setField(User.class, user, "userId", userId);
        user.setEmail("user@test.com");
        return user;
    }

    private void setPrivate(String fieldName, Object value) {
        setField(FlashcardFormController.class, controller, fieldName, value);
    }

    private void setField(Class<?> type, Object target, String fieldName, Object value) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method method = FlashcardFormController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runOnFxThread(Runnable action) {
        runOnFxThreadWithResult(() -> {
            action.run();
            return null;
        });
    }

    private <T> T runOnFxThreadWithResult(Callable<T> action) {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(action.call());
            } catch (Throwable throwable) {
                error.set(throwable);
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
        return result.get();
    }
}
