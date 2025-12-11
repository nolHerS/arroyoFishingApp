package com.example.fishingapp.controller;

import com.example.fishingapp.dto.image.ImageDeleteResponseDto;
import com.example.fishingapp.dto.image.ImageResponseDto;
import com.example.fishingapp.dto.image.ImageUploadResponseDto;
import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.service.CaptureImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST para gestionar imágenes de capturas de peces
 */
@RestController
@RequestMapping("/api/captures")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Imágenes de Capturas", description = "Endpoints para gestionar imágenes de capturas de peces")
public class CaptureImageController {

    private final CaptureImageService captureImageService;

    /**
     * Sube una imagen a una captura específica
     */
    @PostMapping(value = "/{captureId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Subir una imagen a una captura",
            description = "Permite subir una imagen a una captura de pez existente. " +
                    "La imagen será validada, optimizada y almacenada en S3.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Imagen subida exitosamente",
                    content = @Content(schema = @Schema(implementation = ImageResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Imagen inválida o límite excedido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para modificar esta captura"),
            @ApiResponse(responseCode = "404", description = "Captura no encontrada")
    })
    public ResponseEntity<ImageResponseDto> uploadImage(
            @Parameter(description = "ID de la captura", required = true)
            @PathVariable Long captureId,

            @Parameter(description = "Archivo de imagen (JPEG, PNG o WebP, máx 10MB)", required = true)
            @RequestParam("file") MultipartFile file,

            @AuthenticationPrincipal AuthUser authUser
    ) {
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  RECIBIDA PETICIÓN DE SUBIDA DE IMAGEN                  ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("Captura ID: {}", captureId);
        log.info("Usuario: {} (ID: {})", authUser.getUsername(), authUser.getUser().getId());
        log.info("Archivo recibido:");
        log.info("  - Nombre: {}", file.getOriginalFilename());
        log.info("  - Tamaño: {} bytes ({} MB)", file.getSize(), file.getSize() / 1024.0 / 1024.0);
        log.info("  - Content Type: {}", file.getContentType());
        log.info("  - isEmpty: {}", file.isEmpty());

        Long userId = authUser.getUser().getId();

        log.info("🚀 Llamando al servicio de imágenes...");
        ImageResponseDto response = captureImageService.uploadImage(captureId, userId, file);

        log.info("✅ Imagen subida exitosamente");
        log.info("  - ID de imagen: {}", response.id());
        log.info("  - URL: {}", response.originalUrl());
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  ✅ PETICIÓN COMPLETADA CON ÉXITO                        ║");
        log.info("╚══════════════════════════════════════════════════════════╝");


        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Sube múltiples imágenes a una captura
     */
    @PostMapping(value = "/{captureId}/images/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Subir múltiples imágenes a una captura",
            description = "Permite subir varias imágenes a la vez a una captura existente. " +
                    "Cada imagen será procesada individualmente.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Imágenes procesadas (puede haber errores parciales)",
                    content = @Content(schema = @Schema(implementation = ImageUploadResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Error en validación o límite excedido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Captura no encontrada")
    })
    public ResponseEntity<ImageUploadResponseDto> uploadMultipleImages(
            @Parameter(description = "ID de la captura", required = true)
            @PathVariable Long captureId,

            @Parameter(description = "Array de archivos de imagen", required = true)
            @RequestParam("files") MultipartFile[] files,

            @AuthenticationPrincipal AuthUser authUser
    ) {
        log.info("POST /api/captures/{}/images/multiple - Subiendo {} imágenes por usuario {}",
                captureId, files.length, authUser.getUsername());

        Long userId = authUser.getUser().getId();
        ImageUploadResponseDto response = captureImageService.uploadMultipleImages(captureId, userId, files);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene todas las imágenes de una captura
     */
    @GetMapping("/{captureId}/images")
    @Operation(
            summary = "Obtener todas las imágenes de una captura",
            description = "Devuelve la lista completa de imágenes asociadas a una captura específica. " +
                    "Este endpoint es público."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de imágenes obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ImageResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Captura no encontrada")
    })
    public ResponseEntity<List<ImageResponseDto>> getImagesByCapture(
            @Parameter(description = "ID de la captura", required = true)
            @PathVariable Long captureId
    ) {
        log.debug("GET /api/captures/{}/images", captureId);

        List<ImageResponseDto> images = captureImageService.getImagesByCapture(captureId);
        return ResponseEntity.ok(images);
    }

    /**
     * Obtiene una imagen específica por su ID
     */
    @GetMapping("/images/{imageId}")
    @Operation(
            summary = "Obtener una imagen específica",
            description = "Obtiene los detalles de una imagen específica por su ID. Este endpoint es público."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Imagen encontrada",
                    content = @Content(schema = @Schema(implementation = ImageResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Imagen no encontrada")
    })
    public ResponseEntity<ImageResponseDto> getImageById(
            @Parameter(description = "ID de la imagen", required = true)
            @PathVariable Long imageId
    ) {
        log.debug("GET /api/captures/images/{}", imageId);

        ImageResponseDto image = captureImageService.getImageById(imageId);
        return ResponseEntity.ok(image);
    }

    /**
     * Elimina una imagen específica
     */
    @DeleteMapping("/images/{imageId}")
    @Operation(
            summary = "Eliminar una imagen",
            description = "Elimina una imagen específica del almacenamiento y la base de datos. " +
                    "Solo el propietario de la captura puede eliminar imágenes.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Imagen eliminada exitosamente",
                    content = @Content(schema = @Schema(implementation = ImageDeleteResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para eliminar esta imagen"),
            @ApiResponse(responseCode = "404", description = "Imagen no encontrada")
    })
    public ResponseEntity<ImageDeleteResponseDto> deleteImage(
            @Parameter(description = "ID de la imagen a eliminar", required = true)
            @PathVariable Long imageId,

            @AuthenticationPrincipal AuthUser authUser
    ) {
        log.info("DELETE /api/captures/images/{} por usuario {}", imageId, authUser.getUsername());

        Long userId = authUser.getUser().getId();
        ImageDeleteResponseDto response = captureImageService.deleteImage(imageId, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * Elimina todas las imágenes de una captura
     */
    @DeleteMapping("/{captureId}/images")
    @Operation(
            summary = "Eliminar todas las imágenes de una captura",
            description = "Elimina todas las imágenes asociadas a una captura. " +
                    "Solo el propietario puede realizar esta acción.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Imágenes eliminadas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Captura no encontrada")
    })
    public ResponseEntity<Void> deleteAllImagesByCapture(
            @Parameter(description = "ID de la captura", required = true)
            @PathVariable Long captureId,

            @AuthenticationPrincipal AuthUser authUser
    ) {
        log.info("DELETE /api/captures/{}/images - Eliminando todas las imágenes por usuario {}",
                captureId, authUser.getUsername());

        Long userId = authUser.getUser().getId();
        captureImageService.deleteAllImagesByCapture(captureId, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene el contador de imágenes de una captura
     */
    @GetMapping("/{captureId}/images/count")
    @Operation(
            summary = "Contar imágenes de una captura",
            description = "Devuelve el número total de imágenes asociadas a una captura. Este endpoint es público."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contador obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Captura no encontrada")
    })
    public ResponseEntity<CountResponseDto> countImages(
            @Parameter(description = "ID de la captura", required = true)
            @PathVariable Long captureId
    ) {
        log.debug("GET /api/captures/{}/images/count", captureId);

        int count = captureImageService.countImagesByCapture(captureId);
        return ResponseEntity.ok(new CountResponseDto(count));
    }

    /**
     * Dto simple para respuesta de contador
     */
    public record CountResponseDto(
            @Schema(description = "Número de imágenes", example = "3")
            int count
    ) {}
}