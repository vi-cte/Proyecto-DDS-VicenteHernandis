package com.votify.backend.dto;

import com.votify.backend.entity.JuryVotingMode;

import java.time.LocalDate;
import java.util.List;

// Resumen administrativo de un evento.
public record AdminEventResponse(
        Long id,
        String name,
        LocalDate eventDate,
        String description,
        boolean registrationsOpen,
        boolean votingOpen,
        boolean resultsVisible,
        int maxTeamsToVote,
        boolean juryEnabled,
        JuryVotingMode juryVotingMode,
        boolean active,
        long totalVotes,
        long participants,
        List<ResultItemResponse> ranking
) {
}
