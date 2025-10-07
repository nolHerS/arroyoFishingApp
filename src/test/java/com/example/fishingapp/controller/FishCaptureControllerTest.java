package com.example.fishingapp.controller;

import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.AuthUserRepository;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.repository.UserRepository;
import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.security.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class FishCaptureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private FishCaptureRepository fishCaptureRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        fishCaptureRepository.deleteAll();
        authUserRepository.deleteAll();
        userRepository.deleteAll();

        // Primero creamos el User
        testUser = User.builder()
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .build();

        testUser = userRepository.save(testUser);

        // Luego creamos el AuthUser vinculado
        AuthUser testAuthUser = AuthUser.builder()
                .username(testUser.getUsername())
                .email(testUser.getEmail())
                .password("$2a$10$dummypasswordhash")
                .role(Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .createdAt(LocalDateTime.now())
                .user(testUser)
                .build();

        testAuthUser = authUserRepository.save(testAuthUser);

        // Configurar el SecurityContext con el AuthUser
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        testAuthUser,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + testAuthUser.getRole().name()))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
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
    void createCapture_withValidData_ignoresUserIdFromDto() throws Exception {
        // Este test verifica que el userId del DTO es ignorado
        // y se usa siempre el del usuario autenticado
        FishCaptureDto dto = new FishCaptureDto(
                null,
                999L, // Este ID será ignorado
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
                .andExpect(jsonPath("$.userId").value(testUser.getId())) // Verifica que usa el ID correcto
                .andExpect(jsonPath("$.fishType").value("Trucha"));
    }

    @Test
    void findById_returnsCapture_whenExists() throws Exception {
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

        mockMvc.perform(get("/api/fish-captures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllCaptures_throwsException() throws Exception {
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

        mockMvc.perform(delete("/api/fish-captures/" + created.id()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFishCapture_throwsResourceNotFound() throws Exception {
        mockMvc.perform(delete("/api/fish-captures/999"))
                .andExpect(status().isNotFound());
    }
}