package com.example.fishingapp.integration;

import com.example.fishingapp.config.NoSecurityTestConfig;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.UserRepository;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Transactional
@Import(NoSecurityTestConfig.class)
class UserIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User usuarioJuan;
    private User usuarioMaria;

    @BeforeEach
    void setUp() {
        // Usuarios de ejemplo
        usuarioJuan = userRepository.save(new User(null, "juanperez", "Juan Pérez", "juan@example.com"));
        usuarioMaria = userRepository.save(new User(null, "mariagarcia", "María García", "maria@example.com"));
    }

    // ===== CASO OK: Buscar usuario existente =====
    @Test
    void testBuscarUsuarioPorUsername_OK() {
        User encontrado = userRepository.findByUsername("juanperez").orElse(null);
        assertThat(encontrado, notNullValue());
        assertThat(encontrado.getFullName(), equalTo("Juan Pérez"));
    }

    // ===== CASO KO: Buscar usuario inexistente =====
    @Test
    void testBuscarUsuarioPorUsername_KO() {
        User encontrado = userRepository.findByUsername("noexiste").orElse(null);
        assertThat(encontrado, nullValue());
    }

    // ===== CASO OK: Listar todos los usuarios =====
    @Test
    void testListarTodosUsuarios() {
        List<User> usuarios = userRepository.findAll();
        assertThat(usuarios, hasSize(12)); // ahora mismo solo hay 2 usuarios en setup
        assertThat(
                usuarios.stream().map(User::getUsername).toList(),
                containsInAnyOrder("juanperez", "mariagarcia", "luisg", "marial", "javierp", "anator", "carlosf",
                        "lucias", "davidm", "sofiar", "miguelan", "elenaj")
        );
    }

    // ===== CASO OK: Crear nuevo usuario =====
    @Test
    void testCrearNuevoUsuario() {
        User nuevo = new User(null, "carlossanchez", "Carlos Sánchez", "carlos@example.com");
        User guardado = userRepository.save(nuevo);

        assertThat(guardado.getId(), notNullValue());
        assertThat(userRepository.findAll(), hasSize(13)); // ahora 3 usuarios
    }

    // ===== CASO KO: Crear usuario con username duplicado =====
    @Test
    void testCrearUsuarioConUsernameDuplicado_KO() {
        User duplicado = new User(null, "juanperez", "Juanito Pérez", "juanito@example.com");
        try {
            userRepository.saveAndFlush(duplicado);
        } catch (Exception e) {
            assertThat(e, instanceOf(PersistenceException.class));
        }
    }
}
