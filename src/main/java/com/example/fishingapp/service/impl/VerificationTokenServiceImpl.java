package com.example.fishingapp.service.impl;

import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.repository.VerificationTokenRepository;
import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.security.VerificationToken;
import com.example.fishingapp.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${verification.token.expiration:86400}") // 24 horas por defecto
    private long tokenExpirationSeconds;


    @Override
    @Transactional
    public VerificationToken createEmailVerificationToken(AuthUser authUser) {
        return createToken(authUser, VerificationToken.TokenType.EMAIL_VERIFICATION);
    }

    @Override
    @Transactional
    public VerificationToken createPasswordResetToken(AuthUser authUser) {
        return createToken(authUser, VerificationToken.TokenType.PASSWORD_RESET);
    }

    @Override
    @Transactional
    public AuthUser verifyToken(String token, VerificationToken.TokenType expectedType) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("VerificationToken", "Token: "+ token,"Token no encontrado"));

        // Verificar tipo de token
        if (verificationToken.getTokenType() != expectedType) {
            throw new IllegalArgumentException("Tipo de token incorrecto");
        }

        // Verificar si ya fue usado
        if (verificationToken.getUsed()) {
            throw new IllegalStateException("El token ya ha sido utilizado");
        }

        // Verificar si está expirado
        if (verificationToken.isExpired()) {
            verificationTokenRepository.delete(verificationToken);
            throw new IllegalStateException("El token ha expirado");
        }

        // Marcar como usado
        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        return verificationToken.getAuthUser();
    }


    @Override
    @Transactional(readOnly = true)
    public boolean isTokenValid(String token) {
        return verificationTokenRepository.findByToken(token)
                .map(t -> !t.getUsed() && !t.isExpired())
                .orElse(false);
    }

    @Override
    @Transactional
    public void invalidateToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("VerificationToken","Token: "+ token,"Token no encontrado"));

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        verificationTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteUserTokens(AuthUser authUser) {
        verificationTokenRepository.deleteByAuthUserId(authUser.getId());
    }

    /**
     * Método privado para crear tokens
     */
    private VerificationToken createToken(AuthUser authUser, VerificationToken.TokenType tokenType) {
        // Eliminar tokens previos del mismo tipo para este usuario
        verificationTokenRepository.deleteByAuthUserId(authUser.getId());

        // Crear nuevo token
        VerificationToken verificationToken = VerificationToken.builder()
                .authUser(authUser)
                .tokenType(tokenType)
                .expiryDate(LocalDateTime.now().plusSeconds(tokenExpirationSeconds))
                .used(false)
                .build();

        return verificationTokenRepository.save(verificationToken);
    }
}
