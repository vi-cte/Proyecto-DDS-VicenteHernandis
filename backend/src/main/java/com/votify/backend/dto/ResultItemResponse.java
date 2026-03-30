package com.votify.backend.dto;

// DTO de salida para un item del ranking de resultados.
public record ResultItemResponse(
        String teamName,
        long votes
) {
}
