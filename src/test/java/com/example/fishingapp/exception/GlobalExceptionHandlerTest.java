package com.example.fishingapp.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleResourceNotFound_returnsNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso no encontrado");
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Recurso no encontrado", response.getBody().getMessage());
    }

    @Test
    void handleUsernameAlreadyExists_returnsConflict() {
        UsernameAlreadyExistsException ex = new UsernameAlreadyExistsException("Usuario ya existe");
        ResponseEntity<ErrorResponse> response = handler.handleUsernameConflict(ex, request);

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Usuario ya existe", response.getBody().getMessage());
    }

    @Test
    void handleBadCredentials_returnsUnauthorized() {
        BadCredentialsException ex = new BadCredentialsException("Credenciales inválidas");
        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex, request);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Email o contraseña incorrectos", response.getBody().getMessage());
    }

    @Test
    void handleUsernameNotFound_returnsNotFound() {
        UsernameNotFoundException ex = new UsernameNotFoundException("Usuario no encontrado");
        ResponseEntity<ErrorResponse> response = handler.handleUsernameNotFound(ex, request);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Usuario no encontrado", response.getBody().getMessage());
    }

    @Test
    void handleIllegalState_returnsBadRequest() {
        IllegalStateException ex = new IllegalStateException("Token inválido");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalState(ex, request);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Token inválido", response.getBody().getMessage());
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        Exception ex = new Exception("Error desconocido");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Error desconocido", response.getBody().getMessage());
    }

    @Test
    void handleValidationExceptions_returnsBadRequestWithFieldErrors() throws Exception {
        // Creamos un FieldError simulado
        FieldError fieldError1 = new FieldError("userDto", "username", "Username no puede estar vacío");
        FieldError fieldError2 = new FieldError("userDto", "email", "Email inválido");

        // Mock del BindingResult
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Creamos la excepción simulada
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // Ejecutamos el handler
        var response = handler.handleValidationExceptions(exception, request);

        // Verificaciones
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Username no puede estar vacío; Email inválido", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());

        // Verificamos que getFieldErrors se llamó
        verify(bindingResult).getFieldErrors();
    }
}
