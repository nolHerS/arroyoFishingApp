package com.example.fishingapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fish_captures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
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

}
