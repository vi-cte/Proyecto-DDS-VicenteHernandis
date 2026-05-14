package com.votify.backend.dto;

import com.votify.backend.entity.JuryVotingMode;

// Configuración necesaria para renderizar la experiencia de votación.
public record VoteSettingsResponse(
        int maxTeamsToVote,
        JuryVotingMode juryVotingMode
) {
}
