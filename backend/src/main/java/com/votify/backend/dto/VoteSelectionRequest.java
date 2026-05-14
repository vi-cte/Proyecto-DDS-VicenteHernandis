package com.votify.backend.dto;

// Seleccion individual de voto con comentario opcional.
public record VoteSelectionRequest(
        String teamName,
        String comment
) {
}
