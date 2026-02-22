package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import model.AppState;
import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.service.FlashcardSetService;
import view.Navigator;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class TeacherAddSetController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private TextField subjectField;
    @FXML
    private Label fileStatusLabel;

    private File selectedFile;
    private int parsedCount = 0;

    private final FlashcardSetService flashcardSetService = new FlashcardSetService();


    @FXML
    private void initialize() {
        headerController.setTitle("New Set of\nFlashcard");
        headerController.setBackVisible(true);
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL));
    }

    @FXML
    private void onUpload() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        selectedFile = chooser.showOpenDialog(null);

        if (selectedFile == null) return;

        try {
            List<String> lines = Files.readAllLines(selectedFile.toPath());
            parsedCount = Math.max(0, lines.size() - 1);

            fileStatusLabel.setText("Loaded: " + selectedFile.getName() + " (" + parsedCount + " cards)");
        } catch (Exception ex) {
            parsedCount = 0;
            fileStatusLabel.setText("Cannot read file.");
        }
    }

    @FXML
    private void onAdd() {
        // get class từ AppState
        ClassModel c = AppState.selectedClass.get();
        if (c == null) return;

        String subject = subjectField.getText() == null ? "" : subjectField.getText().trim();
        if (subject.isBlank()) return;

        // create FlashcardSet
        FlashcardSet set = new FlashcardSet();
        set.setSubject(subject);
        set.setClassModel(c);

        // save DB
        flashcardSetService.save(set);

        Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL);
    }


    @FXML
    private void onCancel() {
        Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL);
    }
}
