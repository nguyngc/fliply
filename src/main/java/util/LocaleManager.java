package util;

import java.util.Locale;

public class LocaleManager {
    private static Locale currentLocale = new Locale("en", "US");
    private static final Locale DEFAULT_LOCALE = new Locale("en", "US");

    private LocaleManager() {}

    public static Locale getLocale() {
        return currentLocale;
    }

    public static void setLocale(String language, String country) {
        currentLocale = new Locale(language, country);
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale == null ? DEFAULT_LOCALE : locale;
    }

    public static void setLocaleByLanguage(String language) {
        currentLocale = resolveSupportedLocale(language);
    }

    public static String getCurrentLanguageCode() {
        return currentLocale == null ? DEFAULT_LOCALE.getLanguage() : currentLocale.getLanguage();
    }

    public static Locale resolveSupportedLocale(String language) {
        if (language == null || language.isBlank()) {
            return DEFAULT_LOCALE;
        }

        return switch (language.toLowerCase(Locale.ROOT)) {
            case "ar" -> new Locale("ar", "AR");
            case "fi" -> new Locale("fi", "FI");
            case "ko" -> new Locale("ko", "KR");
            case "lo" -> new Locale("lo", "LA");
            case "vi" -> new Locale("vi", "VN");
            case "en" -> DEFAULT_LOCALE;
            default -> DEFAULT_LOCALE;
        };
    }
}
