package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import model.AppState;
import view.Navigator;

public class ClassDetailController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private void initialize() {
        String classCode = AppState.selectedClassCode.get();
        String teacherName = AppState.selectedTeacherName.get();

        if (classCode == null || classCode.isBlank()) classCode = "Class 00001-A";
        if (teacherName == null || teacherName.isBlank()) teacherName = "Teacher’s Name";

        if (headerController != null) {
            headerController.setTitle(classCode);
            headerController.setMeta(teacherName);
            headerController.applyVariant(HeaderController.Variant.STUDENT); // or TEACHER

            headerController.setBackVisible(true);
            headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASSES));
        }
    }

    @FXML
    private void openFlashcardSet(javafx.event.ActionEvent event) {
        Object src = event.getSource();

        String setName = "Flashcard Set";

        if (src instanceof javafx.scene.control.Button btn) {
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
