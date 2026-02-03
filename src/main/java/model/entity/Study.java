package model.entity;

import jakarta.persistence.*;

@Entity
public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int statistic;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne
    @JoinColumn(name = "flashcardSetId")
    private FlashcardSet flashcardSet;

    public Study() {}

    public Study(int statistic, User user, FlashcardSet flashcardSet) {
        this.statistic = statistic;
        this.user = user;
        this.flashcardSet = flashcardSet;
    }

    public int getId() { return id; }
    public int getStatistic() { return statistic; }
    public User getUser() { return user; }
    public FlashcardSet getFlashcardSet() { return flashcardSet; }

    public void setStatistic(int statistic) { this.statistic = statistic; }
    public void setUser(User user) { this.user = user; }
    public void setFlashcardSet(FlashcardSet flashcardSet) { this.flashcardSet = flashcardSet; }

    @Override
    public String toString() {
        return "Study{" +
                "id=" + id +
                ", statistic=" + statistic +
                ", userId=" + (user != null ? user.getUserId() : null) +
                ", flashcardSetId=" + (flashcardSet != null ? flashcardSet.getFlashcardSetId() : null) +
                '}';
    }
}

