package view;

import javafx.application.Application;
import javafx.stage.Stage;

public class View extends Application {
    public void start(Stage primaryStage) {
        view.Navigator.init(primaryStage);
        view.Navigator.go(model.AppState.Screen.WELCOME);
    }
}
