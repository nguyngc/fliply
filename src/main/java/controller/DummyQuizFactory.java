package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.AppState;

import java.security.SecureRandom;

/**
 * Creates in-memory quiz data used for development and tests.
 *
 * <p>The generated questions reuse a fixed set of sample definitions and randomly pick
 * one definition as the correct answer for each term.
 */
public final class DummyQuizFactory {

    private static final SecureRandom RANDOM = new SecureRandom();

    private DummyQuizFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    /** Returns a predefined quiz with 10 sample questions and a 90% score. */
    public static AppState.QuizItem quiz1() {
        return new AppState.QuizItem("Quiz 1", 10, 90, buildQuestions(10));
    }

    /** Returns a predefined quiz with 10 sample questions and a 100% score. */
    public static AppState.QuizItem quiz2() {
        return new AppState.QuizItem("Quiz 2", 10, 100, buildQuestions(10));
    }

    /**
     * Creates a new generated quiz named after the next index in {@code myQuizzes}.
     *
     * @param n number of questions to generate
     * @return generated quiz item with score initialized to 0
     */
    public static AppState.QuizItem generatedQuiz(int n) {
        return new AppState.QuizItem("Quiz " + (AppState.myQuizzes.size() + 1), n, 0, buildQuestions(n));
    }

    /**
     * Builds quiz questions with predictable term labels and randomized correct answers.
     *
     * @param n number of questions to build
     * @return observable list containing {@code n} generated quiz questions
     */
    private static ObservableList<AppState.QuizQuestion> buildQuestions(int n) {
        ObservableList<AppState.QuizQuestion> list = FXCollections.observableArrayList();

        // Shared pool of answer candidates used for every generated question.
        String[] defs = {"Definition 1", "Definition 2", "Definition 3", "Definition 4"};

        for (int i = 1; i <= n; i++) {
            String term = "Term " + i;

            // Randomize which definition is considered correct for this question.
            int correctIdx = RANDOM.nextInt(4);
            String correct = defs[correctIdx];

            // Clone to keep each question's option array independent.
            String[] opts = defs.clone();

            list.add(new AppState.QuizQuestion(term, correct, opts));
        }

        return list;
    }
}
