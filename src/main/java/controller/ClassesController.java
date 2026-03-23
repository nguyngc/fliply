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
import model.entity.ClassModel;
import model.entity.User;
import model.service.ClassDetailsService;
import model.service.StudyService;
import util.LocaleManager;
import view.Navigator;

import java.util.List;
import java.util.ResourceBundle;

public class ClassesController {

    @FXML
    private Parent header;
    @FXML
    private HeaderController headerController;

    @FXML
    private VBox classListBox;

    private final ClassDetailsService  classDetailsService = new  ClassDetailsService();

    @FXML
    private void initialize() {
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());
        //AppState.seedDemoIfNeeded();
        User user = AppState.currentUser.get();
        boolean isTeacher = user.isTeacher();

        // Header
        headerController.setTitle(rb.getString("class.title"));
        String key = isTeacher ? "class.subtitle.teacher" : "class.subtitle.student";
        headerController.setSubtitle(rb.getString(key));

        headerController.applyVariant(isTeacher
                ? HeaderController.Variant.TEACHER
                : HeaderController.Variant.STUDENT
        );

    render();
    }

    private void render() {
        classListBox.getChildren().clear();
        User user = AppState.currentUser.get();
        boolean isTeacher = user.isTeacher();

        // Load classes from DB
        List<ClassModel> classes = classDetailsService.getClassesOfUser(user.getUserId());
        for (ClassModel c : classes) {
            classListBox.getChildren().add(buildClassCard(c, isTeacher)); }
        // Teacher can add more classes
        if (isTeacher) {
            classListBox.getChildren().add(buildAddMoreClassTile());
        }
    }

    private Node buildClassCard(ClassModel c, boolean isTeacher) {
        ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/class_card.fxml"));
            Node node = loader.load();
            ClassCardController ctrl = loader.getController();

            //double progress = 0.80; // demo
            StudyService studyService = new StudyService();
            double progress = studyService.getClassProgress(AppState.currentUser.get(), c);

            if (isTeacher) {
                ctrl.setTeacherCard(
                        c.getClassName(),
                        c.getStudents().size(),
                        c.getFlashcardSets().size(),
                        progress
                );
            } else {
                ctrl.setStudentCard(
                        c.getClassName(),
                        c.getTeacher().getFirstName() + " " + c.getTeacher().getLastName(),
                        progress
                );
            }


            node.setOnMouseClicked(e -> {
                AppState.selectedClass.set(c);
                Navigator.go(isTeacher ? AppState.Screen.TEACHER_CLASS_DETAIL : AppState.Screen.CLASS_DETAIL);
            });

            return node;
        } catch (Exception ex) {
            throw new RuntimeException(rb.getString("class.error"), ex);
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
