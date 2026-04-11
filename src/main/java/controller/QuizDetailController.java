package controller;

import controller.components.HeaderController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import model.AppState;
import model.entity.Quiz;
import model.service.QuizService;
import view.Navigator;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Controller for the quiz detail screen.
 * Displays one question at a time with multiple choice options.
 * Tracks user answers, scores, and provides navigation between questions.
 * Shows results when the quiz is completed.
 */
public class QuizDetailController {

    // ========== Header Components ==========
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    // ========== Question Display ==========
    // Label for displaying the current question/prompt
    @FXML
    private Label termLabel;
    
    // Label for displaying current page number (e.g., "1 / 10")
    @FXML
    private Label pageLabel;

    // ========== Answer Option Buttons ==========
    // Button for option 1
    @FXML
    private Button opt1;
    
    // Button for option 2
    @FXML
    private Button opt2;
    
    // Button for option 3
    @FXML
    private Button opt3;
    
    // Button for option 4
    @FXML
    private Button opt4;

    // ========== Navigation ==========
    // Button to go to previous question
    @FXML
    private Button prevBtn;
    
    // Button to go to next question
    @FXML
    private Button nextBtn;
    
    // Container for navigation buttons
    @FXML
    private HBox navigationBox;

    // ========== Quiz Results ==========
    // Button to view the quiz results (shown only on last question)
    @FXML
    private Button viewResultBtn;

    // ========== Resources and Services ==========
    @FXML
    private ResourceBundle resources;

    // The current quiz being taken
    private Quiz quiz;
    
    // List of quiz questions with options and answers
    private List<QuizService.QuizQuestion> questions;
    
    // Service for quiz operations
    private final QuizService quizService = new QuizService();

