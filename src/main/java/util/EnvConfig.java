package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Resolves runtime configuration from system properties, environment variables, and a local .env file.
 */
public final class EnvConfig {

    private static final String ENV_FILE_PROPERTY = "fliply.env.file";
    private static Map<String, String> dotEnvValues = loadDotEnv();

    private EnvConfig() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String get(String key, String defaultValue) {
        String systemProperty = System.getProperty(key);
        if (isPresent(systemProperty)) {
            return systemProperty;
        }

        String environmentValue = System.getenv(key);
        if (isPresent(environmentValue)) {
            return environmentValue;
        }

        String dotEnvValue = dotEnvValues.get(key);
        if (isPresent(dotEnvValue)) {
            return dotEnvValue;
        }

        return defaultValue;
    }

    static void reload() {
        dotEnvValues = loadDotEnv();
    }

    private static Map<String, String> loadDotEnv() {
        Path envFile = resolveEnvFile();
        if (envFile == null || !Files.exists(envFile)) {
            return Collections.emptyMap();
        }

        Map<String, String> values = new HashMap<>();
        try (Stream<String> lines = Files.lines(envFile)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("#"))
                    .forEach(line -> addEntry(values, line));
        } catch (IOException ignored) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(values);
    }

    private static void addEntry(Map<String, String> values, String line) {
        int separatorIndex = line.indexOf('=');
        if (separatorIndex <= 0) {
            return;
        }

        String key = line.substring(0, separatorIndex).trim();
        String value = line.substring(separatorIndex + 1).trim();
        values.put(key, stripQuotes(value));
    }

    private static Path resolveEnvFile() {
        String configuredPath = System.getProperty(ENV_FILE_PROPERTY);
        if (isPresent(configuredPath)) {
            return Path.of(configuredPath);
        }
        return Path.of(System.getProperty("user.dir"), ".env");
    }

    private static String stripQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
