package model.service;

import model.dao.ClassDetailsDao;
import model.dao.ClassModelDao;
import model.entity.ClassDetails;
import model.entity.ClassModel;
import model.entity.User;
import java.util.List;

public class ClassDetailsService {

    private final ClassDetailsDao classDetailsDao = new ClassDetailsDao();
    private final ClassModelDao classDao = new ClassModelDao();

    public List<ClassModel> getClassesOfUser(int userId) {
        return classDao.findClassesOfUser(userId);
    }


    public List<ClassDetails> getClassesByClassId(int classId) {
        return classDetailsDao.findByClassId(classId);
    }

    public ClassDetails addStudentToClass(User student, ClassModel c) {
        ClassDetails cd = new ClassDetails(c, student);
        classDetailsDao.persist(cd);
        return cd;
    }

    public void update(ClassDetails cd) {
        classDetailsDao.update(cd);
    }

    public void removeStudentFromClass(ClassDetails cd) {
        classDetailsDao.delete(cd);
    }

    public ClassModel reloadClass(int classId) {
        return classDao.findByIdWithRelations(classId);
    }

}
