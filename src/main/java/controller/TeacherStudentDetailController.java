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
import model.service.ClassDetailsService;
import model.service.StudyService;
import view.Navigator;

/**
 * Controller for the teacher's view of a student's progress in a class.
 * Displays the student's name, email, and progress on each flashcard set in the class.
 * Provides a back button to return to the class detail screen.
 */
public class TeacherStudentDetailController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;
    @FXML
    private VBox progressListBox;

    private final ClassDetailsService classDetailsService = new ClassDetailsService();
    private final StudyService studyService = new StudyService();

    /**
     * Initializes the screen by setting up the header with the selected student's information and rendering their progress.
     * If the required navigation context (selected student or class) is missing, navigates back to the class detail screen.
     */
    @FXML
    private void initialize() {
        User s = AppState.selectedStudent.get();
        ClassModel c =  AppState.selectedClass.get();

        // Guard against stale navigation state; return to class detail if required context is missing.
        if (s == null || c == null) {
            Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL);
            return;
        }
        if (c.getClassId() == null) {
            Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL);
            return;
        }
        c = classDetailsService.reloadClass(c.getClassId());
        AppState.selectedClass.set(c);

        headerController.setBackVisible(true);
        headerController.setTitle(s.getFirstName() + " " + s.getLastName());
        headerController.setSubtitle(s.getEmail());
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL));
        headerController.applyVariant(HeaderController.Variant.TEACHER);

        AppState.navOverride.set(AppState.NavItem.CLASSES);

        renderProgress(c);
    }

    /**
     * Builds one progress row per flashcard set for the selected student.
     * @param c the class whose flashcard sets to display progress for
     */
    private void renderProgress(ClassModel c) {
        progressListBox.getChildren().clear();
        User student = AppState.selectedStudent.get();

        for (FlashcardSet set : c.getFlashcardSets()) {
            int totalCards = set.getTotalCards().size();

            // Service returns percentage in [0..100].
            double pct = studyService.getProgressPercent(student, set);

            // Convert percentage to an approximate learned-card count for the "x/y" label.
            int learnedCards = (int) Math.round((pct / 100.0) * totalCards);

            // JavaFX ProgressBar expects normalized progress in [0..1].
            double progress = pct / 100.0;

            String title = set.getSubject() + " (" + learnedCards + "/" + totalCards + ")";

            addProgressCardRow(title, progress);
        }
    }


    /**
     * Creates a styled card-like row with set title, completion text, and progress bar.
     * @param title the title to display on the left side of the row (e.g. "Math (3/10)")
     * @param progress the normalized progress value in [0..1] to display in the progress bar and percentage label
     */
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
        // Keep component-specific skin in CSS so style updates do not require Java changes.
        bar.getStylesheets().add(getClass().getResource("/styles/progress_bar.css").toExternalForm());
        bar.getStyleClass().add("progress-bar");

        rowCard.getChildren().addAll(top, right, bar);
        progressListBox.getChildren().add(rowCard);
    }
}
