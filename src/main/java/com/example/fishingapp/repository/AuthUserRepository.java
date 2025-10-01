package com.example.fishingapp.repository;

import com.example.fishingapp.security.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByEmail(String email);

    boolean existsByEmail(String email);

    // Para verificar si existe un username en la tabla User relacionada
    boolean existsByUserUsername(String username);
}
