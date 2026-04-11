package controller.components;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Controller for a flip card animation component.
 * Displays a flashcard with term on one side and definition on the other.
 * Supports smooth 3D flip animation between the two sides.
 */
public class FlashcardFlipCardController {

    // Root container for the flip animation
    @FXML
    private StackPane root;
    
    // Container for the term side of the card
    @FXML
    private StackPane termPane;
    
    // Container for the definition side of the card
    @FXML
    private StackPane definitionPane;

    // Label for displaying the term text
    @FXML
    private Label termLabel;
    
    // Label for displaying the definition text
    @FXML
    private Label definitionLabel;

    // Flag to track which side of the card is currently visible
    private boolean showingTerm = true;
    
    // Flag to prevent multiple flip animations from running simultaneously
    private boolean animating = false;

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the initial state to show the term side and configures 3D rotation properties.
     */
    @FXML
    private void initialize() {
        // Show the term side initially
        showTerm();
        
        // Set the rotation axis to the Y-axis for horizontal flip effect
        root.setRotationAxis(javafx.geometry.Point3D.ZERO.add(0, 1, 0));
        
        // Make the background transparent
        root.setStyle("-fx-background-color: transparent;");
        
        // Enable caching for better animation performance
        root.setCache(true);
    }

    /**
     * Performs a smooth 3D flip animation between the term and definition sides.
     * The animation consists of two halves:
     * - First half (0° to 90°): Rotates and hides the current side
     * - Second half (-90° to 0°): Reveals the other side
     * Prevents overlapping animations with an animating flag.
     */
    @FXML
    public void flip() {
        // Prevent multiple simultaneous animations
        if (animating) return;
        animating = true;

        // First half: rotate from 0 degrees to 90 degrees
        RotateTransition firstHalf = new RotateTransition(Duration.millis(180), root);
        firstHalf.setFromAngle(0);
        firstHalf.setToAngle(90);
        firstHalf.setInterpolator(Interpolator.EASE_IN);

        // When first half is complete, swap the visible content
        firstHalf.setOnFinished(e -> {
            // Toggle between term and definition at the midpoint of animation
            if (showingTerm) showDefinition();
            else showTerm();

            // Update the state flag
            showingTerm = !showingTerm;

            // Reset rotation angle to -90 degrees for smooth continuation
            root.setRotate(-90);

            // Second half: rotate from -90 degrees back to 0 degrees
            RotateTransition secondHalf = new RotateTransition(Duration.millis(180), root);
            secondHalf.setFromAngle(-90);
            secondHalf.setToAngle(0);
            secondHalf.setInterpolator(Interpolator.EASE_OUT);

            // Mark animation as complete when second half finishes
            secondHalf.setOnFinished(e2 -> animating = false);
            secondHalf.play();
        });

        // Start the first half animation
        firstHalf.play();

    }

    /**
     * Sets the term text to be displayed on the term side of the card.
     *
     * @param text The term text to display
     */
    public void setTerm(String text) {
        termLabel.setText(text);
    }

    /**
     * Sets the definition text to be displayed on the definition side of the card.
     *
     * @param text The definition text to display
     */
    public void setDefinition(String text) {
        definitionLabel.setText(text);
    }

    /**
     * Shows the term side of the card and hides the definition side.
     * Updates visibility and layout management of both panes.
     */
    public void showTerm() {
        // Make term pane visible and managed for layout
        termPane.setVisible(true);
        termPane.setManaged(true);

        // Hide definition pane and remove from layout
        definitionPane.setVisible(false);
        definitionPane.setManaged(false);
    }

    /**
     * Shows the definition side of the card and hides the term side.
     * Updates visibility and layout management of both panes.
     */
    public void showDefinition() {
        // Make definition pane visible and managed for layout
        definitionPane.setVisible(true);
        definitionPane.setManaged(true);

        // Hide term pane and remove from layout
        termPane.setVisible(false);
        termPane.setManaged(false);
    }
}

