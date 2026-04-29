package com.votify.backend.repository;

// Proyeccion del conteo de votos por equipo.
public interface VoteTallyProjection {
    // Devuelve el nombre del equipo agrupado.
    String getTeamName();

    // Devuelve el total de votos del equipo agrupado.
    Long getVotes();
}
