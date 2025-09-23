package com.example.fishingapp.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String fullName;

    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FishCapture> captures;

    public User() {
    }

    public User(Long id, String username, String fullName, String email, List<FishCapture> captures) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.captures = captures;
    }

    public User(Long id, String username, String fullName, String email) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<FishCapture> getCaptures() {
        return captures;
    }

    public void setCaptures(List<FishCapture> captures) {
        this.captures = captures;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username) && Objects.equals(fullName, user.fullName) && Objects.equals(email, user.email) && Objects.equals(captures, user.captures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, fullName, email, captures);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", captures=" + captures +
                '}';
    }
}
