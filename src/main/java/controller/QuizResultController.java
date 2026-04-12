package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.Quiz;
import model.service.QuizService;
import util.I18n;
import view.Navigator;

import java.util.List;
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
    private HeaderController headerController;

    // ========== Content Components ==========
    // Container for displaying quiz result rows
    @FXML
    private VBox resultBox;
    
    // ========== Resources ==========
    @FXML
    private ResourceBundle resources;

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
        Quiz quiz = AppState.selectedQuiz.get();
        if (quiz == null) {
            // Navigate back to quizzes list if no quiz is selected
            Navigator.go(AppState.Screen.QUIZZES);
            return;
        }

        // ========== Configure Header ==========
        if (headerController != null) {
            // Set the header title
            headerController.setTitle(I18n.message(resources, "quizResult.header", "Result"));
            
            // Set subtitle showing the total points earned
            headerController.setSubtitle(I18n.format(resources, "quizResult.subtitle", "Total points: {0}", AppState.quizPoints.get()));
            
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
        String correctLabel = I18n.message(resources, "quizResult.correct", "Correct");
        String incorrectLabel = I18n.message(resources, "quizResult.incorrect", "Incorrect");
        String notAnsweredLabel = I18n.message(resources, "quizResult.notAnswered", "Not answered");

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
            String rowStyle = """
                    -fx-background-color: white;
                    -fx-background-radius: 14;
                    -fx-padding: 12 14;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 14, 0.2, 0, 6);
                    """;
            row.setStyle(rowStyle);

            // ========== Left Side: Question Prompt ==========
            // Display the question number and text
            Label left = new Label((i + 1) + ". " + q.getPrompt());
            left.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #1F1F39;");
            left.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(left, javafx.scene.layout.Priority.ALWAYS);

            // ========== Right Side: Answer Status ==========
            // Display the result status (Correct, Incorrect, or Not answered)
            String statusText;
            if (!answered) {
                statusText = notAnsweredLabel;
            } else if (correct) {
                statusText = correctLabel;
            } else {
                statusText = incorrectLabel;
            }

            Label right = new Label(statusText);
            String statusStyle = "-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: ";
            right.setStyle(statusStyle + (correct ? "#2E7D32;" : "#C62828;"));

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
}
