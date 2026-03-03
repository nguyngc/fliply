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
import model.entity.User;
import model.service.FlashcardService;
import view.Navigator;

import java.io.IOException;
import java.util.List;

public class FlashcardsController {
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;
    @FXML
    private GridPane termGrid;

    private final FlashcardService flashcardService = new FlashcardService();


    @FXML
    private void initialize() {
        // Load Flashcards
        User user = AppState.currentUser.get();
        if (user != null) {
            List<Flashcard> cards = flashcardService.getFlashcardsByUser(user.getUserId());
            AppState.myFlashcards.setAll(cards);
        }

        // Header
        if (headerController != null) {
            headerController.setTitle("My Flashcards");
            headerController.setSubtitle("Total: " + AppState.myFlashcards.size());
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.HOME));
        }

        renderGrid();
    }

    private void renderGrid() {
        termGrid.getChildren().clear();

        for (int i = 0; i < AppState.myFlashcards.size(); i++) {
            Flashcard card = AppState.myFlashcards.get(i);
            int index = i;
            Node tile = loadTile(card.getTerm(), false, () -> {
                AppState.currentDetailList.setAll(AppState.myFlashcards);
                AppState.currentDetailIndex.set(index);

                AppState.isFromFlashcardSet.set(false);
                AppState.navOverride.set(AppState.NavItem.FLASHCARDS);

                Navigator.go(AppState.Screen.FLASHCARD_DETAIL); });

            int col = i % 2;
            int row = i / 2;
            termGrid.add(tile, col, row);
        }
        // Add tile "+"
        Node addTile = buildAddTile();
        int addIndex = AppState.myFlashcards.size();
        int col = addIndex % 2;
        int row = addIndex / 2;
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