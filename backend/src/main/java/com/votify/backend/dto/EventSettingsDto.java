package com.votify.backend.dto;

// Configuración pública y administrativa del estado del evento.
public record EventSettingsDto(
        boolean registrationsOpen,
        boolean votingOpen,
        boolean resultsVisible,
        int maxTeamsToVote
) {}
