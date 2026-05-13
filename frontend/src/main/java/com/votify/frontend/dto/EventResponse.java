package com.votify.frontend.dto;

// Evento seleccionable desde pantallas de usuario.
public class EventResponse {
    private Long id;
    private String name;
    private String eventDate;
    private String description;
    private boolean registrationsOpen;
    private boolean votingOpen;
    private boolean resultsVisible;
    private int maxTeamsToVote;
    private boolean juryEnabled;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isRegistrationsOpen() { return registrationsOpen; }
    public void setRegistrationsOpen(boolean registrationsOpen) { this.registrationsOpen = registrationsOpen; }
    public boolean isVotingOpen() { return votingOpen; }
    public void setVotingOpen(boolean votingOpen) { this.votingOpen = votingOpen; }
    public boolean isResultsVisible() { return resultsVisible; }
    public void setResultsVisible(boolean resultsVisible) { this.resultsVisible = resultsVisible; }
    public int getMaxTeamsToVote() { return maxTeamsToVote; }
    public void setMaxTeamsToVote(int maxTeamsToVote) { this.maxTeamsToVote = maxTeamsToVote; }
    public boolean isJuryEnabled() { return juryEnabled; }
    public void setJuryEnabled(boolean juryEnabled) { this.juryEnabled = juryEnabled; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        if (eventDate == null || eventDate.isBlank()) {
            return name == null ? "Evento" : name;
        }
        return (name == null ? "Evento" : name) + " · " + eventDate;
    }
}
