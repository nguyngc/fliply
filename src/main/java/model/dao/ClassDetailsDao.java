package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.ClassDetails;

import java.util.List;

/**
 * DAO for class enrollment records (ClassDetails).
 */
public class ClassDetailsDao {

    EntityManager createEntityManager() {
        return MariaDbJPAConnection.createEntityManager();
    }

    public void persist(ClassDetails classDetails) {
        try (EntityManager em = createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(classDetails);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public ClassDetails find(int id) {
        try (EntityManager em = createEntityManager()) {
            return em.find(ClassDetails.class, Integer.valueOf(id));
        }
    }

    public List<ClassDetails> findAll() {
        try (EntityManager em = createEntityManager()) {
            TypedQuery<ClassDetails> query = em.createQuery(
                    "SELECT cd FROM ClassDetails cd", ClassDetails.class
            );
            return query.getResultList();
        }
    }

    public void update(ClassDetails classDetails) {
        try (EntityManager em = createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.merge(classDetails);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public void delete(ClassDetails classDetails) {
        try (EntityManager em = createEntityManager()) {
            em.getTransaction().begin();
            try {
                if (!em.contains(classDetails)) {
                    classDetails = em.merge(classDetails);
                }
                em.remove(classDetails);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public void deleteByClassId(int classId) {
        try (EntityManager em = createEntityManager()) {
            em.getTransaction().begin();
            try {
                // Bulk delete is used here to avoid loading all enrollment rows into memory.
                em.createQuery("DELETE FROM ClassDetails cd WHERE cd.classModel.classId = :cid")
                        .setParameter("cid", classId)
                        .executeUpdate();

                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public List<ClassDetails> findByClassId(int classId) {
        try (EntityManager em = createEntityManager()) {
            TypedQuery<ClassDetails> query = em.createQuery(
                    "SELECT cd FROM ClassDetails cd WHERE cd.classModel.classId = :cid",
                    ClassDetails.class
            );
            query.setParameter("cid", classId);
            return query.getResultList();
        }
    }

    public List<ClassDetails> findByStudentId(int userId) {
        try (EntityManager em = createEntityManager()) {
            TypedQuery<ClassDetails> query = em.createQuery(
                    "SELECT cd FROM ClassDetails cd WHERE cd.student.userId = :uid",
                    ClassDetails.class
            );
            query.setParameter("uid", userId);
            return query.getResultList();
        }
    }

    public boolean existsByUserAndClass(int userId, int classId) {
        try (EntityManager em = createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(cd) FROM ClassDetails cd " +
                            "WHERE cd.student.userId = :uid AND cd.classModel.classId = :cid",
                    Long.class
            );

            query.setParameter("uid", userId);
            query.setParameter("cid", classId);

            // COUNT query is the cheapest way to guard duplicate enrollment creation.
            Long count = query.getSingleResult();
            return count != null && count > 0;
        }
    }
}
