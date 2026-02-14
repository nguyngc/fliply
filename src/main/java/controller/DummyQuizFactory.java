package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.AppState;

import java.util.Random;

public class DummyQuizFactory {

    public static AppState.QuizItem quiz1() {
        return new AppState.QuizItem("Quiz 1", 10, 90, buildQuestions(10));
    }

    public static AppState.QuizItem quiz2() {
        return new AppState.QuizItem("Quiz 2", 10, 100, buildQuestions(10));
    }

    public static AppState.QuizItem generatedQuiz(int n) {
        return new AppState.QuizItem("Quiz " + (AppState.myQuizzes.size() + 1), n, 0, buildQuestions(n));
    }

    private static ObservableList<AppState.QuizQuestion> buildQuestions(int n) {
        ObservableList<AppState.QuizQuestion> list = FXCollections.observableArrayList();
        Random r = new Random();

        String[] defs = {"Definition 1", "Definition 2", "Definition 3", "Definition 4"};

        for (int i = 1; i <= n; i++) {
            String term = "Term " + i;

            // choose correct option
            int correctIdx = r.nextInt(4);
            String correct = defs[correctIdx];

            // options copy
            String[] opts = defs.clone();

            list.add(new AppState.QuizQuestion(term, correct, opts));
        }

        return list;
    }
}
