package com.votify.frontend.dto;

// Comentario mostrado al propietario de un equipo en resultados.
public class TeamCommentResponse {
    private String authorLabel;
    private String comment;
    private String createdAt;

    public TeamCommentResponse() {
    }

    public String getAuthorLabel() {
        return authorLabel;
    }

    public void setAuthorLabel(String authorLabel) {
        this.authorLabel = authorLabel;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
