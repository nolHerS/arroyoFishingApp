package com.example.fishingapp.controller;

import com.example.fishingapp.dto.image.ImageDeleteResponseDto;
import com.example.fishingapp.dto.image.ImageResponseDto;
import com.example.fishingapp.dto.image.ImageUploadResponseDto;
import com.example.fishingapp.exception.InvalidImageException;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.exception.UnauthorizedException;
import com.example.fishingapp.model.User;
import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.security.Role;
import com.example.fishingapp.service.CaptureImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios del controlador CaptureImageController
 * Usa @SpringBootTest con @MockBean para mockear el servicio
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("CaptureImageController - Tests Unitarios")
class CaptureImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CaptureImageService captureImageService;

    @MockBean
    private AuthUser mockAuthUser;
    private ImageResponseDto mockImageResponse;

    @BeforeEach
    void setUp() {

        // Crear usuario mock
        User mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .build();

        mockAuthUser = AuthUser.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .user(mockUser)
                .build();

        // Crear respuesta mock
        mockImageResponse = new ImageResponseDto(
                1L,
                "https://s3.tebi.io/bucket/test.jpg",
                "https://s3.tebi.io/bucket/thumb.jpg",
                "test.jpg",
                1024L,
                "image/jpeg",
                1920,
                1080,
                LocalDateTime.now()
        );
    }

    // ==================== TESTS DE SUBIDA DE IMÁGENES ====================

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /api/captures/{captureId}/images - Debe subir imagen exitosamente")
    void testUploadImage_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        when(captureImageService.uploadImage(anyLong(), anyLong(), any()))
                .thenReturn(mockImageResponse);

        // When & Then
        mockMvc.perform(multipart("/api/captures/1/images")
                        .file(file)
                        .with(user(mockAuthUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fileName", is("test.jpg")))
                .andExpect(jsonPath("$.mimeType", is("image/jpeg")));

        verify(captureImageService, times(1)).uploadImage(anyLong(), anyLong(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /api/captures/{captureId}/images - Debe fallar con captura inexistente")
    void testUploadImage_CaptureNotFound() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake content".getBytes()
        );

        when(captureImageService.uploadImage(anyLong(), anyLong(), any()))
                .thenThrow(new ResourceNotFoundException("Captura no encontrada con ID: 999"));

        // When & Then
        mockMvc.perform(multipart("/api/captures/999/images")
                        .file(file)
                        .with(user(mockAuthUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Captura no encontrada")));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /api/captures/{captureId}/images - Debe fallar sin permisos")
    void testUploadImage_Unauthorized() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake content".getBytes()
        );

        when(captureImageService.uploadImage(anyLong(), anyLong(), any()))
                .thenThrow(new UnauthorizedException("No tienes permisos para modificar esta captura"));

        // When & Then
        mockMvc.perform(multipart("/api/captures/1/images")
                        .file(file)
                        .with(user(mockAuthUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("No tienes permisos")));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /api/captures/{captureId}/images - Debe fallar con imagen inválida")
    void testUploadImage_InvalidImage() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake content".getBytes()
        );

        when(captureImageService.uploadImage(anyLong(), anyLong(), any()))
                .thenThrow(new InvalidImageException("El archivo está vacío"));

        // When & Then
        mockMvc.perform(multipart("/api/captures/1/images")
                        .file(file)
                        .with(user(mockAuthUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("vacío")));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /api/captures/{captureId}/images/multiple - Debe subir múltiples imágenes")
    void testUploadMultipleImages_Success() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile("files", "test1.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "test2.jpg", "image/jpeg", "content2".getBytes());

        ImageUploadResponseDto response = new ImageUploadResponseDto(
                1L,
                List.of(mockImageResponse, mockImageResponse),
                2,
                "2 imágenes subidas correctamente"
        );

        when(captureImageService.uploadMultipleImages(anyLong(), anyLong(), any()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/captures/1/images/multiple")
                        .file(file1)
                        .file(file2)
                        .with(user(mockAuthUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.captureId", is(1)))
                .andExpect(jsonPath("$.totalImages", is(2)))
                .andExpect(jsonPath("$.uploadedImages", hasSize(2)));

        verify(captureImageService, times(1)).uploadMultipleImages(anyLong(), anyLong(), any());
    }

    // ==================== TESTS DE OBTENCIÓN DE IMÁGENES ====================

    @Test
    @DisplayName("GET /api/captures/{captureId}/images - Debe obtener todas las imágenes")
    void testGetImagesByCapture_Success() throws Exception {
        // Given
        List<ImageResponseDto> images = List.of(mockImageResponse, mockImageResponse);
        when(captureImageService.getImagesByCapture(1L)).thenReturn(images);

        // When & Then
        mockMvc.perform(get("/api/captures/1/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].fileName", is("test.jpg")));

        verify(captureImageService, times(1)).getImagesByCapture(1L);
    }

    @Test
    @DisplayName("GET /api/captures/{captureId}/images - Debe retornar lista vacía")
    void testGetImagesByCapture_EmptyList() throws Exception {
        // Given
        when(captureImageService.getImagesByCapture(1L)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/captures/1/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/captures/{captureId}/images - Debe fallar con captura inexistente")
    void testGetImagesByCapture_CaptureNotFound() throws Exception {
        // Given
        when(captureImageService.getImagesByCapture(999L))
                .thenThrow(new ResourceNotFoundException("Captura no encontrada con ID: 999"));

        // When & Then
        mockMvc.perform(get("/api/captures/999/images"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Captura no encontrada")));
    }

    @Test
    @DisplayName("GET /api/captures/images/{imageId} - Debe obtener una imagen específica")
    void testGetImageById_Success() throws Exception {
        // Given
        when(captureImageService.getImageById(1L)).thenReturn(mockImageResponse);

        // When & Then
        mockMvc.perform(get("/api/captures/images/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fileName", is("test.jpg")));

        verify(captureImageService, times(1)).getImageById(1L);
    }

    @Test
    @DisplayName("GET /api/captures/images/{imageId} - Debe fallar con imagen inexistente")
    void testGetImageById_ImageNotFound() throws Exception {
        // Given
        when(captureImageService.getImageById(999L))
                .thenThrow(new ResourceNotFoundException("Imagen no encontrada con ID: 999"));

        // When & Then
        mockMvc.perform(get("/api/captures/images/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Imagen no encontrada")));
    }

    @Test
    @DisplayName("GET /api/captures/{captureId}/images/count - Debe contar imágenes correctamente")
    void testCountImages_Success() throws Exception {
        // Given
        when(captureImageService.countImagesByCapture(1L)).thenReturn(5);

        // When & Then
        mockMvc.perform(get("/api/captures/1/images/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(5)));

        verify(captureImageService, times(1)).countImagesByCapture(1L);
    }

    // ==================== TESTS DE ELIMINACIÓN ====================

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("DELETE /api/captures/images/{imageId} - Debe eliminar imagen correctamente")
    void testDeleteImage_Success() throws Exception {
        // Given
        ImageDeleteResponseDto response = ImageDeleteResponseDto.success(1L, 1L);
        when(captureImageService.deleteImage(anyLong(), anyLong())).thenReturn(response);

        // When & Then
        mockMvc.perform(delete("/api/captures/images/1")
                        .with(user(mockAuthUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted", is(true)))
                .andExpect(jsonPath("$.imageId", is(1)))
                .andExpect(jsonPath("$.captureId", is(1)));

        verify(captureImageService, times(1)).deleteImage(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("DELETE /api/captures/images/{imageId} - Debe fallar con imagen inexistente")
    void testDeleteImage_ImageNotFound() throws Exception {
        // Given
        when(captureImageService.deleteImage(anyLong(), anyLong()))
                .thenThrow(new ResourceNotFoundException("Imagen no encontrada con ID: 999"));

        // When & Then
        mockMvc.perform(delete("/api/captures/images/999")
                        .with(user(mockAuthUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Imagen no encontrada")));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("DELETE /api/captures/images/{imageId} - Debe fallar sin permisos")
    void testDeleteImage_Unauthorized() throws Exception {
        // Given
        when(captureImageService.deleteImage(anyLong(), anyLong()))
                .thenThrow(new UnauthorizedException("No tienes permisos para eliminar esta imagen"));

        // When & Then
        mockMvc.perform(delete("/api/captures/images/1")
                        .with(user(mockAuthUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("No tienes permisos")));
    }

    @Test
    @DisplayName("DELETE /api/captures/{captureId}/images - Debe eliminar todas las imágenes")
    void testDeleteAllImages_Success() throws Exception {
        // Mock del servicio
        doNothing().when(captureImageService).deleteAllImagesByCapture(anyLong(), anyLong());

        // Creamos un Authentication personalizado que devuelva nuestro mockAuthUser
        AbstractAuthenticationToken authToken = new AbstractAuthenticationToken(null) {
            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return mockAuthUser; // <--- aquí va nuestro AuthUser
            }
        };
        authToken.setAuthenticated(true);

        // Ejecutar el DELETE con la autenticación simulada
        mockMvc.perform(delete("/api/captures/1/images")
                        .with(authentication(authToken)))
                .andExpect(status().isNoContent());

        // Verificar que el servicio fue llamado con el ID correcto
        verify(captureImageService, times(1)).deleteAllImagesByCapture(1L, mockAuthUser.getId());
    }



    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("DELETE /api/captures/{captureId}/images - Debe fallar con captura inexistente")
    void testDeleteAllImages_CaptureNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Captura no encontrada con ID: 999"))
                .when(captureImageService).deleteAllImagesByCapture(anyLong(), anyLong());

        // When & Then
        mockMvc.perform(delete("/api/captures/999/images")
                        .with(user(mockAuthUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Captura no encontrada")));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("DELETE /api/captures/{captureId}/images - Debe fallar sin permisos")
    void testDeleteAllImages_Unauthorized() throws Exception {
        // Given
        doThrow(new UnauthorizedException("No tienes permisos para eliminar estas imágenes"))
                .when(captureImageService).deleteAllImagesByCapture(anyLong(), anyLong());

        // When & Then
        mockMvc.perform(delete("/api/captures/1/images")
                        .with(user(mockAuthUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("No tienes permisos")));
    }

    // ==================== TESTS DE VALIDACIÓN DE PARÁMETROS ====================

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Debe fallar con ID de captura inválido")
    void testUploadImage_InvalidCaptureId() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

        // When & Then - ID no numérico
        mockMvc.perform(multipart("/api/captures/invalid/images")
                        .file(file)
                        .with(user(mockAuthUser)))
                .andExpect(status().isBadRequest());
    }
}