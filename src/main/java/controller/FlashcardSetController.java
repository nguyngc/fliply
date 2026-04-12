package controller;

import controller.components.HeaderController;
import controller.components.TermTileLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import model.AppState;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.service.FlashcardSetService;
import util.LocalizationService;
import view.Navigator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for displaying all flashcards in a selected flashcard set.
 * Shows flashcards as clickable tiles in a 2-column grid layout.
 * Users can navigate to view, study, or manage individual flashcards within the set.
 */
public class FlashcardSetController {

    // ========== Header Components ==========
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;
    
    // ========== Content Components ==========
    // Grid pane for displaying flashcard tiles
    @FXML
    private GridPane termGrid;

    // List of flashcards in the selected set
    private List<Flashcard> setCards;

    // Service for flashcard set database operations
    private final FlashcardSetService flashcardSetService = new FlashcardSetService();
    
    /**
     * Initializes the controller when the FXML is loaded.
     * Loads the selected flashcard set with all its cards from the database,
     * configures the header with set information, and renders the grid of flashcard tiles.
     */
    @FXML
    private void initialize() {
        // ========== Load Localized Strings ==========
        var localizedStrings = LocalizationService.getLocalizedStrings();
        
        // ========== Load Flashcard Set and Cards ==========
        // Get the flashcard set that was selected from the previous screen
        FlashcardSet set = flashcardSetService.getSetWithCards(AppState.selectedFlashcardSet.get().getFlashcardSetId());
        // Store the flashcards from the set in a local list
        setCards = new ArrayList<>(set.getCards());
        
        // ========== Configure Header ==========
        if (headerController != null) {
            // Set the header title to the flashcard set subject name
            headerController.setTitle(set.getSubject());
            
            // Set subtitle showing the number of flashcards in the set
            headerController.setSubtitle(MessageFormat.format(localizedStrings.get("flashcardSet.subtitle"), setCards.size()));
            
            // Show back button to return to class detail view
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASS_DETAIL));
        }

        // ========== Store Header Info for Detail View ==========
        // Store the set information for use in the flashcard detail view
        AppState.detailHeaderTitle.set(set.getSubject());
        AppState.detailHeaderSubtitle.set(MessageFormat.format(localizedStrings.get("flashcardSet.subtitle"), setCards.size()));

        // Render the grid of flashcard tiles
        renderGrid();
    }

    /**
     * Renders the grid of flashcard tiles from the selected flashcard set.
     * Creates a tile for each flashcard in the set and arranges them in a 2-column layout.
     */
    private void renderGrid() {
        // Clear any existing tiles from the grid
        termGrid.getChildren().clear();

        // Create a tile for each flashcard in the set
        for (int i = 0; i < setCards.size(); i++) {
            int index = i;
            // Get the current flashcard
            Flashcard item = setCards.get(i);

            // Create a tile with the flashcard's term
            Node tile = loadTile(item.getTerm(), () -> {
                // When tile is clicked, set up state for viewing the card detail
                AppState.currentDetailList.setAll(setCards);
                AppState.currentDetailIndex.set(index);

                // Mark that we're coming from a flashcard set (not from general flashcards)
                AppState.isFromFlashcardSet.set(true);
                // Set navigation to highlight the Classes menu
                AppState.navOverride.set(AppState.NavItem.CLASSES);

                // Navigate to the flashcard detail view
                Navigator.go(AppState.Screen.FLASHCARD_DETAIL);
            });

            // Calculate the grid position (2 columns layout)
            int col = i % 2;
            int row = i / 2;
            termGrid.add(tile, col, row);
        }
    }

    /**
     * Loads a flashcard tile from the FXML template.
     * Configures the tile with the given term text, state, and click handler.
     *
     * @param term The term/question text to display on the tile
     * @param read Whether the tile should display as read (true) or unread (false)
     * @param onSelected Callback to invoke when the tile is clicked
     * @return A Node containing the configured flashcard tile
     */
    private Node loadTile(String term, Runnable onSelected) {
        return TermTileLoader.load(getClass(), term, onSelected);
    }
}