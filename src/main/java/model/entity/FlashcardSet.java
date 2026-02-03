package model.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class FlashcardSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int flashcardSetId;

    private String subject;

    @ManyToOne
    @JoinColumn(name = "classId")
    private ClassModel classModel;

    @OneToMany(mappedBy = "flashcardSet")
    private List<Flashcard> flashcards;

    public FlashcardSet() {}

    public FlashcardSet(String subject, ClassModel classModel) {
        this.subject = subject;
        this.classModel = classModel;
    }

    public int getFlashcardSetId() { return flashcardSetId; }
    public String getSubject() { return subject; }
    public ClassModel getClassModel() { return classModel; }

    public void setSubject(String subject) { this.subject = subject; }
    public void setClassModel(ClassModel classModel) { this.classModel = classModel; }

    @Override
    public String toString() {
        return "FlashcardSet{" +
                "flashcardSetId=" + flashcardSetId +
                ", subject='" + subject + '\'' +
                ", classId=" + (classModel != null ? classModel.getClassId() : null) +
                '}';
    }
}

