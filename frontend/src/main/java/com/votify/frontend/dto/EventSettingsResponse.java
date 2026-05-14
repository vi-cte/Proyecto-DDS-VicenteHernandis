package com.votify.frontend.dto;

// DTO de frontend con la configuración actual del evento.
public class EventSettingsResponse {
    private boolean registrationsOpen;
    private boolean votingOpen;
    private boolean resultsVisible;
    private int maxTeamsToVote;
    private String juryVotingMode;

    // Constructor vacío requerido por Jackson.
    public EventSettingsResponse() {}

    // Crea una respuesta de ajustes con todos sus campos.
    public EventSettingsResponse(boolean registrationsOpen, boolean votingOpen, boolean resultsVisible, int maxTeamsToVote, String juryVotingMode) {
        this.registrationsOpen = registrationsOpen;
        this.votingOpen = votingOpen;
        this.resultsVisible = resultsVisible;
        this.maxTeamsToVote = maxTeamsToVote;
        this.juryVotingMode = juryVotingMode;
    }

    // Indica si las inscripciones están abiertas.
    public boolean isRegistrationsOpen() { return registrationsOpen; }
    // Actualiza si las inscripciones están abiertas.
    public void setRegistrationsOpen(boolean registrationsOpen) { this.registrationsOpen = registrationsOpen; }

    // Indica si las votaciones están abiertas.
    public boolean isVotingOpen() { return votingOpen; }
    // Actualiza si las votaciones están abiertas.
    public void setVotingOpen(boolean votingOpen) { this.votingOpen = votingOpen; }

    // Indica si los resultados son visibles.
    public boolean isResultsVisible() { return resultsVisible; }
    // Actualiza si los resultados son visibles.
    public void setResultsVisible(boolean resultsVisible) { this.resultsVisible = resultsVisible; }

    // Devuelve el máximo de equipos votables.
    public int getMaxTeamsToVote() { return maxTeamsToVote; }
    // Actualiza el máximo de equipos votables.
    public void setMaxTeamsToVote(int maxTeamsToVote) { this.maxTeamsToVote = maxTeamsToVote; }

    // Devuelve el modo de votacion del jurado.
    public String getJuryVotingMode() { return juryVotingMode; }
    // Actualiza el modo de votacion del jurado.
    public void setJuryVotingMode(String juryVotingMode) { this.juryVotingMode = juryVotingMode; }
}
