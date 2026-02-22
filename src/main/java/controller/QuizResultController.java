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

import java.util.List;

public class QuizResultController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private VBox resultBox;

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
            headerController.setTitle("Result");
            headerController.setSubtitle("Total points: " + AppState.quizPoints.get());
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.QUIZZES));
        }

        AppState.navOverride.set(AppState.NavItem.QUIZZES);

        renderResults();
    }

    private void renderResults() {
        resultBox.getChildren().clear();

        int total = questions.size();

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

            Label right = new Label(answered ? (correct ? "Correct" : "Incorrect") : "Not answered");
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
}
