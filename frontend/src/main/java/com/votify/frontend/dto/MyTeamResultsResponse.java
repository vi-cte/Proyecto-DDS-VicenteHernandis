package com.votify.frontend.dto;

import java.util.List;

// Resumen privado del equipo del usuario autenticado.
public class MyTeamResultsResponse {
    private String teamName;
    private long votes;
    private int position;
    private long commentsCount;
    private List<TeamCommentResponse> comments;

    public MyTeamResultsResponse() {
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }

    public List<TeamCommentResponse> getComments() {
        return comments;
    }

    public void setComments(List<TeamCommentResponse> comments) {
        this.comments = comments;
    }
}
