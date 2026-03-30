package com.votify.backend.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// DTO de entrada para registrar votos.
public record VoteRequest(
        @NotEmpty(message = "Debes seleccionar al menos un participante")
        List<String> selections
) {
}
