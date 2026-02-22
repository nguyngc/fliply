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
import view.Navigator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    //private AppState.ClassItem c;
    private ClassModel c;
    // Demo "directory" of all students in the system (later this comes from DB)
    //private static final List<AppState.StudentItem> ALL_STUDENTS = new ArrayList<>();
    private final ClassDetailsService classDetailsService =  new ClassDetailsService();
    private final UserService userService = new UserService();

    @FXML
    private void initialize() {
//        AppState.seedDemoIfNeeded();
//        seedStudentDirectoryIfNeeded();

        c = AppState.selectedClass.get();
        if (c == null) {
            Navigator.go(AppState.Screen.CLASSES);
            return;
        }

        // Header
        if (headerController != null) {
            headerController.setBackVisible(true);
            headerController.setTitle(c.getClassName());
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASSES));
            headerController.applyVariant(HeaderController.Variant.TEACHER);
        }

        // Initial state: only show "+ Add more students"
        studentSearchBox.setVisible(false);
        studentSearchBox.setManaged(false);

        // Search listener
        studentSearchField.textProperty().addListener((obs, o, n) -> renderSearchResults());

        // Render
        refreshCounts();
        renderEnrolledStudents();
        renderSets();
    }

//    private void seedStudentDirectoryIfNeeded() {
//        if (!ALL_STUDENTS.isEmpty()) return;
//
//        // Demo students pool (later: DB)
//        ALL_STUDENTS.add(new AppState.StudentItem("Student 1", "student1@email.com"));
//        ALL_STUDENTS.add(new AppState.StudentItem("Student 2", "student2@email.com"));
//        ALL_STUDENTS.add(new AppState.StudentItem("Student 3", "student3@email.com"));
//        ALL_STUDENTS.add(new AppState.StudentItem("Student 4", "student4@email.com"));
//        ALL_STUDENTS.add(new AppState.StudentItem("Student 5", "student5@email.com"));
//        ALL_STUDENTS.add(new AppState.StudentItem("Student 6", "student6@email.com"));
//        ALL_STUDENTS.add(new AppState.StudentItem("Anna Nguyen", "anna.nguyen@email.com"));
//        ALL_STUDENTS.add(new AppState.StudentItem("Minh Tran", "minh.tran@email.com"));
//        ALL_STUDENTS.add(new AppState.StudentItem("Linh Pham", "linh.pham@email.com"));
//    }

    private void refreshCounts() {
        if (studentsSectionLabel != null) {
            studentsSectionLabel.setText("Students (" + c.getStudents().size() + ")");
        }
        if (setsSectionLabel != null) {
            setsSectionLabel.setText("Flashcard Sets (" + c.getFlashcardSets().size() + ")");
        }
    }

    // ---------------- Students: Enrolled list ----------------

    private void renderEnrolledStudents() {
        enrolledStudentsBox.getChildren().clear();

        for (ClassDetails cd : c.getStudents()) {
            User student = cd.getStudent();
            Node row = buildEnrolledStudentRow(student, cd);
            enrolledStudentsBox.getChildren().add(row);
        }

        refreshCounts();
    }

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
//        Label progress = new Label("80% Completed"); // demo
//        progress.setStyle("-fx-text-fill: #3D8FEF; -fx-font-weight: 400; -fx-font-size: 12px;");
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

    // ---------------- Students: Search/Add ----------------

    @FXML
    private void onShowStudentSearch() {
        studentSearchBox.setVisible(true);
        studentSearchBox.setManaged(true);
        studentSearchField.requestFocus();
        renderSearchResults();
    }

    @FXML
    private void onHideStudentSearch() {
        studentSearchBox.setVisible(false);
        studentSearchBox.setManaged(false);
        studentSearchField.clear();
        searchResultsBox.getChildren().clear();
    }

    private void renderSearchResults() {
        if (!studentSearchBox.isVisible()) return;

        searchResultsBox.getChildren().clear();

        String q = studentSearchField.getText() == null
                ? ""
                : studentSearchField.getText().trim().toLowerCase(Locale.ROOT);

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

    // ---------------- Flashcard sets ----------------

    private void renderSets() {
        setListBox.getChildren().clear();

        for (FlashcardSet set : c.getFlashcardSets()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/flashcard_set_card.fxml"));
                Node node = loader.load();

                FlashcardSetCardController ctrl = loader.getController();
                ctrl.setSubject(set.getSubject());
                ctrl.setCardCount(set.getTotalCards().size());

                // Teacher mode: hide progress
                ctrl.setShowProgress(false);

                node.setOnMouseClicked(e -> {
                    AppState.selectedSet.set(set);
                    Navigator.go(AppState.Screen.TEACHER_FLASHCARD_SET_DETAIL);
                });

                setListBox.getChildren().add(node);

            } catch (Exception ex) {
                throw new RuntimeException("Failed to load flashcard_set_card.fxml", ex);
            }
        }

        refreshCounts();
    }

    @FXML
    private void onAddSet() {
        Navigator.go(AppState.Screen.TEACHER_ADD_SET);
    }

//    public ClassDetailsService getClassDetailsService() {
//        return classDetailsService;
//    }
//
//    public void setClassDetailsService(ClassDetailsService classDetailsService) {
//        this.classDetailsService = classDetailsService;
//    }
}
