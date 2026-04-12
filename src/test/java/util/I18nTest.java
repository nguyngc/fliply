package util;

import org.junit.jupiter.api.Test;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

class I18nTest {

    @Test
    void messageReturnsFallbackWhenBundleIsNull() {
        assertEquals("fallback", I18n.message(null, "missing", "fallback"));
    }

    @Test
    void messageReturnsFallbackWhenKeyMissing() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{"hello", "world"}};
            }
        };

        assertEquals("fallback", I18n.message(bundle, "missing", "fallback"));
    }

    @Test
    void messageReturnsValueAndFormatAppliesArguments() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{"template", "Hi {0}"}};
            }
        };

        assertEquals("Hi {0}", I18n.message(bundle, "template", "fallback"));
        assertEquals("Hi there", I18n.format(bundle, "template", "fallback {0}", "there"));
    }

    @Test
    void formatUsesFallbackWhenBundleIsNull() {
        assertEquals("Total: 5", I18n.format(null, "missing", "Total: {0}", 5));
    }
}




