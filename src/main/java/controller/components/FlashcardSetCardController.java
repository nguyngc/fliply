package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class FlashcardSetCardController {

    @FXML
    private Label subjectLabel;
    @FXML
    private Label countLabel;
    @FXML
    private Label progressTextLabel;
    @FXML
    private ProgressBar progressBar;

    public void setSubject(String subject) {
        subjectLabel.setText(subject);
    }

    public void setCardCount(int count) {
        countLabel.setText(count + " cards");
    }

    public void setProgress(double value) {
        progressBar.setProgress(value);
        progressTextLabel.setText((int) Math.round(value * 100) + "% Completed");
        setShowProgress(true);
    }

    /** Teacher uses this to hide progress completely. */
    public void setShowProgress(boolean show) {
        if (progressTextLabel != null) {
            progressTextLabel.setVisible(show);
            progressTextLabel.setManaged(show);
        }
        if (progressBar != null) {
            progressBar.setVisible(show);
            progressBar.setManaged(show);
        }
    }
}
