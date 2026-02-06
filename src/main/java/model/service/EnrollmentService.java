package model.service;

import model.dao.ClassDetailsDao;
import model.entity.ClassDetails;
import model.entity.ClassModel;
import model.entity.User;

import java.util.List;

public class EnrollmentService {
    private final ClassDetailsDao cdDao = new ClassDetailsDao();

    public ClassDetails enroll(User student, ClassModel clazz) {
        // avoid dupplicate
        if (cdDao.existsByUserAndClass(student.getUserId(), clazz.getClassId())) {
            return null;
        }
        ClassDetails cd = new ClassDetails();
        cd.setUser(student);
        cd.setClassModel(clazz);
        cdDao.persist(cd);
        return cd;
    }

    public List<ClassDetails> getStudentsInClass(int classId) {
        return cdDao.findByClassId(classId);
    }

    public void resetClassStudents(int classId) {
        cdDao.deleteByClassId(classId);
    }
}
