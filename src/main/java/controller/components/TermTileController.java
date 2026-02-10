package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class TermTileController {

    public enum State {
        READ,
        UNREAD
    }

    @FXML
    private Button tileButton;

    private State state = State.UNREAD;

    @FXML
    private void initialize() {
        applyState();
    }

    @FXML
    private void onClick() {
        // optional: toggle on click
        setState(state == State.UNREAD ? State.READ : State.UNREAD);
    }

    public void setText(String text) {
        tileButton.setText(text);
    }

    public void setState(State newState) {
        this.state = newState;
        applyState();
    }

    public State getState() {
        return state;
    }

    private void applyState() {
        if (state == State.UNREAD) {
            tileButton.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 16;" +
                            "-fx-border-color: #D9ECFF;" +
                            "-fx-border-radius: 16;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: 600;" +
                            "-fx-text-fill: #2C2C2C;"
            );
        } else {
            tileButton.setStyle(
                    "-fx-background-color: #ACD7FF;" +
                            "-fx-background-radius: 16;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: 600;" +
                            "-fx-text-fill: #2C2C2C;"
            );
        }
    }
}
