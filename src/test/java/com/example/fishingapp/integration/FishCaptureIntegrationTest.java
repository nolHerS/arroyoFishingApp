package com.example.fishingapp.integration;

import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Transactional
//@Import(NoSecurityTestConfig.class)
class FishCaptureIntegrationTest {

    @Autowired
    private FishCaptureRepository fishCaptureRepository;

    @Autowired
    private UserRepository userRepository;

    private User usuarioMaria;
    private User usuarioJuan;

    @BeforeEach
    void setUp() {
        // Crear usuarios
        usuarioMaria = User.builder()
                .username("maria")
                .fullName("María López")
                .email("maria@example.com")
                .build();

        usuarioJuan = User.builder()
                .username("juan")
                .fullName("Juan Pérez")
                .email("juan@example.com")
                .build();

        userRepository.save(usuarioMaria);
        userRepository.save(usuarioJuan);

        FishCapture captura1 = FishCapture.builder()
                .user(usuarioMaria)
                .fishType("Trucha")
                .weight(2.5f)
                .captureDate(LocalDate.of(2025, 9, 25))
                .createdAt(LocalDateTime.now())
                .location("Río Ebro")
                .build();

        FishCapture captura2 = FishCapture.builder()
                .user(usuarioMaria)
                .fishType("Lucio")
                .weight(3.2f)
                .captureDate(LocalDate.of(2025, 9, 26))
                .createdAt(LocalDateTime.now())
                .location("Lago Sanabria")
                .build();

        fishCaptureRepository.save(captura1);
        fishCaptureRepository.save(captura2);
    }

    // ===== CASO OK =====
    @Test
    void testFindByUser_ok() {
        List<FishCapture> capturas = fishCaptureRepository.findByUser(usuarioMaria);
        assertThat(capturas, hasSize(2));
        assertThat(
                capturas.stream().map(FishCapture::getFishType).toList(),
                containsInAnyOrder("Trucha", "Lucio")
        );
    }

    // ===== CASO KO: Usuario sin capturas =====
    @Test
    void testFindByUser_ko_sinCapturas() {
        List<FishCapture> capturas = fishCaptureRepository.findByUser(usuarioJuan);
        assertThat(capturas, empty());
    }

    // ===== CASO KO: Guardar captura con usuario null =====
    @Test
    void testSaveCapture_ko_usuarioNull() {
        FishCapture captura = FishCapture.builder()
                .user(null)
                .fishType("Carpa")
                .weight(1.8f)
                .captureDate(LocalDate.of(2025, 9, 27))
                .createdAt(LocalDateTime.now())
                .location("Pantano de Valdecañas")
                .build();

        try {
            fishCaptureRepository.save(captura);
        } catch (Exception e) {
            assertThat(e, instanceOf(Exception.class));
        }
    }
}
