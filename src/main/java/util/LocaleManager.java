package util;

import java.util.Locale;

/**
 * Stores the active UI locale and resolves supported language shortcuts.
 */
public class LocaleManager {
    private static Locale currentLocale = Locale.of("en", "US");
    private static final Locale DEFAULT_LOCALE = Locale.of("en", "US");

    private LocaleManager() {}

    /**
     * Returns the currently active locale.
     */
    public static Locale getLocale() {
        return currentLocale;
    }

    /**
     * Sets locale using language/country code.
     */
    public static void setLocale(String language, String country) {
        currentLocale = Locale.of(language, country);
    }

    /**
     * Sets locale directly, defaulting to English when null.
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale == null ? DEFAULT_LOCALE : locale;
    }

    /**
     * Resolves and applies a supported locale from a short language code.
     */
    public static void setLocaleByLanguage(String language) {
        currentLocale = resolveSupportedLocale(language);
    }

    /**
     * Returns only the current language code for persistence in user profile.
     */
    public static String getCurrentLanguageCode() {
        return currentLocale == null ? DEFAULT_LOCALE.getLanguage() : currentLocale.getLanguage();
    }

    /**
     * Maps supported languages and falls back to English for unknown/empty input.
     */
    public static Locale resolveSupportedLocale(String language) {
        if (language == null || language.isBlank()) {
            return DEFAULT_LOCALE;
        }

        return switch (language.toLowerCase(Locale.ROOT)) {
            case "ar" -> Locale.of("ar", "AR");
            case "fi" -> Locale.of("fi", "FI");
            case "ko" -> Locale.of("ko", "KR");
            case "lo" -> Locale.of("lo", "LA");
            case "vi" -> Locale.of("vi", "VN");
            case "en" -> DEFAULT_LOCALE;
            default -> DEFAULT_LOCALE;
        };
    }
}
