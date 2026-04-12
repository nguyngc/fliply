package model.entity;

import jakarta.persistence.*;
import model.AppState;

@Entity
@Table(name = "USER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "FirstName", nullable = false)
    private String firstName;

    @Column(name = "LastName", nullable = false)
    private String lastName;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "Password", nullable = false)
    private String password;

    @Column(name = "Role", nullable = false)
    private Integer role;

    @Column(name = "Language", length = 2)
    private String language;

    // Required by JPA for entity instantiation via reflection.
    public User() {}

    public Integer getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Integer getRole() { return role; }
    public String getLanguage() { return language; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Integer role) { this.role = role; }
    public void setLanguage(String language) { this.language = language; }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", language='" + language + '\'' +
                '}';
    }

    public boolean isTeacher() {
        return role != null && role == 1;
    }

    public boolean isStudent() {
        return role != null && role == 0;
    }

}
