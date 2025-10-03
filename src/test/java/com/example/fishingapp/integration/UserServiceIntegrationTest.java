package com.example.fishingapp.integration;

import com.example.fishingapp.config.NoSecurityTestConfig;
import com.example.fishingapp.dto.UserDto;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.repository.UserRepository;
import com.example.fishingapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
//@Import(NoSecurityTestConfig.class)
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FishCaptureRepository fishCaptureRepository;

    private User usuarioAna;

    @BeforeEach
    void setUp() {
        // Limpiar capturas primero para evitar error de FK
        fishCaptureRepository.deleteAll();

        // Limpiar usuarios
        userRepository.deleteAll();

        // Insertar usuario inicial usando directamente el repositorio
        usuarioAna = userRepository.save(User.builder()
                .username("ana")
                .fullName("Ana García")
                .email("ana@example.com")
                .build());
    }

    // ===== CASO OK: Buscar usuario por username =====
    @Test
    void testFindByUsername_ok() {
        UserDto encontrado = userService.findByUsername("ana");
        assertThat(encontrado.fullName(), is(equalTo("Ana García")));
    }

    // ===== CASO KO: Buscar usuario que no existe =====
    @Test
    void testFindByUsername_ko_noExiste() {
        assertThrows(ResourceNotFoundException.class, () -> userService.findByUsername("noExiste"));
    }

    // ===== CASO OK: Eliminar usuario =====
    @Test
    void testDeleteUser_ok() {
        userService.deleteUser("ana");
        assertThat(userRepository.findByUsername("ana").isEmpty(), is(true));
    }

    // ===== CASO KO: Eliminar usuario que no existe =====
    @Test
    void testDeleteUser_ko_noExiste() {
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser("noExiste"));
    }
}