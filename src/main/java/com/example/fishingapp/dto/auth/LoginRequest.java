package com.example.fishingapp.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El email o usuario es obligatorio")
    @Email(message = "El email o usuario debe ser válido")
    private String identifier;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
