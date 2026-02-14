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
        // Seed demo
        if (AppState.myFlashcards.isEmpty()) {
            AppState.myFlashcards.addAll(
                    new AppState.FlashcardItem("CPU", "Central Processing Unit"),
                    new AppState.FlashcardItem("RAM", "Random Access Memory"),
                    new AppState.FlashcardItem("HTTP", "HyperText Transfer Protocol"),
                    new AppState.FlashcardItem("OOP", "Object-Oriented Programming"),
                    new AppState.FlashcardItem("API", "Application Programming Interface"),
                    new AppState.FlashcardItem("SQL", "Structured Query Language"),
                    new AppState.FlashcardItem("JSON", "JavaScript Object Notation"),
                    new AppState.FlashcardItem("UI", "User Interface")
            );
        }

        if (headerController != null) {
            String title = "My Flashcards";
            String subtitle = "Total: " + AppState.myFlashcards.size();

            headerController.setTitle(title);
            headerController.setSubtitle(subtitle);
        }

        // Re-render when list changes (after edit/delete/save)
        AppState.myFlashcards.addListener((javafx.collections.ListChangeListener<AppState.FlashcardItem>) c -> {
            if (headerController != null) {
                headerController.setSubtitle("Total: " + AppState.myFlashcards.size());
            }
            renderGrid();
        });

        renderGrid();
    }

    private void renderGrid() {
        termGrid.getChildren().clear();

        int idx = 0;
        for (int i = 0; i < AppState.myFlashcards.size(); i++) {
            int index = i;
            AppState.FlashcardItem item = AppState.myFlashcards.get(i);

            Node tile = loadTile(item.getTerm(), /*read*/ false, () -> {
                AppState.currentDetailList.setAll(AppState.myFlashcards);
                AppState.currentDetailIndex.set(index);

                AppState.isFromFlashcardSet.set(false);
                AppState.navOverride.set(AppState.NavItem.FLASHCARDS); // highlight Flashcards

                // header for detail
                AppState.detailHeaderTitle.set("My Flashcards");
                AppState.detailHeaderSubtitle.set("Total: " + AppState.myFlashcards.size());

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
            AppState.editingIndex.set(-1);

            AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
            Navigator.go(AppState.Screen.FLASHCARD_FORM);
        });

        return box;
    }
}
