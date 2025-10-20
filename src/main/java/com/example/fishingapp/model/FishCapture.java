package com.example.fishingapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fish_captures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "images") // Evitar carga innecesaria en toString
@EqualsAndHashCode(exclude = "images")
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

    // NUEVA RELACIÓN CON IMÁGENES
    @OneToMany(mappedBy = "fishCapture", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CaptureImage> images = new ArrayList<>();

    // Método helper para añadir imágenes manteniendo la bidireccionalidad
    public void addImage(CaptureImage image) {
        images.add(image);
        image.setFishCapture(this);
    }

    // Método helper para eliminar imágenes
    public void removeImage(CaptureImage image) {
        images.remove(image);
        image.setFishCapture(null);
    }
}