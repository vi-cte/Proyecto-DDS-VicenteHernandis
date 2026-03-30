package com.votify.frontend.dto;

public class ResultItemResponse {
    private String teamName;
    private long votes;

    public ResultItemResponse() {
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }
}
