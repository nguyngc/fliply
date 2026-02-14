package controller;

import controller.components.FlashcardFlipCardController;
import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.AppState;
import view.Navigator;

import java.util.ArrayList;
import java.util.List;

public class FlashcardDetailController {

    private final List<Flashcard> cards = new ArrayList<>();
    private int index = 0;

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private Parent flipCard;
    @FXML
    private FlashcardFlipCardController flipCardController;

    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label pageLabel;

    @FXML
    private void initialize() {
        boolean isFromFlashcardSet = AppState.isFromFlashcardSet.get();

        // Build cards from passed list
        cards.clear();
        if (!AppState.currentDetailList.isEmpty()) {
            for (AppState.FlashcardItem it : AppState.currentDetailList) {
                cards.add(new Flashcard(it.getTerm(), it.getDefinition()));
            }
            index = clamp(AppState.currentDetailIndex.get(), 0, cards.size() - 1);
        } else {
            cards.add(new Flashcard("Term", "Definition"));
            index = 0;
        }

        // Header
        String title = AppState.detailHeaderTitle.get();
        String subtitle = AppState.detailHeaderSubtitle.get();

        if (headerController != null) {
            headerController.setBackVisible(true);

            if (title != null && !title.isBlank()) headerController.setTitle(title);

            headerController.setSubtitle("Total: " + cards.size());

            if (isFromFlashcardSet) {
                headerController.setOnBack(() -> Navigator.go(AppState.Screen.FLASHCARD_SET));
                headerController.setActionsVisible(false);
            } else {
                headerController.setOnBack(() -> Navigator.go(AppState.Screen.FLASHCARDS));
                headerController.setActionsVisible(true);

                headerController.setOnEdit(() -> {
                    // Edit the currently shown card in the master list
                    int idx = index; // index in cards list
                    AppState.editingIndex.set(idx);
                    AppState.flashcardFormMode.set(AppState.FormMode.EDIT);

                    // Pre-fill via selectedTerm/Definition (or read directly from list)
                    Flashcard c = cards.get(idx);
                    AppState.selectedTerm.set(c.term);
                    AppState.selectedDefinition.set(c.definition);

                    AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
                    Navigator.go(AppState.Screen.FLASHCARD_FORM);
                });

                headerController.setOnDelete(() -> {
                    if (cards.isEmpty()) return;

                    // remove from master list (and current detail list)
                    int idx = index;
                    if (idx >= 0 && idx < AppState.myFlashcards.size()) {
                        AppState.myFlashcards.remove(idx);
                    }

                    AppState.currentDetailList.setAll(AppState.myFlashcards);

                    // go back to list
                    AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
                    Navigator.go(AppState.Screen.FLASHCARDS);
                });
            }
        }

        render();
        updateNavButtons();
    }

    private void render() {
        int total = cards.size();
        int current = total == 0 ? 0 : (index + 1);
        pageLabel.setText(current + " / " + total);

        if (flipCardController == null) return;

        if (total == 0) {
            flipCardController.setTerm("No cards");
            flipCardController.setDefinition("This set has no flashcards yet.");
            flipCardController.showTerm();
            return;
        }

        Flashcard c = cards.get(index);
        flipCardController.setTerm(c.term);
        flipCardController.setDefinition(c.definition);
        flipCardController.showTerm();
    }

    private void updateNavButtons() {
        boolean hasCards = !cards.isEmpty();
        prevButton.setDisable(!hasCards || index == 0);
        nextButton.setDisable(!hasCards || index >= cards.size() - 1);
    }

    @FXML
    private void prev() {
        if (index > 0) {
            index--;
            render();
            updateNavButtons();
        }
    }

    @FXML
    private void next() {
        if (index < cards.size() - 1) {
            index++;
            render();
            updateNavButtons();
        }
    }

    private int clamp(int v, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, v));
    }

    private record Flashcard(String term, String definition) {
    }
}
