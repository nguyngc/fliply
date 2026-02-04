package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.ClassModel;

import java.util.List;

public class ClassModelDao {

    /**
     * Persist a new ClassModel entity into the database.
     */
    public void persist(ClassModel classModel) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.persist(classModel);
        em.getTransaction().commit();
    }

    /**
     * Find a ClassModel by its primary key (classId).
     */
    public ClassModel find(int classId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        return em.find(ClassModel.class, classId);
    }

    /**
     * Retrieve all ClassModel records.
     */
    public List<ClassModel> findAll() {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<ClassModel> query = em.createQuery(
                "SELECT c FROM ClassModel c", ClassModel.class
        );
        return query.getResultList();
    }

    /**
     * Update an existing ClassModel entity.
     */
    public void update(ClassModel classModel) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.merge(classModel);
        em.getTransaction().commit();
    }

    /**
     * Delete a ClassModel entity.
     */
    public void delete(ClassModel classModel) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();

        // Ensure entity is managed before removal
        if (!em.contains(classModel)) {
            classModel = em.merge(classModel);
        }

        em.remove(classModel);
        em.getTransaction().commit();
    }

    /**
     * Find all classes taught by a specific teacher.
     */
    public List<ClassModel> findByTeacherId(int teacherId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<ClassModel> query = em.createQuery(
                "SELECT c FROM ClassModel c WHERE c.teacher.userId = :tid",
                ClassModel.class
        );
        query.setParameter("tid", teacherId);
        return query.getResultList();
    }

    /**
     * Check if a class with the same name already exists for a teacher.
     */
    public boolean existsByNameAndTeacher(String className, int teacherId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM ClassModel c WHERE c.className = :name AND c.teacher.userId = :tid",
                Long.class
        );
        query.setParameter("name", className);
        query.setParameter("tid", teacherId);

        return query.getSingleResult() > 0;
    }
}
