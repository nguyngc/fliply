package controller.components;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Utility that keeps password text and eye-icon state in sync between masked and plain text fields.
 */
public final class PasswordVisibilitySupport {
    private PasswordVisibilitySupport() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Sets the initial hidden state (masked field active, plain text field hidden).
     */
    public static void initializeHidden(TextField visibleField, ImageView eyeIcon, Image eyeClosed) {
        if (visibleField != null) {
            visibleField.setVisible(false);
            visibleField.setManaged(false);
        }
        if (eyeIcon != null) {
            eyeIcon.setImage(eyeClosed);
        }
    }

    /**
     * Toggles visibility while copying text between controls so user input is never lost.
     */
    public static void apply(boolean visible,
                             PasswordField maskedField,
                             TextField visibleField,
                             ImageView eyeIcon,
                             Image eyeOpen,
                             Image eyeClosed) {
        if (visible) {
            visibleField.setText(maskedField.getText());
            visibleField.setVisible(true);
            visibleField.setManaged(true);

            maskedField.setVisible(false);
            maskedField.setManaged(false);

            eyeIcon.setImage(eyeOpen);
        } else {
            maskedField.setText(visibleField.getText());
            maskedField.setVisible(true);
            maskedField.setManaged(true);

            visibleField.setVisible(false);
            visibleField.setManaged(false);

            eyeIcon.setImage(eyeClosed);
        }
    }
}
