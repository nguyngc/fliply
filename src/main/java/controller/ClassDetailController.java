package controller;

import controller.components.HeaderController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.service.FlashcardSetService;
import view.Navigator;

import java.util.List;

public class ClassDetailController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;
    @FXML
    private VBox flashcardSetListBox;

    private final FlashcardSetService flashcardSetService = new FlashcardSetService();

    @FXML
    private void initialize() {

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
        headerController.applyVariant(
                AppState.currentUser.get().isTeacher()
                        ? HeaderController.Variant.TEACHER
                        : HeaderController.Variant.STUDENT
        );
        headerController.setBackVisible(true);
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.CLASSES));

        // Load Flashcard Sets
        List<FlashcardSet> sets = flashcardSetService.getSetsByClass(c.getClassId());
        renderSets(sets);
    }

    private void renderSets(List<FlashcardSet> sets) {
        flashcardSetListBox.getChildren().clear();

        for (FlashcardSet set : sets) {
            Button btn = new Button(set.getSubject());
            btn.setPrefHeight(48);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 14;
                    -fx-font-size: 16px;
                    -fx-font-weight: 600;
                    -fx-cursor: hand;
                    """);

            btn.setOnAction(e -> {
                AppState.selectedFlashcardSet.set(set);
                Navigator.go(AppState.Screen.FLASHCARD_SET);
            });

            flashcardSetListBox.getChildren().add(btn);
        }
    }
}
