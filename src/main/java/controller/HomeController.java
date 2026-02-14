package controller;

import controller.components.ClassCardController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AppState;
import view.Navigator;

public class HomeController {

    @FXML
    private Label nameLabel;
    @FXML
    private Label subtitleLabel;

    @FXML
    private StackPane latestClassHolder;

    @FXML
    private VBox latestQuizSection;

    @FXML
    private void initialize() {
        AppState.seedDemoIfNeeded();

        boolean isTeacher = AppState.isTeacher();

        // header text
        nameLabel.setText(isTeacher ? "Teacher" : "Student");
        subtitleLabel.setText(isTeacher ? "Manage your classes" : "Let's start learning");

        // hide quiz section for teacher
        latestQuizSection.setVisible(!isTeacher);
        latestQuizSection.setManaged(!isTeacher);

        renderLatestClass();
    }

    private void renderLatestClass() {
        latestClassHolder.getChildren().clear();

        if (AppState.demoClasses.isEmpty()) return;

        AppState.ClassItem c = AppState.demoClasses.get(0); // latest class demo

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/class_card.fxml"));
            Node node = loader.load();
            ClassCardController ctrl = loader.getController();

            double progress = 0.80; // demo
            if (AppState.isTeacher()) {
                ctrl.setTeacherCard(c.getClassCode(), c.getStudentCount(), c.getSetCount(), progress);
            } else {
                ctrl.setStudentCard(c.getClassCode(), c.getTeacherName(), progress);
            }

            node.setOnMouseClicked(e -> {
                AppState.selectedClass.set(c);
                if (AppState.isTeacher()) Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL);
                else Navigator.go(AppState.Screen.CLASS_DETAIL);
            });

            latestClassHolder.getChildren().add(node);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load class_card.fxml for home", ex);
        }
    }

    @FXML
    private void onLatestQuizClicked(MouseEvent event) {
        // Teacher has no quiz section
        if (AppState.isTeacher()) return;

        if (AppState.myQuizzes == null || AppState.myQuizzes.isEmpty()) {
            Navigator.go(AppState.Screen.QUIZZES);
            return;
        }

        // Pick the latest quiz (first one for demo)
        AppState.selectedQuiz.set(AppState.myQuizzes.get(0));

        // Reset quiz session state
        AppState.quizQuestionIndex.set(0);
        AppState.quizPoints.set(0);
        AppState.quizAnswers.clear();
        AppState.quizCorrectMap.clear();

        // Keep menu highlight correct
        AppState.navOverride.set(AppState.NavItem.QUIZZES);

        // Go detail
        Navigator.go(AppState.Screen.QUIZ_DETAIL);
    }
}
