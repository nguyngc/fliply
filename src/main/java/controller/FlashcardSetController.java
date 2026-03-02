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
import model.service.FlashcardSetService;
import view.Navigator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlashcardSetController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;
    @FXML
    private GridPane termGrid;

    private List<Flashcard> setCards;

    private final FlashcardSetService flashcardSetService = new FlashcardSetService();

    @FXML
    private void initialize() {
        FlashcardSet set = flashcardSetService.getSetWithCards( AppState.selectedFlashcardSet.get().getFlashcardSetId() );
        setCards = new ArrayList<>(set.getCards());
        // Header
        if (headerController != null) {
            headerController.setTitle(set.getSubject());
            headerController.setSubtitle("Total: " + setCards.size());
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASS_DETAIL));
        }

        // For detail header
        AppState.detailHeaderTitle.set(set.getSubject());
        AppState.detailHeaderSubtitle.set("Total: " + setCards.size());

        renderGrid();
    }

    private void renderGrid() {
        termGrid.getChildren().clear();

        for (int i = 0; i < setCards.size(); i++) {
            int index = i;
            Flashcard item = setCards.get(i);

            Node tile = loadTile(item.getTerm(), false, () -> {
                AppState.currentDetailList.setAll(setCards);
                AppState.currentDetailIndex.set(index);

                AppState.isFromFlashcardSet.set(true);
                AppState.navOverride.set(AppState.NavItem.CLASSES); // highlight Classes

                Navigator.go(AppState.Screen.FLASHCARD_DETAIL);
            });

            int col = i % 2;
            int row = i / 2;
            termGrid.add(tile, col, row);
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