package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CLASS_DETAILS")
public class ClassDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClassDetailsId")
    private Integer classDetailsId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserId", referencedColumnName = "UserId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ClassId", referencedColumnName = "ClassId", nullable = false)
    private ClassModel classModel;

    public ClassDetails() {}

    public ClassDetails(User user, ClassModel classModel) {
        this.user = user;
        this.classModel = classModel;
    }

    public Integer getClassDetailsId() { return classDetailsId; }
    public User getUser() { return user; }
    public ClassModel getClassModel() { return classModel; }

    public void setUser(User user) { this.user = user; }
    public void setClassModel(ClassModel classModel) { this.classModel = classModel; }

    @Override
    public String toString() {
        return "ClassDetails{" +
                "classDetailsId=" + classDetailsId +
                ", userId=" + (user != null ? user.getUserId() : null) +
                ", classId=" + (classModel != null ? classModel.getClassId() : null) +
                '}';
    }
}
