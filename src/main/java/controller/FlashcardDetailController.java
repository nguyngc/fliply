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
    private int index = 0;

    @FXML
    private void initialize() {
        String term = AppState.selectedTerm.get();
        String def  = AppState.selectedDefinition.get();

        if (term != null && !term.isBlank() && def != null && !def.isBlank()) {
            cards.clear();
            cards.add(new Flashcard(term, def));
            index = 0;
        }

        // 1) Configure header
        if (headerController != null) {
            headerController.setBackVisible(true);
            headerController.setTitle("Flashcard Set's Subject");
            headerController.setSubtitle("Total: 20 flashcards");

            headerController.setOnBack(this::goBack);
            headerController.applyVariant(HeaderController.Variant.STUDENT); // or TEACHER
        }

        // 2) Demo cards (replace with real list)
        seedDemoCards();

        // Update header with real number
        if (headerController != null) {
            headerController.setSubtitle("Total: " + cards.size() + " flashcards");
        }

        // 3) Render first card
        render();
        updateNavButtons();
    }

    private void seedDemoCards() {
        cards.clear();
        cards.add(new Flashcard("Term", "Definition"));
        cards.add(new Flashcard("CPU", "Central Processing Unit"));
        cards.add(new Flashcard("RAM", "Random Access Memory"));
        cards.add(new Flashcard("HTTP", "HyperText Transfer Protocol"));
        cards.add(new Flashcard("OOP", "Object-Oriented Programming"));
    }

    private void render() {
        int total = cards.size();
        int current = total == 0 ? 0 : (index + 1);

        // Update pager label (center under card)
        pageLabel.setText(current + " / " + total);

        // Update flip card content
        if (flipCardController != null) {
            if (total == 0) {
                flipCardController.setTerm("No cards");
                flipCardController.setDefinition("This set has no flashcards yet.");
                flipCardController.showTerm();
                return;
            }

            Flashcard c = cards.get(index);
            flipCardController.setTerm(c.term);
            flipCardController.setDefinition(c.definition);

            // Always reset to Term side when moving prev/next
            flipCardController.showTerm();
        }
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

    // Header back action (wired via headerController.setOnBack)
    private void goBack() {
        Navigator.go(AppState.Screen.FLASHCARD_SET);
        System.out.println("Back pressed (navigate to previous page)");
    }

    // -------- TODO: Demo data model --------
    private record Flashcard(String term, String definition) {
    }
}
