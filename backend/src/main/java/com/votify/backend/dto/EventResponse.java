package com.votify.backend.dto;

import com.votify.backend.entity.EventPhase;

import java.time.LocalDate;

// Evento disponible para pantallas publicas de inscripcion, votacion y resultados.
public record EventResponse(
        Long id,
        String name,
        LocalDate eventDate,
        String description,
        boolean registrationsOpen,
        boolean votingOpen,
        boolean resultsVisible,
        int maxTeamsToVote,
        boolean juryEnabled,
        EventPhase phase,
        boolean active
) {
}
