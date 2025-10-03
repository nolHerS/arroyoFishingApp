package com.example.fishingapp.controller;

import com.example.fishingapp.config.NoSecurityTestConfig;
import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
//@Import(NoSecurityTestConfig.class)
class FishCaptureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FishCaptureRepository fishCaptureRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        fishCaptureRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .build());
    }

    @Test
    void createCapture_returnsCapture() throws Exception {
        FishCaptureDto dto = new FishCaptureDto(
                null,
                testUser.getId(),
                "Trucha",
                2.5f,
                LocalDate.of(2025, 9, 25),
                "Rio Tajo",
                LocalDateTime.now()
        );

        mockMvc.perform(post("/api/fish-captures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fishType").value("Trucha"))
                .andExpect(jsonPath("$.weight").value(2.5))
                .andExpect(jsonPath("$.location").value("Rio Tajo"));
    }

    @Test
    void createCapture_throwsResourceNotFound() throws Exception {
        FishCaptureDto dto = new FishCaptureDto(
                null,
                999L, // Usuario inexistente
                "Trucha",
                2.5f,
                LocalDate.of(2025, 9, 25),
                "Rio Tajo",
                LocalDateTime.now()
        );

        mockMvc.perform(post("/api/fish-captures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_returnsCapture_whenExists() throws Exception {
        // Crear una captura primero
        FishCaptureDto dto = new FishCaptureDto(
                null,
                testUser.getId(),
                "Trucha",
                2.5f,
                LocalDate.of(2025, 9, 25),
                "Rio Tajo",
                LocalDateTime.now()
        );

        String response = mockMvc.perform(post("/api/fish-captures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        FishCaptureDto created = objectMapper.readValue(response, FishCaptureDto.class);

        // Buscar por ID
        mockMvc.perform(get("/api/fish-captures/" + created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id()))
                .andExpect(jsonPath("$.fishType").value("Trucha"));
    }

    @Test
    void findById_throwsResourceNotFound() throws Exception {
        mockMvc.perform(get("/api/fish-captures/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCaptures_returnsAllFishCaptures() throws Exception {
        // Crear dos capturas
        FishCaptureDto dto1 = new FishCaptureDto(
                null, testUser.getId(), "Trucha", 2.5f,
                LocalDate.of(2025, 9, 25), "Rio Tajo", LocalDateTime.now()
        );
        FishCaptureDto dto2 = new FishCaptureDto(
                null, testUser.getId(), "Lucio", 3.0f,
                LocalDate.of(2025, 9, 26), "Lago", LocalDateTime.now()
        );

        mockMvc.perform(post("/api/fish-captures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/fish-captures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        // Obtener todas
        mockMvc.perform(get("/api/fish-captures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllCaptures_throwsException() throws Exception {
        // Con la BD vacía
        mockMvc.perform(get("/api/fish-captures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getFishCaptureByUsername_returnsCaptures() throws Exception {
        FishCaptureDto dto = new FishCaptureDto(
                null, testUser.getId(), "Trucha", 2.5f,
                LocalDate.of(2025, 9, 25), "Rio Tajo", LocalDateTime.now()
        );

        mockMvc.perform(post("/api/fish-captures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/fish-captures/user/" + testUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fishType").value("Trucha"));
    }

    @Test
    void getFishCaptureByUsername_throwsResourceNotFound() throws Exception {
        mockMvc.perform(get("/api/fish-captures/user/noexiste"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateFishCapture_returnsUpdatedCapture() throws Exception {
        // Crear captura
        FishCaptureDto dto = new FishCaptureDto(
                null, testUser.getId(), "Trucha", 2.5f,
                LocalDate.of(2025, 9, 25), "Rio Tajo", LocalDateTime.now()
        );

        String response = mockMvc.perform(post("/api/fish-captures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        FishCaptureDto created = objectMapper.readValue(response, FishCaptureDto.class);

        // Actualizar
        FishCaptureDto updated = new FishCaptureDto(
                created.id(), testUser.getId(), "Lucio", 3.0f,
                LocalDate.of(2025, 9, 26), "Lago", created.createdAt()
        );

        mockMvc.perform(put("/api/fish-captures/" + created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fishType").value("Lucio"))
                .andExpect(jsonPath("$.weight").value(3.0));
    }

    @Test
    void updateFishCapture_throwsResourceNotFound() throws Exception {
        FishCaptureDto dto = new FishCaptureDto(
                999L, testUser.getId(), "Lucio", 3.0f,
                LocalDate.of(2025, 9, 26), "Lago", LocalDateTime.now()
        );

        mockMvc.perform(put("/api/fish-captures/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteFishCapture_returnsOkMessage() throws Exception {
        // Crear captura
        FishCaptureDto dto = new FishCaptureDto(
                null, testUser.getId(), "Trucha", 2.5f,
                LocalDate.of(2025, 9, 25), "Rio Tajo", LocalDateTime.now()
        );

        String response = mockMvc.perform(post("/api/fish-captures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        FishCaptureDto created = objectMapper.readValue(response, FishCaptureDto.class);

        // Eliminar
        mockMvc.perform(delete("/api/fish-captures/" + created.id()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFishCapture_throwsResourceNotFound() throws Exception {
        mockMvc.perform(delete("/api/fish-captures/999"))
                .andExpect(status().isNotFound());
    }
}