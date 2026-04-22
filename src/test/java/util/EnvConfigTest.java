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
    private static final String TEST_PORT_KEY = "FLIPLY_TEST_DB_PORT";
    private static final String TEST_HOST_KEY = "FLIPLY_TEST_DB_HOST";
    private static final String TEST_NAME_KEY = "FLIPLY_TEST_DB_NAME";
    private static final String TEST_USER_KEY = "FLIPLY_TEST_DB_USER";
    private static final String TEST_BLANK_KEY = "FLIPLY_TEST_BLANK_VALUE";
    private static final String TEST_INVALID_KEY = "FLIPLY_TEST_INVALID_LINE";
    private static final String TEST_MISSING_KEY = "FLIPLY_TEST_MISSING_KEY";

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
            Files.writeString(envFile, TEST_PORT_KEY + "=3306\n" + TEST_HOST_KEY + "=localhost\n");
            System.setProperty("fliply.env.file", envFile.toString());
            System.setProperty(TEST_PORT_KEY, "4406");

            EnvConfig.reload();

            assertEquals("4406", EnvConfig.get(TEST_PORT_KEY, "3307"));
            assertEquals("localhost", EnvConfig.get(TEST_HOST_KEY, "db"));
            assertEquals("fallback", EnvConfig.get(TEST_MISSING_KEY, "fallback"));
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
                    FLIPLY_TEST_DB_NAME="fliply"
                    FLIPLY_TEST_DB_USER='teacher'
                    FLIPLY_TEST_BLANK_VALUE=
                    """);
            System.setProperty("fliply.env.file", envFile.toString());

            EnvConfig.reload();

            assertEquals("fliply", EnvConfig.get(TEST_NAME_KEY, "fallback"));
            assertEquals("teacher", EnvConfig.get(TEST_USER_KEY, "fallback"));
            assertEquals("fallback", EnvConfig.get(TEST_BLANK_KEY, "fallback"));
            assertEquals("fallback", EnvConfig.get(TEST_INVALID_KEY, "fallback"));
        } finally {
            Files.deleteIfExists(envFile);
        }
    }

    @Test
    void get_returnsDefaultWhenConfiguredEnvFileDoesNotExist() {
        System.setProperty("fliply.env.file", "target/does-not-exist.env");

        EnvConfig.reload();

        assertEquals("3307", EnvConfig.get(TEST_PORT_KEY, "3307"));
    }

    @AfterEach
    void clearProperties() {
        System.clearProperty(TEST_PORT_KEY);
        System.clearProperty(TEST_HOST_KEY);
        System.clearProperty(TEST_NAME_KEY);
        System.clearProperty(TEST_USER_KEY);
        System.clearProperty(TEST_BLANK_KEY);
        System.clearProperty(TEST_INVALID_KEY);
        System.clearProperty(TEST_MISSING_KEY);
        System.clearProperty("fliply.env.file");
        EnvConfig.reload();
    }
}
