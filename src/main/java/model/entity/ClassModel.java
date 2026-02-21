package model.entity;

import jakarta.persistence.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "CLASS")
//public class ClassModel extends AppState.ClassItem {
public class ClassModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClassId")
    private Integer classId;

    @Column(name = "ClassName")
    private String className;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TeacherId", referencedColumnName = "UserId", nullable = false)
    private User teacher;

    // --- Students in this class ---
    @OneToMany(mappedBy = "classModel", fetch = FetchType.LAZY)
    private Set<ClassDetails> students = new HashSet<>();

    // --- Flashcard sets in this class ---
    @OneToMany(mappedBy = "classModel", fetch = FetchType.LAZY)
    private Set<FlashcardSet> flashcardSets = new HashSet<>();

    public ClassModel() {}

    public ClassModel(String className, User teacher) {
        this.className = className;
        this.teacher = teacher;
    }

    public Integer getClassId() { return classId; }
    public String getClassName() { return className; }
    public User getTeacher() { return teacher; }

    public void setClassName(String className) { this.className = className; }
    public void setTeacher(User teacher) { this.teacher = teacher; }

    public Set<ClassDetails> getStudents() { return students; }
    public Set<FlashcardSet> getFlashcardSets() { return flashcardSets; }


    @Override
    public String toString() {
        return "ClassModel{" +
                "classId=" + classId +
                ", className='" + className + '\'' +
                ", teacherId=" + (teacher != null ? teacher.getUserId() : null) +
                '}';
    }


    public String getTeacherName() {
        return teacher != null
                ? teacher.getFirstName() + " " + teacher.getLastName()
                : "";
    }

}
