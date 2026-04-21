package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.AppState;
import model.entity.Flashcard;
import model.service.FlashcardService;
import model.service.FlashcardSetService;
import util.I18n;
import util.LocaleManager;
import view.Navigator;
import model.entity.FlashcardSet;
import model.entity.User;
import java.util.ResourceBundle;

import java.util.List;

/**
 * Controller for creating and editing flashcards.
 * Supports two modes: ADD (creating new flashcard) and EDIT (modifying existing flashcard).
 * Provides a form for entering term, definition, and selecting a flashcard set.
 */
public class FlashcardFormController {

    // Combo box for selecting which flashcard set this card belongs to
    @FXML
    private ComboBox<FlashcardSet> subjectCombo;

    // ========== Header Components ==========
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    // ========== Form Input Components ==========
    // Text field for entering the term/question
    @FXML
    private TextField termField;
    
    // Text area for entering the definition/answer
    @FXML
    private TextArea definitionArea;

    // Service for flashcard database operations
    private final FlashcardService flashcardService = new FlashcardService();
    
    // Service for flashcard set database operations
    private final FlashcardSetService flashcardSetService = new FlashcardSetService();
    
    // Resource bundle for localized strings
    private final ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());
    /**
     * Initializes the controller when the FXML is loaded.
     * Loads all available flashcard sets, configures the header, and sets up the form
     * based on the current mode (ADD or EDIT).
     */
    @FXML
    private void initialize() {
        // ========== Load Flashcard Sets ==========
        // Fetch all flashcard sets from the database
        List<FlashcardSet> sets = flashcardSetService.getAllSets();
        subjectCombo.getItems().setAll(sets);

        // ========== Configure for EDIT Mode ==========
        // In EDIT mode, select the flashcard set that the card belongs to
        if (AppState.flashcardFormMode.get() == AppState.FormMode.EDIT) {
            subjectCombo.getSelectionModel().select(AppState.selectedFlashcardSet.get());
        }

        // ========== Configure Header ==========
        if (headerController != null) {
            // Show back button
            headerController.setBackVisible(true);
            
            // Set back action to navigate to flashcards list
            headerController.setOnBack(() -> {
                AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
                Navigator.go(AppState.Screen.FLASHCARDS);
            });

            // Set appropriate title based on form mode
            if (AppState.flashcardFormMode.get() == AppState.FormMode.EDIT) {
                headerController.setTitle(rb.getString("flashcardForm.title1"));
            } else {
                headerController.setTitle(rb.getString("flashcardForm.title2"));
            }
        }

        // ========== Prefill Form Data ==========
        // In EDIT mode, populate fields with the existing flashcard data
        if (AppState.flashcardFormMode.get() == AppState.FormMode.EDIT) {
            termField.setText(AppState.selectedTerm.get());
            definitionArea.setText(AppState.selectedDefinition.get());
        } else {
            // In ADD mode, clear the fields to start fresh
            termField.clear();
            definitionArea.clear();
        }

        // Set the active navigation item to FLASHCARDS
        AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
    }

    /**
     * Handles the save button click event.
     * Saves a new flashcard or updates an existing one based on the current form mode.
     * Validates input and manages the database and app state accordingly.
     * Navigates back to the appropriate screen after saving.
     */
    @FXML
    private void save() {
        // Get and trim input values
        String term = termField.getText() == null ? "" : termField.getText().trim();
        String def = definitionArea.getText() == null ? "" : definitionArea.getText().trim();
        FlashcardSet set = subjectCombo.getSelectionModel().getSelectedItem();
        User user = AppState.currentUser.get();

        // Validate required fields before saving
        String validationError = validateInput(set, term, def, user);
        if (validationError != null) {
            showWarning(validationError);
            return;
        }

        // ========== EDIT MODE ==========
        if (AppState.flashcardFormMode.get() == AppState.FormMode.EDIT) {
            // Get the index of the card being edited
            int idx = AppState.editingIndex.get();

            // Ensure index is valid
            if (idx >= 0 && idx < AppState.currentDetailList.size()) {
                // Get the existing flashcard from the list
                Flashcard card = AppState.currentDetailList.get(idx);

                // Update the flashcard with new values
                card.setTerm(term);
                card.setDefinition(def);
                card.setFlashcardSet(set);

                // Save changes to the database
                flashcardService.update(card);

                // Update the card in app state
                AppState.currentDetailList.set(idx, card);
            }
        } else {
            // ========== ADD MODE ==========
            // Create a new flashcard with the entered data
            Flashcard newCard = new Flashcard(term, def, set, user);
            
            // Save the new card to the database
            flashcardService.save(newCard);

            // Add the new card to the user's flashcard list in app state
            AppState.myFlashcards.add(newCard);
        }

        // ========== Navigate Back ==========
        // Return to the appropriate screen based on origin context
        if (AppState.isFromFlashcardSet.get()) {
            // Return to flashcard set details if form was accessed from there
            Navigator.go(AppState.Screen.FLASHCARD_SET);
        } else {
            // Return to flashcards list if form was accessed from there
            Navigator.go(AppState.Screen.FLASHCARDS);
        }
    }

    private String validateInput(FlashcardSet set, String term, String def, User user) {
        if (set == null || user == null || term.isBlank() || def.isBlank()) {
            return I18n.message(rb,
                    "flashcardForm.warning.fillAllFields",
                    "Please select a subject and fill in term and definition.");
        }
        return null;
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.setTitle(I18n.message(rb, "flashcardForm.alertTitle", "Flashcard"));
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    /**
     * Handles the cancel button click event.
     * Discards any unsaved changes and navigates back to the flashcards list.
     */
    @FXML
    private void cancel() {
        AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
        Navigator.go(AppState.Screen.FLASHCARDS);
    }
}