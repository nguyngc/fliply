package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.entity.User;
import model.service.StudyService;
import view.Navigator;

public class TeacherStudentDetailController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;
    @FXML
    private VBox progressListBox;

    private final StudyService studyService = new StudyService();

    @FXML
    private void initialize() {
//        AppState.StudentItem s = AppState.selectedStudent.get();
//        AppState.ClassItem c = AppState.selectedClass.get();
        User s = AppState.selectedStudent.get();
        ClassModel c =  AppState.selectedClass.get();

        if (s == null || c == null) {
            Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL);
            return;
        }

        headerController.setBackVisible(true);
        headerController.setTitle(s.getFirstName() + " " + s.getLastName());
        headerController.setSubtitle(s.getEmail());
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL));
        headerController.applyVariant(HeaderController.Variant.TEACHER);

        AppState.navOverride.set(AppState.NavItem.CLASSES);

        renderProgress(c);
    }

    private void renderProgress(ClassModel c) {
        progressListBox.getChildren().clear();
        User student = AppState.selectedStudent.get();

        for (FlashcardSet set : c.getFlashcardSets()) {
            double pct = studyService.getProgressPercent(student, set);
            double progress = pct > 1.0 ? (pct / 100.0) : pct;

            String title = set.getSubject() + " (" + set.getTotalCards() + "/" + set.getTotalCards() + ")";
            addProgressCardRow(title, progress);
        }
    }

    private void addProgressCardRow(String title, double progress) {
        VBox rowCard = new VBox(8);
        rowCard.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 18;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 14, 0.2, 0, 6);
                -fx-padding: 18 18 18 18;
                """);

        HBox top = new HBox(10);

        Label left = new Label(title);
        left.setStyle("-fx-font-size: 16px; -fx-font-weight: 500; -fx-text-fill: #2C2C2C;");
        HBox.setHgrow(left, Priority.ALWAYS);
        top.getChildren().addAll(left);

        Label right = new Label((int) Math.round(progress * 100) + "% Completed");
        right.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #3D8FEF;");

        ProgressBar bar = new ProgressBar(progress);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent: #3D8FEF;");
        bar.getStylesheets().add(getClass().getResource("/styles/progress_bar.css").toExternalForm());
        bar.getStyleClass().add("progress-bar");

        rowCard.getChildren().addAll(top, right, bar);
        progressListBox.getChildren().add(rowCard);
    }
}
