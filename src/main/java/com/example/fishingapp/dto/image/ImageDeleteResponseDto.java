package com.example.fishingapp.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Dto para respuesta de eliminación de imagen
 */
@Schema(description = "Respuesta después de eliminar una imagen")
public record ImageDeleteResponseDto(

        @Schema(description = "ID de la imagen eliminada", example = "10")
        Long imageId,

        @Schema(description = "ID de la captura asociada", example = "5")
        Long captureId,

        @Schema(description = "Indica si se eliminó correctamente", example = "true")
        boolean deleted,

        @Schema(description = "Mensaje descriptivo", example = "Imagen eliminada correctamente")
        String message
) {

    /**
     * Constructor de conveniencia para eliminación exitosa
     */
    public static ImageDeleteResponseDto success(Long imageId, Long captureId) {
        return new ImageDeleteResponseDto(
                imageId,
                captureId,
                true,
                "Imagen eliminada correctamente"
        );
    }

    /**
     * Constructor de conveniencia para error
     */
    public static ImageDeleteResponseDto failure(Long imageId, Long captureId, String reason) {
        return new ImageDeleteResponseDto(
                imageId,
                captureId,
                false,
                "Error al eliminar imagen: " + reason
        );
    }
}