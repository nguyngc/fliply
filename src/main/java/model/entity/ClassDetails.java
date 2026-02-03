package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CLASS_DETAILS")
public class ClassDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne
    @JoinColumn(name = "classId")
    private ClassModel classModel;

    public ClassDetails() {}

    public ClassDetails(User user, ClassModel classModel) {
        this.user = user;
        this.classModel = classModel;
    }

    public int getId() { return id; }
    public User getUser() { return user; }
    public ClassModel getClassModel() { return classModel; }

    public void setUser(User user) { this.user = user; }
    public void setClassModel(ClassModel classModel) { this.classModel = classModel; }

    @Override
    public String toString() {
        return "ClassDetails{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getUserId() : null) +
                ", classId=" + (classModel != null ? classModel.getClassId() : null) +
                '}';
    }
}

