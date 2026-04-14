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

/**
 * Centralized navigation helper for loading screens and applying locale-specific UI setup.
 */
public final class Navigator {
    // Languages that should render right-to-left.
    private static final String[] RTL_LANGUAGES = {"ar", "fa", "ur", "he"};

    private static Stage stage;

    private Navigator() {
    }

    /**
     * Stores the primary stage used for all screen navigation.
     *
     * @param primaryStage application primary stage
     */
    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    /**
     * Loads and displays the target screen, then updates navigation state.
     *
     * @param screen target screen to load
     * @throws IllegalStateException if the FXML cannot be loaded
     */
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

            // Apply one-time nav override when present; otherwise use the screen default.
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

    /**
     * Applies Lao-specific styling hooks when the active locale is Lao.
     *
     * @param scene scene to update
     */
    private static void applyLocaleFont(Scene scene) {
        if (scene == null) return;
        // Lao uses a dedicated style hook for consistent glyph rendering.
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

    /**
     * Applies RTL/LTR orientation based on the active locale.
     *
     * @param scene scene to update
     */
    private static void applyTextDirection(Scene scene) {
        if (scene == null) return;

        Locale locale = util.LocaleManager.getLocale();
        boolean isRtl = isRtlLanguage(locale);

        // Apply after root attachment to avoid orientation timing issues.
        Platform.runLater(() -> {
            Parent root = scene.getRoot();
            if (root != null) {
                root.setNodeOrientation(
                        isRtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT
                );
            }
        });
    }

    /**
     * Checks whether a locale should be displayed right-to-left.
     *
     * @param locale locale to evaluate
     * @return true if the locale language is configured as RTL
     */
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

    /**
     * Reloads the currently active screen using current locale/resources.
     */
    public static void reloadCurrent() {
        // Recreate the active screen using the current locale/resources.
        go(AppState.currentScreen.get());
    }

}
