package com.example.fishingapp.integration;

import com.example.fishingapp.dto.image.ImageResponseDto;
import com.example.fishingapp.model.CaptureImage;
import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.AuthUserRepository;
import com.example.fishingapp.repository.CaptureImageRepository;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.repository.UserRepository;
import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.security.Role;
import com.example.fishingapp.service.impl.S3StorageServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración completos para el sistema de imágenes de capturas
 * Verifica el flujo end-to-end con base de datos real (H2)
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("CaptureImage - Tests de Integración E2E")
class CaptureImageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3StorageServiceImpl s3StorageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private FishCaptureRepository fishCaptureRepository;

    @Autowired
    private CaptureImageRepository captureImageRepository;

    private FishCapture testCapture;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        captureImageRepository.deleteAll();
        fishCaptureRepository.deleteAll();
        authUserRepository.deleteAll();
        userRepository.deleteAll();

        // Crear usuario
        User testUser = User.builder()
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .build();
        testUser = userRepository.save(testUser);

        // Crear AuthUser
        AuthUser testAuthUser = AuthUser.builder()
                .username(testUser.getUsername())
                .email(testUser.getEmail())
                .password("$2a$10$dummypasswordhash")
                .role(Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .createdAt(LocalDateTime.now())
                .user(testUser)
                .build();
        testAuthUser = authUserRepository.save(testAuthUser);

        // Crear captura
        testCapture = FishCapture.builder()
                .captureDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .fishType("Trucha")
                .location("Rio Tajo")
                .weight(2.5f)
                .user(testUser)
                .build();
        testCapture = fishCaptureRepository.save(testCapture);

        // Simulamos el comportamiento del S3StorageService
        when(s3StorageService.buildFileKey(anyLong(), anyLong(), anyString()))
                .thenAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    Long captureId = invocation.getArgument(1);
                    String fileName = invocation.getArgument(2);
                    return String.format("captures/user_%d/capture_%d/%s", userId, captureId, fileName);
                });

        when(s3StorageService.buildThumbnailKey(anyLong(), anyLong(), anyString()))
                .thenAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    Long captureId = invocation.getArgument(1);
                    String fileName = invocation.getArgument(2);
                    return String.format("thumbnails/user_%d/capture_%d/thumb_%s", userId, captureId, fileName);
                });

        when(s3StorageService.uploadFile(anyString(), ArgumentMatchers.any(), anyLong(), anyString()))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(0);
                    return "https://mocked-s3.tebi.io/" + key;
                });

        when(s3StorageService.fileExists(anyString())).thenReturn(true);

        // Configurar SecurityContext
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        testAuthUser,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + testAuthUser.getRole().name()))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // ==================== FLUJO COMPLETO: SUBIR Y GESTIONAR IMÁGENES ====================

    @Test
    @DisplayName("Flujo completo: Crear captura, subir imágenes, obtenerlas y eliminarlas")
    void testCompleteImageLifecycle() throws Exception {
        // 1. Verificar que la captura no tiene imágenes inicialmente
        mockMvc.perform(get("/api/captures/" + testCapture.getId() + "/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // 2. Subir primera imagen
        Long imageId1 = uploadTestImage("image1.jpg");
        assertThat(imageId1, notNullValue());

        // 3. Subir segunda imagen
        Long imageId2 = uploadTestImage("image2.jpg");
        assertThat(imageId2, notNullValue());

        // 4. Verificar que ahora hay 2 imágenes
        mockMvc.perform(get("/api/captures/" + testCapture.getId() + "/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // 5. Contar imágenes
        mockMvc.perform(get("/api/captures/" + testCapture.getId() + "/images/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(2)));

        // 6. Obtener una imagen específica
        mockMvc.perform(get("/api/captures/images/" + imageId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(imageId1.intValue())));

        // 7. Eliminar una imagen
        mockMvc.perform(delete("/api/captures/images/" + imageId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted", is(true)));

        // 8. Verificar que solo queda 1 imagen
        mockMvc.perform(get("/api/captures/" + testCapture.getId() + "/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 9. Eliminar todas las imágenes restantes
        mockMvc.perform(delete("/api/captures/" + testCapture.getId() + "/images"))
                .andExpect(status().isNoContent());

        // 10. Verificar que no quedan imágenes
        mockMvc.perform(get("/api/captures/" + testCapture.getId() + "/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== TESTS DE SUBIDA MÚLTIPLE ====================

    @Test
    @DisplayName("Debe subir múltiples imágenes en una sola petición")
    void testUploadMultipleImagesInSingleRequest() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile("files", "test1.jpg", "image/jpeg", createValidTestImage(200,200));
        MockMultipartFile file2 = new MockMultipartFile("files", "test2.jpg", "image/jpeg", createValidTestImage(200,200));
        MockMultipartFile file3 = new MockMultipartFile("files", "test3.jpg", "image/jpeg", createValidTestImage(200,200));

        // When
        mockMvc.perform(multipart("/api/captures/" + testCapture.getId() + "/images/multiple")
                        .file(file1)
                        .file(file2)
                        .file(file3))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalImages", is(3)))
                .andExpect(jsonPath("$.uploadedImages", hasSize(3)));

        // Then - Verificar en base de datos
        long count = captureImageRepository.count();
        assertThat(count, is(3L));
    }

    // ==================== TESTS DE PERSISTENCIA ====================

    @Test
    @DisplayName("Las imágenes deben persistir correctamente en la base de datos")
    void testImagePersistence() throws Exception {
        // Given & When
        Long imageId = uploadTestImage("persistence_test.jpg");

        // Then - Verificar directamente en base de datos
        CaptureImage savedImage = captureImageRepository.findById(imageId).orElse(null);
        assertThat(savedImage, notNullValue());
        assertThat(savedImage.getFileName(), notNullValue());
        assertThat(savedImage.getOriginalUrl(), notNullValue());
        assertThat(savedImage.getThumbnailUrl(), notNullValue());
        assertThat(savedImage.getFishCapture().getId(), is(testCapture.getId()));
        assertThat(savedImage.getUploadedAt(), notNullValue());
    }

    // ==================== TESTS DE AUTORIZACIÓN ====================

    @Test
    @DisplayName("Usuario no puede subir imagen a captura de otro usuario")
    void testCannotUploadImageToOtherUserCapture() throws Exception {
        // Given - Crear otro usuario y su captura
        User otherUser = User.builder()
                .username("otheruser")
                .fullName("Other User")
                .email("other@example.com")
                .build();
        otherUser = userRepository.save(otherUser);

        FishCapture otherCapture = FishCapture.builder()
                .captureDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .fishType("Carpa")
                .location("Lago")
                .weight(3.0f)
                .user(otherUser)
                .build();
        otherCapture = fishCaptureRepository.save(otherCapture);

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", createValidTestImage(200,200));

        // When & Then
        mockMvc.perform(multipart("/api/captures/" + otherCapture.getId() + "/images")
                        .file(file))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("No tienes permisos")));
    }

    @Test
    @DisplayName("Usuario no puede eliminar imagen de captura de otro usuario")
    void testCannotDeleteImageFromOtherUserCapture() throws Exception {
        // Given - Crear otro usuario, captura e imagen
        User otherUser = User.builder()
                .username("otheruser")
                .fullName("Other User")
                .email("other@example.com")
                .build();
        otherUser = userRepository.save(otherUser);

        FishCapture otherCapture = FishCapture.builder()
                .captureDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .fishType("Carpa")
                .location("Lago")
                .weight(3.0f)
                .user(otherUser)
                .build();
        otherCapture = fishCaptureRepository.save(otherCapture);

        CaptureImage otherImage = CaptureImage.builder()
                .originalUrl("https://s3.tebi.io/bucket/other.jpg")
                .thumbnailUrl("https://s3.tebi.io/bucket/thumb_other.jpg")
                .fileName("other.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .width(1920)
                .height(1080)
                .s3Key("captures/other.jpg")
                .fishCapture(otherCapture)
                .uploadedAt(LocalDateTime.now())
                .build();
        otherImage = captureImageRepository.save(otherImage);

        // When & Then
        mockMvc.perform(delete("/api/captures/images/" + otherImage.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("No tienes permisos")));
    }

    // ==================== TESTS DE VALIDACIONES ====================

    @Test
    @DisplayName("Debe fallar al subir archivo vacío")
    void testUploadEmptyFile() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        // When & Then
        mockMvc.perform(multipart("/api/captures/" + testCapture.getId() + "/images")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("vacío")));
    }

    @Test
    @DisplayName("Debe fallar al exceder el límite de imágenes por captura")
    void testExceedImageLimit() throws Exception {
        for (int i = 0; i < 5; i++) {
            uploadTestImage("image" + i + ".jpg");
        }
        // When & Then - Intentar subir una sexta imagen
        MockMultipartFile extraFile = new MockMultipartFile("file", "extra.jpg", "image/jpeg", createValidTestImage(200,200));

        mockMvc.perform(multipart("/api/captures/" + testCapture.getId() + "/images")
                        .file(extraFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("límite máximo")));

        // Verificar que solo hay 5 imágenes en BD
        long count = captureImageRepository.count();
        assertThat(count, is(5L));
    }

    @Test
    @DisplayName("Debe fallar con tipo de archivo no permitido")
    void testInvalidFileType() throws Exception {
        // Given - Un archivo PDF (no permitido)
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "%PDF-1.4 fake pdf content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/captures/" + testCapture.getId() + "/images")
                        .file(pdfFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Tipo de archivo no permitido")));
    }

// ==================== TESTS DE ENDPOINTS PÚBLICOS ====================

    @Test
    @DisplayName("GET público - Cualquiera puede ver imágenes sin autenticación")
    void testPublicGetImagesByCapture() throws Exception {
        // Given
        uploadTestImage("public_image.jpg");

        // Clear security context para simular usuario no autenticado
        SecurityContextHolder.clearContext();

        // When & Then
        mockMvc.perform(get("/api/captures/" + testCapture.getId() + "/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET público - Cualquiera puede ver una imagen específica")
    void testPublicGetImageById() throws Exception {
        // Given
        Long imageId = uploadTestImage("public_image.jpg");

        // Clear security context
        SecurityContextHolder.clearContext();

        // When & Then
        mockMvc.perform(get("/api/captures/images/" + imageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(imageId.intValue())));
    }

    @Test
    @DisplayName("GET público - Cualquiera puede contar imágenes")
    void testPublicCountImages() throws Exception {
        // Given
        uploadTestImage("image1.jpg");
        uploadTestImage("image2.jpg");
        uploadTestImage("image3.jpg");

        // Clear security context
        SecurityContextHolder.clearContext();

        // When & Then
        mockMvc.perform(get("/api/captures/" + testCapture.getId() + "/images/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(3)));
    }

// ==================== TESTS DE ESCENARIOS COMPLEJOS ====================

    @Test
    @DisplayName("Múltiples usuarios pueden tener imágenes en sus propias capturas")
    void testMultipleUsersWithImages() throws Exception {
        // Given - Crear segundo usuario
        User user2 = User.builder()
                .username("user2")
                .fullName("User Two")
                .email("user2@example.com")
                .build();
        user2 = userRepository.save(user2);

        AuthUser authUser2 = AuthUser.builder()
                .username(user2.getUsername())
                .email(user2.getEmail())
                .password("$2a$10$dummypasswordhash")
                .role(Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .createdAt(LocalDateTime.now())
                .user(user2)
                .build();
        authUser2 = authUserRepository.save(authUser2);

        FishCapture capture2 = FishCapture.builder()
                .captureDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .fishType("Carpa")
                .location("Lago")
                .weight(3.5f)
                .user(user2)
                .build();
        capture2 = fishCaptureRepository.save(capture2);

        // When - Usuario 1 sube 2 imágenes
        uploadTestImage("user1_image1.jpg");
        uploadTestImage("user1_image2.jpg");

        // Usuario 2 sube 3 imágenes
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        authUser2,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        MockMultipartFile file1 = new MockMultipartFile("file", "user2_image1.jpg", "image/jpeg", createValidTestImage(200,200));
        MockMultipartFile file2 = new MockMultipartFile("file", "user2_image2.jpg", "image/jpeg", createValidTestImage(200,200));
        MockMultipartFile file3 = new MockMultipartFile("file", "user2_image3.jpg", "image/jpeg", createValidTestImage(200,200));

        mockMvc.perform(multipart("/api/captures/" + capture2.getId() + "/images").file(file1)).andExpect(status().isCreated());
        mockMvc.perform(multipart("/api/captures/" + capture2.getId() + "/images").file(file2)).andExpect(status().isCreated());
        mockMvc.perform(multipart("/api/captures/" + capture2.getId() + "/images").file(file3)).andExpect(status().isCreated());

        // Then - Verificar que cada usuario tiene sus imágenes
        mockMvc.perform(get("/api/captures/" + testCapture.getId() + "/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/captures/" + capture2.getId() + "/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        // Verificar total en BD
        long totalImages = captureImageRepository.count();
        assertThat(totalImages, is(5L));
    }

    @Test
    @DisplayName("Eliminar captura debe requerir eliminar imágenes primero")
    void testCaptureCannotBeDeletedWithImages() throws Exception {
        // Given - Subir algunas imágenes
        uploadTestImage("image1.jpg");
        uploadTestImage("image2.jpg");

        // When - Intentar eliminar la captura directamente fallaría por constraint FK
        // En tu aplicación, primero debes eliminar las imágenes
        mockMvc.perform(delete("/api/captures/" + testCapture.getId() + "/images"))
                .andExpect(status().isNoContent());

        // Then - Ahora sí se puede eliminar la captura (no lo hacemos en el test)
        long imageCount = captureImageRepository.count();
        assertThat(imageCount, is(0L));
    }

    @Test
    @DisplayName("Subida parcial exitosa - Algunas imágenes válidas, otras no")
    void testPartialUploadSuccess() throws Exception {
        // Given - Crear array con imágenes válidas e inválidas mezcladas
        // Nota: Este test depende de que tu servicio maneje errores parciales

        MockMultipartFile validFile = new MockMultipartFile("files", "valid.jpg", "image/jpeg", createValidTestImage(200,200));
        MockMultipartFile emptyFile = new MockMultipartFile("files", "empty.jpg", "image/jpeg", new byte[0]);
        MockMultipartFile validFile2 = new MockMultipartFile("files", "valid2.jpg", "image/jpeg", createValidTestImage(200,200));

        // When
        mockMvc.perform(multipart("/api/captures/" + testCapture.getId() + "/images/multiple")
                        .file(validFile)
                        .file(emptyFile)
                        .file(validFile2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalImages", is(2))) // Solo 2 exitosas
                .andExpect(jsonPath("$.uploadedImages", hasSize(2)))
                .andExpect(jsonPath("$.message", containsString("2 de 3")));

        // Then - Verificar que solo se guardaron las válidas
        long count = captureImageRepository.count();
        assertThat(count, is(2L));
    }

// ==================== TESTS DE CONSISTENCIA ====================

    @Test
    @DisplayName("Las URLs de imágenes deben ser únicas")
    void testImageUrlsAreUnique() throws Exception {
        // Given & When - Subir 3 imágenes
        Long id1 = uploadTestImage("image1.jpg");
        Long id2 = uploadTestImage("image2.jpg");
        Long id3 = uploadTestImage("image3.jpg");

        // Then - Verificar que todas tienen URLs diferentes
        CaptureImage img1 = captureImageRepository.findById(id1).orElseThrow();
        CaptureImage img2 = captureImageRepository.findById(id2).orElseThrow();
        CaptureImage img3 = captureImageRepository.findById(id3).orElseThrow();

        assertThat(img1.getOriginalUrl(), not(equalTo(img2.getOriginalUrl())));
        assertThat(img1.getOriginalUrl(), not(equalTo(img3.getOriginalUrl())));
        assertThat(img2.getOriginalUrl(), not(equalTo(img3.getOriginalUrl())));
    }

    @Test
    @DisplayName("Las fechas de subida deben persistir correctamente")
    void testUploadDatesArePersisted() throws Exception {
        // Given
        LocalDateTime before = LocalDateTime.now();

        // When
        Long imageId = uploadTestImage("timestamped_image.jpg");

        LocalDateTime after = LocalDateTime.now();

        // Then
        CaptureImage savedImage = captureImageRepository.findById(imageId).orElseThrow();
        assertThat(savedImage.getUploadedAt(), notNullValue());
        assertThat(savedImage.getUploadedAt(), greaterThanOrEqualTo(before));
        assertThat(savedImage.getUploadedAt(), lessThanOrEqualTo(after));
    }

// ==================== TESTS DE RENDIMIENTO ====================

    @Test
    @DisplayName("Debe manejar correctamente la subida secuencial de múltiples imágenes")
    void testSequentialUploadPerformance() throws Exception {
        // Given & When - Subir 5 imágenes secuencialmente
        for (int i = 0; i < 5; i++) {
            Long imageId = uploadTestImage("perf_test_" + i + ".jpg");
            assertThat(imageId, notNullValue());
        }

        // Then - Verificar que todas se guardaron
        long count = captureImageRepository.count();
        assertThat(count, is(5L));

        // Verificar que todas pertenecen a la misma captura
        mockMvc.perform(get("/api/captures/" + testCapture.getId() + "/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

// ==================== MÉTODOS AUXILIARES ====================

    /**
     * Helper para subir una imagen de prueba
     */
    private Long uploadTestImage(String filename) throws Exception {
        // Crear una imagen válida de 200x200 píxeles
        byte[] validImageContent = createValidTestImage(200, 200);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                "image/jpeg",
                validImageContent
        );

        String response = mockMvc.perform(multipart("/api/captures/" + testCapture.getId() + "/images")
                        .file(file))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ImageResponseDto imageDto = objectMapper.readValue(response, ImageResponseDto.class);
        return imageDto.id();
    }

    /**
     * Crea un JPEG válido mínimo de 1x1 pixel
     */
    private byte[] createValidJpegBytes() {
        return new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46,
                0x49, 0x46, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xDB, 0x00, 0x43, 0x00, 0x08, 0x06, 0x06, 0x07, 0x06,
                0x05, 0x08, 0x07, 0x07, 0x07, 0x09, 0x09, 0x08, 0x0A, 0x0C, 0x14, 0x0D,
                0x0C, 0x0B, 0x0B, 0x0C, 0x19, 0x12, 0x13, 0x0F, 0x14, 0x1D, 0x1A, 0x1F,
                0x1E, 0x1D, 0x1A, 0x1C, 0x1C, 0x20, 0x24, 0x2E, 0x27, 0x20, 0x22, 0x2C,
                0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C, 0x30, 0x31, 0x34, 0x34, 0x34,
                0x1F, 0x27, 0x39, 0x3D, 0x38, 0x32, 0x3C, 0x2E, 0x33, 0x34, 0x32,
                (byte) 0xFF, (byte) 0xC0, 0x00, 0x0B, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01,
                0x01, 0x11, 0x00, (byte) 0xFF, (byte) 0xC4, 0x00, 0x14, 0x00, 0x01, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x09, (byte) 0xFF, (byte) 0xC4, 0x00, 0x14, 0x10, 0x01, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00,
                0x00, 0x3F, 0x00, (byte) 0xD2, (byte) 0xCF, 0x20, (byte) 0xFF, (byte) 0xD9
        };
    }

    /**
     * Crea una imagen de prueba válida con las dimensiones especificadas
     */
    private byte[] createValidTestImage(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        // Rellenar con un color (blanco)
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);

        // Dibujar algo para que sea una imagen real
        graphics.setColor(Color.BLUE);
        graphics.fillOval(10, 10, width - 20, height - 20);
        graphics.dispose();

        // Convertir a bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}