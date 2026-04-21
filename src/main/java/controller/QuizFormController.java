package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import model.AppState;
import model.entity.Quiz;
import model.entity.User;
import model.service.QuizService;
import util.I18n;
import view.Navigator;

import java.util.ResourceBundle;

/**
 * Controller for the quiz generation form.
 * Allows users to create a new quiz by specifying the number of questions.
 * Generates a quiz from the user's available flashcards.
 */
public class QuizFormController {

    // ========== Header Components ==========
    @FXML private Parent header;
    @FXML private HeaderController headerController;
    
    // ========== Form Input ==========
    // Text field for entering the number of questions for the quiz
    @FXML private TextField countField;
    
    // ========== Resources ==========
    @FXML private ResourceBundle resources;

    // Service for quiz operations
    private final QuizService quizService = new QuizService();

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the header with title and back button functionality.
     */
    @FXML
    private void initialize() {
        // ========== Configure Header ==========
        if (headerController != null) {
            // Set the header title
            headerController.setTitle(I18n.message(resources, "quizForm.header", "New Quiz"));
            
            // Show back button
            headerController.setBackVisible(true);
            
            // Set back button to navigate to quizzes list
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.QUIZZES));
        }
        
        // Set the active navigation item to QUIZZES
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
    }

    /**
     * Handles the generate button click event.
     * Parses the question count, generates a new quiz from available flashcards,
     * and navigates to the quiz detail screen.
     * Shows an error if no flashcards are available or input is invalid.
     */
    @FXML
    private void generate() {
        // ========== Get Current User ==========
        User user = AppState.currentUser.get();
        if (user == null) return;

        String validationError = validateQuestionCount(countField == null ? null : countField.getText(),
                quizService.getAvailableFlashcardCount(user));
        if (validationError != null) {
            showWarning(validationError);
            return;
        }

        int n = Integer.parseInt(countField.getText().trim());

        // ========== Generate Quiz ==========
        // Build a quiz with n questions from user's flashcards
        Quiz quiz = quizService.generateQuiz(user, n);
        
        if (quiz == null) {
            // Show error if no flashcards available
            showWarning(I18n.message(resources, "quizForm.error.noFlashcards", "There are no flashcards available."));
            return;
        }
        
        // ========== Initialize Quiz State ==========
        // Store the generated quiz in app state
        AppState.selectedQuiz.set(quiz);
        
        // Reset quiz progress
        AppState.quizQuestionIndex.set(0);
        AppState.quizPoints.set(0);
        
        // Clear any previous quiz answers
        AppState.quizAnswers.clear();
        AppState.quizCorrectMap.clear();

        // ========== Navigate to Quiz ==========
        // Navigate to the quiz detail screen to start taking the quiz
        Navigator.go(AppState.Screen.QUIZ_DETAIL);
    }

    private String validateQuestionCount(String rawCount, int availableCount) {
        if (rawCount == null || rawCount.trim().isEmpty()) {
            return I18n.message(resources, "quizForm.error.emptyInput", "Please enter the number of questions.");
        }

        final int requestedCount;
        try {
            requestedCount = Integer.parseInt(rawCount.trim());
        } catch (NumberFormatException ignored) {
            return I18n.message(resources, "quizForm.error.invalidNumber", "Please enter a valid positive number.");
        }

        if (requestedCount <= 0) {
            return I18n.message(resources, "quizForm.error.invalidNumber", "Please enter a valid positive number.");
        }

        if (availableCount <= 0) {
            return I18n.message(resources, "quizForm.error.noFlashcards", "There are no flashcards available.");
        }

        if (requestedCount > availableCount) {
            return I18n.format(resources, "quizForm.error.tooManyQuestions",
                    "Only {0} flashcards are available. Please enter a smaller number.",
                    availableCount);
        }

        return null;
    }

    private void showWarning(String message) {
        Alert a = new Alert(Alert.AlertType.WARNING, message);
        a.setTitle(I18n.message(resources, "quizForm.alertTitle", "Quiz"));
        a.setHeaderText(null);
        a.showAndWait();
    }

    /**
     * Handles the cancel button click event.
     * Navigates back to the quizzes list without generating a quiz.
     */
    @FXML
    private void cancel() {
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        Navigator.go(AppState.Screen.QUIZZES);
    }

}
