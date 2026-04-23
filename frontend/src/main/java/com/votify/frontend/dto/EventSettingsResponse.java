package com.votify.frontend.dto;

public class EventSettingsResponse {
    private boolean registrationsOpen;
    private boolean votingOpen;
    private int maxTeamsToVote;

    public EventSettingsResponse() {}

    public EventSettingsResponse(boolean registrationsOpen, boolean votingOpen, int maxTeamsToVote) {
        this.registrationsOpen = registrationsOpen;
        this.votingOpen = votingOpen;
        this.maxTeamsToVote = maxTeamsToVote;
    }

    public boolean isRegistrationsOpen() { return registrationsOpen; }
    public void setRegistrationsOpen(boolean registrationsOpen) { this.registrationsOpen = registrationsOpen; }

    public boolean isVotingOpen() { return votingOpen; }
    public void setVotingOpen(boolean votingOpen) { this.votingOpen = votingOpen; }

    public int getMaxTeamsToVote() { return maxTeamsToVote; }
    public void setMaxTeamsToVote(int maxTeamsToVote) { this.maxTeamsToVote = maxTeamsToVote; }
}