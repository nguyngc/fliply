package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.AppState;

import java.io.IOException;
import java.util.ResourceBundle;

public final class Navigator {

    private static Stage stage;
    private static boolean laoFontLoaded = false;

    private Navigator() {
    }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void go(AppState.Screen screen) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Navigator.class.getResource(screen.fxml),
                    ResourceBundle.getBundle("Messages", util.LocaleManager.getLocale())
            );
            Parent root = loader.load();

            if (util.LocaleManager.getLocale().getLanguage().equals("lo")) {
                if (!laoFontLoaded) {
                    Font.loadFont(
                            Navigator.class.getResourceAsStream("/fonts/NotoSansLao-Regular.ttf"),
                            14
                    );
                    laoFontLoaded = true;
                }
                root.setStyle("-fx-font-family: 'Noto Sans Lao';");
            }

            Scene scene = new Scene(root, 375, 750);
            stage.setScene(scene);
            stage.setTitle("Fliply");
            stage.setResizable(false);
            stage.show();

            AppState.NavItem nav = AppState.navOverride.get() != null
                    ? AppState.navOverride.get()
                    : screen.nav;
            AppState.activeNav.set(nav);
            AppState.navOverride.set(null);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + screen.fxml, e);
        }
    }

    public static void reloadCurrent() {
        go(AppState.currentScreen.get());
    }
}