package com.example.fishingapp.service;

import com.example.fishingapp.dto.image.ImageDeleteResponseDto;
import com.example.fishingapp.dto.image.ImageResponseDto;
import com.example.fishingapp.dto.image.ImageUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Interfaz para el servicio principal de gestión de imágenes de capturas
 * Orquesta la validación, procesamiento, almacenamiento y persistencia
 */
public interface CaptureImageService {

    /**
     * Sube una imagen para una captura específica
     *
     * @param captureId ID de la captura
     * @param userId ID del usuario propietario
     * @param file Archivo de imagen a subir
     * @return DTO con información de la imagen subida
     */
    ImageResponseDto uploadImage(Long captureId, Long userId, MultipartFile file);

    /**
     * Sube múltiples imágenes para una captura
     *
     * @param captureId ID de la captura
     * @param userId ID del usuario propietario
     * @param files Array de archivos de imagen
     * @return DTO con información de todas las imágenes subidas
     */
    ImageUploadResponseDto uploadMultipleImages(Long captureId, Long userId, MultipartFile[] files);

    /**
     * Obtiene todas las imágenes de una captura
     *
     * @param captureId ID de la captura
     * @return Lista de DTOs con información de las imágenes
     */
    List<ImageResponseDto> getImagesByCapture(Long captureId);

    /**
     * Obtiene una imagen específica por su ID
     *
     * @param imageId ID de la imagen
     * @return DTO con información de la imagen
     */
    ImageResponseDto getImageById(Long imageId);

    /**
     * Elimina una imagen específica
     *
     * @param imageId ID de la imagen a eliminar
     * @param userId ID del usuario (para verificar permisos)
     * @return DTO con resultado de la eliminación
     */
    ImageDeleteResponseDto deleteImage(Long imageId, Long userId);

    /**
     * Elimina todas las imágenes de una captura
     *
     * @param captureId ID de la captura
     * @param userId ID del usuario (para verificar permisos)
     */
    void deleteAllImagesByCapture(Long captureId, Long userId);

    void deleteAllImagesByCaptureInternal(Long captureId);
    /**
     * Verifica si un usuario puede modificar las imágenes de una captura
     *
     * @param captureId ID de la captura
     * @param userId ID del usuario
     * @return true si tiene permisos, false si no
     */
    boolean canUserModifyImages(Long captureId, Long userId);

    /**
     * Cuenta el número de imágenes de una captura
     *
     * @param captureId ID de la captura
     * @return Número de imágenes
     */
    int countImagesByCapture(Long captureId);

}