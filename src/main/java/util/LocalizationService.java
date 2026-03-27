package util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class LocalizationService {
    /**
     * Get localized strings for a specific locale
     */
    public static Map<String, String> getLocalizedStrings() {
        Map<String, String> strings = new HashMap<>();
        Locale locale = LocaleManager.getLocale();

        try {
            ResourceBundle bundle = ResourceBundle.getBundle("Messages", locale);

            // Extract all keys
            for (String key : bundle.keySet()) {
                strings.put(key, bundle.getString(key));
            }
        } catch (Exception e) {
            System.err.println("Failed to load resource bundle for locale: " + locale);
            // Fallback to English
            try {
                ResourceBundle fallback = ResourceBundle.getBundle(
                        "Messages",
                        new Locale("en", "US")
                );
                for (String key : fallback.keySet()) {
                    strings.put(key, fallback.getString(key));
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        return strings;
    }
}
