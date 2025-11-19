package com.example.fishingapp.repository;

import com.example.fishingapp.model.CaptureImage;
import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests de integración para CaptureImageRepository
 * Verifica las operaciones de base de datos con H2 en memoria
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CaptureImageRepository - Tests de Integración")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CaptureImageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CaptureImageRepository captureImageRepository;

    private User testUser;
    private FishCapture testCapture;

    @BeforeEach
    void setUp() {
        // Crear usuario de prueba usando persistAndFlush
        testUser = User.builder()
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .build();
        testUser = entityManager.persistAndFlush(testUser);

        // Crear captura de prueba usando persistAndFlush
        testCapture = FishCapture.builder()
                .captureDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .fishType("Trucha")
                .location("Rio Tajo")
                .weight(2.5f)
                .user(testUser)
                .build();
        testCapture = entityManager.persistAndFlush(testCapture);

        // Clear para asegurar que las siguientes consultas vengan de la BD
        entityManager.clear();
    }

    @Test
    @DisplayName("Debe guardar una imagen correctamente")
    void testSaveCaptureImage() {
        // Given
        CaptureImage image = CaptureImage.builder()
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

        // When
        CaptureImage savedImage = captureImageRepository.saveAndFlush(image);

        // Then
        assertThat(savedImage, notNullValue());
        assertThat(savedImage.getId(), notNullValue());
        assertThat(savedImage.getFileName(), is("test.jpg"));
        assertThat(savedImage.getMimeType(), is("image/jpeg"));
        assertThat(savedImage.getFishCapture().getId(), is(testCapture.getId()));
    }

    @Test
    @DisplayName("Debe encontrar imágenes por ID de captura")
    void testFindByFishCaptureId() {
        // Given
        CaptureImage image1 = createAndSaveImage("image1.jpg", testCapture);
        CaptureImage image2 = createAndSaveImage("image2.jpg", testCapture);
        CaptureImage image3 = createAndSaveImage("image3.jpg", testCapture);

        entityManager.clear();

        // When
        List<CaptureImage> images = captureImageRepository.findByFishCaptureId(testCapture.getId());

        // Then
        assertThat(images, hasSize(3));
        assertThat(images.stream().map(CaptureImage::getId).toList(),
                containsInAnyOrder(image1.getId(), image2.getId(), image3.getId()));
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay imágenes para una captura")
    void testFindByFishCaptureId_ReturnsEmptyList_WhenNoImages() {
        // When
        List<CaptureImage> images = captureImageRepository.findByFishCaptureId(testCapture.getId());

        // Then
        assertThat(images, empty());
    }

    @Test
    @DisplayName("Debe contar imágenes por captura")
    void testCountByFishCaptureId() {
        // Given
        createAndSaveImage("image1.jpg", testCapture);
        createAndSaveImage("image2.jpg", testCapture);
        createAndSaveImage("image3.jpg", testCapture);

        // When
        long count = captureImageRepository.countByFishCaptureId(testCapture.getId());

        // Then
        assertThat(count, is(3L));
    }

    @Test
    @DisplayName("Debe encontrar imagen por ID")
    void testFindById() {
        // Given
        CaptureImage image = createAndSaveImage("test.jpg", testCapture);
        entityManager.clear();

        // When
        Optional<CaptureImage> foundImage = captureImageRepository.findById(image.getId());

        // Then
        assertThat(foundImage.isPresent(), is(true));
        assertThat(foundImage.get().getFileName(), is("test.jpg"));
        assertThat(foundImage.get().getFishCapture().getId(), is(testCapture.getId()));
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando imagen no existe")
    void testFindById_ReturnsEmpty_WhenImageNotExists() {
        // When
        Optional<CaptureImage> foundImage = captureImageRepository.findById(999L);

        // Then
        assertThat(foundImage.isPresent(), is(false));
    }

    @Test
    @DisplayName("Debe eliminar una imagen correctamente")
    void testDeleteCaptureImage() {
        // Given
        CaptureImage image = createAndSaveImage("test.jpg", testCapture);
        Long imageId = image.getId();

        // When
        captureImageRepository.delete(image);
        captureImageRepository.flush();

        // Then
        Optional<CaptureImage> deletedImage = captureImageRepository.findById(imageId);
        assertThat(deletedImage.isPresent(), is(false));
    }

    @Test
    @DisplayName("Debe eliminar todas las imágenes de una captura")
    void testDeleteByFishCaptureId() {
        // Given
        createAndSaveImage("image1.jpg", testCapture);
        createAndSaveImage("image2.jpg", testCapture);
        createAndSaveImage("image3.jpg", testCapture);

        // When
        captureImageRepository.deleteByFishCaptureId(testCapture.getId());
        captureImageRepository.flush();

        // Then
        List<CaptureImage> remainingImages = captureImageRepository.findByFishCaptureId(testCapture.getId());
        assertThat(remainingImages, empty());
    }

    @Test
    @DisplayName("Debe contar correctamente el número de imágenes")
    void testCountImages() {
        // Given
        createAndSaveImage("image1.jpg", testCapture);
        createAndSaveImage("image2.jpg", testCapture);

        // When
        long count = captureImageRepository.count();

        // Then
        assertThat(count, is(2L));
    }

    @Test
    @DisplayName("Debe persistir correctamente las fechas de subida")
    void testUploadedAtPersistence() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        CaptureImage image = CaptureImage.builder()
                .originalUrl("https://s3.tebi.io/bucket/test.jpg")
                .thumbnailUrl("https://s3.tebi.io/bucket/thumb.jpg")
                .fileName("test.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .width(1920)
                .height(1080)
                .s3Key("captures/test.jpg")
                .fishCapture(testCapture)
                .uploadedAt(now)
                .build();

        // When
        CaptureImage savedImage = captureImageRepository.saveAndFlush(image);

        // Then
        assertThat(savedImage.getUploadedAt(), notNullValue());
        assertThat(savedImage.getUploadedAt().toLocalDate(), is(now.toLocalDate()));
    }

    @Test
    @DisplayName("Debe manejar múltiples capturas con imágenes")
    void testMultipleCapturesWithImages() {
        // Given
        FishCapture capture2 = FishCapture.builder()
                .captureDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .fishType("Carpa")
                .location("Lago")
                .weight(3.0f)
                .user(testUser)
                .build();
        capture2 = entityManager.persistAndFlush(capture2);

        createAndSaveImage("capture1_image1.jpg", testCapture);
        createAndSaveImage("capture1_image2.jpg", testCapture);
        createAndSaveImage("capture2_image1.jpg", capture2);

        // When
        List<CaptureImage> imagesCapture1 = captureImageRepository.findByFishCaptureId(testCapture.getId());
        List<CaptureImage> imagesCapture2 = captureImageRepository.findByFishCaptureId(capture2.getId());

        // Then
        assertThat(imagesCapture1, hasSize(2));
        assertThat(imagesCapture2, hasSize(1));
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Método auxiliar para crear y guardar una imagen de prueba
     */
    private CaptureImage createAndSaveImage(String fileName, FishCapture capture) {
        CaptureImage image = CaptureImage.builder()
                .originalUrl("https://s3.tebi.io/bucket/" + fileName)
                .thumbnailUrl("https://s3.tebi.io/bucket/thumb_" + fileName)
                .fileName(fileName)
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .width(1920)
                .height(1080)
                .s3Key("captures/" + fileName)
                .fishCapture(capture)
                .uploadedAt(LocalDateTime.now())
                .build();

        return entityManager.persistAndFlush(image);
    }
}