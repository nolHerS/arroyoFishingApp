package com.example.fishingapp.integration;

import com.example.fishingapp.config.BaseIntegrationTest;
import com.example.fishingapp.dto.image.ImageResponseDto;
import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.CaptureImageRepository;
import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.security.Role;
import com.example.fishingapp.service.impl.CloudinaryStorageServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración E2E para CaptureImage
 * Nota: BaseIntegrationTest limpia la BD antes de cada test
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("CaptureImage - Tests de Integración E2E")
class CaptureImageIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CloudinaryStorageServiceImpl cloudinaryStorageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaptureImageRepository captureImageRepository;

    private FishCapture testCapture;

    @BeforeEach
    void prepareTestData() {
        // BaseIntegrationTest.cleanDatabase() ya se ejecutó antes de esto

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

        // Configurar mocks de Cloudinary
        configureMocks();

        // Configurar SecurityContext
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        testAuthUser,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + testAuthUser.getRole().name()))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void configureMocks() {
        // Mock para buildFileKey - Genera keys únicas
        when(cloudinaryStorageService.buildFileKey(anyLong(), anyLong(), anyString()))
                .thenAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    Long captureId = invocation.getArgument(1);
                    String fileName = invocation.getArgument(2);
                    long timestamp = System.nanoTime();
                    return String.format("fish-captures/captures/user_%d/capture_%d/%d_%s",
                            userId, captureId, timestamp, fileName);
                });

        // Mock para buildThumbnailKey
        when(cloudinaryStorageService.buildThumbnailKey(anyLong(), anyLong(), anyString()))
                .thenAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    Long captureId = invocation.getArgument(1);
                    String fileName = invocation.getArgument(2);
                    long timestamp = System.nanoTime();
                    return String.format("fish-captures/thumbnails/user_%d/capture_%d/%d_thumb_%s",
                            userId, captureId, timestamp, fileName);
                });

        // Mock para uploadFile - Retorna URLs únicas
        when(cloudinaryStorageService.uploadFile(anyString(), any(), anyLong(), anyString()))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(0);
                    return "https://res.cloudinary.com/demo/image/upload/" + key.replace("fish-captures/", "");
                });

        // Mock para fileExists
        when(cloudinaryStorageService.fileExists(anyString())).thenReturn(true);
    }

    // ==================== TEST DE LÍMITE DE IMÁGENES ====================

    @Test
    @DisplayName("Debe fallar al exceder el límite de imágenes por captura (5 máximo)")
    void testExceedImageLimit() throws Exception {
        // Given - Subir el máximo permitido (5 imágenes)
        for (int i = 0; i < 5; i++) {
            Long imageId = uploadTestImage("image_" + i + ".jpg");
            assertThat("Imagen " + i + " debería haberse subido", imageId, notNullValue());
        }

        // Verificar que hay exactamente 5 imágenes
        long countAfterUploads = captureImageRepository.countByFishCaptureId(testCapture.getId());
        assertThat("Deberían existir exactamente 5 imágenes", countAfterUploads, is(5L));

        mockMvc.perform(get("/api/captures/" + testCapture.getId() + "/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));

        // When & Then - Intentar subir una sexta imagen (debe fallar)
        MockMultipartFile extraFile = new MockMultipartFile(
                "file",
                "extra.jpg",
                "image/jpeg",
                createValidTestImage(200, 200)
        );

        mockMvc.perform(multipart("/api/captures/" + testCapture.getId() + "/images")
                        .file(extraFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("límite")));

        // Verificar que siguen siendo 5 imágenes
        long finalCount = captureImageRepository.countByFishCaptureId(testCapture.getId());
        assertThat("No deberían haberse agregado más imágenes", finalCount, is(5L));
    }

    // ==================== TEST DE SUBIDA MÚLTIPLE CON LÍMITE ====================

    @Test
    @DisplayName("Debe fallar al exceder límite en subida múltiple")
    void testUploadMultipleImagesExceedingLimit() throws Exception {
        // Subir 3 imágenes primero
        uploadTestImage("image1.jpg");
        uploadTestImage("image2.jpg");
        uploadTestImage("image3.jpg");

        // Intentar subir 3 más (total sería 6, límite es 5)
        MockMultipartFile file1 = new MockMultipartFile("files", "extra1.jpg", "image/jpeg", createValidTestImage(200, 200));
        MockMultipartFile file2 = new MockMultipartFile("files", "extra2.jpg", "image/jpeg", createValidTestImage(200, 200));
        MockMultipartFile file3 = new MockMultipartFile("files", "extra3.jpg", "image/jpeg", createValidTestImage(200, 200));

        mockMvc.perform(multipart("/api/captures/" + testCapture.getId() + "/images/multiple")
                        .file(file1)
                        .file(file2)
                        .file(file3))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("límite")));

        // Verificar que solo hay 3 imágenes (las originales)
        long count = captureImageRepository.countByFishCaptureId(testCapture.getId());
        assertThat(count, is(3L));
    }

    // ==================== OTROS TESTS ====================

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

    @Test
    @DisplayName("Debe subir múltiples imágenes en una sola petición")
    void testUploadMultipleImagesInSingleRequest() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("files", "test1.jpg", "image/jpeg", createValidTestImage(200, 200));
        MockMultipartFile file2 = new MockMultipartFile("files", "test2.jpg", "image/jpeg", createValidTestImage(200, 200));
        MockMultipartFile file3 = new MockMultipartFile("files", "test3.jpg", "image/jpeg", createValidTestImage(200, 200));

        mockMvc.perform(multipart("/api/captures/" + testCapture.getId() + "/images/multiple")
                        .file(file1)
                        .file(file2)
                        .file(file3))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalImages", is(3)))
                .andExpect(jsonPath("$.uploadedImages", hasSize(3)));

        long count = captureImageRepository.count();
        assertThat(count, is(3L));
    }

    @Test
    @DisplayName("Debe fallar al subir archivo vacío")
    void testUploadEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/api/captures/" + testCapture.getId() + "/images")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("vacío")));
    }

    @Test
    @DisplayName("Debe fallar con tipo de archivo no permitido")
    void testInvalidFileType() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "%PDF-1.4 fake pdf content".getBytes()
        );

        mockMvc.perform(multipart("/api/captures/" + testCapture.getId() + "/images")
                        .file(pdfFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("tipo de archivo")));
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Helper para subir una imagen de prueba
     */
    private Long uploadTestImage(String filename) throws Exception {
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
     * Crea una imagen de prueba válida con las dimensiones especificadas
     */
    private byte[] createValidTestImage(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(Color.BLUE);
        graphics.fillOval(10, 10, width - 20, height - 20);
        graphics.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}