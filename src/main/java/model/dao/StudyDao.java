package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.Study;

import java.util.List;

public class StudyDao {

    public void persist(Study study) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(study);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public Study find(int id) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            return em.find(Study.class, Integer.valueOf(id));
        }
    }

    public List<Study> findAll() {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<Study> query = em.createQuery("SELECT s FROM Study s", Study.class);
            return query.getResultList();
        }
    }

    public void update(Study study) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.merge(study);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public void delete(Study study) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                if (!em.contains(study)) {
                    study = em.merge(study);
                }
                em.remove(study);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public List<Study> findByUserId(int userId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<Study> query = em.createQuery(
                    "SELECT s FROM Study s WHERE s.user.userId = :uid",
                    Study.class
            );
            query.setParameter("uid", userId);
            return query.getResultList();
        }
    }

    public List<Study> findByFlashcardSetId(int setId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<Study> query = em.createQuery(
                    "SELECT s FROM Study s WHERE s.flashcardSet.flashcardSetId = :sid",
                    Study.class
            );
            query.setParameter("sid", setId);
            return query.getResultList();
        }
    }

    public boolean existsByUserAndSet(int userId, int setId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(s) FROM Study s " +
                            "WHERE s.user.userId = :uid AND s.flashcardSet.flashcardSetId = :sid",
                    Long.class
            );
            query.setParameter("uid", userId);
            query.setParameter("sid", setId);

            return query.getSingleResult() > 0;
        }
    }
}
