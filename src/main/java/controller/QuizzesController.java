package controller;

import controller.components.HeaderController;
import controller.components.QuizCardController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.Quiz;
import model.entity.User;
import model.service.QuizService;
import util.EmptyStateCards;
import view.Navigator;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Controller for the quizzes list screen.
 * Displays all quizzes available to the current student user.
 * Allows students to view quiz details, take quizzes, and create new quizzes.
 */
public class QuizzesController {

    // ========== Header Components ==========
    @FXML private Parent header;
    @FXML private HeaderController headerController;
    
    // ========== Content Components ==========
    // Container for displaying quiz cards
    @FXML private VBox listBox;
    
    // Label for displaying total number of quizzes
    @FXML private Label totalLabel;
    
    // ========== Resources ==========
    @FXML private ResourceBundle resources;

    // Service for quiz database operations
    private final QuizService quizService = new QuizService();

    /**
     * Initializes the controller when the FXML is loaded.
     * Loads all quizzes for the current user, sets up the header, and renders the quiz list.
     */
    @FXML
    private void initialize() {
        // ========== Load User Quizzes ==========
        // Get the currently logged-in user
        User user = AppState.currentUser.get();
        if (user == null) return;

        // Load all quizzes assigned to the user
        List<Quiz> quizzes = loadQuizzesForUser(user.getUserId());
        // Store quizzes in app state for access from other screens
        AppState.quizList.setAll(quizzes);

        // ========== Configure Header ==========
        if (headerController != null) {
            // Set the header title
            headerController.setTitle(getMessage("quizzes.title", "My Quizzes"));
            // Set subtitle showing total number of quizzes
            setTotalSubtitle(quizzes.size());
        }
        
        // ========== Configure Content ==========
        // Display total quiz count at the bottom of the screen
        if (totalLabel != null) {
            totalLabel.setText(formatTotal(quizzes.size()));
        }

        // Set the active navigation item to QUIZZES
        AppState.navOverride.set(AppState.NavItem.QUIZZES);
        
        // Render the quiz list
        render();
    }

    /**
     * Renders the list of quiz cards.
     * Creates a card for each quiz and adds an "Add Quiz" button at the end.
     * Updates the total quiz count display.
     */
    private void render() {
        // Clear any previously displayed quiz cards
        listBox.getChildren().clear();

        if (AppState.quizList.isEmpty()) {
            listBox.getChildren().add(buildEmptyState());
        }

        // Create a card for each quiz in the user's list
        for (Quiz quiz : AppState.quizList) {
            listBox.getChildren().add(loadQuizCard(quiz));
        }

        // Add the "Create New Quiz" button tile
        listBox.getChildren().add(buildAddTile());
        
        // Update header subtitle with current quiz count
        if (headerController != null) {
            setTotalSubtitle(AppState.quizList.size());
        }
        
        // Update the total label at the bottom of the screen
        if (totalLabel != null) {
            totalLabel.setText(formatTotal(AppState.quizList.size()));
        }
    }

    /**
     * Loads a quiz card from the FXML template.
     * Configures the card with quiz data and sets up the click handler.
     *
     * @param quiz The quiz to display on the card
     * @return A Node containing the configured quiz card
     */
    private Node loadQuizCard(Quiz quiz) {
        try {
            LoadedQuizCard loadedQuizCard = loadQuizCardNode();
            Node node = loadedQuizCard.node();
            QuizCardController cardCtrl = loadedQuizCard.controller();
            
            // Configure the card with quiz data
            if (cardCtrl != null) cardCtrl.setQuiz(quiz);

            // ========== Setup Click Handler ==========
            // When card is clicked, initialize quiz and navigate to detail view
            node.setOnMouseClicked(e -> {
                // Store the selected quiz
                AppState.selectedQuiz.set(quiz);
                
                // Reset quiz progress for taking the quiz
                AppState.quizQuestionIndex.set(0);
                AppState.quizPoints.set(0);
                
                // Clear any previous quiz answers
                AppState.quizAnswers.clear();
                AppState.quizCorrectMap.clear();
                
                // Set navigation to highlight QUIZZES menu
                AppState.navOverride.set(AppState.NavItem.QUIZZES);
                
                // Navigate to the quiz detail view
                navigateTo(AppState.Screen.QUIZ_DETAIL);
            });

            return node;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load quiz_card.fxml", ex);
        }
    }

