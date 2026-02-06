package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "STUDY")
public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StudyId")
    private Integer studyId;

    @Column(name = "Statistic")
    private Integer statistic;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserId", referencedColumnName = "UserId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FlashcardSetId", referencedColumnName = "FlashcardSetId", nullable = false)
    private FlashcardSet flashcardSet;

    public Study() {}

    public Study(Integer statistic, User user, FlashcardSet flashcardSet) {
        this.statistic = statistic;
        this.user = user;
        this.flashcardSet = flashcardSet;
    }

    public Integer getStudyId() { return studyId; }
    public Integer getStatistic() { return statistic; }
    public User getUser() { return user; }
    public FlashcardSet getFlashcardSet() { return flashcardSet; }

    public void setStatistic(Integer statistic) { this.statistic = statistic; }
    public void setUser(User user) { this.user = user; }
    public void setFlashcardSet(FlashcardSet flashcardSet) { this.flashcardSet = flashcardSet; }

    @Override
    public String toString() {
        return "Study{" +
                "studyId=" + studyId +
                ", statistic=" + statistic +
                ", userId=" + (user != null ? user.getUserId() : null) +
                ", flashcardSetId=" + (flashcardSet != null ? flashcardSet.getFlashcardSetId() : null) +
                '}';
    }
}
