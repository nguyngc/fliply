package model.service;

import model.dao.ClassDetailsDao;
import model.dao.ClassModelDao;
import model.entity.ClassDetails;
import model.entity.ClassModel;
import model.entity.User;
import java.util.List;

/**
 * Service layer for class membership operations and class reload helpers.
 */
public class ClassDetailsService {

    private final ClassDetailsDao classDetailsDao = new ClassDetailsDao();
    private final ClassModelDao classDao = new ClassModelDao();

    /**
     * Returns classes where the given user is either the teacher or an enrolled student.
     */
    public List<ClassModel> getClassesOfUser(int userId) {
        return classDao.findClassesOfUser(userId);
    }


    /**
     * Returns all enrollment rows for a specific class.
     */
    public List<ClassDetails> getClassesByClassId(int classId) {
        return classDetailsDao.findByClassId(classId);
    }

    /**
     * Creates and persists a new enrollment row.
     */
    public ClassDetails addStudentToClass(User student, ClassModel c) {
        ClassDetails cd = new ClassDetails(c, student);
        classDetailsDao.persist(cd);
        return cd;
    }

    /**
     * Persists changes to an existing enrollment row.
     */
    public void update(ClassDetails cd) {
        classDetailsDao.update(cd);
    }

    /**
     * Removes a student enrollment from a class.
     */
    public void removeStudentFromClass(ClassDetails cd) {
        classDetailsDao.delete(cd);
    }

    /**
     * Reloads a class with related entities eagerly fetched for UI use.
     */
    public ClassModel reloadClass(int classId) {
        return classDao.findByIdWithRelations(classId);
    }

}
