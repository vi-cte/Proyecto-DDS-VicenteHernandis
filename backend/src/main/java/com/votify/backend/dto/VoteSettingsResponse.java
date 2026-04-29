package com.votify.backend.dto;

// Configuración necesaria para limitar el número de equipos votables.
public record VoteSettingsResponse(int maxTeamsToVote) {
}
