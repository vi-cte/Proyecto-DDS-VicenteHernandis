package com.votify.backend.dto;

import java.util.List;

// Evaluacion multicriterio completa de un equipo por parte del jurado.
public record JuryTeamEvaluationRequest(
        String teamName,
        List<JuryCriterionScoreRequest> criteriaScores,
        String comment
) {
}
