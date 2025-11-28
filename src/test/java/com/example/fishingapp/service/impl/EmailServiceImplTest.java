package com.example.fishingapp.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@TestPropertySource(properties = {
        "app.frontend.url=http://localhost:3000",
        "app.name=CharcaFishing"
})
class EmailServiceImplTest {

    @Autowired
    private EmailServiceImpl emailService;

    @Test
    void sendVerificationEmail_sendsEmailSuccessfully() {
        String email = "test@example.com";
        String username = "testuser";
        String token = "verification-token-123";

        assertDoesNotThrow(() ->
                emailService.sendVerificationEmail(email, username, token)
        );
    }

    @Test
    void sendPasswordResetEmail_sendsEmailSuccessfully() {
        String email = "test@example.com";
        String username = "testuser";
        String token = "reset-token-123";

        assertDoesNotThrow(() ->
                emailService.sendPasswordResetEmail(email, username, token)
        );
    }

    @Test
    void sendWelcomeEmail_sendsEmailSuccessfully() {
        String email = "test@example.com";
        String username = "testuser";

        assertDoesNotThrow(() ->
                emailService.sendWelcomeEmail(email, username)
        );
    }

    @Test
    void sendEmail_sendsGenericEmailSuccessfully() {
        String email = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        assertDoesNotThrow(() ->
                emailService.sendEmail(email, subject, body)
        );
    }

    @Test
    void sendVerificationEmail_withNullValues_doesNotThrowException() {
        assertDoesNotThrow(() ->
                emailService.sendVerificationEmail(null, null, null)
        );
    }

    @Test
    void sendPasswordResetEmail_withNullValues_doesNotThrowException() {
        assertDoesNotThrow(() ->
                emailService.sendPasswordResetEmail(null, null, null)
        );
    }

    @Test
    void sendWelcomeEmail_withNullValues_doesNotThrowException() {
        assertDoesNotThrow(() ->
                emailService.sendWelcomeEmail(null, null)
        );
    }
}
