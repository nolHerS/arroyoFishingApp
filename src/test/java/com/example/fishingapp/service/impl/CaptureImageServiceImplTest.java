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
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.CaptureImageRepository;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.service.ImageProcessingService;
import com.example.fishingapp.service.StorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CaptureImageServiceImpl
 * Verifica la lógica de negocio de gestión de imágenes de capturas
 *
 * Usa LENIENT strictness para permitir mocks configurados que pueden no ejecutarse
 * debido a flujos condicionales y manejo de excepciones en la implementación
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CaptureImageService - Tests Unitarios")
class CaptureImageServiceImplTest {

    @Mock
    private ImageProcessingService imageProcessingService;

    @Mock
    private StorageService storageService;

    @Mock
    private CaptureImageRepository captureImageRepository;

    @Mock
    private FishCaptureRepository fishCaptureRepository;

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private CloudinaryStorageServiceImpl s3StorageService;

    @InjectMocks
    private CaptureImageServiceImpl captureImageService;

    private User testUser;
    private FishCapture testCapture;
    private MockMultipartFile validImageFile;

    @BeforeEach
    void setUp() {
        // Configurar límite de imágenes
        ReflectionTestUtils.setField(captureImageService, "maxImagesPerCapture", 5);

        // Crear usuario de prueba
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .build();

        // Crear captura de prueba
        testCapture = FishCapture.builder()
                .id(1L)
                .captureDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .fishType("Trucha")
                .location("Rio Tajo")
                .weight(2.5f)
                .user(testUser)
                .images(new ArrayList<>())
                .build();

        // Crear archivo de imagen válido
        validImageFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );
    }

    // ==================== TESTS DE SUBIDA DE UNA IMAGEN ====================

    @Test
    @DisplayName("Debe subir imagen correctamente cuando todo es válido")
    void testUploadImage_Success() throws IOException {
        // Given
        setupSuccessfulImageUploadMocks();

        CaptureImage savedImage = buildSavedCaptureImage();
        when(captureImageRepository.save(any(CaptureImage.class))).thenReturn(savedImage);

        ImageResponseDto expectedResponse = buildImageResponseDto(savedImage);
        when(imageMapper.toDto(any(CaptureImage.class))).thenReturn(expectedResponse);

        // When
        ImageResponseDto result = captureImageService.uploadImage(1L, 1L, validImageFile);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.fileName(), is("test.jpg"));
        assertThat(result.mimeType(), is("image/jpeg"));

        // Verificar interacciones clave
        verify(fishCaptureRepository).findById(1L);
        verify(captureImageRepository).countByFishCaptureId(1L);
        verify(imageProcessingService).validateImage(validImageFile);
        verify(captureImageRepository).save(any(CaptureImage.class));

        // Verificar que se guardó con los datos correctos
        ArgumentCaptor<CaptureImage> imageCaptor = ArgumentCaptor.forClass(CaptureImage.class);
        verify(captureImageRepository).save(imageCaptor.capture());
        CaptureImage capturedImage = imageCaptor.getValue();
        assertThat(capturedImage.getFishCapture(), is(testCapture));
        assertThat(capturedImage.getMimeType(), is("image/jpeg"));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando la captura no existe")
    void testUploadImage_ThrowsResourceNotFoundException_WhenCaptureNotExists() {
        // Given
        when(fishCaptureRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> captureImageService.uploadImage(999L, 1L, validImageFile)
        );

        assertThat(exception.getMessage(), containsString("Captura no encontrada"));
        verify(captureImageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar UnauthorizedException cuando el usuario no es propietario")
    void testUploadImage_ThrowsUnauthorizedException_WhenUserNotOwner() {
        // Given
        when(fishCaptureRepository.findById(1L)).thenReturn(Optional.of(testCapture));

        // When & Then
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> captureImageService.uploadImage(1L, 999L, validImageFile)
        );

        assertThat(exception.getMessage(), containsString("No tienes permisos"));
        verify(captureImageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar InvalidImageException cuando se excede el límite de imágenes")
    void testUploadImage_ThrowsInvalidImageException_WhenLimitExceeded() {
        // Given: La captura existe
        when(fishCaptureRepository.findById(1L)).thenReturn(Optional.of(testCapture));

        // Simular límite de imágenes alcanzado: countByFishCaptureId devuelve 5
        when(captureImageRepository.countByFishCaptureId(1L)).thenReturn(5L);

        // When & Then: la excepción se lanza antes de procesar la imagen
        InvalidImageException exception = assertThrows(
                InvalidImageException.class,
                () -> captureImageService.uploadImage(1L, 1L, validImageFile)
        );

        // Verificar mensaje
        assertThat(exception.getMessage(), containsString("límite máximo"));
        assertThat(exception.getMessage(), containsString("5"));

        // Verificar que no se intenta guardar ninguna imagen
        verify(captureImageRepository, never()).save(any());

        // Evitar que Mockito se queje por stubbings no usados
        verify(fishCaptureRepository).findById(1L);
        verify(captureImageRepository).countByFishCaptureId(1L);
    }


    // ==================== TESTS DE SUBIDA MÚLTIPLE ====================

    @Test
    @DisplayName("Debe subir múltiples imágenes correctamente")
    void testUploadMultipleImages_Success() throws IOException {
        // Given
        MockMultipartFile file1 = new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "content2".getBytes());
        MockMultipartFile[] files = {file1, file2};

        setupSuccessfulImageUploadMocks();

        CaptureImage savedImage = buildSavedCaptureImage();
        when(captureImageRepository.save(any(CaptureImage.class))).thenReturn(savedImage);

        ImageResponseDto responseDto = buildImageResponseDto(savedImage);
        when(imageMapper.toDto(any(CaptureImage.class))).thenReturn(responseDto);

        // When
        ImageUploadResponseDto result = captureImageService.uploadMultipleImages(1L, 1L, files);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.captureId(), is(1L));
        assertThat(result.totalImages(), is(2));
        assertThat(result.uploadedImages(), hasSize(2));
        assertThat(result.message(), containsString("2 imagen(es) subida(s) correctamente"));

        verify(imageProcessingService, times(2)).validateImage(any());
        verify(captureImageRepository, times(2)).save(any(CaptureImage.class));
    }

    @Test
    @DisplayName("Debe manejar errores parciales en subida múltiple")
    void testUploadMultipleImages_PartialFailure() throws IOException {
        // Given
        MockMultipartFile validFile = new MockMultipartFile("file1", "valid.jpg", "image/jpeg", "content".getBytes());
        MockMultipartFile invalidFile = new MockMultipartFile("file2", "invalid.jpg", "image/jpeg", "content".getBytes());
        MockMultipartFile[] files = {validFile, invalidFile};

        // Configurar captura y permisos
        when(fishCaptureRepository.findById(1L)).thenReturn(Optional.of(testCapture));
        when(captureImageRepository.findByFishCaptureId(1L)).thenReturn(List.of());

        // Configurar validación: primera OK, segunda falla
        doAnswer(invocation -> {
            MultipartFile file = invocation.getArgument(0);
            if (file.getOriginalFilename().equals("invalid.jpg")) {
                throw new InvalidImageException("Imagen inválida");
            }
            return null;
        }).when(imageProcessingService).validateImage(any());

        // Configurar procesamiento de imagen
        ByteArrayInputStream reusableStream = new ByteArrayInputStream("test content".getBytes());
        when(imageProcessingService.convertToReusableStream(any())).thenReturn(reusableStream);
        when(imageProcessingService.detectMimeType(any())).thenReturn("image/jpeg");
        when(imageProcessingService.getOutputFormat(any())).thenReturn("jpg");
        when(imageProcessingService.getImageDimensions(any())).thenReturn(new int[]{1920, 1080});
        when(imageProcessingService.optimizeImage(any(), any(), anyInt()))
                .thenReturn(new ByteArrayInputStream("optimized".getBytes()));
        when(imageProcessingService.createThumbnail(any(), any()))
                .thenReturn(new ByteArrayInputStream("thumbnail".getBytes()));

        // Configurar storage
        when(storageService.buildFileKey(anyLong(), anyLong(), any())).thenReturn("captures/test.jpg");
        when(s3StorageService.buildThumbnailKey(anyLong(), anyLong(), any())).thenReturn("thumbnails/thumb.jpg");
        when(storageService.uploadFile(any(), any(), anyLong(), any()))
                .thenReturn("https://s3.tebi.io/bucket/test.jpg");

        // Configurar guardado
        CaptureImage savedImage = buildSavedCaptureImage();
        when(captureImageRepository.save(any(CaptureImage.class))).thenReturn(savedImage);

        ImageResponseDto responseDto = buildImageResponseDto(savedImage);
        when(imageMapper.toDto(any(CaptureImage.class))).thenReturn(responseDto);

        // When
        ImageUploadResponseDto result = captureImageService.uploadMultipleImages(1L, 1L, files);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.totalImages(), is(1)); // Solo 1 exitosa
        assertThat(result.uploadedImages(), hasSize(1));
        assertThat(result.message(), containsString("1 de 2"));
        assertThat(result.message(), containsString("1 error(es)"));

        verify(imageProcessingService, times(2)).validateImage(any());
        verify(captureImageRepository, times(1)).save(any(CaptureImage.class));
    }

    @Test
    @DisplayName("Debe lanzar InvalidImageException cuando múltiples imágenes exceden el límite")
    void testUploadMultipleImages_ThrowsInvalidImageException_WhenLimitExceeded() {
        // Given: crear 4 archivos a subir
        MockMultipartFile[] files = new MockMultipartFile[4];
        for (int i = 0; i < 4; i++) {
            files[i] = new MockMultipartFile(
                    "file" + i,
                    "test" + i + ".jpg",
                    "image/jpeg",
                    "content".getBytes()
            );
        }

        // La captura existe
        when(fishCaptureRepository.findById(1L)).thenReturn(Optional.of(testCapture));

        // Simular 3 imágenes existentes, límite máximo 5
        when(captureImageRepository.countByFishCaptureId(1L)).thenReturn(3L);

        // When & Then: se lanza InvalidImageException antes de procesar cualquier imagen
        InvalidImageException exception = assertThrows(
                InvalidImageException.class,
                () -> captureImageService.uploadMultipleImages(1L, 1L, files)
        );

        // Verificar mensaje
        assertThat(exception.getMessage(), containsString("No se pueden subir 4 imágenes"));
        assertThat(exception.getMessage(), containsString("Límite: 5, actuales: 3"));

        // No se debe intentar guardar ninguna imagen
        verify(captureImageRepository, never()).save(any());

        // Verificar interacciones clave
        verify(fishCaptureRepository).findById(1L);
        verify(captureImageRepository).countByFishCaptureId(1L);
    }


    // ==================== TESTS DE OBTENCIÓN DE IMÁGENES ====================

    @Test
    @DisplayName("Debe obtener todas las imágenes de una captura")
    void testGetImagesByCapture_Success() {
        // Given
        List<CaptureImage> images = List.of(
                CaptureImage.builder().id(1L).fileName("image1.jpg").build(),
                CaptureImage.builder().id(2L).fileName("image2.jpg").build()
        );

        when(fishCaptureRepository.existsById(1L)).thenReturn(true);
        when(captureImageRepository.findByFishCaptureId(1L)).thenReturn(images);

        List<ImageResponseDto> responseDtos = List.of(
                new ImageResponseDto(1L, "url1", "thumb1", "image1.jpg", 1024L, "image/jpeg", 1920, 1080, LocalDateTime.now()),
                new ImageResponseDto(2L, "url2", "thumb2", "image2.jpg", 2048L, "image/jpeg", 1920, 1080, LocalDateTime.now())
        );
        when(imageMapper.toDtoList(images)).thenReturn(responseDtos);

        // When
        List<ImageResponseDto> result = captureImageService.getImagesByCapture(1L);

        // Then
        assertThat(result, hasSize(2));
        assertThat(result.get(0).fileName(), is("image1.jpg"));
        assertThat(result.get(1).fileName(), is("image2.jpg"));
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay imágenes")
    void testGetImagesByCapture_ReturnsEmptyList_WhenNoImages() {
        // Given
        when(fishCaptureRepository.existsById(1L)).thenReturn(true);
        when(captureImageRepository.findByFishCaptureId(1L)).thenReturn(List.of());
        when(imageMapper.toDtoList(List.of())).thenReturn(List.of());

        // When
        List<ImageResponseDto> result = captureImageService.getImagesByCapture(1L);

        // Then
        assertThat(result, empty());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al obtener imágenes de captura inexistente")
    void testGetImagesByCapture_ThrowsResourceNotFoundException() {
        // Given
        when(fishCaptureRepository.existsById(999L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> captureImageService.getImagesByCapture(999L)
        );

        assertThat(exception.getMessage(), containsString("Captura no encontrada"));
    }

    @Test
    @DisplayName("Debe obtener una imagen por ID correctamente")
    void testGetImageById_Success() {
        // Given
        CaptureImage image = CaptureImage.builder()
                .id(1L)
                .fileName("test.jpg")
                .mimeType("image/jpeg")
                .build();

        when(captureImageRepository.findById(1L)).thenReturn(Optional.of(image));

        ImageResponseDto responseDto = new ImageResponseDto(
                1L, "url", "thumb", "test.jpg", 1024L, "image/jpeg", 1920, 1080, LocalDateTime.now()
        );
        when(imageMapper.toDto(image)).thenReturn(responseDto);

        // When
        ImageResponseDto result = captureImageService.getImageById(1L);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.id(), is(1L));
        assertThat(result.fileName(), is("test.jpg"));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al obtener imagen inexistente")
    void testGetImageById_ThrowsResourceNotFoundException() {
        // Given
        when(captureImageRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> captureImageService.getImageById(999L)
        );

        assertThat(exception.getMessage(), containsString("Imagen no encontrada"));
    }

    // ==================== TESTS DE ELIMINACIÓN ====================

    @Test
    @DisplayName("Debe eliminar imagen correctamente")
    void testDeleteImage_Success() {
        // Given
        CaptureImage image = CaptureImage.builder()
                .id(1L)
                .originalUrl("https://s3.tebi.io/bucket/captures/test.jpg")
                .thumbnailUrl("https://s3.tebi.io/bucket/thumbnails/thumb.jpg")
                .s3Key("captures/user_1/capture_1/test.jpg")
                .fishCapture(testCapture)
                .build();

        when(captureImageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(fishCaptureRepository.findById(1L)).thenReturn(Optional.of(testCapture));
        doNothing().when(storageService).deleteFile(anyString());

        // When
        ImageDeleteResponseDto result = captureImageService.deleteImage(1L, 1L);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.deleted(), is(true));
        assertThat(result.imageId(), is(1L));
        assertThat(result.captureId(), is(1L));

        verify(captureImageRepository).delete(image);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al eliminar imagen inexistente")
    void testDeleteImage_ThrowsResourceNotFoundException_WhenImageNotExists() {
        // Given
        when(captureImageRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> captureImageService.deleteImage(999L, 1L)
        );

        assertThat(exception.getMessage(), containsString("Imagen no encontrada"));
        verify(captureImageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe lanzar UnauthorizedException al eliminar imagen sin permisos")
    void testDeleteImage_ThrowsUnauthorizedException_WhenUserNotOwner() {
        // Given
        CaptureImage image = CaptureImage.builder()
                .id(1L)
                .fishCapture(testCapture)
                .build();

        when(captureImageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(fishCaptureRepository.findById(1L)).thenReturn(Optional.of(testCapture));

        // When & Then
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> captureImageService.deleteImage(1L, 999L)
        );

        assertThat(exception.getMessage(), containsString("No tienes permisos"));
        verify(captureImageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe continuar eliminando de BD aunque falle eliminación en S3")
    void testDeleteImage_ContinuesWithDatabaseDeletion_WhenS3DeletionFails() {
        // Given
        CaptureImage image = CaptureImage.builder()
                .id(1L)
                .originalUrl("https://s3.tebi.io/bucket/test.jpg")
                .thumbnailUrl("https://s3.tebi.io/bucket/thumb.jpg")
                .s3Key("test-key")
                .fishCapture(testCapture)
                .build();

        when(captureImageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(fishCaptureRepository.findById(1L)).thenReturn(Optional.of(testCapture));

        // S3 falla pero el servicio debe continuar (la excepción se captura internamente)
        doThrow(new RuntimeException("S3 Error")).when(storageService).deleteFile(anyString());

        // When
        ImageDeleteResponseDto result = captureImageService.deleteImage(1L, 1L);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.deleted(), is(true));

        // Verificar que se eliminó de BD aunque S3 fallara
        verify(captureImageRepository).delete(image);
    }

    @Test
    @DisplayName("Debe eliminar todas las imágenes de una captura")
    void testDeleteAllImagesByCapture_Success() {
        // Given
        List<CaptureImage> images = List.of(
                CaptureImage.builder()
                        .id(1L)
                        .s3Key("key1")
                        .thumbnailUrl("https://s3.tebi.io/bucket/thumb1.jpg")
                        .build(),
                CaptureImage.builder()
                        .id(2L)
                        .s3Key("key2")
                        .thumbnailUrl("https://s3.tebi.io/bucket/thumb2.jpg")
                        .build()
        );

        when(fishCaptureRepository.findById(1L)).thenReturn(Optional.of(testCapture));
        when(captureImageRepository.findByFishCaptureId(1L)).thenReturn(images);
        doNothing().when(storageService).deleteFile(anyString());
        doNothing().when(captureImageRepository).deleteByFishCaptureId(1L);

        // When
        captureImageService.deleteAllImagesByCapture(1L, 1L);

        // Then
        verify(captureImageRepository).deleteByFishCaptureId(1L);
        verify(fishCaptureRepository).findById(1L); // Verifica permisos
    }

    @Test
    @DisplayName("Debe lanzar UnauthorizedException al eliminar todas las imágenes sin permisos")
    void testDeleteAllImagesByCapture_ThrowsUnauthorizedException_WhenUserNotOwner() {
        // Given
        when(fishCaptureRepository.findById(1L)).thenReturn(Optional.of(testCapture));

        // When & Then
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> captureImageService.deleteAllImagesByCapture(1L, 999L)
        );

        assertThat(exception.getMessage(), containsString("No tienes permisos"));
        verify(captureImageRepository, never()).deleteByFishCaptureId(anyLong());
    }

    // ==================== TESTS DE UTILIDAD ====================

    @Test
    @DisplayName("Debe contar imágenes correctamente")
    void testCountImagesByCapture() {
        // Given
        List<CaptureImage> images = List.of(
                new CaptureImage(), new CaptureImage(), new CaptureImage()
        );
        when(captureImageRepository.findByFishCaptureId(1L)).thenReturn(images);

        // When
        int count = captureImageService.countImagesByCapture(1L);

        // Then
        assertThat(count, is(3));
    }

    @Test
    @DisplayName("Debe retornar 0 cuando no hay imágenes")
    void testCountImagesByCapture_ReturnsZero_WhenNoImages() {
        // Given
        when(captureImageRepository.findByFishCaptureId(1L)).thenReturn(List.of());

        // When
        int count = captureImageService.countImagesByCapture(1L);

        // Then
        assertThat(count, is(0));
    }

    @Test
    @DisplayName("Debe verificar permisos correctamente cuando usuario es propietario")
    void testCanUserModifyImages_ReturnsTrue_WhenUserIsOwner() {
        // Given
        when(fishCaptureRepository.findById(1L)).thenReturn(Optional.of(testCapture));

        // When
        boolean canModify = captureImageService.canUserModifyImages(1L, 1L);

        // Then
        assertThat(canModify, is(true));
    }

    @Test
    @DisplayName("Debe verificar permisos correctamente cuando usuario no es propietario")
    void testCanUserModifyImages_ReturnsFalse_WhenUserIsNotOwner() {
        // Given
        when(fishCaptureRepository.findById(1L)).thenReturn(Optional.of(testCapture));

        // When
        boolean canModify = captureImageService.canUserModifyImages(1L, 999L);

        // Then
        assertThat(canModify, is(false));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando captura no existe en verificación de permisos")
    void testCanUserModifyImages_ThrowsResourceNotFoundException_WhenCaptureNotExists() {
        // Given
        when(fishCaptureRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> captureImageService.canUserModifyImages(999L, 1L)
        );

        assertThat(exception.getMessage(), containsString("Captura no encontrada"));
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Configura todos los mocks necesarios para una subida exitosa de imagen
     * Con @MockitoSettings(strictness = LENIENT), no necesitamos lenient() explícito
     */
    private void setupSuccessfulImageUploadMocks() throws IOException {
        when(fishCaptureRepository.findById(1L)).thenReturn(Optional.of(testCapture));
        when(captureImageRepository.findByFishCaptureId(1L)).thenReturn(List.of());

        doNothing().when(imageProcessingService).validateImage(any());

        ByteArrayInputStream reusableStream = new ByteArrayInputStream("test content".getBytes());
        when(imageProcessingService.convertToReusableStream(any())).thenReturn(reusableStream);
        when(imageProcessingService.detectMimeType(any())).thenReturn("image/jpeg");
        when(imageProcessingService.getOutputFormat(any())).thenReturn("jpg");
        when(imageProcessingService.getImageDimensions(any())).thenReturn(new int[]{1920, 1080});
        when(imageProcessingService.optimizeImage(any(), any(), anyInt()))
                .thenReturn(new ByteArrayInputStream("optimized".getBytes()));
        when(imageProcessingService.createThumbnail(any(), any()))
                .thenReturn(new ByteArrayInputStream("thumbnail".getBytes()));

        when(storageService.buildFileKey(anyLong(), anyLong(), any())).thenReturn("captures/test.jpg");
        when(s3StorageService.buildThumbnailKey(anyLong(), anyLong(), any())).thenReturn("thumbnails/thumb.jpg");
        when(storageService.uploadFile(any(), any(), anyLong(), any()))
                .thenReturn("https://s3.tebi.io/bucket/test.jpg");
    }

    /**
     * Construye una CaptureImage guardada de prueba
     */
    private CaptureImage buildSavedCaptureImage() {
        return CaptureImage.builder()
                .id(1L)
                .originalUrl("https://s3.tebi.io/bucket/test.jpg")
                .thumbnailUrl("https://s3.tebi.io/bucket/thumb.jpg")
                .fileName("test.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .width(1920)
                .height(1080)
                .s3Key("captures/user_1/capture_1/test.jpg")
                .fishCapture(testCapture)
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Construye un ImageResponseDto de prueba
     */
    private ImageResponseDto buildImageResponseDto(CaptureImage image) {
        return new ImageResponseDto(
                image.getId(),
                image.getOriginalUrl(),
                image.getThumbnailUrl(),
                image.getFileName(),
                image.getFileSize(),
                image.getMimeType(),
                image.getWidth(),
                image.getHeight(),
                image.getUploadedAt()
        );
    }
}