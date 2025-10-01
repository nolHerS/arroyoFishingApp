package com.example.fishingapp.repository;

import com.example.fishingapp.security.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    void deleteByAuthUserId(Long authUserId);

    // Limpiar tokens expirados (útil para tareas programadas)
    void deleteByExpiryDateBefore(LocalDateTime date);
}
