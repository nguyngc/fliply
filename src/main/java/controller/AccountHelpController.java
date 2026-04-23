package controller;

import controller.components.AccountHeaderSupport;
import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import model.AppState;
import util.I18n;
import util.LocaleManager;
import view.Navigator;

import java.util.ResourceBundle;

/**
 * Controller for displaying the help/support information screen.
 * Provides users with assistance and guidance on using the application.
 */
public class AccountHelpController {

    // UI Components injected from FXML
    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;
    @FXML
    private ResourceBundle resources;
    @FXML
    private Label quickStartTitleLabel;
    @FXML
    private Label quickStartBodyLabel;
    @FXML
    private Label roleGuideTitleLabel;
    @FXML
    private Label roleGuideBodyLabel;
    @FXML
    private Label commonTasksTitleLabel;
    @FXML
    private Label commonTasksBodyLabel;
    @FXML
    private Label faqTitleLabel;
    @FXML
    private Label faqBodyLabel;

    private ResourceBundle effectiveResources;

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up the header, configures navigation, and applies styling based on user role.
     */
    @FXML
    private void initialize() {
        effectiveResources = resources != null
                ? resources
                : ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        AccountHeaderSupport.configure(headerController, resources, "help.header.title", () -> Navigator.go(AppState.Screen.ACCOUNT));
        populateHelpContent();

        // Set the active navigation item to ACCOUNT
        AppState.navOverride.set(AppState.NavItem.ACCOUNT);
    }

    /**
     * Populates the help content sections with role-specific guidance and common tasks.
     * Uses resource bundle keys to support internationalization and role-based messaging.
     */
    private void populateHelpContent() {
        boolean teacher = AppState.isTeacher();

        setText(quickStartTitleLabel, message("help.quickStart.title", "Quick Start"));
        setText(quickStartBodyLabel, message("help.quickStart.body",
                "Use the bottom navigation to move between the main screens. Open Account at any time to update your profile, switch language, or change your password."));

        setText(roleGuideTitleLabel, message(roleKey("help.roleGuide.title", teacher),
                teacher ? "Teacher Guide" : "Student Guide"));
        setText(roleGuideBodyLabel, message(roleKey("help.roleGuide.body", teacher),
                teacher
                        ? "Teachers mainly work in Classes. Create a class, open it to review students, and add flashcard sets so the class has study material ready."
                        : "Students mainly move between Classes, Flashcards, and Quizzes. Open a class to review available sets, study your own cards in Flashcards, and use Quizzes for practice."));

        setText(commonTasksTitleLabel, message("help.commonTasks.title", "Common Tasks"));
        setText(commonTasksBodyLabel, message(roleKey("help.commonTasks.body", teacher),
                teacher
                        ? "1. Create a class: Go to Classes and choose + Add more classes.\n\n"
                        + "2. Add learning material: Open a class, then add a flashcard set.\n\n"
                        + "3. Manage account settings: Open Account to edit your profile, language, or password."
                        : "1. Open your classes: Go to Classes to review the classes you joined.\n\n"
                        + "2. Study flashcards: Open Flashcards to review your cards or add a new one.\n\n"
                        + "3. Practice with quizzes: Go to Quizzes, enter a question count, and start the generated quiz."));

        setText(faqTitleLabel, message("help.faq.title", "FAQ"));
        setText(faqBodyLabel, message(roleKey("help.faq.body", teacher),
                teacher
                        ? "Q: Where do I add learning material?\n"
                        + "A: Open a class first, then add or open its flashcard sets.\n\n"
                        + "Q: Why do I not see Flashcards or Quizzes in the bottom menu?\n"
                        + "A: Teacher accounts focus on Home, Classes, and Account, so student-only study tools are hidden.\n\n"
                        + "Q: How do I change language or password?\n"
                        + "A: Open Account, then choose Language or Change Password."
                        : "Q: Why can I not generate a quiz?\n"
                        + "A: Quiz generation needs available flashcards. Add flashcards first or open a class that already has study material.\n\n"
                        + "Q: Where can I find my latest activity?\n"
                        + "A: Home shows your latest class and most recent quiz when they are available.\n\n"
                        + "Q: How do I change language or password?\n"
                        + "A: Open Account, then choose Language or Change Password."));
    }

    /**
     * Constructs a resource key for role-specific messages.
     *
     * @param baseKey the base key for the message
     * @param teacher  whether the user is a teacher or student
     * @return the full resource key for the message
     */
    private String roleKey(String baseKey, boolean teacher) {
        return baseKey + (teacher ? ".teacher" : ".student");
    }

    /**
     * Retrieves a localized message from the resource bundle, with a fallback if the key is not found.
     *
     * @param key      the resource key for the message
     * @param fallback  the fallback text to use if the key is not found
     * @return the localized message or the fallback text
     */
    private String message(String key, String fallback) {
        return I18n.message(effectiveResources, key, fallback);
    }

    /**
     * Safely sets the text of a label if it is not null.
     *
     * @param label the label to update
     * @param text  the text to set on the label
     */
    private void setText(Label label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }
}
