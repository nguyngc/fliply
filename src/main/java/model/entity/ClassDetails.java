package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CLASS_DETAILS")
public class ClassDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClassDetailsId")
    private Integer classDetailsId;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "UserId", referencedColumnName = "UserId", nullable = false)
//    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ClassId", referencedColumnName = "ClassId", nullable = false)
    private ClassModel classModel;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "StudentId", referencedColumnName = "UserId")
    private User student;

    public ClassDetails() {}

    public ClassDetails( ClassModel classModel, User student) {
        this.classModel = classModel;
        this.student = student;
    }

    public Integer getClassDetailsId() { return classDetailsId; }
    public ClassModel getClassModel() { return classModel; }
    public User getStudent() { return student; }

    public void setClassModel(ClassModel classModel) { this.classModel = classModel; }
    public void setStudent(User student) { this.student = student; }

    @Override
    public String toString() {
        return "ClassDetails{" +
                "classDetailsId=" + classDetailsId +
                ", StudentId=" + (student != null ? student.getUserId() : null) +
                ", classId=" + (classModel != null ? classModel.getClassId() : null) +
                '}';
    }

}
