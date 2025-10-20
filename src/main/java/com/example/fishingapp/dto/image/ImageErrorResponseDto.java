package com.example.fishingapp.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dto para respuestas de error en la validación de imágenes
 */
@Schema(description = "Información de error al procesar imágenes")
public record ImageErrorResponseDto(

        @Schema(description = "Timestamp del error")
        LocalDateTime timestamp,

        @Schema(description = "Código de estado HTTP", example = "400")
        int status,

        @Schema(description = "Mensaje principal del error", example = "Error al validar imagen")
        String message,

        @Schema(description = "Lista de errores específicos")
        List<String> errors,

        @Schema(description = "Ruta del endpoint", example = "/api/captures/5/images")
        String path
) {

    /**
     * Constructor compacto
     */
    public ImageErrorResponseDto {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (errors == null) {
            errors = List.of();
        }
    }

    /**
     * Constructor de conveniencia para un solo error
     */
    public static ImageErrorResponseDto singleError(int status, String message, String error, String path) {
        return new ImageErrorResponseDto(
                LocalDateTime.now(),
                status,
                message,
                List.of(error),
                path
        );
    }

    /**
     * Constructor de conveniencia para múltiples errores
     */
    public static ImageErrorResponseDto multipleErrors(int status, String message, List<String> errors, String path) {
        return new ImageErrorResponseDto(
                LocalDateTime.now(),
                status,
                message,
                errors,
                path
        );
    }
}