package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.Quiz;

import java.util.List;

public class QuizDao {

    /**
     * Persist a new Quiz entity into the database.
     */
    public void persist(Quiz quiz) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.persist(quiz);
        em.getTransaction().commit();
    }

    /**
     * Find a Quiz by its primary key (quizId).
     */
    public Quiz find(int quizId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        return em.find(Quiz.class, quizId);
    }

    /**
     * Retrieve all Quiz records.
     */
    public List<Quiz> findAll() {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Quiz> query = em.createQuery(
                "SELECT q FROM Quiz q", Quiz.class
        );
        return query.getResultList();
    }

    /**
     * Update an existing Quiz entity.
     */
    public void update(Quiz quiz) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.merge(quiz);
        em.getTransaction().commit();
    }

    /**
     * Delete a Quiz entity.
     */
    public void delete(Quiz quiz) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();

        // Ensure entity is managed before removal
        if (!em.contains(quiz)) {
            quiz = em.merge(quiz);
        }

        em.remove(quiz);
        em.getTransaction().commit();
    }

    /**
     * Find all quizzes created by a specific user.
     */
    public List<Quiz> findByUserId(int userId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Quiz> query = em.createQuery(
                "SELECT q FROM Quiz q WHERE q.user.userId = :uid",
                Quiz.class
        );
        query.setParameter("uid", userId);
        return query.getResultList();
    }

    /**
     * Check if a quiz exists for a user with the same number of questions.
     * (Optional but useful for preventing duplicates)
     */
    public boolean existsByUserAndQuestionCount(int userId, int noOfQuestions) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(q) FROM Quiz q " +
                        "WHERE q.user.userId = :uid AND q.noOfQuestions = :count",
                Long.class
        );
        query.setParameter("uid", userId);
        query.setParameter("count", noOfQuestions);

        return query.getSingleResult() > 0;
    }
}
