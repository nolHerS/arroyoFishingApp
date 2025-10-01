package com.example.fishingapp.service;

import com.example.fishingapp.dto.auth.AuthResponse;
import com.example.fishingapp.dto.auth.LoginRequest;
import com.example.fishingapp.dto.auth.RefreshTokenRequest;
import com.example.fishingapp.dto.auth.RegisterRequest;

public interface AuthService {

    /**
     * Registra un nuevo usuario en el sistema
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Autentica un usuario y genera tokens JWT
     */
    AuthResponse login(LoginRequest request);

    /**
     * Renueva el access token usando un refresh token válido
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Revoca un refresh token (logout)
     */
    void revokeRefreshToken(String refreshToken);

    /**
     * Revoca todos los refresh tokens de un usuario (logout de todas las sesiones)
     */
    void revokeAllUserTokens(String email);
}
