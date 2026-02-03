package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "QUIZ_DETAILS")
@IdClass(QuizDetailsId.class)
public class QuizDetails {

    @Id
    @ManyToOne
    @JoinColumn(name = "quizId")
    private Quiz quiz;

    @Id
    @ManyToOne
    @JoinColumn(name = "flashcardId")
    private Flashcard flashcard;

    public QuizDetails() {}

    public QuizDetails(Quiz quiz, Flashcard flashcard) {
        this.quiz = quiz;
        this.flashcard = flashcard;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public Flashcard getFlashcard() {
        return flashcard;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public void setFlashcard(Flashcard flashcard) {
        this.flashcard = flashcard;
    }

    @Override
    public String toString() {
        return "QuizDetails{" +
                "quizId=" + (quiz != null ? quiz.getQuizId() : null) +
                ", flashcardId=" + (flashcard != null ? flashcard.getFlashcardId() : null) +
                '}';
    }
}

