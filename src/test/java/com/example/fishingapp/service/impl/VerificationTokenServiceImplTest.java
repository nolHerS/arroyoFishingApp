package com.example.fishingapp.service;

import com.example.fishingapp.config.BaseIntegrationTest;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.AuthUserRepository;
import com.example.fishingapp.repository.UserRepository;
import com.example.fishingapp.repository.VerificationTokenRepository;
import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.security.Role;
import com.example.fishingapp.security.VerificationToken;
import com.example.fishingapp.service.impl.VerificationTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "verification.token.expiration=86400"
})
public class VerificationTokenServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private VerificationTokenServiceImpl verificationTokenService;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private UserRepository userRepository;

    private AuthUser testAuthUser;

    @BeforeEach
    void setUp() {
        verificationTokenRepository.deleteAll();
        authUserRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .build();
        user = userRepository.save(user);

        testAuthUser = AuthUser.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .password("password")
                .role(Role.USER)
                .enabled(false)
                .accountNonLocked(true)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        testAuthUser = authUserRepository.save(testAuthUser);
    }

    @Test
    void createEmailVerificationToken_createsToken() {
        VerificationToken token = verificationTokenService.createEmailVerificationToken(testAuthUser);

        assertNotNull(token);
        assertNotNull(token.getToken());
        assertEquals(VerificationToken.TokenType.EMAIL_VERIFICATION, token.getTokenType());
        assertEquals(testAuthUser.getId(), token.getAuthUser().getId());
        assertFalse(token.getUsed());
        assertFalse(token.isExpired());
    }@Test
    void createPasswordResetToken_createsToken() {
        VerificationToken token = verificationTokenService.createPasswordResetToken(testAuthUser);

        assertNotNull(token);
        assertNotNull(token.getToken());
        assertEquals(VerificationToken.TokenType.PASSWORD_RESET, token.getTokenType());
        assertEquals(testAuthUser.getId(), token.getAuthUser().getId());
        assertFalse(token.getUsed());
        assertFalse(token.isExpired());
    }

    @Test
    void createEmailVerificationToken_deletesOldTokens() {
        VerificationToken firstToken = verificationTokenService.createEmailVerificationToken(testAuthUser);
        VerificationToken secondToken = verificationTokenService.createEmailVerificationToken(testAuthUser);

        assertNotEquals(firstToken.getToken(), secondToken.getToken());

        // El primer token debería haber sido eliminado
        assertFalse(verificationTokenRepository.existsById(firstToken.getId()));
        assertTrue(verificationTokenRepository.existsById(secondToken.getId()));
    }

    @Test
    void verifyToken_withValidToken_returnsAuthUser() {
        VerificationToken token = verificationTokenService.createEmailVerificationToken(testAuthUser);

        AuthUser verifiedUser = verificationTokenService.verifyToken(
                token.getToken(),
                VerificationToken.TokenType.EMAIL_VERIFICATION
        );

        assertNotNull(verifiedUser);
        assertEquals(testAuthUser.getId(), verifiedUser.getId());

        // Verificar que el token fue marcado como usado
        VerificationToken updatedToken = verificationTokenRepository.findById(token.getId()).orElseThrow();
        assertTrue(updatedToken.getUsed());
    }

    @Test
    void verifyToken_withInvalidToken_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () ->
                verificationTokenService.verifyToken(
                        "invalid-token",
                        VerificationToken.TokenType.EMAIL_VERIFICATION
                )
        );
    }

    @Test
    void verifyToken_withWrongType_throwsIllegalArgumentException() {
        VerificationToken token = verificationTokenService.createEmailVerificationToken(testAuthUser);

        assertThrows(IllegalArgumentException.class, () ->
                verificationTokenService.verifyToken(
                        token.getToken(),
                        VerificationToken.TokenType.PASSWORD_RESET
                )
        );
    }

    @Test
    void verifyToken_withUsedToken_throwsIllegalStateException() {
        VerificationToken token = verificationTokenService.createEmailVerificationToken(testAuthUser);

        // Usar el token una vez
        verificationTokenService.verifyToken(
                token.getToken(),
                VerificationToken.TokenType.EMAIL_VERIFICATION
        );

        // Intentar usar el token de nuevo
        assertThrows(IllegalStateException.class, () ->
                verificationTokenService.verifyToken(
                        token.getToken(),
                        VerificationToken.TokenType.EMAIL_VERIFICATION
                )
        );
    }

    @Test
    void verifyToken_withExpiredToken_throwsIllegalStateException() {
        VerificationToken token = verificationTokenService.createEmailVerificationToken(testAuthUser);

        // Forzar expiración
        token.setExpiryDate(LocalDateTime.now().minusDays(1));
        verificationTokenRepository.save(token);

        assertThrows(IllegalStateException.class, () ->
                verificationTokenService.verifyToken(
                        token.getToken(),
                        VerificationToken.TokenType.EMAIL_VERIFICATION
                )
        );

        // Verificar que el token fue eliminado
        assertFalse(verificationTokenRepository.existsById(token.getId()));
    }

    @Test
    void isTokenValid_withValidToken_returnsTrue() {
        VerificationToken token = verificationTokenService.createEmailVerificationToken(testAuthUser);

        boolean isValid = verificationTokenService.isTokenValid(token.getToken());

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_withUsedToken_returnsFalse() {
        VerificationToken token = verificationTokenService.createEmailVerificationToken(testAuthUser);
        token.setUsed(true);
        verificationTokenRepository.save(token);

        boolean isValid = verificationTokenService.isTokenValid(token.getToken());

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_withExpiredToken_returnsFalse() {
        VerificationToken token = verificationTokenService.createEmailVerificationToken(testAuthUser);
        token.setExpiryDate(LocalDateTime.now().minusDays(1));
        verificationTokenRepository.save(token);

        boolean isValid = verificationTokenService.isTokenValid(token.getToken());

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_withInvalidToken_returnsFalse() {
        boolean isValid = verificationTokenService.isTokenValid("invalid-token");

        assertFalse(isValid);
    }

    @Test
    void invalidateToken_marksTokenAsUsed() {
        VerificationToken token = verificationTokenService.createEmailVerificationToken(testAuthUser);

        verificationTokenService.invalidateToken(token.getToken());

        VerificationToken updatedToken = verificationTokenRepository.findById(token.getId()).orElseThrow();
        assertTrue(updatedToken.getUsed());
    }

    @Test
    void invalidateToken_withInvalidToken_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () ->
                verificationTokenService.invalidateToken("invalid-token")
        );
    }

    @Test
    void deleteExpiredTokens_deletesOnlyExpiredTokens() {
        // Crear token válido
        VerificationToken validToken = verificationTokenService.createEmailVerificationToken(testAuthUser);

        // Crear token expirado
        VerificationToken expiredToken = VerificationToken.builder()
                .authUser(testAuthUser)
                .tokenType(VerificationToken.TokenType.PASSWORD_RESET)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .used(false)
                .build();
        expiredToken = verificationTokenRepository.save(expiredToken);

        verificationTokenService.deleteExpiredTokens();

        assertTrue(verificationTokenRepository.existsById(validToken.getId()));
        assertFalse(verificationTokenRepository.existsById(expiredToken.getId()));
    }

    @Test
    void deleteUserTokens_deletesAllUserTokens() {
        // Crear token de verificación de email
        VerificationToken emailToken = verificationTokenService.createEmailVerificationToken(testAuthUser);

        // Crear token de reset de password manualmente para que no elimine el anterior
        VerificationToken resetToken = VerificationToken.builder()
                .authUser(testAuthUser)
                .tokenType(VerificationToken.TokenType.PASSWORD_RESET)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .used(false)
                .build();
        verificationTokenRepository.save(resetToken);

        assertEquals(2, verificationTokenRepository.findAll().size());

        verificationTokenService.deleteUserTokens(testAuthUser);

        assertEquals(0, verificationTokenRepository.findAll().size());
    }
}