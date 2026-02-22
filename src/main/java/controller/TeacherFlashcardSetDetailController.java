package controller;

import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.AppState;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import view.Navigator;

public class TeacherFlashcardSetDetailController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private VBox editorBox;
    @FXML
    private TextField termField;
    @FXML
    private TextArea definitionArea;

    @FXML
    private VBox cardsBox;
    @FXML
    private Button addMoreBtn;

    private FlashcardSet set;


    /**
     * null = adding new, otherwise editing existing row
     */
    private Flashcard editingRow = null;

    @FXML
    private void initialize() {
        //AppState.seedDemoIfNeeded();

        set = AppState.selectedSet.get();
        if (set == null) {
            Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL);
            return;
        }

        // Header
        headerController.setBackVisible(true);
        headerController.setTitle(set.getSubject());
        headerController.setSubtitle("Total: " + set.getCards().size());
        headerController.setOnBack(() -> Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL));
        headerController.applyVariant(HeaderController.Variant.TEACHER);

        AppState.navOverride.set(AppState.NavItem.CLASSES);

        hideEditor();
        renderList();
        updateHeaderTotal();
    }

    // ---------------- UI state ----------------

    private void showEditorForAdd() {
        editingRow = null;
        termField.clear();
        definitionArea.clear();
        editorBox.setVisible(true);
        editorBox.setManaged(true);
        termField.requestFocus();
    }

    private void showEditorForEdit(Flashcard row) {
        editingRow = row;
        termField.setText(row.getTerm());
        definitionArea.setText(row.getDefinition());
        editorBox.setVisible(true);
        editorBox.setManaged(true);
        termField.requestFocus();
    }

    private void hideEditor() {
        editorBox.setVisible(false);
        editorBox.setManaged(false);
        editingRow = null;
    }

    private void updateHeaderTotal() {
        headerController.setSubtitle("Total: " + set.getCards().size());
    }

    // ---------------- Rendering ----------------

    private void renderList() {
        cardsBox.getChildren().clear();

        for (Flashcard row : set.getCards()) {
            cardsBox.getChildren().add(buildRowCard(row));
        }
    }

    private VBox buildRowCard(Flashcard row) {
        VBox card = new VBox(6);
        card.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 18;
                    -fx-padding: 14 14 14 14;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 14, 0.2, 0, 6);
                """);

        HBox top = new HBox(10);
        VBox left = new VBox(4);
        HBox.setHgrow(left, Priority.ALWAYS);

        Label term = new Label(row.getTerm());
        term.setStyle("-fx-font-size: 16px; -fx-font-weight: 500; -fx-text-fill: #1F1F39;");

        Label def = new Label(row.getDefinition());
        def.setStyle("-fx-font-size: 14px; -fx-font-weight: 400; -fx-text-fill: rgba(0,0,0,0.45);");

        left.getChildren().addAll(term, def);

        ImageView editIcon = new ImageView(
                new Image(getClass().getResourceAsStream("/images/edit_btn.png"))
        );
        editIcon.setFitWidth(20);
        editIcon.setFitHeight(20);

        Button editBtn = new Button();
        editBtn.setGraphic(editIcon);
        editBtn.setStyle("""
                    -fx-background-color: #EEF4FF;
                    -fx-background-radius: 20;
                    -fx-padding: 6;
                    -fx-cursor: hand;
                """);

        editBtn.setOnAction(e -> showEditorForEdit(row));

        ImageView deleteIcon = new ImageView(
                new Image(getClass().getResourceAsStream("/images/delete_btn.png"))
        );
        deleteIcon.setFitWidth(20);
        deleteIcon.setFitHeight(20);

        Button deleteBtn = new Button();
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setStyle("""
                    -fx-background-color: #FFEEEE;
                    -fx-background-radius: 20;
                    -fx-padding: 6;
                    -fx-cursor: hand;
                """);
        deleteBtn.setOnAction(e -> {
            set.getCards().remove(row);
            hideEditor();
            renderList();
            updateHeaderTotal();
        });

        top.getChildren().addAll(left, editBtn, deleteBtn);

        card.getChildren().add(top);
        return card;
    }

    // ---------------- Actions ----------------

    @FXML
    private void onAddMore() {
        showEditorForAdd();
    }

    @FXML
    private void onCancel() {
        hideEditor();
    }

    @FXML
    private void onSave() {
        String term = termField.getText() == null ? "" : termField.getText().trim();
        String def = definitionArea.getText() == null ? "" : definitionArea.getText().trim();
        if (term.isBlank() || def.isBlank()) return;

        if (editingRow == null) {
            // add new
            Flashcard newCard = new Flashcard();
            newCard.setTerm(term);
            newCard.setDefinition(def);
            newCard.setFlashcardSet(set);
            set.getCards().add(newCard);
        } else {
            // update existing
            editingRow.setTerm(term);
            editingRow.setDefinition(def);
        }

        hideEditor();
        renderList();
        updateHeaderTotal();
    }
}
