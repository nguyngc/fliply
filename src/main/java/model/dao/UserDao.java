package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.User;

import java.util.List;

public class UserDao {

    /**
     * Persist a new User entity into the database.
     */
    public void persist(User user) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
    }

    /**
     * Find a User by its primary key (userId).
     */
    public User find(int userId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        return em.find(User.class, userId);
    }

    /**
     * Retrieve all User entities.
     */
    public List<User> findAll() {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u", User.class
        );
        return query.getResultList();
    }

    /**
     * Update an existing User entity.
     */
    public void update(User user) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.merge(user);
        em.getTransaction().commit();
    }

    /**
     * Delete a User entity.
     */
    public void delete(User user) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();

        // Ensure the entity is managed before removal
        if (!em.contains(user)) {
            user = em.merge(user);
        }

        em.remove(user);
        em.getTransaction().commit();
    }

    /**
     * Find a user by email (common lookup).
     */
    public User findByEmail(String email) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.email = :email",
                User.class
        );
        query.setParameter("email", email);

        List<User> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Check if a user exists by Google ID.
     */
    public boolean existsByGoogleId(String googleId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.googleId = :gid",
                Long.class
        );
        query.setParameter("gid", googleId);

        return query.getSingleResult() > 0;
    }
}
