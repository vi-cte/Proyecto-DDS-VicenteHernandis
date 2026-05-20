package com.votify.frontend.dto;

// Petición de creación de evento desde administración.
public class AdminEventRequest {
    public String name;
    public String eventDate;
    public String description;
    public boolean registrationsOpen;
    public boolean votingOpen;
    public boolean resultsVisible;
    public int maxTeamsToVote;
    public boolean juryEnabled;
    public String juryVotingMode;
    public String phase;

    public AdminEventRequest(String name, String eventDate, String description, boolean registrationsOpen, boolean votingOpen, boolean resultsVisible, int maxTeamsToVote, boolean juryEnabled, String juryVotingMode, String phase) {
        this.name = name;
        this.eventDate = eventDate;
        this.description = description;
        this.registrationsOpen = registrationsOpen;
        this.votingOpen = votingOpen;
        this.resultsVisible = resultsVisible;
        this.maxTeamsToVote = maxTeamsToVote;
        this.juryEnabled = juryEnabled;
        this.juryVotingMode = juryVotingMode;
        this.phase = phase;
    }
}
