package controller.components;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class FlashcardFlipCardController {

    @FXML
    private StackPane root;
    @FXML
    private StackPane termPane;
    @FXML
    private StackPane definitionPane;

    @FXML
    private Label termLabel;
    @FXML
    private Label definitionLabel;

    private boolean showingTerm = true;
    private boolean animating = false;

    @FXML
    private void initialize() {
        showTerm();
        root.setRotationAxis(javafx.geometry.Point3D.ZERO.add(0, 1, 0));
        root.setStyle("-fx-background-color: transparent;");
        root.setCache(true);
    }

    @FXML
    public void flip() {
        if (animating) return;
        animating = true;

        // First half: rotate 0 -> 90
        RotateTransition firstHalf = new RotateTransition(Duration.millis(180), root);
        firstHalf.setFromAngle(0);
        firstHalf.setToAngle(90);
        firstHalf.setInterpolator(Interpolator.EASE_IN);

        firstHalf.setOnFinished(e -> {
            // swap content at halfway
            if (showingTerm) showDefinition();
            else showTerm();

            showingTerm = !showingTerm;

            // reset angle to -90 so it continues smoothly
            root.setRotate(-90);

            // Second half: rotate -90 -> 0
            RotateTransition secondHalf = new RotateTransition(Duration.millis(180), root);
            secondHalf.setFromAngle(-90);
            secondHalf.setToAngle(0);
            secondHalf.setInterpolator(Interpolator.EASE_OUT);

            secondHalf.setOnFinished(e2 -> animating = false);
            secondHalf.play();
        });

        firstHalf.play();

    }

    public void setTerm(String text) {
        termLabel.setText(text);
    }

    public void setDefinition(String text) {
        definitionLabel.setText(text);
    }

    public void showTerm() {
        termPane.setVisible(true);
        termPane.setManaged(true);

        definitionPane.setVisible(false);
        definitionPane.setManaged(false);
    }

    public void showDefinition() {
        definitionPane.setVisible(true);
        definitionPane.setManaged(true);

        termPane.setVisible(false);
        termPane.setManaged(false);
    }
}

