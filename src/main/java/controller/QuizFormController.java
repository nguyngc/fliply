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
import view.Navigator;
import java.util.MissingResourceException;
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
            headerController.setTitle(getMessage("quizForm.header", "New Quiz"));
            
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
        // ========== Parse Question Count ==========
        // Default to 10 questions if parsing fails
        int n = 10;
        try {
            // Parse the number of questions from the input field
            n = Integer.parseInt(countField.getText().trim());
        } catch (Exception ignored) {
            // Use default if parsing fails
        }
        
        // ========== Get Current User ==========
        User user = AppState.currentUser.get();
        if (user == null) return;

        // ========== Generate Quiz ==========
        // Build a quiz with n questions from user's flashcards
        Quiz quiz = quizService.generateQuiz(user, n);
        
        if (quiz == null) {
            // Show error if no flashcards available or invalid number
            Alert a = new Alert(Alert.AlertType.WARNING,
                    getMessage("quizForm.noFlashcards", "No flashcards available (or invalid number)."));
            a.setHeaderText(null);
            a.showAndWait();
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

    /**
     * Handles the cancel button click event.
     * Navigates back to the quizzes list without generating a quiz.
     */
    @FXML
    private void cancel() {
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
