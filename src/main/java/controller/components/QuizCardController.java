package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class QuizCardController {

    @FXML private Label quizTitleLabel;
    @FXML private Label questionCountLabel;
    @FXML private Label progressTextLabel;
    @FXML private ProgressBar progressBar;

    public void setTitle(String title) {
        quizTitleLabel.setText(title);
    }

    public void setQuestionCount(int count) {
        questionCountLabel.setText(count + " questions");
    }

    /** value: 0.0 - 1.0 */
    public void setProgress(double value) {
        progressBar.setProgress(value);
        progressTextLabel.setText((int) Math.round(value * 100) + "%");
    }
}
