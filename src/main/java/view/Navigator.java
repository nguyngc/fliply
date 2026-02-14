package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.AppState;

import java.io.IOException;

public final class Navigator {

    private static Stage stage;

    private Navigator() {
    }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void go(AppState.Screen screen) {
        try {
            // Load the screen from FXML resource
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(screen.fxml));
            Parent root = loader.load();

            // Set up the scene and stage
            Scene scene = new Scene(root, 375, 750);
            stage.setScene(scene);
            stage.setTitle("Fliply");
            stage.setResizable(false);
            stage.show();

            // set nav highlight (allow override)
            AppState.NavItem nav = AppState.navOverride.get() != null ? AppState.navOverride.get() : screen.nav;
            AppState.activeNav.set(nav);
            AppState.navOverride.set(null); // clear after using
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + screen.fxml, e);
        }
    }
}
