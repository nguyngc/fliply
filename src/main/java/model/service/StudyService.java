package model.service;

import model.dao.StudyDao;
import model.entity.ClassModel;
import model.entity.FlashcardSet;
import model.entity.Study;
import model.entity.User;

/**
 * Computes learning progress from persisted study statistics.
 */
public class StudyService {

    private final StudyDao dao = new StudyDao();

    /**
     * Returns progress for one flashcard set as a fraction in range [0.0, 1.0].
     */
    public double getProgressPercent(User student, FlashcardSet set) {
        Study study = dao.findByStudentAndSet(student.getUserId(), set.getFlashcardSetId());

        if (study == null) return 0.0;

        int learned = study.getStatistic();
        int total = set.getCards().size();

        if (total == 0) return 0.0;

        return (double) learned / total;
    }

    /**
     * Aggregates progress across all sets in a class, weighted by card count per set.
     */
    public double getClassProgress(User student, ClassModel c) {
        int totalCards = 0;
        int learnedCards = 0;

        for (FlashcardSet set : c.getFlashcardSets()) {
            int setTotal = set.getCards().size();
            totalCards += setTotal;

            double percent = getProgressPercent(student, set);
            learnedCards += (int) (percent * setTotal);
        }

        if (totalCards == 0) return 0.0;

        return (double) learnedCards / totalCards;
    }

}
