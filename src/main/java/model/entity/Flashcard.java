package model.entity;

import jakarta.persistence.*;

@Entity
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int flashcardId;

    private String term;
    private String definition;

    @ManyToOne
    @JoinColumn(name = "flashcardSetId")
    private FlashcardSet flashcardSet;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    public Flashcard() {}

    public Flashcard(String term, String definition, FlashcardSet flashcardSet, User user) {
        this.term = term;
        this.definition = definition;
        this.flashcardSet = flashcardSet;
        this.user = user;
    }

    public int getFlashcardId() { return flashcardId; }
    public String getTerm() { return term; }
    public String getDefinition() { return definition; }
    public FlashcardSet getFlashcardSet() { return flashcardSet; }
    public User getUser() { return user; }

    public void setTerm(String term) { this.term = term; }
    public void setDefinition(String definition) { this.definition = definition; }
    public void setFlashcardSet(FlashcardSet flashcardSet) { this.flashcardSet = flashcardSet; }
    public void setUser(User user) { this.user = user; }

    @Override
    public String toString() {
        return "Flashcard{" +
                "flashcardId=" + flashcardId +
                ", term='" + term + '\'' +
                ", definition='" + definition + '\'' +
                ", flashcardSetId=" + (flashcardSet != null ? flashcardSet.getFlashcardSetId() : null) +
                ", userId=" + (user != null ? user.getUserId() : null) +
                '}';
    }
}

