package model.service;

import model.dao.FlashcardDao;
import model.dao.QuizDao;
import model.dao.QuizDetailsDao;
import model.entity.Flashcard;
import model.entity.Quiz;
import model.entity.QuizDetails;
import model.entity.User;

import java.security.SecureRandom;
import java.util.*;

public class QuizService {

    private static final Random RANDOM = new SecureRandom();

    private final QuizDao quizDao = new QuizDao();
    private final QuizDetailsDao quizDetailsDao = new QuizDetailsDao();
    private final FlashcardDao flashcardDao = new FlashcardDao();

    /**
     * Generate quiz from user's available flashcards
     * Save to DB:
     * - QUIZ
     * - QUIZ_DETAILS (quizId + flashcardId)
     */
    public Quiz generateQuiz(User user, int noOfQuestions) {

        if (user == null || user.getUserId() == null) return null;
        if (noOfQuestions <= 0) return null;

        // pool of flashcards user can access
        List<Flashcard> pool = flashcardDao.findAvailableForUser(user.getUserId());
        if (pool == null || pool.isEmpty()) return null;
        if (noOfQuestions > pool.size()) return null;

        // random pick
        Collections.shuffle(pool, RANDOM);

        // create QUIZ
        Quiz quiz = new Quiz();
        quiz.setUser(user);
        quiz.setNoOfQuestions(noOfQuestions);
        quizDao.persist(quiz);

        // create QUIZ_DETAILS
        for (int i = 0; i < noOfQuestions; i++) {
            Flashcard f = pool.get(i);

            QuizDetails qd = new QuizDetails(quiz, f);
            quizDetailsDao.persist(qd);
        }

        return quiz;
    }

    public int getAvailableFlashcardCount(User user) {
        if (user == null || user.getUserId() == null) return 0;

        List<Flashcard> pool = flashcardDao.findAvailableForUser(user.getUserId());
        return pool == null ? 0 : pool.size();
    }

    /**
     * Build questions for UI: each question has 4 options
     * (1 correct + 3 random wrong)
     *
     * This is runtime only.
     */
    public List<QuizQuestion> buildQuizQuestions(int quizId, int userId) {

        List<QuizDetails> details = quizDetailsDao.findByQuizId(quizId);
        if (details == null || details.isEmpty()) return Collections.emptyList();

        // pool for wrong answers
        List<Flashcard> pool = flashcardDao.findAvailableForUser(userId);
        if (pool == null) pool = new ArrayList<>();

        List<QuizQuestion> out = new ArrayList<>();

        for (QuizDetails d : details) {
            Flashcard q = d.getFlashcard();
            if (q == null) continue;

            String prompt = q.getTerm();          // question text
            String correct = q.getDefinition();   // correct answer

            // options = correct + 3 wrong definitions
            LinkedHashSet<String> optionsSet = new LinkedHashSet<>();
            if (correct != null) optionsSet.add(correct);

            int guard = 0;
            while (optionsSet.size() < 4 && guard < 300 && !pool.isEmpty()) {
                Flashcard pick = pool.get(RANDOM.nextInt(pool.size()));
                if (pick == null) { guard++; continue; }

                String wrong = pick.getDefinition();
                if (wrong != null && !wrong.equals(correct)) {
                    optionsSet.add(wrong);
                }
                guard++;
            }

            List<String> options = new ArrayList<>(optionsSet);
            Collections.shuffle(options, RANDOM);

            out.add(new QuizQuestion(q.getFlashcardId(), prompt, correct, options));
        }

        return out;
    }

    public List<Quiz> getQuizzesByUser(Integer userId) {
        if (userId == null) return Collections.emptyList();
        return quizDao.findByUserId(userId);
    }


    // DTO for UI
    public static class QuizQuestion {
        private final int flashcardId;
        private final String prompt;
        private final String correctAnswer;
        private final List<String> options;

        public QuizQuestion(int flashcardId, String prompt, String correctAnswer, List<String> options) {
            this.flashcardId = flashcardId;
            this.prompt = prompt;
            this.correctAnswer = correctAnswer;
            this.options = options;
        }

        public int getFlashcardId() { return flashcardId; }
        public String getPrompt() { return prompt; }
        public String getCorrectAnswer() { return correctAnswer; }
        public List<String> getOptions() { return options; }


    }
}
