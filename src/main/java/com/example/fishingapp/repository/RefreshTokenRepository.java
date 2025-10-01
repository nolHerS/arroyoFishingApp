package com.example.fishingapp.repository;

import com.example.fishingapp.security.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByAuthUserId(Long authUserId);

    // Buscar todos los tokens de un usuario (por si quieres implementar "cerrar todas las sesiones")
    void deleteAllByAuthUserId(Long authUserId);

    // Limpiar tokens expirados o revocados
    void deleteByExpiryDateBeforeOrRevokedTrue(LocalDateTime date);
}