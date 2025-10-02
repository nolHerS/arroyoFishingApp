package com.example.fishingapp.controller;

import com.example.fishingapp.dto.auth.*;
import com.example.fishingapp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Registra un nuevo usuario
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Inicia sesión (login)
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Renueva el access token usando un refresh token
     * POST /api/auth/refresh-token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cierra sesión (revoca el refresh token)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.revokeRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok(
                MessageResponse.builder()
                        .message("Sesión cerrada correctamente")
                        .success(true)
                        .build()
        );
    }

    /**
     * Cierra todas las sesiones de un usuario
     * POST /api/auth/logout-all
     */
    @PostMapping("/logout-all")
    public ResponseEntity<MessageResponse> logoutAll(@RequestParam String email) {
        authService.revokeAllUserTokens(email);
        return ResponseEntity.ok(
                MessageResponse.builder()
                        .message("Todas las sesiones han sido cerradas")
                        .success(true)
                        .build()
        );
    }

    /**
     * Endpoint de prueba para verificar que el servidor está funcionando
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(
                MessageResponse.builder()
                        .message("Auth service is running")
                        .success(true)
                        .build()
        );
    }
}