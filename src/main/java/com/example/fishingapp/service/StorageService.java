package com.example.fishingapp.service;

import com.example.fishingapp.dto.FileMetaData;

import java.io.InputStream;

public interface StorageService {
    /**
     * Sube un archivo al bucket de S3
     *
     * @param key Ruta del archivo en el bucket (ej: "captures/user123/capture456/image.jpg")
     * @param inputStream Stream del archivo a subir
     * @param contentLength Tamaño del archivo en bytes
     * @param contentType Tipo MIME del archivo
     * @return URL pública del archivo subido
     */
    String uploadFile(String key, InputStream inputStream, long contentLength, String contentType);

    /**
     * Elimina un archivo del bucket de S3
     *
     * @param key Ruta del archivo a eliminar
     */
    void deleteFile(String key);

    /**
     * Verifica si un archivo existe en S3
     *
     * @param key Ruta del archivo
     * @return true si existe, false si no
     */
    boolean fileExists(String key);

    /**
     * Obtiene los metadatos de un archivo
     *
     * @param key Ruta del archivo
     * @return Metadatos del archivo
     */
    FileMetaData getFileMetadata(String key);

    /**
     * Construye la clave (key) para almacenar un archivo en S3
     *
     * @param userId ID del usuario
     * @param captureId ID de la captura
     * @param fileName Nombre del archivo
     * @return Clave construida (path completo en el bucket)
     */
    String buildFileKey(Long userId, Long captureId, String fileName);
}
