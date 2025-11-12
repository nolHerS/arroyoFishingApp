package com.example.fishingapp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class S3ConfigTest {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Config s3Config;

    @Test
    void testS3Connection() {
        System.out.println("=== DEBUG S3 CONFIG ===");
        System.out.println("Endpoint configurado: " + s3Config.getEndpoint());
        System.out.println("Bucket: " + s3Config.getBucketName());
        System.out.println("Access Key: " + s3Config.getAccessKey().substring(0, 4) + "...");
        System.out.println("======================");

        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(s3Config.getBucketName())
                    .maxKeys(1)
                    .build();

            System.out.println("Intentando listar objetos del bucket: " + s3Config.getBucketName());
            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            System.out.println("✅ Conexión exitosa!");
            System.out.println("Objetos encontrados: " + response.contents().size());

        } catch (S3Exception e) {
            System.err.println("❌ Error: " + e.awsErrorDetails().errorMessage());
            System.err.println("Status Code: " + e.statusCode());
            System.err.println("Request ID: " + e.requestId());
            fail("Error de conexión: " + e.getMessage());
        }
    }
}