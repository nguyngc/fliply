package model.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    private String firstName;
    private String lastName;
    private String email;
    private String googleId;
    private int role;

    @OneToMany(mappedBy = "teacher")
    private List<ClassModel> classesTaught;

    @OneToMany(mappedBy = "user")
    private List<Flashcard> flashcards;

    @OneToMany(mappedBy = "user")
    private List<Quiz> quizzes;

    // Default constructor
    public User() {}

    // Full constructor
    public User(String firstName, String lastName, String email, String googleId, int role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.googleId = googleId;
        this.role = role;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getGoogleId() { return googleId; }
    public int getRole() { return role; }

    // Setters
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
    public void setRole(int role) { this.role = role; }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", googleId='" + googleId + '\'' +
                ", role=" + role +
                '}';
    }
}
