package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "FLASHCARDSET")
public class FlashcardSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FlashcardSetId")
    private Integer flashcardSetId;

    @Column(name = "Subject")
    private String subject;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ClassId", referencedColumnName = "ClassId", nullable = false)
    private ClassModel classModel;

    public FlashcardSet() {}

    public FlashcardSet(String subject, ClassModel classModel) {
        this.subject = subject;
        this.classModel = classModel;
    }

    public Integer getFlashcardSetId() { return flashcardSetId; }
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
