package com.example.fishingapp.service;

import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.security.VerificationToken;

public interface VerificationTokenService {

    /**
     * Crea un token de verificación de email para un usuario
     */
    VerificationToken createEmailVerificationToken(AuthUser authUser);

    /**
     * Crea un token de reseteo de contraseña para un usuario
     */
    VerificationToken createPasswordResetToken(AuthUser authUser);

    /**
     * Verifica y valida un token de verificación
     * @return El AuthUser asociado al token
     */
    AuthUser verifyToken(String token, VerificationToken.TokenType expectedType);

    /**
     * Verifica si un token es válido (existe, no usado, no expirado)
     */
    boolean isTokenValid(String token);

    /**
     * Invalida/marca como usado un token
     */
    void invalidateToken(String token);

    /**
     * Elimina tokens expirados (útil para tareas programadas)
     */
    void deleteExpiredTokens();

    /**
     * Elimina todos los tokens de un usuario
     */
    void deleteUserTokens(AuthUser authUser);
}
