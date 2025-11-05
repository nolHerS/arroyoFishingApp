package com.example.fishingapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "capture_images")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "fishCapture")
@EqualsAndHashCode(exclude = "fishCapture")
public class CaptureImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, length = 500)
    private String originalUrl;

    @Column(name = "thumbnail_url", nullable = false, length = 500)
    private String thumbnailUrl;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize; // en bytes

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "s3_key", nullable = false)
    private String s3Key; // Ruta en el bucket

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capture_id", nullable = false)
    private FishCapture fishCapture;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
