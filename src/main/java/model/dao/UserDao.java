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
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(user);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    /**
     * Find a User by its primary key (userId).
     */
    public User find(int userId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            return em.find(User.class, Integer.valueOf(userId));
        }
    }

    /**
     * Retrieve all User entities.
     */
    public List<User> findAll() {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
            return query.getResultList();
        }
    }

    /**
     * Update an existing User entity.
     */
    public void update(User user) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.merge(user);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    /**
     * Delete a User entity.
     */
    public void delete(User user) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                User managed = em.contains(user) ? user : em.merge(user);
                em.remove(managed);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    /**
     * Find a user by email.
     */
    public User findByEmail(String email) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            List<User> results = em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email",
                    User.class
            ).setParameter("email", email).getResultList();

            return results.isEmpty() ? null : results.get(0);
        }
    }

    /**
     * Check if a user exists by email.
     */
    public boolean existsByEmail(String email) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            Long count = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.email = :email",
                    Long.class
            ).setParameter("email", email).getSingleResult();

            return count != null && count > 0;
        }
    }

    /**
     * Login: find user by email + password
     */
    public User findByEmailAndPassword(String email, String password) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            List<User> results = em.createQuery(
                            "SELECT u FROM User u WHERE u.email = :email AND u.password = :pw",
                            User.class
                    ).setParameter("email", email)
                    .setParameter("pw", password)
                    .getResultList();

            return results.isEmpty() ? null : results.get(0);
        }
    }
}
