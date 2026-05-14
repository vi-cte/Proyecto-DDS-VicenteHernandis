package com.votify.frontend.dto;

// Selección de voto con comentario opcional por equipo.
public class VoteSelectionRequest {
    private String teamName;
    private String comment;

    public VoteSelectionRequest() {
    }

    public VoteSelectionRequest(String teamName, String comment) {
        this.teamName = teamName;
        this.comment = comment;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
