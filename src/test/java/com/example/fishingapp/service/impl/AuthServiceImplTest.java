package com.example.fishingapp.service.impl;

import com.example.fishingapp.config.BaseIntegrationTest;
import com.example.fishingapp.dto.auth.AuthResponse;
import com.example.fishingapp.dto.auth.LoginRequest;
import com.example.fishingapp.dto.auth.RefreshTokenRequest;
import com.example.fishingapp.dto.auth.RegisterRequest;
import com.example.fishingapp.exception.EmailAlreadyExistsException;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.exception.UsernameAlreadyExistsException;
import com.example.fishingapp.model.User;

import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.security.RefreshToken;
import com.example.fishingapp.security.Role;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B597033733676397924423F4528482B4D6251655468576D5A7134743777217A25432A",
        "jwt.expiration=3600000",
        "jwt.refresh-token.expiration=604800000"
})
class AuthServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_withValidData_createsUserAndReturnsAuthResponse() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .fullName("New User")
                .email("newuser@example.com")
                .password("password123")
                .build();

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getUser());
        assertEquals("newuser", response.getUser().getUsername());
        assertEquals("newuser@example.com", response.getUser().getEmail());
        assertEquals("USER", response.getUser().getRole());

        // Verificar que se guardó en BD
        assertTrue(userRepository.existsByUsername("newuser"));
        assertTrue(authUserRepository.existsByEmail("newuser@example.com"));
    }

    @Test
    void register_withExistingEmail_throwsEmailAlreadyExistsException() {
        // Crear usuario existente
        createTestUser("existing@example.com", "existinguser");

        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .fullName("New User")
                .email("existing@example.com")
                .password("password123")
                .build();

        assertThrows(EmailAlreadyExistsException.class, () ->
                authService.register(request)
        );
    }

    @Test
    void register_withExistingUsername_throwsUsernameAlreadyExistsException() {
        // Crear usuario existente
        createTestUser("user@example.com", "existinguser");

        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .fullName("New User")
                .email("newemail@example.com")
                .password("password123")
                .build();

        assertThrows(UsernameAlreadyExistsException.class, () ->
                authService.register(request)
        );
    }

    @Test
    void login_withValidCredentials_returnsAuthResponse() {
        // Crear usuario
        createTestUser("user@example.com", "testuser", "password123");

        LoginRequest request = LoginRequest.builder()
                .identifier("user@example.com")
                .password("password123")
                .build();

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getUser());

        // Verificar que se actualizó lastLoginAt
        AuthUser authUser = authUserRepository.findByEmail("user@example.com").orElseThrow();
        assertNotNull(authUser.getLastLoginAt());
    }

    @Test
    void login_withInvalidEmail_throwsBadCredentialsException() {
        LoginRequest request = LoginRequest.builder()
                .identifier("nonexistent@example.com")
                .password("password123")
                .build();

        assertThrows(BadCredentialsException.class, () ->
                authService.login(request)
        );
    }

    @Test
    void login_withInvalidPassword_throwsBadCredentialsException() {
        createTestUser("user@example.com", "testuser", "correctpassword");

        LoginRequest request = LoginRequest.builder()
                .identifier("user@example.com")
                .password("wrongpassword")
                .build();

        assertThrows(BadCredentialsException.class, () ->
                authService.login(request)
        );
    }

    @Test
    void refreshToken_withValidToken_returnsNewAccessToken() {
        // Crear usuario y hacer login
        createTestUser("user@example.com", "testuser", "password123");
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier("user@example.com")
                .password("password123")
                .build();
        AuthResponse loginResponse = authService.login(loginRequest);

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(loginResponse.getRefreshToken())
                .build();

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals(loginResponse.getRefreshToken(), response.getRefreshToken());
    }

    @Test
    void refreshToken_withInvalidToken_throwsResourceNotFoundException() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalid-token")
                .build();

        assertThrows(ResourceNotFoundException.class, () ->
                authService.refreshToken(request)
        );
    }

    @Test
    void refreshToken_withRevokedToken_throwsRuntimeException() {
        // Crear usuario y obtener refresh token
        createTestUser("user@example.com", "testuser", "password123");
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier("user@example.com")
                .password("password123")
                .build();
        AuthResponse loginResponse = authService.login(loginRequest);

        // Revocar el token
        authService.revokeRefreshToken(loginResponse.getRefreshToken());

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(loginResponse.getRefreshToken())
                .build();

        assertThrows(RuntimeException.class, () ->
                authService.refreshToken(request)
        );
    }

    @Test
    void refreshToken_withExpiredToken_throwsRuntimeException() {
        // Crear usuario y token
        AuthUser authUser = createTestUser("user@example.com", "testuser", "password123");

        // Crear token expirado manualmente
        RefreshToken expiredToken = RefreshToken.builder()
                .authUser(authUser)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();
        expiredToken = refreshTokenRepository.save(expiredToken);

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(expiredToken.getToken())
                .build();

        assertThrows(RuntimeException.class, () ->
                authService.refreshToken(request)
        );

        // Verificar que el token fue eliminado
        assertFalse(refreshTokenRepository.existsById(expiredToken.getId()));
    }

    @Test
    void revokeRefreshToken_withValidToken_revokesToken() {
        // Crear usuario y hacer login
        createTestUser("user@example.com", "testuser", "password123");
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier("user@example.com")
                .password("password123")
                .build();
        AuthResponse loginResponse = authService.login(loginRequest);

        authService.revokeRefreshToken(loginResponse.getRefreshToken());

        RefreshToken revokedToken = refreshTokenRepository.findByToken(loginResponse.getRefreshToken()).orElseThrow();
        assertTrue(revokedToken.getRevoked());
    }

    @Test
    void revokeRefreshToken_withInvalidToken_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () ->
                authService.revokeRefreshToken("invalid-token")
        );
    }

    @Test
    void revokeAllUserTokens_deletesAllTokens() {
        // Crear usuario y hacer múltiples logins
        createTestUser("user@example.com", "testuser", "password123");
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier("user@example.com")
                .password("password123")
                .build();

        authService.login(loginRequest);
        authService.login(loginRequest);

        long tokenCount = refreshTokenRepository.count();
        assertTrue(tokenCount > 0);

        authService.revokeAllUserTokens("user@example.com");

        assertEquals(0, refreshTokenRepository.count());
    }

    @Test
    void revokeAllUserTokens_withInvalidEmail_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () ->
                authService.revokeAllUserTokens("nonexistent@example.com")
        );
    }

    @Test
    void register_createsRefreshToken() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .fullName("New User")
                .email("newuser@example.com")
                .password("password123")
                .build();

        AuthResponse response = authService.register(request);

        // Verificar que existe el refresh token en BD
        assertTrue(refreshTokenRepository.findByToken(response.getRefreshToken()).isPresent());
    }

    @Test
    void login_deletesOldRefreshTokens() {
        // Crear usuario y hacer múltiples logins
        createTestUser("user@example.com", "testuser", "password123");
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier("user@example.com")
                .password("password123")
                .build();

        authService.login(loginRequest);
        AuthResponse secondLogin = authService.login(loginRequest);

        // Solo debería existir el último refresh token
        assertEquals(1, refreshTokenRepository.count());
        assertTrue(refreshTokenRepository.findByToken(secondLogin.getRefreshToken()).isPresent());
    }

    // Método auxiliar para crear usuarios de prueba
    private void createTestUser(String email, String username) {
        createTestUser(email, username, "password123");
    }

    private AuthUser createTestUser(String email, String username, String password) {
        User user = User.builder()
                .username(username)
                .fullName("Test User")
                .email(email)
                .build();
        user = userRepository.save(user);

        AuthUser authUser = AuthUser.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        return authUserRepository.save(authUser);
    }
}