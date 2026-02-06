package model.service;

import model.dao.FlashcardSetDao;
import model.entity.ClassModel;
import model.entity.FlashcardSet;

import java.util.List;

public class FlashcardSetService {
    private final FlashcardSetDao setDao = new FlashcardSetDao();

    public FlashcardSet createSet(String subject, ClassModel clazz) {
        FlashcardSet fs = new FlashcardSet();
        fs.setSubject(subject);
        fs.setClassModel(clazz);
        setDao.persist(fs);
        return fs;
    }

    public List<FlashcardSet> getSetsByClass(int classId) {
        return setDao.findByClassId(classId);
    }
}
