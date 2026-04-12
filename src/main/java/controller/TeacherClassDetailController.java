package controller;

import controller.components.FlashcardSetCardController;
import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.ClassDetails;
import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.entity.User;
import model.service.ClassDetailsService;
import model.service.UserService;
import util.LocaleManager;
import view.Navigator;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TeacherClassDetailController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private Label studentsSectionLabel;
    @FXML
    private VBox enrolledStudentsBox;

    @FXML
    private Button showSearchBtn;
    @FXML
    private VBox studentSearchBox;
    @FXML
    private TextField studentSearchField;
    @FXML
    private VBox searchResultsBox;

    @FXML
    private Label setsSectionLabel;
    @FXML
    private VBox setListBox;
    
    // The currently selected class
    private ClassModel c;
    
    // Service for class management operations
    private final ClassDetailsService classDetailsService = new ClassDetailsService();
    
    // Service for user management operations
    private final UserService userService = new UserService();
    
    // Resource bundle for localized strings
    private final ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the header, initializes the student search interface, and renders all content.
     */
    @FXML
    private void initialize() {
        // ========== Get Selected Class ==========
        // Retrieve the class selected from the previous screen
        c = AppState.selectedClass.get();
        if (c == null) {
            // Navigate back to classes list if no class is selected
            Navigator.go(AppState.Screen.CLASSES);
            return;
        }

        // ========== Configure Header ==========
        if (headerController != null) {
            // Show back button to return to classes list
            headerController.setBackVisible(true);
            // Set header title to the class name
            headerController.setTitle(c.getClassName());
            // Set back navigation
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASSES));
            // Apply teacher variant styling
            headerController.applyVariant(HeaderController.Variant.TEACHER);
        }

        // ========== Configure Student Search ==========
        // Initially hide the student search interface
        studentSearchBox.setVisible(false);
        studentSearchBox.setManaged(false);

        // ========== Setup Search Listener ==========
        // Listen for text changes in the search field and update results in real-time
        studentSearchField.textProperty().addListener((obs, o, n) -> renderSearchResults());

        // ========== Render Initial Content ==========
        // Update student and set counts
        refreshCounts();
        // Display enrolled students
        renderEnrolledStudents();
        // Display flashcard sets
        renderSets();
    }

    /**
     * Updates the labels displaying student and flashcard set counts.
     */
    private void refreshCounts() {
        // Update students section label with current count
        if (studentsSectionLabel != null) {
            studentsSectionLabel.setText(rb.getString("classDetail.students") + " (" + c.getStudents().size() + ")");
        }
        // Update sets section label with current count
        if (setsSectionLabel != null) {
            setsSectionLabel.setText(rb.getString("classDetail.sets") + " (" + c.getFlashcardSets().size() + ")");
        }
    }

    // ========== STUDENTS SECTION: Enrolled List ==========

    /**
     * Renders the list of enrolled students in the class.
     */
    private void renderEnrolledStudents() {
        enrolledStudentsBox.getChildren().clear();

        for (ClassDetails cd : c.getStudents()) {
            User student = cd.getStudent();
            Node row = buildEnrolledStudentRow(student, cd);
            enrolledStudentsBox.getChildren().add(row);
        }

        refreshCounts();
    }

    /**
     * Builds a student row component with name and remove button.
     * Clicking the name navigates to student detail view.
     * The remove button removes the student from the class.
     *
     * @param student The student user entity
     * @param cd The class-student enrollment relationship
     * @return A Node containing the formatted student row
     */
    private Node buildEnrolledStudentRow(User student, ClassDetails cd) {
        HBox row = new HBox(10);
        row.setStyle("""
                -fx-background-color: rgba(0,0,0,0.02);
                -fx-background-radius: 12;
                -fx-padding: 10 10 10 10;
                -fx-alignment: center-left;
                -fx-cursor: hand;
                """);

        VBox left = new VBox(4);
        Label name = new Label(student.getFirstName() + " " + student.getLastName());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: 400; -fx-text-fill: #1F1F39;");
        left.getChildren().addAll(name);
        HBox.setHgrow(left, Priority.ALWAYS);

        Button del = new Button("x");
        del.setStyle("""
                        -fx-background-color: #FFEEEE;
                        -fx-background-radius: 10;
                        -fx-text-fill: #C62828;
                        -fx-font-weight: 900;
                        -fx-cursor: hand;
                """);
        del.setOnAction(e -> {
            classDetailsService.removeStudentFromClass(cd);
            c = classDetailsService.reloadClass(c.getClassId());
            renderEnrolledStudents();
            renderSearchResults(); // update search list availability
        });

        // Click name/row -> view student detail
        row.setOnMouseClicked(e -> {
            AppState.selectedStudent.set(student);
            Navigator.go(AppState.Screen.TEACHER_STUDENT_DETAIL);
        });

        row.getChildren().addAll(left, del);
        return row;
    }

    // ========== STUDENTS SECTION: Search/Add ==========

    /**
     * Handles the show student search button click event.
     * Displays the search interface and focuses the input field.
     */
    @FXML
    private void onShowStudentSearch() {
        // Show the search interface
        studentSearchBox.setVisible(true);
        studentSearchBox.setManaged(true);
        // Focus the search input field
        studentSearchField.requestFocus();
        // Render initial search results (empty list since field is empty)
        renderSearchResults();
    }

    /**
     * Handles the hide student search button click event.
     * Hides the search interface and clears the search field.
     */
    @FXML
    private void onHideStudentSearch() {
        // Hide the search interface
        studentSearchBox.setVisible(false);
        studentSearchBox.setManaged(false);
        // Clear the search field
        studentSearchField.clear();
        // Clear search results
        searchResultsBox.getChildren().clear();
    }

    /**
     * Renders the student search results based on the current search query.
     * Filters available students (not already enrolled) matching the search text.
     * Updates in real-time as the user types.
     */
    private void renderSearchResults() {
        if (!studentSearchBox.isVisible()) return;

        searchResultsBox.getChildren().clear();

        String q = studentSearchField.getText() == null
                ? ""
                : studentSearchField.getText().trim().toLowerCase(Locale.ROOT);

        // hide list without searching
        if (q.isBlank()) {
            return;
        }
        // get all students
        List<User> allStudents = userService.getAllStudents();

        // Exclude already-enrolled students (by email match)
        var enrolledEmails = c.getStudents().stream()
                .map(s -> s.getStudent().getEmail())
                .collect(Collectors.toSet());

        var matches = allStudents.stream()
                .filter(s -> !enrolledEmails.contains(s.getEmail()))
                .filter(s -> q.isBlank()
                        || s.getFirstName().toLowerCase(Locale.ROOT).contains(q)
                        || s.getLastName().toLowerCase().contains(q)
                        || s.getEmail().toLowerCase(Locale.ROOT).contains(q))
                .toList();

        for (User s : matches) {
            searchResultsBox.getChildren().add(buildSearchResultRow(s));
        }
    }

    /**
     * Builds a search result row component with student info and add button.
     * Clicking the add button enrolls the student in the class.
     *
     * @param student The student user entity to display
     * @return A Node containing the formatted search result row
     */
    private Node buildSearchResultRow(User student) {
        HBox row = new HBox(10);
        row.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                -fx-padding: 10 10 10 10;
                -fx-border-color: rgba(31,31,57,0.10);
                -fx-border-radius: 12;
                -fx-alignment: center-left;
                """);

        VBox left = new VBox(3);
        Label name = new Label(student.getFirstName() + " " + student.getLastName());
        name.setStyle("-fx-font-size: 12px; -fx-font-weight: 400; -fx-text-fill: #1F1F39;");
        Label email = new Label(student.getEmail());
        email.setStyle("-fx-font-size: 12px; -fx-font-weight: 400; -fx-text-fill: rgba(0,0,0,0.45);");
        left.getChildren().addAll(name, email);
        HBox.setHgrow(left, Priority.ALWAYS);

        Button add = new Button("+");
        add.setStyle("""
                -fx-background-color: rgba(61,143,239,0.15);
                -fx-text-fill: #3D8FEF;
                -fx-font-weight: 900;
                -fx-background-radius: 10;
                -fx-cursor: hand;
                """);
        add.setOnAction(e -> {
            // Add to class (later: DB insert into enrollment)
            classDetailsService.addStudentToClass(student, c);
            c = classDetailsService.reloadClass(c.getClassId()); // reload class
            renderEnrolledStudents();
            renderSearchResults();
        });

        row.getChildren().addAll(left, add);
        return row;
    }

    // ========== FLASHCARD SETS SECTION ==========

    /**
     * Renders the list of flashcard sets in the class.
     */
    private void renderSets() {
        // Clear any previously displayed sets
        setListBox.getChildren().clear();

        for (FlashcardSet set : c.getFlashcardSets()) {
            try {
                // Load the flashcard set card component from FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/flashcard_set_card.fxml"));
                Node node = loader.load();

                FlashcardSetCardController ctrl = loader.getController();
                ctrl.setSubject(set.getSubject());
                ctrl.setCardCount(set.getTotalCards().size());

                // Teacher mode: hide progress
                ctrl.setShowProgress(false);

                // Handle card click to navigate to set detail
                node.setOnMouseClicked(e -> {
                    AppState.selectedSet.set(set);
                    Navigator.go(AppState.Screen.TEACHER_FLASHCARD_SET_DETAIL);
                });

                setListBox.getChildren().add(node);

            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed to load flashcard_set_card.fxml", ex);
            }
        }

        // Update set count display
        refreshCounts();
    }

    /**
     * Handles the add set button click event.
     * Navigates to the teacher add flashcard set screen.
     */
    @FXML
    private void onAddSet() {
        Navigator.go(AppState.Screen.TEACHER_ADD_SET);
    }
    
}
