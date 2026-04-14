package util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvConfigTest {

    @Test
    void utilityConstructorThrows() throws Exception {
        Constructor<EnvConfig> ctor = EnvConfig.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, ctor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
    }

    @Test
    void get_prefersSystemPropertiesOverDotEnv() throws Exception {
        Path envFile = Files.createTempFile("fliply-test", ".env");
        try {
            Files.writeString(envFile, "DB_PORT=3306\nDB_HOST=localhost\n");
            System.setProperty("fliply.env.file", envFile.toString());
            System.setProperty("DB_PORT", "4406");

            EnvConfig.reload();

            assertEquals("4406", EnvConfig.get("DB_PORT", "3307"));
            assertEquals("localhost", EnvConfig.get("DB_HOST", "db"));
            assertEquals("fallback", EnvConfig.get("MISSING_KEY", "fallback"));
        } finally {
            Files.deleteIfExists(envFile);
        }
    }

    @Test
    void get_readsQuotedValuesAndIgnoresCommentsAndInvalidLines() throws Exception {
        Path envFile = Files.createTempFile("fliply-test", ".env");
        try {
            Files.writeString(envFile, """
                    # comment
                    INVALID_LINE
                    DB_NAME="fliply"
                    DB_USER='teacher'
                    BLANK_VALUE=
                    """);
            System.setProperty("fliply.env.file", envFile.toString());

            EnvConfig.reload();

            assertEquals("fliply", EnvConfig.get("DB_NAME", "fallback"));
            assertEquals("teacher", EnvConfig.get("DB_USER", "fallback"));
            assertEquals("fallback", EnvConfig.get("BLANK_VALUE", "fallback"));
            assertEquals("fallback", EnvConfig.get("INVALID_LINE", "fallback"));
        } finally {
            Files.deleteIfExists(envFile);
        }
    }

    @Test
    void get_returnsDefaultWhenConfiguredEnvFileDoesNotExist() {
        System.setProperty("fliply.env.file", "target/does-not-exist.env");

        EnvConfig.reload();

        assertEquals("3307", EnvConfig.get("DB_PORT", "3307"));
    }

    @AfterEach
    void clearProperties() {
        System.clearProperty("DB_PORT");
        System.clearProperty("fliply.env.file");
        EnvConfig.reload();
    }
}
