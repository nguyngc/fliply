package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller for a term tile component.
 * Displays a term/question as a clickable tile with visual distinction between read and unread states.
 * Used in study or learning interfaces where users interact with individual terms.
 */
public class TermTileController {

    // The clickable button representing the term tile
    @FXML
    private Button tileButton;
    
    // Callback to be invoked when the tile is selected/clicked
    private Runnable onSelected;
    
    // Current state of the tile (UNREAD or READ)
    private State state = State.UNREAD;

    /**
     * Initializes the controller when the FXML is loaded.
     * Applies the initial styling based on the default state (UNREAD).
     */
    @FXML
    private void initialize() {
        applyState();
    }

    /**
     * Sets the text to be displayed on the tile button.
     *
     * @param text The text to display on the tile
     */
    public void setText(String text) {
        tileButton.setText(text);
    }

    /**
     * Gets the current state of the tile.
     *
     * @return The current State (READ or UNREAD)
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the state of the tile and updates its styling accordingly.
     * Unread tiles display with a blue background, read tiles with a white background and border.
     *
     * @param newState The new State to apply (READ or UNREAD)
     */
    public void setState(State newState) {
        this.state = newState;
        applyState();
    }

    /**
     * Sets the callback to be invoked when the tile is clicked.
     * The parent controller typically decides what action to take (e.g., navigate, mark as read).
     *
     * @param action The Runnable to execute on tile selection
     */
    public void setOnSelected(Runnable action) {
        this.onSelected = action;
    }

    /**
     * Handles the tile button click event.
     * Invokes the registered onSelected callback if one exists.
     * The actual action is determined by the parent controller.
     */
    @FXML
    private void onClick() {
        // Parent decides what happens (navigate, mark read, etc.)
        if (onSelected != null) onSelected.run();
    }

    /**
     * Applies CSS styling to the tile button based on its current state.
     * - UNREAD state: Blue background (#ACD7FF) with bold text
     * - READ state: White background with light blue border (#D9ECFF)
     */
    private void applyState() {
        if (tileButton == null) return;

        if (state == State.UNREAD) {
            // Style for unread state - blue background to draw attention
            tileButton.setStyle(
                    "-fx-background-color: #ACD7FF;" +
                            "-fx-background-radius: 16;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: 600;" +
                            "-fx-text-fill: #2C2C2C;"
            );
        } else {
            // Style for read state - white background with subtle border
            tileButton.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 16;" +
                            "-fx-border-color: #D9ECFF;" +
                            "-fx-border-radius: 16;" +
                            "-fx-border-width: 1;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: 600;" +
                            "-fx-text-fill: #2C2C2C;"
            );
        }
    }

    /**
     * Enum defining the possible states of a term tile.
     * READ: The term has been interacted with or viewed
     * UNREAD: The term is new or not yet interacted with
     */
    public enum State {READ, UNREAD}
}
