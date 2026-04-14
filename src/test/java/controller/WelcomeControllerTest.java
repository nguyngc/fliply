package controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.LocaleManager;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WelcomeControllerTest {

    private WelcomeController controller;
    private Locale previousLocale;

    @BeforeEach
    void setUp() {
        previousLocale = LocaleManager.getLocale();
        controller = new WelcomeController();
    }

    @AfterEach
    void tearDown() {
        LocaleManager.setLocale(previousLocale.getLanguage(), previousLocale.getCountry());
    }

    @Test
    void updateLocale_changesLocaleWithoutNavigation() {
        controller.updateLocale("fi", "FI");
        assertEquals(new Locale("fi", "FI"), LocaleManager.getLocale());

        controller.updateLocale("vi", "VN");
        assertEquals(new Locale("vi", "VN"), LocaleManager.getLocale());
    }
}
