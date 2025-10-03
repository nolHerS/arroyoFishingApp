package com.example.fishingapp.controller;

import com.example.fishingapp.config.NoSecurityTestConfig;
import com.example.fishingapp.dto.UserDto;
import com.example.fishingapp.model.User;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
//@Import(NoSecurityTestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .build());
    }

    @Test
    void findAllUsers_returnsList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    void findAllUsers_throwsException() throws Exception {
        userRepository.deleteAll();

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findUserById_returnsUser_whenExists() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void findUserById_throwsResourceNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findUserByUsername_returnsUser() throws Exception {
        mockMvc.perform(get("/api/users/username/" + testUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void findUserByUsername_throwsResourceNotFound() throws Exception {
        mockMvc.perform(get("/api/users/username/noexiste"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_returnsUpdatedUser() throws Exception {
        UserDto updatedDto = new UserDto(
                testUser.getId(),
                "updateduser",
                "Updated User",
                "updated@example.com"
        );

        mockMvc.perform(put("/api/users/" + testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.fullName").value("Updated User"));
    }

    @Test
    void updateUser_throwsResourceNotFound() throws Exception {
        UserDto dto = new UserDto(999L, "test", "Test", "test@test.com");

        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_returnsOkMessage() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getUsername()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_throwsResourceNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/noexiste"))
                .andExpect(status().isNotFound());
    }
}