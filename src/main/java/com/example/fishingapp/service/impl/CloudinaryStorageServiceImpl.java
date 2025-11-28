package com.example.fishingapp.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.fishingapp.dto.FileMetaData;
import com.example.fishingapp.exception.StorageException;
import com.example.fishingapp.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryStorageServiceImpl implements StorageService {

    public static final String IMAGE = "image";
    public static final String RESOURCE_TYPE = "resource_type";
    private final Cloudinary cloudinary;

    private static final String FOLDER_CAPTURES = "fish-captures/captures";
    private static final String FOLDER_THUMBNAILS = "fish-captures/thumbnails";

    @Override
    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        log.info("════════════════════════════════════════════════════════");
        log.info("📤 INICIANDO SUBIDA A CLOUDINARY");
        log.info("════════════════════════════════════════════════════════");
        log.info("Key: {}", key);
        log.info("Content Length: {} bytes", contentLength);
        log.info("Content Type: {}", contentType);

        try {
            // Extraer el folder y el public_id del key
            String folder = extractFolder(key);
            String publicId = extractPublicId(key);

            log.info("📁 Folder: {}", folder);
            log.info("🆔 Public ID: {}", publicId);

            // Subir a Cloudinary
            Map uploadResult = cloudinary.uploader().upload(inputStream.readAllBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "public_id", publicId,
                            RESOURCE_TYPE, IMAGE,
                            "overwrite", true,
                            "quality", "auto:good", // Optimización automática
                            "fetch_format", "auto"   // Formato automático (WebP cuando sea posible)
                    )
            );

            String url = (String) uploadResult.get("secure_url");

            log.info("✅ Archivo subido exitosamente");
            log.info("🔗 URL: {}", url);
            log.info("📊 Bytes: {}", uploadResult.get("bytes"));
            log.info("📐 Dimensiones: {}x{}", uploadResult.get("width"), uploadResult.get("height"));
            log.info("════════════════════════════════════════════════════════");
            log.info("✅ SUBIDA COMPLETADA");
            log.info("════════════════════════════════════════════════════════");

            return url;

        } catch (IOException e) {
            log.error("════════════════════════════════════════════════════════");
            log.error("❌ ERROR AL SUBIR A CLOUDINARY");
            log.error("════════════════════════════════════════════════════════");
            log.error("Error: {}", e.getMessage(), e);
            throw new StorageException("Error al subir archivo a Cloudinary", e);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            log.info("🗑️ Eliminando archivo de Cloudinary: {}", key);

            String publicId = extractFullPublicId(key);
            log.info("🆔 Public ID completo: {}", publicId);

            Map result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap(RESOURCE_TYPE, IMAGE)
            );

            String resultStatus = (String) result.get("result");

            if ("ok".equals(resultStatus)) {
                log.info("✅ Archivo eliminado correctamente");
            } else {
                log.warn("⚠️ Resultado de eliminación: {}", resultStatus);
            }

        } catch (IOException e) {
            log.error("❌ Error al eliminar archivo: {}", e.getMessage(), e);
            throw new StorageException("Error al eliminar archivo de Cloudinary", e);
        }
    }

    @Override
    public boolean fileExists(String key) {
        try {
            String publicId = extractFullPublicId(key);

            Map result = cloudinary.api().resource(publicId,
                    ObjectUtils.asMap(RESOURCE_TYPE, IMAGE)
            );

            return result != null && result.containsKey("public_id");

        } catch (Exception e) {
            log.debug("Archivo no existe: {}", key);
            return false;
        }
    }

    @Override
    public FileMetaData getFileMetadata(String key) {
        try {
            String publicId = extractFullPublicId(key);

            Map result = cloudinary.api().resource(publicId,
                    ObjectUtils.asMap(RESOURCE_TYPE, IMAGE)
            );

            long bytes = Long.parseLong(result.get("bytes").toString());
            String format = (String) result.get("format");
            String createdAt = (String) result.get("created_at");

            // Cloudinary devuelve "2025-02-23T12:45:00Z" → se puede convertir directo
            Instant lastModified = Instant.parse(createdAt);

            return new FileMetaData(bytes, "image/" + format, lastModified);

        } catch (Exception e) {
            log.error("Error obteniendo metadatos: {}", e.getMessage());
            throw new StorageException("Error obteniendo metadatos", e);
        }
    }


    @Override
    public String buildFileKey(Long userId, Long captureId, String fileName) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sanitized = sanitizeFileName(fileName);

        // Formato: captures/user_123/capture_456/20241111_120000_image.jpg
        return String.format("%s/user_%d/capture_%d/%s_%s",
                FOLDER_CAPTURES,
                userId,
                captureId,
                timestamp,
                sanitized);
    }

    /**
     * Construye la clave para un thumbnail
     */
    public String buildThumbnailKey(Long userId, Long captureId, String fileName) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sanitized = sanitizeFileName(fileName);

        return String.format("%s/user_%d/capture_%d/%s_thumb_%s",
                FOLDER_THUMBNAILS,
                userId,
                captureId,
                timestamp,
                sanitized);
    }

    /**
     * Extrae el folder del key (todo menos el nombre del archivo)
     */
    private String extractFolder(String key) {
        int lastSlash = key.lastIndexOf('/');
        if (lastSlash > 0) {
            return key.substring(0, lastSlash);
        }
        return "";
    }

    /**
     * Extrae el public_id del key (nombre del archivo sin extensión)
     */
    private String extractPublicId(String key) {
        int lastSlash = key.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? key.substring(lastSlash + 1) : key;

        // Remover extensión
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(0, lastDot);
        }
        return fileName;
    }

    /**
     * Extrae el public_id completo (folder + nombre sin extensión)
     */
    private String extractFullPublicId(String key) {
        // Remover extensión del key completo
        int lastDot = key.lastIndexOf('.');
        if (lastDot > 0) {
            return key.substring(0, lastDot);
        }
        return key;
    }

    /**
     * Sanitiza el nombre del archivo
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) return IMAGE;

        return fileName
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_")
                .toLowerCase();
    }
}