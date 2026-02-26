package controller;

import controller.components.HeaderController;
import controller.components.TermTileController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import model.AppState;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import view.Navigator;

import java.io.IOException;
import java.util.Collection;

public class FlashcardSetController {
    @SuppressWarnings("unused")
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;
    @FXML
    private GridPane termGrid;

    private Collection<Flashcard> setCards;

    @FXML
    private void initialize() {
        FlashcardSet set = AppState.selectedFlashcardSet.get();
        if (set == null) {
            Navigator.go(AppState.Screen.CLASS_DETAIL);
            return;
        }
        // Use the live collection from the selected set so edits/deletes map back correctly
        setCards = set.getCards();
        // Header
        if (headerController != null) {
            headerController.setTitle(set.getSubject());
            headerController.setSubtitle("Total: " + (setCards == null ? 0 : setCards.size()));
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASS_DETAIL));
        }

        // For detail header
        AppState.detailHeaderTitle.set(set.getSubject());
        AppState.detailHeaderSubtitle.set("Total: " + (setCards == null ? 0 : setCards.size()));

        renderGrid();
    }

    private void renderGrid() {
        termGrid.getChildren().clear();

        if (setCards == null || setCards.isEmpty()) return;

        int i = 0;
        for (Flashcard item : setCards) {
            int index = i;

            Node tile = loadTile(item.getTerm(), /*read*/ false, () -> {
                AppState.currentDetailList.setAll(setCards);
                AppState.currentDetailIndex.set(index);

                AppState.isFromFlashcardSet.set(true);
                AppState.navOverride.set(AppState.NavItem.CLASSES); // highlight Classes

                Navigator.go(AppState.Screen.FLASHCARD_DETAIL);
            });

            int col = i % 2;
            int row = i / 2;
            termGrid.add(tile, col, row);
            i++;
        }
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
}
