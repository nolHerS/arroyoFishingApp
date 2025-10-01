package com.example.fishingapp.repository;

import com.example.fishingapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email); // ← Añade este si no lo tienes

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
