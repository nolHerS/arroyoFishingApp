package com.example.fishingapp.repository;

import com.example.fishingapp.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByUsername: devuelve usuario existente")
    void testFindByUsernameExists() {
        User user = User.builder()
                .username("maria89")
                .fullName("María López")
                .email("maria.lopez@example.com")
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("maria89");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("maria89");
    }

    @Test
    @DisplayName("findByUsername: devuelve vacío si no existe")
    void testFindByUsernameNotFound() {
        Optional<User> found = userRepository.findByUsername("noexiste");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll: devuelve todos los usuarios")
    void testFindAllUsers() {
        User user1 = User.builder().username("carlos21").fullName("Carlos Sánchez").email("carlos.sanchez@example.com").build();
        User user2 = User.builder().username("laura_p").fullName("Laura Pérez").email("laura.perez@example.com").build();
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername).containsExactlyInAnyOrder("carlos21", "laura_p");
    }

    @Test
    @DisplayName("save: guarda un usuario correctamente")
    void testSaveUser() {
        User user = User.builder()
                .username("antonio_m")
                .fullName("Antonio Martínez")
                .email("antonio.martinez@example.com")
                .build();
        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("antonio_m");
    }

    @Test
    @DisplayName("delete: elimina un usuario existente")
    void testDeleteUser() {
        User user = User.builder()
                .username("sofia_r")
                .fullName("Sofía Rodríguez")
                .email("sofia.rodriguez@example.com")
                .build();
        userRepository.save(user);

        long countBefore = userRepository.count();
        userRepository.delete(user);
        assertThat(userRepository.count()).isEqualTo(countBefore - 1);
    }

    @Test
    @DisplayName("deleteById: no falla si el id no existe")
    void testDeleteByIdNotFound() {
        long countBefore = userRepository.count();
        userRepository.deleteById(999L);
        assertThat(userRepository.count()).isEqualTo(countBefore);
    }

}
