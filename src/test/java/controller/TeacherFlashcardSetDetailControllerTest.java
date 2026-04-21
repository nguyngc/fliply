package controller;

import controller.components.HeaderController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeacherFlashcardSetDetailControllerTest {

    static {
        System.setProperty("javafx.cachedir", System.getProperty("java.io.tmpdir") + "/openjfx-cache");
        new JFXPanel();
    }

    private TestableTeacherFlashcardSetDetailController controller;
    private FakeHeaderController headerController;
    private VBox editorBox;
    private TextField termField;
    private TextArea definitionArea;
    private VBox cardsBox;
    private Button addMoreBtn;
    private FlashcardSet previousSelectedSet;
    private AppState.NavItem previousNavOverride;

    private static final class TestableTeacherFlashcardSetDetailController extends TeacherFlashcardSetDetailController {
        private Map<String, String> localizedStrings = Map.of("teacherFlashcardSetDetail.subtitle", "Total: {0}");
        private FlashcardSet loadedSet;
        private Integer lastLoadedSetId;
        private AppState.Screen lastNavigatedScreen;

        @Override
        Map<String, String> loadLocalizedStrings() {
            return localizedStrings;
        }

        @Override
        FlashcardSet loadSetWithCards(int setId) {
            lastLoadedSetId = setId;
            return loadedSet;
        }

        @Override
        void navigateTo(AppState.Screen screen) {
            lastNavigatedScreen = screen;
        }
    }

    private static final class FakeHeaderController extends HeaderController {
        private boolean backVisible;
        private String title;
        private String subtitle;
        private Runnable backAction;
        private Variant variant;

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
        public void setOnBack(Runnable action) {
            backAction = action;
        }

        @Override
        public void applyVariant(Variant variant) {
            this.variant = variant;
        }
    }

    @BeforeEach
    void setUp() {
        previousSelectedSet = AppState.selectedSet.get();
        previousNavOverride = AppState.navOverride.get();

        controller = new TestableTeacherFlashcardSetDetailController();
        headerController = new FakeHeaderController();
        editorBox = new VBox();
        termField = new TextField();
        definitionArea = new TextArea();
        cardsBox = new VBox();
        addMoreBtn = new Button();

        setPrivate("header", new Parent() {});
        setPrivate("headerController", headerController);
        setPrivate("editorBox", editorBox);
        setPrivate("termField", termField);
        setPrivate("definitionArea", definitionArea);
        setPrivate("cardsBox", cardsBox);
        setPrivate("addMoreBtn", addMoreBtn);

        AppState.selectedSet.set(null);
        AppState.navOverride.set(null);
    }

    @AfterEach
    void tearDown() {
        AppState.selectedSet.set(previousSelectedSet);
        AppState.navOverride.set(previousNavOverride);
    }

    @Test
    void initialize_withoutSelectedSet_navigatesBack() {
        callPrivate("initialize");

        assertEquals(AppState.Screen.TEACHER_CLASS_DETAIL, controller.lastNavigatedScreen);
        assertNull(controller.lastLoadedSetId);
    }

    @Test
    void initialize_withoutSetId_navigatesBack() {
        AppState.selectedSet.set(createSet(null, "Biology"));

        callPrivate("initialize");

        assertEquals(AppState.Screen.TEACHER_CLASS_DETAIL, controller.lastNavigatedScreen);
        assertNull(controller.lastLoadedSetId);
    }

    @Test
    void initialize_loadsSetConfiguresHeaderAndRendersCards() {
        FlashcardSet initialSet = createSet(7, "Biology");
        FlashcardSet loadedSet = createSet(
                7,
                "Biology",
                createCard("Cell", "Basic unit"),
                createCard("DNA", "Genetic code")
        );
        controller.loadedSet = loadedSet;
        AppState.selectedSet.set(initialSet);

        callPrivate("initialize");

        assertSame(loadedSet, AppState.selectedSet.get());
        assertEquals(7, controller.lastLoadedSetId);
        assertTrue(headerController.backVisible);
        assertEquals("Biology", headerController.title);
        assertEquals("Total: 2", headerController.subtitle);
        assertEquals(HeaderController.Variant.TEACHER, headerController.variant);
        assertEquals(AppState.NavItem.CLASSES, AppState.navOverride.get());
        assertFalse(editorBox.isVisible());
        assertFalse(editorBox.isManaged());
        assertEquals(2, cardsBox.getChildren().size());
        assertEquals("Cell", getCardTermLabel(0).getText());
        assertEquals("Basic unit", getCardDefinitionLabel(0).getText());

        headerController.backAction.run();
        assertEquals(AppState.Screen.TEACHER_CLASS_DETAIL, controller.lastNavigatedScreen);
    }

    @Test
    void onAddMore_showsEditorForAddAndClearsFields() {
        FlashcardSet loadedSet = createSet(7, "Biology", createCard("Cell", "Basic unit"));
        controller.loadedSet = loadedSet;
        AppState.selectedSet.set(createSet(7, "Biology"));
        callPrivate("initialize");

        termField.setText("Old term");
        definitionArea.setText("Old definition");
        setPrivate("editingRow", loadedSet.getCards().iterator().next());

        callPrivate("onAddMore");

        assertTrue(editorBox.isVisible());
        assertTrue(editorBox.isManaged());
        assertEquals("", termField.getText());
        assertEquals("", definitionArea.getText());
        assertNull(getPrivate("editingRow"));
    }

    @Test
    void onCancel_hidesEditorAndClearsEditingRow() {
        Flashcard existingCard = createCard("Cell", "Basic unit");
        FlashcardSet loadedSet = createSet(7, "Biology", existingCard);
        controller.loadedSet = loadedSet;
        AppState.selectedSet.set(createSet(7, "Biology"));
        callPrivate("initialize");

        getEditButton(0).fire();
        callPrivate("onCancel");

        assertFalse(editorBox.isVisible());
        assertFalse(editorBox.isManaged());
        assertNull(getPrivate("editingRow"));
    }

    @Test
    void onSave_withBlankFields_doesNothing() {
        Flashcard existingCard = createCard("Cell", "Basic unit");
        FlashcardSet loadedSet = createSet(7, "Biology", existingCard);
        controller.loadedSet = loadedSet;
        AppState.selectedSet.set(createSet(7, "Biology"));
        callPrivate("initialize");

        callPrivate("onAddMore");
        termField.setText("   ");
        definitionArea.setText("Definition");
        callPrivate("onSave");

        assertEquals(1, loadedSet.getCards().size());
        assertEquals(1, cardsBox.getChildren().size());
        assertTrue(editorBox.isVisible());
        assertTrue(editorBox.isManaged());
        assertEquals("Total: 1", headerController.subtitle);
    }

    @Test
    void onSave_addMode_addsCardRendersListAndUpdatesHeader() {
        FlashcardSet loadedSet = createSet(7, "Biology", createCard("Cell", "Basic unit"));
        controller.loadedSet = loadedSet;
        AppState.selectedSet.set(createSet(7, "Biology"));
        callPrivate("initialize");

        callPrivate("onAddMore");
        termField.setText("DNA");
        definitionArea.setText("Genetic code");
        callPrivate("onSave");

        assertEquals(2, loadedSet.getCards().size());
        assertEquals(2, cardsBox.getChildren().size());
        assertFalse(editorBox.isVisible());
        assertFalse(editorBox.isManaged());
        assertEquals("Total: 2", headerController.subtitle);

        Flashcard newCard = getCardFromSet(loadedSet, 1);
        assertEquals("DNA", newCard.getTerm());
        assertEquals("Genetic code", newCard.getDefinition());
        assertSame(loadedSet, newCard.getFlashcardSet());
        assertEquals("DNA", getCardTermLabel(1).getText());
    }

    @Test
    void editAndDeleteButtons_updateAndRemoveCards() {
        Flashcard firstCard = createCard("Cell", "Basic unit");
        Flashcard secondCard = createCard("DNA", "Genetic code");
        FlashcardSet loadedSet = createSet(7, "Biology", firstCard, secondCard);
        controller.loadedSet = loadedSet;
        AppState.selectedSet.set(createSet(7, "Biology"));
        callPrivate("initialize");

        getEditButton(0).fire();
        assertTrue(editorBox.isVisible());
        assertSame(firstCard, getPrivate("editingRow"));
        assertEquals("Cell", termField.getText());
        assertEquals("Basic unit", definitionArea.getText());

        termField.setText("Cell membrane");
        definitionArea.setText("Outer layer");
        callPrivate("onSave");

        assertEquals(2, loadedSet.getCards().size());
        assertEquals("Cell membrane", firstCard.getTerm());
        assertEquals("Outer layer", firstCard.getDefinition());
        assertEquals("Cell membrane", getCardTermLabel(0).getText());
        assertEquals("Total: 2", headerController.subtitle);

        getEditButton(0).fire();
        assertTrue(editorBox.isVisible());
        getDeleteButton(0).fire();

        assertEquals(1, loadedSet.getCards().size());
        assertEquals(1, cardsBox.getChildren().size());
        assertFalse(editorBox.isVisible());
        assertFalse(editorBox.isManaged());
        assertEquals("Total: 1", headerController.subtitle);
        assertEquals("DNA", getCardTermLabel(0).getText());
    }

    private FlashcardSet createSet(Integer setId, String subject, Flashcard... cards) {
        FlashcardSet set = new FlashcardSet();
        set.setSubject(subject);
        setEntityField(FlashcardSet.class, set, "flashcardSetId", setId);

        List<Flashcard> collection = new ArrayList<>(List.of(cards));
        for (Flashcard card : collection) {
            card.setFlashcardSet(set);
        }
        setEntityField(FlashcardSet.class, set, "cards", collection);
        return set;
    }

    private Flashcard createCard(String term, String definition) {
        Flashcard card = new Flashcard();
        card.setTerm(term);
        card.setDefinition(definition);
        return card;
    }

    private Flashcard getCardFromSet(FlashcardSet set, int index) {
        return new ArrayList<>(set.getCards()).get(index);
    }

    private Label getCardTermLabel(int index) {
        VBox left = getCardContentBox(index);
        return (Label) left.getChildren().get(0);
    }

    private Label getCardDefinitionLabel(int index) {
        VBox left = getCardContentBox(index);
        return (Label) left.getChildren().get(1);
    }

    private VBox getCardContentBox(int index) {
        VBox card = (VBox) cardsBox.getChildren().get(index);
        HBox top = (HBox) card.getChildren().get(0);
        return (VBox) top.getChildren().get(0);
    }

    private Button getEditButton(int index) {
        VBox card = (VBox) cardsBox.getChildren().get(index);
        HBox top = (HBox) card.getChildren().get(0);
        return (Button) top.getChildren().get(1);
    }

    private Button getDeleteButton(int index) {
        VBox card = (VBox) cardsBox.getChildren().get(index);
        HBox top = (HBox) card.getChildren().get(0);
        return (Button) top.getChildren().get(2);
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
            Field field = TeacherFlashcardSetDetailController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String fieldName) {
        try {
            Field field = TeacherFlashcardSetDetailController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String methodName) {
        try {
            Method method = TeacherFlashcardSetDetailController.class.getDeclaredMethod(methodName);
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
