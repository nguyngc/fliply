package model.service;

import model.dao.ClassDetailsDao;
import model.entity.ClassDetails;
import model.entity.ClassModel;
import model.entity.User;

import java.util.List;

/**
 * Service for student enrollment operations within a class.
 */
public class EnrollmentService {
    private final ClassDetailsDao cdDao = new ClassDetailsDao();

    /**
     * Enrolls a student only when an enrollment for the same class does not already exist.
     */
    public ClassDetails enroll(User student, ClassModel clazz) {
        // Avoid creating duplicate enrollment rows.
        if (cdDao.existsByUserAndClass(student.getUserId(), clazz.getClassId())) {
            return null;
        }
        ClassDetails cd = new ClassDetails();
        cd.setStudent(student);
        cd.setClassModel(clazz);
        cdDao.persist(cd);
        return cd;
    }

    /**
     * Returns all students currently enrolled in a class.
     */
    public List<ClassDetails> getStudentsInClass(int classId) {
        return cdDao.findByClassId(classId);
    }

    /**
     * Removes all enrollment rows for a class.
     */
    public void resetClassStudents(int classId) {
        cdDao.deleteByClassId(classId);
    }
}
