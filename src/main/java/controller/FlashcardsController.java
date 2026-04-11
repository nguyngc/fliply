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
import util.LocaleManager;
import util.LocalizationService;
import view.Navigator;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for displaying all flashcards belonging to the current user.
 * Displays flashcards as clickable tiles in a grid layout.
 * Allows users to view, create, and manage flashcards.
 */
public class FlashcardsController {
    // ========== Header Components ==========
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;
    
    // ========== Content Components ==========
    // Grid pane for displaying flashcard tiles
    @FXML
    private GridPane termGrid;

    // Service for flashcard database operations
    private final FlashcardService flashcardService = new FlashcardService();
    
    // Resource bundle for localized strings
    private final ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());


    /**
     * Initializes the controller when the FXML is loaded.
     * Loads all flashcards for the current user from the database,
     * configures the header, and renders the grid of flashcard tiles.
     */
    @FXML
    private void initialize() {
        // ========== Load User Flashcards ==========
        // Get the currently logged-in user
        User user = AppState.currentUser.get();
        
        // Load all flashcards belonging to the user from the database
        if (user != null) {
            List<Flashcard> cards = flashcardService.getFlashcardsByUser(user.getUserId());
            // Store flashcards in app state for access from other screens
            AppState.myFlashcards.setAll(cards);
        }

        // ========== Configure Header ==========
        if (headerController != null) {
            // Set the main title
            headerController.setTitle(rb.getString("flashcards.title"));
            
            // Set subtitle showing total number of flashcards
            headerController.setSubtitle(rb.getString("flashcards.total") + AppState.myFlashcards.size());
        }

        // Render the flashcard tiles in the grid
        renderGrid();
    }

    /**
     * Renders the grid of flashcard tiles.
     * Creates a tile for each flashcard in the user's list and adds an "Add" button.
     * Arranges tiles in a 2-column grid layout.
     */
    private void renderGrid() {
        // Clear any existing tiles from the grid
        termGrid.getChildren().clear();

        // Create a tile for each flashcard in the user's collection
        for (int i = 0; i < AppState.myFlashcards.size(); i++) {
            // Get the current flashcard
            Flashcard card = AppState.myFlashcards.get(i);
            int index = i;
            
            // Create a tile with the flashcard's term
            Node tile = loadTile(card.getTerm(), false, () -> {
                // When tile is clicked, set up state for viewing the card detail
                AppState.currentDetailList.setAll(AppState.myFlashcards);
                AppState.currentDetailIndex.set(index);

                // Mark that we're coming from the general flashcards screen (not from a set)
                AppState.isFromFlashcardSet.set(false);
                AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
                AppState.detailHeaderTitle.set(card.getTerm());

                // Navigate to the flashcard detail view
                Navigator.go(AppState.Screen.FLASHCARD_DETAIL);
            });

            // Calculate the grid position (2 columns layout)
            int col = i % 2;
            int row = i / 2;
            termGrid.add(tile, col, row);
        }
        
        // ========== Add the "+" Button Tile ==========
        // Create an "Add Flashcard" button tile
        Node addTile = buildAddTile();
        int addIndex = AppState.myFlashcards.size();
        int col = addIndex % 2;
        int row = addIndex / 2;
        termGrid.add(addTile, col, row);
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
    private Node loadTile(String term, boolean read, Runnable onSelected) {
        try {
            // Load the term tile FXML template
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/term_tile.fxml"));
            Node node = loader.load();
            TermTileController ctrl = loader.getController();

            // Set the term text
            ctrl.setText(term);
            
            // Set the initial state (read or unread)
            ctrl.setState(read ? TermTileController.State.READ : TermTileController.State.UNREAD);

            // Configure click handler
            ctrl.setOnSelected(() -> {
                // Mark the tile as read when clicked
                ctrl.setState(TermTileController.State.READ);
                // Invoke the callback to navigate or perform other actions
                onSelected.run();
            });

            return node;
        } catch (IOException ex) {
            throw new RuntimeException(rb.getString("flashcards.error"), ex);
        }
    }

    /**
     * Builds a clickable "Add Flashcard" tile with a plus (+) symbol.
     * The tile navigates to the flashcard form when clicked.
     *
     * @return A Node containing the "Add" tile with appropriate styling and click handler
     */
    private Node buildAddTile() {
        // Create a container for the add tile
        StackPane box = new StackPane();
        box.setPrefHeight(52);
        
        // Apply styling to match the flashcard tiles
        box.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 16;
                -fx-border-radius: 16;
                -fx-border-color: rgba(61,143,239,0.20);
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 18, 0.2, 0, 8);
                -fx-cursor: hand;
                """);

        // Create the plus icon label
        Label plus = new Label("+");
        plus.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #2C2C2C;");
        box.getChildren().add(plus);

        // Add click handler to navigate to flashcard creation form
        box.setOnMouseClicked(e -> {
            // Set the form mode to ADD
            AppState.flashcardFormMode.set(AppState.FormMode.ADD);
            // Navigate to the flashcard form
            Navigator.go(AppState.Screen.FLASHCARD_FORM);
        });

        return box;
    }
}