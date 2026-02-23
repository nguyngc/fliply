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
import model.entity.*;
import model.service.ClassDetailsService;
import model.service.StudyService;
import view.Navigator;

import java.util.List;

import static model.AppState.isTeacher;

public class HomeController {
    private final ClassDetailsService classDetailsService = new ClassDetailsService();
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
        //AppState.seedDemoIfNeeded();
        //boolean isTeacher = AppState.isTeacher();
        User user = AppState.currentUser.get();
        boolean isTeacher = AppState.currentUser.get().isTeacher();

        // header text
        //nameLabel.setText(isTeacher ? "Teacher" : "Student");
        nameLabel.setText(user.getFirstName() + "!");
        subtitleLabel.setText(isTeacher ? "Manage your classes" : "Let's start learning");

        // hide quiz section for teacher
        latestQuizSection.setVisible(!isTeacher);
        latestQuizSection.setManaged(!isTeacher);

        renderLatestClass();
    }

    private void renderLatestClass() {
        latestClassHolder.getChildren().clear();
        User user = AppState.currentUser.get();
        //if (AppState.demoClasses.isEmpty()) return;
        //AppState.ClassItem c = AppState.demoClasses.get(0); // latest class demo
        List<ClassModel> classes = classDetailsService.getClassesOfUser(user.getUserId());
        if (classes.isEmpty()) return;
        // sort newest first
        classes.sort((a, b) -> b.getClassId() - a.getClassId());
        ClassModel cd = classes.getFirst();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/class_card.fxml"));
            Node node = loader.load();
            ClassCardController ctrl = loader.getController();
            // progress
            StudyService studyService = new StudyService();
            double progress = 0.0;
            if (!user.isTeacher()) {
                int totalLearned = 0;
                int totalCards = 0;
                for (FlashcardSet set : cd.getFlashcardSets()) {
                    totalLearned += (int) (studyService.getProgressPercent(user, set) * set.getCards().size());
                    totalCards += set.getCards().size(); }
                progress = (totalCards == 0) ? 0.0 : (double) totalLearned / totalCards;
            }
            // Set cards UI
            if (user.isTeacher()) {
                ctrl.setTeacherCard( cd.getClassName(), cd.getStudents().size(), cd.getFlashcardSets().size(), progress );
            } else {
                ctrl.setStudentCard( cd.getClassName(), cd.getTeacherName(), progress ); }

            node.setOnMouseClicked(e -> {
                AppState.selectedClass.set(cd);
                if (isTeacher()) Navigator.go(AppState.Screen.CLASSES);
                else Navigator.go(AppState.Screen.CLASSES);
            });

            latestClassHolder.getChildren().add(node);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load class_card.fxml for home", ex);
        }
    }

    @FXML
    private void onLatestQuizClicked(MouseEvent event) {
        // Teacher has no quiz section
        if (isTeacher()) return;

        if (AppState.myQuizzes.isEmpty()) {
            Navigator.go(AppState.Screen.QUIZZES);
            return;
        }

        // Pick the latest quiz
        List<Quiz> quizzes = AppState.myQuizzes;
        // sort newest first
        quizzes.sort((a, b) -> b.getQuizId() - a.getQuizId());
        AppState.selectedQuiz.set(quizzes.getFirst());

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
