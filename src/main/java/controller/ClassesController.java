package controller;

import controller.components.ClassCardController;
import controller.components.HeaderController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AppState;
import view.Navigator;

public class ClassesController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private VBox classListBox;

    @FXML
    private void initialize() {
        AppState.seedDemoIfNeeded();

        if (headerController != null) {
            headerController.setTitle("My Classes");
            headerController.setSubtitle("Total: " + AppState.demoClasses.size());
            headerController.applyVariant(AppState.isTeacher()
                    ? HeaderController.Variant.TEACHER
                    : HeaderController.Variant.STUDENT
            );
        }

        render();
    }

    private void render() {
        classListBox.getChildren().clear();

        boolean isTeacher = AppState.isTeacher();

        for (AppState.ClassItem c : AppState.demoClasses) {
            classListBox.getChildren().add(buildClassCard(c, isTeacher));
        }

        if (isTeacher) {
            classListBox.getChildren().add(buildAddMoreClassTile());
        }
    }

    private Node buildClassCard(AppState.ClassItem c, boolean isTeacher) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/class_card.fxml"));
            Node node = loader.load();
            ClassCardController ctrl = loader.getController();

            double progress = 0.80; // demo
            if (isTeacher) {
                ctrl.setTeacherCard(c.getClassCode(), c.getStudentCount(), c.getSetCount(), progress);
            } else {
                ctrl.setStudentCard(c.getClassCode(), c.getTeacherName(), progress);
            }

            node.setOnMouseClicked(e -> {
                AppState.selectedClass.set(c);

                if (AppState.selectedClassCode != null) AppState.selectedClassCode.set(c.getClassCode());
                if (AppState.selectedTeacherName != null) AppState.selectedTeacherName.set(c.getTeacherName());

                if (isTeacher) Navigator.go(AppState.Screen.TEACHER_CLASS_DETAIL);
                else Navigator.go(AppState.Screen.CLASS_DETAIL);
            });

            return node;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load class_card.fxml", ex);
        }
    }

    private Node buildAddMoreClassTile() {
        StackPane tile = new StackPane();
        tile.setPrefHeight(60);
        tile.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 18;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 14, 0.2, 0, 6);
                -fx-cursor: hand;
                """);

        Label text = new Label("+ Add more class");
        text.setStyle("-fx-text-fill: #3D8FEF; -fx-font-size: 16px; -fx-font-weight: 600;");
        tile.getChildren().add(text);

        tile.setOnMouseClicked(e -> Navigator.go(AppState.Screen.TEACHER_ADD_CLASS));
        return tile;
    }
}
