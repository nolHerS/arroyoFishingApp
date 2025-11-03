package com.example.fishingapp.controller;

import com.example.fishingapp.dto.image.ImageDeleteResponseDto;
import com.example.fishingapp.dto.image.ImageResponseDto;
import com.example.fishingapp.dto.image.ImageUploadResponseDto;
import com.example.fishingapp.exception.GlobalExceptionHandler;
import com.example.fishingapp.exception.InvalidImageException;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.exception.UnauthorizedException;
import com.example.fishingapp.model.User;
import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.security.Role;
import com.example.fishingapp.security.filter.JwtAuthenticationFilter;
import com.example.fishingapp.service.CaptureImageService;
import com.example.fishingapp.service.impl.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests unitarios del controlador CaptureImageController
 */
@WebMvcTest(CaptureImageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("CaptureImageController - Tests Unitarios")
class CaptureImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CaptureImageService captureImageService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private AuthUser mockAuthUser;
    private ImageResponseDto mockImageResponse;

    /**
     * Configuración para resolver @AuthenticationPrincipal en los tests
     */
    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.getParameterType().equals(AuthUser.class) &&
                            parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter,
                                              ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest,
                                              WebDataBinderFactory binderFactory) {
                    SecurityContext context = SecurityContextHolder.getContext();
                    Authentication auth = context.getAuthentication();

                    if (auth != null && auth.getPrincipal() instanceof AuthUser) {
                        return auth.getPrincipal();
                    }

                    return null;
                }
            });
        }
    }

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
                .password("hashedPassword")
                .role(Role.USER)
                .user(mockUser)
                .enabled(true)
                .accountNonLocked(true)
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

        // Limpiar el contexto de seguridad antes de cada test
        SecurityContextHolder.clearContext();
    }

    /**
     * Helper para configurar la autenticación en el SecurityContext
     */
    private void setupAuthentication() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .build();

        AuthUser authUser = AuthUser.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .role(Role.USER)
                .user(user)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                authUser,
                null,
                authUser.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    // ==================== TESTS DE SUBIDA DE IMÁGENES ====================

    @Test
    @DisplayName("POST /api/captures/{captureId}/images - Debe subir imagen exitosamente")
    void testUploadImage_Success() throws Exception {
        // Given
        setupAuthentication();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        when(captureImageService.uploadImage(eq(1L), eq(1L), any()))
                .thenReturn(mockImageResponse);

        // When & Then
        mockMvc.perform(multipart("/api/captures/1/images")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fileName", is("test.jpg")))
                .andExpect(jsonPath("$.mimeType", is("image/jpeg")))
                .andExpect(jsonPath("$.originalUrl", is("https://s3.tebi.io/bucket/test.jpg")))
                .andExpect(jsonPath("$.thumbnailUrl", is("https://s3.tebi.io/bucket/thumb.jpg")));

        verify(captureImageService, times(1)).uploadImage(eq(1L), eq(1L), any());
    }

    @Test
    @DisplayName("POST /api/captures/{captureId}/images - Debe fallar con captura inexistente")
    void testUploadImage_CaptureNotFound() throws Exception {
        // Given
        setupAuthentication();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake content".getBytes()
        );

        when(captureImageService.uploadImage(eq(999L), eq(1L), any()))
                .thenThrow(new ResourceNotFoundException("Captura no encontrada con ID: 999"));

        // When & Then
        mockMvc.perform(multipart("/api/captures/999/images")
                        .file(file))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Captura no encontrada")));

        verify(captureImageService, times(1)).uploadImage(eq(999L), eq(1L), any());
    }

    @Test
    @DisplayName("POST /api/captures/{captureId}/images - Debe fallar sin permisos")
    void testUploadImage_Unauthorized() throws Exception {
        // Given
        setupAuthentication();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake content".getBytes()
        );

        when(captureImageService.uploadImage(eq(1L), eq(1L), any()))
                .thenThrow(new UnauthorizedException("No tienes permisos para modificar esta captura"));

        // When & Then
        mockMvc.perform(multipart("/api/captures/1/images")
                        .file(file))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("No tienes permisos")));

        verify(captureImageService, times(1)).uploadImage(eq(1L), eq(1L), any());
    }

    @Test
    @DisplayName("POST /api/captures/{captureId}/images - Debe fallar con imagen inválida")
    void testUploadImage_InvalidImage() throws Exception {
        // Given
        setupAuthentication();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake content".getBytes()
        );

        when(captureImageService.uploadImage(eq(1L), eq(1L), any()))
                .thenThrow(new InvalidImageException("El archivo está vacío"));

        // When & Then
        mockMvc.perform(multipart("/api/captures/1/images")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("vacío")));

        verify(captureImageService, times(1)).uploadImage(eq(1L), eq(1L), any());
    }

    @Test
    @DisplayName("POST /api/captures/{captureId}/images - Debe fallar sin autenticación")
    void testUploadImage_Unauthenticated() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake content".getBytes()
        );

        // When & Then (sin llamar a setupAuthentication())
        mockMvc.perform(multipart("/api/captures/1/images")
                        .file(file))
                .andExpect(status().isInternalServerError()); // AuthUser será null

        verify(captureImageService, never()).uploadImage(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("POST /api/captures/{captureId}/images/multiple - Debe subir múltiples imágenes")
    void testUploadMultipleImages_Success() throws Exception {
        // Given
        setupAuthentication();

        MockMultipartFile file1 = new MockMultipartFile("files", "test1.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "test2.jpg", "image/jpeg", "content2".getBytes());

        ImageResponseDto image1 = new ImageResponseDto(1L, "url1", "thumb1", "test1.jpg", 1024L, "image/jpeg", 1920, 1080, LocalDateTime.now());
        ImageResponseDto image2 = new ImageResponseDto(2L, "url2", "thumb2", "test2.jpg", 2048L, "image/jpeg", 1920, 1080, LocalDateTime.now());
        ImageUploadResponseDto response = new ImageUploadResponseDto(1L, List.of(image1, image2), 2, "2 imagen(es) subida(s) correctamente");

        when(captureImageService.uploadMultipleImages(eq(1L), eq(1L), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/captures/1/images/multiple")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.captureId", is(1)))
                .andExpect(jsonPath("$.totalImages", is(2)))
                .andExpect(jsonPath("$.uploadedImages", hasSize(2)))
                .andExpect(jsonPath("$.message", is("2 imagen(es) subida(s) correctamente")));

        verify(captureImageService, times(1)).uploadMultipleImages(eq(1L), eq(1L), any());
    }

    // ==================== TESTS DE OBTENCIÓN DE IMÁGENES ====================

    @Test
    @DisplayName("GET /api/captures/{captureId}/images - Debe obtener todas las imágenes")
    void testGetImagesByCapture_Success() throws Exception {
        // Given
        ImageResponseDto image1 = new ImageResponseDto(1L, "url1", "thumb1", "test1.jpg", 1024L, "image/jpeg", 1920, 1080, LocalDateTime.now());
        ImageResponseDto image2 = new ImageResponseDto(2L, "url2", "thumb2", "test2.jpg", 2048L, "image/jpeg", 1920, 1080, LocalDateTime.now());

        List<ImageResponseDto> images = List.of(image1, image2);
        when(captureImageService.getImagesByCapture(1L)).thenReturn(images);

        // When & Then
        mockMvc.perform(get("/api/captures/1/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].fileName", is("test1.jpg")))
                .andExpect(jsonPath("$[1].fileName", is("test2.jpg")));

        verify(captureImageService, times(1)).getImagesByCapture(1L);
    }

    @Test
    @DisplayName("GET /api/captures/{captureId}/images - Debe retornar lista vacía")
    void testGetImagesByCapture_EmptyList() throws Exception {
        // Given
        when(captureImageService.getImagesByCapture(1L)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/captures/1/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(captureImageService, times(1)).getImagesByCapture(1L);
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

        verify(captureImageService, times(1)).getImagesByCapture(999L);
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
                .andExpect(jsonPath("$.fileName", is("test.jpg")))
                .andExpect(jsonPath("$.fileSize", is(1024)))
                .andExpect(jsonPath("$.width", is(1920)))
                .andExpect(jsonPath("$.height", is(1080)));

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

        verify(captureImageService, times(1)).getImageById(999L);
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

    @Test
    @DisplayName("GET /api/captures/{captureId}/images/count - Debe retornar cero si no hay imágenes")
    void testCountImages_Zero() throws Exception {
        // Given
        when(captureImageService.countImagesByCapture(1L)).thenReturn(0);

        // When & Then
        mockMvc.perform(get("/api/captures/1/images/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(0)));

        verify(captureImageService, times(1)).countImagesByCapture(1L);
    }

    // ==================== TESTS DE ELIMINACIÓN ====================

    @Test
    @DisplayName("DELETE /api/captures/images/{imageId} - Debe eliminar imagen correctamente")
    void testDeleteImage_Success() throws Exception {
        // Given
        setupAuthentication();

        ImageDeleteResponseDto response = ImageDeleteResponseDto.success(1L, 1L);
        when(captureImageService.deleteImage(eq(1L), eq(1L))).thenReturn(response);

        // When & Then
        mockMvc.perform(delete("/api/captures/images/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted", is(true)))
                .andExpect(jsonPath("$.imageId", is(1)))
                .andExpect(jsonPath("$.captureId", is(1)));

        verify(captureImageService, times(1)).deleteImage(eq(1L), eq(1L));
    }

    @Test
    @DisplayName("DELETE /api/captures/images/{imageId} - Debe fallar con imagen inexistente")
    void testDeleteImage_ImageNotFound() throws Exception {
        // Given
        setupAuthentication();

        when(captureImageService.deleteImage(eq(999L), eq(1L)))
                .thenThrow(new ResourceNotFoundException("Imagen no encontrada con ID: 999"));

        // When & Then
        mockMvc.perform(delete("/api/captures/images/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Imagen no encontrada")));

        verify(captureImageService, times(1)).deleteImage(eq(999L), eq(1L));
    }

    @Test
    @DisplayName("DELETE /api/captures/images/{imageId} - Debe fallar sin permisos")
    void testDeleteImage_Unauthorized() throws Exception {
        // Given
        setupAuthentication();

        when(captureImageService.deleteImage(eq(1L), eq(1L)))
                .thenThrow(new UnauthorizedException("No tienes permisos para eliminar esta imagen"));

        // When & Then
        mockMvc.perform(delete("/api/captures/images/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("No tienes permisos")));

        verify(captureImageService, times(1)).deleteImage(eq(1L), eq(1L));
    }

    @Test
    @DisplayName("DELETE /api/captures/images/{imageId} - Debe fallar sin autenticación")
    void testDeleteImage_Unauthenticated() throws Exception {
        // When & Then (sin llamar a setupAuthentication())
        mockMvc.perform(delete("/api/captures/images/1"))
                .andExpect(status().isInternalServerError()); // AuthUser será null

        verify(captureImageService, never()).deleteImage(anyLong(), anyLong());
    }

    @Test
    @DisplayName("DELETE /api/captures/{captureId}/images - Debe eliminar todas las imágenes")
    void testDeleteAllImages_Success() throws Exception {
        // Given
        setupAuthentication();

        doNothing().when(captureImageService).deleteAllImagesByCapture(eq(1L), eq(1L));

        // When & Then
        mockMvc.perform(delete("/api/captures/1/images"))
                .andExpect(status().isNoContent());

        verify(captureImageService, times(1)).deleteAllImagesByCapture(eq(1L), eq(1L));
    }

    @Test
    @DisplayName("DELETE /api/captures/{captureId}/images - Debe fallar con captura inexistente")
    void testDeleteAllImages_CaptureNotFound() throws Exception {
        // Given
        setupAuthentication();

        doThrow(new ResourceNotFoundException("Captura no encontrada con ID: 999"))
                .when(captureImageService).deleteAllImagesByCapture(eq(999L), eq(1L));

        // When & Then
        mockMvc.perform(delete("/api/captures/999/images"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Captura no encontrada")));

        verify(captureImageService, times(1)).deleteAllImagesByCapture(eq(999L), eq(1L));
    }

    @Test
    @DisplayName("DELETE /api/captures/{captureId}/images - Debe fallar sin permisos")
    void testDeleteAllImages_Unauthorized() throws Exception {
        // Given
        setupAuthentication();

        doThrow(new UnauthorizedException("No tienes permisos para eliminar estas imágenes"))
                .when(captureImageService).deleteAllImagesByCapture(eq(1L), eq(1L));

        // When & Then
        mockMvc.perform(delete("/api/captures/1/images"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("No tienes permisos")));

        verify(captureImageService, times(1)).deleteAllImagesByCapture(eq(1L), eq(1L));
    }

    @Test
    @DisplayName("DELETE /api/captures/{captureId}/images - Debe fallar sin autenticación")
    void testDeleteAllImages_Unauthenticated() throws Exception {
        // When & Then (sin llamar a setupAuthentication())
        mockMvc.perform(delete("/api/captures/1/images"))
                .andExpect(status().isInternalServerError()); // AuthUser será null

        verify(captureImageService, never()).deleteAllImagesByCapture(anyLong(), anyLong());
    }

    // ==================== TESTS DE VALIDACIÓN DE PARÁMETROS ====================

    @Test
    @DisplayName("Debe fallar con ID de captura inválido")
    void testUploadImage_InvalidCaptureId() throws Exception {
        // Given
        setupAuthentication();

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

        // When & Then - ID no numérico
        mockMvc.perform(multipart("/api/captures/invalid/images")
                        .file(file))
                .andExpect(status().isBadRequest());

        verify(captureImageService, never()).uploadImage(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("Debe fallar con ID de imagen inválido en GET")
    void testGetImageById_InvalidImageId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/captures/images/invalid"))
                .andExpect(status().isBadRequest());

        verify(captureImageService, never()).getImageById(anyLong());
    }

    @Test
    @DisplayName("Debe fallar con ID de imagen inválido en DELETE")
    void testDeleteImage_InvalidImageId() throws Exception {
        // Given
        setupAuthentication();

        // When & Then
        mockMvc.perform(delete("/api/captures/images/invalid"))
                .andExpect(status().isBadRequest());

        verify(captureImageService, never()).deleteImage(anyLong(), anyLong());
    }
}