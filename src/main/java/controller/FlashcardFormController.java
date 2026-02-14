package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.AppState;
import view.Navigator;

public class FlashcardFormController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private TextField termField;
    @FXML
    private TextArea definitionArea;

    @FXML
    private void initialize() {
        if (headerController != null) {
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> {
                AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
                Navigator.go(AppState.Screen.FLASHCARDS);
            });

            if (AppState.flashcardFormMode.get() == AppState.FormMode.EDIT) {
                headerController.setTitle("Edit Flashcard");
            } else {
                headerController.setTitle("New Flashcard");
            }
        }

        // Prefill if EDIT
        if (AppState.flashcardFormMode.get() == AppState.FormMode.EDIT) {
            termField.setText(AppState.selectedTerm.get());
            definitionArea.setText(AppState.selectedDefinition.get());
        } else {
            termField.clear();
            definitionArea.clear();
        }

        // Keep Flashcards menu active
        AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
    }

    @FXML
    private void save() {
        String term = termField.getText() == null ? "" : termField.getText().trim();
        String def = definitionArea.getText() == null ? "" : definitionArea.getText().trim();

        if (term.isBlank()) return; // simple validation (optional)

        if (AppState.flashcardFormMode.get() == AppState.FormMode.EDIT) {
            int idx = AppState.editingIndex.get();
            if (idx >= 0 && idx < AppState.myFlashcards.size()) {
                AppState.myFlashcards.set(idx, new AppState.FlashcardItem(term, def));
            }
        } else {
            AppState.myFlashcards.add(new AppState.FlashcardItem(term, def));
        }

        // After save -> back to list
        AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
        Navigator.go(AppState.Screen.FLASHCARDS);
    }

    @FXML
    private void cancel() {
        AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
        Navigator.go(AppState.Screen.FLASHCARDS);
    }
}
