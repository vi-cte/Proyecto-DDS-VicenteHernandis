package com.votify.backend.dto;

import com.votify.backend.entity.JuryVotingMode;

// Configuración pública y administrativa del estado del evento.
public record EventSettingsDto(
        boolean registrationsOpen,
        boolean votingOpen,
        boolean resultsVisible,
        int maxTeamsToVote,
        JuryVotingMode juryVotingMode
) {}
