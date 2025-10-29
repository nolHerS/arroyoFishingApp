package com.example.fishingapp.service;

import com.example.fishingapp.config.S3Config;
import com.example.fishingapp.exception.StorageException;
import com.example.fishingapp.service.impl.S3StorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para S3StorageServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("S3StorageService - Tests Unitarios")
class S3StorageServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Config s3Config;

    @InjectMocks
    private S3StorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        // Configurar comportamiento del S3Config mock
        lenient().when(s3Config.getBucketName()).thenReturn("test-bucket");
        lenient().when(s3Config.getEndpoint()).thenReturn("https://s3.tebi.io");
    }

    @Test
    @DisplayName("Debe subir archivo correctamente y retornar URL pública")
    void testUploadFile_Success() {
        // Given
        String key = "captures/user_1/capture_5/test.jpg";
        byte[] fileContent = "test image content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        String contentType = "image/jpeg";

        // Mock successful S3 upload
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // When
        String resultUrl = storageService.uploadFile(key, inputStream, fileContent.length, contentType);

        // Then
        assertThat(resultUrl, notNullValue());
        assertThat(resultUrl, containsString("https://s3.tebi.io"));
        assertThat(resultUrl, containsString("test-bucket"));
        assertThat(resultUrl, containsString(key));

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Debe lanzar StorageException cuando S3 falla al subir")
    void testUploadFile_ThrowsStorageException_WhenS3Fails() {
        // Given
        String key = "captures/user_1/capture_5/test.jpg";
        byte[] fileContent = "test image content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        String contentType = "image/jpeg";

        // Mock S3 exception
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("S3 Error").build());

        // When & Then
        StorageException exception = assertThrows(
                StorageException.class,
                () -> storageService.uploadFile(key, inputStream, fileContent.length, contentType)
        );

        assertThat(exception.getMessage(), containsString("Error al subir el archivo"));
    }

    @Test
    @DisplayName("Debe eliminar archivo correctamente de S3")
    void testDeleteFile_Success() {
        // Given
        String key = "captures/user_1/capture_5/test.jpg";

        // Mock successful deletion
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        // When & Then
        storageService.deleteFile(key);

        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Debe lanzar StorageException cuando S3 falla al eliminar")
    void testDeleteFile_ThrowsStorageException_WhenS3Fails() {
        // Given
        String key = "captures/user_1/capture_5/test.jpg";

        // Mock S3 exception
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("Delete failed").build());

        // When & Then
        StorageException exception = assertThrows(
                StorageException.class,
                () -> storageService.deleteFile(key)
        );

        assertThat(exception.getMessage(), containsString("Error al eliminar el archivo"));
    }

    @Test
    @DisplayName("Debe verificar si un archivo existe en S3")
    void testFileExists_ReturnsTrue_WhenFileExists() {
        // Given
        String key = "captures/user_1/capture_5/test.jpg";

        // Mock file exists
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());

        // When
        boolean exists = storageService.fileExists(key);

        // Then
        assertThat(exists, is(true));
        verify(s3Client, times(1)).headObject(any(HeadObjectRequest.class));
    }

    @Test
    @DisplayName("Debe retornar false si el archivo no existe en S3")
    void testFileExists_ReturnsFalse_WhenFileDoesNotExist() {
        // Given
        String key = "captures/user_1/capture_5/nonexistent.jpg";

        // Mock file doesn't exist
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        // When
        boolean exists = storageService.fileExists(key);

        // Then
        assertThat(exists, is(false));
    }

    @Test
    @DisplayName("Debe obtener metadatos del archivo correctamente")
    void testGetFileMetadata_Success() {
        // Given
        String key = "captures/user_1/capture_5/test.jpg";

        HeadObjectResponse expectedResponse = HeadObjectResponse.builder()
                .contentLength(1024L)
                .contentType("image/jpeg")
                .build();

        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(expectedResponse);

        // When
        HeadObjectResponse metadata = storageService.getFileMetadata(key);

        // Then
        assertThat(metadata, notNullValue());
        assertThat(metadata.contentLength(), is(1024L));
        assertThat(metadata.contentType(), is("image/jpeg"));
    }

    @Test
    @DisplayName("Debe lanzar StorageException cuando falla al obtener metadatos")
    void testGetFileMetadata_ThrowsStorageException_WhenFails() {
        // Given
        String key = "captures/user_1/capture_5/test.jpg";

        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("Metadata error").build());

        // When & Then
        StorageException exception = assertThrows(
                StorageException.class,
                () -> storageService.getFileMetadata(key)
        );

        assertThat(exception.getMessage(), containsString("Error al obtener información del archivo"));
    }

    @Test
    @DisplayName("Debe construir key de archivo correctamente")
    void testBuildFileKey_Success() {
        // Given
        Long userId = 1L;
        Long captureId = 5L;
        String fileName = "my fish.jpg";

        // When
        String key = storageService.buildFileKey(userId, captureId, fileName);

        // Then
        assertThat(key, notNullValue());
        assertThat(key, startsWith("captures/user_1/capture_5/"));
        assertThat(key, containsString("my_fish.jpg"));
        assertThat(key, not(containsString(" "))); // No debe contener espacios
    }

    @Test
    @DisplayName("Debe construir thumbnail key correctamente")
    void testBuildThumbnailKey_Success() {
        // Given
        Long userId = 1L;
        Long captureId = 5L;
        String fileName = "test.jpg";

        // When
        String key = storageService.buildThumbnailKey(userId, captureId, fileName);

        // Then
        assertThat(key, notNullValue());
        assertThat(key, startsWith("thumbnails/user_1/capture_5/"));
        assertThat(key, containsString("thumb"));
        assertThat(key, endsWith("test.jpg"));
    }

    @Test
    @DisplayName("Debe sanitizar nombres de archivo con caracteres especiales")
    void testBuildFileKey_SanitizesSpecialCharacters() {
        // Given
        Long userId = 1L;
        Long captureId = 5L;
        String fileName = "Mi Pez #1 (grande).jpg";

        // When
        String key = storageService.buildFileKey(userId, captureId, fileName);

        // Then
        assertThat(key, not(containsString("#")));
        assertThat(key, not(containsString("(")));
        assertThat(key, not(containsString(")")));
        assertThat(key, not(containsString(" ")));
    }
}