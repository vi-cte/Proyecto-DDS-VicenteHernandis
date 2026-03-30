package com.votify.backend.repository;

// Proyeccion del conteo de votos por equipo.
public interface VoteTallyProjection {
    String getTeamName();
    Long getVotes();
}
