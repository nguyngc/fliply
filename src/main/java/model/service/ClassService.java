package model.service;

import model.dao.ClassModelDao;
import model.entity.ClassModel;
import model.entity.User;

import java.util.List;

public class ClassService {
    private final ClassModelDao classDao = new ClassModelDao();

    public ClassModel createClass(String className, User teacher) {
        ClassModel c = new ClassModel();
        c.setClassName(className);
        c.setTeacher(teacher);
        classDao.persist(c);
        return c;
    }

    public List<ClassModel> getClassesByTeacher(int teacherId) {
        return classDao.findByTeacherId(teacherId);
    }
}
