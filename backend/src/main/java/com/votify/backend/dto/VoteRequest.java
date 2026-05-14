package com.votify.backend.dto;

import java.util.List;

// DTO de entrada para registrar votos.
public record VoteRequest(
        List<String> selections,
        List<VoteSelectionRequest> selectionEntries,
        String juryWinnerSelection,
        String juryTechnicalSelection,
        String juryWinnerComment,
        String juryTechnicalComment,
        String juryTeamSelection,
        List<JuryCriterionScoreRequest> juryCriteriaScores,
        String juryTeamComment
) {
    // Constructor de compatibilidad para votos públicos existentes.
    public VoteRequest(List<String> selections) {
        this(selections, List.of(), null, null, null, null, null, List.of(), null);
    }

    // Constructor de compatibilidad para votos simples del jurado.
    public VoteRequest(String juryWinnerSelection, String juryTechnicalSelection) {
        this(List.of(), List.of(), juryWinnerSelection, juryTechnicalSelection, null, null, null, List.of(), null);
    }
}
