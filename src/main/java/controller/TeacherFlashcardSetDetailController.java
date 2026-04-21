package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.service.FlashcardSetService;
import util.LocalizationService;
import view.Navigator;

import java.text.MessageFormat;
import java.util.Map;

/**
 * Controller for the teacher flashcard set detail screen.
 * Displays all flashcards in a set and allows teachers to add, edit, and delete cards.
 * Provides an inline editor for managing flashcard term and definition.
 */
public class TeacherFlashcardSetDetailController {

    // ========== Header Components ==========
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    // ========== Editor Components ==========
    // Container for the inline flashcard editor (add/edit mode)
    @FXML
    private VBox editorBox;
    
    // Text field for entering the flashcard term/question
    @FXML
    private TextField termField;
    
    // Text area for entering the flashcard definition/answer
    @FXML
    private TextArea definitionArea;

    // ========== List Display Components ==========
    // Container for displaying the list of flashcards
    @FXML
    private VBox cardsBox;
    
    // Button to add a new flashcard
    @FXML
    private Button addMoreBtn;

    // ========== Data and Resources ==========
    // The currently selected flashcard set
    private FlashcardSet set;
    
    // Map for localized strings from resource bundle
    private Map<String, String> localizedStrings;

    // The flashcard currently being edited (null if adding new)
    private Flashcard editingRow = null;
    private final FlashcardSetService flashcardSetService = new FlashcardSetService();

    /**
     * Initializes the controller when the FXML is loaded.
     * Loads the flashcard set, sets up the header, and renders all flashcards.
     */
    @FXML
    private void initialize() {
        // ========== Load Localized Strings ==========
        localizedStrings = loadLocalizedStrings();
        
        // ========== Get Selected Flashcard Set ==========
        // Retrieve the flashcard set selected from the previous screen
        set = AppState.selectedSet.get();
        if (set == null) {
            // Navigate back if no set is selected
            navigateTo(AppState.Screen.TEACHER_CLASS_DETAIL);
            return;
        }
        if (set.getFlashcardSetId() == null) {
            navigateTo(AppState.Screen.TEACHER_CLASS_DETAIL);
            return;
        }
        set = loadSetWithCards(set.getFlashcardSetId());
        AppState.selectedSet.set(set);

        // ========== Configure Header ==========
        headerController.setBackVisible(true);
        headerController.setTitle(set.getSubject());
        headerController.setSubtitle(MessageFormat.format(localizedStrings.get("teacherFlashcardSetDetail.subtitle"), set.getCards().size()));
        headerController.setOnBack(() -> navigateTo(AppState.Screen.TEACHER_CLASS_DETAIL));
        headerController.applyVariant(HeaderController.Variant.TEACHER);

        // Set the active navigation item
        AppState.navOverride.set(AppState.NavItem.CLASSES);

        // ========== Initialize UI ==========
        // Hide the editor initially
        hideEditor();
        // Display all flashcards
        renderList();
        // Update the header with card count
        updateHeaderTotal();
    }

    // ========== UI STATE MANAGEMENT ==========

    /**
     * Shows the editor in add mode.
     * Clears the input fields and focuses on the term field.
     */
    private void showEditorForAdd() {
        // Set to null to indicate adding new card
        editingRow = null;
        // Clear input fields
        termField.clear();
        definitionArea.clear();
        // Show the editor
        editorBox.setVisible(true);
        editorBox.setManaged(true);
        // Focus on term field
        termField.requestFocus();
    }

    /**
     * Shows the editor in edit mode with the specified flashcard data.
     * Pre-fills the input fields with the card's current term and definition.
     *
     * @param row The flashcard to edit
     */
    private void showEditorForEdit(Flashcard row) {
        // Set the row being edited
        editingRow = row;
        // Pre-fill the fields with current data
        termField.setText(row.getTerm());
        definitionArea.setText(row.getDefinition());
        // Show the editor
        editorBox.setVisible(true);
        editorBox.setManaged(true);
        // Focus on term field
        termField.requestFocus();
    }

    /**
     * Hides the editor and resets the editing state.
     */
    private void hideEditor() {
        // Hide the editor
        editorBox.setVisible(false);
        editorBox.setManaged(false);
        // Reset the editing reference
        editingRow = null;
    }

    /**
     * Updates the header subtitle with the current flashcard count.
     */
    private void updateHeaderTotal() {
        headerController.setSubtitle(MessageFormat.format(localizedStrings.get("teacherFlashcardSetDetail.subtitle"), set.getCards().size()));
    }

