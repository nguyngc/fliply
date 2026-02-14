package model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global app state used for demo navigation + passing data between screens.
 */
public final class AppState {

    // ---------- Current User -----------
    public static final SimpleStringProperty currentFirstName = new SimpleStringProperty("Student");
    public static final SimpleStringProperty currentLastName = new SimpleStringProperty("User");
    public static final SimpleStringProperty currentEmail = new SimpleStringProperty("student@email.com");
    public static final SimpleStringProperty demoPassword = new SimpleStringProperty("123456");

    // ---------- Navigation / session ----------
    public static final ObjectProperty<Screen> currentScreen = new SimpleObjectProperty<>(Screen.WELCOME);
    public static final ObjectProperty<NavItem> activeNav = new SimpleObjectProperty<>(Screen.WELCOME.nav);
    public static final ObjectProperty<NavItem> navOverride = new SimpleObjectProperty<>(null);

    // ---------- Header  ----------
    public static final StringProperty detailHeaderTitle = new SimpleStringProperty("");
    public static final StringProperty detailHeaderSubtitle = new SimpleStringProperty("");
    public static final BooleanProperty isFromFlashcardSet = new SimpleBooleanProperty(false);

    // ---------- Flashcards  ----------
    public static final StringProperty selectedFlashcardSetName = new SimpleStringProperty("");
    public static final ObservableList<FlashcardItem> myFlashcards = FXCollections.observableArrayList();
    public static final ObjectProperty<FormMode> flashcardFormMode = new SimpleObjectProperty<>(FormMode.ADD);
    public static final IntegerProperty editingIndex = new SimpleIntegerProperty(-1);
    public static final ObservableList<FlashcardItem> currentDetailList = FXCollections.observableArrayList();
    public static final IntegerProperty currentDetailIndex = new SimpleIntegerProperty(0);
    public static final StringProperty selectedTerm = new SimpleStringProperty("");
    public static final StringProperty selectedDefinition = new SimpleStringProperty("");

    // ---------- Quizzes ------------
    public static final ObservableList<QuizItem> myQuizzes = FXCollections.observableArrayList();
    public static final ObjectProperty<QuizItem> selectedQuiz = new SimpleObjectProperty<>(null);
    public static final IntegerProperty quizQuestionIndex = new SimpleIntegerProperty(0);
    public static final IntegerProperty quizPoints = new SimpleIntegerProperty(0);
    public static final Map<Integer, String> quizAnswers = new HashMap<>();
    public static final Map<Integer, Boolean> quizCorrectMap = new HashMap<>();

    // ---------- Classes ----------
    public static final ObjectProperty<Role> role = new SimpleObjectProperty<>(Role.STUDENT);
    public static final ObservableList<ClassItem> demoClasses = FXCollections.observableArrayList();
    public static final StringProperty selectedClassCode = new SimpleStringProperty("");
    public static final ObjectProperty<ClassItem> selectedClass = new SimpleObjectProperty<>(null);
    public static final ObjectProperty<StudentItem> selectedStudent = new SimpleObjectProperty<>(null);
    public static final StringProperty selectedTeacherName = new SimpleStringProperty("");
    public static final ObjectProperty<FlashcardSetItem> selectedSet = new SimpleObjectProperty<>(null);

    // -------- ENUM -----------
    public enum FormMode {ADD, EDIT}
    public enum Role {STUDENT, TEACHER}
    public enum NavItem {HOME, CLASSES, FLASHCARDS, QUIZZES, ACCOUNT, NONE}

    private AppState() {
    }

    public enum Screen {
        WELCOME("/screens/welcome.fxml", NavItem.NONE),
        LOGIN("/screens/login.fxml", NavItem.NONE),
        REGISTER("/screens/register.fxml", NavItem.NONE),

        HOME("/screens/home.fxml", NavItem.HOME),

        CLASSES("/screens/classes.fxml", NavItem.CLASSES),
        CLASS_DETAIL("/screens/class_detail.fxml", NavItem.CLASSES),
        FLASHCARD_SET("/screens/flashcard_set.fxml", NavItem.CLASSES),
        FLASHCARD_DETAIL("/screens/flashcard_detail.fxml", NavItem.CLASSES),

        FLASHCARDS("/screens/flashcards.fxml", NavItem.FLASHCARDS),
        FLASHCARD_FORM("/screens/flashcard_form.fxml", NavItem.FLASHCARDS),

        QUIZZES("/screens/quizzes.fxml", NavItem.QUIZZES),
        QUIZ_FORM("/screens/quiz_form.fxml", NavItem.QUIZZES),
        QUIZ_DETAIL("/screens/quiz_detail.fxml", NavItem.QUIZZES),
        QUIZ_RESULT("/screens/quiz_result.fxml", NavItem.QUIZZES),

        TEACHER_ADD_CLASS("/screens/teacher_add_class.fxml", NavItem.CLASSES),
        TEACHER_ADD_SET("/screens/teacher_add_set.fxml", NavItem.CLASSES),
        TEACHER_STUDENT_DETAIL("/screens/teacher_student_detail.fxml", NavItem.CLASSES),
        TEACHER_CLASS_DETAIL("/screens/teacher_class_detail.fxml", NavItem.CLASSES),
        TEACHER_FLASHCARD_SET_DETAIL("/screens/teacher_flashcard_set_detail.fxml", NavItem.CLASSES),

