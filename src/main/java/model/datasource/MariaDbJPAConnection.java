package model.datasource;

import jakarta.persistence.*;

/**
 * Singleton class to manage JPA EntityManager for MariaDB.
 */
public class MariaDbJPAConnection {
    // Holds the EntityManagerFactory instance
    private static EntityManagerFactory emf = null;
    // Holds the EntityManager instance
    private static EntityManager em = null;
    /**
     * Returns a singleton EntityManager instance.
     * Initializes EntityManagerFactory and EntityManager if not already created.
     * Note: Not thread-safe, synchronization needed for multi-threaded use.
     * @return EntityManager instance
     */
    public static EntityManager getInstance() {
        // Create EntityManager if it doesn't exist
        if (em==null) {
            // Create EntityManagerFactory if it doesn't exist
            if (emf==null) {
                emf = Persistence.createEntityManagerFactory("FliplyDbUnit");
            }
            em = emf.createEntityManager();
        }
        return em;
    }
}
