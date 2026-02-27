package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import model.entity.Quiz;

public class QuizCardController {

    @FXML private Label quizTitleLabel;
    @FXML private Label questionCountLabel;
    @FXML private Label progressTextLabel;
    @FXML private ProgressBar progressBar;

    public void setQuiz(Quiz quiz) {
        if (quiz == null) return;

        quizTitleLabel.setText("Quiz " + quiz.getQuizId());

        int total = (quiz.getNoOfQuestions() == null) ? 0 : quiz.getNoOfQuestions();
        questionCountLabel.setText(total + " questions");

        // progress hiddne
        if (progressTextLabel != null) {
            progressTextLabel.setVisible(false);
            progressTextLabel.setManaged(false);
        }
        if (progressBar != null) {
            progressBar.setVisible(false);
            progressBar.setManaged(false);
        }
    }
}