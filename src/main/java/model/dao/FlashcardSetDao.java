package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.FlashcardSet;

import java.util.List;

public class FlashcardSetDao {

    /**
     * Persist a new FlashcardSet entity into the database.
     */
    public void persist(FlashcardSet flashcardSet) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.persist(flashcardSet);
        em.getTransaction().commit();
    }

    /**
     * Find a FlashcardSet by its primary key (flashcardSetId).
     */
    public FlashcardSet find(int flashcardSetId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        return em.find(FlashcardSet.class, flashcardSetId);
    }

    /**
     * Retrieve all FlashcardSet records.
     */
    public List<FlashcardSet> findAll() {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<FlashcardSet> query = em.createQuery(
                "SELECT fs FROM FlashcardSet fs", FlashcardSet.class
        );
        return query.getResultList();
    }

    /**
     * Update an existing FlashcardSet entity.
     */
    public void update(FlashcardSet flashcardSet) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.merge(flashcardSet);
        em.getTransaction().commit();
    }

    /**
     * Delete a FlashcardSet entity.
     */
    public void delete(FlashcardSet flashcardSet) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();

        // Ensure entity is managed before removal
        if (!em.contains(flashcardSet)) {
            flashcardSet = em.merge(flashcardSet);
        }

        em.remove(flashcardSet);
        em.getTransaction().commit();
    }

    /**
     * Find all flashcard sets belonging to a specific class.
     */
    public List<FlashcardSet> findByClassId(int classId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<FlashcardSet> query = em.createQuery(
                "SELECT fs FROM FlashcardSet fs WHERE fs.classModel.classId = :cid",
                FlashcardSet.class
        );
        query.setParameter("cid", classId);
        return query.getResultList();
    }

    /**
     * Check if a flashcard set with the same subject already exists in a class.
     */
    public boolean existsBySubjectInClass(String subject, int classId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(fs) FROM FlashcardSet fs " +
                        "WHERE fs.subject = :subject AND fs.classModel.classId = :cid",
                Long.class
        );
        query.setParameter("subject", subject);
        query.setParameter("cid", classId);

        return query.getSingleResult() > 0;
    }
}
