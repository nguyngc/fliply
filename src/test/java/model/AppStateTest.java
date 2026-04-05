package model;

import javafx.collections.FXCollections;
import model.entity.Quiz;
import model.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppStateTest {

    @BeforeEach
    void setUp() {
        AppState.demoClasses.clear();
        AppState.currentUser.set(null);
        AppState.currentScreen.set(AppState.Screen.WELCOME);
        AppState.activeNav.set(AppState.Screen.WELCOME.nav);
        AppState.navOverride.set(null);
        AppState.setRole(AppState.Role.STUDENT);
    }

    @AfterEach
    void tearDown() {
        AppState.demoClasses.clear();
        AppState.currentUser.set(null);
        AppState.navOverride.set(null);
        AppState.setRole(AppState.Role.STUDENT);
    }

    @Test
    void roleHelpersWork() {
        assertFalse(AppState.isTeacher());

        AppState.setRole(AppState.Role.TEACHER);

        assertEquals(AppState.Role.TEACHER, AppState.getRole());
        assertTrue(AppState.isTeacher());
    }

    @Test
    void seedDemoIfNeeded_populatesOnce() {
        AppState.seedDemoIfNeeded();

        assertEquals(2, AppState.demoClasses.size());
        assertEquals("Class 00001-A", AppState.demoClasses.get(0).getClassCode());
        assertEquals(4, AppState.demoClasses.get(0).getStudentCount());
        assertEquals(2, AppState.demoClasses.get(0).getSetCount());
        assertEquals(3, AppState.demoClasses.get(0).getSets().get(0).getCards().size());

        AppState.seedDemoIfNeeded();
        assertEquals(2, AppState.demoClasses.size());
    }

    @Test
    void screenMetadata_isMappedCorrectly() {
        assertEquals("/screens/class_detail.fxml", AppState.Screen.CLASS_DETAIL.fxml);
        assertEquals(AppState.NavItem.CLASSES, AppState.Screen.CLASS_DETAIL.nav);
        assertEquals(AppState.NavItem.NONE, AppState.Screen.LOGIN.nav);
    }

    @Test
    void nestedDataClassesExposeTheirValues() {
        AppState.FlashcardItem card = new AppState.FlashcardItem("Term", "Definition");
        card.setTerm("New Term");
        card.setDefinition("New Definition");

        assertEquals("New Term", card.getTerm());
        assertEquals("New Definition", card.getDefinition());

        AppState.QuizQuestion question = new AppState.QuizQuestion("CPU", "Central Processing Unit", new String[]{"A", "B", "C", "D"});
        assertEquals("CPU", question.getTerm());
        assertEquals("Central Processing Unit", question.getCorrect());
        assertArrayEquals(new String[]{"A", "B", "C", "D"}, question.getOptions());

        AppState.QuizItem quizItem = new AppState.QuizItem("Quiz 1", 5, 80, FXCollections.observableArrayList());
        assertEquals("Quiz 1", quizItem.getTitle());
        assertEquals(5, quizItem.getTotalQuestions());
        assertEquals(80, quizItem.getProgressPercent());
        assertTrue(quizItem.getQuestions().isEmpty());

        AppState.ClassItem classItem = new AppState.ClassItem("C-001", "Teacher Name");
        assertNotNull(classItem.getId());
        assertEquals("C-001", classItem.getClassCode());
        assertEquals("Teacher Name", classItem.getTeacherName());
    }

    @Test
    void userAndSelectionStateCanBeStored() {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane@example.com");
        user.setPassword("secret");
        user.setRole(0);

        AppState.currentUser.set(user);
        AppState.selectedTerm.set("CPU");
        AppState.selectedDefinition.set("Central Processing Unit");
        AppState.quizPoints.set(7);
        AppState.quizAnswers.put(1, "A");
        AppState.quizCorrectMap.put(1, true);

        assertSame(user, AppState.currentUser.get());
        assertEquals("CPU", AppState.selectedTerm.get());
        assertEquals("Central Processing Unit", AppState.selectedDefinition.get());
        assertEquals(7, AppState.quizPoints.get());
        assertEquals("A", AppState.quizAnswers.get(1));
        assertTrue(AppState.quizCorrectMap.get(1));
    }
}

