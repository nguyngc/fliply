package util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for managing localization and retrieving localized strings.
 * Provides a method to get all localized strings for the current locale.
 * Handles missing resource bundles with logging and fallback support.
 */
public final class LocalizationService {
    private static final Logger LOGGER = Logger.getLogger(LocalizationService.class.getName());

    private LocalizationService() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Get localized strings for a specific locale
     * @return a map of localized strings for the current locale, with keys corresponding to resource bundle keys
     */
    public static Map<String, String> getLocalizedStrings() {
        return getLocalizedStrings("Messages", LocaleManager.getLocale(), "Messages", Locale.of("en", "US"));
    }

    static Map<String, String> getLocalizedStrings(String baseName,
                                                   Locale locale,
                                                   String fallbackBaseName,
                                                   Locale fallbackLocale) {
        Map<String, String> strings = new HashMap<>();

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);

            // Extract all keys
            for (String key : bundle.keySet()) {
                strings.put(key, bundle.getString(key));
            }
        } catch (MissingResourceException e) {
            LOGGER.log(Level.WARNING, "Failed to load resource bundle for locale: {0}", locale);
            // Fallback to English
            try {
                ResourceBundle fallback = ResourceBundle.getBundle(
                        fallbackBaseName,
                        fallbackLocale
                );
                for (String key : fallback.keySet()) {
                    strings.put(key, fallback.getString(key));
                }
            } catch (MissingResourceException ex) {
                LOGGER.log(Level.SEVERE, "Failed to load fallback bundle", ex);
            }
        }

        return strings;
    }
}
