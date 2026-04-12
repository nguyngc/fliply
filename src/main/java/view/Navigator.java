package view;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.AppState;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public final class Navigator {
    private static final String[] RTL_LANGUAGES = {"ar", "fa", "ur", "he"};

    private static Stage stage;

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

            // Set up the scene and stage
            Scene scene = new Scene(root, 375, 750);
            applyLocaleFont(scene);
            applyTextDirection(scene);
            stage.setScene(scene);
            stage.setTitle("Fliply");
            stage.setResizable(false);
            stage.show();

            AppState.NavItem nav = AppState.navOverride.get() != null
                    ? AppState.navOverride.get()
                    : screen.nav;
            AppState.activeNav.set(nav);
            AppState.navOverride.set(null);
            AppState.currentScreen.set(screen);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + screen.fxml, e);
        }
    }

    private static void applyLocaleFont(Scene scene) {
        if (scene == null) return;
        if (!"lo".equals(util.LocaleManager.getLocale().getLanguage())) return;

        String mainCss = Navigator.class.getResource("/styles/style.css").toExternalForm();
        if (!scene.getStylesheets().contains(mainCss)) {
            scene.getStylesheets().add(mainCss);
        }

        Parent root = scene.getRoot();
        if (root == null) return;
        if (!root.getStyleClass().contains("locale-lo")) {
            root.getStyleClass().add("locale-lo");
        }
    }

    private static void applyTextDirection(Scene scene) {
        if (scene == null) return;

        Locale locale = util.LocaleManager.getLocale();
        boolean isRtl = isRtlLanguage(locale);

        Platform.runLater(() -> {
            Parent root = scene.getRoot();
            if (root != null) {
                root.setNodeOrientation(
                        isRtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT
                );
            }
        });
    }

    private static boolean isRtlLanguage(Locale locale) {
        if (locale == null) return false;

        String language = locale.getLanguage();
        for (String rtlLanguage : RTL_LANGUAGES) {
            if (rtlLanguage.equals(language)) {
                return true;
            }
        }
        return false;
    }

    public static void reloadCurrent() {
        go(AppState.currentScreen.get());
    }
    
}
