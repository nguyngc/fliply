package util;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LocaleManagerTest {

    @Test
    void resolveSupportedLocale_handlesKnownAndFallbackLanguages() {
        assertEquals(Locale.of("vi", "VN"), LocaleManager.resolveSupportedLocale("vi"));
        assertEquals(Locale.of("en", "US"), LocaleManager.resolveSupportedLocale("unknown"));
        assertEquals(Locale.of("en", "US"), LocaleManager.resolveSupportedLocale(null));
        assertEquals(Locale.of("en", "US"), LocaleManager.resolveSupportedLocale("   "));
    }

    @Test
    void setLocaleByLanguage_updatesCurrentLocale() {
        Locale original = LocaleManager.getLocale();
        try {
            LocaleManager.setLocaleByLanguage("ko");
            assertEquals(Locale.of("ko", "KR"), LocaleManager.getLocale());
        } finally {
            LocaleManager.setLocale(original);
        }
    }

    @Test
    void setLocale_nullFallsBackToDefault() {
        Locale original = LocaleManager.getLocale();
        try {
            LocaleManager.setLocale((Locale) null);
            assertEquals("en", LocaleManager.getCurrentLanguageCode());
            assertEquals(Locale.of("en", "US"), LocaleManager.getLocale());
        } finally {
            LocaleManager.setLocale(original);
        }
    }
}

