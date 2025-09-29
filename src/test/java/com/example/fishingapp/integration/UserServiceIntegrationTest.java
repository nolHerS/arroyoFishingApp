package com.example.fishingapp.integration;

import com.example.fishingapp.dto.UserDto;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.exception.UsernameAlreadyExistsException;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.repository.UserRepository;
import com.example.fishingapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FishCaptureRepository fishCaptureRepository;

    private UserDto usuarioAna;

    @BeforeEach
    void setUp() {
        // Limpiar capturas primero para evitar error de FK
        fishCaptureRepository.deleteAll();

        // Limpiar usuarios
        userRepository.deleteAll();

        // Insertar usuario inicial
        usuarioAna = new UserDto(null, "ana", "Ana García", "ana@example.com");
        userService.createUser(usuarioAna);
    }

    // ===== CASO OK: Crear usuario =====
    @Test
    void testCreateUser_ok() {
        UserDto nuevoUsuario = new UserDto(null, "juan", "Juan Pérez", "juan@example.com");

        UserDto creado = userService.createUser(nuevoUsuario);

        assertThat(creado.id(), is(notNullValue()));
        assertThat(creado.username(), is(equalTo("juan")));
        assertThat(creado.fullName(), is(equalTo("Juan Pérez")));
        assertThat(creado.email(), is(equalTo("juan@example.com")));
    }

    // ===== CASO KO: Crear usuario con username existente =====
    @Test
    void testCreateUser_ko_usernameExistente() {
        UserDto duplicado = new UserDto(null, "ana", "Ana García 2", "ana2@example.com");

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.createUser(duplicado));
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
