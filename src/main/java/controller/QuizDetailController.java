package controller;

import controller.components.HeaderController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.AppState;
import view.Navigator;

import java.util.Arrays;

public class QuizDetailController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private Label termLabel;
    @FXML
    private Label pageLabel;

    @FXML
    private Button opt1;
    @FXML
    private Button opt2;
    @FXML
    private Button opt3;
    @FXML
    private Button opt4;

    @FXML
    private Button prevBtn;
    @FXML
    private Button nextBtn;

    @FXML
    private Button viewResultBtn;

    private AppState.QuizItem quiz;
    private boolean answeredThisQuestion = false;

    @FXML
    private void initialize() {
        quiz = AppState.selectedQuiz.get();
        if (quiz == null) {
            Navigator.go(AppState.Screen.QUIZZES);
            return;
        }

        if (headerController != null) {
            headerController.setTitle(quiz.getTitle());
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.QUIZZES));
        }

        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        render();
    }

    private void render() {
        int idx = AppState.quizQuestionIndex.get();
        int total = quiz.getQuestions().size();

        idx = clamp(idx, 0, total - 1);
        AppState.quizQuestionIndex.set(idx);

        AppState.QuizQuestion q = quiz.getQuestions().get(idx);

        termLabel.setText(q.getTerm());
        String[] opts = q.getOptions();

        opt1.setText(opts[0]);
        opt2.setText(opts[1]);
        opt3.setText(opts[2]);
        opt4.setText(opts[3]);

        pageLabel.setText((idx + 1) + " / " + total);

        if (headerController != null) {
            headerController.setSubtitle("Total points: " + AppState.quizPoints.get());
        }

        prevBtn.setDisable(idx == 0);

        boolean isLast = (idx == total - 1);

        // if last question, hide Next and show View Result (only when answered)
        if (isLast) {
            nextBtn.setVisible(false);
            nextBtn.setManaged(false);
        } else {
            nextBtn.setVisible(true);
            nextBtn.setManaged(true);
        }

        // restore selection if answered
        resetOptionStyles();

        if (AppState.quizAnswers.containsKey(idx)) {
            String chosen = AppState.quizAnswers.get(idx);
            String correct = q.getCorrect();

            // lock buttons
            setOptionsDisabled(true);

            // apply stored styles
            if (chosen.equals(correct)) {
                getButtonByText(chosen).setStyle(correctStyle());
            } else {
                getButtonByText(chosen).setStyle(wrongStyle());
                getButtonByText(correct).setStyle(correctStyle());
            }
        } else {
            setOptionsDisabled(false);
        }

        // View result button shows only on last question AND answered
        boolean canView = isLast && AppState.quizAnswers.containsKey(idx);
        viewResultBtn.setVisible(canView);
        viewResultBtn.setManaged(canView);
    }

    @FXML
    private void chooseOption(ActionEvent e) {
        int idx = AppState.quizQuestionIndex.get();
        if (AppState.quizAnswers.containsKey(idx)) return; // already answered

        Button clicked = (Button) e.getSource();
        AppState.QuizQuestion q = quiz.getQuestions().get(idx);

        String chosen = clicked.getText();
        String correct = q.getCorrect();

        AppState.quizAnswers.put(idx, chosen);

        boolean isCorrect = chosen.equals(correct);
        AppState.quizCorrectMap.put(idx, isCorrect);

        if (isCorrect) {
            AppState.quizPoints.set(AppState.quizPoints.get() + 1);
            clicked.setStyle(correctStyle());
        } else {
            clicked.setStyle(wrongStyle());
            getButtonByText(correct).setStyle(correctStyle());
        }

        setOptionsDisabled(true);

        if (headerController != null) {
            headerController.setSubtitle("Total points: " + AppState.quizPoints.get());
        }

        render(); // update viewResult button visibility if last question
    }

    @FXML
    private void prev() {
        AppState.quizQuestionIndex.set(AppState.quizQuestionIndex.get() - 1);
        render();
    }

    @FXML
    private void next() {
        AppState.quizQuestionIndex.set(AppState.quizQuestionIndex.get() + 1);
        render();
    }


    @FXML
    private void viewResult() {
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        Navigator.go(AppState.Screen.QUIZ_RESULT);
    }

    private void setOptionsDisabled(boolean disabled) {
        opt1.setDisable(disabled);
        opt2.setDisable(disabled);
        opt3.setDisable(disabled);
        opt4.setDisable(disabled);
    }

    private int clamp(int v, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, v));
    }

    private Button getButtonByText(String text) {
        for (Button b : Arrays.asList(opt1, opt2, opt3, opt4)) {
            if (b.getText().equals(text)) return b;
        }
        return opt1;
    }

    private void resetOptionStyles() {
        String base = "-fx-background-color: white; " +
                "-fx-background-radius: 16; " +
                "-fx-font-weight: 600;" +
                " -fx-border-color: D9F4F1;" +
                "-fx-border-radius: 16;";
        opt1.setStyle(base);
        opt2.setStyle(base);
        opt3.setStyle(base);
        opt4.setStyle(base);
    }

    private String correctStyle() {
        return "-fx-background-color: rgba(61,143,239,0.20); " +
                "-fx-border-color: #3D8FEF; " +
                "-fx-border-radius: 16; " +
                "-fx-background-radius: 16; " +
                "-fx-font-weight: 600;";
    }

    private String wrongStyle() {
        return "-fx-background-color: rgba(255,0,0,0.08); " +
                "-fx-border-color: rgba(255,0,0,0.45); " +
                "-fx-border-radius: 16; " +
                "-fx-background-radius: 16; " +
                "-fx-font-weight: 600;";
    }
}
