package com.example.fishingapp.service.impl;

import com.example.fishingapp.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.name:CharcaFishing}")
    private String appName;

    @Override
    public void sendVerificationEmail(String toEmail, String username, String verificationToken) {
        String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;

        String subject = "Verifica tu cuenta en " + appName;
        String body = buildVerificationEmailBody(username, verificationLink);

        sendEmail(toEmail, subject, body);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        String subject = "Recuperación de contraseña - " + appName;
        String body = buildPasswordResetEmailBody(username, resetLink);

        sendEmail(toEmail, subject, body);
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String username) {
        String subject = "¡Bienvenido a " + appName + "!";
        String body = buildWelcomeEmailBody(username);

        sendEmail(toEmail, subject, body);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        // TODO: Implementar envío real con JavaMailSender cuando configures SMTP
        // Por ahora, solo logueamos el email

        log.info("========================================");
        log.info("📧 EMAIL ENVIADO (MOCK)");
        log.info("Para: {}", to);
        log.info("Asunto: {}", subject);
        log.info("Cuerpo:\n{}", body);
        log.info("========================================");

        /* Implementación real cuando configures SMTP:

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true); // true = HTML

        mailSender.send(message);
        */
    }

    /**
     * Construye el cuerpo del email de verificación
     */
    private String buildVerificationEmailBody(String username, String verificationLink) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2>¡Hola %s!</h2>
                <p>Gracias por registrarte en %s.</p>
                <p>Por favor, verifica tu cuenta haciendo clic en el siguiente enlace:</p>
                <a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">
                    Verificar mi cuenta
                </a>
                <p>Este enlace expirará en 24 horas.</p>
                <p>Si no solicitaste este registro, ignora este correo.</p>
                <br>
                <p>Saludos,<br>El equipo de %s</p>
            </body>
            </html>
            """, username, appName, verificationLink, appName);
    }

    /**
     * Construye el cuerpo del email de reseteo de contraseña
     */
    private String buildPasswordResetEmailBody(String username, String resetLink) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2>Hola %s</h2>
                <p>Recibimos una solicitud para restablecer tu contraseña en %s.</p>
                <p>Haz clic en el siguiente enlace para crear una nueva contraseña:</p>
                <a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #dc3545; color: white; text-decoration: none; border-radius: 5px;">
                    Restablecer contraseña
                </a>
                <p>Este enlace expirará en 24 horas.</p>
                <p><strong>Si no solicitaste este cambio, ignora este correo y tu contraseña permanecerá sin cambios.</strong></p>
                <br>
                <p>Saludos,<br>El equipo de %s</p>
            </body>
            </html>
            """, username, appName, resetLink, appName);
    }

    /**
     * Construye el cuerpo del email de bienvenida
     */
    private String buildWelcomeEmailBody(String username) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2>¡Bienvenido a %s, %s! 🎣</h2>
                <p>Tu cuenta ha sido verificada exitosamente.</p>
                <p>Ahora puedes:</p>
                <ul>
                    <li>Compartir tus capturas de pesca</li>
                    <li>Ver las capturas de otros pescadores</li>
                    <li>Conectar con la comunidad pesquera de Arroyo de la Luz</li>
                </ul>
                <p>¡Esperamos que disfrutes tu experiencia!</p>
                <br>
                <p>Saludos,<br>El equipo de %s</p>
            </body>
            </html>
            """, appName, username, appName);
    }
}
