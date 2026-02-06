package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.Quiz;

import java.util.List;

public class QuizDao {

    public void persist(Quiz quiz) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(quiz);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public Quiz find(int quizId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            return em.find(Quiz.class, Integer.valueOf(quizId));
        }
    }

    public List<Quiz> findAll() {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<Quiz> query = em.createQuery("SELECT q FROM Quiz q", Quiz.class);
            return query.getResultList();
        }
    }

    public void update(Quiz quiz) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.merge(quiz);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public void delete(Quiz quiz) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                if (!em.contains(quiz)) {
                    quiz = em.merge(quiz);
                }
                em.remove(quiz);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public List<Quiz> findByUserId(int userId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<Quiz> query = em.createQuery(
                    "SELECT q FROM Quiz q WHERE q.user.userId = :uid",
                    Quiz.class
            );
            query.setParameter("uid", userId);
            return query.getResultList();
        }
    }

    public boolean existsByUserAndQuestionCount(int userId, int noOfQuestions) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
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
}
