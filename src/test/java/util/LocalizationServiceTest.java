package util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationServiceTest {

    private final Locale originalLocale = LocaleManager.getLocale();

    @AfterEach
    void tearDown() {
        LocaleManager.setLocale(originalLocale.getLanguage(), originalLocale.getCountry());
    }

    @Test
    void utilityConstructorThrows() throws Exception {
        Constructor<LocalizationService> ctor = LocalizationService.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, ctor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
    }

    @Test
    void localeManagerStoresAndReturnsLocale() {
        LocaleManager.setLocale("fi", "FI");

        assertEquals(Locale.of("fi", "FI"), LocaleManager.getLocale());
    }

    @Test
    void getLocalizedStrings_returnsEnglishStrings() {
        LocaleManager.setLocale("en", "US");

        Map<String, String> strings = LocalizationService.getLocalizedStrings();

        assertEquals("Fliply", strings.get("welcome.title"));
        assertEquals("Classes", strings.get("nav.classes"));
        assertEquals("New Set of Flashcard", strings.get("teacherAddSet.title"));
    }

    @Test
    void getLocalizedStrings_returnsVietnameseStrings() {
        LocaleManager.setLocale("vi", "VN");

        Map<String, String> strings = LocalizationService.getLocalizedStrings();

        assertEquals("Fliply", strings.get("welcome.title"));
        assertEquals("Lớp học", strings.get("nav.classes"));
        assertEquals("Bộ thẻ học mới", strings.get("teacherAddSet.title"));
    }
}

