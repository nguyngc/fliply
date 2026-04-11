package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import model.AppState;
import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.entity.User;
import model.service.ClassDetailsService;
import model.service.FlashcardService;
import model.service.FlashcardSetService;
import util.FlashcardFileParser;
import util.LocalizationService;
import view.Navigator;

import java.io.File;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Controller for the teacher add flashcard set screen.
 * Allows teachers to create a new flashcard set and import cards from a CSV file.
 * Handles file selection, parsing, and bulk flashcard creation.
 */
public class TeacherAddSetController {

    // ========== Header Components ==========
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    // ========== Form Input Components ==========
    // Text field for entering the subject name of the flashcard set
    @FXML
    private TextField subjectField;
    
    // Label for displaying file upload status and parsed card count
    @FXML
    private Label fileStatusLabel;

    // ========== File and Parsing State ==========
    // The CSV file selected by the user
    private File selectedFile;
    
    // Number of flashcards parsed from the selected file
    private int parsedCount = 0;
    
    // Map for localized strings from resource bundle
    private Map<String, String> localizedStrings;

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the header with title and back button functionality.
     */
    @FXML
    private void initialize() {
        // Load localized strings for the current locale
        localizedStrings = LocalizationService.getLocalizedStrings();
        
        // Set the header title
        headerController.setTitle(localizedStrings.get("teacherAddSet.title"));
        
        // Show back button
        headerController.setBackVisible(true);
        
        // Set back button to navigate to teacher class detail screen
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL));
    }

    /**
     * Handles the upload button click event.
     * Opens a file chooser for selecting a CSV file containing flashcards.
     * Parses the file and displays the number of cards that will be imported.
     */
    @FXML
    private void onUpload() {
        // ========== Open File Chooser ==========
        // Create a file chooser dialog
        FileChooser chooser = new FileChooser();
        // Filter to show only CSV files
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        // Show the file chooser dialog
        selectedFile = chooser.showOpenDialog(null);

        // If user cancelled the file chooser
        if (selectedFile == null) return;

        // ========== Parse File ==========
        try {
            // Read all lines from the selected file
            List<String> lines = Files.readAllLines(selectedFile.toPath());
            // Calculate number of cards (total lines minus 1 for header row)
            parsedCount = Math.max(0, lines.size() - 1);

            // Display success message with filename and card count
            fileStatusLabel.setText(MessageFormat.format(
                    localizedStrings.get("teacherAddSet.fileStatusSuccess"),
                    selectedFile.getName(),
                    parsedCount
            ));
        } catch (Exception ex) {
            // Reset count and display error message if parsing fails
            parsedCount = 0;
            fileStatusLabel.setText(localizedStrings.get("teacherAddSet.fileStatusError"));
        }
    }

    /**
     * Handles the add button click event.
     * Validates inputs, parses the CSV file, creates the flashcard set, and imports all flashcards.
     * Updates the class with the new set and navigates back.
     */
    @FXML
    private void onAdd() {
        try {
            // ========== Validate File ==========
            // Check if a file has been selected
            if (selectedFile == null) return;

            // ========== Parse Flashcards from File ==========
            // Parse the CSV file to extract flashcard data
            List<FlashcardFileParser.ParsedCard> cards = FlashcardFileParser.parse(selectedFile);
            // If no cards were parsed, return early
            if (cards.isEmpty()) return;

            // ========== Get Selected Class ==========
            // Retrieve the class from app state
            ClassModel c = AppState.selectedClass.get();
            if (c == null) return;

            // ========== Validate Subject Name ==========
            // Get and trim the subject name from the input field
            String subject = subjectField.getText() == null ? "" : subjectField.getText().trim();
            // Check that subject is not empty
            if (subject.isBlank()) return;

            // ========== Create Flashcard Set ==========
            // Create a new flashcard set with the provided subject name
            final FlashcardSetService flashcardSetService = new FlashcardSetService();
            FlashcardSet set = flashcardSetService.createSet(subject, c);

            // ========== Import Flashcards ==========
            // Get the current teacher user
            User teacher = AppState.currentUser.get();
            final FlashcardService flashcardService = new FlashcardService();
            
            // Create a flashcard for each parsed card from the file
            for (FlashcardFileParser.ParsedCard card : cards) {
                flashcardService.createFlashcard(card.term(), card.definition(), set, teacher);
            }

            // ========== Reload Class and Navigate ==========
            // Reload the class details to reflect the new flashcard set
            ClassDetailsService classDetailsService = new ClassDetailsService();
            AppState.selectedClass.set(classDetailsService.reloadClass(c.getClassId()));

            // Navigate back to the teacher class detail screen
            Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL);
        } catch (Exception ex) {
            // Log exception for debugging
            ex.printStackTrace();
            System.err.println("Error: " + ex.getMessage());
        }
    }

    /**
     * Handles the cancel button click event.
     * Navigates back to the teacher class detail screen without creating a set.
     */
    @FXML
    private void onCancel() {
        Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL);
    }
}
