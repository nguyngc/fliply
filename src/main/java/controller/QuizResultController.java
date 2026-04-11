package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.Quiz;
import model.service.QuizService;
import view.Navigator;

import java.text.MessageFormat;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Controller for the quiz result screen.
 * Displays a summary of quiz performance including scores and answer review.
 * Shows each question with the user's answer status (correct, incorrect, or not answered).
 * Provides options to retake the quiz or return to the quiz list.
 */
public class QuizResultController {

    // ========== Header Components ==========
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    // ========== Content Components ==========
    // Container for displaying quiz result rows
    @FXML
    private VBox resultBox;
    
    // ========== Resources ==========
    @FXML
    private ResourceBundle resources;

    // The quiz that was taken
    private Quiz quiz;
    
    // List of quiz questions
    private List<QuizService.QuizQuestion> questions;
    
    // Service for quiz operations
    private final QuizService quizService = new QuizService();

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the header with quiz results and renders the answer review.
     */
    @FXML
    private void initialize() {
        // ========== Get Selected Quiz ==========
        // Retrieve the quiz that was just completed
        quiz = AppState.selectedQuiz.get();
        if (quiz == null) {
            // Navigate back to quizzes list if no quiz is selected
            Navigator.go(AppState.Screen.QUIZZES);
            return;
        }

        // ========== Configure Header ==========
        if (headerController != null) {
            // Set the header title
            headerController.setTitle(getMessage("quizResult.header", "Result"));
            
            // Set subtitle showing the total points earned
            String subtitleTemplate = getMessage("quizResult.subtitle", "Total points: {0}");
            headerController.setSubtitle(MessageFormat.format(subtitleTemplate, AppState.quizPoints.get()));
            
            // Show back button to return to quizzes list
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.QUIZZES));
        }

        // Set the active navigation item to QUIZZES
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        
        // ========== Load Quiz Questions ==========
        // Build the quiz questions for review
        questions = quizService.buildQuizQuestions(quiz.getQuizId(), AppState.currentUser.get().getUserId());
        
        // Render the results
        renderResults();
    }

    /**
     * Renders the quiz results with a row for each question showing answer status.
     * Displays the question prompt and whether it was answered correctly, incorrectly, or not answered.
     * Uses color coding: green for correct, red for incorrect/not answered.
     */
    private void renderResults() {
        // Clear any previously rendered results
        resultBox.getChildren().clear();

        // Get total number of questions
        int total = questions.size();
        
        // Get localized labels for answer statuses
        String correctLabel = getMessage("quizResult.correct", "Correct");
        String incorrectLabel = getMessage("quizResult.incorrect", "Incorrect");
        String notAnsweredLabel = getMessage("quizResult.notAnswered", "Not answered");

        // ========== Render Each Question ==========
        // Create a row for each question showing the result
        for (int i = 0; i < total; i++) {
            // Get the question
            QuizService.QuizQuestion q = questions.get(i);

            // ========== Determine Answer Status ==========
            // Check if the question was answered
            boolean answered = AppState.quizCorrectMap.containsKey(i);
            // Check if the answer was correct
            boolean correct = answered && Boolean.TRUE.equals(AppState.quizCorrectMap.get(i));

            // ========== Create Result Row ==========
            // Create a horizontal box to display the question and result
            HBox row = new HBox();
            row.setSpacing(10);
            row.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 14;
                    -fx-padding: 12 14;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 14, 0.2, 0, 6);
                    """);

            // ========== Left Side: Question Prompt ==========
            // Display the question number and text
            Label left = new Label((i + 1) + ". " + q.getPrompt());
            left.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #1F1F39;");
            left.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(left, javafx.scene.layout.Priority.ALWAYS);

            // ========== Right Side: Answer Status ==========
            // Display the result status (Correct, Incorrect, or Not answered)
            Label right = new Label(answered ? (correct ? correctLabel : incorrectLabel) : notAnsweredLabel);
            right.setStyle(correct
                    ? "-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #2E7D32;"  // Green for correct
                    : "-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #C62828;"  // Red for incorrect/unanswered
            );

            // Add both labels to the row
            row.getChildren().addAll(left, right);
            
            // Add the row to the results box
            resultBox.getChildren().add(row);
        }
    }

    /**
     * Handles the restart button click event.
     * Clears the quiz state and navigates back to the quiz detail screen to retake the quiz.
     */
    @FXML
    private void restart() {
        // ========== Clear Quiz State ==========
        // Clear all user answers from previous attempt
        AppState.quizAnswers.clear();
        
        // Clear the correctness map
        AppState.quizCorrectMap.clear();
        
        // Reset the score to 0
        AppState.quizPoints.set(0);
        
        // Reset the question index to start from the beginning
        AppState.quizQuestionIndex.set(0);

        // ========== Navigate to Quiz ==========
        // Set navigation item to QUIZZES
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        
        // Navigate back to quiz detail screen to retake the quiz
        Navigator.go(AppState.Screen.QUIZ_DETAIL);
    }

    /**
     * Handles the back to list button click event.
     * Navigates back to the quizzes list without clearing quiz state.
     */
    @FXML
    private void backToList() {
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        Navigator.go(AppState.Screen.QUIZZES);
    }

    /**
     * Retrieves a localized message from the resource bundle.
     * Provides a fallback message if the key is not found.
     *
     * @param key The resource bundle key
     * @param fallback The fallback message if key is not found
     * @return The localized message or the fallback
     */
    private String getMessage(String key, String fallback) {
        if (resources == null) {
            return fallback;
        }
        try {
            return resources.getString(key);
        } catch (MissingResourceException ignored) {
            return fallback;
        }
    }
}
