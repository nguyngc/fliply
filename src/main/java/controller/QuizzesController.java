package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.Quiz;
import model.entity.User;
import model.service.QuizService;
import view.Navigator;

import java.io.IOException;
import java.util.List;

public class QuizzesController {
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;
    @FXML
    private VBox listBox;
    @FXML
    private Label totalLabel;

    private final QuizService quizService = new QuizService();

    @FXML
    private void initialize() {
//        // Seed demo
//        if (AppState.myQuizzes.isEmpty()) {
//            AppState.myQuizzes.add(DummyQuizFactory.quiz1());
//            AppState.myQuizzes.add(DummyQuizFactory.quiz2());
//        }
        User user = AppState.currentUser.get();
        if (user == null) return;
        // Load quizzes from DB
        List<Quiz> quizzes = quizService.getQuizzesByUser(user.getUserId());
        AppState.quizList.setAll(quizzes);

        if (headerController != null) {
            headerController.setTitle("My Quizzes");
            headerController.setSubtitle("Total: " + quizzes.size());
        }

        render();

        // Keep menu highlight
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
    }

    private void render() {
        listBox.getChildren().clear();

        for (Quiz quiz : AppState.myQuizzes) {
            Node card = loadQuizCard(quiz);
            listBox.getChildren().add(card);
        }

        listBox.getChildren().add(buildAddTile());
    }

    private Node loadQuizCard(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/quiz_card.fxml"));
            Node node = loader.load();

            node.setOnMouseClicked(e -> {
                // Set selected quiz
                AppState.selectedQuiz.set(quiz);

                // RESET QUIZ SESSION STATE
                AppState.quizQuestionIndex.set(0);
                AppState.quizPoints.set(0);
                AppState.quizAnswers.clear();
                AppState.quizCorrectMap.clear();

                // Keep menu highlight
                AppState.navOverride.set(AppState.NavItem.QUIZZES);

                // Navigate
                Navigator.go(AppState.Screen.QUIZ_DETAIL);
            });

            return node;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load quiz_card.fxml", ex);
        }
    }

    private Node buildAddTile() {
        StackPane box = new StackPane();
        box.setPrefHeight(90);
        box.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 18;
                -fx-border-radius: 18;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 18, 0.2, 0, 8);
                -fx-cursor: hand;
                """);

        Label plus = new Label("+");
        plus.setStyle("-fx-font-size: 40px; -fx-font-weight: 900; -fx-text-fill: #2C2C2C;");
        box.getChildren().add(plus);

        box.setOnMouseClicked(e -> {
            AppState.navOverride.set(AppState.NavItem.QUIZZES);
            Navigator.go(AppState.Screen.QUIZ_FORM);
        });

        return box;
    }
}
