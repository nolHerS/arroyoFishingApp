package com.example.fishingapp.service;

public interface EmailService {
    /**
     * Envía un email de verificación al usuario
     */
    void sendVerificationEmail(String toEmail, String username, String verificationToken);

    /**
     * Envía un email para resetear la contraseña
     */
    void sendPasswordResetEmail(String toEmail, String username, String resetToken);

    /**
     * Envía un email de bienvenida después del registro exitoso
     */
    void sendWelcomeEmail(String toEmail, String username);

    /**
     * Envía un email genérico
     */
    void sendEmail(String to, String subject, String body);
}
