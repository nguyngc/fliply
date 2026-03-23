package controller;

import controller.components.HeaderController;
import controller.components.QuizCardController;
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
import java.text.MessageFormat;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class QuizzesController {

    @FXML private Parent header;
    @FXML private HeaderController headerController;
    @FXML private VBox listBox;
    @FXML private Label totalLabel;
    @FXML private ResourceBundle resources;

    private final QuizService quizService = new QuizService();

    @FXML
    private void initialize() {
        User user = AppState.currentUser.get();
        if (user == null) return;

        List<Quiz> quizzes = quizService.getQuizzesByUser(user.getUserId());
        AppState.quizList.setAll(quizzes);

        if (headerController != null) {
            headerController.setTitle(getMessage("quizzes.title", "My Quizzes"));
            setTotalSubtitle(quizzes.size());
        }
        if (totalLabel != null) {
            totalLabel.setText(formatTotal(quizzes.size()));
        }

        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        render();
    }

    private void render() {
        listBox.getChildren().clear();

        for (Quiz quiz : AppState.quizList) {
            listBox.getChildren().add(loadQuizCard(quiz));
        }

        listBox.getChildren().add(buildAddTile());
        if (headerController != null) {
            setTotalSubtitle(AppState.quizList.size());
        }
        if (totalLabel != null) {
            totalLabel.setText(formatTotal(AppState.quizList.size()));
        }
    }

    private Node loadQuizCard(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/quiz_card.fxml"));
            Node node = loader.load();

            QuizCardController cardCtrl = loader.getController();
            if (cardCtrl != null) cardCtrl.setQuiz(quiz);

            node.setOnMouseClicked(e -> {
                AppState.selectedQuiz.set(quiz);
                AppState.quizQuestionIndex.set(0);
                AppState.quizPoints.set(0);
                AppState.quizAnswers.clear();
                AppState.quizCorrectMap.clear();
                AppState.navOverride.set(AppState.NavItem.QUIZZES);
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

    private String formatTotal(int total) {
        return MessageFormat.format(getMessage("quizzes.subtitle", "Total: {0}"), total);
    }

    private void setTotalSubtitle(int total) {
        headerController.setSubtitle(formatTotal(total));
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