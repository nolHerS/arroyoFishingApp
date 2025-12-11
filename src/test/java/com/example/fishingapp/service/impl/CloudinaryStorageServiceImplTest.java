package com.example.fishingapp.service.impl;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.api.ApiResponse;
import com.example.fishingapp.dto.FileMetaData;
import com.example.fishingapp.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CloudinaryStorageServiceImplTest {

    private Uploader uploader;
    private Api cloudinaryApi;
    private ApiResponse apiResponse;

    private CloudinaryStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        Cloudinary cloudinary = mock(Cloudinary.class);
        uploader = mock(Uploader.class);
        cloudinaryApi = mock(Api.class);
        apiResponse = mock(ApiResponse.class);

        storageService = new CloudinaryStorageServiceImpl(cloudinary);

        when(cloudinary.uploader()).thenReturn(uploader);
        when(cloudinary.api()).thenReturn(cloudinaryApi);
    }

    // -------------------------------------------------------------
    // Upload tests
    // -------------------------------------------------------------

    @Test
    void uploadFile_success() throws Exception {
        String key = "fish-captures/captures/user_1/capture_1/test.jpg";
        byte[] fileContent = "fake image".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", "https://cloudinary.com/myimage.jpg"));

        String url = storageService.uploadFile(key, inputStream, fileContent.length, "image/jpeg");

        assertEquals("https://cloudinary.com/myimage.jpg", url);
        verify(uploader, times(1)).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadFile_failure_throwsException() throws Exception {
        String key = "fish-captures/captures/user_1/capture_1/test.jpg";
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());

        when(uploader.upload(any(), anyMap()))
                .thenThrow(StorageException.class);

        assertThrows(StorageException.class, () ->
                storageService.uploadFile(key, inputStream, 4, "image/jpeg")
        );
    }

    // -------------------------------------------------------------
    // Delete tests
    // -------------------------------------------------------------

    @Test
    void deleteFile_success() throws Exception {
        when(uploader.destroy(anyString(), anyMap()))
                .thenReturn(Map.of("result", "ok"));

        assertDoesNotThrow(() ->
                storageService.deleteFile("fish-captures/captures/user_1/capture_1/test.jpg")
        );
    }

    @Test
    void deleteFile_failure_throwsException() throws Exception {
        when(uploader.destroy(anyString(), anyMap()))
                .thenThrow(StorageException.class);

        assertThrows(StorageException.class, () ->
                storageService.deleteFile("fish-captures/captures/user_1/capture_1/test.jpg")
        );
    }

    // -------------------------------------------------------------
    // fileExists tests (mockear ApiResponse correctamente)
    // -------------------------------------------------------------

    @Test
    void fileExists_true() throws Exception {
        // cloudinary.api().resource(...) devuelve ApiResponse
        when(cloudinaryApi.resource(anyString(), anyMap()))
                .thenReturn(apiResponse);

        // ApiResponse actúa como Map: configure containsKey/get
        when(apiResponse.containsKey("public_id")).thenReturn(true);
        when(apiResponse.get("public_id")).thenReturn("fish-captures/captures/user_1/capture_1/test");

        assertTrue(storageService.fileExists("fish-captures/captures/user_1/capture_1/test.jpg"));

        verify(cloudinaryApi, times(1)).resource(anyString(), anyMap());
    }

    @Test
    void fileExists_false_whenException() throws Exception {
        when(cloudinaryApi.resource(anyString(), anyMap()))
                .thenThrow(new RuntimeException("Not found"));

        assertFalse(storageService.fileExists("fish-captures/captures/user_1/capture_1/test.jpg"));
    }

    // -------------------------------------------------------------
    // getFileMetadata tests (mockear ApiResponse con get/containsKey)
    // -------------------------------------------------------------

    @Test
    void getFileMetadata_success() throws Exception {
        when(cloudinaryApi.resource(anyString(), anyMap()))
                .thenReturn(apiResponse);

        when(apiResponse.get("bytes")).thenReturn(1234);
        when(apiResponse.get("format")).thenReturn("jpg");
        when(apiResponse.get("created_at")).thenReturn("2025-02-23T12:45:00Z");

        FileMetaData data = storageService.getFileMetadata(
                "fish-captures/captures/user_1/capture_1/test.jpg"
        );

//        assertEquals(1234L, data.bytes());
        assertEquals("image/jpg", data.contentType());
        assertEquals(Instant.parse("2025-02-23T12:45:00Z"), data.lastModified());

        verify(cloudinaryApi, times(1)).resource(anyString(), anyMap());
        verify(apiResponse, atLeastOnce()).get(anyString());
    }

    @Test
    void getFileMetadata_failure_throwsException() throws Exception {
        when(cloudinaryApi.resource(anyString(), anyMap()))
                .thenThrow(new RuntimeException("API error"));

        assertThrows(StorageException.class, () ->
                storageService.getFileMetadata("fish-captures/captures/user_1/capture_1/test.jpg")
        );
    }

    // -------------------------------------------------------------
    // buildFileKey test
    // -------------------------------------------------------------

    @Test
    void buildFileKey_generatesCorrectFormat() {
        String key = storageService.buildFileKey(1L, 2L, "foto prueba.JPG");

        assertTrue(key.contains("fish-captures/captures/user_1/capture_2/"));
        assertTrue(key.endsWith("_foto_prueba.jpg"));
    }
}
