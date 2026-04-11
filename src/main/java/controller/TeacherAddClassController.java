package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import model.AppState;
import model.dao.ClassModelDao;
import model.entity.ClassModel;
import model.service.TeacherAddClassService;
import util.LocaleManager;
import view.Navigator;

import java.util.ResourceBundle;

/**
 * Controller for the teacher add/create class screen.
 * Allows teachers to create a new class by entering a class code/name.
 * Validates input and handles class creation through the service layer.
 */
public class TeacherAddClassController {

    // ========== Header Components ==========
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    // ========== Form Input ==========
    // Text field for entering the class code/name
    @FXML
    private TextField classCodeField;

    // ========== Services ==========
    // Service for class creation operations
    private final TeacherAddClassService teacherAddClass = new TeacherAddClassService();

    // ========== Resources ==========
    // Resource bundle for localized strings
    private final ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the header with title and back button functionality.
     */
    @FXML
    private void initialize() {
        // Set the header title
        headerController.setTitle(rb.getString("addClass.new"));
        
        // Show back button
        headerController.setBackVisible(true);
        
        // Set back button to navigate to classes screen
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASSES));
    }

    /**
     * Handles the add button click event.
     * Validates the class code input and creates a new class.
     * Displays error message if class creation fails.
     */
    @FXML
    private void onAdd() {
        // Get the class code from the input field and trim whitespace
        String code = classCodeField.getText() == null ? "" : classCodeField.getText().trim();
        
        // Validate that class code is not empty
        if (code.isBlank()) return;
        
        // ========== Create Class ==========
        try {
            // Call the service to create a new class with the entered code
            teacherAddClass.createClass(code);
            
            // Navigate back to the classes list after successful creation
            Navigator.go(AppState.Screen.CLASSES);
        } catch (IllegalArgumentException e) {
            // Display error message if class creation fails (e.g., duplicate code)
            System.out.println(rb.getString("addClass.error") + e.getMessage());
        }
    }

    /**
     * Handles the cancel button click event.
     * Navigates back to the classes screen without creating a class.
     */
    @FXML
    private void onCancel() {
        Navigator.go(AppState.Screen.CLASSES);
    }
}
