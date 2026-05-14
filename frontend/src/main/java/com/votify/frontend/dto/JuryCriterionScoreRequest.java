package com.votify.frontend.dto;

// Puntuacion de un criterio en la votacion multicriterio del jurado.
public class JuryCriterionScoreRequest {
    private String criterion;
    private Integer score;

    public JuryCriterionScoreRequest() {
    }

    public JuryCriterionScoreRequest(String criterion, Integer score) {
        this.criterion = criterion;
        this.score = score;
    }

    public String getCriterion() {
        return criterion;
    }

    public void setCriterion(String criterion) {
        this.criterion = criterion;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
