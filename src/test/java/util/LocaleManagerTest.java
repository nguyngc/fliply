package util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
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

    @Test
    void resolveSupportedLocale_coversRemainingSupportedLanguages() {
        assertEquals(Locale.of("ar", "AR"), LocaleManager.resolveSupportedLocale("ar"));
        assertEquals(Locale.of("fi", "FI"), LocaleManager.resolveSupportedLocale("fi"));
        assertEquals(Locale.of("lo", "LA"), LocaleManager.resolveSupportedLocale("lo"));
        assertEquals(Locale.of("en", "US"), LocaleManager.resolveSupportedLocale("en"));
    }

    @Test
    void getCurrentLanguageCode_fallsBackWhenCurrentLocaleIsNull() throws Exception {
        Field currentLocaleField = LocaleManager.class.getDeclaredField("currentLocale");
        currentLocaleField.setAccessible(true);
        Locale original = (Locale) currentLocaleField.get(null);
        try {
            currentLocaleField.set(null, null);

            assertEquals("en", LocaleManager.getCurrentLanguageCode());
        } finally {
            currentLocaleField.set(null, original);
        }
    }
}
