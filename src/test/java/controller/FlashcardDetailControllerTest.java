package controller;

import controller.components.FlashcardFlipCardController;
import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import model.AppState;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlashcardDetailControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableFlashcardDetailController controller;
    private FakeHeaderController headerController;
    private FakeFlipCardController flipCardController;
    private Button prevButton;
    private Button nextButton;
    private Label pageLabel;
    private HBox navigationBox;
    private boolean previousFromFlashcardSet;
    private String previousDetailHeaderTitle;
    private int previousCurrentDetailIndex;
    private int previousEditingIndex;
    private AppState.FormMode previousFlashcardFormMode;
    private String previousSelectedTerm;
    private String previousSelectedDefinition;
    private AppState.NavItem previousNavOverride;
    private FlashcardSet previousSelectedFlashcardSet;
    private List<Flashcard> previousCurrentDetailList;
    private List<Flashcard> previousMyFlashcards;

    private static final class TestableFlashcardDetailController extends FlashcardDetailController {
        private Map<String, String> localizedStrings = Map.of("flashcardSet.subtitle", "Total: {0}");
        private AppState.Screen lastNavigatedScreen;
        private Flashcard deletedCard;
        private RuntimeException deleteFailure;
        private boolean deleteErrorShown;

        @Override
        Map<String, String> loadLocalizedStrings() {
            return localizedStrings;
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }

        @Override
        void deleteFlashcard(Flashcard card) {
            deletedCard = card;
            if (deleteFailure != null) {
                throw deleteFailure;
            }
        }

        @Override
        void showDeleteError() {
            deleteErrorShown = true;
        }
    }

    private static final class FakeHeaderController extends HeaderController {
        private boolean backVisible;
        private String title;
        private String subtitle;
        private boolean actionsVisible;
        private Runnable backAction;
        private Runnable editAction;
        private Runnable deleteAction;

        @Override
        public void setBackVisible(boolean visible) {
            backVisible = visible;
        }

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        @Override
        public void setActionsVisible(boolean visible) {
            actionsVisible = visible;
        }

        @Override
        public void setOnBack(Runnable action) {
            backAction = action;
        }

        @Override
        public void setOnEdit(Runnable action) {
            editAction = action;
        }

        @Override
        public void setOnDelete(Runnable action) {
            deleteAction = action;
        }
    }

    private static final class FakeFlipCardController extends FlashcardFlipCardController {
        private String term;
        private String definition;
        private int showTermCalls;

        @Override
        public void setTerm(String text) {
            term = text;
        }

        @Override
        public void setDefinition(String text) {
            definition = text;
        }

        @Override
        public void showTerm() {
            showTermCalls++;
        }
    }

    @BeforeEach
    void setUp() {
        previousFromFlashcardSet = AppState.isFromFlashcardSet.get();
        previousDetailHeaderTitle = AppState.detailHeaderTitle.get();
        previousCurrentDetailIndex = AppState.currentDetailIndex.get();
        previousEditingIndex = AppState.editingIndex.get();
        previousFlashcardFormMode = AppState.flashcardFormMode.get();
        previousSelectedTerm = AppState.selectedTerm.get();
        previousSelectedDefinition = AppState.selectedDefinition.get();
        previousNavOverride = AppState.navOverride.get();
        previousSelectedFlashcardSet = AppState.selectedFlashcardSet.get();
        previousCurrentDetailList = new ArrayList<>(AppState.currentDetailList);
        previousMyFlashcards = new ArrayList<>(AppState.myFlashcards);

        controller = new TestableFlashcardDetailController();
        headerController = new FakeHeaderController();
        flipCardController = new FakeFlipCardController();
        prevButton = new Button();
        nextButton = new Button();
        pageLabel = new Label();
        navigationBox = new HBox();

        setPrivate("header", new Parent() {});
        setPrivate("headerController", headerController);
        setPrivate("flipCard", new Parent() {});
        setPrivate("flipCardController", flipCardController);
        setPrivate("prevButton", prevButton);
        setPrivate("nextButton", nextButton);
        setPrivate("pageLabel", pageLabel);
        setPrivate("navigationBox", navigationBox);

        AppState.isFromFlashcardSet.set(false);
        AppState.detailHeaderTitle.set("");
        AppState.currentDetailIndex.set(0);
        AppState.editingIndex.set(-1);
        AppState.flashcardFormMode.set(AppState.FormMode.ADD);
        AppState.selectedTerm.set("");
        AppState.selectedDefinition.set("");
        AppState.navOverride.set(null);
        AppState.selectedFlashcardSet.set(null);
        AppState.currentDetailList.clear();
        AppState.myFlashcards.clear();
    }

    @AfterEach
    void tearDown() {
        AppState.isFromFlashcardSet.set(previousFromFlashcardSet);
        AppState.detailHeaderTitle.set(previousDetailHeaderTitle);
        AppState.currentDetailIndex.set(previousCurrentDetailIndex);
        AppState.editingIndex.set(previousEditingIndex);
        AppState.flashcardFormMode.set(previousFlashcardFormMode);
        AppState.selectedTerm.set(previousSelectedTerm);
        AppState.selectedDefinition.set(previousSelectedDefinition);
        AppState.navOverride.set(previousNavOverride);
        AppState.selectedFlashcardSet.set(previousSelectedFlashcardSet);
        AppState.currentDetailList.clear();
        AppState.currentDetailList.addAll(previousCurrentDetailList);
        AppState.myFlashcards.clear();
        AppState.myFlashcards.addAll(previousMyFlashcards);
    }

    @Test
    void initialize_fromFlashcardSet_configuresHeaderAndShowsClampedCard() {
        Flashcard card1 = createCard("Cell", "Basic unit");
        Flashcard card2 = createCard("DNA", "Genetic code");
        AppState.isFromFlashcardSet.set(true);
        AppState.detailHeaderTitle.set("Biology");
        AppState.currentDetailList.addAll(card1, card2);
        AppState.currentDetailIndex.set(99);

        callPrivate("initialize");

        assertTrue(headerController.backVisible);
        assertEquals("Biology", headerController.title);
        assertEquals("Total: 2", headerController.subtitle);
        assertFalse(headerController.actionsVisible);
        assertEquals(NodeOrientation.LEFT_TO_RIGHT, navigationBox.getNodeOrientation());
        assertEquals("2 / 2", pageLabel.getText());
        assertEquals("DNA", flipCardController.term);
        assertEquals("Genetic code", flipCardController.definition);
        assertEquals(1, flipCardController.showTermCalls);
        assertFalse(prevButton.isDisable());
        assertTrue(nextButton.isDisable());

        headerController.backAction.run();
        assertEquals(AppState.Screen.FLASHCARD_SET, controller.lastNavigatedScreen);
    }

    @Test
    void initialize_withoutCards_rendersPlaceholderAndActionsAreNoOp() {
        AppState.detailHeaderTitle.set("My Cards");

        callPrivate("initialize");

        assertTrue(headerController.backVisible);
        assertEquals("My Cards", headerController.title);
        assertEquals("Total: 0", headerController.subtitle);
        assertTrue(headerController.actionsVisible);
        assertEquals("0 / 0", pageLabel.getText());
        assertEquals("No cards", flipCardController.term);
        assertEquals("This set has no flashcards yet.", flipCardController.definition);
        assertEquals(1, flipCardController.showTermCalls);
        assertTrue(prevButton.isDisable());
        assertTrue(nextButton.isDisable());

        headerController.editAction.run();
        headerController.deleteAction.run();

        assertNull(controller.lastNavigatedScreen);
        assertNull(controller.deletedCard);
        assertFalse(controller.deleteErrorShown);

        headerController.backAction.run();
        assertEquals(AppState.Screen.FLASHCARDS, controller.lastNavigatedScreen);
    }

    @Test
    void editAction_setsEditingStateAndNavigatesToForm() {
        Flashcard card1 = createCard("Cell", "Basic unit");
        Flashcard card2 = createCard("DNA", "Genetic code");
        AppState.detailHeaderTitle.set("My Cards");
        AppState.currentDetailList.addAll(card1, card2);
        AppState.currentDetailIndex.set(1);

        callPrivate("initialize");
        headerController.editAction.run();

        assertEquals(1, AppState.editingIndex.get());
        assertEquals(AppState.FormMode.EDIT, AppState.flashcardFormMode.get());
        assertEquals("DNA", AppState.selectedTerm.get());
        assertEquals("Genetic code", AppState.selectedDefinition.get());
        assertEquals(AppState.NavItem.FLASHCARDS, AppState.navOverride.get());
        assertEquals(AppState.Screen.FLASHCARD_FORM, controller.lastNavigatedScreen);
    }

    @Test
    void deleteAction_withRemainingCards_updatesStateAndRendersPreviousCard() {
        Flashcard card1 = createCard("Cell", "Basic unit");
        Flashcard card2 = createCard("DNA", "Genetic code");
        FlashcardSet set = createSet(card1, card2);
        AppState.currentDetailList.addAll(card1, card2);
        AppState.currentDetailIndex.set(1);
        AppState.selectedFlashcardSet.set(set);
        AppState.myFlashcards.addAll(card1, card2);

        callPrivate("initialize");
        headerController.deleteAction.run();

        assertSame(card2, controller.deletedCard);
        assertEquals(1, AppState.currentDetailList.size());
        assertSame(card1, AppState.currentDetailList.getFirst());
        assertEquals(1, AppState.myFlashcards.size());
        assertSame(card1, AppState.myFlashcards.getFirst());
        assertEquals(1, set.getCards().size());
        assertSame(card1, new ArrayList<>(set.getCards()).getFirst());
        assertEquals(0, AppState.currentDetailIndex.get());
        assertEquals("1 / 1", pageLabel.getText());
        assertEquals("Cell", flipCardController.term);
        assertEquals("Basic unit", flipCardController.definition);
        assertTrue(prevButton.isDisable());
        assertTrue(nextButton.isDisable());
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void deleteAction_withLastCard_navigatesBackToFlashcards() {
        Flashcard card = createCard("Cell", "Basic unit");
        FlashcardSet set = createSet(card);
        AppState.currentDetailList.add(card);
        AppState.selectedFlashcardSet.set(set);
        AppState.myFlashcards.add(card);

        callPrivate("initialize");
        headerController.deleteAction.run();

        assertSame(card, controller.deletedCard);
        assertTrue(AppState.currentDetailList.isEmpty());
        assertTrue(AppState.myFlashcards.isEmpty());
        assertTrue(set.getCards().isEmpty());
        assertEquals(AppState.NavItem.FLASHCARDS, AppState.navOverride.get());
        assertEquals(AppState.Screen.FLASHCARDS, controller.lastNavigatedScreen);
    }

    @Test
    void deleteAction_whenDeleteFails_showsErrorAndKeepsCards() {
        Flashcard card = createCard("Cell", "Basic unit");
        FlashcardSet set = createSet(card);
        controller.deleteFailure = new IllegalArgumentException("Delete failed");
        AppState.currentDetailList.add(card);
        AppState.selectedFlashcardSet.set(set);
        AppState.myFlashcards.add(card);

        callPrivate("initialize");
        headerController.deleteAction.run();

        assertTrue(controller.deleteErrorShown);
        assertSame(card, controller.deletedCard);
        assertEquals(1, AppState.currentDetailList.size());
        assertEquals(1, AppState.myFlashcards.size());
        assertEquals(1, set.getCards().size());
        assertNull(controller.lastNavigatedScreen);
    }

    @Test
    void prevAndNext_updatePageAndIgnoreOutOfBoundsClicks() {
        Flashcard card1 = createCard("Cell", "Basic unit");
        Flashcard card2 = createCard("DNA", "Genetic code");
        AppState.currentDetailList.addAll(card1, card2);

        callPrivate("initialize");

        callPrivate("prev");
        assertEquals(0, AppState.currentDetailIndex.get());
        assertEquals("1 / 2", pageLabel.getText());
        assertEquals("Cell", flipCardController.term);

        callPrivate("next");
        assertEquals(1, AppState.currentDetailIndex.get());
        assertEquals("2 / 2", pageLabel.getText());
        assertEquals("DNA", flipCardController.term);
        assertFalse(prevButton.isDisable());
        assertTrue(nextButton.isDisable());

        callPrivate("next");
        assertEquals(1, AppState.currentDetailIndex.get());
        assertEquals("2 / 2", pageLabel.getText());

        callPrivate("prev");
        assertEquals(0, AppState.currentDetailIndex.get());
        assertEquals("1 / 2", pageLabel.getText());
        assertEquals("Cell", flipCardController.term);
        assertTrue(prevButton.isDisable());
        assertFalse(nextButton.isDisable());
    }

    private Flashcard createCard(String term, String definition) {
        Flashcard card = new Flashcard();
        card.setTerm(term);
        card.setDefinition(definition);
        return card;
    }

    private FlashcardSet createSet(Flashcard... cards) {
        FlashcardSet set = new FlashcardSet();
        ArrayList<Flashcard> collection = new ArrayList<>(List.of(cards));
        for (Flashcard card : collection) {
            card.setFlashcardSet(set);
        }
        setEntityField(FlashcardSet.class, set, "cards", collection);
        return set;
    }

    private void setEntityField(Class<?> type, Object target, String fieldName, Object value) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setPrivate(String fieldName, Object value) {
        try {
            Field field = FlashcardDetailController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method method = FlashcardDetailController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(cause);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
