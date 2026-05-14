package com.votify.frontend.dto;

// DTO de frontend con el límite máximo de equipos votables.
public class VoteSettingsResponse {
    private int maxTeamsToVote;
    private String juryVotingMode;

    // Constructor vacío requerido por Jackson.
    public VoteSettingsResponse() {
    }

    // Devuelve el máximo de equipos que se pueden votar.
    public int getMaxTeamsToVote() {
        return maxTeamsToVote;
    }

    // Actualiza el máximo de equipos que se pueden votar.
    public void setMaxTeamsToVote(int maxTeamsToVote) {
        this.maxTeamsToVote = maxTeamsToVote;
    }

    public String getJuryVotingMode() {
        return juryVotingMode;
    }

    public void setJuryVotingMode(String juryVotingMode) {
        this.juryVotingMode = juryVotingMode;
    }
}
