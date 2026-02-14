package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class TermTileController {

    @FXML
    private Button tileButton;
    private Runnable onSelected;
    private State state = State.UNREAD;

    @FXML
    private void initialize() {
        applyState();
    }

    public void setText(String text) {
        tileButton.setText(text);
    }

    public State getState() {
        return state;
    }

    public void setState(State newState) {
        this.state = newState;
        applyState();
    }

    public void setOnSelected(Runnable action) {
        this.onSelected = action;
    }

    @FXML
    private void onClick() {
        // Parent decides what happens (navigate, mark read, etc.)
        if (onSelected != null) onSelected.run();
    }

    private void applyState() {
        if (tileButton == null) return;

        if (state == State.UNREAD) {
            tileButton.setStyle(
                    "-fx-background-color: #ACD7FF;" +
                            "-fx-background-radius: 16;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: 600;" +
                            "-fx-text-fill: #2C2C2C;"
            );
        } else {
            tileButton.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 16;" +
                            "-fx-border-color: #D9ECFF;" +
                            "-fx-border-radius: 16;" +
                            "-fx-border-width: 1;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: 600;" +
                            "-fx-text-fill: #2C2C2C;"
            );
        }
    }

    public enum State {READ, UNREAD}
}
