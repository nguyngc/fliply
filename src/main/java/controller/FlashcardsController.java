package controller;

import controller.components.HeaderController;
import controller.components.TermTileController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import model.AppState;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import view.Navigator;

import java.io.IOException;

public class FlashcardsController {
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;
    @FXML
    private GridPane termGrid;

    @FXML
    private void initialize() {
        FlashcardSet set = AppState.selectedSet.get();
        if (set == null) {
            Navigator.go(AppState.Screen.HOME);
            return;
        }

        headerController.setTitle(set.getSubject());
        headerController.setSubtitle("Total: " + set.getCards().size());

        renderGrid(set);
    }

    private void renderGrid(FlashcardSet set) {
        termGrid.getChildren().clear();

        int idx = 0;
        for (Flashcard card : set.getCards()) {

            Node tile = loadTile(card.getTerm(), false, () -> {
                AppState.currentFlashcard.set(card);
                Navigator.go(AppState.Screen.FLASHCARD_DETAIL);
            });

            int col = idx % 2;
            int row = idx / 2;
            termGrid.add(tile, col, row);
            idx++;
        }

        // Add (+) tile
        Node addTile = buildAddTile();
        int col = idx % 2;
        int row = idx / 2;
        termGrid.add(addTile, col, row);
    }


    private Node loadTile(String term, boolean read, Runnable onSelected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/term_tile.fxml"));
            Node node = loader.load();
            TermTileController ctrl = loader.getController();

            ctrl.setText(term);
            ctrl.setState(read ? TermTileController.State.READ : TermTileController.State.UNREAD);

            ctrl.setOnSelected(() -> {
                ctrl.setState(TermTileController.State.READ);
                onSelected.run();
            });

            return node;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load term_tile.fxml", ex);
        }
    }

    private Node buildAddTile() {
        StackPane box = new StackPane();
        box.setPrefHeight(52);
        box.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 16;
                -fx-border-radius: 16;
                -fx-border-color: rgba(61,143,239,0.20);
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 18, 0.2, 0, 8);
                -fx-cursor: hand;
                """);

        Label plus = new Label("+");
        plus.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #2C2C2C;");
        box.getChildren().add(plus);

        box.setOnMouseClicked(e -> {
            AppState.flashcardFormMode.set(AppState.FormMode.ADD);
//            AppState.editingIndex.set(-1);
//
//            AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
            Navigator.go(AppState.Screen.FLASHCARD_FORM);
        });

        return box;
    }
}
