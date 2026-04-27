package com.votify.frontend.dto;

public class EventSettingsResponse {
    private boolean registrationsOpen;
    private boolean votingOpen;
    private boolean resultsVisible;
    private int maxTeamsToVote;

    public EventSettingsResponse() {}

    public EventSettingsResponse(boolean registrationsOpen, boolean votingOpen, boolean resultsVisible, int maxTeamsToVote) {
        this.registrationsOpen = registrationsOpen;
        this.votingOpen = votingOpen;
        this.resultsVisible = resultsVisible;
        this.maxTeamsToVote = maxTeamsToVote;
    }

    public boolean isRegistrationsOpen() { return registrationsOpen; }
    public void setRegistrationsOpen(boolean registrationsOpen) { this.registrationsOpen = registrationsOpen; }

    public boolean isVotingOpen() { return votingOpen; }
    public void setVotingOpen(boolean votingOpen) { this.votingOpen = votingOpen; }

    public boolean isResultsVisible() { return resultsVisible; }
    public void setResultsVisible(boolean resultsVisible) { this.resultsVisible = resultsVisible; }

    public int getMaxTeamsToVote() { return maxTeamsToVote; }
    public void setMaxTeamsToVote(int maxTeamsToVote) { this.maxTeamsToVote = maxTeamsToVote; }
}