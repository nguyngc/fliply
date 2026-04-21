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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the teacher add flashcard set screen.
 * Allows teachers to create a new flashcard set and import cards from a CSV file.
 * Handles file selection, parsing, and bulk flashcard creation.
 */
public class TeacherAddSetController {
    private static final Logger LOGGER = Logger.getLogger(TeacherAddSetController.class.getName());

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
        localizedStrings = loadLocalizedStrings();
        
        // Set the header title
        headerController.setTitle(localizedStrings.get("teacherAddSet.title"));
        
        // Show back button
        headerController.setBackVisible(true);
        
        // Set back button to navigate to teacher class detail screen
        headerController.setOnBack(() -> navigateTo(AppState.Screen.TEACHER_CLASS_DETAIL));
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
        selectedFile = chooseFile();

        // If user cancelled the file chooser
        if (selectedFile == null) return;

        // ========== Parse File ==========
        try {
            // Read all lines from the selected file
            List<String> lines = readAllLines(selectedFile);
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
            List<FlashcardFileParser.ParsedCard> cards = parseSelectedFile(selectedFile);
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
            FlashcardSet set = createSet(subject, c);

            // ========== Import Flashcards ==========
            // Get the current teacher user
            User teacher = AppState.currentUser.get();
            // Create a flashcard for each parsed card from the file
            for (FlashcardFileParser.ParsedCard card : cards) {
                createFlashcard(card.term(), card.definition(), set, teacher);
            }

            // ========== Reload Class and Navigate ==========
            // Reload the class details to reflect the new flashcard set
            AppState.selectedClass.set(reloadClass(c.getClassId()));

            // Navigate back to the teacher class detail screen
            navigateTo(AppState.Screen.TEACHER_CLASS_DETAIL);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error while adding flashcard set", ex);
        }
    }

    /**
     * Handles the cancel button click event.
     * Navigates back to the teacher class detail screen without creating a set.
     */
    @FXML
    private void onCancel() {
        navigateTo(AppState.Screen.TEACHER_CLASS_DETAIL);
    }

    // ========== Helper Methods ==========
    /** Loads localized strings from the resource bundle for the current locale.
     *
     * @return A map of localization keys to their corresponding localized strings.
     */
    Map<String, String> loadLocalizedStrings() {
        return LocalizationService.getLocalizedStrings();
    }

    /** Opens a file chooser dialog for selecting a CSV file.
     *
     * @return The selected File object, or null if the user cancels the dialog.
     */
    File chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        return chooser.showOpenDialog(null);
    }

    /** Reads all lines from the given file.
     *
     * @param file The file to read from.
     * @return A list of strings, each representing a line from the file.
     * @throws Exception If an error occurs while reading the file.
     */
    List<String> readAllLines(File file) throws Exception {
        return Files.readAllLines(file.toPath());
    }

    /** Parses the selected CSV file to extract flashcard data.
     *
     * @param file The CSV file to parse.
     * @return A list of ParsedCard objects containing term and definition pairs.
     * @throws Exception If an error occurs during parsing.
     */
    List<FlashcardFileParser.ParsedCard> parseSelectedFile(File file) throws Exception {
        return FlashcardFileParser.parse(file);
    }

    /** Creates a new flashcard set with the given subject name and associates it with the specified class.
     *
     * @param subject The subject name for the flashcard set.
     * @param classModel The class to associate the flashcard set with.
     * @return The created FlashcardSet object.
     */
    FlashcardSet createSet(String subject, ClassModel classModel) {
        FlashcardSetService flashcardSetService = new FlashcardSetService();
        return flashcardSetService.createSet(subject, classModel);
    }

    /** Creates a new flashcard with the given term and definition, associates it with the specified flashcard set, and records the teacher who created it.
     *
     * @param term The term for the flashcard.
     * @param definition The definition for the flashcard.
     * @param set The flashcard set to associate the flashcard with.
     * @param teacher The user who is creating the flashcard (should be a teacher).
     */
    void createFlashcard(String term, String definition, FlashcardSet set, User teacher) {
        FlashcardService flashcardService = new FlashcardService();
        flashcardService.createFlashcard(term, definition, set, teacher);
    }

    /** Reloads the class details for the given class ID to reflect any changes made (e.g., new flashcard set).
     *
     * @param classId The ID of the class to reload.
     * @return The reloaded ClassModel object with updated details.
     */
    ClassModel reloadClass(int classId) {
        ClassDetailsService classDetailsService = new ClassDetailsService();
        return classDetailsService.reloadClass(classId);
    }

    /** Navigates to the specified screen using the Navigator.
     *
     * @param screen The screen to navigate to.
     */
    void navigateTo(AppState.Screen screen) {
        Navigator.go(screen);
    }
}
