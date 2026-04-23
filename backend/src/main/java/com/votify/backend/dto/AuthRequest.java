package com.votify.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo no válido")
        String email,
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}