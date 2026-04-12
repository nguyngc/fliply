package model.service;

import model.dao.ClassModelDao;
import model.entity.ClassModel;
import model.entity.User;

import java.util.List;

/**
 * Service for teacher-owned class creation and retrieval.
 */
public class ClassService {
    private final ClassModelDao classDao = new ClassModelDao();

    /**
     * Creates and persists a new class assigned to the provided teacher.
     */
    public ClassModel createClass(String className, User teacher) {
        ClassModel c = new ClassModel();
        c.setClassName(className);
        c.setTeacher(teacher);
        classDao.persist(c);
        return c;
    }

    /**
     * Returns classes taught by the given teacher id.
     */
    public List<ClassModel> getClassesByTeacher(int teacherId) {
        return classDao.findByTeacherId(teacherId);
    }
}