    /**
     * Initializes the controller when the FXML is loaded.
     * Loads the quiz and its questions, sets up the header, and renders the first question.
     */
    @FXML
    private void initialize() {
        // ========== Get Selected Quiz ==========
        // Retrieve the quiz selected from the previous screen
        quiz = AppState.selectedQuiz.get();
        if (quiz == null) {
            // Navigate back to quizzes list if no quiz is selected
            Navigator.go(AppState.Screen.QUIZZES);
            return;
        }
        
        // ========== Build Quiz Questions ==========
        // Generate the quiz questions with shuffled options
        questions = quizService.buildQuizQuestions(quiz.getQuizId(), AppState.currentUser.get().getUserId());

        // ========== Configure Header ==========
        if (headerController != null) {
            // Set header title with quiz ID
            String titleTemplate = getMessage("quizDetail.header", "Quiz #{0}");
            headerController.setTitle(MessageFormat.format(titleTemplate, quiz.getQuizId()));
            
            // Show back button to return to quizzes list
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.QUIZZES));
        }

        // ========== Setup Navigation ==========
        // Ensure navigation buttons use left-to-right orientation
        if (navigationBox != null) {
            navigationBox.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }

        // Set the active navigation item to QUIZZES
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        
        // Render the first question
        render();
    }

    /**
     * Renders the current question and its options on the screen.
     * Handles button states, displays previously selected answers if applicable,
     * and manages navigation button visibility.
     */
    private void render() {
        // ========== Get Current Question Index ==========
        int idx = AppState.quizQuestionIndex.get();
        int total = questions.size();

        // Clamp index to valid range
        idx = clamp(idx, 0, total - 1);
        AppState.quizQuestionIndex.set(idx);

        // ========== Get Question Data ==========
        QuizService.QuizQuestion q = questions.get(idx);

        // ========== Display Question and Options ==========
        // Set the question prompt
        termLabel.setText(q.getPrompt());
        
        // Get all answer options
        List<String> opts = q.getOptions();

        // Display the four options on buttons
        opt1.setText(opts.get(0));
        opt2.setText(opts.get(1));
        opt3.setText(opts.get(2));
        opt4.setText(opts.get(3));

        // Enable text wrapping for long option texts
        opt1.setWrapText(true);
        opt2.setWrapText(true);
        opt3.setWrapText(true);
        opt4.setWrapText(true);

        // ========== Update Page Counter ==========
        pageLabel.setText((idx + 1) + " / " + total);

        // ========== Update Header Subtitle ==========
        // Shows current score
        updateSubtitle();

        // ========== Manage Navigation Buttons ==========
        // Disable Previous button if on first question
        prevBtn.setDisable(idx == 0);

        // Check if this is the last question
        boolean isLast = (idx == total - 1);

        // On last question, hide Next button and show View Result button
        if (isLast) {
            nextBtn.setVisible(false);
            nextBtn.setManaged(false);
        } else {
            nextBtn.setVisible(true);
            nextBtn.setManaged(true);
        }

        // ========== Restore Previous Answer If Exists ==========
        // Reset button styles to default
        resetOptionStyles();

        // If this question was already answered
        if (AppState.quizAnswers.containsKey(idx)) {
            // Get the user's previous answer and the correct answer
            String chosen = AppState.quizAnswers.get(idx);
            String correct = q.getCorrectAnswer();

            // Disable option buttons (question already answered)
            setOptionsDisabled(true);

            // Apply visual feedback for the selected answer
            if (chosen.equals(correct)) {
                // User was correct - highlight in blue
                getButtonByText(chosen).setStyle(correctStyle());
            } else {
                // User was wrong - highlight selected answer in red and correct in blue
                getButtonByText(chosen).setStyle(wrongStyle());
                getButtonByText(correct).setStyle(correctStyle());
            }
        } else {
            // Question not yet answered - enable option buttons
            setOptionsDisabled(false);
        }

        // ========== Show/Hide Results Button ==========
        // View Result button only shows on last question AND if it's already answered
        boolean canView = isLast && AppState.quizAnswers.containsKey(idx);
        viewResultBtn.setVisible(canView);
        viewResultBtn.setManaged(canView);
    }

    /**
     * Handles answer option button click events.
     * Records the selected answer, updates the score if correct, and applies visual feedback.
     * Disables option buttons after an answer is selected to prevent changing it.
     *
     * @param e The action event from the clicked button
     */
    @FXML
    private void chooseOption(ActionEvent e) {
        // Get the current question index
        int idx = AppState.quizQuestionIndex.get();
        
        // If already answered, prevent changing the answer
        if (AppState.quizAnswers.containsKey(idx)) return;

        // Get the clicked button
        Button clicked = (Button) e.getSource();
        
        // Get the current question data
        QuizService.QuizQuestion q = questions.get(idx);

        // Get the text of the selected option
        String chosen = clicked.getText();
        
        // Get the correct answer
        String correct = q.getCorrectAnswer();

        // ========== Record Answer ==========
        // Store the user's answer
        AppState.quizAnswers.put(idx, chosen);

        // ========== Check Correctness ==========
        boolean isCorrect = chosen.equals(correct);
        AppState.quizCorrectMap.put(idx, isCorrect);

        // ========== Update Score and Apply Feedback ==========
        if (isCorrect) {
            // Increment score for correct answer
            AppState.quizPoints.set(AppState.quizPoints.get() + 1);
            // Highlight correct answer in blue
            clicked.setStyle(correctStyle());
        } else {
            // Highlight selected answer in red
            clicked.setStyle(wrongStyle());
            // Highlight the correct answer in blue
            getButtonByText(correct).setStyle(correctStyle());
        }

        // Disable all option buttons to prevent changing the answer
        setOptionsDisabled(true);

        // Update the header subtitle to show new score
        updateSubtitle();

        // Re-render to update View Result button visibility if on last question
        render();
    }

    /**
     * Handles the Previous button click event.
     * Navigates to the previous question if available.
     */
    @FXML
    private void prev() {
        AppState.quizQuestionIndex.set(AppState.quizQuestionIndex.get() - 1);
        render();
    }

    /**
     * Handles the Next button click event.
     * Navigates to the next question if available.
     */
    @FXML
    private void next() {
        AppState.quizQuestionIndex.set(AppState.quizQuestionIndex.get() + 1);
        render();
    }

    /**
     * Handles the View Result button click event.
     * Navigates to the quiz results screen to display the final score and review answers.
     */
    @FXML
    private void viewResult() {
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        Navigator.go(AppState.Screen.QUIZ_RESULT);
    }

    /**
     * Enables or disables all answer option buttons.
     * Used to prevent option selection after an answer has been chosen.
     *
     * @param disabled True to disable buttons, false to enable them
     */
    private void setOptionsDisabled(boolean disabled) {
        opt1.setDisable(disabled);
        opt2.setDisable(disabled);
        opt3.setDisable(disabled);
        opt4.setDisable(disabled);
    }

    /**
     * Clamps a value to be within the specified range.
     * If max is less than min, returns min.
     *
     * @param v The value to clamp
     * @param min The minimum allowed value
     * @param max The maximum allowed value
     * @return The clamped value
     */
    private int clamp(int v, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, v));
    }

    /**
     * Finds and returns the button with the given text.
     * Used to apply styles to answer options by their text content.
     *
     * @param text The text to search for
     * @return The button with matching text, or opt1 if not found
     */
    private Button getButtonByText(String text) {
        for (Button b : Arrays.asList(opt1, opt2, opt3, opt4)) {
            if (b.getText().equals(text)) return b;
        }
        return opt1;
    }

    /**
     * Updates the header subtitle to show the current quiz score.
     * Displays "Total points: X" where X is the current score.
     */
    private void updateSubtitle() {
        if (headerController == null) {
            return;
        }
        String subtitleTemplate = getMessage("quizDetail.subtitle", "Total points: {0}");
        headerController.setSubtitle(MessageFormat.format(subtitleTemplate, AppState.quizPoints.get()));
    }

    /**
     * Resets all answer option buttons to their default (neutral) styling.
     * Used when rendering a new question or restoring initial state.
     */
    private void resetOptionStyles() {
        String base = "-fx-background-color: white; " +
                "-fx-background-radius: 16; " +
                "-fx-font-weight: 600;" +
                " -fx-border-color: D9F4F1;" +
                "-fx-border-radius: 16;";
        opt1.setStyle(base);
        opt2.setStyle(base);
        opt3.setStyle(base);
        opt4.setStyle(base);
    }

    /**
     * Returns the CSS style for highlighting a correct answer.
     * Uses a light blue background with a blue border.
     *
     * @return CSS style string for correct answers
     */
    private String correctStyle() {
        return "-fx-background-color: rgba(61,143,239,0.20); " +
                "-fx-border-color: #3D8FEF; " +
                "-fx-border-radius: 16; " +
                "-fx-background-radius: 16; " +
                "-fx-font-weight: 600;";
    }

    /**
     * Returns the CSS style for highlighting an incorrect answer.
     * Uses a light red background with a red border.
     *
     * @return CSS style string for wrong answers
     */
    private String wrongStyle() {
        return "-fx-background-color: rgba(255,0,0,0.08); " +
                "-fx-border-color: rgba(255,0,0,0.45); " +
                "-fx-border-radius: 16; " +
                "-fx-background-radius: 16; " +
                "-fx-font-weight: 600;";
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
