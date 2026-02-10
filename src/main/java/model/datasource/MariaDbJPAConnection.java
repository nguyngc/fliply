package model.datasource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class MariaDbJPAConnection {

    private static EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("FliplyDbUnit");

    public static EntityManager createEntityManager() {
        // Recreate EMF if it's closed (for test scenarios)
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory("FliplyDbUnit");
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
}
