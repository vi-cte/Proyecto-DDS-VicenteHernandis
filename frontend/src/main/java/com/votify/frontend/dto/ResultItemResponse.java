package com.votify.frontend.dto;

// DTO de frontend con el resultado de votos de un equipo.
public class ResultItemResponse {
    private String teamName;
    private long votes;

    // Constructor vacío requerido por Jackson.
    public ResultItemResponse() {
    }

    // Devuelve el nombre del equipo.
    public String getTeamName() {
        return teamName;
    }

    // Actualiza el nombre del equipo.
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    // Devuelve el número de votos del equipo.
    public long getVotes() {
        return votes;
    }

    // Actualiza el número de votos del equipo.
    public void setVotes(long votes) {
        this.votes = votes;
    }
}
