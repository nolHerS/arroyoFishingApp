package com.example.fishingapp.repository;

import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FishCaptureRepositoryTest {

    @Autowired
    private FishCaptureRepository fishCaptureRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByUser: devuelve capturas de un usuario existente")
    void testFindByUserExists() {
        User user = User.builder()
                .username("miguel88")
                .fullName("Miguel Hernández")
                .email("miguel.hernandez@example.com")
                .build();
        userRepository.save(user);

        FishCapture capture1 = FishCapture.builder()
                .captureDate(LocalDate.of(2025, 9, 25))
                .createdAt(LocalDateTime.now())
                .fishType("Trucha")
                .location("Río Ebro")
                .weight(2.3f)
                .user(user)
                .build();

        FishCapture capture2 = FishCapture.builder()
                .captureDate(LocalDate.of(2025, 9, 26))
                .createdAt(LocalDateTime.now())
                .fishType("Black Bass")
                .location("Embalse de Mequinenza")
                .weight(1.9f)
                .user(user)
                .build();

        fishCaptureRepository.save(capture1);
        fishCaptureRepository.save(capture2);

        List<FishCapture> captures = fishCaptureRepository.findByUser(user);

        assertThat(captures, hasSize(2));
        assertThat(
                captures.stream().map(FishCapture::getFishType).toList(),
                containsInAnyOrder("Trucha", "Black Bass")
        );
    }

    @Test
    @DisplayName("findByUser: devuelve vacío si el usuario no tiene capturas")
    void testFindByUserEmpty() {
        User user = User.builder()
                .username("laura_p")
                .fullName("Laura Pérez")
                .email("laura.perez@example.com")
                .build();
        userRepository.save(user);

        List<FishCapture> captures = fishCaptureRepository.findByUser(user);
        assertThat(captures, empty());
    }

    @Test
    @DisplayName("save: guarda una captura correctamente")
    void testSaveFishCapture() {
        User user = User.builder()
                .username("carlos_s")
                .fullName("Carlos Sánchez")
                .email("carlos.sanchez@example.com")
                .build();
        userRepository.save(user);

        FishCapture capture = FishCapture.builder()
                .captureDate(LocalDate.of(2025, 9, 27))
                .createdAt(LocalDateTime.now())
                .fishType("Carpa")
                .location("Laguna de El Portillo")
                .weight(3.5f)
                .user(user)
                .build();

        FishCapture saved = fishCaptureRepository.save(capture);

        assertThat(saved.getId(), notNullValue());
        assertThat(saved.getFishType(), equalTo("Carpa"));
    }

    @Test
    @DisplayName("delete: elimina una captura existente")
    void testDeleteFishCapture() {
        User user = User.builder()
                .username("sofia_r")
                .fullName("Sofía Rodríguez")
                .email("sofia.rodriguez@example.com")
                .build();
        userRepository.save(user);

        FishCapture capture = FishCapture.builder()
                .captureDate(LocalDate.of(2025, 9, 28))
                .createdAt(LocalDateTime.now())
                .fishType("Lucio")
                .location("Río Tajo")
                .weight(4.2f)
                .user(user)
                .build();
        fishCaptureRepository.save(capture);

        long countBefore = fishCaptureRepository.count();
        fishCaptureRepository.delete(capture);
        assertThat(fishCaptureRepository.count(), equalTo(countBefore - 1L));
    }
}
