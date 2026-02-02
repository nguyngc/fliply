package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class View extends Application {
    public void start(Stage primaryStage) {
        try {
            // Load the welcome screen from FXML resource
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/screen/welcome.fxml"));
            Parent root = loader.load();

            // Set up the scene and stage
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Fliply");
            primaryStage.show();
        } catch (IOException e) {
            // Print stack trace if FXML loading fails
            e.printStackTrace();
        }
    }
}
