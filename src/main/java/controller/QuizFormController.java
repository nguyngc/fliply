package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import model.AppState;
import view.Navigator;

public class QuizFormController {

    @FXML private Parent header;
    @FXML private HeaderController headerController;
    @FXML private TextField countField;

    @FXML
    private void initialize() {
        if (headerController != null) {
            headerController.setTitle("New Quiz");
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.QUIZZES));
        }
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
    }

    @FXML
    private void generate() {
        int n = 10;
        try { n = Integer.parseInt(countField.getText().trim()); } catch (Exception ignored) {}

        // Build a dummy quiz with n questions
        AppState.QuizItem quiz = DummyQuizFactory.generatedQuiz(n);
        AppState.myQuizzes.add(quiz);

        AppState.selectedQuiz.set(quiz);
        AppState.quizQuestionIndex.set(0);
        AppState.quizPoints.set(0);
        AppState.quizAnswers.clear();
        AppState.quizCorrectMap.clear();

        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        Navigator.go(AppState.Screen.QUIZ_DETAIL);
    }

    @FXML
    private void cancel() {
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        Navigator.go(AppState.Screen.QUIZZES);
    }
}
