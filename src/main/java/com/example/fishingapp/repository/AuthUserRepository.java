package com.example.fishingapp.repository;

import com.example.fishingapp.security.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByUsername(String username);

    Optional<AuthUser> findByEmail(String email);

    boolean existsByUsername(String username);

    // Añade este método
    @Query("SELECT au FROM AuthUser au WHERE au.username = :identifier OR au.email = :identifier")
    Optional<AuthUser> findByUsernameOrEmail(@Param("identifier") String identifier);

    // Para verificar si existe un username en la tabla User relacionada
    boolean existsByUserUsername(String username);
}
