package com.votify.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

// DTO de entrada para crear un participante.
public record ParticipantRequest(
        @NotBlank(message = "El nombre del equipo es obligatorio")
        String teamName,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo no valido")
        String email,
        String phone,
        String description,
        String logo,
        List<String> members
) {
}
