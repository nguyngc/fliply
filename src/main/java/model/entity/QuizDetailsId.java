package model.entity;

import java.io.Serializable;
import java.util.Objects;

public class QuizDetailsId implements Serializable {

    private int quiz;
    private int flashcard;

    public QuizDetailsId() {}

    public QuizDetailsId(int quiz, int flashcard) {
        this.quiz = quiz;
        this.flashcard = flashcard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuizDetailsId)) return false;
        QuizDetailsId that = (QuizDetailsId) o;
        return quiz == that.quiz && flashcard == that.flashcard;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quiz, flashcard);
    }
}
