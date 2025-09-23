package com.example.fishingapp.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "fish_captures")
public class FishCapture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String fishType;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private LocalDate captureDate;

    private String location;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFishType() {
        return fishType;
    }

    public void setFishType(String fishType) {
        this.fishType = fishType;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public LocalDate getCaptureDate() {
        return captureDate;
    }

    public void setCaptureDate(LocalDate captureDate) {
        this.captureDate = captureDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FishCapture that = (FishCapture) o;
        return Objects.equals(id, that.id) && Objects.equals(user, that.user) && Objects.equals(fishType, that.fishType) && Objects.equals(weight, that.weight) && Objects.equals(captureDate, that.captureDate) && Objects.equals(location, that.location) && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, fishType, weight, captureDate, location, createdAt);
    }

    @Override
    public String toString() {
        return "FishCapture{" +
                "id=" + id +
                ", user=" + user +
                ", fishType='" + fishType + '\'' +
                ", weight=" + weight +
                ", captureDate=" + captureDate +
                ", location='" + location + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
