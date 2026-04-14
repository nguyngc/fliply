package view;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The View class serves as the entry point for the JavaFX application.
 * It initializes the Navigator and sets the initial screen to the welcome screen.
 */
public class View extends Application {
    /**
     * Initializes the JavaFX application by setting up the Navigator and displaying the welcome screen.
     * @param primaryStage the primary stage for this application, onto which the application scene can be set
     */
    public void start(Stage primaryStage) {
        view.Navigator.init(primaryStage);
        view.Navigator.go(model.AppState.Screen.WELCOME);
    }
}
