package model.service;

import model.dao.FlashcardDao;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.entity.User;

import java.util.List;

public class FlashcardService {
    private final FlashcardDao flashDao = new FlashcardDao();

    public Flashcard createFlashcard(String term, String definition, FlashcardSet set, User user) {
        // avoid dup
        if (flashDao.existsByTermInSet(term, set.getFlashcardSetId())) {
            return null;
        }
        Flashcard f = new Flashcard();
        f.setTerm(term);
        f.setDefinition(definition);
        f.setFlashcardSet(set);
        f.setUser(user);
        flashDao.persist(f);
        return f;
    }

    public List<Flashcard> getFlashcardsBySet(int setId) {
        return flashDao.findByFlashcardSetId(setId);
    }
}
