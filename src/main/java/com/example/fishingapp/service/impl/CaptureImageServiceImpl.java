package com.example.fishingapp.service.impl;

import com.example.fishingapp.dto.image.ImageDeleteResponseDto;
import com.example.fishingapp.dto.image.ImageResponseDto;
import com.example.fishingapp.dto.image.ImageUploadResponseDto;
import com.example.fishingapp.exception.InvalidImageException;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.exception.UnauthorizedException;
import com.example.fishingapp.mapper.ImageMapper;
import com.example.fishingapp.model.CaptureImage;
import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.repository.CaptureImageRepository;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.service.CaptureImageService;
import com.example.fishingapp.service.ImageProcessingService;
import com.example.fishingapp.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de gestión de imágenes de capturas
 * Orquesta todo el flujo: validación -> procesamiento -> almacenamiento -> persistencia
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaptureImageServiceImpl implements CaptureImageService {

    private final ImageProcessingService imageProcessingService;
    private final StorageService storageService;
    private final CaptureImageRepository captureImageRepository;
    private final FishCaptureRepository fishCaptureRepository;
    private final ImageMapper imageMapper;

    // Necesitamos acceder a métodos específicos de S3StorageServiceImpl
    private final CloudinaryStorageServiceImpl cloudinaryStorageService;

    @Value("${app.image.max-images-per-capture}")
    private int maxImagesPerCapture;

    @Override
    @Transactional
    public ImageResponseDto uploadImage(Long captureId, Long userId, MultipartFile file) {
        log.info("Iniciando subida de imagen para captura {} por usuario {}", captureId, userId);

        // 1. Verificar que la captura existe y pertenece al usuario
        FishCapture capture = validateCaptureOwnership(captureId, userId);

        // 2. Verificar límite de imágenes
        long currentImageCount = captureImageRepository.countByFishCaptureId(captureId);

        log.info("🔍 DEBUG: currentImageCount={}, maxImagesPerCapture={}, condition={}",
                currentImageCount, maxImagesPerCapture, (currentImageCount >= maxImagesPerCapture));


        if (currentImageCount >= maxImagesPerCapture) {
            throw new InvalidImageException(
                    String.format("Se ha alcanzado el límite máximo de %d imágenes por captura",
                            maxImagesPerCapture));
        }

        // 3. Validar la imagen
        imageProcessingService.validateImage(file);

        // 4. Procesar y subir la imagen
        CaptureImage savedImage = processAndUploadImage(file, capture, userId);

        log.info("Imagen subida exitosamente: ID {}", savedImage.getId());
        return imageMapper.toDto(savedImage);
    }

    @Override
    @Transactional
    public ImageUploadResponseDto uploadMultipleImages(Long captureId, Long userId, MultipartFile[] files) {
        log.info("Iniciando subida de {} imágenes para captura {} por usuario {}",
                files.length, captureId, userId);

        // 1. Verificar que la captura existe y pertenece al usuario
        FishCapture capture = validateCaptureOwnership(captureId, userId);

        // 2. Verificar límite de imágenes
        long currentImageCount = captureImageRepository.countByFishCaptureId(captureId);
        int newImageCount = files.length;

        if (currentImageCount + newImageCount > maxImagesPerCapture) {
            throw new InvalidImageException(
                    String.format("No se pueden subir %d imágenes. Límite: %d, actuales: %d",
                            newImageCount, maxImagesPerCapture, currentImageCount));
        }

        // 3. Procesar cada imagen
        List<ImageResponseDto> uploadedImages = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            try {
                // Validar
                imageProcessingService.validateImage(file);

                // Procesar y subir
                CaptureImage savedImage = processAndUploadImage(file, capture, userId);
                uploadedImages.add(imageMapper.toDto(savedImage));

                log.info("Imagen {}/{} subida exitosamente", i + 1, files.length);

            } catch (Exception e) {
                String errorMsg = String.format("Error en imagen '%s': %s",
                        file.getOriginalFilename(), e.getMessage());
                errors.add(errorMsg);
                log.error("Error al procesar imagen {}/{}: {}", i + 1, files.length, e.getMessage());
            }
        }

        // 4. Construir respuesta
        String message = buildUploadResultMessage(uploadedImages.size(), errors.size(), files.length);

        if (!errors.isEmpty()) {
            log.warn("Subida completada con {} errores", errors.size());
        }

        return new ImageUploadResponseDto(
                captureId,
                uploadedImages,
                uploadedImages.size(),
                message
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageResponseDto> getImagesByCapture(Long captureId) {
        log.debug("Obteniendo imágenes de captura {}", captureId);

        // Verificar que la captura existe
        if (!fishCaptureRepository.existsById(captureId)) {
            throw new ResourceNotFoundException("Captura no encontrada con ID: " + captureId);
        }

        List<CaptureImage> images = captureImageRepository.findByFishCaptureId(captureId);
        log.debug("Encontradas {} imágenes para captura {}", images.size(), captureId);

        return imageMapper.toDtoList(images);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageResponseDto getImageById(Long imageId) {
        log.debug("Obteniendo imagen con ID {}", imageId);

        CaptureImage image = captureImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + imageId));

        return imageMapper.toDto(image);
    }

    @Override
    @Transactional
    public ImageDeleteResponseDto deleteImage(Long imageId, Long userId) {
        log.info("Eliminando imagen {} por usuario {}", imageId, userId);

        // 1. Obtener la imagen
        CaptureImage image = captureImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + imageId));

        // 2. Verificar permisos
        Long captureId = image.getFishCapture().getId();
        if (!canUserModifyImages(captureId, userId)) {
            throw new UnauthorizedException("No tienes permisos para eliminar esta imagen");
        }

        // 3. Eliminar de S3
        try {
            storageService.deleteFile(image.getS3Key());

            // Eliminar también el thumbnail
            String thumbnailKey = extractS3KeyFromUrl(image.getThumbnailUrl());
            storageService.deleteFile(thumbnailKey);

            log.info("Archivos eliminados de S3 correctamente");
        } catch (Exception e) {
            log.error("Error al eliminar archivos de S3: {}", e.getMessage());
            // Continuamos con la eliminación de BD aunque falle S3
        }

        // 4. Eliminar de la base de datos
        captureImageRepository.delete(image);

        log.info("Imagen {} eliminada exitosamente", imageId);
        return ImageDeleteResponseDto.success(imageId, captureId);
    }

    @Override
    @Transactional
    public void deleteAllImagesByCapture(Long captureId, Long userId) {
        log.info("Eliminando todas las imágenes de captura {} por usuario {}", captureId, userId);

        // Verificar permisos
        if (!canUserModifyImages(captureId, userId)) {
            throw new UnauthorizedException("No tienes permisos para eliminar estas imágenes");
        }

        List<CaptureImage> images = captureImageRepository.findByFishCaptureId(captureId);

        // Eliminar cada imagen
        for (CaptureImage image : images) {
            try {
                // Eliminar de S3
                storageService.deleteFile(image.getS3Key());
                String thumbnailKey = extractS3KeyFromUrl(image.getThumbnailUrl());
                storageService.deleteFile(thumbnailKey);
            } catch (Exception e) {
                log.error("Error al eliminar imagen {} de S3: {}", image.getId(), e.getMessage());
            }
        }

        // Eliminar de BD
        captureImageRepository.deleteByFishCaptureId(captureId);

        log.info("Eliminadas {} imágenes de captura {}", images.size(), captureId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserModifyImages(Long captureId, Long userId) {
        FishCapture capture = fishCaptureRepository.findById(captureId)
                .orElseThrow(() -> new ResourceNotFoundException("Captura no encontrada con ID: " + captureId));

        boolean canModify = capture.getUser().getId().equals(userId);
        log.debug("Usuario {} {} modificar imágenes de captura {}",
                userId, canModify ? "puede" : "NO puede", captureId);

        return canModify;
    }

    /**
     * Elimina todas las imágenes de una captura SIN validar userId
     * Se usa internamente cuando se elimina una captura (cascada)
     */
    @Override
    @Transactional
    public void deleteAllImagesByCaptureInternal(Long captureId) {
        log.info("🗑️ Eliminando todas las imágenes de captura {} (cascada interna)", captureId);

        List<CaptureImage> images = captureImageRepository.findByFishCaptureId(captureId);

        if (images.isEmpty()) {
            log.info("ℹ️ No hay imágenes para eliminar en captura {}", captureId);
            return;
        }

        // Eliminar cada imagen de S3
        for (CaptureImage image : images) {
            try {
                // Eliminar imagen original
                storageService.deleteFile(image.getS3Key());
                log.debug("✅ Imagen original eliminada de S3: {}", image.getS3Key());

                // Eliminar thumbnail
                String thumbnailKey = extractS3KeyFromUrl(image.getThumbnailUrl());
                storageService.deleteFile(thumbnailKey);
                log.debug("✅ Thumbnail eliminado de S3: {}", thumbnailKey);

            } catch (Exception e) {
                log.error("⚠️ Error al eliminar imagen {} de S3: {}", image.getId(), e.getMessage());
                // Continuar con las demás aunque falle una
            }
        }

        // Eliminar de BD
        captureImageRepository.deleteByFishCaptureId(captureId);

        log.info("✅ Eliminadas {} imágenes de captura {}", images.size(), captureId);
    }


    @Override
    public int countImagesByCapture(Long captureId) {
        return captureImageRepository.findByFishCaptureId(captureId).size();
    }

    // ========== MÉTODOS PRIVADOS AUXILIARES ==========

    /**
     * Valida que la captura existe y pertenece al usuario
     */
    private FishCapture validateCaptureOwnership(Long captureId, Long userId) {
        FishCapture capture = fishCaptureRepository.findById(captureId)
                .orElseThrow(() -> new ResourceNotFoundException("Captura no encontrada con ID: " + captureId));

        if (!capture.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permisos para modificar esta captura");
        }

        return capture;
    }

    private CaptureImage processAndUploadImage(MultipartFile file, FishCapture capture, Long userId) {
        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║  PROCESANDO Y SUBIENDO IMAGEN                         ║");
        log.info("╚════════════════════════════════════════════════════════╝");
        log.info("Archivo original: {}", file.getOriginalFilename());
        log.info("Tamaño: {} bytes", file.getSize());
        log.info("Content Type: {}", file.getContentType());
        log.info("Captura ID: {}", capture.getId());
        log.info("Usuario ID: {}", userId);

        try {
            // 1. Detectar tipo MIME y formato
            log.info("📋 Paso 1: Detectando tipo MIME...");
            ByteArrayInputStream reusableStream = imageProcessingService.convertToReusableStream(file);
            String mimeType = imageProcessingService.detectMimeType(reusableStream);
            String outputFormat = imageProcessingService.getOutputFormat(mimeType);
            log.info("  ✓ MIME type: {}", mimeType);
            log.info("  ✓ Output format: {}", outputFormat);

            // 2. Obtener dimensiones originales
            log.info("📐 Paso 2: Obteniendo dimensiones...");
            reusableStream.reset();
            int[] dimensions = imageProcessingService.getImageDimensions(reusableStream);
            log.info("  ✓ Dimensiones: {}x{}", dimensions[0], dimensions[1]);

            // 3. Optimizar imagen original
            log.info("🔧 Paso 3: Optimizando imagen...");
            reusableStream.reset();
            ByteArrayInputStream optimizedImage = imageProcessingService.optimizeImage(
                    reusableStream, outputFormat, 1920);
            log.info("  ✓ Imagen optimizada. Tamaño: {} bytes", optimizedImage.available());

            // 4. Crear thumbnail
            log.info("🖼️ Paso 4: Creando thumbnail...");
            reusableStream.reset();
            ByteArrayInputStream thumbnail = imageProcessingService.createThumbnail(
                    reusableStream, outputFormat);
            log.info("  ✓ Thumbnail creado. Tamaño: {} bytes", thumbnail.available());

            // 5. Generar nombres de archivo únicos
            log.info("📝 Paso 5: Generando nombres de archivo...");
            String originalFileName = file.getOriginalFilename();
            String sanitizedFileName = sanitizeFileName(originalFileName);
            log.info("  ✓ Nombre sanitizado: {}", sanitizedFileName);

            // 6. Construir keys para S3
            log.info("🔑 Paso 6: Construyendo keys para S3...");
            String originalKey = storageService.buildFileKey(userId, capture.getId(), sanitizedFileName);
            String thumbnailKey = cloudinaryStorageService.buildThumbnailKey(userId, capture.getId(), sanitizedFileName);
            log.info("  ✓ Key original: {}", originalKey);
            log.info("  ✓ Key thumbnail: {}", thumbnailKey);

            // 7. Subir imagen original a S3
            log.info("☁️ Paso 7: Subiendo imagen original a S3...");
            String originalUrl = storageService.uploadFile(
                    originalKey,
                    optimizedImage,
                    optimizedImage.available(),
                    mimeType);
            log.info("  ✅ URL imagen original: {}", originalUrl);

            // 8. Subir thumbnail a S3
            log.info("☁️ Paso 8: Subiendo thumbnail a S3...");
            String thumbnailUrl = storageService.uploadFile(
                    thumbnailKey,
                    thumbnail,
                    thumbnail.available(),
                    mimeType);
            log.info("  ✅ URL thumbnail: {}", thumbnailUrl);

            // 9. Crear entidad y guardar en BD
            log.info("💾 Paso 9: Guardando en base de datos...");
            CaptureImage captureImage = CaptureImage.builder()
                    .originalUrl(originalUrl)
                    .thumbnailUrl(thumbnailUrl)
                    .fileName(sanitizedFileName)
                    .fileSize(file.getSize())
                    .mimeType(mimeType)
                    .width(dimensions[0])
                    .height(dimensions[1])
                    .s3Key(originalKey)
                    .fishCapture(capture)
                    .build();

            CaptureImage savedImage = captureImageRepository.save(captureImage);
            log.info("  ✅ Imagen guardada con ID: {}", savedImage.getId());

            log.info("╔════════════════════════════════════════════════════════╗");
            log.info("║  ✅ PROCESO COMPLETADO EXITOSAMENTE                    ║");
            log.info("╚════════════════════════════════════════════════════════╝");

            return savedImage;

        } catch (Exception e) {
            log.error("╔════════════════════════════════════════════════════════╗");
            log.error("║  ❌ ERROR EN EL PROCESO                                ║");
            log.error("╚════════════════════════════════════════════════════════╝");
            log.error("Error en paso: {}", e.getMessage());
            log.error("Stack trace completo:", e);
            throw new RuntimeException("Error procesando imagen: " + e.getMessage(), e);
        }
    }

    /**
     * Construye mensaje de resultado de subida múltiple
     */
    private String buildUploadResultMessage(int successful, int failed, int total) {
        if (failed == 0) {
            return String.format("%d imagen(es) subida(s) correctamente", successful);
        } else {
            return String.format("%d de %d imagen(es) subida(s) correctamente. %d error(es)",
                    successful, total, failed);
        }
    }

    /**
     * Extrae la key de S3 desde una URL completa
     */
    private String extractS3KeyFromUrl(String url) {
        // URL formato: https://s3.tebi.io/bucket-name/key
        String[] parts = url.split("/");
        // Obtener todo después del nombre del bucket
        StringBuilder key = new StringBuilder();
        for (int i = 4; i < parts.length; i++) {
            if (i > 4) key.append("/");
            key.append(parts[i]);
        }
        return key.toString();
    }

    /**
     * Sanitiza nombre de archivo
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "image.jpg";
        }
        return fileName
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_")
                .toLowerCase();
    }
}
