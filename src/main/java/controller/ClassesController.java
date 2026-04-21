package controller;

import controller.components.ClassCardController;
import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.ClassModel;
import model.entity.User;
import model.service.ClassDetailsService;
import model.service.StudyService;
import util.LocaleManager;
import view.Navigator;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for managing the classes view in the application.
 * Handles displaying classes for both teachers and students with appropriate UI elements.
 */
public class ClassesController {

    // UI Components injected from FXML
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    // Container for class cards
    @FXML
    private VBox classListBox;

    // Service for fetching class details from database
    private final ClassDetailsService classDetailsService = new ClassDetailsService();

    // Resource bundle for localized strings
    private ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the header with appropriate title and styling based on user role.
     */
    @FXML
    private void initialize() {
        // Get current logged-in user from app state
        User user = AppState.currentUser.get();
        boolean isTeacher = user.isTeacher();

        // Configure header with title and subtitle based on user role
        headerController.setTitle(rb.getString("class.title"));
        String key = isTeacher ? "class.subtitle.teacher" : "class.subtitle.student";
        headerController.setSubtitle(rb.getString(key));

        // Apply visual variant (styling) based on user role
        headerController.applyVariant(isTeacher
                ? HeaderController.Variant.TEACHER
                : HeaderController.Variant.STUDENT
        );

        // Render the class list
        render();
    }

    /**
     * Populates the class list with cards for all classes the user is enrolled in.
     * For teachers, also adds an "Add Class" button at the bottom.
     */
    private void render() {
        // Clear existing cards from the list
        classListBox.getChildren().clear();
        
        // Get current user and check if they are a teacher
        User user = AppState.currentUser.get();
        if (user == null || user.getUserId() == null) return;
        boolean isTeacher = user.isTeacher();

        // Load all classes from database that the user belongs to
        List<ClassModel> classes = loadClassesForUser(user.getUserId());
        
        // Create a card for each class and add to the list
        for (ClassModel c : classes) {
            if (c.getClassId() != null) {
                ClassModel loadedClass = reloadClass(c.getClassId());
                if (loadedClass != null) {
                    classListBox.getChildren().add(buildClassCard(loadedClass, isTeacher));
                }
            }
        }
        
        // Teachers have the option to create additional classes
        if (isTeacher) {
            classListBox.getChildren().add(buildAddMoreClassTile());
        }
    }

    /**
     * Creates a visual card representing a class.
     * The card displays different information based on whether the user is a teacher or student.
     *
     * @param c The class model to display
     * @param isTeacher Whether the current user is a teacher
     * @return A Node containing the class card UI
     */
    private Node buildClassCard(ClassModel c, boolean isTeacher) {
        try {
            // Load the class card UI and its controller.
            LoadedClassCard loadedCard = loadClassCard();
            Node node = loadedCard.node();
            ClassCardController ctrl = loadedCard.controller();

            // Calculate the user's progress in this class
            double progress = calculateProgress(AppState.currentUser.get(), c);

            // Configure the card differently based on user role
            if (isTeacher) {
                // Teacher view: show class name, number of students, and flashcard sets
                ctrl.setTeacherCard(
                        c.getClassName(),
                        c.getStudents().size(),
                        c.getFlashcardSets().size(),
                        progress
                );
            } else {
                // Student view: show class name, teacher name, and progress
                ctrl.setStudentCard(
                        c.getClassName(),
                        c.getTeacher().getFirstName() + " " + c.getTeacher().getLastName(),
                        progress
                );
            }

            // Add click handler to navigate to class details when card is clicked
            node.setOnMouseClicked(e -> {
                AppState.selectedClass.set(c);
                // Navigate to appropriate detail view based on user role
                navigateTo(isTeacher ? AppState.Screen.TEACHER_CLASS_DETAIL : AppState.Screen.CLASS_DETAIL);
            });

            return node;
        } catch (Exception ex) {
            throw new IllegalArgumentException(rb.getString("class.error"), ex);
        }
    }

    /**
     * Creates a clickable tile for teachers to add a new class.
     * This tile appears at the bottom of the class list for teacher users.
     *
     * @return A Node containing the "Add Class" tile UI
     */
    private Node buildAddMoreClassTile() {
        // Create a container for the "Add Class" tile
        StackPane tile = new StackPane();
        tile.setPrefHeight(60);
        
        // Apply styling to make it look like the class cards
        tile.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 18;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 14, 0.2, 0, 6);
                -fx-cursor: hand;
                """);

        // Create the text label with localized "Add Class" string
        Label text = new Label(rb.getString("class.addClass"));
        text.setStyle("-fx-text-fill: #3D8FEF; -fx-font-size: 16px; -fx-font-weight: 600;");
        tile.getChildren().add(text);

        // Add click handler to navigate to the add class screen
        tile.setOnMouseClicked(e -> navigateTo(AppState.Screen.TEACHER_ADD_CLASS));
        return tile;
    }

    // ================================ Helper Methods ============================
    /**
     * Loads the list of classes that the user is enrolled in from the database.
     *
     * @param userId The ID of the user whose classes to load
     * @return A list of ClassModel objects representing the user's classes
     */
    List<ClassModel> loadClassesForUser(int userId) {
        return classDetailsService.getClassesOfUser(userId);
    }

    /**
     * Reloads the class details from the database to ensure we have the most up-to-date information.
     *
     * @param classId The ID of the class to reload
     * @return A ClassModel object with the latest details of the class
     */
    ClassModel reloadClass(int classId) {
        return classDetailsService.reloadClass(classId);
    }

    /**
     * Calculates the user's progress in a given class as a percentage.
     *
     * @param user The user whose progress to calculate
     * @param classModel The class for which to calculate progress
     * @return A double value between 0 and 100 representing the user's progress in the class
     */
    double calculateProgress(User user, ClassModel classModel) {
        StudyService studyService = new StudyService();
        return studyService.getClassProgress(user, classModel);
    }

    /**
     * Loads the class card UI component and its controller from FXML.
     *
     * @return A LoadedClassCard containing the Node and its controller
     * @throws Exception if there is an error loading the FXML
     */
    LoadedClassCard loadClassCard() throws Exception {
        FXMLLoader loader = createClassCardLoader();
        Node node = loader.load();
        return new LoadedClassCard(node, loader.getController());
    }

    /**
     * Creates a new FXMLLoader for the class card FXML file.
     *
     * @return A new FXMLLoader instance ready to load the class card UI
     */
    FXMLLoader createClassCardLoader() {
        return new FXMLLoader(getClass().getResource("/components/class_card.fxml"));
    }

    /**
     * Navigates to a different screen in the application.
     *
     * @param screen The screen to navigate to
     */
    void navigateTo(AppState.Screen screen) {
        Navigator.go(screen);
    }

    /**
     * A simple data class to hold the loaded class card Node and its controller together.
     * This allows us to easily return both from the loadClassCard method.
     */
    static final class LoadedClassCard {
        private final Node node;
        private final ClassCardController controller;

        /**
         * Constructs a new LoadedClassCard with the given Node and controller.
         *
         * @param node The Node representing the class card UI
         * @param controller The controller for the class card, used to configure its content
         */
        LoadedClassCard(Node node, ClassCardController controller) {
            this.node = node;
            this.controller = controller;
        }

        /**
         * Gets the Node representing the class card UI.
         *
         * @return The Node of the class card
         */
        Node node() {
            return node;
        }

        /**
         * Gets the controller for the class card, which can be used to set its content.
         *
         * @return The ClassCardController for the class card
         */
        ClassCardController controller() {
            return controller;
        }
    }
}
