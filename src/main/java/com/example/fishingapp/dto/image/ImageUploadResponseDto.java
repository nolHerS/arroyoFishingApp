package com.example.fishingapp.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Dto para respuesta de subida de imágenes (simple o múltiple)
 */
@Schema(description = "Respuesta después de subir imágenes a una captura")
public record ImageUploadResponseDto(

        @Schema(description = "ID de la captura", example = "5")
        Long captureId,

        @Schema(description = "Lista de imágenes subidas exitosamente")
        List<ImageResponseDto> uploadedImages,

        @Schema(description = "Número total de imágenes subidas", example = "3")
        int totalImages,

        @Schema(description = "Mensaje descriptivo del resultado", example = "3 imágenes subidas correctamente")
        String message
) {

    /**
     * Constructor compacto con validación
     */
    public ImageUploadResponseDto {
        if (captureId == null || captureId <= 0) {
            throw new IllegalArgumentException("El ID de captura debe ser válido");
        }
        if (uploadedImages == null) {
            uploadedImages = List.of();
        }
        if (totalImages < 0) {
            totalImages = 0;
        }
    }

    /**
     * Constructor de conveniencia para crear respuesta exitosa
     */
    public static ImageUploadResponseDto success(Long captureId, List<ImageResponseDto> images) {
        int count = images != null ? images.size() : 0;
        String msg = count == 1
                ? "1 imagen subida correctamente"
                : String.format("%d imágenes subidas correctamente", count);

        return new ImageUploadResponseDto(captureId, images, count, msg);
    }

    /**
     * Constructor de conveniencia para crear respuesta con error
     */
    public static ImageUploadResponseDto error(Long captureId, String errorMessage) {
        return new ImageUploadResponseDto(captureId, List.of(), 0, errorMessage);
    }

    /**
     * Verifica si la subida fue exitosa
     */
    public boolean isSuccessful() {
        return totalImages > 0 && uploadedImages != null && !uploadedImages.isEmpty();
    }
}