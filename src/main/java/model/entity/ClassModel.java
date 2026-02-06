package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CLASS")
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

    @Override
    public String toString() {
        return "ClassModel{" +
                "classId=" + classId +
                ", className='" + className + '\'' +
                ", teacherId=" + (teacher != null ? teacher.getUserId() : null) +
                '}';
    }
}
