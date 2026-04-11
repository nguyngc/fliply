package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.service.FlashcardSetService;
import util.LocaleManager;
import view.Navigator;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for displaying the details of a selected class.
 * Shows the class name, teacher information, and lists all flashcard sets available in the class.
 */
public class ClassDetailController {

    // UI Components injected from FXML
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;
    
    // Container for flashcard set buttons
    @FXML
    private VBox flashcardSetListBox;

    // Service for fetching flashcard sets from the database
    private final FlashcardSetService flashcardSetService = new FlashcardSetService();

    // Resource bundle for localized strings
    private final ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

    /**
     * Initializes the controller when the FXML is loaded.
     * Retrieves the selected class, sets up the header with class details,
     * loads all flashcard sets for the class, and configures navigation.
     */
    @FXML
    private void initialize() {
        // Get the class selected from the previous screen
        ClassModel c = AppState.selectedClass.get();
        
        // Handle case where no class is selected
        if (c == null) {
            headerController.setTitle(rb.getString("class.title"));
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASSES));
            return;
        }

        // Set header title to the class name
        headerController.setTitle(c.getClassName());
        
        // Display teacher name as metadata in the header
        headerController.setMeta(rb.getString("class.teacher") + " " + c.getTeacher().getFirstName() + " " + c.getTeacher().getLastName());
        
        // Apply styling variant based on whether the user is a teacher or student
        headerController.applyVariant(
                AppState.currentUser.get().isTeacher()
                        ? HeaderController.Variant.TEACHER
                        : HeaderController.Variant.STUDENT
        );
        
        // Configure back button to navigate back to classes list
        headerController.setBackVisible(true);
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASSES));

        // Load and display all flashcard sets for this class
        List<FlashcardSet> sets = flashcardSetService.getSetsByClass(c.getClassId());
        renderSets(sets);
    }

    /**
     * Populates the flashcard set list with clickable buttons for each set in the class.
     * Each button displays the subject name and navigates to the flashcard set details when clicked.
     *
     * @param sets List of flashcard sets to display
     */
    private void renderSets(List<FlashcardSet> sets) {
        // Clear any previously displayed sets
        flashcardSetListBox.getChildren().clear();

        // Create a button for each flashcard set
        for (FlashcardSet set : sets) {
            // Create button with the flashcard set's subject name
            Button btn = new Button(set.getSubject());
            btn.setPrefHeight(48);
            btn.setMaxWidth(Double.MAX_VALUE);
            
            // Apply styling to match the application's design
            btn.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 14;
                    -fx-font-size: 16px;
                    -fx-font-weight: 600;
                    -fx-cursor: hand;
                    """);

            // Add click handler to open the flashcard set when button is clicked
            btn.setOnAction(e -> {
                AppState.selectedFlashcardSet.set(set);
                Navigator.go(AppState.Screen.FLASHCARD_SET);
            });

            // Add the button to the list
            flashcardSetListBox.getChildren().add(btn);
        }
    }
}
