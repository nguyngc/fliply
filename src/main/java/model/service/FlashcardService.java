package model.service;

import model.dao.FlashcardDao;
import model.dao.QuizDetailsDao;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.entity.User;

import java.util.List;

public class FlashcardService {
    private final FlashcardDao flashDao = new FlashcardDao();
    private final QuizDetailsDao quizDetailsDao = new QuizDetailsDao();

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

    public void update(Flashcard card) {
        flashDao.update(card);
    }
    public void delete(Flashcard card) {
        if (card == null || card.getFlashcardId() == null) {
            return;
        }
        // Remove dependent quiz details first to satisfy FK constraints.
        quizDetailsDao.deleteByFlashcardId(card.getFlashcardId());
        flashDao.delete(card);
    }

    public void save(Flashcard newCard) {
        flashDao.persist(newCard);
    }

    public List<Flashcard> getFlashcardsByUser(int userId) {
        return flashDao.findByUserId(userId);
    }
}
