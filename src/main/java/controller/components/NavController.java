package controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.AppState;
import view.Navigator;

public class NavController {

    private final ToggleGroup group = new ToggleGroup();
    // preload images
    private final Image homeActive = load("images/home_btn.png");
    private final Image homeInactive = load("images/home_btn_inactive.png");
    private final Image classActive = load("images/class_btn.png");
    private final Image classInactive = load("images/class_btn_inactive.png");
    private final Image flashcardActive = load("images/flashcard_btn.png");
    private final Image flashcardInactive = load("images/flashcard_btn_inactive.png");
    private final Image quizActive = load("images/quiz_btn.png");
    private final Image quizInactive = load("images/quiz_btn_inactive.png");
    private final Image accountActive = load("images/account_btn.png");
    private final Image accountInactive = load("images/account_btn_inactive.png");

    @FXML
    private ImageView homeIcon;
    @FXML
    private ImageView classIcon;
    @FXML
    private ImageView flashcardIcon;
    @FXML
    private ImageView quizIcon;
    @FXML
    private ImageView accountIcon;
    @FXML
    private ToggleButton homeBtn;
    @FXML
    private ToggleButton classBtn;
    @FXML
    private ToggleButton flashBtn;
    @FXML
    private ToggleButton quizBtn;
    @FXML
    private ToggleButton accountBtn;
    @FXML
    private Label homeLabel;
    @FXML
    private Label classLabel;
    @FXML
    private Label flashLabel;
    @FXML
    private Label quizLabel;
    @FXML
    private Label accountLabel;


    @FXML
    private void initialize() {
        // Toggle group
        homeBtn.setToggleGroup(group);
        classBtn.setToggleGroup(group);
        flashBtn.setToggleGroup(group);
        quizBtn.setToggleGroup(group);
        accountBtn.setToggleGroup(group);

        // Initial state
        homeIcon.setImage(homeInactive);
        classIcon.setImage(classInactive);
        flashcardIcon.setImage(flashcardInactive);
        quizIcon.setImage(quizInactive);
        accountIcon.setImage(accountInactive);

        // Listen to selection changes
        homeBtn.selectedProperty().addListener((obs, oldV, selected) -> homeIcon.setImage(selected ? homeActive : homeInactive));

        classBtn.selectedProperty().addListener((obs, oldV, selected) -> classIcon.setImage(selected ? classActive : classInactive));

        flashBtn.selectedProperty().addListener((obs, oldV, selected) -> flashcardIcon.setImage(selected ? flashcardActive : flashcardInactive));

        quizBtn.selectedProperty().addListener((obs, oldV, selected) -> quizIcon.setImage(selected ? quizActive : quizInactive));

        accountBtn.selectedProperty().addListener((obs, oldV, selected) -> accountIcon.setImage(selected ? accountActive : accountInactive));

        // Sync with global navigation state
        AppState.activeNav.addListener((obs, oldV, newV) -> updateFromAppState(newV));
        updateFromAppState(AppState.activeNav.get());

        // Role-based menu visibility (Teacher: Home, Classes, Account)
        AppState.role.addListener((obs, o, n) -> applyRole(n));
        applyRole(AppState.role.get());
    }

    private void updateFromAppState(AppState.NavItem nav) {
        homeBtn.setSelected(nav == AppState.NavItem.HOME);
        classBtn.setSelected(nav == AppState.NavItem.CLASSES);
        flashBtn.setSelected(nav == AppState.NavItem.FLASHCARDS);
        quizBtn.setSelected(nav == AppState.NavItem.QUIZZES);
        accountBtn.setSelected(nav == AppState.NavItem.ACCOUNT);

        homeLabel.setStyle(labelStyle(nav == AppState.NavItem.HOME));
        classLabel.setStyle(labelStyle(nav == AppState.NavItem.CLASSES));
        flashLabel.setStyle(labelStyle(nav == AppState.NavItem.FLASHCARDS));
        quizLabel.setStyle(labelStyle(nav == AppState.NavItem.QUIZZES));
        accountLabel.setStyle(labelStyle(nav == AppState.NavItem.ACCOUNT));
    }

    private String labelStyle(boolean active) {
        return active
                ? "-fx-text-fill: #3D8FEF; -fx-font-size: 11px; -fx-font-weight: 600;"
                : "-fx-text-fill: #8C8C8C; -fx-font-size: 11px; -fx-font-weight: 500;";
    }

    private Image load(String path) {
        return new Image(getClass().getClassLoader().getResourceAsStream(path));
    }

    private void applyRole(AppState.Role role) {
        boolean teacher = role == AppState.Role.TEACHER;
        flashBtn.setVisible(!teacher);
        flashBtn.setManaged(!teacher);
        quizBtn.setVisible(!teacher);
        quizBtn.setManaged(!teacher);
    }

    @FXML
    private void goHome() {
        Navigator.go(AppState.Screen.HOME);
    }

    @FXML
    private void goClass() {
        Navigator.go(AppState.Screen.CLASSES);
    }

    @FXML
    private void goFlash() {
        Navigator.go(AppState.Screen.FLASHCARDS);
    }

    @FXML
    private void goQuiz() {
        Navigator.go(AppState.Screen.QUIZZES);
    }

    @FXML
    private void goAccount() {
        Navigator.go(AppState.Screen.ACCOUNT);
    }
}
