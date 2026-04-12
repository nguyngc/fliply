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
import java.io.IOException;

import static model.AppState.isTeacher;

/**
 * Controller for the home screen.
 * Displays a personalized welcome message, the latest class, and (for students) the latest quiz.
 * Provides quick access to recent classes and quizzes through clickable cards.
 */
public class HomeController {

    // ========== Services ==========
    private final ClassDetailsService classDetailsService = new ClassDetailsService();
    private final QuizService quizService = new QuizService();

    // ========== Header Components ==========
    // Label for displaying user's first name
    @FXML private Label nameLabel;
    
    // Label for displaying role-specific subtitle
    @FXML private Label subtitleLabel;

    // ========== Latest Class Section ==========
    // Container for the latest class card
    @FXML private StackPane latestClassHolder;

    // ========== Latest Quiz Section ==========
    // Section container for latest quiz (hidden for teachers)
    @FXML private VBox latestQuizSection;
    
    // Controller for the quiz card component
    @FXML private QuizCardController latestQuizCardController;

    // Cache of the latest quiz for handling click events
    private Quiz latestQuiz;

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the welcome message, renders the latest class card, and (for students) the latest quiz.
     * Hides quiz section for teachers as they don't have quizzes to take.
     */
    @FXML
    private void initialize() {
        // Get localized strings for the current locale
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());
        
        // Get the current logged-in user
        User user = AppState.currentUser.get();
        if (user == null) return;

        boolean teacher = user.isTeacher();

        // ========== Set Personalized Welcome ==========
        // Display user's first name in the welcome message
        nameLabel.setText(user.getFirstName() + "!");
        
        // Set role-specific subtitle (different for teachers and students)
        String key = teacher ? "home.subtitle.teacher" : "home.subtitle.student";
        subtitleLabel.setText(rb.getString(key));

        // ========== Configure Role-Based UI ==========
        // Hide quiz section for teachers (students only have quizzes)
        latestQuizSection.setVisible(!teacher);
        latestQuizSection.setManaged(!teacher);

        // ========== Render Content ==========
        // Render the latest class card
        renderLatestClass();
        
        // Render the latest quiz only for students
        if (!teacher) {
            renderLatestQuiz();
        }
    }

    /**
     * Renders the latest class card on the home screen.
     * Displays the most recently accessed or created class with role-specific information.
     * For students, shows progress; for teachers, shows student and flashcard set counts.
     */
    private void renderLatestClass() {
        // Get localized strings for error messages
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());
        
        // Clear any previously rendered content
        latestClassHolder.getChildren().clear();

        // Get the current user
        User user = AppState.currentUser.get();
        if (user == null || user.getUserId() == null) return;

        // ========== Load Classes ==========
        // Fetch all classes the user is enrolled in or teaches
        List<ClassModel> classes = classDetailsService.getClassesOfUser(user.getUserId());
        if (classes.isEmpty()) return;

        // Get the latest class (by ID, most recent)
        classes.sort((a, b) -> b.getClassId() - a.getClassId());
        ClassModel cd = classes.getFirst();

        try {
            // ========== Load Class Card Component ==========
            // Load the class card FXML template
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/class_card.fxml"));
            Node node = loader.load();
            ClassCardController ctrl = loader.getController();

            // ========== Calculate Progress ==========
            StudyService studyService = new StudyService();
            double progress = 0.0;

            // Calculate progress for students only
            if (!user.isTeacher()) {
                int totalLearned = 0;
                int totalCards = 0;
                // Sum progress across all flashcard sets in the class
                for (FlashcardSet set : cd.getFlashcardSets()) {
                    totalLearned += (int) (studyService.getProgressPercent(user, set) * set.getCards().size());
                    totalCards += set.getCards().size();
                }
                progress = (totalCards == 0) ? 0.0 : (double) totalLearned / totalCards;
            }

            // ========== Configure Card Based on Role ==========
            // Display different information for teachers and students
            if (user.isTeacher()) {
                // Teacher view: show student count and flashcard set count
                ctrl.setTeacherCard(cd.getClassName(), cd.getStudents().size(), cd.getFlashcardSets().size(), progress);
            } else {
                // Student view: show teacher name and progress
                ctrl.setStudentCard(cd.getClassName(), cd.getTeacherName(), progress);
            }

            // ========== Add Click Handler ==========
            // Navigate to class details when card is clicked
            node.setOnMouseClicked(e -> {
                AppState.selectedClass.set(cd);
                Navigator.go(AppState.Screen.CLASSES);
            });

            // Add the card to the display
            latestClassHolder.getChildren().add(node);
        } catch (IOException ex) {
            throw new IllegalStateException(rb.getString("home.error"), ex);
        }
    }

    /**
     * Renders the latest quiz card on the home screen (students only).
     * Displays the most recently created or assigned quiz for the user.
     * Teachers do not have access to this method.
     */
    private void renderLatestQuiz() {
        // Get the current user (should be a student)
        User user = AppState.currentUser.get();
        if (user == null || user.getUserId() == null) return;

        // ========== Load Quizzes ==========
        // Fetch all quizzes assigned to the user
        List<Quiz> quizzes = quizService.getQuizzesByUser(user.getUserId());
        if (quizzes == null || quizzes.isEmpty()) {
            // No quizzes available
            latestQuiz = null;
            return;
        }

        // Get the latest quiz (by ID, most recent)
        quizzes.sort((a, b) -> b.getQuizId() - a.getQuizId());
        latestQuiz = quizzes.getFirst();

        // ========== Display Quiz Card ==========
        // Configure the quiz card component with the latest quiz data
        if (latestQuizCardController != null) {
            latestQuizCardController.setQuiz(latestQuiz);
        }
    }

    /**
     * Handles the latest quiz card click event.
     * Only available for students. Initializes the quiz and navigates to the quiz detail screen.
     * Resets quiz state (questions, points, answers) on each click.
     *
     * @param event The mouse click event
     */
    @FXML
    private void onLatestQuizClicked(MouseEvent event) {
        // Return early if user is a teacher
        if (isTeacher()) return;

        // If no quiz is available, navigate to the quizzes list
        if (latestQuiz == null) {
            Navigator.go(AppState.Screen.QUIZZES);
            return;
        }

        // ========== Initialize Quiz State ==========
        // Store the selected quiz in app state
        AppState.selectedQuiz.set(latestQuiz);

        // Reset quiz progress
        AppState.quizQuestionIndex.set(0);
        AppState.quizPoints.set(0);
        
        // Clear previous quiz answers and results
        AppState.quizAnswers.clear();
        AppState.quizCorrectMap.clear();

        // ========== Navigate to Quiz ==========
        // Set the active navigation item to QUIZZES
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        
        // Navigate to the quiz detail screen
        Navigator.go(AppState.Screen.QUIZ_DETAIL);
    }
}