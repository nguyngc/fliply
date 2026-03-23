package controller;

import controller.components.ClassCardController;
import controller.components.QuizCardController;
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
import model.service.QuizService;
import model.service.StudyService;
import util.LocaleManager;
import view.Navigator;

import java.util.List;
import java.util.ResourceBundle;

import static model.AppState.isTeacher;

public class HomeController {

    private final ClassDetailsService classDetailsService = new ClassDetailsService();
    private final QuizService quizService = new QuizService();

    @FXML private Label nameLabel;
    @FXML private Label subtitleLabel;

    @FXML private StackPane latestClassHolder;

    @FXML private VBox latestQuizSection;

    @FXML private QuizCardController latestQuizCardController;

    private Quiz latestQuiz; // keep latest quiz for click

    @FXML
    private void initialize() {
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());
        User user = AppState.currentUser.get();
        if (user == null) return;

        boolean teacher = user.isTeacher();

        nameLabel.setText(user.getFirstName() + "!");
        String key = teacher ? "home.subtitle.teacher" : "home.subtitle.student";
        subtitleLabel.setText(rb.getString(key));

        // hide quiz section for teacher
        latestQuizSection.setVisible(!teacher);
        latestQuizSection.setManaged(!teacher);

        renderLatestClass();
        if (!teacher) {
            renderLatestQuiz();
        }
    }

    private void renderLatestClass() {
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());
        latestClassHolder.getChildren().clear();

        User user = AppState.currentUser.get();
        if (user == null || user.getUserId() == null) return;

        List<ClassModel> classes = classDetailsService.getClassesOfUser(user.getUserId());
        if (classes.isEmpty()) return;

        classes.sort((a, b) -> b.getClassId() - a.getClassId());
        ClassModel cd = classes.getFirst();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/class_card.fxml"));
            Node node = loader.load();
            ClassCardController ctrl = loader.getController();

            StudyService studyService = new StudyService();
            double progress = 0.0;

            if (!user.isTeacher()) {
                int totalLearned = 0;
                int totalCards = 0;
                for (FlashcardSet set : cd.getFlashcardSets()) {
                    totalLearned += (int) (studyService.getProgressPercent(user, set) * set.getCards().size());
                    totalCards += set.getCards().size();
                }
                progress = (totalCards == 0) ? 0.0 : (double) totalLearned / totalCards;
            }

            if (user.isTeacher()) {
                ctrl.setTeacherCard(cd.getClassName(), cd.getStudents().size(), cd.getFlashcardSets().size(), progress);
            } else {
                ctrl.setStudentCard(cd.getClassName(), cd.getTeacherName(), progress);
            }

            node.setOnMouseClicked(e -> {
                AppState.selectedClass.set(cd);
                Navigator.go(AppState.Screen.CLASSES);
            });

            latestClassHolder.getChildren().add(node);
        } catch (Exception ex) {
            throw new RuntimeException(rb.getString("home.error"), ex);
        }
    }

    private void renderLatestQuiz() {
        User user = AppState.currentUser.get();
        if (user == null || user.getUserId() == null) return;

        List<Quiz> quizzes = quizService.getQuizzesByUser(user.getUserId());
        if (quizzes == null || quizzes.isEmpty()) {
            latestQuiz = null;
            return;
        }

        quizzes.sort((a, b) -> b.getQuizId() - a.getQuizId());
        latestQuiz = quizzes.getFirst();

        if (latestQuizCardController != null) {
            latestQuizCardController.setQuiz(latestQuiz);
        }
    }

    @FXML
    private void onLatestQuizClicked(MouseEvent event) {
        if (isTeacher()) return;

        if (latestQuiz == null) {
            Navigator.go(AppState.Screen.QUIZZES);
            return;
        }

        AppState.selectedQuiz.set(latestQuiz);

        AppState.quizQuestionIndex.set(0);
        AppState.quizPoints.set(0);
        AppState.quizAnswers.clear();
        AppState.quizCorrectMap.clear();

        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        Navigator.go(AppState.Screen.QUIZ_DETAIL);
    }
}