package controller;

import controller.components.FlashcardFlipCardController;
import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.AppState;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.service.FlashcardService;
import view.Navigator;

import java.util.ArrayList;
import java.util.List;

public class FlashcardDetailController {

    private final List<Flashcard> cards = new ArrayList<>();
    private int index = 0;

    private final FlashcardService flashcardService = new FlashcardService();

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
            cards.addAll(AppState.currentDetailList);
            index = clamp(AppState.currentDetailIndex.get(), 0, cards.size() - 1);
        }

        if (headerController != null) {
            headerController.setBackVisible(true);
            headerController.setTitle(AppState.detailHeaderTitle.get());
            headerController.setSubtitle("Total: " + cards.size());

            if (isFromFlashcardSet) {
                headerController.setOnBack(() -> Navigator.go(AppState.Screen.FLASHCARD_SET));
                headerController.setActionsVisible(false);
            } else {
                headerController.setOnBack(() -> Navigator.go(AppState.Screen.FLASHCARDS));
                headerController.setActionsVisible(true);

                headerController.setOnEdit(() -> {
                    if (cards.isEmpty()) return;

                    int idx = index;
                    AppState.editingIndex.set(idx);
                    AppState.flashcardFormMode.set(AppState.FormMode.EDIT);

                    Flashcard c = cards.get(idx);
                    AppState.selectedTerm.set(c.getTerm());
                    AppState.selectedDefinition.set(c.getDefinition());

                    AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
                    Navigator.go(AppState.Screen.FLASHCARD_FORM);
                });

                headerController.setOnDelete(() -> {
                    if (cards.isEmpty()) return;

                    int idx = index;
                    if (idx < 0 || idx >= cards.size()) return;

                    Flashcard toRemove = cards.get(idx);

                    // Delete in DB first
                    try {
                        flashcardService.delete(toRemove);
                    } catch (Exception ex) {
                        Alert a = new Alert(Alert.AlertType.ERROR,
                                "Delete failed. Could not delete flashcard from database.");
                        a.showAndWait();
                        return;
                    }

                    // Remove from state lists
                    if (idx >= 0 && idx < AppState.currentDetailList.size()) {
                        AppState.currentDetailList.remove(idx);
                    }

                    FlashcardSet selectedSet = AppState.selectedFlashcardSet.get();
                    if (selectedSet != null) {
                        selectedSet.getCards().remove(toRemove);
                    }

                    AppState.myFlashcards.remove(toRemove);

                    // Refresh local list + UI
                    cards.clear();
                    cards.addAll(AppState.currentDetailList);

                    if (cards.isEmpty()) {
                        AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
                        Navigator.go(AppState.Screen.FLASHCARDS);
                        return;
                    }

                    index = clamp(idx, 0, cards.size() - 1);
                    AppState.currentDetailIndex.set(index);

                    render();
                    updateNavButtons();
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
        flipCardController.setTerm(c.getTerm());
        flipCardController.setDefinition(c.getDefinition());
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
            AppState.currentDetailIndex.set(index);
            render();
            updateNavButtons();
        }
    }

    @FXML
    private void next() {
        if (index < cards.size() - 1) {
            index++;
            AppState.currentDetailIndex.set(index);
            render();
            updateNavButtons();
        }
    }

    private int clamp(int v, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, v));
    }
}