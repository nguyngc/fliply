package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.QuizDetails;

import java.util.List;

public class QuizDetailsDao {

    EntityManager createEntityManager() {
        return MariaDbJPAConnection.createEntityManager();
    }

    public void persist(QuizDetails quizDetails) {
        try (EntityManager em = createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(quizDetails);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public QuizDetails find(int quizDetailsId) {
        try (EntityManager em = createEntityManager()) {
            return em.find(QuizDetails.class, Integer.valueOf(quizDetailsId));
        }
    }

    public List<QuizDetails> findAll() {
        try (EntityManager em = createEntityManager()) {
            return em.createQuery("SELECT qd FROM QuizDetails qd", QuizDetails.class)
                    .getResultList();
        }
    }

    public void delete(QuizDetails quizDetails) {
        try (EntityManager em = createEntityManager()) {
            em.getTransaction().begin();
            try {
                if (!em.contains(quizDetails)) {
                    quizDetails = em.merge(quizDetails);
                }
                em.remove(quizDetails);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public List<QuizDetails> findByQuizId(int quizId) {
        try (EntityManager em = createEntityManager()) {
            TypedQuery<QuizDetails> q = em.createQuery(
                    "SELECT qd FROM QuizDetails qd WHERE qd.quiz.quizId = :qid",
                    QuizDetails.class
            );
            q.setParameter("qid", quizId);
            return q.getResultList();
        }
    }

    public List<QuizDetails> findByFlashcardId(int flashcardId) {
        try (EntityManager em = createEntityManager()) {
            TypedQuery<QuizDetails> q = em.createQuery(
                    "SELECT qd FROM QuizDetails qd WHERE qd.flashcard.flashcardId = :fid",
                    QuizDetails.class
            );
            q.setParameter("fid", flashcardId);
            return q.getResultList();
        }
    }

    public void deleteByFlashcardId(int flashcardId) {
        try (EntityManager em = createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.createQuery("DELETE FROM QuizDetails qd WHERE qd.flashcard.flashcardId = :fid")
                        .setParameter("fid", flashcardId)
                        .executeUpdate();
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public boolean exists(int quizId, int flashcardId) {
        try (EntityManager em = createEntityManager()) {
            Long count = em.createQuery(
                            "SELECT COUNT(qd) FROM QuizDetails qd " +
                                    "WHERE qd.quiz.quizId = :qid AND qd.flashcard.flashcardId = :fid",
                            Long.class
                    ).setParameter("qid", quizId)
                    .setParameter("fid", flashcardId)
                    .getSingleResult();

            return count != null && count > 0;
        }
    }
}
