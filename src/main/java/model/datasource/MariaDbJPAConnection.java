package model.datasource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class MariaDbJPAConnection {

    private static EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("FliplyDbUnit", buildDbProperties());

    public static EntityManager createEntityManager() {
        // Recreate EMF if it's closed (for test scenarios)
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory("FliplyDbUnit", buildDbProperties());
        }
        return emf.createEntityManager();
    }

    @Deprecated
    public static EntityManager getInstance() {
        return createEntityManager();
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    private static Map<String, Object> buildDbProperties() {
        String host = getEnvOrDefault("DB_HOST", "localhost");
        String port = getEnvOrDefault("DB_PORT", "3306");
        String dbName = getEnvOrDefault("DB_NAME", "fliply");
        String user = getEnvOrDefault("DB_USER", "appuser");
        String pass = getEnvOrDefault("DB_PASS", "password");

        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", "jdbc:mariadb://" + host + ":" + port + "/" + dbName);
        properties.put("jakarta.persistence.jdbc.user", user);
        properties.put("jakarta.persistence.jdbc.password", pass);
        return properties;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
