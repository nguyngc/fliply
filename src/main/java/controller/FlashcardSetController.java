package controller;

import controller.components.HeaderController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import model.AppState;
import view.Navigator;

public class FlashcardSetController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private void initialize() {
        if (headerController != null) {
            headerController.setTitle(AppState.selectedFlashcardSetName.get().isBlank() ? "Flashcards" : AppState.selectedFlashcardSetName.get());
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASS_DETAIL));
        }
    }

    @FXML
    private void openFlashcardDetail(ActionEvent event) {
        Button btn = (Button) event.getSource();
        Object data = btn.getUserData();

        String term = "Term";
        String definition = "Definition";

        if (data != null) {
            String[] parts = data.toString().split("\\|", 2);
            if (parts.length >= 2) {
                term = parts[0];
                definition = parts[1];
            }
        }

        AppState.selectedTerm.set(term);
        AppState.selectedDefinition.set(definition);

        Navigator.go(AppState.Screen.FLASHCARD_DETAIL);
    }
}
