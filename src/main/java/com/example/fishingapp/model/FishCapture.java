package com.example.fishingapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "fish_captures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FishCapture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "capture_date", nullable = false)
    private LocalDate captureDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "fish_type", nullable = false)
    private String fishType;

    @Column(name = "location")
    private String location;

    @Column(name = "weight", nullable = false)
    private Float weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
