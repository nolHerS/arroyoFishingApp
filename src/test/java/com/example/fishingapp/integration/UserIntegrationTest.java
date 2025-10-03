package com.example.fishingapp.integration;

import com.example.fishingapp.config.NoSecurityTestConfig;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Transactional
//@Import(NoSecurityTestConfig.class)
class UserIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User usuarioJuan;
    private User usuarioMaria;

    @BeforeEach
    void setUp() {
        // Usuarios de ejemplo - Corrección: estabas asignando dos veces a usuarioJuan
        usuarioJuan = userRepository.save(User.builder()
                .username("juanperez")
                .fullName("Juan Pérez")  // Corregido el nombre para que coincida con el test
                .email("juan@example.com")
                .build());

        usuarioMaria = userRepository.save(User.builder()
                .username("mariagarcia")
                .fullName("María García")
                .email("maria@example.com")
                .build());
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
        assertThat(usuarios, hasSize(2)); // Cambiado de 12 a 2, ya que solo creas 2 en setUp
        assertThat(
                usuarios.stream().map(User::getUsername).toList(),
                containsInAnyOrder("juanperez", "mariagarcia")
        );
    }
}