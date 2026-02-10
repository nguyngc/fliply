package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class FlashcardFlipCardController {

    @FXML
    private StackPane termPane;
    @FXML
    private StackPane definitionPane;

    @FXML
    private Label termLabel;
    @FXML
    private Label definitionLabel;

    private boolean showingTerm = true;

    @FXML
    private void initialize() {
        showTerm();
    }

    @FXML
    public void flip() {
        if (showingTerm) showDefinition();
        else showTerm();
    }

    public void setTerm(String text) {
        termLabel.setText(text);
    }

    public void setDefinition(String text) {
        definitionLabel.setText(text);
    }

    public void showTerm() {
        showingTerm = true;
        termPane.setVisible(true);
        termPane.setManaged(true);

        definitionPane.setVisible(false);
        definitionPane.setManaged(false);
    }

    public void showDefinition() {
        showingTerm = false;
        definitionPane.setVisible(true);
        definitionPane.setManaged(true);

        termPane.setVisible(false);
        termPane.setManaged(false);
    }
}

