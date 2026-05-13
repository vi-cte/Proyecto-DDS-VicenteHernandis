package com.votify.backend.dto;

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
        boolean active,
        long totalVotes,
        long participants,
        List<ResultItemResponse> ranking
) {
}
