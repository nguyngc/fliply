package model.service;

import model.AppState;
import model.dao.ClassModelDao;
import model.entity.ClassModel;

public class TeacherAddClassService {

    private final ClassModelDao classDao = new ClassModelDao();

    public void createClass(String className) {

        int teacherId = AppState.currentUser.get().getUserId();

        // class duplicate
        if (classDao.existsByNameAndTeacher(className, teacherId)) {
            throw new IllegalArgumentException("Class already exists for this teacher.");
        }

        // entity
        ClassModel newClass = new ClassModel();
        newClass.setClassName(className);
        newClass.setTeacher(AppState.currentUser.get());

        // save database
        classDao.persist(newClass);
    }
}
