package model.datasource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class MariaDbJPAConnection {

    private static final String DB_URL =
            System.getenv().getOrDefault("DB_URL",
                    "jdbc:mariadb://localhost:3307/fliply");

    private static final String DB_USER =
            System.getenv().getOrDefault("DB_USER", "appuser");

    private static final String DB_PASSWORD =
            System.getenv().getOrDefault("DB_PASS", "password");

    private static EntityManagerFactory emf;

    static {
        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url", DB_URL);
        props.put("jakarta.persistence.jdbc.user", DB_USER);
        props.put("jakarta.persistence.jdbc.password", DB_PASSWORD);
        props.put("jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");

        emf = Persistence.createEntityManagerFactory("FliplyDbUnit", props);
    }

    public static EntityManager createEntityManager() {
        if (emf == null || !emf.isOpen()) {
            Map<String, Object> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.url", DB_URL);
            props.put("jakarta.persistence.jdbc.user", DB_USER);
            props.put("jakarta.persistence.jdbc.password", DB_PASSWORD);
            props.put("jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");

            emf = Persistence.createEntityManagerFactory("FliplyDbUnit", props);
        }
        return emf.createEntityManager();
    }
}
