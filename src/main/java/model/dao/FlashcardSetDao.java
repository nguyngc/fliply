package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.FlashcardSet;

import java.util.List;

public class FlashcardSetDao {

    public void persist(FlashcardSet flashcardSet) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(flashcardSet);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public FlashcardSet find(int flashcardSetId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            return em.find(FlashcardSet.class, Integer.valueOf(flashcardSetId));
        }
    }

    public List<FlashcardSet> findAll() {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<FlashcardSet> query = em.createQuery(
                    "SELECT fs FROM FlashcardSet fs", FlashcardSet.class
            );
            return query.getResultList();
        }
    }

    public void update(FlashcardSet flashcardSet) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.merge(flashcardSet);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public void delete(FlashcardSet flashcardSet) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                if (!em.contains(flashcardSet)) {
                    flashcardSet = em.merge(flashcardSet);
                }
                em.remove(flashcardSet);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public List<FlashcardSet> findByClassId(int classId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<FlashcardSet> query = em.createQuery(
                    "SELECT fs FROM FlashcardSet fs WHERE fs.classModel.classId = :cid",
                    FlashcardSet.class
            );
            query.setParameter("cid", classId);
            return query.getResultList();
        }
    }

    public boolean existsBySubjectInClass(String subject, int classId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
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
}
