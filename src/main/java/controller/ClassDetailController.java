package controller;

import controller.components.HeaderController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import model.AppState;
import model.entity.ClassModel;
import view.Navigator;

public class ClassDetailController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private void initialize() {
        //AppState.seedDemoIfNeeded();
        //AppState.ClassItem c = AppState.selectedClass.get();
        //String classCode = (c != null) ? c.getClassCode() : AppState.selectedClassCode.get();
        //String teacherName = (c != null) ? c.getTeacherName() : AppState.selectedTeacherName.get();
        ClassModel c = AppState.selectedClass.get();
        if (c == null) {
            headerController.setTitle("Class");
            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASSES));
            return;
        }
        // Header
        headerController.setTitle(c.getClassName());
        headerController.setMeta("Teacher: " + c.getTeacher().getFirstName() + " " + c.getTeacher().getLastName());
        headerController.applyVariant( AppState.currentUser.get().isTeacher() ? HeaderController.Variant.TEACHER : HeaderController.Variant.STUDENT );
        headerController.setBackVisible(true);
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASSES)); }

    @FXML
    private void openFlashcardSet(ActionEvent event) {
        Object src = event.getSource();

        String setName = "Flashcard Set";

        if (src instanceof Button btn) {
            Object data = btn.getUserData();
            if (data != null) {
                setName = data.toString();
            }
        }

        // Save for next page (demo)
        AppState.selectedFlashcardSetName.set(setName);

        Navigator.go(AppState.Screen.FLASHCARD_SET);
    }
}
