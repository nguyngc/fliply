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

public class QuizResultController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private VBox resultBox;
    @FXML
    private ResourceBundle resources;

    private Quiz quiz;
    private List<QuizService.QuizQuestion> questions;
    private final QuizService quizService = new QuizService();

    @FXML
    private void initialize() {
        quiz = AppState.selectedQuiz.get();
        if (quiz == null) {
            Navigator.go(AppState.Screen.QUIZZES);
            return;
        }

        if (headerController != null) {
            headerController.setTitle(getMessage("quizResult.header", "Result"));
            String subtitleTemplate = getMessage("quizResult.subtitle", "Total points: {0}");
            headerController.setSubtitle(MessageFormat.format(subtitleTemplate, AppState.quizPoints.get()));
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.QUIZZES));
        }

        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        questions = quizService.buildQuizQuestions(quiz.getQuizId(), AppState.currentUser.get().getUserId());
        renderResults();
    }

    private void renderResults() {
        resultBox.getChildren().clear();

        int total = questions.size();
        String correctLabel = getMessage("quizResult.correct", "Correct");
        String incorrectLabel = getMessage("quizResult.incorrect", "Incorrect");
        String notAnsweredLabel = getMessage("quizResult.notAnswered", "Not answered");

        for (int i = 0; i < total; i++) {
            QuizService.QuizQuestion q = questions.get(i);

            boolean answered = AppState.quizCorrectMap.containsKey(i);
            boolean correct = answered && Boolean.TRUE.equals(AppState.quizCorrectMap.get(i));

            HBox row = new HBox();
            row.setSpacing(10);
            row.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 14;
                    -fx-padding: 12 14;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 14, 0.2, 0, 6);
                    """);

            Label left = new Label((i + 1) + ". " + q.getPrompt());
            left.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #1F1F39;");
            left.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(left, javafx.scene.layout.Priority.ALWAYS);

            Label right = new Label(answered ? (correct ? correctLabel : incorrectLabel) : notAnsweredLabel);
            right.setStyle(correct
                    ? "-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #2E7D32;"
                    : "-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #C62828;"
            );

            row.getChildren().addAll(left, right);
            resultBox.getChildren().add(row);
        }
    }

    @FXML
    private void restart() {
        AppState.quizAnswers.clear();
        AppState.quizCorrectMap.clear();
        AppState.quizPoints.set(0);
        AppState.quizQuestionIndex.set(0);

        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        Navigator.go(AppState.Screen.QUIZ_DETAIL);
    }

    @FXML
    private void backToList() {
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        Navigator.go(AppState.Screen.QUIZZES);
    }

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
