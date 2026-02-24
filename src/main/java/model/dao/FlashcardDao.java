package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.Flashcard;

import java.util.List;

public class FlashcardDao {

    public void persist(Flashcard flashcard) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(flashcard);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public Flashcard find(int flashcardId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            return em.find(Flashcard.class, Integer.valueOf(flashcardId));
        }
    }

    public List<Flashcard> findAll() {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<Flashcard> query = em.createQuery(
                    "SELECT f FROM Flashcard f", Flashcard.class
            );
            return query.getResultList();
        }
    }

    public void update(Flashcard flashcard) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.merge(flashcard);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public void delete(Flashcard flashcard) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                if (!em.contains(flashcard)) {
                    flashcard = em.merge(flashcard);
                }
                em.remove(flashcard);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public List<Flashcard> findByFlashcardSetId(int setId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<Flashcard> query = em.createQuery(
                    "SELECT f FROM Flashcard f WHERE f.flashcardSet.flashcardSetId = :sid",
                    Flashcard.class
            );
            query.setParameter("sid", setId);
            return query.getResultList();
        }
    }

    public List<Flashcard> findByUserId(int userId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<Flashcard> query = em.createQuery(
                    "SELECT f FROM Flashcard f WHERE f.user.userId = :uid",
                    Flashcard.class
            );
            query.setParameter("uid", userId);
            return query.getResultList();
        }
    }

    public boolean existsByTermInSet(String term, int setId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
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

    public List<Flashcard> findAvailableForUser(int userId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {

            // flashcard created by user or flashcard in class (user already join)
            TypedQuery<Flashcard> query = em.createQuery(
                    "SELECT DISTINCT f FROM Flashcard f " +
                            "WHERE f.user.userId = :uid " +
                            "   OR f.flashcardSet.classModel.classId IN " +
                            "      (SELECT cd.classModel.classId FROM ClassDetails cd WHERE cd.student.userId = :uid)",
                    Flashcard.class
            );

            query.setParameter("uid", userId);
            return query.getResultList();
        }
    }

}
