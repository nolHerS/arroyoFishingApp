package com.example.fishingapp.dto.image;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para responder con información de una imagen
 * Usa Record para inmutabilidad y concisión
 */
@Schema(description = "Información detallada de una imagen de captura")
public record ImageResponseDto(
        @Schema(description = "ID único de la imagen", example = "1")
        Long id,

        @Schema(description = "URL de la imagen original", example = "https://s3.tebi.io/bucket/captures/user_1/capture_5/image.jpg")
        String originalUrl,

        @Schema(description = "URL de la miniatura", example = "https://s3.tebi.io/bucket/thumbnails/user_1/capture_5/thumb_image.jpg")
        String thumbnailUrl,

        @Schema(description = "Nombre del archivo", example = "my_fish.jpg")
        String fileName,

        @Schema(description = "Tamaño del archivo en bytes", example = "2048576")
        Long fileSize,

        @Schema(description = "Tipo MIME del archivo", example = "image/jpeg")
        String mimeType,

        @Schema(description = "Ancho de la imagen en píxeles", example = "1920")
        Integer width,

        @Schema(description = "Alto de la imagen en píxeles", example = "1080")
        Integer height,

        @Schema(description = "Fecha y hora de subida", example = "2025-10-20T14:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime uploadedAt
) {
    /**
     * Constructor compacto para validaciones (opcional)
     */
    public ImageResponseDto {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("La URL original no puede estar vacía");
        }
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new IllegalArgumentException("La URL del thumbnail no puede estar vacía");
        }
    }

    /**
     * Método helper para obtener el tamaño en formato legible
     */
    public String getReadableFileSize() {
        if (fileSize == null) {
            return "0 B";
        }

        double size = fileSize;
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

    /**
     * Verifica si la imagen es de tipo JPEG
     */
    public boolean isJpeg() {
        return mimeType != null && mimeType.equals("image/jpeg");
    }

    /**
     * Verifica si la imagen es de tipo PNG
     */
    public boolean isPng() {
        return mimeType != null && mimeType.equals("image/png");
    }

    /**
     * Verifica si la imagen es de tipo WebP
     */
    public boolean isWebp() {
        return mimeType != null && mimeType.equals("image/webp");
    }
}