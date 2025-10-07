package com.example.fishingapp.controller;

import com.example.fishingapp.dto.auth.*;
import com.example.fishingapp.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void register_returnsCreatedAuthResponse() throws Exception {
        RegisterRequest request = new RegisterRequest("ImaHer","Imanol Hernandez", "imanol@prueba.com", "password123");
        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        Mockito.when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", is("access-token")))
                .andExpect(jsonPath("$.refreshToken", is("refresh-token")));
    }

    @Test
    void login_returnsOkAuthResponse() throws Exception {
        LoginRequest request = new LoginRequest("imanol@prueba.com", "password123");
        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        Mockito.when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("access-token")))
                .andExpect(jsonPath("$.refreshToken", is("refresh-token")));
    }

    @Test
    void refreshToken_returnsOkAuthResponse() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        AuthResponse response = AuthResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("refresh-token")
                .build();

        Mockito.when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("new-access-token")))
                .andExpect(jsonPath("$.refreshToken", is("refresh-token")));
    }

    @Test
    void logout_returnsOkMessageResponse() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Sesión cerrada correctamente")))
                .andExpect(jsonPath("$.success", is(true)));

        Mockito.verify(authService).revokeRefreshToken("refresh-token");
    }

    @Test
    void logoutAll_returnsOkMessageResponse() throws Exception {
        String email = "imanol@prueba.com";

        mockMvc.perform(post("/api/auth/logout-all")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Todas las sesiones han sido cerradas")))
                .andExpect(jsonPath("$.success", is(true)));

        Mockito.verify(authService).revokeAllUserTokens(email);
    }

    @Test
    void health_returnsOkMessageResponse() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Auth service is running")))
                .andExpect(jsonPath("$.success", is(true)));
    }
}
