package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "QUIZ_DETAILS")
public class QuizDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QuizDetailsId")
    private Integer quizDetailsId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "QuizId", referencedColumnName = "QuizId", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FlashcardId", referencedColumnName = "FlashcardId", nullable = false)
    private Flashcard flashcard;

    public QuizDetails() {}

    public QuizDetails(Quiz quiz, Flashcard flashcard) {
        this.quiz = quiz;
        this.flashcard = flashcard;
    }

    public Integer getQuizDetailsId() { return quizDetailsId; }
    public Quiz getQuiz() { return quiz; }
    public Flashcard getFlashcard() { return flashcard; }

    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public void setFlashcard(Flashcard flashcard) { this.flashcard = flashcard; }

    @Override
    public String toString() {
        return "QuizDetails{" +
                "quizDetailsId=" + quizDetailsId +
                ", quizId=" + (quiz != null ? quiz.getQuizId() : null) +
                ", flashcardId=" + (flashcard != null ? flashcard.getFlashcardId() : null) +
                '}';
    }
}
