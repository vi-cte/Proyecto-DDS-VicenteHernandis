package com.votify.frontend.dto;

public class VoteSettingsResponse {
    private int maxTeamsToVote;

    public VoteSettingsResponse() {
    }

    public int getMaxTeamsToVote() {
        return maxTeamsToVote;
    }

    public void setMaxTeamsToVote(int maxTeamsToVote) {
        this.maxTeamsToVote = maxTeamsToVote;
    }
}
