package controller;

import controller.components.FlashcardFlipCardController;
import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import model.AppState;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.service.FlashcardService;
import util.LocalizationService;
import view.Navigator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller for displaying flashcard details with flip card animation.
 * Allows users to view, navigate through, edit, and delete flashcards.
 * Supports two contexts: viewing from a flashcard set or from the general flashcards list.
 */
public class FlashcardDetailController {

    // List of flashcards to display
    private final List<Flashcard> cards = new ArrayList<>();
    // Current index in the flashcard list
    private int index = 0;

    // Service for flashcard database operations
    private final FlashcardService flashcardService = new FlashcardService();

    // ========== Header Components ==========
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    // ========== Flip Card Components ==========
    @FXML
    private Parent flipCard;
    @FXML
    private FlashcardFlipCardController flipCardController;

    // ========== Navigation Components ==========
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label pageLabel;
    @FXML
    private HBox navigationBox;

    // Map for localized strings from resource bundle
    private Map<String, String> localizedStrings;

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the flashcard list, header configuration, and navigation based on context.
     * Supports two navigation contexts:
     * - From FlashcardSet: Back button returns to set, no edit/delete
     * - From Flashcards screen: Back button returns to list, edit/delete enabled
     */
    @FXML
    private void initialize() {
        // Load localized strings for the current locale
        localizedStrings = loadLocalizedStrings();
        
        // Determine if we're viewing from a flashcard set or general flashcards
        boolean isFromFlashcardSet = AppState.isFromFlashcardSet.get();

        // Ensure navigation box uses left-to-right orientation
        if (navigationBox != null) {
            navigationBox.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }

        // ========== Load Flashcards ==========
        // Load the flashcard list from app state
        cards.clear();
        if (!AppState.currentDetailList.isEmpty()) {
            cards.addAll(AppState.currentDetailList);
            // Restore the current index, clamped to valid range
            int maxIndex = cards.size() - 1;
            index = maxIndex < 0 ? 0 : Math.clamp(AppState.currentDetailIndex.get(), 0, maxIndex);
        }

        // ========== Configure Header ==========
        if (headerController != null) {
            // Show back button
            headerController.setBackVisible(true);
            headerController.setTitle(AppState.detailHeaderTitle.get());
            headerController.setSubtitle(MessageFormat.format(localizedStrings.get("flashcardSet.subtitle"), cards.size()));

            // Configure based on navigation context
            if (isFromFlashcardSet) {
                // Viewing from a flashcard set - limited actions
                headerController.setOnBack(() -> navigateTo(AppState.Screen.FLASHCARD_SET));
                headerController.setActionsVisible(false);
            } else {
                // Viewing from flashcards screen - full actions enabled
                headerController.setOnBack(() -> navigateTo(AppState.Screen.FLASHCARDS));
                headerController.setActionsVisible(true);

                // ========== Configure Edit Action ==========
                headerController.setOnEdit(() -> {
                    if (cards.isEmpty()) return;

                    // Get the current card index
                    int idx = index;
                    AppState.editingIndex.set(idx);
                    AppState.flashcardFormMode.set(AppState.FormMode.EDIT);

                    // Load the current card's data into app state for editing
                    Flashcard c = cards.get(idx);
                    AppState.selectedTerm.set(c.getTerm());
                    AppState.selectedDefinition.set(c.getDefinition());

                    // Navigate to flashcard form
                    AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
                    navigateTo(AppState.Screen.FLASHCARD_FORM);
                });

                // ========== Configure Delete Action ==========
                headerController.setOnDelete(() -> {
                    if (cards.isEmpty()) return;

                    int idx = index;
                    if (idx < 0 || idx >= cards.size()) return;

                    // Get the flashcard to delete
                    Flashcard toRemove = cards.get(idx);
                    Integer toRemoveId = toRemove.getFlashcardId();

                    // Delete from database first
                    try {
                        deleteFlashcard(toRemove);
                    } catch (Exception ex) {
                        // Show error dialog if delete fails
                        showDeleteError();
                        return;
                    }

                    // Remove the card from app state, matching by ID when available and
                    // falling back to object identity for transient in-memory cards.
                    AppState.currentDetailList.removeIf(c -> matchesCard(c, toRemove, toRemoveId));

                    // Remove from selected flashcard set if applicable
                    FlashcardSet selectedSet = AppState.selectedFlashcardSet.get();
                    if (selectedSet != null) {
                        selectedSet.getCards().removeIf(c -> matchesCard(c, toRemove, toRemoveId));
                    }

                    // Remove from user's general flashcard list
                    AppState.myFlashcards.removeIf(c -> matchesCard(c, toRemove, toRemoveId));

                    // Refresh local list with updated state
                    cards.clear();
                    cards.addAll(AppState.currentDetailList);

                    showDeleteSuccess();

                    // If no cards remain, return to the flashcards list.
                    if (cards.isEmpty()) {
                        AppState.navOverride.set(AppState.NavItem.FLASHCARDS);
                        navigateTo(AppState.Screen.FLASHCARDS);
                        return;
                    }

                    // Clamp the current index so the next available card stays visible.
                    int maxIndex = cards.size() - 1;
                    index = maxIndex < 0 ? 0 : Math.clamp(idx, 0, maxIndex);
                    AppState.currentDetailIndex.set(index);

                    render();
                    updateNavButtons();
                });
            }
        }

        // Render the initial flashcard display
        render();
        updateNavButtons();
    }