    /**
     * Builds a clickable "Create New Quiz" tile with a plus (+) symbol.
     * The tile navigates to the quiz form when clicked.
     *
     * @return A Node containing the "Add" tile with appropriate styling and click handler
     */
    private Node buildAddTile() {
        // Create a container for the add tile
        StackPane box = new StackPane();
        box.setPrefHeight(90);
        
        // Apply styling to the tile
        box.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 18;
                -fx-border-radius: 18;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 18, 0.2, 0, 8);
                -fx-cursor: hand;
                """);

        // Create the plus icon label
        Label plus = new Label("+");
        plus.setStyle("-fx-font-size: 40px; -fx-font-weight: 900; -fx-text-fill: #2C2C2C;");
        box.getChildren().add(plus);

        // Add click handler to navigate to quiz creation form
        box.setOnMouseClicked(e -> {
            AppState.navOverride.set(AppState.NavItem.QUIZZES);
            navigateTo(AppState.Screen.QUIZ_FORM);
        });

        return box;
    }

    /**
     * Builds an empty state card to display when there are no quizzes.
     *
     * @return A Node containing the empty state card with title and message
     */
    private Node buildEmptyState() {
        return EmptyStateCards.create(
                getMessage("quizzes.empty.title", "No quizzes yet"),
                getMessage("quizzes.empty.body", "Generate a quiz from your flashcards to start practicing here.")
        );
    }

    /**
     * Formats the total quiz count into a localized message string.
     *
     * @param total The total number of quizzes
     * @return A formatted string like "Total: 5"
     */
    private String formatTotal(int total) {
        return MessageFormat.format(getMessage("quizzes.subtitle", "Total: {0}"), total);
    }

    /**
     * Sets the header subtitle to display the total quiz count.
     *
     * @param total The total number of quizzes to display
     */
    private void setTotalSubtitle(int total) {
        headerController.setSubtitle(formatTotal(total));
    }

    /**
     * Retrieves a localized message from the resource bundle.
     * Provides a fallback message if the key is not found.
     *
     * @param key The resource bundle key
     * @param fallback The fallback message if key is not found
     * @return The localized message or the fallback
     */
    private String getMessage(String key, String fallback) {
        if (resources == null) {
            return fallback;
        }
        try {
            return resources.getString(key);
        } catch (MissingResourceException ignored) {
            return fallback;
        }
    }

    // ========== Helper Methods for Data Loading and Navigation ==========

    /**
     * Loads all quizzes assigned to the specified user from the database using the QuizService.
     * @param userId
     * @return A list of Quiz objects associated with the user
     */
    List<Quiz> loadQuizzesForUser(int userId) {
        return quizService.getQuizzesByUser(userId);
    }

    /**
     * Loads the quiz card FXML and returns both the Node and its controller.
     *
     * @return A LoadedQuizCard containing the Node and QuizCardController
     * @throws IOException If loading the FXML fails
     */
    LoadedQuizCard loadQuizCardNode() throws IOException {
        FXMLLoader loader = createQuizCardLoader();
        Node node = loader.load();
        return new LoadedQuizCard(node, loader.getController());
    }

    /**
     * Creates an FXMLLoader for the quiz card FXML template.
     *
     * @return A new FXMLLoader instance configured to load quiz_card.fxml
     */
    FXMLLoader createQuizCardLoader() {
        return new FXMLLoader(getClass().getResource("/components/quiz_card.fxml"));
    }

    /**
     * Navigates to the specified screen using the Navigator.
     *
     * @param screen The screen to navigate to
     */
    void navigateTo(AppState.Screen screen) {
        Navigator.go(screen);
    }

    // ========== Helper Class for Loaded Quiz Card ==========
    static final class LoadedQuizCard {
        private final Node node;
        private final QuizCardController controller;

        /**
         * Constructs a LoadedQuizCard with the given Node and QuizCardController.
         *
         * @param node The Node representing the loaded quiz card
         * @param controller The controller associated with the quiz card
         */
        LoadedQuizCard(Node node, QuizCardController controller) {
            this.node = node;
            this.controller = controller;
        }

        /**
         * Returns the Node representing the loaded quiz card.
         *
         * @return The Node of the quiz card
         */
        Node node() {
            return node;
        }

        /**
         * Returns the QuizCardController associated with the loaded quiz card.
         *
         * @return The QuizCardController of the quiz card
         */
        QuizCardController controller() {
            return controller;
        }
    }
}