        ACCOUNT("/screens/account.fxml", NavItem.ACCOUNT),
        ACCOUNT_EDIT("/screens/account_edit.fxml", NavItem.ACCOUNT),
        ACCOUNT_PASSWORD("/screens/account_password.fxml", NavItem.ACCOUNT),
        ACCOUNT_HELP("/screens/account_help.fxml", NavItem.ACCOUNT),
        ACCOUNT_ABOUT("/screens/account_about.fxml", NavItem.ACCOUNT);


        public final String fxml;
        public final NavItem nav;

        Screen(String fxml, NavItem nav) {
            this.fxml = fxml;
            this.nav = nav;
        }
    }

    // ---------- Role ----------
    public static Role getRole() {
        return role.get();
    }

    public static void setRole(Role r) {
        role.set(r);
    }

    public static boolean isTeacher() {
        return role.get() == Role.TEACHER;
    }

    // ---------- Data classes ----------

    public static class FlashcardItem {
        private String term;
        private String definition;

        public FlashcardItem(String term, String definition) {
            this.term = term;
            this.definition = definition;
        }

        public void setTerm(String term) {
            this.term = term;
        }

        public String getTerm() {
            return term;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        public String getDefinition() {
            return definition;
        }
    }

    public static class QuizItem {
        private final String title;
        private final int totalQuestions;
        private final int progressPercent; // for list display only
        private final ObservableList<QuizQuestion> questions;

        public QuizItem(String title, int totalQuestions, int progressPercent, ObservableList<QuizQuestion> questions) {
            this.title = title;
            this.totalQuestions = totalQuestions;
            this.progressPercent = progressPercent;
            this.questions = questions;
        }

        public String getTitle() {
            return title;
        }

        public int getTotalQuestions() {
            return totalQuestions;
        }

        public int getProgressPercent() {
            return progressPercent;
        }

        public ObservableList<QuizQuestion> getQuestions() {
            return questions;
        }
    }

    public static class QuizQuestion {
        private final String term;
        private final String correct;
        private final String[] options; // length 4

        public QuizQuestion(String term, String correct, String[] options) {
            this.term = term;
            this.correct = correct;
            this.options = options;
        }

        public String getTerm() {
            return term;
        }

        public String getCorrect() {
            return correct;
        }

        public String[] getOptions() {
            return options;
        }
    }

    // ---------- Demo Classes (Teacher & Student share) ----------
    public static class ClassItem {
        private final String id = UUID.randomUUID().toString();
        private final String classCode;
        private final String teacherName;

        private final ObservableList<StudentItem> students = FXCollections.observableArrayList();
        private final ObservableList<FlashcardSetItem> sets = FXCollections.observableArrayList();
        private final ObservableList<FlashcardItem> cards = FXCollections.observableArrayList();

        public ClassItem(String classCode, String teacherName) {
            this.classCode = classCode;
            this.teacherName = teacherName;
        }

        public String getId() { return id; }
        public String getClassCode() { return classCode; }
        public String getTeacherName() { return teacherName; }
        public ObservableList<StudentItem> getStudents() { return students; }
        public ObservableList<FlashcardSetItem> getSets() { return sets; }

        public int getStudentCount() { return students.size(); }
        public int getSetCount() { return sets.size(); }
    }

    public static class StudentItem {
        private final String id = UUID.randomUUID().toString();
        private final String name;
        private final String email;

        public StudentItem(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    public static class FlashcardSetItem {
        private final String id = UUID.randomUUID().toString();
        private final String subject;
        private final int totalCards;
        private final int progressPercent; // demo status
        private final ObservableList<FlashcardItem> cards = FXCollections.observableArrayList();

        public FlashcardSetItem(String subject, int totalCards, int progressPercent) {
            this.subject = subject;
            this.totalCards = totalCards;
            this.progressPercent = progressPercent;
        }

        public String getId() { return id; }
        public String getSubject() { return subject; }
        public int getTotalCards() { return totalCards; }
        public int getProgressPercent() { return progressPercent; }
        public ObservableList<FlashcardItem> getCards() { return cards; }
    }

    // ---------- Demo seed ----------
    public static void seedDemoIfNeeded() {
        if (!demoClasses.isEmpty()) return;

        ClassItem c1 = new ClassItem("Class 00001-A", "Teacher's Name");
        c1.getStudents().addAll(
                new StudentItem("Student 1", "student1@email.com"),
                new StudentItem("Student 2", "student2@email.com"),
                new StudentItem("Student 3", "student3@email.com"),
                new StudentItem("Student 4", "student4@email.com")
        );
        c1.getSets().addAll(
                new FlashcardSetItem("Flashcard Set 1", 20, 100),
                new FlashcardSetItem("Flashcard Set 2", 20, 60)
        );
        c1.getSets().get(0).getCards().addAll(
                new FlashcardItem("CPU", "Central Processing Unit"),
                new FlashcardItem("RAM", "Random Access Memory"),
                new FlashcardItem("HTTP", "HyperText Transfer Protocol")
        );
        c1.getSets().get(1).getCards().addAll(
                new FlashcardItem("OOP", "Object-Oriented Programming"),
                new FlashcardItem("API", "Application Programming Interface")
        );

        ClassItem c2 = new ClassItem("Class 00002-B", "Teacher's Name");
        c2.getStudents().addAll(
                new StudentItem("Student 5", "student5@email.com"),
                new StudentItem("Student 6", "student6@email.com")
        );
        c2.getSets().addAll(
                new FlashcardSetItem("Flashcard Set 3", 20, 80)
        );

        demoClasses.addAll(c1, c2);
    }
}
