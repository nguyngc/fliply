package model.service;

import model.dao.FlashcardDao;
import model.dao.QuizDetailsDao;
import model.entity.Flashcard;
import model.entity.FlashcardSet;
import model.entity.User;
import util.FlashcardFileParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlashcardService {
    private final FlashcardDao flashDao = new FlashcardDao();
    private final QuizDetailsDao quizDetailsDao = new QuizDetailsDao();

    public Flashcard createFlashcard(String term, String definition, FlashcardSet set, User user) {
        Map<String, String> definitions = new HashMap<>();
        definitions.put("en", definition);
        return createFlashcard(term, definitions, set, user);
    }

    public Flashcard createFlashcard(FlashcardFileParser.ParsedCard card, FlashcardSet set, User user) {
        if (card == null) {
            return null;
        }
        return createFlashcard(card.term(), card.definitions(), set, user);
    }

    public Flashcard createFlashcard(String term, Map<String, String> definitions, FlashcardSet set, User user) {
        // avoid dup
        if (flashDao.existsByTermInSet(term, set.getFlashcardSetId())) {
            return null;
        }
        Flashcard f = new Flashcard();
        f.setTerm(term);
        f.setDefinitions(definitions);
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
