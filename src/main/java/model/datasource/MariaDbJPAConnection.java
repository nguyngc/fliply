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
        Map<String, Object> properties = new HashMap<>();

        String mode = getConfigOrDefault("fliply.db.mode", "mariadb");
        if ("h2".equalsIgnoreCase(mode) || "test".equalsIgnoreCase(mode)) {
            properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
            properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:fliply;MODE=MariaDB;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;NON_KEYWORDS=USER,CLASS");
            properties.put("jakarta.persistence.jdbc.user", "sa");
            properties.put("jakarta.persistence.jdbc.password", "");
            properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            properties.put("hibernate.hbm2ddl.auto", "create-drop");
            properties.put("hibernate.show_sql", "false");
            return properties;
        }

        String host = getEnvOrDefault("DB_HOST", "localhost");
        String port = getEnvOrDefault("DB_PORT", "3307");
        String dbName = getEnvOrDefault("DB_NAME", "fliply");
        String user = getEnvOrDefault("DB_USER", "root");
        String pass = getEnvOrDefault("DB_PASS", "123456");

        properties.put("jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");
        properties.put("jakarta.persistence.jdbc.url", "jdbc:mariadb://" + host + ":" + port + "/" + dbName);
        properties.put("jakarta.persistence.jdbc.user", user);
        properties.put("jakarta.persistence.jdbc.password", pass);
        properties.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "true");
        return properties;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private static String getConfigOrDefault(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
