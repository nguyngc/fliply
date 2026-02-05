package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.Study;

import java.util.List;

public class StudyDao {

    /**
     * Persist a new Study entity into the database.
     */
    public void persist(Study study) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.persist(study);
        em.getTransaction().commit();
    }

    /**
     * Find a Study by its primary key (id).
     */
    public Study find(int id) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        return em.find(Study.class, id);
    }

    /**
     * Retrieve all Study records.
     */
    public List<Study> findAll() {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Study> query = em.createQuery(
                "SELECT s FROM Study s", Study.class
        );
        return query.getResultList();
    }

    /**
     * Update an existing Study entity.
     */
    public void update(Study study) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.merge(study);
        em.getTransaction().commit();
    }

    /**
     * Delete a Study entity.
     */
    public void delete(Study study) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();

        // Ensure entity is managed before removal
        if (!em.contains(study)) {
            study = em.merge(study);
        }

        em.remove(study);
        em.getTransaction().commit();
    }

    /**
     * Find all Study records for a specific user.
     */
    public List<Study> findByUserId(int userId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Study> query = em.createQuery(
                "SELECT s FROM Study s WHERE s.user.userId = :uid",
                Study.class
        );
        query.setParameter("uid", userId);
        return query.getResultList();
    }

    /**
     * Find all Study records for a specific flashcard set.
     */
    public List<Study> findByFlashcardSetId(int setId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Study> query = em.createQuery(
                "SELECT s FROM Study s WHERE s.flashcardSet.flashcardSetId = :sid",
                Study.class
        );
        query.setParameter("sid", setId);
        return query.getResultList();
    }

    /**
     * Check if a user has studied a specific flashcard set.
     */
    public boolean existsByUserAndSet(int userId, int setId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(s) FROM Study s WHERE s.user.userId = :uid AND s.flashcardSet.flashcardSetId = :sid",
                Long.class
        );
        query.setParameter("uid", userId);
        query.setParameter("sid", setId);

        return query.getSingleResult() > 0;
    }
}
