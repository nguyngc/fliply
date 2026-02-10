package model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class AppState {

    public static final ObjectProperty<Role> role = new SimpleObjectProperty<>(Role.STUDENT);
    public static final ObjectProperty<Screen> currentScreen = new SimpleObjectProperty<>(Screen.WELCOME);
    public static final ObjectProperty<NavItem> activeNav = new SimpleObjectProperty<>(Screen.WELCOME.nav);

    // DEMO selected info (used to pass data between screens)
    public static final javafx.beans.property.StringProperty selectedClassCode = new javafx.beans.property.SimpleStringProperty("");
    public static final javafx.beans.property.StringProperty selectedTeacherName = new javafx.beans.property.SimpleStringProperty("");
    public static final javafx.beans.property.StringProperty selectedFlashcardSetName = new javafx.beans.property.SimpleStringProperty("");
    public static final javafx.beans.property.StringProperty selectedTerm = new javafx.beans.property.SimpleStringProperty("");
    public static final javafx.beans.property.StringProperty selectedDefinition = new javafx.beans.property.SimpleStringProperty("");


    private AppState() {
    }

    public enum Role {STUDENT, TEACHER}

    public enum NavItem {HOME, CLASSES, FLASHCARDS, QUIZZES, ACCOUNT, NONE}

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

        QUIZZES("/screens/quizzes.fxml", NavItem.QUIZZES),

        ACCOUNT("/screens/account.fxml", NavItem.ACCOUNT);

        public final String fxml;
        public final NavItem nav;

        Screen(String fxml, NavItem nav) {
            this.fxml = fxml;
            this.nav = nav;
        }
    }
}
