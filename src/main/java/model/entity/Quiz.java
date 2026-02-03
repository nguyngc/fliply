package model.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int quizId;

    private int noOfQuestions;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "QUIZ_DETAILS",
            joinColumns = @JoinColumn(name = "quizId"),
            inverseJoinColumns = @JoinColumn(name = "flashcardId")
    )
    private List<Flashcard> flashcards;

    public Quiz() {}

    public Quiz(int noOfQuestions, User user) {
        this.noOfQuestions = noOfQuestions;
        this.user = user;
    }

    public int getQuizId() { return quizId; }
    public int getNoOfQuestions() { return noOfQuestions; }
    public User getUser() { return user; }
    public List<Flashcard> getFlashcards() { return flashcards; }

    public void setNoOfQuestions(int noOfQuestions) { this.noOfQuestions = noOfQuestions; }
    public void setUser(User user) { this.user = user; }
    public void setFlashcards(List<Flashcard> flashcards) { this.flashcards = flashcards; }

    @Override
    public String toString() {
        return "Quiz{" +
                "quizId=" + quizId +
                ", noOfQuestions=" + noOfQuestions +
                ", userId=" + (user != null ? user.getUserId() : null) +
                '}';
    }
}

