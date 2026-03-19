package util;

import java.util.Locale;

public class LocaleManager {
    private static Locale currentLocale = new Locale("en", "US");

    private LocaleManager() {}

    public static Locale getLocale() {
        return currentLocale;
    }

    public static void setLocale(String language, String country) {
        currentLocale = new Locale(language, country);
    }
}