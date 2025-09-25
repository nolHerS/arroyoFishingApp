package com.example.fishingapp.controller;

import com.example.fishingapp.dto.UserDto;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.exception.UsernameAlreadyExistsException;
import com.example.fishingapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(UserController.class)
@ExtendWith(SpringExtension.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void createUser_returnsCreatedUser() throws Exception {

        UserDto userDto = new UserDto(1L,"ImaHer","Imanol Hernandez","imanol@prueba.com");

        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": 1,
                                    "username": "ImaHer",
                                    "fullName": "Imanol Hernandez",
                                    "email": "imanol@prueba.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("ImaHer")))
                .andExpect(jsonPath("$.fullName", is("Imanol Hernandez")))
                .andExpect(jsonPath("$.email", is("imanol@prueba.com")));
    }

    @Test
    void createUser_throwsUsernameAlreadyExists() throws Exception {
        UserDto userDto = new UserDto(1L,"ImaHer","Imanol Hernandez","imanol@prueba.com");

        when(userService.createUser(any(UserDto.class)))
                .thenThrow(new UsernameAlreadyExistsException("Username Already Exists For User ImaHer"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username Already Exists For User ImaHer"))
                .andExpect(jsonPath("$.path").value("/api/users"));
    }

    @Test
    void findUserByUsername_returnsUser() throws Exception {
        UserDto userDto = new UserDto(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");

        when(userService.findByUsername("ImaHer")).thenReturn(userDto);

        mockMvc.perform(get("/api/users/username/ImaHer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("ImaHer")));
    }

    @Test
    void findUserByUsername_throwsResourceNotFound() throws Exception {
        String username = "nonexistent";

        when(userService.findByUsername(username))
                .thenThrow(new ResourceNotFoundException("User", "username", username));

        mockMvc.perform(get("/api/users/username/{username}", username))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("User not found with username")))
                .andExpect(jsonPath("$.path").value("/api/users/username/" + username));
    }

    @Test
    void findAllUsers_returnsList() throws Exception {
        List<UserDto> users = List.of(
                new UserDto(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com"),
                new UserDto(2L, "TestUser", "Test Nombre", "test@prueba.com")
        );

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));
    }

    @Test
    void findAllUsers_throwsException() throws Exception {
        when(userService.getAllUsers())
                .thenThrow(new RuntimeException("Error interno"));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Error interno"))
                .andExpect(jsonPath("$.path").value("/api/users"));
    }


    @Test
    void findUserById_returnsUser_whenExists() throws Exception {
        // Arrange
        UserDto userDto = new UserDto(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");

        when(userService.findById(1L)).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/api/users/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("ImaHer"))
                .andExpect(jsonPath("$.fullName").value("Imanol Hernandez"))
                .andExpect(jsonPath("$.email").value("imanol@prueba.com"));

        verify(userService).findById(1L);
    }

    @Test
    void findUserById_throwsResourceNotFound() throws Exception {
        Long idUser = 1L;

        when(userService.findById(idUser))
                .thenThrow(new ResourceNotFoundException("User", "id", idUser.toString()));

        mockMvc.perform(get("/api/users/id/{idUser}", idUser))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("User not found with id")))
                .andExpect(jsonPath("$.path").value("/api/users/id/" + idUser));
    }

    @Test
    void updateUser_returnsUpdatedUser() throws Exception {
        UserDto updatedUser = new UserDto(1L, "ImaHer", "Nombre Actualizado", "nuevo@prueba.com");

        when(userService.updateUserDto(any(UserDto.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "username": "ImaHer",
                                  "fullName": "Nombre Actualizado",
                                  "email": "nuevo@prueba.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Nombre Actualizado")))
                .andExpect(jsonPath("$.email", is("nuevo@prueba.com")));
    }

    @Test
    void updateUser_throwsResourceNotFound() throws Exception {
        UserDto userDto = new UserDto(1L,"ImaHer","Nombre Actualizado","nuevo@prueba.com");

        when(userService.updateUserDto(any(UserDto.class)))
                .thenThrow(new ResourceNotFoundException("User", "id", "1"));

        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("User not found with id")))
                .andExpect(jsonPath("$.path").value("/api/users"));
    }


    @Test
    void deleteUser_returnsOkMessage() throws Exception {
        UserDto userDto = new UserDto(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");

        when(userService.findByUsername("ImaHer")).thenReturn(userDto);
        doNothing().when(userService).deleteUser("ImaHer");

        mockMvc.perform(delete("/api/users/ImaHer"))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuario: " + userDto.toString() + " borrado."));
    }

    @Test
    void deleteUser_throwsResourceNotFound() throws Exception {
        String usernameUser = "nonexistent";

        when(userService.findByUsername(usernameUser))
                .thenThrow(new ResourceNotFoundException("User", "username", usernameUser));

        mockMvc.perform(delete("/api/users/{usernameUser}", usernameUser))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("User not found with username")))
                .andExpect(jsonPath("$.path").value("/api/users/" + usernameUser));

        // Verificamos que no se llama al método delete si el usuario no existe
        verify(userService, never()).deleteUser(usernameUser);
    }
}
