package com.example.fishingapp.integration;

import com.example.fishingapp.config.NoSecurityTestConfig;
import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.UserRepository;
import com.example.fishingapp.service.FishCaptureService;
import com.example.fishingapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Transactional
@Import(NoSecurityTestConfig.class)
class FishCaptureServiceIntegrationTest {

    @Autowired
    private FishCaptureService fishCaptureService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User usuarioAna;
    private User usuarioCarlos;

    @BeforeEach
    void setUp() {
        // Crear usuarios
        usuarioAna = userRepository.save(User.builder()
                .username("ana")
                .fullName("Ana García")
                .email("ana@example.com")
                .build());

        usuarioCarlos = userRepository.save(User.builder()
                .username("carlos")
                .fullName("Carlos Ruiz")
                .email("carlos@example.com")
                .build());

        // Crear capturas iniciales para Ana
        FishCaptureDto captura1 = new FishCaptureDto(
                null,
                usuarioAna.getId(),
                "Trucha",
                2.5f,
                LocalDate.of(2025, 9, 25),
                "Río Ebro",
                LocalDateTime.now()
        );

        FishCaptureDto captura2 = new FishCaptureDto(
                null,
                usuarioAna.getId(),
                "Lucio",
                3.2f,
                LocalDate.of(2025, 9, 26),
                "Lago Sanabria",
                LocalDateTime.now()
        );

        fishCaptureService.createFishCapture(captura1, usuarioAna.getId());
        fishCaptureService.createFishCapture(captura2, usuarioAna.getId());
    }

    // ===== CASO OK =====
    @Test
    void testGetAllFishCapturesByUsername_ok() {
        List<FishCaptureDto> capturas = fishCaptureService.getAllFishCapturesByUsername("ana");

        assertThat(capturas, hasSize(2));
        assertThat(capturas.stream().allMatch(c -> c.userId().equals(usuarioAna.getId())), is(true));
        assertThat(capturas.stream().map(FishCaptureDto::fishType).toList(),
                containsInAnyOrder("Trucha", "Lucio"));
    }

    // ===== CASO KO =====
    @Test
    void testGetAllFishCapturesByUsername_ko_sinCapturas() {
        List<FishCaptureDto> capturas = fishCaptureService.getAllFishCapturesByUsername("carlos");

        assertThat(capturas, is(empty()));
    }

    // ===== CASO OK =====
    @Test
    void testCreateFishCapture_ok() {
        FishCaptureDto nuevaCaptura = new FishCaptureDto(
                null,
                usuarioCarlos.getId(),
                "Carpa",
                1.8f,
                LocalDate.of(2025, 9, 27),
                "Pantano de Valdecañas",
                LocalDateTime.now()
        );

        FishCaptureDto creada = fishCaptureService.createFishCapture(nuevaCaptura, usuarioCarlos.getId());

        assertThat(creada.id(), notNullValue());
        assertThat(creada.userId(), equalTo(usuarioCarlos.getId()));
        assertThat(creada.fishType(), equalTo("Carpa"));
    }

    // ===== CASO KO =====
    @Test
    void testCreateFishCapture_ko_usuarioInexistente() {
        FishCaptureDto captura = new FishCaptureDto(
                null,
                999L, // ID que no existe
                "Salmón",
                2.0f,
                LocalDate.of(2025, 9, 28),
                "Río Duero",
                LocalDateTime.now()
        );

        try {
            fishCaptureService.createFishCapture(captura, 999L);
        } catch (Exception e) {
            assertThat(e, instanceOf(Exception.class));
        }
    }
}
