package com.example.fishingapp.service.impl;

import com.example.fishingapp.config.S3Config;
import com.example.fishingapp.exception.StorageException;
import com.example.fishingapp.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementación del servicio de almacenamiento usando S3/Tebi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageServiceImpl implements StorageService {

    private final S3Client s3Client;
    private final S3Config s3Config;

    private static final String FOLDER_CAPTURES = "captures";
    private static final String FOLDER_THUMBNAILS = "thumbnails";

    @Override
    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            log.info("Subiendo archivo a S3 con key: {}", key);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .acl(ObjectCannedACL.PUBLIC_READ) // Hace el archivo público
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));

            // Construir la URL pública
            String publicUrl = buildPublicUrl(key);

            log.info("Archivo subido exitosamente: {}", publicUrl);
            return publicUrl;

        } catch (S3Exception e) {
            log.error("Error al subir archivo a S3: {}", e.getMessage(), e);
            throw new StorageException("Error al subir el archivo al almacenamiento", e);
        } catch (Exception e) {
            log.error("Error inesperado al subir archivo: {}", e.getMessage(), e);
            throw new StorageException("Error inesperado al subir el archivo", e);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            log.info("Eliminando archivo de S3 con key: {}", key);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Archivo eliminado exitosamente: {}", key);

        } catch (S3Exception e) {
            log.error("Error al eliminar archivo de S3: {}", e.getMessage(), e);
            throw new StorageException("Error al eliminar el archivo del almacenamiento", e);
        } catch (Exception e) {
            log.error("Error inesperado al eliminar archivo: {}", e.getMessage(), e);
            throw new StorageException("Error inesperado al eliminar el archivo", e);
        }
    }


    @Override
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            log.debug("El archivo no existe: {}", key);
            return false;
        } catch (S3Exception e) {
            log.error("Error al verificar existencia del archivo: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public HeadObjectResponse getFileMetadata(String key) {
        try {
            log.debug("Obteniendo metadatos del archivo: {}", key);

            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            log.debug("Metadatos obtenidos para: {}", key);
            return response;

        } catch (S3Exception e) {
            log.error("Error al obtener metadatos del archivo: {}", e.getMessage(), e);
            throw new StorageException("Error al obtener información del archivo", e);
        }
    }

    @Override
    public String buildFileKey(Long userId, Long captureId, String fileName) {
        // Construir path: captures/user_{userId}/capture_{captureId}/{timestamp}_{fileName}
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sanitizedFileName = sanitizeFileName(fileName);

        return String.format("%s/user_%d/capture_%d/%s_%s",
                FOLDER_CAPTURES,
                userId,
                captureId,
                timestamp,
                sanitizedFileName);
    }

    /**
     * Construye la clave para un thumbnail
     */
    public String buildThumbnailKey(Long userId, Long captureId, String fileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sanitizedFileName = sanitizeFileName(fileName);

        return String.format("%s/user_%d/capture_%d/%s_thumb_%s",
                FOLDER_THUMBNAILS,
                userId,
                captureId,
                timestamp,
                sanitizedFileName);
    }

    /**
     * Construye la URL pública del archivo
     */
    private String buildPublicUrl(String key) {
        return String.format("%s/%s/%s",
                s3Config.getEndpoint(),
                s3Config.getBucketName(),
                key);
    }

    /**
     * Sanitiza el nombre del archivo para evitar problemas
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "image";
        }

        // Remover caracteres especiales y espacios
        return fileName
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_") // Reemplazar múltiples guiones bajos por uno solo
                .toLowerCase();
    }
}
