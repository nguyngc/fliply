package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 * Controller for a flashcard set card component.
 * Displays information about a flashcard set including subject name, card count, and learning progress.
 * Progress display can be toggled, useful for different user roles (teacher vs student).
 */
public class FlashcardSetCardController {

    // Label for displaying the flashcard set subject/name
    @FXML
    private Label subjectLabel;
    
    // Label for displaying the number of flashcards in the set
    @FXML
    private Label countLabel;
    
    // Label for displaying the progress percentage as text
    @FXML
    private Label progressTextLabel;
    
    // Progress bar showing visual representation of completion progress
    @FXML
    private ProgressBar progressBar;

    /**
     * Sets the subject name to be displayed on the card.
     *
     * @param subject The subject/name of the flashcard set
     */
    public void setSubject(String subject) {
        subjectLabel.setText(subject);
    }

    /**
     * Sets the number of flashcards in the set to be displayed.
     *
     * @param count The number of cards in this flashcard set
     */
    public void setCardCount(int count) {
        countLabel.setText(count + " cards");
    }

    /**
     * Updates the progress bar and progress text label.
     * Displays the progress as both a visual progress bar and percentage text.
     * Also makes the progress elements visible.
     *
     * @param value The progress value as a decimal (0.0 to 1.0)
     */
    public void setProgress(double value) {
        progressBar.setProgress(value);
        progressTextLabel.setText((int) Math.round(value * 100) + "% Completed");
        setShowProgress(true);
    }

    /**
     * Controls the visibility of progress-related UI elements.
     * Useful for hiding progress from teachers who may not need to see student progress on their own sets.
     *
     * @param show True to display progress elements, false to hide them
     */
    public void setShowProgress(boolean show) {
        if (progressTextLabel != null) {
            progressTextLabel.setVisible(show);
            progressTextLabel.setManaged(show);
        }
        if (progressBar != null) {
            progressBar.setVisible(show);
            progressBar.setManaged(show);
        }
    }
}
