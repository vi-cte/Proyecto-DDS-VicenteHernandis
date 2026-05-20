package com.votify.frontend.dto;

import java.util.List;

// Evaluacion multicriterio completa de un equipo por parte del jurado.
public class JuryTeamEvaluationRequest {
    private String teamName;
    private List<JuryCriterionScoreRequest> criteriaScores;
    private String comment;

    public JuryTeamEvaluationRequest() {
    }

    public JuryTeamEvaluationRequest(String teamName, List<JuryCriterionScoreRequest> criteriaScores, String comment) {
        this.teamName = teamName;
        this.criteriaScores = criteriaScores;
        this.comment = comment;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public List<JuryCriterionScoreRequest> getCriteriaScores() {
        return criteriaScores;
    }

    public void setCriteriaScores(List<JuryCriterionScoreRequest> criteriaScores) {
        this.criteriaScores = criteriaScores;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
