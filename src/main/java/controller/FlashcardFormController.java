package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.AppState;
import model.entity.Flashcard;
import model.service.FlashcardService;
import view.Navigator;
import model.entity.FlashcardSet;
import model.entity.User;


public class FlashcardFormController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private TextField termField;
    @FXML
    private TextArea definitionArea;

    private final FlashcardService flashcardService =  new FlashcardService();

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

        if (term.isBlank()) return;

        FlashcardSet set = AppState.selectedFlashcardSet.get();
        User user = AppState.currentUser.get();

        if (set == null || user == null) return;

        // EDIT MODE
        if (AppState.flashcardFormMode.get() == AppState.FormMode.EDIT) {
            int idx = AppState.editingIndex.get();

            if (idx >= 0 && idx < AppState.currentDetailList.size()) {
                Flashcard card = AppState.currentDetailList.get(idx);

                card.setTerm(term);
                card.setDefinition(def);

                flashcardService.update(card);
            }

        } else {
            // ADD MODE
            Flashcard newCard = new Flashcard(term, def, set, user);
            flashcardService.save(newCard);

            AppState.currentDetailList.add(newCard);
        }

        // After save -> back to list
        //AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
        Navigator.go(AppState.Screen.FLASHCARDS);
    }

    @FXML
    private void cancel() {
        AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
        Navigator.go(AppState.Screen.FLASHCARDS);
    }
}
