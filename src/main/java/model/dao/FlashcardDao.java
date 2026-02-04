package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.Flashcard;

import java.util.List;

public class FlashcardDao {

    /**
     * Persist a new Flashcard entity into the database.
     */
    public void persist(Flashcard flashcard) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.persist(flashcard);
        em.getTransaction().commit();
    }

    /**
     * Find a Flashcard by its primary key (flashcardId).
     */
    public Flashcard find(int flashcardId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        return em.find(Flashcard.class, flashcardId);
    }

    /**
     * Retrieve all Flashcard records.
     */
    public List<Flashcard> findAll() {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Flashcard> query = em.createQuery(
                "SELECT f FROM Flashcard f", Flashcard.class
        );
        return query.getResultList();
    }

    /**
     * Update an existing Flashcard entity.
     */
    public void update(Flashcard flashcard) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.merge(flashcard);
        em.getTransaction().commit();
    }

    /**
     * Delete a Flashcard entity.
     */
    public void delete(Flashcard flashcard) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();

        // Ensure entity is managed before removal
        if (!em.contains(flashcard)) {
            flashcard = em.merge(flashcard);
        }

        em.remove(flashcard);
        em.getTransaction().commit();
    }

    /**
     * Find all flashcards belonging to a specific flashcard set.
     */
    public List<Flashcard> findByFlashcardSetId(int setId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Flashcard> query = em.createQuery(
                "SELECT f FROM Flashcard f WHERE f.flashcardSet.flashcardSetId = :sid",
                Flashcard.class
        );
        query.setParameter("sid", setId);
        return query.getResultList();
    }

    /**
     * Find all flashcards created by a specific user.
     */
    public List<Flashcard> findByUserId(int userId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Flashcard> query = em.createQuery(
                "SELECT f FROM Flashcard f WHERE f.user.userId = :uid",
                Flashcard.class
        );
        query.setParameter("uid", userId);
        return query.getResultList();
    }

    /**
     * Check if a flashcard with the same term already exists in a set.
     */
    public boolean existsByTermInSet(String term, int setId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(f) FROM Flashcard f " +
                        "WHERE f.term = :term AND f.flashcardSet.flashcardSetId = :sid",
                Long.class
        );
        query.setParameter("term", term);
        query.setParameter("sid", setId);

        return query.getSingleResult() > 0;
    }
}
