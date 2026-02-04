package model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.datasource.MariaDbJPAConnection;
import model.entity.ClassDetails;

import java.util.List;

public class ClassDetailsDao {

    /**
     * Persist a new ClassDetails entity into the database.
     */
    public void persist(ClassDetails classDetails) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.persist(classDetails);
        em.getTransaction().commit();
    }

    /**
     * Find a ClassDetails by its primary key (id).
     */
    public ClassDetails find(int id) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        return em.find(ClassDetails.class, id);
    }

    /**
     * Retrieve all ClassDetails records.
     */
    public List<ClassDetails> findAll() {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<ClassDetails> query = em.createQuery(
                "SELECT cd FROM ClassDetails cd", ClassDetails.class
        );
        return query.getResultList();
    }

    /**
     * Update an existing ClassDetails entity.
     */
    public void update(ClassDetails classDetails) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();
        em.merge(classDetails);
        em.getTransaction().commit();
    }

    /**
     * Delete a ClassDetails entity.
     */
    public void delete(ClassDetails classDetails) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();

        // Ensure entity is managed before removal
        if (!em.contains(classDetails)) {
            classDetails = em.merge(classDetails);
        }

        em.remove(classDetails);
        em.getTransaction().commit();
    }

    /**
     * Delete all students in a class when teacher reset class.
     */
    public void deleteByClassId(int classId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        em.getTransaction().begin();

        em.createQuery("DELETE FROM ClassDetails cd WHERE cd.classModel.classId = :cid")
                .setParameter("cid", classId)
                .executeUpdate();

        em.getTransaction().commit();
    }

    /**
     * Find all students in a specific class.
     */
    public List<ClassDetails> findByClassId(int classId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<ClassDetails> query = em.createQuery(
                "SELECT cd FROM ClassDetails cd WHERE cd.classModel.classId = :cid",
                ClassDetails.class
        );
        query.setParameter("cid", classId);
        return query.getResultList();
    }

    /**
     * Find all classes that a specific user has joined.
     */
    public List<ClassDetails> findByUserId(int userId) {
        EntityManager em = MariaDbJPAConnection.getInstance();
        TypedQuery<ClassDetails> query = em.createQuery(
                "SELECT cd FROM ClassDetails cd WHERE cd.user.userId = :uid",
                ClassDetails.class
        );
        query.setParameter("uid", userId);
        return query.getResultList();
    }

    /**
     * Check if a user is already enrolled in a class.
     */
    public boolean existsByUserAndClass(int userId, int classId) {
        EntityManager em = MariaDbJPAConnection.getInstance();

        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(cd) FROM ClassDetails cd " +
                        "WHERE cd.user.userId = :uid AND cd.classModel.classId = :cid",
                Long.class
        );

        query.setParameter("uid", userId);
        query.setParameter("cid", classId);

        return query.getSingleResult() > 0;
    }
}