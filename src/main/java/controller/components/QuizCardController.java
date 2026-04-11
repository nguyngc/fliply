package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import model.entity.Quiz;

/**
 * Controller for a quiz card component.
 * Displays quiz information including title, question count, and optionally progress.
 * Progress elements are hidden by default for quiz cards.
 */
public class QuizCardController {

    // Label for displaying the quiz title/ID
    @FXML private Label quizTitleLabel;
    
    // Label for displaying the total number of questions in the quiz
    @FXML private Label questionCountLabel;
    
    // Label for displaying the progress percentage as text
    @FXML private Label progressTextLabel;
    
    // Progress bar showing visual representation of completion progress
    @FXML private ProgressBar progressBar;

    /**
     * Populates the card with quiz information from a Quiz entity.
     * Sets the quiz title and question count.
     * Hides the progress elements (progress text and progress bar).
     *
     * @param quiz The Quiz object containing the information to display, or null
     */
    public void setQuiz(Quiz quiz) {
        // Return early if quiz is null
        if (quiz == null) return;

        // Set the quiz title/ID
        quizTitleLabel.setText("Quiz " + quiz.getQuizId());

        // Get the total number of questions, default to 0 if null
        int total = (quiz.getNoOfQuestions() == null) ? 0 : quiz.getNoOfQuestions();
        questionCountLabel.setText(total + " questions");

        // Hide the progress text label and remove it from layout
        if (progressTextLabel != null) {
            progressTextLabel.setVisible(false);
            progressTextLabel.setManaged(false);
        }
        
        // Hide the progress bar and remove it from layout
        if (progressBar != null) {
            progressBar.setVisible(false);
            progressBar.setManaged(false);
        }
    }
}