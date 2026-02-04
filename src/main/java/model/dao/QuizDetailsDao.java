package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.QuizDetails;
import model.entity.QuizDetailsId;

import java.util.List;

public class QuizDetailsDao {

    /**
     * Persist a new QuizDetails record (quiz–flashcard link).
     */
    public void persist(QuizDetails quizDetails) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.persist(quizDetails);
        em.getTransaction().commit();
    }

    /**
     * Find a QuizDetails record by composite key.
     */
    public QuizDetails find(int quizId, int flashcardId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        QuizDetailsId id = new QuizDetailsId(quizId, flashcardId);
        return em.find(QuizDetails.class, id);
    }

    /**
     * Retrieve all QuizDetails records.
     */
    public List<QuizDetails> findAll() {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<QuizDetails> query = em.createQuery(
                "SELECT qd FROM QuizDetails qd", QuizDetails.class
        );
        return query.getResultList();
    }

    /**
     * Delete a QuizDetails record.
     */
    public void delete(QuizDetails quizDetails) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();

        if (!em.contains(quizDetails)) {
            quizDetails = em.merge(quizDetails);
        }

        em.remove(quizDetails);
        em.getTransaction().commit();
    }

    /**
     * Delete by composite key.
     */
    public void deleteById(int quizId, int flashcardId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        QuizDetailsId id = new QuizDetailsId(quizId, flashcardId);

        QuizDetails qd = em.find(QuizDetails.class, id);
        if (qd != null) {
            delete(qd);
        }
    }

    /**
     * Find all flashcards belonging to a quiz.
     */
    public List<QuizDetails> findByQuizId(int quizId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<QuizDetails> query = em.createQuery(
                "SELECT qd FROM QuizDetails qd WHERE qd.quiz.quizId = :qid",
                QuizDetails.class
        );
        query.setParameter("qid", quizId);
        return query.getResultList();
    }

    /**
     * Check if a flashcard is already linked to a quiz.
     */
    public boolean exists(int quizId, int flashcardId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(qd) FROM QuizDetails qd " +
                        "WHERE qd.quiz.quizId = :qid AND qd.flashcard.flashcardId = :fid",
                Long.class
        );
        query.setParameter("qid", quizId);
        query.setParameter("fid", flashcardId);

        return query.getSingleResult() > 0;
    }

    /**
     * Delete all flashcards linked to a quiz.
     */
    public void deleteByQuizId(int quizId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();

        em.createQuery(
                "DELETE FROM QuizDetails qd WHERE qd.quiz.quizId = :qid"
        ).setParameter("qid", quizId).executeUpdate();

        em.getTransaction().commit();
    }
}
