package util;

import javafx.scene.control.Alert;

import java.util.function.Function;

/**
 * Small wrapper for JavaFX alerts with an injectable factory for tests.
 */
public final class Dialogs {
    static Function<Alert.AlertType, Alert> alertFactory = Alert::new;

    private Dialogs() {
    }

    // Package-private test hook.
    static void setAlertFactory(Function<Alert.AlertType, Alert> factory) {
        alertFactory = factory == null ? Alert::new : factory;
    }

    // Package-private test hook.
    static void resetAlertFactory() {
        alertFactory = Alert::new;
    }

    /**
     * Shows a blocking alert with no header text.
     */
    public static void show(Alert.AlertType type, String title, String content) {
        Alert alert = alertFactory.apply(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
