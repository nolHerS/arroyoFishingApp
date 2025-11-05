package com.example.fishingapp.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Interfaz para el servicio de procesamiento de imágenes
 * Define el contrato para validación, optimización y manipulación de imágenes
 */
public interface ImageProcessingService {

    /**
     * Valida que el archivo sea una imagen válida
     *
     * @param file Archivo a validar
     * @throws com.example.fishingapp.exception.InvalidImageException si la imagen no es válida
     */
    void validateImage(MultipartFile file);

    /**
     * Obtiene las dimensiones de una imagen
     *
     * @param inputStream Stream de la imagen
     * @return Array con [ancho, alto]
     */
    int[] getImageDimensions(InputStream inputStream);

    /**
     * Crea un thumbnail (miniatura) de una imagen
     *
     * @param originalImage Stream de la imagen original
     * @param outputFormat Formato de salida (jpg, png, webp)
     * @return Stream del thumbnail generado
     */
    ByteArrayInputStream createThumbnail(InputStream originalImage, String outputFormat);

    /**
     * Optimiza una imagen reduciendo su calidad si es necesario
     *
     * @param originalImage Stream de la imagen original
     * @param outputFormat Formato de salida
     * @param maxWidth Ancho máximo (null para no limitar)
     * @return Stream de la imagen optimizada
     */
    ByteArrayInputStream optimizeImage(InputStream originalImage, String outputFormat, Integer maxWidth);

    /**
     * Obtiene el formato de salida basado en el tipo MIME
     *
     * @param mimeType Tipo MIME de la imagen
     * @return Formato de salida (jpg, png, webp)
     */
    String getOutputFormat(String mimeType);

    /**
     * Detecta el tipo MIME real de un archivo
     *
     * @param inputStream Stream del archivo
     * @return Tipo MIME detectado
     */
    String detectMimeType(InputStream inputStream);

    /**
     * Convierte el InputStream de un MultipartFile a ByteArrayInputStream
     * para poder reutilizarlo múltiples veces
     *
     * @param file Archivo multipart
     * @return ByteArrayInputStream que puede ser reutilizado
     */
    ByteArrayInputStream convertToReusableStream(MultipartFile file);
}