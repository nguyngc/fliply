package util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Safe message lookup helpers with fallback support.
 */
public final class I18n {
    private I18n() {
    }

    /**
     * Returns a localized message or fallback when resources/key is missing.
     */
    public static String message(ResourceBundle resources, String key, String fallback) {
        if (resources == null) {
            return fallback;
        }
        try {
            return resources.getString(key);
        } catch (MissingResourceException ignored) {
            return fallback;
        }
    }

    /**
     * Formats a localized message pattern with MessageFormat.
     */
    public static String format(ResourceBundle resources, String key, String fallback, Object... args) {
        return MessageFormat.format(message(resources, key, fallback), args);
    }
}
