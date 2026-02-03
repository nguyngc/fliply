package model.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "ClassModel")
public class ClassModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int classId;

    private String className;

    @ManyToOne
    @JoinColumn(name = "teacherId")
    private User teacher;

    @OneToMany(mappedBy = "classModel")
    private List<FlashcardSet> flashcardSets;

    public ClassModel() {}

    public ClassModel(String className, User teacher) {
        this.className = className;
        this.teacher = teacher;
    }

    public int getClassId() { return classId; }
    public String getClassName() { return className; }
    public User getTeacher() { return teacher; }

    public void setClassName(String className) { this.className = className; }
    public void setTeacher(User teacher) { this.teacher = teacher; }

    @Override
    public String toString() {
        return "ClassModel{" +
                "classId=" + classId +
                ", className='" + className + '\'' +
                ", teacher=" + (teacher != null ? teacher.getUserId() : null) +
                '}';
    }
}

