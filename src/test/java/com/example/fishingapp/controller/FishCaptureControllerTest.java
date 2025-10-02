package com.example.fishingapp.controller;

import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.service.FishCaptureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FishCaptureController.class)
@AutoConfigureMockMvc(addFilters = false)
class FishCaptureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FishCaptureService fishCaptureService;

    @Test
    void createCapture_returnsCapture() throws Exception {
        Long userId = 1L;
        FishCaptureDto captureDto = new FishCaptureDto(null, userId, "Trucha", 2.5F,
                LocalDate.of(2025,9,25),"Rio Tajo", LocalDateTime.now());

        FishCaptureDto savedDto = new FishCaptureDto(1L, userId, "Trucha", 2.5F,
                LocalDate.of(2025,9,25),"Rio Tajo", LocalDateTime.now());

        when(fishCaptureService.createFishCapture(any(FishCaptureDto.class), eq(userId))).thenReturn(savedDto);

        mockMvc.perform(post("/api/fishCaptures/create/{username}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(captureDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userId", is(userId.intValue())))
                .andExpect(jsonPath("$.fishType", is("Trucha")))
                .andExpect(jsonPath("$.weight", is(2.5)))
                .andExpect(jsonPath("$.location", is("Rio Tajo")));
    }

    @Test
    void createCapture_throwsResourceNotFound() throws Exception {
        Long userId = 1L;
        FishCaptureDto captureDto = new FishCaptureDto(null, userId, "Trucha", 2.5F,
                LocalDate.of(2025,9,25),"Rio Tajo", LocalDateTime.now());

        when(fishCaptureService.createFishCapture(any(FishCaptureDto.class), eq(userId)))
                .thenThrow(new ResourceNotFoundException("User","id",userId.toString()));

        mockMvc.perform(post("/api/fishCaptures/create/{username}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(captureDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with id : '1'"))
                .andExpect(jsonPath("$.path").value("/api/fishCaptures/create/1"));
    }

    @Test
    void findById_returnsCapture_whenExists() throws Exception {
        Long captureId = 1L;
        Long userId = 1L;

        FishCaptureDto savedDto = new FishCaptureDto(
                captureId,
                userId,
                "Trucha",
                2.5F,
                LocalDate.of(2025, 9, 25),
                "Rio Tajo",
                LocalDateTime.now()
        );

        when(fishCaptureService.findById(captureId)).thenReturn(savedDto);

        mockMvc.perform(post("/api/fishCaptures/findId/{idFishCapture}", captureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(captureId.intValue())))
                .andExpect(jsonPath("$.userId", is(userId.intValue())))
                .andExpect(jsonPath("$.fishType", is("Trucha")))
                .andExpect(jsonPath("$.weight", is(2.5)))
                .andExpect(jsonPath("$.location", is("Rio Tajo")));

        verify(fishCaptureService).findById(captureId);
    }


    @Test
    void findById_throwsResourceNotFound() throws Exception {
        Long captureId = 1L;
        when(fishCaptureService.findById(captureId))
                .thenThrow(new ResourceNotFoundException("FishCapture","id",captureId.toString()));

        mockMvc.perform(post("/api/fishCaptures/findId/{idFishCapture}", captureId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("FishCapture not found with id : '1'"))
                .andExpect(jsonPath("$.path").value("/api/fishCaptures/findId/1"));
    }

    @Test
    void getFishCaptureByUsername_returnsCaptures() throws Exception {
        String username = "ImaHer";
        Long userId = 1L;

        List<FishCaptureDto> captures = List.of(
                new FishCaptureDto(1L, userId, "Trucha", 2.5F,
                        LocalDate.of(2025, 9, 25), "Rio Tajo", LocalDateTime.now()),
                new FishCaptureDto(2L, userId, "Carpa", 1.8F,
                        LocalDate.of(2025, 9, 20), "Lago Grande", LocalDateTime.now())
        );

        when(fishCaptureService.getAllFishCapturesByUsername(username)).thenReturn(captures);

        mockMvc.perform(get("/api/fishCaptures/findUsername/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].fishType", is("Trucha")))
                .andExpect(jsonPath("$[0].location", is("Rio Tajo")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].fishType", is("Carpa")))
                .andExpect(jsonPath("$[1].location", is("Lago Grande")));

        verify(fishCaptureService).getAllFishCapturesByUsername(username);
    }


    @Test
    void getFishCaptureByUsername_throwsResourceNotFound() throws Exception {
        String username = "nonexistent";
        when(fishCaptureService.getAllFishCapturesByUsername(username))
                .thenThrow(new ResourceNotFoundException("User","username",username));

        mockMvc.perform(get("/api/fishCaptures/findUsername/{username}", username))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with username : 'nonexistent'"))
                .andExpect(jsonPath("$.path").value("/api/fishCaptures/findUsername/nonexistent"));
    }

    @Test
    void getAllCaptures_returnsAllFishCaptures() throws Exception {
        Long userId = 1L;

        List<FishCaptureDto> captures = List.of(
                new FishCaptureDto(1L, userId, "Trucha", 2.5F,
                        LocalDate.of(2025, 9, 25), "Rio Tajo", LocalDateTime.now()),
                new FishCaptureDto(2L, userId, "Carpa", 1.8F,
                        LocalDate.of(2025, 9, 20), "Lago Grande", LocalDateTime.now())
        );

        when(fishCaptureService.getAllFishCapture()).thenReturn(captures);

        mockMvc.perform(get("/api/fishCaptures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].fishType", is("Trucha")))
                .andExpect(jsonPath("$[0].location", is("Rio Tajo")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].fishType", is("Carpa")))
                .andExpect(jsonPath("$[1].location", is("Lago Grande")));

        verify(fishCaptureService).getAllFishCapture();
    }


    @Test
    void getAllCaptures_throwsException() throws Exception {
        when(fishCaptureService.getAllFishCapture())
                .thenThrow(new RuntimeException("Error interno"));

        mockMvc.perform(get("/api/fishCaptures"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Error interno"))
                .andExpect(jsonPath("$.path").value("/api/fishCaptures"));
    }

    @Test
    void updateFishCapture_returnsUpdatedCapture() throws Exception {
        Long userId = 1L;

        FishCaptureDto inputDto = new FishCaptureDto(
                1L, userId, "Trucha", 2.5F,
                LocalDate.of(2025, 9, 25), "Rio Tajo", LocalDateTime.now()
        );

        FishCaptureDto updatedDto = new FishCaptureDto(
                1L, userId, "Carpa", 3.0F,
                LocalDate.of(2025, 9, 26), "Lago Grande", LocalDateTime.now()
        );

        when(fishCaptureService.updateFishCaptureDto(any(FishCaptureDto.class), anyLong()))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/fishCaptures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fishType", is("Carpa")))
                .andExpect(jsonPath("$.weight", is(3.0)))
                .andExpect(jsonPath("$.location", is("Lago Grande")));

        verify(fishCaptureService).updateFishCaptureDto(any(FishCaptureDto.class), anyLong());
    }


    @Test
    void updateFishCapture_throwsResourceNotFound() throws Exception {
        FishCaptureDto captureDto = new FishCaptureDto(1L, 1L, "Trucha", 2.5F,
                LocalDate.of(2025,9,25),"Rio Tajo", LocalDateTime.now());

        when(fishCaptureService.updateFishCaptureDto(any(FishCaptureDto.class), anyLong()))
                .thenThrow(new ResourceNotFoundException("FishCapture","id","1"));

        mockMvc.perform(put("/api/fishCaptures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(captureDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("FishCapture not found with id : '1'"))
                .andExpect(jsonPath("$.path").value("/api/fishCaptures"));
    }

    @Test
    void deleteFishCapture_returnsOkMessage() throws Exception {
        Long captureId = 1L;
        Long userId = 1L;

        FishCaptureDto captureDto = new FishCaptureDto(
                captureId,
                1L,
                "Trucha",
                2.5F,
                LocalDate.of(2025, 9, 25),
                "Rio Tajo",
                LocalDateTime.now()
        );

        // Mock: el servicio encuentra la captura
        when(fishCaptureService.findById(captureId)).thenReturn(captureDto);

        // Mock: el delete no devuelve nada
        doNothing().when(fishCaptureService).deleteFishCaptureDto(captureId, userId);

        // Ejecutamos DELETE
        mockMvc.perform(delete("/api/fishCaptures/{idFishCapture}", captureId))
                .andExpect(status().isOk())
                .andExpect(content().string("Captura borrada: " + captureDto.toString()));

        // Verificamos que los métodos fueron llamados
        verify(fishCaptureService).findById(captureId);
        verify(fishCaptureService).deleteFishCaptureDto(captureId, userId);
    }

    @Test
    void deleteFishCapture_throwsResourceNotFound() throws Exception {
        Long captureId = 1L;
        Long userId = 1L;
        when(fishCaptureService.findById(captureId))
                .thenThrow(new ResourceNotFoundException("FishCapture","id",captureId.toString()));

        mockMvc.perform(delete("/api/fishCaptures/{idFishCapture}", captureId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("FishCapture not found with id : '1'"))
                .andExpect(jsonPath("$.path").value("/api/fishCaptures/1"));

        verify(fishCaptureService, never()).deleteFishCaptureDto(captureId, userId);
    }
}