    // ========== RENDERING ==========

    /**
     * Renders the list of all flashcards in the set.
     */
    private void renderList() {
        cardsBox.getChildren().clear();

        for (Flashcard row : set.getCards()) {
            cardsBox.getChildren().add(buildRowCard(row));
        }
    }

    /**
     * Builds a flashcard row component with term, definition, edit and delete buttons.
     *
     * @param row The flashcard entity to display
     * @return A VBox containing the formatted flashcard row
     */
    private VBox buildRowCard(Flashcard row) {
        // Create the card container
        VBox card = new VBox(6);
        card.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 18;
                    -fx-padding: 14 14 14 14;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 14, 0.2, 0, 6);
                """);

        HBox top = new HBox(10);
        VBox left = new VBox(4);
        HBox.setHgrow(left, Priority.ALWAYS);

        // ========== Left Side: Term and Definition ==========
        Label term = new Label(row.getTerm());
        term.setStyle("-fx-font-size: 16px; -fx-font-weight: 500; -fx-text-fill: #1F1F39;");

        Label def = new Label(row.getDefinition());
        def.setStyle("-fx-font-size: 14px; -fx-font-weight: 400; -fx-text-fill: rgba(0,0,0,0.45);");

        left.getChildren().addAll(term, def);

        // ========== Right Side: Edit Button ==========
        ImageView editIcon = new ImageView(
                new Image(getClass().getResourceAsStream("/images/edit_btn.png"))
        );
        editIcon.setFitWidth(20);
        editIcon.setFitHeight(20);

        Button editBtn = new Button();
        editBtn.setGraphic(editIcon);
        editBtn.setStyle("""
                    -fx-background-color: #EEF4FF;
                    -fx-background-radius: 20;
                    -fx-padding: 6;
                    -fx-cursor: hand;
                """);
        editBtn.setOnAction(e -> showEditorForEdit(row));

        // ========== Right Side: Delete Button ==========
        ImageView deleteIcon = new ImageView(
                new Image(getClass().getResourceAsStream("/images/delete_btn.png"))
        );
        deleteIcon.setFitWidth(20);
        deleteIcon.setFitHeight(20);

        Button deleteBtn = new Button();
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setStyle("""
                    -fx-background-color: #FFEEEE;
                    -fx-background-radius: 20;
                    -fx-padding: 6;
                    -fx-cursor: hand;
                """);
        deleteBtn.setOnAction(e -> {
            // Remove the flashcard from the set
            set.getCards().remove(row);
            // Hide the editor if open
            hideEditor();
            // Re-render the list
            renderList();
            // Update the header count
            updateHeaderTotal();
        });

        top.getChildren().addAll(left, editBtn, deleteBtn);

        card.getChildren().add(top);
        return card;
    }

    // ========== ACTION HANDLERS ==========

    /**
     * Handles the add more button click event.
     * Shows the editor in add mode for creating a new flashcard.
     */
    @FXML
    private void onAddMore() {
        showEditorForAdd();
    }

    /**
     * Handles the cancel button click event.
     * Closes the editor without saving.
     */
    @FXML
    private void onCancel() {
        hideEditor();
    }

    /**
     * Handles the save button click event.
     * Validates inputs and saves the flashcard (add or edit).
     */
    @FXML
    private void onSave() {
        // Get and trim input values
        String term = termField.getText() == null ? "" : termField.getText().trim();
        String def = definitionArea.getText() == null ? "" : definitionArea.getText().trim();
        
        // Validate that both fields are not empty
        if (term.isBlank() || def.isBlank()) return;

        // ========== Add or Edit ==========
        if (editingRow == null) {
            // ========== ADD MODE ==========
            // Create a new flashcard
            Flashcard newCard = new Flashcard();
            newCard.setTerm(term);
            newCard.setDefinition(def);
            newCard.setFlashcardSet(set);
            // Add to the set
            set.getCards().add(newCard);
        } else {
            // ========== EDIT MODE ==========
            // Update the existing card
            editingRow.setTerm(term);
            editingRow.setDefinition(def);
        }

        // ========== Refresh UI ==========
        // Hide the editor
        hideEditor();
        // Re-render the list
        renderList();
        // Update the header count
        updateHeaderTotal();
    }

    Map<String, String> loadLocalizedStrings() {
        return LocalizationService.getLocalizedStrings();
    }

    FlashcardSet loadSetWithCards(int setId) {
        return flashcardSetService.getSetWithCards(setId);
    }

    void navigateTo(AppState.Screen screen) {
        Navigator.go(screen);
    }
}
