package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "USER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "FirstName")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "Email")
    private String email;

    @Column(name = "GoogleId")
    private String googleId;

    @Column(name = "Role")
    private Integer role;

    public User() {}

    public Integer getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getGoogleId() { return googleId; }
    public Integer getRole() { return role; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
    public void setRole(Integer role) { this.role = role; }

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
