package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "QUIZ")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QuizId")
    private Integer quizId;

    @Column(name = "NoOfQuestions")
    private Integer noOfQuestions;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserId", referencedColumnName = "UserId", nullable = false)
    private User user;

    public Quiz() {}

    public Quiz(Integer noOfQuestions, User user) {
        this.noOfQuestions = noOfQuestions;
        this.user = user;
    }

    public Integer getQuizId() { return quizId; }
    public Integer getNoOfQuestions() { return noOfQuestions; }
    public User getUser() { return user; }

    public void setNoOfQuestions(Integer noOfQuestions) { this.noOfQuestions = noOfQuestions; }
    public void setUser(User user) { this.user = user; }

    @Override
    public String toString() {
        return "Quiz{" +
                "quizId=" + quizId +
                ", noOfQuestions=" + noOfQuestions +
                ", userId=" + (user != null ? user.getUserId() : null) +
                '}';
    }
}
