package com.example.fishingapp.service.impl;

import com.example.fishingapp.exception.InvalidImageException;
import com.example.fishingapp.service.ImageProcessingService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Implementación del servicio de procesamiento de imágenes
 * Maneja validación, optimización y manipulación de imágenes
 */
@Service
@Slf4j
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private final Tika tika = new Tika();

    @Value("${app.image.max-size}")
    private long maxFileSize;

    @Value("${app.image.allowed-types}")
    private String allowedTypesString;

    @Value("${app.image.thumbnail.width}")
    private int thumbnailWidth;

    @Value("${app.image.thumbnail.height}")
    private int thumbnailHeight;

    @Override
    public void validateImage(MultipartFile file) {
        log.debug("Iniciando validación de imagen: {}", file.getOriginalFilename());

        // Validar que el archivo no esté vacío
        if (file.isEmpty()) {
            log.warn("Intento de subir archivo vacío");
            throw new InvalidImageException("El archivo está vacío");
        }

        // Validar tamaño
        if (file.getSize() > maxFileSize) {
            log.warn("Archivo excede el tamaño máximo: {} bytes (máximo: {} bytes)",
                    file.getSize(), maxFileSize);
            throw new InvalidImageException(
                    String.format("El archivo excede el tamaño máximo permitido de %d MB",
                            maxFileSize / 1024 / 1024));
        }

        // Validar tipo MIME real (no confiar en la extensión del nombre del archivo)
        String detectedMimeType;
        try {
            detectedMimeType = tika.detect(file.getInputStream());
            log.debug("Tipo MIME detectado: {}", detectedMimeType);
        } catch (IOException e) {
            log.error("Error al leer el archivo para detectar tipo MIME", e);
            throw new InvalidImageException("Error al leer el archivo");
        }

        // Verificar que el tipo MIME esté en la lista de permitidos
        List<String> allowedTypes = Arrays.asList(allowedTypesString.split(","));
        if (!allowedTypes.contains(detectedMimeType)) {
            log.warn("Tipo de archivo no permitido: {}. Permitidos: {}",
                    detectedMimeType, allowedTypesString);
            throw new InvalidImageException(
                    String.format("Tipo de archivo no permitido: %s. Tipos permitidos: %s",
                            detectedMimeType, allowedTypesString));
        }

        // Validar que realmente sea una imagen válida que se pueda procesar
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                log.warn("El archivo no pudo ser procesado como imagen");
                throw new InvalidImageException("El archivo no es una imagen válida");
            }

            // Validar dimensiones mínimas (opcional)
            if (image.getWidth() < 100 || image.getHeight() < 100) {
                log.warn("Imagen con dimensiones muy pequeñas: {}x{}",
                        image.getWidth(), image.getHeight());
                throw new InvalidImageException(
                        "La imagen es demasiado pequeña. Dimensiones mínimas: 100x100 píxeles");
            }

            log.debug("Imagen válida: {}x{} píxeles", image.getWidth(), image.getHeight());

        } catch (IOException e) {
            log.error("Error al procesar la imagen", e);
            throw new InvalidImageException("Error al procesar la imagen");
        }

        log.info("Imagen validada correctamente: {} - {} bytes - {}",
                file.getOriginalFilename(), file.getSize(), detectedMimeType);
    }

    @Override
    public int[] getImageDimensions(InputStream inputStream) {
        try {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                log.warn("No se pudieron obtener dimensiones de la imagen");
                return new int[]{0, 0};
            }

            int width = image.getWidth();
            int height = image.getHeight();
            log.debug("Dimensiones obtenidas: {}x{}", width, height);

            return new int[]{width, height};

        } catch (IOException e) {
            log.error("Error al obtener dimensiones de la imagen", e);
            return new int[]{0, 0};
        }
    }

    @Override
    public ByteArrayInputStream createThumbnail(InputStream originalImage, String outputFormat) {
        try {
            log.debug("Creando thumbnail con formato: {}", outputFormat);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(originalImage)
                    .size(thumbnailWidth, thumbnailHeight)
                    .outputFormat(outputFormat)
                    .outputQuality(0.8) // 80% de calidad para thumbnails
                    .toOutputStream(outputStream);

            byte[] thumbnailBytes = outputStream.toByteArray();
            log.info("Thumbnail creado: {}x{} - {} bytes",
                    thumbnailWidth, thumbnailHeight, thumbnailBytes.length);

            return new ByteArrayInputStream(thumbnailBytes);

        } catch (IOException e) {
            log.error("Error al crear thumbnail", e);
            throw new InvalidImageException("Error al crear miniatura de la imagen", e);
        }
    }

    @Override
    public ByteArrayInputStream optimizeImage(InputStream originalImage, String outputFormat, Integer maxWidth) {
        try {
            log.debug("Optimizando imagen con formato: {} y ancho máximo: {}",
                    outputFormat, maxWidth);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            if (maxWidth != null && maxWidth > 0) {
                // Redimensionar manteniendo el aspect ratio
                Thumbnails.of(originalImage)
                        .width(maxWidth)
                        .outputFormat(outputFormat)
                        .outputQuality(0.85) // 85% de calidad para imágenes optimizadas
                        .toOutputStream(outputStream);
            } else {
                // Solo optimizar sin redimensionar
                Thumbnails.of(originalImage)
                        .scale(1.0)
                        .outputFormat(outputFormat)
                        .outputQuality(0.85)
                        .toOutputStream(outputStream);
            }

            byte[] optimizedBytes = outputStream.toByteArray();
            log.info("Imagen optimizada correctamente - {} bytes", optimizedBytes.length);

            return new ByteArrayInputStream(optimizedBytes);

        } catch (IOException e) {
            log.error("Error al optimizar imagen", e);
            throw new InvalidImageException("Error al optimizar la imagen", e);
        }
    }

    @Override
    public String getOutputFormat(String mimeType) {
        String format = switch (mimeType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };

        log.debug("Formato de salida para {}: {}", mimeType, format);
        return format;
    }

    @Override
    public String detectMimeType(InputStream inputStream) {
        try {
            String mimeType = tika.detect(inputStream);
            log.debug("Tipo MIME detectado: {}", mimeType);
            return mimeType;
        } catch (IOException e) {
            log.error("Error al detectar tipo MIME", e);
            return "application/octet-stream";
        }
    }

    @Override
    public ByteArrayInputStream convertToReusableStream(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            log.debug("Stream convertido a reutilizable: {} bytes", bytes.length);
            return new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            log.error("Error al convertir archivo a stream reutilizable", e);
            throw new InvalidImageException("Error al procesar el archivo", e);
        }
    }
}