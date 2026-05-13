package com.votify.backend.dto;

import java.time.LocalDate;

// Petición para crear un evento desde administración.
public record AdminEventRequest(
        String name,
        LocalDate eventDate,
        String description,
        boolean registrationsOpen,
        boolean votingOpen,
        boolean resultsVisible,
        int maxTeamsToVote,
        boolean juryEnabled
) {
}
