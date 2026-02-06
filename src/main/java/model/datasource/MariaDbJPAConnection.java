package model.datasource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class MariaDbJPAConnection {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("FliplyDbUnit");

    public static EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    @Deprecated
    public static EntityManager getInstance() {
        return createEntityManager();
    }

    public static void shutdown() {
        if (emf.isOpen()) emf.close();
    }
}
