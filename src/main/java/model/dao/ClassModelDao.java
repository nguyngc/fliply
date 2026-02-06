package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.ClassModel;

import java.util.List;

public class ClassModelDao {

    public void persist(ClassModel classModel) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(classModel);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public ClassModel find(int classId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            return em.find(ClassModel.class, Integer.valueOf(classId));
        }
    }

    public List<ClassModel> findAll() {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<ClassModel> query =
                    em.createQuery("SELECT c FROM ClassModel c", ClassModel.class);
            return query.getResultList();
        }
    }

    public void update(ClassModel classModel) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.merge(classModel);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public void delete(ClassModel classModel) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            em.getTransaction().begin();
            try {
                if (!em.contains(classModel)) {
                    classModel = em.merge(classModel);
                }
                em.remove(classModel);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public List<ClassModel> findByTeacherId(int teacherId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<ClassModel> query = em.createQuery(
                    "SELECT c FROM ClassModel c WHERE c.teacher.userId = :tid",
                    ClassModel.class
            );
            query.setParameter("tid", teacherId);
            return query.getResultList();
        }
    }

    public boolean existsByNameAndTeacher(String className, int teacherId) {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(c) FROM ClassModel c WHERE c.className = :name AND c.teacher.userId = :tid",
                    Long.class
            );
            query.setParameter("name", className);
            query.setParameter("tid", teacherId);

            return query.getSingleResult() > 0;
        }
    }
}