    /**
     * Renders the current flashcard to the UI.
     * Displays the page number and the term/definition of the current card.
     * Shows a placeholder message if no cards are available.
     */
    private void render() {
        // Calculate current page number
        int total = cards.size();
        int current = total == 0 ? 0 : (index + 1);
        pageLabel.setText(current + " / " + total);

        if (flipCardController == null) return;

        // Display placeholder if no cards exist
        if (total == 0) {
            flipCardController.setTerm("No cards");
            flipCardController.setDefinition("This set has no flashcards yet.");
            flipCardController.showTerm();
            return;
        }

        // Display the current flashcard
        Flashcard c = cards.get(index);
        flipCardController.setTerm(c.getTerm());
        flipCardController.setDefinition(c.getDefinition());
        flipCardController.showTerm();
    }

    /**
     * Updates the state of the navigation buttons (Previous/Next).
     * Disables the Previous button if at the first card.
     * Disables the Next button if at the last card.
     * Disables both buttons if there are no cards.
     */
    private void updateNavButtons() {
        boolean hasCards = !cards.isEmpty();
        prevButton.setDisable(!hasCards || index == 0);
        nextButton.setDisable(!hasCards || index >= cards.size() - 1);
    }

    /**
     * Handles the Previous button click event.
     * Navigates to the previous flashcard if available.
     * Updates the UI and button states.
     */
    @FXML
    private void prev() {
        if (index > 0) {
            index--;
            AppState.currentDetailIndex.set(index);
            render();
            updateNavButtons();
        }
    }

    /**
     * Handles the Next button click event.
     * Navigates to the next flashcard if available.
     * Updates the UI and button states.
     */
    @FXML
    private void next() {
        if (index < cards.size() - 1) {
            index++;
            AppState.currentDetailIndex.set(index);
            render();
            updateNavButtons();
        }
    }

    // ========== Helper Methods ==========
    /** Loads localized strings from the resource bundle for the current locale.
     * Returns a map of key-value pairs for string lookup.
     */
    Map<String, String> loadLocalizedStrings() {
        return LocalizationService.getLocalizedStrings();
    }

    /** Navigates to the specified screen using the Navigator utility.
     * @param screen The target screen to navigate to.
     */
    void navigateTo(AppState.Screen screen) {
        Navigator.go(screen);
    }

    /** Deletes the specified flashcard from the database using the FlashcardService.
     * @param card The flashcard to delete.
     * @throws Exception if the delete operation fails.
     */
    void deleteFlashcard(Flashcard card) {
        flashcardService.delete(card);
    }

    /** Shows an error alert if the flashcard delete operation fails.
     * Displays a localized error message to the user.
     */
    void showDeleteError() {
        Alert a = new Alert(Alert.AlertType.ERROR,
                "Delete failed. Could not delete flashcard from database.");
        a.showAndWait();
    }

    /** Shows a success alert after a flashcard is successfully deleted.
     * Displays a localized success message to the user.
     */
    void showDeleteSuccess() {
        String successMessage = localizedStrings.getOrDefault(
                "flashcardDetail.delete.success",
                "Flashcard deleted successfully.");
        Alert success = new Alert(Alert.AlertType.INFORMATION, successMessage);
        success.setTitle(localizedStrings.getOrDefault("flashcardForm.alertTitle", "Flashcard"));
        success.setHeaderText(null);
        success.showAndWait();
    }

    private boolean matchesCard(Flashcard candidate, Flashcard target, Integer targetId) {
        if (candidate == null || target == null) {
            return false;
        }
        if (targetId != null) {
            return targetId.equals(candidate.getFlashcardId());
        }
        return candidate == target;
    }

}
